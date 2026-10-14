package thaumcraft.api.golems.seals;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public class SealPos {
    public BlockPos pos;
    public Direction face;

    public SealPos(BlockPos pos, Direction face) {
        this.pos = pos;
        this.face = face;
    }

    @Override
    public int hashCode() {
        byte b0 = (byte) (face.ordinal() + 1);
        int i = 31 * b0 + pos.getX();
        i = 31 * i + pos.getY();
        i = 31 * i + pos.getZ();
        return i;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SealPos sp)) {
            return false;
        }
        return pos.equals(sp.pos) && face.equals(sp.face);
    }
}
