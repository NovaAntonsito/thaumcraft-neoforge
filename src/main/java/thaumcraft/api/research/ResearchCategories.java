package thaumcraft.api.research;

import java.util.LinkedHashMap;

import javax.annotation.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.api.aspects.AspectList;

public class ResearchCategories {
    // Research
    public static LinkedHashMap<String, ResearchCategory> researchCategories = new LinkedHashMap<>();

    /** @return the research category linked to this key */
    @Nullable
    public static ResearchCategory getResearchCategory(String key) {
        return researchCategories.get(key);
    }

    /** @return the name of the research category linked to this key ("tc.research_category." + key). */
    public static Component getCategoryName(String key) {
        return Component.translatable("tc.research_category." + key);
    }

    /** @return the research entry with this key, from any category */
    @Nullable
    public static ResearchEntry getResearch(String key) {
        for (ResearchCategory cat : researchCategories.values()) {
            for (ResearchEntry ri : cat.research.values()) {
                if (ri.key.equals(key)) return ri;
            }
        }
        return null;
    }

    /**
     * @param researchkey the research the player needs to have completed before this category becomes visible. Null to always show.
     * @param formula aspects required to gain knowledge in this category
     */
    @Nullable
    public static ResearchCategory registerCategory(String key, String researchkey, AspectList formula, ResourceLocation icon, ResourceLocation background) {
        return registerCategory(key, researchkey, formula, icon, background, null);
    }

    /** @param background2 the foreground image that lies between the background and icons */
    @Nullable
    public static ResearchCategory registerCategory(String key, String researchkey, AspectList formula, ResourceLocation icon, ResourceLocation background, ResourceLocation background2) {
        if (getResearchCategory(key) == null) {
            ResearchCategory rl = new ResearchCategory(key, researchkey, formula, icon, background, background2);
            researchCategories.put(key, rl);
            return rl;
        }
        return null;
    }
}
