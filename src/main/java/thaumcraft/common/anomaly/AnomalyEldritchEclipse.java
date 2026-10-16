package thaumcraft.common.anomaly;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;
import thaumcraft.api.capabilities.PlayerWarp;
import thaumcraft.common.lib.InternalMethodHandler;
import thaumcraft.common.registry.TCMobEffects;

/**
 * Eldritch eclipse (negative, from tier 2). The night lasts twice as long, and every minute each player under the
 * open night sky gains 1 temporary warp, unless warded.
 * TODO: double the warp events once they are ported.
 */
public class AnomalyEldritchEclipse extends AnomalyType {
    public AnomalyEldritchEclipse() {
        super("eldritch_eclipse", Polarity.NEGATIVE);
        weight(6).durationMinutes(10, 20).minTier(2);
    }

    private static boolean isNight(ServerLevel level) {
        long t = level.getDayTime() % 24000L;
        return t >= 13000L && t < 23000L;
    }

    @Override
    public void onTick(ServerLevel level, ActiveAnomaly anomaly) {
        if (!isNight(level)) return;
        long elapsed = level.getGameTime() - anomaly.start();
        // Every other second the clock goes back a second: the night runs at half speed.
        if (level.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT) && (elapsed / 20L) % 2L == 1L) {
            level.setDayTime(level.getDayTime() - 20L);
        }
        boolean minute = elapsed % 1200L < 20L && elapsed >= 20L;
        for (ServerPlayer p : level.players()) {
            level.sendParticles(ParticleTypes.ASH, p.getX(), p.getY() + 4.0, p.getZ(), 10, 8.0, 3.0, 8.0, 0.0);
            if (minute && level.canSeeSky(p.blockPosition().above()) && !p.hasEffect(TCMobEffects.WARP_WARD) && !p.isCreative()) {
                InternalMethodHandler.addWarpToPlayer(p, 1, PlayerWarp.EnumWarpType.TEMPORARY);
                p.displayClientMessage(Component.translatable("tc.anomaly.eldritch_eclipse.warp").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC), true);
            }
        }
    }
}
