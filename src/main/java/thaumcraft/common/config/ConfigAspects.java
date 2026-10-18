package thaumcraft.common.config;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import thaumcraft.Thaumcraft;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.ThaumcraftApi.EntityTagsNBT;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.common.aspects.ObjectAspects;
import thaumcraft.common.lib.crafting.ThaumcraftCraftingManager;

/**
 * Partial port of ConfigAspects. The item list itself is data now:
 * {@code data/thaumcraft/thaumcraft/aspects/vanilla.json} and {@code thaumcraft.json}.
 * What stays in code is the potion loop, which depended on the brewing recipes, and the entity aspects.
 */
public final class ConfigAspects {
    private ConfigAspects() {
    }

    /**
     * registerEntityTags: the aspects the thaumometer shows on creatures. The 1.12 entity names became entity
     * types; the elder guardian (an NBT flag on the guardian in 1.12) is its own type now.
     * TODO: the Thaumcraft creatures (flux rift, firebat, pech, thaumic slime, brainy zombie, the rest of the taint, eldritch
     * guardian, cultists, eldritch crab, wisp, golem...), once they are ported.
     */
    public static void registerEntityTags() {
        ThaumcraftApi.registerEntityTag(EntityType.ZOMBIE, new AspectList().add(Aspect.UNDEAD, 20).add(Aspect.MAN, 10).add(Aspect.EARTH, 5));
        ThaumcraftApi.registerEntityTag(EntityType.HUSK, new AspectList().add(Aspect.UNDEAD, 20).add(Aspect.MAN, 10).add(Aspect.FIRE, 5));
        ThaumcraftApi.registerEntityTag(EntityType.GIANT, new AspectList().add(Aspect.UNDEAD, 25).add(Aspect.MAN, 15).add(Aspect.EARTH, 10));
        ThaumcraftApi.registerEntityTag(EntityType.SKELETON, new AspectList().add(Aspect.UNDEAD, 20).add(Aspect.MAN, 5).add(Aspect.EARTH, 5));
        ThaumcraftApi.registerEntityTag(EntityType.WITHER_SKELETON, new AspectList().add(Aspect.UNDEAD, 25).add(Aspect.MAN, 5).add(Aspect.ENTROPY, 10));
        ThaumcraftApi.registerEntityTag(EntityType.CREEPER, new AspectList().add(Aspect.PLANT, 15).add(Aspect.FIRE, 15));
        ThaumcraftApi.registerEntityTag(EntityType.CREEPER, new AspectList().add(Aspect.PLANT, 15).add(Aspect.FIRE, 15).add(Aspect.ENERGY, 15), new EntityTagsNBT("powered", true));
        ThaumcraftApi.registerEntityTag(EntityType.HORSE, new AspectList().add(Aspect.BEAST, 15).add(Aspect.EARTH, 5).add(Aspect.AIR, 5));
        ThaumcraftApi.registerEntityTag(EntityType.DONKEY, new AspectList().add(Aspect.BEAST, 15).add(Aspect.EARTH, 5).add(Aspect.AIR, 5));
        ThaumcraftApi.registerEntityTag(EntityType.MULE, new AspectList().add(Aspect.BEAST, 15).add(Aspect.EARTH, 5).add(Aspect.AIR, 5));
        ThaumcraftApi.registerEntityTag(EntityType.SKELETON_HORSE, new AspectList().add(Aspect.BEAST, 5).add(Aspect.UNDEAD, 10).add(Aspect.EARTH, 5).add(Aspect.AIR, 5));
        ThaumcraftApi.registerEntityTag(EntityType.ZOMBIE_HORSE, new AspectList().add(Aspect.BEAST, 10).add(Aspect.UNDEAD, 5).add(Aspect.EARTH, 5).add(Aspect.AIR, 5));
        ThaumcraftApi.registerEntityTag(EntityType.PIG, new AspectList().add(Aspect.BEAST, 10).add(Aspect.EARTH, 10).add(Aspect.DESIRE, 5));
        ThaumcraftApi.registerEntityTag(EntityType.EXPERIENCE_ORB, new AspectList().add(Aspect.MIND, 10));
        ThaumcraftApi.registerEntityTag(EntityType.SHEEP, new AspectList().add(Aspect.BEAST, 10).add(Aspect.EARTH, 10));
        ThaumcraftApi.registerEntityTag(EntityType.COW, new AspectList().add(Aspect.BEAST, 15).add(Aspect.EARTH, 15));
        ThaumcraftApi.registerEntityTag(EntityType.MOOSHROOM, new AspectList().add(Aspect.BEAST, 15).add(Aspect.PLANT, 15).add(Aspect.EARTH, 15));
        ThaumcraftApi.registerEntityTag(EntityType.SNOW_GOLEM, new AspectList().add(Aspect.COLD, 10).add(Aspect.MAN, 5).add(Aspect.MECHANISM, 5).add(Aspect.MAGIC, 5));
        ThaumcraftApi.registerEntityTag(EntityType.OCELOT, new AspectList().add(Aspect.BEAST, 10).add(Aspect.ENTROPY, 10));
        ThaumcraftApi.registerEntityTag(EntityType.CHICKEN, new AspectList().add(Aspect.BEAST, 5).add(Aspect.FLIGHT, 5).add(Aspect.AIR, 5));
        ThaumcraftApi.registerEntityTag(EntityType.SQUID, new AspectList().add(Aspect.BEAST, 5).add(Aspect.WATER, 10));
        ThaumcraftApi.registerEntityTag(EntityType.WOLF, new AspectList().add(Aspect.BEAST, 15).add(Aspect.EARTH, 10).add(Aspect.AVERSION, 5));
        ThaumcraftApi.registerEntityTag(EntityType.BAT, new AspectList().add(Aspect.BEAST, 5).add(Aspect.FLIGHT, 5).add(Aspect.DARKNESS, 5));
        ThaumcraftApi.registerEntityTag(EntityType.SPIDER, new AspectList().add(Aspect.BEAST, 10).add(Aspect.ENTROPY, 10).add(Aspect.TRAP, 10));
        ThaumcraftApi.registerEntityTag(EntityType.SLIME, new AspectList().add(Aspect.LIFE, 10).add(Aspect.WATER, 10).add(Aspect.ALCHEMY, 5));
        ThaumcraftApi.registerEntityTag(EntityType.GHAST, new AspectList().add(Aspect.UNDEAD, 15).add(Aspect.FIRE, 15));
        ThaumcraftApi.registerEntityTag(EntityType.ZOMBIFIED_PIGLIN, new AspectList().add(Aspect.UNDEAD, 15).add(Aspect.FIRE, 15).add(Aspect.BEAST, 10));
        ThaumcraftApi.registerEntityTag(EntityType.ENDERMAN, new AspectList().add(Aspect.ELDRITCH, 10).add(Aspect.MOTION, 15).add(Aspect.DESIRE, 5));
        ThaumcraftApi.registerEntityTag(EntityType.CAVE_SPIDER, new AspectList().add(Aspect.BEAST, 5).add(Aspect.DEATH, 10).add(Aspect.TRAP, 10));
        ThaumcraftApi.registerEntityTag(EntityType.SILVERFISH, new AspectList().add(Aspect.BEAST, 5).add(Aspect.EARTH, 10));
        ThaumcraftApi.registerEntityTag(EntityType.BLAZE, new AspectList().add(Aspect.ELDRITCH, 5).add(Aspect.FIRE, 15).add(Aspect.FLIGHT, 5));
        ThaumcraftApi.registerEntityTag(EntityType.MAGMA_CUBE, new AspectList().add(Aspect.WATER, 5).add(Aspect.FIRE, 10).add(Aspect.ALCHEMY, 5));
        ThaumcraftApi.registerEntityTag(EntityType.ENDER_DRAGON, new AspectList().add(Aspect.ELDRITCH, 50).add(Aspect.BEAST, 30).add(Aspect.ENTROPY, 50).add(Aspect.FLIGHT, 10));
        ThaumcraftApi.registerEntityTag(EntityType.WITHER, new AspectList().add(Aspect.UNDEAD, 50).add(Aspect.ENTROPY, 25).add(Aspect.FIRE, 25));
        ThaumcraftApi.registerEntityTag(EntityType.WITCH, new AspectList().add(Aspect.MAN, 15).add(Aspect.MAGIC, 5).add(Aspect.ALCHEMY, 10));
        ThaumcraftApi.registerEntityTag(EntityType.VILLAGER, new AspectList().add(Aspect.MAN, 15));
        ThaumcraftApi.registerEntityTag(EntityType.IRON_GOLEM, new AspectList().add(Aspect.METAL, 15).add(Aspect.MAN, 5).add(Aspect.MECHANISM, 5).add(Aspect.MAGIC, 5));
        ThaumcraftApi.registerEntityTag(EntityType.END_CRYSTAL, new AspectList().add(Aspect.ELDRITCH, 15).add(Aspect.AURA, 15).add(Aspect.LIFE, 15));
        ThaumcraftApi.registerEntityTag(EntityType.ITEM_FRAME, new AspectList().add(Aspect.SENSES, 5).add(Aspect.CRAFT, 5));
        ThaumcraftApi.registerEntityTag(EntityType.PAINTING, new AspectList().add(Aspect.SENSES, 10).add(Aspect.CRAFT, 5));
        ThaumcraftApi.registerEntityTag(EntityType.GUARDIAN, new AspectList().add(Aspect.BEAST, 10).add(Aspect.ELDRITCH, 10).add(Aspect.WATER, 10));
        ThaumcraftApi.registerEntityTag(EntityType.ELDER_GUARDIAN, new AspectList().add(Aspect.BEAST, 10).add(Aspect.ELDRITCH, 15).add(Aspect.WATER, 15));
        ThaumcraftApi.registerEntityTag(EntityType.RABBIT, new AspectList().add(Aspect.BEAST, 5).add(Aspect.EARTH, 5).add(Aspect.MOTION, 5));
        ThaumcraftApi.registerEntityTag(EntityType.ENDERMITE, new AspectList().add(Aspect.BEAST, 5).add(Aspect.ELDRITCH, 5).add(Aspect.MOTION, 5));
        ThaumcraftApi.registerEntityTag(EntityType.POLAR_BEAR, new AspectList().add(Aspect.BEAST, 15).add(Aspect.COLD, 10));
        ThaumcraftApi.registerEntityTag(EntityType.SHULKER, new AspectList().add(Aspect.ELDRITCH, 10).add(Aspect.TRAP, 5).add(Aspect.FLIGHT, 5).add(Aspect.PROTECT, 5));
        ThaumcraftApi.registerEntityTag(EntityType.EVOKER, new AspectList().add(Aspect.ELDRITCH, 5).add(Aspect.MAGIC, 5).add(Aspect.MAN, 10));
        ThaumcraftApi.registerEntityTag(EntityType.VINDICATOR, new AspectList().add(Aspect.AVERSION, 5).add(Aspect.MAGIC, 5).add(Aspect.MAN, 10));
        ThaumcraftApi.registerEntityTag(EntityType.ILLUSIONER, new AspectList().add(Aspect.SENSES, 5).add(Aspect.MAGIC, 5).add(Aspect.MAN, 10));
        ThaumcraftApi.registerEntityTag(EntityType.LLAMA, new AspectList().add(Aspect.BEAST, 15).add(Aspect.WATER, 5));
        ThaumcraftApi.registerEntityTag(EntityType.PARROT, new AspectList().add(Aspect.BEAST, 5).add(Aspect.FLIGHT, 5).add(Aspect.SENSES, 5));
        ThaumcraftApi.registerEntityTag(EntityType.STRAY, new AspectList().add(Aspect.UNDEAD, 20).add(Aspect.MAN, 5).add(Aspect.TRAP, 5));
        ThaumcraftApi.registerEntityTag(EntityType.VEX, new AspectList().add(Aspect.ELDRITCH, 5).add(Aspect.FLIGHT, 5).add(Aspect.MAGIC, 5).add(Aspect.MAN, 5));
        ThaumcraftApi.registerEntityTag(thaumcraft.common.registry.TCEntities.TAINTACLE.get(), new AspectList().add(Aspect.FLUX, 15).add(Aspect.BEAST, 10));
        // The original registers the taint seeds twice; the second entries are the ones that count.
        ThaumcraftApi.registerEntityTag(thaumcraft.common.registry.TCEntities.TAINT_SEED.get(), new AspectList().add(Aspect.FLUX, 20).add(Aspect.AURA, 10).add(Aspect.PLANT, 5));
        ThaumcraftApi.registerEntityTag(thaumcraft.common.registry.TCEntities.TAINT_SEED_PRIME.get(), new AspectList().add(Aspect.FLUX, 25).add(Aspect.AURA, 15).add(Aspect.PLANT, 5));
        ThaumcraftApi.registerEntityTag(thaumcraft.common.registry.TCEntities.TAINTACLE_SMALL.get(), new AspectList().add(Aspect.FLUX, 5).add(Aspect.BEAST, 5));
        ThaumcraftApi.registerEntityTag(thaumcraft.common.registry.TCEntities.TAINT_SWARM.get(), new AspectList().add(Aspect.FLUX, 15).add(Aspect.AIR, 5));
        ThaumcraftApi.registerEntityTag(thaumcraft.common.registry.TCEntities.TAINT_SEED.get(), new AspectList().add(Aspect.PLANT, 20).add(Aspect.BEAST, 20).add(Aspect.FLUX, 20));
        ThaumcraftApi.registerEntityTag(thaumcraft.common.registry.TCEntities.TAINT_SEED_PRIME.get(), new AspectList().add(Aspect.PLANT, 30).add(Aspect.BEAST, 30).add(Aspect.FLUX, 30));
        ThaumcraftApi.registerEntityTag(thaumcraft.common.registry.TCEntities.TAINTACLE_GIANT.get(), new AspectList().add(Aspect.ELDRITCH, 40).add(Aspect.BEAST, 40).add(Aspect.FLUX, 40));
        ThaumcraftApi.registerEntityTag(thaumcraft.common.registry.TCEntities.FIRE_BAT.get(), new AspectList().add(Aspect.BEAST, 5).add(Aspect.FLIGHT, 5).add(Aspect.FIRE, 10));
        ThaumcraftApi.registerEntityTag(thaumcraft.common.registry.TCEntities.THAUMIC_SLIME.get(), new AspectList().add(Aspect.LIFE, 5).add(Aspect.WATER, 5).add(Aspect.FLUX, 5).add(Aspect.ALCHEMY, 5));
        ThaumcraftApi.registerEntityTag(thaumcraft.common.registry.TCEntities.BRAINY_ZOMBIE.get(), new AspectList().add(Aspect.UNDEAD, 20).add(Aspect.MAN, 10).add(Aspect.MIND, 5).add(Aspect.AVERSION, 5));
        ThaumcraftApi.registerEntityTag(thaumcraft.common.registry.TCEntities.GIANT_BRAINY_ZOMBIE.get(), new AspectList().add(Aspect.UNDEAD, 25).add(Aspect.MAN, 15).add(Aspect.MIND, 5).add(Aspect.AVERSION, 10));
        ThaumcraftApi.registerEntityTag(thaumcraft.common.registry.TCEntities.MIND_SPIDER.get(), new AspectList().add(Aspect.FLUX, 5).add(Aspect.FIRE, 5));
        for (Aspect tag : Aspect.aspects.values()) {
            ThaumcraftApi.registerEntityTag(thaumcraft.common.registry.TCEntities.WISP.get(), new AspectList().add(tag, 5).add(Aspect.AURA, 5).add(Aspect.FLIGHT, 5), new EntityTagsNBT("Type", tag.getTag()));
        }
    }

