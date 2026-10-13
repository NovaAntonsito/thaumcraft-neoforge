package thaumcraft.common.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import thaumcraft.Thaumcraft;

/** Tags owned by the mod. */
public final class TCTags {
    /** Blocks that heat a crucible from below. Original: lava, fire, nitor and magma. */
    public static final TagKey<Block> CRUCIBLE_HEATERS = TagKey.create(Registries.BLOCK, Thaumcraft.id("crucible_heaters"));

    private TCTags() {
    }
}
