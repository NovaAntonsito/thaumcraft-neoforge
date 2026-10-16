package thaumcraft.common.config;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Locale;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.LlamaSpit;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.api.research.ScanAspect;
import thaumcraft.api.research.ScanBlock;
import thaumcraft.api.research.ScanEntity;
import thaumcraft.api.research.ScanItem;
import thaumcraft.api.research.ScanTag;
import thaumcraft.api.research.ScanningManager;
import thaumcraft.common.lib.research.ResearchManager;
import thaumcraft.common.lib.research.ResearchNoteManager;
import thaumcraft.common.lib.research.ScanEnchantment;
import thaumcraft.common.lib.research.ScanGeneric;
import thaumcraft.common.lib.research.ScanPotion;

/**
 * Port of ConfigResearch: the research categories and files.
 * TODO: warp items, periodic checks. Theorycraft is replaced by the TC4 research notes.
 * The category backgrounds were 1.12 jpgs; 1.21 only loads png textures, so they were converted.
 */
public final class ConfigResearch {
    public static final String[] TCCategories = {"BASICS", "ALCHEMY", "AUROMANCY", "ARTIFICE", "INFUSION", "GOLEMANCY", "ELDRITCH"};
    private static final ResourceLocation BACK_OVER = Thaumcraft.id("textures/gui/gui_research_back_over.png");
    private static boolean loaded;

    private ConfigResearch() {
    }

    /** init + postInit of the original: register the categories and research files, then parse them. */
    public static void init() {
        if (loaded) return;
        loaded = true;
        initCategories();
        initScannables();
        for (String cat : TCCategories) {
            ResearchManager.jsonLocs.add(Thaumcraft.id("research/" + cat.toLowerCase(Locale.ROOT)));
        }
        ResearchManager.jsonLocs.add(Thaumcraft.id("research/scans"));
        ResearchManager.jsonLocs.add(Thaumcraft.id("research/anomalies"));
        ResearchManager.parseAllResearch();
        // TC4 Research Port: Reborn: the THEORY knowledge stages become research notes.
        ResearchNoteManager.init();
    }

