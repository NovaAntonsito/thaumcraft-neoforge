package thaumcraft.common.tiles.devices;

import java.util.ArrayList;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.api.aura.AuraHelper;
import thaumcraft.client.fx.ClientFX;
import thaumcraft.common.blocks.devices.BlockMirror;
import thaumcraft.common.registry.TCBlockEntities;
import thaumcraft.common.registry.TCDataComponents;
import thaumcraft.common.tiles.TileThaumcraft;

/**
 * Port of TileMirror (magic mirror). Two linked mirrors pass items between them: whatever falls into one, or is
 * put in by a hopper, comes out of the other one item at a time. Moving items builds up instability, which
 * leaks into the aura as flux.
 */
public class TileMirror extends TileThaumcraft implements Container {
    public boolean linked;
    public int linkX;
    public int linkY;
    public int linkZ;
    public ResourceKey<Level> linkDim = Level.OVERWORLD;
    public int instability;
    int count;
    int inc = 40;
    private ArrayList<ItemStack> outputStacks = new ArrayList<>();

    public TileMirror(BlockPos pos, BlockState state) {
        this(TCBlockEntities.MIRROR.get(), pos, state);
    }

    protected TileMirror(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    // ---- the linked mirror ----

    /** DimensionManager.getWorld: null on the client or when that dimension is gone. */
    @Nullable
    protected ServerLevel getTargetWorld() {
        if (level instanceof ServerLevel sl) {
            return sl.getServer().getLevel(linkDim);
        }
        return null;
    }

    protected BlockPos getLinkPos() {
        return new BlockPos(linkX, linkY, linkZ);
    }

    @Nullable
    protected TileMirror getTarget() {
        ServerLevel targetWorld = getTargetWorld();
        if (targetWorld == null) {
            return null;
        }
        BlockEntity te = targetWorld.getBlockEntity(getLinkPos());
        return te instanceof TileMirror tm && tm.getClass() == getClass() ? tm : null;
    }

    private boolean pointsAtMe(TileMirror tm) {
        return tm.linkX == worldPosition.getX() && tm.linkY == worldPosition.getY() && tm.linkZ == worldPosition.getZ()
                && tm.linkDim == level.dimension();
    }

    /** Sets the link from an item (the mirror the item was linked to). */
    public void setLink(GlobalPos pos) {
        linkX = pos.pos().getX();
        linkY = pos.pos().getY();
        linkZ = pos.pos().getZ();
        linkDim = pos.dimension();
    }

    public void restoreLink() {
        if (isDestinationValid()) {
            TileMirror tm = getTarget();
            if (tm != null) {
                tm.linked = true;
                tm.linkX = worldPosition.getX();
                tm.linkY = worldPosition.getY();
                tm.linkZ = worldPosition.getZ();
                tm.linkDim = level.dimension();
                tm.syncTile(false);
                linked = true;
                setChanged();
                tm.setChanged();
                syncTile(false);
            }
        }
    }

    public void invalidateLink() {
        ServerLevel targetWorld = getTargetWorld();
        if (targetWorld == null) {
            return;
        }
        if (!targetWorld.isLoaded(getLinkPos())) {
            return;
        }
        if (targetWorld.getBlockEntity(getLinkPos()) instanceof TileMirror tm && tm.getClass() == getClass()) {
            tm.linked = false;
            setChanged();
            tm.setChanged();
            tm.syncTile(false);
        }
    }

    private void unlink() {
        linked = false;
        setChanged();
        syncTile(false);
    }

    public boolean isLinkValid() {
        if (!linked) {
            return false;
        }
        if (getTargetWorld() == null) {
            return false;
        }
        TileMirror tm = getTarget();
        if (tm == null || !tm.linked || !pointsAtMe(tm)) {
            unlink();
            return false;
        }
        return true;
    }

    public boolean isLinkValidSimple() {
        if (!linked) {
            return false;
        }
        TileMirror tm = getTarget();
        return tm != null && tm.linked && pointsAtMe(tm);
    }

    public boolean isDestinationValid() {
        if (getTargetWorld() == null) {
            return false;
        }
        TileMirror tm = getTarget();
        if (tm == null) {
            unlink();
            return false;
        }
        return !tm.isLinkValid();
    }

    protected void addInstability(@Nullable Level targetWorld, int amt) {
        instability += amt;
        setChanged();
        if (targetWorld != null && targetWorld.getBlockEntity(getLinkPos()) instanceof TileMirror tm) {
            tm.instability += amt;
            if (tm.instability < 0) {
                tm.instability = 0;
            }
            tm.setChanged();
        }
    }

    // ---- items ----

    public boolean transport(ItemEntity ie) {
        ItemStack items = ie.getItem();
        if (!linked || !isLinkValid()) {
            return false;
        }
        TileMirror target = getTarget();
        if (target != null) {
            target.addStack(items.copy());
            addInstability(null, items.getCount());
            ie.discard();
            setChanged();
            target.setChanged();
            level.blockEvent(worldPosition, getBlockState().getBlock(), 1, 0);
            return true;
        }
        return false;
    }

    public boolean transportDirect(ItemStack items) {
        if (items == null || items.isEmpty()) {
            return false;
        }
        addStack(items.copy());
        setChanged();
        return true;
    }

    /** Item mirrors spit out what they received; essentia mirrors hold no items. */
    protected boolean ejectsItems() {
        return true;
    }

    public void eject() {
        if (!outputStacks.isEmpty() && count > 20) {
            int i = level.random.nextInt(outputStacks.size());
            if (!outputStacks.get(i).isEmpty()) {
                ItemStack outItem = outputStacks.get(i).copyWithCount(1);
                if (spawnItem(outItem)) {
                    outputStacks.get(i).shrink(1);
                    addInstability(null, 1);
                    level.blockEvent(worldPosition, getBlockState().getBlock(), 1, 0);
                    if (outputStacks.get(i).isEmpty()) {
                        outputStacks.remove(i);
                    }
                }
            } else {
                outputStacks.remove(i);
            }
            setChanged();
        }
    }

    public boolean spawnItem(ItemStack stack) {
        Direction face = getBlockState().getValue(BlockMirror.FACING);
        ItemEntity ie2 = new ItemEntity(level, worldPosition.getX() + 0.5, worldPosition.getY() + 0.25, worldPosition.getZ() + 0.5, stack);
        ie2.setDeltaMovement(face.getStepX() * 0.15f, face.getStepY() * 0.15f, face.getStepZ() * 0.15f);
        // timeUntilPortal = 20: keeps the item from falling straight back into the mirror.
        ie2.setPortalCooldown(20);
        return level.addFreshEntity(ie2);
    }

    public void addStack(ItemStack stack) {
        outputStacks.add(stack);
        setChanged();
    }

    // ---- tick ----

    public static void tick(Level level, BlockPos pos, BlockState state, TileMirror te) {
        te.update();
    }

    protected void update() {
        if (!level.isClientSide) {
            if (ejectsItems()) {
                eject();
            }
            checkInstability();
            if (count++ % inc == 0) {
                if (!isLinkValidSimple()) {
                    if (inc < 600) {
                        inc += 20;
                    }
                    restoreLink();
                } else {
                    inc = 40;
                }
            }
        }
    }

    /** Instability that turns into one point of flux: 128 for items, 64 for essentia. */
    protected int getInstabilityLimit() {
        return 128;
    }

    public void checkInstability() {
        int limit = getInstabilityLimit();
        if (instability > limit) {
            AuraHelper.polluteAura(level, worldPosition, 1.0f, true);
            instability -= limit;
            setChanged();
        }
        if (instability > 0 && count % 100 == 0) {
            --instability;
        }
    }

    /** Block event 1: wisps drift out of the mirror when an item goes through. */
    @Override
    public boolean triggerEvent(int i, int j) {
        if (i == 1) {
            if (level.isClientSide) {
                Direction face = getBlockState().getValue(BlockMirror.FACING);
                double xx = worldPosition.getX() + 0.33 + level.random.nextFloat() * 0.33f - face.getStepX() / 2.0;
                double yy = worldPosition.getY() + 0.33 + level.random.nextFloat() * 0.33f - face.getStepY() / 2.0;
                double zz = worldPosition.getZ() + 0.33 + level.random.nextFloat() * 0.33f - face.getStepZ() / 2.0;
                thaumcraft.client.fx.FXDispatcher.drawWispyMotes(xx, yy, zz, face.getStepX() / 50.0 + level.random.nextGaussian() * 0.01,
                        face.getStepY() / 50.0 + level.random.nextGaussian() * 0.01, face.getStepZ() / 50.0 + level.random.nextGaussian() * 0.01,
                        net.minecraft.util.Mth.nextInt(level.random, 10, 30), level.random.nextFloat() / 3.0f, 0.0f, level.random.nextFloat() / 2.0f,
                        (float) (level.random.nextGaussian() * 0.01));
            }
            return true;
        }
        return super.triggerEvent(i, j);
    }

    // ---- NBT ----

    @Override
    protected void readSyncNBT(CompoundTag tag, HolderLookup.Provider registries) {
        linked = tag.getBoolean("linked");
        linkX = tag.getInt("linkX");
        linkY = tag.getInt("linkY");
        linkZ = tag.getInt("linkZ");
        ResourceLocation dim = ResourceLocation.tryParse(tag.getString("linkDim"));
        linkDim = dim != null ? ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, dim) : Level.OVERWORLD;
        instability = tag.getInt("instability");
    }

