package thaumcraft.common.tiles.devices;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.common.registry.TCBlockEntities;

/** Port of TileHungryChest: a vanilla chest with its own type, so it never pairs with its neighbors. */
public class TileHungryChest extends ChestBlockEntity {
    public TileHungryChest(BlockPos pos, BlockState state) {
        super(TCBlockEntities.HUNGRY_CHEST.get(), pos, state);
    }
}
