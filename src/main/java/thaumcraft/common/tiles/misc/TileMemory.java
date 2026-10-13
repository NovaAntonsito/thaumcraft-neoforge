package thaumcraft.common.tiles.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.common.tiles.TileThaumcraft;

/**
 * A tile that remembers the block it replaced. 1.21: the old block is saved as a block state compound instead of
 * the numeric id and meta. Only saved, never synced.
 */
public abstract class TileMemory extends TileThaumcraft {
    public BlockState oldblock = Blocks.AIR.defaultBlockState();

    protected TileMemory(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    protected void loadAdditional(CompoundTag nbttagcompound, HolderLookup.Provider registries) {
        super.loadAdditional(nbttagcompound, registries);
        oldblock = NbtUtils.readBlockState(registries.lookupOrThrow(Registries.BLOCK), nbttagcompound.getCompound("oldblock"));
    }

    @Override
    protected void saveAdditional(CompoundTag nbttagcompound, HolderLookup.Provider registries) {
        super.saveAdditional(nbttagcompound, registries);
        nbttagcompound.put("oldblock", NbtUtils.writeBlockState(oldblock));
    }
}
