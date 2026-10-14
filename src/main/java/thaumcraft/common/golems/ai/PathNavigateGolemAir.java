package thaumcraft.common.golems.ai;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Port of PathNavigateGolemAir: flight paths measured from the middle of the golem.
 * The original FlightNodeProcessor is the vanilla flying node evaluator here.
 */
public class PathNavigateGolemAir extends FlyingPathNavigation {
    public PathNavigateGolemAir(Mob entity, Level level) {
        super(entity, level);
        setCanOpenDoors(false);
        setCanFloat(true);
    }

    @Override
    protected Vec3 getTempMobPos() {
        return new Vec3(mob.getX(), mob.getY() + mob.getBbHeight() * 0.5, mob.getZ());
    }
}
