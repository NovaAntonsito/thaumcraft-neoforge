package thaumcraft.common.tiles.crafting;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.tags.FluidTags;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.capabilities.ThaumcraftCapabilities;
import thaumcraft.api.items.IScribeTools;
import thaumcraft.common.items.consumables.ItemResearchNote;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.lib.research.ResearchNoteData;
import thaumcraft.common.lib.research.ResearchNoteManager;
import thaumcraft.common.lib.utils.HexUtils;
import thaumcraft.common.registry.TCBlockEntities;
import thaumcraft.common.tiles.TileThaumcraft;

/**
 * The TC4 research table, from TC4 Research Port: Reborn. Slot 0 holds scribing tools, slot 1 the note being solved.
 * Every 30 seconds the surroundings may give a free point of a primal aspect ("bonus aspects").
 */
public class TileResearchTable extends TileThaumcraft implements Container {
    private final NonNullList<ItemStack> items = NonNullList.withSize(2, ItemStack.EMPTY);
    public AspectList bonusAspects = new AspectList();
    @Nullable
    public ResearchNoteData note;
    public int nextRecalc;

    public TileResearchTable(BlockPos pos, BlockState state) {
        super(TCBlockEntities.RESEARCH_TABLE.get(), pos, state);
    }

    @Override
    protected void writeSyncNBT(CompoundTag tag, HolderLookup.Provider registries) {
        ContainerHelper.saveAllItems(tag, items, true, registries);
        AspectList.CODEC.encodeStart(NbtOps.INSTANCE, bonusAspects).result().ifPresent(t -> tag.put("bonusAspects", t));
    }

    @Override
    protected void readSyncNBT(CompoundTag tag, HolderLookup.Provider registries) {
        items.clear();
        ContainerHelper.loadAllItems(tag, items, registries);
        bonusAspects = tag.contains("bonusAspects")
                ? AspectList.CODEC.parse(NbtOps.INSTANCE, tag.get("bonusAspects")).result().map(AspectList::copy).orElseGet(AspectList::new)
                : new AspectList();
        gatherResults();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, TileResearchTable tile) {
        if (level.isClientSide) return;
        if (tile.nextRecalc++ > 600) {
            tile.nextRecalc = 0;
            tile.recalculateBonus();
            tile.setChanged();
            tile.syncTile(false);
        }
        if (tile.note != null && tile.note.generic) {
            tile.note = ResearchNoteManager.computeNoteData(level, tile.note.key);
            if (tile.note != null) ItemResearchNote.setData(tile.getItem(1), tile.note);
            tile.syncTile(false);
        }
    }

    @Override
    public void setChanged() {
        super.setChanged();
        gatherResults();
    }

    public void gatherResults() {
        note = ItemResearchNote.getData(items.get(1));
    }

    public boolean canConsumeInkFromTable() {
        ItemStack tools = items.get(0);
        return tools.getItem() instanceof IScribeTools && tools.getDamageValue() < tools.getMaxDamage();
    }

    public void consumeInkFromTable() {
        if (canConsumeInkFromTable() && !thaumcraft.common.anomaly.AnomalyModifiers.freeInk(level)) {
            ItemStack tools = items.get(0);
            tools.setDamageValue(tools.getDamageValue() + 1);
            setChanged();
            syncTile(false);
        }
    }

    public boolean hasScribingTools() {
        return items.get(0).getItem() instanceof IScribeTools;
    }

    public boolean hasResearchNote() {
        return items.get(1).getItem() instanceof ItemResearchNote;
    }

