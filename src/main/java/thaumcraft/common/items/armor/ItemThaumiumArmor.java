package thaumcraft.common.items.armor;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import thaumcraft.Thaumcraft;

/** Thaumium armor. Repaired with thaumium by its material. */
public class ItemThaumiumArmor extends ArmorItem {
    private static final ResourceLocation TEXTURE_1 = Thaumcraft.id("textures/entity/armor/thaumium_1.png");
    private static final ResourceLocation TEXTURE_2 = Thaumcraft.id("textures/entity/armor/thaumium_2.png");

    public ItemThaumiumArmor(Holder<ArmorMaterial> material, Type type, Properties properties) {
        super(material, type, properties);
    }

    @Override
    public ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, ArmorMaterial.Layer layer, boolean innerModel) {
        return getType() == Type.LEGGINGS ? TEXTURE_2 : TEXTURE_1;
    }
}
