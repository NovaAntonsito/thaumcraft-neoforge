package thaumcraft.common.entities.monster.cult;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.entities.ai.combat.AICultistHurtByTarget;

/** The crimson knight: plate armor and a sword. */
public class EntityCultistKnight extends EntityCultist {
    public EntityCultistKnight(EntityType<? extends EntityCultistKnight> type, Level world) {
        super(type, world);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return EntityCultist.createAttributes().add(Attributes.MAX_HEALTH, 30.0);
    }

    /** initEntityAI. 1.21 has no EntityAIRestrictOpenDoor (it kept villagers inside at night). */
    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0, false));
        goalSelector.addGoal(5, new OpenDoorGoal(this, true));
        goalSelector.addGoal(6, new MoveTowardsRestrictionGoal(this, 0.8));
        goalSelector.addGoal(7, new RandomStrollGoal(this, 0.8));
        goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0f));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new AICultistHurtByTarget(this, true));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        // TODO: EntityEldritchGuardian (stage 9d-3) at priority 3.
        targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, AbstractIllager.class, true));
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance diff) {
        setItemSlot(EquipmentSlot.HEAD, new ItemStack(ConfigItems.crimsonPlateHelm.get()));
        setItemSlot(EquipmentSlot.CHEST, new ItemStack(ConfigItems.crimsonPlateChest.get()));
        setItemSlot(EquipmentSlot.LEGS, new ItemStack(ConfigItems.crimsonPlateLegs.get()));
        setItemSlot(EquipmentSlot.FEET, new ItemStack(ConfigItems.crimsonBoots.get()));
        if (random.nextFloat() < ((level().getDifficulty() == Difficulty.HARD) ? 0.05f : 0.01f)) {
            int i = random.nextInt(5);
            // TODO: the void sword and the thaumium sword are not ported yet; an iron sword until then.
            if (i == 0) {
                setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_SWORD));
                setItemSlot(EquipmentSlot.HEAD, new ItemStack(ConfigItems.crimsonRobeHelm.get()));
            } else {
                setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_SWORD));
                if (random.nextBoolean()) {
                    setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
                }
            }
        } else {
            setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_SWORD));
        }
    }

    /** EnchantmentHelper.addRandomEnchantment(rand, held, 5 + f * rand(18), false). */
    @Override
    protected void populateDefaultEquipmentEnchantments(ServerLevelAccessor level, RandomSource random, DifficultyInstance diff) {
        float f = diff.getSpecialMultiplier();
        ItemStack held = getMainHandItem();
        if (!held.isEmpty() && random.nextFloat() < 0.25f * f) {
            var registry = level.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
            setItemInHand(InteractionHand.MAIN_HAND, EnchantmentHelper.enchantItem(random, held, (int) (5.0f + f * random.nextInt(18)),
                    level.registryAccess(), registry.getTag(EnchantmentTags.IN_ENCHANTING_TABLE)));
        }
    }
}