    /**
     * Writes (or with a null aspect, erases) an aspect on a cell. Writing costs one ink and one point of the aspect,
     * taken from the table's bonus first when the player has none. Research Mastery makes one in ten free;
     * erasing gives the point back a quarter of the time with Research Expertise, half with Research Mastery.
     */
    public void placeAspect(int q, int r, @Nullable Aspect aspect, ServerPlayer player) {
        if (note == null) gatherResults();
        if (!canConsumeInkFromTable()) return;
        ItemStack noteStack = items.get(1);
        if (noteStack.getItem() instanceof ItemResearchNote && note != null && !note.isComplete()) {
            boolean r1 = ThaumcraftCapabilities.getKnowledge(player).isResearchComplete("RESEARCHEXPERTISE");
            boolean r2 = ThaumcraftCapabilities.getKnowledge(player).isResearchComplete("RESEARCHMASTERY");
            HexUtils.Hex hex = new HexUtils.Hex(q, r);
            ResearchNoteData.HexEntry current = note.hexEntries.get(hex.toString());
            if (current == null || current.type() == 1) return;
            ResearchNoteData.HexEntry he;
            if (aspect != null) {
                he = new ResearchNoteData.HexEntry(aspect, 2);
                if (r2 && level.random.nextFloat() < 0.1f) {
                    level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS,
                            0.2f, 0.9f + level.random.nextFloat() * 0.2f);
                } else if (ThaumcraftCapabilities.getAspects(player).aspectCount(aspect) <= 0) {
                    if (bonusAspects.getAmount(aspect) <= 0) return;
                    bonusAspects.remove(aspect, 1);
                } else {
                    ResearchNoteManager.spendAspect(player, aspect, 1);
                }
            } else {
                float f = level.random.nextFloat();
                if (current.aspect() != null && (r1 && f < 0.25f || r2 && f < 0.5f)) {
                    level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS,
                            0.2f, 0.9f + level.random.nextFloat() * 0.2f);
                    ResearchNoteManager.checkAndSyncAspectKnowledge(player, current.aspect(), 1);
                }
                he = new ResearchNoteData.HexEntry(null, 0);
            }
            note.hexEntries.put(hex.toString(), he);
            note.hexes.put(hex.toString(), hex);
            ItemResearchNote.setData(noteStack, note);
            consumeInkFromTable();
            if (ResearchNoteManager.checkResearchCompletion(noteStack, note, player)) {
                level.blockEvent(worldPosition, getBlockState().getBlock(), 1, 1);
            }
        }
        setChanged();
        syncTile(false);
    }

    /** One free primal point from the surroundings: darkness, height, fire, earth, water, redstone, bookshelves. */
    private void recalculateBonus() {
        int height = level.dimensionType().logicalHeight();
        int relY = worldPosition.getY() - level.getMinBuildHeight();
        if (!level.isDay() && level.getMaxLocalRawBrightness(worldPosition) < 4 && !level.canSeeSky(worldPosition) && level.random.nextInt(20) == 0) {
            bonusAspects.merge(Aspect.ENTROPY, 1);
        }
        if (relY > height * 0.5f && level.random.nextInt(20) == 0) bonusAspects.merge(Aspect.AIR, 1);
        if (relY > height * 0.66f && level.random.nextInt(20) == 0) bonusAspects.merge(Aspect.AIR, 1);
        if (relY > height * 0.75f && level.random.nextInt(20) == 0) bonusAspects.merge(Aspect.AIR, 1);

        // TODO: the vis crystals (one per primal, 1 in 10) once they are ported, and the brain in a jar.
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        for (int x = -8; x <= 8; ++x) {
            for (int z = -8; z <= 8; ++z) {
                for (int y = -8; y <= 8; ++y) {
                    p.set(worldPosition.getX() + x, worldPosition.getY() + y, worldPosition.getZ() + z);
                    if (level.isOutsideBuildHeight(p)) continue;
                    BlockState bs = level.getBlockState(p);
                    FluidState fs = bs.getFluidState();
                    // The 1.12 materials: FIRE/LAVA, GROUND, WATER, CIRCUITS/PISTON.
                    if (bs.is(BlockTags.FIRE) || fs.is(FluidTags.LAVA)) {
                        if (bonusAspects.getAmount(Aspect.FIRE) < 1 && level.random.nextInt(20) == 0) {
                            bonusAspects.merge(Aspect.FIRE, 1);
                            return;
                        }
                    } else if (bs.is(BlockTags.DIRT)) {
                        if (bonusAspects.getAmount(Aspect.EARTH) < 1 && level.random.nextInt(20) == 0) {
                            bonusAspects.merge(Aspect.EARTH, 1);
                            return;
                        }
                    } else if (fs.is(FluidTags.WATER)) {
                        if (bonusAspects.getAmount(Aspect.WATER) < 1 && level.random.nextInt(15) == 0) {
                            bonusAspects.merge(Aspect.WATER, 1);
                            return;
                        }
                    } else if (isCircuit(bs)) {
                        if (bonusAspects.getAmount(Aspect.ORDER) < 1 && level.random.nextInt(20) == 0) {
                            bonusAspects.merge(Aspect.ORDER, 1);
                            return;
                        }
                    }
                    if (bs.is(Blocks.BOOKSHELF) && level.random.nextInt(300) == 0) {
                        Aspect[] aspects = Aspect.aspects.values().toArray(new Aspect[0]);
                        bonusAspects.merge(aspects[level.random.nextInt(aspects.length)], 1);
                        return;
                    }
                }
            }
        }
    }

    /** Material.CIRCUITS / PISTON: redstone parts, torches, rails and pistons. */
    private static boolean isCircuit(BlockState bs) {
        return bs.isSignalSource() || bs.is(Blocks.REDSTONE_WIRE) || bs.is(Blocks.REPEATER) || bs.is(Blocks.COMPARATOR)
                || bs.is(BlockTags.RAILS) || bs.is(Blocks.TORCH) || bs.is(Blocks.WALL_TORCH)
                || bs.is(Blocks.PISTON) || bs.is(Blocks.STICKY_PISTON) || bs.is(Blocks.PISTON_HEAD) || bs.is(Blocks.MOVING_PISTON);
    }

    /** Block event 1: the note is solved. */
    @Override
    public boolean triggerEvent(int i, int j) {
        if (i == 1) {
            if (level.isClientSide) {
                level.playLocalSound(worldPosition, SoundsTC.LEARN.get(), SoundSource.BLOCKS, 1.0f, 1.0f, false);
            }
            return true;
        }
        return super.triggerEvent(i, j);
    }

    // --- Container ---

    @Override
    public int getContainerSize() {
        return 2;
    }

    @Override
    public boolean isEmpty() {
        return items.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack out = ContainerHelper.removeItem(items, slot, amount);
        if (!out.isEmpty()) {
            setChanged();
            syncTile(false);
        }
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
        syncTile(false);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot == 0 ? stack.getItem() instanceof IScribeTools : stack.getItem() instanceof ItemResearchNote;
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        items.clear();
    }
}
