package voidexpeditions.perk;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import thaumcraft.api.aura.AuraHelper;
import thaumcraft.api.capabilities.PlayerWarp;
import thaumcraft.api.capabilities.ThaumcraftCapabilities;
import thaumcraft.api.events.WarpEventCheck;
import thaumcraft.common.entities.EntityPermanentItem;
import thaumcraft.common.registry.TCAttachments;
import voidexpeditions.VoidExpeditions;
import voidexpeditions.registry.VEAttachments;
import voidexpeditions.registry.VEItems;
import voidexpeditions.run.ExpeditionDim;

/** The perks that do something rather than add numbers, while their run lasts. */
@EventBusSubscriber(modid = VoidExpeditions.MODID)
public final class PerkEffects {
    private static final String[] ARROWS = {"↑", "↗", "→", "↘", "↓", "↙", "←", "↖"};

    private PerkEffects() {
    }

    @SubscribeEvent
    static void onTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer p) || !p.getData(VEAttachments.RUN).active) return;
        if (p.tickCount % 20 == 0 && Perks.has(p, Perks.VOID_COMPASS) && ExpeditionDim.is(p.level())) compass(p);
        if (p.tickCount % 40 != 0) return;
        ServerLevel level = p.serverLevel();
        BlockPos pos = p.blockPosition();
        // Vis siphon: the aura around refills toward its base. Flux ward: it is cleansed.
        if (Perks.has(p, Perks.VIS_SIPHON) && AuraHelper.getVis(level, pos) < AuraHelper.getAuraBase(level, pos)) {
            AuraHelper.addVis(level, pos, 2.0f);
        }
        if (Perks.has(p, Perks.FLUX_WARD)) AuraHelper.drainFlux(level, pos, 1.0f, false);
        if (Perks.has(p, Perks.NIGHT_EYE)) p.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 300, 0, true, false));
        if (Perks.has(p, Perks.SIGIL_SENSE) && ExpeditionDim.is(level)) {
            for (EntityPermanentItem e : level.getEntitiesOfClass(EntityPermanentItem.class, p.getBoundingBox().inflate(96.0),
                    e -> e.getItem().is(VEItems.VOID_SIGIL.get()))) {
                e.setGlowingTag(true);
            }
        }
    }

    /** Warp ward: no warp events during the run. */
    @SubscribeEvent
    static void onWarpCheck(WarpEventCheck event) {
        if (Perks.has(event.getEntity(), Perks.WARP_WARD)) event.setCanceled(true);
    }

    /** Clear mind: each portal crossed washes away the temporary warp. */
    public static void onPortalCrossed(ServerPlayer p) {
        if (!Perks.has(p, Perks.CLEAR_MIND)) return;
        PlayerWarp warp = ThaumcraftCapabilities.getWarp(p);
        if (warp.get(PlayerWarp.EnumWarpType.TEMPORARY) > 0) {
            warp.set(PlayerWarp.EnumWarpType.TEMPORARY, 0);
            p.syncData(TCAttachments.WARP);
        }
    }

    /** Void compass: the way and distance to the labyrinth's portal, on the action bar. */
    private static void compass(ServerPlayer p) {
        CompoundTag home = p.getData(VEAttachments.HOME);
        if (!home.contains("PortalX")) return;
        double dx = home.getInt("PortalX") + 0.5 - p.getX();
        double dz = home.getInt("PortalZ") + 0.5 - p.getZ();
        float bearing = (float) Math.toDegrees(Math.atan2(-dx, dz));
        int sector = Math.floorMod(Math.round(Mth.wrapDegrees(bearing - p.getYRot()) / 45.0f), 8);
        int dist = (int) Math.sqrt(dx * dx + dz * dz);
        p.displayClientMessage(Component.translatable("voidexpeditions.perk.void_compass.bar", ARROWS[sector], dist)
                .withStyle(ChatFormatting.DARK_PURPLE), true);
    }
}
