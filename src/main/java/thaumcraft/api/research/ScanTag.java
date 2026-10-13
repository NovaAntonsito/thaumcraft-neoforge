package thaumcraft.api.research;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Port of ScanOreDictionary: unlocks a research key when an item (or the block's item) in any of the tags is scanned.
 * The ore dictionary names became item tags.
 */
public class ScanTag implements IScanThing {
    String research;
    TagKey<Item>[] entries;

    @SafeVarargs
    public ScanTag(String research, TagKey<Item>... entries) {
        this.research = research;
        this.entries = entries;
    }

    @Override
    public boolean checkThing(Player player, Object obj) {
        ItemStack stack = obj instanceof BlockPos ? new ItemStack(player.level().getBlockState((BlockPos) obj).getBlock().asItem())
                : ScanningManager.getItemFromParms(player, obj);
        if (stack.isEmpty()) return false;
        for (TagKey<Item> entry : entries) {
            if (stack.is(entry)) return true;
        }
        return false;
    }

    @Override
    public String getResearchKey(Player player, Object object) {
        return research;
    }
}
