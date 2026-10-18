package thaumcraft.common.entities.ai.pech;

import java.util.EnumSet;

import net.minecraft.world.entity.ai.goal.Goal;
import thaumcraft.common.entities.monster.EntityPech;

/** Port of AIPechTradePlayer: a tamed pech stands still while someone trades with it. */
public class AIPechTradePlayer extends Goal {
    private final EntityPech villager;

    public AIPechTradePlayer(EntityPech villager) {
        this.villager = villager;
        setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        return villager.isAlive() && !villager.isInWater() && villager.isTamed() && villager.onGround() && !villager.hurtMarked
                && villager.trading;
    }

    @Override
    public void start() {
        villager.getNavigation().stop();
    }

    @Override
    public void stop() {
        villager.trading = false;
    }
}
