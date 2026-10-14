package thaumcraft.api.golems.parts;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.api.golems.EnumGolemTrait;

public class GolemLeg {
    protected static GolemLeg[] legs;
    public byte id;
    public String key;
    public String[] research;
    public ResourceLocation icon;
    /** ItemStacks, or "base"/"mech" for the material's base and mechanism components. */
    public Object[] components;
    public EnumGolemTrait[] traits;
    public ILegFunction function;
    public PartModel model;
    private static byte lastID;

    public GolemLeg(String key, String[] research, ResourceLocation icon, PartModel model, Object[] comp, EnumGolemTrait[] tags) {
        this.key = key;
        this.research = research;
        this.icon = icon;
        components = comp;
        traits = tags;
        this.model = model;
        function = null;
    }

    public GolemLeg(String key, String[] research, ResourceLocation icon, PartModel model, Object[] comp, ILegFunction function, EnumGolemTrait[] tags) {
        this(key, research, icon, model, comp, tags);
        this.function = function;
    }

    public static void register(GolemLeg thing) {
        thing.id = lastID;
        ++lastID;
        if (thing.id >= legs.length) {
            GolemLeg[] temp = new GolemLeg[thing.id + 1];
            System.arraycopy(legs, 0, temp, 0, legs.length);
            legs = temp;
        }
        legs[thing.id] = thing;
    }

    public Component getLocalizedName() {
        return Component.translatable("golem.leg." + key.toLowerCase());
    }

    public Component getLocalizedDescription() {
        return Component.translatable("golem.leg.text." + key.toLowerCase());
    }

    public static GolemLeg[] getLegs() {
        return legs;
    }

    static {
        legs = new GolemLeg[1];
        lastID = 0;
    }

    public interface ILegFunction extends IGenericFunction {
    }
}
