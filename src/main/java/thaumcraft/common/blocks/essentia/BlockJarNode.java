package thaumcraft.common.blocks.essentia;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.tiles.misc.TileJarNode;

/**
 * The node jar of Thaumcraft 4 (BlockJar metadata 2): a jar holding a frozen aura node. It glows (light 11). The node
 * goes with the jar when broken (the loot table copies the node_data component); a wand lets it out.
 */
public class BlockJarNode extends BaseEntityBlock {
    public static final MapCodec<BlockJarNode> CODEC = simpleCodec(p -> new BlockJarNode());

    public BlockJarNode() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.NONE).strength(0.3f).sound(SoundsTC.SOUND_TYPE_JAR).noOcclusion()
                .lightLevel(s -> 11));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return BlockJar.SHAPE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileJarNode(pos, state);
    }

    /** receiveClientEvent 9: the sparkle around a newly made jar. */
    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource rand) {
        if (world.getBlockEntity(pos) instanceof TileJarNode te && te.animate > System.currentTimeMillis()) {
            for (int i = 0; i < 6; ++i) {
                thaumcraft.client.fx.FXDispatcher.sparkle(pos.getX() - 1 + rand.nextFloat() * 3, pos.getY() - 1 + rand.nextFloat() * 4,
                        pos.getZ() - 1 + rand.nextFloat() * 3, 1.0f, 0.5f + rand.nextFloat() * 0.5f, 1.0f);
            }
        }
    }
}
