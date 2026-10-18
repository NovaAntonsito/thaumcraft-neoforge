package thaumcraft.common.items.armor;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import thaumcraft.Thaumcraft;
import thaumcraft.api.items.IGoggles;
import thaumcraft.api.items.IRevealer;
import thaumcraft.common.registry.TCDataComponents;

/**
 * Thaumium fortress armor. Repaired with thaumium by its material. The helm can take goggles of revealing and one
 * of three masks by infusion (the "goggles" and "mask" components). The set bonus and the extra protection against
 * magic of the original ISpecialArmor are in EntityEvents.
 */
@SuppressWarnings("deprecation")
public class ItemFortressArmor extends ArmorItem implements IGoggles, IRevealer {
    private static final ResourceLocation TEXTURE = Thaumcraft.id("textures/entity/armor/fortress_armor.png");

    public ItemFortressArmor(Holder<ArmorMaterial> material, Type type, Properties properties) {
        super(material, type, properties);
    }

    @Override
    public ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, ArmorMaterial.Layer layer, boolean innerModel) {
        return TEXTURE;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (hasGoggles(stack)) {
            tooltip.add(Component.translatable("item.goggles.name").withStyle(ChatFormatting.DARK_PURPLE));
        }
        Integer mask = stack.get(TCDataComponents.FORTRESS_MASK.get());
        if (mask != null) {
            tooltip.add(Component.translatable("item.fortress_helm.mask." + mask).withStyle(ChatFormatting.GOLD));
        }
        super.appendHoverText(stack, context, tooltip, flag);
    }

    public static boolean hasGoggles(ItemStack stack) {
        return stack.getOrDefault(TCDataComponents.FORTRESS_GOGGLES.get(), false);
    }

    /** The mask on a fortress helm, or -1. */
    public static int getMask(ItemStack stack) {
        if (!(stack.getItem() instanceof ItemFortressArmor)) {
            return -1;
        }
        return stack.getOrDefault(TCDataComponents.FORTRESS_MASK.get(), -1);
    }

    @Override
    public boolean showNodes(ItemStack itemstack, LivingEntity player) {
        return hasGoggles(itemstack);
    }

    @Override
    public boolean showIngamePopups(ItemStack itemstack, LivingEntity player) {
        return hasGoggles(itemstack);
    }
}
