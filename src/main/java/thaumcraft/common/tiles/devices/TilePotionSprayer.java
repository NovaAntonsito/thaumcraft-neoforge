package thaumcraft.common.tiles.devices;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectHelper;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.api.aspects.IEssentiaTransport;
import thaumcraft.client.fx.ClientFX;
import thaumcraft.common.blocks.devices.BlockPotionSprayer;
import thaumcraft.common.config.ConfigAspects;
import thaumcraft.common.registry.TCBlockEntities;
import thaumcraft.common.tiles.TileThaumcraft;

/**
 * Port of TilePotionSprayer. The potion in its slot sets a recipe (the aspects of its brewing ingredients, at most
 * 10 of each); the sprayer sucks those aspects in through tubes on any side but its face, and each full recipe is
 * one charge (up to 8). A redstone pulse sprays one charge on every living thing within a block of the spot two
 * blocks in front of it.
 */
public class TilePotionSprayer extends TileThaumcraft implements WorldlyContainer, IAspectContainer, IEssentiaTransport {
    private static final int[] SLOTS = {0};

    private ItemStack potion = ItemStack.EMPTY;
    public AspectList recipe = new AspectList();
    public AspectList recipeProgress = new AspectList();
    public int charges = 0;
    public int color = 0;
    int counter = 0;
    boolean activated = false;
    int venting = 0;
    @Nullable
    Aspect currentSuction = null;

