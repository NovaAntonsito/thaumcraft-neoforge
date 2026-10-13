package thaumcraft.api.research;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

/** Unlocks a research key when one of the blocks is scanned, in the world or as an item. */
public class ScanBlock implements IScanThing {
    String research;
    Block[] blocks;

    public ScanBlock(Block block) {
        this("!" + BuiltInRegistries.BLOCK.getKey(block), block);
    }

    public ScanBlock(String research, Block... blocks) {
        this.research = research;
        this.blocks = blocks;
        for (Block block : blocks) {
            ItemStack stack = new ItemStack(block);
            if (!stack.isEmpty()) ScanningManager.addScannableThing(new ScanItem(research, stack));
        }
    }

    @Override
    public boolean checkThing(Player player, Object obj) {
        if (obj instanceof BlockPos pos) {
            for (Block block : blocks) {
                if (player.level().getBlockState(pos).is(block)) return true;
            }
        }
        return false;
    }

    @Override
    public String getResearchKey(Player player, Object object) {
        return research;
    }
}
