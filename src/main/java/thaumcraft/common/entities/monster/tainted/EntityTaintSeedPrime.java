package thaumcraft.common.entities.monster.tainted;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.registry.TCEntities;

/** Port of EntityTaintSeedPrime ("Giant Taint Seed"): what a flux rift can turn into. Bigger, tougher, wider reach. */
public class EntityTaintSeedPrime extends EntityTaintSeed {
    public EntityTaintSeedPrime(EntityType<? extends EntityTaintSeedPrime> type, Level world) {
        super(type, world);
        xpReward = 12;
    }

    public EntityTaintSeedPrime(Level world) {
        this(TCEntities.TAINT_SEED_PRIME.get(), world);
    }

    @Override
    protected int getArea() {
        return 2;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 150.0).add(Attributes.ATTACK_DAMAGE, 7.0)
                .add(Attributes.MOVEMENT_SPEED, 0.0);
    }

    /** dropFewItems: one to three flux crystals. */
    @Override
    protected void dropCustomDeathLoot(ServerLevel world, DamageSource source, boolean recentlyHit) {
        spawnAtLocation(ConfigItems.crystalEssence.get().makeCrystal(Aspect.FLUX, 1), getBbHeight() / 2.0f);
        if (random.nextBoolean()) {
            spawnAtLocation(ConfigItems.crystalEssence.get().makeCrystal(Aspect.FLUX, 1), getBbHeight() / 2.0f);
        }
        if (random.nextBoolean()) {
            spawnAtLocation(ConfigItems.crystalEssence.get().makeCrystal(Aspect.FLUX, 1), getBbHeight() / 2.0f);
        }
    }
}
