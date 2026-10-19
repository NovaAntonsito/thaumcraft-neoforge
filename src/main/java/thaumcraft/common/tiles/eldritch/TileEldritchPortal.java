package thaumcraft.common.tiles.eldritch;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.lib.research.ResearchManager;
import thaumcraft.common.registry.TCBlockEntities;
import thaumcraft.common.world.dim.OuterLands;
import thaumcraft.common.world.dim.TeleporterThaumcraft;

/**
 * Port of TC4's TileEldritchPortal. Clients play the portal hum every 250 ticks and grow the portal over its first
 * 30 ticks. Every 5 ticks the server sends players standing in it (not riding, not ridden) to the Outer Lands, or
 * from there back to the overworld; the first trip there completes ENTEROUTER. Standing in it keeps a 100 tick
 * portal cooldown going.
 */
public class TileEldritchPortal extends BlockEntity {
    public int opencount = -1;
    private int count = 0;

    public TileEldritchPortal(BlockPos pos, BlockState state) {
        super(TCBlockEntities.ELDRITCH_PORTAL.get(), pos, state);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, TileEldritchPortal tile) {
        ++tile.count;
        if (tile.count % 250 == 0 || tile.count == 0) {
            level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundsTC.EVILPORTAL.get(),
                    SoundSource.BLOCKS, 1.0f, 1.0f, false);
        }
        if (tile.opencount < 30) {
            ++tile.opencount;
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TileEldritchPortal tile) {
        ++tile.count;
        if (tile.count % 5 != 0 || !(level instanceof ServerLevel world)) {
            return;
        }
        List<ServerPlayer> ents = world.getEntitiesOfClass(ServerPlayer.class, new AABB(pos).inflate(0.5, 1.0, 0.5));
        for (ServerPlayer player : ents) {
            if (player.isPassenger() || player.isVehicle()) {
                continue;
            }
            if (player.isOnPortalCooldown()) {
                player.setPortalCooldown(100);
            } else if (!OuterLands.is(world)) {
                ServerLevel outer = world.getServer().getLevel(OuterLands.KEY);
                if (outer != null) {
                    player.setPortalCooldown(100);
                    player.changeDimension(TeleporterThaumcraft.transition(outer, player));
                    ResearchManager.startResearchWithPopup(player, "ENTEROUTER");
                    ResearchManager.completeResearch(player, "ENTEROUTER");
                }
            } else {
                player.setPortalCooldown(100);
                player.changeDimension(TeleporterThaumcraft.transition(world.getServer().overworld(), player));
            }
        }
    }
}
