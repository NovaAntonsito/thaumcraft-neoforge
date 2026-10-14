package thaumcraft.common.golems.parts;

import java.util.HashMap;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.api.golems.IGolemAPI;
import thaumcraft.api.golems.parts.GolemLeg;

public class GolemLegWheels implements GolemLeg.ILegFunction {
    public static HashMap<Integer, Float> ani = new HashMap<>();

    @Override
    public void onUpdateTick(IGolemAPI golem) {
        LivingEntity g = golem.getGolemEntity();
        if (golem.getGolemWorld().isClientSide) {
            double dist = Math.sqrt(g.distanceToSqr(g.xo, g.yo, g.zo));
            float lastRot = 0.0f;
            if (ani.containsKey(g.getId())) {
                lastRot = ani.get(g.getId());
            }
            double d0 = g.getX() - g.xo;
            double d3 = g.getZ() - g.zo;
            float dirTravel = (float) (Math.atan2(d3, d0) * 180.0 / Math.PI) - 90.0f;
            double dir = 360.0f - (g.getYRot() - dirTravel);
            lastRot += (float) (dist / 1.571 * dir);
            if (lastRot > 360.0f) {
                lastRot -= 360.0f;
            }
            ani.put(g.getId(), lastRot);
            if (g.onGround() && !g.isInWater() && dist > 0.25) {
                BlockPos blockpos = new BlockPos(Mth.floor(g.getX()), Mth.floor(g.getY() - 0.2), Mth.floor(g.getZ()));
                BlockState state = g.level().getBlockState(blockpos);
                if (state.getRenderShape() != RenderShape.INVISIBLE) {
                    g.level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, state),
                            g.getX() + (golem.getGolemWorld().random.nextFloat() - 0.5) * g.getBbWidth(), g.getBoundingBox().minY + 0.1,
                            g.getZ() + (golem.getGolemWorld().random.nextFloat() - 0.5) * g.getBbWidth(),
                            -g.getDeltaMovement().x * 4.0, 1.5, -g.getDeltaMovement().z * 4.0);
                }
            }
        }
    }
}
