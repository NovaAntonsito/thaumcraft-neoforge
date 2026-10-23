package voidexpeditions.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import thaumcraft.Thaumcraft;
import thaumcraft.common.lib.SoundsTC;
import voidexpeditions.network.BuyPayload;
import voidexpeditions.network.ConfigureTerminalPayload;
import voidexpeditions.perk.Perk;
import voidexpeditions.perk.Perks;
import voidexpeditions.registry.VEAttachments;
import voidexpeditions.registry.VEItems;
import voidexpeditions.run.Difficulty;
import voidexpeditions.run.RunLength;
import voidexpeditions.run.RunState;
import voidexpeditions.shop.Shop;

/**
 * The expedition terminal, written on a sheet of Thaumcraft's research paper: the difficulty and length of the next
 * run (click to change, right click to go back), and the shop of void shards for it. Lines light up under the mouse
 * like ink still wet; the starting perk is picked with the arrows at its ends.
 */
public class TerminalScreen extends Screen {
    private static final ResourceLocation PAPER = Thaumcraft.id("textures/gui/paper.png");
    private static final ResourceLocation BOOK = Thaumcraft.id("textures/gui/gui_researchbook.png");
    private static final ResourceLocation BASE = Thaumcraft.id("textures/gui/gui_base.png");
    private static final int SHEET = 256;
    private static final int TEXT_W = 168;
    private static final int LINE = 14;
    private static final int INK = 0x2B1D12;
    private static final int INK_SOFT = 0x5A3E1B;
    private static final int INK_LIT = 0x7A1FA0;
    private static final int INK_DONE = 0x1F6B12;

    private final BlockPos pos;
    private Difficulty difficulty;
    private int length;
    private final List<Perk> startPerks = new ArrayList<>();
    private int perkIndex;
    private final List<Row> rows = new ArrayList<>();

    /** A clickable line of ink. */
    private record Row(int y, Supplier<Component> label, BooleanSupplier enabled, Runnable click, @Nullable Runnable back,
                       Supplier<Component> tooltip, boolean arrows) {
    }

    public TerminalScreen(BlockPos pos, Difficulty difficulty, int length) {
        super(Component.translatable("voidexpeditions.terminal.title"));
        this.pos = pos;
        this.difficulty = difficulty;
        this.length = length;
        for (Perk p : Perks.all()) {
            if (Shop.canStartWith(p)) startPerks.add(p);
        }
    }

    private int top() {
        return height / 2 - 96;
    }

    @Override
    protected void init() {
        rows.clear();
        int y = top() + 56;
        rows.add(new Row(y, () -> Component.translatable("voidexpeditions.terminal.difficulty", difficulty.displayName()),
                () -> true, () -> setDifficulty(1), () -> setDifficulty(-1),
                () -> Component.translatable("voidexpeditions.difficulty." + difficulty.name().toLowerCase(Locale.ROOT) + ".desc"),
                false));
        rows.add(new Row(y + LINE, () -> Component.translatable("voidexpeditions.terminal.length", length),
                () -> true, () -> setLength(1), () -> setLength(-1),
                () -> Component.translatable("voidexpeditions.terminal.length.desc"), false));
        int shop = y + LINE * 2 + 30;
        rows.add(new Row(shop, () -> shopLabel("voidexpeditions.shop.extra_life", Shop.EXTRA_LIFE, run().nextExtraLife),
                () -> !run().active && !run().nextExtraLife, () -> buy(Shop.Offer.EXTRA_LIFE), null,
                () -> Component.translatable("voidexpeditions.shop.extra_life.desc"), false));
        rows.add(new Row(shop + LINE, this::perkLabel, () -> !run().active && run().nextStartPerk.isEmpty(),
                () -> buy(Shop.Offer.START_PERK), null, this::perkTooltip, true));
        rows.add(new Row(shop + LINE * 2, () -> shopLabel("voidexpeditions.shop.focus", Shop.RANDOM_FOCUS, run().focusBought),
                () -> !run().active && !run().focusBought, () -> buy(Shop.Offer.RANDOM_FOCUS), null,
                () -> Component.translatable("voidexpeditions.shop.focus.desc"), false));
    }

    private RunState run() {
        return minecraft.player.getData(VEAttachments.RUN);
    }

    private void setDifficulty(int step) {
        difficulty = Difficulty.byIndex(difficulty.ordinal() + step);
        configure();
    }

    private void setLength(int step) {
        int n = length;
        for (int i = 0; i < Math.floorMod(step, RunLength.OPTIONS.length); i++) n = RunLength.next(n);
        length = n;
        configure();
    }

    private void configure() {
        PacketDistributor.sendToServer(new ConfigureTerminalPayload(pos, difficulty.ordinal(), length));
    }

    private void buy(Shop.Offer offer) {
        String perk = startPerks.isEmpty() ? "" : startPerks.get(perkIndex).id();
        PacketDistributor.sendToServer(new BuyPayload(pos, offer.ordinal(), perk));
    }

    private Component shopLabel(String key, int price, boolean owned) {
        if (owned) return Component.translatable("voidexpeditions.shop.owned", Component.translatable(key));
        return Component.translatable("voidexpeditions.shop.priced", Component.translatable(key), price);
    }

