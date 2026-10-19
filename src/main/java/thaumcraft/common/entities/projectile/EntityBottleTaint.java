package thaumcraft.common.entities.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import thaumcraft.api.entities.ITaintedMob;
import thaumcraft.client.fx.ClientFX;
import thaumcraft.common.blocks.world.taint.TaintHelper;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.registry.TCEntities;
import thaumcraft.common.registry.TCMobEffects;

/** Port of EntityBottleTaint: a thrown bottle of taint. It poisons everything within 5 blocks and splashes flux goo around. */
public class EntityBottleTaint extends ThrowableItemProjectile {
    public EntityBottleTaint(EntityType<? extends EntityBottleTaint> type, Level world) {
        super(type, world);
    }

    public EntityBottleTaint(Level world, LivingEntity thrower) {
        super(TCEntities.BOTTLE_TAINT.get(), thrower, world);
    }

    public EntityBottleTaint(Level world, double x, double y, double z) {
        super(TCEntities.BOTTLE_TAINT.get(), x, y, z, world);
    }

    @Override
    protected Item getDefaultItem() {
        return ConfigItems.bottleTaint.get();
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 3) {
            for (int a = 0; a < 100; ++a) {
                thaumcraft.client.fx.FXDispatcher.taintsplosionFX(this);
            }
            thaumcraft.client.fx.FXDispatcher.bottleTaintBreak(getX(), getY(), getZ());
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    protected void onHit(HitResult ray) {
        super.onHit(ray);
        Level world = level();
        if (!world.isClientSide) {
            for (LivingEntity el : world.getEntitiesOfClass(LivingEntity.class, new AABB(getX(), getY(), getZ(), getX(), getY(), getZ()).inflate(5.0))) {
                if (!(el instanceof ITaintedMob) && !el.isInvertedHealAndHarm()) {
                    el.addEffect(new MobEffectInstance(TCMobEffects.FLUX_TAINT, 100, 0, false, true));
                }
            }
            for (int a = 0; a < 10; ++a) {
                int xx = (int) ((random.nextFloat() - random.nextFloat()) * 4.0f);
                int zz = (int) ((random.nextFloat() - random.nextFloat()) * 4.0f);
                BlockPos p = blockPosition().offset(xx, 0, zz);
                if (world.random.nextBoolean()) {
                    if (isNormalCube(world, p.below()) && TaintHelper.isReplaceable(world.getBlockState(p))) {
                        world.setBlockAndUpdate(p, ConfigBlocks.fluxGoo.get().defaultBlockState());
                    } else {
                        p = p.below();
                        if (isNormalCube(world, p.below()) && TaintHelper.isReplaceable(world.getBlockState(p))) {
                            world.setBlockAndUpdate(p, ConfigBlocks.fluxGoo.get().defaultBlockState());
                        }
                    }
                }
            }
            world.broadcastEntityEvent(this, (byte) 3);
            discard();
        }
    }

    /** World.isBlockNormalCube: a full, solid block. */
    private static boolean isNormalCube(Level world, BlockPos pos) {
        return world.getBlockState(pos).isCollisionShapeFullBlock(world, pos);
    }
}
