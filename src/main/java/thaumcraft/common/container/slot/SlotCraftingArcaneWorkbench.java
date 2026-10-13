package thaumcraft.common.container.slot;

import java.util.List;

import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.RecipeCraftingHolder;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.event.EventHooks;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.crafting.ArcaneCraftingInput;
import thaumcraft.api.crafting.IArcaneRecipe;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.lib.crafting.ThaumcraftCraftingManager;
import thaumcraft.common.tiles.crafting.TileArcaneWorkbench;

/** Port of SlotCraftingArcaneWorkbench: taking the result spends vis and crystals and uses up the grid. */
public class SlotCraftingArcaneWorkbench extends Slot {
    private final Container craftMatrix;
    private final Player player;
    private final TileArcaneWorkbench tile;
    private int amountCrafted;

    public SlotCraftingArcaneWorkbench(TileArcaneWorkbench te, Player player, Container result, int index, int x, int y) {
        super(result, index, x, y);
        this.player = player;
        this.craftMatrix = te.inventoryCraft;
        this.tile = te;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }

    @Override
    public ItemStack remove(int amount) {
        if (hasItem()) {
            amountCrafted += Math.min(amount, getItem().getCount());
        }
        return super.remove(amount);
    }

    @Override
    protected void onQuickCraft(ItemStack stack, int amount) {
        amountCrafted += amount;
        checkTakeAchievements(stack);
    }

    @Override
    protected void onSwapCraft(int amount) {
        amountCrafted += amount;
    }

    @Override
    protected void checkTakeAchievements(ItemStack stack) {
        if (amountCrafted > 0) {
            stack.onCraftedBy(player.level(), player, amountCrafted);
            EventHooks.firePlayerCraftingEvent(player, stack, craftMatrix);
        }
        if (container instanceof RecipeCraftingHolder holder) {
            holder.awardUsedRecipes(player, List.of());
        }
        amountCrafted = 0;
    }

    @Override
    public void onTake(Player thePlayer, ItemStack stack) {
        checkTakeAchievements(stack);
        ArcaneCraftingInput input = tile.inventoryCraft.asInput();
        RecipeHolder<IArcaneRecipe> holder = ThaumcraftCraftingManager.findMatchingArcaneRecipe(thePlayer.level(), thePlayer, input);
        IArcaneRecipe recipe = holder != null ? holder.value() : null;
        CommonHooks.setCraftingPlayer(thePlayer);
        NonNullList<ItemStack> nonnulllist = recipe != null ? recipe.getRemainingGridItems(input) : vanillaRemaining(input);
        CommonHooks.setCraftingPlayer(null);
        int vis = 0;
        AspectList crystals = null;
        if (recipe != null) {
            // TODO: vis *= 1 - CasterManager.getTotalVisDiscount(player) once casters and robes are ported.
            vis = recipe.getVis();
            crystals = recipe.getCrystals();
            if (vis > 0) {
                tile.getAura();
                tile.spendAura(vis);
            }
        }
        for (int i = 0; i < Math.min(9, nonnulllist.size()); ++i) {
            ItemStack itemstack = craftMatrix.getItem(i);
            ItemStack itemstack2 = nonnulllist.get(i);
            if (!itemstack.isEmpty()) {
                craftMatrix.removeItem(i, 1);
                itemstack = craftMatrix.getItem(i);
            }
            if (!itemstack2.isEmpty()) {
                if (itemstack.isEmpty()) {
                    craftMatrix.setItem(i, itemstack2);
                } else if (ItemStack.isSameItemSameComponents(itemstack, itemstack2)) {
                    itemstack2.grow(itemstack.getCount());
                    craftMatrix.setItem(i, itemstack2);
                } else if (!thePlayer.getInventory().add(itemstack2)) {
                    thePlayer.drop(itemstack2, false);
                }
            }
        }
        if (crystals != null) {
            for (Aspect aspect : crystals.getAspects()) {
                ItemStack cs = ConfigItems.crystalEssence.get().makeCrystal(aspect, crystals.getAmount(aspect));
                for (int j = 0; j < 6; ++j) {
                    ItemStack itemstack3 = craftMatrix.getItem(ArcaneCraftingInput.GRID + j);
                    if (ItemStack.isSameItemSameComponents(cs, itemstack3)) {
                        craftMatrix.removeItem(ArcaneCraftingInput.GRID + j, cs.getCount());
                    }
                }
            }
        }
    }

    /** Remaining items of a normal crafting recipe made in the workbench grid. */
    private NonNullList<ItemStack> vanillaRemaining(ArcaneCraftingInput input) {
        CraftingInput.Positioned positioned = input.grid();
        CraftingInput grid = positioned.input();
        NonNullList<ItemStack> out = NonNullList.withSize(ArcaneCraftingInput.GRID, ItemStack.EMPTY);
        RecipeHolder<CraftingRecipe> recipe = player.level().getRecipeManager()
                .getRecipeFor(RecipeType.CRAFTING, grid, player.level()).orElse(null);
        if (recipe == null) {
            return out;
        }
        NonNullList<ItemStack> remaining = recipe.value().getRemainingItems(grid);
        for (int y = 0; y < grid.height(); y++) {
            for (int x = 0; x < grid.width(); x++) {
                out.set(x + positioned.left() + (y + positioned.top()) * 3, remaining.get(x + y * grid.width()));
            }
        }
        return out;
    }
}
