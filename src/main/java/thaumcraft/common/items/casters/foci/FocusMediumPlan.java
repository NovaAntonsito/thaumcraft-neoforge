package thaumcraft.common.items.casters.foci;

import java.util.ArrayList;
import java.util.stream.Collectors;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.casters.FocusMedium;
import thaumcraft.api.casters.ICaster;
import thaumcraft.api.casters.NodeSetting;
import thaumcraft.api.casters.Trajectory;
import thaumcraft.api.items.IArchitect;
import thaumcraft.common.items.casters.CasterManager;
import thaumcraft.common.lib.utils.BlockUtils;

/**
 * Plan medium: targets every block of an area around the block you look at (the whole volume, or only the exposed
 * surface of the same block). The area size is set on the caster with the G key.
 * 1.21: a missed ray is a MISS result instead of null.
 */
public class FocusMediumPlan extends FocusMedium implements IArchitect {
    ArrayList<BlockPos> checked = new ArrayList<>();

    @Override
    public String getResearch() {
        return "FOCUSPLAN";
    }

    @Override
    public String getKey() {
        return "thaumcraft.PLAN";
    }

    @Override
    public int getComplexity() {
        return 4;
    }

    @Override
    public Aspect getAspect() {
        return Aspect.CRAFT;
    }

    @Override
    public NodeSetting[] createSettings() {
        int[] method = {0, 1};
        String[] methodDesc = {"focus.plan.full", "focus.plan.surface"};
        return new NodeSetting[] {new NodeSetting("method", "focus.plan.method", new NodeSetting.NodeSettingIntList(method, methodDesc))};
    }

