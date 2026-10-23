package voidexpeditions.block;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.common.lib.SoundsTC;
import voidexpeditions.registry.VEBlocks;

/**
 * The void gate: a ring of 16 segments standing upright, seven blocks across like a stargate, around 21 empty
 * blocks, and its expedition terminal standing on the ground two blocks in front of or behind the lowest segment,
 * like a dialing device. A caster on a segment or on the terminal forms the whole: the segments light up, the void
 * horizon fills the ring and the terminal is linked to it. Taking any part away collapses the horizon.
 * <p>
 * Offsets are (u, v): u along the ring's horizontal axis, v up, from the middle of the ring.
 */
public final class GateStructure {
    public static final int RADIUS = 3;
    public static final List<int[]> FRAME = List.of(
            new int[]{-1, 3}, new int[]{0, 3}, new int[]{1, 3},
            new int[]{-2, 2}, new int[]{2, 2},
            new int[]{-3, 1}, new int[]{3, 1},
            new int[]{-3, 0}, new int[]{3, 0},
            new int[]{-3, -1}, new int[]{3, -1},
            new int[]{-2, -2}, new int[]{2, -2},
            new int[]{-1, -3}, new int[]{0, -3}, new int[]{1, -3});
    public static final List<int[]> INTERIOR;

    static {
        ArrayList<int[]> in = new ArrayList<>();
        for (int v = -2; v <= 2; v++) {
            int half = Math.abs(v) == 2 ? 1 : 2;
            for (int u = -half; u <= half; u++) in.add(new int[]{u, v});
        }
        INTERIOR = List.copyOf(in);
    }

    /** Set while a gate is being taken apart, so its own block changes do not start another collapse. */
    private static boolean collapsing;

    private GateStructure() {
    }

    /** How far in front of or behind the lowest segment the terminal stands. */
    public static final int TERMINAL_DISTANCE = 2;

    private static BlockPos at(BlockPos center, Direction.Axis axis, int u, int v) {
        return axis == Direction.Axis.X ? center.offset(u, v, 0) : center.offset(0, v, u);
    }

    /** The horizontal direction the ring faces: across its plane. */
    private static Direction facing(Direction.Axis axis) {
        return axis == Direction.Axis.X ? Direction.SOUTH : Direction.EAST;
    }

    /** An unlinked terminal in either place of this ring, or null. */
    @Nullable
    private static ExpeditionTerminalBlockEntity findTerminal(Level level, BlockPos center, Direction.Axis axis) {
        BlockPos foot = center.below(RADIUS);
        for (int d : new int[]{TERMINAL_DISTANCE, -TERMINAL_DISTANCE}) {
            if (level.getBlockEntity(foot.relative(facing(axis), d)) instanceof ExpeditionTerminalBlockEntity t && !t.isLinked()) {
                return t;
            }
        }
        return null;
    }

    /** The middle of a whole, unformed ring through this segment, along the given axis; null if there is none. */
    @Nullable
    private static BlockPos findCenter(Level level, BlockPos segment, Direction.Axis axis) {
        for (int[] f : FRAME) {
            BlockPos center = at(segment, axis, -f[0], -f[1]);
            if (isWholeRing(level, center, axis)) return center;
        }
        return null;
    }

    private static boolean isWholeRing(Level level, BlockPos center, Direction.Axis axis) {
        for (int[] f : FRAME) {
            BlockState s = level.getBlockState(at(center, axis, f[0], f[1]));
            if (!s.is(VEBlocks.VOID_GATE_RING.get()) || s.getValue(VoidGateRingBlock.FORMED)) return false;
        }
        for (int[] i : INTERIOR) {
            if (!level.getBlockState(at(center, axis, i[0], i[1])).canBeReplaced()) return false;
        }
        return true;
    }

    private static final Direction.Axis[] AXES = {Direction.Axis.X, Direction.Axis.Z};

    /** A caster on a segment: forms the gate it belongs to, if the ring is whole and its terminal in place. */
    public static boolean tryFormFromSegment(Level level, BlockPos segment, Player player) {
        for (Direction.Axis axis : AXES) {
            BlockPos center = findCenter(level, segment, axis);
            if (center != null) return tryForm(level, center, axis, player);
        }
        say(level, player, "voidexpeditions.gate.incomplete");
        return false;
    }

