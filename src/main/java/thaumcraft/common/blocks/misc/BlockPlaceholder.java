package thaumcraft.common.blocks.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import thaumcraft.common.blocks.crafting.BlockGolemBuilder;
import thaumcraft.common.blocks.devices.BlockInfernalFurnace;
import thaumcraft.common.config.ConfigBlocks;

/**
 * Port of BlockPlaceholder: the invisible stand-ins for the blocks of a built multiblock (the multiblock's renderer
 * draws them). They drop the block they replaced (loot table).
 * Nether brick and obsidian belong to an infernal furnace: breaking one takes the furnace apart.
 * Bars, anvil, cauldron and table belong to a golem press: clicking one opens the press, breaking one takes it apart.
 */
public class BlockPlaceholder extends Block {
    private final boolean furnace;

    public BlockPlaceholder() {
        this(true, 0);
    }

    /** furnace: part of an infernal furnace, otherwise of a golem press. The cauldron gives light 13. */
    public BlockPlaceholder(boolean furnace, int light) {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(2.5f, 10.0f).sound(SoundType.STONE).noOcclusion()
                .pushReaction(PushReaction.BLOCK).lightLevel(s -> light));
        this.furnace = furnace;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (world.isClientSide) {
            return furnace ? InteractionResult.PASS : InteractionResult.SUCCESS;
        }
        if (!furnace) {
            for (int a = -1; a <= 1; ++a) {
                for (int b = -1; b <= 1; ++b) {
                    for (int c = -1; c <= 1; ++c) {
                        if (world.getBlockState(pos.offset(a, b, c)).is(ConfigBlocks.golemBuilder.get())) {
                            BlockGolemBuilder.openGui(world, pos.offset(a, b, c), player);
                            return InteractionResult.SUCCESS;
                        }
                    }
                }
            }
        }
        return super.useWithoutItem(state, world, pos, player, hit);
    }

    @Override
    protected void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && !world.isClientSide) {
            search:
            for (int a = -1; a <= 1; ++a) {
                for (int b = -1; b <= 1; ++b) {
                    for (int c = -1; c <= 1; ++c) {
                        BlockPos p = pos.offset(a, b, c);
                        BlockState s = world.getBlockState(p);
                        if (furnace && !BlockInfernalFurnace.ignore && s.is(ConfigBlocks.infernalFurnace.get())) {
                            BlockInfernalFurnace.destroyFurnace(world, p, s, pos);
                            break search;
                        }
                        if (!furnace && !BlockGolemBuilder.ignore && s.is(ConfigBlocks.golemBuilder.get())) {
                            BlockGolemBuilder.destroy(world, p, s, pos);
                            break search;
                        }
                    }
                }
            }
        }
        super.onRemove(state, world, pos, newState, movedByPiston);
    }
}
