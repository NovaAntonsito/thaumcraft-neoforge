package thaumcraft.common.tiles.essentia;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.IEssentiaTransport;
import thaumcraft.api.casters.IInteractWithCaster;
import thaumcraft.client.fx.ClientFX;
import thaumcraft.common.blocks.essentia.BlockTube;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.registry.TCBlockEntities;
import thaumcraft.common.tiles.TileThaumcraft;

/**
 * Port of TileTube. Holds at most one point of essentia and passes it towards the strongest suction around it.
 * {@link #openSides} are the sides a caster has left connected; {@link #facing} is used by the valve and the
 * directional tube.
 */
public class TileTube extends TileThaumcraft implements IEssentiaTransport, IInteractWithCaster {
    public static int freq = 5;
    public Direction facing = Direction.NORTH;
    public boolean[] openSides = {true, true, true, true, true, true};
    @Nullable Aspect essentiaType;
    int essentiaAmount;
    @Nullable Aspect suctionType;
    int suction;
    int venting;
    int count;
    int ventColor;

    public TileTube(BlockPos pos, BlockState state) {
        this(TCBlockEntities.TUBE.get(), pos, state);
    }

    protected TileTube(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    protected void readSyncNBT(CompoundTag tag, HolderLookup.Provider registries) {
        essentiaType = Aspect.getAspect(tag.getString("type"));
        essentiaAmount = tag.getInt("amount");
        facing = Direction.from3DDataValue(tag.getInt("side"));
        byte[] sides = tag.getByteArray("open");
        if (sides.length == 6) {
            for (int a = 0; a < 6; ++a) {
                openSides[a] = sides[a] == 1;
            }
        }
        markRenderDirty();
    }

    @Override
    protected void writeSyncNBT(CompoundTag tag, HolderLookup.Provider registries) {
        if (essentiaType != null) {
            tag.putString("type", essentiaType.getTag());
        }
        tag.putInt("amount", essentiaAmount);
        byte[] sides = new byte[6];
        for (int a = 0; a < 6; ++a) {
            sides[a] = (byte) (openSides[a] ? 1 : 0);
        }
        tag.putInt("side", facing.get3DDataValue());
        tag.putByteArray("open", sides);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        suctionType = Aspect.getAspect(tag.getString("stype"));
        suction = tag.getInt("samount");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (suctionType != null) {
            tag.putString("stype", suctionType.getTag());
        }
        tag.putInt("samount", suction);
    }

    /** The client redraws the block when the synced data changes (filter color, valve facing). */
    protected void markRenderDirty() {
        if (level != null && level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 8);
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState state, TileTube te) {
        te.update();
    }

    public void update() {
        if (venting > 0) {
            --venting;
        }
        if (count == 0) {
            count = level.random.nextInt(10);
        }
        if (!level.isClientSide) {
            if (venting <= 0) {
                if (++count % 2 == 0) {
                    calculateSuction(null, false, false);
                    checkVenting();
                    if (essentiaType != null && essentiaAmount == 0) {
                        essentiaType = null;
                    }
                }
                if (count % 5 == 0 && suction > 0) {
                    equalizeWithNeighbours(false);
                }
            }
            // 1.12 worked the connections out on every redraw (getActualState); here they live in the block state.
            if (count % 10 == 0) {
                BlockTube.refreshConnections(level, worldPosition);
            }
        } else if (venting > 0) {
            Random r = new Random(hashCode() * 4L);
            float rp = r.nextFloat() * 360.0f;
            float ry = r.nextFloat() * 360.0f;
            double fx = -Mth.sin(ry / 180.0f * Mth.PI) * Mth.cos(rp / 180.0f * Mth.PI);
            double fz = Mth.cos(ry / 180.0f * Mth.PI) * Mth.cos(rp / 180.0f * Mth.PI);
            double fy = -Mth.sin(rp / 180.0f * Mth.PI);
            ClientFX.drawVentParticles(level, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5,
                    fx / 5.0, fy / 5.0, fz / 5.0, ventColor);
        }
    }

    void calculateSuction(@Nullable Aspect filter, boolean restrict, boolean directional) {
        suction = 0;
        suctionType = null;
        for (Direction loc : Direction.values()) {
            if (directional && facing != loc.getOpposite()) {
                continue;
            }
            if (!isConnectable(loc)) {
                continue;
            }
            BlockEntity te = ThaumcraftApiHelper.getConnectableTile(level, worldPosition, loc);
            if (te == null) {
                continue;
            }
            IEssentiaTransport ic = (IEssentiaTransport) te;
            Aspect theirs = ic.getSuctionType(loc.getOpposite());
            if (filter != null && theirs != null && theirs != filter) {
                continue;
            }
            if (filter == null && getEssentiaAmount(loc) > 0 && theirs != null && getEssentiaType(loc) != theirs) {
                continue;
            }
            if (filter != null && getEssentiaAmount(loc) > 0 && getEssentiaType(loc) != null && theirs != null && getEssentiaType(loc) != theirs) {
                continue;
            }
            int suck = ic.getSuctionAmount(loc.getOpposite());
            if (suck > 0 && suck > suction + 1) {
                Aspect st = theirs;
                if (st == null) {
                    st = filter;
                }
                setSuction(st, restrict ? suck / 2 : suck - 1);
            }
        }
    }

    void checkVenting() {
        for (Direction loc : Direction.values()) {
            if (!isConnectable(loc)) {
                continue;
            }
            BlockEntity te = ThaumcraftApiHelper.getConnectableTile(level, worldPosition, loc);
            if (te != null) {
                IEssentiaTransport ic = (IEssentiaTransport) te;
                int suck = ic.getSuctionAmount(loc.getOpposite());
                if (suction > 0 && (suck == suction || suck == suction - 1) && suctionType != ic.getSuctionType(loc.getOpposite())
                        && !(te instanceof TileTubeFilter)) {
                    int c = -1;
                    if (suctionType != null) {
                        c = aspectIndex(suctionType);
                    }
                    level.blockEvent(worldPosition, getBlockState().getBlock(), 1, c);
                    venting = 40;
                }
            }
        }
    }

    void equalizeWithNeighbours(boolean directional) {
        if (essentiaAmount > 0) {
            return;
        }
        for (Direction loc : Direction.values()) {
            if (directional && facing == loc.getOpposite()) {
                continue;
            }
            if (!isConnectable(loc)) {
                continue;
            }
            BlockEntity te = ThaumcraftApiHelper.getConnectableTile(level, worldPosition, loc);
            if (te == null) {
                continue;
            }
            IEssentiaTransport ic = (IEssentiaTransport) te;
            if (!ic.canOutputTo(loc.getOpposite())) {
                continue;
            }
            Aspect theirs = ic.getEssentiaType(loc.getOpposite());
            if ((getSuctionType(null) == null || getSuctionType(null) == theirs || theirs == null)
                    && getSuctionAmount(null) > ic.getSuctionAmount(loc.getOpposite()) && getSuctionAmount(null) >= ic.getMinimumSuction()) {
                Aspect a = getSuctionType(null);
                if (a == null) {
                    a = theirs;
                    if (a == null) {
                        a = ic.getEssentiaType(null);
                    }
                }
                if (a == null) {
                    continue;
                }
                int am = addEssentia(a, ic.takeEssentia(a, 1, loc.getOpposite()), loc);
                if (am > 0) {
                    if (level.random.nextInt(100) == 0) {
                        level.blockEvent(worldPosition, getBlockState().getBlock(), 0, 0);
                    }
                    return;
                }
            }
        }
    }

    /** ModConfig.aspectOrder of the original: the registration order. */
    static int aspectIndex(Aspect aspect) {
        int i = 0;
        for (Aspect a : Aspect.aspects.values()) {
            if (a == aspect) {
                return i;
            }
            ++i;
        }
        return -1;
    }

    @Nullable
    static Aspect aspectByIndex(int index) {
        int i = 0;
        for (Aspect a : Aspect.aspects.values()) {
            if (i++ == index) {
                return a;
            }
        }
        return null;
    }

    @Override
    public boolean isConnectable(@Nullable Direction face) {
        return face != null && openSides[face.get3DDataValue()];
    }

    @Override
    public boolean canInputFrom(@Nullable Direction face) {
        return face != null && openSides[face.get3DDataValue()];
    }

    @Override
    public boolean canOutputTo(@Nullable Direction face) {
        return face != null && openSides[face.get3DDataValue()];
    }

    @Override
    public void setSuction(@Nullable Aspect aspect, int amount) {
        suctionType = aspect;
        suction = amount;
    }

    @Nullable
    @Override
    public Aspect getSuctionType(@Nullable Direction loc) {
        return suctionType;
    }

    @Override
    public int getSuctionAmount(@Nullable Direction loc) {
        return suction;
    }

    @Nullable
    @Override
    public Aspect getEssentiaType(@Nullable Direction loc) {
        return essentiaType;
    }

    @Override
    public int getEssentiaAmount(@Nullable Direction loc) {
        return essentiaAmount;
    }

    @Override
    public int takeEssentia(Aspect aspect, int amount, Direction face) {
        if (canOutputTo(face) && essentiaType == aspect && essentiaAmount > 0 && amount > 0) {
            --essentiaAmount;
            if (essentiaAmount <= 0) {
                essentiaType = null;
            }
            setChanged();
            return 1;
        }
        return 0;
    }

    @Override
    public int addEssentia(Aspect aspect, int amount, Direction face) {
        if (canInputFrom(face) && essentiaAmount == 0 && amount > 0) {
            essentiaType = aspect;
            ++essentiaAmount;
            setChanged();
            return 1;
        }
        return 0;
    }

    @Override
    public int getMinimumSuction() {
        return 0;
    }

    /** Block event 0: the creak of essentia moving. 1: gas venting, j is the aspect index (-1 untyped). */
    @Override
    public boolean triggerEvent(int i, int j) {
        if (i == 0) {
            if (level.isClientSide) {
                level.playLocalSound(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, SoundsTC.CREAK.get(),
                        SoundSource.AMBIENT, 1.0f, 1.3f + level.random.nextFloat() * 0.2f, false);
            }
            return true;
        }
        if (i == 1) {
            if (level.isClientSide) {
                if (venting <= 0) {
                    level.playLocalSound(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, SoundEvents.LAVA_EXTINGUISH,
                            SoundSource.BLOCKS, 0.1f, 1.0f + level.random.nextFloat() * 0.1f, false);
                }
                venting = 50;
                Aspect a = j == -1 ? null : aspectByIndex(j);
                ventColor = a == null ? 0xAAAAAA : a.getColor();
            }
            return true;
        }
        return super.triggerEvent(i, j);
    }

    @Override
    public boolean onCasterRightClick(Level world, ItemStack casterStack, Player player, BlockPos bp, Direction side, InteractionHand hand) {
        int hit = pickPart(player);
        if (hit < 0) {
            return false;
        }
        if (hit < 6) {
            world.playLocalSound(bp.getX() + 0.5, bp.getY() + 0.5, bp.getZ() + 0.5, SoundsTC.TOOL.get(), SoundSource.BLOCKS, 0.5f,
                    0.9f + world.random.nextFloat() * 0.2f, false);
            player.swing(hand);
            toggleSide(hit);
            return true;
        }
        if (hit == 6) {
            world.playLocalSound(bp.getX() + 0.5, bp.getY() + 0.5, bp.getZ() + 0.5, SoundsTC.TOOL.get(), SoundSource.BLOCKS, 0.5f,
                    0.9f + world.random.nextFloat() * 0.2f, false);
            player.swing(hand);
            int a = facing.ordinal();
            setChanged();
            while (++a < 20) {
                Direction d = Direction.values()[a % 6];
                if (canFaceCenterTo(d)) {
                    facing = d;
                    syncTile(true);
                    setChanged();
                    if (!world.isClientSide) {
                        BlockTube.refreshAround(world, worldPosition);
                    }
                    break;
                }
            }
            return true;
        }
        return false;
    }

    /** Which way the core can point when cycled with a caster. */
    protected boolean canFaceCenterTo(Direction d) {
        return canConnectSide(d.getOpposite()) && isConnectable(d.getOpposite());
    }

    /** Opens or closes one side, here and on a tube next to it, as clicking on that arm with a caster does. */
    protected void toggleSide(int side) {
        setChanged();
        openSides[side] = !openSides[side];
        syncTile(true);
        Direction dir = Direction.from3DDataValue(side);
        BlockEntity tile = level.getBlockEntity(worldPosition.relative(dir));
        if (tile instanceof TileTube tube) {
            tube.openSides[dir.getOpposite().get3DDataValue()] = openSides[side];
            tube.syncTile(true);
            tile.setChanged();
        }
        if (!level.isClientSide) {
            BlockTube.refreshAround(level, worldPosition);
        }
    }

    public boolean canConnectSide(Direction side) {
        return level != null && level.getBlockEntity(worldPosition.relative(side)) instanceof IEssentiaTransport;
    }

    /**
     * The clickable parts, as the 1.12 IndexedCuboid6 list: 0-5 an arm towards that side (when something that
     * moves essentia is there), 6 the core. Local coordinates.
     */
    public List<AABB> getTraceableCuboids(BlockGetter world) {
        List<AABB> cuboids = new ArrayList<>(7);
        double min = 0.375;
        double max = 0.625;
        for (int i = 0; i < 6; ++i) {
            cuboids.add(null);
        }
        if (connectsTo(world, Direction.DOWN)) cuboids.set(0, new AABB(min, 0, min, max, 0.375, max));
        if (connectsTo(world, Direction.UP)) cuboids.set(1, new AABB(min, 0.625, min, max, 1, max));
        if (connectsTo(world, Direction.NORTH)) cuboids.set(2, new AABB(min, min, 0, max, max, 0.375));
        if (connectsTo(world, Direction.SOUTH)) cuboids.set(3, new AABB(min, min, 0.625, max, max, 1));
        if (connectsTo(world, Direction.WEST)) cuboids.set(4, new AABB(0, min, min, 0.375, max, max));
        if (connectsTo(world, Direction.EAST)) cuboids.set(5, new AABB(0.625, min, min, 1, max, max));
        cuboids.add(new AABB(0.375, 0.375, 0.375, 0.625, 0.625, 0.625));
        return cuboids;
    }

    protected boolean connectsTo(BlockGetter world, Direction side) {
        return world.getBlockEntity(worldPosition.relative(side)) instanceof IEssentiaTransport;
    }

    /** RayTracer.retraceBlock: the index of the part the player is looking at, or -1. */
    public int pickPart(Player player) {
        return pickPart(level, worldPosition, getTraceableCuboids(level), player);
    }

    public static int pickPart(BlockGetter world, BlockPos pos, List<AABB> cuboids, Player player) {
        Vec3 eye = player.getEyePosition(1.0f).subtract(pos.getX(), pos.getY(), pos.getZ());
        Vec3 end = eye.add(player.getViewVector(1.0f).scale(player.blockInteractionRange() + 1.0));
        int best = -1;
        double bestDist = Double.MAX_VALUE;
        for (int i = 0; i < cuboids.size(); ++i) {
            AABB box = cuboids.get(i);
            if (box == null) {
                continue;
            }
            var hit = box.clip(eye, end);
            if (hit.isPresent()) {
                double d = hit.get().distanceToSqr(eye);
                if (d < bestDist) {
                    bestDist = d;
                    best = i;
                }
            }
        }
        return best;
    }
}
