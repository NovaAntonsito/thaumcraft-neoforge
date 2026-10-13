package thaumcraft.common.lib.potions;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Port of PotionWarpWard: a beneficial effect that does nothing by itself. While it lasts, warp events are held
 * back (PlayerEvents, a later stage). Its icon is cell (3, 2) of textures/misc/potions.png.
 */
public class PotionWarpWard extends MobEffect {
    public PotionWarpWard() {
        super(MobEffectCategory.BENEFICIAL, 0xE0F2F7);
    }
}
