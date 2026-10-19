package thaumcraft.common.world.voidrealm;

import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.Vec3;
import thaumcraft.Config;
import thaumcraft.api.capabilities.PlayerWarp;
import thaumcraft.api.capabilities.ThaumcraftCapabilities;
import thaumcraft.common.entities.EntityVoidRift;
import thaumcraft.common.lib.InternalMethodHandler;
import thaumcraft.common.registry.TCAttachments;
import thaumcraft.common.world.dim.MazeThread;
import thaumcraft.common.world.dim.TeleporterThaumcraft;

/**
 * Dream branch: the Whispers of the Void. Opens the rift of a player's next encounter, takes players through it into
 * that encounter's labyrinth and brings them back. The encounters a player has done are the journal pages they have
 * read (research VOIDPAGE1 to VOIDPAGE5); the open rift and the way home live in their {@link TCAttachments#DREAM} data.
 * The overworld's saved data counts the labyrinths made so far, so each gets a place of its own.
 */
public class VoidEncounters extends SavedData {
    public static final String UNLOCK = "VOIDWHISPERS";
    public static final String PAGE = "VOIDPAGE";
    public static final String FINAL = "VOIDEXPLORER";
    public static final String WEAPON_FLAG = "!void_weapon";
    private static final String NAME = "thaumcraft_void";
    private static final Factory<VoidEncounters> FACTORY = new Factory<>(VoidEncounters::new, VoidEncounters::load, null);

    /** Three days. */
    private static final long STALE = 72000L;

    private int nextSlot;

    public static VoidEncounters get(ServerLevel any) {
        return any.getServer().overworld().getDataStorage().computeIfAbsent(FACTORY, NAME);
    }

    private static VoidEncounters load(CompoundTag tag, HolderLookup.Provider provider) {
        VoidEncounters v = new VoidEncounters();
        v.nextSlot = tag.getInt("NextSlot");
        return v;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        tag.putInt("NextSlot", nextSlot);
        return tag;
    }

    private static CompoundTag data(ServerPlayer player) {
        CompoundTag dream = player.getData(TCAttachments.DREAM);
        if (!dream.contains("Void")) dream.put("Void", new CompoundTag());
        return dream.getCompound("Void");
    }

    /** The encounter a player faces next, 1 to 5, or 6 once the last page is read. */
    public static int nextEncounter(ServerPlayer player) {
        int n = 1;
        while (n <= VoidDim.ENCOUNTERS && ThaumcraftCapabilities.knowsResearchStrict(player, PAGE + n)) ++n;
        return n;
    }

    /** Whether the void calls this player now: the event is unlocked and unfinished and no rift of theirs is open. */
    public static boolean eligible(ServerPlayer player) {
        return Config.VOID_WHISPERS.get() && ThaumcraftCapabilities.knowsResearchStrict(player, UNLOCK)
                && nextEncounter(player) <= VoidDim.ENCOUNTERS && !hasOpenRift(player) && !VoidDim.is(player.level());
    }

    /**
     * Whether the rift of the player's next encounter is open. A rift older than {@link #STALE} ticks no longer
     * counts, in case it was lost, so the void can call again.
     */
    public static boolean hasOpenRift(ServerPlayer player) {
        CompoundTag d = data(player);
        return d.hasUUID("Rift") && d.getInt("RiftEncounter") == nextEncounter(player)
                && player.level().getGameTime() - d.getLong("RiftTime") < STALE;
    }

    public static void forgetRift(ServerPlayer player) {
        data(player).remove("Rift");
        data(player).remove("RiftEncounter");
        data(player).remove("RiftTime");
    }

