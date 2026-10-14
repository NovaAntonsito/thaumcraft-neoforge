package thaumcraft.common.lib.utils;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import thaumcraft.api.research.ResearchStage;
import thaumcraft.common.registry.TCDataComponents;

/**
 * Partial port of InventoryUtils: the research requirement checks on the player's main inventory, and ejectStackAt.
 * A requirement is an ItemStack, an item tag name (was an ore dictionary name) or an enchantment requirement
 * (was the enchanted_placeholder item, see checkEnchantedPlaceholder).
 */
public final class InventoryUtils {
    private InventoryUtils() {
    }

    /** The count a requirement asks for. */
    public static int requiredCount(Object req) {
        return req instanceof ItemStack stack ? stack.getCount() : 1;
    }

    /** areItemStacksEqual with a relaxed NBT filter: the same item, and the same aspects if the requirement has them. */
    public static boolean matches(Object req, ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (req instanceof ItemStack r) {
            if (!stack.is(r.getItem())) return false;
            return !r.has(TCDataComponents.ASPECTS.get()) || r.get(TCDataComponents.ASPECTS.get()).equals(stack.get(TCDataComponents.ASPECTS.get()));
        }
        if (req instanceof String tag) {
            ResourceLocation id = ResourceLocation.tryParse(tag);
            return id != null && stack.is(TagKey.create(Registries.ITEM, id));
        }
        if (req instanceof ResearchStage.EnchantmentRequirement ench) {
            return enchantmentLevel(stack.getEnchantments(), ench.enchantment()) >= ench.level()
                    || enchantmentLevel(stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY), ench.enchantment()) >= ench.level();
        }
        return false;
    }

    private static int enchantmentLevel(ItemEnchantments enchantments, ResourceLocation id) {
        for (Object2IntMap.Entry<Holder<Enchantment>> e : enchantments.entrySet()) {
            if (e.getKey().unwrapKey().map(k -> k.location().equals(id)).orElse(false)) return e.getIntValue();
        }
        return 0;
    }

    public static boolean isPlayerCarryingAmount(Player player, Object req) {
        int count = requiredCount(req);
        for (ItemStack stack : player.getInventory().items) {
            if (matches(req, stack)) {
                count -= stack.getCount();
                if (count <= 0) return true;
            }
        }
        return false;
    }

    public static boolean consumePlayerItem(Player player, Object req, boolean nocheck) {
        if (!nocheck && !isPlayerCarryingAmount(player, req)) return false;
        int count = requiredCount(req);
        for (ItemStack stack : player.getInventory().items) {
            if (matches(req, stack)) {
                int take = Math.min(count, stack.getCount());
                stack.shrink(take);
                count -= take;
                if (count <= 0) return true;
            }
        }
        return false;
    }

    /**
     * Puts a stack into the inventory on the given side, or throws whatever is left out that way.
     * The original went through ThaumcraftInvHelper.insertStackAt.
     */
    public static void ejectStackAt(net.minecraft.world.level.Level world, net.minecraft.core.BlockPos pos, net.minecraft.core.Direction side,
                                    ItemStack out) {
        net.minecraft.core.BlockPos target = pos.relative(side);
        net.neoforged.neoforge.items.IItemHandler handler = world.getCapability(net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK,
                target, side.getOpposite());
        if (handler != null) {
            out = net.neoforged.neoforge.items.ItemHandlerHelper.insertItemStacked(handler, out, false);
        }
        if (!out.isEmpty()) {
            if (world.getBlockState(target).isCollisionShapeFullBlock(world, target)) {
                pos = pos.relative(side.getOpposite());
            }
            net.minecraft.world.entity.item.ItemEntity entityitem2 = new net.minecraft.world.entity.item.ItemEntity(world,
                    pos.getX() + 0.5 + side.getStepX(), pos.getY() + side.getStepY(), pos.getZ() + 0.5 + side.getStepZ(), out);
            entityitem2.setDeltaMovement(0.3 * side.getStepX(), 0.3 * side.getStepY(), 0.3 * side.getStepZ());
            world.addFreshEntity(entityitem2);
        }
    }

    /**
     * Whether the inventories touching pos (except above it) hold enough of the stack, with the BASEORE filter:
     * the same item, ignoring damage and components.
     */
    public static boolean checkAdjacentChests(net.minecraft.world.level.Level world, net.minecraft.core.BlockPos pos, ItemStack itemStack) {
        int c = itemStack.getCount();
        for (net.minecraft.core.Direction face : net.minecraft.core.Direction.values()) {
            if (face == net.minecraft.core.Direction.UP) continue;
            net.neoforged.neoforge.items.IItemHandler handler = world.getCapability(net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK,
                    pos.relative(face), face.getOpposite());
            if (handler == null) continue;
            for (int i = 0; i < handler.getSlots(); ++i) {
                ItemStack s = handler.getStackInSlot(i);
                if (!s.isEmpty() && s.is(itemStack.getItem())) {
                    c -= s.getCount();
                    if (c <= 0) return true;
                }
            }
        }
        return false;
    }

    /** Takes the stack out of the inventories touching pos (except above it). Returns whether all of it was found. */
    public static boolean consumeFromAdjacentChests(net.minecraft.world.level.Level world, net.minecraft.core.BlockPos pos, ItemStack itemStack) {
        int c = itemStack.getCount();
        for (net.minecraft.core.Direction face : net.minecraft.core.Direction.values()) {
            if (face == net.minecraft.core.Direction.UP || c <= 0) continue;
            net.neoforged.neoforge.items.IItemHandler handler = world.getCapability(net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK,
                    pos.relative(face), face.getOpposite());
            if (handler == null) continue;
            for (int i = 0; i < handler.getSlots() && c > 0; ++i) {
                ItemStack s = handler.getStackInSlot(i);
                if (!s.isEmpty() && s.is(itemStack.getItem())) {
                    c -= handler.extractItem(i, c, false).getCount();
                }
            }
        }
        return c <= 0;
    }

    public static boolean consumeItemsFromAdjacentInventoryOrPlayer(net.minecraft.world.level.Level world, net.minecraft.core.BlockPos pos, Player player,
                                                                     boolean sim, ItemStack... items) {
        for (ItemStack stack : items) {
            if (!checkAdjacentChests(world, pos, stack) && !isPlayerCarryingAmount(player, stack)) {
                return false;
            }
        }
        if (!sim) {
            for (ItemStack stack : items) {
                if (!consumeFromAdjacentChests(world, pos, stack.copy())) {
                    consumePlayerItem(player, stack, true);
                }
            }
        }
        return true;
    }
}
