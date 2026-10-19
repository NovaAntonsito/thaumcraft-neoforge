package thaumcraft.client.lib;

import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import thaumcraft.Thaumcraft;
import thaumcraft.client.ClientPacketHandlers;
import thaumcraft.common.items.curios.ItemThaumonomicon;
import thaumcraft.common.lib.SoundsTC;

/**
 * The Thaumonomicon opening in the hand (stage 10 of the port plan; not in Thaumcraft). Using it opens the 3D book
 * over 8 ticks and then the research browser; when the browser closes, the book closes over another 8. Putting
 * the book away stops it at once.
 */
@EventBusSubscriber(modid = Thaumcraft.MODID, value = Dist.CLIENT)
public final class ThaumonomiconAnimation {
    private enum State { IDLE, OPENING, READING, CLOSING }

    private static final float STEP = 0.125f;
    private static State state = State.IDLE;
    private static InteractionHand hand = InteractionHand.MAIN_HAND;
    private static float progress = 0.0f;
    private static float prevProgress = 0.0f;

    private ThaumonomiconAnimation() {
    }

    /** ItemThaumonomicon.use on the client. */
    public static void start(Player player, InteractionHand usedHand) {
        if (state == State.OPENING || state == State.READING) {
            return;
        }
        hand = usedHand;
        state = State.OPENING;
        player.level().playLocalSound(player.getX(), player.getY(), player.getZ(), SoundsTC.PAGE.get(), SoundSource.PLAYERS, 1.0f, 1.0f, false);
    }

    /** How far open the book in this hand is, 0 closed to 1 open. */
    public static float open(ItemStack stack, float partialTick) {
        Player player = Minecraft.getInstance().player;
        if (state == State.IDLE || player == null || player.getItemInHand(hand) != stack) {
            return 0.0f;
        }
        return Mth.lerp(partialTick, prevProgress, progress);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        prevProgress = progress;
        if (state == State.IDLE) {
            return;
        }
        if (mc.player == null || !(mc.player.getItemInHand(hand).getItem() instanceof ItemThaumonomicon)) {
            state = State.IDLE;
            progress = prevProgress = 0.0f;
            return;
        }
        switch (state) {
            case OPENING -> {
                progress = Math.min(1.0f, progress + STEP);
                if (progress >= 1.0f) {
                    state = State.READING;
                    if (mc.screen == null) {
                        ClientPacketHandlers.openResearchBrowser();
                    }
                }
            }
            case READING -> {
                if (mc.screen == null) {
                    state = State.CLOSING;
                }
            }
            case CLOSING -> {
                progress = Math.max(0.0f, progress - STEP);
                if (progress <= 0.0f) {
                    state = State.IDLE;
                }
            }
            default -> {
            }
        }
    }
}
