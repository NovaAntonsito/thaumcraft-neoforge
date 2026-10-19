package thaumcraft.client.ponder;

import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.common.blocks.essentia.BlockSmelter;
import thaumcraft.common.blocks.essentia.BlockTube;
import thaumcraft.common.items.consumables.ItemPhial;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.tiles.crafting.TileCrucible;
import thaumcraft.common.tiles.crafting.TileInfusionMatrix;
import thaumcraft.common.tiles.crafting.TilePedestal;
import thaumcraft.common.tiles.crafting.TileResearchTable;
import thaumcraft.common.tiles.essentia.TileCentrifuge;
import thaumcraft.common.tiles.essentia.TileJarFillable;
import thaumcraft.common.tiles.essentia.TileTubeBuffer;
import thaumcraft.common.tiles.essentia.TileTubeFilter;
import thaumcraft.common.tiles.essentia.TileTubeValve;

/**
 * The Ponder storyboards. Every .text(...) here must match the thaumcraft.ponder.&lt;scene&gt;.text_N entries in
 * en_us.json, in the same order (outside Ponder's editing mode the lang file wins).
 */
public final class ThaumcraftPonderScenes {
    private ThaumcraftPonderScenes() {
    }

    // ---- (a) crucible: heat, water, throw items, recipe ----
    public static void crucible(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("crucible", "Alchemy in the Crucible");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();
        scene.idle(10);

        BlockPos heat = util.grid().at(2, 1, 2);
        BlockPos cru = util.grid().at(2, 2, 2);
        Vec3 above = util.vector().topOf(cru).add(0, 1.5, 0);

        scene.world().showSection(util.select().position(heat), Direction.DOWN);
        scene.idle(10);
        scene.world().showSection(util.select().position(cru), Direction.DOWN);
        scene.idle(15);

        scene.overlay().showText(70)
                .text("The Crucible needs heat below it: fire, lava or a magma block")
                .pointAt(util.vector().blockSurface(heat, Direction.WEST))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        scene.overlay().showControls(util.vector().topOf(cru), Pointing.DOWN, 40).rightClick()
                .withItem(new ItemStack(Items.WATER_BUCKET));
        scene.idle(10);
        scene.world().modifyBlockEntity(cru, TileCrucible.class, te -> te.tank.setFluid(new FluidStack(Fluids.WATER, 1000)));
        scene.idle(20);
        scene.overlay().showText(70)
                .text("Fill it with water. After a while the heat makes the water boil")
                .pointAt(util.vector().centerOf(cru))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(40);
        scene.world().modifyBlockEntity(cru, TileCrucible.class, te -> te.heat = 200);
        scene.idle(40);

        // Throw two vis crystals: they melt into their aspects.
        scene.world().createItemEntity(above, Vec3.ZERO, ConfigItems.crystalEssence.get().makeCrystal(Aspect.MAGIC, 1));
        scene.idle(8);
        scene.world().createItemEntity(above, Vec3.ZERO, ConfigItems.crystalEssence.get().makeCrystal(Aspect.EARTH, 1));
        scene.idle(20);
        scene.world().modifyEntities(ItemEntity.class, Entity::discard);
        scene.world().modifyBlockEntity(cru, TileCrucible.class,
                te -> te.aspects = new AspectList().add(Aspect.MAGIC, 5).add(Aspect.EARTH, 5));
        scene.overlay().showText(80)
                .text("Items thrown into the boiling water melt down into their aspects, which stay dissolved in it")
                .pointAt(util.vector().topOf(cru))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(90);

        // The catalyst: iron ingot + 5 Praecantatio + 5 Terra = thaumium ingot.
        scene.world().createItemEntity(above, Vec3.ZERO, new ItemStack(Items.IRON_INGOT));
        scene.idle(20);
        scene.world().modifyEntities(ItemEntity.class, Entity::discard);
        scene.world().modifyBlockEntity(cru, TileCrucible.class, te -> te.aspects = new AspectList());
        scene.world().createItemEntity(util.vector().topOf(cru), new Vec3(0, 0.25, 0.05),
                new ItemStack(ConfigItems.ingots.get("thaumium").get()));
        scene.effects().indicateSuccess(cru);
        scene.overlay().showText(90)
                .text("If the dissolved aspects match a known recipe, throwing in its catalyst uses them up and makes the result. Here: iron ingot with Praecantatio and Terra gives Thaumium")
                .colored(PonderPalette.GREEN)
                .pointAt(util.vector().topOf(cru))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(100);

        scene.overlay().showText(80)
                .text("Recipes must be researched first. Too much essentia, or leftovers, spill out as flux into the aura")
                .colored(PonderPalette.RED)
                .pointAt(util.vector().centerOf(cru))
                .placeNearTarget();
        scene.idle(90);
        scene.markAsFinished();
    }

