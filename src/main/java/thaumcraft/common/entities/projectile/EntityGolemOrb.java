package thaumcraft.common.entities.projectile;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import thaumcraft.client.fx.ClientFX;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.registry.TCEntities;

/**
 * The energy orb of clerics, the praetor and the eldritch golem. It homes on its target, does magic damage based on
 * the thrower's attack and can be swatted back. Red orbs (cultists) hit harder and last longer.
 */
public class EntityGolemOrb extends ThrowableProjectile implements IEntityWithComplexSpawn {
    int targetID = 0;
    LivingEntity target;
    public boolean red = false;

    public EntityGolemOrb(EntityType<? extends EntityGolemOrb> type, Level world) {
        super(type, world);
    }

    public EntityGolemOrb(Level world, LivingEntity thrower, LivingEntity t, boolean r) {
        super(TCEntities.GOLEM_ORB.get(), thrower, world);
        target = t;
        red = r;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    protected double getDefaultGravity() {
        return 0.0;
    }

    @Override
    public void writeSpawnData(RegistryFriendlyByteBuf data) {
        data.writeInt(target != null ? target.getId() : -1);
        data.writeBoolean(red);
    }

    @Override
    public void readSpawnData(RegistryFriendlyByteBuf data) {
        int id = data.readInt();
        if (id >= 0 && level().getEntity(id) instanceof LivingEntity le) {
            target = le;
        }
        red = data.readBoolean();
    }

    @Override
    protected void onHit(HitResult mop) {
        Entity thrower = getOwner();
        if (!level().isClientSide && thrower instanceof LivingEntity lt && mop instanceof EntityHitResult ehr) {
            double dmg = lt.getAttribute(Attributes.ATTACK_DAMAGE) != null ? lt.getAttributeValue(Attributes.ATTACK_DAMAGE) : 0.0;
            ehr.getEntity().hurt(damageSources().indirectMagic(this, thrower), (float) dmg * (red ? 1.0f : 0.6f));
        }
        playSound(SoundsTC.SHOCK.get(), 1.0f, 1.0f + (random.nextFloat() - random.nextFloat()) * 0.2f);
        if (level().isClientSide) {
            thaumcraft.client.fx.FXDispatcher.burst(getX(), getY(), getZ(), 1.0f);
        }
        discard();
    }

    @Override
    public void tick() {
        super.tick();
        if (tickCount > (red ? 240 : 160)) {
            discard();
        }
        if (target != null) {
            // As in the original: the pull is divided by the squared distance.
            double d = distanceToSqr(target);
            double dx = target.getX() - getX();
            double dy = target.getBoundingBox().minY + target.getBbHeight() * 0.6 - getY();
            double dz = target.getZ() - getZ();
            double d2 = 0.2;
            dx /= d;
            dy /= d;
            dz /= d;
            Vec3 m = getDeltaMovement().add(dx * d2, dy * d2, dz * d2);
            setDeltaMovement(Mth.clamp((float) m.x, -0.25f, 0.25f), Mth.clamp((float) m.y, -0.25f, 0.25f), Mth.clamp((float) m.z, -0.25f, 0.25f));
        }
    }

    @Override
    public boolean hurt(DamageSource source, float damage) {
        if (isInvulnerableTo(source)) {
            return false;
        }
        if (source.getEntity() != null) {
            Vec3 vec3 = source.getEntity().getLookAngle();
            setDeltaMovement(vec3.scale(0.9));
            playSound(SoundsTC.ZAP.get(), 1.0f, 1.0f + (random.nextFloat() - random.nextFloat()) * 0.2f);
            return true;
        }
        return false;
    }
}
