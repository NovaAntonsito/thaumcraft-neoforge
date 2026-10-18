package thaumcraft.common.lib.potions;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Port of PotionDeathGaze ("Deadly Gaze"). Does nothing by itself: every second, whatever the player looks at withers
 * and turns on them (WarpEvents.checkDeathGaze). It darkens the edges of the screen and drains its colors.
 * Its icon is cell (4, 2) of textures/misc/potions.png.
 */
public class PotionDeathGaze extends MobEffect {
    public PotionDeathGaze() {
        super(MobEffectCategory.HARMFUL, 6702131);
    }
}
