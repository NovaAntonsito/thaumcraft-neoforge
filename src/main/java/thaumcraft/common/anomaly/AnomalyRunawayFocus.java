package thaumcraft.common.anomaly;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import thaumcraft.api.aura.AuraHelper;

/**
 * Runaway focus (mixed). Foci cost half the vis, but every cast also lets loose one random side effect
 * ({@link #afterCast}, called by {@code ItemCaster}): a harmless lightning flash where you look, a short
 * levitation, nearby creatures set on fire, a burst of speed, nearby creatures pushed away, or 3 flux.
 */
public class AnomalyRunawayFocus extends AnomalyType {
    public AnomalyRunawayFocus() {
        super("runaway_focus", Polarity.MIXED);
        weight(8).durationMinutes(5, 15).modifier(AnomalyStat.FOCUS_VIS_COST, 0.5f);
    }

    private static boolean isActive(ServerLevel level) {
        if (!AnomalyManager.isEnabledIn(level)) return false;
        for (ActiveAnomaly a : AnomalyManager.get(level).getActive()) {
            if (a.type() instanceof AnomalyRunawayFocus) return true;
        }
        return false;
    }

    /** Server side, right after a focus was cast. */
    public static void afterCast(Player player) {
        if (!(player.level() instanceof ServerLevel level) || !isActive(level)) return;
        Vec3 eye = player.getEyePosition();
        switch (level.random.nextInt(6)) {
            case 0 -> {
                Vec3 hit = player.pick(24.0, 1.0f, false).getLocation();
                AnomalyVisStorm.strike(level, BlockPos.containing(hit));
            }
            case 1 -> player.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 30, 0));
            case 2 -> {
                for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(4.0), e -> e != player)) {
                    e.igniteForSeconds(3.0f);
                }
                level.sendParticles(ParticleTypes.FLAME, player.getX(), player.getY() + 1.0, player.getZ(), 40, 2.0, 0.5, 2.0, 0.02);
            }
            case 3 -> player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 1));
            case 4 -> {
                for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, new AABB(player.blockPosition()).inflate(6.0), e -> e != player)) {
                    Vec3 push = e.position().subtract(player.position()).normalize().scale(1.2);
                    e.push(push.x, 0.4, push.z);
                    e.hurtMarked = true;
                }
                level.sendParticles(ParticleTypes.EXPLOSION, player.getX(), player.getY() + 1.0, player.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
            }
            default -> {
                AuraHelper.polluteAura(level, player.blockPosition(), 3.0f, true);
                level.sendParticles(ParticleTypes.SQUID_INK, eye.x, eye.y, eye.z, 15, 0.3, 0.3, 0.3, 0.02);
            }
        }
    }
}
