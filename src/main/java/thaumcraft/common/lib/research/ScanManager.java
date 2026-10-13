package thaumcraft.common.lib.research;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.network.PacketDistributor;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectHelper;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.capabilities.PlayerAspects;
import thaumcraft.api.capabilities.ThaumcraftCapabilities;
import thaumcraft.api.research.ScanningManager;
import thaumcraft.common.lib.network.playerdata.PacketAspectDiscoveryError;

/** Reborn's ScanManager: what a scan reveals, and whether the player knows enough to understand it. */
public final class ScanManager {
    private ScanManager() {
    }

    /** The aspects of an entity, or of the item behind a stack, a dropped item or a block. */
    @Nullable
    public static AspectList objScanAspects(Player player, @Nullable Object obj) {
        if (obj == null) return new AspectList();
        if (obj instanceof Entity e && !(obj instanceof ItemEntity)) {
            return AspectHelper.getEntityAspects(e);
        }
        ItemStack is = ScanningManager.getItemFromParms(player, obj);
        return is.isEmpty() ? new AspectList() : AspectHelper.getObjectAspects(is);
    }

    /**
     * A thing can be scanned only when the player knows the components of all its aspects; otherwise the first
     * missing one is named in a notification. Blocks with an inventory are refused, so their contents are not
     * scanned along with them.
     */
    public static boolean canScanThing(Player player, @Nullable Object thing, boolean notify) {
        AspectList al = objScanAspects(player, thing);
        if (al == null) return true;
        if (thing instanceof BlockPos pos
                && player.level().getCapability(Capabilities.ItemHandler.BLOCK, pos, Direction.UP) != null) {
            return false;
        }
        PlayerAspects known = ThaumcraftCapabilities.getAspects(player);
        for (Aspect aspect : al.getAspects()) {
            if (aspect.getComponents() != null) {
                for (Aspect component : aspect.getComponents()) {
                    if (!known.isKnowAspect(component)) {
                        if (notify && player instanceof ServerPlayer sp) {
                            PacketDistributor.sendToPlayer(sp, new PacketAspectDiscoveryError(component.getTag()));
                        }
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
