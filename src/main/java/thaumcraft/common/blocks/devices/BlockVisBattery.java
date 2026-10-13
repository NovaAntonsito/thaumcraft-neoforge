package thaumcraft.common.blocks.devices;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.MapColor;
import thaumcraft.api.aura.AuraHelper;

/**
 * Port of BlockVisBattery (vis battery). Soaks up vis while the aura is above 90% of its base and gives it back when
 * the aura drops below 75%; a redstone signal empties it into the aura quickly. Holds 10 vis. Light and comparator
 * output follow the charge.
 */
public class BlockVisBattery extends Block {
    public static final IntegerProperty CHARGE = IntegerProperty.create("charge", 0, 10);

    public BlockVisBattery() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(0.5f).sound(SoundType.STONE).randomTicks()
                .lightLevel(s -> s.getValue(CHARGE)));
        registerDefaultState(stateDefinition.any().setValue(CHARGE, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CHARGE);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource rand) {
        update(state, worldIn, pos, rand);
    }

    /** updateTick in 1.12 served both the random and the scheduled ticks. */
    @Override
    protected void tick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource rand) {
        update(state, worldIn, pos, rand);
    }

    private void update(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource rand) {
        int charge = state.getValue(CHARGE);
        if (worldIn.hasNeighborSignal(pos)) {
            if (charge > 0) {
                AuraHelper.addVis(worldIn, pos, 1.0f);
                worldIn.setBlockAndUpdate(pos, state.setValue(CHARGE, charge - 1));
                worldIn.scheduleTick(pos, this, 5);
            }
        } else {
            float aura = AuraHelper.getVis(worldIn, pos);
            int base = AuraHelper.getAuraBase(worldIn, pos);
            if (charge < 10 && aura > base * 0.9 && aura > 1.0f) {
                AuraHelper.drainVis(worldIn, pos, 1.0f, false);
                worldIn.setBlockAndUpdate(pos, state.setValue(CHARGE, charge + 1));
                worldIn.scheduleTick(pos, this, 100 + rand.nextInt(100));
            } else if (charge > 0 && aura < base * 0.75) {
                AuraHelper.addVis(worldIn, pos, 1.0f);
                worldIn.setBlockAndUpdate(pos, state.setValue(CHARGE, charge - 1));
                worldIn.scheduleTick(pos, this, 20 + rand.nextInt(20));
            }
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        if (worldIn.hasNeighborSignal(pos)) {
            worldIn.scheduleTick(pos, this, 1);
        }
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos) {
        return state.getValue(CHARGE);
    }
}
