package thaumcraft.api.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Port of ILabelable: blocks (or tiles) a jar label can be applied to. */
public interface ILabelable {
    /** Returns true if the label was applied; the label item is then consumed. */
    boolean applyLabel(Player player, BlockPos pos, Direction side, ItemStack labelstack);
}
