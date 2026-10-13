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

/** Heal effect. 1.21: particles are vanilla end rod sparkles until the FX stage. */
public class FocusEffectHeal extends FocusEffect {
    @Override
    public String getResearch() {
        return "FOCUSHEAL";
    }

    @Override
    public String getKey() {
        return "thaumcraft.HEAL";
    }

    @Override
    public Aspect getAspect() {
        return Aspect.LIFE;
    }

    @Override
    public int getComplexity() {
        return getSettingValue("power") * 4;
    }

    @Override
    public float getDamageForDisplay(float finalPower) {
        return -getSettingValue("power") * finalPower;
    }

    @Override
    public boolean execute(HitResult target, Trajectory trajectory, float finalPower, int num) {
        Level world = getPackage().world;
        TCNetwork.sendToAllAround(world, target.getLocation().x, target.getLocation().y, target.getLocation().z, 64.0,
                new PacketFXFocusPartImpact(target.getLocation().x, target.getLocation().y, target.getLocation().z, new String[] {getKey()}));
        if (target instanceof EntityHitResult ehr && ehr.getEntity() instanceof LivingEntity le) {
            if (le.getType().is(EntityTypeTags.UNDEAD)) {
                le.hurt(world.damageSources().indirectMagic(getPackage().getCaster(), getPackage().getCaster()), getSettingValue("power") * finalPower * 1.5f);
            } else {
                le.heal(getSettingValue("power") * finalPower);
            }
        }
        return false;
    }

    @Override
    public NodeSetting[] createSettings() {
        return new NodeSetting[] {new NodeSetting("power", "focus.heal.power", new NodeSetting.NodeSettingIntRange(1, 5))};
    }

    @Override
    public void onCast(Entity caster) {
        caster.level().playSound(null, caster.blockPosition().above(), SoundEvents.CHORUS_FLOWER_GROW, SoundSource.PLAYERS, 2.0f,
                2.0f + (float) (caster.level().random.nextGaussian() * 0.1));
    }

    @Override
    public void renderParticleFX(Level world, double x, double y, double z, double vx, double vy, double vz) {
        world.addParticle(ParticleTypes.END_ROD, x, y, z, (vx + world.random.nextGaussian() * 0.01) * 0.5, (vy + world.random.nextGaussian() * 0.01) * 0.5,
                (vz + world.random.nextGaussian() * 0.01) * 0.5);
    }
}
