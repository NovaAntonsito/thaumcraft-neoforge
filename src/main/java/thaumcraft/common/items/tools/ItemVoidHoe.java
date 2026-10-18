package thaumcraft.common.items.tools;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import thaumcraft.api.ThaumcraftMaterials;
import thaumcraft.api.items.IWarpingGear;

/** The void hoe: mends itself, weakens what it hits and gives 1 warp. Repaired with void metal by its material.
 * As a 1.12 hoe it adds no damage and its attack speed is the material damage minus 3. */
public class ItemVoidHoe extends HoeItem implements IWarpingGear {
    public ItemVoidHoe(Properties properties) {
        super(ThaumcraftMaterials.TOOLMAT_VOID, properties.attributes(HoeItem.createAttributes(ThaumcraftMaterials.TOOLMAT_VOID, -3.0f, 0.0f)));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (stack.isDamaged() && entity != null && entity.tickCount % 20 == 0 && entity instanceof LivingEntity) {
            stack.setDamageValue(stack.getDamageValue() - 1);
        }
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (!player.level().isClientSide && entity instanceof LivingEntity living) {
            if (!(entity instanceof Player) || player.getServer() == null || player.getServer().isPvpAllowed()) {
                living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80));
            }
        }
        return super.onLeftClickEntity(stack, player, entity);
    }

    @Override
    public int getWarp(ItemStack itemstack, Player player) {
        return 1;
    }
}
