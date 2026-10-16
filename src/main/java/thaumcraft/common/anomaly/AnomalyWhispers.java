package thaumcraft.common.anomaly;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.neoforged.neoforge.network.PacketDistributor;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.capabilities.PlayerAspects;
import thaumcraft.api.capabilities.ThaumcraftCapabilities;
import thaumcraft.common.lib.network.playerdata.PacketAspectPool;
import thaumcraft.common.registry.TCMobEffects;

/**
 * Whispers (negative). Each player hears sounds and sees flickers only they can perceive (1 in 10 a second), and
 * now and then forgets 1 point of a random aspect from their pool (1 in 180 a second, about one every 3 minutes).
 * Warp ward makes a player immune.
 */
public class AnomalyWhispers extends AnomalyType {
    public AnomalyWhispers() {
        super("whispers", Polarity.NEGATIVE);
        weight(8).durationMinutes(5, 15);
    }

    @Override
    public void onTick(ServerLevel level, ActiveAnomaly anomaly) {
        for (ServerPlayer p : level.players()) {
            if (p.hasEffect(TCMobEffects.WARP_WARD)) continue;
            if (level.random.nextInt(10) == 0) {
                SoundEvent s = switch (level.random.nextInt(4)) {
                    case 0 -> SoundEvents.AMBIENT_CAVE.value();
                    case 1 -> SoundEvents.ENDERMAN_AMBIENT;
                    case 2 -> SoundEvents.SOUL_ESCAPE.value();
                    default -> SoundEvents.ZOMBIE_AMBIENT;
                };
                p.playNotifySound(s, SoundSource.AMBIENT, 0.6f, 0.5f + level.random.nextFloat() * 0.4f);
                double ox = (level.random.nextDouble() - 0.5) * 8.0;
                double oz = (level.random.nextDouble() - 0.5) * 8.0;
                level.sendParticles(p, ParticleTypes.SMOKE, false, p.getX() + ox, p.getY() + 1.0, p.getZ() + oz, 15, 0.2, 0.6, 0.2, 0.01);
            }
            if (level.random.nextInt(180) == 0) forget(p);
        }
    }

    private static void forget(ServerPlayer p) {
        PlayerAspects pool = ThaumcraftCapabilities.getAspects(p);
        List<Aspect> have = new ArrayList<>();
        for (Aspect a : pool.aspectsPool().getAspects()) {
            if (pool.aspectCount(a) > 0) have.add(a);
        }
        if (have.isEmpty()) return;
        Aspect a = have.get(p.getRandom().nextInt(have.size()));
        if (!pool.addToAspectPool(a, -1)) return;
        PacketDistributor.sendToPlayer(p, new PacketAspectPool(a.getTag(), 0, pool.aspectCount(a)));
        p.displayClientMessage(Component.translatable("tc.anomaly.whispers.forget", a.getDisplayName())
                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC), true);
    }
}
