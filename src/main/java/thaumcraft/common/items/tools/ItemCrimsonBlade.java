package thaumcraft.common.items.tools;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.SimpleTier;
import thaumcraft.api.items.IWarpingGear;

/** The crimson blade: mends itself, saps whoever it hits (weakness and hunger) and gives 2 warp. */
public class ItemCrimsonBlade extends SwordItem implements IWarpingGear {
    /** toolMatCrimsonVoid: level 4, 200 uses, speed 8, damage 3.5, enchantability 20, repaired with void metal. */
    public static final Tier CRIMSON_VOID = new SimpleTier(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 200, 8.0f, 3.5f, 20,
            () -> Ingredient.of(thaumcraft.common.config.ConfigItems.ingots.get("void").get()));

    public ItemCrimsonBlade(Properties properties) {
        super(CRIMSON_VOID, properties.attributes(SwordItem.createAttributes(CRIMSON_VOID, 3, -2.4f)));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (stack.isDamaged() && entity != null && entity.tickCount % 20 == 0 && entity instanceof LivingEntity) {
            stack.setDamageValue(stack.getDamageValue() - 1);
        }
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!target.level().isClientSide()) {
            if (!(target instanceof Player) || !(attacker instanceof Player) || target.getServer() == null
                    || target.getServer().isPvpAllowed()) {
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60));
                target.addEffect(new MobEffectInstance(MobEffects.HUNGER, 120));
            }
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public int getWarp(ItemStack itemstack, Player player) {
        return 2;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("enchantment.special.sapgreat").withStyle(ChatFormatting.GOLD));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
