package thaumcraft.common.blocks.essentia;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Port of BlockSmelterVent (auxiliary venting port). Attached to the side of an essentia smeltery, each one
 * blows a third of the flux the smeltery makes out into the air instead of the aura.
 */
public class BlockSmelterVent extends BlockSmelterAux {
    public static final MapCodec<BlockSmelterVent> CODEC = simpleCodec(p -> new BlockSmelterVent());
    private static final VoxelShape NORTH = Shapes.box(0.125, 0.125, 0.0, 0.875, 0.875, 0.5);
    private static final VoxelShape SOUTH = Shapes.box(0.125, 0.125, 0.5, 0.875, 0.875, 1.0);
    private static final VoxelShape WEST = Shapes.box(0.0, 0.125, 0.125, 0.5, 0.875, 0.875);
    private static final VoxelShape EAST = Shapes.box(0.5, 0.125, 0.125, 1.0, 0.875, 0.875);

    public BlockSmelterVent() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(1.0f, 10.0f).sound(SoundType.METAL).noOcclusion());
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case SOUTH -> SOUTH;
            case WEST -> WEST;
            case EAST -> EAST;
            default -> NORTH;
        };
    }
}
