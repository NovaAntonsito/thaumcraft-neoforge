package thaumcraft.client.lib;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;

/**
 * The row of aspect icons under an item's tooltip (HudHandler.renderAspectsInGui). The original padded the
 * tooltip with blank lines and drew over them; here it is a tooltip component.
 */
public record AspectTooltip(AspectList tags) implements TooltipComponent {

    public static final class Client implements ClientTooltipComponent {
        private final AspectList tags;

        public Client(AspectTooltip tooltip) {
            this.tags = tooltip.tags();
        }

        @Override
        public int getHeight() {
            return 18;
        }

        @Override
        public int getWidth(Font font) {
            return tags.size() * 18;
        }

        @Override
        public void renderImage(Font font, int x, int y, GuiGraphics g) {
            int index = 0;
            for (Aspect tag : tags.getAspectsSortedByAmount()) {
                if (tag != null) {
                    UtilsFX.drawTag(g, x + index * 18, y, tag, tags.getAmount(tag), 0);
                    ++index;
                }
            }
        }
    }
}