    @Override
    protected void writeSyncNBT(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putBoolean("linked", linked);
        tag.putInt("linkX", linkX);
        tag.putInt("linkY", linkY);
        tag.putInt("linkZ", linkZ);
        tag.putString("linkDim", linkDim.location().toString());
        tag.putInt("instability", instability);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ListTag list = tag.getList("Items", Tag.TAG_COMPOUND);
        outputStacks = new ArrayList<>();
        for (int i = 0; i < list.size(); ++i) {
            ItemStack.parse(registries, list.getCompound(i)).ifPresent(outputStacks::add);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ListTag list = new ListTag();
        for (ItemStack stack : outputStacks) {
            if (!stack.isEmpty()) {
                list.add(stack.save(registries));
            }
        }
        tag.put("Items", list);
    }

    /** The dropped mirror keeps its link (the original set it on the dropped stack in dropMirror). */
    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        if (linked) {
            components.set(TCDataComponents.MIRROR_LINK.get(), GlobalPos.of(linkDim, getLinkPos()));
        }
    }

    @Override
    protected void applyImplicitComponents(BlockEntity.DataComponentInput componentInput) {
        super.applyImplicitComponents(componentInput);
        GlobalPos link = componentInput.get(TCDataComponents.MIRROR_LINK.get());
        if (link != null) {
            setLink(link);
        }
    }

    // ---- inventory: anything put in goes straight through ----

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public ItemStack getItem(int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        TileMirror target = getTarget();
        if (target != null) {
            target.addStack(stack.copy());
            addInstability(null, stack.getCount());
            level.blockEvent(worldPosition, getBlockState().getBlock(), 1, 0);
        } else {
            spawnItem(stack.copy());
        }
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return getTarget() != null;
    }

    @Override
    public boolean stillValid(Player player) {
        return false;
    }

    @Override
    public void clearContent() {
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }
}
