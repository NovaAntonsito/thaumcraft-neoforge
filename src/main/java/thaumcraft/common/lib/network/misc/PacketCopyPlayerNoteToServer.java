package thaumcraft.common.lib.network.misc;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import io.netty.buffer.ByteBuf;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.capabilities.ThaumcraftCapabilities;
import thaumcraft.common.items.consumables.ItemResearchNote;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.lib.research.ResearchNoteData;
import thaumcraft.common.lib.research.ResearchNoteManager;
import thaumcraft.common.lib.utils.InventoryUtils;
import thaumcraft.common.tiles.crafting.TileResearchTable;

/**
 * Reborn's PacketCopyPlayerNoteToServer (Research Duplication): copies the solved note on the table.
 * Costs paper, ink, and copies+1 points of each of its aspects; each copy makes the next one dearer.
 */
public record PacketCopyPlayerNoteToServer(BlockPos pos) implements CustomPacketPayload {
    public static final Type<PacketCopyPlayerNoteToServer> TYPE = new Type<>(Thaumcraft.id("copy_note"));
    public static final StreamCodec<ByteBuf, PacketCopyPlayerNoteToServer> STREAM_CODEC =
            BlockPos.STREAM_CODEC.map(PacketCopyPlayerNoteToServer::new, PacketCopyPlayerNoteToServer::pos);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketCopyPlayerNoteToServer msg, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) return;
        if (player.distanceToSqr(msg.pos.getCenter()) > 64.0) return;
        if (!(player.level().getBlockEntity(msg.pos) instanceof TileResearchTable te)) return;
        if (!ThaumcraftCapabilities.getKnowledge(player).isResearchComplete("RESEARCHDUPE")) return;
        ItemStack tools = te.getItem(0);
        ItemStack note = te.getItem(1);
        ResearchNoteData data = ItemResearchNote.getData(note);
        if (data == null || !data.isComplete()) return;
        boolean failed = false;
        if (tools.isEmpty()) {
            player.sendSystemMessage(Component.translatable("researchnote.missing.tools").withStyle(ChatFormatting.RED));
            failed = true;
        } else if (!te.canConsumeInkFromTable()) {
            player.sendSystemMessage(Component.translatable("tile.researchtable.noink.0").withStyle(ChatFormatting.RED));
            player.sendSystemMessage(Component.translatable("tile.researchtable.noink.1").withStyle(ChatFormatting.RED));
            failed = true;
        }
        if (!InventoryUtils.isPlayerCarryingAmount(player, new ItemStack(Items.PAPER))) {
            player.sendSystemMessage(Component.translatable("researchnote.missing.paper").withStyle(ChatFormatting.RED));
            failed = true;
        }
        int cost = data.copies + 1;
        for (Aspect aspect : data.aspects.getAspects()) {
            if (ThaumcraftCapabilities.getAspects(player).aspectCount(aspect) < cost && te.bonusAspects.getAmount(aspect) < cost) {
                player.sendSystemMessage(Component.translatable("tc.research.copy.failure", aspect.getDisplayName()).withStyle(ChatFormatting.RED));
                failed = true;
            }
        }
        if (failed) return;
        te.consumeInkFromTable();
        InventoryUtils.consumePlayerItem(player, new ItemStack(Items.PAPER), true);
        for (Aspect aspect : data.aspects.getAspects()) {
            if (ThaumcraftCapabilities.getAspects(player).aspectCount(aspect) >= cost) {
                ResearchNoteManager.spendAspect(player, aspect, cost);
            } else {
                te.bonusAspects.remove(aspect, cost);
            }
        }
        data.copies += 1;
        ItemResearchNote.setData(note, data);
        te.setChanged();
        te.syncTile(false);
        ItemStack copy = note.copy();
        if (!player.getInventory().add(copy)) player.drop(copy, false);
        player.inventoryMenu.broadcastChanges();
        player.level().playSound(null, msg.pos, SoundsTC.WRITE.get(), SoundSource.PLAYERS, 0.75f, 1.0f);
    }
}
