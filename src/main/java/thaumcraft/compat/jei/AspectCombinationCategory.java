package thaumcraft.compat.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.common.config.ConfigItems;

/** How each compound aspect is made from two others, as in the research table. */
public class AspectCombinationCategory implements IRecipeCategory<AspectCombinationCategory.Combination> {
    public static final RecipeType<Combination> TYPE = RecipeType.create(Thaumcraft.MODID, "aspect_combination", Combination.class);

    public record Combination(Aspect first, Aspect second, Aspect result) {
    }

    private final IDrawable icon;
    private final IDrawableStatic slot;

    public AspectCombinationCategory(IGuiHelper gui) {
        this.icon = gui.createDrawableItemStack(new ItemStack(ConfigItems.thaumonomicon.get()));
        this.slot = gui.getSlotDrawable();
    }

    @Override
    public RecipeType<Combination> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("tc.jei.aspect_combination");
    }

    @Override
    public int getWidth() {
        return 100;
    }

    @Override
    public int getHeight() {
        return 18;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, Combination c, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 1).addIngredient(AspectIngredient.TYPE, c.first());
        builder.addSlot(RecipeIngredientRole.INPUT, 37, 1).addIngredient(AspectIngredient.TYPE, c.second());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 83, 1).addIngredient(AspectIngredient.TYPE, c.result());
    }

    @Override
    public void draw(Combination c, IRecipeSlotsView slots, GuiGraphics g, double mouseX, double mouseY) {
        var font = Minecraft.getInstance().font;
        slot.draw(g, 0, 0);
        slot.draw(g, 36, 0);
        slot.draw(g, 82, 0);
        g.drawString(font, "+", 24, 5, 0x808080, false);
        g.drawString(font, "=", 63, 5, 0x808080, false);
    }
}
