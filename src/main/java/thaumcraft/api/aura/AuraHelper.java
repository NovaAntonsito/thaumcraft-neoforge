package thaumcraft.api.aura;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import thaumcraft.common.world.aura.AuraHandler;

/**
 * Port of thaumcraft.api.aura.AuraHelper. The original went through ThaumcraftApi.internalMethods;
 * here it calls AuraHandler directly.
 */
public class AuraHelper {
    /** Removes vis from the aura at the given location. Returns how much was actually drained. */
    public static float drainVis(Level world, BlockPos pos, float amount, boolean simulate) {
        return AuraHandler.drainVis(world, pos, amount, simulate);
    }

    /** Removes flux from the aura at the given location. Returns how much was actually drained. */
    public static float drainFlux(Level world, BlockPos pos, float amount, boolean simulate) {
        return AuraHandler.drainFlux(world, pos, amount, simulate);
    }

    /** Adds vis to the aura at the given location. */
    public static void addVis(Level world, BlockPos pos, float amount) {
        AuraHandler.addVis(world, pos, amount);
    }

    public static float getVis(Level world, BlockPos pos) {
        return AuraHandler.getVis(world, pos);
    }

    /** Adds flux to the aura at the given location; with showEffect, pink wisps show where it went. */
    public static void polluteAura(Level world, BlockPos pos, float amount, boolean showEffect) {
        if (world.isClientSide) return;
        AuraHandler.addFlux(world, pos, amount);
        if (showEffect && amount > 0.0f) {
            thaumcraft.common.network.TCNetwork.sendToAllAround(world, pos.getX(), pos.getY(), pos.getZ(), 32.0,
                    new thaumcraft.common.lib.network.fx.PacketFXPollute(pos, amount));
        }
    }

    public static float getFlux(Level world, BlockPos pos) {
        return AuraHandler.getFlux(world, pos);
    }

    /** The base (natural maximum) aura level of the chunk at the given location. */
    public static int getAuraBase(Level world, BlockPos pos) {
        return AuraHandler.getAuraBase(world, pos);
    }

    /** True if the aura is low enough that a player with the AURAPRESERVE research should stop draining it. */
    public static boolean shouldPreserveAura(Level world, @Nullable Player player, BlockPos pos) {
        return AuraHandler.shouldPreserveAura(world, player, pos);
    }
}
