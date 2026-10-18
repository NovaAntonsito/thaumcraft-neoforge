package thaumcraft.common.items.armor;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import thaumcraft.Thaumcraft;

/** Crimson cult plate. The model (ModelKnightArmor) is set up in ThaumcraftClient. Repaired with iron by its material. */
public class ItemCultistPlateArmor extends ArmorItem {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Thaumcraft.MODID, "textures/entity/armor/cultist_plate_armor.png");
    private static final ResourceLocation ZOMBIE_TEXTURE = ResourceLocation.fromNamespaceAndPath(Thaumcraft.MODID, "textures/entity/armor/zombie_plate_armor.png");

    public ItemCultistPlateArmor(Holder<ArmorMaterial> material, Type type, Properties properties) {
        super(material, type, properties);
    }

    @Override
    public ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, ArmorMaterial.Layer layer, boolean innerModel) {
        return entity instanceof thaumcraft.common.entities.monster.EntityInhabitedZombie ? ZOMBIE_TEXTURE : TEXTURE;
    }
}
