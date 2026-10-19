package thaumcraft.common.world.dim;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

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
        MazeHandler.generateEldritch(context.level(), context.random(), origin.getX() >> 4, origin.getZ() >> 4);
        return true;
    }
}
