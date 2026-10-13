package thaumcraft.common.blocks.misc;

import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Partial port of BlockEffect: only effect_glimmer, the invisible light that the Lamplighter infusion
 * enchantment leaves behind. It behaves as air (replaceable, no collision, no drops) with light level 15.
 * TODO: effect_shock and effect_sap with their focus effects.
 */
public class BlockEffect extends AirBlock {
    public BlockEffect() {
        super(BlockBehaviour.Properties.of().replaceable().noCollission().noLootTable().air().lightLevel(s -> 15));
    }
}
