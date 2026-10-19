package thaumcraft.common.world.voidrealm;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import thaumcraft.Config;
import thaumcraft.Thaumcraft;
import thaumcraft.api.capabilities.PlayerWarp;
import thaumcraft.api.capabilities.ThaumcraftCapabilities;
import thaumcraft.api.casters.ICaster;
import thaumcraft.common.anomaly.AnomalyVoidCalling;
import thaumcraft.common.entities.EntityVoidRift;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.lib.network.playerdata.PacketSyncKnowledge;
import thaumcraft.common.lib.research.ResearchManager;
import thaumcraft.common.registry.TCAttachments;

/**
 * Dream branch: what sets off the Whispers of the Void. A player who knows the focal manipulator (BASEAUROMANCY stage
 * 2) and has made a caster or a wand hears the void the first time they wake up: the chapter opens and the first rift
 * appears. The later encounters come as the "call of the void" anomaly. Void warp wears off by itself here.
 */
@EventBusSubscriber(modid = Thaumcraft.MODID)
public final class VoidEvents {
    private static final String MANIPULATOR = "BASEAUROMANCY@2";

    private VoidEvents() {
    }

    /** The first magic weapon: any caster or wand made at a table or by infusion. */
    @SubscribeEvent
    static void onCrafted(PlayerEvent.ItemCraftedEvent event) {
        if (event.getEntity() instanceof ServerPlayer p && event.getCrafting().getItem() instanceof ICaster) {
            markWeapon(p);
        }
    }

    private static void markWeapon(ServerPlayer p) {
        if (!ThaumcraftCapabilities.knowsResearchStrict(p, VoidEncounters.WEAPON_FLAG)) {
            ResearchManager.completeResearch(p, VoidEncounters.WEAPON_FLAG);
        }
    }

    @SubscribeEvent
    static void onWake(PlayerWakeUpEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer p) || event.wakeImmediately() || !Config.VOID_WHISPERS.get()) return;
        if (ThaumcraftCapabilities.knowsResearchStrict(p, VoidEncounters.UNLOCK) || !ThaumcraftCapabilities.knowsResearch(p, MANIPULATOR)) return;
        // Casters made before this branch: one in the inventory counts too.
        if (!ThaumcraftCapabilities.knowsResearchStrict(p, VoidEncounters.WEAPON_FLAG)) {
            for (ItemStack s : p.getInventory().items) {
                if (s.getItem() instanceof ICaster) {
                    markWeapon(p);
                    break;
                }
            }
        }
        if (!ThaumcraftCapabilities.knowsResearchStrict(p, VoidEncounters.WEAPON_FLAG)) return;
        ResearchManager.completeResearch(p, VoidEncounters.UNLOCK);
        PacketSyncKnowledge.sync(p);
        p.level().playSound(null, p.blockPosition(), SoundsTC.WHISPERS.get(), SoundSource.PLAYERS, 1.0f, 0.6f);
        p.sendSystemMessage(Component.translatable("tc.void.wake").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
        // The first encounter is safe: it starts now, through the anomaly when it can so it gets its marker.
        if (!AnomalyVoidCalling.startFor(p.serverLevel(), p)) {
            EntityVoidRift rift = VoidEncounters.openRift(p.serverLevel(), p);
            if (rift != null) {
                p.sendSystemMessage(Component.translatable("tc.void.call.at", rift.getBlockX(), rift.getBlockZ())
                        .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
            }
        }
    }

    /** Void warp: one point wears off every voidWarpDecayMinutes. Nothing else takes it away. */
    @SubscribeEvent
    static void onTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer p)) return;
        int period = Config.VOID_WARP_DECAY.get() * 1200;
        if (p.tickCount % period != 0) return;
        PlayerWarp warp = p.getData(TCAttachments.WARP);
        if (warp.get(PlayerWarp.EnumWarpType.VOID) > 0) {
            warp.reduce(PlayerWarp.EnumWarpType.VOID, 1);
            p.syncData(TCAttachments.WARP);
        }
    }
}
