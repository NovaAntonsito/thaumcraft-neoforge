package thaumcraft.api.research;

import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;

public class ResearchCategory {
    /** Is the smallest column used on the GUI. */
    public int minDisplayColumn;
    /** Is the smallest row used on the GUI. */
    public int minDisplayRow;
    /** Is the biggest column used on the GUI. */
    public int maxDisplayColumn;
    /** Is the biggest row used on the GUI. */
    public int maxDisplayRow;

    /** display variables **/
    public ResourceLocation icon;
    public ResourceLocation background;
    public ResourceLocation background2;

    public String researchKey;
    public String key;

    public AspectList formula;

    // Research. Linked so the GUI and commands walk entries in file order, like the 1.12 HashMap mostly did.
    public Map<String, ResearchEntry> research = new LinkedHashMap<>();

    public ResearchCategory(String key, String researchKey, AspectList formula, ResourceLocation icon, ResourceLocation background) {
        this(key, researchKey, formula, icon, background, null);
    }

    public ResearchCategory(String key, String researchKey, AspectList formula, ResourceLocation icon, ResourceLocation background, ResourceLocation background2) {
        this.key = key;
        this.researchKey = researchKey;
        this.icon = icon;
        this.background = background;
        this.background2 = background2;
        this.formula = formula;
    }

    /** For a given list of aspects, the raw knowledge you will be able to gain for this field. */
    public int applyFormula(AspectList as) {
        return applyFormula(as, 1);
    }

    /** As above, with a multiplier for the total; should usually not be needed by addon mods. */
    public int applyFormula(AspectList as, double mod) {
        if (formula == null) return 0;
        double total = 0;
        for (Aspect aspect : formula.getAspects()) {
            total += (mod * mod) * as.getAmount(aspect) * (formula.getAmount(aspect) / 10d);
        }
        if (total > 0) total = Math.sqrt(total);
        return Mth.ceil(total);
    }
}
