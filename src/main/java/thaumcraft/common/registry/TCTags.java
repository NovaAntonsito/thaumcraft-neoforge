package thaumcraft.common.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import thaumcraft.Thaumcraft;

/** Tags owned by the mod. */
public final class TCTags {
    /** Blocks that heat a crucible from below. Original: lava, fire, nitor and magma. */
    public static final TagKey<Block> CRUCIBLE_HEATERS = TagKey.create(Registries.BLOCK, Thaumcraft.id("crucible_heaters"));

    private TCTags() {
    }

    /**
     * A CRUCIBLE_HEATERS block that is actually giving heat. Create's blaze burner is in the tag (when Create is
     * installed) but only heats while lit: its "blaze" state must not be none or smouldering. Read by property name
     * so there is no compile dependency on Create.
     */
    public static boolean isHeating(BlockState state) {
        if (!state.is(CRUCIBLE_HEATERS)) return false;
        for (Property<?> p : state.getProperties()) {
            if (p.getName().equals("blaze")) {
                String level = valueName(state, p);
                return !level.equals("none") && !level.equals("smouldering");
            }
        }
        return true;
    }

    private static <T extends Comparable<T>> String valueName(BlockState state, Property<T> p) {
        return p.getName(state.getValue(p));
    }
}