    private Component perkLabel() {
        RunState run = run();
        Perk owned = run.nextStartPerk.isEmpty() ? null : Perks.get(run.nextStartPerk);
        if (owned != null) return Component.translatable("voidexpeditions.shop.owned", plain(owned));
        if (startPerks.isEmpty()) return Component.empty();
        return Component.translatable("voidexpeditions.shop.priced", plain(startPerks.get(perkIndex)), Shop.START_PERK);
    }

    private Component perkTooltip() {
        Perk p = startPerks.isEmpty() ? null : startPerks.get(perkIndex);
        return p == null ? Component.empty() : Component.translatable("voidexpeditions.shop.start_perk.desc", p.description());
    }

    /** A perk's name without its colour, which would not read as ink. */
    private static Component plain(Perk perk) {
        return Component.translatable("voidexpeditions.perk." + perk.id());
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        int cx = width / 2;
        int top = top();
        RenderSystem.enableBlend();
        g.blit(PAPER, cx - SHEET / 2, top - 24, 0, 0.0f, 0.0f, SHEET, SHEET, SHEET, SHEET);

        centered(g, title.copy().withStyle(ChatFormatting.BOLD), top + 6, INK_LIT);
        long today = minecraft.level.getDayTime() / 24000L;
        centered(g, Component.translatable("voidexpeditions.terminal.runs_left", Math.max(0, run().runsLeft(today))), top + 20, INK_SOFT);
        centered(g, Component.translatable("voidexpeditions.terminal.hint"), top + 32, INK_SOFT);
        rule(g, top + 46);

        seal(g, cx + 66, top + 186);

        int shopTop = rows.get(2).y() - 22;
        rule(g, shopTop - 4);
        int shards = minecraft.player.getInventory().countItem(VEItems.VOID_SHARD.get());
        Component shopTitle = Component.translatable("voidexpeditions.shop.title", shards).withStyle(ChatFormatting.BOLD);
        int tw = font.width(shopTitle);
        g.renderItem(new ItemStack(VEItems.VOID_SHARD.get()), cx - tw / 2 - 18, shopTop + 1);
        g.drawString(font, shopTitle, cx - tw / 2, shopTop + 5, INK_LIT, false);

        Row hovered = null;
        for (Row row : rows) {
            boolean enabled = row.enabled().getAsBoolean();
            boolean over = enabled && Math.abs(mouseX - cx) < TEXT_W / 2 && mouseY >= row.y() - 2 && mouseY < row.y() + 10;
            if (over) hovered = row;
            Component label = row.label().get();
            int color = !enabled ? (owned(label) ? INK_DONE : INK_SOFT) : over ? INK_LIT : INK;
            centered(g, label, row.y(), color);
            if (row.arrows() && enabled) {
                g.drawString(font, "‹", cx - TEXT_W / 2, row.y(), over ? INK_LIT : INK, false);
                g.drawString(font, "›", cx + TEXT_W / 2 - font.width("›"), row.y(), over ? INK_LIT : INK, false);
            }
        }
        if (hovered != null) {
            g.renderTooltip(font, font.split(hovered.tooltip().get(), 200), mouseX, mouseY);
        }
    }

    /** Bought lines read as done, in green ink. */
    private static boolean owned(Component label) {
        return label.getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents t
                && "voidexpeditions.shop.owned".equals(t.getKey());
    }

    private void centered(GuiGraphics g, Component text, int y, int color) {
        g.drawString(font, text, width / 2 - font.width(text) / 2, y, color, false);
    }

    /** The thaumonomicon's ink rule. */
    private void rule(GuiGraphics g, int y) {
        RenderSystem.enableBlend();
        g.blit(BOOK, width / 2 - 48, y - 2, 24, 184, 96, 4);
    }

    /** The red wax seal of gui_base.png, a quarter size, with the gate pressed into it. */
    private void seal(GuiGraphics g, int x, int y) {
        var pose = g.pose();
        pose.pushPose();
        pose.translate(x, y, 0);
        pose.scale(0.25f, 0.25f, 1.0f);
        RenderSystem.enableBlend();
        g.blit(BASE, -80, -80, 96, 0, 160, 160);
        pose.popPose();
        g.renderItem(new ItemStack(VEItems.VOID_GATE_RING.get()), x - 8, y - 8);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int cx = width / 2;
        for (Row row : rows) {
            if (!row.enabled().getAsBoolean() || Math.abs(mouseX - cx) >= TEXT_W / 2 || mouseY < row.y() - 2 || mouseY >= row.y() + 10) {
                continue;
            }
            if (row.arrows() && Math.abs(mouseX - cx) > TEXT_W / 2 - 14) {
                perkIndex = Math.floorMod(perkIndex + (mouseX < cx ? -1 : 1), startPerks.size());
            } else if (button == 1 && row.back() != null) {
                row.back().run();
            } else {
                row.click().run();
            }
            if (minecraft.player != null) minecraft.player.playSound(SoundsTC.PAGETURN.get(), 0.6f, 1.0f);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
