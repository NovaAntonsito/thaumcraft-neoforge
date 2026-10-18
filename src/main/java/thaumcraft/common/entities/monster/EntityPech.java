package thaumcraft.common.entities.monster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveThroughVillageGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.NameTagItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.casters.FocusEngine;
import thaumcraft.api.casters.FocusMediumRoot;
import thaumcraft.api.casters.FocusPackage;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.container.ContainerPech;
import thaumcraft.common.entities.ai.pech.AINearestAttackableTargetPech;
import thaumcraft.common.entities.ai.pech.AIPechItemEntityGoto;
import thaumcraft.common.entities.ai.pech.AIPechTradePlayer;
import thaumcraft.common.items.casters.foci.FocusEffectAir;
import thaumcraft.common.items.casters.foci.FocusEffectCurse;
import thaumcraft.common.items.casters.foci.FocusEffectEarth;
import thaumcraft.common.items.casters.foci.FocusEffectFire;
import thaumcraft.common.items.casters.foci.FocusEffectFlux;
import thaumcraft.common.items.casters.foci.FocusMediumProjectile;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.lib.crafting.ThaumcraftCraftingManager;
import thaumcraft.common.lib.utils.InventoryUtils;
import thaumcraft.common.registry.TCEntities;

/**
 * Port of EntityPech: a little magical-forest humanoid with a huge backpack. There are three kinds, picked by what it
 * spawns holding: foragers (0), mages with a pech wand that cast random foci (1) and stalkers with a bow (2).
 * <p>
 * Pechs avoid players and loot whatever lies around, keeping it in their pack (9 slots). Hitting one angers every
 * pech within 32 blocks. Throwing them something valued (see valuedItems, or anything with more than 1 Desire) may
 * tame them, and a tamed pech trades: right click to open the trade window.
 */
