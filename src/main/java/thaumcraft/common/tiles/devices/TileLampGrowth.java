package thaumcraft.common.tiles.devices;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.PacketDistributor;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.IEssentiaTransport;
import thaumcraft.common.blocks.devices.BlockLamp;
import thaumcraft.common.lib.network.fx.PacketFXBlockMist;
import thaumcraft.common.lib.utils.CropUtils;
import thaumcraft.common.registry.TCBlockEntities;
import thaumcraft.common.tiles.TileThaumcraft;

/**
 * Port of TileLampGrowth: draws Herba essentia through its stand and spends a charge each time it hurries a
 * plant within 6 blocks. Holds 20 charges plus one essentia in reserve.
 */
public class TileLampGrowth extends TileThaumcraft implements IEssentiaTransport {
    private boolean reserve = false;
    public int charges = -1;
    public int maxCharges = 20;
    @Nullable private BlockPos last;
    @Nullable private BlockState lastState;
    private final List<BlockPos> checklist = new ArrayList<>();
    private int drawDelay = 0;

    public TileLampGrowth(BlockPos pos, BlockState state) {
        super(TCBlockEntities.LAMP_GROWTH.get(), pos, state);
    }

    private Direction facing() {
        return getBlockState().getValue(BlockLamp.FACING);
    }

    public void serverTick() {
        if (charges <= 0) {
            if (reserve) {
                charges = maxCharges;
                reserve = false;
                setChanged();
                syncTile(true);
            } else if (drawEssentia()) {
                charges = maxCharges;
                setChanged();
                syncTile(true);
            }
            BlockState state = getBlockState();
            if (charges <= 0) {
                if (state.getValue(BlockLamp.ENABLED)) {
                    level.setBlock(worldPosition, state.setValue(BlockLamp.ENABLED, false), 3);
                }
            } else if (!gettingPower() && !state.getValue(BlockLamp.ENABLED)) {
                level.setBlock(worldPosition, state.setValue(BlockLamp.ENABLED, true), 3);
            }
        }
        if (!reserve && drawEssentia()) {
            reserve = true;
        }
        if (charges == 0) {
            charges = -1;
            syncTile(true);
        }
        if (!gettingPower() && charges > 0) {
            updatePlant();
        }
    }

    /** IGrowable, or the CACTUS and PLANTS materials of 1.12, but never a grass block. */
    boolean isPlant(BlockPos bp) {
        BlockState b = level.getBlockState(bp);
        boolean flag = b.getBlock() instanceof BonemealableBlock;
        boolean plant = b.getBlock() instanceof BushBlock || b.getBlock() instanceof CactusBlock || b.getBlock() instanceof SugarCaneBlock;
        return (flag || plant) && !b.is(Blocks.GRASS_BLOCK);
    }

    private void updatePlant() {
        if (last != null) {
            BlockState bs = level.getBlockState(last);
            if (bs != lastState) {
                Player p = level.getNearestPlayer(last.getX(), last.getY(), last.getZ(), 32.0, false);
                if (p != null && level instanceof ServerLevel server) {
                    PacketDistributor.sendToPlayersNear(server, null, last.getX(), last.getY(), last.getZ(), 32.0,
                            new PacketFXBlockMist(last, 0x40FF40));
                }
                lastState = bs;
            }
        }
        int distance = 6;
        if (checklist.isEmpty()) {
            for (int a = -distance; a <= distance; ++a) {
                for (int b = -distance; b <= distance; ++b) {
                    checklist.add(worldPosition.offset(a, distance, b));
                }
            }
            Collections.shuffle(checklist, new java.util.Random(level.random.nextLong()));
        }
        BlockPos first = checklist.remove(0);
        int x = first.getX();
        int y = first.getY();
        int z = first.getZ();
        while (y >= worldPosition.getY() - distance) {
            BlockPos bp = new BlockPos(x, y, z);
            if (!level.isEmptyBlock(bp) && isPlant(bp) && worldPosition.distToCenterSqr(x + 0.5, y + 0.5, z + 0.5) < distance * distance
                    && !CropUtils.isGrownCrop(level, bp) && CropUtils.doesLampGrow(level, bp)) {
                --charges;
                last = bp;
                BlockState bs2 = level.getBlockState(bp);
                lastState = bs2;
                // scheduleUpdate ran the growth tick in 1.12; plants grow on random ticks now.
                if (level instanceof ServerLevel server) {
                    RandomSource rand = server.random;
                    bs2.randomTick(server, bp, rand);
                }
                return;
            }
            --y;
        }
    }

    @Override
    protected void readSyncNBT(CompoundTag tag, HolderLookup.Provider registries) {
        reserve = tag.getBoolean("reserve");
        charges = tag.getInt("charges");
    }

    @Override
    protected void writeSyncNBT(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putBoolean("reserve", reserve);
        tag.putInt("charges", charges);
    }

    boolean drawEssentia() {
        if (++drawDelay % 5 != 0) {
            return false;
        }
        Direction facing = facing();
        BlockEntity te = ThaumcraftApiHelper.getConnectableTile(level, worldPosition, facing);
        if (te instanceof IEssentiaTransport ic) {
            if (!ic.canOutputTo(facing.getOpposite())) {
                return false;
            }
            return ic.getSuctionAmount(facing.getOpposite()) < getSuctionAmount(facing)
                    && ic.takeEssentia(Aspect.PLANT, 1, facing.getOpposite()) == 1;
        }
        return false;
    }

    @Override
    public boolean isConnectable(Direction face) {
        return face == facing();
    }

    @Override
    public boolean canInputFrom(Direction face) {
        return face == facing();
    }

    @Override
    public boolean canOutputTo(Direction face) {
        return false;
    }

    @Override
    public void setSuction(@Nullable Aspect aspect, int amount) {
    }

    @Override
    public int getMinimumSuction() {
        return 0;
    }

    @Override
    public Aspect getSuctionType(Direction face) {
        return Aspect.PLANT;
    }

    @Override
    public int getSuctionAmount(Direction face) {
        return (face == facing() && (!reserve || charges <= 0)) ? 128 : 0;
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
        return 0;
    }
}
