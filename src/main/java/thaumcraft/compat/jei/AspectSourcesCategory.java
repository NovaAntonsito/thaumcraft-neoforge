package thaumcraft.compat.jei;

import java.util.List;

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

/** "What has Ignis?": one page per aspect (more if it is common) with every item that contains it. */
public class AspectSourcesCategory implements IRecipeCategory<AspectSourcesCategory.Page> {
    public static final RecipeType<Page> TYPE = RecipeType.create(Thaumcraft.MODID, "aspect_sources", Page.class);
    public static final int COLUMNS = 9;
    public static final int ROWS = 5;
    private static final int TOP = 22;

    /** One page: the aspect and up to COLUMNS x ROWS items, each with how much of it they hold. */
    public record Page(Aspect aspect, List<ItemStack> items, List<Integer> amounts, int page, int pages) {
    }

    private final IDrawable icon;
    private final IDrawableStatic slot;

    public AspectSourcesCategory(IGuiHelper gui) {
        this.icon = gui.createDrawableItemStack(new ItemStack(ConfigItems.thaumometer.get()));
        this.slot = gui.getSlotDrawable();
    }

    @Override
    public RecipeType<Page> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("tc.jei.aspect_sources");
    }

    @Override
    public int getWidth() {
        return COLUMNS * 18;
    }

    @Override
    public int getHeight() {
        return TOP + ROWS * 18;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, Page page, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.OUTPUT, 1, 1).addIngredient(AspectIngredient.TYPE, page.aspect());
        for (int i = 0; i < page.items().size(); i++) {
            builder.addSlot(RecipeIngredientRole.INPUT, 1 + (i % COLUMNS) * 18, TOP + 1 + (i / COLUMNS) * 18).addItemStack(page.items().get(i));
        }
    }

    @Override
    public void draw(Page page, IRecipeSlotsView slots, GuiGraphics g, double mouseX, double mouseY) {
        var font = Minecraft.getInstance().font;
        g.drawString(font, page.aspect().getDisplayName(), 22, 1, 0xFFFFFF, true);
        Component sub = page.pages() > 1
                ? Component.translatable("tc.jei.aspect_sources.page", page.items().size(), page.page() + 1, page.pages())
                : Component.translatable("tc.jei.aspect_sources.count", page.items().size());
        g.drawString(font, sub, 22, 11, 0x808080, false);
        for (int i = 0; i < page.items().size(); i++) {
            slot.draw(g, (i % COLUMNS) * 18, TOP + (i / COLUMNS) * 18);
        }
        // How much of the aspect each item holds, small in the corner like the aspect tooltip.
        g.pose().pushPose();
        g.pose().translate(0, 0, 200);
        g.pose().scale(0.5f, 0.5f, 1.0f);
        for (int i = 0; i < page.items().size(); i++) {
            String n = Integer.toString(page.amounts().get(i));
            int x = (1 + (i % COLUMNS) * 18 + 17) * 2 - font.width(n);
            int y = (TOP + 1 + (i / COLUMNS) * 18) * 2;
            g.drawString(font, n, x, y, page.aspect().getColor(), true);
        }
        g.pose().popPose();
    }
}
