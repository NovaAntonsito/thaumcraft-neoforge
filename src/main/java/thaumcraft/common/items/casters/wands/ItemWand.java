package thaumcraft.common.items.casters.wands;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import thaumcraft.api.casters.FocusPackage;
import thaumcraft.api.items.IRechargable;
import thaumcraft.api.items.RechargeHelper;
import thaumcraft.common.items.casters.ItemCaster;
import thaumcraft.common.items.casters.ItemFocus;
import thaumcraft.common.registry.TCDataComponents;

/**
 * A wand of Thaumcraft 4 on the Thaumcraft 6 caster: same foci, but it casts from the vis it holds instead of the
 * aura. It fills from the aura while held (one vis a second), on a recharge pedestal or with an amulet of vis.
 * The rod sets the capacity; the cap the cost; both the most complex focus it casts. Not in Thaumcraft 6.
 */
public class ItemWand extends ItemCaster implements IRechargable, thaumcraft.common.items.ISubItems {

    public ItemWand(Item.Properties properties) {
        super(0, properties);
    }

    public static WandParts getParts(ItemStack stack) {
        return stack.getOrDefault(TCDataComponents.WAND_PARTS.get(), WandParts.DEFAULT);
    }

    public static ItemStack make(Item wand, String rod, String cap, boolean full) {
        ItemStack stack = new ItemStack(wand);
        WandParts parts = new WandParts(rod, cap);
        stack.set(TCDataComponents.WAND_PARTS.get(), parts);
        if (full) stack.set(TCDataComponents.CHARGE.get(), parts.getRod().capacity());
        return stack;
    }

    /** The vis held, with the fraction left over from casts (the charge itself is whole vis). */
    public static float getVis(ItemStack stack) {
        return Math.max(0, RechargeHelper.getCharge(stack)) + stack.getOrDefault(TCDataComponents.WAND_FRACTION.get(), 0.0f);
    }

    private static void setVis(ItemStack stack, float vis) {
        int whole = Mth.floor(vis);
        stack.set(TCDataComponents.CHARGE.get(), whole);
        stack.set(TCDataComponents.WAND_FRACTION.get(), vis - whole);
    }

    @Override
    public int getMaxCharge(ItemStack stack, @Nullable LivingEntity player) {
        return getParts(stack).getRod().capacity();
    }

    @Override
    public EnumChargeDisplay showInHud(ItemStack stack, @Nullable LivingEntity player) {
        return EnumChargeDisplay.NEVER;
    }

    @Override
    public boolean consumeVis(ItemStack is, Player player, float amount, boolean crafting, boolean sim) {
        amount *= getConsumptionModifier(is, player, crafting);
        float vis = getVis(is);
        if (vis < amount) return false;
        if (!sim) setVis(is, vis - amount);
        return true;
    }

    @Override
    public float getConsumptionModifier(ItemStack is, @Nullable Player player, boolean crafting) {
        return super.getConsumptionModifier(is, player, crafting) * getParts(is).getCap().costModifier();
    }

    private static boolean tooComplex(ItemStack wand, ItemStack focus) {
        FocusPackage p = focus.isEmpty() ? null : ItemFocus.getPackage(focus);
        return p != null && p.getComplexity() > getParts(wand).maxComplexity();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (tooComplex(held, getFocusStack(held))) {
            if (world.isClientSide) {
                player.displayClientMessage(Component.translatable("tc.wand.toocomplex").withStyle(ChatFormatting.RED), true);
            }
            return InteractionResultHolder.fail(held);
        }
        ItemFocus focus = getFocus(held);
        if (focus != null && !world.isClientSide && getVis(held) < focus.getVisCost(getFocusStack(held)) * getConsumptionModifier(held, player, false)) {
            player.displayClientMessage(Component.translatable("tc.wandnocharge").withStyle(ChatFormatting.RED), true);
        }
        return super.use(world, player, hand);
    }

    @Override
    public void inventoryTick(ItemStack is, Level w, Entity e, int slot, boolean currentItem) {
        super.inventoryTick(is, w, e, slot, currentItem);
        if (w.isClientSide || !(e instanceof ServerPlayer player)) return;
        boolean held = player.getMainHandItem() == is || player.getOffhandItem() == is;
        if (held && e.tickCount % 20 == 0) {
            RechargeHelper.rechargeItem(w, is, player.blockPosition(), player, 1);
        }
        // WandRodPrimalOnUpdate: below a tenth of its capacity, a primal rod gains a vis every 200 ticks.
        WandParts.Rod rod = getParts(is).getRod();
        if (rod.primal() != null && e.tickCount % 200 == 0 && getVis(is) < rod.capacity() / 10.0f) {
            RechargeHelper.rechargeItemBlindly(is, player, 1);
        }
    }

    @Override
    public Component getName(ItemStack stack) {
        WandParts parts = getParts(stack);
        return Component.translatable("item.thaumcraft.wand.name",
                Component.translatable("item.thaumcraft.wand.cap." + parts.getCap().id()),
                Component.translatable("item.thaumcraft.wand.rod." + parts.getRod().id()));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        WandParts parts = getParts(stack);
        tooltip.add(Component.translatable("tc.wandcharge", String.format("%.1f", getVis(stack)) + " / " + parts.getRod().capacity())
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable("tc.wand.complexity", parts.maxComplexity()).withStyle(ChatFormatting.DARK_PURPLE));
        if (parts.getCap().costModifier() != 1.0f) {
            int pct = Math.round((parts.getCap().costModifier() - 1.0f) * 100);
            tooltip.add(Component.translatable("tc.wand.cost", (pct > 0 ? "+" : "") + pct + "%").withStyle(ChatFormatting.GRAY));
        }
        if (tooComplex(stack, getFocusStack(stack))) {
            tooltip.add(Component.translatable("tc.wand.toocomplex").withStyle(ChatFormatting.RED));
        }
        super.appendHoverText(stack, context, tooltip, flag);
    }

    /** One full wand per rod, with the cap Thaumcraft 4 paired it with in its creative tab. */
    @Override
    public void fillSubItems(java.util.function.Consumer<ItemStack> output) {
        output.accept(make(this, "wood", "iron", true));
        output.accept(make(this, "greatwood", "gold", true));
        for (String rod : new String[]{"obsidian", "blaze", "ice", "quartz", "bone", "reed"}) {
            output.accept(make(this, rod, "thaumium", true));
        }
        output.accept(make(this, "silverwood", "thaumium", true));
        output.accept(make(this, "silverwood", "void", true));
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0f * Math.min(1.0f, getVis(stack) / getMaxCharge(stack, null)));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0xCF00FF;
    }
}
