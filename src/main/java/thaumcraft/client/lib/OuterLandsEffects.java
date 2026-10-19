package thaumcraft.client.lib;

import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.world.phys.Vec3;

/**
 * The client half of TC4's WorldProviderOuter: no sky and no clouds, fog everywhere, and a fixed dim purple fog
 * (10518688 scaled to 0.15).
 */
public class OuterLandsEffects extends DimensionSpecialEffects {
    public OuterLandsEffects() {
        super(Float.NaN, false, SkyType.NONE, false, false);
    }

    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 fogColor, float brightness) {
        int i = 10518688;
        return new Vec3((i >> 16 & 255) / 255.0 * 0.15, (i >> 8 & 255) / 255.0 * 0.15, (i & 255) / 255.0 * 0.15);
    }

    @Override
    public boolean isFoggyAt(int x, int y) {
        return true;
    }

    @Override
    public float[] getSunriseColor(float timeOfDay, float partialTicks) {
        return null;
    }
}
