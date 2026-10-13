package thaumcraft.common.lib.network.fx;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import thaumcraft.Thaumcraft;
import thaumcraft.api.casters.FocusEffect;
import thaumcraft.api.casters.FocusEngine;

/** Port of PacketFXFocusPartImpact: particles of each effect where a focus hit. */
public record PacketFXFocusPartImpact(float x, float y, float z, String parts) implements CustomPacketPayload {
    public static final Type<PacketFXFocusPartImpact> TYPE = new Type<>(Thaumcraft.id("fx_focus_part_impact"));
    public static final StreamCodec<ByteBuf, PacketFXFocusPartImpact> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, PacketFXFocusPartImpact::x,
            ByteBufCodecs.FLOAT, PacketFXFocusPartImpact::y,
            ByteBufCodecs.FLOAT, PacketFXFocusPartImpact::z,
            ByteBufCodecs.STRING_UTF8, PacketFXFocusPartImpact::parts,
            PacketFXFocusPartImpact::new);

    public PacketFXFocusPartImpact(double x, double y, double z, String[] parts) {
        this((float) x, (float) y, (float) z, String.join("%", parts));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketFXFocusPartImpact message, IPayloadContext context) {
        Level world = context.player().level();
        String[] partKeys = message.parts.split("%");
        int amt = Math.max(1, 15 / partKeys.length);
        RandomSource r = world.random;
        for (String k : partKeys) {
            if (FocusEngine.getElement(k) instanceof FocusEffect part) {
                for (int a = 0; a < amt; ++a) {
                    part.renderParticleFX(world, message.x, message.y, message.z, r.nextGaussian() * 0.15, r.nextGaussian() * 0.15, r.nextGaussian() * 0.15);
                }
            }
        }
    }
}
