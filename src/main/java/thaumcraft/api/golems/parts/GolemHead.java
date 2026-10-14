package thaumcraft.api.golems.parts;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.api.golems.EnumGolemTrait;

public class GolemHead {
    protected static GolemHead[] heads;
    public byte id;
    public String key;
    public String[] research;
    public ResourceLocation icon;
    /** ItemStacks, or "base"/"mech" for the material's base and mechanism components. */
    public Object[] components;
    public EnumGolemTrait[] traits;
    public IHeadFunction function;
    public PartModel model;
    private static byte lastID;

    public GolemHead(String key, String[] research, ResourceLocation icon, PartModel model, Object[] comp, EnumGolemTrait[] tags) {
        this.key = key;
        this.research = research;
        this.icon = icon;
        components = comp;
        traits = tags;
        this.model = model;
        function = null;
    }

    public GolemHead(String key, String[] research, ResourceLocation icon, PartModel model, Object[] comp, IHeadFunction function, EnumGolemTrait[] tags) {
        this(key, research, icon, model, comp, tags);
        this.function = function;
    }

    public static void register(GolemHead thing) {
        thing.id = lastID;
        ++lastID;
        if (thing.id >= heads.length) {
            GolemHead[] temp = new GolemHead[thing.id + 1];
            System.arraycopy(heads, 0, temp, 0, heads.length);
            heads = temp;
        }
        heads[thing.id] = thing;
    }

    public Component getLocalizedName() {
        return Component.translatable("golem.head." + key.toLowerCase());
    }

    public Component getLocalizedDescription() {
        return Component.translatable("golem.head.text." + key.toLowerCase());
    }

    public static GolemHead[] getHeads() {
        return heads;
    }

    static {
        heads = new GolemHead[1];
        lastID = 0;
    }

    public interface IHeadFunction extends IGenericFunction {
    }
}
