package thaumcraft.common.lib;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.level.Level;
import thaumcraft.Thaumcraft;

/**
 * Port of DamageSourceThaumcraft. Damage sources are data driven since 1.19.4: each one is a damage type in
 * data/thaumcraft/damage_type, and setDamageBypassesArmor / setMagicDamage are the minecraft:bypasses_armor and
 * c:is_magic damage type tags.
 * TODO: tentacle, swarm and dissolve come with the creatures that use them.
 */
public final class DamageSourceThaumcraft {
    public static final ResourceKey<DamageType> TAINT = ResourceKey.create(Registries.DAMAGE_TYPE, Thaumcraft.id("taint"));

    private DamageSourceThaumcraft() {
    }

    public static DamageSource taint(Level world) {
        return world.damageSources().source(TAINT);
    }
}
