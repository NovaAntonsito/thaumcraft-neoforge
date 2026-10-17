package thaumcraft.common.lib.potions;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Port of PotionVisExhaust ("Flux Flu"). Does nothing by itself: while it lasts, casting foci costs 10% more vis
 * per level (CasterManager). Its icon is cell (5, 1) of textures/misc/potions.png.
 */
public class PotionVisExhaust extends MobEffect {
    public PotionVisExhaust() {
        super(MobEffectCategory.HARMFUL, 6702199);
    }
}
