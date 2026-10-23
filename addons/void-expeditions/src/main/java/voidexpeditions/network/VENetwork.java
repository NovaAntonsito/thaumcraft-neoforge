package voidexpeditions.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class VENetwork {
    private VENetwork() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(OpenPerkChoicePayload.TYPE, OpenPerkChoicePayload.STREAM_CODEC, OpenPerkChoicePayload::handle);
        registrar.playToServer(ChoosePerkPayload.TYPE, ChoosePerkPayload.STREAM_CODEC, ChoosePerkPayload::handle);
        registrar.playToServer(ConfigureTerminalPayload.TYPE, ConfigureTerminalPayload.STREAM_CODEC, ConfigureTerminalPayload::handle);
        registrar.playToServer(BuyPayload.TYPE, BuyPayload.STREAM_CODEC, BuyPayload::handle);
    }
}
