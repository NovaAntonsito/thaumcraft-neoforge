package thaumcraft.common.tiles.crafting;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.common.blocks.devices.BlockInlay;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.registry.TCBlockEntities;
import thaumcraft.common.tiles.TileThaumcraft;

/**
 * Port of TilePedestal (a TileThaumcraftInventory with one synced slot holding one item).
 * It is a Container so hoppers can fill it, as with the original IInventory.
 */
public class TilePedestal extends TileThaumcraft implements Container {
    private ItemStack stack = ItemStack.EMPTY;

    public TilePedestal(BlockPos pos, BlockState state) {
        super(TCBlockEntities.PEDESTAL.get(), pos, state);
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

    /** isItemValidForSlot: only into an empty pedestal. */
    @Override
    public boolean canPlaceItem(int slot, ItemStack s) {
        return stack.isEmpty();
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        setItem(0, ItemStack.EMPTY);
    }

    public void setInventorySlotContentsFromInfusion(int slot, ItemStack s) {
        setItem(slot, s);
    }

    /** Follows the vis inlay uphill (rising charge) to an infusion stabilizer with at least 5 charge. */
    @Nullable
    public BlockPos findInstabilityMitigator() {
        int charge = getBlockState().hasProperty(BlockInlay.CHARGE) ? getBlockState().getValue(BlockInlay.CHARGE) : 0;
        if (charge > 0) {
            return seekSourceRecursive(worldPosition, charge);
        }
        return null;
    }

    @Nullable
    private BlockPos seekSourceRecursive(BlockPos pos, int lastCharge) {
        for (Direction face : Direction.Plane.HORIZONTAL) {
            BlockPos pp = pos.relative(face);
            int ss = BlockInlay.getSourceStrengthAt(level, pp);
            if (ss >= 5) {
                return pp;
            }
            BlockState bs = level.getBlockState(pp);
            if (bs.hasProperty(BlockInlay.CHARGE)) {
                int charge = bs.getValue(BlockInlay.CHARGE);
                if (charge > lastCharge) {
                    BlockPos ob = seekSourceRecursive(pp, charge);
                    if (ob != null) {
                        return ob;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Client effects sent with level.blockEvent: 11 when instability knocks the item off, 12 when infusion
     * finishes, 5 for the stabilizer shield.
     * TODO: the original FXDispatcher.drawBamf and drawPedestalShield (stage 10). Vanilla particles for now.
     */
    @Override
    public boolean triggerEvent(int i, int j) {
        if (i == 11 || i == 12) {
            if (level != null && level.isClientSide) {
                BlockPos p = worldPosition.above();
                level.playLocalSound(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5, SoundsTC.POOF.get(), SoundSource.BLOCKS, 0.4f,
                        1.0f + (float) level.random.nextGaussian() * 0.05f, false);
                for (int a = 0; a < 8 + level.random.nextInt(3); ++a) {
                    level.addParticle(i == 11 ? ParticleTypes.WITCH : ParticleTypes.POOF, p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5,
                            level.random.nextGaussian() * 0.05, level.random.nextGaussian() * 0.05, level.random.nextGaussian() * 0.05);
                }
            }
            return true;
        }
        if (i == 5) {
            return true;
        }
        return super.triggerEvent(i, j);
    }
}
