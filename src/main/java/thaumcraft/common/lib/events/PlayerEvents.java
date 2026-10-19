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

/** Partial port of PlayerEvents: the research hooks (knowledge sync, the crystal, the dream and the book), warp and worn gear. */
@EventBusSubscriber(modid = Thaumcraft.MODID)
public final class PlayerEvents {
    private PlayerEvents() {
    }

    /** livingTick: knowledge changed by ResearchManager is sent once a second, and warp does its work. */
    @SubscribeEvent
    public static void livingTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (player.tickCount % 20 == 0 && ResearchManager.syncList.remove(player.getUUID()) != null) {
                PacketSyncKnowledge.sync(player);
            }
            handleWarp(player);
            breakFlight(player);
        }
    }

    /** TC4 EventHandlerEntity.livingTick: no flying in the Outer Lands outside creative, checked once a second. */
    private static void breakFlight(ServerPlayer player) {
        if (thaumcraft.common.world.dim.OuterLands.is(player.level()) && !player.getAbilities().instabuild
                && player.tickCount % 20 == 0 && player.getAbilities().flying) {
            player.getAbilities().flying = false;
            player.onUpdateAbilities();
            player.sendSystemMessage(net.minecraft.network.chat.Component.translatable("tc.break.fly")
                    .withStyle(net.minecraft.ChatFormatting.ITALIC, net.minecraft.ChatFormatting.GRAY));
        }
    }

    /** A warp event every 2000 ticks (not in wuss mode, nor under warp ward), and Deadly Gaze every second. */
    private static void handleWarp(Player player) {
        if (!Config.WUSS_MODE.get() && player.tickCount > 0 && player.tickCount % 2000 == 0
                && !player.hasEffect(thaumcraft.common.registry.TCMobEffects.WARP_WARD)) {
            WarpEvents.checkWarpEvent(player);
        }
        if (player.tickCount % 20 == 0 && player.hasEffect(thaumcraft.common.registry.TCMobEffects.DEATH_GAZE)) {
            WarpEvents.checkDeathGaze(player);
        }
    }

    /**
     * finishedUsingItem: under Unnatural Hunger, rotten flesh or a zombie brain takes a level and 30 seconds off it;
     * other food does nothing for it. The lighter effect has no cure left but those foods.
     */
    /** handleSpeedMods of livingTick: the traveller boots' step height goes away when sneaking or without them. */
    @SubscribeEvent
    public static void speedMods(PlayerTickEvent.Post event) {
        thaumcraft.common.items.armor.ItemBootsTraveller.handleSpeedMods(event.getEntity());
    }

    /** playerJumps: charged traveller boots jump higher. */
    @SubscribeEvent
    public static void playerJumps(net.neoforged.neoforge.event.entity.living.LivingEvent.LivingJumpEvent event) {
        if (event.getEntity() instanceof net.minecraft.world.entity.player.Player player
                && player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.FEET).getItem() instanceof thaumcraft.common.items.armor.ItemBootsTraveller
                && thaumcraft.api.items.RechargeHelper.getCharge(player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.FEET)) > 0) {
            player.setDeltaMovement(player.getDeltaMovement().add(0.0, 0.2750000059604645, 0.0));
        }
    }

    /** onFallDamage: the traveller boots and the cloudstepper ring soften falls. */
    @SubscribeEvent
    public static void onFallDamage(net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent event) {
        if (!event.getSource().is(net.minecraft.world.damagesource.DamageTypes.FALL) || !(event.getEntity() instanceof Player player)) {
            return;
        }
        if (player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.FEET).is(thaumcraft.common.config.ConfigItems.travellerBoots.get())) {
            float f = Math.max(0.0f, event.getAmount() / 2.0f - 1.0f);
            if (f < 1.0f) {
                event.setCanceled(true);
                event.setAmount(0.0f);
            } else {
                event.setAmount(f);
            }
        }
        if (thaumcraft.common.lib.utils.BaublesHelper.isBaubleEquipped(player, thaumcraft.common.config.ConfigItems.cloudRing.get()) >= 0) {
            float f = Math.max(0.0f, event.getAmount() / 3.0f - 2.0f);
            if (f < 1.0f) {
                event.setCanceled(true);
                event.setAmount(0.0f);
            } else if (f < event.getAmount()) {
                event.setAmount(f);
            }
        }
    }

    /**
     * pickupXP: with the headband of curiosity, orbs worth more than 1 lose half their value, and that half is a
     * chance at 1 theory (5% per point) or 1 observation (20% per point) in a random category.
     */
    @SubscribeEvent
    public static void pickupXP(net.neoforged.neoforge.event.entity.player.PlayerXpEvent.PickupXp event) {
        Player player = event.getEntity();
        if (player == null || player.level().isClientSide
                || thaumcraft.common.lib.utils.BaublesHelper.isBaubleEquipped(player, thaumcraft.common.config.ConfigItems.curiosityBand.get()) < 0
                || event.getOrb().getValue() <= 1) {
            return;
        }
        net.minecraft.world.entity.ExperienceOrb orb = event.getOrb();
        int d = orb.value / 2;
        orb.value -= d;
        float r = player.getRandom().nextFloat();
        String[] s = thaumcraft.api.research.ResearchCategories.researchCategories.keySet().toArray(new String[0]);
        if (s.length == 0) {
            return;
        }
        if (r < 0.05 * d) {
            String cat = s[player.getRandom().nextInt(s.length)];
            ResearchManager.addKnowledge(player, PlayerKnowledge.EnumKnowledgeType.THEORY, thaumcraft.api.research.ResearchCategories.getResearchCategory(cat), 1);
        } else if (r < 0.2 * d) {
            String cat = s[player.getRandom().nextInt(s.length)];
            ResearchManager.addKnowledge(player, PlayerKnowledge.EnumKnowledgeType.OBSERVATION, thaumcraft.api.research.ResearchCategories.getResearchCategory(cat), 1);
        }
    }

    /** onDeath: a worn charm of undying works like a totem of undying in the hand, and is used up. */
    @SubscribeEvent
    public static void onDeath(net.neoforged.neoforge.event.entity.living.LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        int slot = thaumcraft.common.lib.utils.BaublesHelper.isBaubleEquipped(player, thaumcraft.common.config.ConfigItems.charmUndying.get());
        if (slot >= 0) {
            ItemStack charm = thaumcraft.common.lib.utils.BaublesHelper.getStackInSlot(player, slot);
            if (player instanceof ServerPlayer sp) {
                sp.awardStat(net.minecraft.stats.Stats.ITEM_USED.get(Items.TOTEM_OF_UNDYING));
                net.minecraft.advancements.CriteriaTriggers.USED_TOTEM.trigger(sp, charm);
            }
            ItemStack left = charm.copy();
            left.shrink(1);
            thaumcraft.common.lib.utils.BaublesHelper.setStackInSlot(player, slot, left);
            player.setHealth(1.0f);
            player.removeAllEffects();
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.REGENERATION, 900, 1));
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.ABSORPTION, 100, 1));
            player.level().broadcastEntityEvent(player, (byte) 35);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void finishedUsingItem(net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent.Finish event) {
        net.minecraft.world.entity.LivingEntity entity = event.getEntity();
        if (!entity.level().isClientSide && entity.hasEffect(thaumcraft.common.registry.TCMobEffects.UNNATURAL_HUNGER)) {
            if (event.getItem().is(Items.ROTTEN_FLESH) || event.getItem().is(thaumcraft.common.config.ConfigItems.brain.get())) {
                net.minecraft.world.effect.MobEffectInstance pe = entity.getEffect(thaumcraft.common.registry.TCMobEffects.UNNATURAL_HUNGER);
                int amp = pe.getAmplifier() - 1;
                int duration = pe.getDuration() - 600;
                entity.removeEffect(thaumcraft.common.registry.TCMobEffects.UNNATURAL_HUNGER);
                if (duration > 0 && amp >= 0) {
                    pe = new net.minecraft.world.effect.MobEffectInstance(thaumcraft.common.registry.TCMobEffects.UNNATURAL_HUNGER, duration, amp, true, false);
                    pe.getCures().clear();
                    entity.addEffect(pe);
                }
                entity.sendSystemMessage(Component.translatable("warp.text.hunger.2").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.ITALIC));
            } else if (event.getItem().has(net.minecraft.core.component.DataComponents.FOOD)) {
                entity.sendSystemMessage(Component.translatable("warp.text.hunger.1").withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC));
            }
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

    /** Warp an item gives while worn or held: IWarpingGear plus the warp component (the 1.12 "TC.WARP" tag). */
    public static int getFinalWarp(ItemStack stack, Player player) {
        if (stack == null || stack.isEmpty()) {
            return 0;
        }
        int warp = 0;
        if (stack.getItem() instanceof thaumcraft.api.items.IWarpingGear armor) {
            warp += armor.getWarp(stack, player);
        }
        warp += stack.getOrDefault(thaumcraft.common.registry.TCDataComponents.WARP.get(), 0);
        return warp;
    }

    public static int getFinalDiscount(ItemStack stack, Player player) {
        if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof thaumcraft.api.items.IVisDiscountGear gear)) {
            return 0;
        }
        return gear.getVisDiscount(stack, player);
    }
}
