package thaumcraft.common.golems;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.NameTagItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import thaumcraft.Config;
import thaumcraft.api.capabilities.ThaumcraftCapabilities;
import thaumcraft.api.golems.EnumGolemTrait;
import thaumcraft.api.golems.IGolemAPI;
import thaumcraft.api.golems.IGolemProperties;
import thaumcraft.api.golems.tasks.Task;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.entities.construct.EntityOwnedConstruct;
import thaumcraft.common.golems.ai.AIFollowOwner;
import thaumcraft.common.golems.ai.AIGotoBlock;
import thaumcraft.common.golems.ai.AIGotoEntity;
import thaumcraft.common.golems.ai.AIGotoHome;
import thaumcraft.common.golems.ai.AIOwnerHurtByTarget;
import thaumcraft.common.golems.ai.AIOwnerHurtTarget;
import thaumcraft.common.golems.ai.PathNavigateGolemAir;
import thaumcraft.common.golems.ai.PathNavigateGolemGround;
import thaumcraft.common.golems.tasks.TaskHandler;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.registry.TCDataComponents;

/**
 * Port of EntityThaumcraftGolem. Its parts come from a GolemProperties long, synced split in two ints; a third int
 * holds the flag color (byte 0) and the state flags (byte 1: bit 1 following its owner, bit 3 in combat).
 */
public class EntityThaumcraftGolem extends EntityOwnedConstruct implements IGolemAPI, RangedAttackMob {
    int rankXp = 0;
    private static final EntityDataAccessor<Integer> PROPS1 = SynchedEntityData.defineId(EntityThaumcraftGolem.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> PROPS2 = SynchedEntityData.defineId(EntityThaumcraftGolem.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> PROPS3 = SynchedEntityData.defineId(EntityThaumcraftGolem.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Byte> CLIMBING = SynchedEntityData.defineId(EntityThaumcraftGolem.class, EntityDataSerializers.BYTE);
    public boolean redrawParts = false;
    private boolean firstRun = true;
    protected Task task = null;
    protected int taskID = Integer.MAX_VALUE;
    public static int XPM = 1000;

    public EntityThaumcraftGolem(EntityType<? extends EntityThaumcraftGolem> type, Level level) {
        super(type, level);
        xpReward = 5;
        // What the golem carries is dropped by dropCarried; the random equipment drop would duplicate it.
        setDropChance(EquipmentSlot.MAINHAND, 0.0f);
        setDropChance(EquipmentSlot.OFFHAND, 0.0f);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, 0.3).add(Attributes.MAX_HEALTH, 10.0)
                .add(Attributes.ATTACK_DAMAGE, 0.0).add(Attributes.FOLLOW_RANGE, 40.0);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(2, new AIGotoEntity(this));
        goalSelector.addGoal(3, new AIGotoBlock(this));
        goalSelector.addGoal(4, new AIGotoHome(this));
        goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0f));
        goalSelector.addGoal(6, new RandomLookAroundGoal(this));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(PROPS1, 0);
        builder.define(PROPS2, 0);
        builder.define(PROPS3, 0);
        builder.define(CLIMBING, (byte) 0);
    }

    @Override
    public IGolemProperties getProperties() {
        long l = ((long) entityData.get(PROPS1) << 32) | (entityData.get(PROPS2) & 0xFFFFFFFFL);
        return GolemProperties.fromLong(l);
    }

    @Override
    public void setProperties(IGolemProperties prop) {
        long l = prop.toLong();
        entityData.set(PROPS1, (int) (l >>> 32));
        entityData.set(PROPS2, (int) l);
    }

    @Override
    public byte getGolemColor() {
        return (byte) (entityData.get(PROPS3) & 0xFF);
    }

    public void setGolemColor(byte b) {
        entityData.set(PROPS3, (entityData.get(PROPS3) & ~0xFF) | (b & 0xFF));
    }

    public byte getFlags() {
        return (byte) ((entityData.get(PROPS3) >> 8) & 0xFF);
    }

