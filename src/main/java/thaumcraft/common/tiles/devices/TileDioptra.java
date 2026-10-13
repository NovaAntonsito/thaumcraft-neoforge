package thaumcraft.common.tiles.devices;

import java.util.Arrays;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.common.blocks.devices.BlockDioptra;
import thaumcraft.common.registry.TCBlockEntities;
import thaumcraft.common.tiles.TileThaumcraft;
import thaumcraft.common.world.aura.AuraChunk;
import thaumcraft.common.world.aura.AuraHandler;

/**
 * Port of TileDioptra. Once a second it samples the 13x13 chunks around it and stores vis (or flux, when switched)
 * as heights from 0 to 64, which its renderer draws as a relief map. The center cell drives the comparator.
 */
public class TileDioptra extends TileThaumcraft {
    public int counter = 0;
    public byte[] grid_amt = new byte[169];

    public TileDioptra(BlockPos pos, BlockState state) {
        super(TCBlockEntities.DIOPTRA.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, TileDioptra te) {
        te.update();
    }

    private void update() {
        ++counter;
        if (!level.isClientSide) {
            if (counter % 20 == 0) {
                Arrays.fill(grid_amt, (byte) 0);
                boolean vis = getBlockState().getValue(BlockDioptra.ENABLED);
                for (int xx = 0; xx < 13; ++xx) {
                    for (int zz = 0; zz < 13; ++zz) {
                        AuraChunk ac = AuraHandler.getAuraChunk(level.dimension(), (worldPosition.getX() >> 4) + xx - 6,
                                (worldPosition.getZ() >> 4) + zz - 6);
                        if (ac != null) {
                            float amt = vis ? ac.getVis() : ac.getFlux();
                            grid_amt[xx + zz * 13] = (byte) Math.min(64.0f, amt / 500.0f * 64.0f);
                        }
                    }
                }
                setChanged();
                syncTile(false);
            }
        } else {
            counter = 0;
        }
    }

    @Override
    protected void readSyncNBT(CompoundTag tag, HolderLookup.Provider registries) {
        if (tag.contains("grid_a")) {
            byte[] g = tag.getByteArray("grid_a");
            if (g.length == 169) grid_amt = g;
        }
    }

    @Override
    protected void writeSyncNBT(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putByteArray("grid_a", grid_amt);
    }
}
