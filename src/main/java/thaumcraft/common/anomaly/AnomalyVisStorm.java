package thaumcraft.common.anomaly;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.levelgen.Heightmap;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aura.AuraHelper;
import thaumcraft.common.config.ConfigItems;

/**
 * Vis storm (mixed). Once a second each player has a 1 in 6 chance of a vis bolt striking up to 20 blocks away.
 * The bolt is a harmless lightning flash. Three in four add 15 vis to that chunk, and half of those drop a primal
 * vis crystal; the other one in four releases 8 flux. Vis regenerates three times as fast.
 */
public class AnomalyVisStorm extends AnomalyType {
    public AnomalyVisStorm() {
        super("vis_storm", Polarity.MIXED);
        weight(8).durationMinutes(5, 10).modifier(AnomalyStat.VIS_REGEN, 3.0f);
    }

    @Override
    public void onTick(ServerLevel level, ActiveAnomaly anomaly) {
        for (ServerPlayer p : level.players()) {
            if (level.random.nextInt(6) != 0) continue;
            int dx = level.random.nextInt(41) - 20;
            int dz = level.random.nextInt(41) - 20;
            BlockPos pos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, p.blockPosition().offset(dx, 0, dz));
            strike(level, pos);
        }
    }

    public static void strike(ServerLevel level, BlockPos pos) {
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
        if (bolt != null) {
            bolt.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
            bolt.setVisualOnly(true);
            level.addFreshEntity(bolt);
        }
        if (level.random.nextInt(4) == 0) {
            AuraHelper.polluteAura(level, pos, 8.0f, true);
            level.sendParticles(ParticleTypes.SQUID_INK, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 20, 0.6, 0.6, 0.6, 0.02);
        } else {
            AuraHelper.addVis(level, pos, 15.0f);
            level.sendParticles(ParticleTypes.WITCH, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 30, 0.6, 1.0, 0.6, 0.05);
            if (level.random.nextBoolean()) {
                List<Aspect> primals = Aspect.getPrimalAspects();
                Aspect a = primals.get(level.random.nextInt(primals.size()));
                ItemEntity item = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                        ConfigItems.crystalEssence.get().makeCrystal(a, 1));
                item.setDefaultPickUpDelay();
                level.addFreshEntity(item);
            }
        }
        level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.WEATHER, 2.0f, 0.6f + level.random.nextFloat() * 0.4f);
    }
}