    /** Per item and potion, like the per-PotionType registerObjectTag calls of the original loop. */
    private static final Map<String, AspectList> POTION_CACHE = new ConcurrentHashMap<>();

    public static void clearPotionCache() {
        POTION_CACHE.clear();
    }

    /**
     * Aspects of a potion, splash/lingering potion or tipped arrow holding a potion, or null for anything else.
     * Port of the PotionType loop in registerItemAspects.
     */
    @Nullable
    public static AspectList getPotionTags(ItemStack stack) {
        Aspect extra;
        if (stack.is(Items.POTION)) {
            extra = Aspect.WATER;
        } else if (stack.is(Items.TIPPED_ARROW)) {
            extra = Aspect.AVERSION;
        } else if (stack.is(Items.SPLASH_POTION)) {
            extra = Aspect.ENERGY;
        } else if (stack.is(Items.LINGERING_POTION)) {
            extra = Aspect.TRAP;
        } else {
            return null;
        }
        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
        if (contents == null || contents.potion().isEmpty()) {
            return null;
        }
        Holder<Potion> potion = contents.potion().get();
        String key = stack.getItem() + "|" + potion.getRegisteredName();
        AspectList cached = POTION_CACHE.get(key);
        if (cached != null) {
            return cached;
        }
        AspectList out = getPotionAspects(potion).add(extra, 5);
        if (ObjectAspects.recipes() != null) {
            POTION_CACHE.put(key, out);
        }
        return out;
    }