    public void setFlags(byte b) {
        entityData.set(PROPS3, (entityData.get(PROPS3) & ~0xFF00) | ((b & 0xFF) << 8));
    }

    private void updateEntityAttributes() {
        IGolemProperties props = getProperties();
        int mh = 10 + props.getMaterial().healthMod;
        if (props.hasTrait(EnumGolemTrait.FRAGILE)) {
            mh = (int) (mh * 0.75);
        }
        mh += props.getRank();
        getAttribute(Attributes.MAX_HEALTH).setBaseValue(mh);
        getAttribute(Attributes.STEP_HEIGHT).setBaseValue(props.hasTrait(EnumGolemTrait.WHEELED) ? 0.5 : 0.6);
        restrictTo(getRestrictCenter().equals(BlockPos.ZERO) ? blockPosition() : getRestrictCenter(), props.hasTrait(EnumGolemTrait.SCOUT) ? 48 : 32);
        getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(props.hasTrait(EnumGolemTrait.SCOUT) ? 56.0 : 40.0);
        // getTotalArmorValue in 1.12.
        int armor = props.getMaterial().armor;
        if (props.hasTrait(EnumGolemTrait.ARMORED)) {
            armor = (int) Math.max(armor * 1.5, armor + 1);
        }
        if (props.hasTrait(EnumGolemTrait.FRAGILE)) {
            armor = (int) (armor * 0.75);
        }
        getAttribute(Attributes.ARMOR).setBaseValue(armor);
        navigation = getGolemNavigator();
        if (props.hasTrait(EnumGolemTrait.FLYER)) {
            moveControl = new FlyingMoveControl(this);
        }
        if (props.hasTrait(EnumGolemTrait.FIGHTER)) {
            double da = props.getMaterial().damage;
            if (props.hasTrait(EnumGolemTrait.BRUTAL)) {
                da = Math.max(da * 1.5, da + 1.0);
            }
            da += props.getRank() * 0.25;
            getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(da);
        } else {
            getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(0.0);
        }
        createAI();
    }

