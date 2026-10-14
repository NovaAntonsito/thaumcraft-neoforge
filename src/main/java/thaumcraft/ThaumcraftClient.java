package thaumcraft;

import net.minecraft.resources.ResourceLocation;
import thaumcraft.client.renderers.tile.TileHungryChestRenderer;
import thaumcraft.client.renderers.tile.TileJarBrainRenderer;
import thaumcraft.client.gui.GuiSpa;
import thaumcraft.client.gui.GuiFocalManipulator;
import thaumcraft.client.gui.GuiPotionSprayer;
import thaumcraft.common.registry.TCFluids;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import thaumcraft.client.renderers.tile.TileRechargePedestalRenderer;
import thaumcraft.client.renderers.tile.TileDioptraRenderer;
import thaumcraft.common.blocks.devices.BlockInlay;
import thaumcraft.common.blocks.devices.BlockStabilizer;
import thaumcraft.common.tiles.devices.TileStabilizer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.ChatFormatting;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import thaumcraft.client.ponder.ThaumcraftPonderPlugin;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RecipesUpdatedEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import thaumcraft.client.gui.GuiArcaneWorkbench;
import thaumcraft.common.registry.TCMenus;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.common.aspects.ObjectAspects;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.registry.TCBlockEntities;
import thaumcraft.client.renderers.tile.TileCrucibleRenderer;
import thaumcraft.client.renderers.tile.TileAlembicRenderer;
import thaumcraft.client.renderers.tile.TileJarRenderer;
import thaumcraft.client.renderers.tile.TileInfusionMatrixRenderer;
import thaumcraft.client.renderers.tile.TilePedestalRenderer;
import thaumcraft.common.lib.enchantment.EnumInfusionEnchantment;
import thaumcraft.common.registry.TCDataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredBlock;
import java.util.Map;
import thaumcraft.client.gui.GuiSmelter;
import thaumcraft.common.blocks.essentia.BlockJarItem;
import thaumcraft.common.config.ConfigBlocks;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import thaumcraft.client.gui.EssentiaHud;
import thaumcraft.client.gui.GuiResearchTable;
import thaumcraft.client.renderers.tile.TileResearchTableRenderer;
import thaumcraft.common.items.consumables.ItemResearchNote;
import thaumcraft.client.gui.NotificationsHud;
import thaumcraft.client.gui.ThaumometerHud;
import thaumcraft.client.gui.GuiDeconstructionTable;
import thaumcraft.client.renderers.tile.TileDeconstructionTableRenderer;
import thaumcraft.client.renderers.tile.TileBellowsRenderer;
import thaumcraft.client.renderers.tile.TileCentrifugeRenderer;
import thaumcraft.client.renderers.tile.TileTubeBufferRenderer;
import thaumcraft.client.renderers.tile.TileTubeOnewayRenderer;
import thaumcraft.client.renderers.tile.TileTubeValveRenderer;
import thaumcraft.common.blocks.essentia.BlockTube;
import thaumcraft.common.registry.TCEntities;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import thaumcraft.client.gui.GuiHandMirror;
import thaumcraft.client.gui.GuiVoidSiphon;
import thaumcraft.client.gui.GuiThaumatorium;
import thaumcraft.client.renderers.tile.TileThaumatoriumRenderer;
import thaumcraft.client.renderers.tile.TileVoidSiphonRenderer;
import thaumcraft.client.renderers.entity.RenderFluxRift;
import thaumcraft.client.renderers.tile.TileMirrorRenderer;
import thaumcraft.common.items.curios.ItemPrimordialPearl;

