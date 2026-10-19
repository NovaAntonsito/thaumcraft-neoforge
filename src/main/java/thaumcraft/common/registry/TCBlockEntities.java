package thaumcraft.common.registry;

import thaumcraft.common.tiles.devices.TileArcaneEar;
import thaumcraft.common.tiles.devices.TileRedstoneRelay;
import thaumcraft.common.tiles.devices.TileLevitator;
import thaumcraft.common.tiles.devices.TileHungryChest;
import thaumcraft.common.tiles.devices.TileJarBrain;
import thaumcraft.common.tiles.devices.TileSpa;
import thaumcraft.common.tiles.devices.TilePotionSprayer;
import thaumcraft.common.tiles.devices.TileWaterJug;
import thaumcraft.common.tiles.devices.TileLampArcane;
import thaumcraft.common.tiles.devices.TileLampGrowth;
import thaumcraft.common.tiles.devices.TileLampFertility;
import thaumcraft.common.tiles.devices.TileInfernalFurnace;
import thaumcraft.common.tiles.devices.TileCondenser;
import thaumcraft.common.tiles.devices.TileStabilizer;
import thaumcraft.common.tiles.devices.TileVisGenerator;
import thaumcraft.common.tiles.devices.TileRechargePedestal;
import thaumcraft.common.tiles.devices.TileDioptra;
import java.util.function.Supplier;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;
import thaumcraft.Thaumcraft;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.tiles.crafting.TileArcaneWorkbench;
import thaumcraft.common.tiles.crafting.TileCrucible;
import thaumcraft.common.tiles.crafting.TileInfusionMatrix;
import thaumcraft.common.tiles.crafting.TilePedestal;
import thaumcraft.common.tiles.crafting.TileResearchTable;
import thaumcraft.common.tiles.crafting.TileDeconstructionTable;
import thaumcraft.common.tiles.misc.TileNitor;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredBlock;
import thaumcraft.common.tiles.essentia.TileAlembic;
import thaumcraft.common.tiles.essentia.TileJarFillable;
import thaumcraft.common.tiles.essentia.TileJarFillableVoid;
import thaumcraft.common.tiles.essentia.TileSmelter;
import thaumcraft.common.tiles.essentia.TileCentrifuge;
import thaumcraft.common.tiles.essentia.TileTube;
import thaumcraft.common.tiles.essentia.TileTubeBuffer;
import thaumcraft.common.tiles.essentia.TileTubeFilter;
import thaumcraft.common.tiles.essentia.TileTubeOneway;
import thaumcraft.common.tiles.essentia.TileTubeRestrict;
import thaumcraft.common.tiles.essentia.TileTubeValve;
import thaumcraft.common.tiles.devices.TileBellows;
import thaumcraft.common.tiles.devices.TileMirror;
import thaumcraft.common.tiles.crafting.TileVoidSiphon;
import thaumcraft.common.tiles.crafting.TileThaumatorium;
import thaumcraft.common.tiles.crafting.TileThaumatoriumTop;
import thaumcraft.common.tiles.devices.TileMirrorEssentia;