    // ---- (b) infusion altar: matrix, pillars, symmetric pedestals, stability ----
    public static void infusionAltar(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("infusion_altar", "Building the Infusion Altar");
        scene.configureBasePlate(0, 0, 7);
        scene.showBasePlate();
        scene.idle(10);

        BlockPos center = util.grid().at(3, 1, 3);
        BlockPos matrix = util.grid().at(3, 3, 3);
        BlockPos[] corners = {util.grid().at(2, 1, 2), util.grid().at(2, 1, 4), util.grid().at(4, 1, 2), util.grid().at(4, 1, 4)};
        BlockPos[] outer = {util.grid().at(3, 1, 0), util.grid().at(3, 1, 6), util.grid().at(0, 1, 3), util.grid().at(6, 1, 3)};
        BlockPos[] candles = {util.grid().at(1, 1, 1), util.grid().at(5, 1, 5), util.grid().at(1, 1, 5), util.grid().at(5, 1, 1)};

        // Before salis mundus: two arcane stones in each corner instead of a pillar.
        BlockState[] pillarStates = new BlockState[4];
        BlockState stone = ConfigBlocks.stoneArcane.get().defaultBlockState();
        Direction[] facings = {Direction.EAST, Direction.NORTH, Direction.SOUTH, Direction.WEST};
        for (int i = 0; i < 4; i++) {
            pillarStates[i] = ConfigBlocks.pillarArcane.get().defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, facings[i]);
            scene.world().setBlock(corners[i], stone, false);
            scene.world().setBlock(corners[i].above(), stone, false);
        }

        scene.world().showSection(util.select().position(center), Direction.DOWN);
        scene.idle(10);
        Selection stones = util.select().fromTo(2, 1, 2, 2, 2, 2).add(util.select().fromTo(2, 1, 4, 2, 2, 4))
                .add(util.select().fromTo(4, 1, 2, 4, 2, 2)).add(util.select().fromTo(4, 1, 4, 4, 2, 4));
        scene.world().showSection(stones, Direction.DOWN);
        scene.idle(10);
        scene.world().showSection(util.select().position(matrix), Direction.DOWN);
        scene.idle(15);

        scene.overlay().showText(90)
                .text("The altar: a Runic Matrix two blocks above an Arcane Pedestal, and two Arcane Stones stacked in each corner")
                .pointAt(util.vector().centerOf(matrix))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(100);

