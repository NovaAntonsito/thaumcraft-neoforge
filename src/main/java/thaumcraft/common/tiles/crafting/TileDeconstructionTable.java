package thaumcraft.common.tiles.crafting;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.PacketDistributor;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectHelper;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.capabilities.PlayerAspects;
import thaumcraft.api.capabilities.ThaumcraftCapabilities;
import thaumcraft.common.lib.crafting.ThaumcraftCraftingManager;
import thaumcraft.common.lib.network.playerdata.PacketAspectPool;
import thaumcraft.common.registry.TCBlockEntities;
import thaumcraft.common.tiles.TileThaumcraft;

/**
 * Reborn's TileDeconstructionTable. An item with aspects in the slot is broken down one at a time, every two
 * seconds; the more primal vis it holds, the likelier it leaves a primal aspect behind. The aspect waits on the
 * table until a player takes it, which adds one point to their pool.
 */
public class TileDeconstructionTable extends TileThaumcraft implements WorldlyContainer {
    private static final int[] SLOTS = {0};
    private static final int[] NO_SLOTS = {};

    public ItemStack breakItem = ItemStack.EMPTY;
    @Nullable
    public Aspect aspect;
    public int breaktime;

    public TileDeconstructionTable(BlockPos pos, BlockState state) {
        super(TCBlockEntities.DECONSTRUCTION_TABLE.get(), pos, state);
    }

    @Override
    protected void writeSyncNBT(CompoundTag tag, HolderLookup.Provider registries) {
        if (aspect != null) tag.putString("Aspect", aspect.getTag());
        if (!breakItem.isEmpty()) tag.put("BreakItem", breakItem.save(registries));
        tag.putInt("breaktime", breaktime);
    }

    @Override
    protected void readSyncNBT(CompoundTag tag, HolderLookup.Provider registries) {
        aspect = Aspect.getAspect(tag.getString("Aspect"));
        breakItem = tag.contains("BreakItem") ? ItemStack.parseOptional(registries, tag.getCompound("BreakItem")) : ItemStack.EMPTY;
        breaktime = tag.getInt("breaktime");
    }

    public int getBreakTimeScaled(int par1) {
        return breaktime * par1 / 40;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, TileDeconstructionTable tile) {
        boolean flag1 = false;
        if (tile.breaktime == 0 && tile.canBreak()) {
            tile.breaktime = 40;
            flag1 = true;
        }
        if (tile.breaktime > 0 && tile.canBreak()) {
            --tile.breaktime;
            if (tile.breaktime == 0) {
                tile.breakItem();
                flag1 = true;
            }
        } else {
            tile.breaktime = 0;
        }
        if (flag1) {
            tile.syncTile(false);
            tile.setChanged();
        }
    }

    public boolean takeAspect(Player player) {
        if (aspect != null) {
            if (player instanceof ServerPlayer sp) {
                PlayerAspects storage = ThaumcraftCapabilities.getAspects(sp);
                storage.addToAspectPool(aspect, 1);
                PacketDistributor.sendToPlayer(sp, new PacketAspectPool(aspect.getTag(), 1, storage.aspectCount(aspect)));
                aspect = null;
                syncTile(false);
                setChanged();
            }
            aspect = null;
            return true;
        }
        return false;
    }

    private boolean canBreak() {
        if (!breakItem.isEmpty() && aspect == null) {
            AspectList al = ThaumcraftCraftingManager.getObjectTags(breakItem);
            return al != null && al.size() != 0;
        }
        return false;
    }

    public void breakItem() {
        if (canBreak()) {
            AspectList al = ThaumcraftCraftingManager.getObjectTags(breakItem);
            AspectList primal = AspectHelper.reduceToPrimals(al);
            if (level.random.nextInt(80) < primal.visSize()) {
                aspect = primal.getAspects()[level.random.nextInt(primal.getAspects().length)];
            }
            breakItem.shrink(1);
        }
    }

    // ---- inventory (ISidedInventory) ----

    @Override
    public int[] getSlotsForFace(Direction side) {
        return side != Direction.UP ? SLOTS : NO_SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction direction) {
        return index == 0 && canPlaceItem(0, stack) && direction != Direction.UP;
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return index == 0 && canPlaceItem(0, stack) && direction != Direction.UP;
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return breakItem.isEmpty();
    }

    @Override
    public ItemStack getItem(int index) {
        return index == 0 ? breakItem : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int index, int count) {
        if (index != 0) return ItemStack.EMPTY;
        ItemStack out = breakItem.split(count);
        if (!out.isEmpty()) setChanged();
        return out;
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        if (index != 0) return ItemStack.EMPTY;
        ItemStack ret = breakItem;
        breakItem = ItemStack.EMPTY;
        return ret;
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        if (index == 0) {
            breakItem = stack;
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
        AspectList al = ThaumcraftCraftingManager.getObjectTags(stack);
        return al != null && al.size() > 0;
    }

    @Override
    public void clearContent() {
        breakItem = ItemStack.EMPTY;
    }
}
