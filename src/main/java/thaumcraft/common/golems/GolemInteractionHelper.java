package thaumcraft.common.golems;

import java.util.UUID;

import com.mojang.authlib.GameProfile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import thaumcraft.api.golems.IGolemAPI;
import thaumcraft.common.lib.utils.InventoryUtils;

/** Port of GolemInteractionHelper: a golem clicking a block through a fake player. */
public class GolemInteractionHelper {
    /** The "FakeThaumcraftGolem" profile. 1.21 needs an id, so it is derived from the name. */
    public static final GameProfile GOLEM_PROFILE = new GameProfile(UUID.nameUUIDFromBytes("FakeThaumcraftGolem".getBytes()), "FakeThaumcraftGolem");

    /**
     * The golem's fake player, standing where the golem is. 1.12 also gave it a FakeNetHandlerPlayServer; NeoForge
     * fake players already have a connection that drops the packets.
     */
    public static FakePlayer getFakePlayer(Level world, IGolemAPI golem) {
        FakePlayer fp = FakePlayerFactory.get((ServerLevel) world, GOLEM_PROFILE);
        LivingEntity e = golem.getGolemEntity();
        fp.moveTo(e.getX(), e.getY(), e.getZ(), e.getYRot(), e.getXRot());
        return fp;
    }

    public static void golemClick(Level world, IGolemAPI golem, BlockPos pos, Direction face, ItemStack clickStack, boolean sneaking, boolean rightClick) {
        FakePlayer fp = getFakePlayer(world, golem);
        fp.setItemInHand(InteractionHand.MAIN_HAND, clickStack);
        fp.setShiftKeyDown(sneaking);
        if (!rightClick) {
            try {
                fp.gameMode.handleBlockBreakAction(pos, ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, face, world.getMaxBuildHeight(), 0);
            } catch (Exception ex) {
            }
        } else {
            if (fp.getMainHandItem().getItem() instanceof BlockItem bi && !mayPlace(world, bi.getBlock(), pos, face)) {
                LivingEntity e = golem.getGolemEntity();
                e.setPos(e.getX() + face.getStepX(), e.getY() + face.getStepY(), e.getZ() + face.getStepZ());
            }
            try {
                fp.gameMode.useItemOn(fp, world, fp.getMainHandItem(), InteractionHand.MAIN_HAND,
                        new BlockHitResult(Vec3.atCenterOf(pos), face, pos, false));
            } catch (Exception ex2) {
            }
        }
        golem.addRankXp(1);
        if (!fp.getMainHandItem().isEmpty() && fp.getMainHandItem().getCount() <= 0) {
            fp.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        }
        dropSomeItems(fp, golem);
        fp.setShiftKeyDown(false);
        golem.swingArm();
    }

    private static boolean mayPlace(Level world, Block blockIn, BlockPos pos, Direction side) {
        return world.isUnobstructed(blockIn.defaultBlockState(), pos, CollisionContext.empty());
    }

    /** Whatever the fake player ended up holding goes to the golem, or is dropped at it. */
    private static void dropSomeItems(FakePlayer fp2, IGolemAPI golem) {
        var main = fp2.getInventory().items;
        var armor = fp2.getInventory().armor;
        for (int i = 0; i < main.size(); ++i) {
            if (!main.get(i).isEmpty()) {
                if (golem.canCarry(main.get(i), true)) {
                    main.set(i, golem.holdItem(main.get(i)));
                }
                if (!main.get(i).isEmpty() && main.get(i).getCount() > 0) {
                    InventoryUtils.dropItemAtEntity(golem.getGolemWorld(), main.get(i), golem.getGolemEntity());
                }
                main.set(i, ItemStack.EMPTY);
            }
        }
        for (int i = 0; i < armor.size(); ++i) {
            if (!armor.get(i).isEmpty()) {
                if (golem.canCarry(armor.get(i), true)) {
                    armor.set(i, golem.holdItem(armor.get(i)));
                }
                // kept from 1.12: this checks the main inventory slot instead of the armor slot
                if (!main.get(i).isEmpty() && armor.get(i).getCount() > 0) {
                    InventoryUtils.dropItemAtEntity(golem.getGolemWorld(), armor.get(i), golem.getGolemEntity());
                }
                armor.set(i, ItemStack.EMPTY);
            }
        }
    }
}
