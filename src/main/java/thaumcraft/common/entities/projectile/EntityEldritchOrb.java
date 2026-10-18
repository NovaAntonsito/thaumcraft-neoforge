package thaumcraft.common.entities.projectile;

import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import thaumcraft.common.registry.TCEntities;

/**
 * The orb an eldritch guardian hurls: it flies straight for 100 ticks and bursts on whatever it hits, doing magic
 * damage (two thirds of the thrower's attack) and weakness to every living thing within 2 blocks that is not undead.
 */
public class EntityEldritchOrb extends ThrowableProjectile {
    public EntityEldritchOrb(EntityType<? extends EntityEldritchOrb> type, Level world) {
        super(type, world);
    }

    public EntityEldritchOrb(Level world, LivingEntity p) {
        super(TCEntities.ELDRITCH_ORB.get(), p, world);
        shootFromRotation(p, p.getXRot(), p.getYRot(), -5.0f, 0.75f, 0.0f);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    protected double getDefaultGravity() {
        return 0.0;
    }

    @Override
    public void tick() {
        super.tick();
        if (tickCount > 100) {
            discard();
        }
    }

    @Override
    protected void onHit(HitResult mop) {
        Entity thrower = getOwner();
        if (!level().isClientSide && thrower != null) {
            double dmg = thrower instanceof LivingEntity lt && lt.getAttribute(Attributes.ATTACK_DAMAGE) != null
                    ? lt.getAttributeValue(Attributes.ATTACK_DAMAGE) : 0.0;
            for (Entity entity1 : level().getEntities(thrower, getBoundingBox().inflate(2.0, 2.0, 2.0))) {
                if (entity1 instanceof LivingEntity le && !le.isInvertedHealAndHarm()) {
                    le.hurt(damageSources().indirectMagic(this, thrower), (float) dmg * 0.666f);
                    le.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 160, 0));
                }
            }
            playSound(SoundEvents.LAVA_EXTINGUISH, 0.5f, 2.6f + (random.nextFloat() - random.nextFloat()) * 0.8f);
            discard();
        }
    }
}
