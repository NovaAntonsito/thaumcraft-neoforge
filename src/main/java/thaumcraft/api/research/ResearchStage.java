package thaumcraft.api.research;

import java.util.Locale;

import javax.annotation.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.api.capabilities.PlayerKnowledge.EnumKnowledgeType;

public class ResearchStage {
    String text;
    ResourceLocation[] recipes;
    /** ItemStack, or a String naming an item tag (the 1.12 ore dictionary entry), or an {@link EnchantmentRequirement}. */
    Object[] obtain;
    /** ItemStack, or a String naming an item tag. */
    Object[] craft;
    int[] craftReference;
    Knowledge[] know;
    String[] research;
    String[] researchIcon;
    int warp;

    public String getText() {
        return text;
    }

    public Component getTextLocalized() {
        return Component.translatable(getText());
    }

    public void setText(String text) {
        this.text = text;
    }

    public ResourceLocation[] getRecipes() {
        return recipes;
    }

    public void setRecipes(ResourceLocation[] recipes) {
        this.recipes = recipes;
    }

    public Object[] getObtain() {
        return obtain;
    }

    public void setObtain(Object[] obtain) {
        this.obtain = obtain;
    }

    public Object[] getCraft() {
        return craft;
    }

    public void setCraft(Object[] craft) {
        this.craft = craft;
    }

    public int[] getCraftReference() {
        return craftReference;
    }

    public void setCraftReference(int[] craftReference) {
        this.craftReference = craftReference;
    }

    public Knowledge[] getKnow() {
        return know;
    }

    public void setKnow(Knowledge[] know) {
        this.know = know;
    }

    public String[] getResearch() {
        return research;
    }

    public void setResearch(String[] research) {
        this.research = research;
    }

    public String[] getResearchIcon() {
        return researchIcon;
    }

    public void setResearchIcon(String[] research) {
        researchIcon = research;
    }

    public int getWarp() {
        return warp;
    }

    public void setWarp(int warp) {
        this.warp = warp;
    }

    /**
     * Replaces the 1.12 enchanted_placeholder item: any item carrying this enchantment (or a book storing it)
     * at the given level or higher.
     */
    public record EnchantmentRequirement(ResourceLocation enchantment, int level) {
    }

    public static class Knowledge {
        public EnumKnowledgeType type;
        public ResearchCategory category;
        public int amount = 0;

        public Knowledge(EnumKnowledgeType type, ResearchCategory category, int num) {
            this.type = type;
            this.category = category;
            amount = num;
        }

        @Nullable
        public static Knowledge parse(String text) {
            String[] s = text.split(";");
            if (s.length == 2) {
                int num = 0;
                try {
                    num = Integer.parseInt(s[1]);
                } catch (Exception e) {
                }
                EnumKnowledgeType t = EnumKnowledgeType.valueOf(s[0].toUpperCase(Locale.ROOT));
                if (!t.hasFields() && num > 0) {
                    return new Knowledge(t, null, num);
                }
            } else if (s.length == 3) {
                int num = 0;
                try {
                    num = Integer.parseInt(s[2]);
                } catch (Exception e) {
                }
                EnumKnowledgeType t = EnumKnowledgeType.valueOf(s[0].toUpperCase(Locale.ROOT));
                ResearchCategory f = ResearchCategories.getResearchCategory(s[1].toUpperCase(Locale.ROOT));
                if (f != null && num > 0) {
                    return new Knowledge(t, f, num);
                }
            }
            return null;
        }
    }
}
