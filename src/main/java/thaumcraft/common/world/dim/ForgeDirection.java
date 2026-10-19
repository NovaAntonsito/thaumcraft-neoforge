package thaumcraft.common.world.dim;

/**
 * The 1.7 ForgeDirection, kept for the maze generators: their stair metas depend on its ordinals
 * (DOWN, UP, NORTH, SOUTH, WEST, EAST, UNKNOWN).
 */
public enum ForgeDirection {
    DOWN(0, -1, 0),
    UP(0, 1, 0),
    NORTH(0, 0, -1),
    SOUTH(0, 0, 1),
    WEST(-1, 0, 0),
    EAST(1, 0, 0),
    UNKNOWN(0, 0, 0);

    static final ForgeDirection[] VALID_DIRECTIONS = {DOWN, UP, NORTH, SOUTH, WEST, EAST};
    private static final int[] OPPOSITES = {1, 0, 3, 2, 5, 4, 6};

    final int offsetX;
    final int offsetY;
    final int offsetZ;

    ForgeDirection(int x, int y, int z) {
        this.offsetX = x;
        this.offsetY = y;
        this.offsetZ = z;
    }

    ForgeDirection getOpposite() {
        return values()[OPPOSITES[ordinal()]];
    }
}
