package thaumcraft.common.blocks.world.ore;

import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;

/**
 * Port of BlockOreTC. Drops are now loot tables (data/thaumcraft/loot_table/blocks/ore_*.json);
 * the experience (1-4 for amber and quartz, none for cinnabar) is the DropExperienceBlock provider.
 */
public class BlockOreTC extends DropExperienceBlock {
    public BlockOreTC(float hardness, IntProvider xp) {
        super(xp, BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .instrument(NoteBlockInstrument.BASEDRUM)
                .requiresCorrectToolForDrops()
                .strength(hardness, 5.0f)
                .sound(SoundType.STONE));
    }

    /** Ores that drop an item instead of themselves give 1-4 xp. */
    public static IntProvider xpForGem() {
        return UniformInt.of(1, 4);
    }

    public static IntProvider noXp() {
        return ConstantInt.of(0);
    }
}
