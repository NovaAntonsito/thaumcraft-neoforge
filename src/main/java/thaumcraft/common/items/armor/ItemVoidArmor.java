package thaumcraft.common.items.armor;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import thaumcraft.Thaumcraft;
import thaumcraft.api.items.IWarpingGear;

/**
 * Void metal armor: mends itself and gives 1 warp. Repaired with void metal by its material.
 * 1.21: inventoryTick runs for the armor slots too, so it covers onArmorTick and onUpdate of 1.12. As in the original,
 * worn pieces mend twice as fast (both ran for them).
 */
public class ItemVoidArmor extends ArmorItem implements IWarpingGear {
    private static final ResourceLocation TEXTURE_1 = Thaumcraft.id("textures/entity/armor/void_1.png");
    private static final ResourceLocation TEXTURE_2 = Thaumcraft.id("textures/entity/armor/void_2.png");

    public ItemVoidArmor(Holder<ArmorMaterial> material, Type type, Properties properties) {
        super(material, type, properties);
    }

    @Override
    public ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, ArmorMaterial.Layer layer, boolean innerModel) {
        return getType() == Type.LEGGINGS ? TEXTURE_2 : TEXTURE_1;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (!level.isClientSide && stack.isDamaged() && entity.tickCount % 20 == 0 && entity instanceof LivingEntity living) {
            stack.setDamageValue(stack.getDamageValue() - 1);
            // onArmorTick mended worn pieces once more.
            if (stack.isDamaged() && living.getItemBySlot(getEquipmentSlot()) == stack) {
                stack.setDamageValue(stack.getDamageValue() - 1);
            }
        }
    }

    @Override
    public int getWarp(ItemStack itemstack, Player player) {
        return 1;
    }
}
