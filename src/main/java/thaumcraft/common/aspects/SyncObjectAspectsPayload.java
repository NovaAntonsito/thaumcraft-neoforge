package thaumcraft.common.aspects;

import java.util.List;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import thaumcraft.Thaumcraft;

/** Server to client: item aspects loaded from datapacks. */
public record SyncObjectAspectsPayload(List<ObjectAspects.Entry> entries) implements CustomPacketPayload {
    public static final Type<SyncObjectAspectsPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Thaumcraft.MODID, "sync_object_aspects"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncObjectAspectsPayload> STREAM_CODEC =
            ObjectAspects.Entry.STREAM_CODEC.apply(ByteBufCodecs.list())
                    .map(SyncObjectAspectsPayload::new, SyncObjectAspectsPayload::entries)
                    .cast();

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /** Runs on the client main thread. */
    public static void handle(SyncObjectAspectsPayload payload, IPayloadContext context) {
        // In singleplayer the client shares memory with the integrated server and already has the data.
        if (context.connection().isMemoryConnection()) return;
        ObjectAspects.setEntries(payload.entries());
    }
}
