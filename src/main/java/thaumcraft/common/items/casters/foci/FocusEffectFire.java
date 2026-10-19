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

/** Fire effect. 1.21: particles are vanilla flames until the FX stage. */
public class FocusEffectFire extends FocusEffect {
    @Override
    public String getResearch() {
        return "BASEAUROMANCY";
    }

    @Override
    public String getKey() {
        return "thaumcraft.FIRE";
    }

    @Override
    public Aspect getAspect() {
        return Aspect.FIRE;
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
        if (!(target instanceof EntityHitResult ehr)) {
            if (target instanceof BlockHitResult bhr && target.getType() == HitResult.Type.BLOCK && getSettingValue("duration") > 0) {
                BlockPos pos = bhr.getBlockPos().relative(bhr.getDirection());
                if (world.isEmptyBlock(pos) && world.random.nextFloat() < finalPower) {
                    world.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0f, world.random.nextFloat() * 0.4f + 0.8f);
                    world.setBlock(pos, Blocks.FIRE.defaultBlockState(), 11);
                    return true;
                }
            }
            return false;
        }
        Entity hit = ehr.getEntity();
        if (hit.fireImmune()) {
            return false;
        }
        float fire = (float) (1 + getSettingValue("duration") * getSettingValue("duration"));
        float damage = getDamageForDisplay(finalPower);
        fire *= finalPower;
        hit.hurt(world.damageSources().source(DamageTypes.FIREBALL, hit, getPackage().getCaster()), damage);
        if (fire > 0.0f) {
            hit.igniteForSeconds(Math.round(fire));
        }
        return true;
    }

    @Override
    public NodeSetting[] createSettings() {
        return new NodeSetting[] {new NodeSetting("power", "focus.common.power", new NodeSetting.NodeSettingIntRange(1, 5)),
                new NodeSetting("duration", "focus.fire.burn", new NodeSetting.NodeSettingIntRange(0, 5))};
    }

    @Override
    public void renderParticleFX(Level world, double posX, double posY, double posZ, double motionX, double motionY, double motionZ) {
        thaumcraft.client.fx.FXDispatcher.GenPart pp = new thaumcraft.client.fx.FXDispatcher.GenPart();
        pp.grav = -0.2f;
        pp.age = 10;
        pp.alpha = new float[] { 0.7f };
        pp.partStart = 640;
        pp.partInc = 1;
        pp.partNum = 10;
        pp.slowDown = 0.75;
        pp.scale = new float[] { (float) (1.5 + world.random.nextGaussian() * 0.20000000298023224) };
        thaumcraft.client.fx.FXDispatcher.drawGenericParticles(posX, posY, posZ, motionX, motionY, motionZ, pp);
    }

    @Override
    public void onCast(Entity caster) {
        caster.level().playSound(null, caster.blockPosition().above(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.0f,
                1.0f + (float) (caster.level().random.nextGaussian() * 0.05));
    }
}
