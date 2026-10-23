package voidexpeditions;

import com.mojang.logging.LogUtils;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;
import org.slf4j.Logger;
import thaumcraft.api.casters.CasterModifiers;
import thaumcraft.api.maze.MazeWorlds;
import thaumcraft.common.lib.research.ResearchManager;
import voidexpeditions.network.VENetwork;
import voidexpeditions.perk.Perks;
import voidexpeditions.registry.VEAttachments;
import voidexpeditions.registry.VEBlockEntities;
import voidexpeditions.registry.VEBlocks;
import voidexpeditions.registry.VEItems;
import voidexpeditions.run.ExpeditionDim;
import voidexpeditions.run.ExpeditionMazeWorld;

/** Void Expeditions: rogue-lite runs through chains of void labyrinths, opened from a terminal. */
@Mod(VoidExpeditions.MODID)
public class VoidExpeditions {
    public static final String MODID = "voidexpeditions";
    public static final Logger LOGGER = LogUtils.getLogger();

    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    static {
        TABS.register("main", () -> CreativeModeTab.builder()
                .title(Component.translatable("itemGroup.voidexpeditions"))
                .icon(() -> new ItemStack(VEItems.VOID_SIGIL.get()))
                .displayItems((params, out) -> {
                    out.accept(VEItems.EXPEDITION_TERMINAL.get());
                    out.accept(VEItems.VOID_GATE_RING.get());
                    out.accept(VEItems.VOID_SIGIL.get());
                    out.accept(VEItems.VOID_SHARD.get());
                })
                .build());
    }

    public VoidExpeditions(IEventBus modEventBus, ModContainer modContainer) {
        VEBlocks.BLOCKS.register(modEventBus);
        VEItems.ITEMS.register(modEventBus);
        VEBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        VEAttachments.ATTACHMENT_TYPES.register(modEventBus);
        TABS.register(modEventBus);
        modEventBus.addListener(VENetwork::register);

        MazeWorlds.register(ExpeditionDim.KEY, ExpeditionMazeWorld.INSTANCE);
        CasterModifiers.register(Perks.CASTER_MODIFIER);
        // Parsed with Thaumcraft's own in its common setup, after every mod is constructed.
        ResearchManager.jsonLocs.add(id("research/expeditions"));
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
