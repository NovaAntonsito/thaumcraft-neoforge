package thaumcraft.common.tiles.crafting;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import thaumcraft.common.blocks.crafting.BlockVoidSiphon;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.entities.EntityFluxRift;
import thaumcraft.common.registry.TCBlockEntities;
import thaumcraft.common.tiles.TileThaumcraft;

/**
 * Port of TileVoidSiphon. While enabled it drains visible flux rifts within 8 blocks, making them smaller and
 * less stable, and turns what it draws into void seeds (one per 2000 progress).
 */
public class TileVoidSiphon extends TileThaumcraft implements WorldlyContainer {
    private static final int[] SLOTS = {0};

    private ItemStack output = ItemStack.EMPTY;
    int counter = 0;
    public int progress = 0;

    public TileVoidSiphon(BlockPos pos, BlockState state) {
        super(TCBlockEntities.VOID_SIPHON.get(), pos, state);
    }

    /** TileThaumcraftInventory synced its slots to the client. */
    @Override
    protected void writeSyncNBT(CompoundTag tag, HolderLookup.Provider registries) {
        if (!output.isEmpty()) tag.put("Output", output.save(registries));
    }

    @Override
    protected void readSyncNBT(CompoundTag tag, HolderLookup.Provider registries) {
        output = tag.contains("Output") ? ItemStack.parseOptional(registries, tag.getCompound("Output")) : ItemStack.EMPTY;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        progress = tag.getShort("progress");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putShort("progress", (short) progress);
    }

    private boolean hasRoom() {
        return output.isEmpty() || (output.is(ConfigItems.voidSeed.get()) && output.getCount() < output.getMaxStackSize());
    }

    public static void tick(Level level, BlockPos pos, BlockState state, TileVoidSiphon tile) {
        tile.update(state);
    }

    private void update(BlockState state) {
        ++counter;
        boolean enabled = state.getValue(BlockVoidSiphon.ENABLED);
        if (level.isClientSide || !enabled || counter % 20 != 0 || progress >= 2000 || !hasRoom()) {
            return;
        }
        boolean b = false;
        for (EntityFluxRift fr : getValidRifts()) {
            double d = Math.sqrt(fr.getRiftSize());
            progress += (int) d;
            fr.setRiftStability((float) (fr.getRiftStability() - d / 15.0));
            if (level.random.nextInt(33) == 0) {
                fr.setRiftSize(fr.getRiftSize() - 1);
            }
            b = d >= 1.0;
        }
        if (b && counter % 40 == 0) {
            level.blockEvent(worldPosition, state.getBlock(), 5, counter);
        }
        b = false;
        while (progress >= 2000 && hasRoom()) {
            progress -= 2000;
            if (output.isEmpty()) {
                output = new ItemStack(ConfigItems.voidSeed.get());
            } else {
                output.grow(1);
            }
            b = true;
        }
        if (b) {
            syncTile(false);
            setChanged();
        }
    }

    private List<EntityFluxRift> getValidRifts() {
        List<EntityFluxRift> ret = new ArrayList<>();
        Vec3 c = Vec3.atCenterOf(worldPosition);
        for (EntityFluxRift fr : level.getEntitiesOfClass(EntityFluxRift.class, new AABB(worldPosition).inflate(8.0),
                e -> e.distanceToSqr(c) < 64.0)) {
            // Dream branch: the void rifts of the Whispers of the Void cannot be siphoned.
            if (!fr.isAlive() || fr.getRiftSize() < 2 || fr instanceof thaumcraft.common.entities.EntityVoidRift) {
                continue;
            }
            Vec3 v1 = new Vec3(worldPosition.getX() + 0.5, worldPosition.getY() + 1, worldPosition.getZ() + 0.5);
            Vec3 v2 = fr.position();
            v1 = v1.add(v2.subtract(v1).normalize());
            // EntityUtils.canEntityBeSeen: nothing between the point and the rift's eyes.
            if (level.clip(new ClipContext(v1, fr.getEyePosition(), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, fr)).getType()
                    != HitResult.Type.MISS) {
                continue;
            }
            ret.add(fr);
        }
        return ret;
    }

    @Override
    public boolean triggerEvent(int i, int j) {
        if (i == 5) {
            if (level.isClientSide) {
                for (EntityFluxRift fr : getValidRifts()) {
                    thaumcraft.client.fx.FXDispatcher.voidStreak(fr.getX(), fr.getY(), fr.getZ(), worldPosition.getX() + 0.5,
                            worldPosition.getY() + 0.5625f, worldPosition.getZ() + 0.5, j, 0.04f);
                }
            }
            return true;
        }
        return super.triggerEvent(i, j);
    }

    // ---- inventory (ISidedInventory): an output slot, taken from any side ----

    @Override
    public int[] getSlotsForFace(Direction side) {
        return SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction direction) {
        return false;
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
        return output.isEmpty();
    }

    @Override
    public ItemStack getItem(int index) {
        return index == 0 ? output : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int index, int count) {
        if (index != 0) return ItemStack.EMPTY;
        ItemStack out = output.split(count);
        if (!out.isEmpty()) {
            setChanged();
            syncTile(false);
        }
        return out;
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        if (index != 0) return ItemStack.EMPTY;
        ItemStack ret = output;
        output = ItemStack.EMPTY;
        return ret;
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        if (index == 0) {
            output = stack;
            setChanged();
            syncTile(false);
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return net.minecraft.world.Container.stillValidBlockEntity(this, player);
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        return stack.is(ConfigItems.voidSeed.get());
    }

    @Override
    public void clearContent() {
        output = ItemStack.EMPTY;
    }
}
