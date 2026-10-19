package thaumcraft.common.items.casters.foci;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.joml.Vector3f;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.casters.FocusEffect;
import thaumcraft.api.casters.NodeSetting;
import thaumcraft.api.casters.Trajectory;
import thaumcraft.common.lib.network.fx.PacketFXFocusPartImpact;
import thaumcraft.common.network.TCNetwork;

/** Frost effect. 1.21: particles are vanilla snowflakes until the FX stage. */
public class FocusEffectFrost extends FocusEffect {
    @Override
    public String getResearch() {
        return "FOCUSELEMENTAL";
    }

    @Override
    public String getKey() {
        return "thaumcraft.FROST";
    }

    @Override
    public Aspect getAspect() {
        return Aspect.COLD;
    }

    @Override
    public int getComplexity() {
        return getSettingValue("duration") + getSettingValue("power") * 2;
    }

    @Override
    public float getDamageForDisplay(float finalPower) {
        return (3 + getSettingValue("power")) * finalPower;
    }

    @Override
    public boolean execute(HitResult target, Trajectory trajectory, float finalPower, int num) {
        Level world = getPackage().world;
        TCNetwork.sendToAllAround(world, target.getLocation().x, target.getLocation().y, target.getLocation().z, 64.0,
                new PacketFXFocusPartImpact(target.getLocation().x, target.getLocation().y, target.getLocation().z, new String[] {getKey()}));
        if (target instanceof EntityHitResult ehr) {
            Entity hit = ehr.getEntity();
            float damage = getDamageForDisplay(finalPower);
            int duration = 20 * getSettingValue("duration");
            int potency = (int) (1.0f + getSettingValue("power") * finalPower / 3.0f);
            hit.hurt(world.damageSources().thrown(hit, getPackage().getCaster()), damage);
            if (hit instanceof LivingEntity le) {
                le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, potency));
            }
        } else if (target instanceof BlockHitResult bhr && target.getType() == HitResult.Type.BLOCK) {
            float f = Math.min(16.0f, 2 * getSettingValue("power") * finalPower);
            int fi = Mth.floor(f);
            BlockState frosted = Blocks.FROSTED_ICE.defaultBlockState();
            for (BlockPos p : BlockPos.betweenClosed(bhr.getBlockPos().offset(-fi, -fi, -fi), bhr.getBlockPos().offset(fi, fi, fi))) {
                if (p.distToCenterSqr(target.getLocation()) <= f * f) {
                    BlockState bs = world.getBlockState(p);
                    if (!bs.is(Blocks.WATER) || !bs.getFluidState().isSource() || !frosted.canSurvive(world, p)
                            || !world.isUnobstructed(frosted, p, net.minecraft.world.phys.shapes.CollisionContext.empty())) {
                        continue;
                    }
                    world.setBlockAndUpdate(p, frosted);
                    world.scheduleTick(p.immutable(), Blocks.FROSTED_ICE, Mth.nextInt(world.random, 60, 120));
                }
            }
        }
        return false;
    }

    @Override
    public NodeSetting[] createSettings() {
        return new NodeSetting[] {new NodeSetting("power", "focus.common.power", new NodeSetting.NodeSettingIntRange(1, 5)),
                new NodeSetting("duration", "focus.common.duration", new NodeSetting.NodeSettingIntRange(2, 10))};
    }

    @Override
    public void renderParticleFX(Level world, double posX, double posY, double posZ, double motionX, double motionY, double motionZ) {
        thaumcraft.client.fx.FocusFX.frost(posX, posY, posZ, motionX, motionY, motionZ);
    }

    @Override
    public void onCast(Entity caster) {
        caster.level().playSound(null, caster.blockPosition().above(), SoundEvents.ZOMBIE_VILLAGER_CURE, SoundSource.PLAYERS, 0.2f,
                1.0f + (float) (caster.level().random.nextGaussian() * 0.05));
    }
}