    /**
     * The things the thaumometer learns from. The "![aspect]" triggers come first, as the original added them
     * when each aspect was created.
     * TODO: the Thaumcraft mobs (wisp, pech, firebat, taint, cultists, golems, flux rift...), the ancient and eldritch stones,
     * primordial pearl, zombie brain, pech wand and void seed, once they are ported. ScanSky (celestial notes)
     * waits for the celestial notes item.
     */
    private static void initScannables() {
        for (Aspect aspect : Aspect.aspects.values()) {
            ScanningManager.addScannableThing(new ScanAspect("!" + aspect.getTag(), aspect));
        }
        ScanningManager.addScannableThing(new ScanGeneric());
        ScanningManager.addScannableThing(new thaumcraft.common.anomaly.ScanAnomaly());
        // Enchantments are data driven now; the vanilla ones are known up front.
        for (Field f : Enchantments.class.getFields()) {
            if (Modifier.isStatic(f.getModifiers()) && f.getType() == ResourceKey.class) {
                try {
                    @SuppressWarnings("unchecked")
                    ResourceKey<Enchantment> key = (ResourceKey<Enchantment>) f.get(null);
                    ScanningManager.addScannableThing(new ScanEnchantment(key));
                } catch (IllegalAccessException ignored) {
                }
            }
        }
        BuiltInRegistries.MOB_EFFECT.holders().forEach(h -> ScanningManager.addScannableThing(new ScanPotion(h)));
        ScanningManager.addScannableThing(new ScanEntity("f_SPIDER", Spider.class, true));
        ScanningManager.addScannableThing(new ScanEntity("f_BAT", Bat.class, true));
        ScanningManager.addScannableThing(new ScanEntity("f_FLY", Bat.class, true));
        ScanningManager.addScannableThing(new ScanEntity("f_FLY", Parrot.class, true));
        ScanningManager.addScannableThing(new ScanEntity("f_FLY", Ghast.class, true));
        ScanningManager.addScannableThing(new ScanEntity("f_FLY", Blaze.class, true));
        Block[] crystals = {ConfigBlocks.crystalAir.get(), ConfigBlocks.crystalFire.get(), ConfigBlocks.crystalWater.get(),
                ConfigBlocks.crystalEarth.get(), ConfigBlocks.crystalOrder.get(), ConfigBlocks.crystalEntropy.get(), ConfigBlocks.crystalTaint.get()};
        Block[] ores = new Block[crystals.length + 2];
        ores[0] = ConfigBlocks.oreAmber.get();
        ores[1] = ConfigBlocks.oreCinnabar.get();
        System.arraycopy(crystals, 0, ores, 2, crystals.length);
        ScanningManager.addScannableThing(new ScanBlock("ORE", ores));
        ScanningManager.addScannableThing(new ScanBlock("!OREAMBER", ConfigBlocks.oreAmber.get()));
        ScanningManager.addScannableThing(new ScanBlock("!ORECINNABAR", ConfigBlocks.oreCinnabar.get()));
        ScanningManager.addScannableThing(new ScanBlock("!ORECRYSTAL", crystals));
        ScanningManager.addScannableThing(new ScanBlock("PLANTS", ConfigBlocks.logGreatwood.get(), ConfigBlocks.logSilverwood.get(),
                ConfigBlocks.saplingGreatwood.get(), ConfigBlocks.saplingSilverwood.get(), ConfigBlocks.cinderpearl.get(),
                ConfigBlocks.shimmerleaf.get(), ConfigBlocks.vishroom.get()));
        ScanningManager.addScannableThing(new ScanBlock("!PLANTWOOD", ConfigBlocks.logGreatwood.get()));
        ScanningManager.addScannableThing(new ScanBlock("!PLANTWOOD", ConfigBlocks.logSilverwood.get()));
        ScanningManager.addScannableThing(new ScanBlock("!PLANTWOOD", ConfigBlocks.saplingGreatwood.get()));
        ScanningManager.addScannableThing(new ScanBlock("!PLANTWOOD", ConfigBlocks.saplingSilverwood.get()));
        ScanningManager.addScannableThing(new ScanBlock("!PLANTCINDERPEARL", ConfigBlocks.cinderpearl.get()));
        ScanningManager.addScannableThing(new ScanBlock("!PLANTSHIMMERLEAF", ConfigBlocks.shimmerleaf.get()));
        ScanningManager.addScannableThing(new ScanBlock("!PLANTVISHROOM", ConfigBlocks.vishroom.get()));
        ScanningManager.addScannableThing(new ScanEntity("f_toomuchflux", thaumcraft.common.entities.EntityFluxRift.class, true));
        ScanningManager.addScannableThing(new ScanEntity("!FluxRift", thaumcraft.common.entities.EntityFluxRift.class, true));
        ScanningManager.addScannableThing(new ScanItem("PRIMPEARL", new ItemStack(ConfigItems.primordialPearl.get())));
        ScanningManager.addScannableThing(new ScanItem("f_VOIDSEED", new ItemStack(ConfigItems.voidSeed.get())));
        ScanningManager.addScannableThing(new ScanItem("!DRAGONBREATH", new ItemStack(Items.DRAGON_BREATH)));
        ScanningManager.addScannableThing(new ScanItem("!TOTEMUNDYING", new ItemStack(Items.TOTEM_OF_UNDYING)));
        ScanningManager.addScannableThing(new ScanBlock("f_TELEPORT", Blocks.NETHER_PORTAL, Blocks.END_PORTAL, Blocks.END_PORTAL_FRAME));
        ScanningManager.addScannableThing(new ScanItem("f_TELEPORT", new ItemStack(Items.ENDER_PEARL)));
        ScanningManager.addScannableThing(new ScanEntity("f_TELEPORT", EnderMan.class, true));
        ScanningManager.addScannableThing(new ScanBlock("f_DISPENSER", Blocks.DISPENSER));
        ScanningManager.addScannableThing(new ScanItem("f_DISPENSER", new ItemStack(Blocks.DISPENSER)));
        ScanningManager.addScannableThing(new ScanItem("f_MATCLAY", new ItemStack(Items.CLAY_BALL)));
        // HARDENED_CLAY and STAINED_HARDENED_CLAY are the terracottas; ScanMaterial(Material.CLAY) was the clay block.
        Block[] clay = new Block[DyeColor.values().length + 2];
        clay[0] = Blocks.TERRACOTTA;
        clay[1] = Blocks.CLAY;
        for (DyeColor color : DyeColor.values()) {
            clay[color.getId() + 2] = BuiltInRegistries.BLOCK.get(ResourceLocation.withDefaultNamespace(color.getName() + "_terracotta"));
        }
        ScanningManager.addScannableThing(new ScanBlock("f_MATCLAY", clay));
        ScanningManager.addScannableThing(new ScanTag("f_MATIRON", Tags.Items.ORES_IRON, Tags.Items.INGOTS_IRON, Tags.Items.STORAGE_BLOCKS_IRON, cTag("plates/iron")));
        ScanningManager.addScannableThing(new ScanTag("f_MATBRASS", cTag("ingots/brass"), cTag("storage_blocks/brass"), cTag("plates/brass")));
        ScanningManager.addScannableThing(new ScanTag("f_MATTHAUMIUM", cTag("ingots/thaumium"), cTag("storage_blocks/thaumium"), cTag("plates/thaumium")));
        ScanningManager.addScannableThing(new ScanTag("f_MATVOID", cTag("ingots/void"), cTag("storage_blocks/void"), cTag("plates/void")));
        ScanningManager.addScannableThing(new ScanEntity("f_arrow", AbstractArrow.class, true));
        ScanningManager.addScannableThing(new ScanItem("f_arrow", new ItemStack(Items.ARROW)));
        ScanningManager.addScannableThing(new ScanEntity("f_fireball", AbstractHurtingProjectile.class, true));
        ScanningManager.addScannableThing(new ScanEntity("f_spit", LlamaSpit.class, true));
    }

