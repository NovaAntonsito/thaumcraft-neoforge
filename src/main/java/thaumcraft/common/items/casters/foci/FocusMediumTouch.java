package thaumcraft.common.items.casters.foci;

import java.util.ArrayList;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.casters.FocusEffect;
import thaumcraft.api.casters.FocusMedium;
import thaumcraft.api.casters.Trajectory;
import thaumcraft.common.lib.network.fx.PacketFXFocusEffect;
import thaumcraft.common.lib.utils.EntityUtils;
import thaumcraft.common.network.TCNetwork;

public class FocusMediumTouch extends FocusMedium {
    @Override
    public String getResearch() {
        return "BASEAUROMANCY";
    }

    @Override
    public String getKey() {
        return "thaumcraft.TOUCH";
    }

    @Override
    public int getComplexity() {
        return 2;
    }

    @Override
    public EnumSupplyType[] willSupply() {
        return new EnumSupplyType[] {EnumSupplyType.TRAJECTORY, EnumSupplyType.TARGET};
    }

    @Override
    public Aspect getAspect() {
        return Aspect.AVERSION;
    }

    /** 1.12 used the player's block reach; 1.21 has it as an attribute. */
    double getRange() {
        if (this instanceof FocusMediumBolt) {
            return 16.0;
        }
        return getPackage().getCaster() instanceof Player p ? p.blockInteractionRange() : 4.5;
    }

    /** rayTraceBlocks returned null on a miss; clip returns a MISS result instead. */
    static HitResult rayTraceBlocks(net.minecraft.world.level.Level world, Vec3 start, Vec3 end, net.minecraft.world.entity.Entity caster) {
        HitResult r = world.clip(new ClipContext(start, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, caster));
        return r.getType() == HitResult.Type.MISS ? null : r;
    }

    @Override
    public Trajectory[] supplyTrajectories() {
        if (getParent() == null) {
            return new Trajectory[0];
        }
        ArrayList<Trajectory> trajectories = new ArrayList<>();
        double range = getRange();
        for (Trajectory sT : getParent().supplyTrajectories()) {
            Vec3 end = sT.direction.normalize();
            HitResult ray = EntityUtils.getPointedEntityRay(getPackage().world, getPackage().getCaster(), sT.source, end, 0.25, range, 0.25f, false);
            if (ray == null) {
                end = end.scale(range);
                end = end.add(sT.source);
                ray = rayTraceBlocks(getPackage().world, sT.source, end, getPackage().getCaster());
                if (ray != null) {
                    end = ray.getLocation();
                }
            } else if (ray instanceof EntityHitResult ehr) {
                end = end.scale(sT.source.distanceTo(ehr.getEntity().position()));
                end = end.add(sT.source);
            }
            trajectories.add(new Trajectory(end, sT.direction.normalize()));
        }
        return trajectories.toArray(new Trajectory[0]);
    }

    @Override
    public HitResult[] supplyTargets() {
        if (getParent() == null || !(getPackage().getCaster() instanceof Player)) {
            return new HitResult[0];
        }
        ArrayList<HitResult> targets = new ArrayList<>();
        double range = getRange();
        for (Trajectory sT : getParent().supplyTrajectories()) {
            Vec3 end = sT.direction.normalize();
            HitResult ray = EntityUtils.getPointedEntityRay(getPackage().world, getPackage().getCaster(), sT.source, end, 0.25, range, 0.25f, false);
            if (ray == null) {
                end = end.scale(range);
                end = end.add(sT.source);
                ray = rayTraceBlocks(getPackage().world, sT.source, end, getPackage().getCaster());
            }
            if (ray != null) {
                targets.add(ray);
            }
        }
        return targets.toArray(new HitResult[0]);
    }

    @Override
    public boolean execute(Trajectory trajectory) {
        FocusEffect[] fe = getPackage().getFocusEffects();
        if (fe != null && fe.length > 0) {
            String[] effects = new String[fe.length];
            for (int a = 0; a < fe.length; ++a) {
                effects[a] = fe[a].getKey();
            }
            TCNetwork.sendToAllAround(getPackage().world, trajectory.source.x, trajectory.source.y, trajectory.source.z, 64.0,
                    new PacketFXFocusEffect((float) trajectory.source.x, (float) trajectory.source.y, (float) trajectory.source.z,
                            (float) trajectory.direction.x / 2.0f, (float) trajectory.direction.y / 2.0f, (float) trajectory.direction.z / 2.0f, effects));
        }
        return true;
    }
}
