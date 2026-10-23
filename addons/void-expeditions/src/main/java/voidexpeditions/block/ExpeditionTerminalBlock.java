package voidexpeditions.block;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import thaumcraft.api.casters.ICaster;
import thaumcraft.api.casters.IInteractWithCaster;
import voidexpeditions.client.ClientHooks;

/**
 * The void gate's dialing device, part of its multiblock (see {@link GateStructure}): sets the difficulty and length
 * of the expeditions through it. A caster on it forms the gate; breaking it collapses the gate.
 */
public class ExpeditionTerminalBlock extends BaseEntityBlock implements IInteractWithCaster {
    public static final MapCodec<ExpeditionTerminalBlock> CODEC = simpleCodec(ExpeditionTerminalBlock::new);

    public ExpeditionTerminalBlock(Properties properties) {
        super(properties);
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
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ExpeditionTerminalBlockEntity(pos, state);
    }

    @Override
    public boolean onCasterRightClick(Level world, ItemStack casterStack, Player player, BlockPos pos, Direction side, InteractionHand hand) {
        if (world.getBlockEntity(pos) instanceof ExpeditionTerminalBlockEntity t && t.isLinked()) return false;
        return GateStructure.tryFormFromTerminal(world, pos, player);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        BlockPos gate = !newState.is(this) && level.getBlockEntity(pos) instanceof ExpeditionTerminalBlockEntity t ? t.getGateCenter() : null;
        super.onRemove(state, level, pos, newState, movedByPiston);
        if (gate != null) GateStructure.collapse(level, gate);
    }

    /** With a caster in hand the click goes to the caster, which forms the gate. */
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                              InteractionHand hand, BlockHitResult hit) {
        if (stack.getItem() instanceof ICaster) return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide && level.getBlockEntity(pos) instanceof ExpeditionTerminalBlockEntity terminal) {
            ClientHooks.openTerminal(terminal);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
