package thaumcraft.common.items.curios;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import thaumcraft.api.capabilities.PlayerKnowledge;
import thaumcraft.api.capabilities.PlayerWarp;
import thaumcraft.api.capabilities.ThaumcraftCapabilities;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.api.research.ResearchCategory;
import thaumcraft.common.lib.InternalMethodHandler;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.lib.research.ResearchManager;
import thaumcraft.common.registry.TCAttachments;

/**
 * Port of ItemCurio. Each 1.12 variant is its own item now; meta is the old damage value:
 * 0 arcane, 1 preserved, 2 ancient, 3 eldritch, 4 knowledge, 5 twisted, 6 the crimson rites.
 * Studying one uses it up and gives knowledge in its category plus a bit in two random ones.
 */
public class ItemCurio extends Item {
    private final int meta;

    public ItemCurio(int meta, Item.Properties properties) {
        super(properties.rarity(Rarity.UNCOMMON));
        this.meta = meta;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.curio.text"));
    }

    private static void addKnowledge(Player player, String category, int oProg, int tProg) {
        ResearchCategory rc = ResearchCategories.getResearchCategory(category);
        ResearchManager.addKnowledge(player, PlayerKnowledge.EnumKnowledgeType.OBSERVATION, rc, Mth.nextInt(player.getRandom(), oProg / 2, oProg));
        ResearchManager.addKnowledge(player, PlayerKnowledge.EnumKnowledgeType.THEORY, rc, Mth.nextInt(player.getRandom(), tProg / 3, tProg / 2));
    }

    /** InternalMethodHandler.getActualWarp: normal plus permanent warp. */
    private static int getActualWarp(Player player) {
        PlayerWarp wc = player.getData(TCAttachments.WARP);
        return wc.get(PlayerWarp.EnumWarpType.NORMAL) + wc.get(PlayerWarp.EnumWarpType.PERMANENT);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player player, InteractionHand hand) {
        worldIn.playSound(null, player.getX(), player.getY(), player.getZ(), SoundsTC.LEARN.get(), SoundSource.NEUTRAL, 0.5f,
                0.4f / (worldIn.random.nextFloat() * 0.4f + 0.8f));
        if (!worldIn.isClientSide) {
            int oProg = PlayerKnowledge.EnumKnowledgeType.OBSERVATION.getProgression();
            int tProg = PlayerKnowledge.EnumKnowledgeType.THEORY.getProgression();
            switch (meta) {
                default -> addKnowledge(player, "AUROMANCY", oProg, tProg);
                case 1 -> addKnowledge(player, "ALCHEMY", oProg, tProg);
                case 2 -> addKnowledge(player, "GOLEMANCY", oProg, tProg);
                case 3 -> {
                    addKnowledge(player, "ELDRITCH", oProg, tProg);
                    InternalMethodHandler.addWarpToPlayer(player, 1, PlayerWarp.EnumWarpType.NORMAL);
                    InternalMethodHandler.addWarpToPlayer(player, 5, PlayerWarp.EnumWarpType.TEMPORARY);
                }
                case 4 -> addKnowledge(player, "INFUSION", oProg, tProg);
                case 5 -> addKnowledge(player, "ARTIFICE", oProg, tProg);
                case 6 -> {
                    int aw = getActualWarp(player);
                    if (aw <= 20) {
                        player.sendSystemMessage(Component.translatable("fail.crimsonrites").withStyle(ChatFormatting.DARK_PURPLE));
                        return super.use(worldIn, player, hand);
                    }
                    PlayerKnowledge knowledge = ThaumcraftCapabilities.getKnowledge(player);
                    if (!knowledge.isResearchKnown("CrimsonRites")) {
                        ResearchManager.completeResearch(player, "CrimsonRites");
                    }
                    addKnowledge(player, "ELDRITCH", oProg, tProg);
                    InternalMethodHandler.addWarpToPlayer(player, 1, PlayerWarp.EnumWarpType.NORMAL);
                    InternalMethodHandler.addWarpToPlayer(player, 5, PlayerWarp.EnumWarpType.TEMPORARY);
                    if (player.getRandom().nextBoolean()) {
                        InternalMethodHandler.addWarpToPlayer(player, 1, PlayerWarp.EnumWarpType.PERMANENT);
                    }
                }
            }
            ResearchCategory[] rc = ResearchCategories.researchCategories.values().toArray(new ResearchCategory[0]);
            if (rc.length > 0) {
                ResearchManager.addKnowledge(player, PlayerKnowledge.EnumKnowledgeType.OBSERVATION, rc[player.getRandom().nextInt(rc.length)],
                        Mth.nextInt(player.getRandom(), oProg / 2, oProg));
                ResearchManager.addKnowledge(player, PlayerKnowledge.EnumKnowledgeType.THEORY, rc[player.getRandom().nextInt(rc.length)],
                        Mth.nextInt(player.getRandom(), tProg / 3, tProg / 2));
            }
            if (!player.getAbilities().instabuild) {
                player.getItemInHand(hand).shrink(1);
            }
            player.sendSystemMessage(Component.translatable("tc.knowledge.gained").withStyle(ChatFormatting.DARK_PURPLE));
        }
        player.awardStat(Stats.ITEM_USED.get(this));
        return super.use(worldIn, player, hand);
    }
}
