package thaumcraft.common.lib.events;

import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.util.Mth;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.item.ItemExpireEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import thaumcraft.Config;
import thaumcraft.Thaumcraft;
import thaumcraft.api.entities.IEldritchMob;
import thaumcraft.api.entities.ITaintedMob;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.entities.construct.EntityOwnedConstruct;
import thaumcraft.common.entities.monster.EntityBrainyZombie;
import thaumcraft.common.entities.monster.mods.ChampionModTainted;
import thaumcraft.common.entities.monster.mods.ChampionModifier;
import thaumcraft.common.entities.monster.tainted.EntityTaintacle;
import thaumcraft.common.items.consumables.ItemBathSalts;
import thaumcraft.common.lib.utils.EntityUtils;
import thaumcraft.common.registry.TCAttributes;
import thaumcraft.common.registry.TCMobEffects;

/**
 * Partial port of EntityEvents: bath salts that dissolve in a still water source turn it into purifying fluid, and the
 * champion mobs (spawn chance, modifier effects, loot bags) and tainted mobs.
 * TODO: the player hurt research (f_onfire, f_arrow...), fortress mask, runic shield FX, cultist drops and dissolve.
 */
@EventBusSubscriber(modid = Thaumcraft.MODID)
public final class EntityEvents {
    /**
     * ConfigEntities.championModWhitelist: the mobs that can spawn as champions and how much likelier they are
     * (level - 1 is taken off the roll).
     */
    public static final Map<Class<?>, Integer> championModWhitelist = new LinkedHashMap<>();

    static {
        championModWhitelist.put(Zombie.class, 0);
        championModWhitelist.put(Spider.class, 0);
        championModWhitelist.put(Blaze.class, 0);
        championModWhitelist.put(EnderMan.class, 0);
        championModWhitelist.put(Skeleton.class, 0);
        championModWhitelist.put(Witch.class, 1);
        championModWhitelist.put(thaumcraft.common.entities.monster.EntityEldritchCrab.class, 0);
        championModWhitelist.put(EntityTaintacle.class, 2);
        championModWhitelist.put(thaumcraft.common.entities.monster.EntityInhabitedZombie.class, 3);
    }

    private EntityEvents() {
    }

    @SubscribeEvent
    public static void itemExpire(ItemExpireEvent event) {
        ItemEntity item = event.getEntity();
        if (item.getItem().getItem() instanceof ItemBathSalts) {
            Level level = item.level();
            BlockPos bp = item.blockPosition();
            BlockState bs = level.getBlockState(bp);
            if (bs.is(Blocks.WATER) && bs.getFluidState().isSource()) {
                level.setBlockAndUpdate(bp, ConfigBlocks.purifyingFluid.get().defaultBlockState());
            }
        }
    }

    @SubscribeEvent
    public static void livingTick(EntityTickEvent.Pre event) {
        if (event.getEntity() instanceof PathfinderMob mob && !(mob instanceof EntityOwnedConstruct) && mob.isAlive()) {
            int t = EntityUtils.getChampionMod(mob);
            if (t >= ChampionModifier.mods.length) {
                mob.discard();
                return;
            }
            if (mob.level().isClientSide) {
                // RenderEventHandler.livingTick of the original.
                if (t >= 0) {
                    ChampionModifier.mods[t].effect.showFX(mob);
                }
            } else if (t >= 0 && ChampionModifier.mods[t].type == 0) {
                ChampionModifier.mods[t].effect.performEffect(mob, null, null, 0.0f);
            }
        }
    }

