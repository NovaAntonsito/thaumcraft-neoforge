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

    // ---- Stage 8b: the stack helpers and filter matching the golem seals use ----

    public static ItemStack copyMaxedStack(ItemStack stack) {
        return copyLimitedStack(stack, stack.getMaxStackSize());
    }

    public static ItemStack copyLimitedStack(ItemStack stack, int limit) {
        if (stack == null) {
            return ItemStack.EMPTY;
        }
        ItemStack s = stack.copy();
        if (s.getCount() > limit) {
            s.setCount(limit);
        }
        return s;
    }

    public static ItemStack removeStackFrom(net.minecraft.world.level.Level world, net.minecraft.core.BlockPos pos, net.minecraft.core.Direction side,
                                           ItemStack stack, thaumcraft.api.ThaumcraftInvHelper.InvFilter filter, boolean simulate) {
        return removeStackFrom(thaumcraft.api.ThaumcraftInvHelper.getItemHandlerAt(world, pos, side), stack, filter, simulate);
    }

    public static ItemStack removeStackFrom(net.neoforged.neoforge.items.IItemHandler inventory, ItemStack stack,
                                           thaumcraft.api.ThaumcraftInvHelper.InvFilter filter, boolean simulate) {
        int amount = stack.getCount();
        int removed = 0;
        if (inventory != null) {
            for (int a = 0; a < inventory.getSlots(); ++a) {
                if (areItemStacksEqual(stack, inventory.getStackInSlot(a), filter)) {
                    int s = Math.min(amount - removed, inventory.getStackInSlot(a).getCount());
                    ItemStack es = inventory.extractItem(a, s, simulate);
                    if (!es.isEmpty()) {
                        removed += es.getCount();
                    }
                }
                if (removed >= amount) {
                    break;
                }
            }
        }
        if (removed == 0) {
            return ItemStack.EMPTY;
        }
        ItemStack s2 = stack.copy();
        s2.setCount(removed);
        return s2;
    }

    /** Kept as in the original, where the isEmpty() check is inverted, so this always counts 0. */
    public static int countStackInWorld(net.minecraft.world.level.Level world, net.minecraft.core.BlockPos pos, ItemStack stack, double range,
                                        thaumcraft.api.ThaumcraftInvHelper.InvFilter filter) {
        int count = 0;
        java.util.List<net.minecraft.world.entity.item.ItemEntity> l = EntityUtils.getEntitiesInRange(world, pos, null,
                net.minecraft.world.entity.item.ItemEntity.class, range);
        for (net.minecraft.world.entity.item.ItemEntity ei : l) {
            if (ei.getItem().isEmpty() && areItemStacksEqual(stack, ei.getItem(), filter)) {
                count += ei.getItem().getCount();
            }
        }
        return count;
    }

    public static boolean areItemStacksEqualStrict(ItemStack stack0, ItemStack stack1) {
        return areItemStacksEqual(stack0, stack1, thaumcraft.api.ThaumcraftInvHelper.InvFilter.STRICT);
    }

    public static ItemStack findFirstMatchFromFilter(net.minecraft.core.NonNullList<ItemStack> filterStacks, boolean blacklist,
                                                     net.neoforged.neoforge.items.IItemHandler inv, net.minecraft.core.Direction face,
                                                     thaumcraft.api.ThaumcraftInvHelper.InvFilter filter) {
        return findFirstMatchFromFilter(filterStacks, blacklist, inv, face, filter, false);
    }

    public static ItemStack findFirstMatchFromFilter(net.minecraft.core.NonNullList<ItemStack> filterStacks, boolean blacklist,
                                                     net.neoforged.neoforge.items.IItemHandler inv, net.minecraft.core.Direction face,
                                                     thaumcraft.api.ThaumcraftInvHelper.InvFilter filter, boolean leaveOne) {
        slots:
        for (int a = 0; a < inv.getSlots(); ++a) {
            ItemStack is = inv.getStackInSlot(a);
            if (is.isEmpty() || is.getCount() <= 0) continue;
            if (leaveOne && thaumcraft.api.ThaumcraftInvHelper.countTotalItemsIn(inv, is, filter) < 2) continue;
            boolean allow = false;
            boolean allEmpty = true;
            for (ItemStack fs : filterStacks) {
                if (fs == null || fs.isEmpty()) continue;
                allEmpty = false;
                boolean r = areItemStacksEqual(fs.copy(), is.copy(), filter);
                if (blacklist) {
                    if (r) continue slots;
                    allow = true;
                } else if (r) {
                    return is;
                }
            }
            if (blacklist && (allow || allEmpty)) {
                return is;
            }
        }
        return ItemStack.EMPTY;
    }

    public static boolean matchesFilters(net.minecraft.core.NonNullList<ItemStack> nonNullList, boolean blacklist, ItemStack is,
                                         thaumcraft.api.ThaumcraftInvHelper.InvFilter filter) {
        if (is == null || is.isEmpty() || is.getCount() <= 0) {
            return false;
        }
        boolean allow = false;
        boolean allEmpty = true;
        for (ItemStack fs : nonNullList) {
            if (fs == null || fs.isEmpty()) continue;
            allEmpty = false;
            boolean r = areItemStacksEqual(fs.copy(), is.copy(), filter);
            if (blacklist) {
                if (r) return false;
                allow = true;
            } else if (r) {
                return true;
            }
        }
        return blacklist && (allow || allEmpty);
    }

    public static ItemStack findFirstMatchFromFilter(net.minecraft.core.NonNullList<ItemStack> filterStacks,
                                                     net.minecraft.core.NonNullList<Integer> filterStacksSizes, boolean blacklist,
                                                     net.minecraft.core.NonNullList<ItemStack> itemStacks,
                                                     thaumcraft.api.ThaumcraftInvHelper.InvFilter filter) {
        return findFirstMatchFromFilterTuple(filterStacks, filterStacksSizes, blacklist, itemStacks, filter).getA();
    }

    public static net.minecraft.util.Tuple<ItemStack, Integer> findFirstMatchFromFilterTuple(
            net.minecraft.core.NonNullList<ItemStack> filterStacks, net.minecraft.core.NonNullList<Integer> filterStacksSizes, boolean blacklist,
            net.minecraft.core.NonNullList<ItemStack> stacks, thaumcraft.api.ThaumcraftInvHelper.InvFilter filter) {
        stacks:
        for (ItemStack is : stacks) {
            if (is == null || is.isEmpty() || is.getCount() <= 0) continue;
            boolean allow = false;
            boolean allEmpty = true;
            for (int idx = 0; idx < filterStacks.size(); ++idx) {
                ItemStack fs = filterStacks.get(idx);
                if (fs == null || fs.isEmpty()) continue;
                allEmpty = false;
                boolean r = areItemStacksEqual(fs.copy(), is.copy(), filter);
                if (blacklist) {
                    if (r) continue stacks;
                    allow = true;
                } else if (r) {
                    return new net.minecraft.util.Tuple<>(is, filterStacksSizes.get(idx));
                }
            }
            if (blacklist && (allow || allEmpty)) {
                return new net.minecraft.util.Tuple<>(is, 0);
            }
        }
        return new net.minecraft.util.Tuple<>(ItemStack.EMPTY, 0);
    }

    /**
     * Item comparison with the filter settings. 1.12 kept damage apart from NBT; here "NBT" means every data component
     * except damage. useOre (shared ore dictionary name) is now a shared "c:" item tag. relaxedNBT is compared strictly.
     */
    public static boolean areItemStacksEqual(ItemStack stack0, ItemStack stack1, thaumcraft.api.ThaumcraftInvHelper.InvFilter filter) {
        if (stack0 == null && stack1 != null) return false;
        if (stack0 != null && stack1 == null) return false;
        if (stack0 == null) return true;
        if (stack0.isEmpty() && !stack1.isEmpty()) return false;
        if (!stack0.isEmpty() && stack1.isEmpty()) return false;
        if (stack0.isEmpty()) return true;
        if (filter.useMod) {
            return net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack0.getItem()).getNamespace()
                    .equals(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack1.getItem()).getNamespace());
        }
        if (filter.useOre) {
            boolean shared = stack0.getTags().anyMatch(t -> t.location().getNamespace().equals("c") && stack1.is(t));
            if (shared) return true;
        }
        boolean t1 = true;
        if (!filter.igNBT) {
            ItemStack a = stack0.copy();
            ItemStack b = stack1.copy();
            a.remove(DataComponents.DAMAGE);
            b.remove(DataComponents.DAMAGE);
            t1 = ItemStack.isSameItemSameComponents(a, b);
        }
        boolean t2 = !filter.igDmg && stack0.getDamageValue() != stack1.getDamageValue();
        return stack0.getItem() == stack1.getItem() && !t2 && t1;
    }
}
