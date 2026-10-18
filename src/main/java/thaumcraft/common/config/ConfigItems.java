package thaumcraft.common.config;

import thaumcraft.common.items.consumables.ItemBathSalts;
import thaumcraft.common.items.consumables.ItemZombieBrain;
import thaumcraft.common.registry.TCFluids;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Items;
import thaumcraft.common.items.consumables.ItemAlumentum;
import thaumcraft.common.items.tools.ItemResonator;
import thaumcraft.common.items.tools.ItemHandMirror;
import thaumcraft.common.items.curios.ItemPrimordialPearl;

import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.common.items.casters.ItemCaster;
import thaumcraft.common.items.curios.ItemThaumonomicon;
import thaumcraft.Config;
import net.minecraft.world.item.Rarity;
import thaumcraft.common.items.consumables.ItemLabel;
import thaumcraft.common.items.consumables.ItemPhial;
import thaumcraft.common.items.consumables.ItemResearchNote;
import thaumcraft.common.items.tools.ItemScribingTools;
import thaumcraft.common.items.tools.ItemThaumometer;
import thaumcraft.common.items.resources.ItemCrystalEssence;
import thaumcraft.common.items.resources.ItemMagicDust;

/**
 * Port of ConfigItems. Field names follow thaumcraft.api.items.ItemsTC.
 * <p>
 * 1.12.2 used one item with metadata variants ("ingot" 0-2, "nugget" 0-10). Since 1.13 metadata is gone,
 * so each variant is its own item: ingot_thaumium, nugget_iron, ... (same names as the original textures).
 */
