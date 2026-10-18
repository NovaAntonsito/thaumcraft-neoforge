package thaumcraft.common.entities.monster.cult;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.loot.LootTable;
import javax.annotation.Nullable;
import thaumcraft.Thaumcraft;
import thaumcraft.client.fx.ClientFX;
import thaumcraft.common.registry.TCEntities;

/** The base of the crimson cult: knights, clerics and (stage 9d-4) the praetor. */
public class EntityCultist extends Monster {
    public static final ResourceKey<LootTable> LOOT = ResourceKey.create(Registries.LOOT_TABLE,
            ResourceLocation.fromNamespaceAndPath(Thaumcraft.MODID, "entities/cultist"));

    public EntityCultist(EntityType<? extends EntityCultist> type, Level world) {
        super(type, world);
        xpReward = 10;
        if (getNavigation() instanceof GroundPathNavigation nav) {
            nav.setCanOpenDoors(true);
        }
        setDropChance(EquipmentSlot.CHEST, 0.05f);
        setDropChance(EquipmentSlot.FEET, 0.05f);
        setDropChance(EquipmentSlot.HEAD, 0.05f);
        setDropChance(EquipmentSlot.LEGS, 0.05f);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, 4.0);
    }

    @Override
    protected ResourceKey<LootTable> getDefaultLootTable() {
        return LOOT;
    }

    /** setLoot of the original. */
    @Override
    protected void populateDefaultEquipmentSlots(net.minecraft.util.RandomSource random, DifficultyInstance diff) {
    }

    /** setEnchantmentBasedOnDifficulty of the original. */
    @Override
    protected void populateDefaultEquipmentEnchantments(ServerLevelAccessor level, net.minecraft.util.RandomSource random, DifficultyInstance diff) {
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance diff, MobSpawnType reason, @Nullable SpawnGroupData data) {
        populateDefaultEquipmentSlots(random, diff);
        populateDefaultEquipmentEnchantments(level, random, diff);
        return super.finalizeSpawn(level, diff, reason, data);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        if (nbt.contains("HomeD")) {
            restrictTo(new BlockPos(nbt.getInt("HomeX"), nbt.getInt("HomeY"), nbt.getInt("HomeZ")), nbt.getInt("HomeD"));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        if (hasRestriction() && getRestrictRadius() > 0.0f) {
            nbt.putInt("HomeD", (int) getRestrictRadius());
            nbt.putInt("HomeX", getRestrictCenter().getX());
            nbt.putInt("HomeY", getRestrictCenter().getY());
            nbt.putInt("HomeZ", getRestrictCenter().getZ());
        }
    }

    /** isOnSameTeam. TODO: EntityCultistLeader (stage 9d-4). */
    @Override
    public boolean isAlliedTo(Entity el) {
        return el instanceof EntityCultist || super.isAlliedTo(el);
    }

    /** canAttackClass. TODO: EntityCultistLeader (stage 9d-4). */
    @Override
    public boolean canAttackType(EntityType<?> type) {
        return type != TCEntities.CULTIST_CLERIC.get() && type != TCEntities.CULTIST_KNIGHT.get() && super.canAttackType(type);
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
                ClientFX.cultistSpawn(level(), getX() + random.nextFloat() * getBbWidth() * 2.0f - getBbWidth() + d0 * d4,
                        getY() + random.nextFloat() * getBbHeight() + d2 * d4, getZ() + random.nextFloat() * getBbWidth() * 2.0f - getBbWidth() + d3 * d4,
                        d0, d2, d3);
            }
        } else {
            level().broadcastEntityEvent(this, (byte) 20);
        }
    }
}
