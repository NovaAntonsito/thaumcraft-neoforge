package thaumcraft.api.casters;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import thaumcraft.api.aspects.Aspect;

public class FocusMediumRoot extends FocusMedium {

    public FocusMediumRoot() {
        super();
    }

    Trajectory[] trajectories = null;
    HitResult[] targets = null;

    public FocusMediumRoot(Trajectory[] trajectories, HitResult[] targets) {
        super();
        this.trajectories = trajectories;
        this.targets = targets;
    }

    @Override
    public String getResearch() {
        return "BASEAUROMANCY";
    }

    @Override
    public String getKey() {
        return "ROOT";
    }

    @Override
    public int getComplexity() {
        return 0;
    }

    @Override
    public EnumSupplyType[] willSupply() {
        return new EnumSupplyType[] {EnumSupplyType.TARGET, EnumSupplyType.TRAJECTORY};
    }

    @Override
    public HitResult[] supplyTargets() {
        return targets;
    }

    @Override
    public Trajectory[] supplyTrajectories() {
        return trajectories;
    }

    public void setupFromCaster(LivingEntity caster) {
        trajectories = new Trajectory[] {new Trajectory(generateSourceVector(caster), caster.getLookAngle())};
        targets = new HitResult[] {new EntityHitResult(caster)};
    }

    /**
     * Useful if you want to cast stuff from something other than the player and where their getLookVec() is often not accurate.
     * @param offset use to aim above or below the target
     */
    public void setupFromCasterToTarget(LivingEntity caster, Entity target, double offset) {
        Vec3 sv = generateSourceVector(caster);
        double d0 = target.getX() - sv.x;
        double d1 = target.getBoundingBox().minY + (double) (target.getBbHeight() / 2.0F) - sv.y;
        double d2 = target.getZ() - sv.z;
        Vec3 lv = new Vec3(d0, d1 + offset, d2);
        trajectories = new Trajectory[] {new Trajectory(sv, lv.normalize())};
        targets = new HitResult[] {new EntityHitResult(caster)};
    }

    /**
     * Useful if you want to cast stuff from something other than the player and where their getLookVec() is often not accurate.
     */
    public void setupFromCasterToTargetLoc(LivingEntity caster, double x, double y, double z) {
        Vec3 sv = generateSourceVector(caster);
        Vec3 lv = new Vec3(x - sv.x, y - sv.y, z - sv.z);
        trajectories = new Trajectory[] {new Trajectory(sv, lv.normalize())};
        targets = new HitResult[] {new EntityHitResult(caster)};
    }

    private Vec3 generateSourceVector(LivingEntity e) {
        return e.position().add(0, e.getEyeHeight() - 0.10000000149011612D, 0);
    }

    @Override
    public Aspect getAspect() {
        return null;
    }
}
