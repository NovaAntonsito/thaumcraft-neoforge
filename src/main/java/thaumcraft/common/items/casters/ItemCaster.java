package thaumcraft.common.items.casters;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.PacketDistributor;
import thaumcraft.api.casters.CasterTriggerRegistry;
import thaumcraft.api.casters.FocusEngine;
import thaumcraft.api.casters.FocusPackage;
import thaumcraft.api.casters.ICaster;
import thaumcraft.api.casters.IFocusBlockPicker;
import thaumcraft.api.casters.IFocusElement;
import thaumcraft.api.casters.IInteractWithCaster;
import thaumcraft.api.items.IArchitect;
import thaumcraft.common.lib.network.misc.PacketAuraToClient;
import thaumcraft.common.lib.utils.BlockUtils;
import net.minecraft.world.phys.HitResult;
import thaumcraft.common.registry.TCDataComponents;
import thaumcraft.common.world.aura.AuraChunk;
import thaumcraft.common.world.aura.AuraHandler;

/**
 * The caster's gauntlet. Holds a focus (CASTER_FOCUS component) and casts it with vis drawn from the aura.
 * With a plan focus it is an architect item (the area overlay); with an exchange focus, sneak + right click on a block
 * picks it (CASTER_PICKED component).
 */
public class ItemCaster extends Item implements ICaster, IArchitect {
    final int area;
    final DecimalFormat myFormatter = new DecimalFormat("#######.#");

