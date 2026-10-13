package thaumcraft.api.casters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * This class serves a similar function to IInteractWithCaster in that it allows casters to interact
 * with object in the world. In this case it is most useful for adding interaction with non-mod
 * blocks where you can't control what happens in their code.
 *
 * @author azanor
 */
public class CasterTriggerRegistry {

    private static final HashMap<String, LinkedHashMap<BlockState, List<Trigger>>> triggers = new HashMap<>();
    private static final String DEFAULT = "default";

    private static class Trigger {
        ICasterTriggerManager manager;
        int event;

        public Trigger(ICasterTriggerManager manager, int event) {
            this.manager = manager;
            this.event = event;
        }
    }

    /**
     * Registers an action to perform when a caster right clicks on a specific block.
     * A manager class needs to be created that implements ICasterTriggerManager.
     * @param event a logical number that you can use to differentiate different events or actions
     * @param modid a unique identifier. It is best to register your own triggers using your mod id to avoid conflicts with mods that register triggers for the same block
     */
    public static void registerWandBlockTrigger(ICasterTriggerManager manager, int event, BlockState state, String modid) {
        LinkedHashMap<BlockState, List<Trigger>> temp = triggers.computeIfAbsent(modid, k -> new LinkedHashMap<>());
        temp.computeIfAbsent(state, k -> new ArrayList<>()).add(new Trigger(manager, event));
    }

    /**
     * for legacy support
     */
    public static void registerCasterBlockTrigger(ICasterTriggerManager manager, int event, BlockState state) {
        registerWandBlockTrigger(manager, event, state, DEFAULT);
    }

    /**
     * Checks all trigger registries if one exists for the given block state
     */
    public static boolean hasTrigger(BlockState state) {
        for (LinkedHashMap<BlockState, List<Trigger>> temp : triggers.values()) {
            if (temp.containsKey(state)) return true;
        }
        return false;
    }

    /**
     * modid sensitive version
     */
    public static boolean hasTrigger(BlockState state, String modid) {
        return triggers.containsKey(modid) && triggers.get(modid).containsKey(state);
    }

    /**
     * This is called by the onItemUseFirst function in casters.
     */
    public static boolean performTrigger(Level world, ItemStack casterStack, Player player, BlockPos pos, Direction side, BlockState state) {
        for (LinkedHashMap<BlockState, List<Trigger>> temp : triggers.values()) {
            List<Trigger> l = temp.get(state);
            if (l == null || l.isEmpty()) continue;
            for (Trigger trig : l) {
                if (trig.manager.performTrigger(world, casterStack, player, pos, side, trig.event)) return true;
            }
        }
        return false;
    }

    /**
     * modid sensitive version
     */
    public static boolean performTrigger(Level world, ItemStack casterStack, Player player, BlockPos pos, Direction side, BlockState state, String modid) {
        if (!triggers.containsKey(modid)) return false;
        List<Trigger> l = triggers.get(modid).get(state);
        if (l == null || l.isEmpty()) return false;
        for (Trigger trig : l) {
            if (trig.manager.performTrigger(world, casterStack, player, pos, side, trig.event)) return true;
        }
        return false;
    }
}
