package thaumcraft.common.registry;

import java.util.EnumMap;
import java.util.List;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.registries.DeferredRegister;
import thaumcraft.Thaumcraft;

/**
 * The armor materials of ThaumcraftMaterials. The 1.12 durability factor goes in the item
 * properties now (ArmorItem.Type.getDurability), see ConfigItems.
 */
public final class TCArmorMaterials {
    public static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS = DeferredRegister.create(Registries.ARMOR_MATERIAL, Thaumcraft.MODID);

    /** ARMORMAT_CULTIST_PLATE: factor 18, {2, 5, 6, 2}, enchantability 13, iron sound. */
    public static final Holder<ArmorMaterial> CULTIST_PLATE = register("cultist_plate", 2, 5, 6, 2, 13,
            SoundEvents.ARMOR_EQUIP_IRON);
    /** ARMORMAT_CULTIST_ROBE: factor 17, {2, 4, 5, 2}, enchantability 13, chain sound. */
    public static final Holder<ArmorMaterial> CULTIST_ROBE = register("cultist_robe", 2, 4, 5, 2, 13,
            SoundEvents.ARMOR_EQUIP_CHAIN);

    /** ARMORMAT_CULTIST_LEADER: factor 30, {3, 6, 7, 3}, enchantability 20, iron sound, toughness 1. */
    public static final Holder<ArmorMaterial> CULTIST_LEADER = register("cultist_leader", 3, 6, 7, 3, 20,
            SoundEvents.ARMOR_EQUIP_IRON, 1.0f);

    public static final int CULTIST_PLATE_DURABILITY = 18;
    public static final int CULTIST_ROBE_DURABILITY = 17;
    public static final int CULTIST_LEADER_DURABILITY = 30;

    private TCArmorMaterials() {
    }

    private static Holder<ArmorMaterial> register(String name, int boots, int legs, int chest, int helmet, int enchantability,
            Holder<net.minecraft.sounds.SoundEvent> sound) {
        return register(name, boots, legs, chest, helmet, enchantability, sound, 0.0f);
    }

    private static Holder<ArmorMaterial> register(String name, int boots, int legs, int chest, int helmet, int enchantability,
            Holder<net.minecraft.sounds.SoundEvent> sound, float toughness) {
        EnumMap<ArmorItem.Type, Integer> defense = new EnumMap<>(ArmorItem.Type.class);
        defense.put(ArmorItem.Type.BOOTS, boots);
        defense.put(ArmorItem.Type.LEGGINGS, legs);
        defense.put(ArmorItem.Type.CHESTPLATE, chest);
        defense.put(ArmorItem.Type.HELMET, helmet);
        defense.put(ArmorItem.Type.BODY, chest);
        // The texture comes from getArmorTexture, the layer is only a fallback.
        List<ArmorMaterial.Layer> layers = List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(Thaumcraft.MODID, name)));
        return ARMOR_MATERIALS.register(name, () -> new ArmorMaterial(defense, enchantability, sound,
                () -> Ingredient.of(Items.IRON_INGOT), layers, toughness, 0.0f));
    }
}
