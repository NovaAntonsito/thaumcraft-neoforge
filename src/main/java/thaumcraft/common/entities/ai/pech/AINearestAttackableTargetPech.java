package thaumcraft.common.entities.ai.pech;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import thaumcraft.common.entities.monster.EntityPech;

/** Port of AINearestAttackableTargetPech: a pech only goes after players while it is angry. */
public class AINearestAttackableTargetPech<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {
    public AINearestAttackableTargetPech(Mob owner, Class<T> targetClass, boolean checkSight) {
        super(owner, targetClass, checkSight);
    }

    @Override
    public boolean canUse() {
        return (!(mob instanceof EntityPech pech) || pech.getAnger() != 0) && super.canUse();
    }
}
