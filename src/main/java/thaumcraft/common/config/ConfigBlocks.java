package thaumcraft.common.config;

import thaumcraft.common.blocks.devices.BlockArcaneEar;
import thaumcraft.common.blocks.devices.BlockRedstoneRelay;
import thaumcraft.common.blocks.devices.BlockLevitator;
import thaumcraft.common.blocks.devices.BlockHungryChest;
import thaumcraft.common.blocks.devices.BlockSpa;
import thaumcraft.common.blocks.devices.BlockPotionSprayer;
import thaumcraft.common.blocks.devices.BlockWaterJug;
import thaumcraft.common.blocks.essentia.BlockJarBrain;
import thaumcraft.common.blocks.essentia.BlockJarBrainItem;
import thaumcraft.common.blocks.misc.BlockFluidPure;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import thaumcraft.common.blocks.basic.BlockCandle;
import thaumcraft.common.blocks.basic.BlockPillar;
import thaumcraft.common.blocks.basic.BlockTable;
import thaumcraft.common.blocks.crafting.BlockResearchTable;
import thaumcraft.common.blocks.crafting.BlockDeconstructionTable;
import thaumcraft.common.blocks.crafting.BlockInfusionMatrix;
import thaumcraft.common.blocks.devices.BlockPedestal;
import thaumcraft.common.blocks.misc.BlockEffect;
import thaumcraft.common.blocks.misc.BlockNitor;
import thaumcraft.common.blocks.misc.BlockPlaceholder;
import thaumcraft.common.blocks.devices.BlockLamp;
import thaumcraft.common.blocks.devices.BlockInfernalFurnace;
import thaumcraft.common.blocks.devices.BlockCondenser;
import thaumcraft.common.blocks.devices.BlockCondenserLattice;
import thaumcraft.common.blocks.devices.BlockInlay;
import thaumcraft.common.blocks.devices.BlockStabilizer;
import thaumcraft.common.blocks.devices.BlockVisGenerator;
import thaumcraft.common.blocks.devices.BlockVisBattery;
import thaumcraft.common.blocks.devices.BlockRechargePedestal;
import thaumcraft.common.blocks.devices.BlockDioptra;
import thaumcraft.common.tiles.devices.TileLampArcane;
import thaumcraft.common.tiles.devices.TileLampGrowth;
import thaumcraft.common.tiles.devices.TileLampFertility;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import thaumcraft.Thaumcraft;
import thaumcraft.common.blocks.basic.BlockMetalTC;
import thaumcraft.common.blocks.basic.BlockTranslucent;
import thaumcraft.common.blocks.crafting.BlockArcaneWorkbench;
import thaumcraft.common.blocks.crafting.BlockCrucible;
import thaumcraft.common.blocks.essentia.BlockAlembic;
import thaumcraft.common.blocks.essentia.BlockJar;
import thaumcraft.common.blocks.essentia.BlockJarItem;
import thaumcraft.common.blocks.essentia.BlockSmelter;
import thaumcraft.common.blocks.essentia.BlockSmelterAux;
import thaumcraft.common.blocks.essentia.BlockSmelterVent;
import thaumcraft.common.blocks.essentia.BlockTube;
import thaumcraft.common.blocks.essentia.BlockCentrifuge;
import thaumcraft.common.blocks.devices.BlockBellows;
import thaumcraft.common.blocks.devices.BlockMirror;
import thaumcraft.common.blocks.crafting.BlockVoidSiphon;
import thaumcraft.common.blocks.crafting.BlockThaumatorium;
import thaumcraft.common.blocks.devices.BlockBrainBox;
import thaumcraft.common.blocks.devices.BlockMirrorItem;
import thaumcraft.common.tiles.devices.TileMirror;
import thaumcraft.common.tiles.devices.TileMirrorEssentia;
import thaumcraft.common.tiles.essentia.TileTube;
import thaumcraft.common.tiles.essentia.TileTubeBuffer;
import thaumcraft.common.tiles.essentia.TileTubeFilter;
import thaumcraft.common.tiles.essentia.TileTubeOneway;
import thaumcraft.common.tiles.essentia.TileTubeRestrict;
import thaumcraft.common.tiles.essentia.TileTubeValve;
import thaumcraft.common.blocks.world.ore.BlockOreTC;
import thaumcraft.common.blocks.world.ore.BlockCrystal;
import thaumcraft.common.blocks.world.plants.BlockLeavesTC;
import thaumcraft.common.blocks.world.plants.BlockPlantCinderpearl;
import thaumcraft.common.blocks.world.plants.BlockPlantShimmerleaf;
import thaumcraft.common.blocks.world.plants.BlockPlantVishroom;
import thaumcraft.common.blocks.world.plants.BlockSaplingTC;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.common.tiles.essentia.TileJarFillable;
import thaumcraft.common.tiles.essentia.TileJarFillableVoid;