    public TilePotionSprayer(BlockPos pos, BlockState state) {
        super(TCBlockEntities.POTION_SPRAYER.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, TilePotionSprayer tile) {
        tile.update(state);
    }

    private Direction facing() {
        return getBlockState().getValue(BlockPotionSprayer.FACING);
    }

    private void update(BlockState state) {
        ++counter;
        Direction facing = state.getValue(BlockPotionSprayer.FACING);
        if (!level.isClientSide) {
            if (counter % 5 == 0) {
                currentSuction = null;
                if (potion.isEmpty() || charges >= 8) {
                    return;
                }
                boolean done = true;
                for (Aspect aspect : recipe.getAspectsSortedByName()) {
                    if (recipeProgress.getAmount(aspect) < recipe.getAmount(aspect)) {
                        currentSuction = aspect;
                        done = false;
                        break;
                    }
                }
                if (done) {
                    recipeProgress = new AspectList();
                    ++charges;
                    syncTile(false);
                    setChanged();
                } else if (currentSuction != null) {
                    fill();
                }
            }
            if (!state.getValue(BlockPotionSprayer.ENABLED)) {
                if (!activated && charges > 0) {
                    --charges;
                    PotionContents contents = potion.get(DataComponents.POTION_CONTENTS);
                    if (contents != null) {
                        int area = 1;
                        BlockPos p = worldPosition.relative(facing, 2);
                        AABB box = new AABB(p.getX() - area, p.getY() - area, p.getZ() - area, p.getX() + 1 + area, p.getY() + 1 + area,
                                p.getZ() + 1 + area);
                        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box)) {
                            if (!e.isAlive() || !e.isAffectedByPotions()) {
                                continue;
                            }
                            for (MobEffectInstance effect : contents.getAllEffects()) {
                                Holder<MobEffect> holder = effect.getEffect();
                                if (holder.value().isInstantenous()) {
                                    holder.value().applyInstantenousEffect(null, null, e, effect.getAmplifier(), 1.0);
                                } else {
                                    e.addEffect(new MobEffectInstance(holder, effect.getDuration(), effect.getAmplifier()));
                                }
                            }
                        }
                    }
                    level.playSound(null, worldPosition, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 0.25f,
                            2.6f + (level.random.nextFloat() - level.random.nextFloat()) * 0.8f);
                    level.blockEvent(worldPosition, state.getBlock(), 0, 0);
                    syncTile(false);
                    setChanged();
                }
                activated = true;
            } else if (activated) {
                activated = false;
            }
        } else if (venting > 0) {
            --venting;
            for (int a = 0; a < venting / 2; ++a) {
                float fx = 0.1f - level.random.nextFloat() * 0.2f;
                float fz = 0.1f - level.random.nextFloat() * 0.2f;
                float fy = 0.1f - level.random.nextFloat() * 0.2f;
                float fx2 = (float) (level.random.nextGaussian() * 0.06);
                float fz2 = (float) (level.random.nextGaussian() * 0.06);
                float fy2 = (float) (level.random.nextGaussian() * 0.06);
                thaumcraft.client.fx.FXDispatcher.drawVentParticles2(worldPosition.getX() + 0.5f + fx + facing.getStepX() / 2.0f,
                        worldPosition.getY() + 0.5f + fy + facing.getStepY() / 2.0f, worldPosition.getZ() + 0.5f + fz + facing.getStepZ() / 2.0f,
                        fx2 + facing.getStepX() * 0.25, fy2 + facing.getStepY() * 0.25, fz2 + facing.getStepZ() * 0.25, color, 4.0f);
            }
        }
    }

    @Override
    public boolean triggerEvent(int i, int j) {
        if (i >= 0) {
            if (level.isClientSide) {
                venting = 15;
            }
            return true;
        }
        return super.triggerEvent(i, j);
    }

    @Override
    protected void readSyncNBT(CompoundTag tag, HolderLookup.Provider registries) {
        recipe = AspectList.CODEC.parse(NbtOps.INSTANCE, tag.get("recipe")).result().orElseGet(AspectList::new);
        recipeProgress = AspectList.CODEC.parse(NbtOps.INSTANCE, tag.get("progress")).result().orElseGet(AspectList::new);
        charges = tag.getInt("charges");
        color = tag.getInt("color");
        potion = tag.contains("Potion") ? ItemStack.parseOptional(registries, tag.getCompound("Potion")) : ItemStack.EMPTY;
    }

    @Override
    protected void writeSyncNBT(CompoundTag tag, HolderLookup.Provider registries) {
        AspectList.CODEC.encodeStart(NbtOps.INSTANCE, recipe).result().ifPresent(t -> tag.put("recipe", t));
        AspectList.CODEC.encodeStart(NbtOps.INSTANCE, recipeProgress).result().ifPresent(t -> tag.put("progress", t));
        tag.putInt("charges", charges);
        tag.putInt("color", color);
        if (!potion.isEmpty()) tag.put("Potion", potion.save(registries));
    }

    /** SlotPotion.isValidPotion: a drinkable, splash or lingering potion that does something. */
    public static boolean isValidPotion(ItemStack stack) {
        if (!stack.is(Items.POTION) && !stack.is(Items.SPLASH_POTION) && !stack.is(Items.LINGERING_POTION)) {
            return false;
        }
        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
        if (contents == null || contents.potion().isEmpty()) {
            return false;
        }
        Holder<Potion> p = contents.potion().get();
        return !p.is(Potions.WATER) && !p.is(Potions.AWKWARD) && !p.is(Potions.MUNDANE) && !p.is(Potions.THICK);
    }

    private void recalcAspects() {
        if (level == null || level.isClientSide) {
            return;
        }
        color = 3355443;
        PotionContents contents = potion.isEmpty() ? null : potion.get(DataComponents.POTION_CONTENTS);
        if (contents == null || contents.potion().isEmpty()) {
            recipe = new AspectList();
        } else {
            recipe = ConfigAspects.getPotionAspects(contents.potion().get());
            color = contents.getColor();
        }
        charges = 0;
        recipe = AspectHelper.cullTags(recipe, 10);
        recipeProgress = new AspectList();
        syncTile(false);
        setChanged();
    }

    void fill() {
        Direction facing = facing();
        for (int y = 0; y <= 1; ++y) {
            for (Direction dir : Direction.values()) {
                if (dir == facing) {
                    continue;
                }
                BlockEntity te = ThaumcraftApiHelper.getConnectableTile(level, worldPosition.above(y), dir);
                if (te instanceof IEssentiaTransport ic) {
                    if (ic.getEssentiaAmount(dir.getOpposite()) > 0 && ic.getSuctionAmount(dir.getOpposite()) < getSuctionAmount(null)
                            && getSuctionAmount(null) >= ic.getMinimumSuction()) {
                        int ess = ic.takeEssentia(currentSuction, 1, dir.getOpposite());
                        if (ess > 0) {
                            addToContainer(currentSuction, ess);
                            return;
                        }
                    }
                }
            }
        }
    }

    // ---- IAspectContainer ----

    @Override
    public int addToContainer(Aspect tt, int am) {
        int ce = recipe.getAmount(tt) - recipeProgress.getAmount(tt);
        if (ce <= 0) {
            return am;
        }
        int add = Math.min(ce, am);
        recipeProgress.add(tt, add);
        syncTile(false);
        setChanged();
        return am - add;
    }

    @Override
    public boolean takeFromContainer(Aspect tt, int am) {
        return false;
    }

    @Override
    public boolean doesContainerContainAmount(Aspect tt, int am) {
        return recipeProgress.getAmount(tt) >= am;
    }

    @Override
    public int containerContains(Aspect tt) {
        return recipeProgress.getAmount(tt);
    }

    @Override
    public boolean doesContainerAccept(Aspect tag) {
        return true;
    }

    @Override
    public AspectList getAspects() {
        return recipeProgress;
    }

    @Override
    public void setAspects(AspectList aspects) {
        recipeProgress = aspects;
    }

    // ---- IEssentiaTransport: input from every side but the face ----

    @Override
    public boolean isConnectable(Direction face) {
        return face != facing();
    }

    @Override
    public boolean canInputFrom(Direction face) {
        return face != facing();
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

    // ---- inventory (TileThaumcraftInventory, one potion slot) ----

    @Override
    public int[] getSlotsForFace(Direction side) {
        return SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction direction) {
        return canPlaceItem(index, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return true;
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        return !stack.isEmpty() && isValidPotion(stack);
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return potion.isEmpty();
    }

    @Override
    public ItemStack getItem(int index) {
        return index == 0 ? potion : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int index, int count) {
        if (index != 0) return ItemStack.EMPTY;
        ItemStack out = potion.split(count);
        if (!out.isEmpty()) {
            recalcAspects();
            setChanged();
            syncTile(false);
        }
        return out;
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        if (index != 0) return ItemStack.EMPTY;
        ItemStack ret = potion;
        potion = ItemStack.EMPTY;
        return ret;
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        if (index == 0) {
            potion = stack;
            recalcAspects();
            setChanged();
            syncTile(false);
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return net.minecraft.world.Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        potion = ItemStack.EMPTY;
    }
}
