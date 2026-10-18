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
import thaumcraft.api.capabilities.ThaumcraftCapabilities;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.api.research.ResearchCategory;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.lib.research.ResearchManager;

/**
 * Port of ItemPechWand: the wand pech mages carry. Once the player knows the basics of auromancy, studying it uses it
 * up and gives some observation and theory in random categories, and the first time, the FOCUSPECH flag.
 */
public class ItemPechWand extends Item {
    public ItemPechWand(Item.Properties properties) {
        super(properties.rarity(Rarity.RARE));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.curio.text"));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player player, InteractionHand hand) {
        PlayerKnowledge knowledge = ThaumcraftCapabilities.getKnowledge(player);
        if (!knowledge.isResearchKnown("BASEAUROMANCY")) {
            if (!worldIn.isClientSide) {
                player.sendSystemMessage(Component.translatable("not.pechwand").withStyle(ChatFormatting.RED));
            }
            return super.use(worldIn, player, hand);
        }
        if (!player.getAbilities().instabuild) {
            player.getItemInHand(hand).shrink(1);
        }
        worldIn.playSound(null, player.getX(), player.getY(), player.getZ(), SoundsTC.LEARN.get(), SoundSource.NEUTRAL, 0.5f,
                0.4f / (worldIn.random.nextFloat() * 0.4f + 0.8f));
        if (!worldIn.isClientSide) {
            if (!knowledge.isResearchKnown("FOCUSPECH")) {
                ResearchManager.progressResearch(player, "FOCUSPECH");
                player.sendSystemMessage(Component.translatable("got.pechwand").withStyle(ChatFormatting.DARK_PURPLE));
            }
            ResearchCategory[] rc = ResearchCategories.researchCategories.values().toArray(new ResearchCategory[0]);
            if (rc.length > 0) {
                int oProg = PlayerKnowledge.EnumKnowledgeType.OBSERVATION.getProgression();
                ResearchManager.addKnowledge(player, PlayerKnowledge.EnumKnowledgeType.OBSERVATION, rc[player.getRandom().nextInt(rc.length)],
                        Mth.nextInt(player.getRandom(), oProg / 3, oProg / 2));
                int tProg = PlayerKnowledge.EnumKnowledgeType.THEORY.getProgression();
                ResearchManager.addKnowledge(player, PlayerKnowledge.EnumKnowledgeType.THEORY, rc[player.getRandom().nextInt(rc.length)],
                        Mth.nextInt(player.getRandom(), tProg / 5, tProg / 4));
            }
        }
        player.awardStat(Stats.ITEM_USED.get(this));
        return super.use(worldIn, player, hand);
    }
}
