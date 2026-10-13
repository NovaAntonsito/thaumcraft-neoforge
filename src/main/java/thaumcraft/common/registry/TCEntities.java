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

    /** Attributes of the living entities above. */
    public static void registerAttributes(net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent event) {
        event.put(SPELL_BAT.get(), thaumcraft.common.entities.monster.EntitySpellBat.createAttributes().build());
    }

    private TCEntities() {
    }
}