/**
 * Port of ConfigBlocks. Field names follow thaumcraft.api.blocks.BlocksTC; registry names are the original ones.
 * Every block also gets its BlockItem here (the original ItemBlock registration).
 */
public final class ConfigBlocks {
    private ConfigBlocks() {
    }

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Thaumcraft.MODID);

    public static final DeferredBlock<Block> oreAmber = register("ore_amber", () -> new BlockOreTC(1.5f, BlockOreTC.xpForGem()));
    public static final DeferredBlock<Block> oreCinnabar = register("ore_cinnabar", () -> new BlockOreTC(2.0f, BlockOreTC.noXp()));
    public static final DeferredBlock<Block> oreQuartz = register("ore_quartz", () -> new BlockOreTC(3.0f, BlockOreTC.xpForGem()));

    public static final DeferredBlock<Block> amberBlock = register("amber_block", BlockTranslucent::new);
    public static final DeferredBlock<Block> amberBrick = register("amber_brick", BlockTranslucent::new);

    public static final DeferredBlock<Block> metalBlockBrass = register("metal_brass", () -> new BlockMetalTC(MapColor.GOLD));
    public static final DeferredBlock<Block> metalBlockThaumium = register("metal_thaumium", () -> new BlockMetalTC(MapColor.COLOR_PURPLE));
    public static final DeferredBlock<Block> crucible = register("crucible", BlockCrucible::new);
    public static final DeferredBlock<Block> arcaneWorkbench = register("arcane_workbench", BlockArcaneWorkbench::new);
    public static final DeferredBlock<Block> wandWorkbench = register("wand_workbench", thaumcraft.common.blocks.crafting.BlockFocalManipulator::new);

    public static final DeferredBlock<Block> metalBlockVoid = register("metal_void", () -> new BlockMetalTC(MapColor.COLOR_BLACK));

    // Greatwood and silverwood. TODO: stairs and slabs.
    public static final DeferredBlock<Block> logGreatwood = register("log_greatwood", () -> log(MapColor.WOOD, 0));
    /** BlockLogsTC.getLightValue: silverwood logs give light 5. */
    public static final DeferredBlock<Block> logSilverwood = register("log_silverwood", () -> log(MapColor.QUARTZ, 5));
    public static final DeferredBlock<Block> leafGreatwood = register("leaves_greatwood", () -> new BlockLeavesTC(false));
    public static final DeferredBlock<Block> leafSilverwood = register("leaves_silverwood", () -> new BlockLeavesTC(true));
    public static final DeferredBlock<Block> saplingGreatwood = register("sapling_greatwood", () -> new BlockSaplingTC(false));
    public static final DeferredBlock<Block> saplingSilverwood = register("sapling_silverwood", () -> new BlockSaplingTC(true));

    // Stage 9a: vis crystal clusters (ShardType order) and the world plants.
    public static final DeferredBlock<Block> crystalAir = register("crystal_aer", () -> new BlockCrystal(Aspect.AIR));
    public static final DeferredBlock<Block> crystalFire = register("crystal_ignis", () -> new BlockCrystal(Aspect.FIRE));
    public static final DeferredBlock<Block> crystalWater = register("crystal_aqua", () -> new BlockCrystal(Aspect.WATER));
    public static final DeferredBlock<Block> crystalEarth = register("crystal_terra", () -> new BlockCrystal(Aspect.EARTH));
    public static final DeferredBlock<Block> crystalOrder = register("crystal_ordo", () -> new BlockCrystal(Aspect.ORDER));
    public static final DeferredBlock<Block> crystalEntropy = register("crystal_perditio", () -> new BlockCrystal(Aspect.ENTROPY));
    public static final DeferredBlock<Block> crystalTaint = register("crystal_vitium", () -> new BlockCrystal(Aspect.FLUX));
    public static final DeferredBlock<Block> shimmerleaf = register("shimmerleaf", BlockPlantShimmerleaf::new);
    public static final DeferredBlock<Block> cinderpearl = register("cinderpearl", BlockPlantCinderpearl::new);
    public static final DeferredBlock<Block> vishroom = register("vishroom", BlockPlantVishroom::new);
    public static final DeferredBlock<Block> grassAmbient = register("grass_ambient", thaumcraft.common.blocks.world.BlockGrassAmbient::new);
    // Stage 9b: taint.
    public static final DeferredBlock<Block> taintFibre = register("taint_fibre", thaumcraft.common.blocks.world.taint.BlockTaintFibre::new);
    public static final DeferredBlock<Block> taintCrust = register("taint_crust", thaumcraft.common.blocks.world.taint.BlockTaint::new);
    public static final DeferredBlock<Block> taintSoil = register("taint_soil", thaumcraft.common.blocks.world.taint.BlockTaint::new);
    public static final DeferredBlock<Block> taintRock = register("taint_rock", thaumcraft.common.blocks.world.taint.BlockTaint::new);
    public static final DeferredBlock<Block> taintGeyser = register("taint_geyser", thaumcraft.common.blocks.world.taint.BlockTaint::new);
    public static final DeferredBlock<Block> taintLog = register("taint_log", thaumcraft.common.blocks.world.taint.BlockTaintLog::new);
    public static final DeferredBlock<Block> taintFeature = register("taint_feature", thaumcraft.common.blocks.world.taint.BlockTaintFeature::new);
    public static final DeferredBlock<Block> fluxGoo = register("flux_goo", thaumcraft.common.blocks.world.taint.BlockFluxGoo::new);
    public static final DeferredBlock<Block> stonePorous = register("stone_porous", thaumcraft.common.blocks.basic.BlockStonePorous::new);
    public static final DeferredBlock<Block> plankGreatwood = register("plank_greatwood", () -> planks(MapColor.WOOD));
    public static final DeferredBlock<Block> plankSilverwood = register("plank_silverwood", () -> planks(MapColor.QUARTZ));

    public static final DeferredBlock<Block> smelterBasic = register("smelter_basic", () -> new BlockSmelter(0));
    public static final DeferredBlock<Block> alembic = register("alembic", BlockAlembic::new);

    public static final DeferredBlock<Block> tableWood = register("table_wood", () -> new BlockTable(BlockBehaviour.Properties.of()
            .mapColor(MapColor.WOOD).strength(2.0f, 2.0f).sound(SoundType.WOOD).ignitedByLava()));
    public static final DeferredBlock<Block> tableStone = register("table_stone", () -> new BlockTable(BlockBehaviour.Properties.of()
            .mapColor(MapColor.STONE).strength(2.5f, 2.0f).sound(SoundType.STONE)));
    public static final DeferredBlock<Block> researchTable = register("research_table", BlockResearchTable::new);
    public static final DeferredBlock<Block> deconstructionTable = register("deconstruction_table", BlockDeconstructionTable::new);
    public static final DeferredBlock<Block> jarNormal = registerJar("jar_normal", () -> new BlockJar(TileJarFillable::new));
    public static final DeferredBlock<Block> jarVoid = registerJar("jar_void", () -> new BlockJar(TileJarFillableVoid::new));

    // Stage 4d: arcane stone and the infusion altar.
    public static final DeferredBlock<Block> stoneArcane = register("stone_arcane", () -> stone());
    public static final DeferredBlock<Block> stoneArcaneBrick = register("stone_arcane_brick", () -> stone());
    public static final DeferredBlock<Block> stairsArcane = register("stairs_arcane", () -> new StairBlock(stoneArcane.get().defaultBlockState(), stoneProps()));
    public static final DeferredBlock<Block> stairsArcaneBrick = register("stairs_arcane_brick", () -> new StairBlock(stoneArcaneBrick.get().defaultBlockState(), stoneProps()));
    public static final DeferredBlock<Block> slabArcaneStone = register("slab_arcane_stone", () -> new SlabBlock(stoneProps()));
    public static final DeferredBlock<Block> slabArcaneBrick = register("slab_arcane_brick", () -> new SlabBlock(stoneProps()));
    public static final DeferredBlock<Block> pedestalArcane = register("pedestal_arcane", BlockPedestal::new);
    public static final DeferredBlock<Block> pillarArcane = register("pillar_arcane", BlockPillar::new);
    public static final DeferredBlock<Block> infusionMatrix = register("infusion_matrix", BlockInfusionMatrix::new);
    /** One candle and one nitor per dye color, as in the original BlocksTC.candles and BlocksTC.nitor maps. */
    public static final Map<DyeColor, DeferredBlock<Block>> candles = dyed("candle_", BlockCandle::new);
    public static final Map<DyeColor, DeferredBlock<Block>> nitor = dyed("nitor_", BlockNitor::new);
    // Stage 6a: essentia tubes, smeltery upgrades, bellows and the centrifuge.
    public static final DeferredBlock<Block> metalAlchemical = register("metal_alchemical", () -> new BlockMetalTC(MapColor.METAL));
    public static final DeferredBlock<Block> metalAlchemicalAdvanced = register("metal_alchemical_advanced", () -> new BlockMetalTC(MapColor.METAL));
    public static final DeferredBlock<Block> smelterThaumium = register("smelter_thaumium", () -> new BlockSmelter(1));
    public static final DeferredBlock<Block> smelterVoid = register("smelter_void", () -> new BlockSmelter(2));
    public static final DeferredBlock<Block> smelterAux = register("smelter_aux", BlockSmelterAux::new);
    public static final DeferredBlock<Block> smelterVent = register("smelter_vent", BlockSmelterVent::new);
    public static final DeferredBlock<Block> tube = register("tube", () -> new BlockTube(TileTube::new));
    public static final DeferredBlock<Block> tubeValve = register("tube_valve", () -> new BlockTube(TileTubeValve::new));
    public static final DeferredBlock<Block> tubeFilter = register("tube_filter", () -> new BlockTube(TileTubeFilter::new));
    public static final DeferredBlock<Block> tubeRestrict = register("tube_restrict", () -> new BlockTube(TileTubeRestrict::new));
    public static final DeferredBlock<Block> tubeOneway = register("tube_oneway", () -> new BlockTube(TileTubeOneway::new));
    public static final DeferredBlock<Block> tubeBuffer = register("tube_buffer", () -> new BlockTube(TileTubeBuffer::new));
    public static final DeferredBlock<Block> bellows = register("bellows", BlockBellows::new);
    public static final DeferredBlock<Block> centrifuge = register("centrifuge", BlockCentrifuge::new);

    // Stage 6b: mirrors, the void siphon and the thaumatorium.
    public static final DeferredBlock<Block> mirror = registerMirror("mirror", () -> new BlockMirror(TileMirror::new));
    public static final DeferredBlock<Block> voidSiphon = register("void_siphon", BlockVoidSiphon::new);
    /** No items: both halves drop an alchemical construct (setCreativeTab(null) in the original). */
    public static final DeferredBlock<Block> thaumatorium = BLOCKS.register("thaumatorium", () -> new BlockThaumatorium(false));
    public static final DeferredBlock<Block> thaumatoriumTop = BLOCKS.register("thaumatorium_top", () -> new BlockThaumatorium(true));
    public static final DeferredBlock<Block> brainBox = register("brain_box", BlockBrainBox::new);
    public static final DeferredBlock<Block> mirrorEssentia = registerMirror("mirror_essentia", () -> new BlockMirror(TileMirrorEssentia::new));

    // Stage 6c: lamps, the infernal furnace, flux condenser, stabilizer, vis devices and the dioptra.
    public static final DeferredBlock<Block> lampArcane = register("lamp_arcane", () -> new BlockLamp(TileLampArcane::new));
    public static final DeferredBlock<Block> lampGrowth = register("lamp_growth", () -> new BlockLamp(TileLampGrowth::new));
    public static final DeferredBlock<Block> lampFertility = register("lamp_fertility", () -> new BlockLamp(TileLampFertility::new));
    /** Built with salis mundus; no item (setCreativeTab(null) in the original). */
    public static final DeferredBlock<Block> infernalFurnace = BLOCKS.register("infernal_furnace", BlockInfernalFurnace::new);
    /** The invisible blocks that make up the infernal furnace shell. No items. */
    public static final DeferredBlock<Block> placeholderNetherbrick = BLOCKS.register("placeholder_brick", BlockPlaceholder::new);
    public static final DeferredBlock<Block> placeholderObsidian = BLOCKS.register("placeholder_obsidian", BlockPlaceholder::new);

    // Stage 12b: the eldritch altars of TC4 (BlockEldritch metas 0-4) and the obsidian tile (BlockCosmeticSolid meta 1).
    public static final DeferredBlock<Block> eldritchAltar = BLOCKS.register("eldritch_altar",
            () -> new thaumcraft.common.blocks.eldritch.BlockEldritch(thaumcraft.common.blocks.eldritch.BlockEldritch.Type.ALTAR));
    public static final DeferredBlock<Block> eldritchObelisk = BLOCKS.register("eldritch_obelisk",
            () -> new thaumcraft.common.blocks.eldritch.BlockEldritch(thaumcraft.common.blocks.eldritch.BlockEldritch.Type.OBELISK));
    public static final DeferredBlock<Block> eldritchObeliskBody = BLOCKS.register("eldritch_obelisk_body",
            () -> new thaumcraft.common.blocks.eldritch.BlockEldritch(thaumcraft.common.blocks.eldritch.BlockEldritch.Type.OBELISK_BODY));
    public static final DeferredBlock<Block> eldritchCap = BLOCKS.register("eldritch_cap",
            () -> new thaumcraft.common.blocks.eldritch.BlockEldritch(thaumcraft.common.blocks.eldritch.BlockEldritch.Type.CAP));
    public static final DeferredBlock<Block> eldritchStoneGlowing = register("eldritch_stone_glowing", thaumcraft.common.blocks.eldritch.BlockEldritchStone::new);
    public static final DeferredBlock<Block> obsidianTile = register("obsidian_tile", () -> new Block(BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_BLACK).strength(30.0f, 999.0f).sound(SoundType.STONE).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> condenser = register("condenser", BlockCondenser::new);
    public static final DeferredBlock<Block> condenserLattice = register("condenser_lattice", () -> new BlockCondenserLattice(false));
    public static final DeferredBlock<Block> condenserLatticeDirty = register("condenser_lattice_dirty", () -> new BlockCondenserLattice(true));
    public static final DeferredBlock<Block> stabilizer = register("stabilizer", BlockStabilizer::new);
    public static final DeferredBlock<Block> inlay = register("inlay", BlockInlay::new);
    public static final DeferredBlock<Block> visGenerator = register("vis_generator", BlockVisGenerator::new);
    public static final DeferredBlock<Block> visBattery = register("vis_battery", BlockVisBattery::new);
    public static final DeferredBlock<Block> rechargePedestal = register("recharge_pedestal", BlockRechargePedestal::new);
    public static final DeferredBlock<Block> dioptra = register("dioptra", BlockDioptra::new);

    // Stage 6d: arcane ears, redstone relay, levitator, hungry chest, brain in a jar, spa, potion sprayer and everfull urn.
    public static final DeferredBlock<Block> arcaneEar = register("arcane_ear", () -> new BlockArcaneEar(false));
    public static final DeferredBlock<Block> arcaneEarToggle = register("arcane_ear_toggle", () -> new BlockArcaneEar(true));
    public static final DeferredBlock<Block> redstoneRelay = register("redstone_relay", BlockRedstoneRelay::new);
    public static final DeferredBlock<Block> levitator = register("levitator", BlockLevitator::new);
    public static final DeferredBlock<Block> hungryChest = register("hungry_chest", BlockHungryChest::new);
    public static final DeferredBlock<Block> jarBrain = registerJarBrain();
    public static final DeferredBlock<Block> spa = register("spa", BlockSpa::new);
    public static final DeferredBlock<Block> potionSprayer = register("potion_sprayer", BlockPotionSprayer::new);
    public static final DeferredBlock<Block> everfullUrn = register("everfull_urn", BlockWaterJug::new);
    /** The purifying fluid; its item is the bucket (ConfigItems.purifyingFluidBucket). */
    public static final DeferredBlock<BlockFluidPure> purifyingFluid = BLOCKS.register("purifying_fluid", BlockFluidPure::new);

    // Stage 8a: the golem press, built with salis mundus, and its placeholders. No items.
    public static final DeferredBlock<Block> golemBuilder = BLOCKS.register("golem_builder", thaumcraft.common.blocks.crafting.BlockGolemBuilder::new);
    public static final DeferredBlock<Block> placeholderBars = BLOCKS.register("placeholder_bars", () -> new BlockPlaceholder(false, 0));
    public static final DeferredBlock<Block> placeholderAnvil = BLOCKS.register("placeholder_anvil", () -> new BlockPlaceholder(false, 0));
    public static final DeferredBlock<Block> placeholderCauldron = BLOCKS.register("placeholder_cauldron", () -> new BlockPlaceholder(false, 13));
    public static final DeferredBlock<Block> placeholderTable = BLOCKS.register("placeholder_table", () -> new BlockPlaceholder(false, 0));

    /** The light left by the Lamplight infusion enchantment. No item. */
    public static final DeferredBlock<Block> effectGlimmer = BLOCKS.register("effect_glimmer", BlockEffect::new);
    public static final DeferredBlock<Block> effectSap = BLOCKS.register("effect_sap", thaumcraft.common.blocks.misc.BlockEffectSap::new);
    /** The dimensional tear of the rift focus. No item, like the original (no creative tab, nothing dropped). */
    public static final DeferredBlock<Block> hole = BLOCKS.register("hole", thaumcraft.common.blocks.misc.BlockHole::new);

    // Stage 9d-4: the loot crates and urns, and the banners (the crimson cult one has no dye).
    public static final DeferredBlock<Block> lootCrateCommon = register("loot_crate_common",
            () -> new thaumcraft.common.blocks.world.BlockLoot(false, thaumcraft.common.blocks.world.BlockLoot.LootType.COMMON));
    public static final DeferredBlock<Block> lootCrateUncommon = register("loot_crate_uncommon",
            () -> new thaumcraft.common.blocks.world.BlockLoot(false, thaumcraft.common.blocks.world.BlockLoot.LootType.UNCOMMON));
    public static final DeferredBlock<Block> lootCrateRare = register("loot_crate_rare",
            () -> new thaumcraft.common.blocks.world.BlockLoot(false, thaumcraft.common.blocks.world.BlockLoot.LootType.RARE));
    public static final DeferredBlock<Block> lootUrnCommon = register("loot_urn_common",
            () -> new thaumcraft.common.blocks.world.BlockLoot(true, thaumcraft.common.blocks.world.BlockLoot.LootType.COMMON));
    public static final DeferredBlock<Block> lootUrnUncommon = register("loot_urn_uncommon",
            () -> new thaumcraft.common.blocks.world.BlockLoot(true, thaumcraft.common.blocks.world.BlockLoot.LootType.UNCOMMON));
    public static final DeferredBlock<Block> lootUrnRare = register("loot_urn_rare",
            () -> new thaumcraft.common.blocks.world.BlockLoot(true, thaumcraft.common.blocks.world.BlockLoot.LootType.RARE));
    public static final DeferredBlock<Block> bannerCrimsonCult = registerBanner("banner_crimson_cult", null);
    public static final Map<DyeColor, DeferredBlock<Block>> banners = bannersDyed();

    /** BlockStoneTC: hardness 2, resistance 10 (6 in 1.21 units). Usable as beacon base (tag). */
    private static BlockBehaviour.Properties stoneProps() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM)
                .requiresCorrectToolForDrops().strength(2.0f, 6.0f).sound(SoundType.STONE);
    }

    private static Block stone() {
        return new Block(stoneProps());
    }

    /** dye.getUnlocalizedName() in 1.12 is "lightBlue"/"silver"; the original lowercased it: candle_lightblue, candle_silver. */
    private static Map<DyeColor, DeferredBlock<Block>> dyed(String prefix, Function<DyeColor, Block> factory) {
        Map<DyeColor, DeferredBlock<Block>> out = new EnumMap<>(DyeColor.class);
        for (DyeColor dye : DyeColor.values()) {
            out.put(dye, register(prefix + legacyDyeName(dye), () -> factory.apply(dye)));
        }
        return out;
    }

    public static String legacyDyeName(DyeColor dye) {
        return switch (dye) {
            case LIGHT_BLUE -> "lightblue";
            case LIGHT_GRAY -> "silver";
            default -> dye.getName();
        };
    }

    /** BlockLogsTC: hardness 2, resistance 5. */
    private static Block log(MapColor color, int light) {
        return new RotatedPillarBlock(BlockBehaviour.Properties.of().mapColor(color).instrument(NoteBlockInstrument.BASS)
                .strength(2.0f, 5.0f).sound(SoundType.WOOD).ignitedByLava().lightLevel(s -> light));
    }

    /** BlockPlanksTC: hardness 2, resistance 5, like vanilla planks. */
    private static Block planks(MapColor color) {
        return new Block(BlockBehaviour.Properties.of().mapColor(color).instrument(NoteBlockInstrument.BASS)
                .strength(2.0f, 5.0f).sound(SoundType.WOOD).ignitedByLava());
    }

    /** Jars use BlockJarItem, which keeps their essentia. */
    private static DeferredBlock<Block> registerJar(String name, Supplier<Block> block) {
        DeferredBlock<Block> holder = BLOCKS.register(name, block);
        ConfigItems.ITEMS.register(name, () -> new BlockJarItem(holder.get(), new Item.Properties()));
        return holder;
    }

    /** The brain in a jar uses BlockJarBrainItem, which keeps its experience. */
    private static DeferredBlock<Block> registerJarBrain() {
        DeferredBlock<Block> holder = BLOCKS.register("jar_brain", BlockJarBrain::new);
        ConfigItems.ITEMS.register("jar_brain", () -> new BlockJarBrainItem(holder.get(), new Item.Properties()));
        return holder;
    }

    /** Banners use BlockBannerTCItem, which places them standing or on a wall and carries the aspect. */
    private static DeferredBlock<Block> registerBanner(String name, DyeColor dye) {
        DeferredBlock<Block> holder = BLOCKS.register(name, () -> new thaumcraft.common.blocks.basic.BlockBannerTC(dye));
        ConfigItems.ITEMS.register(name, () -> new thaumcraft.common.blocks.basic.BlockBannerTCItem(holder.get(), new Item.Properties()));
        return holder;
    }

    private static Map<DyeColor, DeferredBlock<Block>> bannersDyed() {
        Map<DyeColor, DeferredBlock<Block>> out = new EnumMap<>(DyeColor.class);
        for (DyeColor dye : DyeColor.values()) {
            out.put(dye, registerBanner("banner_" + legacyDyeName(dye), dye));
        }
        return out;
    }

    /** Mirrors use BlockMirrorItem, which carries the link. */
    private static DeferredBlock<Block> registerMirror(String name, Supplier<Block> block) {
        DeferredBlock<Block> holder = BLOCKS.register(name, block);
        ConfigItems.ITEMS.register(name, () -> new BlockMirrorItem(holder.get(), new Item.Properties()));
        return holder;
    }

    private static DeferredBlock<Block> register(String name, Supplier<Block> block) {
        DeferredBlock<Block> holder = BLOCKS.register(name, block);
        ConfigItems.ITEMS.register(name, () -> new BlockItem(holder.get(), new Item.Properties()));
        return holder;
    }
}
