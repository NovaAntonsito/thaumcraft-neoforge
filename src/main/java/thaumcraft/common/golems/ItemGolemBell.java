package thaumcraft.common.golems;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import thaumcraft.api.golems.ISealDisplayer;
import thaumcraft.api.golems.seals.ISealEntity;
import thaumcraft.api.golems.seals.SealPos;
import thaumcraft.common.golems.client.gui.SealBaseContainer;
import thaumcraft.common.golems.seals.SealHandler;
import thaumcraft.common.lib.SoundsTC;

/**
 * Port of ItemGolemBell. On a golem it toggles follow/stay (handled by the golem). Clicking a seal opens its settings,
 * sneak-clicking removes it.
 * TODO (stage 8c): sneak-clicking with GOLEMLOGISTICS opens the logistics screen.
 */
public class ItemGolemBell extends Item implements ISealDisplayer {
    public ItemGolemBell(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand hand) {
        playerIn.swing(hand);
        if (!worldIn.isClientSide) {
            HitResult mop = playerIn.pick(playerIn.blockInteractionRange(), 0.0f, false);
            if (mop.getType() == HitResult.Type.BLOCK) {
                ISealEntity se = getSeal(playerIn);
                if (se != null && playerIn.isShiftKeyDown()) {
                    SealHandler.removeSealEntity(playerIn.level(), se.getSealPos(), false);
                    worldIn.playSound(null, se.getSealPos().pos, SoundsTC.ZAP.get(), SoundSource.BLOCKS, 0.5f, 1.0f);
                } else if (se != null && playerIn instanceof ServerPlayer sp) {
                    SealBaseContainer.open(sp, se);
                }
                return InteractionResultHolder.fail(playerIn.getItemInHand(hand));
            }
        } else {
            playerIn.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.6f, 1.0f + worldIn.random.nextFloat() * 0.1f);
        }
        return super.use(worldIn, playerIn, hand);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Player player = context.getPlayer();
        Level world = context.getLevel();
        if (player == null) {
            return InteractionResult.PASS;
        }
        player.swing(context.getHand());
        if (!world.isClientSide) {
            ISealEntity se = SealHandler.getSealEntity(world.dimension(), new SealPos(context.getClickedPos(), context.getClickedFace()));
            if (se != null) {
                if (player.isShiftKeyDown()) {
                    SealHandler.removeSealEntity(world, se.getSealPos(), false);
                    world.playSound(null, context.getClickedPos(), SoundsTC.ZAP.get(), SoundSource.BLOCKS, 0.5f, 1.0f);
                } else if (player instanceof ServerPlayer sp) {
                    SealBaseContainer.open(sp, se);
                }
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    /** The seal the player looks at within 5 blocks, stepping along the look vector a tenth at a time. */
    public static ISealEntity getSeal(Player playerIn) {
        float f = playerIn.getXRot();
        float f2 = playerIn.getYRot();
        Vec3 vec0 = new Vec3(playerIn.getX(), playerIn.getEyeY(), playerIn.getZ());
        float f3 = Mth.cos(-f2 * Mth.DEG_TO_RAD - (float) Math.PI);
        float f4 = Mth.sin(-f2 * Mth.DEG_TO_RAD - (float) Math.PI);
        float f5 = -Mth.cos(-f * Mth.DEG_TO_RAD);
        float f6 = Mth.sin(-f * Mth.DEG_TO_RAD);
        float f7 = f4 * f5;
        float f8 = f3 * f5;
        double d4 = 5.0;
        Vec3 vec2 = vec0.add(f7 * d4, f6 * d4, f8 * d4);
        Vec3 vec3 = new Vec3(f7 * d4, f6 * d4, f8 * d4);
        Vec3 vec4 = vec0.add(vec3.x / 10.0, vec3.y / 10.0, vec3.z / 10.0);
        for (int a = 0; a < vec3.length() * 10.0; ++a) {
            BlockPos pos = BlockPos.containing(vec4);
            Direction side = collisionRayTrace(pos, vec0, vec2);
            if (side != null) {
                ISealEntity se = SealHandler.getSealEntity(playerIn.level().dimension(), new SealPos(pos, side));
                if (se != null) {
                    return se;
                }
            }
            vec4 = vec4.add(vec3.x / 10.0, vec3.y / 10.0, vec3.z / 10.0);
        }
        return null;
    }

    /** The face of the unit cube at pos the segment enters first, or null if it misses. */
    private static Direction collisionRayTrace(BlockPos pos, Vec3 start, Vec3 end) {
        return net.minecraft.world.phys.AABB.unitCubeFromLowerCorner(Vec3.atLowerCornerOf(pos)).clip(start, end).map(hit -> {
            double x = hit.x - pos.getX();
            double y = hit.y - pos.getY();
            double z = hit.z - pos.getZ();
            if (x <= 1.0E-7) return Direction.WEST;
            if (x >= 1.0 - 1.0E-7) return Direction.EAST;
            if (y <= 1.0E-7) return Direction.DOWN;
            if (y >= 1.0 - 1.0E-7) return Direction.UP;
            if (z <= 1.0E-7) return Direction.NORTH;
            return Direction.SOUTH;
        }).orElse(null);
    }
}
