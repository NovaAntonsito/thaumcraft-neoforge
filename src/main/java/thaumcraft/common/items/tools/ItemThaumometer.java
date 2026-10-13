package thaumcraft.common.items.tools;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.network.PacketDistributor;
import thaumcraft.Config;
import thaumcraft.api.capabilities.ThaumcraftCapabilities;
import thaumcraft.api.research.ScanningManager;
import thaumcraft.client.fx.ClientFX;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.lib.network.misc.PacketAuraToClient;
import thaumcraft.common.lib.research.ResearchManager;
import thaumcraft.common.lib.research.ScanManager;
import thaumcraft.common.lib.utils.EntityUtils;
import thaumcraft.common.world.aura.AuraChunk;
import thaumcraft.common.world.aura.AuraHandler;

/**
 * The thaumometer. Scans what the player looks at (a creature first, then a block) and, held, shows the aura of
 * the chunk. With TC4 Research Port: Reborn the scan takes a second of holding the button (instantScans restores
 * the TC6 click), and a thing whose aspects the player cannot understand yet is not scanned.
 */
public class ItemThaumometer extends Item {

    public ItemThaumometer(Item.Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.UNCOMMON));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player p, InteractionHand hand) {
        ItemStack stack = p.getItemInHand(hand);
        // Reborn's hookThaumometerRightClick: hold to scan.
        if (!Config.INSTANT_SCANS.get()) {
            p.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        }
        if (world.isClientSide) {
            ClientFX.thaumometerScan(p);
            world.playLocalSound(p.getX(), p.getY(), p.getZ(), SoundsTC.SCAN.get(), SoundSource.PLAYERS, 0.5f, 1.0f, false);
        } else {
            doScan(world, p);
        }
        return InteractionResultHolder.success(stack);
    }

    /**
     * Runs before the block's own use, so blocks with a screen (chests, tables) are scanned instead of opened.
     */
    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Player p = context.getPlayer();
        if (p == null) return InteractionResult.PASS;
        return use(context.getLevel(), p, context.getHand()).getResult();
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 20;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }

    /** Reborn's hookThaumometerUsingTick: runes every 5 ticks, the ticking sound, and the scan on the last tick. */
    @Override
    public void onUseTick(Level world, LivingEntity entity, ItemStack stack, int count) {
        if (!(entity instanceof Player player) || Config.INSTANT_SCANS.get()) return;
        if (world.isClientSide) {
            if (count % 5 == 0) ClientFX.thaumometerScan(player);
        } else {
            if (count <= 1) {
                world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundsTC.SCAN.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
                doScan(world, player);
            }
            if (count % 2 == 0) {
                world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundsTC.TICKS.get(), SoundSource.PLAYERS,
                        0.2f, 0.45f + world.random.nextFloat() * 0.1f);
            }
        }
    }

    /** Held in hand (or in the first slot): the aura every second, and the scannable highlights on the client. */
    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int itemSlot, boolean isSelected) {
        boolean held = isSelected || itemSlot == 0;
        if (held && !world.isClientSide && entity.tickCount % 20 == 0 && entity instanceof ServerPlayer player) {
            updateAura(world, player);
        }
        if (held && world.isClientSide && entity.tickCount % 5 == 0 && entity instanceof Player player) {
            ClientFX.thaumometerHeld(player);
        }
    }

    private void updateAura(Level world, ServerPlayer player) {
        AuraChunk ac = AuraHandler.getAuraChunk(world.dimension(), player.blockPosition().getX() >> 4, player.blockPosition().getZ() >> 4);
        if (ac != null) {
            if ((ac.getFlux() > ac.getVis() || ac.getFlux() > ac.getBase() / 3) && !ThaumcraftCapabilities.knowsResearch(player, "FLUX")) {
                ResearchManager.startResearchWithPopup(player, "FLUX");
                player.displayClientMessage(Component.translatable("research.FLUX.warn").withStyle(ChatFormatting.DARK_PURPLE), true);
            }
            PacketDistributor.sendToPlayer(player, new PacketAuraToClient(ac));
        }
    }

    /**
     * A creature in sight first, then the block looked at, else nothing. Reborn checks canScanThing before each:
     * a creature that cannot be understood falls through to the block, a block to the empty scan.
     */
    public void doScan(Level world, Player player) {
        if (world.isClientSide) return;
        Entity target = EntityUtils.getPointedEntity(world, player, 1.0, 9.0, 0.0f, true);
        if (target != null && ScanManager.canScanThing(player, target, true)) {
            ScanningManager.scanTheThing(player, target);
            return;
        }
        BlockHitResult mop = rayTrace(world, player);
        if (mop.getType() == HitResult.Type.BLOCK && ScanManager.canScanThing(player, mop.getBlockPos(), true)) {
            ScanningManager.scanTheThing(player, mop.getBlockPos());
        } else {
            ScanningManager.scanTheThing(player, null);
        }
    }

    /** Item.rayTrace with liquids: the block or fluid within reach. */
    public static BlockHitResult rayTrace(Level world, Player player) {
        return getPlayerPOVHitResult(world, player, ClipContext.Fluid.ANY);
    }
}
