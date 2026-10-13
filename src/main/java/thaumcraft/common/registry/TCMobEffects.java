package thaumcraft.common.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import thaumcraft.Thaumcraft;
import thaumcraft.common.lib.potions.PotionWarpWard;

/** Mob effects (Registrar.registerPotions). Registry names are lowercase in 1.21: warpWard is warp_ward. */
public final class TCMobEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, Thaumcraft.MODID);

    public static final DeferredHolder<MobEffect, PotionWarpWard> WARP_WARD = MOB_EFFECTS.register("warp_ward", PotionWarpWard::new);

    private TCMobEffects() {
    }
}
