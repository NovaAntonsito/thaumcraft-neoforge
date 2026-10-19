package thaumcraft.common.blocks.world;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import thaumcraft.common.items.casters.wands.ItemWand;
import thaumcraft.common.items.tools.ItemThaumometer;
import thaumcraft.common.lib.utils.EntityUtils;
import thaumcraft.common.registry.TCBlockEntities;
import thaumcraft.common.tiles.misc.TileAuraNode;

/**
 * The node part of the Thaumcraft 4 BlockAiry: an aura node in the air. It has no collision and cannot be broken.
 * It can be targeted (a small box in the middle) only by a player holding a wand or a thaumometer or wearing
 * something that reveals nodes, so that it does not get in the way otherwise.
 */
public class BlockAuraNode extends BaseEntityBlock {
    public static final MapCodec<BlockAuraNode> CODEC = simpleCodec(p -> new BlockAuraNode());
    private static final VoxelShape SHAPE = box(4.8, 4.8, 4.8, 11.2, 11.2, 11.2);

    public BlockAuraNode() {
        super(BlockBehaviour.Properties.of().strength(-1.0f, 3600000.0f).noCollission().noOcclusion().noLootTable()
                .lightLevel(s -> 6).pushReaction(PushReaction.BLOCK).isViewBlocking((s, w, p) -> false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (context instanceof EntityCollisionContext ec && ec.getEntity() instanceof Player p
                && (p.getMainHandItem().getItem() instanceof ItemWand || p.getMainHandItem().getItem() instanceof ItemThaumometer
                || p.getOffhandItem().getItem() instanceof ItemThaumometer || EntityUtils.hasRevealer(p))) {
            return SHAPE;
        }
        return Shapes.empty();
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
        return ItemStack.EMPTY;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileAuraNode(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, TCBlockEntities.AURA_NODE.get(), level.isClientSide ? TileAuraNode::clientTick : TileAuraNode::serverTick);
    }
}
