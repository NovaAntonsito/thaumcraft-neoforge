package thaumcraft.common.registry;

import java.util.List;
import java.util.function.Supplier;

import net.minecraft.nbt.CompoundTag;

import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import thaumcraft.Thaumcraft;
import thaumcraft.api.capabilities.PlayerAspects;
import thaumcraft.api.capabilities.PlayerKnowledge;
import thaumcraft.api.capabilities.PlayerWarp;
import thaumcraft.common.world.aura.AuraChunk;

/**
 * Data attachments. The player ones replace the capabilities in ThaumcraftCapabilities.
 * copyOnDeath replaces the manual copy in PlayerEvent.Clone; sync sends the data to its owner on player.syncData(...).
 * Knowledge is synced by PacketSyncKnowledge instead, since the client shows a toast for each new research.
 */
public final class TCAttachments {
    private TCAttachments() {
    }

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Thaumcraft.MODID);

    public static final Supplier<AttachmentType<PlayerKnowledge>> KNOWLEDGE = ATTACHMENT_TYPES.register("knowledge",
            () -> AttachmentType.builder(PlayerKnowledge::new)
                    .serialize(PlayerKnowledge.CODEC)
                    .copyOnDeath()
                    .build());

    public static final Supplier<AttachmentType<PlayerWarp>> WARP = ATTACHMENT_TYPES.register("warp",
            () -> AttachmentType.builder(PlayerWarp::new)
                    .serialize(PlayerWarp.CODEC)
                    .copyOnDeath()
                    .sync((holder, player) -> holder == player, PlayerWarp.STREAM_CODEC)
                    .build());

    /** The TC4 research points per aspect (see PlayerAspects). Not in TC6. */
    public static final Supplier<AttachmentType<PlayerAspects>> ASPECTS = ATTACHMENT_TYPES.register("aspects",
            () -> AttachmentType.builder(PlayerAspects::new)
                    .serialize(PlayerAspects.CODEC)
                    .copyOnDeath()
                    .sync((holder, player) -> holder == player, PlayerAspects.STREAM_CODEC)
                    .build());

    /** Per-chunk aura (vis, flux, base). Saved with the chunk; replaces the "Thaumcraft" tag written in ChunkDataEvent. */
    public static final Supplier<AttachmentType<AuraChunk>> AURA = ATTACHMENT_TYPES.register("aura",
            () -> AttachmentType.builder(() -> new AuraChunk(null, (short) 0, 0.0f, 0.0f))
                    .serialize(AuraChunk.CODEC)
                    .build());

    /**
     * The golem seals placed in a chunk, one SealEntity.writeNBT tag each. Replaces the "seals" list the original
     * wrote into the chunk NBT in ChunkDataEvent.Save.
     */
    public static final Supplier<AttachmentType<List<CompoundTag>>> SEALS = ATTACHMENT_TYPES.register("seals",
            () -> AttachmentType.<List<CompoundTag>>builder(() -> List.of())
                    .serialize(CompoundTag.CODEC.listOf(), list -> !list.isEmpty())
                    .build());
}
