package thaumcraft.common.blocks.basic;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

/**
 * Port of BlockTranslucent (amber blocks). The translucent render layer is set in the block model
 * ("render_type": "minecraft:translucent"); beacon base is the minecraft:beacon_base_blocks tag.
 */
public class BlockTranslucent extends Block {
    public BlockTranslucent() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_ORANGE)
                .strength(0.5f)
                .sound(SoundType.STONE)
                .noOcclusion()
                .isViewBlocking((state, level, pos) -> false));
    }

    /** Same as shouldSideBeRendered in the original: hide faces between two blocks of the same kind. */
    @Override
    protected boolean skipRendering(BlockState state, BlockState adjacent, Direction side) {
        return adjacent.is(this) || super.skipRendering(state, adjacent, side);
    }
}
