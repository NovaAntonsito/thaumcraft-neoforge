package thaumcraft.common.entities.monster;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import javax.annotation.Nullable;
import thaumcraft.api.entities.IEldritchMob;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.entities.monster.cult.EntityCultist;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.registry.TCEntities;

/**
 * The inhabited zombie ("Shambling Husk"): a corpse in crimson plate driven by the eldritch crab that took its head.
 * It drops nothing; when it dies the crab, helm and all, climbs out of it. Only one lives within 32 blocks.
 * 1.21: it does not turn into a drowned under water, as there were no drowned in 1.12.
 */
public class EntityInhabitedZombie extends Zombie implements IEldritchMob {
    public EntityInhabitedZombie(EntityType<? extends EntityInhabitedZombie> type, Level world) {
        super(type, world);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Zombie.createAttributes().add(Attributes.MAX_HEALTH, 30.0).add(Attributes.ATTACK_DAMAGE, 5.0)
                .add(Attributes.SPAWN_REINFORCEMENTS_CHANCE, 0.0);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, EntityCultist.class, true));
    }

    @Override
    protected boolean convertsInWater() {
        return false;
    }

    /** onKillEntity: it does not turn villagers into zombies. */
    @Override
    public boolean killedEntity(ServerLevel world, LivingEntity entity) {
        return true;
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance diff, MobSpawnType reason, @Nullable SpawnGroupData data) {
        float d = (level().getDifficulty() == Difficulty.HARD) ? 0.9f : 0.6f;
        setItemSlot(EquipmentSlot.HEAD, new ItemStack(ConfigItems.crimsonPlateHelm.get()));
        if (random.nextFloat() <= d) {
            setItemSlot(EquipmentSlot.CHEST, new ItemStack(ConfigItems.crimsonPlateChest.get()));
        }
        if (random.nextFloat() <= d) {
            setItemSlot(EquipmentSlot.LEGS, new ItemStack(ConfigItems.crimsonPlateLegs.get()));
        }
        return super.finalizeSpawn(level, diff, reason, data);
    }

    /** onDeath did nothing: no loot, no death event. */
    @Override
    public void die(DamageSource source) {
    }

    /** onDeathUpdate: the crab comes out at eye height with its helm on, and the husk is gone in a puff at once. */
    @Override
    protected void tickDeath() {
        if (!level().isClientSide && !isRemoved()) {
            EntityEldritchCrab crab = new EntityEldritchCrab(TCEntities.ELDRITCH_CRAB.get(), level());
            crab.moveTo(getX(), getY() + getEyeHeight(), getZ(), getYRot(), getXRot());
            crab.setHelm(true);
            level().addFreshEntity(crab);
            dropExperience(getKillCredit());
            level().broadcastEntityEvent(this, (byte) 60);
            remove(RemovalReason.KILLED);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundsTC.CRABTALK.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return SoundEvents.GENERIC_HURT;
    }

    /** getCanSpawnHere: no other inhabited zombie nearby. */
    public static boolean checkInhabitedZombieSpawnRules(EntityType<EntityInhabitedZombie> type, ServerLevelAccessor world,
            MobSpawnType reason, BlockPos pos, RandomSource rand) {
        return world.getEntitiesOfClass(EntityInhabitedZombie.class, new AABB(pos).inflate(32.0, 16.0, 32.0)).isEmpty()
                && Monster.checkMonsterSpawnRules(type, world, reason, pos, rand);
    }
}
