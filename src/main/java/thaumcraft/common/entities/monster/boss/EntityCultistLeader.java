package thaumcraft.common.entities.monster.boss;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import thaumcraft.client.fx.ClientFX;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.entities.ai.combat.AICultistHurtByTarget;
import thaumcraft.common.entities.ai.combat.AILongRangeAttack;
import thaumcraft.common.entities.monster.cult.EntityCultist;
import thaumcraft.common.entities.monster.mods.ChampionModifier;
import thaumcraft.common.entities.projectile.EntityGolemOrb;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.lib.utils.EntityUtils;
import thaumcraft.common.registry.TCEntities;
import thaumcraft.common.registry.TCAttributes;

/**
 * Port of EntityCultistLeader: the crimson praetor, the boss of the greater crimson portal. Praetor armor and a
 * crimson blade, a red energy orb from afar, and regeneration for the cultists around it. It drops a rare treasure
 * (no pearl: the portal drops that). As a champion it is named "Praetor [title] the [modifier]".
 */
public class EntityCultistLeader extends EntityThaumcraftBoss implements RangedAttackMob {
    private static final EntityDataAccessor<Byte> NAME = SynchedEntityData.defineId(EntityCultistLeader.class, EntityDataSerializers.BYTE);
    static final String[] titles = new String[] { "Alberic", "Anselm", "Bastian", "Beturian", "Chabier", "Chorache", "Chuse", "Dodorol", "Ebardo",
            "Ferrando", "Fertus", "Guillen", "Larpe", "Obano", "Zelipe" };

