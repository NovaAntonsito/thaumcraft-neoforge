package thaumcraft.common.anomaly;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aura.AuraHelper;
import thaumcraft.common.config.ConfigItems;

/**
 * The two anomalies where vis crystals fall from the sky near players.
 * <ul>
 * <li><b>Crystal rain</b> (positive): once a second each player has a 1 in 4 chance of a primal crystal falling up
 * to 10 blocks away.</li>
 * <li><b>Vitium rain</b> (negative): once a second each player has a 1 in 3 chance of a vitium crystal falling. A
 * vitium crystal left on the ground for {@link #BURST_TICKS} ticks bursts and releases {@link #BURST_FLUX} flux.</li>
 * </ul>
 */
public class AnomalyCrystalRain extends AnomalyType {
    public static final String VITIUM_TAG = "thaumcraft_vitium_rain";
    public static final int BURST_TICKS = 600;
    public static final float BURST_FLUX = 5.0f;
    private final boolean vitium;

    private AnomalyCrystalRain(String id, Polarity polarity, boolean vitium) {
        super(id, polarity);
        this.vitium = vitium;
    }

    public static AnomalyCrystalRain crystalRain() {
        AnomalyCrystalRain t = new AnomalyCrystalRain("crystal_rain", Polarity.POSITIVE, false);
        t.weight(8).durationMinutes(5, 10);
        return t;
    }

    public static AnomalyCrystalRain vitiumRain() {
        AnomalyCrystalRain t = new AnomalyCrystalRain("vitium_rain", Polarity.NEGATIVE, true);
        t.weight(8).durationMinutes(5, 10);
        return t;
    }

    @Override
    public void onTick(ServerLevel level, ActiveAnomaly anomaly) {
        List<Aspect> primals = Aspect.getPrimalAspects();
        for (ServerPlayer p : level.players()) {
            if (vitium) burst(level, p);
            if (level.random.nextInt(vitium ? 3 : 4) != 0) continue;
            int dx = level.random.nextInt(21) - 10;
            int dz = level.random.nextInt(21) - 10;
            BlockPos ground = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, p.blockPosition().offset(dx, 0, dz));
            double y = Math.max(ground.getY() + 12.0, p.getY() + 12.0);
            Aspect aspect = vitium ? Aspect.FLUX : primals.get(level.random.nextInt(primals.size()));
            ItemEntity item = new ItemEntity(level, ground.getX() + 0.5, y, ground.getZ() + 0.5,
                    ConfigItems.crystalEssence.get().makeCrystal(aspect, 1));
            item.setDeltaMovement(0.0, -0.6, 0.0);
            item.setDefaultPickUpDelay();
            if (vitium) item.addTag(VITIUM_TAG);
            level.addFreshEntity(item);
            level.sendParticles(vitium ? ParticleTypes.SQUID_INK : ParticleTypes.END_ROD, ground.getX() + 0.5, y, ground.getZ() + 0.5,
                    8, 0.2, 0.2, 0.2, 0.02);
        }
    }

    /** Vitium crystals that lay on the ground too long burst into flux. */
    private static void burst(ServerLevel level, ServerPlayer p) {
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, new AABB(p.blockPosition()).inflate(48.0),
                e -> e.getTags().contains(VITIUM_TAG) && e.getAge() >= BURST_TICKS)) {
            BlockPos pos = item.blockPosition();
            AuraHelper.polluteAura(level, pos, BURST_FLUX * item.getItem().getCount(), true);
            level.sendParticles(ParticleTypes.SQUID_INK, item.getX(), item.getY() + 0.3, item.getZ(), 20, 0.3, 0.3, 0.3, 0.05);
            level.playSound(null, pos, SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.AMBIENT, 1.0f, 0.5f);
            item.discard();
        }
    }
}