    /** A caster on a terminal: forms the gate it stands before, if there is a whole ring there. */
    public static boolean tryFormFromTerminal(Level level, BlockPos terminal, Player player) {
        for (Direction.Axis axis : AXES) {
            for (int d : new int[]{TERMINAL_DISTANCE, -TERMINAL_DISTANCE}) {
                BlockPos center = terminal.relative(facing(axis), -d).above(RADIUS);
                if (isWholeRing(level, center, axis)) return tryForm(level, center, axis, player);
            }
        }
        say(level, player, "voidexpeditions.gate.no_ring");
        return false;
    }

    private static boolean tryForm(Level level, BlockPos center, Direction.Axis axis, Player player) {
        ExpeditionTerminalBlockEntity terminal = findTerminal(level, center, axis);
        if (terminal == null) {
            say(level, player, "voidexpeditions.gate.no_terminal_slot");
            return false;
        }
        if (level instanceof ServerLevel sl) form(sl, center, axis, terminal);
        return true;
    }

    private static void say(Level level, Player player, String key) {
        if (!level.isClientSide) {
            player.displayClientMessage(Component.translatable(key).withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC), true);
        }
    }

    private static void form(ServerLevel level, BlockPos center, Direction.Axis axis, ExpeditionTerminalBlockEntity terminal) {
        terminal.link(center);
        for (int[] f : FRAME) {
            BlockPos p = at(center, axis, f[0], f[1]);
            level.setBlock(p, level.getBlockState(p).setValue(VoidGateRingBlock.FORMED, true), Block.UPDATE_ALL);
        }
        BlockState horizon = VEBlocks.VOID_HORIZON.get().defaultBlockState().setValue(VoidHorizonBlock.AXIS, axis);
        for (int[] i : INTERIOR) {
            BlockPos p = at(center, axis, i[0], i[1]);
            level.setBlock(p, horizon, Block.UPDATE_ALL);
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5, 12, 0.4, 0.4, 0.4, 0.15);
        }
        level.playSound(null, center, SoundEvents.END_PORTAL_SPAWN, SoundSource.BLOCKS, 0.7f, 1.4f);
        level.playSound(null, center, SoundsTC.CRAFTSTART.get(), SoundSource.BLOCKS, 1.0f, 0.8f);
    }

    /**
     * A segment, the horizon or the terminal was taken away (for the terminal, {@code from} is the ring's middle):
     * every horizon block and lit segment joined to it goes out, and the terminals linked to it are freed. Lit
     * segments touch each other only at corners, so the search goes through all 26 neighbours.
     */
    public static void collapse(Level level, BlockPos from) {
        if (collapsing || level.isClientSide) return;
        collapsing = true;
        try {
            Set<BlockPos> seen = new HashSet<>();
            Deque<BlockPos> queue = new ArrayDeque<>();
            queue.add(from);
            seen.add(from);
            while (!queue.isEmpty()) {
                BlockPos p = queue.poll();
                for (BlockPos n : BlockPos.betweenClosed(p.offset(-1, -1, -1), p.offset(1, 1, 1))) {
                    if (n.distManhattan(from) > 4 * RADIUS || !seen.add(n.immutable())) continue;
                    BlockState s = level.getBlockState(n);
                    if (s.is(VEBlocks.VOID_HORIZON.get())) {
                        level.setBlock(n, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                        queue.add(n.immutable());
                    } else if (s.is(VEBlocks.VOID_GATE_RING.get()) && s.getValue(VoidGateRingBlock.FORMED)) {
                        level.setBlock(n, s.setValue(VoidGateRingBlock.FORMED, false), Block.UPDATE_ALL);
                        queue.add(n.immutable());
                    }
                }
            }
            int reach = RADIUS + TERMINAL_DISTANCE + 2;
            for (BlockPos p : BlockPos.betweenClosed(from.offset(-reach, -reach, -reach), from.offset(reach, reach, reach))) {
                if (level.getBlockEntity(p) instanceof ExpeditionTerminalBlockEntity t && t.isLinked() && seen.contains(t.getGateCenter())) {
                    t.unlink();
                }
            }
            level.playSound(null, from, SoundEvents.BEACON_DEACTIVATE, SoundSource.BLOCKS, 1.0f, 0.6f);
        } finally {
            collapsing = false;
        }
    }
}
