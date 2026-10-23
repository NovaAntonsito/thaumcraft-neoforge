package voidexpeditions.run;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import thaumcraft.api.capabilities.ThaumcraftCapabilities;
import thaumcraft.common.entities.monster.boss.EntityTaintacleGiant;
import thaumcraft.common.entities.monster.boss.EntityThaumcraftBoss;
import thaumcraft.common.lib.network.playerdata.PacketSyncKnowledge;
import thaumcraft.common.lib.research.ResearchManager;
import voidexpeditions.VoidExpeditions;
import voidexpeditions.perk.Perks;

/**
 * Deaths lose the run, keeping the inventory, unless the extra life saves it; bosses open the last portal; the perks
 * are set again on login. Finishing the Whispers of the Void (VOIDEXPLORER) reveals the secret research of the gate.
 */
@EventBusSubscriber(modid = VoidExpeditions.MODID)
public final class ExpeditionEvents {
    public static final String GATE_RESEARCH = "VOIDGATE";
    private static final String WHISPERS_END = "VOIDEXPLORER";

    private ExpeditionEvents() {
    }

    /**
     * Before anything drops, so the inventory can be kept: a player on a run spends the extra life or loses the run.
     * A boss falling in the expeditions opens the last labyrinths' portals.
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    static void onDeath(LivingDeathEvent event) {
        if (event.isCanceled()) return;
        LivingEntity e = event.getEntity();
        if (e instanceof ServerPlayer p) {
            if (Expeditions.tryExtraLife(p)) {
                event.setCanceled(true);
            } else {
                Expeditions.onDeath(p);
            }
        } else if ((e instanceof EntityThaumcraftBoss || e instanceof EntityTaintacleGiant)
                && e.level() instanceof ServerLevel level && ExpeditionDim.is(level)) {
            Expeditions.onBossDeath(level, e.position());
        }
    }

    @SubscribeEvent
    static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer p && !event.isEndConquered()) Expeditions.restoreAfterDeath(p);
    }

    @SubscribeEvent
    static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer p) Perks.refresh(p);
    }

    @SubscribeEvent
    static void onTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer p) || p.tickCount % 100 != 0) return;
        if (ThaumcraftCapabilities.knowsResearchStrict(p, WHISPERS_END) && !ThaumcraftCapabilities.knowsResearchStrict(p, GATE_RESEARCH)) {
            ResearchManager.completeResearch(p, GATE_RESEARCH);
            PacketSyncKnowledge.sync(p);
            p.sendSystemMessage(Component.translatable("voidexpeditions.research.revealed")
                    .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
        }
    }
}
