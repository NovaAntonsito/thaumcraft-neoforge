package thaumcraft.common.lib.research;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import thaumcraft.api.research.IScanThing;
import thaumcraft.api.research.ScanningManager;

/** An item carrying the enchantment (stored enchantments of books included). Key: "!" and the enchantment id. */
public class ScanEnchantment implements IScanThing {
    final ResourceKey<Enchantment> enchantment;

    public ScanEnchantment(ResourceKey<Enchantment> ench) {
        enchantment = ench;
    }

    @Override
    public boolean checkThing(Player player, Object obj) {
        if (obj == null) return false;
        ItemStack is = ScanningManager.getItemFromParms(player, obj);
        if (is.isEmpty()) return false;
        return EnchantmentHelper.getEnchantmentsForCrafting(is).keySet().stream().anyMatch(h -> h.is(enchantment));
    }

    @Override
    public String getResearchKey(Player player, Object obj) {
        return "!" + enchantment.location();
    }
}
