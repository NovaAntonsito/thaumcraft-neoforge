package thaumcraft.common.tiles.devices;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.IEssentiaTransport;
import thaumcraft.common.blocks.devices.BlockLamp;
import thaumcraft.common.registry.TCBlockEntities;
import thaumcraft.common.tiles.TileThaumcraft;

/**
 * Port of TileLampFertility: draws Desiderium essentia (up to 10 charges) and every 15 seconds puts a pair of
 * adult animals of the same kind within 7 blocks in love, for 5 charges. It stops when there are more than 9 of
 * that kind nearby.
 */
public class TileLampFertility extends TileThaumcraft implements IEssentiaTransport {
    public int charges = 0;
    private int count = 0;
    private int drawDelay = 0;

    public TileLampFertility(BlockPos pos, BlockState state) {
        super(TCBlockEntities.LAMP_FERTILITY.get(), pos, state);
    }

    private Direction facing() {
        return getBlockState().getValue(BlockLamp.FACING);
    }

    public void serverTick() {
        if (charges < 10) {
            if (drawEssentia()) {
                ++charges;
                setChanged();
                syncTile(true);
            }
            BlockState state = getBlockState();
            if (charges <= 1) {
                if (state.getValue(BlockLamp.ENABLED)) {
                    level.setBlock(worldPosition, state.setValue(BlockLamp.ENABLED, false), 3);
                }
            } else if (!gettingPower() && !state.getValue(BlockLamp.ENABLED)) {
                level.setBlock(worldPosition, state.setValue(BlockLamp.ENABLED, true), 3);
            }
        }
        if (!gettingPower() && charges > 1 && count++ % 300 == 0) {
            updateAnimals();
        }
    }

    private void updateAnimals() {
        int distance = 7;
        List<Animal> var5 = level.getEntitiesOfClass(Animal.class, new AABB(worldPosition).inflate(distance));
        for (Animal var8 : var5) {
            if (var8.getAge() != 0 || var8.isInLove()) {
                continue;
            }
            List<Animal> sa = new ArrayList<>();
            for (Animal var9 : var5) {
                if (var9.getClass().equals(var8.getClass())) {
                    sa.add(var9);
                }
            }
            if (sa.size() > 9) {
                continue;
            }
            Animal partner = null;
            for (Animal var11 : sa) {
                if (var11.getAge() != 0 || var11.isInLove()) {
                    continue;
                }
                if (partner != null) {
                    charges -= 5;
                    var11.setInLove(null);
                    partner.setInLove(null);
                    return;
                }
                partner = var11;
            }
        }
    }

    @Override
    protected void readSyncNBT(CompoundTag tag, HolderLookup.Provider registries) {
        charges = tag.getInt("charges");
    }

    @Override
    protected void writeSyncNBT(CompoundTag tag, HolderLookup.Provider registries) {
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
                    && ic.takeEssentia(Aspect.DESIRE, 1, facing.getOpposite()) == 1;
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
        return Aspect.DESIRE;
    }

    @Override
    public int getSuctionAmount(Direction face) {
        return face == facing() ? 128 - charges * 10 : 0;
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