    @SubscribeEvent
    public static void entityHurt(LivingIncomingDamageEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof Player || entity.level().isClientSide) {
            return;
        }
        if (entity.getHealth() < 2.0f && !entity.isInvertedHealAndHarm() && entity.isAlive() && !(entity instanceof EntityOwnedConstruct)
                && !(entity instanceof ITaintedMob) && entity.hasEffect(TCMobEffects.FLUX_TAINT) && entity.getRandom().nextBoolean()) {
            EntityUtils.makeTainted(entity);
            return;
        }
        if (entity instanceof Monster mob) {
            int t = EntityUtils.getChampionMod(mob);
            Entity source = event.getSource().getEntity();
            if (t >= 0 || mob instanceof IEldritchMob) {
                if ((t == 5 || mob instanceof IEldritchMob) && mob.getAbsorptionAmount() > 0.0f) {
                    // TODO: PacketFXShield and the runicShieldCharge sound (runic shielding, stage 10).
                } else if (t >= 0 && ChampionModifier.mods[t].type == 2 && source instanceof LivingEntity attacker) {
                    event.setAmount(ChampionModifier.mods[t].effect.performEffect(mob, attacker, event.getSource(), event.getAmount()));
                }
            }
            // As in the original this only runs when the victim is a monster too.
            if (event.getAmount() > 0.0f && source instanceof Monster attacker) {
                int ta = EntityUtils.getChampionMod(attacker);
                if (ta >= 0 && ta < ChampionModifier.mods.length && ChampionModifier.mods[ta].type == 1) {
                    event.setAmount(ChampionModifier.mods[ta].effect.performEffect(attacker, mob, event.getSource(), event.getAmount()));
                }
            }
        }
    }

    /**
     * The fortress masks of entityHurt: the sipping fiend heals its wearer a little when they strike, the angry ghost
     * withers whoever strikes its wearer. The chance grows with the damage.
     * Then the part of the original ISpecialArmor that vanilla armor has no counterpart for: fortress and void robe
     * pieces stop defense/35 of magic damage each.
     */
    @SubscribeEvent
    public static void gearHurt(LivingIncomingDamageEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide) {
            return;
        }
        if (event.getSource().getEntity() instanceof Player leecher
                && thaumcraft.common.items.armor.ItemFortressArmor.getMask(leecher.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD)) == 2
                && leecher.level().random.nextFloat() < event.getAmount() / 12.0f) {
            leecher.heal(1.0f);
        }
        if (entity instanceof Player player && event.getSource().getEntity() instanceof LivingEntity attacker
                && thaumcraft.common.items.armor.ItemFortressArmor.getMask(player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD)) == 1
                && player.level().random.nextFloat() < event.getAmount() / 10.0f) {
            attacker.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.WITHER, 80));
        }
        if (event.getSource().is(net.minecraft.tags.DamageTypeTags.WITCH_RESISTANT_TO)
                && !event.getSource().is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            double ratio = 0.0;
            for (ItemStack piece : entity.getArmorSlots()) {
                if (piece.getItem() instanceof thaumcraft.common.items.armor.ItemFortressArmor
                        || piece.getItem() instanceof thaumcraft.common.items.armor.ItemVoidRobeArmor) {
                    ratio += ((net.minecraft.world.item.ArmorItem) piece.getItem()).getDefense() / 35.0;
                }
            }
            if (ratio > 0.0) {
                event.setAmount((float) (event.getAmount() * (1.0 - Math.min(1.0, ratio))));
            }
        }
    }

    /**
     * The set bonus of ItemFortressArmor.getProperties: every fortress piece worn adds 1 armor and 1 toughness,
     * and 1 more armor for each masked piece. 1.21: transient attribute modifiers, refreshed when the armor changes.
     */
    @SubscribeEvent
    public static void fortressSet(net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent event) {
        if (!event.getSlot().isArmor()) {
            return;
        }
        LivingEntity entity = event.getEntity();
        int q = 0;
        int masks = 0;
        for (net.minecraft.world.entity.EquipmentSlot slot : new net.minecraft.world.entity.EquipmentSlot[] {
                net.minecraft.world.entity.EquipmentSlot.LEGS, net.minecraft.world.entity.EquipmentSlot.CHEST, net.minecraft.world.entity.EquipmentSlot.HEAD }) {
            ItemStack piece = entity.getItemBySlot(slot);
            if (piece.getItem() instanceof thaumcraft.common.items.armor.ItemFortressArmor) {
                ++q;
                if (piece.has(thaumcraft.common.registry.TCDataComponents.FORTRESS_MASK.get())) {
                    ++masks;
                }
            }
        }
        setBonus(entity.getAttribute(Attributes.ARMOR), FORTRESS_ARMOR, q * (1 + masks));
        setBonus(entity.getAttribute(Attributes.ARMOR_TOUGHNESS), FORTRESS_TOUGHNESS, q);
    }

    private static final net.minecraft.resources.ResourceLocation FORTRESS_ARMOR = Thaumcraft.id("fortress_set_armor");
    private static final net.minecraft.resources.ResourceLocation FORTRESS_TOUGHNESS = Thaumcraft.id("fortress_set_toughness");

    private static void setBonus(AttributeInstance attribute, net.minecraft.resources.ResourceLocation id, int amount) {
        if (attribute == null) {
            return;
        }
        attribute.removeModifier(id);
        if (amount > 0) {
            attribute.addTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(id, amount,
                    net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE));
        }
    }

    @SubscribeEvent
    public static void livingDrops(LivingDropsEvent event) {
        LivingEntity entity = event.getEntity();
        Level world = entity.level();
        if (world.isClientSide) return;
        Entity killer = event.getSource().getEntity();
        boolean fakeplayer = killer instanceof FakePlayer;
        int looting = EntityUtils.getLootingLevel(world, event.getSource());
        int t = EntityUtils.getChampionMod(entity);
        if (event.isRecentlyHit() && !fakeplayer && entity instanceof Monster && !(entity instanceof thaumcraft.common.entities.monster.boss.EntityThaumcraftBoss)
                && t >= 0 && t != 13) {
            int i = 5 + world.random.nextInt(3);
            while (i > 0) {
                int j = ExperienceOrb.getExperienceValue(i);
                i -= j;
                world.addFreshEntity(new ExperienceOrb(world, entity.getX(), entity.getY(), entity.getZ(), j));
            }
            int lb = Math.min(2, Mth.floor((world.random.nextInt(9) + looting) / 5.0f));
            ItemStack bag = new ItemStack(lb == 2 ? ConfigItems.lootBagRare.get() : lb == 1 ? ConfigItems.lootBagUncommon.get() : ConfigItems.lootBagCommon.get());
            event.getDrops().add(new ItemEntity(world, entity.getX(), entity.getY() + entity.getEyeHeight(), entity.getZ(), bag));
        }
        if (entity instanceof Zombie && !(entity instanceof EntityBrainyZombie) && event.isRecentlyHit() && world.random.nextInt(10) - looting < 1) {
            event.getDrops().add(new ItemEntity(world, entity.getX(), entity.getY() + entity.getEyeHeight(), entity.getZ(),
                    new ItemStack(ConfigItems.brain.get())));
        }
        // The crimson rites: 1 in 4, 1 in 20 once the cult is researched, 1 in 50 while carrying a copy.
        if (entity instanceof thaumcraft.common.entities.monster.cult.EntityCultist && !fakeplayer && killer instanceof net.minecraft.world.entity.player.Player p) {
            int c = thaumcraft.api.capabilities.ThaumcraftCapabilities.getKnowledge(p).isResearchKnown("!CrimsonCultist@2") ? 20 : 4;
            if (p.getInventory().contains(new ItemStack(ConfigItems.crimsonRites.get()))) {
                c = 50;
            }
            if (world.random.nextInt(c) == 0) {
                event.getDrops().add(new ItemEntity(world, entity.getX(), entity.getY() + entity.getEyeHeight(), entity.getZ(),
                        new ItemStack(ConfigItems.crimsonRites.get())));
            }
        }
    }

    @SubscribeEvent
    public static void entitySpawns(EntityJoinLevelEvent event) {
        Level world = event.getLevel();
        if (world.isClientSide) return;
        if (event.getEntity() instanceof PathfinderMob creature && EntityUtils.getChampionMod(creature) == 13) {
            AttributeInstance modai = creature.getAttribute(TCAttributes.TAINTED_MOD);
            if (modai != null) {
                ChampionModTainted.setTaintedAI(modai, 0.0);
            }
        }
        if (event.getEntity() instanceof Monster mob) {
            AttributeInstance cai = mob.getAttribute(TCAttributes.CHAMPION_MOD);
            if (cai != null && cai.getValue() < -1.0) {
                boolean allow = Config.ALLOW_CHAMPION_MOBS.get();
                int c = world.random.nextInt(100);
                if (world.getDifficulty() == Difficulty.EASY || !allow) {
                    c += 2;
                }
                if (world.getDifficulty() == Difficulty.HARD) {
                    c -= (allow ? 2 : 0);
                }
                // TODO: -3 in the Outer Lands.
                Holder<Biome> bg = world.getBiome(mob.blockPosition());
                if (bg.is(Tags.Biomes.IS_SPOOKY) || bg.is(BiomeTags.IS_NETHER) || bg.is(BiomeTags.IS_END)) {
                    c -= (allow ? 2 : 1);
                }
                int cc = 0;
                boolean whitelisted = false;
                for (Map.Entry<Class<?>, Integer> e : championModWhitelist.entrySet()) {
                    if (e.getKey().isAssignableFrom(mob.getClass())) {
                        whitelisted = true;
                        if (!allow && !(mob instanceof thaumcraft.common.entities.monster.boss.EntityThaumcraftBoss)) {
                            continue;
                        }
                        cc = Math.max(cc, e.getValue() - 1);
                    }
                }
                c -= cc;
                if (whitelisted && c <= 0 && mob.getAttribute(Attributes.MAX_HEALTH).getBaseValue() >= 10.0) {
                    EntityUtils.makeChampion(mob, false);
                } else {
                    cai.removeModifier(ChampionModifier.ATTRIBUTE_MOD_NONE.id());
                    cai.addPermanentModifier(ChampionModifier.ATTRIBUTE_MOD_NONE);
                }
            }
        }
    }
}
