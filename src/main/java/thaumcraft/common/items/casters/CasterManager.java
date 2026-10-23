package thaumcraft.common.items.casters;

import java.util.HashMap;
import java.util.TreeMap;

import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import thaumcraft.api.casters.FocusEngine;
import thaumcraft.api.casters.FocusPackage;
import thaumcraft.api.casters.ICaster;
import thaumcraft.api.items.IVisDiscountGear;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.lib.utils.BaublesHelper;
import thaumcraft.common.registry.TCDataComponents;

/**
 * Vis discounts, caster cooldowns and focus swapping.
 */
public class CasterManager {
    static HashMap<Integer, Long> cooldownServer = new HashMap<>();
    static HashMap<Integer, Long> cooldownClient = new HashMap<>();

    private static final EquipmentSlot[] ARMOR = {EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD};

    public static float getTotalVisDiscount(Player player) {
        int total = 0;
        if (player == null) {
            return 0.0f;
        }
        for (ItemStack bauble : BaublesHelper.getBaubles(player)) {
            if (bauble.getItem() instanceof IVisDiscountGear gear) {
                total += gear.getVisDiscount(bauble, player);
            }
        }
        for (EquipmentSlot slot : ARMOR) {
            ItemStack is = player.getItemBySlot(slot);
            if (is.getItem() instanceof IVisDiscountGear gear) {
                total += gear.getVisDiscount(is, player);
            }
        }
        total += thaumcraft.api.casters.CasterModifiers.getVisDiscount(player);
        // Flux Flu and Flux Phage: 10% more cost per level.
        net.minecraft.world.effect.MobEffectInstance ve = player.getEffect(thaumcraft.common.registry.TCMobEffects.VIS_EXHAUST);
        net.minecraft.world.effect.MobEffectInstance ive = player.getEffect(thaumcraft.common.registry.TCMobEffects.INFECTIOUS_VIS_EXHAUST);
        if (ve != null || ive != null) {
            int level1 = ve != null ? ve.getAmplifier() : 0;
            int level2 = ive != null ? ive.getAmplifier() : 0;
            total -= (Math.max(level1, level2) + 1) * 10;
        }
        return total / 100.0f;
    }

