package thaumcraft.common.tiles.eldritch;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.api.capabilities.PlayerWarp;
import thaumcraft.common.lib.InternalMethodHandler;
import thaumcraft.common.lib.network.fx.PacketFXBlockZap;
import thaumcraft.common.network.TCNetwork;
import thaumcraft.common.registry.TCBlockEntities;

/**
 * Port of TC4's TileEldritchTrap, the Runed Stone. Every 10 to 35 ticks it zaps the closest player within 3 blocks
 * for 2 magic damage, and half the time gives them 1-2 temporary warp.
 */
public class TileEldritchTrap extends BlockEntity {
    int count = 20;

    public TileEldritchTrap(BlockPos pos, BlockState state) {
        super(TCBlockEntities.ELDRITCH_TRAP.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TileEldritchTrap tile) {
        if (tile.count-- <= 0) {
            tile.count = 10 + level.random.nextInt(25);
            Player p = level.getNearestPlayer(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 3.0, false);
            if (p != null) {
                p.hurt(level.damageSources().magic(), 2.0F);
                if (level.random.nextBoolean()) {
                    InternalMethodHandler.addWarpToPlayer(p, 1 + level.random.nextInt(2), PlayerWarp.EnumWarpType.TEMPORARY);
                }

                TCNetwork.sendToAllAround(level, pos.getX(), pos.getY(), pos.getZ(), 32.0,
                        new PacketFXBlockZap(pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F, (float) p.getX(),
                                (float) p.getBoundingBox().minY + p.getEyeHeight(), (float) p.getZ()));
            }
        }
    }
}
