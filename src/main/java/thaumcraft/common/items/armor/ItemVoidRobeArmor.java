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
import thaumcraft.api.items.IGoggles;
import thaumcraft.api.items.IRevealer;
import thaumcraft.api.items.IVisDiscountGear;
import thaumcraft.api.items.IWarpingGear;

/**
 * The void thaumaturge armor: dyeable void metal robes that mend themselves, lower vis costs by 5% and give 3 warp
 * each. The hood works as goggles of revealing. The extra protection against magic of the original ISpecialArmor is
 * in EntityEvents. 1.21: the color is the vanilla dyed color component, as for the thaumaturge's robes.
 */
@SuppressWarnings("deprecation")
public class ItemVoidRobeArmor extends ArmorItem implements IVisDiscountGear, IGoggles, IRevealer, IWarpingGear {
    private static final ResourceLocation TEXTURE = Thaumcraft.id("textures/entity/armor/void_robe_armor.png");
    private static final ResourceLocation TEXTURE_OVERLAY = Thaumcraft.id("textures/entity/armor/void_robe_armor_overlay.png");

    public ItemVoidRobeArmor(Holder<ArmorMaterial> material, Type type, Properties properties) {
        super(material, type, properties);
    }

    /** As in the original, the dyed layer is the "overlay" texture and the undyed one the plain texture. */
    @Override
    public ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, ArmorMaterial.Layer layer, boolean innerModel) {
        return layer.dyeable() ? TEXTURE_OVERLAY : TEXTURE;
    }

    /** onUpdate and onArmorTick: 1 durability back per second, 2 while worn. */
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
    public boolean showNodes(ItemStack itemstack, LivingEntity player) {
        return getType() == Type.HELMET;
    }

    @Override
    public boolean showIngamePopups(ItemStack itemstack, LivingEntity player) {
        return getType() == Type.HELMET;
    }

    @Override
    public int getVisDiscount(ItemStack stack, Player player) {
        return 5;
    }

    @Override
    public int getWarp(ItemStack itemstack, Player player) {
        return 3;
    }
}
