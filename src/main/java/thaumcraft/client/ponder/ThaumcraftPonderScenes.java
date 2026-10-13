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
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.common.blocks.essentia.BlockSmelter;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.tiles.crafting.TileCrucible;
import thaumcraft.common.tiles.crafting.TileInfusionMatrix;
import thaumcraft.common.tiles.crafting.TilePedestal;
import thaumcraft.common.tiles.essentia.TileJarFillable;

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
}
