package thaumcraft.common.blocks.eldritch;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

/**
 * BlockEldritch meta 4: Glowing Crusted Stone. Light 12, hardness 2, resistance 30, no mob spawns.
 * (Meta 5, the Glyphed Stone, and the maze blocks come with the Outer Lands.)
 */
public class BlockEldritchStone extends Block {
    public BlockEldritchStone() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(2.0f, 30.0f).sound(SoundType.STONE)
                .lightLevel(s -> 12).requiresCorrectToolForDrops().isValidSpawn(BlockEldritch::never));
    }
}