    @Override
    public HitResult[] supplyTargets() {
        if (getParent() == null || !(getPackage().getCaster() instanceof Player player)) {
            return new HitResult[0];
        }
        ArrayList<HitResult> targets = new ArrayList<>();
        ItemStack casterStack = ItemStack.EMPTY;
        if (player.getMainHandItem().getItem() instanceof ICaster) {
            casterStack = player.getMainHandItem();
        } else if (player.getOffhandItem().getItem() instanceof ICaster) {
            casterStack = player.getOffhandItem();
        }
        Level world = getPackage().world;
        for (Trajectory sT : getParent().supplyTrajectories()) {
            Vec3 end = sT.direction.normalize().scale(16.0).add(sT.source);
            BlockHitResult target = world.clip(new ClipContext(sT.source, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
            if (target.getType() == HitResult.Type.BLOCK) {
                ArrayList<BlockPos> usl = getArchitectBlocks(casterStack, world, target.getBlockPos(), target.getDirection(), player);
                ArrayList<BlockPos> sl = usl.stream().sorted(new BlockUtils.BlockPosComparator(target.getBlockPos()))
                        .collect(Collectors.toCollection(ArrayList::new));
                for (BlockPos p : sl) {
                    targets.add(new BlockHitResult(new Vec3(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5), target.getDirection(), p, false));
                }
            }
        }
        return targets.toArray(new HitResult[0]);
    }

    @Override
    public HitResult getArchitectMOP(ItemStack stack, Level world, LivingEntity player) {
        Vec3 start = player.position().add(0.0, player.getEyeHeight(), 0.0);
        Vec3 end = player.getLookAngle().scale(16.0).add(start);
        return world.clip(new ClipContext(start, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
    }

    @Override
    public boolean useBlockHighlight(ItemStack stack) {
        return false;
    }

    @Override
    public boolean isExclusive() {
        return true;
    }

    @Override
    public boolean showAxis(ItemStack stack, Level world, Player player, Direction side, EnumAxis axis) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        int dim = CasterManager.getAreaDim(stack);
        if (getSettingValue("method") == 0) {
            switch (axis) {
                case Y -> {
                    if (dim == 0 || dim == 3) {
                        return true;
                    }
                }
                case Z -> {
                    if (dim == 0 || dim == 2) {
                        return true;
                    }
                }
                case X -> {
                    if (dim == 0 || dim == 1) {
                        return true;
                    }
                }
            }
        } else {
            switch (side.getAxis()) {
                case Y -> {
                    if ((axis == EnumAxis.X && (dim == 0 || dim == 1)) || (axis == EnumAxis.Z && (dim == 0 || dim == 2))) {
                        return true;
                    }
                }
                case Z -> {
                    if ((axis == EnumAxis.Y && (dim == 0 || dim == 1)) || (axis == EnumAxis.X && (dim == 0 || dim == 2))) {
                        return true;
                    }
                }
                case X -> {
                    if ((axis == EnumAxis.Y && (dim == 0 || dim == 1)) || (axis == EnumAxis.Z && (dim == 0 || dim == 2))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public ArrayList<BlockPos> getArchitectBlocks(ItemStack stack, Level world, BlockPos pos, Direction side, Player player) {
        ArrayList<BlockPos> out = new ArrayList<>();
        if (stack == null || stack.isEmpty()) {
            return out;
        }
        if (getSettingValue("method") == 0) {
            checked.clear();
            checkNeighboursFull(world, pos, pos, side, CasterManager.getAreaX(stack), CasterManager.getAreaY(stack), CasterManager.getAreaZ(stack), out, player);
        } else {
            BlockState bi = world.getBlockState(pos);
            checked.clear();
            if (side.getAxis() == Direction.Axis.Z) {
                checkNeighboursSurface(world, pos, bi, pos, side, CasterManager.getAreaZ(stack), CasterManager.getAreaY(stack), CasterManager.getAreaX(stack), out, player);
            } else {
                checkNeighboursSurface(world, pos, bi, pos, side, CasterManager.getAreaX(stack), CasterManager.getAreaY(stack), CasterManager.getAreaZ(stack), out, player);
            }
        }
        return out;
    }

    public void checkNeighboursFull(Level world, BlockPos pos1, BlockPos pos2, Direction side, int sizeX, int sizeY, int sizeZ, ArrayList<BlockPos> list, Player player) {
        if (checked.contains(pos2)) {
            return;
        }
        checked.add(pos2);
        if (!world.isEmptyBlock(pos2)) {
            list.add(pos2);
        }
        int xs = -sizeX + pos1.getX();
        int xe = sizeX + pos1.getX();
        int ys = -sizeY + pos1.getY();
        int ye = sizeY + pos1.getY();
        int zs = -sizeZ + pos1.getZ();
        int ze = sizeZ + pos1.getZ();
        xs -= sizeX * side.getStepX();
        xe -= sizeX * side.getStepX();
        ys -= sizeY * side.getStepY();
        ye -= sizeY * side.getStepY();
        zs -= sizeZ * side.getStepZ();
        ze -= sizeZ * side.getStepZ();
        for (Direction dir : Direction.values()) {
            BlockPos q = pos2.relative(dir);
            if (q.getX() >= xs && q.getX() <= xe && q.getY() >= ys && q.getY() <= ye && q.getZ() >= zs && q.getZ() <= ze) {
                checkNeighboursFull(world, pos1, q, side, sizeX, sizeY, sizeZ, list, player);
            }
        }
    }

    public void checkNeighboursSurface(Level world, BlockPos pos1, BlockState bi, BlockPos pos2, Direction side, int sizeX, int sizeY, int sizeZ, ArrayList<BlockPos> list,
                                       Player player) {
        if (checked.contains(pos2)) {
            return;
        }
        checked.add(pos2);
        switch (side.getAxis()) {
            case Y -> {
                if (Math.abs(pos2.getX() - pos1.getX()) > sizeX) {
                    return;
                }
                if (Math.abs(pos2.getZ() - pos1.getZ()) > sizeZ) {
                    return;
                }
            }
            case Z -> {
                if (Math.abs(pos2.getX() - pos1.getX()) > sizeX) {
                    return;
                }
                if (Math.abs(pos2.getY() - pos1.getY()) > sizeZ) {
                    return;
                }
            }
            case X -> {
                if (Math.abs(pos2.getY() - pos1.getY()) > sizeX) {
                    return;
                }
                if (Math.abs(pos2.getZ() - pos1.getZ()) > sizeZ) {
                    return;
                }
            }
        }
        if (world.getBlockState(pos2) == bi && BlockUtils.isBlockExposed(world, pos2) && !world.isEmptyBlock(pos2)) {
            list.add(pos2);
            for (Direction dir : Direction.values()) {
                if (dir != side && dir.getOpposite() != side) {
                    checkNeighboursSurface(world, pos1, bi, pos2.relative(dir), side, sizeX, sizeY, sizeZ, list, player);
                }
            }
        }
    }
}
