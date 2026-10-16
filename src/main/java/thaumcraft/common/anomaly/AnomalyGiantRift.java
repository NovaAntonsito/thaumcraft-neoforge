package thaumcraft.common.anomaly;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.entities.EntityFluxRift;

/**
 * Giant void rift (negative, local). A flux rift of size 120 (about 16 blocks long; natural ones are 5 to 20, so
 * one or two blocks) opens 24 to 48 blocks from a random player and grows by 4 every 10 seconds up to 300 (about
 * 40 blocks). While the anomaly lasts the rift stays stable, so it never collapses on its own.
 * <p>
 * Players close it by using salis mundus within 8 blocks: each use shrinks it by a quarter (at least 20). Closed in time, it collapses
 * as usual (void seeds and a chance of a primordial pearl) and also drops a sure primordial pearl. If time runs out
 * it stays in the world as a normal rift and keeps eating blocks until it collapses by itself.
 */
public class AnomalyGiantRift extends AnomalyType {
    private static final int START_SIZE = 120;
    private static final int MAX_SIZE = 300;
    private static final int GROWTH = 4;
    private static final long GROWTH_INTERVAL = 200L;
    private static final int CLOSE_SIZE = 20;
    private static final int MIN_SHRINK = 20;

    public AnomalyGiantRift() {
        super("giant_rift", Polarity.NEGATIVE);
        weight(4).durationMinutes(8, 15).minTier(2);
    }

    @Override
    public void onStart(ServerLevel level, ActiveAnomaly anomaly) {
        List<ServerPlayer> players = level.players();
        if (players.isEmpty()) return;
        ServerPlayer target = players.get(level.random.nextInt(players.size()));
        double angle = level.random.nextDouble() * Math.PI * 2.0;
        double dist = 24.0 + level.random.nextDouble() * 24.0;
        BlockPos column = BlockPos.containing(target.getX() + Math.cos(angle) * dist, target.getY(), target.getZ() + Math.sin(angle) * dist);
        BlockPos pos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, column).above(4);
        EntityFluxRift rift = new EntityFluxRift(level);
        rift.setRiftSeed(level.random.nextInt());
        rift.moveTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, level.random.nextInt(360), 0.0f);
        if (level.addFreshEntity(rift)) {
            rift.setRiftSize(START_SIZE);
            rift.setRiftStability(10.0f);
            anomaly.data().putUUID("rift", rift.getUUID());
            anomaly.data().putLong("nextGrow", level.getGameTime() + GROWTH_INTERVAL);
            anomaly.setLocation(pos);
        }
    }

    @Nullable
    private static EntityFluxRift getRift(ServerLevel level, ActiveAnomaly anomaly) {
        if (!anomaly.data().hasUUID("rift")) return null;
        Entity e = level.getEntity(anomaly.data().getUUID("rift"));
        return e instanceof EntityFluxRift r && r.isAlive() ? r : null;
    }

    @Override
    public void onTick(ServerLevel level, ActiveAnomaly anomaly) {
        EntityFluxRift rift = getRift(level, anomaly);
        if (rift == null || rift.getCollapse()) return;
        // Anchored: stable, but not "very stable", which would stop it from feeding on flux.
        if (rift.getRiftStability() < 10.0f) rift.setRiftStability(10.0f);
        long now = level.getGameTime();
        if (now >= anomaly.data().getLong("nextGrow")) {
            anomaly.data().putLong("nextGrow", now + GROWTH_INTERVAL);
            if (rift.getRiftSize() < MAX_SIZE) {
                rift.setRiftSize(Math.min(MAX_SIZE, rift.getRiftSize() + GROWTH));
                level.playSound(null, rift.blockPosition(), SoundEvents.PORTAL_AMBIENT, SoundSource.HOSTILE, 1.0f, 0.5f);
            }
        }
    }

    /**
     * Over once its rift is gone (closed). While nobody is near, the rift's entities are not loaded, so this only
     * checks when entities are ticking at the rift's place.
     */
    @Override
    public boolean isFinished(ServerLevel level, ActiveAnomaly anomaly) {
        BlockPos loc = anomaly.getLocation();
        if (loc == null) return true;
        if (!level.isPositionEntityTicking(loc)) return false;
        return getRift(level, anomaly) == null;
    }

    @Override
    public void onEnd(ServerLevel level, ActiveAnomaly anomaly) {
        EntityFluxRift rift = getRift(level, anomaly);
        if (rift != null && !rift.getCollapse()) {
            // Time ran out: the rift stays, now a normal one that slowly loses stability.
            rift.setRiftStability(-10.0f);
            for (ServerPlayer p : level.players()) {
                p.sendSystemMessage(Component.translatable("tc.anomaly.giant_rift.escaped").withStyle(ChatFormatting.DARK_PURPLE));
            }
        }
    }

    /**
     * Salis mundus used near the giant rift: shrinks it. Returns true when the use was taken (the caller then uses up
     * the dust). Called from {@link AnomalyEvents#onRightClickItem}.
     */
    public static boolean tryShrink(ServerLevel level, Player player) {
        if (!AnomalyManager.isEnabledIn(level)) return false;
        for (ActiveAnomaly a : AnomalyManager.get(level).getActive()) {
            if (!(a.type() instanceof AnomalyGiantRift)) continue;
            EntityFluxRift rift = getRift(level, a);
            if (rift == null || rift.getCollapse()) continue;
            Vec3 eye = player.getEyePosition();
            boolean near = rift.getBoundingBox().inflate(8.0).contains(eye);
            if (!near) continue;
            int size = rift.getRiftSize() - Math.max(MIN_SHRINK, rift.getRiftSize() / 4);
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, rift.getX(), rift.getY(), rift.getZ(), 60, 1.5, 1.5, 1.5, 0.2);
            level.playSound(null, rift.blockPosition(), SoundEvents.RESPAWN_ANCHOR_DEPLETE.value(), SoundSource.HOSTILE, 1.5f, 0.6f);
            if (size <= CLOSE_SIZE) {
                rift.setRiftStability(60.0f);
                rift.setCollapse(true);
                rift.spawnAtLocation(new ItemStack(ConfigItems.primordialPearl.get()), 0.0f);
                for (ServerPlayer p : level.players()) {
                    p.sendSystemMessage(Component.translatable("tc.anomaly.giant_rift.closed", player.getDisplayName())
                            .withStyle(ChatFormatting.LIGHT_PURPLE));
                }
            } else {
                rift.setRiftSize(size);
                player.displayClientMessage(Component.translatable("tc.anomaly.giant_rift.shrink", size - CLOSE_SIZE)
                        .withStyle(ChatFormatting.DARK_PURPLE), true);
            }
            return true;
        }
        return false;
    }
}
