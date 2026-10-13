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
            // TODO: FXDispatcher.drawAlumentum and its generic sparkles (stage 10). Flames and smoke for now.
            for (double i = 0.0; i < 3.0; ++i) {
                double coeff = i / 3.0;
                double px = xo + (getX() - xo) * coeff;
                double py = yo + (getY() - yo) * coeff + getBbHeight() / 2.0f;
                double pz = zo + (getZ() - zo) * coeff;
                level().addParticle(ParticleTypes.FLAME, px, py, pz, 0.0125 * (random.nextFloat() - 0.5), 0.0125 * (random.nextFloat() - 0.5),
                        0.0125 * (random.nextFloat() - 0.5));
                level().addParticle(ParticleTypes.SMOKE, getX() + random.nextGaussian() * 0.2, getY() + random.nextGaussian() * 0.2,
                        getZ() + random.nextGaussian() * 0.2, 0.0, 0.0, 0.0);
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
