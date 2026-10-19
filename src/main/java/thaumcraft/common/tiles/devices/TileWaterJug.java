package thaumcraft.common.tiles.devices;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import thaumcraft.api.aura.AuraHelper;
import thaumcraft.common.registry.TCBlockEntities;
import thaumcraft.common.tiles.TileThaumcraft;

/**
 * Port of TileWaterJug. Refills its 1000 mB of water from the aura (1 vis per 1000 mB). Every quarter second it
 * looks at one spot of the 5x3x5 area around it, remembers fluid tanks and cauldrons, and pours 25 mB into the first
 * tank that takes it, or a third of a bucket into a cauldron that is not full.
 * <p>
 * 1.21: an empty cauldron is its own block, so both kinds count. The Botania petal apothecary is not supported.
 */
public class TileWaterJug extends TileThaumcraft {
    int zone = 0;
    int counter = 0;
    List<Integer> handlers = new ArrayList<>();
    int zc = 0;
    int tcount = 0;

    public final FluidTank tank = new FluidTank(1000, fs -> fs.is(Fluids.WATER));

    /** The IFluidHandler of the urn: water can be drained from it, never added. Only offered on the top face. */
    public final IFluidHandler handler = new IFluidHandler() {
        @Override
        public int getTanks() {
            return 1;
        }

        @Override
        public FluidStack getFluidInTank(int t) {
            return tank.getFluid();
        }

        @Override
        public int getTankCapacity(int t) {
            return tank.getCapacity();
        }

        @Override
        public boolean isFluidValid(int t, FluidStack stack) {
            return stack.is(Fluids.WATER);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return 0;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            return drained(tank.drain(resource, action), action);
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            return drained(tank.drain(maxDrain, action), action);
        }
    };

    public TileWaterJug(BlockPos pos, BlockState state) {
        super(TCBlockEntities.EVERFULL_URN.get(), pos, state);
    }

    /** The original drain(): resync when the urn stops being full, so the splashes stop. */
    private FluidStack drained(FluidStack fs, IFluidHandler.FluidAction action) {
        if (action.execute() && !fs.isEmpty()) {
            setChanged();
            if (tank.getFluidAmount() + fs.getAmount() >= tank.getCapacity()) {
                syncTile(false);
            }
        }
        return fs;
    }

    public void drainWater(int amount) {
        handler.drain(new FluidStack(Fluids.WATER, amount), IFluidHandler.FluidAction.EXECUTE);
    }

    @Override
    protected void readSyncNBT(CompoundTag tag, HolderLookup.Provider registries) {
        tank.readFromNBT(registries, tag);
    }

    @Override
    protected void writeSyncNBT(CompoundTag tag, HolderLookup.Provider registries) {
        tank.writeToNBT(registries, tag);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        handlers = new ArrayList<>();
        ListTag list = tag.getList("handlers", Tag.TAG_INT);
        for (int i = 0; i < list.size(); ++i) {
            handlers.add(list.getInt(i));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        // The original wrote an empty list here (a bug); the list is saved for real.
        ListTag list = new ListTag();
        for (int h : handlers) {
            list.add(IntTag.valueOf(h));
        }
        tag.put("handlers", list);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, TileWaterJug tile) {
        tile.update();
    }

    private static BlockPos zonePos(BlockPos origin, int z) {
        return origin.offset(z / 5 % 5 - 2, z / 5 / 5 % 3 - 1, z % 5 - 2);
    }

    private boolean isHandler(BlockPos p) {
        BlockState bs = level.getBlockState(p);
        if (bs.is(Blocks.CAULDRON) || bs.is(Blocks.WATER_CAULDRON)) {
            return true;
        }
        return !p.equals(worldPosition) && level.getBlockEntity(p) != null
                && level.getCapability(Capabilities.FluidHandler.BLOCK, p, Direction.UP) != null;
    }

    private void update() {
        ++counter;
        if (level.isClientSide) {
            if (tcount > 0) {
                if (tcount % 5 == 0) {
                    thaumcraft.client.fx.FXDispatcher.waterTrailFx(worldPosition, zonePos(worldPosition, zc), counter, 2650102, 0.1f);
                }
                --tcount;
            }
            return;
        }
        if (counter % 5 != 0) {
            return;
        }
        ++zone;
        if (isHandler(zonePos(worldPosition, zone)) && !handlers.contains(zone)) {
            handlers.add(zone);
            setChanged();
        }
        int i = 0;
        while (i < handlers.size() && tank.getFluidAmount() >= 25) {
            int zz = handlers.get(i);
            BlockPos p = zonePos(worldPosition, zz);
            BlockState bs2 = level.getBlockState(p);
            IFluidHandler fh = p.equals(worldPosition) ? null : level.getCapability(Capabilities.FluidHandler.BLOCK, p, Direction.UP);
            if (fh != null) {
                int q = fh.fill(new FluidStack(Fluids.WATER, 25), IFluidHandler.FluidAction.EXECUTE);
                if (q > 0) {
                    drainWater(q);
                    setChanged();
                    level.blockEvent(worldPosition, getBlockState().getBlock(), 1, zz);
                    break;
                }
            } else {
                boolean cauldron = bs2.is(Blocks.CAULDRON) || bs2.is(Blocks.WATER_CAULDRON);
                if (!cauldron || tank.getFluidAmount() < 333) {
                    handlers.remove(i);
                    setChanged();
                    continue;
                }
                if (bs2.is(Blocks.CAULDRON) || bs2.getValue(LayeredCauldronBlock.LEVEL) < 3) {
                    BlockState next = bs2.is(Blocks.CAULDRON) ? Blocks.WATER_CAULDRON.defaultBlockState()
                            : bs2.cycle(LayeredCauldronBlock.LEVEL);
                    level.setBlock(p, next, 2);
                    level.updateNeighbourForOutputSignal(p, next.getBlock());
                    level.blockEvent(worldPosition, getBlockState().getBlock(), 1, zz);
                    drainWater(333);
                }
            }
            ++i;
        }
        if (tank.getFluidAmount() < 1000) {
            float da = (1000 - tank.getFluidAmount()) / 1000.0f;
            if (da > 0.1f) {
                da = 0.1f;
            }
            float dv = AuraHelper.drainVis(level, worldPosition, da, false);
            int wa = (int) (1000.0f * dv);
            if (wa > 0) {
                tank.fill(new FluidStack(Fluids.WATER, wa), IFluidHandler.FluidAction.EXECUTE);
                setChanged();
                if (tank.getFluidAmount() >= tank.getCapacity()) {
                    syncTile(false);
                }
            }
        }
    }

    @Override
    public boolean triggerEvent(int i, int j) {
        if (i == 1) {
            if (level.isClientSide) {
                zc = j;
                tcount = 5;
            }
            return true;
        }
        return super.triggerEvent(i, j);
    }
}
