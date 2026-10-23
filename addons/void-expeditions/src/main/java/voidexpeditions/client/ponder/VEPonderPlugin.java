package voidexpeditions.client.ponder;

import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;
import voidexpeditions.VoidExpeditions;
import voidexpeditions.registry.VEBlocks;
import voidexpeditions.registry.VEItems;

/**
 * Ponder tutorials of the void gate (hold W over a segment or the terminal). Only touched from {@link #register()},
 * which the client calls when Ponder is loaded. Scene texts live in the lang files under
 * voidexpeditions.ponder.&lt;scene&gt;.header / text_N; the structure is tools/gen_ponder_nbt.py's void_gate.nbt.
 */
public class VEPonderPlugin implements PonderPlugin {
    public static final ResourceLocation TAG_EXPEDITIONS = VoidExpeditions.id("void_expeditions");

    public static void register() {
        PonderIndex.addPlugin(new VEPonderPlugin());
    }

    @Override
    public String getModId() {
        return VoidExpeditions.MODID;
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        PonderSceneRegistrationHelper<DeferredHolder<?, ?>> h = helper.withKeyFunction(DeferredHolder::getId);
        h.forComponents(VEBlocks.VOID_GATE_RING, VEBlocks.EXPEDITION_TERMINAL)
                .addStoryBoard("void_gate", VEPonderScenes::voidGate)
                .addStoryBoard("void_gate", VEPonderScenes::expedition);
        h.forComponents(VEItems.VOID_SIGIL, VEItems.VOID_SHARD)
                .addStoryBoard("void_gate", VEPonderScenes::expedition);
    }

    @Override
    public void registerTags(PonderTagRegistrationHelper<ResourceLocation> helper) {
        PonderTagRegistrationHelper<DeferredHolder<?, ?>> h = helper.withKeyFunction(DeferredHolder::getId);
        // Title and description: voidexpeditions.ponder.tag.void_expeditions / .description.
        helper.registerTag(TAG_EXPEDITIONS).item(VEItems.VOID_GATE_RING.get(), true, false).addToIndex().register();
        h.addToTag(TAG_EXPEDITIONS).add(VEBlocks.VOID_GATE_RING).add(VEBlocks.EXPEDITION_TERMINAL);
    }
}
