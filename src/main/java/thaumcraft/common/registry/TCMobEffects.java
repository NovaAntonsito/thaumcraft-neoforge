package thaumcraft.common.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import thaumcraft.Thaumcraft;
import thaumcraft.common.lib.potions.PotionBlurredVision;
import thaumcraft.common.lib.potions.PotionDeathGaze;
import thaumcraft.common.lib.potions.PotionFluxTaint;
import thaumcraft.common.lib.potions.PotionInfectiousVisExhaust;
import thaumcraft.common.lib.potions.PotionSunScorned;
import thaumcraft.common.lib.potions.PotionThaumarhia;
import thaumcraft.common.lib.potions.PotionUnnaturalHunger;
import thaumcraft.common.lib.potions.PotionVisExhaust;
import thaumcraft.common.lib.potions.PotionWarpWard;

/**
 * Mob effects (Registrar.registerPotions). Registry names are lowercase in 1.21: warpWard is warp_ward, fluxTaint is
 * flux_taint, visExhaust is vis_exhaust, infectiousVisExhaust is infectious_vis_exhaust, unnaturalHunger is
 * unnatural_hunger, deathGaze is death_gaze, blurredVision is blurred_vision and sunScorned is sun_scorned.
 */
public final class TCMobEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, Thaumcraft.MODID);

    public static final DeferredHolder<MobEffect, PotionFluxTaint> FLUX_TAINT = MOB_EFFECTS.register("flux_taint", PotionFluxTaint::new);
    public static final DeferredHolder<MobEffect, PotionVisExhaust> VIS_EXHAUST = MOB_EFFECTS.register("vis_exhaust", PotionVisExhaust::new);
    public static final DeferredHolder<MobEffect, PotionInfectiousVisExhaust> INFECTIOUS_VIS_EXHAUST =
            MOB_EFFECTS.register("infectious_vis_exhaust", PotionInfectiousVisExhaust::new);
    public static final DeferredHolder<MobEffect, PotionWarpWard> WARP_WARD = MOB_EFFECTS.register("warp_ward", PotionWarpWard::new);
    // Stage 9d-5: the effects of warp.
    public static final DeferredHolder<MobEffect, PotionUnnaturalHunger> UNNATURAL_HUNGER = MOB_EFFECTS.register("unnatural_hunger", PotionUnnaturalHunger::new);
    public static final DeferredHolder<MobEffect, PotionDeathGaze> DEATH_GAZE = MOB_EFFECTS.register("death_gaze", PotionDeathGaze::new);
    public static final DeferredHolder<MobEffect, PotionBlurredVision> BLURRED_VISION = MOB_EFFECTS.register("blurred_vision", PotionBlurredVision::new);
    public static final DeferredHolder<MobEffect, PotionSunScorned> SUN_SCORNED = MOB_EFFECTS.register("sun_scorned", PotionSunScorned::new);
    public static final DeferredHolder<MobEffect, PotionThaumarhia> THAUMARHIA = MOB_EFFECTS.register("thaumarhia", PotionThaumarhia::new);

    private TCMobEffects() {
    }
}
