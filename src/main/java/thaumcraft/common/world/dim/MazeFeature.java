package thaumcraft.common.world.dim;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import thaumcraft.api.maze.IMazeWorld;
import thaumcraft.api.maze.MazeWorlds;

/**
 * ThaumcraftWorldGenerator.worldGeneration in the Outer Lands: each chunk builds its maze cell, if it has one
 * (MazeHandler.generateEldritch). Placed by the eldritch biome.
 */
public class MazeFeature extends Feature<NoneFeatureConfiguration> {
    public MazeFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        BlockPos origin = context.origin();
        int cx = origin.getX() >> 4;
        int cz = origin.getZ() >> 4;
        // Other maze worlds share the maze map; each dimension only builds its own cells.
        IMazeWorld maze = MazeWorlds.get(context.level().getLevel());
        if (maze != null ? !maze.ownsChunk(cx, cz) : MazeWorlds.isClaimed(cx, cz)) return true;
        MazeHandler.generateEldritch(context.level(), context.random(), cx, cz);
        return true;
    }
}
