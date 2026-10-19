package thaumcraft.common.entities.monster.mods;

import java.lang.reflect.Field;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveThroughVillageGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.player.Player;
import thaumcraft.Thaumcraft;
import thaumcraft.client.fx.ClientFX;
import thaumcraft.common.registry.TCAttributes;

public class ChampionModTainted implements IChampionModifierEffect {
    public static final ResourceLocation IS_TAINTED = ResourceLocation.fromNamespaceAndPath(Thaumcraft.MODID, "istainted");
    private static Field goalSelector;
    private static Field targetSelector;

    @Override
    public float performEffect(LivingEntity boss, LivingEntity target, DamageSource source, float amount) {
        if (boss instanceof PathfinderMob critter) {
            resetAI(critter);
        }
        return amount;
    }

    /** Turns a peaceful critter hostile: its goals are replaced with melee goals against players. */
    public static void resetAI(PathfinderMob critter) {
        AttributeInstance modai = critter.getAttribute(TCAttributes.TAINTED_MOD);
        if (modai == null) return;
        if (!(critter instanceof Monster) && modai.getValue() == 0.0) {
            try {
                GoalSelector tasks = selector(critter, false);
                GoalSelector targetTasks = selector(critter, true);
                tasks.removeAllGoals(g -> true);
                targetTasks.removeAllGoals(g -> true);
                tasks.addGoal(0, new FloatGoal(critter));
                tasks.addGoal(2, new thaumcraft.common.entities.ai.combat.EntityCritterAIAttackMelee(critter, 1.2, false));
                tasks.addGoal(5, new MoveTowardsRestrictionGoal(critter, 1.0));
                tasks.addGoal(7, new RandomStrollGoal(critter, 1.0));
                tasks.addGoal(8, new LookAtPlayerGoal(critter, Player.class, 8.0f));
                tasks.addGoal(8, new RandomLookAroundGoal(critter));
                tasks.addGoal(6, new MoveThroughVillageGoal(critter, 1.0, false, 4, () -> false));
                targetTasks.addGoal(1, new HurtByTargetGoal(critter, ZombifiedPiglin.class).setAlertOthers());
                targetTasks.addGoal(2, new NearestAttackableTargetGoal<>(critter, Player.class, true));
                setTaintedAI(modai, 1.0);
            } catch (Exception ex) {
            }
        }
    }

    public static void setTaintedAI(AttributeInstance modai, double value) {
        modai.removeModifier(IS_TAINTED);
        modai.addPermanentModifier(new AttributeModifier(IS_TAINTED, value, AttributeModifier.Operation.ADD_VALUE));
    }

    // Mob.goalSelector and targetSelector are protected; the runtime uses Mojang names, so plain reflection works.
    private static GoalSelector selector(Mob mob, boolean target) throws ReflectiveOperationException {
        if (goalSelector == null) {
            goalSelector = Mob.class.getDeclaredField("goalSelector");
            goalSelector.setAccessible(true);
            targetSelector = Mob.class.getDeclaredField("targetSelector");
            targetSelector.setAccessible(true);
        }
        return (GoalSelector) (target ? targetSelector : goalSelector).get(mob);
    }

    @Override
    public void showFX(LivingEntity boss) {
        float w = boss.level().random.nextFloat() * boss.getBbWidth();
        float d = boss.level().random.nextFloat() * boss.getBbWidth();
        float h = boss.level().random.nextFloat() * boss.getBbHeight();
        thaumcraft.client.fx.FXDispatcher.drawGenericParticles(boss.getBoundingBox().minX + w, boss.getBoundingBox().minY + h, boss.getBoundingBox().minZ + d, 0.0, -0.01, 0.0, 0.1f + boss.level().random.nextFloat() * 0.2f, 0.0f, 0.1f + boss.level().random.nextFloat() * 0.1f, 0.25f, false, 1, 5, 1, 6 + boss.level().random.nextInt(6), 0, 2.0f + boss.level().random.nextFloat(), 0.5f, 1);
    }
}
