package thaumcraft.common.tiles.crafting;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.EventHooks;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.api.aspects.IEssentiaTransport;
import thaumcraft.api.capabilities.ThaumcraftCapabilities;
import thaumcraft.api.crafting.CrucibleRecipe;
import thaumcraft.client.fx.ClientFX;
import thaumcraft.common.blocks.crafting.BlockThaumatorium;
import thaumcraft.common.blocks.devices.BlockBrainBox;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.lib.utils.InventoryUtils;
import thaumcraft.common.registry.TCBlockEntities;
import thaumcraft.common.registry.TCRecipes;
import thaumcraft.common.registry.TCTags;
import thaumcraft.common.tiles.TileThaumcraft;

/**
 * Port of TileThaumatorium: an automated crucible. A player picks up to maxRecipes crucible recipes for the
 * catalyst in its slot; while heated from below and not powered it pulls the essentia of the matching recipe
 * through tubes (suction 128) and ejects the result out of its front.
 * <p>
 * The original remembered recipes by CrucibleRecipe.hash; here they are the recipe ids of the datapack.
 */
public class TileThaumatorium extends TileThaumcraft implements WorldlyContainer, IAspectContainer, IEssentiaTransport {
    private static final int[] SLOTS = {0};

    private ItemStack catalyst = ItemStack.EMPTY;
    public AspectList essentia = new AspectList();
    public List<ResourceLocation> recipeHash = new ArrayList<>();
    public List<AspectList> recipeEssentia = new ArrayList<>();
    public List<String> recipePlayer = new ArrayList<>();
    public int currentCraft = -1;
    public int maxRecipes = 1;
    @Nullable
    public Aspect currentSuction = null;
    int venting = 0;
    int counter = 0;
    boolean heated = false;
    @Nullable
    CrucibleRecipe currentRecipe = null;
    /** The recipes the last player to look could pick for the catalyst, plus the ones already picked. */
    public List<RecipeHolder<CrucibleRecipe>> recipes = new ArrayList<>();

    public TileThaumatorium(BlockPos pos, BlockState state) {
        super(TCBlockEntities.THAUMATORIUM.get(), pos, state);
    }

    @Nullable
    public CrucibleRecipe getRecipe(ResourceLocation id) {
        if (level == null) return null;
        Optional<RecipeHolder<?>> h = level.getRecipeManager().byKey(id);
        return h.isPresent() && h.get().value() instanceof CrucibleRecipe cr ? cr : null;
    }

    @Override
    protected void readSyncNBT(CompoundTag tag, HolderLookup.Provider registries) {
        essentia = AspectList.CODEC.parse(NbtOps.INSTANCE, tag.get("Aspects")).result().orElseGet(AspectList::new);
        maxRecipes = tag.getByte("maxrec");
        recipeEssentia = new ArrayList<>();
        recipeHash = new ArrayList<>();
        recipePlayer = new ArrayList<>();
        ListTag ids = tag.getList("recipes", Tag.TAG_STRING);
        for (int a = 0; a < ids.size(); ++a) {
            ResourceLocation id = ResourceLocation.tryParse(ids.getString(a));
            CrucibleRecipe recipe = id != null ? getRecipe(id) : null;
            // The level is not set yet while a chunk loads: keep the id and fill the essentia in later.
            if (id != null && (recipe != null || level == null)) {
                recipeEssentia.add(recipe != null ? recipe.getAspects().copy() : new AspectList());
                recipePlayer.add("");
                recipeHash.add(id);
            }
        }
    }

