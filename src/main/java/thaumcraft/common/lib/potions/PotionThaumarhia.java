package thaumcraft.common.lib.potions;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import thaumcraft.common.config.ConfigBlocks;

/**
 * Port of PotionThaumarhia ("Thaumorrhea"): every second, 1 in 15, the target leaves a puddle of flux goo where it
 * stands. Its icon is cell (7, 2) of textures/misc/potions.png.
 */
public class PotionThaumarhia extends MobEffect {
    public PotionThaumarhia() {
        super(MobEffectCategory.HARMFUL, 6702199);
    }

    @Override
    public boolean applyEffectTick(LivingEntity target, int amplifier) {
        BlockPos pos = target.blockPosition();
        if (!target.level().isClientSide && target.level().random.nextInt(15) == 0 && target.level().isEmptyBlock(pos)) {
            target.level().setBlockAndUpdate(pos, ConfigBlocks.fluxGoo.get().defaultBlockState());
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % 20 == 0;
    }
}
