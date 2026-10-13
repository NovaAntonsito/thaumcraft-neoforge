package thaumcraft.api.items;

import javax.annotation.Nullable;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/** Port of IRechargable: items that hold vis charge and can be refilled on a recharge pedestal. */
public interface IRechargable {
    int getMaxCharge(ItemStack stack, @Nullable LivingEntity player);

    EnumChargeDisplay showInHud(ItemStack stack, @Nullable LivingEntity player);

    enum EnumChargeDisplay {
        NEVER, NORMAL, PERIODIC
    }
}
