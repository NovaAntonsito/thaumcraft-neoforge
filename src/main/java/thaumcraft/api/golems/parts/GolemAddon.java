package thaumcraft.api.golems.parts;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.api.golems.EnumGolemTrait;

public class GolemAddon {
    protected static GolemAddon[] addons;
    public byte id;
    public String key;
    public String[] research;
    public ResourceLocation icon;
    /** ItemStacks, or "base"/"mech" for the material's base and mechanism components. */
    public Object[] components;
    public EnumGolemTrait[] traits;
    public IAddonFunction function;
    public PartModel model;
    private static byte lastID;

    public GolemAddon(String key, String[] research, ResourceLocation icon, PartModel model, Object[] comp, EnumGolemTrait[] tags) {
        this.key = key;
        this.research = research;
        this.icon = icon;
        components = comp;
        traits = tags;
        this.model = model;
        function = null;
    }

    public GolemAddon(String key, String[] research, ResourceLocation icon, PartModel model, Object[] comp, IAddonFunction function, EnumGolemTrait[] tags) {
        this(key, research, icon, model, comp, tags);
        this.function = function;
    }

    public static void register(GolemAddon thing) {
        thing.id = lastID;
        ++lastID;
        if (thing.id >= addons.length) {
            GolemAddon[] temp = new GolemAddon[thing.id + 1];
            System.arraycopy(addons, 0, temp, 0, addons.length);
            addons = temp;
        }
        addons[thing.id] = thing;
    }

    public Component getLocalizedName() {
        return Component.translatable("golem.addon." + key.toLowerCase());
    }

    public Component getLocalizedDescription() {
        return Component.translatable("golem.addon.text." + key.toLowerCase());
    }

    public static GolemAddon[] getAddons() {
        return addons;
    }

    static {
        addons = new GolemAddon[1];
        lastID = 0;
    }

    public interface IAddonFunction extends IGenericFunction {
    }
}
