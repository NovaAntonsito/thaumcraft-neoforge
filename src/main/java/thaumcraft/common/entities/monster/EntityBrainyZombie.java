package thaumcraft.common.entities.monster;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootTable;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.lib.utils.EntityUtils;

/**
 * Port of EntityBrainyZombie ("Angry Zombie"): a tougher zombie (25 health, 5 damage, one more point of armor) that
 * never calls reinforcements and often drops a zombie brain. It drops the normal zombie loot too.
 * 1.21: it does not turn into a drowned under water, as there were no drowned in 1.12.
 */
public class EntityBrainyZombie extends Zombie {
    public EntityBrainyZombie(EntityType<? extends EntityBrainyZombie> type, Level world) {
        super(type, world);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Zombie.createAttributes().add(Attributes.MAX_HEALTH, 25.0).add(Attributes.ATTACK_DAMAGE, 5.0)
                .add(Attributes.SPAWN_REINFORCEMENTS_CHANCE, 0.0);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    @Override
    protected boolean convertsInWater() {
        return false;
    }

    @Override
    public int getArmorValue() {
        return super.getArmorValue() + 1;
    }

    @Override
    protected ResourceKey<LootTable> getDefaultLootTable() {
        return EntityType.ZOMBIE.getDefaultLootTable();
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel world, DamageSource source, boolean recentlyHit) {
        if (world.random.nextInt(10) - EntityUtils.getLootingLevel(world, source) <= 4) {
            spawnAtLocation(new ItemStack(ConfigItems.brain.get()), 1.5f);
        }
        super.dropCustomDeathLoot(world, source, recentlyHit);
    }
}
