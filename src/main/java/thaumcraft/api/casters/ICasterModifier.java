package thaumcraft.api.casters;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * Something besides gear that changes how a player casts, such as an addon's temporary buffs. Register it with
 * {@link CasterModifiers#register}.
 */
public interface ICasterModifier {

    /** Percentage points off the vis cost of casting and arcane crafting, like {@link thaumcraft.api.items.IVisDiscountGear}. */
    default int getVisDiscount(Player player) {
        return 0;
    }

    /** A multiplier on the power of every focus this entity casts. */
    default float getPowerMultiplier(LivingEntity caster) {
        return 1.0f;
    }

    /** A multiplier on the cooldown after casting, in ticks. */
    default float getCooldownMultiplier(LivingEntity caster) {
        return 1.0f;
    }
}