        scene.overlay().showControls(util.vector().topOf(corners[3].above()), Pointing.DOWN, 40).rightClick()
                .withItem(new ItemStack(ConfigItems.salisMundus.get()));
        scene.idle(15);
        for (int i = 0; i < 4; i++) {
            scene.world().setBlock(corners[i].above(), Blocks.AIR.defaultBlockState(), false);
            scene.world().setBlock(corners[i], pillarStates[i], true);
        }
        scene.overlay().showText(70)
                .text("Sprinkle Salis Mundus on it and the stones become the infusion pillars")
                .pointAt(util.vector().centerOf(corners[3]))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        scene.overlay().showControls(util.vector().blockSurface(matrix, Direction.WEST), Pointing.LEFT, 40).rightClick()
                .withItem(new ItemStack(ConfigItems.casterBasic.get()));
        scene.idle(10);
        scene.world().modifyBlockEntity(matrix, TileInfusionMatrix.class, te -> te.active = true);
        scene.overlay().showText(70)
                .text("Right-click the Matrix with a Caster's Gauntlet to wake it up")
                .pointAt(util.vector().centerOf(matrix))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        // Pedestals around, in symmetric pairs.
        Selection outerSel = util.select().position(outer[0]);
        for (int i = 1; i < 4; i++) {
            outerSel = outerSel.add(util.select().position(outer[i]));
        }
        scene.world().showSection(outerSel, Direction.DOWN);
        scene.idle(10);
        scene.world().modifyBlockEntity(center, TilePedestal.class, te -> te.setItem(0, new ItemStack(Items.DIAMOND)));
        ItemStack[] ingredients = {new ItemStack(Items.GOLD_INGOT), new ItemStack(Items.GOLD_INGOT),
                new ItemStack(Items.REDSTONE), new ItemStack(Items.REDSTONE)};
        for (int i = 0; i < 4; i++) {
            ItemStack s = ingredients[i];
            scene.world().modifyBlockEntity(outer[i], TilePedestal.class, te -> te.setItem(0, s.copy()));
            scene.idle(5);
        }
        scene.overlay().showText(90)
                .text("The central item goes on the pedestal under the Matrix. The other ingredients go on pedestals around it, up to 8 blocks away")
                .pointAt(util.vector().topOf(center))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(100);

        // Stability.
        Selection candleSel = util.select().position(candles[0]);
        for (int i = 1; i < 4; i++) {
            candleSel = candleSel.add(util.select().position(candles[i]));
        }
        scene.world().showSection(candleSel, Direction.DOWN);
        scene.idle(10);
        scene.overlay().showOutline(PonderPalette.GREEN, "pairs", outerSel.copy().add(candleSel), 90);
        scene.overlay().showText(90)
                .text("Stability: everything around the altar should come in pairs, mirrored across the Matrix. Candles in pairs add stability")
                .colored(PonderPalette.GREEN)
                .pointAt(util.vector().topOf(candles[1]))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(100);

        scene.world().hideSection(util.select().position(candles[3]), Direction.UP);
        scene.idle(15);
        scene.overlay().showOutline(PonderPalette.RED, "lonely", util.select().position(candles[2]), 80);
        scene.overlay().showText(80)
                .text("A block without its partner, or a pedestal whose pair is empty, lowers stability. Unstable infusions can throw items, zap you or explode")
                .colored(PonderPalette.RED)
                .pointAt(util.vector().topOf(candles[2]))
                .placeNearTarget();
        scene.idle(90);
        scene.world().showSection(util.select().position(candles[3]), Direction.DOWN);
        scene.idle(15);

