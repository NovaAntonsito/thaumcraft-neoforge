package thaumcraft.common.golems.ai;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.Level;

/**
 * Port of PathNavigateGolemGround. The original copied the 1.12 ground navigator to plug in GolemNodeProcessor and
 * lift paths over cauldrons; 1.21's ground navigator already walks through doors and adjusts targets in the air or
 * inside blocks the same way.
 * TODO: GolemNodeProcessor's path costs (it keeps golems off rails and some hazards).
 */
public class PathNavigateGolemGround extends GroundPathNavigation {
    public PathNavigateGolemGround(Mob entity, Level level) {
        super(entity, level);
        setCanOpenDoors(false);
    }
}