    public EntityCultistLeader(EntityType<? extends EntityCultistLeader> type, Level world) {
        super(type, world);
        xpReward = 40;
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new AILongRangeAttack(this, 16.0, 1.0, 30, 40, 24.0f));
        goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.1, false));
        goalSelector.addGoal(6, new MoveTowardsRestrictionGoal(this, 0.8));
        goalSelector.addGoal(7, new RandomStrollGoal(this, 0.8));
        goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0f));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new AICultistHurtByTarget(this, true));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return EntityThaumcraftBoss.createAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.32)
                .add(Attributes.MAX_HEALTH, 150.0)
                .add(Attributes.ATTACK_DAMAGE, 5.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(NAME, (byte) 0);
    }

    @Override
    public void generateName() {
        int t = (int) getAttributeValue(TCAttributes.CHAMPION_MOD);
        if (t >= 0) {
            setCustomName(Component.translatable("entity.thaumcraft.cultist_leader.custom", getTitle(), ChampionModifier.mods[t].getModNameLocalized()));
        }
    }

    private String getTitle() {
        return titles[entityData.get(NAME)];
    }

    private void setTitle(int title) {
        entityData.set(NAME, (byte) title);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putByte("title", entityData.get(NAME));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        setTitle(nbt.getByte("title"));
    }

    /**
     * setEquipmentBasedOnDifficulty: praetor helm, chest and greaves, crimson boots, and the crimson blade.
     * TODO: on easy it held the void sword, not ported yet (stage 9e); the crimson blade until then.
     */
    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
        setItemSlot(EquipmentSlot.HEAD, new ItemStack(ConfigItems.crimsonPraetorHelm.get()));
        setItemSlot(EquipmentSlot.CHEST, new ItemStack(ConfigItems.crimsonPraetorChest.get()));
        setItemSlot(EquipmentSlot.LEGS, new ItemStack(ConfigItems.crimsonPraetorLegs.get()));
        setItemSlot(EquipmentSlot.FEET, new ItemStack(ConfigItems.crimsonBoots.get()));
        setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ConfigItems.crimsonBlade.get()));
    }

    /** setEnchantmentBasedOnDifficulty: EnchantmentHelper.addRandomEnchantment(rand, held, 7 + f * rand(22), false). */
    @Override
    protected void populateDefaultEquipmentEnchantments(ServerLevelAccessor level, RandomSource random, DifficultyInstance diff) {
        float f = diff.getSpecialMultiplier();
        ItemStack held = getMainHandItem();
        if (!held.isEmpty() && random.nextFloat() < 0.5f * f) {
            var registry = level.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
            setItemInHand(InteractionHand.MAIN_HAND, EnchantmentHelper.enchantItem(random, held, (int) (7.0f + f * random.nextInt(22)),
                    level.registryAccess(), registry.getTag(EnchantmentTags.IN_ENCHANTING_TABLE)));
        }
    }

    /** isOnSameTeam. */
    @Override
    public boolean isAlliedTo(Entity el) {
        return el instanceof EntityCultist || el instanceof EntityCultistLeader;
    }

    /** canAttackClass. */
    @Override
    public boolean canAttackType(EntityType<?> type) {
        return type != TCEntities.CULTIST_CLERIC.get() && type != TCEntities.CULTIST_LEADER.get() && type != TCEntities.CULTIST_KNIGHT.get()
                && super.canAttackType(type);
    }

    /** dropFewItems: only the rare treasure. */
    @Override
    protected void dropCustomDeathLoot(ServerLevel world, DamageSource source, boolean recentlyHit) {
        spawnAtLocation(new ItemStack(ConfigItems.lootBagRare.get()), 1.5f);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance diff, MobSpawnType reason, @Nullable SpawnGroupData data) {
        populateDefaultEquipmentSlots(random, diff);
        populateDefaultEquipmentEnchantments(world, random, diff);
        setTitle(random.nextInt(titles.length));
        return super.finalizeSpawn(world, diff, reason, data);
    }

    /** updateAITasks: regeneration II for 3 seconds to the cultists within 8 blocks that have none. */
    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        List<EntityCultist> list = EntityUtils.getEntitiesInRange(level(), getX(), getY(), getZ(), this, EntityCultist.class, 8.0);
        for (EntityCultist e : list) {
            if (!e.hasEffect(MobEffects.REGENERATION)) {
                e.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 1));
            }
        }
    }

    @Override
    public void performRangedAttack(LivingEntity target, float f) {
        if (hasLineOfSight(target)) {
            swing(InteractionHand.MAIN_HAND);
            getLookControl().setLookAt(target.getX(), target.getBoundingBox().minY + target.getBbHeight() / 2.0f, target.getZ(), 30.0f, 30.0f);
            EntityGolemOrb blast = new EntityGolemOrb(level(), this, target, true);
            double d0 = target.getX() - getX();
            double d2 = target.getBoundingBox().minY + target.getBbHeight() / 2.0f - (getY() + getBbHeight() / 2.0f);
            double d3 = target.getZ() - getZ();
            blast.shoot(d0, d2 + 2.0, d3, 0.66f, 3.0f);
            playSound(SoundsTC.EGATTACK.get(), 1.0f, 1.0f + random.nextFloat() * 0.1f);
            level().addFreshEntity(blast);
        }
    }

    /** spawnExplosionParticle: the red puff of a cultist coming out of a portal. */
    @Override
    public void spawnAnim() {
        if (level().isClientSide) {
            for (int i = 0; i < 20; ++i) {
                double d0 = random.nextGaussian() * 0.05;
                double d2 = random.nextGaussian() * 0.05;
                double d3 = random.nextGaussian() * 0.05;
                double d4 = 2.0;
                thaumcraft.client.fx.FXDispatcher.cultistSpawn(getX() + random.nextFloat() * getBbWidth() * 2.0f - getBbWidth() + d0 * d4,
                        getY() + random.nextFloat() * getBbHeight() + d2 * d4, getZ() + random.nextFloat() * getBbWidth() * 2.0f - getBbWidth() + d3 * d4,
                        d0, d2, d3);
            }
        } else {
            level().broadcastEntityEvent(this, (byte) 20);
        }
    }
}
