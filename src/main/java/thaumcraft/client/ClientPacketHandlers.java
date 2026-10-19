package thaumcraft.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.capabilities.PlayerKnowledge;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.api.research.ResearchEntry;
import thaumcraft.client.gui.GuiResearchBrowser;
import thaumcraft.client.gui.ResearchToast;
import thaumcraft.client.gui.ThaumometerHud;
import thaumcraft.client.lib.PlayerNotifications;
import thaumcraft.common.registry.TCAttachments;

/** Client-side handling called from common code (packets, items). Only ever invoked on the client. */
public final class ClientPacketHandlers {
    private ClientPacketHandlers() {
    }

    /** PacketSyncKnowledge.onMessage: replaces the knowledge and shows a toast for each new research. */
    public static void syncKnowledge(Player player, PlayerKnowledge knowledge) {
        player.setData(TCAttachments.KNOWLEDGE, knowledge);
        for (String key : knowledge.getResearchList()) {
            if (knowledge.hasResearchFlag(key, PlayerKnowledge.EnumResearchFlag.POPUP)) {
                ResearchEntry ri = ResearchCategories.getResearch(key);
                if (ri != null) Minecraft.getInstance().getToasts().addToast(new ResearchToast(ri));
            }
            knowledge.clearResearchFlag(key, PlayerKnowledge.EnumResearchFlag.POPUP);
        }
    }

    /** player.openGui(..., 12, ...): the thaumonomicon. */
    public static void openResearchBrowser() {
        Minecraft.getInstance().setScreen(new GuiResearchBrowser());
    }

    private static long lastSound;

    /** PacketAspectPool.onMessage: sets the new total; a gain shows a notification and flying icons. */
    public static void aspectPool(Player player, String key, int amount, int total) {
        Aspect aspect = Aspect.getAspect(key);
        if (aspect == null) return;
        boolean success = player.getData(TCAttachments.ASPECTS).setAspectCount(aspect, total);
        if (success && amount > 0) {
            PlayerNotifications.addNotification(Component.translatable("tc.addaspectpool", amount + "", aspect.getDisplayName()), aspect);
            for (int a = 0; a < amount; ++a) PlayerNotifications.addAspectNotification(aspect);
            if (System.currentTimeMillis() > lastSound) {
                player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.1f, 0.9f + player.getRandom().nextFloat() * 0.2f);
                lastSound = System.currentTimeMillis() + 100L;
            }
        }
    }

    /** PacketAspectDiscovery.onMessage: a new aspect is learned. */
    public static void aspectDiscovery(Player player, String key) {
        Aspect aspect = Aspect.getAspect(key);
        if (aspect == null) return;
        player.getData(TCAttachments.ASPECTS).researchAspect(aspect);
        PlayerNotifications.addNotification(Component.translatable("tc.addaspectdiscovery", aspect.getDisplayName()).withStyle(ChatFormatting.GOLD), aspect);
        player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.2f, 0.5f + player.getRandom().nextFloat() * 0.2f);
    }

    /** PacketAspectDiscoveryError.onMessage: an aspect the player has not discovered blocks a scan. */
    public static void aspectDiscoveryError(String key) {
        PlayerNotifications.addNotification(Component.translatable("tc.discoveryerror",
                Component.translatable("tc.aspect.help." + key)).withStyle(ChatFormatting.RED));
    }

    /** PacketAuraToClient.onMessage: HudHandler.currentAura for the thaumometer's bar. */
    /** The aura atlas (dream branch): the first packet opens its screen, later ones refresh it. */
    public static void auraAtlas(thaumcraft.common.lib.network.misc.PacketAuraAtlasToClient msg) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof thaumcraft.client.gui.GuiAuraAtlas atlas) {
            atlas.update(msg);
        } else if (msg.open()) {
            mc.setScreen(new thaumcraft.client.gui.GuiAuraAtlas(msg));
        }
    }

    public static void auraToClient(short base, float vis, float flux) {
        ThaumometerHud.auraBase = base;
        ThaumometerHud.auraVis = vis;
        ThaumometerHud.auraFlux = flux;
    }

    /** ItemResearchNote.displayToast: "Research note complete". */
    public static void showNoteToast(ResearchEntry entry) {
        Minecraft.getInstance().getToasts().addToast(new ResearchToast(entry, "researchnote.complete"));
    }
}
