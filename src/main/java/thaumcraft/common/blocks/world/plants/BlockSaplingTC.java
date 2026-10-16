package thaumcraft.common.blocks.world.plants;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import thaumcraft.common.world.objects.WorldGenGreatwoodTrees;
import thaumcraft.common.world.objects.WorldGenSilverwoodTrees;

/**
 * Port of BlockSaplingTC. Greatwood needs four saplings in a square; silverwood grows from one.
 * Bone meal only works one time in four.
 */
public class BlockSaplingTC extends BushBlock implements BonemealableBlock {
    public static final MapCodec<BlockSaplingTC> CODEC = simpleCodec(p -> new BlockSaplingTC(false));
    public static final IntegerProperty STAGE = IntegerProperty.create("stage", 0, 1);
    protected static final VoxelShape SAPLING_AABB = Block.box(1.6, 0.0, 1.6, 14.4, 12.8, 14.4);
    private final boolean silverwood;

    public BlockSaplingTC(boolean silverwood) {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollission().randomTicks().instabreak()
                .sound(SoundType.GRASS).pushReaction(PushReaction.DESTROY));
        this.silverwood = silverwood;
        registerDefaultState(stateDefinition.any().setValue(STAGE, 0));
    }

    @Override
    protected MapCodec<? extends BushBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STAGE);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SAPLING_AABB;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource rand) {
        if (worldIn.getMaxLocalRawBrightness(pos.above()) >= 9 && rand.nextInt(7) == 0) {
            grow(worldIn, pos, state, rand);
        }
    }

    public void grow(ServerLevel worldIn, BlockPos pos, BlockState state, RandomSource rand) {
        if (state.getValue(STAGE) == 0) {
            worldIn.setBlock(pos, state.cycle(STAGE), Block.UPDATE_INVISIBLE);
        } else {
            generateTree(worldIn, pos, state, rand);
        }
    }

    /** TerrainGen.saplingGrowTree (the Forge event) is not fired: NeoForge's version needs a configured feature. */
    public void generateTree(ServerLevel worldIn, BlockPos pos, BlockState state, RandomSource rand) {
        int i = 0;
        int j = 0;
        boolean flag = false;
        boolean found = false;
        if (!silverwood) {
            search:
            for (i = 0; i >= -1; --i) {
                for (j = 0; j >= -1; --j) {
                    if (isTwoByTwoOfType(worldIn, pos, i, j, this)) {
                        flag = true;
                        found = true;
                        break search;
                    }
                }
            }
            if (!found) {
                return;
            }
        } else {
            i = 0;
            j = 0;
        }
        BlockState air = Blocks.AIR.defaultBlockState();
        if (flag) {
            worldIn.setBlock(pos.offset(i, 0, j), air, Block.UPDATE_INVISIBLE);
            worldIn.setBlock(pos.offset(i + 1, 0, j), air, Block.UPDATE_INVISIBLE);
            worldIn.setBlock(pos.offset(i, 0, j + 1), air, Block.UPDATE_INVISIBLE);
            worldIn.setBlock(pos.offset(i + 1, 0, j + 1), air, Block.UPDATE_INVISIBLE);
        } else {
            worldIn.setBlock(pos, air, Block.UPDATE_INVISIBLE);
        }
        boolean grown = silverwood
                ? new WorldGenSilverwoodTrees(true, 7, 4).generate(worldIn, rand, pos.offset(i, 0, j))
                : new WorldGenGreatwoodTrees(true, false).generate(worldIn, rand, pos.offset(i, 0, j));
        if (!grown) {
            if (flag) {
                worldIn.setBlock(pos.offset(i, 0, j), state, Block.UPDATE_INVISIBLE);
                worldIn.setBlock(pos.offset(i + 1, 0, j), state, Block.UPDATE_INVISIBLE);
                worldIn.setBlock(pos.offset(i, 0, j + 1), state, Block.UPDATE_INVISIBLE);
                worldIn.setBlock(pos.offset(i + 1, 0, j + 1), state, Block.UPDATE_INVISIBLE);
            } else {
                worldIn.setBlock(pos.offset(i, 0, j), state, Block.UPDATE_INVISIBLE);
            }
        }
    }

    private boolean isTwoByTwoOfType(Level worldIn, BlockPos pos, int x, int z, Block type) {
        return isTypeAt(worldIn, pos.offset(x, 0, z), type) && isTypeAt(worldIn, pos.offset(x + 1, 0, z), type)
                && isTypeAt(worldIn, pos.offset(x, 0, z + 1), type) && isTypeAt(worldIn, pos.offset(x + 1, 0, z + 1), type);
    }

    public boolean isTypeAt(Level worldIn, BlockPos pos, Block type) {
        return worldIn.getBlockState(pos).getBlock() == type;
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public boolean isBonemealSuccess(Level worldIn, RandomSource rand, BlockPos pos, BlockState state) {
        return worldIn.random.nextFloat() < 0.25;
    }

    @Override
    public void performBonemeal(ServerLevel worldIn, RandomSource rand, BlockPos pos, BlockState state) {
        grow(worldIn, pos, state, rand);
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 60;
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 30;
    }
}
