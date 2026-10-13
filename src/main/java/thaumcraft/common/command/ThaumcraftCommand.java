package thaumcraft.common.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import net.minecraft.world.entity.player.Player;
import thaumcraft.api.capabilities.PlayerKnowledge;
import thaumcraft.api.capabilities.ThaumcraftCapabilities;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.api.research.ResearchCategory;
import thaumcraft.api.research.ResearchEntry;
import thaumcraft.api.research.ResearchStage;
import thaumcraft.common.lib.network.playerdata.PacketSyncKnowledge;
import thaumcraft.common.lib.research.ResearchManager;
import thaumcraft.common.lib.research.ResearchNoteManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import java.util.Map;
import java.util.function.Supplier;
import thaumcraft.common.config.ConfigBlocks;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectHelper;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aura.AuraHelper;
import thaumcraft.api.capabilities.PlayerWarp;
import thaumcraft.api.capabilities.PlayerWarp.EnumWarpType;
import thaumcraft.common.registry.TCAttachments;
import net.minecraft.ChatFormatting;
import net.neoforged.neoforge.network.PacketDistributor;
import thaumcraft.api.capabilities.PlayerAspects;
import thaumcraft.common.lib.network.playerdata.PacketAspectPool;

/**
 * Debug commands:
 * <ul>
 * <li>/thaumcraft aspects: aspects of the held item, and reduced to primals.</li>
 * <li>/thaumcraft aura: vis, flux and base of the current chunk.</li>
 * <li>/thaumcraft aura add|drain vis|flux &lt;amount&gt;</li>
 * <li>/thaumcraft warp get [player]</li>
 * <li>/thaumcraft warp add &lt;player&gt; &lt;permanent|normal|temporary&gt; &lt;amount&gt;</li>
 * <li>/thaumcraft findore &lt;amber|cinnabar|quartz&gt; [radius in chunks]: teleports to the nearest one.</li>
 * <li>/thaumcraft research list | &lt;player&gt; &lt;list|all|reset|&lt;research&gt;&gt; | &lt;player&gt; revoke &lt;research&gt; (original)</li>
 * <li>/thaumcraft reload: re-reads the research files (original)</li>
 * <li>/thaumcraft aspects &lt;player&gt; research &lt;aspect|all&gt; | add|set &lt;aspect|all&gt; &lt;amount&gt;: the aspect pool (Reborn's /oldresearch)</li>
 * </ul>
 */
