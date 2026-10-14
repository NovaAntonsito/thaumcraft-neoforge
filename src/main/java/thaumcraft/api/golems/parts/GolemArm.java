package thaumcraft.api.golems.parts;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import thaumcraft.api.golems.IGolemAPI;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.api.golems.EnumGolemTrait;

public class GolemArm {
    protected static GolemArm[] arms;
    public byte id;
    public String key;
    public String[] research;
    public ResourceLocation icon;
    /** ItemStacks, or "base"/"mech" for the material's base and mechanism components. */
    public Object[] components;
    public EnumGolemTrait[] traits;
    public IArmFunction function;
    public PartModel model;
    private static byte lastID;

    public GolemArm(String key, String[] research, ResourceLocation icon, PartModel model, Object[] comp, EnumGolemTrait[] tags) {
        this.key = key;
        this.research = research;
        this.icon = icon;
        components = comp;
        traits = tags;
        this.model = model;
        function = null;
    }

    public GolemArm(String key, String[] research, ResourceLocation icon, PartModel model, Object[] comp, IArmFunction function, EnumGolemTrait[] tags) {
        this(key, research, icon, model, comp, tags);
        this.function = function;
    }

    public static void register(GolemArm thing) {
        thing.id = lastID;
        ++lastID;
        if (thing.id >= arms.length) {
            GolemArm[] temp = new GolemArm[thing.id + 1];
            System.arraycopy(arms, 0, temp, 0, arms.length);
            arms = temp;
        }
        arms[thing.id] = thing;
    }

    public Component getLocalizedName() {
        return Component.translatable("golem.arm." + key.toLowerCase());
    }

    public Component getLocalizedDescription() {
        return Component.translatable("golem.arm.text." + key.toLowerCase());
    }

    public static GolemArm[] getArms() {
        return arms;
    }

    static {
        arms = new GolemArm[1];
        lastID = 0;
    }

    public interface IArmFunction extends IGenericFunction {
        void onMeleeAttack(IGolemAPI golem, Entity target);

        void onRangedAttack(IGolemAPI golem, LivingEntity target, float range);

        /** EntityAIAttackRanged in 1.12. */
        Goal getRangedAttackAI(RangedAttackMob golem);
    }
}