    private void createAI() {
        goalSelector.removeAllGoals(g -> true);
        targetSelector.removeAllGoals(g -> true);
        if (isFollowingOwner()) {
            goalSelector.addGoal(4, new AIFollowOwner(this, 1.0, 10.0f, 2.0f));
        } else {
            goalSelector.addGoal(3, new AIGotoEntity(this));
            goalSelector.addGoal(4, new AIGotoBlock(this));
            goalSelector.addGoal(5, new AIGotoHome(this));
        }
        goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0f));
        goalSelector.addGoal(9, new RandomLookAroundGoal(this));
        IGolemProperties props = getProperties();
        if (props.hasTrait(EnumGolemTrait.FIGHTER)) {
            if (getNavigation() instanceof GroundPathNavigation) {
                goalSelector.addGoal(0, new FloatGoal(this));
            }
            if (props.hasTrait(EnumGolemTrait.RANGED)) {
                Goal aa = null;
                if (props.getArms().function != null) {
                    aa = props.getArms().function.getRangedAttackAI(this);
                }
                if (aa != null) {
                    goalSelector.addGoal(1, aa);
                }
            }
            goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.15, false));
            if (isFollowingOwner()) {
                targetSelector.addGoal(1, new AIOwnerHurtByTarget(this));
                targetSelector.addGoal(2, new AIOwnerHurtTarget(this));
            }
            targetSelector.addGoal(3, new HurtByTargetGoal(this));
        }
    }

    @Override
    public boolean onClimbable() {
        return isBesideClimbableBlock();
    }

    /** onInitialSpawn in 1.12; the golem placer calls it directly. */
    public void onPlaced() {
        restrictTo(blockPosition(), 32);
        updateEntityAttributes();
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType,
                                        @Nullable SpawnGroupData spawnGroupData) {
        onPlaced();
        return spawnGroupData;
    }

    @Override
    public void aiStep() {
        updateSwingTime();
        super.aiStep();
    }

    @Override
    public void tick() {
        super.tick();
        IGolemProperties props = getProperties();
        if (props.hasTrait(EnumGolemTrait.FLYER)) {
            setNoGravity(true);
        }
        if (!level().isClientSide) {
            if (firstRun) {
                firstRun = false;
                if (hasRestriction() && !blockPosition().equals(getRestrictCenter())) {
                    goHome();
                }
            }
            if (task != null && task.isSuspended()) {
                task = null;
            }
            if (getTarget() != null && !getTarget().isAlive()) {
                setTarget(null);
            }
            if (getTarget() != null && props.hasTrait(EnumGolemTrait.RANGED) && distanceToSqr(getTarget()) > 1024.0) {
                setTarget(null);
            }
            if (level().getServer() != null && !level().getServer().isPvpAllowed() && getTarget() instanceof Player) {
                setTarget(null);
            }
            if (tickCount % (props.hasTrait(EnumGolemTrait.REPAIR) ? 40 : 100) == 0) {
                heal(1.0f);
            }
            if (props.hasTrait(EnumGolemTrait.CLIMBER)) {
                setBesideClimbableBlock(horizontalCollision);
            }
        } else if (tickCount < 20 || tickCount % 20 == 0) {
            redrawParts = true;
        }
        if (props.getHead().function != null) {
            props.getHead().function.onUpdateTick(this);
        }
        if (props.getArms().function != null) {
            props.getArms().function.onUpdateTick(this);
        }
        if (props.getLegs().function != null) {
            props.getLegs().function.onUpdateTick(this);
        }
        if (props.getAddon().function != null) {
            props.getAddon().function.onUpdateTick(this);
        }
    }

    /**
     * The emotes: 5 task taken, 6 task dropped, 7 unused, 8 told to stay, 9 rank up.
     * TODO (stage 10): the original FXDispatcher particles; vanilla particles stand in for now.
     */
    @Override
    public void handleEntityEvent(byte par1) {
        double y = getY() + getBbHeight() + 0.1;
        switch (par1) {
            case 5 -> level().addParticle(random.nextBoolean() ? ParticleTypes.HAPPY_VILLAGER : ParticleTypes.NOTE, getX(), y, getZ(), 0.0, 0.0, 0.0);
            case 6 -> level().addParticle(ParticleTypes.SMOKE, getX(), y, getZ(), 0.0, 0.025, 0.0);
            case 7 -> level().addParticle(ParticleTypes.ENCHANT, getX(), y, getZ(), 0.0, 0.05, 0.0);
            case 8 -> level().addParticle(ParticleTypes.ANGRY_VILLAGER, getX(), y, getZ(), 0.0, 0.01, 0.0);
            case 9 -> {
                for (int a = 0; a < 5; ++a) {
                    ParticleOptions p = ParticleTypes.END_ROD;
                    level().addParticle(p, getX(), getY() + getBbHeight(), getZ(), random.nextGaussian() * 0.01, random.nextFloat() * 0.02,
                            random.nextGaussian() * 0.01);
                }
            }
            default -> super.handleEntityEvent(par1);
        }
    }

    public float getGolemMoveSpeed() {
        IGolemProperties props = getProperties();
        return 1.0f + props.getRank() * 0.025f + (props.hasTrait(EnumGolemTrait.LIGHT) ? 0.2f : 0.0f)
                + (props.hasTrait(EnumGolemTrait.HEAVY) ? -0.175f : 0.0f) + (props.hasTrait(EnumGolemTrait.FLYER) ? -0.33f : 0.0f)
                + (props.hasTrait(EnumGolemTrait.WHEELED) ? 0.25f : 0.0f);
    }

    public PathNavigation getGolemNavigator() {
        IGolemProperties props = getProperties();
        return props.hasTrait(EnumGolemTrait.FLYER) ? new PathNavigateGolemAir(this, level())
                : props.hasTrait(EnumGolemTrait.CLIMBER) ? new WallClimberNavigation(this, level()) : new PathNavigateGolemGround(this, level());
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new PathNavigateGolemGround(this, level);
    }

    /** canTriggerWalking: only heavy golems make step sounds and trigger blocks they walk on. */
    @Override
    protected Entity.MovementEmission getMovementEmission() {
        IGolemProperties props = getProperties();
        return props.hasTrait(EnumGolemTrait.HEAVY) && !props.hasTrait(EnumGolemTrait.FLYER) ? Entity.MovementEmission.ALL : Entity.MovementEmission.NONE;
    }

    @Override
    public boolean causeFallDamage(float distance, float damageMultiplier, DamageSource source) {
        IGolemProperties props = getProperties();
        if (!props.hasTrait(EnumGolemTrait.FLYER) && !props.hasTrait(EnumGolemTrait.CLIMBER)) {
            return super.causeFallDamage(distance, damageMultiplier, source);
        }
        return false;
    }

    /** Teleports the golem back to its home, climbing up until it finds a ceiling, if it fits there. */
    private void goHome() {
        double d0 = getX();
        double d2 = getY();
        double d3 = getZ();
        double px = getRestrictCenter().getX() + 0.5;
        double py = getRestrictCenter().getY();
        double pz = getRestrictCenter().getZ() + 0.5;
        boolean flag = false;
        BlockPos blockpos = BlockPos.containing(px, py, pz);
        boolean flag2 = false;
        while (!flag2 && blockpos.getY() < level().getMaxBuildHeight()) {
            BlockPos blockpos2 = blockpos.above();
            BlockState iblockstate = level().getBlockState(blockpos2);
            if (iblockstate.blocksMotion()) {
                flag2 = true;
            } else {
                ++py;
                blockpos = blockpos2;
            }
        }
        if (flag2) {
            teleportTo(px, py, pz);
            if (level().noCollision(this, getBoundingBox())) {
                flag = true;
            }
        }
        if (!flag) {
            teleportTo(d0, d2, d3);
        } else {
            getNavigation().stop();
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        setProperties(GolemProperties.fromLong(nbt.getLong("props")));
        restrictTo(BlockPos.of(nbt.getLong("homepos")), 32);
        setFlags(nbt.getByte("gflags"));
        rankXp = nbt.getInt("rankXP");
        setGolemColor(nbt.getByte("color"));
        updateEntityAttributes();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putLong("props", getProperties().toLong());
        nbt.putLong("homepos", getRestrictCenter().asLong());
        nbt.putByte("gflags", getFlags());
        nbt.putInt("rankXP", rankXp);
        nbt.putByte("color", getGolemColor());
    }

    /** damageEntity in 1.12. */
    @Override
    protected void actuallyHurt(DamageSource ds, float damage) {
        IGolemProperties props = getProperties();
        if (ds.is(DamageTypeTags.IS_FIRE) && props.hasTrait(EnumGolemTrait.FIREPROOF)) {
            return;
        }
        if (ds.is(DamageTypeTags.IS_EXPLOSION) && props.hasTrait(EnumGolemTrait.BLASTPROOF)) {
            damage = Math.min(getMaxHealth() / 2.0f, damage * 0.3f);
        }
        if (ds.is(DamageTypes.CACTUS)) {
            return;
        }
        if (hasRestriction() && (ds.is(DamageTypes.IN_WALL) || ds.is(DamageTypes.FELL_OUT_OF_WORLD))) {
            goHome();
        }
        super.actuallyHurt(ds, damage);
    }

    /**
     * processInteract: sneaking picks the golem up as a placer, the bell (with GOLEMDIRECT) toggles follow/stay and a
     * dye paints its flag.
     */
    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!isAlive()) {
            return InteractionResult.PASS;
        }
        ItemStack held = player.getItemInHand(hand);
        if (held.getItem() instanceof NameTagItem) {
            return InteractionResult.PASS;
        }
        if (isOwner(player)) {
            if (!level().isClientSide) {
                if (player.isShiftKeyDown()) {
                    playSound(SoundsTC.ZAP.get(), 1.0f, 1.0f);
                    if (task != null) {
                        task.setReserved(false);
                    }
                    dropCarried();
                    ItemStack placer = new ItemStack(ConfigItems.golemPlacer.get());
                    placer.set(TCDataComponents.GOLEM_PROPS.get(), getProperties().toLong());
                    placer.set(TCDataComponents.GOLEM_XP.get(), rankXp);
                    spawnAtLocation(placer, 0.5f);
                    discard();
                    player.swing(hand, true);
                } else if (held.getItem() instanceof ItemGolemBell && ThaumcraftCapabilities.getKnowledge(player).isResearchKnown("GOLEMDIRECT")) {
                    if (task != null) {
                        task.setReserved(false);
                    }
                    playSound(SoundsTC.SCAN.get(), 1.0f, 1.0f);
                    setFollowingOwner(!isFollowingOwner());
                    if (isFollowingOwner()) {
                        player.displayClientMessage(Component.translatable("golem.follow"), true);
                        if (Config.SHOW_GOLEM_EMOTES.getAsBoolean()) {
                            level().broadcastEntityEvent(this, (byte) 5);
                        }
                        clearRestriction();
                    } else {
                        player.displayClientMessage(Component.translatable("golem.stay"), true);
                        if (Config.SHOW_GOLEM_EMOTES.getAsBoolean()) {
                            level().broadcastEntityEvent(this, (byte) 8);
                        }
                        restrictTo(blockPosition(), getProperties().hasTrait(EnumGolemTrait.SCOUT) ? 48 : 32);
                    }
                    updateEntityAttributes();
                    player.swing(hand, true);
                } else if (!held.isEmpty()) {
                    // The ore dictionary "dye*" names: the flag color is the dye's wool color + 1.
                    DyeColor dye = DyeColor.getColor(held);
                    if (dye != null) {
                        playSound(SoundsTC.ZAP.get(), 1.0f, 1.0f);
                        setGolemColor((byte) (dye.getId() + 1));
                        held.consume(1, player);
                        player.swing(hand, true);
                    }
                }
            }
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public void die(DamageSource cause) {
        if (task != null) {
            task.setReserved(false);
        }
        super.die(cause);
        if (!level().isClientSide) {
            dropCarried();
        }
    }

    protected void dropCarried() {
        for (ItemStack s : getCarrying()) {
            if (!s.isEmpty()) {
                spawnAtLocation(s, 0.25f);
            }
        }
    }

    /** dropFewItems: each component has a chance (better with looting) to drop, in part. */
    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        int looting = 0;
        if (source.getEntity() instanceof LivingEntity attacker) {
            looting = net.minecraft.world.item.enchantment.EnchantmentHelper.getEnchantmentLevel(
                    level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT)
                            .getHolderOrThrow(net.minecraft.world.item.enchantment.Enchantments.LOOTING), attacker);
        }
        float b = looting * 0.15f;
        for (ItemStack stack : getProperties().generateComponents()) {
            ItemStack s = stack.copy();
            if (random.nextFloat() < 0.3f + b) {
                if (s.getCount() > 0) {
                    s.shrink(random.nextInt(s.getCount()));
                }
                spawnAtLocation(s, 0.25f);
            }
        }
    }

    public boolean isBesideClimbableBlock() {
        return (entityData.get(CLIMBING) & 0x1) != 0;
    }

    public void setBesideClimbableBlock(boolean climbing) {
        byte b0 = entityData.get(CLIMBING);
        entityData.set(CLIMBING, climbing ? (byte) (b0 | 0x1) : (byte) (b0 & ~0x1));
    }

    private static boolean getBit(byte b, int bit) {
        return (b & (1 << bit)) != 0;
    }

    private static byte setBit(byte b, int bit, boolean on) {
        return (byte) (on ? b | (1 << bit) : b & ~(1 << bit));
    }

    public boolean isFollowingOwner() {
        return getBit(getFlags(), 1);
    }

    public void setFollowingOwner(boolean par1) {
        setFlags(setBit(getFlags(), 1, par1));
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        super.setTarget(target);
        setInCombat(getTarget() != null);
    }

    @Override
    public boolean isInCombat() {
        return getBit(getFlags(), 3);
    }

    public void setInCombat(boolean par1) {
        setFlags(setBit(getFlags(), 3, par1));
    }

    /**
     * attackEntityAsMob: the vanilla attack (damage, enchantments, knockback, fire), then the arm function. Deft golems
     * count as a player hit so the target drops its player-only loot (recentlyHit in 1.12).
     */
    @Override
    public boolean doHurtTarget(Entity ent) {
        boolean flag = super.doHurtTarget(ent);
        if (flag) {
            if (ent instanceof LivingEntity le && getProperties().hasTrait(EnumGolemTrait.DEFT) && getOwnerEntity() instanceof Player owner) {
                le.setLastHurtByPlayer(owner);
            }
            if (getProperties().getArms().function != null) {
                getProperties().getArms().function.onMeleeAttack(this, ent);
            }
            if (ent instanceof Mob && !ent.isAlive()) {
                addRankXp(8);
            }
        }
        return flag;
    }

    public Task getTask() {
        if (task == null && taskID != Integer.MAX_VALUE) {
            task = TaskHandler.getTask(level().dimension(), taskID);
            taskID = Integer.MAX_VALUE;
        }
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    @Override
    public void addRankXp(int xp) {
        if (!getProperties().hasTrait(EnumGolemTrait.SMART) || level().isClientSide) {
            return;
        }
        int rank = getProperties().getRank();
        if (rank < 10) {
            rankXp += xp;
            int xn = (rank + 1) * (rank + 1) * XPM;
            if (rankXp >= xn) {
                rankXp -= xn;
                IGolemProperties props = getProperties();
                props.setRank(rank + 1);
                setProperties(props);
                if (Config.SHOW_GOLEM_EMOTES.getAsBoolean()) {
                    level().broadcastEntityEvent(this, (byte) 9);
                    playSound(SoundEvents.PLAYER_LEVELUP, 0.25f, 1.0f);
                }
            }
        }
    }

    private int carrySlots() {
        return getProperties().hasTrait(EnumGolemTrait.HAULER) ? 2 : 1;
    }

    private static EquipmentSlot slot(int a) {
        return a == 0 ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
    }

    @Override
    public ItemStack holdItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return stack;
        }
        for (int a = 0; a < carrySlots(); ++a) {
            ItemStack in = getItemBySlot(slot(a));
            if (in.isEmpty()) {
                setItemSlot(slot(a), stack);
                return ItemStack.EMPTY;
            }
            if (in.getCount() < in.getMaxStackSize() && ItemStack.isSameItemSameComponents(in, stack)) {
                int d = Math.min(stack.getCount(), in.getMaxStackSize() - in.getCount());
                stack.shrink(d);
                in.grow(d);
                if (stack.getCount() <= 0) {
                    stack = ItemStack.EMPTY;
                }
            }
        }
        return stack;
    }

    @Override
    public ItemStack dropItem(ItemStack stack) {
        ItemStack out = ItemStack.EMPTY;
        for (int a = 0; a < carrySlots(); ++a) {
            ItemStack in = getItemBySlot(slot(a));
            if (!in.isEmpty()) {
                if (stack == null || stack.isEmpty()) {
                    out = in.copy();
                    setItemSlot(slot(a), ItemStack.EMPTY);
                } else if (ItemStack.isSameItemSameComponents(in, stack)) {
                    out = in.copy();
                    out.setCount(Math.min(stack.getCount(), out.getCount()));
                    in.shrink(stack.getCount());
                    if (in.getCount() <= 0) {
                        setItemSlot(slot(a), ItemStack.EMPTY);
                    }
                }
                if (!out.isEmpty()) {
                    break;
                }
            }
        }
        if (getProperties().hasTrait(EnumGolemTrait.HAULER) && getItemBySlot(EquipmentSlot.MAINHAND).isEmpty()
                && !getItemBySlot(EquipmentSlot.OFFHAND).isEmpty()) {
            setItemSlot(EquipmentSlot.MAINHAND, getItemBySlot(EquipmentSlot.OFFHAND).copy());
            setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
        }
        return out;
    }

    @Override
    public int canCarryAmount(ItemStack stack) {
        int ss = 0;
        for (int a = 0; a < carrySlots(); ++a) {
            ItemStack in = getItemBySlot(slot(a));
            if (in.isEmpty()) {
                // An empty slot holds a full stack (ItemStack.EMPTY had a max size of 64 in 1.12).
                ss += stack == null || stack.isEmpty() ? 64 : stack.getMaxStackSize();
            } else if (stack != null && ItemStack.isSameItemSameComponents(in, stack)) {
                ss += in.getMaxStackSize() - in.getCount();
            }
        }
        return ss;
    }

    @Override
    public boolean canCarry(ItemStack stack, boolean partial) {
        int ca = canCarryAmount(stack);
        if (ca > 0) {
            return partial || stack == null || ca >= stack.getCount();
        }
        return false;
    }

    @Override
    public boolean isCarrying(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        for (int a = 0; a < carrySlots(); ++a) {
            ItemStack in = getItemBySlot(slot(a));
            if (!in.isEmpty() && ItemStack.isSameItemSameComponents(in, stack)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public NonNullList<ItemStack> getCarrying() {
        if (getProperties().hasTrait(EnumGolemTrait.HAULER)) {
            NonNullList<ItemStack> stacks = NonNullList.withSize(2, ItemStack.EMPTY);
            stacks.set(0, getItemBySlot(EquipmentSlot.MAINHAND));
            stacks.set(1, getItemBySlot(EquipmentSlot.OFFHAND));
            return stacks;
        }
        return NonNullList.withSize(1, getItemBySlot(EquipmentSlot.MAINHAND));
    }

    @Override
    public LivingEntity getGolemEntity() {
        return this;
    }

    @Override
    public Level getGolemWorld() {
        return level();
    }

    @Override
    public void swingArm() {
        swing(InteractionHand.MAIN_HAND);
    }

    @Override
    public void performRangedAttack(LivingEntity target, float range) {
        if (getProperties().getArms().function != null) {
            getProperties().getArms().function.onRangedAttack(this, target, range);
        }
    }

    /** Flight: pushes the golem towards the wanted position and faces where it goes (or its target). */
    class FlyingMoveControl extends MoveControl {
        FlyingMoveControl(EntityThaumcraftGolem golem) {
            super(golem);
        }

        @Override
        public void tick() {
            if (operation == MoveControl.Operation.MOVE_TO) {
                double d0 = wantedX - getX();
                double d2 = wantedY - getY();
                double d3 = wantedZ - getZ();
                double d4 = Math.sqrt(d0 * d0 + d2 * d2 + d3 * d3);
                if (d4 < getBoundingBox().getSize()) {
                    operation = MoveControl.Operation.WAIT;
                    setDeltaMovement(getDeltaMovement().scale(0.5));
                } else {
                    setDeltaMovement(getDeltaMovement().add(d0 / d4 * 0.033 * speedModifier, d2 / d4 * 0.0125 * speedModifier,
                            d3 / d4 * 0.033 * speedModifier));
                    Vec3 m = getDeltaMovement();
                    if (getTarget() == null) {
                        setYRot(-(float) Mth.atan2(m.x, m.z) * Mth.RAD_TO_DEG);
                    } else {
                        double d5 = getTarget().getX() - EntityThaumcraftGolem.this.getX();
                        double d6 = getTarget().getZ() - EntityThaumcraftGolem.this.getZ();
                        setYRot(-(float) Mth.atan2(d5, d6) * Mth.RAD_TO_DEG);
                    }
                    yBodyRot = getYRot();
                }
            }
        }
    }
}
