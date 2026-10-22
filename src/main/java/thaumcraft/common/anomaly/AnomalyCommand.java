package thaumcraft.common.anomaly;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

/**
 * /thaumcraft anomaly ... (merged into the /thaumcraft root):
 * <ul>
 * <li>status: time to the next one, cycle, tier and the active ones</li>
 * <li>list: every anomaly</li>
 * <li>start &lt;id&gt; [minutes]: starts one now</li>
 * <li>stop [id]: stops one, or all</li>
 * <li>skip: the next one comes now</li>
 * <li>timer &lt;minutes&gt;: time to the next one</li>
 * </ul>
 */
public final class AnomalyCommand {
    private AnomalyCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("thaumcraft")
                .then(Commands.literal("anomaly")
                        .requires(src -> src.hasPermission(2))
                        .then(Commands.literal("status").executes(ctx -> status(ctx.getSource())))
                        .then(Commands.literal("list").executes(ctx -> list(ctx.getSource())))
                        .then(Commands.literal("start")
                                .then(Commands.argument("id", StringArgumentType.word())
                                        .suggests((ctx, b) -> SharedSuggestionProvider.suggest(Anomalies.all().stream().map(t -> t.id), b))
                                        .executes(ctx -> start(ctx.getSource(), StringArgumentType.getString(ctx, "id"), -1))
                                        .then(Commands.argument("minutes", IntegerArgumentType.integer(1, 1440))
                                                .executes(ctx -> start(ctx.getSource(), StringArgumentType.getString(ctx, "id"),
                                                        IntegerArgumentType.getInteger(ctx, "minutes"))))))
                        .then(Commands.literal("stop")
                                .executes(ctx -> stop(ctx.getSource(), null))
                                .then(Commands.argument("id", StringArgumentType.word())
                                        .suggests((ctx, b) -> SharedSuggestionProvider.suggest(Anomalies.all().stream().map(t -> t.id), b))
                                        .executes(ctx -> stop(ctx.getSource(), StringArgumentType.getString(ctx, "id")))))
                        .then(Commands.literal("skip").executes(ctx -> timer(ctx.getSource(), 1)))
                        .then(Commands.literal("timer")
                                .then(Commands.argument("minutes", IntegerArgumentType.integer(1, 1440))
                                        .executes(ctx -> timer(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "minutes") * 1200))))));
    }

    private static AnomalyManager manager(CommandSourceStack src) {
        ServerLevel level = src.getLevel();
        if (!AnomalyManager.isEnabledIn(level)) {
            src.sendFailure(Component.translatable("commands.thaumcraft.anomaly.disabled"));
            return null;
        }
        return AnomalyManager.get(level);
    }

    private static String time(long ticks) {
        long s = ticks / 20L;
        return String.format("%d:%02d", s / 60L, s % 60L);
    }

    private static int status(CommandSourceStack src) {
        AnomalyManager m = manager(src);
        if (m == null) return 0;
        long now = src.getLevel().getGameTime();
        src.sendSuccess(() -> Component.translatable("commands.thaumcraft.anomaly.status", time(m.getRemaining(now)),
                m.isPaused() ? Component.translatable("commands.thaumcraft.anomaly.paused") : "",
                m.getCycle(), m.getTier(), m.getMaxActive()), false);
        for (ActiveAnomaly a : m.getActive()) {
            AnomalyType t = a.type();
            src.sendSuccess(() -> Component.literal(" - ").append(Component.translatable(t.getTranslationKey()).withColor(t.polarity.color))
                    .append(Component.translatable("commands.thaumcraft.anomaly.active", a.id(), time(a.end() - now))), false);
        }
        return 1;
    }

    private static int list(CommandSourceStack src) {
        for (AnomalyType t : Anomalies.all()) {
            src.sendSuccess(() -> Component.literal(t.id + " ").append(Component.translatable(t.getTranslationKey()).withColor(t.polarity.color))
                    .append(Component.translatable("commands.thaumcraft.anomaly.list_entry",
                            Component.translatable("tc.anomaly.polarity." + t.polarity.name().toLowerCase()), t.getWeight(), t.getMinTier())
                            .withStyle(ChatFormatting.GRAY)), false);
        }
        return Anomalies.all().size();
    }

    private static int start(CommandSourceStack src, String id, int minutes) {
        AnomalyManager m = manager(src);
        if (m == null) return 0;
        AnomalyType t = Anomalies.get(id);
        if (t == null) {
            src.sendFailure(Component.translatable("commands.thaumcraft.anomaly.unknown", id));
            return 0;
        }
        if (m.isActive(id)) {
            src.sendFailure(Component.translatable("commands.thaumcraft.anomaly.already_active", id));
            return 0;
        }
        m.start(src.getLevel(), t, src.getLevel().getGameTime(), minutes > 0 ? minutes * 1200 : -1);
        src.sendSuccess(() -> Component.translatable("commands.thaumcraft.anomaly.started", id), true);
        return 1;
    }

    private static int stop(CommandSourceStack src, String id) {
        AnomalyManager m = manager(src);
        if (m == null) return 0;
        int n = m.stop(src.getLevel(), id);
        src.sendSuccess(() -> Component.translatable(n == 1 ? "commands.thaumcraft.anomaly.stopped.one" : "commands.thaumcraft.anomaly.stopped", n), true);
        return n;
    }

    private static int timer(CommandSourceStack src, int ticks) {
        AnomalyManager m = manager(src);
        if (m == null) return 0;
        m.setTimer(src.getLevel(), ticks);
        src.sendSuccess(() -> Component.translatable("commands.thaumcraft.anomaly.timer", time(ticks)), true);
        return 1;
    }
}
