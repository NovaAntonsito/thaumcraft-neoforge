package thaumcraft.common.registry;

import java.util.function.Supplier;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredRegister;
import thaumcraft.Thaumcraft;
import thaumcraft.common.entities.EntityFluxRift;
import thaumcraft.common.entities.projectile.EntityAlumentum;

/** Entity types (the 1.12 EntityRegistry calls in ConfigEntities). Only the ones ported so far. */
public final class TCEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, Thaumcraft.MODID);

    /** ConfigEntities: "Alumentum", tracked up to 64 blocks (4 chunks). Updates every 10 ticks like vanilla throwables (20 in 1.12). */
    public static final Supplier<EntityType<EntityAlumentum>> ALUMENTUM = ENTITY_TYPES.register("alumentum",
            () -> EntityType.Builder.<EntityAlumentum>of(EntityAlumentum::new, MobCategory.MISC).sized(0.25f, 0.25f)
                    .clientTrackingRange(4).updateInterval(10).build("alumentum"));

    /** ConfigEntities: "FluxRift", tracked up to 64 blocks, updates every 20 ticks, no velocity. */
    public static final Supplier<EntityType<EntityFluxRift>> FLUX_RIFT = ENTITY_TYPES.register("flux_rift",
            () -> EntityType.Builder.<EntityFluxRift>of(EntityFluxRift::new, MobCategory.MISC).sized(2.0f, 2.0f).fireImmune()
                    .clientTrackingRange(4).updateInterval(20).build("flux_rift"));

    /** ConfigEntities: "FocusProjectile", tracked up to 64 blocks, updates every 20 ticks (10 here, like vanilla throwables). */
    public static final Supplier<EntityType<thaumcraft.common.entities.projectile.EntityFocusProjectile>> FOCUS_PROJECTILE = ENTITY_TYPES.register("focus_projectile",
            () -> EntityType.Builder.<thaumcraft.common.entities.projectile.EntityFocusProjectile>of(thaumcraft.common.entities.projectile.EntityFocusProjectile::new, MobCategory.MISC)
                    .sized(0.15f, 0.15f).clientTrackingRange(4).updateInterval(10).build("focus_projectile"));

    /** ConfigEntities: "FocusCloud", tracked up to 64 blocks, updates every 20 ticks. */
    public static final Supplier<EntityType<thaumcraft.common.entities.projectile.EntityFocusCloud>> FOCUS_CLOUD = ENTITY_TYPES.register("focus_cloud",
            () -> EntityType.Builder.<thaumcraft.common.entities.projectile.EntityFocusCloud>of(thaumcraft.common.entities.projectile.EntityFocusCloud::new, MobCategory.MISC)
                    .sized(0.15f, 0.15f).fireImmune().clientTrackingRange(4).updateInterval(20).build("focus_cloud"));

    /** ConfigEntities: "FocusMine", tracked up to 64 blocks, updates every 20 ticks (10 here, like vanilla throwables). */
    public static final Supplier<EntityType<thaumcraft.common.entities.projectile.EntityFocusMine>> FOCUS_MINE = ENTITY_TYPES.register("focus_mine",
            () -> EntityType.Builder.<thaumcraft.common.entities.projectile.EntityFocusMine>of(thaumcraft.common.entities.projectile.EntityFocusMine::new, MobCategory.MISC)
                    .sized(0.15f, 0.15f).clientTrackingRange(4).updateInterval(10).build("focus_mine"));

    /** ConfigEntities: "Spellbat", tracked up to 64 blocks, updates every 3 ticks. No spawn egg: it dies without a caster. */
    public static final Supplier<EntityType<thaumcraft.common.entities.monster.EntitySpellBat>> SPELL_BAT = ENTITY_TYPES.register("spellbat",
            () -> EntityType.Builder.<thaumcraft.common.entities.monster.EntitySpellBat>of(thaumcraft.common.entities.monster.EntitySpellBat::new, MobCategory.MISC)
                    .sized(0.5f, 0.9f).clientTrackingRange(4).updateInterval(3).build("spellbat"));

    /** ConfigEntities: "Golem", tracked up to 64 blocks, updates every 3 ticks. */
    public static final Supplier<EntityType<thaumcraft.common.golems.EntityThaumcraftGolem>> GOLEM = ENTITY_TYPES.register("golem",
            () -> EntityType.Builder.<thaumcraft.common.golems.EntityThaumcraftGolem>of(thaumcraft.common.golems.EntityThaumcraftGolem::new, MobCategory.MISC)
                    .sized(0.4f, 0.9f).eyeHeight(0.7f).clientTrackingRange(4).updateInterval(3).build("golem"));

    /** ConfigEntities: "GolemDart", tracked up to 64 blocks, updates every 20 ticks. */
    public static final Supplier<EntityType<thaumcraft.common.entities.projectile.EntityGolemDart>> GOLEM_DART = ENTITY_TYPES.register("golem_dart",
            () -> EntityType.Builder.<thaumcraft.common.entities.projectile.EntityGolemDart>of(thaumcraft.common.entities.projectile.EntityGolemDart::new, MobCategory.MISC)
                    .sized(0.2f, 0.2f).clientTrackingRange(4).updateInterval(20).build("golem_dart"));

    /** ConfigEntities: "FallingTaint", tracked up to 64 blocks, updates every 3 ticks. */
    public static final Supplier<EntityType<thaumcraft.common.entities.EntityFallingTaint>> FALLING_TAINT = ENTITY_TYPES.register("falling_taint",
            () -> EntityType.Builder.<thaumcraft.common.entities.EntityFallingTaint>of(thaumcraft.common.entities.EntityFallingTaint::new, MobCategory.MISC)
                    .sized(0.98f, 0.98f).clientTrackingRange(4).updateInterval(3).build("falling_taint"));

    /** ConfigEntities: "BottleTaint", tracked up to 64 blocks, updates every 20 ticks (10 here, like vanilla throwables). */
    public static final Supplier<EntityType<thaumcraft.common.entities.projectile.EntityBottleTaint>> BOTTLE_TAINT = ENTITY_TYPES.register("bottle_taint",
            () -> EntityType.Builder.<thaumcraft.common.entities.projectile.EntityBottleTaint>of(thaumcraft.common.entities.projectile.EntityBottleTaint::new, MobCategory.MISC)
                    .sized(0.25f, 0.25f).clientTrackingRange(4).updateInterval(10).build("bottle_taint"));

    /** ConfigEntities: "TaintSeed", tracked up to 64 blocks, updates every 20 ticks. */
    public static final Supplier<EntityType<thaumcraft.common.entities.monster.tainted.EntityTaintSeed>> TAINT_SEED = ENTITY_TYPES.register("taint_seed",
            () -> EntityType.Builder.<thaumcraft.common.entities.monster.tainted.EntityTaintSeed>of(thaumcraft.common.entities.monster.tainted.EntityTaintSeed::new, MobCategory.MONSTER)
                    .sized(1.5f, 1.25f).clientTrackingRange(4).updateInterval(20).build("taint_seed"));

    /** ConfigEntities: "TaintSeedPrime", tracked up to 64 blocks, updates every 20 ticks. */
    public static final Supplier<EntityType<thaumcraft.common.entities.monster.tainted.EntityTaintSeedPrime>> TAINT_SEED_PRIME = ENTITY_TYPES.register("taint_seed_prime",
            () -> EntityType.Builder.<thaumcraft.common.entities.monster.tainted.EntityTaintSeedPrime>of(thaumcraft.common.entities.monster.tainted.EntityTaintSeedPrime::new, MobCategory.MONSTER)
                    .sized(2.0f, 2.0f).clientTrackingRange(4).updateInterval(20).build("taint_seed_prime"));

    /** ConfigEntities: "TaintCrawler", tracked up to 64 blocks, updates every 3 ticks. Eye height 0.1. */
    public static final Supplier<EntityType<thaumcraft.common.entities.monster.tainted.EntityTaintCrawler>> TAINT_CRAWLER = ENTITY_TYPES.register("taint_crawler",
            () -> EntityType.Builder.<thaumcraft.common.entities.monster.tainted.EntityTaintCrawler>of(thaumcraft.common.entities.monster.tainted.EntityTaintCrawler::new, MobCategory.MONSTER)
                    .sized(0.5f, 0.4f).eyeHeight(0.1f).clientTrackingRange(4).updateInterval(3).build("taint_crawler"));

    /** ConfigEntities: "Taintacle", tracked up to 64 blocks, updates every 3 ticks. */
    public static final Supplier<EntityType<thaumcraft.common.entities.monster.tainted.EntityTaintacle>> TAINTACLE = ENTITY_TYPES.register("taintacle",
            () -> EntityType.Builder.<thaumcraft.common.entities.monster.tainted.EntityTaintacle>of(thaumcraft.common.entities.monster.tainted.EntityTaintacle::new, MobCategory.MONSTER)
                    .sized(0.8f, 3.0f).clientTrackingRange(4).updateInterval(3).build("taintacle"));

    /** ConfigEntities: "TaintacleTiny" (Taint Tendril), tracked up to 64 blocks, updates every 3 ticks. */
    public static final Supplier<EntityType<thaumcraft.common.entities.monster.tainted.EntityTaintacleSmall>> TAINTACLE_SMALL = ENTITY_TYPES.register("taintacle_tiny",
            () -> EntityType.Builder.<thaumcraft.common.entities.monster.tainted.EntityTaintacleSmall>of(thaumcraft.common.entities.monster.tainted.EntityTaintacleSmall::new, MobCategory.MONSTER)
                    .sized(0.22f, 1.0f).clientTrackingRange(4).updateInterval(3).build("taintacle_tiny"));

    /** ConfigEntities: "TaintSwarm", tracked up to 64 blocks, updates every 3 ticks. */
    public static final Supplier<EntityType<thaumcraft.common.entities.monster.tainted.EntityTaintSwarm>> TAINT_SWARM = ENTITY_TYPES.register("taint_swarm",
            () -> EntityType.Builder.<thaumcraft.common.entities.monster.tainted.EntityTaintSwarm>of(thaumcraft.common.entities.monster.tainted.EntityTaintSwarm::new, MobCategory.MONSTER)
                    .sized(2.0f, 2.0f).clientTrackingRange(4).updateInterval(3).build("taint_swarm"));

    /** ConfigEntities: "TaintacleGiant", tracked up to 96 blocks, updates every 3 ticks. */
    public static final Supplier<EntityType<thaumcraft.common.entities.monster.boss.EntityTaintacleGiant>> TAINTACLE_GIANT = ENTITY_TYPES.register("taintacle_giant",
            () -> EntityType.Builder.<thaumcraft.common.entities.monster.boss.EntityTaintacleGiant>of(thaumcraft.common.entities.monster.boss.EntityTaintacleGiant::new, MobCategory.MONSTER)
                    .sized(1.1f, 6.0f).clientTrackingRange(6).updateInterval(3).build("taintacle_giant"));

    /** ConfigEntities: "ThaumSlime", tracked up to 64 blocks, updates every 3 ticks. Its size scales this box. */
    public static final Supplier<EntityType<thaumcraft.common.entities.monster.EntityThaumicSlime>> THAUMIC_SLIME = ENTITY_TYPES.register("thaum_slime",
            () -> EntityType.Builder.<thaumcraft.common.entities.monster.EntityThaumicSlime>of(thaumcraft.common.entities.monster.EntityThaumicSlime::new, MobCategory.MONSTER)
                    .sized(0.51000005f, 0.51000005f).eyeHeight(0.325f).clientTrackingRange(4).updateInterval(3).build("thaum_slime"));

    /** ConfigEntities: "BrainyZombie", tracked up to 64 blocks, updates every 3 ticks. */
    public static final Supplier<EntityType<thaumcraft.common.entities.monster.EntityBrainyZombie>> BRAINY_ZOMBIE = ENTITY_TYPES.register("brainy_zombie",
            () -> EntityType.Builder.<thaumcraft.common.entities.monster.EntityBrainyZombie>of(thaumcraft.common.entities.monster.EntityBrainyZombie::new, MobCategory.MONSTER)
                    .sized(0.6f, 1.95f).eyeHeight(1.74f).passengerAttachments(2.0125f).ridingOffset(-0.7f).clientTrackingRange(4).updateInterval(3).build("brainy_zombie"));

    /** ConfigEntities: "GiantBrainyZombie", tracked up to 64 blocks, updates every 3 ticks. It grows when angry. */
    public static final Supplier<EntityType<thaumcraft.common.entities.monster.EntityGiantBrainyZombie>> GIANT_BRAINY_ZOMBIE = ENTITY_TYPES.register("giant_brainy_zombie",
            () -> EntityType.Builder.<thaumcraft.common.entities.monster.EntityGiantBrainyZombie>of(thaumcraft.common.entities.monster.EntityGiantBrainyZombie::new, MobCategory.MONSTER)
                    .sized(0.6f, 1.95f).eyeHeight(1.74f).passengerAttachments(2.0125f).ridingOffset(-0.7f).clientTrackingRange(4).updateInterval(3).build("giant_brainy_zombie"));

    /** ConfigEntities: "Wisp", tracked up to 64 blocks, updates every 3 ticks. */
    public static final Supplier<EntityType<thaumcraft.common.entities.monster.EntityWisp>> WISP = ENTITY_TYPES.register("wisp",
            () -> EntityType.Builder.<thaumcraft.common.entities.monster.EntityWisp>of(thaumcraft.common.entities.monster.EntityWisp::new, MobCategory.MONSTER)
                    .sized(0.9f, 0.9f).clientTrackingRange(4).updateInterval(3).build("wisp"));

    /** ConfigEntities: "Firebat", tracked up to 64 blocks, updates every 3 ticks. Immune to fire. */
    public static final Supplier<EntityType<thaumcraft.common.entities.monster.EntityFireBat>> FIRE_BAT = ENTITY_TYPES.register("firebat",
            () -> EntityType.Builder.<thaumcraft.common.entities.monster.EntityFireBat>of(thaumcraft.common.entities.monster.EntityFireBat::new, MobCategory.MONSTER)
                    .sized(0.5f, 0.9f).fireImmune().clientTrackingRange(4).updateInterval(3).build("firebat"));

    /** ConfigEntities: "MindSpider", tracked up to 64 blocks, updates every 3 ticks. Eye height 0.45. */
    public static final Supplier<EntityType<thaumcraft.common.entities.monster.EntityMindSpider>> MIND_SPIDER = ENTITY_TYPES.register("mind_spider",
            () -> EntityType.Builder.<thaumcraft.common.entities.monster.EntityMindSpider>of(thaumcraft.common.entities.monster.EntityMindSpider::new, MobCategory.MONSTER)
                    .sized(0.7f, 0.5f).eyeHeight(0.45f).clientTrackingRange(4).updateInterval(3).build("mind_spider"));

    /** ConfigEntities: "Pech", tracked up to 64 blocks, updates every 3 ticks. Eye height is two thirds of its height. */
    public static final Supplier<EntityType<thaumcraft.common.entities.monster.EntityPech>> PECH = ENTITY_TYPES.register("pech",
            () -> EntityType.Builder.<thaumcraft.common.entities.monster.EntityPech>of(thaumcraft.common.entities.monster.EntityPech::new, MobCategory.MONSTER)
                    .sized(0.6f, 1.8f).eyeHeight(1.188f).clientTrackingRange(4).updateInterval(3).build("pech"));

    // Stage 9d-2: the crimson cult.
    /** ConfigEntities: "CultistKnight", tracked up to 64 blocks, updates every 3 ticks. */
    public static final Supplier<EntityType<thaumcraft.common.entities.monster.cult.EntityCultistKnight>> CULTIST_KNIGHT = ENTITY_TYPES.register("cultist_knight",
            () -> EntityType.Builder.<thaumcraft.common.entities.monster.cult.EntityCultistKnight>of(thaumcraft.common.entities.monster.cult.EntityCultistKnight::new, MobCategory.MONSTER)
                    .sized(0.6f, 1.8f).clientTrackingRange(4).updateInterval(3).build("cultist_knight"));
    /** ConfigEntities: "CultistCleric", tracked up to 64 blocks, updates every 3 ticks. */
    public static final Supplier<EntityType<thaumcraft.common.entities.monster.cult.EntityCultistCleric>> CULTIST_CLERIC = ENTITY_TYPES.register("cultist_cleric",
            () -> EntityType.Builder.<thaumcraft.common.entities.monster.cult.EntityCultistCleric>of(thaumcraft.common.entities.monster.cult.EntityCultistCleric::new, MobCategory.MONSTER)
                    .sized(0.6f, 1.8f).clientTrackingRange(4).updateInterval(3).build("cultist_cleric"));
    /** ConfigEntities: "CultistPortalLesser", tracked up to 64 blocks, updates every 20 ticks. Immune to fire. */
    public static final Supplier<EntityType<thaumcraft.common.entities.monster.cult.EntityCultistPortalLesser>> CULTIST_PORTAL_LESSER = ENTITY_TYPES.register("cultist_portal_lesser",
            () -> EntityType.Builder.<thaumcraft.common.entities.monster.cult.EntityCultistPortalLesser>of(thaumcraft.common.entities.monster.cult.EntityCultistPortalLesser::new, MobCategory.MONSTER)
                    .sized(1.5f, 3.0f).fireImmune().clientTrackingRange(4).updateInterval(20).build("cultist_portal_lesser"));
    /** ConfigEntities: "GolemOrb", tracked up to 64 blocks, updates every 3 ticks. */
    public static final Supplier<EntityType<thaumcraft.common.entities.projectile.EntityGolemOrb>> GOLEM_ORB = ENTITY_TYPES.register("golem_orb",
            () -> EntityType.Builder.<thaumcraft.common.entities.projectile.EntityGolemOrb>of(thaumcraft.common.entities.projectile.EntityGolemOrb::new, MobCategory.MISC)
                    .sized(0.25f, 0.25f).clientTrackingRange(4).updateInterval(3).build("golem_orb"));

    // Stage 9d-3: the eldritch.
    /** ConfigEntities: "EldritchGuardian", tracked up to 64 blocks, updates every 3 ticks. Eye height 2.1. */
    public static final Supplier<EntityType<thaumcraft.common.entities.monster.EntityEldritchGuardian>> ELDRITCH_GUARDIAN = ENTITY_TYPES.register("eldritch_guardian",
            () -> EntityType.Builder.<thaumcraft.common.entities.monster.EntityEldritchGuardian>of(thaumcraft.common.entities.monster.EntityEldritchGuardian::new, MobCategory.MONSTER)
                    .sized(0.8f, 2.25f).eyeHeight(2.1f).clientTrackingRange(4).updateInterval(3).build("eldritch_guardian"));
    /** ConfigEntities: "EldritchCrab", tracked up to 64 blocks, updates every 3 ticks. */
    public static final Supplier<EntityType<thaumcraft.common.entities.monster.EntityEldritchCrab>> ELDRITCH_CRAB = ENTITY_TYPES.register("eldritch_crab",
            () -> EntityType.Builder.<thaumcraft.common.entities.monster.EntityEldritchCrab>of(thaumcraft.common.entities.monster.EntityEldritchCrab::new, MobCategory.MONSTER)
                    .sized(0.8f, 0.6f).clientTrackingRange(4).updateInterval(3).build("eldritch_crab"));
    /** ConfigEntities: "InhabitedZombie", tracked up to 64 blocks, updates every 3 ticks. The size and eye height of a zombie. */
    public static final Supplier<EntityType<thaumcraft.common.entities.monster.EntityInhabitedZombie>> INHABITED_ZOMBIE = ENTITY_TYPES.register("inhabited_zombie",
            () -> EntityType.Builder.<thaumcraft.common.entities.monster.EntityInhabitedZombie>of(thaumcraft.common.entities.monster.EntityInhabitedZombie::new, MobCategory.MONSTER)
                    .sized(0.6f, 1.95f).eyeHeight(1.74f).passengerAttachments(2.0125f).ridingOffset(-0.7f).clientTrackingRange(4).updateInterval(3).build("inhabited_zombie"));
    /** ConfigEntities: "EldritchOrb", tracked up to 64 blocks, updates every 20 ticks. */
    public static final Supplier<EntityType<thaumcraft.common.entities.projectile.EntityEldritchOrb>> ELDRITCH_ORB = ENTITY_TYPES.register("eldritch_orb",
            () -> EntityType.Builder.<thaumcraft.common.entities.projectile.EntityEldritchOrb>of(thaumcraft.common.entities.projectile.EntityEldritchOrb::new, MobCategory.MISC)
                    .sized(0.25f, 0.25f).clientTrackingRange(4).updateInterval(20).build("eldritch_orb"));

    // Stage 9d-4: the bosses.
    /** ConfigEntities: "CultistPortalGreater", tracked up to 64 blocks, updates every 20 ticks. Immune to fire. */
    public static final Supplier<EntityType<thaumcraft.common.entities.monster.boss.EntityCultistPortalGreater>> CULTIST_PORTAL_GREATER = ENTITY_TYPES.register("cultist_portal_greater",
            () -> EntityType.Builder.<thaumcraft.common.entities.monster.boss.EntityCultistPortalGreater>of(thaumcraft.common.entities.monster.boss.EntityCultistPortalGreater::new, MobCategory.MONSTER)
                    .sized(1.5f, 3.0f).fireImmune().clientTrackingRange(4).updateInterval(20).build("cultist_portal_greater"));
    /** ConfigEntities: "CultistLeader", tracked up to 64 blocks, updates every 3 ticks. */
    public static final Supplier<EntityType<thaumcraft.common.entities.monster.boss.EntityCultistLeader>> CULTIST_LEADER = ENTITY_TYPES.register("cultist_leader",
            () -> EntityType.Builder.<thaumcraft.common.entities.monster.boss.EntityCultistLeader>of(thaumcraft.common.entities.monster.boss.EntityCultistLeader::new, MobCategory.MONSTER)
                    .sized(0.75f, 2.25f).clientTrackingRange(4).updateInterval(3).build("cultist_leader"));

    /** Attributes of the living entities above. */
    public static void registerAttributes(net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent event) {
        event.put(GOLEM.get(), thaumcraft.common.golems.EntityThaumcraftGolem.createAttributes().build());
        event.put(SPELL_BAT.get(), thaumcraft.common.entities.monster.EntitySpellBat.createAttributes().build());
        event.put(TAINT_SEED.get(), thaumcraft.common.entities.monster.tainted.EntityTaintSeed.createAttributes().build());
        event.put(TAINT_SEED_PRIME.get(), thaumcraft.common.entities.monster.tainted.EntityTaintSeedPrime.createAttributes().build());
        event.put(TAINT_CRAWLER.get(), thaumcraft.common.entities.monster.tainted.EntityTaintCrawler.createAttributes().build());
        event.put(TAINTACLE.get(), thaumcraft.common.entities.monster.tainted.EntityTaintacle.createAttributes().build());
        event.put(TAINTACLE_SMALL.get(), thaumcraft.common.entities.monster.tainted.EntityTaintacleSmall.createAttributes().build());
        event.put(TAINT_SWARM.get(), thaumcraft.common.entities.monster.tainted.EntityTaintSwarm.createAttributes().build());
        event.put(TAINTACLE_GIANT.get(), thaumcraft.common.entities.monster.boss.EntityTaintacleGiant.createAttributes().build());
        event.put(THAUMIC_SLIME.get(), net.minecraft.world.entity.monster.Monster.createMonsterAttributes().build());
        event.put(BRAINY_ZOMBIE.get(), thaumcraft.common.entities.monster.EntityBrainyZombie.createAttributes().build());
        event.put(GIANT_BRAINY_ZOMBIE.get(), thaumcraft.common.entities.monster.EntityGiantBrainyZombie.createAttributes().build());
        event.put(WISP.get(), thaumcraft.common.entities.monster.EntityWisp.createAttributes().build());
        event.put(FIRE_BAT.get(), thaumcraft.common.entities.monster.EntityFireBat.createAttributes().build());
        event.put(MIND_SPIDER.get(), thaumcraft.common.entities.monster.EntityMindSpider.createAttributes().build());
        event.put(PECH.get(), thaumcraft.common.entities.monster.EntityPech.createAttributes().build());
        event.put(CULTIST_KNIGHT.get(), thaumcraft.common.entities.monster.cult.EntityCultistKnight.createAttributes().build());
        event.put(CULTIST_CLERIC.get(), thaumcraft.common.entities.monster.cult.EntityCultistCleric.createAttributes().build());
        event.put(CULTIST_PORTAL_LESSER.get(), thaumcraft.common.entities.monster.cult.EntityCultistPortalLesser.createAttributes().build());
        event.put(ELDRITCH_GUARDIAN.get(), thaumcraft.common.entities.monster.EntityEldritchGuardian.createAttributes().build());
        event.put(ELDRITCH_CRAB.get(), thaumcraft.common.entities.monster.EntityEldritchCrab.createAttributes().build());
        event.put(INHABITED_ZOMBIE.get(), thaumcraft.common.entities.monster.EntityInhabitedZombie.createAttributes().build());
        event.put(CULTIST_PORTAL_GREATER.get(), thaumcraft.common.entities.monster.boss.EntityCultistPortalGreater.createAttributes().build());
        event.put(CULTIST_LEADER.get(), thaumcraft.common.entities.monster.boss.EntityCultistLeader.createAttributes().build());
    }

    /** Where the mobs that spawn on their own may appear (getCanSpawnHere of the original). */
    public static void registerSpawnPlacements(net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent event) {
        event.register(BRAINY_ZOMBIE.get(), net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND, net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                net.minecraft.world.entity.monster.Monster::checkMonsterSpawnRules, net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(GIANT_BRAINY_ZOMBIE.get(), net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND, net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                net.minecraft.world.entity.monster.Monster::checkMonsterSpawnRules, net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(FIRE_BAT.get(), net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND, net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                thaumcraft.common.entities.monster.EntityFireBat::checkFireBatSpawnRules, net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(WISP.get(), net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND, net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                thaumcraft.common.entities.monster.EntityWisp::checkWispSpawnRules, net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(PECH.get(), net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND, net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                thaumcraft.common.entities.monster.EntityPech::checkPechSpawnRules, net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ELDRITCH_GUARDIAN.get(), net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND, net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                thaumcraft.common.entities.monster.EntityEldritchGuardian::checkGuardianSpawnRules, net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ELDRITCH_CRAB.get(), net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND, net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                net.minecraft.world.entity.monster.Monster::checkMonsterSpawnRules, net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(INHABITED_ZOMBIE.get(), net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND, net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                thaumcraft.common.entities.monster.EntityInhabitedZombie::checkInhabitedZombieSpawnRules, net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }

    private TCEntities() {
    }
}