    public static boolean consumeVisFromInventory(Player player, float cost) {
        for (int a = player.getInventory().items.size() - 1; a >= 0; --a) {
            ItemStack item = player.getInventory().items.get(a);
            if (item.getItem() instanceof ICaster caster) {
                boolean done = caster.consumeVis(item, player, cost, true, false);
                if (done) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void playTicks(Player player, float pitch) {
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundsTC.TICKS.get(), SoundSource.PLAYERS, 0.3f, pitch);
    }

    /**
     * Mounts the focus with this sorting key (or the next one), taking it from the inventory or a focus pouch, and puts
     * the old focus in a pouch or the inventory. "REMOVE" takes the focus off.
     * Pouches worn as baubles come first. Their slots are kept as -1 - bauble slot, inventory slots as they are.
     */
    public static void changeFocus(ItemStack is, Level w, Player player, String focus) {
        ICaster wand = (ICaster) is.getItem();
        TreeMap<String, Integer> foci = new TreeMap<>();
        HashMap<Integer, Integer> pouches = new HashMap<>();
        int pouchcount = 0;
        ItemStack item;
        net.neoforged.neoforge.items.IItemHandlerModifiable baubles = BaublesHelper.getBaublesHandler(player);
        for (int a = 0; baubles != null && a < baubles.getSlots(); ++a) {
            item = baubles.getStackInSlot(a);
            if (item.getItem() instanceof ItemFocusPouch fp) {
                ++pouchcount;
                pouches.put(pouchcount, -1 - a);
                NonNullList<ItemStack> inv = fp.getInventory(item);
                for (int q = 0; q < inv.size(); ++q) {
                    ItemStack in = inv.get(q);
                    if (in.getItem() instanceof ItemFocus f) {
                        String sh = f.getSortingHelper(in);
                        if (sh != null) {
                            foci.put(sh, q + pouchcount * 1000);
                        }
                    }
                }
            }
        }
        for (int a = 0; a < 36; ++a) {
            item = player.getInventory().items.get(a);
            if (item.getItem() instanceof ItemFocus f) {
                String sh2 = f.getSortingHelper(item);
                if (sh2 == null) {
                    continue;
                }
                foci.put(sh2, a);
            }
            if (item.getItem() instanceof ItemFocusPouch fp) {
                ++pouchcount;
                pouches.put(pouchcount, a);
                NonNullList<ItemStack> inv = fp.getInventory(item);
                for (int q = 0; q < inv.size(); ++q) {
                    ItemStack in = inv.get(q);
                    if (in.getItem() instanceof ItemFocus f) {
                        String sh = f.getSortingHelper(in);
                        if (sh != null) {
                            foci.put(sh, q + pouchcount * 1000);
                        }
                    }
                }
            }
        }
        if (focus.equals("REMOVE") || foci.isEmpty()) {
            if (wand.getFocus(is) != null && (addFocusToPouch(player, wand.getFocusStack(is).copy(), pouches) || player.getInventory().add(wand.getFocusStack(is).copy()))) {
                wand.setFocus(is, ItemStack.EMPTY);
                playTicks(player, 0.9f);
            }
            return;
        }
        String newkey = focus;
        if (foci.get(newkey) == null) {
            newkey = foci.higherKey(newkey);
        }
        if (newkey == null || foci.get(newkey) == null) {
            newkey = foci.firstKey();
        }
        int slot = foci.get(newkey);
        item = ItemStack.EMPTY;
        if (slot < 1000 && slot >= 0) {
            item = player.getInventory().items.get(slot).copy();
        } else {
            int pid = slot / 1000;
            if (pouches.containsKey(pid)) {
                int pouchslot = pouches.get(pid);
                int focusslot = slot - pid * 1000;
                item = fetchFocusFromPouch(player, focusslot, getPouch(player, pouchslot), pouchslot);
            }
        }
        if (item.isEmpty()) {
            return;
        }
        if (slot < 1000 && slot >= 0) {
            player.getInventory().setItem(slot, ItemStack.EMPTY);
        }
        playTicks(player, 1.0f);
        if (wand.getFocus(is) != null && (addFocusToPouch(player, wand.getFocusStack(is).copy(), pouches) || player.getInventory().add(wand.getFocusStack(is).copy()))) {
            wand.setFocus(is, ItemStack.EMPTY);
        }
        if (wand.getFocus(is) == null) {
            wand.setFocus(is, item);
        } else if (!addFocusToPouch(player, item, pouches)) {
            player.getInventory().add(item);
        }
    }

    private static ItemStack fetchFocusFromPouch(Player player, int focusid, ItemStack pouch, int pouchslot) {
        ItemStack focus = ItemStack.EMPTY;
        ItemFocusPouch fp = (ItemFocusPouch) pouch.getItem();
        NonNullList<ItemStack> inv = fp.getInventory(pouch);
        ItemStack contents = inv.get(focusid);
        if (contents.getItem() instanceof ItemFocus) {
            focus = contents.copy();
            inv.set(focusid, ItemStack.EMPTY);
            fp.setInventory(pouch, inv);
            storePouch(player, pouch, pouchslot);
        }
        return focus;
    }

    /** The pouch at a slot from changeFocus: the inventory for 0 and up, a bauble slot for -1 and down. */
    private static ItemStack getPouch(Player player, int pouchslot) {
        return pouchslot >= 0 ? player.getInventory().items.get(pouchslot) : BaublesHelper.getStackInSlot(player, -1 - pouchslot);
    }

    private static void storePouch(Player player, ItemStack pouch, int pouchslot) {
        if (pouchslot >= 0) {
            player.getInventory().setChanged();
        } else {
            BaublesHelper.setStackInSlot(player, -1 - pouchslot, pouch);
        }
    }

    private static boolean addFocusToPouch(Player player, ItemStack focus, HashMap<Integer, Integer> pouches) {
        for (Integer pouchslot : pouches.values()) {
            ItemStack pouch = getPouch(player, pouchslot);
            if (!(pouch.getItem() instanceof ItemFocusPouch fp)) {
                continue;
            }
            NonNullList<ItemStack> inv = fp.getInventory(pouch);
            for (int q = 0; q < inv.size(); ++q) {
                if (inv.get(q).isEmpty()) {
                    inv.set(q, focus.copy());
                    fp.setInventory(pouch, inv);
                    storePouch(player, pouch, pouchslot);
                    return true;
                }
            }
        }
        return false;
    }

    /** The G key: grows the plan focus area along the current axis, or (with ctrl) moves to the next axis. */
    public static void toggleMisc(ItemStack itemstack, Level world, Player player, int mod) {
        if (!(itemstack.getItem() instanceof ICaster caster)) {
            return;
        }
        FocusPackage fp = ItemFocus.getPackage(caster.getFocusStack(itemstack));
        if (fp != null && FocusEngine.doesPackageContainElement(fp, "thaumcraft.PLAN")) {
            int dim = getAreaDim(itemstack);
            if (mod == 0) {
                int areax = getAreaX(itemstack);
                int areay = getAreaY(itemstack);
                int areaz = getAreaZ(itemstack);
                int max = getAreaSize(itemstack);
                if (dim == 0) {
                    ++areax;
                    ++areaz;
                    ++areay;
                } else if (dim == 1) {
                    ++areax;
                } else if (dim == 2) {
                    ++areaz;
                } else if (dim == 3) {
                    ++areay;
                }
                if (areax > max) {
                    areax = 0;
                }
                if (areaz > max) {
                    areaz = 0;
                }
                if (areay > max) {
                    areay = 0;
                }
                setAreaX(itemstack, areax);
                setAreaY(itemstack, areay);
                setAreaZ(itemstack, areaz);
            }
            if (mod == 1) {
                if (++dim > 3) {
                    dim = 0;
                }
                setAreaDim(itemstack, dim);
            }
        }
    }

    /** The original kept a potency check here that was never set; the area is always up to 3 blocks out. */
    private static int getAreaSize(ItemStack itemstack) {
        boolean pot = false;
        if (!(itemstack.getItem() instanceof ICaster)) {
            return 0;
        }
        return pot ? 6 : 3;
    }

    public static int getAreaDim(ItemStack stack) {
        return stack.getOrDefault(TCDataComponents.AREA_DIM.get(), 0);
    }

    public static int getAreaX(ItemStack stack) {
        return getArea(stack, TCDataComponents.AREA_X.get());
    }

    public static int getAreaY(ItemStack stack) {
        return getArea(stack, TCDataComponents.AREA_Y.get());
    }

    public static int getAreaZ(ItemStack stack) {
        return getArea(stack, TCDataComponents.AREA_Z.get());
    }

    private static int getArea(ItemStack stack, DataComponentType<Integer> type) {
        Integer a = stack.get(type);
        if (a != null) {
            return Math.min(a, getAreaSize(stack));
        }
        return getAreaSize(stack);
    }

    public static void setAreaX(ItemStack stack, int area) {
        stack.set(TCDataComponents.AREA_X.get(), area);
    }

    public static void setAreaY(ItemStack stack, int area) {
        stack.set(TCDataComponents.AREA_Y.get(), area);
    }

    public static void setAreaZ(ItemStack stack, int area) {
        stack.set(TCDataComponents.AREA_Z.get(), area);
    }

    public static void setAreaDim(ItemStack stack, int dim) {
        stack.set(TCDataComponents.AREA_DIM.get(), dim);
    }

    static boolean isOnCooldown(LivingEntity entityLiving) {
        if (entityLiving.level().isClientSide && cooldownClient.containsKey(entityLiving.getId())) {
            return cooldownClient.get(entityLiving.getId()) > System.currentTimeMillis();
        }
        return !entityLiving.level().isClientSide && cooldownServer.containsKey(entityLiving.getId())
                && cooldownServer.get(entityLiving.getId()) > System.currentTimeMillis();
    }

    public static float getCooldown(LivingEntity entityLiving) {
        if (entityLiving.level().isClientSide && cooldownClient.containsKey(entityLiving.getId())) {
            return (cooldownClient.get(entityLiving.getId()) - System.currentTimeMillis()) / 1000.0f;
        }
        return 0.0f;
    }

    public static void setCooldown(LivingEntity entityLiving, int cd) {
        if (cd == 0) {
            cooldownClient.remove(entityLiving.getId());
            cooldownServer.remove(entityLiving.getId());
        } else if (entityLiving.level().isClientSide) {
            cooldownClient.put(entityLiving.getId(), System.currentTimeMillis() + cd * 50L);
        } else {
            cooldownServer.put(entityLiving.getId(), System.currentTimeMillis() + cd * 50L);
        }
    }
}
