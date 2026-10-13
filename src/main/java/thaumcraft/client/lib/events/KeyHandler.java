package thaumcraft.client.lib.events;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import thaumcraft.Thaumcraft;
import thaumcraft.api.casters.ICaster;
import thaumcraft.client.gui.GuiFocusRadial;
import thaumcraft.common.lib.network.misc.PacketFocusChangeToServer;
import thaumcraft.common.lib.network.misc.PacketItemKeyToServer;
import net.minecraft.client.gui.screens.Screen;

/**
 * Port of KeyHandler. F (change caster focus): holding it with a caster opens the focus radial; sneak + F takes the
 * focus off. G (misc caster toggle): grows the plan focus area; ctrl + G changes its axis.
 * 1.21: the radial is a screen that does not pause the game (it frees the mouse the way the original ungrabbed it).
 * TODO (stage 8): F on the golem bell.
 */
@EventBusSubscriber(modid = Thaumcraft.MODID, value = Dist.CLIENT)
public final class KeyHandler {
    private KeyHandler() {
    }

    public static final KeyMapping keyF = new KeyMapping("key.thaumcraft.change_focus", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_F,
            "key.categories.thaumcraft");

    /** "Misc Caster Toggle" in the original, on G. */
    public static final KeyMapping keyG = new KeyMapping("key.thaumcraft.misc_toggle", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G,
            "key.categories.thaumcraft");

    private static boolean keyPressedF = false;
    private static boolean keyPressedG = false;
    public static long lastPressG = 0L;
    public static boolean radialLock = false;
    public static long lastPressF = 0L;

    @SubscribeEvent
    public static void playerTick(ClientTickEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) {
            return;
        }
        if (keyF.isDown()) {
            if (mc.screen == null) {
                if (!keyPressedF) {
                    lastPressF = System.currentTimeMillis();
                    radialLock = false;
                }
                if (!radialLock && (player.getMainHandItem().getItem() instanceof ICaster || player.getOffhandItem().getItem() instanceof ICaster)) {
                    if (player.isShiftKeyDown()) {
                        PacketDistributor.sendToServer(new PacketFocusChangeToServer("REMOVE"));
                        radialLock = true;
                    } else if (GuiFocusRadial.hasFoci(player)) {
                        mc.setScreen(new GuiFocusRadial());
                        radialLock = true;
                    }
                }
                keyPressedF = true;
            }
        } else {
            if (keyPressedF) {
                lastPressF = System.currentTimeMillis();
            }
            keyPressedF = false;
        }
        if (keyG.isDown()) {
            if (mc.screen == null) {
                if (!keyPressedG) {
                    lastPressG = System.currentTimeMillis();
                    PacketDistributor.sendToServer(new PacketItemKeyToServer(1, Screen.hasControlDown() ? 1 : (Screen.hasShiftDown() ? 2 : 0)));
                }
                keyPressedG = true;
            }
        } else {
            if (keyPressedG) {
                lastPressG = System.currentTimeMillis();
            }
            keyPressedG = false;
        }
    }
}
