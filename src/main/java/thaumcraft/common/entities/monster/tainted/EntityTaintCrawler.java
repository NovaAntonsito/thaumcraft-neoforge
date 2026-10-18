package thaumcraft.common.entities.monster.tainted;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.entities.ITaintedMob;
import thaumcraft.common.blocks.world.taint.BlockTaintFibre;
import thaumcraft.common.blocks.world.taint.TaintHelper;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.registry.TCEntities;
import thaumcraft.common.registry.TCMobEffects;

/**
 * Port of EntityTaintCrawler: a small grub that crawls out of swollen taint. Every two seconds it leaves a taint fibre
 * where it stands, and its bite can give taint poison on normal and hard. It counts as an arthropod (entity tag).
 */
public class EntityTaintCrawler extends Monster implements ITaintedMob {
    public EntityTaintCrawler(EntityType<? extends EntityTaintCrawler> type, Level world) {
        super(type, world);
        xpReward = 3;
    }

    public EntityTaintCrawler(Level world) {
        this(TCEntities.TAINT_CRAWLER.get(), world);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 8.0).add(Attributes.MOVEMENT_SPEED, 0.275)
                .add(Attributes.ATTACK_DAMAGE, 2.0);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, false));
        goalSelector.addGoal(3, new RandomStrollGoal(this, 1.0));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0f));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return !(target instanceof ITaintedMob) && super.canAttack(target);
    }

    @Override
    public boolean isAlliedTo(Entity other) {
        return other instanceof ITaintedMob || super.isAlliedTo(other);
    }

    @Override
    public float getVoicePitch() {
        return 0.7f;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.SILVERFISH_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.SILVERFISH_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SILVERFISH_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        playSound(SoundEvents.SILVERFISH_STEP, 0.15f, 1.0f);
    }

    @Override
    protected MovementEmission getMovementEmission() {
        return MovementEmission.EVENTS;
    }

    @Override
    public void tick() {
        // The original compared lastPos with a fresh BlockPos by reference, so the check always passed; it is left out.
        if (!level().isClientSide && isAlive() && tickCount % 40 == 0) {
            BlockPos pos = blockPosition();
            BlockState bs = level().getBlockState(pos);
            if (!bs.is(net.minecraft.tags.BlockTags.LEAVES) && bs.getFluidState().isEmpty() && !TaintHelper.isTaint(bs)
                    && (bs.isAir() || bs.canBeReplaced() || bs.getBlock() instanceof BushBlock)
                    && TaintHelper.isAdjacentToSolidBlock(level(), pos) && !BlockTaintFibre.isOnlyAdjacentToTaint(level(), pos)) {
                level().setBlockAndUpdate(pos, ConfigBlocks.taintFibre.get().defaultBlockState());
            }
        }
        super.tick();
    }

    /** dropFewItems: a flux crystal one time in eight. */
    @Override
    protected void dropCustomDeathLoot(ServerLevel world, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(world, source, recentlyHit);
        if (random.nextInt(8) == 0) {
            spawnAtLocation(ConfigItems.crystalEssence.get().makeCrystal(Aspect.FLUX, 1), getBbHeight() / 2.0f);
        }
    }

    @Override
    public boolean doHurtTarget(Entity victim) {
        if (super.doHurtTarget(victim)) {
            if (victim instanceof LivingEntity living) {
                int b0 = 0;
                if (level().getDifficulty() == Difficulty.NORMAL) {
                    b0 = 3;
                } else if (level().getDifficulty() == Difficulty.HARD) {
                    b0 = 6;
                }
                if (b0 > 0 && random.nextInt(b0 + 1) > 2) {
                    living.addEffect(new MobEffectInstance(TCMobEffects.FLUX_TAINT, b0 * 20, 0));
                }
            }
            return true;
        }
        return false;
    }
}
