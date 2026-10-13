package thaumcraft.common.tiles.devices;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import thaumcraft.common.items.consumables.ItemBathSalts;
import thaumcraft.common.registry.TCBlockEntities;
import thaumcraft.common.registry.TCFluids;
import thaumcraft.common.tiles.TileThaumcraft;

/**
 * Port of TileSpa. Every two seconds, unless powered, it pours a source block above itself: purifying fluid made
 * from 1000 mB of water and one bath salt (mix mode), or 1000 mB of whatever is in the tank. Once the block above
 * is full it spreads to free spots within two blocks that touch the pool.
 * <p>
 * 1.21: fluids no longer have the quanta of BlockFluidBase, so checkQuanta is gone; the spa always places sources.
 */
public class TileSpa extends TileThaumcraft implements WorldlyContainer {
    private static final int[] SLOTS = {0};
    private static final int[] NO_SLOTS = {};

    private ItemStack salts = ItemStack.EMPTY;
    private boolean mix = true;
    private int counter = 0;

    public final FluidTank tank = new FluidTank(5000) {
        @Override
        protected void onContentsChanged() {
            setChanged();
            syncTile(false);
        }
    };

    public TileSpa(BlockPos pos, BlockState state) {
        super(TCBlockEntities.SPA.get(), pos, state);
    }

    public void toggleMix() {
        mix = !mix;
        syncTile(false);
        setChanged();
    }

    public boolean getMix() {
        return mix;
    }

    @Override
    protected void readSyncNBT(CompoundTag tag, HolderLookup.Provider registries) {
        mix = tag.getBoolean("mix");
        tank.readFromNBT(registries, tag);
        salts = tag.contains("Salts") ? ItemStack.parseOptional(registries, tag.getCompound("Salts")) : ItemStack.EMPTY;
    }

    @Override
    protected void writeSyncNBT(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putBoolean("mix", mix);
        tank.writeToNBT(registries, tag);
        if (!salts.isEmpty()) tag.put("Salts", salts.save(registries));
    }

    public static void tick(Level level, BlockPos pos, BlockState state, TileSpa tile) {
        tile.update();
    }

    private void update() {
        if (counter++ % 40 != 0 || level.hasNeighborSignal(worldPosition) || !hasIngredients()) {
            return;
        }
        Fluid tb = mix ? TCFluids.PURIFYING.get() : tank.getFluid().getFluid();
        if (isSourceOf(level.getFluidState(worldPosition.above()), tb)) {
            for (int xx = -2; xx <= 2; ++xx) {
                for (int zz = -2; zz <= 2; ++zz) {
                    BlockPos p = worldPosition.offset(xx, 1, zz);
                    if (isValidLocation(p, true, tb)) {
                        consumeIngredients();
                        level.setBlockAndUpdate(p, sourceOf(tb).createLegacyBlock());
                        return;
                    }
                }
            }
        } else if (isValidLocation(worldPosition.above(), false, tb)) {
            consumeIngredients();
            level.setBlockAndUpdate(worldPosition.above(), sourceOf(tb).createLegacyBlock());
        }
    }

    private static FluidState sourceOf(Fluid fluid) {
        return fluid instanceof FlowingFluid ff ? ff.getSource(false) : fluid.defaultFluidState();
    }

    private static boolean isSourceOf(FluidState state, Fluid fluid) {
        return state.isSource() && state.getType().isSame(fluid);
    }

    private boolean hasIngredients() {
        if (mix) {
            FluidStack fs = tank.getFluid();
            return fs.is(Fluids.WATER) && fs.getAmount() >= 1000 && salts.getItem() instanceof ItemBathSalts;
        }
        // canBePlacedInWorld: the fluid has a block.
        return !tank.isEmpty() && !sourceOf(tank.getFluid().getFluid()).createLegacyBlock().isAir() && tank.getFluidAmount() >= 1000;
    }

    private void consumeIngredients() {
        if (mix) {
            removeItem(0, 1);
        }
        tank.drain(1000, net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
    }

    private boolean isValidLocation(BlockPos pos, boolean mustBeAdjacent, Fluid target) {
        if (target.is(FluidTags.WATER) && level.dimensionType().ultraWarm()) {
            return false;
        }
        BlockState b = level.getBlockState(pos);
        if (!level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP) || !b.canBeReplaced()
                || isSourceOf(b.getFluidState(), target)) {
            return false;
        }
        if (!mustBeAdjacent) {
            return true;
        }
        // BlockUtils.isBlockTouching
        for (Direction d : Direction.values()) {
            if (isSourceOf(level.getFluidState(pos.relative(d)), target)) {
                return true;
            }
        }
        return false;
    }

    // ---- inventory (TileThaumcraftInventory, one slot for bath salts, not from above) ----

    @Override
    public int[] getSlotsForFace(Direction side) {
        return side != Direction.UP ? SLOTS : NO_SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction direction) {
        return direction != Direction.UP && canPlaceItem(index, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return direction != Direction.UP;
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        return stack.getItem() instanceof ItemBathSalts;
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return salts.isEmpty();
    }

    @Override
    public ItemStack getItem(int index) {
        return index == 0 ? salts : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int index, int count) {
        if (index != 0) return ItemStack.EMPTY;
        ItemStack out = salts.split(count);
        if (!out.isEmpty()) {
            setChanged();
            syncTile(false);
        }
        return out;
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        if (index != 0) return ItemStack.EMPTY;
        ItemStack ret = salts;
        salts = ItemStack.EMPTY;
        return ret;
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        if (index == 0) {
            salts = stack;
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
        salts = ItemStack.EMPTY;
    }
}