public final class ThaumcraftCommand {
    private ThaumcraftCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("thaumcraft")
                .then(Commands.literal("findore")
                        .requires(src -> src.hasPermission(2))
                        .then(Commands.argument("ore", StringArgumentType.word())
                                .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(ORES.keySet(), builder))
                                .executes(ctx -> findOre(ctx.getSource(), StringArgumentType.getString(ctx, "ore"), 8))
                                .then(Commands.argument("radius", IntegerArgumentType.integer(1, 32))
                                        .executes(ctx -> findOre(ctx.getSource(), StringArgumentType.getString(ctx, "ore"),
                                                IntegerArgumentType.getInteger(ctx, "radius"))))))
                .then(Commands.literal("aspects")
                        .executes(ctx -> showHeldAspects(ctx.getSource())))
                .then(Commands.literal("aura")
                        .executes(ctx -> showAura(ctx.getSource()))
                        .then(Commands.literal("add")
                                .requires(src -> src.hasPermission(2))
                                .then(Commands.literal("vis").then(Commands.argument("amount", FloatArgumentType.floatArg(0))
                                        .executes(ctx -> changeAura(ctx.getSource(), true, true, FloatArgumentType.getFloat(ctx, "amount")))))
                                .then(Commands.literal("flux").then(Commands.argument("amount", FloatArgumentType.floatArg(0))
                                        .executes(ctx -> changeAura(ctx.getSource(), false, true, FloatArgumentType.getFloat(ctx, "amount"))))))
                        .then(Commands.literal("drain")
                                .requires(src -> src.hasPermission(2))
                                .then(Commands.literal("vis").then(Commands.argument("amount", FloatArgumentType.floatArg(0))
                                        .executes(ctx -> changeAura(ctx.getSource(), true, false, FloatArgumentType.getFloat(ctx, "amount")))))
                                .then(Commands.literal("flux").then(Commands.argument("amount", FloatArgumentType.floatArg(0))
                                        .executes(ctx -> changeAura(ctx.getSource(), false, false, FloatArgumentType.getFloat(ctx, "amount")))))))
                .then(Commands.literal("warp")
                        .then(Commands.literal("get")
                                .executes(ctx -> showWarp(ctx.getSource(), ctx.getSource().getPlayerOrException()))
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(ctx -> showWarp(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))))
                        .then(Commands.literal("add")
                                .requires(src -> src.hasPermission(2))
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("type", StringArgumentType.word())
                                                .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(
                                                        java.util.Arrays.stream(EnumWarpType.values()).map(EnumWarpType::getSerializedName), builder))
                                                .then(Commands.argument("amount", IntegerArgumentType.integer())
                                                        .executes(ctx -> addWarp(ctx.getSource(),
                                                                EntityArgument.getPlayer(ctx, "player"),
                                                                StringArgumentType.getString(ctx, "type"),
                                                                IntegerArgumentType.getInteger(ctx, "amount"))))))))
                .then(Commands.literal("reload")
                        .requires(src -> src.hasPermission(2))
                        .executes(ctx -> reloadResearch(ctx.getSource())))
                .then(Commands.literal("research")
                        .requires(src -> src.hasPermission(2))
                        .then(Commands.literal("list")
                                .executes(ctx -> listResearch(ctx.getSource())))
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.literal("list")
                                        .executes(ctx -> listAllResearch(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"))))
                                .then(Commands.literal("all")
                                        .executes(ctx -> giveAllResearch(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"))))
                                .then(Commands.literal("reset")
                                        .executes(ctx -> resetResearch(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"))))
                                .then(Commands.literal("revoke")
                                        .then(Commands.argument("research", StringArgumentType.greedyString())
                                                .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(researchKeys(), builder))
                                                .executes(ctx -> revokeResearch(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"),
                                                        StringArgumentType.getString(ctx, "research")))))
                                .then(Commands.argument("research", StringArgumentType.greedyString())
                                        .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(researchKeys(), builder))
                                        .executes(ctx -> giveResearch(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"),
                                                StringArgumentType.getString(ctx, "research"))))))
                // Reborn's /oldresearch researchaspect|addaspect|setaspect, as /thaumcraft aspects <player> research|add|set.
                // Same literal as the held-item aspects above (Brigadier merges them), so the permission goes on the player node.
                .then(Commands.literal("aspects")
                        .then(Commands.argument("player", EntityArgument.player())
                                .requires(src -> src.hasPermission(2))
                                .then(Commands.literal("research")
                                        .then(Commands.argument("aspect", StringArgumentType.word())
                                                .suggests(ThaumcraftCommand::suggestAspects)
                                                .executes(ctx -> researchAspect(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"),
                                                        StringArgumentType.getString(ctx, "aspect")))))
                                .then(Commands.literal("add")
                                        .then(Commands.argument("aspect", StringArgumentType.word())
                                                .suggests(ThaumcraftCommand::suggestAspects)
                                                .then(Commands.argument("amount", IntegerArgumentType.integer())
                                                        .executes(ctx -> addAspect(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"),
                                                                StringArgumentType.getString(ctx, "aspect"), IntegerArgumentType.getInteger(ctx, "amount"), false)))))
                                .then(Commands.literal("set")
                                        .then(Commands.argument("aspect", StringArgumentType.word())
                                                .suggests(ThaumcraftCommand::suggestAspects)
                                                .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                                        .executes(ctx -> addAspect(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"),
                                                                StringArgumentType.getString(ctx, "aspect"), IntegerArgumentType.getInteger(ctx, "amount"), true))))))));
    }

    private static List<String> researchKeys() {
        List<String> out = new ArrayList<>();
        for (ResearchCategory cat : ResearchCategories.researchCategories.values()) out.addAll(cat.research.keySet());
        return out;
    }

    private static int reloadResearch(CommandSourceStack source) {
        for (ResearchCategory rc : ResearchCategories.researchCategories.values()) rc.research.clear();
        ResearchManager.parseAllResearch();
        ResearchNoteManager.patchResearch();
        source.sendSuccess(() -> Component.literal("\u00a75Success!"), true);
        return 1;
    }

    private static int listResearch(CommandSourceStack source) {
        for (String key : researchKeys()) source.sendSystemMessage(Component.literal("\u00a75" + key));
        return 1;
    }

    private static int giveResearch(CommandSourceStack source, ServerPlayer player, String research) {
        if (ResearchCategories.getResearch(research) == null) {
            source.sendFailure(Component.literal("Research does not exist."));
            return 0;
        }
        giveRecursiveResearch(player, research);
        PacketSyncKnowledge.sync(player);
        player.sendSystemMessage(Component.literal("\u00a75" + source.getTextName() + " gave you " + research + " research and its requisites."));
        source.sendSuccess(() -> Component.literal("\u00a75Success!"), true);
        return 1;
    }

    public static void giveRecursiveResearch(Player player, String research) {
        if (research.contains("@")) research = research.substring(0, research.indexOf("@"));
        ResearchEntry res = ResearchCategories.getResearch(research);
        PlayerKnowledge knowledge = ThaumcraftCapabilities.getKnowledge(player);
        if (knowledge.isResearchComplete(research)) return;
        if (res != null && res.getParents() != null) {
            for (String rsi : res.getParentsStripped()) giveRecursiveResearch(player, rsi);
        }
        if (res != null && res.getStages() != null) {
            for (ResearchStage page : res.getStages()) {
                if (page.getResearch() != null) {
                    for (String gr : page.getResearch()) ResearchManager.completeResearch(player, gr);
                }
            }
        }
        ResearchManager.completeResearch(player, research);
        for (ResearchCategory rc : ResearchCategories.researchCategories.values()) {
            for (ResearchEntry ri : rc.research.values()) {
                if (ri.getStages() == null) continue;
                for (ResearchStage stage : ri.getStages()) {
                    if (stage.getResearch() != null && Arrays.asList(stage.getResearch()).contains(research)) {
                        knowledge.setResearchFlag(ri.getKey(), PlayerKnowledge.EnumResearchFlag.PAGE);
                        break;
                    }
                }
            }
        }
        if (res != null && res.getSiblings() != null) {
            for (String rsi : res.getSiblings()) giveRecursiveResearch(player, rsi);
        }
    }

    private static int revokeResearch(CommandSourceStack source, ServerPlayer player, String research) {
        if (ResearchCategories.getResearch(research) == null) {
            source.sendFailure(Component.literal("Research does not exist."));
            return 0;
        }
        revokeRecursiveResearch(player, research);
        PacketSyncKnowledge.sync(player);
        player.sendSystemMessage(Component.literal("\u00a75" + source.getTextName() + " removed " + research + " research and its children."));
        source.sendSuccess(() -> Component.literal("\u00a75Success!"), true);
        return 1;
    }

    public static void revokeRecursiveResearch(Player player, String research) {
        if (research.contains("@")) research = research.substring(0, research.indexOf("@"));
        PlayerKnowledge knowledge = ThaumcraftCapabilities.getKnowledge(player);
        if (!knowledge.isResearchComplete(research)) return;
        for (ResearchCategory rc : ResearchCategories.researchCategories.values()) {
            for (ResearchEntry ri : List.copyOf(rc.research.values())) {
                if (ri.getParents() != null && knowledge.isResearchComplete(ri.getKey())) {
                    for (String rsi : ri.getParentsStripped()) {
                        if (rsi.equals(research)) revokeRecursiveResearch(player, ri.getKey());
                    }
                }
            }
        }
        knowledge.removeResearch(research);
    }

    private static int listAllResearch(CommandSourceStack source, ServerPlayer player) {
        String ss = String.join(", ", ThaumcraftCapabilities.getKnowledge(player).getResearchList());
        source.sendSystemMessage(Component.literal("\u00a75Research for " + player.getName().getString()));
        source.sendSystemMessage(Component.literal("\u00a75" + ss));
        return 1;
    }

    private static int giveAllResearch(CommandSourceStack source, ServerPlayer player) {
        for (String key : researchKeys()) giveRecursiveResearch(player, key);
        player.sendSystemMessage(Component.literal("\u00a75" + source.getTextName() + " has given you all research."));
        source.sendSuccess(() -> Component.literal("\u00a75Success!"), true);
        PacketSyncKnowledge.sync(player);
        return 1;
    }

    private static int resetResearch(CommandSourceStack source, ServerPlayer player) {
        ThaumcraftCapabilities.getKnowledge(player).clear();
        for (ResearchCategory cat : ResearchCategories.researchCategories.values()) {
            for (ResearchEntry ri : cat.research.values()) {
                if (ri.hasMeta(ResearchEntry.EnumResearchMeta.AUTOUNLOCK)) ResearchManager.completeResearch(player, ri.getKey(), false);
            }
        }
        player.sendSystemMessage(Component.literal("\u00a75" + source.getTextName() + " has reset all your research."));
        source.sendSuccess(() -> Component.literal("\u00a75Success!"), true);
        PacketSyncKnowledge.sync(player);
        return 1;
    }

    private static java.util.concurrent.CompletableFuture<com.mojang.brigadier.suggestion.Suggestions> suggestAspects(
            com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx, com.mojang.brigadier.suggestion.SuggestionsBuilder builder) {
        List<String> list = new ArrayList<>(Aspect.aspects.keySet());
        list.add("all");
        return SharedSuggestionProvider.suggest(list, builder);
    }

    /** "all", or one aspect; null (with a failure message) for an unknown name. */
    private static List<Aspect> parseAspects(CommandSourceStack source, String name) {
        if (name.equals("all")) return new ArrayList<>(Aspect.aspects.values());
        Aspect aspect = Aspect.getAspect(name);
        if (aspect == null) {
            source.sendFailure(Component.literal("Invalid aspect name"));
            return null;
        }
        return List.of(aspect);
    }

    private static int researchAspect(CommandSourceStack source, ServerPlayer player, String name) {
        List<Aspect> aspects = parseAspects(source, name);
        if (aspects == null) return 0;
        PlayerAspects pool = ThaumcraftCapabilities.getAspects(player);
        int n = 0;
        for (Aspect aspect : aspects) {
            if (pool.researchAspect(aspect)) n++;
        }
        player.syncData(TCAttachments.ASPECTS);
        int count = n;
        source.sendSuccess(() -> Component.literal("Successfully researched " + count + " aspects").withStyle(ChatFormatting.GREEN), true);
        return n;
    }

    /** addaspect (shown to the player as a gain) or setaspect. */
    private static int addAspect(CommandSourceStack source, ServerPlayer player, String name, int amount, boolean set) {
        List<Aspect> aspects = parseAspects(source, name);
        if (aspects == null) return 0;
        PlayerAspects pool = ThaumcraftCapabilities.getAspects(player);
        for (Aspect aspect : aspects) {
            if (set) {
                pool.setAspectCount(aspect, amount);
            } else {
                pool.addToAspectPool(aspect, amount);
                PacketDistributor.sendToPlayer(player, new PacketAspectPool(aspect.getTag(), amount, pool.aspectCount(aspect)));
            }
        }
        if (set) player.syncData(TCAttachments.ASPECTS);
        source.sendSuccess(() -> Component.literal("Successfully").withStyle(ChatFormatting.GREEN), true);
        return 1;
    }

    private static final Map<String, Supplier<Block>> ORES = Map.of(
            "amber", ConfigBlocks.oreAmber,
            "cinnabar", ConfigBlocks.oreCinnabar,
            "quartz", ConfigBlocks.oreQuartz);

    /** Scans the chunks around the player (generating them if needed) and teleports next to the closest ore. */
    private static int findOre(CommandSourceStack source, String name, int radius) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        Supplier<Block> ore = ORES.get(name);
        if (ore == null) {
            source.sendFailure(Component.translatable("commands.thaumcraft.findore.unknown", name));
            return 0;
        }
        Block block = ore.get();
        ServerLevel level = player.serverLevel();
        BlockPos origin = player.blockPosition();
        ChunkPos center = new ChunkPos(origin);
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;
        int found = 0;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int cx = center.x - radius; cx <= center.x + radius; cx++) {
            for (int cz = center.z - radius; cz <= center.z + radius; cz++) {
                LevelChunk chunk = level.getChunk(cx, cz);
                LevelChunkSection[] sections = chunk.getSections();
                for (int i = 0; i < sections.length; i++) {
                    LevelChunkSection section = sections[i];
                    if (section.hasOnlyAir() || !section.maybeHas(state -> state.is(block))) continue;
                    int baseY = SectionPos.sectionToBlockCoord(chunk.getSectionYFromSectionIndex(i));
                    for (int y = 0; y < 16; y++) for (int z = 0; z < 16; z++) for (int x = 0; x < 16; x++) {
                        if (!section.getBlockState(x, y, z).is(block)) continue;
                        found++;
                        pos.set(chunk.getPos().getMinBlockX() + x, baseY + y, chunk.getPos().getMinBlockZ() + z);
                        double d = pos.distSqr(origin);
                        if (d < bestDist) {
                            bestDist = d;
                            best = pos.immutable();
                        }
                    }
                }
            }
        }
        if (best == null) {
            source.sendFailure(Component.translatable("commands.thaumcraft.findore.none", block.getName(), radius));
            return 0;
        }
        BlockPos target = findStandingSpot(level, best);
        player.teleportTo(level, target.getX() + 0.5, target.getY(), target.getZ() + 0.5, player.getYRot(), player.getXRot());
        BlockPos orePos = best;
        int total = found;
        source.sendSuccess(() -> Component.translatable("commands.thaumcraft.findore.found", block.getName(),
                orePos.getX(), orePos.getY(), orePos.getZ(), total), false);
        return total;
    }

    /** A 2-block-high air space near the ore, so the player does not suffocate. Falls back to the ore itself. */
    private static BlockPos findStandingSpot(ServerLevel level, BlockPos ore) {
        for (int r = 1; r <= 4; r++) {
            for (BlockPos p : BlockPos.betweenClosed(ore.offset(-r, -r, -r), ore.offset(r, r, r))) {
                if (level.getBlockState(p).isAir() && level.getBlockState(p.above()).isAir()
                        && !level.getBlockState(p.below()).isAir()) {
                    return p.immutable();
                }
            }
        }
        return ore;
    }

    private static int showHeldAspects(CommandSourceStack source) throws CommandSyntaxException {
        ItemStack stack = source.getPlayerOrException().getMainHandItem();
        AspectList aspects = AspectHelper.getObjectAspects(stack);
        if (aspects.isEmpty()) {
            source.sendFailure(Component.translatable("commands.thaumcraft.aspects.none", stack.getHoverName()));
            return 0;
        }
        source.sendSuccess(() -> Component.translatable("commands.thaumcraft.aspects.header", stack.getHoverName()).append(" ").append(format(aspects)), false);
        source.sendSuccess(() -> Component.translatable("commands.thaumcraft.aspects.primals").append(" ").append(format(AspectHelper.reduceToPrimals(aspects))), false);
        return aspects.size();
    }

    private static MutableComponent format(AspectList list) {
        MutableComponent out = Component.empty();
        boolean first = true;
        for (Aspect aspect : list.getAspectsSortedByAmount()) {
            if (!first) out.append(", ");
            out.append(aspect.getDisplayName()).append(" " + list.getAmount(aspect));
            first = false;
        }
        return out;
    }

    private static int showAura(CommandSourceStack source) {
        ServerLevel level = source.getLevel();
        BlockPos pos = BlockPos.containing(source.getPosition());
        String vis = String.format("%.1f", AuraHelper.getVis(level, pos));
        String flux = String.format("%.1f", AuraHelper.getFlux(level, pos));
        int base = AuraHelper.getAuraBase(level, pos);
        source.sendSuccess(() -> Component.translatable("commands.thaumcraft.aura.get", pos.getX() >> 4, pos.getZ() >> 4, vis, flux, base), false);
        return (int) AuraHelper.getVis(level, pos);
    }

    private static int changeAura(CommandSourceStack source, boolean vis, boolean add, float amount) {
        ServerLevel level = source.getLevel();
        BlockPos pos = BlockPos.containing(source.getPosition());
        if (vis && add) AuraHelper.addVis(level, pos, amount);
        else if (vis) AuraHelper.drainVis(level, pos, amount, false);
        else if (add) AuraHelper.polluteAura(level, pos, amount, false);
        else AuraHelper.drainFlux(level, pos, amount, false);
        return showAura(source);
    }

    private static int showWarp(CommandSourceStack source, ServerPlayer player) {
        PlayerWarp warp = player.getData(TCAttachments.WARP);
        source.sendSuccess(() -> Component.translatable("commands.thaumcraft.warp.get", player.getDisplayName(),
                warp.get(EnumWarpType.PERMANENT), warp.get(EnumWarpType.NORMAL), warp.get(EnumWarpType.TEMPORARY), warp.getTotal()), false);
        return warp.getTotal();
    }

    private static int addWarp(CommandSourceStack source, ServerPlayer player, String typeName, int amount) {
        EnumWarpType type = null;
        for (EnumWarpType t : EnumWarpType.values()) if (t.getSerializedName().equals(typeName)) type = t;
        if (type == null) {
            source.sendFailure(Component.translatable("commands.thaumcraft.warp.bad_type", typeName));
            return 0;
        }
        PlayerWarp warp = player.getData(TCAttachments.WARP);
        warp.add(type, amount);
        player.syncData(TCAttachments.WARP);
        return showWarp(source, player);
    }
}
