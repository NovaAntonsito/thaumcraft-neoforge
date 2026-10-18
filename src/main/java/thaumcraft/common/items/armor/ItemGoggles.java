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
import thaumcraft.Thaumcraft;
import thaumcraft.api.items.IGoggles;
import thaumcraft.api.items.IRevealer;
import thaumcraft.api.items.IVisDiscountGear;
import thaumcraft.common.config.ConfigItems;

/**
 * The goggles of revealing: show essentia contents and the like, and lower vis costs by 5%.
 * Repaired with brass. Wearing them as a bauble (head slot) comes with Curios in stage 9e-3.
 */
@SuppressWarnings("deprecation")
public class ItemGoggles extends ArmorItem implements IVisDiscountGear, IRevealer, IGoggles {
    private static final ResourceLocation TEXTURE = Thaumcraft.id("textures/entity/armor/goggles.png");

    public ItemGoggles(Holder<ArmorMaterial> material, Properties properties) {
        super(material, Type.HELMET, properties);
    }

    @Override
    public ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, ArmorMaterial.Layer layer, boolean innerModel) {
        return TEXTURE;
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repair) {
        return repair.is(ConfigItems.ingots.get("brass").get()) || super.isValidRepairItem(stack, repair);
    }

    @Override
    public int getVisDiscount(ItemStack stack, Player player) {
        return 5;
    }

    @Override
    public boolean showNodes(ItemStack itemstack, LivingEntity player) {
        return true;
    }

    @Override
    public boolean showIngamePopups(ItemStack itemstack, LivingEntity player) {
        return true;
    }
}
