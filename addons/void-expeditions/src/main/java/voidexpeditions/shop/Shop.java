package voidexpeditions.shop;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import voidexpeditions.perk.Perk;
import voidexpeditions.perk.Perks;
import voidexpeditions.registry.VEAttachments;
import voidexpeditions.registry.VEItems;
import voidexpeditions.run.RunState;

/**
 * What void shards buy at the terminal, for the next run: an extra life and a starting perk (paid every run), and a
 * focus of random powers, kept for good but only one per run.
 */
public final class Shop {
    public static final int EXTRA_LIFE = 6;
    public static final int START_PERK = 4;
    public static final int RANDOM_FOCUS = 15;

    public enum Offer {
        EXTRA_LIFE, START_PERK, RANDOM_FOCUS
    }

    private Shop() {
    }

    public static int price(Offer offer) {
        return switch (offer) {
            case EXTRA_LIFE -> EXTRA_LIFE;
            case START_PERK -> START_PERK;
            case RANDOM_FOCUS -> RANDOM_FOCUS;
        };
    }

    /** The perks that can be bought to start with: all but the cursed ones. */
    public static boolean canStartWith(Perk perk) {
        return perk.category() != Perk.Category.CURSED;
    }

    public static void buy(ServerPlayer player, Offer offer, String perkId) {
        RunState run = player.getData(VEAttachments.RUN);
        if (run.active) {
            say(player, "voidexpeditions.shop.during_run", ChatFormatting.RED);
            return;
        }
        switch (offer) {
            case EXTRA_LIFE -> {
                if (run.nextExtraLife || !pay(player, EXTRA_LIFE)) return;
                run.nextExtraLife = true;
            }
            case START_PERK -> {
                Perk perk = Perks.get(perkId);
                if (perk == null || !canStartWith(perk) || !run.nextStartPerk.isEmpty() || !pay(player, START_PERK)) return;
                run.nextStartPerk = perk.id();
            }
            case RANDOM_FOCUS -> {
                if (run.focusBought) {
                    say(player, "voidexpeditions.shop.focus_once", ChatFormatting.RED);
                    return;
                }
                ItemStack focus = RandomFocus.make(player.getRandom());
                if (focus.isEmpty() || !pay(player, RANDOM_FOCUS)) return;
                run.focusBought = true;
                if (!player.getInventory().add(focus)) player.drop(focus, false);
            }
        }
        player.syncData(VEAttachments.RUN);
        say(player, "voidexpeditions.shop.bought", ChatFormatting.LIGHT_PURPLE);
    }

    /** Takes the shards if the player has enough. */
    private static boolean pay(ServerPlayer player, int price) {
        int have = player.getInventory().countItem(VEItems.VOID_SHARD.get());
        if (have < price) {
            say(player, "voidexpeditions.shop.too_poor", ChatFormatting.RED);
            return false;
        }
        player.getInventory().clearOrCountMatchingItems(s -> s.is(VEItems.VOID_SHARD.get()), price,
                player.inventoryMenu.getCraftSlots());
        return true;
    }

    private static void say(ServerPlayer player, String key, ChatFormatting color) {
        player.displayClientMessage(Component.translatable(key).withStyle(color), true);
    }
}