public class EntityPech extends Monster implements RangedAttackMob {
    private static final EntityDataAccessor<Byte> TYPE = SynchedEntityData.defineId(EntityPech.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Integer> ANGER = SynchedEntityData.defineId(EntityPech.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> TAMED = SynchedEntityData.defineId(EntityPech.class, EntityDataSerializers.BOOLEAN);

    /** The things pechs value most, and how much (Item.getIdFromItem keys in 1.12). Filled by ConfigEntities. */
    public static final HashMap<Item, Integer> valuedItems = new HashMap<>();
    /** Per pech type, the trades: {value 1 to 5, stack}. Filled by ConfigEntities. */
    public static final HashMap<Integer, ArrayList<List<Object>>> tradeInventory = new HashMap<>();

    public NonNullList<ItemStack> loot = NonNullList.withSize(9, ItemStack.EMPTY);
    public boolean trading = false;
    private final RangedAttackGoal aiArrowAttack = new RangedAttackGoal(this, 0.6, 20, 50, 15.0f);
    private final RangedAttackGoal aiBlastAttack = new RangedAttackGoal(this, 0.6, 20, 50, 15.0f);
    private final MeleeAttackGoal aiMeleeAttack = new MeleeAttackGoal(this, 0.6, false);
    private final AvoidEntityGoal<Player> aiAvoidPlayer = new AvoidEntityGoal<>(this, Player.class, 8.0f, 0.5, 0.6);
    public float mumble = 0.0f;
    int chargecount = 0;

    public EntityPech(EntityType<? extends EntityPech> type, Level world) {
        super(type, world);
        xpReward = 8;
    }

    public EntityPech(Level world) {
        this(TCEntities.PECH.get(), world);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 30.0).add(Attributes.ATTACK_DAMAGE, 6.0)
                .add(Attributes.MOVEMENT_SPEED, 0.5);
    }

    /** getName: the kind of pech, unless it has a name. */
    @Override
    protected Component getTypeName() {
        return switch (getPechType()) {
            case 1 -> Component.translatable("entity.thaumcraft.pech.1");
            case 2 -> Component.translatable("entity.thaumcraft.pech.2");
            default -> Component.translatable("entity.thaumcraft.pech");
        };
    }

    @Override
    protected void registerGoals() {
        if (getNavigation() instanceof GroundPathNavigation nav) {
            nav.setCanFloat(false);
        }
        setDropChance(EquipmentSlot.MAINHAND, 0.2f);
        setDropChance(EquipmentSlot.OFFHAND, 0.2f);
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new AIPechTradePlayer(this));
        goalSelector.addGoal(3, new AIPechItemEntityGoto(this));
        goalSelector.addGoal(5, new OpenDoorGoal(this, true));
        goalSelector.addGoal(6, new MoveTowardsRestrictionGoal(this, 0.5));
        goalSelector.addGoal(6, new MoveThroughVillageGoal(this, 1.0, false, 4, () -> false));
        goalSelector.addGoal(9, new RandomStrollGoal(this, 0.6));
        goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 3.0f, 1.0f));
        goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0f));
        goalSelector.addGoal(11, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new AINearestAttackableTargetPech(this, Player.class, true));
    }

    /** Bow: arrows. Pech wand: blasts. Anything else: melee. Untamed pechs also keep away from players. */
    public void setCombatTask() {
        if (level() != null && !level().isClientSide) {
            goalSelector.removeGoal(aiMeleeAttack);
            goalSelector.removeGoal(aiArrowAttack);
            goalSelector.removeGoal(aiBlastAttack);
            ItemStack itemstack = getMainHandItem();
            if (itemstack.is(Items.BOW)) {
                goalSelector.addGoal(2, aiArrowAttack);
            } else if (itemstack.is(ConfigItems.pechWand.get())) {
                goalSelector.addGoal(2, aiBlastAttack);
            } else {
                goalSelector.addGoal(2, aiMeleeAttack);
            }
            if (isTamed()) {
                goalSelector.removeGoal(aiAvoidPlayer);
            } else {
                goalSelector.addGoal(4, aiAvoidPlayer);
            }
        }
    }

    /** attackEntityWithRangedAttack: stalkers shoot (one arrow in five poisons), mages cast a random focus. */
    @Override
    public void performRangedAttack(LivingEntity target, float f) {
        if (getPechType() == 2) {
            Arrow entityarrow = new Arrow(level(), this, new ItemStack(Items.ARROW), getMainHandItem());
            if (level().random.nextFloat() < 0.2) {
                entityarrow.addEffect(new MobEffectInstance(MobEffects.POISON, 40));
            }
            double d0 = target.getX() - getX();
            double d2 = target.getBoundingBox().minY + target.getBbHeight() / 3.0f - entityarrow.getY();
            double d3 = target.getZ() - getZ();
            double d4 = Math.sqrt(d0 * d0 + d3 * d3);
            int difficulty = level().getDifficulty().getId();
            entityarrow.shoot(d0, d2 + d4 * 0.20000000298023224, d3, 1.6f, (float) (14 - difficulty * 4));
            // Power, Punch and Flame of the bow now come from the arrow's weapon.
            entityarrow.setBaseDamage(f * 2.0f + random.nextGaussian() * 0.25 + difficulty * 0.11f);
            playSound(SoundEvents.ARROW_SHOOT, 1.0f, 1.0f / (getRandom().nextFloat() * 0.4f + 0.8f));
            level().addFreshEntity(entityarrow);
        } else if (getPechType() == 1) {
            FocusPackage p = new FocusPackage(this);
            FocusMediumRoot root = new FocusMediumRoot();
            double off = distanceTo(target) / 6.0f;
            root.setupFromCasterToTarget(this, target, off);
            p.addNode(root);
            FocusMediumProjectile fp = new FocusMediumProjectile();
            fp.initialize();
            fp.getSetting("speed").setValue(2);
            p.addNode(fp);
            p.addNode(random.nextBoolean() ? new FocusEffectCurse()
                    : (random.nextBoolean() ? new FocusEffectFlux()
                            : (random.nextBoolean() ? new FocusEffectEarth() : (random.nextBoolean() ? new FocusEffectAir() : new FocusEffectFire()))));
            FocusEngine.castFocusPackage(this, p, true);
            swing(InteractionHand.MAIN_HAND);
        }
    }

    @Override
    public void setItemSlot(EquipmentSlot slotIn, ItemStack stack) {
        super.setItemSlot(slotIn, stack);
        if (!level().isClientSide && slotIn == EquipmentSlot.MAINHAND) {
            setCombatTask();
        }
    }

    /** setEquipmentBasedOnDifficulty. */
    @Override
    protected void populateDefaultEquipmentSlots(RandomSource rand, DifficultyInstance difficulty) {
        super.populateDefaultEquipmentSlots(rand, difficulty);
        switch (rand.nextInt(20)) {
            case 0, 12 -> setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ConfigItems.pechWand.get()));
            case 1 -> setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STONE_SWORD));
            case 3 -> setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STONE_AXE));
            case 5 -> setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_SWORD));
            case 6 -> setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_AXE));
            case 7 -> setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.FISHING_ROD));
            case 8 -> setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STONE_PICKAXE));
            case 9 -> setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_PICKAXE));
            case 2, 4, 10, 11, 13 -> setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BOW));
            default -> {
            }
        }
    }

    /** onInitialSpawn: the held item decides the kind of pech. */
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance diff, MobSpawnType reason, @Nullable SpawnGroupData data) {
        populateDefaultEquipmentSlots(random, diff);
        ItemStack itemstack = getMainHandItem();
        if (itemstack.is(ConfigItems.pechWand.get())) {
            setPechType(1);
            setDropChance(EquipmentSlot.MAINHAND, 0.1f);
        } else if (!itemstack.isEmpty()) {
            if (itemstack.is(Items.BOW)) {
                setPechType(2);
            }
            populateDefaultEquipmentEnchantments(world, random, diff);
        }
        float f = diff.getSpecialMultiplier();
        setCanPickUpLoot(random.nextFloat() < 0.75f * f);
        setCombatTask();
        return super.finalizeSpawn(world, diff, reason, data);
    }

    /**
     * getCanSpawnHere: fewer than 4 pechs within 16 blocks, in a magical biome (outside the overworld only the
     * magical forest and the eerie biome count), and the usual monster rules.
     */
    public static boolean checkPechSpawnRules(EntityType<EntityPech> type, ServerLevelAccessor world, MobSpawnType reason, BlockPos pos,
            RandomSource rand) {
        Holder<Biome> biome = world.getBiome(pos);
        boolean magicBiome = biome.is(net.neoforged.neoforge.common.Tags.Biomes.IS_MAGICAL);
        int count = world.getEntitiesOfClass(EntityPech.class, new net.minecraft.world.phys.AABB(pos).inflate(16.0)).size();
        if (world.getLevel().dimension() != Level.OVERWORLD && !biome.is(thaumcraft.common.world.biomes.TCBiomes.MAGICAL_FOREST)
                && !biome.is(thaumcraft.common.world.biomes.TCBiomes.EERIE)) {
            magicBiome = false;
        }
        return count < 4 && magicBiome && Monster.checkMonsterSpawnRules(type, world, reason, pos, rand);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TYPE, (byte) 0);
        builder.define(ANGER, 0);
        builder.define(TAMED, false);
    }

    public int getPechType() {
        return entityData.get(TYPE);
    }

    public int getAnger() {
        return entityData.get(ANGER);
    }

    public boolean isTamed() {
        return entityData.get(TAMED);
    }

    public void setPechType(int par1) {
        entityData.set(TYPE, (byte) par1);
    }

    public void setAnger(int par1) {
        entityData.set(ANGER, par1);
    }

    public void setTamed(boolean par1) {
        entityData.set(TAMED, par1);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putByte("PechType", (byte) getPechType());
        nbt.putShort("Anger", (short) getAnger());
        nbt.putBoolean("Tamed", isTamed());
        ContainerHelper.saveAllItems(nbt, loot, registryAccess());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        if (nbt.contains("PechType")) {
            setPechType(nbt.getByte("PechType"));
        }
        setAnger(nbt.getShort("Anger"));
        setTamed(nbt.getBoolean("Tamed"));
        loot = NonNullList.withSize(9, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(nbt, loot, registryAccess());
        setCombatTask();
    }

    /** canDespawn: a pech with five or more stacks in its pack stays. */
    @Override
    public boolean removeWhenFarAway(double distance) {
        int q = 0;
        for (ItemStack is : loot) {
            if (!is.isEmpty()) {
                ++q;
            }
        }
        return q < 5;
    }

    @Override
    public boolean canBeLeashed() {
        return false;
    }

    /** dropLoot: each stack in the pack has a one in three chance to drop, on top of the loot table. */
    @Override
    protected void dropCustomDeathLoot(ServerLevel world, DamageSource source, boolean recentlyHit) {
        for (ItemStack stack : loot) {
            if (!stack.isEmpty() && world.random.nextFloat() < 0.33f) {
                spawnAtLocation(stack.copy(), 1.5f);
            }
        }
        super.dropCustomDeathLoot(world, source, recentlyHit);
    }

    /** handleStatusUpdate: 16 and 17 make it mumble, 18 is happy, 19 angry. */
    @Override
    public void handleEntityEvent(byte par1) {
        if (par1 == 16) {
            mumble = (float) Math.PI;
        } else if (par1 == 17) {
            mumble = (float) (Math.PI * 2.0);
        } else if (par1 == 18) {
            for (int i = 0; i < 5; ++i) {
                spawnMoodParticle(true);
            }
        }
        if (par1 == 19) {
            for (int i = 0; i < 5; ++i) {
                spawnMoodParticle(false);
            }
            mumble = (float) (Math.PI * 2.0);
        } else {
            super.handleEntityEvent(par1);
        }
    }

    private void spawnMoodParticle(boolean happy) {
        double d0 = random.nextGaussian() * 0.02;
        double d2 = random.nextGaussian() * 0.02;
        double d3 = random.nextGaussian() * 0.02;
        float width = getBbWidth();
        level().addParticle(happy ? ParticleTypes.HAPPY_VILLAGER : ParticleTypes.ANGRY_VILLAGER,
                getX() + random.nextFloat() * width * 2.0f - width, getY() + 0.5 + random.nextFloat() * getBbHeight(),
                getZ() + random.nextFloat() * width * 2.0f - width, d0, d2, d3);
    }

    /** playLivingSound: with another pech close by it sometimes chats instead. */
    @Override
    public void playAmbientSound() {
        if (!level().isClientSide) {
            if (random.nextInt(3) == 0) {
                for (Entity entity1 : level().getEntities(this, getBoundingBox().inflate(4.0, 2.0, 4.0))) {
                    if (entity1 instanceof EntityPech) {
                        level().broadcastEntityEvent(this, (byte) 17);
                        playSound(SoundsTC.PECH_TRADE.get(), getSoundVolume(), getVoicePitch());
                        return;
                    }
                }
            }
            level().broadcastEntityEvent(this, (byte) 16);
        }
        super.playAmbientSound();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 120;
    }

    @Override
    protected float getSoundVolume() {
        return 0.4f;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundsTC.PECH_IDLE.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return SoundsTC.PECH_HIT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundsTC.PECH_DEATH.get();
    }

    private void becomeAngryAt(Entity entity) {
        if (entity instanceof Player player && player.isCreative()) {
            return;
        }
        if (getAnger() <= 0) {
            level().broadcastEntityEvent(this, (byte) 19);
            playSound(SoundsTC.PECH_CHARGE.get(), getSoundVolume(), getVoicePitch());
        }
        setTarget((LivingEntity) entity);
        setAnger(400 + random.nextInt(400));
        setTamed(false);
        setCombatTask();
    }

    @Override
    public int getArmorValue() {
        return Math.min(20, super.getArmorValue() + 2);
    }

    /** attackEntityFrom: a player hitting one pech angers every pech within 32 blocks. */
    @Override
    public boolean hurt(DamageSource damSource, float par2) {
        if (isInvulnerableTo(damSource)) {
            return false;
        }
        Entity entity = damSource.getEntity();
        if (entity instanceof Player) {
            for (Entity entity2 : level().getEntities(this, getBoundingBox().inflate(32.0, 16.0, 32.0))) {
                if (entity2 instanceof EntityPech entitypech) {
                    entitypech.becomeAngryAt(entity);
                }
            }
            becomeAngryAt(entity);
        }
        return super.hurt(damSource, par2);
    }

    @Override
    public void tick() {
        if (mumble > 0.0f) {
            mumble *= 0.75f;
        }
        if (getAnger() > 0) {
            setAnger(getAnger() - 1);
        }
        if (getAnger() > 0 && getTarget() != null) {
            if (chargecount > 0) {
                --chargecount;
            }
            if (chargecount == 0) {
                chargecount = 100;
                playSound(SoundsTC.PECH_CHARGE.get(), getSoundVolume(), getVoicePitch());
            }
            level().broadcastEntityEvent(this, (byte) 17);
        }
        if (level().isClientSide && random.nextInt(15) == 0 && getAnger() > 0) {
            spawnMoodParticle(false);
        }
        if (level().isClientSide && random.nextInt(25) == 0 && isTamed()) {
            spawnMoodParticle(true);
        }
        super.tick();
    }

    /** updateAITasks: it heals a point every two seconds. */
    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (tickCount % 40 == 0) {
            heal(1.0f);
        }
    }

    /** Whether it would walk over to pick up this stack: something valued, or room in its pack. */
    public boolean canPickup(ItemStack entityItem) {
        if (entityItem == null) {
            return false;
        }
        if (!isTamed() && valuedItems.containsKey(entityItem.getItem())) {
            return true;
        }
        for (int a = 0; a < loot.size(); ++a) {
            if (loot.get(a).isEmpty()) {
                return true;
            }
            if (InventoryUtils.areItemStacksEqualStrict(entityItem, loot.get(a))
                    && entityItem.getCount() + loot.get(a).getCount() <= loot.get(a).getMaxStackSize()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Picks up a stack and returns what is left. An untamed pech eats one valued item at a time, with a value in
     * ten chance to be tamed by it; anything else (or anything, once tamed) goes in its pack.
     */
    public ItemStack pickupItem(ItemStack entityItem) {
        if (entityItem == null || entityItem.isEmpty()) {
            return ItemStack.EMPTY;
        }
        if (isTamed() || !isValued(entityItem)) {
            for (int a = 0; a < loot.size(); ++a) {
                if (!entityItem.isEmpty() && !loot.get(a).isEmpty() && loot.get(a).getCount() < loot.get(a).getMaxStackSize()
                        && InventoryUtils.areItemStacksEqualStrict(entityItem, loot.get(a))) {
                    if (entityItem.getCount() + loot.get(a).getCount() <= loot.get(a).getMaxStackSize()) {
                        loot.get(a).grow(entityItem.getCount());
                        return ItemStack.EMPTY;
                    }
                    int sz = Math.min(entityItem.getCount(), loot.get(a).getMaxStackSize() - loot.get(a).getCount());
                    loot.get(a).grow(sz);
                    entityItem.shrink(sz);
                }
            }
            for (int a = 0; a < loot.size(); ++a) {
                if (!entityItem.isEmpty() && loot.get(a).isEmpty()) {
                    loot.set(a, entityItem.copy());
                    return ItemStack.EMPTY;
                }
            }
            return entityItem;
        }
        if (random.nextInt(10) < getValue(entityItem)) {
            setTamed(true);
            setCombatTask();
            level().broadcastEntityEvent(this, (byte) 18);
        }
        entityItem.shrink(1);
        return entityItem.isEmpty() ? ItemStack.EMPTY : entityItem;
    }

    /** processInteract: a tamed pech opens its trade window. */
    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (player.isShiftKeyDown() || player.getItemInHand(hand).getItem() instanceof NameTagItem) {
            return InteractionResult.PASS;
        }
        if (isTamed()) {
            if (!level().isClientSide) {
                player.openMenu(new SimpleMenuProvider((id, inventory, p) -> new ContainerPech(id, inventory, this),
                        Component.translatable("container.pech")), buf -> buf.writeVarInt(getId()));
            }
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }

    public boolean isValued(ItemStack item) {
        if (item == null || item.isEmpty()) {
            return false;
        }
        boolean value = valuedItems.containsKey(item.getItem());
        if (!value) {
            AspectList al = ThaumcraftCraftingManager.getObjectTags(item);
            if (al != null && al.getAmount(Aspect.DESIRE) > 1) {
                value = true;
            }
        }
        return value;
    }

    public int getValue(ItemStack item) {
        if (item == null || item.isEmpty()) {
            return 0;
        }
        int value = valuedItems.getOrDefault(item.getItem(), 0);
        if (value == 0) {
            AspectList al = ThaumcraftCraftingManager.getObjectTags(item);
            value = al == null ? 0 : Math.min(32, al.getAmount(Aspect.DESIRE) / 2);
        }
        return value;
    }
}
