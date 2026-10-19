package thaumcraft.common.tiles.devices;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aura.AuraHelper;
import thaumcraft.api.internal.CommonInternals;
import thaumcraft.common.blocks.devices.BlockBellows;
import thaumcraft.common.blocks.devices.BlockInfernalFurnace;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.lib.utils.InventoryUtils;
import thaumcraft.common.registry.TCBlockEntities;
import thaumcraft.common.tiles.TileThaumcraft;

/**
 * Port of TileInfernalFurnace: smelts what falls into it, one item at a time, with no fuel. Every bellows blowing
 * into it shortens the cook time and rolls the smelting bonus once more; vis from the aura makes it faster too.
 * Results come out through the iron bars side (or into the inventory there). Items go in from above only.
 */
public class TileInfernalFurnace extends TileThaumcraft implements WorldlyContainer {
    private static final int[] SLOTS = new int[32];
    static {
        for (int i = 0; i < SLOTS.length; ++i) SLOTS[i] = i;
    }

    private final NonNullList<ItemStack> items = NonNullList.withSize(32, ItemStack.EMPTY);
    public int furnaceCookTime = 0;
    public int furnaceMaxCookTime = 0;
    public int speedyTime = 0;
    public int facingX = -5;
    public int facingZ = -5;

    public TileInfernalFurnace(BlockPos pos, BlockState state) {
        super(TCBlockEntities.INFERNAL_FURNACE.get(), pos, state);
    }

    public Direction getFacing() {
        return getBlockState().getValue(BlockInfernalFurnace.FACING);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items.clear();
        ContainerHelper.loadAllItems(tag, items, registries);
        furnaceCookTime = tag.getShort("CookTime");
        speedyTime = tag.getShort("SpeedyTime");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
        tag.putShort("CookTime", (short) furnaceCookTime);
        tag.putShort("SpeedyTime", (short) speedyTime);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TileInfernalFurnace te) {
        te.update();
    }

    private void update() {
        if (facingX == -5) {
            setFacing();
        }
        boolean cookedflag = false;
        if (furnaceCookTime > 0) {
            --furnaceCookTime;
            cookedflag = true;
        }
        if (furnaceMaxCookTime <= 0) {
            furnaceMaxCookTime = calcCookTime();
        }
        if (furnaceCookTime > furnaceMaxCookTime) {
            furnaceCookTime = furnaceMaxCookTime;
        }
        if (furnaceCookTime <= 0 && cookedflag) {
            for (int a = 0; a < getContainerSize(); ++a) {
                ItemStack in = getItem(a);
                if (!in.isEmpty()) {
                    ItemStack itemstack = getSmeltingResult(in);
                    if (!itemstack.isEmpty()) {
                        if (speedyTime > 0) {
                            --speedyTime;
                        }
                        ejectItem(itemstack.copy(), in);
                        level.blockEvent(worldPosition, ConfigBlocks.infernalFurnace.get(), 3, 0);
                        if (level.random.nextInt(20) == 0) {
                            AuraHelper.polluteAura(level, worldPosition.relative(getFacing().getOpposite()), 1.0f, true);
                        }
                        removeItem(a, 1);
                        break;
                    }
                    setItem(a, ItemStack.EMPTY);
                }
            }
        }
        if (speedyTime <= 0) {
            speedyTime = (int) AuraHelper.drainVis(level, worldPosition, 20.0f, false);
        }
        if (furnaceCookTime == 0 && !cookedflag) {
            for (int a = 0; a < getContainerSize(); ++a) {
                if (canSmelt(getItem(a))) {
                    furnaceMaxCookTime = calcCookTime();
                    furnaceCookTime = furnaceMaxCookTime;
                    break;
                }
            }
        }
    }

    /** Bellows two blocks away (the far side of the wall), facing the furnace and not powered. At most 4. */
    private int getBellows() {
        int bellows = 0;
        for (Direction dir : Direction.values()) {
            if (dir != Direction.UP) {
                BlockPos p2 = worldPosition.relative(dir, 2);
                BlockEntity tile = level.getBlockEntity(p2);
                if (tile instanceof TileBellows && level.getBlockState(p2).getValue(BlockBellows.FACING) == dir.getOpposite()
                        && level.getBestNeighborSignal(p2) == 0) {
                    ++bellows;
                }
            }
        }
        return Math.min(4, bellows);
    }

    private int calcCookTime() {
        int b = getBellows();
        if (b > 0) {
            b *= 20 - (b - 1);
        }
        return Math.max(10, (speedyTime > 0 ? 80 : 140) - b);
    }

    /** Called with an item that landed in the lava: smeltable items go in from the top, anything else burns away. */
    public ItemStack addItemsToInventory(ItemStack stack) {
        if (canSmelt(stack)) {
            stack = insert(stack);
        } else {
            destroyItem();
            stack = ItemStack.EMPTY;
        }
        return stack;
    }

    /** ThaumcraftInvHelper.insertStackAt(UP): fills matching stacks first, then empty slots. */
    private ItemStack insert(ItemStack stack) {
        stack = stack.copy();
        for (int pass = 0; pass < 2 && !stack.isEmpty(); ++pass) {
            for (int a = 0; a < items.size() && !stack.isEmpty(); ++a) {
                ItemStack in = items.get(a);
                if (pass == 0 && !in.isEmpty() && ItemStack.isSameItemSameComponents(in, stack)) {
                    int move = Math.min(stack.getCount(), in.getMaxStackSize() - in.getCount());
                    if (move > 0) {
                        in.grow(move);
                        stack.shrink(move);
                    }
                } else if (pass == 1 && in.isEmpty()) {
                    items.set(a, stack.split(Math.min(stack.getCount(), stack.getMaxStackSize())));
                }
            }
        }
        setChanged();
        return stack;
    }

