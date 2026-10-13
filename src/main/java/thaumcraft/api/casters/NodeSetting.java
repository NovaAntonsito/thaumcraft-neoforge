package thaumcraft.api.casters;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.util.Mth;

public class NodeSetting {

    int value;
    public String key;
    String description;
    INodeSettingType type;
    String research;

    public NodeSetting(String key, String description, INodeSettingType setting, String research) {
        this.key = key;
        type = setting;
        value = setting.getDefault();
        this.description = description;
        this.research = research;
    }

    public NodeSetting(String key, String description, INodeSettingType setting) {
        this(key, description, setting, null);
    }

    public int getValue() {
        return type.getValue(value);
    }

    public void setValue(int truevalue) {
        int lv = -1;
        value = 0;
        while (getValue() != truevalue && lv != value) {
            lv = value;
            increment();
        }
    }

    /**
     * This setting will only be visible if this research is unlocked. If not the default will be used.
     */
    public String getResearch() {
        return research;
    }

    /** Client only (I18n). */
    public String getValueText() {
        return I18n.get(type.getValueText(value));
    }

    public void increment() {
        value++;
        value = getType().clamp(value);
    }

    public void decrement() {
        value--;
        value = getType().clamp(value);
    }

    public INodeSettingType getType() {
        return type;
    }

    /** Client only (I18n). */
    public String getLocalizedName() {
        return I18n.get(description);
    }

    public interface INodeSettingType {
        int getDefault();

        int clamp(int i);

        int getValue(int value);

        String getValueText(int value);
    }

    public static class NodeSettingIntList implements INodeSettingType {
        int[] values;
        String[] descriptions;

        public NodeSettingIntList(int[] values, String[] descriptions) {
            this.values = values;
            this.descriptions = descriptions;
        }

        @Override
        public int getDefault() {
            return 0;
        }

        @Override
        public int clamp(int old) {
            return Mth.clamp(old, 0, values.length - 1);
        }

        @Override
        public int getValue(int value) {
            return values[clamp(value)];
        }

        @Override
        public String getValueText(int value) {
            return descriptions[value];
        }
    }

    public static class NodeSettingIntRange implements INodeSettingType {
        int min, max;

        public NodeSettingIntRange(int min, int max) {
            this.min = min;
            this.max = max;
        }

        @Override
        public int getDefault() {
            return min;
        }

        @Override
        public int clamp(int old) {
            return Mth.clamp(old, min, max);
        }

        @Override
        public int getValue(int value) {
            return clamp(value);
        }

        @Override
        public String getValueText(int value) {
            return "" + getValue(value);
        }
    }
}
