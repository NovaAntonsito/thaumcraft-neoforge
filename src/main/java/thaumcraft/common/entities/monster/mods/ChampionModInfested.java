package thaumcraft.common.entities.monster.mods;

import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import thaumcraft.client.fx.ClientFX;
import thaumcraft.common.entities.monster.tainted.EntityTaintCrawler;
import thaumcraft.common.lib.SoundsTC;

public class ChampionModInfested implements IChampionModifierEffect {
    @Override
    public float performEffect(LivingEntity mob, LivingEntity target, DamageSource source, float amount) {
        if (mob.getRandom().nextFloat() < 0.4f && !mob.level().isClientSide) {
            EntityTaintCrawler spiderling = new EntityTaintCrawler(mob.level());
            spiderling.moveTo(mob.getX(), mob.getY() + mob.getBbHeight() / 2.0f, mob.getZ(), mob.getRandom().nextFloat() * 360.0f, 0.0f);
            mob.level().addFreshEntity(spiderling);
            mob.playSound(SoundsTC.GORE.get(), 0.5f, 1.0f);
        }
        return amount;
    }

    @Override
    public void showFX(LivingEntity mob) {
        RandomSource rand = mob.getRandom();
        if (rand.nextBoolean()) {
            ClientFX.slimeJump(mob);
        }
    }
}
