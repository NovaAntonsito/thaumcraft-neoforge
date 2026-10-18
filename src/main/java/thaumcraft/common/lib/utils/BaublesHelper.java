package thaumcraft.common.lib.utils;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

/**
 * Stands in for the 1.12 BaublesApi: the worn bauble slots, now the Curios slots, seen as one list of slots.
 * Bauble types map to Curios slots: amulet to necklace, ring, belt, head and charm.
 */
public final class BaublesHelper {
    public static final TagKey<Item> AMULET = curiosTag("necklace");
    public static final TagKey<Item> RING = curiosTag("ring");
    public static final TagKey<Item> BELT = curiosTag("belt");
    public static final TagKey<Item> HEAD = curiosTag("head");
    public static final TagKey<Item> CHARM = curiosTag("charm");

    private BaublesHelper() {
    }

    private static TagKey<Item> curiosTag(String slot) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("curios", slot));
    }

    /** The worn curios as one handler, or null if the entity has none. */
    @Nullable
    public static IItemHandlerModifiable getBaublesHandler(@Nullable LivingEntity entity) {
        if (entity == null) {
            return null;
        }
        return CuriosApi.getCuriosInventory(entity).map(ICuriosItemHandler::getEquippedCurios).orElse(null);
    }

    /** Every worn bauble, empty slots left out. */
    public static List<ItemStack> getBaubles(@Nullable LivingEntity entity) {
        List<ItemStack> out = new ArrayList<>();
        IItemHandlerModifiable baubles = getBaublesHandler(entity);
        if (baubles != null) {
            for (int a = 0; a < baubles.getSlots(); ++a) {
                ItemStack stack = baubles.getStackInSlot(a);
                if (!stack.isEmpty()) {
                    out.add(stack);
                }
            }
        }
        return out;
    }

    /** isBaubleEquipped: the first slot that holds this item, or -1. */
    public static int isBaubleEquipped(@Nullable LivingEntity entity, Item item) {
        IItemHandlerModifiable baubles = getBaublesHandler(entity);
        if (baubles != null) {
            for (int a = 0; a < baubles.getSlots(); ++a) {
                if (baubles.getStackInSlot(a).is(item)) {
                    return a;
                }
            }
        }
        return -1;
    }

    public static ItemStack getStackInSlot(@Nullable LivingEntity entity, int slot) {
        IItemHandlerModifiable baubles = getBaublesHandler(entity);
        if (baubles == null || slot < 0 || slot >= baubles.getSlots()) {
            return ItemStack.EMPTY;
        }
        return baubles.getStackInSlot(slot);
    }

    /** Puts the stack back in its slot, so Curios sends the change to the client. */
    public static void setStackInSlot(@Nullable LivingEntity entity, int slot, ItemStack stack) {
        IItemHandlerModifiable baubles = getBaublesHandler(entity);
        if (baubles != null && slot >= 0 && slot < baubles.getSlots()) {
            baubles.setStackInSlot(slot, stack);
        }
    }

    /** The 1.12 bauble type names the infusion enchantments use: "amulet", "belt", "ring", or "none". */
    public static String getBaubleType(ItemStack stack) {
        if (stack.is(AMULET)) {
            return "amulet";
        }
        if (stack.is(BELT)) {
            return "belt";
        }
        if (stack.is(RING)) {
            return "ring";
        }
        return "none";
    }

    /** Whether the item can be worn in any bauble slot. */
    public static boolean isBauble(ItemStack stack) {
        return stack.is(AMULET) || stack.is(RING) || stack.is(BELT) || stack.is(HEAD) || stack.is(CHARM);
    }
}