    /**
     * Lays out the labyrinth of the player's next encounter and opens its rift 150 to 300 blocks away, on the ground.
     * Returns the rift, or null if it could not be placed.
     */
    @Nullable
    public static EntityVoidRift openRift(ServerLevel level, ServerPlayer player) {
        int encounter = nextEncounter(player);
        if (encounter > VoidDim.ENCOUNTERS) return null;
        VoidEncounters v = get(level);
        int cx = VoidDim.OFFSET + v.nextSlot * VoidDim.SPACING;
        int cz = encounter * VoidDim.SPACING;
        v.nextSlot++;
        v.setDirty();
        int size = VoidDim.mazeSize(encounter);
        // Small mazes are quick to lay out, so this runs here instead of on its own thread: the cells must be
        // there before anyone walks in.
        new MazeThread(cx, cz, size, size, level.random.nextLong()).run();

        double angle = level.random.nextDouble() * Math.PI * 2.0;
        double dist = 150.0 + level.random.nextDouble() * 150.0;
        int x = (int) (player.getX() + Math.cos(angle) * dist);
        int z = (int) (player.getZ() + Math.sin(angle) * dist);
        level.getChunk(x >> 4, z >> 4);
        BlockPos ground = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(x, 0, z));
        if (ground.getY() <= level.getMinBuildHeight()) ground = new BlockPos(x, level.getSeaLevel(), z);
        EntityVoidRift rift = new EntityVoidRift(level);
        rift.setRiftSeed(level.random.nextInt());
        rift.moveTo(x + 0.5, ground.getY() + 2.5, z + 0.5, level.random.nextInt(360), 0.0f);
        rift.setMaze(cx, cz, encounter, player.getUUID());
        if (!level.addFreshEntity(rift)) return null;
        rift.setRiftSize(30);
        CompoundTag d = data(player);
        d.putUUID("Rift", rift.getUUID());
        d.putInt("RiftEncounter", encounter);
        d.putLong("RiftPos", rift.blockPosition().asLong());
        d.putLong("RiftTime", level.getGameTime());
        return rift;
    }

    /** Into the rift: remembers the way back, adds void warp and lands the player by the labyrinth's portal. */
    public static void enter(ServerPlayer player, EntityVoidRift rift) {
        ServerLevel target = player.server.getLevel(VoidDim.KEY);
        if (target == null) return;
        CompoundTag d = data(player);
        d.putString("ReturnDim", player.level().dimension().location().toString());
        Vec3 back = rift.position().add(3.0, -2.0, 0.0);
        d.putDouble("ReturnX", back.x);
        d.putDouble("ReturnY", back.y);
        d.putDouble("ReturnZ", back.z);
        player.setPortalCooldown(100);
        InternalMethodHandler.addWarpToPlayer(player, Config.VOID_WARP_GAIN.get(), PlayerWarp.EnumWarpType.VOID);
        player.changeDimension(TeleporterThaumcraft.transitionTo(target, player, rift.getMazeX() * 16 + 8, rift.getMazeZ() * 16 + 8));
        player.displayClientMessage(Component.translatable("tc.void.enter").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC), true);
    }

    /** Through the labyrinth's portal: back where the player went in (the overworld spawn if that is lost). */
    public static void leave(ServerPlayer player) {
        CompoundTag d = data(player);
        ServerLevel back = null;
        if (d.contains("ReturnDim")) {
            ResourceLocation rl = ResourceLocation.tryParse(d.getString("ReturnDim"));
            if (rl != null) back = player.server.getLevel(ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, rl));
        }
        Vec3 pos;
        if (back == null) {
            back = player.server.overworld();
            BlockPos spawn = back.getSharedSpawnPos();
            pos = Vec3.atBottomCenterOf(back.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, spawn));
        } else {
            pos = new Vec3(d.getDouble("ReturnX"), d.getDouble("ReturnY"), d.getDouble("ReturnZ"));
        }
        player.setPortalCooldown(100);
        player.changeDimension(new DimensionTransition(back, pos, Vec3.ZERO, player.getYRot(), player.getXRot(),
                DimensionTransition.PLACE_PORTAL_TICKET));
    }

    /** Whether the encounter of a rift is done for its owner (they read its page). */
    public static boolean isDone(ServerLevel level, UUID owner, int encounter) {
        ServerPlayer p = level.getServer().getPlayerList().getPlayer(owner);
        return p != null && ThaumcraftCapabilities.knowsResearchStrict(p, PAGE + encounter);
    }
}
