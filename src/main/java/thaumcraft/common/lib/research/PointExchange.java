package thaumcraft.common.lib.research;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.capabilities.PlayerAspects;
import thaumcraft.api.capabilities.PlayerWarp;
import thaumcraft.api.capabilities.ThaumcraftCapabilities;
import thaumcraft.api.items.IScribeTools;
import thaumcraft.common.items.consumables.ItemCartographicNote;
import thaumcraft.common.lib.InternalMethodHandler;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.lib.utils.InventoryUtils;
import thaumcraft.common.registry.TCAttachments;
import thaumcraft.common.tiles.crafting.TileResearchTable;

/**
 * Not in Thaumcraft (dream branch): the point exchange of the research table. Spare research points buy ink, a calmer
 * mind, a rare aspect or a cartographic note. Only points of compound aspects count, and one of each is always kept:
 * the primals are topped up for free, so they buy nothing. Since points can still be made by combining aspects,
 * everything but the ink can be bought once a day.
 */
public final class PointExchange {
    public static final String RESEARCH = "POINTEXCHANGE";
    private static final long COOLDOWN = 24000L;

    public enum Offer {
        INK(10, false), CALM(15, true), RARE(20, true), CARTOGRAPHY(30, true);

        public final int cost;
        public final boolean daily;

        Offer(int cost, boolean daily) {
            this.cost = cost;
            this.daily = daily;
        }

        public String key() {
            return "tc.exchange." + name().toLowerCase(java.util.Locale.ROOT);
        }
    }

    private PointExchange() {
    }

    /** The points the exchange can take: every compound aspect above its first point. */
    public static int surplus(PlayerAspects aspects) {
        int sum = 0;
        for (Aspect a : aspects.aspectsPool().getAspects()) {
            if (!a.isPrimal()) sum += Math.max(0, aspects.aspectCount(a) - 1);
        }
        return sum;
    }

    /** Takes the cost one point at a time from the compound aspect with the most points. */
    private static void pay(ServerPlayer player, int cost) {
        PlayerAspects aspects = ThaumcraftCapabilities.getAspects(player);
        for (int i = 0; i < cost; ++i) {
            Aspect best = null;
            for (Aspect a : aspects.aspectsPool().getAspects()) {
                if (!a.isPrimal() && aspects.aspectCount(a) > 1 && (best == null || aspects.aspectCount(a) > aspects.aspectCount(best))) best = a;
            }
            if (best == null) return;
            ResearchNoteManager.spendAspect(player, best, 1);
        }
    }

    private static String cooldownKey(Offer offer) {
        return "thaumcraft.exchange." + offer.name().toLowerCase(java.util.Locale.ROOT);
    }

    public static void exchange(ServerPlayer player, TileResearchTable table, Offer offer) {
        if (!ThaumcraftCapabilities.knowsResearch(player, RESEARCH)) return;
        PlayerAspects aspects = ThaumcraftCapabilities.getAspects(player);
        if (surplus(aspects) < offer.cost) {
            fail(player, Component.translatable("tc.exchange.poor", offer.cost));
            return;
        }
        long now = player.level().getGameTime();
        long last = player.getPersistentData().getLong(cooldownKey(offer));
        if (offer.daily && last > 0 && now - last < COOLDOWN) {
            fail(player, Component.translatable("tc.exchange.wait", (COOLDOWN - (now - last)) / 1200 + 1));
            return;
        }
        switch (offer) {
            case INK -> {
                ItemStack tools = table.getItem(0);
                if (!(tools.getItem() instanceof IScribeTools) || tools.getDamageValue() == 0) {
                    fail(player, Component.translatable("tc.exchange.ink.full"));
                    return;
                }
                tools.setDamageValue(0);
                table.setChanged();
                table.syncTile(false);
            }
            case CALM -> {
                if (player.getData(TCAttachments.WARP).get(PlayerWarp.EnumWarpType.TEMPORARY) <= 0) {
                    fail(player, Component.translatable("tc.exchange.calm.none"));
                    return;
                }
                InternalMethodHandler.addWarpToPlayer(player, -2, PlayerWarp.EnumWarpType.TEMPORARY);
            }
            case RARE -> {
                pay(player, offer.cost);
                rareAspect(player, aspects);
            }
            case CARTOGRAPHY -> {
                if (!InventoryUtils.isPlayerCarryingAmount(player, new ItemStack(Items.PAPER))) {
                    fail(player, Component.translatable("tile.researchtable.need.paper"));
                    return;
                }
                InventoryUtils.consumePlayerItem(player, new ItemStack(Items.PAPER), true);
                ItemStack note = ItemCartographicNote.create(CartographyTargets.random(player.getRandom()), player.getRandom());
                if (!player.getInventory().add(note)) player.drop(note, false);
            }
        }
        if (offer != Offer.RARE) pay(player, offer.cost);
        if (offer.daily) player.getPersistentData().putLong(cooldownKey(offer), now);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundsTC.WRITE.get(), SoundSource.PLAYERS, 0.75f, 1.0f);
    }

    /** An aspect the player could discover next (both components known), else three points of the most complex one known. */
    private static void rareAspect(ServerPlayer player, PlayerAspects aspects) {
        List<Aspect> next = new ArrayList<>();
        Aspect hardest = null;
        for (Aspect a : Aspect.aspects.values()) {
            if (a.isPrimal()) continue;
            if (!aspects.isKnowAspect(a) && aspects.isKnowParentAspect(a)) next.add(a);
            if (aspects.isKnowAspect(a) && (hardest == null
                    || ResearchNoteManager.getAspectComplexity(a) > ResearchNoteManager.getAspectComplexity(hardest))) hardest = a;
        }
        if (!next.isEmpty()) {
            ResearchNoteManager.checkAndSyncAspectKnowledge(player, next.get(player.getRandom().nextInt(next.size())), 1);
        } else if (hardest != null) {
            ResearchNoteManager.checkAndSyncAspectKnowledge(player, hardest, 3);
        }
    }

    private static void fail(Player player, Component message) {
        player.displayClientMessage(message.copy().withStyle(ChatFormatting.RED), true);
    }
}
