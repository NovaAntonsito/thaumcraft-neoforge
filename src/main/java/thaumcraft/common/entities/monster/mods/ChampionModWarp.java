package thaumcraft.common.entities.monster.mods;

import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import thaumcraft.client.fx.ClientFX;
import net.minecraft.world.entity.player.Player;
import thaumcraft.api.capabilities.PlayerWarp;
import thaumcraft.api.capabilities.ThaumcraftCapabilities;
import thaumcraft.common.registry.TCAttachments;

public class ChampionModWarp implements IChampionModifierEffect {
    @Override
    public float performEffect(LivingEntity mob, LivingEntity target, DamageSource source, float amount) {
        if (mob.getRandom().nextFloat() < 0.33f && target instanceof Player player) {
            ThaumcraftCapabilities.getWarp(player).add(PlayerWarp.EnumWarpType.TEMPORARY, 1 + mob.getRandom().nextInt(3));
            player.syncData(TCAttachments.WARP);
        }
        return amount;
    }

    @Override
    public void showFX(LivingEntity boss) {
        if (boss.level().random.nextBoolean()) {
            return;
        }
        float w = boss.level().random.nextFloat() * boss.getBbWidth();
        float d = boss.level().random.nextFloat() * boss.getBbWidth();
        float h = boss.level().random.nextFloat() * boss.getBbHeight();
        thaumcraft.client.fx.FXDispatcher.drawGenericParticles(boss.getBoundingBox().minX + w, boss.getBoundingBox().minY + h, boss.getBoundingBox().minZ + d, 0.0, 0.0, 0.0, 0.8f + boss.level().random.nextFloat() * 0.2f, 0.0f, 0.9f + boss.level().random.nextFloat() * 0.1f, 0.7f, true, 264, 8, 1, 10 + boss.level().random.nextInt(4), 0, 0.6f + boss.level().random.nextFloat() * 0.4f, 0.0f, 0);
    }
}
