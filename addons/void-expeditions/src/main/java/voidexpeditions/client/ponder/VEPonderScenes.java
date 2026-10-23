package voidexpeditions.client.ponder;

import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import thaumcraft.common.config.ConfigItems;
import voidexpeditions.block.GateStructure;
import voidexpeditions.block.VoidGateRingBlock;
import voidexpeditions.block.VoidHorizonBlock;
import voidexpeditions.registry.VEBlocks;
import voidexpeditions.registry.VEItems;

/**
 * The void gate's tutorials, on void_gate.nbt: an unlit ring standing along x with its middle at (4, 4, 4), and its
 * terminal on the ground at (4, 1, 6). The texts given here are the English defaults; the lang files hold them too.
 */
public final class VEPonderScenes {
    private VEPonderScenes() {
    }

    private static BlockPos center(SceneBuildingUtil util) {
        return util.grid().at(4, 4, 4);
    }

    private static BlockPos at(SceneBuildingUtil util, int[] uv) {
        return center(util).offset(uv[0], uv[1], 0);
    }

    /** The ring's segments in order around the circle, from the lowest one, as a stargate is dialed. */
    private static int[][] aroundTheRing() {
        return new int[][]{{0, -3}, {1, -3}, {2, -2}, {3, -1}, {3, 0}, {3, 1}, {2, 2}, {1, 3}, {0, 3}, {-1, 3},
                {-2, 2}, {-3, 1}, {-3, 0}, {-3, -1}, {-2, -2}, {-1, -3}};
    }

    private static Selection interior(SceneBuildingUtil util) {
        Selection sel = null;
        for (int[] i : GateStructure.INTERIOR) {
            Selection p = util.select().position(at(util, i));
            sel = sel == null ? p : sel.add(p);
        }
        return sel;
    }

    /** Lights the ring and fills it with the horizon, as forming does. */
    private static void light(SceneBuilder scene, SceneBuildingUtil util, boolean lit) {
        BlockState ring = VEBlocks.VOID_GATE_RING.get().defaultBlockState().setValue(VoidGateRingBlock.FORMED, lit);
        for (int[] f : GateStructure.FRAME) scene.world().setBlock(at(util, f), ring, false);
        BlockState inside = lit ? VEBlocks.VOID_HORIZON.get().defaultBlockState().setValue(VoidHorizonBlock.AXIS, Direction.Axis.X)
                : Blocks.AIR.defaultBlockState();
        for (int[] i : GateStructure.INTERIOR) scene.world().setBlock(at(util, i), inside, lit);
    }

    public static void voidGate(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("void_gate", "Raising the Void Gate");
        scene.configureBasePlate(0, 0, 9);
        scene.showBasePlate();
        scene.idle(10);

        BlockPos center = center(util);
        BlockPos terminal = util.grid().at(4, 1, 6);
        for (int[] f : aroundTheRing()) {
            scene.world().showSection(util.select().position(at(util, f)), Direction.DOWN);
            scene.idle(3);
        }
        scene.world().showSection(interior(util), Direction.DOWN);
        scene.idle(10);
        scene.overlay().showText(90)
                .text("Sixteen Void Gate Rings stand upright in a circle seven blocks across. Nothing may be inside it")
                .pointAt(util.vector().centerOf(center))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(100);

        scene.world().showSection(util.select().position(terminal), Direction.DOWN);
        scene.idle(10);
        scene.overlay().showOutline(PonderPalette.GREEN, "terminal", util.select().position(terminal), 80);
        scene.overlay().showText(90)
                .text("Its Expedition Terminal goes on the ground, two blocks in front of or behind the lowest segment")
                .pointAt(util.vector().topOf(terminal))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(100);

        scene.overlay().showControls(util.vector().topOf(terminal), Pointing.DOWN, 40).rightClick()
                .withItem(new ItemStack(ConfigItems.casterBasic.get()));
        scene.idle(15);
        light(scene, util, true);
        scene.overlay().showText(90)
                .text("Touch the terminal or any segment with a Caster: the ring lights up and the void horizon fills it")
                .pointAt(util.vector().centerOf(center))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(100);

        scene.overlay().showControls(util.vector().topOf(terminal), Pointing.DOWN, 40).rightClick();
        scene.idle(10);
        scene.overlay().showText(100)
                .text("Right-click the terminal to choose the difficulty and length of the expedition, and to spend Void Shards in its shop")
                .pointAt(util.vector().topOf(terminal))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(110);

        BlockPos breakAt = at(util, new int[]{3, 0});
        scene.overlay().showControls(util.vector().blockSurface(breakAt, Direction.EAST), Pointing.LEFT, 30).leftClick()
                .withItem(new ItemStack(Items.DIAMOND_PICKAXE));
        scene.idle(10);
        light(scene, util, false);
        scene.world().setBlock(breakAt, Blocks.AIR.defaultBlockState(), true);
        scene.overlay().showText(80)
                .text("Take any part away, a segment or the terminal, and the horizon collapses")
                .pointAt(util.vector().centerOf(breakAt))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(90);
    }

    public static void expedition(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("void_expedition", "Setting out on an Expedition");
        scene.configureBasePlate(0, 0, 9);
        scene.showBasePlate();
        light(scene, util, true);
        scene.world().showSection(util.select().fromTo(0, 1, 0, 8, 7, 8), Direction.DOWN);
        scene.idle(20);

        BlockPos center = center(util);
        scene.overlay().showText(90)
                .text("Step into the horizon: the void takes you through a chain of labyrinths, as many as the terminal says")
                .pointAt(util.vector().centerOf(center))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(100);

        Vec3 front = util.vector().of(4.5, 2.5, 2.0);
        scene.world().createItemEntity(front, Vec3.ZERO, new ItemStack(VEItems.VOID_SIGIL.get()));
        scene.overlay().showText(100)
                .text("In each labyrinth a Void Sigil waits in the key room. Carry it to the portal in the middle to go on")
                .pointAt(front)
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(110);

        scene.overlay().showText(100)
                .colored(PonderPalette.BLUE)
                .text("The portal offers three gifts of the void: pick one. Gifts stack, and last only while the expedition does")
                .pointAt(util.vector().centerOf(center))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(110);

        scene.overlay().showText(100)
                .colored(PonderPalette.RED)
                .text("The last labyrinth has no sigil: open its boss room with the runed tablet and defeat the guardian")
                .pointAt(util.vector().centerOf(center))
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(110);

        Vec3 shards = util.vector().of(4.5, 2.5, 6.5);
        scene.world().createItemEntity(shards, Vec3.ZERO, new ItemStack(VEItems.VOID_SHARD.get(), 8));
        scene.overlay().showText(100)
                .colored(PonderPalette.GREEN)
                .text("Come back with treasure and Void Shards. Dying loses the expedition and its gifts, but not what you carry")
                .pointAt(shards)
                .placeNearTarget()
                .attachKeyFrame();
        scene.idle(110);
    }
}
