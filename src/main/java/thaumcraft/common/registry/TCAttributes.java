package thaumcraft.common.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import thaumcraft.Thaumcraft;

/**
 * The champion attributes. In 1.12 EntityEvents.entityConstuct registered them on every creature as it was built;
 * now they are added to every living entity type but the player at load (the code only reads them on creatures).
 */
public final class TCAttributes {
    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(Registries.ATTRIBUTE, Thaumcraft.MODID);

    /**
     * ThaumcraftApiHelper.CHAMPION_MOD ("tc.mobmod"): -2 not decided yet, -1 a normal mob, 0 to 13 the champion
     * modifier (ChampionModifier.mods). Synced, so the client can show the effects.
     */
    public static final DeferredHolder<Attribute, Attribute> CHAMPION_MOD = ATTRIBUTES.register("mobmod",
            () -> new RangedAttribute("attribute.thaumcraft.mobmod", -2.0, -2.0, 100.0).setSyncable(true));

    /** ChampionModTainted.TAINTED_MOD ("tc.mobmodtaint"): 1 once a tainted critter got its hostile AI. */
    public static final DeferredHolder<Attribute, Attribute> TAINTED_MOD = ATTRIBUTES.register("mobmodtaint",
            () -> new RangedAttribute("attribute.thaumcraft.mobmodtaint", 0.0, 0.0, 1.0));

    public static void addToEntities(EntityAttributeModificationEvent event) {
        for (EntityType<? extends LivingEntity> type : event.getTypes()) {
            if (type == EntityType.PLAYER) continue;
            if (!event.has(type, CHAMPION_MOD)) {
                event.add(type, CHAMPION_MOD, -2.0);
            }
            if (!event.has(type, TAINTED_MOD)) {
                event.add(type, TAINTED_MOD, 0.0);
            }
        }
    }

    private TCAttributes() {
    }
}
