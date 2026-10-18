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
    public void showFX(LivingEntity mob) {
        RandomSource rand = mob.getRandom();
        if (rand.nextBoolean()) return;
        ClientFX.championMote(mob, 1.0f, 0.0, 0.8f + rand.nextFloat() * 0.2f, 0.0f, 0.9f + rand.nextFloat() * 0.1f, 0.6f + rand.nextFloat() * 0.4f);
    }
}
