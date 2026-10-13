package thaumcraft.common.blocks.basic;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/** Port of BlockMetalTC. Beacon base is now the minecraft:beacon_base_blocks tag. */
public class BlockMetalTC extends Block {
    public BlockMetalTC(MapColor color) {
        super(BlockBehaviour.Properties.of()
                .mapColor(color)
                .requiresCorrectToolForDrops()
                .strength(4.0f, 10.0f)
                .sound(SoundType.METAL));
    }
}
