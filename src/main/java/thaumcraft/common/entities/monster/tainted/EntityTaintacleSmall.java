package thaumcraft.common.entities.monster.tainted;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import thaumcraft.common.registry.TCEntities;

/** Port of EntityTaintacleSmall ("Taint Tendril"): what taintacles and taint seeds raise. It withers after 10 seconds. */
public class EntityTaintacleSmall extends EntityTaintacle {
    int lifetime = 200;

    public EntityTaintacleSmall(EntityType<? extends EntityTaintacleSmall> type, Level world) {
        super(type, world);
        xpReward = 0;
    }

    public EntityTaintacleSmall(Level world) {
        this(TCEntities.TAINTACLE_SMALL.get(), world);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return net.minecraft.world.entity.monster.Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 5.0).add(Attributes.ATTACK_DAMAGE, 2.0)
                .add(Attributes.MOVEMENT_SPEED, 0.0);
    }

    @Override
    public void tick() {
        super.tick();
        if (lifetime-- <= 0) {
            hurt(damageSources().magic(), 10.0f);
        }
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel world, DamageSource source, boolean recentlyHit) {
    }
}
