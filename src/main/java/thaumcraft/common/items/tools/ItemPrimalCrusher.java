package thaumcraft.common.items.tools;

import java.util.function.Consumer;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.common.SimpleTier;
import thaumcraft.Thaumcraft;
import thaumcraft.api.items.IWarpingGear;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.items.ISubItems;
import thaumcraft.common.lib.enchantment.EnumInfusionEnchantment;

/**
 * The primal crusher: a pickaxe and shovel in one, that mends itself and gives 2 warp. Made with Destructive and
 * Refining.
 * 1.21: what it mines fast is the thaumcraft:mineable/primal_crusher tag (everything a pickaxe or a shovel mines);
 * 1.12 had its own list of blocks plus every rock and metal block.
 */
public class ItemPrimalCrusher extends DiggerItem implements IWarpingGear, ISubItems {
    /** PRIMALVOID: level 5, 500 uses, speed 8, damage 4, enchantability 20, repaired with void metal. */
    public static final Tier MATERIAL = new SimpleTier(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 500, 8.0f, 4.0f, 20,
            () -> Ingredient.of(ConfigItems.ingots.get("void").get()));
    public static final TagKey<Block> MINEABLE = TagKey.create(Registries.BLOCK, Thaumcraft.id("mineable/primal_crusher"));

    public ItemPrimalCrusher(Properties properties) {
        super(MATERIAL, MINEABLE, properties.attributes(DiggerItem.createAttributes(MATERIAL, 3.5f, -2.8f)));
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ItemAbility itemAbility) {
        return ItemAbilities.DEFAULT_PICKAXE_ACTIONS.contains(itemAbility) || ItemAbilities.DEFAULT_SHOVEL_ACTIONS.contains(itemAbility);
    }

    @Override
    public int getWarp(ItemStack itemstack, Player player) {
        return 2;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (stack.isDamaged() && entity != null && entity.tickCount % 20 == 0 && entity instanceof LivingEntity) {
            stack.setDamageValue(stack.getDamageValue() - 1);
        }
    }

    @Override
    public void fillSubItems(Consumer<ItemStack> output) {
        ItemStack w1 = new ItemStack(this);
        EnumInfusionEnchantment.addInfusionEnchantment(w1, EnumInfusionEnchantment.DESTRUCTIVE, 1);
        EnumInfusionEnchantment.addInfusionEnchantment(w1, EnumInfusionEnchantment.REFINING, 1);
        output.accept(w1);
    }
}
