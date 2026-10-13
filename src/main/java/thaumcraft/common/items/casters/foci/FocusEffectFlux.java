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

/** Flux effect. 1.21: particles are purple dust until the FX stage. */
public class FocusEffectFlux extends FocusEffect {
    @Override
    public String getResearch() {
        return "FOCUSFLUX";
    }

    @Override
    public String getKey() {
        return "thaumcraft.FLUX";
    }

    @Override
    public Aspect getAspect() {
        return Aspect.FLUX;
    }

    @Override
    public int getComplexity() {
        return getSettingValue("power") * 3;
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
            hit.hurt(world.damageSources().indirectMagic(hit, getPackage().getCaster()), getDamageForDisplay(finalPower));
        }
        return false;
    }

    @Override
    public NodeSetting[] createSettings() {
        return new NodeSetting[] {new NodeSetting("power", "focus.common.power", new NodeSetting.NodeSettingIntRange(1, 5))};
    }

    @Override
    public void onCast(Entity caster) {
        caster.level().playSound(null, caster.blockPosition().above(), SoundEvents.CHORUS_FLOWER_GROW, SoundSource.PLAYERS, 2.0f,
                2.0f + (float) (caster.level().random.nextGaussian() * 0.1));
    }

    @Override
    public void renderParticleFX(Level world, double x, double y, double z, double vx, double vy, double vz) {
        DustParticleOptions dust = new DustParticleOptions(new Vector3f(0.25f + world.random.nextFloat() * 0.25f, 0.0f, 0.25f + world.random.nextFloat() * 0.25f),
                1.5f + world.random.nextFloat());
        world.addParticle(dust, x, y, z, vx + world.random.nextGaussian() * 0.01, vy + world.random.nextGaussian() * 0.01, vz + world.random.nextGaussian() * 0.01);
    }
}
