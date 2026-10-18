package thaumcraft.common.items.armor;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import thaumcraft.Thaumcraft;
import thaumcraft.api.items.IVisDiscountGear;

/**
 * The thaumaturge's robes: dyeable cloth that lowers vis costs (3%, 2% for the boots).
 * 1.21: the color is the vanilla dyed color component, so the vanilla armor dye recipe and the
 * cauldron wash them. The stacks start with the original's default purple (6961280).
 */
public class ItemRobeArmor extends ArmorItem implements IVisDiscountGear {
    public static final int DEFAULT_COLOR = 6961280;
    private static final ResourceLocation ROBES_1 = Thaumcraft.id("textures/entity/armor/robes_1.png");
    private static final ResourceLocation ROBES_1_OVERLAY = Thaumcraft.id("textures/entity/armor/robes_1_overlay.png");
    private static final ResourceLocation ROBES_2 = Thaumcraft.id("textures/entity/armor/robes_2.png");
    private static final ResourceLocation ROBES_2_OVERLAY = Thaumcraft.id("textures/entity/armor/robes_2_overlay.png");

    public ItemRobeArmor(Holder<ArmorMaterial> material, Type type, Properties properties) {
        super(material, type, properties);
    }

    /** type == null (the dyed layer) is the plain texture, "overlay" the undyed one. Legs use robes_2. */
    @Override
    public ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, ArmorMaterial.Layer layer, boolean innerModel) {
        if (getType() == Type.LEGGINGS) {
            return layer.dyeable() ? ROBES_2 : ROBES_2_OVERLAY;
        }
        return layer.dyeable() ? ROBES_1 : ROBES_1_OVERLAY;
    }

    @Override
    public int getVisDiscount(ItemStack stack, Player player) {
        return getType() == Type.BOOTS ? 2 : 3;
    }
}
