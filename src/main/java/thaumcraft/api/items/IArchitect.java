package thaumcraft.api.items;

import java.util.ArrayList;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

public interface IArchitect {

	/**
	 * Returns the location that should be used as the starting point. 
	 */
	public HitResult getArchitectMOP(ItemStack stack, Level world, LivingEntity player);

	/**
	 * @return will this trigger on block highlighting event
	 */
	public boolean useBlockHighlight(ItemStack stack);

	/**
	 * Returns a list of blocks that should be highlighted in world. The starting point is whichever block the player currently has highlighted in the world.
	 */
	public ArrayList<BlockPos> getArchitectBlocks(ItemStack stack, Level world, 
			BlockPos pos, Direction side, Player player);
	
	/**
	 * which axis should be displayed. 
	 */
	public boolean showAxis(ItemStack stack, Level world, Player player, Direction side, 
			EnumAxis axis);
	
	public enum EnumAxis {
		X, // east / west
		Y, // up / down
		Z; // north / south
	}
}
