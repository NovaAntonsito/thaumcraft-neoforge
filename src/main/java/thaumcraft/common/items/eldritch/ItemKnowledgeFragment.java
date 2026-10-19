package thaumcraft.common.items.eldritch;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import thaumcraft.api.capabilities.PlayerKnowledge;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.api.research.ResearchCategory;
import thaumcraft.common.lib.research.ResearchManager;

/**
 * ItemResource meta 9 of TC4: the Knowledge Fragment, dropped by Glyphed Stone. Using one used it up and gave 1-2
 * research points of each primal aspect. 1.12 has no aspect pools, so it gives a random research category half to
 * a whole observation, like the bonus a curio gives.
 */
public class ItemKnowledgeFragment extends Item {
    public ItemKnowledgeFragment(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        if (!world.isClientSide) {
            ResearchCategory[] rc = ResearchCategories.researchCategories.values().toArray(new ResearchCategory[0]);
            if (rc.length > 0) {
                int oProg = PlayerKnowledge.EnumKnowledgeType.OBSERVATION.getProgression();
                ResearchManager.addKnowledge(player, PlayerKnowledge.EnumKnowledgeType.OBSERVATION, rc[player.getRandom().nextInt(rc.length)],
                        Mth.nextInt(player.getRandom(), oProg / 2, oProg));
            }
            player.sendSystemMessage(Component.translatable("tc.knowledge.gained").withStyle(ChatFormatting.DARK_PURPLE));
        }
        player.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResultHolder.sidedSuccess(stack, world.isClientSide);
    }
}
