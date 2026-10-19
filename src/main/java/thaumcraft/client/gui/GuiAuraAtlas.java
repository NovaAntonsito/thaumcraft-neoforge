package thaumcraft.client.gui;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.nodes.NodeModifier;
import thaumcraft.api.nodes.NodeType;
import thaumcraft.common.anomaly.AnomalyClientState;
import thaumcraft.common.anomaly.AnomalyType;
import thaumcraft.common.anomaly.Anomalies;
import thaumcraft.common.anomaly.PacketAnomalySync;
import thaumcraft.common.items.tools.ItemAuraAtlas;
import thaumcraft.common.lib.network.misc.PacketAuraAtlasRequestToServer;
import thaumcraft.common.lib.network.misc.PacketAuraAtlasToClient;
import thaumcraft.common.tiles.misc.TileAuraNode;

/**
 * Not in Thaumcraft 4 (dream branch): the aura atlas. A square of chunks around the player on a sheet of paper, each
 * tinted by its vis (violet) and flux (dark purple). Known nodes are drawn with the core of the node renderer in the
 * color of their strongest aspect, local anomalies as a star in the color of their kind. Hovering shows the numbers.
 */
public class GuiAuraAtlas extends Screen {
    private static final ResourceLocation PAPER = Thaumcraft.id("textures/gui/paper.png");
    private static final ResourceLocation NODES = Thaumcraft.id("textures/misc/nodes.png");
    private static final ResourceLocation PLAYER = ResourceLocation.withDefaultNamespace("textures/map/decorations/player.png");
    private static final int CELL = 12;
    private static final int VIS_COLOR = 0x9A60D0;
    private static final int FLUX_COLOR = 0x400A40;

    private PacketAuraAtlasToClient data;
    private int ticks;

    public GuiAuraAtlas(PacketAuraAtlasToClient data) {
        super(Component.translatable("item.thaumcraft.aura_atlas"));
        this.data = data;
    }

    public void update(PacketAuraAtlasToClient data) {
        this.data = data;
    }

