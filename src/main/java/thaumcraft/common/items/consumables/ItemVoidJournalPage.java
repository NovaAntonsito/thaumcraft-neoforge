package thaumcraft.common.items.consumables;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import thaumcraft.api.capabilities.ThaumcraftCapabilities;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.lib.network.playerdata.PacketSyncKnowledge;
import thaumcraft.common.lib.research.ResearchManager;
import thaumcraft.common.registry.TCDataComponents;
import thaumcraft.common.world.voidrealm.VoidEncounters;

/**
 * Dream branch: a page of the lost explorer's journal, found in each void labyrinth. Reading it adds its entry to the
 * Whispers of the Void chapter, which ends that encounter; the fifth also opens the last entry.
 */
public class ItemVoidJournalPage extends Item implements thaumcraft.common.items.ISubItems {
    public ItemVoidJournalPage(Item.Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.EPIC));
    }

    public static ItemStack page(int n) {
        ItemStack stack = new ItemStack(ConfigItems.voidJournalPage.get());
        stack.set(TCDataComponents.JOURNAL_PAGE.get(), n);
        return stack;
    }

    public static int getPage(ItemStack stack) {
        return stack.getOrDefault(TCDataComponents.JOURNAL_PAGE.get(), 1);
    }

    @Override
    public void fillSubItems(java.util.function.Consumer<ItemStack> output) {
        for (int n = 1; n <= 5; ++n) output.accept(page(n));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        int n = getPage(stack);
        String key = VoidEncounters.PAGE + n;
        if (ThaumcraftCapabilities.knowsResearchStrict(player, key)) {
            if (!world.isClientSide) player.displayClientMessage(Component.translatable("tc.void.page.known").withStyle(ChatFormatting.GRAY), true);
            return InteractionResultHolder.fail(stack);
        }
        if (player instanceof ServerPlayer sp) {
            ResearchManager.completeResearch(sp, key);
            if (n >= 5) ResearchManager.completeResearch(sp, VoidEncounters.FINAL);
            VoidEncounters.forgetRift(sp);
            PacketSyncKnowledge.sync(sp);
            world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundsTC.LEARN.get(), SoundSource.PLAYERS, 0.75f, 1.0f);
            player.displayClientMessage(Component.translatable("tc.void.page.read").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC), true);
        }
        stack.shrink(1);
        return InteractionResultHolder.sidedSuccess(stack, world.isClientSide);
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable("item.thaumcraft.void_journal_page", Component.translatable("tc.void.page." + getPage(stack)));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.thaumcraft.void_journal_page.text").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
    }
}
