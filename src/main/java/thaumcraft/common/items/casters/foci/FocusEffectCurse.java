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

/**
 * Curse effect. 1.21: the impact uses the focus part burst instead of PacketFXBlockBamf, and particles are dark
 * dust until the FX stage.
 */
public class FocusEffectCurse extends FocusEffect {
    @Override
    public String getResearch() {
        return "FOCUSCURSE";
    }

    @Override
    public String getKey() {
        return "thaumcraft.CURSE";
    }

    @Override
    public Aspect getAspect() {
        return Aspect.DEATH;
    }

    @Override
    public int getComplexity() {
        return getSettingValue("duration") + getSettingValue("power") * 3;
    }

    @Override
    public float getDamageForDisplay(float finalPower) {
        return (1.0f + getSettingValue("power")) * finalPower;
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
            int eff = (int) (getSettingValue("power") * finalPower / 2.0f);
            if (eff < 0) {
                eff = 0;
            }
            hit.hurt(world.damageSources().indirectMagic(hit, getPackage().getCaster()), damage);
            if (hit instanceof LivingEntity le) {
                le.addEffect(new MobEffectInstance(MobEffects.POISON, duration, eff));
                float c = 0.85f;
                if (world.random.nextFloat() < c) {
                    le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, eff));
                    c -= 0.15f;
                }
                if (world.random.nextFloat() < c) {
                    le.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, eff));
                    c -= 0.15f;
                }
                if (world.random.nextFloat() < c) {
                    le.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, duration * 2, eff));
                    c -= 0.15f;
                }
                if (world.random.nextFloat() < c) {
                    le.addEffect(new MobEffectInstance(MobEffects.HUNGER, duration * 3, eff));
                    c -= 0.15f;
                }
                if (world.random.nextFloat() < c) {
                    le.addEffect(new MobEffectInstance(MobEffects.UNLUCK, duration * 3, eff));
                }
            }
        } else if (target instanceof BlockHitResult bhr && target.getType() == HitResult.Type.BLOCK) {
            float f = (float) Math.min(8.0, 1.5 * getSettingValue("power") * finalPower);
            int fi = Mth.floor(f);
            BlockState sap = thaumcraft.common.config.ConfigBlocks.effectSap.get().defaultBlockState();
            for (BlockPos p : BlockPos.betweenClosed(bhr.getBlockPos().offset(-fi, -fi, -fi), bhr.getBlockPos().offset(fi, fi, fi))) {
                if (p.distToCenterSqr(target.getLocation()) <= f * f && world.isEmptyBlock(p.above())
                        && world.getBlockState(p).isCollisionShapeFullBlock(world, p)) {
                    world.setBlockAndUpdate(p.above(), sap);
                }
            }
        }
        return false;
    }

    @Override
    public NodeSetting[] createSettings() {
        return new NodeSetting[] {new NodeSetting("power", "focus.common.power", new NodeSetting.NodeSettingIntRange(1, 5)),
                new NodeSetting("duration", "focus.common.duration", new NodeSetting.NodeSettingIntRange(1, 10))};
    }

    @Override
    public void renderParticleFX(Level world, double posX, double posY, double posZ, double motionX, double motionY, double motionZ) {
        DustParticleOptions dust = new DustParticleOptions(new Vector3f(0.41f + world.random.nextFloat() * 0.2f, 0.0f, 0.019f + world.random.nextFloat() * 0.2f),
                1.0f + world.random.nextFloat() * 2.0f);
        world.addParticle(dust, posX, posY, posZ, motionX, motionY, motionZ);
    }

    @Override
    public void onCast(Entity caster) {
        caster.level().playSound(null, caster.blockPosition().above(), SoundEvents.ELDER_GUARDIAN_CURSE, SoundSource.PLAYERS, 0.15f,
                1.0f + caster.level().random.nextFloat() / 2.0f);
    }
}
