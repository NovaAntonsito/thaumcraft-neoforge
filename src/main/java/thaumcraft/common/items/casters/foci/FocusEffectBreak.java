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

/** Break effect. 1.21: particles are vanilla crits until the FX stage. */
public class FocusEffectBreak extends FocusEffect {
    @Override
    public String getResearch() {
        return "FOCUSBREAK";
    }

    @Override
    public String getKey() {
        return "thaumcraft.BREAK";
    }

    @Override
    public Aspect getAspect() {
        return Aspect.ENTROPY;
    }

    @Override
    public int getComplexity() {
        return getSettingValue("power") * 3 + getSettingValue("silk") * 4 + ((getSettingValue("fortune") == 0) ? 0 : ((getSettingValue("fortune") + 1) * 3));
    }

    @Override
    public boolean execute(HitResult target, Trajectory trajectory, float finalPower, int num) {
        if (target instanceof BlockHitResult bhr && target.getType() == HitResult.Type.BLOCK) {
            Level world = getPackage().world;
            BlockPos pos = bhr.getBlockPos();
            TCNetwork.sendToAllAround(world, target.getLocation().x, target.getLocation().y, target.getLocation().z, 64.0,
                    new PacketFXFocusPartImpact(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new String[] {getKey()}));
            boolean silk = getSettingValue("silk") > 0;
            int fortune = getSettingValue("fortune");
            float strength = getSettingValue("power") * finalPower;
            float dur = world.getBlockState(pos).getDestroySpeed(world, pos) * 100.0f;
            dur = (float) Math.sqrt(dur);
            if (getPackage().getCaster() instanceof Player player) {
                thaumcraft.common.lib.events.ServerEvents.addBreaker(world, pos, world.getBlockState(pos), player, true, silk, fortune, strength, dur, dur,
                        (int) (dur / strength / 3.0f * num), 0.25f + (silk ? 0.25f : 0.0f) + fortune * 0.1f, null);
            }
            return true;
        }
        return true;
    }

    @Override
    public NodeSetting[] createSettings() {
        int[] silk = {0, 1};
        String[] silkDesc = {"focus.common.no", "focus.common.yes"};
        int[] fortune = {0, 1, 2, 3, 4};
        String[] fortuneDesc = {"focus.common.no", "I", "II", "III", "IV"};
        return new NodeSetting[] {new NodeSetting("power", "focus.break.power", new NodeSetting.NodeSettingIntRange(1, 5)),
                new NodeSetting("fortune", "focus.common.fortune", new NodeSetting.NodeSettingIntList(fortune, fortuneDesc)),
                new NodeSetting("silk", "focus.common.silk", new NodeSetting.NodeSettingIntList(silk, silkDesc))};
    }

    @Override
    public void renderParticleFX(Level world, double posX, double posY, double posZ, double motionX, double motionY, double motionZ) {
        thaumcraft.client.fx.FocusFX.breakFocus(posX, posY, posZ, motionX, motionY, motionZ);
    }

    @Override
    public void onCast(Entity caster) {
        caster.level().playSound(null, caster.blockPosition().above(), SoundEvents.END_GATEWAY_SPAWN, SoundSource.PLAYERS, 0.1f,
                2.0f + (float) (caster.level().random.nextGaussian() * 0.05));
    }
}
