package thaumcraft.common.tiles.essentia;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectSource;
import thaumcraft.api.aspects.IEssentiaTransport;
import thaumcraft.common.registry.TCBlockEntities;
import thaumcraft.common.registry.TCDataComponents;
import thaumcraft.common.tiles.TileThaumcraft;

/**
 * Port of TileJarFillable (warded jar): holds up to 250 essentia of one aspect.
 * A label locks it to {@link #aspectFilter}; a brass brace sets {@link #blocked}.
 * The 1.12 TileJar base only added the render box, so it was folded in here.
 */
public class TileJarFillable extends TileThaumcraft implements IAspectSource, IEssentiaTransport {
    public static final int CAPACITY = 250;

    @Nullable public Aspect aspect;
    @Nullable public Aspect aspectFilter;
    public int amount;
    /** The side the label faces, as a 3D data value (the 1.12 EnumFacing ordinal). */
    public int facing = 2;
    public boolean blocked;
    int count;

    public TileJarFillable(BlockPos pos, BlockState state) {
        this(TCBlockEntities.JAR.get(), pos, state);
    }

    protected TileJarFillable(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    protected void readSyncNBT(CompoundTag tag, HolderLookup.Provider registries) {
        aspect = Aspect.getAspect(tag.getString("Aspect"));
        aspectFilter = Aspect.getAspect(tag.getString("AspectFilter"));
        amount = tag.getShort("Amount");
        facing = tag.getByte("facing");
        blocked = tag.getBoolean("blocked");
    }

    @Override
    protected void writeSyncNBT(CompoundTag tag, HolderLookup.Provider registries) {
        if (aspect != null) {
            tag.putString("Aspect", aspect.getTag());
        }
        if (aspectFilter != null) {
            tag.putString("AspectFilter", aspectFilter.getTag());
        }
        tag.putShort("Amount", (short) amount);
        tag.putByte("facing", (byte) facing);
        tag.putBoolean("blocked", blocked);
    }

    // ---- item <-> block (BlockJarItem.placeBlockAt and BlockJar.spawnFilledJar) ----

    @Override
    protected void applyImplicitComponents(DataComponentInput input) {
        super.applyImplicitComponents(input);
        setAspects(input.get(TCDataComponents.ASPECTS.get()));
        Aspect filter = input.get(TCDataComponents.ASPECT_FILTER.get());
        if (filter != null) {
            aspectFilter = filter;
        }
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        if (amount > 0 && aspect != null) {
            components.set(TCDataComponents.ASPECTS.get(), new AspectList().add(aspect, amount));
        }
        if (aspectFilter != null) {
            components.set(TCDataComponents.ASPECT_FILTER.get(), aspectFilter);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void removeComponentsFromTag(CompoundTag tag) {
        tag.remove("Aspect");
        tag.remove("AspectFilter");
        tag.remove("Amount");
    }

    // ---- IAspectContainer ----

    @Override
    public AspectList getAspects() {
        AspectList al = new AspectList();
        if (aspect != null && amount > 0) {
            al.add(aspect, amount);
        }
        return al;
    }

    @Override
    public void setAspects(@Nullable AspectList aspects) {
        if (aspects != null && aspects.size() > 0) {
            aspect = aspects.getAspectsSortedByAmount()[0];
            amount = aspects.getAmount(aspects.getAspectsSortedByAmount()[0]);
        }
    }

    @Override
    public int addToContainer(Aspect tt, int am) {
        if (am == 0) {
            return am;
        }
        if ((amount < CAPACITY && tt == aspect) || amount == 0) {
            aspect = tt;
            int added = Math.min(am, CAPACITY - amount);
            amount += added;
            am -= added;
        }
        syncTile(false);
        setChanged();
        return am;
    }

    @Override
    public boolean takeFromContainer(Aspect tt, int am) {
        if (amount >= am && tt == aspect) {
            amount -= am;
            if (amount <= 0) {
                aspect = null;
                amount = 0;
            }
            syncTile(false);
            setChanged();
            return true;
        }
        return false;
    }

    @Override
    public boolean doesContainerContainAmount(Aspect tag, int amt) {
        return amount >= amt && tag == aspect;
    }

    @Override
    public int containerContains(Aspect tag) {
        return tag == aspect ? amount : 0;
    }

    @Override
    public boolean doesContainerAccept(Aspect tag) {
        return aspectFilter == null || tag.equals(aspectFilter);
    }

    // ---- IEssentiaTransport ----

    @Override
    public boolean isConnectable(Direction face) {
        return face == Direction.UP;
    }

    @Override
    public boolean canInputFrom(Direction face) {
        return face == Direction.UP;
    }

    @Override
    public boolean canOutputTo(Direction face) {
        return face == Direction.UP;
    }

    @Override
    public void setSuction(@Nullable Aspect aspect, int amount) {
    }

    @Override
    public int getMinimumSuction() {
        return aspectFilter != null ? 64 : 32;
    }

    @Nullable
    @Override
    public Aspect getSuctionType(Direction loc) {
        return aspectFilter != null ? aspectFilter : aspect;
    }

    @Override
    public int getSuctionAmount(Direction loc) {
        if (amount >= CAPACITY) {
            return 0;
        }
        if (aspectFilter != null) {
            return 64;
        }
        return 32;
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
    public int addEssentia(Aspect aspect, int amount, Direction face) {
        return canInputFrom(face) ? amount - addToContainer(aspect, amount) : 0;
    }

    // ---- tick ----

    public static void tick(Level level, BlockPos pos, BlockState state, TileJarFillable te) {
        te.update();
    }

    protected void update() {
        if (!level.isClientSide && ++count % 5 == 0 && amount < CAPACITY) {
            fillJar();
        }
    }

    /** Draws essentia from a connected tube or device above. */
    void fillJar() {
        BlockEntity te = ThaumcraftApiHelper.getConnectableTile(level, worldPosition, Direction.UP);
        if (te != null) {
            IEssentiaTransport ic = (IEssentiaTransport) te;
            if (!ic.canOutputTo(Direction.DOWN)) {
                return;
            }
            Aspect ta = null;
            if (aspectFilter != null) {
                ta = aspectFilter;
            } else if (aspect != null && amount > 0) {
                ta = aspect;
            } else if (ic.getEssentiaAmount(Direction.DOWN) > 0 && ic.getSuctionAmount(Direction.DOWN) < getSuctionAmount(Direction.UP)
                    && getSuctionAmount(Direction.UP) >= ic.getMinimumSuction()) {
                ta = ic.getEssentiaType(Direction.DOWN);
            }
            if (ta != null && ic.getSuctionAmount(Direction.DOWN) < getSuctionAmount(Direction.UP)) {
                addToContainer(ta, ic.takeEssentia(ta, 1, Direction.DOWN));
            }
        }
    }

    @Override
    public boolean isBlocked() {
        return blocked;
    }
}
