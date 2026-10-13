package thaumcraft.common.blocks.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import thaumcraft.common.blocks.devices.BlockInfernalFurnace;
import thaumcraft.common.config.ConfigBlocks;

/**
 * Port of BlockPlaceholder: the invisible stand-ins for the nether brick and obsidian of a built infernal furnace
 * (the furnace model draws them). They drop the block they replaced (loot table) and breaking one takes the furnace
 * apart.
 * TODO: the bars, anvil, cauldron and table placeholders of the golem press (stage 8).
 */
public class BlockPlaceholder extends Block {
    public BlockPlaceholder() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(2.5f, 10.0f).sound(SoundType.STONE).noOcclusion()
                .pushReaction(PushReaction.BLOCK));
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && !BlockInfernalFurnace.ignore && !world.isClientSide) {
            search:
            for (int a = -1; a <= 1; ++a) {
                for (int b = -1; b <= 1; ++b) {
                    for (int c = -1; c <= 1; ++c) {
                        BlockState s = world.getBlockState(pos.offset(a, b, c));
                        if (s.is(ConfigBlocks.infernalFurnace.get())) {
                            BlockInfernalFurnace.destroyFurnace(world, pos.offset(a, b, c), s, pos);
                            break search;
                        }
                    }
                }
            }
        }
        super.onRemove(state, world, pos, newState, movedByPiston);
    }
}
