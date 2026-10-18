package thaumcraft.common.items.armor;

import javax.annotation.Nullable;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import thaumcraft.Thaumcraft;
import thaumcraft.api.items.IRechargable;
import thaumcraft.api.items.RechargeHelper;
import thaumcraft.common.registry.TCDataComponents;

/**
 * The boots of the traveller. While charged they step up full blocks, speed up walking and swimming and help
 * steering in the air; PlayerEvents adds the higher jump. One point of charge lasts a minute of wearing.
 * 1.21: inventoryTick stands in for onArmorTick. The step height is an attribute modifier now, not the
 * stepHeight field the original saved in PlayerEvents.prevStep, and the air steering (jumpMovementFactor
 * 0.05 instead of 0.02) is added as the missing 0.03 of movement.
 */
public class ItemBootsTraveller extends ArmorItem implements IRechargable {
    private static final ResourceLocation TEXTURE = Thaumcraft.id("textures/entity/armor/bootstraveler.png");
    public static final ResourceLocation STEP_ID = Thaumcraft.id("traveller_step");
    private static final AttributeModifier STEP = new AttributeModifier(STEP_ID, 0.4, AttributeModifier.Operation.ADD_VALUE);

    public ItemBootsTraveller(Holder<ArmorMaterial> material, Properties properties) {
        super(material, Type.BOOTS, properties);
    }

    @Override
    public ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, ArmorMaterial.Layer layer, boolean innerModel) {
        return TEXTURE;
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repair) {
        return repair.is(Items.LEATHER) || super.isValidRepairItem(stack, repair);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);
        if (entity instanceof Player player && player.getItemBySlot(EquipmentSlot.FEET) == stack) {
            onArmorTick(world, player, stack);
        }
    }

    private void onArmorTick(Level world, Player player, ItemStack itemStack) {
        boolean hasCharge = RechargeHelper.getCharge(itemStack) > 0;
        if (!world.isClientSide && player.tickCount % 20 == 0) {
            int e = itemStack.getOrDefault(TCDataComponents.BOOTS_ENERGY.get(), 0);
            if (e > 0) {
                --e;
            } else if (RechargeHelper.consumeCharge(itemStack, player, 1)) {
                e = 60;
            }
            itemStack.set(TCDataComponents.BOOTS_ENERGY.get(), e);
        }
        if (hasCharge && !player.getAbilities().flying && player.zza > 0.0f) {
            if (world.isClientSide && !player.isShiftKeyDown()) {
                AttributeInstance step = player.getAttribute(Attributes.STEP_HEIGHT);
                if (step != null && !step.hasModifier(STEP_ID)) {
                    step.addTransientModifier(STEP);
                }
            }
            if (player.onGround()) {
                float bonus = 0.05f;
                if (player.isInWater()) {
                    bonus /= 4.0f;
                }
                player.moveRelative(bonus, new Vec3(0.0, 0.0, 1.0));
            } else {
                if (player.isInWater()) {
                    player.moveRelative(0.025f, new Vec3(0.0, 0.0, 1.0));
                }
                player.moveRelative(0.03f, new Vec3(player.xxa, 0.0, player.zza));
            }
        }
    }

    /** PlayerEvents.handleSpeedMods: back to the normal step height when sneaking or without the boots. */
    public static void handleSpeedMods(Player player) {
        if (player.level().isClientSide && (player.isShiftKeyDown() || !(player.getItemBySlot(EquipmentSlot.FEET).getItem() instanceof ItemBootsTraveller))) {
            AttributeInstance step = player.getAttribute(Attributes.STEP_HEIGHT);
            if (step != null && step.hasModifier(STEP_ID)) {
                step.removeModifier(STEP_ID);
            }
        }
    }

    @Override
    public int getMaxCharge(ItemStack stack, @Nullable LivingEntity player) {
        return 240;
    }

    @Override
    public EnumChargeDisplay showInHud(ItemStack stack, @Nullable LivingEntity player) {
        return EnumChargeDisplay.PERIODIC;
    }
}