    public ItemCaster(int area, Item.Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.UNCOMMON));
        this.area = area;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        if (oldStack.getItem() == this && newStack.getItem() == this) {
            ItemFocus oldf = getFocus(oldStack);
            ItemFocus newf = getFocus(newStack);
            int s1 = 0;
            int s2 = 0;
            if (oldf != null && oldf.getSortingHelper(getFocusStack(oldStack)) != null) {
                s1 = oldf.getSortingHelper(getFocusStack(oldStack)).hashCode();
            }
            if (newf != null && newf.getSortingHelper(getFocusStack(newStack)) != null) {
                s2 = newf.getSortingHelper(getFocusStack(newStack)).hashCode();
            }
            return s1 != s2;
        }
        return newStack.getItem() != oldStack.getItem();
    }

    private float getAuraPool(Player player) {
        float tot = 0.0f;
        Level w = player.level();
        BlockPos pos = player.blockPosition();
        switch (area) {
            default -> tot = AuraHandler.getVis(w, pos);
            case 1 -> {
                tot = AuraHandler.getVis(w, pos);
                for (Direction face : Direction.Plane.HORIZONTAL) {
                    tot += AuraHandler.getVis(w, pos.relative(face, 16));
                }
            }
            case 2 -> {
                for (int xx = -1; xx <= 1; ++xx) {
                    for (int zz = -1; zz <= 1; ++zz) {
                        tot += AuraHandler.getVis(w, pos.offset(xx * 16, 0, zz * 16));
                    }
                }
            }
        }
        return tot;
    }

    @Override
    public boolean consumeVis(ItemStack is, Player player, float amount, boolean crafting, boolean sim) {
        amount *= getConsumptionModifier(is, player, crafting);
        float tot = getAuraPool(player);
        if (tot < amount) {
            return false;
        }
        if (sim) {
            return true;
        }
        Level w = player.level();
        BlockPos pos = player.blockPosition();
        drain:
        switch (area) {
            default -> amount -= AuraHandler.drainVis(w, pos, amount, sim);
            case 1 -> {
                float i = amount / 5.0f;
                while (amount > 0.0f) {
                    if (i > amount) {
                        i = amount;
                    }
                    amount -= AuraHandler.drainVis(w, pos, i, sim);
                    if (amount <= 0.0f) {
                        break;
                    }
                    if (i > amount) {
                        i = amount;
                    }
                    for (Direction face : Direction.Plane.HORIZONTAL) {
                        amount -= AuraHandler.drainVis(w, pos.relative(face, 16), i, sim);
                        if (amount <= 0.0f) {
                            break drain;
                        }
                    }
                }
            }
            case 2 -> {
                float i = amount / 9.0f;
                while (amount > 0.0f) {
                    if (i > amount) {
                        i = amount;
                    }
                    for (int xx = -1; xx <= 1; ++xx) {
                        for (int zz = -1; zz <= 1; ++zz) {
                            amount -= AuraHandler.drainVis(w, pos.offset(xx * 16, 0, zz * 16), i, sim);
                            if (amount <= 0.0f) {
                                break drain;
                            }
                        }
                    }
                }
            }
        }
        return amount <= 0.0f;
    }

    @Override
    public float getConsumptionModifier(ItemStack is, @Nullable Player player, boolean crafting) {
        float consumptionModifier = 1.0f;
        if (player != null) {
            consumptionModifier -= CasterManager.getTotalVisDiscount(player);
            if (!crafting) {
                consumptionModifier *= thaumcraft.common.anomaly.AnomalyModifiers.get(player.level(), thaumcraft.common.anomaly.AnomalyStat.FOCUS_VIS_COST);
            }
        }
        return Math.max(consumptionModifier, 0.1f);
    }

    @Override
    @Nullable
    public ItemFocus getFocus(ItemStack stack) {
        ItemStack fs = getFocusStack(stack);
        if (!fs.isEmpty() && fs.getItem() instanceof ItemFocus f) {
            return f;
        }
        return null;
    }

    @Override
    public ItemStack getFocusStack(ItemStack stack) {
        ItemContainerContents c = stack.get(TCDataComponents.CASTER_FOCUS.get());
        return c == null ? ItemStack.EMPTY : c.copyOne();
    }

    @Override
    public void setFocus(ItemStack stack, ItemStack focus) {
        if (focus == null || focus.isEmpty()) {
            stack.remove(TCDataComponents.CASTER_FOCUS.get());
        } else {
            stack.set(TCDataComponents.CASTER_FOCUS.get(), ItemContainerContents.fromItems(List.of(focus.copy())));
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        ItemStack focus = getFocusStack(stack);
        String text = "";
        if (!focus.isEmpty()) {
            float amt = ((ItemFocus) focus.getItem()).getVisCost(focus);
            if (amt > 0.0f) {
                text = myFormatter.format(amt) + " ";
            }
        }
        var line = Component.translatable("tc.vis.cost").withStyle(ChatFormatting.ITALIC, ChatFormatting.AQUA);
        if (!text.isEmpty()) {
            line.append(Component.literal(" " + text).withStyle(ChatFormatting.RESET))
                    .append(Component.translatable("item.Focus.cost1").withStyle(ChatFormatting.RESET));
        }
        tooltip.add(line);
        ItemFocus f = getFocus(stack);
        if (f != null) {
            tooltip.add(focus.getHoverName().copy().withStyle(ChatFormatting.BOLD, ChatFormatting.ITALIC, ChatFormatting.GREEN));
            f.addFocusInformation(focus, tooltip);
        }
    }

    @Override
    public void inventoryTick(ItemStack is, Level w, Entity e, int slot, boolean currentItem) {
        if (!w.isClientSide && e.tickCount % 10 == 0 && e instanceof ServerPlayer sp) {
            for (ItemStack h : sp.getHandSlots()) {
                if (!h.isEmpty() && h.getItem() instanceof ICaster) {
                    updateAura(is, w, sp);
                    break;
                }
            }
        }
    }

    private void updateAura(ItemStack stack, Level world, ServerPlayer player) {
        float cv = 0.0f;
        float cf = 0.0f;
        short bv = 0;
        int cx = player.blockPosition().getX() >> 4;
        int cz = player.blockPosition().getZ() >> 4;
        switch (area) {
            default -> {
                AuraChunk ac = AuraHandler.getAuraChunk(world.dimension(), cx, cz);
                if (ac != null) {
                    cv = ac.getVis();
                    cf = ac.getFlux();
                    bv = ac.getBase();
                }
            }
            case 1 -> {
                AuraChunk ac = AuraHandler.getAuraChunk(world.dimension(), cx, cz);
                if (ac != null) {
                    cv = ac.getVis();
                    cf = ac.getFlux();
                    bv = ac.getBase();
                    for (Direction face : Direction.Plane.HORIZONTAL) {
                        ac = AuraHandler.getAuraChunk(world.dimension(), cx + face.getStepX(), cz + face.getStepZ());
                        if (ac != null) {
                            cv += ac.getVis();
                            cf += ac.getFlux();
                            bv += ac.getBase();
                        }
                    }
                }
            }
            case 2 -> {
                for (int xx = -1; xx <= 1; ++xx) {
                    for (int zz = -1; zz <= 1; ++zz) {
                        AuraChunk ac = AuraHandler.getAuraChunk(world.dimension(), cx + xx, cz + zz);
                        if (ac != null) {
                            cv += ac.getVis();
                            cf += ac.getFlux();
                            bv += ac.getBase();
                        }
                    }
                }
            }
        }
        PacketDistributor.sendToPlayer(player, new PacketAuraToClient(bv, cv, cf));
    }

    /** Blocks and tiles that react to the caster get the click first, as in the original. */
    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction side = context.getClickedFace();
        BlockState bs = world.getBlockState(pos);
        if (bs.getBlock() instanceof IInteractWithCaster block
                && block.onCasterRightClick(world, stack, player, pos, side, context.getHand())) {
            return InteractionResult.PASS;
        }
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof IInteractWithCaster ic
                && ic.onCasterRightClick(world, stack, player, pos, side, context.getHand())) {
            return InteractionResult.PASS;
        }
        if (CasterTriggerRegistry.hasTrigger(bs)) {
            return CasterTriggerRegistry.performTrigger(world, stack, player, pos, side, bs) ? InteractionResult.SUCCESS : InteractionResult.FAIL;
        }
        ItemStack fb = getFocusStack(stack);
        if (!fb.isEmpty()) {
            FocusPackage core = ItemFocus.getPackage(fb);
            if (core != null) {
                for (IFocusElement fe : core.nodes) {
                    if (fe instanceof IFocusBlockPicker && player.isShiftKeyDown() && world.getBlockEntity(pos) == null) {
                        if (!world.isClientSide) {
                            ItemStack isout = new ItemStack(bs.getBlock());
                            if (!bs.isAir()) {
                                ItemStack is = BlockUtils.getSilkTouchDrop(bs);
                                if (!is.isEmpty()) {
                                    isout = is.copy();
                                }
                            }
                            storePickedBlock(stack, isout);
                            return InteractionResult.SUCCESS;
                        }
                        player.swing(context.getHand());
                        return InteractionResult.PASS;
                    }
                }
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        ItemStack focusStack = getFocusStack(held);
        ItemFocus focus = getFocus(held);
        if (focus == null || CasterManager.isOnCooldown(player)) {
            return super.use(world, player, hand);
        }
        CasterManager.setCooldown(player, focus.getActivationTime(focusStack));
        FocusPackage core = ItemFocus.getPackage(focusStack);
        if (core == null) {
            return InteractionResultHolder.fail(held);
        }
        if (player.isShiftKeyDown()) {
            for (IFocusElement fe : core.nodes) {
                if (fe instanceof IFocusBlockPicker) {
                    return InteractionResultHolder.pass(held);
                }
            }
        }
        if (world.isClientSide) {
            return InteractionResultHolder.success(held);
        }
        if (consumeVis(held, player, focus.getVisCost(focusStack), false, false)) {
            FocusEngine.castFocusPackage(player, core);
            thaumcraft.common.anomaly.AnomalyRunawayFocus.afterCast(player);
            player.swing(hand, true);
            return InteractionResultHolder.success(held);
        }
        return InteractionResultHolder.fail(held);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public ArrayList<BlockPos> getArchitectBlocks(ItemStack stack, Level world, BlockPos pos, Direction side, Player player) {
        ItemFocus focus = getFocus(stack);
        if (focus != null) {
            FocusPackage fp = ItemFocus.getPackage(getFocusStack(stack));
            if (fp != null) {
                for (IFocusElement fe : fp.nodes) {
                    if (fe instanceof IArchitect architect) {
                        return architect.getArchitectBlocks(stack, world, pos, side, player);
                    }
                }
            }
        }
        return null;
    }

    @Override
    public boolean showAxis(ItemStack stack, Level world, Player player, Direction side, EnumAxis axis) {
        ItemFocus focus = getFocus(stack);
        if (focus != null) {
            FocusPackage fp = ItemFocus.getPackage(getFocusStack(stack));
            if (fp != null) {
                for (IFocusElement fe : fp.nodes) {
                    if (fe instanceof IArchitect architect) {
                        return architect.showAxis(stack, world, player, side, axis);
                    }
                }
            }
        }
        return false;
    }

    @Override
    @Nullable
    public HitResult getArchitectMOP(ItemStack stack, Level world, LivingEntity player) {
        ItemFocus focus = getFocus(stack);
        if (focus != null) {
            FocusPackage fp = ItemFocus.getPackage(getFocusStack(stack));
            if (fp != null && FocusEngine.doesPackageContainElement(fp, "thaumcraft.PLAN")) {
                return ((IArchitect) FocusEngine.getElement("thaumcraft.PLAN")).getArchitectMOP(getFocusStack(stack), world, player);
            }
        }
        return null;
    }

    @Override
    public boolean useBlockHighlight(ItemStack stack) {
        return false;
    }

    public void storePickedBlock(ItemStack stack, ItemStack stackout) {
        stack.set(TCDataComponents.CASTER_PICKED.get(), ItemContainerContents.fromItems(List.of(stackout.copy())));
    }

    /** The picked block, only while the mounted focus has a block picker (exchange). EMPTY otherwise. */
    @Override
    public ItemStack getPickedBlock(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemContainerContents picked = stack.get(TCDataComponents.CASTER_PICKED.get());
        ItemFocus focus = getFocus(stack);
        if (focus != null && picked != null) {
            FocusPackage fp = ItemFocus.getPackage(getFocusStack(stack));
            if (fp != null) {
                for (IFocusElement fe : fp.nodes) {
                    if (fe instanceof IFocusBlockPicker) {
                        return picked.copyOne();
                    }
                }
            }
        }
        return ItemStack.EMPTY;
    }
}