    private static TagKey<Item> cTag(String path) {
        return ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", path));
    }

    private static void initCategories() {
        ResearchCategories.registerCategory("BASICS", null, new AspectList().add(Aspect.PLANT, 5).add(Aspect.ORDER, 5).add(Aspect.ENTROPY, 5).add(Aspect.AIR, 5).add(Aspect.FIRE, 5).add(Aspect.EARTH, 3).add(Aspect.WATER, 5),
                Thaumcraft.id("textures/items/thaumonomicon_cheat.png"), Thaumcraft.id("textures/gui/gui_research_back_1.png"), BACK_OVER);
        ResearchCategories.registerCategory("AUROMANCY", "UNLOCKAUROMANCY", new AspectList().add(Aspect.AURA, 20).add(Aspect.MAGIC, 20).add(Aspect.FLUX, 15).add(Aspect.CRYSTAL, 5).add(Aspect.COLD, 5).add(Aspect.AIR, 5),
                Thaumcraft.id("textures/research/cat_auromancy.png"), Thaumcraft.id("textures/gui/gui_research_back_2.png"), BACK_OVER);
        ResearchCategories.registerCategory("ALCHEMY", "UNLOCKALCHEMY", new AspectList().add(Aspect.ALCHEMY, 30).add(Aspect.FLUX, 10).add(Aspect.MAGIC, 10).add(Aspect.LIFE, 5).add(Aspect.AVERSION, 5).add(Aspect.DESIRE, 5).add(Aspect.WATER, 5),
                Thaumcraft.id("textures/research/cat_alchemy.png"), Thaumcraft.id("textures/gui/gui_research_back_3.png"), BACK_OVER);
        ResearchCategories.registerCategory("ARTIFICE", "UNLOCKARTIFICE", new AspectList().add(Aspect.MECHANISM, 10).add(Aspect.CRAFT, 10).add(Aspect.METAL, 10).add(Aspect.TOOL, 10).add(Aspect.ENERGY, 10).add(Aspect.LIGHT, 5).add(Aspect.FLIGHT, 5).add(Aspect.TRAP, 5).add(Aspect.FIRE, 5),
                Thaumcraft.id("textures/research/cat_artifice.png"), Thaumcraft.id("textures/gui/gui_research_back_4.png"), BACK_OVER);
        ResearchCategories.registerCategory("INFUSION", "UNLOCKINFUSION", new AspectList().add(Aspect.MAGIC, 30).add(Aspect.PROTECT, 10).add(Aspect.TOOL, 10).add(Aspect.FLUX, 5).add(Aspect.CRAFT, 5).add(Aspect.SOUL, 5).add(Aspect.EARTH, 3),
                Thaumcraft.id("textures/research/cat_infusion.png"), Thaumcraft.id("textures/gui/gui_research_back_7.png"), BACK_OVER);
        ResearchCategories.registerCategory("GOLEMANCY", "UNLOCKGOLEMANCY", new AspectList().add(Aspect.MAN, 20).add(Aspect.MOTION, 10).add(Aspect.MIND, 10).add(Aspect.MECHANISM, 10).add(Aspect.EXCHANGE, 5).add(Aspect.SENSES, 5).add(Aspect.BEAST, 5).add(Aspect.ORDER, 5),
                Thaumcraft.id("textures/research/cat_golemancy.png"), Thaumcraft.id("textures/gui/gui_research_back_5.png"), BACK_OVER);
        ResearchCategories.registerCategory("ELDRITCH", "UNLOCKELDRITCH", new AspectList().add(Aspect.ELDRITCH, 20).add(Aspect.DARKNESS, 10).add(Aspect.MAGIC, 5).add(Aspect.MIND, 5).add(Aspect.VOID, 5).add(Aspect.DEATH, 5).add(Aspect.UNDEAD, 5).add(Aspect.ENTROPY, 5),
                Thaumcraft.id("textures/research/cat_eldritch.png"), Thaumcraft.id("textures/gui/gui_research_back_6.png"), BACK_OVER);
    }
}
