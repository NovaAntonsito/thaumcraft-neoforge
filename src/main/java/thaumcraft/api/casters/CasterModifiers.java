package thaumcraft.api.casters;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/** The registered {@link ICasterModifier}s, combined. */
public final class CasterModifiers {
    private static final List<ICasterModifier> MODIFIERS = new CopyOnWriteArrayList<>();

    private CasterModifiers() {
    }

    public static void register(ICasterModifier modifier) {
        MODIFIERS.add(modifier);
    }

    public static int getVisDiscount(Player player) {
        int total = 0;
        for (ICasterModifier m : MODIFIERS) total += m.getVisDiscount(player);
        return total;
    }

    public static float getPowerMultiplier(LivingEntity caster) {
        float total = 1.0f;
        for (ICasterModifier m : MODIFIERS) total *= m.getPowerMultiplier(caster);
        return total;
    }

    public static float getCooldownMultiplier(LivingEntity caster) {
        float total = 1.0f;
        for (ICasterModifier m : MODIFIERS) total *= m.getCooldownMultiplier(caster);
        return total;
    }
}
