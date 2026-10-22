package thaumcraft.compat.jade;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.phys.Vec2;
import snownee.jade.api.ui.Element;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.client.lib.UtilsFX;

/** A row of aspect icons with their amounts, like the item tooltip; wraps after eight. */
public class AspectsElement extends Element {
    private static final int PER_ROW = 8;
    private final AspectList aspects;

    public AspectsElement(AspectList aspects) {
        this.aspects = aspects;
    }

    @Override
    public Vec2 getSize() {
        int n = aspects.size();
        return new Vec2(Math.min(n, PER_ROW) * 18, ((n + PER_ROW - 1) / PER_ROW) * 18);
    }

    @Override
    public void render(GuiGraphics g, float x, float y, float maxX, float maxY) {
        int i = 0;
        for (Aspect a : aspects.getAspectsSortedByAmount()) {
            if (a == null) continue;
            UtilsFX.drawTag(g, (int) x + (i % PER_ROW) * 18, (int) y + 1 + (i / PER_ROW) * 18, a, aspects.getAmount(a), 0);
            i++;
        }
    }

    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder();
        for (Aspect a : aspects.getAspectsSortedByAmount()) {
            if (a == null) continue;
            if (!sb.isEmpty()) sb.append(", ");
            sb.append(a.getName()).append(' ').append(aspects.getAmount(a));
        }
        return sb.toString();
    }
}
