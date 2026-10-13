package thaumcraft.api.crafting;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** Port of thaumcraft.api.crafting.IDustTrigger: what happens when salis mundus is used on a block. */
public interface IDustTrigger {

    /**
     * Checks whether using dust on this block and face is a valid operation. Runs on client and server.
     * @return the placement offset (from the clicked block) and facing, or null if not valid
     */
    @Nullable
    Placement getValidFace(Level world, Player player, BlockPos pos, Direction face);

    record Placement(int xOffset, int yOffset, int zOffset, @Nullable Direction facing) {
    }

    /** The operation to perform if the location is valid. Runs on client and server. */
    void execute(Level world, Player player, BlockPos pos, Placement placement, Direction side);

    /** Blocks that should show the dust sparkle fx. By default the clicked block. */
    default List<BlockPos> sparkle(Level world, Player player, BlockPos pos, Placement placement) {
        return List.of(pos);
    }

    List<IDustTrigger> triggers = new ArrayList<>();

    static void registerDustTrigger(IDustTrigger trigger) {
        triggers.add(trigger);
    }
}
