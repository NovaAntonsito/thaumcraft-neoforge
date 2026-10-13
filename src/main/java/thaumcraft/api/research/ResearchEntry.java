package thaumcraft.api.research;

import java.util.Arrays;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import thaumcraft.api.research.ResearchStage.Knowledge;

public class ResearchEntry {
    /** A short string used as a key for this research. Must be unique */
    String key;
    /** A short string used as a reference to the research category to which this must be added. */
    String category;
    /** A text name of the research entry. Can be a localizable string. */
    String name;
    /** This links to any research that needs to be completed before this research can be discovered or learnt. */
    String[] parents;
    /** any research linked to this that will be unlocked automatically when this research is complete */
    String[] siblings;
    /** the horizontal position of the research icon */
    int displayColumn;
    /** the vertical position of the research icon */
    int displayRow;
    /** the icon to be used for this research: an ItemStack, a texture ResourceLocation or a "focus:" string */
    Object[] icons;
    /** special meta-data tags that indicate how this research must be handled */
    EnumResearchMeta[] meta;
    /** items the player will receive on completion of this research */
    ItemStack[] rewardItem;
    /** knowledge the player will receive on completion of this research */
    Knowledge[] rewardKnow;

    public enum EnumResearchMeta {
        ROUND,
        SPIKY, // these also grant .5 bonus inspiration for theorycrafting
        REVERSE,
        HIDDEN, // these also grant .1 bonus inspiration for theorycrafting
        AUTOUNLOCK,
        HEX
    }

    /** The various stages present in this research entry */
    ResearchStage[] stages;
    /** The various addena present in this research entry */
    ResearchAddendum[] addenda;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public Component getLocalizedName() {
        return Component.translatable(getName());
    }

    public void setName(String name) {
        this.name = name;
    }

    public String[] getParents() {
        return parents;
    }

    /** @return parents with ALL prefixes and postfixes stripped away */
    public String[] getParentsClean() {
        String[] out = null;
        if (parents != null) {
            out = getParentsStripped();
            for (int q = 0; q < out.length; q++) {
                if (out[q].contains("@")) out[q] = out[q].substring(0, out[q].indexOf("@"));
            }
        }
        return out;
    }

    /** @return parents with prefixes stripped away */
    public String[] getParentsStripped() {
        String[] out = null;
        if (parents != null) {
            out = new String[parents.length];
            for (int q = 0; q < out.length; q++) {
                out[q] = parents[q];
                if (out[q].startsWith("~")) out[q] = out[q].substring(1);
            }
        }
        return out;
    }

    public void setParents(String[] parents) {
        this.parents = parents;
    }

    public String[] getSiblings() {
        return siblings;
    }

    public void setSiblings(String[] siblings) {
        this.siblings = siblings;
    }

    public int getDisplayColumn() {
        return displayColumn;
    }

    public void setDisplayColumn(int displayColumn) {
        this.displayColumn = displayColumn;
    }

    public int getDisplayRow() {
        return displayRow;
    }

    public void setDisplayRow(int displayRow) {
        this.displayRow = displayRow;
    }

    public Object[] getIcons() {
        return icons;
    }

    public void setIcons(Object[] icons) {
        this.icons = icons;
    }

    public EnumResearchMeta[] getMeta() {
        return meta;
    }

    public boolean hasMeta(EnumResearchMeta me) {
        return meta != null && Arrays.asList(meta).contains(me);
    }

    public void setMeta(EnumResearchMeta[] meta) {
        this.meta = meta;
    }

    public ResearchStage[] getStages() {
        return stages;
    }

    public void setStages(ResearchStage[] stages) {
        this.stages = stages;
    }

    public ItemStack[] getRewardItem() {
        return rewardItem;
    }

    public void setRewardItem(ItemStack[] rewardItem) {
        this.rewardItem = rewardItem;
    }

    public Knowledge[] getRewardKnow() {
        return rewardKnow;
    }

    public void setRewardKnow(Knowledge[] rewardKnow) {
        this.rewardKnow = rewardKnow;
    }

    public ResearchAddendum[] getAddenda() {
        return addenda;
    }

    public void setAddenda(ResearchAddendum[] addenda) {
        this.addenda = addenda;
    }
}