    public static AspectList getPotionAspects(Holder<Potion> potion) {
        AspectList tmp = new AspectList();
        boolean didit = false;
        if (!potion.is(Potions.WATER)) {
            List<ItemStack> hs = new ArrayList<>();
            getPotionReagentsRecursive(potion, hs, 0);
            for (ItemStack his : hs) {
                AspectList tmp2 = ThaumcraftCraftingManager.getObjectTags(his);
                tmp.add(tmp2);
                didit = true;
                tmp.add(Aspect.ALCHEMY, 3);
            }
            for (Aspect a2 : tmp.getAspects()) {
                int amt = tmp.getAmount(a2);
                tmp.remove(a2, (int) (amt * 0.66));
            }
            if (!didit) {
                tmp.add(Aspect.MAGIC, 5);
                tmp.add(Aspect.ALCHEMY, 5);
            }
        } else {
            tmp.add(Aspect.WATER, 5);
        }
        return tmp;
    }

    /**
     * Port of ThaumcraftCraftingManager.getPotionReagentsRecursive: walks the brewing chain back to water,
     * collecting each step's reagent. The mix list is private in 1.21 too, so this still uses reflection.
     */
    private static void getPotionReagentsRecursive(Holder<Potion> potion, List<ItemStack> out, int depth) {
        PotionBrewing brewing = ObjectAspects.brewing();
        if (brewing == null || depth > 16) {
            return;
        }
        try {
            for (Object mix : potionMixes(brewing)) {
                Holder<?> to = (Holder<?>) MIX_TO.invoke(mix);
                if (to.value() != potion.value()) {
                    continue;
                }
                ItemStack[] reagents = ((Ingredient) MIX_INGREDIENT.invoke(mix)).getItems();
                if (reagents.length > 0) {
                    out.add(reagents[0]);
                }
                @SuppressWarnings("unchecked")
                Holder<Potion> from = (Holder<Potion>) MIX_FROM.invoke(mix);
                if (!from.is(Potions.WATER)) {
                    getPotionReagentsRecursive(from, out, depth + 1);
                }
                break;
            }
        } catch (ReflectiveOperationException | RuntimeException e) {
            Thaumcraft.LOGGER.debug("Could not read brewing recipes", e);
        }
    }

    private static Field POTION_MIXES;
    private static Method MIX_FROM;
    private static Method MIX_INGREDIENT;
    private static Method MIX_TO;

    private static List<?> potionMixes(PotionBrewing brewing) throws ReflectiveOperationException {
        if (POTION_MIXES == null) {
            Field field = PotionBrewing.class.getDeclaredField("potionMixes");
            field.setAccessible(true);
            Class<?> mixClass = Class.forName("net.minecraft.world.item.alchemy.PotionBrewing$Mix");
            MIX_FROM = accessible(mixClass.getDeclaredMethod("from"));
            MIX_INGREDIENT = accessible(mixClass.getDeclaredMethod("ingredient"));
            MIX_TO = accessible(mixClass.getDeclaredMethod("to"));
            POTION_MIXES = field;
        }
        return (List<?>) POTION_MIXES.get(brewing);
    }

    private static Method accessible(Method method) {
        method.setAccessible(true);
        return method;
    }
}
