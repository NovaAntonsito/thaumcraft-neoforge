package thaumcraft.client.lib.events;

import java.lang.reflect.Field;
import java.util.HashMap;

import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.network.PacketDistributor;
import thaumcraft.client.fx.ClientFX;
import thaumcraft.common.lib.network.playerdata.PacketPlayerFlagToServer;

/** The client half of the worn baubles. */
public final class ClientBaubles {
    /** ItemCloudRing.jumpList: true while the air jump is still there to use, by player name. */
    public static final HashMap<String, Boolean> jumpList = new HashMap<>();
    private static Field noJumpDelay;

    private ClientBaubles() {
    }

    /** 1.12 jumpTicks: the delay between jumps, private in 1.21. */
    private static int jumpTicks(LivingEntity player) {
        try {
            if (noJumpDelay == null) {
                noJumpDelay = ObfuscationReflectionHelper.findField(LivingEntity.class, "noJumpDelay");
            }
            return noJumpDelay.getInt(player);
        } catch (Exception e) {
            return 0;
        }
    }

    /** ItemCloudRing.onWornTick: pressing jump in the air jumps again, once until landing. */
    public static void cloudRingTick(LivingEntity player) {
        Minecraft mc = Minecraft.getInstance();
        if (player != mc.player) {
            return;
        }
        String name = player.getName().getString();
        boolean spacePressed = mc.options.keyJump.consumeClick();
        if (spacePressed && !jumpList.containsKey(name)) {
            jumpList.put(name, true);
        }
        if (spacePressed && !player.onGround() && !player.isInWater() && jumpTicks(player) == 0 && jumpList.containsKey(name) && jumpList.get(name)) {
            ClientFX.drawBamf(player.level(), player.getX(), player.getY() + 0.5, player.getZ(), 0xFFFFFF, false, false);
            player.level().playLocalSound(player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.1f,
                    1.0f + (float) player.level().random.nextGaussian() * 0.05f, false);
            jumpList.put(name, false);
            Vec3 m = player.getDeltaMovement();
            double my = 0.75;
            if (player.hasEffect(MobEffects.JUMP)) {
                my += (player.getEffect(MobEffects.JUMP).getAmplifier() + 1) * 0.1f;
            }
            double mx = m.x;
            double mz = m.z;
            if (player.isSprinting()) {
                float f = player.getYRot() * 0.017453292f;
                mx -= Mth.sin(f) * 0.2f;
                mz += Mth.cos(f) * 0.2f;
            }
            player.setDeltaMovement(mx, my, mz);
            player.fallDistance = 0.0f;
            PacketDistributor.sendToServer(new PacketPlayerFlagToServer((byte) 1));
            CommonHooks.onLivingJump(player);
        }
        if (player.onGround() && jumpTicks(player) == 0) {
            jumpList.remove(name);
        }
    }
}
