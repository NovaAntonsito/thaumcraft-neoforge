package thaumcraft.common.items.tools;

import java.util.function.Consumer;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import thaumcraft.api.ThaumcraftMaterials;
import thaumcraft.common.items.ISubItems;
import thaumcraft.common.lib.enchantment.EnumInfusionEnchantment;

/** The pickaxe of the core: sets on fire what it hits. Made with Refining and Sounding II. */
public class ItemElementalPickaxe extends PickaxeItem implements ISubItems {
    public ItemElementalPickaxe(Properties properties) {
        super(ThaumcraftMaterials.TOOLMAT_ELEMENTAL, properties.attributes(PickaxeItem.createAttributes(ThaumcraftMaterials.TOOLMAT_ELEMENTAL, 1.0f, -2.8f)));
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (!player.level().isClientSide) {
            if (!(entity instanceof Player) || player.getServer() == null || player.getServer().isPvpAllowed()) {
                entity.igniteForSeconds(2);
            }
        }
        return super.onLeftClickEntity(stack, player, entity);
    }

    @Override
    public void fillSubItems(Consumer<ItemStack> output) {
        ItemStack w1 = new ItemStack(this);
        EnumInfusionEnchantment.addInfusionEnchantment(w1, EnumInfusionEnchantment.REFINING, 1);
        EnumInfusionEnchantment.addInfusionEnchantment(w1, EnumInfusionEnchantment.SOUNDING, 2);
        output.accept(w1);
    }
}
