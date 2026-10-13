package thaumcraft.common.lib.events;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WrittenBookContent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import thaumcraft.Config;
import thaumcraft.Thaumcraft;
import thaumcraft.api.capabilities.PlayerKnowledge;
import thaumcraft.api.capabilities.ThaumcraftCapabilities;
import thaumcraft.common.registry.TCAttachments;
import thaumcraft.common.items.resources.ItemCrystalEssence;
import thaumcraft.common.items.curios.ItemThaumonomicon;
import thaumcraft.common.lib.network.playerdata.PacketSyncKnowledge;
import thaumcraft.common.lib.research.ResearchManager;
import thaumcraft.common.lib.research.ResearchNoteManager;

/** Partial port of PlayerEvents: the research hooks (knowledge sync, the crystal, the dream and the book). */
@EventBusSubscriber(modid = Thaumcraft.MODID)
public final class PlayerEvents {
    private PlayerEvents() {
    }

    /** livingTick: knowledge changed by ResearchManager is sent once a second. */
    @SubscribeEvent
    public static void livingTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player && player.tickCount % 20 == 0
                && ResearchManager.syncList.remove(player.getUUID()) != null) {
            PacketSyncKnowledge.sync(player);
        }
    }

    @SubscribeEvent
    public static void pickupItem(ItemEntityPickupEvent.Post event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        PlayerKnowledge knowledge = ThaumcraftCapabilities.getKnowledge(player);
        ItemStack stack = event.getOriginalStack();
        if (stack.getItem() instanceof ItemCrystalEssence && !knowledge.isResearchKnown("!gotcrystals")) {
            knowledge.addResearch("!gotcrystals");
            PacketSyncKnowledge.sync(player);
            player.sendSystemMessage(Component.translatable("got.crystals").withStyle(ChatFormatting.DARK_PURPLE));
            if (Config.NO_SLEEP.getAsBoolean() && !knowledge.isResearchKnown("!gotdream")) {
                giveDreamJournal(player);
            }
        }
        if (stack.getItem() instanceof ItemThaumonomicon && !knowledge.isResearchKnown("!gotthaumonomicon")) {
            knowledge.addResearch("!gotthaumonomicon");
            PacketSyncKnowledge.sync(player);
        }
    }

    @SubscribeEvent
    public static void wakeUp(PlayerWakeUpEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        PlayerKnowledge knowledge = ThaumcraftCapabilities.getKnowledge(player);
        if (knowledge.isResearchKnown("!gotcrystals") && !knowledge.isResearchKnown("!gotdream")) {
            giveDreamJournal(player);
        }
    }

    private static void giveDreamJournal(ServerPlayer player) {
        PlayerKnowledge knowledge = ThaumcraftCapabilities.getKnowledge(player);
        knowledge.addResearch("!gotdream");
        PacketSyncKnowledge.sync(player);
        ItemStack book = startBook(player);
        if (!player.getInventory().add(book)) player.drop(book, false);
        player.sendSystemMessage(Component.translatable("got.dream").withStyle(ChatFormatting.DARK_PURPLE));
    }

    /**
     * ConfigItems.startBook: a tattered written book with the dream. The original translated the text on the
     * server; here the pages and name stay translatable, so each client reads them in its own language.
     */
    private static ItemStack startBook(Player player) {
        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        List<Filterable<Component>> pages = List.of(
                Filterable.passThrough(Component.translatable("book.start.1")),
                Filterable.passThrough(Component.translatable("book.start.2")),
                Filterable.passThrough(Component.translatable("book.start.3")));
        book.set(DataComponents.WRITTEN_BOOK_CONTENT, new WrittenBookContent(Filterable.passThrough(""),
                player.getName().getString(), 3, pages, true));
        book.set(DataComponents.CUSTOM_NAME, Component.translatable("book.start.title").setStyle(Style.EMPTY.withItalic(false)));
        return book;
    }

    /** playerJoin: the client gets its knowledge (also after respawning and changing dimension). */
    @SubscribeEvent
    public static void playerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PacketSyncKnowledge.sync(player);
            ResearchNoteManager.topUpPrimals(ThaumcraftCapabilities.getAspects(player));
            player.syncData(TCAttachments.ASPECTS);
        }
    }

    @SubscribeEvent
    public static void playerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PacketSyncKnowledge.sync(player);
            player.syncData(TCAttachments.ASPECTS);
        }
    }

    @SubscribeEvent
    public static void playerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PacketSyncKnowledge.sync(player);
            player.syncData(TCAttachments.ASPECTS);
        }
    }
}
