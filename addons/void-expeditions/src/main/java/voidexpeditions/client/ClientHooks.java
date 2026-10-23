package voidexpeditions.client;

import java.util.List;

import net.minecraft.client.Minecraft;
import voidexpeditions.block.ExpeditionTerminalBlockEntity;

/** The client side of blocks and packets. Only called on the client, so its classes never load on a server. */
public final class ClientHooks {
    private ClientHooks() {
    }

    public static void openTerminal(ExpeditionTerminalBlockEntity terminal) {
        Minecraft.getInstance().setScreen(new TerminalScreen(terminal.getBlockPos(), terminal.getDifficulty(), terminal.getLength()));
    }

    public static void openPerkChoice(List<String> offered, int stage, int length) {
        Minecraft.getInstance().setScreen(new PerkChoiceScreen(offered, stage, length));
    }
}
