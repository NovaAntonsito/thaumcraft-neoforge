package thaumcraft.common.golems.seals;

import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandler;
import thaumcraft.api.ThaumcraftInvHelper;
import thaumcraft.api.golems.EnumGolemTrait;
import thaumcraft.api.golems.GolemHelper;
import thaumcraft.api.golems.IGolemAPI;
import thaumcraft.api.golems.seals.ISealConfigToggles;
import thaumcraft.api.golems.seals.ISealEntity;
import thaumcraft.api.golems.tasks.Task;

/** Port of SealStock: keeps the whitelisted amounts in the inventory it is on by asking Provide seals for more. */
public class SealStock extends SealFiltered implements ISealConfigToggles {
    int delay;
    protected SealToggle[] props;
    ResourceLocation icon;

    public SealStock() {
        delay = new Random(System.nanoTime()).nextInt(50);
        props = new SealToggle[] {
                new SealToggle(true, "pmeta", "golem.prop.meta"),
                new SealToggle(true, "pnbt", "golem.prop.nbt"),
                new SealToggle(false, "pore", "golem.prop.ore"),
                new SealToggle(false, "pmod", "golem.prop.mod")};
        icon = ResourceLocation.fromNamespaceAndPath("thaumcraft", "items/seals/seal_stock");
    }

    @Override
    public String getKey() {
        return "thaumcraft:stock";
    }

    @Override
    public int getFilterSize() {
        return 9;
    }

    @Override
    public void tickSeal(Level world, ISealEntity seal) {
        if (delay++ % 20 != 0) {
            return;
        }
        IItemHandler inv = ThaumcraftInvHelper.getItemHandlerAt(world, seal.getSealPos().pos, seal.getSealPos().face);
        if (inv != null) {
            for (int a = 0; a < 9; ++a) {
                // 1.21: an empty filter slot would count as nothing wanted anyway; skip it before the comparison
                if (getFilterSlot(a).isEmpty()) {
                    continue;
                }
                int amt = ThaumcraftInvHelper.countTotalItemsIn(inv, getFilterSlot(a),
                        new ThaumcraftInvHelper.InvFilter(!props[0].value, !props[1].value, props[2].value, props[3].value));
                if (amt < getFilterSlotSize(a)) {
                    ItemStack fs = getFilterSlot(a).copy();
                    fs.setCount(Math.min(fs.getMaxStackSize(), getFilterSlotSize(a) - amt));
                    fs = ThaumcraftInvHelper.hasRoomFor(world, seal.getSealPos().pos, seal.getSealPos().face, fs);
                    if (!fs.isEmpty()) {
                        GolemHelper.requestProvisioning(world, seal.getSealPos().pos, seal.getSealPos().face, fs);
                    }
                }
            }
        }
    }

    @Override
    public void onTaskStarted(Level world, IGolemAPI golem, Task task) {
    }

    @Override
    public boolean onTaskCompletion(Level world, IGolemAPI golem, Task task) {
        return true;
    }

    @Override
    public boolean canGolemPerformTask(IGolemAPI golem, Task task) {
        return false;
    }

    @Override
    public boolean canPlaceAt(Level world, BlockPos pos, Direction side) {
        return ThaumcraftInvHelper.getItemHandlerAt(world, pos, side) != null;
    }

    @Override
    public ResourceLocation getSealIcon() {
        return icon;
    }

    @Override
    public int[] getGuiCategories() {
        return new int[] {1, 3, 0, 4};
    }

    @Override
    public EnumGolemTrait[] getRequiredTags() {
        return null;
    }

    @Override
    public EnumGolemTrait[] getForbiddenTags() {
        return new EnumGolemTrait[] {EnumGolemTrait.CLUMSY};
    }

    @Override
    public void onTaskSuspension(Level world, Task task) {
    }

    @Override
    public void onRemoval(Level world, BlockPos pos, Direction side) {
    }

    @Override
    public boolean hasStacksizeLimiters() {
        return true;
    }

    @Override
    public boolean isBlacklist() {
        return false;
    }

    @Override
    public SealToggle[] getToggles() {
        return props;
    }

    @Override
    public void setToggle(int indx, boolean value) {
        props[indx].setValue(value);
    }
}
