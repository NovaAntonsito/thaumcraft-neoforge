package voidexpeditions.registry;

import java.util.function.Supplier;

import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import voidexpeditions.VoidExpeditions;
import voidexpeditions.run.RunState;

public final class VEAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, VoidExpeditions.MODID);

    /** The player's expedition, synced to them for the perk screen and the caster perks. */
    public static final Supplier<AttachmentType<RunState>> RUN = ATTACHMENT_TYPES.register("run",
            () -> AttachmentType.builder(RunState::new)
                    .serialize(RunState.CODEC)
                    .copyOnDeath()
                    .sync((holder, player) -> holder == player, RunState.STREAM_CODEC)
                    .build());

    /** Server only: the way home, and the inventory kept through a death in the void. */
    public static final Supplier<AttachmentType<CompoundTag>> HOME = ATTACHMENT_TYPES.register("home",
            () -> AttachmentType.builder(() -> new CompoundTag())
                    .serialize(CompoundTag.CODEC)
                    .copyOnDeath()
                    .build());

    private VEAttachments() {
    }
}
