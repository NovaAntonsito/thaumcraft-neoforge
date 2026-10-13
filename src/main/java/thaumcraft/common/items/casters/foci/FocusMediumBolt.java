package thaumcraft.common.items.casters.foci;

import java.awt.Color;

import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.casters.FocusEffect;
import thaumcraft.api.casters.FocusEngine;
import thaumcraft.api.casters.Trajectory;
import thaumcraft.common.lib.network.fx.PacketFXZap;
import thaumcraft.common.lib.utils.EntityUtils;
import thaumcraft.common.network.TCNetwork;

public class FocusMediumBolt extends FocusMediumTouch {
    @Override
    public String getResearch() {
        return "FOCUSBOLT";
    }

    @Override
    public String getKey() {
        return "thaumcraft.BOLT";
    }

    @Override
    public int getComplexity() {
        return 5;
    }

    @Override
    public Aspect getAspect() {
        return Aspect.ENERGY;
    }

    @Override
    public boolean execute(Trajectory trajectory) {
        float range = 16.0f;
        Vec3 end = trajectory.direction.normalize();
        HitResult ray = EntityUtils.getPointedEntityRay(getPackage().world, getPackage().getCaster(), trajectory.source, end, 0.25, range, 0.25f, false);
        if (ray == null) {
            end = end.scale(range);
            end = end.add(trajectory.source);
            ray = rayTraceBlocks(getPackage().world, trajectory.source, end, getPackage().getCaster());
            if (ray != null) {
                end = ray.getLocation();
            }
        } else if (ray instanceof EntityHitResult ehr) {
            end = end.scale(trajectory.source.distanceTo(ehr.getEntity().position()));
            end = end.add(trajectory.source);
        }
        int r = 0;
        int g = 0;
        int b = 0;
        FocusEffect[] effects = getPackage().getFocusEffects();
        for (FocusEffect ef : effects) {
            Color c = new Color(FocusEngine.getElementColor(ef.getKey()));
            r += c.getRed();
            g += c.getGreen();
            b += c.getBlue();
        }
        if (effects.length > 0) {
            r /= effects.length;
            g /= effects.length;
            b /= effects.length;
        }
        Color c2 = new Color(r, g, b);
        TCNetwork.sendToAllAround(getPackage().world, trajectory.source.x, trajectory.source.y, trajectory.source.z, 64.0,
                new PacketFXZap(trajectory.source, end, c2.getRGB(), getPackage().getPower() * 0.66f));
        return true;
    }
}
