package thaumcraft.api.crafting;

import java.util.List;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeInput;

/**
 * The 15 slots of an arcane workbench: 0-8 the 3x3 grid, 9-14 the crystal slots (air, fire, water, earth, order,
 * entropy). Replaces the 1.12 InventoryArcaneWorkbench (an InventoryCrafting of 5x3) as recipe input.
 */
public record ArcaneCraftingInput(List<ItemStack> items) implements RecipeInput, IArcaneWorkbench {
    public static final int GRID = 9;
    public static final int SIZE = 15;

    @Override
    public ItemStack getItem(int index) {
        return items.get(index);
    }

    @Override
    public int size() {
        return items.size();
    }

    /** The 3x3 grid, trimmed, with its offset inside the grid (as the vanilla crafting menu does it). */
    public CraftingInput.Positioned grid() {
        return CraftingInput.ofPositioned(3, 3, items.subList(0, GRID));
    }
}