    @Override
    protected void writeSyncNBT(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putByte("maxrec", (byte) maxRecipes);
        AspectList.CODEC.encodeStart(NbtOps.INSTANCE, essentia).result().ifPresent(t -> tag.put("Aspects", t));
        ListTag ids = new ListTag();
        for (ResourceLocation id : recipeHash) ids.add(StringTag.valueOf(id.toString()));
        tag.put("recipes", ids);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        catalyst = tag.contains("Item") ? ItemStack.parseOptional(registries, tag.getCompound("Item")) : ItemStack.EMPTY;
        ListTag players = tag.getList("OutputPlayer", Tag.TAG_STRING);
        for (int a = 0; a < players.size(); ++a) {
            if (recipePlayer.size() > a) {
                recipePlayer.set(a, players.getString(a));
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!catalyst.isEmpty()) tag.put("Item", catalyst.save(registries));
        ListTag players = new ListTag();
        for (String p : recipePlayer) {
            if (p != null) players.add(StringTag.valueOf(p));
        }
        tag.put("OutputPlayer", players);
    }

    /** Recipes saved before the recipe manager was reachable get their essentia now. */
    @Override
    public void onLoad() {
        super.onLoad();
        for (int a = recipeHash.size() - 1; a >= 0; --a) {
            CrucibleRecipe recipe = getRecipe(recipeHash.get(a));
            if (recipe == null) {
                recipeHash.remove(a);
                recipeEssentia.remove(a);
                recipePlayer.remove(a);
            } else {
                recipeEssentia.set(a, recipe.getAspects().copy());
            }
        }
    }

    /** Lava, fire, nitor or magma two blocks down (under the crucible). */
    boolean checkHeat() {
        BlockState bs = level.getBlockState(worldPosition.below(2));
        return bs.is(TCTags.CRUCIBLE_HEATERS) || ConfigBlocks.nitor.values().stream().anyMatch(n -> bs.is(n.get()));
    }

    public Direction getFacing() {
        BlockState bs = getBlockState();
        return bs.hasProperty(BlockThaumatorium.FACING) ? bs.getValue(BlockThaumatorium.FACING) : Direction.NORTH;
    }

    public ItemStack getCurrentOutputRecipe() {
        ItemStack out = ItemStack.EMPTY;
        if (currentCraft >= 0 && recipeHash.size() > currentCraft) {
            CrucibleRecipe recipe = getRecipe(recipeHash.get(currentCraft));
            if (recipe != null) {
                out = recipe.getRecipeOutput().copy();
            }
        }
        return out;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, TileThaumatorium tile) {
        tile.update();
    }

    private void update() {
        if (!level.isClientSide) {
            if (counter == 0 || counter % 40 == 0) {
                heated = checkHeat();
                getUpgrades();
            }
            ++counter;
            if (heated && !gettingPower() && counter % 5 == 0 && !recipeHash.isEmpty()) {
                if (catalyst.isEmpty()) {
                    currentSuction = null;
                    return;
                }
                if (currentCraft < 0 || currentCraft >= recipeHash.size() || currentRecipe == null || !currentRecipe.catalystMatches(catalyst)) {
                    for (int a = 0; a < recipeHash.size(); ++a) {
                        CrucibleRecipe recipe = getRecipe(recipeHash.get(a));
                        if (recipe != null && recipe.catalystMatches(catalyst)) {
                            currentCraft = a;
                            currentRecipe = recipe;
                            break;
                        }
                    }
                }
                if (currentCraft < 0 || currentCraft >= recipeHash.size()) {
                    return;
                }
                boolean done = true;
                currentSuction = null;
                for (Aspect aspect : recipeEssentia.get(currentCraft).getAspectsSortedByName()) {
                    if (essentia.getAmount(aspect) < recipeEssentia.get(currentCraft).getAmount(aspect)) {
                        currentSuction = aspect;
                        done = false;
                        break;
                    }
                }
                if (done) {
                    completeRecipe();
                } else if (currentSuction != null) {
                    fill();
                }
            }
        } else if (venting > 0) {
            --venting;
            float fx = 0.1f - level.random.nextFloat() * 0.2f;
            float fz = 0.1f - level.random.nextFloat() * 0.2f;
            float fy = 0.1f - level.random.nextFloat() * 0.2f;
            float fx2 = 0.1f - level.random.nextFloat() * 0.2f;
            float fz2 = 0.1f - level.random.nextFloat() * 0.2f;
            float fy2 = 0.1f - level.random.nextFloat() * 0.2f;
            Direction facing = getFacing();
            ClientFX.drawVentParticles(level, worldPosition.getX() + 0.5f + fx + facing.getStepX() / 2.0f, worldPosition.getY() + 0.5f + fy,
                    worldPosition.getZ() + 0.5f + fz + facing.getStepZ() / 2.0f, facing.getStepX() / 4.0f + fx2, fy2,
                    facing.getStepZ() / 4.0f + fz2, 0xFFFFFF);
        }
    }

    private void completeRecipe() {
        if (currentRecipe != null && currentCraft < recipeHash.size() && currentRecipe.matches(essentia, catalyst)) {
            ItemStack used = removeItem(0, 1);
            essentia = new AspectList();
            ItemStack dropped = getCurrentOutputRecipe();
            Player p = level.getServer() != null ? level.getServer().getPlayerList().getPlayerByName(recipePlayer.get(currentCraft)) : null;
            if (p != null) {
                // InventoryFake: the crafting grid of the event holds the catalyst.
                EventHooks.firePlayerCraftingEvent(p, dropped, new SimpleContainer(used));
            }
            InventoryUtils.ejectStackAt(level, worldPosition, getFacing(), dropped);
            level.playSound(null, worldPosition, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 0.25f,
                    2.6f + (level.random.nextFloat() - level.random.nextFloat()) * 0.8f);
            currentCraft = -1;
            syncTile(false);
            setChanged();
        }
    }

    void fill() {
        Direction facing = getFacing();
        for (int y = 0; y <= 1; ++y) {
            for (Direction dir : Direction.values()) {
                if (dir == facing || dir == Direction.DOWN || (y == 0 && dir == Direction.UP)) {
                    continue;
                }
                BlockEntity te = ThaumcraftApiHelper.getConnectableTile(level, worldPosition.above(y), dir);
                if (te instanceof IEssentiaTransport ic && ic.getEssentiaAmount(dir.getOpposite()) > 0
                        && ic.getSuctionAmount(dir.getOpposite()) < getSuctionAmount(null) && getSuctionAmount(null) >= ic.getMinimumSuction()) {
                    int ess = ic.takeEssentia(currentSuction, 1, dir.getOpposite());
                    if (ess > 0) {
                        addToContainer(currentSuction, ess);
                        return;
                    }
                }
            }
        }
    }

    /** Each mnemonic matrix on a side (not the front, not below) pointing at the thaumatorium adds two recipes. */
    public void getUpgrades() {
        Direction facing = getFacing();
        int mr = 1;
        for (int yy = 0; yy <= 1; ++yy) {
            for (Direction dir : Direction.values()) {
                if (dir == Direction.DOWN || dir == facing) {
                    continue;
                }
                BlockState bs = level.getBlockState(worldPosition.above(yy).relative(dir));
                if (bs.is(ConfigBlocks.brainBox.get()) && bs.getValue(BlockBrainBox.FACING) == dir.getOpposite()) {
                    mr += 2;
                }
            }
        }
        if (mr != maxRecipes) {
            maxRecipes = mr;
            while (recipeHash.size() > maxRecipes) {
                recipeHash.remove(recipeHash.size() - 1);
                recipeEssentia.remove(recipeEssentia.size() - 1);
                recipePlayer.remove(recipePlayer.size() - 1);
            }
            syncTile(false);
            setChanged();
        }
    }

    /** Recipes the player knows for the current catalyst, and the ones already picked, sorted by output name. */
    public void updateRecipes(Player player) {
        List<RecipeHolder<CrucibleRecipe>> recipesTemp = new ArrayList<>();
        if (!catalyst.isEmpty()) {
            for (RecipeHolder<CrucibleRecipe> h : level.getRecipeManager().getAllRecipesFor(TCRecipes.CRUCIBLE_TYPE.get())) {
                CrucibleRecipe creps = h.value();
                if ((ThaumcraftCapabilities.knowsResearchStrict(player, creps.getResearch()) && creps.catalystMatches(catalyst))
                        || recipeHash.contains(h.id())) {
                    recipesTemp.add(h);
                }
            }
        }
        recipesTemp.sort(Comparator.comparing(h -> h.value().getRecipeOutput().getHoverName().getString()));
        recipes = recipesTemp;
    }

    /** The picked recipes missing from the list first, then the list. */
    public List<ResourceLocation> generateRecipeHashlist() {
        List<ResourceLocation> hashList = new ArrayList<>();
        for (ResourceLocation hash : recipeHash) {
            if (recipes.stream().noneMatch(h -> h.id().equals(hash))) {
                hashList.add(hash);
            }
        }
        for (RecipeHolder<CrucibleRecipe> h : recipes) {
            hashList.add(h.id());
        }
        return hashList;
    }

    /** PacketSelectThaumotoriumRecipeToServer: picks a recipe, or drops it when already picked. */
    public void selectRecipe(Player player, ResourceLocation id) {
        int i = recipeHash.indexOf(id);
        boolean flag = false;
        if (i >= 0) {
            recipeEssentia.remove(i);
            recipePlayer.remove(i);
            recipeHash.remove(i);
            currentCraft = -1;
            flag = true;
        }
        if (!flag && recipeHash.size() < maxRecipes) {
            for (RecipeHolder<CrucibleRecipe> h : recipes) {
                if (h.id().equals(id)) {
                    recipeEssentia.add(h.value().getAspects().copy());
                    recipePlayer.add(player.getName().getString());
                    recipeHash.add(h.id());
                    flag = true;
                    break;
                }
            }
        }
        if (flag) {
            setChanged();
            syncTile(false);
        }
    }

    @Override
    public boolean gettingPower() {
        return level.hasNeighborSignal(worldPosition) || level.hasNeighborSignal(worldPosition.below()) || level.hasNeighborSignal(worldPosition.above());
    }

    @Override
    public boolean triggerEvent(int i, int j) {
        if (i >= 0) {
            if (level.isClientSide) {
                venting = 7;
            }
            return true;
        }
        return super.triggerEvent(i, j);
    }

    // ---- IAspectContainer ----

    @Override
    public int addToContainer(Aspect tt, int am) {
        if (currentRecipe == null) {
            return am;
        }
        int ce = currentRecipe.getAspects().getAmount(tt) - essentia.getAmount(tt);
        if (ce <= 0) {
            return am;
        }
        int add = Math.min(ce, am);
        essentia.add(tt, add);
        syncTile(false);
        setChanged();
        return am - add;
    }

    @Override
    public boolean takeFromContainer(Aspect tt, int am) {
        if (essentia.getAmount(tt) >= am) {
            essentia.remove(tt, am);
            syncTile(false);
            setChanged();
            return true;
        }
        return false;
    }

    @Override
    public boolean doesContainerContainAmount(Aspect tt, int am) {
        return essentia.getAmount(tt) >= am;
    }

    @Override
    public int containerContains(Aspect tt) {
        return essentia.getAmount(tt);
    }

    @Override
    public boolean doesContainerAccept(Aspect tag) {
        return true;
    }

    @Override
    public AspectList getAspects() {
        return essentia;
    }

    @Override
    public void setAspects(AspectList aspects) {
        essentia = aspects;
    }

    // ---- IEssentiaTransport ----

    @Override
    public boolean isConnectable(Direction face) {
        return face != getFacing();
    }

    @Override
    public boolean canInputFrom(Direction face) {
        return face != getFacing();
    }

    @Override
    public boolean canOutputTo(Direction face) {
        return false;
    }

    @Override
    public void setSuction(@Nullable Aspect aspect, int amount) {
        currentSuction = aspect;
    }

    @Override
    public Aspect getSuctionType(Direction face) {
        return currentSuction;
    }

    @Override
    public int getSuctionAmount(Direction face) {
        return currentSuction != null ? 128 : 0;
    }

    @Override
    public Aspect getEssentiaType(Direction face) {
        return null;
    }

    @Override
    public int getEssentiaAmount(Direction face) {
        return 0;
    }

    @Override
    public int takeEssentia(Aspect aspect, int amount, Direction face) {
        return canOutputTo(face) && takeFromContainer(aspect, amount) ? amount : 0;
    }

    @Override
    public int addEssentia(Aspect aspect, int amount, Direction face) {
        return canInputFrom(face) ? amount - addToContainer(aspect, amount) : 0;
    }

    @Override
    public int getMinimumSuction() {
        return 0;
    }

    // ---- inventory (TileThaumcraftInventory): the catalyst slot ----

    @Override
    public int[] getSlotsForFace(Direction side) {
        return SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction direction) {
        return true;
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return true;
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return catalyst.isEmpty();
    }

    @Override
    public ItemStack getItem(int index) {
        return index == 0 ? catalyst : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int index, int count) {
        if (index != 0) return ItemStack.EMPTY;
        ItemStack out = catalyst.split(count);
        if (!out.isEmpty()) setChanged();
        return out;
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        if (index != 0) return ItemStack.EMPTY;
        ItemStack ret = catalyst;
        catalyst = ItemStack.EMPTY;
        return ret;
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        if (index == 0) {
            catalyst = stack;
            setChanged();
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return net.minecraft.world.Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        catalyst = ItemStack.EMPTY;
    }
}