/** Client-only code. Not loaded on dedicated servers. */
@Mod(value = Thaumcraft.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = Thaumcraft.MODID, value = Dist.CLIENT)
public class ThaumcraftClient {
    public ThaumcraftClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        // Optional Ponder tutorials: the plugin class touches Ponder, so only load it when Ponder is there.
        if (ModList.get().isLoaded("ponder")) {
            ThaumcraftPonderPlugin.register();
        }
    }

    @SubscribeEvent
    static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(TCBlockEntities.CRUCIBLE.get(), TileCrucibleRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.JAR.get(), TileJarRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.JAR_VOID.get(), TileJarRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.ALEMBIC.get(), TileAlembicRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.RESEARCH_TABLE.get(), TileResearchTableRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.PEDESTAL.get(), TilePedestalRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.DECONSTRUCTION_TABLE.get(), TileDeconstructionTableRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.INFUSION_MATRIX.get(), TileInfusionMatrixRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.TUBE_VALVE.get(), TileTubeValveRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.TUBE_ONEWAY.get(), TileTubeOnewayRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.TUBE_BUFFER.get(), TileTubeBufferRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.BELLOWS.get(), TileBellowsRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.CENTRIFUGE.get(), TileCentrifugeRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.MIRROR.get(), TileMirrorRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.VOID_SIPHON.get(), TileVoidSiphonRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.THAUMATORIUM.get(), TileThaumatoriumRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.MIRROR_ESSENTIA.get(), TileMirrorRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.RECHARGE_PEDESTAL.get(), TileRechargePedestalRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.DIOPTRA.get(), TileDioptraRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.HUNGRY_CHEST.get(), TileHungryChestRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.JAR_BRAIN.get(), TileJarBrainRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.FOCAL_MANIPULATOR.get(), thaumcraft.client.renderers.tile.TileFocalManipulatorRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.HOLE.get(), thaumcraft.client.renderers.tile.TileHoleRenderer::new);
        event.registerEntityRenderer(TCEntities.ALUMENTUM.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(TCEntities.FLUX_RIFT.get(), RenderFluxRift::new);
        event.registerEntityRenderer(TCEntities.FOCUS_PROJECTILE.get(), thaumcraft.client.renderers.entity.projectile.RenderNoProjectile::new);
        event.registerEntityRenderer(TCEntities.FOCUS_CLOUD.get(), thaumcraft.client.renderers.entity.projectile.RenderNoProjectile::new);
        event.registerEntityRenderer(TCEntities.FOCUS_MINE.get(), thaumcraft.client.renderers.entity.projectile.RenderFocusMine::new);
        event.registerEntityRenderer(TCEntities.SPELL_BAT.get(), thaumcraft.client.renderers.entity.mob.RenderSpellBat::new);
        event.registerBlockEntityRenderer(TCBlockEntities.GOLEM_BUILDER.get(), thaumcraft.client.renderers.tile.TileGolemBuilderRenderer::new);
        event.registerEntityRenderer(TCEntities.GOLEM.get(), thaumcraft.common.golems.client.RenderThaumcraftGolem::new);
        event.registerEntityRenderer(TCEntities.GOLEM_DART.get(), thaumcraft.client.renderers.entity.projectile.RenderDart::new);
    }

    @SubscribeEvent
    static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(TCMenus.ARCANE_WORKBENCH.get(), GuiArcaneWorkbench::new);
        event.register(TCMenus.SMELTER.get(), GuiSmelter::new);
        event.register(TCMenus.RESEARCH_TABLE.get(), GuiResearchTable::new);
        event.register(TCMenus.DECONSTRUCTION_TABLE.get(), GuiDeconstructionTable::new);
        event.register(TCMenus.HAND_MIRROR.get(), GuiHandMirror::new);
        event.register(TCMenus.VOID_SIPHON.get(), GuiVoidSiphon::new);
        event.register(TCMenus.THAUMATORIUM.get(), GuiThaumatorium::new);
        event.register(TCMenus.SPA.get(), GuiSpa::new);
        event.register(TCMenus.FOCAL_MANIPULATOR.get(), GuiFocalManipulator::new);
        event.register(TCMenus.FOCUS_POUCH.get(), thaumcraft.client.gui.GuiFocusPouch::new);
        event.register(TCMenus.POTION_SPRAYER.get(), GuiPotionSprayer::new);
        event.register(TCMenus.GOLEM_BUILDER.get(), thaumcraft.client.gui.GuiGolemBuilder::new);
    }

    /** FluidPure: the water textures tinted 0x77FFCCAA (getColor of the original). */
    @SubscribeEvent
    static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {
                return ResourceLocation.withDefaultNamespace("block/water_still");
            }

            @Override
            public ResourceLocation getFlowingTexture() {
                return ResourceLocation.withDefaultNamespace("block/water_flow");
            }

            @Override
            public int getTintColor() {
                return 0x77FFCCAA;
            }
        }, TCFluids.PURIFYING_TYPE.get());
    }

    @SubscribeEvent
    static void registerKeys(net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent event) {
        event.register(thaumcraft.client.lib.events.KeyHandler.keyF);
        event.register(thaumcraft.client.lib.events.KeyHandler.keyG);
    }

    @SubscribeEvent
    static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.CROSSHAIR, EssentiaHud.ID, EssentiaHud::render);
        event.registerAbove(VanillaGuiLayers.CROSSHAIR, ThaumometerHud.ID, ThaumometerHud::render);
        event.registerAbove(VanillaGuiLayers.CROSSHAIR, thaumcraft.client.gui.CasterHud.ID, thaumcraft.client.gui.CasterHud::render);
        event.registerAboveAll(NotificationsHud.ID, NotificationsHud::render);
        event.registerAbove(VanillaGuiLayers.BOSS_OVERLAY, thaumcraft.client.gui.AnomalyHud.ID, thaumcraft.client.gui.AnomalyHud::render);
    }

    /** Clients generate item aspects from the recipes the server sent. */
    @SubscribeEvent
    static void onRecipesUpdated(RecipesUpdatedEvent event) {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection != null) {
            ObjectAspects.setContext(event.getRecipeManager(), connection.registryAccess(), connection.potionBrewing());
        }
    }

    /** Port of ColorHandler: vis crystals are tinted with their aspect's color. */
    @SubscribeEvent
    static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register((stack, tintIndex) -> {
            AspectList aspects = ConfigItems.crystalEssence.get().getAspects(stack);
            return 0xFF000000 | (aspects != null ? aspects.getAspects()[0].getColor() : 0xFFFFFF);
        }, ConfigItems.crystalEssence.get());
        // Reborn's research note colors: layer 1 (the overlay) takes the note's color.
        event.register((stack, tintIndex) -> tintIndex == 1 ? 0xFF000000 | ItemResearchNote.getColor(stack) : 0xFFFFFFFF,
                ConfigItems.researchNote.get());
        // Phials and labels: the overlay layer. Jars: the essentia inside the item model (tintindex 1).
        event.register((stack, tintIndex) -> tintIndex == 1 ? aspectColor(ConfigItems.phial.get().getAspects(stack)) : -1,
                ConfigItems.phial.get());
        event.register((stack, tintIndex) -> tintIndex == 1 ? aspectColor(ConfigItems.label.get().getAspects(stack)) : -1,
                ConfigItems.label.get());
        event.register((stack, tintIndex) -> tintIndex == 1 && stack.getItem() instanceof BlockJarItem jar ? aspectColor(jar.getAspects(stack)) : -1,
                ConfigBlocks.jarNormal.get(), ConfigBlocks.jarVoid.get());
        // Foci are tinted with the color of their effects; the caster's focus layer with its mounted focus.
        event.register((stack, tintIndex) -> stack.getItem() instanceof thaumcraft.common.items.casters.ItemFocus f
                ? 0xFF000000 | f.getFocusColor(stack) : -1, ConfigItems.focus1.get(), ConfigItems.focus2.get(), ConfigItems.focus3.get());
        event.register((stack, tintIndex) -> {
            if (tintIndex > 0 && stack.getItem() instanceof thaumcraft.common.items.casters.ItemCaster c) {
                ItemStack fs = c.getFocusStack(stack);
                if (fs.getItem() instanceof thaumcraft.common.items.casters.ItemFocus f) {
                    return 0xFF000000 | f.getFocusColor(fs);
                }
            }
            return -1;
        }, ConfigItems.casterBasic.get());
        // itemGolemColourHandler: the golem's material color.
        event.register((stack, tintIndex) -> {
            Long props = stack.get(thaumcraft.common.registry.TCDataComponents.GOLEM_PROPS.get());
            return 0xFF000000 | (props != null ? thaumcraft.common.golems.GolemProperties.fromLong(props).getMaterial().itemColor : 0xFFFFFF);
        }, ConfigItems.golemPlacer.get());
    }

    /** Port of ColorHandler for candles and nitor: tinted with their dye's map color. */
    @SubscribeEvent
    static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register((state, level, pos, tintIndex) -> 0xFF000000 | state.getMapColor(level, pos).col, dyedBlocks());
        // The filtered tube's core takes its filter's color.
        event.register((state, level, pos, tintIndex) -> tintIndex == 1 ? 0xFF000000 | BlockTube.filterColor(level, pos) : -1,
                ConfigBlocks.tubeFilter.get());
        // Inlay and stabilizer: grey when empty, brighter with charge.
        event.register((state, level, pos, tintIndex) -> tintIndex == 0 ? BlockInlay.colorMultiplier(state.getValue(BlockInlay.CHARGE)) : -1,
                ConfigBlocks.inlay.get());
        event.register((state, level, pos, tintIndex) -> {
            if (tintIndex != 0) {
                return -1;
            }
            int charge = level != null && pos != null && level.getBlockEntity(pos) instanceof TileStabilizer te ? te.getEnergy() : 0;
            return BlockStabilizer.colorMultiplier(charge);
        }, ConfigBlocks.stabilizer.get());
    }

    @SubscribeEvent
    static void registerDyedItemColors(RegisterColorHandlersEvent.Item event) {
        for (Block block : dyedBlocks()) {
            int color = 0xFF000000 | block.defaultMapColor().col;
            event.register((stack, tintIndex) -> color, block);
        }
    }

    private static Block[] dyedBlocks() {
        return java.util.stream.Stream.concat(ConfigBlocks.candles.values().stream(), ConfigBlocks.nitor.values().stream())
                .map(DeferredBlock::get).toArray(Block[]::new);
    }

    /** PlayerEvents.tooltipEvent: infusion enchantments (just under the name) and warping. */
    @SubscribeEvent
    static void onInfusionTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        int line = 1;
        for (Map.Entry<EnumInfusionEnchantment, Integer> e : EnumInfusionEnchantment.getInfusionEnchantmentMap(stack).entrySet()) {
            MutableComponent s = Component.translatable("enchantment.infusion." + e.getKey().name());
            if (e.getKey().maxLevel > 1) {
                s.append(" ").append(Component.translatable("enchantment.level." + e.getValue()));
            }
            event.getToolTip().add(Math.min(line++, event.getToolTip().size()), s.withStyle(ChatFormatting.GOLD));
        }
        // TODO: getFinalWarp also counts IWarpingGear items.
        int warp = stack.getOrDefault(TCDataComponents.WARP.get(), 0);
        if (warp > 0) {
            event.getToolTip().add(Component.translatable("item.warping").append(" " + warp).withStyle(ChatFormatting.DARK_PURPLE));
        }
    }

    private static int aspectColor(@Nullable AspectList aspects) {
        return 0xFF000000 | (aspects != null ? aspects.getAspects()[0].getColor() : 0xFFFFFF);
    }

    /**
     * Item model properties (the 1.12 addPropertyOverride calls): "fill" 0-4 for jars,
     * and "filled" for phials and labels, which used metadata in 1.12.
     */
    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemBlockRenderTypes.setRenderLayer(TCFluids.PURIFYING.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(TCFluids.PURIFYING_FLOWING.get(), RenderType.translucent());
            ItemPropertyFunction fill = (stack, level, entity, seed) -> {
                if (!(stack.getItem() instanceof BlockJarItem jar)) return 0.0f;
                double d = jar.getDurabilityForDisplay(stack);
                if (d == 1.0) return 0.0f;
                if (d >= 0.75) return 1.0f;
                if (d >= 0.5) return 2.0f;
                if (d >= 0.25) return 3.0f;
                return 4.0f;
            };
            ItemProperties.register(ConfigBlocks.jarNormal.get().asItem(), Thaumcraft.id("fill"), fill);
            ItemProperties.register(ConfigBlocks.jarVoid.get().asItem(), Thaumcraft.id("fill"), fill);
            ItemProperties.register(ConfigItems.phial.get(), Thaumcraft.id("filled"),
                    (stack, level, entity, seed) -> ConfigItems.phial.get().getAspects(stack) != null ? 1.0f : 0.0f);
            ItemProperties.register(ConfigItems.casterBasic.get(), Thaumcraft.id("focus"),
                    (stack, level, entity, seed) -> ConfigItems.casterBasic.get().getFocus(stack) != null ? 1.0f : 0.0f);
            // Reborn's discovery model (metadata 64): a solved note.
            ItemProperties.register(ConfigItems.researchNote.get(), Thaumcraft.id("complete"),
                    (stack, level, entity, seed) -> ItemResearchNote.isComplete(stack) ? 1.0f : 0.0f);
            ItemProperties.register(ConfigItems.label.get(), Thaumcraft.id("filled"),
                    (stack, level, entity, seed) -> ConfigItems.label.get().getAspects(stack) != null ? 1.0f : 0.0f);
            // The pearl wears down: pearl, nodule, mote.
            ItemProperties.register(ConfigItems.primordialPearl.get(), Thaumcraft.id("type"),
                    (stack, level, entity, seed) -> ItemPrimordialPearl.getType(stack));
            // Mirror items with a link use the open pane (metadata 1 in 1.12).
            ItemPropertyFunction linked = (stack, level, entity, seed) -> stack.has(TCDataComponents.MIRROR_LINK.get()) ? 1.0f : 0.0f;
            ItemProperties.register(ConfigBlocks.mirror.get().asItem(), Thaumcraft.id("linked"), linked);
            ItemProperties.register(ConfigBlocks.mirrorEssentia.get().asItem(), Thaumcraft.id("linked"), linked);
        });
    }
}
