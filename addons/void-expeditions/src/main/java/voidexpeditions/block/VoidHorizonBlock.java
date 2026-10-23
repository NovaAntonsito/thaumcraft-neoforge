package voidexpeditions.block;

import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import voidexpeditions.run.Expeditions;

/**
 * The void horizon that fills a formed gate (see {@link GateStructure}): walked into, it starts an expedition with
 * the settings of the gate's terminal. It cannot be broken or picked; it goes when its gate collapses.
 */
public class VoidHorizonBlock extends Block {
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;
    private static final VoxelShape X_SHAPE = Block.box(0, 0, 6, 16, 16, 10);
    private static final VoxelShape Z_SHAPE = Block.box(6, 0, 0, 10, 16, 16);

    public VoidHorizonBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(AXIS, Direction.Axis.X));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AXIS);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(AXIS) == Direction.Axis.X ? X_SHAPE : Z_SHAPE;
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!(entity instanceof ServerPlayer player) || player.isOnPortalCooldown() || player.isPassenger()) return;
        player.setPortalCooldown(60);
        ExpeditionTerminalBlockEntity terminal = findTerminal(level, pos);
        if (terminal == null) {
            player.displayClientMessage(Component.translatable("voidexpeditions.gate.no_terminal")
                    .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC), true);
            return;
        }
        Expeditions.start(player, terminal.getDifficulty(), terminal.getLength());
    }

    /** The terminal linked to this horizon's gate. */
    @Nullable
    private static ExpeditionTerminalBlockEntity findTerminal(Level level, BlockPos pos) {
        int r = 2 * GateStructure.RADIUS + GateStructure.TERMINAL_DISTANCE;
        for (BlockPos p : BlockPos.betweenClosed(pos.offset(-r, -r, -r), pos.offset(r, r, r))) {
            if (level.getBlockEntity(p) instanceof ExpeditionTerminalBlockEntity t && t.isLinked()
                    && t.getGateCenter().distManhattan(pos) <= 2 * GateStructure.RADIUS) {
                return t;
            }
        }
        return null;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        super.onRemove(state, level, pos, newState, movedByPiston);
        if (!newState.is(this)) GateStructure.collapse(level, pos);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(3) != 0) return;
        boolean x = state.getValue(AXIS) == Direction.Axis.X;
        double px = pos.getX() + (x ? random.nextDouble() : 0.5);
        double pz = pos.getZ() + (x ? 0.5 : random.nextDouble());
        double out = (random.nextDouble() - 0.5) * 0.6;
        level.addParticle(ParticleTypes.REVERSE_PORTAL, px, pos.getY() + random.nextDouble(), pz, x ? 0 : out, 0.02, x ? out : 0);
    }
}
