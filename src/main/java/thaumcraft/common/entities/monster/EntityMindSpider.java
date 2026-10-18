package thaumcraft.common.entities.monster;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;
import thaumcraft.api.entities.IEldritchMob;

/**
 * Port of EntityMindSpider: a small ghostly spider with 1 health that only one player (the viewer) can see, if it has
 * one. A harmless one cannot bite, gives no experience and fades away after a minute. It drops nothing.
 * It is spawned by the warp events (not ported yet).
 */
public class EntityMindSpider extends Spider implements IEldritchMob {
    private static final EntityDataAccessor<Boolean> HARMLESS = SynchedEntityData.defineId(EntityMindSpider.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<String> VIEWER = SynchedEntityData.defineId(EntityMindSpider.class, EntityDataSerializers.STRING);
    private int lifeSpan = Integer.MAX_VALUE;

    public EntityMindSpider(EntityType<? extends EntityMindSpider> type, Level world) {
        super(type, world);
        xpReward = 1;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Spider.createAttributes().add(Attributes.MAX_HEALTH, 1.0).add(Attributes.ATTACK_DAMAGE, 1.0);
    }

    @Override
    protected ResourceKey<LootTable> getDefaultLootTable() {
        return BuiltInLootTables.EMPTY;
    }

    @Override
    protected int getBaseExperienceReward() {
        return isHarmless() ? 0 : super.getBaseExperienceReward();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(HARMLESS, false);
        builder.define(VIEWER, "");
    }

    public String getViewer() {
        return entityData.get(VIEWER);
    }

    public void setViewer(String player) {
        entityData.set(VIEWER, String.valueOf(player));
    }

    public boolean isHarmless() {
        return entityData.get(HARMLESS);
    }

    public void setHarmless(boolean h) {
        if (h) {
            lifeSpan = 1200;
        }
        entityData.set(HARMLESS, h);
    }

    @Override
    public float getVoicePitch() {
        return 0.7f;
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide && tickCount > lifeSpan) {
            discard();
        }
    }

    @Override
    public boolean isIgnoringBlockTriggers() {
        return true;
    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        return !isHarmless() && super.doHurtTarget(target);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        setHarmless(nbt.getBoolean("harmless"));
        setViewer(nbt.getString("viewer"));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putBoolean("harmless", isHarmless());
        nbt.putString("viewer", getViewer());
    }

    /** No spider jockeys or potion effects. */
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance diff, MobSpawnType reason, @Nullable SpawnGroupData data) {
        return data;
    }
}
