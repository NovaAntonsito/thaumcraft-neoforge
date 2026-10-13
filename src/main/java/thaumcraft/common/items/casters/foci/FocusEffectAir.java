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

/** Air effect. 1.21: particles are vanilla clouds until the FX stage. */
public class FocusEffectAir extends FocusEffect {
    @Override
    public String getResearch() {
        return "FOCUSELEMENTAL";
    }

    @Override
    public String getKey() {
        return "thaumcraft.AIR";
    }

    @Override
    public Aspect getAspect() {
        return Aspect.AIR;
    }

    @Override
    public int getComplexity() {
        return getSettingValue("power") * 2;
    }

    @Override
    public float getDamageForDisplay(float finalPower) {
        return (1 + getSettingValue("power")) * finalPower;
    }

    @Override
    public boolean execute(HitResult target, Trajectory trajectory, float finalPower, int num) {
        Level world = getPackage().world;
        TCNetwork.sendToAllAround(world, target.getLocation().x, target.getLocation().y, target.getLocation().z, 64.0,
                new PacketFXFocusPartImpact(target.getLocation().x, target.getLocation().y, target.getLocation().z, new String[] {getKey()}));
        world.playSound(null, target.getLocation().x, target.getLocation().y, target.getLocation().z, SoundEvents.ENDER_DRAGON_FLAP, SoundSource.PLAYERS, 0.5f, 0.66f);
        if (target instanceof EntityHitResult ehr) {
            Entity hit = ehr.getEntity();
            float damage = getDamageForDisplay(finalPower);
            hit.hurt(world.damageSources().thrown(hit, getPackage().getCaster()), damage);
            if (hit instanceof LivingEntity le) {
                if (trajectory != null) {
                    le.knockback(damage * 0.25f, -trajectory.direction.x, -trajectory.direction.z);
                } else {
                    le.knockback(damage * 0.25f, -Mth.sin(hit.getYRot() * 0.017453292f), Mth.cos(hit.getYRot() * 0.017453292f));
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public NodeSetting[] createSettings() {
        return new NodeSetting[] {new NodeSetting("power", "focus.common.power", new NodeSetting.NodeSettingIntRange(1, 5))};
    }

    @Override
    public void renderParticleFX(Level world, double posX, double posY, double posZ, double motionX, double motionY, double motionZ) {
        world.addParticle(ParticleTypes.CLOUD, posX, posY, posZ, motionX * 0.5, motionY * 0.5 + 0.02, motionZ * 0.5);
    }

    @Override
    public void onCast(Entity caster) {
        caster.level().playSound(null, caster.blockPosition().above(), thaumcraft.common.lib.SoundsTC.WIND.get(), SoundSource.PLAYERS, 0.125f, 2.0f);
    }
}