    /** The original played these on the client only, so nothing showed; here they reach the players nearby. */
    private void destroyItem() {
        level.playSound(null, worldPosition, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 0.3f,
                2.6f + (level.random.nextFloat() - level.random.nextFloat()) * 0.8f);
        if (level instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.LAVA, worldPosition.getX() + level.random.nextFloat(), worldPosition.getY() + 1,
                    worldPosition.getZ() + level.random.nextFloat(), 1, 0, 0, 0, 0);
        }
    }

    public void ejectItem(ItemStack items, ItemStack furnaceItemStack) {
        if (items.isEmpty()) {
            return;
        }
        List<ItemStack> ejecti = new ArrayList<>();
        ejecti.add(items.copy());
        int bellows = getBellows() + 1;
        float lx = 0.5f + facingX * 1.2f;
        float lz = 0.5f + facingZ * 1.2f;
        for (int a = 0; a < bellows; ++a) {
            for (ItemStack bonus : getSmeltingBonus(furnaceItemStack)) {
                if (!bonus.isEmpty()) {
                    ejecti.add(bonus);
                }
            }
        }
        Direction facing = getFacing().getOpposite();
        for (ItemStack outItem : ejecti) {
            if (!outItem.isEmpty()) {
                InventoryUtils.ejectStackAt(level, worldPosition, facing, outItem);
            }
        }
        int cnt = items.getCount();
        float xpf = getSmeltingExperience(furnaceItemStack);
        if (xpf == 0.0f) {
            cnt = 0;
        } else if (xpf < 1.0f) {
            int var4 = Mth.floor(cnt * xpf);
            if (var4 < Mth.ceil(cnt * xpf) && (float) Math.random() < cnt * xpf - var4) {
                ++var4;
            }
            cnt = var4;
        }
        while (cnt > 0) {
            int var4 = ExperienceOrb.getExperienceValue(cnt);
            cnt -= var4;
            ExperienceOrb xp = new ExperienceOrb(level, worldPosition.getX() + lx, worldPosition.getY() + 0.4f, worldPosition.getZ() + lz, var4);
            double mx = facingX == 0 ? (level.random.nextFloat() - level.random.nextFloat()) * 0.025f : facingX * 0.13f;
            double mz = facingZ == 0 ? (level.random.nextFloat() - level.random.nextFloat()) * 0.025f : facingZ * 0.13f;
            xp.setDeltaMovement(mx, 0.0, mz);
            level.addFreshEntity(xp);
        }
    }

    /** The bonus items for one roll. An ItemStack input matches by item; a tag input stands for the ore name. */
    private List<ItemStack> getSmeltingBonus(ItemStack in) {
        List<ItemStack> out = new ArrayList<>();
        for (ThaumcraftApi.SmeltBonus bonus : CommonInternals.smeltingBonus) {
            boolean match;
            if (bonus.in instanceof ItemStack bs) {
                match = in.getItem() == bs.getItem();
            } else {
                @SuppressWarnings("unchecked")
                TagKey<Item> tag = (TagKey<Item>) bonus.in;
                match = in.is(tag);
            }
            if (match && level.random.nextFloat() <= bonus.chance) {
                ItemStack is = bonus.out.copy();
                if (is.getCount() < 1) {
                    is.setCount(1);
                }
                out.add(is);
            }
        }
        return out;
    }

    private Optional<RecipeHolder<SmeltingRecipe>> getRecipe(ItemStack stack) {
        if (stack.isEmpty() || level == null) return Optional.empty();
        return level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(stack), level);
    }

    private ItemStack getSmeltingResult(ItemStack stack) {
        return getRecipe(stack).map(h -> h.value().getResultItem(level.registryAccess())).orElse(ItemStack.EMPTY);
    }

    private float getSmeltingExperience(ItemStack stack) {
        return getRecipe(stack).map(h -> h.value().getExperience()).orElse(0.0f);
    }

    private boolean canSmelt(ItemStack stack) {
        return !getSmeltingResult(stack).isEmpty();
    }

    private void setFacing() {
        Direction face = getFacing().getOpposite();
        facingX = face.getStepX();
        facingZ = face.getStepZ();
    }

    /** Event 3: lava spits out of the opening each time an item is smelted. */
    @Override
    public boolean triggerEvent(int i, int j) {
        if (i == 3) {
            if (level != null && level.isClientSide) {
                if (facingX == -5) {
                    setFacing();
                }
                for (int a = 0; a < 5; ++a) {
                    thaumcraft.client.fx.FXDispatcher.furnaceLavaFx(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), facingX, facingZ);
                    level.playLocalSound(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, SoundEvents.LAVA_POP,
                            SoundSource.BLOCKS, 0.1f + level.random.nextFloat() * 0.1f, 0.9f + level.random.nextFloat() * 0.15f, false);
                }
            }
            return true;
        }
        return super.triggerEvent(i, j);
    }

    // WorldlyContainer: everything goes in from the top and nothing comes out.

    @Override
    public int[] getSlotsForFace(Direction side) {
        return side == Direction.UP ? SLOTS : new int[0];
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, Direction direction) {
        return direction == Direction.UP;
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return false;
    }

    @Override
    public int getContainerSize() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack s : items) {
            if (!s.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack out = ContainerHelper.removeItem(items, slot, amount);
        if (!out.isEmpty()) setChanged();
        return out;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return false;
    }

    @Override
    public void clearContent() {
        items.clear();
    }
}
