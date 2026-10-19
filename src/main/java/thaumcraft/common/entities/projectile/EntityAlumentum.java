package thaumcraft.common.entities.projectile;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.registry.TCEntities;

/** Port of EntityAlumentum: a thrown alumentum that explodes on impact, setting fires. */
public class EntityAlumentum extends ThrowableItemProjectile {
    public EntityAlumentum(EntityType<? extends EntityAlumentum> type, Level world) {
        super(type, world);
    }

    public EntityAlumentum(Level world, LivingEntity thrower) {
        super(TCEntities.ALUMENTUM.get(), thrower, world);
    }

    public EntityAlumentum(Level world, double x, double y, double z) {
        super(TCEntities.ALUMENTUM.get(), x, y, z, world);
    }

    @Override
    protected Item getDefaultItem() {
        return ConfigItems.alumentum.get();
    }

    /** The original ignored the requested velocity and always threw at 0.75. */
    @Override
    public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
        super.shoot(x, y, z, 0.75f, inaccuracy);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            for (double i = 0.0; i < 3.0; ++i) {
                double coeff = i / 3.0;
                thaumcraft.client.fx.FXDispatcher.drawAlumentum((float) (xo + (getX() - xo) * coeff), (float) (yo + (getY() - yo) * coeff) + getBbHeight() / 2.0f,
                        (float) (zo + (getZ() - zo) * coeff), 0.0125f * (random.nextFloat() - 0.5f), 0.0125f * (random.nextFloat() - 0.5f),
                        0.0125f * (random.nextFloat() - 0.5f), random.nextFloat() * 0.2f, random.nextFloat() * 0.1f, random.nextFloat() * 0.1f, 0.5f, 4.0f);
                thaumcraft.client.fx.FXDispatcher.drawGenericParticles(getX() + level().random.nextGaussian() * 0.20000000298023224,
                        getY() + level().random.nextGaussian() * 0.20000000298023224, getZ() + level().random.nextGaussian() * 0.20000000298023224, 0.0, 0.0, 0.0,
                        1.0f, 1.0f, 1.0f, 0.7f, false, 448, 8, 1, 8, 0, 0.3f, 0.0f, 1);
            }
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!level().isClientSide) {
            level().explode(this, getX(), getY(), getZ(), 1.1f, true, Level.ExplosionInteraction.TNT);
            discard();
        }
    }
}
