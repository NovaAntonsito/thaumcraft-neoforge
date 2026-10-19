package thaumcraft.compat.jei;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.TooltipFlag;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.client.lib.UtilsFX;

/** Aspects as a JEI ingredient: listed with the items, searchable by name, drawn as their icon. */
public final class AspectIngredient {
    public static final IIngredientType<Aspect> TYPE = () -> Aspect.class;

    private AspectIngredient() {
    }

    public static final class Helper implements IIngredientHelper<Aspect> {
        @Override
        public IIngredientType<Aspect> getIngredientType() {
            return TYPE;
        }

        @Override
        public String getDisplayName(Aspect aspect) {
            return aspect.getName();
        }

        @Override
        public String getUniqueId(Aspect aspect, UidContext context) {
            return "thaumcraft:aspect/" + aspect.getTag();
        }

        @Override
        public ResourceLocation getResourceLocation(Aspect aspect) {
            return Thaumcraft.id(aspect.getTag());
        }

        @Override
        public Aspect copyIngredient(Aspect aspect) {
            return aspect;
        }

        @Override
        public String getErrorInfo(@Nullable Aspect aspect) {
            return aspect == null ? "null aspect" : "aspect " + aspect.getTag();
        }

        @Override
        public Iterable<Integer> getColors(Aspect aspect) {
            return List.of(aspect.getColor());
        }
    }

    public static final class Renderer implements IIngredientRenderer<Aspect> {
        @Override
        public void render(GuiGraphics g, Aspect aspect) {
            UtilsFX.drawTag(g, 0, 0, aspect, 0, 0);
        }

        @Override
        public List<Component> getTooltip(Aspect aspect, TooltipFlag flag) {
            List<Component> lines = new ArrayList<>();
            lines.add(aspect.getDisplayName());
            lines.add(aspect.getLocalizedDescription().withStyle(ChatFormatting.GRAY));
            Aspect[] parts = aspect.getComponents();
            if (parts != null && parts.length == 2) {
                lines.add(Component.translatable("tc.jei.aspect.components", parts[0].getDisplayName(), parts[1].getDisplayName())
                        .withStyle(ChatFormatting.DARK_GRAY));
            } else {
                lines.add(Component.translatable("tc.jei.aspect.primal").withStyle(ChatFormatting.DARK_GRAY));
            }
            return lines;
        }
    }
}
