package thaumcraft.common.items.curios;

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
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import thaumcraft.Config;
import thaumcraft.api.capabilities.ThaumcraftCapabilities;
import thaumcraft.api.research.ResearchCategory;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.api.research.ResearchEntry;
import thaumcraft.client.ClientPacketHandlers;
import thaumcraft.common.command.ThaumcraftCommand;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.lib.network.playerdata.PacketSyncKnowledge;
import thaumcraft.common.lib.research.ResearchManager;

/**
 * Port of ItemThaumonomicon. The "normal" and "cheat" metadata variants are two items now:
 * thaumonomicon and thaumonomicon_cheat (creative only, needs allowCheatSheet).
 */
public class ItemThaumonomicon extends Item {
    private final boolean cheat;

    public ItemThaumonomicon(boolean cheat, Properties properties) {
        super(properties.stacksTo(1));
        this.cheat = cheat;
    }

    public boolean isCheat() {
        return cheat;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (cheat) tooltip.add(Component.literal("Creative only").withStyle(ChatFormatting.DARK_PURPLE));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        if (!world.isClientSide) {
            if (Config.ALLOW_CHEAT_SHEET.getAsBoolean() && cheat) {
                for (ResearchCategory cat : ResearchCategories.researchCategories.values()) {
                    for (ResearchEntry ri : cat.research.values()) {
                        ThaumcraftCommand.giveRecursiveResearch(player, ri.getKey());
                    }
                }
            } else {
                for (ResearchCategory cat : ResearchCategories.researchCategories.values()) {
                    for (ResearchEntry ri : cat.research.values()) {
                        if (ThaumcraftCapabilities.knowsResearch(player, ri.getKey()) && ri.getSiblings() != null) {
                            for (String sib : ri.getSiblings()) {
                                if (!ThaumcraftCapabilities.knowsResearch(player, sib)) ResearchManager.completeResearch(player, sib);
                            }
                        }
                    }
                }
            }
            if (player instanceof ServerPlayer sp) PacketSyncKnowledge.sync(sp);
        } else {
            world.playLocalSound(player.getX(), player.getY(), player.getZ(), SoundsTC.PAGE.get(), SoundSource.PLAYERS, 1.0f, 1.0f, false);
            ClientPacketHandlers.openResearchBrowser();
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), world.isClientSide);
    }
}
