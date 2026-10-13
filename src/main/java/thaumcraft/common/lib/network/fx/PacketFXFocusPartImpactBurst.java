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

/** Port of PacketFXFocusPartImpactBurst: a bigger burst of focus effect particles. */
public record PacketFXFocusPartImpactBurst(float x, float y, float z, String parts) implements CustomPacketPayload {
    public static final Type<PacketFXFocusPartImpactBurst> TYPE = new Type<>(Thaumcraft.id("fx_focus_part_impact_burst"));
    public static final StreamCodec<ByteBuf, PacketFXFocusPartImpactBurst> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, PacketFXFocusPartImpactBurst::x,
            ByteBufCodecs.FLOAT, PacketFXFocusPartImpactBurst::y,
            ByteBufCodecs.FLOAT, PacketFXFocusPartImpactBurst::z,
            ByteBufCodecs.STRING_UTF8, PacketFXFocusPartImpactBurst::parts,
            PacketFXFocusPartImpactBurst::new);

    public PacketFXFocusPartImpactBurst(double x, double y, double z, String[] parts) {
        this((float) x, (float) y, (float) z, String.join("%", parts));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketFXFocusPartImpactBurst message, IPayloadContext context) {
        Level world = context.player().level();
        String[] partKeys = message.parts.split("%");
        int amt = Math.max(1, 20 / partKeys.length);
        RandomSource r = world.random;
        for (String k : partKeys) {
            if (FocusEngine.getElement(k) instanceof FocusEffect part) {
                for (int a = 0; a < amt; ++a) {
                    part.renderParticleFX(world, message.x, message.y, message.z, r.nextGaussian() * 0.4, r.nextGaussian() * 0.4, r.nextGaussian() * 0.4);
                }
            }
        }
    }
}
