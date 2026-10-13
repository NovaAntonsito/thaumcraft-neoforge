package thaumcraft.common.tiles.essentia;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.api.aspects.IEssentiaTransport;
import thaumcraft.common.registry.TCBlockEntities;
import thaumcraft.common.tiles.TileThaumcraft;

/**
 * Port of TileAlembic. Sits on an essentia smelter and distills its slurry, one aspect per alembic, up to 128.
 * {@link #facing} is the labelled side (DOWN when there is no label); tubes can't connect there.
 */
public class TileAlembic extends TileThaumcraft implements IAspectContainer, IEssentiaTransport {
    @Nullable public Aspect aspect;
    @Nullable public Aspect aspectFilter;
    public int amount;
    public int maxAmount = 128;
    public int facing = Direction.DOWN.get3DDataValue();
    public boolean aboveFurnace;

    public TileAlembic(BlockPos pos, BlockState state) {
        super(TCBlockEntities.ALEMBIC.get(), pos, state);
    }

    @Override
    public AspectList getAspects() {
        return aspect != null ? new AspectList().add(aspect, amount) : new AspectList();
    }

    @Override
    public void setAspects(AspectList aspects) {
    }

    @Override
    protected void readSyncNBT(CompoundTag tag, HolderLookup.Provider registries) {
        facing = tag.getByte("facing");
        aspectFilter = Aspect.getAspect(tag.getString("AspectFilter"));
        aspect = Aspect.getAspect(tag.getString("aspect"));
        amount = tag.getShort("amount");
    }

    @Override
    protected void writeSyncNBT(CompoundTag tag, HolderLookup.Provider registries) {
        if (aspect != null) {
            tag.putString("aspect", aspect.getTag());
        }
        if (aspectFilter != null) {
            tag.putString("AspectFilter", aspectFilter.getTag());
        }
        tag.putShort("amount", (short) amount);
        tag.putByte("facing", (byte) facing);
    }

    @Override
    public int addToContainer(Aspect tt, int am) {
        if (aspectFilter != null && tt != aspectFilter) {
            return am;
        }
        if ((amount < maxAmount && tt == aspect) || amount == 0) {
            aspect = tt;
            int added = Math.min(am, maxAmount - amount);
            amount += added;
            am -= added;
        }
        setChanged();
        syncTile(false);
        return am;
    }

    @Override
    public boolean takeFromContainer(Aspect tt, int am) {
        if (amount == 0 || aspect == null) {
            aspect = null;
            amount = 0;
        }
        if (aspect != null && amount >= am && tt == aspect) {
            amount -= am;
            if (amount <= 0) {
                aspect = null;
                amount = 0;
            }
            setChanged();
            syncTile(false);
            return true;
        }
        return false;
    }

    @Override
    public boolean doesContainerContainAmount(Aspect tt, int am) {
        return amount >= am && tt == aspect;
    }

    @Override
    public int containerContains(Aspect tt) {
        return tt == aspect ? amount : 0;
    }

    @Override
    public boolean doesContainerAccept(Aspect tag) {
        return true;
    }

    @Override
    public boolean isConnectable(Direction face) {
        return face.get3DDataValue() != facing && face != Direction.DOWN;
    }

    @Override
    public boolean canInputFrom(Direction face) {
        return false;
    }

    @Override
    public boolean canOutputTo(Direction face) {
        return face.get3DDataValue() != facing && face != Direction.DOWN;
    }

    @Override
    public void setSuction(@Nullable Aspect aspect, int amount) {
    }

    @Nullable
    @Override
    public Aspect getSuctionType(Direction loc) {
        return null;
    }

    @Override
    public int getSuctionAmount(Direction loc) {
        return 0;
    }

    @Nullable
    @Override
    public Aspect getEssentiaType(Direction loc) {
        return aspect;
    }

    @Override
    public int getEssentiaAmount(Direction loc) {
        return amount;
    }

    @Override
    public int takeEssentia(Aspect aspect, int amount, Direction face) {
        return canOutputTo(face) && takeFromContainer(aspect, amount) ? amount : 0;
    }

    @Override
    public int addEssentia(Aspect aspect, int amount, Direction loc) {
        return 0;
    }

    @Override
    public int getMinimumSuction() {
        return 0;
    }

    /**
     * Moves one point of an aspect from the smelter at pos into the stack of alembics above it.
     * First an alembic already holding that aspect, then any free (or matching-label) one.
     */
    public static boolean processAlembics(Level world, BlockPos pos, Aspect aspect) {
        int deep = 1;
        while (true) {
            BlockEntity te = world.getBlockEntity(pos.above(deep));
            if (te instanceof TileAlembic alembic) {
                if (alembic.amount > 0 && alembic.aspect == aspect && alembic.addToContainer(aspect, 1) == 0) {
                    return true;
                }
                ++deep;
            } else {
                deep = 1;
                while (true) {
                    te = world.getBlockEntity(pos.above(deep));
                    if (!(te instanceof TileAlembic alembic2)) {
                        return false;
                    }
                    if ((alembic2.aspectFilter == null || alembic2.aspectFilter == aspect) && alembic2.addToContainer(aspect, 1) == 0) {
                        return true;
                    }
                    ++deep;
                }
            }
        }
    }
}