    @Override
    public void tick() {
        Player player = Minecraft.getInstance().player;
        if (player == null || !ItemAuraAtlas.isHolding(player)) {
            onClose();
            return;
        }
        if (++ticks % 40 == 0) PacketDistributor.sendToServer(PacketAuraAtlasRequestToServer.INSTANCE);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private int side() {
        return (data.radius() * 2 + 1) * CELL;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTicks) {
        super.render(g, mouseX, mouseY, partialTicks);
        int side = side();
        int paper = side + 40;
        int px = (width - paper) / 2;
        int py = (height - paper) / 2;
        RenderSystem.enableBlend();
        g.blit(PAPER, px, py, paper, paper, 0, 0, 256, 256, 256, 256);
        g.drawCenteredString(font, title, width / 2, py + 8, 0x40206A);
        int gx = px + 20;
        int gy = py + 22;
        int n = data.radius() * 2 + 1;
        List<Component> tooltip = null;

        for (int z = 0; z < n; ++z) {
            for (int x = 0; x < n; ++x) {
                PacketAuraAtlasToClient.Cell c = data.cells().get(z * n + x);
                int cx = gx + x * CELL;
                int cy = gy + z * CELL;
                if (c.base() < 0) {
                    g.fill(cx, cy, cx + CELL, cy + CELL, 0x20000000);
                } else {
                    float base = Math.max(1, c.base());
                    int va = (int) (Mth.clamp(c.vis() / base, 0.0f, 1.5f) / 1.5f * 200.0f);
                    g.fill(cx, cy, cx + CELL, cy + CELL, (va << 24) | VIS_COLOR);
                    int fa = (int) (Mth.clamp(c.flux() / base, 0.0f, 1.0f) * 200.0f);
                    if (fa > 0) g.fill(cx, cy, cx + CELL, cy + CELL, (fa << 24) | FLUX_COLOR);
                }
                if (mouseX >= cx && mouseX < cx + CELL && mouseY >= cy && mouseY < cy + CELL) {
                    tooltip = cellTooltip(data.cx() - data.radius() + x, data.cz() - data.radius() + z, c);
                }
            }
        }
        // The chunk grid lines.
        for (int i = 0; i <= n; ++i) {
            g.fill(gx + i * CELL, gy, gx + i * CELL + 1, gy + side, 0x30402060);
            g.fill(gx, gy + i * CELL, gx + side, gy + i * CELL + 1, 0x30402060);
        }

        int ox = (data.cx() - data.radius()) * 16;
        int oz = (data.cz() - data.radius()) * 16;
        float scale = CELL / 16.0f;
        long frame = (System.nanoTime() / 40000000L) % 32;
        for (PacketAuraAtlasToClient.Node node : data.nodes()) {
            int sx = gx + (int) ((node.pos().getX() - ox + 0.5f) * scale);
            int sy = gy + (int) ((node.pos().getZ() - oz + 0.5f) * scale);
            NodeType type = NodeType.byId(node.type());
            int color = 0xFFFFFF;
            int best = -1;
            for (Aspect a : node.aspects().getAspects()) {
                if (node.aspects().getAmount(a) > best) {
                    best = node.aspects().getAmount(a);
                    color = a.getColor();
                }
            }
            int size = node.energized() ? 14 : 10;
            g.setColor(((color >> 16) & 0xFF) / 255f, ((color >> 8) & 0xFF) / 255f, (color & 0xFF) / 255f, 1.0f);
            // A frame of the node's core strip in nodes.png, the same one the node renderer draws.
            int strip = switch (type) {
                case UNSTABLE -> 6;
                case DARK -> 2;
                case TAINTED -> 5;
                case PURE -> 4;
                case HUNGRY -> 3;
                default -> 1;
            };
            g.blit(NODES, sx - size / 2, sy - size / 2, size, size, frame * 64.0f, strip * 64.0f, 64, 64, 2048, 2048);
            g.setColor(1, 1, 1, 1);
            if (Math.abs(mouseX - sx) <= size / 2 && Math.abs(mouseY - sy) <= size / 2) {
                tooltip = nodeTooltip(node, type);
            }
        }

        for (PacketAnomalySync.Entry e : AnomalyClientState.active) {
            if (!e.local()) continue;
            int sx = gx + (int) ((e.x() - ox) * scale);
            int sy = gy + (int) ((e.z() - oz) * scale);
            if (sx < gx || sy < gy || sx >= gx + side || sy >= gy + side) continue;
            AnomalyType type = Anomalies.get(e.id());
            int color = type != null ? type.polarity.color : 0xC8B4E0;
            String star = "✦";
            g.drawString(font, star, sx - font.width(star) / 2, sy - 4, 0xFF000000 | color, true);
            if (Math.abs(mouseX - sx) <= 5 && Math.abs(mouseY - sy) <= 5) {
                tooltip = List.of(Component.translatable(type != null ? type.getTranslationKey() : e.id()).withStyle(ChatFormatting.GOLD),
                        Component.translatable("tc.atlas.anomaly", time(AnomalyClientState.remainingNow(e, minecraft.level.getGameTime())))
                                .withStyle(ChatFormatting.GRAY));
            }
        }

        Player player = Minecraft.getInstance().player;
        if (player != null) {
            float sx = gx + (float) (player.getX() - ox) * scale;
            float sy = gy + (float) (player.getZ() - oz) * scale;
            g.pose().pushPose();
            g.pose().translate(sx, sy, 0);
            g.pose().mulPose(Axis.ZP.rotationDegrees(player.getYRot() + 180.0f));
            g.blit(PLAYER, -4, -4, 8, 8, 0, 0, 8, 8, 8, 8);
            g.pose().popPose();
        }

        g.drawCenteredString(font, Component.translatable("tc.atlas.legend"), width / 2, gy + side + 5, 0x6A4A80);
        if (tooltip != null) g.renderComponentTooltip(font, tooltip, mouseX, mouseY);
    }

    private static List<Component> cellTooltip(int chunkX, int chunkZ, PacketAuraAtlasToClient.Cell c) {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable("tc.atlas.chunk", chunkX * 16 + 8, chunkZ * 16 + 8).withStyle(ChatFormatting.GRAY));
        if (c.base() < 0) {
            lines.add(Component.translatable("tc.atlas.unknown").withStyle(ChatFormatting.DARK_GRAY));
        } else {
            lines.add(Component.translatable("tc.atlas.vis", String.format("%.1f", c.vis()), c.base()).withStyle(ChatFormatting.LIGHT_PURPLE));
            lines.add(Component.translatable("tc.atlas.flux", String.format("%.1f", c.flux())).withStyle(ChatFormatting.DARK_PURPLE));
        }
        return lines;
    }

    private static List<Component> nodeTooltip(PacketAuraAtlasToClient.Node node, NodeType type) {
        List<Component> lines = new ArrayList<>(TileAuraNode.describe(type, NodeModifier.byId(node.modifier())));
        if (node.energized()) lines.add(Component.translatable("tc.atlas.energized").withStyle(ChatFormatting.GOLD));
        lines.add(Component.translatable("tc.atlas.pos", node.pos().getX(), node.pos().getY(), node.pos().getZ()).withStyle(ChatFormatting.GRAY));
        for (Aspect a : node.aspects().getAspects()) {
            lines.add(Component.literal(" ").append(a.getDisplayName()).append(" " + node.aspects().getAmount(a))
                    .withColor(a.getColor()));
        }
        return lines;
    }

    private static String time(long ticks) {
        long s = ticks / 20;
        return s / 60 + ":" + String.format("%02d", s % 60);
    }
}
