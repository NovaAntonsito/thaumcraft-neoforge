package thaumcraft.common.tiles.devices;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.IEssentiaTransport;
import thaumcraft.api.aura.AuraHelper;
import thaumcraft.client.fx.ClientFX;
import thaumcraft.common.blocks.devices.BlockCondenser;
import thaumcraft.common.blocks.devices.BlockCondenserLattice;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.registry.TCBlockEntities;
import thaumcraft.common.tiles.TileThaumcraft;

/**
 * Port of TileCondenser (flux condenser). Burns essentia pulled from tubes on its sides to drain flux from the aura
 * into Vitium essentia, which tubes take from below. The lattice built on top sets how fast it works: up to 40
 * lattice blocks within reach, worth less the more neighbours each one has. Lattice gets clogged now and then and
 * clogged lattice (and anything above it) stops counting until it is cleaned with a filter.
 */
public class TileCondenser extends TileThaumcraft implements IEssentiaTransport {
    private int essentia;
    private int flux;
    private static final int MAX = 100;
    private int count = 0;
    private final List<Long> history = new ArrayList<>();
    private final List<Long> blockList = new ArrayList<>();
    private final List<Long> uncloggedList = new ArrayList<>();
    public float latticeCount = -1.0f;
    public int interval = 0;
    public int cost = 0;

    public TileCondenser(BlockPos pos, BlockState state) {
        super(TCBlockEntities.CONDENSER.get(), pos, state);
    }

    @Override
    protected void readSyncNBT(CompoundTag tag, HolderLookup.Provider registries) {
        essentia = tag.getShort("essentia");
        flux = tag.getShort("flux");
    }

    @Override
    protected void writeSyncNBT(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putShort("essentia", (short) essentia);
        tag.putShort("flux", (short) flux);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, TileCondenser te) {
        te.update();
    }

    private void update() {
        if (latticeCount < 0.0f) {
            triggerCheck();
        }
        ++count;
        if (getBlockState().getValue(BlockCondenser.ENABLED) && latticeCount > 0.0f) {
            if (level.isClientSide) {
                if (essentia > 0 && !uncloggedList.isEmpty() && count % Math.max(3, interval / 50) == 0) {
                    BlockPos p = BlockPos.of(uncloggedList.get(level.random.nextInt(uncloggedList.size())));
                    ClientFX.spark(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5, 0.33f + level.random.nextFloat() * 0.66f,
                            0.33f + level.random.nextFloat() * 0.66f, 0.33f + level.random.nextFloat() * 0.66f);
                }
            } else {
                if (count % 5 == 0 && essentia < MAX) {
                    fill();
                }
                if (interval > 0 && essentia >= cost && flux < MAX && count % interval == 0 && AuraHelper.getFlux(level, worldPosition) >= 1.0f) {
                    AuraHelper.drainFlux(level, worldPosition, 1.0f, false);
                    essentia -= cost;
                    ++flux;
                    if (level.random.nextInt(50) == 0) {
                        makeLatticeDirty();
                    }
                    syncTile(false);
                    setChanged();
                }
            }
        }
    }

    private void makeLatticeDirty() {
        if (!uncloggedList.isEmpty()) {
            int q = level.random.nextInt(uncloggedList.size());
            if (q == 0) {
                q = level.random.nextInt(uncloggedList.size());
            }
            BlockPos p = BlockPos.of(uncloggedList.get(q));
            BlockState bs = level.getBlockState(p);
            if (bs.is(ConfigBlocks.condenserLattice.get())) {
                level.setBlock(p, BlockCondenserLattice.copyConnections(bs, ConfigBlocks.condenserLatticeDirty.get().defaultBlockState()), 3);
                ((BlockCondenserLattice) bs.getBlock()).triggerUpdate(level, p);
            }
        }
    }