public final class ConfigItems {
    private ConfigItems() {
    }

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Thaumcraft.MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Thaumcraft.MODID);

    public static final DeferredItem<Item> amber = ITEMS.registerSimpleItem("amber");
    public static final DeferredItem<Item> quicksilver = ITEMS.registerSimpleItem("quicksilver");

    /** "thaumium","void","brass" */
    public static final Map<String, DeferredItem<Item>> ingots = variants("ingot", "thaumium", "void", "brass");
    /** "iron","copper","tin","silver","lead","quicksilver","thaumium","void","brass","quartz","rareearth" */
    public static final Map<String, DeferredItem<Item>> nuggets = variants("nugget",
            "iron", "copper", "tin", "silver", "lead", "quicksilver", "thaumium", "void", "brass", "quartz", "rareearth");

    /** "brass","iron","thaumium","void" */
    public static final Map<String, DeferredItem<Item>> plate = variants("plate", "brass", "iron", "thaumium", "void");
    public static final DeferredItem<Item> mechanismSimple = ITEMS.registerSimpleItem("mechanism_simple");
    public static final DeferredItem<Item> mechanismComplex = ITEMS.registerSimpleItem("mechanism_complex");
    public static final DeferredItem<Item> visResonator = ITEMS.registerSimpleItem("vis_resonator");
    public static final DeferredItem<Item> mirroredGlass = ITEMS.registerSimpleItem("mirrored_glass");
    public static final DeferredItem<Item> morphicResonator = ITEMS.registerSimpleItem("morphic_resonator");
    public static final DeferredItem<Item> fabric = ITEMS.registerSimpleItem("fabric");
    public static final DeferredItem<Item> filter = ITEMS.registerSimpleItem("filter");
    public static final DeferredItem<Item> jarBrace = ITEMS.registerSimpleItem("jar_brace");
    public static final DeferredItem<ItemPhial> phial = ITEMS.registerItem("phial", ItemPhial::new);
    public static final DeferredItem<ItemLabel> label = ITEMS.registerItem("label", ItemLabel::new);

    public static final DeferredItem<ItemScribingTools> scribingTools = ITEMS.registerItem("scribing_tools", ItemScribingTools::new);
    public static final DeferredItem<ItemResearchNote> researchNote = ITEMS.registerItem("research_note", ItemResearchNote::new);

    public static final DeferredItem<ItemMagicDust> salisMundus = ITEMS.registerItem("salis_mundus", ItemMagicDust::new);

    public static final DeferredItem<Item> tallow = ITEMS.registerSimpleItem("tallow");
    public static final DeferredItem<ItemBathSalts> bathSalts = ITEMS.registerItem("bath_salts", ItemBathSalts::new);
    public static final DeferredItem<ItemZombieBrain> brain = ITEMS.registerItem("brain", ItemZombieBrain::new);
    /** FluidRegistry.addBucketForFluid(FluidPure): the universal bucket of the original is a plain bucket item now. */
    public static final DeferredItem<BucketItem> purifyingFluidBucket = ITEMS.registerItem("purifying_fluid_bucket",
            p -> new BucketItem(TCFluids.PURIFYING.get(), p.craftRemainder(Items.BUCKET).stacksTo(1)));
    public static final DeferredItem<ItemThaumometer> thaumometer = ITEMS.registerItem("thaumometer", ItemThaumometer::new);
    public static final DeferredItem<ItemResonator> resonator = ITEMS.registerItem("resonator", ItemResonator::new);
    public static final DeferredItem<ItemAlumentum> alumentum = ITEMS.registerItem("alumentum", ItemAlumentum::new);
    public static final DeferredItem<ItemCaster> casterBasic = ITEMS.registerItem("caster_basic", p -> new ItemCaster(0, p));
    public static final DeferredItem<thaumcraft.common.items.casters.ItemFocus> focus1 =
            ITEMS.registerItem("focus_1", p -> new thaumcraft.common.items.casters.ItemFocus(15, p));
    public static final DeferredItem<thaumcraft.common.items.casters.ItemFocus> focus2 =
            ITEMS.registerItem("focus_2", p -> new thaumcraft.common.items.casters.ItemFocus(25, p));
    public static final DeferredItem<thaumcraft.common.items.casters.ItemFocus> focus3 =
            ITEMS.registerItem("focus_3", p -> new thaumcraft.common.items.casters.ItemFocus(50, p));
    public static final DeferredItem<thaumcraft.common.items.casters.ItemFocusPouch> focusPouch =
            ITEMS.registerItem("focus_pouch", thaumcraft.common.items.casters.ItemFocusPouch::new);

    public static final DeferredItem<ItemThaumonomicon> thaumonomicon = ITEMS.registerItem("thaumonomicon",
            p -> new ItemThaumonomicon(false, p.rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<ItemThaumonomicon> thaumonomiconCheat = ITEMS.registerItem("thaumonomicon_cheat",
            p -> new ItemThaumonomicon(true, p.rarity(Rarity.EPIC)));

    public static final DeferredItem<ItemCrystalEssence> crystalEssence = ITEMS.registerItem("crystal_essence", ItemCrystalEssence::new);

    // Stage 6b.
    public static final DeferredItem<ItemPrimordialPearl> primordialPearl = ITEMS.registerItem("primordial_pearl", ItemPrimordialPearl::new);
    public static final DeferredItem<Item> voidSeed = ITEMS.registerSimpleItem("void_seed");
    /** "clockwork","biothaumic" */
    public static final Map<String, DeferredItem<Item>> mind = variants("mind", "clockwork", "biothaumic");
    public static final DeferredItem<ItemHandMirror> handMirror = ITEMS.registerItem("hand_mirror", ItemHandMirror::new);

    // Stage 9c.
    public static final DeferredItem<thaumcraft.common.items.curios.ItemLootBag> lootBagCommon = ITEMS.registerItem("loot_bag_common",
            p -> new thaumcraft.common.items.curios.ItemLootBag(0, p));
    public static final DeferredItem<thaumcraft.common.items.curios.ItemLootBag> lootBagUncommon = ITEMS.registerItem("loot_bag_uncommon",
            p -> new thaumcraft.common.items.curios.ItemLootBag(1, p));
    public static final DeferredItem<thaumcraft.common.items.curios.ItemLootBag> lootBagRare = ITEMS.registerItem("loot_bag_rare",
            p -> new thaumcraft.common.items.curios.ItemLootBag(2, p));
    public static final DeferredItem<thaumcraft.common.items.curios.ItemPechWand> pechWand = ITEMS.registerItem("pech_wand",
            thaumcraft.common.items.curios.ItemPechWand::new);

    // Stage 9b.
    public static final DeferredItem<thaumcraft.common.items.consumables.ItemBottleTaint> bottleTaint =
            ITEMS.registerItem("bottle_taint", thaumcraft.common.items.consumables.ItemBottleTaint::new);
    /** The 1.12 spawn eggs were one item; now each mob has its own (same colors as ConfigEntities). */
    public static final DeferredItem<net.neoforged.neoforge.common.DeferredSpawnEggItem> taintSeedSpawnEgg = ITEMS.registerItem("taint_seed_spawn_egg",
            p -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(thaumcraft.common.registry.TCEntities.TAINT_SEED, 10618530, 4465237, p));
    public static final DeferredItem<net.neoforged.neoforge.common.DeferredSpawnEggItem> taintSeedPrimeSpawnEgg = ITEMS.registerItem("taint_seed_prime_spawn_egg",
            p -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(thaumcraft.common.registry.TCEntities.TAINT_SEED_PRIME, 10618530, 5583718, p));
    public static final DeferredItem<net.neoforged.neoforge.common.DeferredSpawnEggItem> taintCrawlerSpawnEgg = ITEMS.registerItem("taint_crawler_spawn_egg",
            p -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(thaumcraft.common.registry.TCEntities.TAINT_CRAWLER, 10618530, 3158064, p));
    public static final DeferredItem<net.neoforged.neoforge.common.DeferredSpawnEggItem> taintacleSpawnEgg = ITEMS.registerItem("taintacle_spawn_egg",
            p -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(thaumcraft.common.registry.TCEntities.TAINTACLE, 10618530, 4469572, p));
    public static final DeferredItem<net.neoforged.neoforge.common.DeferredSpawnEggItem> taintSwarmSpawnEgg = ITEMS.registerItem("taint_swarm_spawn_egg",
            p -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(thaumcraft.common.registry.TCEntities.TAINT_SWARM, 10618530, 16744576, p));
    public static final DeferredItem<net.neoforged.neoforge.common.DeferredSpawnEggItem> taintacleGiantSpawnEgg = ITEMS.registerItem("taintacle_giant_spawn_egg",
            p -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(thaumcraft.common.registry.TCEntities.TAINTACLE_GIANT, 6842578, 10618530, p));

    // Stage 9c-2.
    public static final DeferredItem<net.neoforged.neoforge.common.DeferredSpawnEggItem> thaumSlimeSpawnEgg = ITEMS.registerItem("thaum_slime_spawn_egg",
            p -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(thaumcraft.common.registry.TCEntities.THAUMIC_SLIME, 10618530, -32513, p));
    public static final DeferredItem<net.neoforged.neoforge.common.DeferredSpawnEggItem> brainyZombieSpawnEgg = ITEMS.registerItem("brainy_zombie_spawn_egg",
            p -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(thaumcraft.common.registry.TCEntities.BRAINY_ZOMBIE, -16129, -16744448, p));
    public static final DeferredItem<net.neoforged.neoforge.common.DeferredSpawnEggItem> giantBrainyZombieSpawnEgg = ITEMS.registerItem("giant_brainy_zombie_spawn_egg",
            p -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(thaumcraft.common.registry.TCEntities.GIANT_BRAINY_ZOMBIE, -16129, -16760832, p));
    public static final DeferredItem<net.neoforged.neoforge.common.DeferredSpawnEggItem> wispSpawnEgg = ITEMS.registerItem("wisp_spawn_egg",
            p -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(thaumcraft.common.registry.TCEntities.WISP, -16129, -1, p));
    public static final DeferredItem<net.neoforged.neoforge.common.DeferredSpawnEggItem> fireBatSpawnEgg = ITEMS.registerItem("firebat_spawn_egg",
            p -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(thaumcraft.common.registry.TCEntities.FIRE_BAT, -16129, -806354944, p));
    public static final DeferredItem<net.neoforged.neoforge.common.DeferredSpawnEggItem> mindSpiderSpawnEgg = ITEMS.registerItem("mind_spider_spawn_egg",
            p -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(thaumcraft.common.registry.TCEntities.MIND_SPIDER, 4996656, 4473924, p));
    // Stage 9c-3.
    public static final DeferredItem<net.neoforged.neoforge.common.DeferredSpawnEggItem> pechSpawnEgg = ITEMS.registerItem("pech_spawn_egg",
            p -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(thaumcraft.common.registry.TCEntities.PECH, -16129, -12582848, p));

    // Stage 8a.
    public static final DeferredItem<thaumcraft.common.golems.ItemGolemPlacer> golemPlacer =
            ITEMS.registerItem("golem", thaumcraft.common.golems.ItemGolemPlacer::new);
    public static final DeferredItem<thaumcraft.common.golems.ItemGolemBell> golemBell =
            ITEMS.registerItem("golem_bell", thaumcraft.common.golems.ItemGolemBell::new);
    public static final DeferredItem<Item> moduleVision = ITEMS.registerSimpleItem("module_vision");
    public static final DeferredItem<Item> moduleAggression = ITEMS.registerSimpleItem("module_aggression");

    // Stage 8b.
    /** Every seal, in the order of the original registration. */
    public static final Map<String, DeferredItem<thaumcraft.common.golems.seals.ItemSealPlacer>> seals = sealVariants("blank",
            "pickup", "pickup_advanced", "fill", "fill_advanced", "empty", "empty_advanced", "harvest", "butcher", "guard",
            "guard_advanced", "lumber", "breaker", "use", "provider", "stock", "breaker_advanced");

    /** Port of CreativeTabThaumcraft. TODO icon: the original uses the goggles, not ported yet. */
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TABTC = CREATIVE_TABS.register("thaumcraft",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.thaumcraft"))
                    .icon(() -> new ItemStack(ingots.get("thaumium").get()))
                    .displayItems((params, output) -> {
                        for (DeferredHolder<Item, ? extends Item> item : ITEMS.getEntries()) {
                            if (item.get() instanceof ItemCrystalEssence crystal) {
                                for (Aspect aspect : Aspect.aspects.values()) output.accept(crystal.makeCrystal(aspect, 1));
                            } else if (item.get() instanceof ItemPhial) {
                                // ItemPhial.getSubItems: an empty phial, then a filled one per aspect.
                                output.accept(item.get());
                                for (Aspect aspect : Aspect.aspects.values()) output.accept(ItemPhial.makeFilledPhial(aspect));
                            } else if (item.get() instanceof ItemThaumonomicon book && book.isCheat()) {
                                // getSubItems: the cheat variant only with allowCheatSheet.
                                if (Config.ALLOW_CHEAT_SHEET.getAsBoolean()) output.accept(item.get());
                            } else if (item.get() instanceof thaumcraft.common.golems.ItemGolemPlacer) {
                                output.acceptAll(thaumcraft.common.golems.ItemGolemPlacer.creativeStacks(item.get()));
                            } else if (item.get() instanceof ItemResearchNote) {
                                // A blank note is useless: notes come from the thaumonomicon.
                            } else {
                                output.accept(item.get());
                            }
                        }
                    })
                    .build());

    /** One placer item per seal: seal_blank places nothing, seal_pickup places "thaumcraft:pickup" and so on. */
    private static Map<String, DeferredItem<thaumcraft.common.golems.seals.ItemSealPlacer>> sealVariants(String... names) {
        Map<String, DeferredItem<thaumcraft.common.golems.seals.ItemSealPlacer>> out = new LinkedHashMap<>();
        for (String name : names) {
            String key = name.equals("blank") ? null : Thaumcraft.MODID + ":" + name;
            out.put(name, ITEMS.registerItem("seal_" + name, p -> new thaumcraft.common.golems.seals.ItemSealPlacer(key, p)));
        }
        return out;
    }

    /** The seals, registered in ConfigItems.init in the original, in the same order. */
    public static void registerSeals() {
        thaumcraft.common.golems.seals.SealHandler.registerSeal(new thaumcraft.common.golems.seals.SealPickup());
        thaumcraft.common.golems.seals.SealHandler.registerSeal(new thaumcraft.common.golems.seals.SealPickupAdvanced());
        thaumcraft.common.golems.seals.SealHandler.registerSeal(new thaumcraft.common.golems.seals.SealFill());
        thaumcraft.common.golems.seals.SealHandler.registerSeal(new thaumcraft.common.golems.seals.SealFillAdvanced());
        thaumcraft.common.golems.seals.SealHandler.registerSeal(new thaumcraft.common.golems.seals.SealEmpty());
        thaumcraft.common.golems.seals.SealHandler.registerSeal(new thaumcraft.common.golems.seals.SealEmptyAdvanced());
        thaumcraft.common.golems.seals.SealHandler.registerSeal(new thaumcraft.common.golems.seals.SealHarvest());
        thaumcraft.common.golems.seals.SealHandler.registerSeal(new thaumcraft.common.golems.seals.SealButcher());
        thaumcraft.common.golems.seals.SealHandler.registerSeal(new thaumcraft.common.golems.seals.SealGuard());
        thaumcraft.common.golems.seals.SealHandler.registerSeal(new thaumcraft.common.golems.seals.SealGuardAdvanced());
        thaumcraft.common.golems.seals.SealHandler.registerSeal(new thaumcraft.common.golems.seals.SealLumber());
        thaumcraft.common.golems.seals.SealHandler.registerSeal(new thaumcraft.common.golems.seals.SealBreaker());
        thaumcraft.common.golems.seals.SealHandler.registerSeal(new thaumcraft.common.golems.seals.SealUse());
        thaumcraft.common.golems.seals.SealHandler.registerSeal(new thaumcraft.common.golems.seals.SealProvide());
        thaumcraft.common.golems.seals.SealHandler.registerSeal(new thaumcraft.common.golems.seals.SealStock());
        thaumcraft.common.golems.seals.SealHandler.registerSeal(new thaumcraft.common.golems.seals.SealBreakerAdvanced());
    }

    private static Map<String, DeferredItem<Item>> variants(String base, String... names) {
        Map<String, DeferredItem<Item>> out = new LinkedHashMap<>();
        for (String name : names) out.put(name, ITEMS.registerSimpleItem(base + "_" + name));
        return out;
    }

    /**
     * The focus elements, registered in ConfigItems.init in the original.
     * TODO (stage 7b): CLOUD, MINE, PLAN, SPELLBAT, RIFT and EXCHANGE.
     */
    public static void registerFocusElements() {
        thaumcraft.api.casters.FocusEngine.registerElement(thaumcraft.api.casters.FocusMediumRoot::new, foci("root"), 10066329);
        thaumcraft.api.casters.FocusEngine.registerElement(thaumcraft.common.items.casters.foci.FocusMediumTouch::new, foci("touch"), 11371909);
        thaumcraft.api.casters.FocusEngine.registerElement(thaumcraft.common.items.casters.foci.FocusMediumBolt::new, foci("bolt"), 11377029);
        thaumcraft.api.casters.FocusEngine.registerElement(thaumcraft.common.items.casters.foci.FocusMediumProjectile::new, foci("projectile"), 11382149);
        thaumcraft.api.casters.FocusEngine.registerElement(thaumcraft.common.items.casters.foci.FocusMediumCloud::new, foci("cloud"), 10071429);
        thaumcraft.api.casters.FocusEngine.registerElement(thaumcraft.common.items.casters.foci.FocusMediumMine::new, foci("mine"), 8760709);
        thaumcraft.api.casters.FocusEngine.registerElement(thaumcraft.common.items.casters.foci.FocusMediumPlan::new, foci("plan"), 8760728);
        thaumcraft.api.casters.FocusEngine.registerElement(thaumcraft.common.items.casters.foci.FocusMediumSpellBat::new, foci("spellbat"), 8760748);
        thaumcraft.api.casters.FocusEngine.registerElement(thaumcraft.common.items.casters.foci.FocusEffectFire::new, foci("fire"), 16734721);
        thaumcraft.api.casters.FocusEngine.registerElement(thaumcraft.common.items.casters.foci.FocusEffectFrost::new, foci("frost"), 14811135);
        thaumcraft.api.casters.FocusEngine.registerElement(thaumcraft.common.items.casters.foci.FocusEffectAir::new, foci("air"), 16777086);
        thaumcraft.api.casters.FocusEngine.registerElement(thaumcraft.common.items.casters.foci.FocusEffectEarth::new, foci("earth"), 5685248);
        thaumcraft.api.casters.FocusEngine.registerElement(thaumcraft.common.items.casters.foci.FocusEffectFlux::new, foci("flux"), 8388736);
        thaumcraft.api.casters.FocusEngine.registerElement(thaumcraft.common.items.casters.foci.FocusEffectBreak::new, foci("break"), 9063176);
        thaumcraft.api.casters.FocusEngine.registerElement(thaumcraft.common.items.casters.foci.FocusEffectRift::new, foci("rift"), 3084645);
        thaumcraft.api.casters.FocusEngine.registerElement(thaumcraft.common.items.casters.foci.FocusEffectExchange::new, foci("exchange"), 5735255);
        thaumcraft.api.casters.FocusEngine.registerElement(thaumcraft.common.items.casters.foci.FocusEffectCurse::new, foci("curse"), 6946821);
        thaumcraft.api.casters.FocusEngine.registerElement(thaumcraft.common.items.casters.foci.FocusEffectHeal::new, foci("heal"), 14548997);
        thaumcraft.api.casters.FocusEngine.registerElement(thaumcraft.common.items.casters.foci.FocusModScatter::new, foci("scatter"), 10066329);
        thaumcraft.api.casters.FocusEngine.registerElement(thaumcraft.common.items.casters.foci.FocusModSplitTarget::new, foci("split_target"), 10066329);
        thaumcraft.api.casters.FocusEngine.registerElement(thaumcraft.common.items.casters.foci.FocusModSplitTrajectory::new, foci("split_trajectory"), 10066329);
    }

    private static net.minecraft.resources.ResourceLocation foci(String name) {
        return net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/foci/" + name + ".png");
    }
}
