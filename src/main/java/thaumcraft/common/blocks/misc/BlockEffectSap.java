package thaumcraft.common.blocks.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3f;
import thaumcraft.common.lib.events.ServerEvents;

/**
 * Port of BlockEffect for effect_sap, the sapping field the curse focus leaves on the ground. Entities inside it get
 * wither, slowness and hunger; it vanishes on its next random tick.
 * 1.21: a separate class, not marked as air (chunk sections never random tick air blocks). The original sparks are
 * drawn with dust particles until the FX stage; IEldritchMob is not ported yet, so eldritch mobs are not exempt.
 */
public class BlockEffectSap extends Block {
    public BlockEffectSap() {
        super(BlockBehaviour.Properties.of().replaceable().noCollission().noOcclusion().noLootTable().randomTicks()
                .explosionResistance(999.0f).lightLevel(s -> 7));
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected void entityInside(BlockState state, Level world, BlockPos pos, Entity entity) {
        if (!world.isClientSide && entity instanceof LivingEntity le && !le.hasEffect(MobEffects.WITHER)) {
            ServerEvents.addRunnableServer(world, () -> {
                le.addEffect(new MobEffectInstance(MobEffects.WITHER, 40, 0, true, true));
                le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1, true, true));
                le.addEffect(new MobEffectInstance(MobEffects.HUNGER, 40, 1, true, true));
            }, 0);
        }
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource rand) {
        world.removeBlock(pos, false);
    }

    @Override
    public void animateTick(BlockState state, Level w, BlockPos pos, RandomSource r) {
        float h = r.nextFloat() * 0.33f;
        w.addParticle(new DustParticleOptions(new Vector3f(0.3f - r.nextFloat() * 0.1f, 0.0f, 0.5f + r.nextFloat() * 0.2f), 0.6f + h),
                pos.getX() + r.nextFloat(), pos.getY() + 0.1515f + h / 2.0f, pos.getZ() + r.nextFloat(), 0, 0, 0);
    }
}
