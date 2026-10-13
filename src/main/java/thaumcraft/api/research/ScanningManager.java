package thaumcraft.api.research;

import java.util.ArrayList;

import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import thaumcraft.api.capabilities.ThaumcraftCapabilities;
import thaumcraft.common.lib.research.ResearchManager;

/** Port of thaumcraft.api.research.ScanningManager. */
public class ScanningManager {

    static final ArrayList<IScanThing> things = new ArrayList<>();

    /**
     * Add things to scan.
     * Example: ScanningManager.addScannableThing(new ScanItem("HIPSTER", new ItemStack(Items.APPLE)));
     * unlocks the HIPSTER research when an apple is scanned.
     */
    public static void addScannableThing(IScanThing obj) {
        things.add(obj);
    }

    /**
     * @param object the thaumcraft scanning tools only pass in an Entity, a BlockPos, an ItemStack or null
     */
    public static void scanTheThing(Player player, @Nullable Object object) {
        boolean found = false;
        boolean suppress = false;
        for (IScanThing thing : things) {
            if (thing.checkThing(player, object)) {
                String key = thing.getResearchKey(player, object);
                if (key == null || key.isEmpty() || ResearchManager.progressResearch(player, key)) {
                    if (key == null || key.isEmpty()) suppress = true;
                    found = true;
                    thing.onSuccess(player, object);
                }
            }
        }
        if (!suppress) {
            if (!found) {
                player.displayClientMessage(Component.translatable("tc.unknownobject").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC), true);
            } else {
                player.displayClientMessage(Component.translatable("tc.knownobject").withStyle(ChatFormatting.GREEN, ChatFormatting.ITALIC), true);
            }
        }

        // scan contents of inventories
        if (object instanceof BlockPos pos) {
            IItemHandler handler = player.level().getCapability(Capabilities.ItemHandler.BLOCK, pos, Direction.UP);
            if (handler != null) {
                int scanned = 0;
                for (int slot = 0; slot < handler.getSlots(); slot++) {
                    ItemStack stack = handler.getStackInSlot(slot);
                    if (!stack.isEmpty()) {
                        scanTheThing(player, stack);
                        scanned++;
                    }
                    if (scanned >= 100) {
                        player.displayClientMessage(Component.translatable("tc.invtoolarge").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC), true);
                        break; // to prevent lag with massive inventories
                    }
                }
            }
        }
    }

    /** @return true if the object can be scanned for research the player has not yet discovered */
    public static boolean isThingStillScannable(Player player, @Nullable Object object) {
        for (IScanThing thing : things) {
            if (thing.checkThing(player, object)) {
                try {
                    if (!ThaumcraftCapabilities.knowsResearch(player, thing.getResearchKey(player, object))) {
                        return true;
                    }
                } catch (Exception ignored) {
                }
            }
        }
        return false;
    }

    public static ItemStack getItemFromParms(Player player, @Nullable Object obj) {
        ItemStack is = ItemStack.EMPTY;
        if (obj instanceof ItemStack stack) is = stack;
        if (obj instanceof ItemEntity item) is = item.getItem();
        if (obj instanceof BlockPos pos) {
            BlockState state = player.level().getBlockState(pos);
            is = new ItemStack(state.getBlock().asItem());
            if (is.isEmpty()) {
                try {
                    is = state.getCloneItemStack(new net.minecraft.world.phys.BlockHitResult(pos.getCenter(), Direction.UP, pos, false),
                            player.level(), pos, player);
                } catch (Exception ignored) {
                }
            }
            // Water and Lava blocks cant be registered as itemstacks anymore? Oh Minecraft!
            if (is.isEmpty()) {
                FluidState fluid = state.getFluidState();
                if (fluid.is(FluidTags.WATER)) is = new ItemStack(Items.WATER_BUCKET);
                if (fluid.is(FluidTags.LAVA)) is = new ItemStack(Items.LAVA_BUCKET);
            }
        }
        return is;
    }
}
