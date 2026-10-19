package thaumcraft.common.tiles.devices;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.api.items.IRechargable;
import thaumcraft.api.items.RechargeHelper;
import thaumcraft.client.fx.ClientFX;
import thaumcraft.common.registry.TCBlockEntities;
import thaumcraft.common.tiles.TileThaumcraft;

/**
 * Port of TileRechargePedestal. Every half second it pulls up to 5 vis from the aura into the rechargeable item it
 * holds. It shows the item's charge as Potentia to the thaumometer and goggles. Hoppers can put rechargeable items
 * in and take them out.
 */
public class TileRechargePedestal extends TileThaumcraft implements WorldlyContainer, IAspectContainer {
    private static final int[] SLOTS = {0};
    private ItemStack stack = ItemStack.EMPTY;
    private int counter = 0;

    public TileRechargePedestal(BlockPos pos, BlockState state) {
        super(TCBlockEntities.RECHARGE_PEDESTAL.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, TileRechargePedestal te) {
        if (!level.isClientSide && te.counter++ % 10 == 0 && !te.stack.isEmpty()
                && RechargeHelper.rechargeItem(level, te.stack, pos, null, 5) > 0.0f) {
            te.syncTile(false);
            te.setChanged();
            List<Aspect> al = Aspect.getPrimalAspects();
            level.blockEvent(pos, state.getBlock(), 5, al.get(level.random.nextInt(al.size())).getColor());
        }
    }

    @Override
    protected void readSyncNBT(CompoundTag tag, HolderLookup.Provider registries) {
        stack = tag.contains("Item") ? ItemStack.parseOptional(registries, tag.getCompound("Item")) : ItemStack.EMPTY;
    }

    @Override
    protected void writeSyncNBT(CompoundTag tag, HolderLookup.Provider registries) {
        if (!stack.isEmpty()) {
            tag.put("Item", stack.save(registries));
        }
    }

    /** Event 5: vis motes drifting in from around the pedestal. */
    @Override
    public boolean triggerEvent(int i, int j) {
        if (i == 5) {
            if (level != null && level.isClientSide) {
                BlockPos up = worldPosition.above();
                thaumcraft.client.fx.FXDispatcher.visSparkle(worldPosition.getX() + level.random.nextInt(3) - level.random.nextInt(3), up.getY() + level.random.nextInt(3),
                        worldPosition.getZ() + level.random.nextInt(3) - level.random.nextInt(3), worldPosition.getX(), up.getY(), worldPosition.getZ(), j);
            }
            return true;
        }
        return super.triggerEvent(i, j);
    }

    // Container: one synced slot that only takes rechargeable items.

    @Override
    public int[] getSlotsForFace(Direction side) {
        return SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack s, Direction direction) {
        return s.getItem() instanceof IRechargable;
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack s, Direction direction) {
        return true;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack s) {
        return s.getItem() instanceof IRechargable;
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return stack.isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return stack;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        if (stack.isEmpty() || amount <= 0) return ItemStack.EMPTY;
        ItemStack out = stack.split(amount);
        setChanged();
        syncTile(false);
        return out;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack out = stack;
        stack = ItemStack.EMPTY;
        setChanged();
        syncTile(false);
        return out;
    }

    @Override
    public void setItem(int slot, ItemStack s) {
        stack = s;
        setChanged();
        syncTile(false);
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        setItem(0, ItemStack.EMPTY);
    }

    // IAspectContainer: shows the held item's charge.

    @Override
    public AspectList getAspects() {
        if (stack.getItem() instanceof IRechargable) {
            return new AspectList().add(Aspect.ENERGY, Math.round((float) RechargeHelper.getCharge(stack)));
        }
        return null;
    }

    @Override
    public void setAspects(AspectList aspects) {
    }

    @Override
    public boolean doesContainerAccept(Aspect tag) {
        return true;
    }

    @Override
    public int addToContainer(Aspect tag, int amount) {
        return 0;
    }

    @Override
    public boolean takeFromContainer(Aspect tag, int amount) {
        return false;
    }

    @Override
    public boolean doesContainerContainAmount(Aspect tag, int amount) {
        return false;
    }

    @Override
    public int containerContains(Aspect tag) {
        return 0;
    }
}
