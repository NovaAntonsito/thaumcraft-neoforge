package thaumcraft.api.items;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import thaumcraft.api.aura.AuraHelper;
import thaumcraft.common.registry.TCDataComponents;

/**
 * Port of RechargeHelper: reads and changes the vis charge of IRechargable items. The charge lives in the
 * thaumcraft:charge data component instead of the "tc.charge" NBT int.
 */
public class RechargeHelper {
    private RechargeHelper() {
    }

    /**
     * Recharges an item from the aura. In general this should only be called every 5 ticks.
     * @param player may be null
     * @return how much charge was actually added
     */
    public static float rechargeItem(Level world, ItemStack is, BlockPos pos, @Nullable Player player, int amt) {
        if (is == null || is.isEmpty() || !(is.getItem() instanceof IRechargable chargeItem)) return 0;
        if (player != null && AuraHelper.shouldPreserveAura(world, player, pos)) return 0;
        amt = Math.min(amt, chargeItem.getMaxCharge(is, player) - getCharge(is));
        int drained = (int) AuraHelper.drainVis(world, pos, amt, false);
        if (drained > 0) {
            addCharge(is, player, drained);
            return drained;
        }
        return 0;
    }

    /** Recharges an item without draining the aura. */
    public static float rechargeItemBlindly(ItemStack is, @Nullable Player player, int amt) {
        if (is == null || is.isEmpty() || !(is.getItem() instanceof IRechargable chargeItem)) return 0;
        amt = Math.min(amt, chargeItem.getMaxCharge(is, player) - getCharge(is));
        if (amt > 0) addCharge(is, player, amt);
        return amt;
    }

    private static void addCharge(ItemStack is, @Nullable LivingEntity player, int amt) {
        if (is == null || is.isEmpty() || !(is.getItem() instanceof IRechargable chargeItem)) return;
        int amount = Math.min(chargeItem.getMaxCharge(is, player), amt + getCharge(is));
        is.set(TCDataComponents.CHARGE.get(), amount);
    }

    /** @return the charge, or -1 if the item is not rechargeable */
    public static int getCharge(ItemStack is) {
        if (is == null || is.isEmpty() || !(is.getItem() instanceof IRechargable)) return -1;
        return is.getOrDefault(TCDataComponents.CHARGE.get(), 0);
    }

    /** @return the charge level as a fraction (1 is full) */
    public static float getChargePercentage(ItemStack is, @Nullable Player player) {
        if (is == null || is.isEmpty() || !(is.getItem() instanceof IRechargable chargeItem)) return -1;
        float c = getCharge(is);
        float m = chargeItem.getMaxCharge(is, player);
        return c / m;
    }

    /** Takes vis charge from the item. @return if the item had the charge removed */
    public static boolean consumeCharge(ItemStack is, @Nullable LivingEntity player, int amt) {
        if (is == null || is.isEmpty() || !(is.getItem() instanceof IRechargable)) return false;
        Integer charge = is.get(TCDataComponents.CHARGE.get());
        if (charge != null && charge >= amt) {
            is.set(TCDataComponents.CHARGE.get(), charge - amt);
            return true;
        }
        return false;
    }
}
