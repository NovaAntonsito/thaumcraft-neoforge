package thaumcraft.common.blocks.essentia;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
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
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import thaumcraft.client.fx.ClientFX;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.registry.TCBlockEntities;
import thaumcraft.common.tiles.devices.TileJarBrain;

/**
 * The brain in a jar side of BlockJar. Shaking it (right click) makes the brain give up some of the experience it
 * has eaten; it keeps the rest when broken (the loot table copies the xp component). It counts as five bookshelves
 * for an enchanting table and sparks when full.
 */
public class BlockJarBrain extends BaseEntityBlock {
    public static final MapCodec<BlockJarBrain> CODEC = simpleCodec(p -> new BlockJarBrain());

    public BlockJarBrain() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.NONE).strength(0.3f).sound(SoundsTC.SOUND_TYPE_JAR).noOcclusion());
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
        return new TileJarBrain(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, TCBlockEntities.JAR_BRAIN.get(), TileJarBrain::tick);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (!(world.getBlockEntity(pos) instanceof TileJarBrain te)) {
            return InteractionResult.PASS;
        }
        te.eatDelay = 40;
        if (!world.isClientSide) {
            int amount = world.random.nextInt(Math.min(te.xp + 1, 64));
            if (amount > 0) {
                te.xp -= amount;
                ExperienceOrb.award((ServerLevel) world, Vec3.atCenterOf(pos), amount);
                te.setChanged();
                te.syncTile(false);
            }
        } else {
            world.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundsTC.JAR.get(), SoundSource.BLOCKS, 0.2f, 1.0f, false);
        }
        return InteractionResult.sidedSuccess(world.isClientSide);
    }

    @Override
    public float getEnchantPowerBonus(BlockState state, LevelReader level, BlockPos pos) {
        return 5.0f;
    }

    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource rand) {
        if (world.getBlockEntity(pos) instanceof TileJarBrain te && te.xp >= TileJarBrain.XP_MAX) {
            thaumcraft.client.fx.FXDispatcher.spark(pos.getX() + 0.5f, pos.getY() + 0.8f, pos.getZ() + 0.5f, 3.0f, 0.2f + rand.nextFloat() * 0.2f, 1.0f,
                    0.3f + rand.nextFloat() * 0.2f, 0.5f);
        }
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof TileJarBrain te) {
            float r = te.xp / (float) TileJarBrain.XP_MAX;
            return Mth.floor(r * 14.0f) + (te.xp > 0 ? 1 : 0);
        }
        return 0;
    }
}
