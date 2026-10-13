package thaumcraft.common.items.consumables;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
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
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.api.research.ResearchEntry;
import thaumcraft.client.ClientPacketHandlers;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.lib.network.playerdata.PacketSyncKnowledge;
import thaumcraft.common.lib.research.ResearchManager;
import thaumcraft.common.lib.research.ResearchNoteData;
import thaumcraft.common.lib.research.ResearchNoteManager;
import thaumcraft.common.registry.TCDataComponents;

/**
 * The TC4 research note, from TC4 Research Port: Reborn. Solved at a research table it becomes a discovery
 * (metadata 64 in 1.12, the "complete" flag of its data here); using a discovery completes that research stage.
 */
public class ItemResearchNote extends Item {
    public ItemResearchNote(Item.Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.RARE));
    }

    /** A copy of the note's grid, or null if the stack is not a written note. */
    @Nullable
    public static ResearchNoteData getData(ItemStack stack) {
        ResearchNoteData data = stack.get(TCDataComponents.RESEARCH_NOTE);
        return data != null ? data.copy() : null;
    }

    public static void setData(ItemStack stack, ResearchNoteData data) {
        stack.set(TCDataComponents.RESEARCH_NOTE, data.copy());
        // EnumRarity.RARE for a note, UNCOMMON for a discovery (rarity is a component in 1.21).
        stack.set(DataComponents.RARITY, data.complete ? Rarity.UNCOMMON : Rarity.RARE);
    }

    public static boolean isComplete(ItemStack stack) {
        ResearchNoteData data = stack.get(TCDataComponents.RESEARCH_NOTE);
        return data != null && data.complete;
    }

    /** The ribbon and scroll tint. */
    public static int getColor(ItemStack stack) {
        ResearchNoteData data = stack.get(TCDataComponents.RESEARCH_NOTE);
        return data != null ? data.color : 2337949;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        ResearchNoteData data = getData(stack);
        if (data != null && data.isComplete() && !ThaumcraftCapabilities.getKnowledge(player).isResearchComplete(data.key)) {
            if (!world.isClientSide) {
                ResearchManager.progressResearch(player, data.key);
                if (player instanceof ServerPlayer sp) PacketSyncKnowledge.sync(sp);
                world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundsTC.LEARN.get(), SoundSource.PLAYERS, 0.75f, 1.0f);
            } else {
                ResearchEntry entry = ResearchCategories.getResearch(ResearchNoteManager.getStrippedKey(data.key));
                if (entry != null) ClientPacketHandlers.showNoteToast(entry);
            }
            return InteractionResultHolder.sidedSuccess(ItemStack.EMPTY, world.isClientSide);
        }
        return InteractionResultHolder.fail(stack);
    }

    /** item.thaumcraft.research_note / item.thaumcraft.discovery */
    @Override
    public String getDescriptionId(ItemStack stack) {
        return isComplete(stack) ? "item.thaumcraft.discovery" : getDescriptionId();
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        ResearchNoteData rd = stack.get(TCDataComponents.RESEARCH_NOTE);
        if (rd != null && !rd.key.isEmpty()) {
            ResearchEntry re = ResearchCategories.getResearch(ResearchNoteManager.getStrippedKey(rd.key));
            if (re != null) {
                tooltip.add(Component.translatable("tc.research_category." + re.getCategory()).withStyle(ChatFormatting.YELLOW)
                        .append(": ").append(re.getLocalizedName().copy().withStyle(ChatFormatting.WHITE)));
            }
        }
    }
}
