package voidexpeditions.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import thaumcraft.api.casters.IInteractWithCaster;

/** A segment of the void gate's ring (see {@link GateStructure}). Lit while its ring is formed. */
public class VoidGateRingBlock extends Block implements IInteractWithCaster {
    public static final BooleanProperty FORMED = BooleanProperty.create("formed");

    public VoidGateRingBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FORMED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FORMED);
    }

    @Override
    public boolean onCasterRightClick(Level world, ItemStack casterStack, Player player, BlockPos pos, Direction side, InteractionHand hand) {
        if (world.getBlockState(pos).getValue(FORMED)) return false;
        return GateStructure.tryFormFromSegment(world, pos, player);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        boolean wasFormed = state.getValue(FORMED);
        super.onRemove(state, level, pos, newState, movedByPiston);
        if (wasFormed && !newState.is(this)) GateStructure.collapse(level, pos);
    }
}