    /** Pulls one essentia from each side tube that offers it. Vitium arriving this way clogs the lattice instead. */
    private void fill() {
        for (Direction face : Direction.Plane.HORIZONTAL) {
            BlockEntity te = ThaumcraftApiHelper.getConnectableTile(level, worldPosition, face);
            if (te instanceof IEssentiaTransport ic) {
                Aspect ta = null;
                if (!ic.canOutputTo(face.getOpposite())) {
                    return;
                }
                if (ic.getEssentiaAmount(face.getOpposite()) > 0 && ic.getSuctionAmount(face.getOpposite()) < getSuctionAmount(face)
                        && getSuctionAmount(face) >= ic.getMinimumSuction()) {
                    ta = ic.getEssentiaType(face.getOpposite());
                }
                if (ta != null) {
                    if (ta != Aspect.FLUX) {
                        essentia += ic.takeEssentia(ta, 1, face.getOpposite());
                    } else {
                        makeLatticeDirty();
                    }
                    syncTile(false);
                    setChanged();
                    if (essentia >= MAX) {
                        break;
                    }
                }
            }
        }
    }

    public void triggerCheck() {
        history.clear();
        blockList.clear();
        uncloggedList.clear();
        latticeCount = 0.0f;
        interval = 0;
        performCheck(worldPosition, true, false);
        history.clear();
        if (latticeCount <= 0.0f) {
            latticeCount = 0.0f;
        } else {
            if (latticeCount > 40.0f) {
                latticeCount = 40.0f;
            }
            interval = Math.round(600.0f - latticeCount * 15.0f);
            if (interval < 5) {
                interval = 5;
            }
            cost = (int) (4.0 + Math.sqrt(blockList.size()));
        }
    }

    private void performCheck(BlockPos pos, boolean skip, boolean clogged) {
        if (latticeCount < 0.0f) {
            return;
        }
        history.add(pos.asLong());
        boolean found = false;
        int sides = 0;
        for (Direction face : Direction.values()) {
            if (!skip || face == Direction.UP) {
                BlockPos p2 = pos.relative(face);
                BlockState bs = level.getBlockState(p2);
                boolean lattice = bs.is(ConfigBlocks.condenserLattice.get());
                boolean latticeDirty = bs.is(ConfigBlocks.condenserLatticeDirty.get());
                if (skip && latticeDirty) {
                    clogged = true;
                }
                if (lattice || latticeDirty) {
                    ++sides;
                }
                if (!history.contains(p2.asLong())) {
                    if (face == Direction.DOWN && bs.is(ConfigBlocks.condenser.get())) {
                        latticeCount = -99.0f;
                        return;
                    }
                    if (worldPosition.getY() < p2.getY() && worldPosition.distSqr(p2) <= 74.0 && (lattice || latticeDirty)) {
                        blockList.add(p2.asLong());
                        if (lattice) {
                            uncloggedList.add(p2.asLong());
                        }
                        found = true;
                        performCheck(p2, false, clogged || latticeDirty);
                        if (skip) {
                            break;
                        }
                    }
                }
            }
        }
        if (found && !clogged) {
            latticeCount += 1.0f - 0.15f * sides;
        }
    }

    @Override
    public boolean isConnectable(Direction face) {
        return face != Direction.UP;
    }

    @Override
    public boolean canInputFrom(Direction face) {
        return face != Direction.UP && face != Direction.DOWN;
    }

    @Override
    public boolean canOutputTo(Direction face) {
        return face == Direction.DOWN;
    }

    @Override
    public void setSuction(@Nullable Aspect aspect, int amount) {
    }

    @Override
    public Aspect getSuctionType(Direction face) {
        return null;
    }

    @Override
    public int getSuctionAmount(Direction face) {
        return (face == Direction.DOWN || essentia >= MAX) ? 0 : 128;
    }

    @Override
    public int takeEssentia(Aspect aspect, int amount, Direction face) {
        int amt = (canOutputTo(face) && (aspect == null || aspect == Aspect.FLUX)) ? Math.min(amount, flux) : 0;
        if (amt > 0) {
            flux -= amt;
            syncTile(false);
            setChanged();
        }
        return amt;
    }

    /** As in the original, essentia pushed in by a tube is accepted but not stored: the condenser only fills itself. */
    @Override
    public int addEssentia(Aspect aspect, int amount, Direction face) {
        int amt = canInputFrom(face) ? Math.min(amount, MAX - essentia) : 0;
        if (amt > 0) {
            syncTile(false);
            setChanged();
        }
        return amt;
    }

    @Override
    public Aspect getEssentiaType(Direction face) {
        return Aspect.FLUX;
    }

    @Override
    public int getEssentiaAmount(Direction face) {
        return flux;
    }

    @Override
    public int getMinimumSuction() {
        return 0;
    }
}
