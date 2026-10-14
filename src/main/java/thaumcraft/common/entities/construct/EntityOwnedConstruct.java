package thaumcraft.common.entities.construct;

import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.NameTagItem;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.PlayerTeam;
import thaumcraft.common.lib.SoundsTC;

/**
 * Port of EntityOwnedConstruct: a construct that belongs to a player. It only exists when spawned by its item
 * (validSpawn); anything else, like /summon, makes it vanish on the next tick.
 */
public class EntityOwnedConstruct extends PathfinderMob implements OwnableEntity {
    protected static final EntityDataAccessor<Byte> TAMED = SynchedEntityData.defineId(EntityOwnedConstruct.class, EntityDataSerializers.BYTE);
    protected static final EntityDataAccessor<Optional<UUID>> OWNER_UNIQUE_ID = SynchedEntityData.defineId(EntityOwnedConstruct.class,
            EntityDataSerializers.OPTIONAL_UUID);
    boolean validSpawn = false;

    protected EntityOwnedConstruct(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TAMED, (byte) 0);
        builder.define(OWNER_UNIQUE_ID, Optional.empty());
    }

    public boolean isOwned() {
        return (entityData.get(TAMED) & 0x4) != 0;
    }

    public void setOwned(boolean tamed) {
        byte b0 = entityData.get(TAMED);
        entityData.set(TAMED, tamed ? (byte) (b0 | 0x4) : (byte) (b0 & ~0x4));
    }

    @Nullable
    @Override
    public UUID getOwnerUUID() {
        return entityData.get(OWNER_UNIQUE_ID).orElse(null);
    }

    public void setOwnerId(@Nullable UUID id) {
        entityData.set(OWNER_UNIQUE_ID, Optional.ofNullable(id));
    }

    /** canBreatheUnderwater is final in 1.21: golems are also in the minecraft:can_breathe_under_water entity tag. */
    @Override
    protected int decreaseAirSupply(int air) {
        return air;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundsTC.CLACK.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return SoundsTC.CLACK.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundsTC.TOOL.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 240;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (getTarget() != null && isAlliedTo(getTarget())) {
            setTarget(null);
        }
        if (!level().isClientSide && !validSpawn) {
            discard();
        }
    }

    public void setValidSpawn() {
        validSpawn = true;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("v", validSpawn);
        tag.putString("OwnerUUID", getOwnerUUID() == null ? "" : getOwnerUUID().toString());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        validSpawn = tag.getBoolean("v");
        String s = tag.getString("OwnerUUID");
        if (!s.isEmpty()) {
            try {
                setOwnerId(UUID.fromString(s));
                setOwned(true);
            } catch (Throwable var4) {
                setOwned(false);
            }
        }
    }

    @Nullable
    public LivingEntity getOwnerEntity() {
        return getOwner();
    }

    public boolean isOwner(LivingEntity entityIn) {
        return entityIn != null && entityIn == getOwnerEntity();
    }

    @Nullable
    @Override
    public PlayerTeam getTeam() {
        if (isOwned()) {
            LivingEntity owner = getOwnerEntity();
            if (owner != null) {
                return owner.getTeam();
            }
        }
        return super.getTeam();
    }

    @Override
    public boolean isAlliedTo(Entity otherEntity) {
        if (isOwned()) {
            LivingEntity owner = getOwnerEntity();
            if (otherEntity == owner) {
                return true;
            }
            if (owner != null) {
                return owner.isAlliedTo(otherEntity);
            }
        }
        return super.isAlliedTo(otherEntity);
    }

    @Override
    public void die(DamageSource cause) {
        if (!level().isClientSide && level().getGameRules().getBoolean(GameRules.RULE_SHOWDEATHMESSAGES) && hasCustomName()
                && getOwnerEntity() instanceof ServerPlayer sp) {
            sp.sendSystemMessage(getCombatTracker().getDeathMessage());
        }
        super.die(cause);
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!isAlive()) {
            return InteractionResult.PASS;
        }
        if (player.isShiftKeyDown() || player.getMainHandItem().getItem() instanceof NameTagItem) {
            return InteractionResult.PASS;
        }
        if (!level().isClientSide && !isOwner(player)) {
            player.displayClientMessage(Component.translatable("tc.notowned").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC), true);
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }
}
