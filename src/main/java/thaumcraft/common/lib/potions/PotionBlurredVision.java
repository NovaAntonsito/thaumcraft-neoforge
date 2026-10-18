package thaumcraft.common.lib.potions;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** Port of PotionBlurredVision: the screen is blurred while it lasts. Its icon is cell (5, 2) of textures/misc/potions.png. */
public class PotionBlurredVision extends MobEffect {
    public PotionBlurredVision() {
        super(MobEffectCategory.HARMFUL, 8421504);
    }
}
