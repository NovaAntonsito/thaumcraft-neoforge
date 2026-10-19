package thaumcraft.common.blocks.eldritch;

import net.minecraft.core.BlockPos;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

/**
 * BlockEldritch metas 4 and 5: Glowing Crusted Stone and Glyphed Stone. Light 12, hardness 2, resistance 30, no mob
 * spawns. The glyphed stone drops 1-4 experience.
 */
public class BlockEldritchStone extends DropExperienceBlock {
    public BlockEldritchStone() {
        this(ConstantInt.of(0));
    }

    public BlockEldritchStone(IntProvider xp) {
        super(xp, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(2.0f, 30.0f).sound(SoundType.STONE)
                .lightLevel(s -> 12).requiresCorrectToolForDrops().isValidSpawn(BlockEldritchStone::never));
    }

    public static Boolean never(BlockState state, BlockGetter level, BlockPos pos, EntityType<?> entity) {
        return false;
    }
}
