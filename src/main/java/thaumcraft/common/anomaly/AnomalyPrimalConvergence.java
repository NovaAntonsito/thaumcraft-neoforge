package thaumcraft.common.anomaly;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.levelgen.Heightmap;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.common.config.ConfigItems;

/**
 * Primal convergence (positive). One primal is drawn when it starts. Crucible recipes need 30 % less of that
 * primal ({@link AnomalyModifiers#crucibleCost}), and its vis crystals appear near players: once a second each
 * player has a 1 in 40 chance of one landing up to 8 blocks away.
 */
public class AnomalyPrimalConvergence extends AnomalyType {
    public static final float COST = 0.7f;

    public AnomalyPrimalConvergence() {
        super("primal_convergence", Polarity.POSITIVE);
        weight(10).durationMinutes(5, 15);
    }

    @Nullable
    public static Aspect getPrimal(ActiveAnomaly anomaly) {
        return Aspect.getAspect(anomaly.data().getString("primal"));
    }

    @Override
    public void onStart(ServerLevel level, ActiveAnomaly anomaly) {
        List<Aspect> primals = Aspect.getPrimalAspects();
        anomaly.data().putString("primal", primals.get(level.random.nextInt(primals.size())).getTag());
    }

    @Override
    public void onTick(ServerLevel level, ActiveAnomaly anomaly) {
        Aspect primal = getPrimal(anomaly);
        if (primal == null) return;
        // Said on the first second, after the "anomaly started" message.
        if (!anomaly.data().getBoolean("told")) {
            anomaly.data().putBoolean("told", true);
            for (ServerPlayer p : level.players()) {
                p.sendSystemMessage(Component.translatable("tc.anomaly.primal_convergence.chosen", primal.getDisplayName())
                        .withStyle(ChatFormatting.GRAY));
            }
        }
        for (ServerPlayer p : level.players()) {
            if (level.random.nextInt(40) != 0) continue;
            int dx = level.random.nextInt(17) - 8;
            int dz = level.random.nextInt(17) - 8;
            BlockPos pos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, p.blockPosition().offset(dx, 0, dz));
            ItemEntity item = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 2.0, pos.getZ() + 0.5,
                    ConfigItems.crystalEssence.get().makeCrystal(primal, 1));
            item.setDefaultPickUpDelay();
            level.addFreshEntity(item);
            level.sendParticles(ParticleTypes.ENCHANT, pos.getX() + 0.5, pos.getY() + 2.0, pos.getZ() + 0.5, 20, 0.4, 0.4, 0.4, 0.5);
            level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.AMBIENT, 1.0f, 1.2f);
        }
    }
}
