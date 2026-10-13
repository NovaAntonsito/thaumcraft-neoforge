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

/** Earth effect. 1.21: particles are vanilla stone chips until the FX stage. */
public class FocusEffectEarth extends FocusEffect {
    @Override
    public String getResearch() {
        return "FOCUSELEMENTAL";
    }

    @Override
    public String getKey() {
        return "thaumcraft.EARTH";
    }

    @Override
    public Aspect getAspect() {
        return Aspect.EARTH;
    }

    @Override
    public int getComplexity() {
        return getSettingValue("power") * 3;
    }

    @Override
    public float getDamageForDisplay(float finalPower) {
        return 2 * getSettingValue("power") * finalPower;
    }

    @Override
    public boolean execute(HitResult target, Trajectory trajectory, float finalPower, int num) {
        Level world = getPackage().world;
        TCNetwork.sendToAllAround(world, target.getLocation().x, target.getLocation().y, target.getLocation().z, 64.0,
                new PacketFXFocusPartImpact(target.getLocation().x, target.getLocation().y, target.getLocation().z, new String[] {getKey()}));
        if (target instanceof EntityHitResult ehr) {
            Entity hit = ehr.getEntity();
            float damage = getDamageForDisplay(finalPower);
            hit.hurt(world.damageSources().thrown(hit, getPackage().getCaster()), damage);
            return true;
        }
        if (target instanceof BlockHitResult bhr && target.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = bhr.getBlockPos();
            if (getPackage().getCaster() instanceof Player player && world.getBlockState(pos).getDestroySpeed(world, pos) <= getDamageForDisplay(finalPower) / 25.0f) {
                thaumcraft.common.lib.events.ServerEvents.addBreaker(world, pos, world.getBlockState(pos), player, false, false, 0, 1.0f, 0.0f, 1.0f, num, 0.1f, null);
            }
        }
        return false;
    }

    @Override
    public NodeSetting[] createSettings() {
        return new NodeSetting[] {new NodeSetting("power", "focus.common.power", new NodeSetting.NodeSettingIntRange(1, 5))};
    }

    @Override
    public void renderParticleFX(Level world, double posX, double posY, double posZ, double motionX, double motionY, double motionZ) {
        world.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()), posX, posY, posZ, motionX, motionY, motionZ);
    }

    @Override
    public void onCast(Entity caster) {
        caster.level().playSound(null, caster.blockPosition().above(), SoundEvents.DRAGON_FIREBALL_EXPLODE, SoundSource.PLAYERS, 0.25f,
                1.0f + (float) (caster.level().random.nextGaussian() * 0.05));
    }
}
