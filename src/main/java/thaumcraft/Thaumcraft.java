package thaumcraft;

import thaumcraft.common.registry.TCFluids;
import thaumcraft.common.registry.TCMobEffects;
import thaumcraft.common.registry.TCEntities;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.common.Tags;
import thaumcraft.common.registry.TCMenus;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.common.aspects.ObjectAspects;
import thaumcraft.common.command.ThaumcraftCommand;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.config.ConfigAspects;
import thaumcraft.common.config.ConfigResearch;
import net.minecraft.world.item.ItemStack;
import java.util.function.Function;
import thaumcraft.common.lib.crafting.InfusionEnchantmentRecipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.common.network.TCNetwork;
import thaumcraft.common.registry.TCAttachments;
import thaumcraft.common.registry.TCDataComponents;
import thaumcraft.common.registry.TCBlockEntities;
import thaumcraft.common.registry.TCRecipes;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.lib.crafting.DustTriggerSimple;
import thaumcraft.common.lib.crafting.DustTriggerMultiblock;
import thaumcraft.api.crafting.Part;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.api.crafting.IDustTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.tags.TagKey;
import net.minecraft.core.registries.Registries;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@Mod(Thaumcraft.MODID)
public class Thaumcraft {
    public static final String MODID = "thaumcraft";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Thaumcraft(IEventBus modEventBus, ModContainer modContainer) {
        // Mod bus: registries and setup.
        ConfigBlocks.BLOCKS.register(modEventBus);
        ConfigItems.ITEMS.register(modEventBus);
        ConfigItems.CREATIVE_TABS.register(modEventBus);
        TCDataComponents.DATA_COMPONENTS.register(modEventBus);
        TCAttachments.ATTACHMENT_TYPES.register(modEventBus);
        TCBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        TCEntities.ENTITY_TYPES.register(modEventBus);
        TCRecipes.RECIPE_TYPES.register(modEventBus);
        TCRecipes.RECIPE_SERIALIZERS.register(modEventBus);
        SoundsTC.SOUNDS.register(modEventBus);
        TCMenus.MENUS.register(modEventBus);
        TCFluids.FLUID_TYPES.register(modEventBus);
        TCFluids.FLUIDS.register(modEventBus);
        TCMobEffects.MOB_EFFECTS.register(modEventBus);
        modEventBus.addListener(Thaumcraft::onCommonSetup);
        modEventBus.addListener(Thaumcraft::onRegisterCapabilities);
        modEventBus.addListener(TCNetwork::register);
        modEventBus.addListener(TCEntities::registerAttributes);

        // Game bus: server events.
        NeoForge.EVENT_BUS.addListener(Thaumcraft::onAddReloadListeners);
        NeoForge.EVENT_BUS.addListener(Thaumcraft::onDatapackSync);
        NeoForge.EVENT_BUS.addListener(Thaumcraft::onRegisterCommands);
        NeoForge.EVENT_BUS.addListener(Thaumcraft::onServerStarted);
        NeoForge.EVENT_BUS.addListener(Thaumcraft::onTagsUpdated);
        NeoForge.EVENT_BUS.addListener(thaumcraft.common.anomaly.AnomalyEvents::onLevelTick);
        NeoForge.EVENT_BUS.addListener(thaumcraft.common.anomaly.AnomalyEvents::onLogin);
        NeoForge.EVENT_BUS.addListener(thaumcraft.common.anomaly.AnomalyEvents::onChangeDimension);
        NeoForge.EVENT_BUS.addListener(thaumcraft.common.anomaly.AnomalyEvents::onRespawn);
        NeoForge.EVENT_BUS.addListener(thaumcraft.common.anomaly.AnomalyEvents::onRightClickItem);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        LOGGER.info("Thaumcraft: {} aspects registered", Aspect.aspects.size());
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    /**
     * ConfigResearch.init/postInit, then ConfigRecipes.initializeAlchemyRecipes: salis mundus on a bookshelf,
     * a crafting table or a cauldron (1.12 had one cauldron block).
     */
    private static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ConfigAspects.registerEntityTags();
            ConfigItems.registerFocusElements();
            ConfigResearch.init();
            // DustTriggerSimple(BOOKSHELF / CRAFTING_TABLE) and DustTriggerOre("bookshelf" / "workbench") of the original are one tag check.
            IDustTrigger.registerDustTrigger(new DustTriggerSimple("!gotdream",
                    state -> state.is(Tags.Blocks.BOOKSHELVES), () -> new ItemStack(ConfigItems.thaumonomicon.get())));
            IDustTrigger.registerDustTrigger(new DustTriggerSimple("FIRSTSTEPS@1",
                    state -> state.is(Tags.Blocks.PLAYER_WORKSTATIONS_CRAFTING_TABLES), () -> new ItemStack(ConfigBlocks.arcaneWorkbench.get())));
            IDustTrigger.registerDustTrigger(new DustTriggerSimple("UNLOCKALCHEMY@1",
                    state -> state.is(Blocks.CAULDRON) || state.is(Blocks.WATER_CAULDRON), () -> new ItemStack(ConfigBlocks.crucible.get())));
            // Reborn: salis mundus on a wooden table also makes a research table.
            IDustTrigger.registerDustTrigger(new DustTriggerSimple("RESEARCH",
                    state -> state.is(ConfigBlocks.tableWood.get()), () -> new ItemStack(ConfigBlocks.researchTable.get())));
            thaumcraft.common.golems.GolemProperties.registerDefaults();
            ConfigItems.registerSeals();
            thaumcraft.common.lib.utils.CropUtils.registerDefaults();
            registerInfusionAltar();
            registerFakeRecipes();
            // CommonProxy: dispensers throw alumentum (BehaviorDispenseAlumetum).
            net.minecraft.world.level.block.DispenserBlock.registerProjectileBehavior(ConfigItems.alumentum.get());
        });
    }

    /**
     * ConfigRecipes.initializeAlchemyRecipes: the infusion altar blueprint (top layer first).
     * Arcane stone in the corners: the upper ones vanish and the lower ones become pillars.
     */
    private static void registerInfusionAltar() {
        Block stone = ConfigBlocks.stoneArcane.get();
        Part im = new Part(ConfigBlocks.infusionMatrix.get(), null);
        Part snt = new Part(stone, Blocks.AIR);
        Part snb1 = new Part(stone, pillar(Direction.EAST));
        Part snb2 = new Part(stone, pillar(Direction.NORTH));
        Part snb3 = new Part(stone, pillar(Direction.SOUTH));
        Part snb4 = new Part(stone, pillar(Direction.WEST));
        Part pn = new Part(ConfigBlocks.pedestalArcane.get().defaultBlockState(), null);
        Part[][][] infusionAltarNormalBlueprint = {
                {{null, null, null}, {null, im, null}, {null, null, null}},
                {{snt, null, snt}, {null, null, null}, {snt, null, snt}},
                {{snb1, null, snb2}, {null, pn, null}, {snb3, null, snb4}}};
        IDustTrigger.registerDustTrigger(new DustTriggerMultiblock("INFUSION", infusionAltarNormalBlueprint));
        ThaumcraftApi.addMultiblockRecipeToCatalog(id("infusionaltar"), new ThaumcraftApi.BluePrint("INFUSION", infusionAltarNormalBlueprint,
                new ItemStack(stone, 8), new ItemStack(ConfigBlocks.pedestalArcane.get()), new ItemStack(ConfigBlocks.infusionMatrix.get())));
        // TODO: the ancient and eldritch altars (their stones are not ported yet).
        registerThaumatorium();
        registerGolemPress();
        registerInfernalFurnace();
        registerSmeltingBonuses();
    }

    /** ConfigRecipes.initializeAlchemyRecipes: two alchemical constructs over a crucible, facing the player. */
    private static void registerThaumatorium() {
        Part th1 = new Part(ConfigBlocks.metalAlchemical.get().defaultBlockState(), ConfigBlocks.thaumatoriumTop.get()).setApplyPlayerFacing(true);
        Part th2 = new Part(ConfigBlocks.metalAlchemical.get().defaultBlockState(), ConfigBlocks.thaumatorium.get()).setApplyPlayerFacing(true);
        Part th3 = new Part(ConfigBlocks.crucible.get(), null);
        Part[][][] thaumotoriumBlueprint = {{{th1}}, {{th2}}, {{th3}}};
        IDustTrigger.registerDustTrigger(new DustTriggerMultiblock("THAUMATORIUM", thaumotoriumBlueprint));
        ThaumcraftApi.addMultiblockRecipeToCatalog(id("thaumatorium"), new ThaumcraftApi.BluePrint("THAUMATORIUM", thaumotoriumBlueprint,
                new ItemStack(ConfigBlocks.metalAlchemical.get(), 2), new ItemStack(ConfigBlocks.crucible.get())));
    }

    /** ConfigRecipes.initializeAlchemyRecipes: the golem press, a piston facing up under iron bars, with a cauldron, an anvil and a stone table. */
    private static void registerGolemPress() {
        Part gp1 = new Part(Blocks.IRON_BARS, ConfigBlocks.placeholderBars.get());
        Part gp2 = new Part(Blocks.CAULDRON, ConfigBlocks.placeholderCauldron.get());
        Part gp3 = new Part(Blocks.PISTON.defaultBlockState().setValue(net.minecraft.world.level.block.piston.PistonBaseBlock.FACING, Direction.UP),
                ConfigBlocks.golemBuilder.get());
        Part gp4 = new Part(Blocks.ANVIL, ConfigBlocks.placeholderAnvil.get());
        Part gp5 = new Part(ConfigBlocks.tableStone.get(), ConfigBlocks.placeholderTable.get());
        Part[][][] golempressBlueprint = {{{null, null}, {gp1, null}}, {{gp2, gp4}, {gp3, gp5}}};
        IDustTrigger.registerDustTrigger(new DustTriggerMultiblock("MINDCLOCKWORK", golempressBlueprint));
        ThaumcraftApi.addMultiblockRecipeToCatalog(id("golempress"), new ThaumcraftApi.BluePrint("MINDCLOCKWORK",
                new ItemStack(ConfigBlocks.golemBuilder.get()), golempressBlueprint, new ItemStack(Blocks.IRON_BARS), new ItemStack(Items.CAULDRON),
                new ItemStack(Blocks.PISTON), new ItemStack(Blocks.ANVIL), new ItemStack(ConfigBlocks.tableStone.get())));
    }

    /** ConfigRecipes.initializeAlchemyRecipes: the infernal furnace, a 3x3x3 shell of nether brick and obsidian around lava. */
    private static void registerInfernalFurnace() {
        Part nb = new Part(Blocks.NETHER_BRICKS, ConfigBlocks.placeholderNetherbrick.get());
        Part ob = new Part(Blocks.OBSIDIAN, ConfigBlocks.placeholderObsidian.get());
        Part ib = new Part(Blocks.IRON_BARS, "AIR");
        Part la = new Part(Fluids.LAVA, ConfigBlocks.infernalFurnace.get(), true);
        Part[][][] infernalFurnaceBlueprint = {
                {{nb, ob, nb}, {ob, null, ob}, {nb, ob, nb}},
                {{nb, ob, nb}, {ob, la, ob}, {nb, ib, nb}},
                {{nb, ob, nb}, {ob, ob, ob}, {nb, ob, nb}}};
        IDustTrigger.registerDustTrigger(new DustTriggerMultiblock("INFERNALFURNACE", infernalFurnaceBlueprint));
        ThaumcraftApi.addMultiblockRecipeToCatalog(id("infernalfurnace"), new ThaumcraftApi.BluePrint("INFERNALFURNACE",
                infernalFurnaceBlueprint, new ItemStack(Blocks.NETHER_BRICKS, 12), new ItemStack(Blocks.OBSIDIAN, 12),
                new ItemStack(Blocks.IRON_BARS), new ItemStack(Items.LAVA_BUCKET)));
    }

    /**
     * ConfigRecipes.initializeSmelting: the infernal furnace's bonus nuggets. Ore dictionary names become c: tags.
     * Since 1.17 ores drop raw materials, so each metal's c:raw_materials tag gets the same bonus as its ore.
     * Native clusters and meat chunks are not ported yet (TODO), so their bonuses are left out.
     */
    private static void registerSmeltingBonuses() {
        String[][] metals = {{"gold", null}, {"iron", null}, {"copper", "copper"}, {"tin", "tin"}, {"silver", "silver"}, {"lead", "lead"}};
        for (String[] m : metals) {
            ItemStack nugget = m[1] == null ? new ItemStack(m[0].equals("gold") ? Items.GOLD_NUGGET : Items.IRON_NUGGET)
                    : new ItemStack(ConfigItems.nuggets.get(m[1]).get());
            ThaumcraftApi.addSmeltingBonus(itemTag("ores/" + m[0]), nugget);
            ThaumcraftApi.addSmeltingBonus(itemTag("raw_materials/" + m[0]), nugget);
        }
        ThaumcraftApi.addSmeltingBonus(itemTag("ores/cinnabar"), new ItemStack(ConfigItems.nuggets.get("quicksilver").get()));
        ThaumcraftApi.addSmeltingBonus(itemTag("ores/quartz"), new ItemStack(ConfigItems.nuggets.get("quartz").get()));
        ItemStack rare = new ItemStack(ConfigItems.nuggets.get("rareearth").get());
        Object[][] rareEarth = {{"ores/diamond", 0.025f}, {"ores/redstone", 0.01f}, {"ores/lapis", 0.01f}, {"ores/emerald", 0.025f},
                {"ores/gold", 0.02f}, {"ores/iron", 0.01f}, {"ores/cinnabar", 0.025f}, {"ores/copper", 0.01f}, {"ores/tin", 0.01f},
                {"ores/silver", 0.02f}, {"ores/lead", 0.01f}, {"ores/quartz", 0.01f}};
        for (Object[] r : rareEarth) {
            ThaumcraftApi.addSmeltingBonus(itemTag((String) r[0]), rare.copy(), (Float) r[1]);
        }
    }

    private static TagKey<Item> itemTag(String path) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", path));
    }

    /**
     * The original's fake catalog recipes. Here they point to the real datapack recipe (or recipe group) to show.
     */
    private static void registerFakeRecipes() {
        ThaumcraftApi.addFakeCraftingRecipe(id("salismundusfake"), id("salismundus"));
        ThaumcraftApi.addFakeCraftingRecipe(id("viscrystalgroup"), id("vis_crystal"));
        // Each infusion enchantment shown on a sample tool.
        Object[][] ie = {{"burrowing", Items.WOODEN_PICKAXE}, {"collector", Items.STONE_AXE}, {"destructive", Items.STONE_PICKAXE},
                {"refining", Items.IRON_PICKAXE}, {"sounding", Items.GOLDEN_PICKAXE}, {"arcing", Items.WOODEN_SWORD},
                {"essence", Items.STONE_SWORD}, {"lamplight", Items.GOLDEN_PICKAXE}};
        for (Object[] e : ie) {
            ResourceLocation real = id("infusion/ie" + e[0]);
            ItemStack tool = new ItemStack((Item) e[1]);
            ThaumcraftApi.addFakeCraftingRecipe(id("ie" + e[0] + "fake"), (Function<RecipeManager, Object>) rm -> rm.byKey(real)
                    .map(h -> h.value() instanceof InfusionEnchantmentRecipe r ? new InfusionEnchantmentRecipe(r, tool) : null).orElse(null));
        }
    }

    private static BlockState pillar(Direction facing) {
        return ConfigBlocks.pillarArcane.get().defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, facing);
    }

    /** The crucible's water tank, so pipes and buckets from other mods can fill it (the original IFluidHandler). */
    private static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, TCBlockEntities.CRUCIBLE.get(), (te, side) -> te.tank);
        // The smeltery's sided inventory (ISidedInventory): items from the sides, fuel from below.
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, TCBlockEntities.SMELTER.get(),
                (te, side) -> side == null ? new InvWrapper(te) : new SidedInvWrapper(te, side));
        // The deconstruction table takes items from every side but the top.
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, TCBlockEntities.DECONSTRUCTION_TABLE.get(),
                (te, side) -> side == null ? new InvWrapper(te) : new SidedInvWrapper(te, side));
        // The magic mirror is an inventory: whatever goes in comes out of its twin.
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, TCBlockEntities.MIRROR.get(), (te, side) -> new InvWrapper(te));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, TCBlockEntities.THAUMATORIUM.get(),
                (te, side) -> side == null ? new InvWrapper(te) : new SidedInvWrapper(te, side));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, TCBlockEntities.THAUMATORIUM_TOP.get(),
                (te, side) -> te.getThaumatorium() == null ? null : side == null ? new InvWrapper(te) : new SidedInvWrapper(te, side));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, TCBlockEntities.GOLEM_BUILDER.get(),
                (te, side) -> side == null ? new InvWrapper(te) : new SidedInvWrapper(te, side));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, TCBlockEntities.VOID_SIPHON.get(),
                (te, side) -> side == null ? new InvWrapper(te) : new SidedInvWrapper(te, side));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, TCBlockEntities.INFERNAL_FURNACE.get(),
                (te, side) -> side == null ? new InvWrapper(te) : new SidedInvWrapper(te, side));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, TCBlockEntities.RECHARGE_PEDESTAL.get(),
                (te, side) -> side == null ? new InvWrapper(te) : new SidedInvWrapper(te, side));
        // The vis generator only feeds Forge Energy out of its FACING side.
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, TCBlockEntities.VIS_GENERATOR.get(),
                (te, side) -> side == null ? te : te.getEnergy(side));
        // Spa: the tank from every side, the bath salts from every side but the top.
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, TCBlockEntities.SPA.get(), (te, side) -> te.tank);
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, TCBlockEntities.SPA.get(),
                (te, side) -> side == null ? new InvWrapper(te) : new SidedInvWrapper(te, side));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, TCBlockEntities.POTION_SPRAYER.get(),
                (te, side) -> side == null ? new InvWrapper(te) : new SidedInvWrapper(te, side));
        // The everfull urn gives water from its top only.
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, TCBlockEntities.EVERFULL_URN.get(),
                (te, side) -> side == Direction.UP ? te.handler : null);
        // The hungry chest is a vanilla chest for pipes and hoppers.
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, TCBlockEntities.HUNGRY_CHEST.get(), (te, side) -> new InvWrapper(te));
    }

    private static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new ObjectAspects.ReloadListener());
    }

    /** Fires when a player joins and on every /reload: send item aspects. */
    private static void onDatapackSync(OnDatapackSyncEvent event) {
        if (event.getPlayer() == null) {
            // A /reload replaced the recipe manager: aspects generated from recipes must be rebuilt.
            MinecraftServer server = event.getPlayerList().getServer();
            ObjectAspects.setContext(server.getRecipeManager(), server.registryAccess(), server.potionBrewing());
        }
        event.getRelevantPlayers().forEach(TCNetwork::syncObjectAspects);
    }

    /** Item aspects are generated from recipes (ThaumcraftCraftingManager.generateTags), so they need them. */
    private static void onServerStarted(ServerStartedEvent event) {
        MinecraftServer server = event.getServer();
        ObjectAspects.setContext(server.getRecipeManager(), server.registryAccess(), server.potionBrewing());
    }

    private static void onTagsUpdated(TagsUpdatedEvent event) {
        ObjectAspects.invalidate();
    }

    private static void onRegisterCommands(RegisterCommandsEvent event) {
        ThaumcraftCommand.register(event.getDispatcher());
        thaumcraft.common.anomaly.AnomalyCommand.register(event.getDispatcher());
    }
}
