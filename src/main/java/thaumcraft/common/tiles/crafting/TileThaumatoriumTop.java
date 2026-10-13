package thaumcraft.common.tiles.crafting;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.api.aspects.IEssentiaTransport;
import thaumcraft.common.registry.TCBlockEntities;
import thaumcraft.common.tiles.TileThaumcraft;

/** Port of TileThaumatoriumTop: the upper half, which hands everything to the thaumatorium below. */
public class TileThaumatoriumTop extends TileThaumcraft implements WorldlyContainer, IAspectContainer, IEssentiaTransport {
    private static final int[] SLOTS = {0};
    private static final int[] NO_SLOTS = {};

    public TileThaumatoriumTop(BlockPos pos, BlockState state) {
        super(TCBlockEntities.THAUMATORIUM_TOP.get(), pos, state);
    }

    @Nullable
    public TileThaumatorium getThaumatorium() {
        return level != null && level.getBlockEntity(worldPosition.below()) instanceof TileThaumatorium t ? t : null;
    }

    @Override
    public int addToContainer(Aspect tt, int am) {
        TileThaumatorium t = getThaumatorium();
        return t == null ? am : t.addToContainer(tt, am);
    }

    @Override
    public boolean takeFromContainer(Aspect tt, int am) {
        TileThaumatorium t = getThaumatorium();
        return t != null && t.takeFromContainer(tt, am);
    }

    @Override
    public boolean doesContainerContainAmount(Aspect tt, int am) {
        TileThaumatorium t = getThaumatorium();
        return t != null && t.doesContainerContainAmount(tt, am);
    }

    @Override
    public int containerContains(Aspect tt) {
        TileThaumatorium t = getThaumatorium();
        return t == null ? 0 : t.containerContains(tt);
    }

    @Override
    public boolean doesContainerAccept(Aspect tag) {
        return true;
    }

    @Override
    public AspectList getAspects() {
        TileThaumatorium t = getThaumatorium();
        return t == null ? null : t.essentia;
    }

    @Override
    public void setAspects(AspectList aspects) {
        TileThaumatorium t = getThaumatorium();
        if (t != null) t.setAspects(aspects);
    }

    @Override
    public boolean isConnectable(Direction face) {
        TileThaumatorium t = getThaumatorium();
        return t != null && t.isConnectable(face);
    }

    @Override
    public boolean canInputFrom(Direction face) {
        TileThaumatorium t = getThaumatorium();
        return t != null && t.canInputFrom(face);
    }

    @Override
    public boolean canOutputTo(Direction face) {
        return false;
    }

    @Override
    public void setSuction(@Nullable Aspect aspect, int amount) {
        TileThaumatorium t = getThaumatorium();
        if (t != null) t.setSuction(aspect, amount);
    }

    @Override
    public Aspect getSuctionType(Direction face) {
        TileThaumatorium t = getThaumatorium();
        return t == null ? null : t.getSuctionType(face);
    }

    @Override
    public int getSuctionAmount(Direction face) {
        TileThaumatorium t = getThaumatorium();
        return t == null ? 0 : t.getSuctionAmount(face);
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
        return 0;
    }

    @Override
    public int addEssentia(Aspect aspect, int amount, Direction face) {
        TileThaumatorium t = getThaumatorium();
        return t == null ? 0 : t.addEssentia(aspect, amount, face);
    }

    @Override
    public int getMinimumSuction() {
        return 0;
    }

    // ---- inventory: the thaumatorium's catalyst slot ----

    @Override
    public int[] getSlotsForFace(Direction side) {
        return getThaumatorium() == null ? NO_SLOTS : SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction direction) {
        return getThaumatorium() != null;
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return getThaumatorium() != null;
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        TileThaumatorium t = getThaumatorium();
        return t == null || t.isEmpty();
    }

    @Override
    public ItemStack getItem(int index) {
        TileThaumatorium t = getThaumatorium();
        return t == null ? ItemStack.EMPTY : t.getItem(index);
    }

    @Override
    public ItemStack removeItem(int index, int count) {
        TileThaumatorium t = getThaumatorium();
        return t == null ? ItemStack.EMPTY : t.removeItem(index, count);
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        TileThaumatorium t = getThaumatorium();
        return t == null ? ItemStack.EMPTY : t.removeItemNoUpdate(index);
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        TileThaumatorium t = getThaumatorium();
        if (t != null) t.setItem(index, stack);
    }

    @Override
    public boolean stillValid(Player player) {
        TileThaumatorium t = getThaumatorium();
        return t != null && t.stillValid(player);
    }

    @Override
    public void clearContent() {
    }
}