/** Block entity types (the 1.12 GameRegistry.registerTileEntity calls). */
public final class TCBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Thaumcraft.MODID);

    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<TileCrucible>> CRUCIBLE = BLOCK_ENTITIES.register("crucible",
            () -> BlockEntityType.Builder.of(TileCrucible::new, ConfigBlocks.crucible.get()).build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<TileArcaneWorkbench>> ARCANE_WORKBENCH = BLOCK_ENTITIES.register("arcane_workbench",
            () -> BlockEntityType.Builder.of(TileArcaneWorkbench::new, ConfigBlocks.arcaneWorkbench.get()).build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<TileJarFillable>> JAR = BLOCK_ENTITIES.register("jar",
            () -> BlockEntityType.Builder.of(TileJarFillable::new, ConfigBlocks.jarNormal.get()).build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<TileJarFillableVoid>> JAR_VOID = BLOCK_ENTITIES.register("jar_void",
            () -> BlockEntityType.Builder.of(TileJarFillableVoid::new, ConfigBlocks.jarVoid.get()).build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<TileAlembic>> ALEMBIC = BLOCK_ENTITIES.register("alembic",
            () -> BlockEntityType.Builder.of(TileAlembic::new, ConfigBlocks.alembic.get()).build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<TileSmelter>> SMELTER = BLOCK_ENTITIES.register("smelter",
            () -> BlockEntityType.Builder.of(TileSmelter::new, ConfigBlocks.smelterBasic.get(), ConfigBlocks.smelterThaumium.get(),
                    ConfigBlocks.smelterVoid.get()).build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<TilePedestal>> PEDESTAL = BLOCK_ENTITIES.register("pedestal",
            () -> BlockEntityType.Builder.of(TilePedestal::new, ConfigBlocks.pedestalArcane.get()).build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<TileInfusionMatrix>> INFUSION_MATRIX = BLOCK_ENTITIES.register("infusion_matrix",
            () -> BlockEntityType.Builder.of(TileInfusionMatrix::new, ConfigBlocks.infusionMatrix.get()).build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<TileResearchTable>> RESEARCH_TABLE = BLOCK_ENTITIES.register("research_table",
            () -> BlockEntityType.Builder.of(TileResearchTable::new, ConfigBlocks.researchTable.get()).build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<thaumcraft.common.tiles.misc.TileHole>> HOLE = BLOCK_ENTITIES.register("hole",
            () -> BlockEntityType.Builder.of(thaumcraft.common.tiles.misc.TileHole::new, ConfigBlocks.hole.get()).build(null));

    public static final Supplier<BlockEntityType<thaumcraft.common.tiles.crafting.TileFocalManipulator>> FOCAL_MANIPULATOR = BLOCK_ENTITIES.register("focal_manipulator",
            () -> BlockEntityType.Builder.of(thaumcraft.common.tiles.crafting.TileFocalManipulator::new, ConfigBlocks.wandWorkbench.get()).build(null));

    public static final Supplier<BlockEntityType<TileDeconstructionTable>> DECONSTRUCTION_TABLE = BLOCK_ENTITIES.register("deconstruction_table",
            () -> BlockEntityType.Builder.of(TileDeconstructionTable::new, ConfigBlocks.deconstructionTable.get()).build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<TileTube>> TUBE = BLOCK_ENTITIES.register("tube",
            () -> BlockEntityType.Builder.of(TileTube::new, ConfigBlocks.tube.get()).build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<TileTubeValve>> TUBE_VALVE = BLOCK_ENTITIES.register("tube_valve",
            () -> BlockEntityType.Builder.of(TileTubeValve::new, ConfigBlocks.tubeValve.get()).build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<TileTubeFilter>> TUBE_FILTER = BLOCK_ENTITIES.register("tube_filter",
            () -> BlockEntityType.Builder.of(TileTubeFilter::new, ConfigBlocks.tubeFilter.get()).build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<TileTubeRestrict>> TUBE_RESTRICT = BLOCK_ENTITIES.register("tube_restrict",
            () -> BlockEntityType.Builder.of(TileTubeRestrict::new, ConfigBlocks.tubeRestrict.get()).build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<TileTubeOneway>> TUBE_ONEWAY = BLOCK_ENTITIES.register("tube_oneway",
            () -> BlockEntityType.Builder.of(TileTubeOneway::new, ConfigBlocks.tubeOneway.get()).build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<TileTubeBuffer>> TUBE_BUFFER = BLOCK_ENTITIES.register("tube_buffer",
            () -> BlockEntityType.Builder.of(TileTubeBuffer::new, ConfigBlocks.tubeBuffer.get()).build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<TileBellows>> BELLOWS = BLOCK_ENTITIES.register("bellows",
            () -> BlockEntityType.Builder.of(TileBellows::new, ConfigBlocks.bellows.get()).build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<TileCentrifuge>> CENTRIFUGE = BLOCK_ENTITIES.register("centrifuge",
            () -> BlockEntityType.Builder.of(TileCentrifuge::new, ConfigBlocks.centrifuge.get()).build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<TileMirror>> MIRROR = BLOCK_ENTITIES.register("mirror",
            () -> BlockEntityType.Builder.of(TileMirror::new, ConfigBlocks.mirror.get()).build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<TileMirrorEssentia>> MIRROR_ESSENTIA = BLOCK_ENTITIES.register("mirror_essentia",
            () -> BlockEntityType.Builder.of(TileMirrorEssentia::new, ConfigBlocks.mirrorEssentia.get()).build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<TileVoidSiphon>> VOID_SIPHON = BLOCK_ENTITIES.register("void_siphon",
            () -> BlockEntityType.Builder.of(TileVoidSiphon::new, ConfigBlocks.voidSiphon.get()).build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<TileThaumatorium>> THAUMATORIUM = BLOCK_ENTITIES.register("thaumatorium",
            () -> BlockEntityType.Builder.of(TileThaumatorium::new, ConfigBlocks.thaumatorium.get()).build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<TileThaumatoriumTop>> THAUMATORIUM_TOP = BLOCK_ENTITIES.register("thaumatorium_top",
            () -> BlockEntityType.Builder.of(TileThaumatoriumTop::new, ConfigBlocks.thaumatoriumTop.get()).build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<TileLampArcane>> LAMP_ARCANE = BLOCK_ENTITIES.register("lamp_arcane",
            () -> BlockEntityType.Builder.of(TileLampArcane::new, ConfigBlocks.lampArcane.get()).build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<TileLampGrowth>> LAMP_GROWTH = BLOCK_ENTITIES.register("lamp_growth",
            () -> BlockEntityType.Builder.of(TileLampGrowth::new, ConfigBlocks.lampGrowth.get()).build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<TileLampFertility>> LAMP_FERTILITY = BLOCK_ENTITIES.register("lamp_fertility",
            () -> BlockEntityType.Builder.of(TileLampFertility::new, ConfigBlocks.lampFertility.get()).build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<TileInfernalFurnace>> INFERNAL_FURNACE = BLOCK_ENTITIES.register("infernal_furnace",
            () -> BlockEntityType.Builder.of(TileInfernalFurnace::new, ConfigBlocks.infernalFurnace.get()).build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<TileCondenser>> CONDENSER = BLOCK_ENTITIES.register("condenser",
            () -> BlockEntityType.Builder.of(TileCondenser::new, ConfigBlocks.condenser.get()).build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<TileStabilizer>> STABILIZER = BLOCK_ENTITIES.register("stabilizer",
            () -> BlockEntityType.Builder.of(TileStabilizer::new, ConfigBlocks.stabilizer.get()).build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<TileVisGenerator>> VIS_GENERATOR = BLOCK_ENTITIES.register("vis_generator",
            () -> BlockEntityType.Builder.of(TileVisGenerator::new, ConfigBlocks.visGenerator.get()).build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<TileRechargePedestal>> RECHARGE_PEDESTAL = BLOCK_ENTITIES.register("recharge_pedestal",
            () -> BlockEntityType.Builder.of(TileRechargePedestal::new, ConfigBlocks.rechargePedestal.get()).build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<TileDioptra>> DIOPTRA = BLOCK_ENTITIES.register("dioptra",
            () -> BlockEntityType.Builder.of(TileDioptra::new, ConfigBlocks.dioptra.get()).build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<TileArcaneEar>> ARCANE_EAR = BLOCK_ENTITIES.register("arcane_ear",
            () -> BlockEntityType.Builder.of(TileArcaneEar::new, ConfigBlocks.arcaneEar.get(), ConfigBlocks.arcaneEarToggle.get()).build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<TileRedstoneRelay>> REDSTONE_RELAY = BLOCK_ENTITIES.register("redstone_relay",
            () -> BlockEntityType.Builder.of(TileRedstoneRelay::new, ConfigBlocks.redstoneRelay.get()).build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<TileLevitator>> LEVITATOR = BLOCK_ENTITIES.register("levitator",
            () -> BlockEntityType.Builder.of(TileLevitator::new, ConfigBlocks.levitator.get()).build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<TileHungryChest>> HUNGRY_CHEST = BLOCK_ENTITIES.register("hungry_chest",
            () -> BlockEntityType.Builder.of(TileHungryChest::new, ConfigBlocks.hungryChest.get()).build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<TileJarBrain>> JAR_BRAIN = BLOCK_ENTITIES.register("jar_brain",
            () -> BlockEntityType.Builder.of(TileJarBrain::new, ConfigBlocks.jarBrain.get()).build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<TileSpa>> SPA = BLOCK_ENTITIES.register("spa",
            () -> BlockEntityType.Builder.of(TileSpa::new, ConfigBlocks.spa.get()).build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<TilePotionSprayer>> POTION_SPRAYER = BLOCK_ENTITIES.register("potion_sprayer",
            () -> BlockEntityType.Builder.of(TilePotionSprayer::new, ConfigBlocks.potionSprayer.get()).build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<TileWaterJug>> EVERFULL_URN = BLOCK_ENTITIES.register("everfull_urn",
            () -> BlockEntityType.Builder.of(TileWaterJug::new, ConfigBlocks.everfullUrn.get()).build(null));

    /** One type for the 16 nitor colors. */
    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<TileNitor>> NITOR = BLOCK_ENTITIES.register("nitor",
            () -> BlockEntityType.Builder.of(TileNitor::new,
                    ConfigBlocks.nitor.values().stream().map(DeferredBlock::get).toArray(Block[]::new)).build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<thaumcraft.common.tiles.crafting.TileGolemBuilder>> GOLEM_BUILDER = BLOCK_ENTITIES.register("golem_builder",
            () -> BlockEntityType.Builder.of(thaumcraft.common.tiles.crafting.TileGolemBuilder::new, ConfigBlocks.golemBuilder.get()).build(null));

    /** One type for the crimson cult banner and the 16 dyed ones. */
    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<thaumcraft.common.tiles.misc.TileBanner>> BANNER = BLOCK_ENTITIES.register("banner",
            () -> BlockEntityType.Builder.of(thaumcraft.common.tiles.misc.TileBanner::new,
                    java.util.stream.Stream.concat(java.util.stream.Stream.of(ConfigBlocks.bannerCrimsonCult),
                            ConfigBlocks.banners.values().stream()).map(DeferredBlock::get).toArray(Block[]::new)).build(null));

    private TCBlockEntities() {
    }

    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<thaumcraft.common.tiles.eldritch.TileEldritchAltar>> ELDRITCH_ALTAR = BLOCK_ENTITIES.register("eldritch_altar",
            () -> BlockEntityType.Builder.of(thaumcraft.common.tiles.eldritch.TileEldritchAltar::new, ConfigBlocks.eldritchAltar.get()).build(null));
    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<thaumcraft.common.tiles.eldritch.TileEldritchObelisk>> ELDRITCH_OBELISK = BLOCK_ENTITIES.register("eldritch_obelisk",
            () -> BlockEntityType.Builder.of(thaumcraft.common.tiles.eldritch.TileEldritchObelisk::new, ConfigBlocks.eldritchObelisk.get()).build(null));
    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<thaumcraft.common.tiles.eldritch.TileEldritchCap>> ELDRITCH_CAP = BLOCK_ENTITIES.register("eldritch_cap",
            () -> BlockEntityType.Builder.of(thaumcraft.common.tiles.eldritch.TileEldritchCap::new, ConfigBlocks.eldritchCap.get()).build(null));
}