        scene.overlay().showControls(util.vector().blockSurface(matrix, Direction.WEST), Pointing.LEFT, 40).rightClick()
                .withItem(new ItemStack(ConfigItems.casterBasic.get()));
        scene.idle(10);
        scene.world().modifyBlockEntity(matrix, TileInfusionMatrix.class, te -> te.crafting = true);
        scene.overlay().showText(90)
                .text("With jars holding the needed essentia within 12 blocks, right-click the Matrix again to start the infusion")
                .pointAt(util.vector().centerOf(matrix))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(100);
        scene.markAsFinished();
    }

    // ---- (c) smelting: smeltery, alembic, tube, jar and bellows ----
    public static void smelting(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("smelting", "Distilling Essentia");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();
        scene.idle(10);

        BlockPos smelter = util.grid().at(2, 1, 2);
        BlockPos alembic = util.grid().at(2, 2, 2);
        BlockPos jar = util.grid().at(4, 1, 2);
        Selection tubes = util.select().fromTo(3, 2, 2, 4, 2, 2);
        Selection bellows = util.select().position(1, 1, 2).add(util.select().position(2, 1, 1));

        scene.world().showSection(util.select().position(smelter), Direction.DOWN);
        scene.idle(15);
        scene.overlay().showText(80)
                .text("The Essentia Smeltery breaks items down into essentia. Fuel goes in the bottom slot, items to smelt in the top one")
                .pointAt(util.vector().blockSurface(smelter, Direction.SOUTH))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(30);
        scene.overlay().showControls(util.vector().blockSurface(smelter, Direction.SOUTH), Pointing.DOWN, 40).rightClick()
                .withItem(new ItemStack(Items.COAL));
        scene.idle(10);
        scene.world().modifyBlock(smelter, s -> s.setValue(BlockSmelter.ENABLED, true), false);
        scene.idle(50);

        scene.world().showSection(util.select().position(alembic), Direction.DOWN);
        scene.idle(15);
        scene.overlay().showText(70)
                .text("Alembics stacked on top collect the essentia it makes")
                .pointAt(util.vector().centerOf(alembic))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(80);

        scene.world().showSection(tubes, Direction.WEST);
        scene.idle(10);
        scene.world().showSection(util.select().position(jar), Direction.DOWN);
        scene.idle(15);
        scene.overlay().showText(80)
                .text("A tube on the side of an alembic carries the essentia away, here into a Warded Jar")
                .pointAt(util.vector().centerOf(3, 2, 2))
                .placeNearTarget()
                .attachKeyFrame();
        for (int i = 1; i <= 8; i++) {
            int amount = i * 8;
            scene.world().modifyBlockEntity(jar, TileJarFillable.class, te -> {
                te.aspect = Aspect.FIRE;
                te.amount = amount;
            });
            scene.idle(10);
        }
        scene.idle(10);

        scene.world().showSection(bellows, Direction.DOWN);
        scene.idle(15);
        scene.overlay().showOutline(PonderPalette.GREEN, "bellows", bellows, 80);
        scene.overlay().showText(80)
                .text("Bellows on the sides or the back of the smeltery, pointing at it, make it work 12.5% faster each")
                .colored(PonderPalette.GREEN)
                .pointAt(util.vector().centerOf(1, 1, 2))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(90);

        scene.overlay().showText(80)
                .text("Not all essentia survives: the basic smeltery loses about a fifth of it as flux into the aura")
                .colored(PonderPalette.RED)
                .pointAt(util.vector().topOf(alembic))
                .placeNearTarget();
        scene.idle(90);
        scene.markAsFinished();
    }

    // ---- (d) arcane workbench: salis mundus on a crafting table, vis and crystals ----
    public static void arcaneWorkbench(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("arcane_workbench", "The Arcane Workbench");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();
        scene.idle(10);

        BlockPos bench = util.grid().at(2, 1, 2);
        BlockState workbench = ConfigBlocks.arcaneWorkbench.get().defaultBlockState();
        scene.world().setBlock(bench, Blocks.CRAFTING_TABLE.defaultBlockState(), false);
        scene.world().showSection(util.select().position(bench), Direction.DOWN);
        scene.idle(15);

        scene.overlay().showControls(util.vector().topOf(bench), Pointing.DOWN, 40).rightClick()
                .withItem(new ItemStack(ConfigItems.salisMundus.get()));
        scene.idle(15);
        scene.world().setBlock(bench, workbench, true);
        scene.overlay().showText(80)
                .text("Once your research has begun, sprinkle Salis Mundus on a Crafting Table to turn it into an Arcane Workbench")
                .pointAt(util.vector().topOf(bench))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(90);

        scene.overlay().showText(90)
                .text("Arcane recipes are laid out like normal ones, but they also cost vis, drawn from the aura of the chunk the workbench stands in")
                .pointAt(util.vector().centerOf(bench))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(100);

        scene.overlay().showControls(util.vector().topOf(bench), Pointing.DOWN, 50)
                .withItem(ConfigItems.crystalEssence.get().makeCrystal(Aspect.ORDER, 1));
        scene.overlay().showText(80)
                .text("Some recipes also ask for vis crystals, placed in the six slots around the crafting grid")
                .pointAt(util.vector().topOf(bench))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(90);

        scene.overlay().showText(70)
                .text("Only recipes you have researched can be made, and the chunk must hold enough vis")
                .colored(PonderPalette.RED)
                .pointAt(util.vector().centerOf(bench))
                .placeNearTarget();
        scene.idle(80);
        scene.markAsFinished();
    }

    // ---- (e) research table: scribing tools on a wooden table, notes and bonus aspects ----
    public static void researchTable(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("research_table", "The Research Table");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();
        scene.idle(10);

        BlockPos table = util.grid().at(2, 1, 2);
        BlockPos water = util.grid().at(0, 1, 4);
        BlockPos dirt = util.grid().at(4, 1, 4);
        BlockPos torch = util.grid().at(0, 1, 0);
        BlockState research = ConfigBlocks.researchTable.get().defaultBlockState()
                .setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH);
        scene.world().setBlock(table, ConfigBlocks.tableWood.get().defaultBlockState(), false);
        scene.world().showSection(util.select().position(table), Direction.DOWN);
        scene.idle(15);

        ItemStack tools = new ItemStack(ConfigItems.scribingTools.get());
        scene.overlay().showControls(util.vector().topOf(table), Pointing.DOWN, 40).rightClick().withItem(tools);
        scene.idle(15);
        scene.world().setBlock(table, research, true);
        scene.world().modifyBlockEntity(table, TileResearchTable.class, te -> te.setItem(0, tools.copy()));
        scene.overlay().showText(80)
                .text("Right-click a Wooden Table with Scribing Tools to turn it into a Research Table. The tools stay on it")
                .pointAt(util.vector().topOf(table))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(90);

        scene.overlay().showText(80)
                .text("Put a research note in the table next to the tools to work on it")
                .pointAt(util.vector().centerOf(table))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(90);

        scene.overlay().showText(100)
                .text("Write aspects on the hexagons of the note to link the aspects already on it. Neighbouring aspects must be related: one has to be made from the other")
                .pointAt(util.vector().topOf(table))
                .placeNearTarget();
        scene.idle(110);

        scene.overlay().showText(70)
                .text("Every aspect you write uses up some ink from the Scribing Tools")
                .colored(PonderPalette.RED)
                .pointAt(util.vector().topOf(table))
                .placeNearTarget();
        scene.idle(80);

        Selection around = util.select().position(water).add(util.select().position(dirt)).add(util.select().position(torch));
        scene.world().showSection(around, Direction.DOWN);
        scene.idle(15);
        scene.overlay().showOutline(PonderPalette.GREEN, "around", around, 100);
        scene.overlay().showText(100)
                .text("Now and then the surroundings give the table a free primal point: water gives Aqua, dirt Terra, fire or lava Ignis, redstone devices Ordo. Darkness and height help too")
                .colored(PonderPalette.GREEN)
                .pointAt(util.vector().centerOf(water))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(110);
        scene.markAsFinished();
    }

    // Shared by both tube scenes: the jar on the left drains into the labelled jar on the right.
    private static void flow(SceneBuilder scene, BlockPos from, BlockPos to, Aspect aspect, int steps) {
        for (int i = 1; i <= steps; i++) {
            int moved = i * 4;
            scene.world().modifyBlockEntity(from, TileJarFillable.class, te -> te.amount = Math.max(0, 32 - moved));
            scene.world().modifyBlockEntity(to, TileJarFillable.class, te -> {
                te.aspect = aspect;
                te.amount = moved;
            });
            scene.idle(8);
        }
    }

    private static void fillJars(SceneBuilder scene, BlockPos from, BlockPos to, Aspect aspect) {
        scene.world().modifyBlockEntity(from, TileJarFillable.class, te -> {
            te.aspect = aspect;
            te.amount = 32;
        });
        scene.world().modifyBlockEntity(to, TileJarFillable.class, te -> {
            te.aspect = null;
            te.amount = 0;
            te.aspectFilter = aspect;
        });
    }

    private static BlockState tubeState(Block block) {
        return block.defaultBlockState().setValue(BlockTube.WEST, true).setValue(BlockTube.EAST, true);
    }

    // ---- (f) essentia tubes: suction, valve and filter ----
    public static void essentiaTubes(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("essentia_tubes", "Essentia Tubes");
        scene.configureBasePlate(0, 0, 7);
        scene.showBasePlate();
        scene.idle(10);

        BlockPos source = util.grid().at(1, 1, 2);
        BlockPos target = util.grid().at(5, 1, 2);
        BlockPos middle = util.grid().at(3, 2, 2);
        fillJars(scene, source, target, Aspect.FIRE);

        scene.world().showSection(util.select().position(source).add(util.select().position(target)), Direction.DOWN);
        scene.idle(10);
        scene.world().showSection(util.select().fromTo(1, 2, 2, 5, 2, 2), Direction.DOWN);
        scene.idle(15);

        scene.overlay().showText(90)
                .text("Essentia moves through tubes towards whatever pulls on it hardest. That pull is called suction")
                .pointAt(util.vector().centerOf(middle))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(100);

        scene.overlay().showText(80)
                .text("A labelled Warded Jar pulls harder than an unlabelled one, so the essentia flows into it")
                .pointAt(util.vector().topOf(target))
                .placeNearTarget();
        flow(scene, source, target, Aspect.FIRE, 4);
        scene.idle(50);

        // Valve.
        scene.world().setBlock(middle, tubeState(ConfigBlocks.tubeValve.get()), true);
        scene.idle(15);
        scene.overlay().showControls(util.vector().topOf(middle), Pointing.DOWN, 40).rightClick();
        scene.idle(10);
        scene.world().modifyBlockEntity(middle, TileTubeValve.class, te -> te.allowFlow = false);
        scene.overlay().showText(90)
                .text("The Essentia Valve closes the line when right-clicked with an empty hand, or while it gets a redstone signal")
                .pointAt(util.vector().centerOf(middle))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(100);
        scene.overlay().showControls(util.vector().topOf(middle), Pointing.DOWN, 30).rightClick();
        scene.idle(10);
        scene.world().modifyBlockEntity(middle, TileTubeValve.class, te -> te.allowFlow = true);
        scene.idle(20);

        // Filter.
        scene.world().setBlock(middle, tubeState(ConfigBlocks.tubeFilter.get()), true);
        scene.idle(15);
        scene.overlay().showControls(util.vector().topOf(middle), Pointing.DOWN, 40).rightClick()
                .withItem(ItemPhial.makeFilledPhial(Aspect.FIRE));
        scene.idle(10);
        scene.world().modifyBlockEntity(middle, TileTubeFilter.class, te -> te.aspectFilter = Aspect.FIRE);
        scene.overlay().showText(100)
                .text("The Essentia Filter lets a single aspect through. Right-click it with a filled phial to choose it, and sneak and right-click to clear it")
                .pointAt(util.vector().centerOf(middle))
                .placeNearTarget()
                .attachKeyFrame();
        flow(scene, source, target, Aspect.FIRE, 4);
        scene.idle(80);
        scene.markAsFinished();
    }

    // ---- (g) the other tubes: restricted, directional and buffer ----
    public static void tubeVariants(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("tube_variants", "Special Essentia Tubes");
        scene.configureBasePlate(0, 0, 7);
        scene.showBasePlate();
        scene.idle(10);

        BlockPos source = util.grid().at(1, 1, 2);
        BlockPos target = util.grid().at(5, 1, 2);
        BlockPos middle = util.grid().at(3, 2, 2);
        fillJars(scene, source, target, Aspect.WATER);
        scene.world().setBlock(middle, tubeState(ConfigBlocks.tubeRestrict.get()), false);

        scene.world().showSection(util.select().position(source).add(util.select().position(target)), Direction.DOWN);
        scene.idle(10);
        scene.world().showSection(util.select().fromTo(1, 2, 2, 5, 2, 2), Direction.DOWN);
        scene.idle(15);

        scene.overlay().showText(90)
                .text("The Restricted Tube halves the suction that passes through it, so other paths with stronger suction win")
                .pointAt(util.vector().centerOf(middle))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(100);

        // Directional tube.
        scene.world().setBlock(middle, tubeState(ConfigBlocks.tubeOneway.get()), true);
        scene.idle(15);
        scene.overlay().showControls(util.vector().topOf(middle), Pointing.DOWN, 40).rightClick()
                .withItem(new ItemStack(ConfigItems.casterBasic.get()));
        scene.idle(10);
        scene.overlay().showText(90)
                .text("The Directional Tube lets essentia through in one direction only. Right-click its middle with a Caster's Gauntlet to turn it")
                .pointAt(util.vector().centerOf(middle))
                .placeNearTarget()
                .attachKeyFrame();
        flow(scene, source, target, Aspect.WATER, 4);
        scene.idle(70);

        // Buffer.
        scene.world().setBlock(middle, tubeState(ConfigBlocks.tubeBuffer.get()), true);
        scene.idle(15);
        scene.world().modifyBlockEntity(middle, TileTubeBuffer.class, te -> te.aspects = new AspectList().add(Aspect.WATER, 10));
        scene.overlay().showText(90)
                .text("The Essentia Buffer holds up to 10 essentia in the line and hands it on with a very weak suction")
                .pointAt(util.vector().centerOf(middle))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(100);
        scene.overlay().showControls(util.vector().blockSurface(middle, Direction.EAST), Pointing.DOWN, 40).rightClick()
                .withItem(new ItemStack(ConfigItems.casterBasic.get()));
        scene.idle(10);
        scene.overlay().showText(90)
                .text("Right-click one of its sides with a Caster's Gauntlet to choke it: once for low suction on that side, twice to close it")
                .pointAt(util.vector().blockSurface(middle, Direction.EAST))
                .placeNearTarget();
        scene.idle(100);
        scene.markAsFinished();
    }

    // ---- (h) centrifuge: splitting compound essentia ----
    public static void centrifuge(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("centrifuge", "The Essentia Centrifuge");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();
        scene.idle(10);

        BlockPos source = util.grid().at(2, 1, 2);
        BlockPos spin = util.grid().at(2, 2, 2);
        BlockPos left = util.grid().at(1, 1, 2);
        BlockPos right = util.grid().at(3, 1, 2);
        scene.world().modifyBlockEntity(source, TileJarFillable.class, te -> {
            te.aspect = Aspect.CRYSTAL;
            te.amount = 32;
        });

        scene.world().showSection(util.select().position(source), Direction.DOWN);
        scene.idle(10);
        scene.world().showSection(util.select().position(spin), Direction.DOWN);
        scene.idle(15);
        scene.overlay().showText(90)
                .text("The Essentia Centrifuge splits compound essentia back into the two aspects it is made of. It pulls essentia in from below")
                .pointAt(util.vector().centerOf(spin))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(100);

        scene.world().showSection(util.select().fromTo(1, 1, 2, 3, 3, 2).substract(util.select().fromTo(2, 1, 2, 2, 2, 2)),
                Direction.DOWN);
        scene.idle(15);
        scene.overlay().showText(100)
                .text("Every point comes out of the top as one of its two components, picked at random. Here Vitreus becomes Terra or Aer")
                .pointAt(util.vector().topOf(spin))
                .placeNearTarget()
                .attachKeyFrame();
        Aspect[] outs = {Aspect.EARTH, Aspect.AIR, Aspect.AIR, Aspect.EARTH, Aspect.EARTH, Aspect.AIR};
        int[] counts = {0, 0};
        for (int i = 0; i < outs.length; i++) {
            int left0 = 32 - (i + 1);
            Aspect out = outs[i];
            int side = out == Aspect.EARTH ? 0 : 1;
            int amount = ++counts[side];
            scene.world().modifyBlockEntity(source, TileJarFillable.class, te -> te.amount = left0);
            scene.world().modifyBlockEntity(spin, TileCentrifuge.class, te -> te.aspectIn = Aspect.CRYSTAL);
            scene.idle(10);
            scene.world().modifyBlockEntity(side == 0 ? left : right, TileJarFillable.class, te -> {
                te.aspect = out;
                te.amount = amount;
            });
            scene.idle(8);
        }
        scene.idle(30);

        scene.overlay().showText(80)
                .text("Primal aspects cannot be split, and a redstone signal stops the centrifuge")
                .colored(PonderPalette.RED)
                .pointAt(util.vector().centerOf(spin))
                .placeNearTarget();
        scene.idle(90);
        scene.markAsFinished();
    }
}
