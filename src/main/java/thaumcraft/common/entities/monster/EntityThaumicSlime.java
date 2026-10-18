package thaumcraft.common.entities.monster;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.entities.ITaintedMob;
import thaumcraft.client.fx.ClientFX;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.registry.TCEntities;

/**
 * Port of EntityThaumicSlime: a slime of flux goo. It crawls out of flux goo and grows by soaking it up. Bigger than
 * size 2, it spits little slimes at players 4 to 16 blocks away, getting smaller each time. When it dies it breaks into
 * as many size 1 slimes as its size. It never spawns on its own.
 */
public class EntityThaumicSlime extends Slime implements ITaintedMob {
    int launched = 10;
    int spitCounter = 100;

    public EntityThaumicSlime(EntityType<? extends EntityThaumicSlime> type, Level world) {
        super(type, world);
        int i = 1 << 1 + random.nextInt(3);
        setSize(i, true);
    }

    public EntityThaumicSlime(Level world) {
        this(TCEntities.THAUMIC_SLIME.get(), world);
    }

    /** A size 1 slime spat by shooter at target. */
    public EntityThaumicSlime(Level world, LivingEntity shooter, LivingEntity target) {
        this(world);
        setSize(1, true);
        double y = (shooter.getBoundingBox().minY + shooter.getBoundingBox().maxY) / 2.0;
        double var6 = target.getX() - shooter.getX();
        double var7 = target.getBoundingBox().minY + target.getBbHeight() / 3.0f - y;
        double var8 = target.getZ() - shooter.getZ();
        double var9 = Math.sqrt(var6 * var6 + var8 * var8);
        if (var9 >= 1.0E-7) {
            float var10 = (float) (Math.atan2(var8, var6) * 180.0 / Math.PI) - 90.0f;
            float var11 = (float) (-(Math.atan2(var7, var9) * 180.0 / Math.PI));
            double var12 = var6 / var9;
            double var13 = var8 / var9;
            moveTo(shooter.getX() + var12, y, shooter.getZ() + var13, var10, var11);
            float var14 = (float) var9 * 0.2f;
            shoot(var6, var7 + var14, var8, 1.5f, 1.0f);
        } else {
            setPos(getX(), y, getZ());
        }
    }

    public void shoot(double par1, double par3, double par5, float par7, float par8) {
        double var9 = Math.sqrt(par1 * par1 + par3 * par3 + par5 * par5);
        par1 /= var9;
        par3 /= var9;
        par5 /= var9;
        par1 += random.nextGaussian() * 0.007499999832361937 * par8;
        par3 += random.nextGaussian() * 0.007499999832361937 * par8;
        par5 += random.nextGaussian() * 0.007499999832361937 * par8;
        par1 *= par7;
        par3 *= par7;
        par5 *= par7;
        setDeltaMovement(par1, par3, par5);
        double var10 = Math.sqrt(par1 * par1 + par5 * par5);
        float n = (float) (Math.atan2(par1, par5) * 180.0 / Math.PI);
        setYRot(n);
        yRotO = n;
        float n2 = (float) (Math.atan2(par3, var10) * 180.0 / Math.PI);
        setXRot(n2);
        xRotO = n2;
    }

    @Override
    public void setSize(int size, boolean resetHealth) {
        super.setSize(size, resetHealth);
        xpReward = size + 2;
    }

    @SuppressWarnings("unchecked")
    @Override
    public EntityType<? extends EntityThaumicSlime> getType() {
        return (EntityType<? extends EntityThaumicSlime>) super.getType();
    }

    /** slimeJumpFX instead of the vanilla slime bits when it lands. */
    @Override
    protected boolean spawnCustomParticles() {
        if (level().isClientSide) {
            for (int j = 0; j < getSize() * 2; ++j) {
                ClientFX.slimeJump(this);
            }
        }
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        int i = getSize();
        if (level().isClientSide) {
            if (launched > 0) {
                --launched;
                for (int k = 0; k < i * (launched + 1); ++k) {
                    ClientFX.slimeJump(this);
                }
            }
        } else if (isAlive()) {
            Player entityplayer = level().getNearestPlayer(this, 16.0);
            if (entityplayer != null) {
                if (spitCounter > 0) {
                    --spitCounter;
                }
                lookAt(entityplayer, 10.0f, 20.0f);
                if (distanceTo(entityplayer) > 4.0f && spitCounter <= 0 && getSize() > 2) {
                    spitCounter = 101;
                    level().addFreshEntity(new EntityThaumicSlime(level(), this, entityplayer));
                    playSound(SoundsTC.GORE.get(), 1.0f, ((random.nextFloat() - random.nextFloat()) * 0.2f + 1.0f) * 0.8f);
                    setSize(getSize() - 1, true);
                }
            }
        }
    }

    /** setDead: it breaks into as many size 1 slimes as its size (vanilla splits into 2 to 4 of half the size). */
    @Override
    public void remove(RemovalReason reason) {
        int i = getSize();
        if (!level().isClientSide && i > 1 && isDeadOrDying()) {
            for (int k = 0; k < i; ++k) {
                float f = (k % 2 - 0.5f) * i / 4.0f;
                float f2 = (k / 2 - 0.5f) * i / 4.0f;
                EntityThaumicSlime entityslime = new EntityThaumicSlime(level());
                entityslime.setSize(1, true);
                entityslime.moveTo(getX() + f, getY() + 0.5, getZ() + f2, random.nextFloat() * 360.0f, 0.0f);
                level().addFreshEntity(entityslime);
            }
            // So the vanilla split below does nothing.
            setSize(1, false);
        }
        super.remove(reason);
    }

    @Override
    protected float getAttackDamage() {
        return getSize() + 1;
    }

    /** canDamagePlayer: even the smallest ones hurt. */
    @Override
    protected boolean isDealsDamage() {
        return isEffectiveAi();
    }

    @Override
    protected void dealDamage(LivingEntity target) {
        int i = getSize();
        if (launched > 0) {
            i += 2;
        }
        if (isAlive() && hasLineOfSight(target) && distanceToSqr(target) < 0.6 * i * 0.6 * i
                && target.hurt(damageSources().mobAttack(this), getAttackDamage())) {
            playSound(SoundEvents.SLIME_ATTACK, 1.0f, (random.nextFloat() - random.nextFloat()) * 0.2f + 1.0f);
            if (level() instanceof ServerLevel sl) {
                net.minecraft.world.item.enchantment.EnchantmentHelper.doPostAttackEffects(sl, target, damageSources().mobAttack(this));
            }
        }
    }

    /** dropFewItems: a flux crystal, unless it is the smallest size. */
    @Override
    protected void dropCustomDeathLoot(ServerLevel world, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(world, source, recentlyHit);
        if (getSize() > 1) {
            spawnAtLocation(ConfigItems.crystalEssence.get().makeCrystal(Aspect.FLUX, 1), getBbHeight() / 2.0f);
        }
    }
}
