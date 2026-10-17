package thaumcraft.common.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import thaumcraft.Thaumcraft;
import thaumcraft.common.lib.potions.PotionFluxTaint;
import thaumcraft.common.lib.potions.PotionInfectiousVisExhaust;
import thaumcraft.common.lib.potions.PotionVisExhaust;
import thaumcraft.common.lib.potions.PotionWarpWard;

/**
 * Mob effects (Registrar.registerPotions). Registry names are lowercase in 1.21: warpWard is warp_ward, fluxTaint is
 * flux_taint, visExhaust is vis_exhaust and infectiousVisExhaust is infectious_vis_exhaust.
 */
public final class TCMobEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, Thaumcraft.MODID);

    public static final DeferredHolder<MobEffect, PotionFluxTaint> FLUX_TAINT = MOB_EFFECTS.register("flux_taint", PotionFluxTaint::new);
    public static final DeferredHolder<MobEffect, PotionVisExhaust> VIS_EXHAUST = MOB_EFFECTS.register("vis_exhaust", PotionVisExhaust::new);
    public static final DeferredHolder<MobEffect, PotionInfectiousVisExhaust> INFECTIOUS_VIS_EXHAUST =
            MOB_EFFECTS.register("infectious_vis_exhaust", PotionInfectiousVisExhaust::new);
    public static final DeferredHolder<MobEffect, PotionWarpWard> WARP_WARD = MOB_EFFECTS.register("warp_ward", PotionWarpWard::new);

    private TCMobEffects() {
    }
}
