package voidexpeditions.run;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import thaumcraft.Thaumcraft;
import thaumcraft.common.world.dim.MazeThread;
import thaumcraft.common.world.dim.TeleporterThaumcraft;
import voidexpeditions.network.OpenPerkChoicePayload;
import voidexpeditions.perk.Perk;
import voidexpeditions.perk.PerkEffects;
import voidexpeditions.perk.Perks;
import voidexpeditions.registry.VEAttachments;
import voidexpeditions.registry.VEItems;

/**
 * The run itself. Stepping into a gate starts one with its terminal's settings and lays out the first labyrinth.
 * In each labyrinth the void sigil of the key room opens its central portal: three perks are offered, and picking one
 * spends the sigil and moves on to the next labyrinth. The last labyrinth's portal opens once its boss falls, and the
 * run is won: a chest of loot and void shards wait at home. Dying loses the run (the perks, not the inventory) unless
 * an extra life was bought; sneaking into a portal that will not open gives it up. Two runs a day at most. The overworld's saved data counts the labyrinths made, so each gets a place of its own.
 */
public final class Expeditions extends SavedData {
    /** How many runs a player may start each day. */
    public static final int RUNS_PER_DAY = 2;
    private static final String NAME = "voidexpeditions";
    private static final Factory<Expeditions> FACTORY = new Factory<>(Expeditions::new, Expeditions::load, null);

    private int nextSlot;

    public static Expeditions get(ServerLevel any) {
        return any.getServer().overworld().getDataStorage().computeIfAbsent(FACTORY, NAME);
    }

    private static Expeditions load(CompoundTag tag, HolderLookup.Provider provider) {
        Expeditions e = new Expeditions();
        e.nextSlot = tag.getInt("NextSlot");
        return e;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        tag.putInt("NextSlot", nextSlot);
        return tag;
    }

    public static RunState run(ServerPlayer player) {
        return player.getData(VEAttachments.RUN);
    }

    private static void sync(ServerPlayer player) {
        player.syncData(VEAttachments.RUN);
    }

    private static void say(ServerPlayer player, String key, Object... args) {
        player.displayClientMessage(Component.translatable(key, args).withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC), true);
    }

    /** The overworld's day, which the daily limit of runs counts by. */
    public static long today(ServerPlayer player) {
        return player.server.overworld().getDayTime() / 24000L;
    }

    /** A player steps into a gate whose terminal is set to these: the run starts in the first labyrinth. */
    public static void start(ServerPlayer player, Difficulty difficulty, int length) {
        RunState run = run(player);
        if (run.active) return;
        ServerLevel target = player.server.getLevel(ExpeditionDim.KEY);
        if (target == null) return;
        long today = today(player);
        if (run.runsLeft(today) <= 0) {
            say(player, "voidexpeditions.run.daily_limit", RUNS_PER_DAY);
            return;
        }
        CompoundTag home = player.getData(VEAttachments.HOME);
        home.putString("Dim", player.level().dimension().location().toString());
        home.putDouble("X", player.getX());
        home.putDouble("Y", player.getY());
        home.putDouble("Z", player.getZ());
        run.start(difficulty, RunLength.clamp(length), today);
        Perks.refresh(player);
        sync(player);
        player.sendSystemMessage(Component.translatable("voidexpeditions.run.start", difficulty.displayName(), run.length)
                .withStyle(ChatFormatting.DARK_PURPLE));
        enterEncounter(player, target);
    }

    /** Lays out the labyrinth of the run's current encounter and lands the player by its portal. */
    private static void enterEncounter(ServerPlayer player, ServerLevel target) {
        RunState run = run(player);
        Expeditions e = get(target);
        int cx = ExpeditionDim.START + e.nextSlot * ExpeditionDim.SPACING;
        int cz = ExpeditionDim.mazeZ(run.difficulty, run.stage);
        e.nextSlot++;
        e.setDirty();
        int size = ExpeditionDim.mazeSize(run.stage);
        // Small mazes are quick to lay out, so this runs here instead of on its own thread: the cells must be there
        // before the player arrives.
        new MazeThread(cx, cz, size, size, target.random.nextLong()).run();
        CompoundTag home = player.getData(VEAttachments.HOME);
        home.putInt("PortalX", cx * 16 + 8);
        home.putInt("PortalZ", cz * 16 + 8);
        player.setPortalCooldown(100);
        player.changeDimension(TeleporterThaumcraft.transitionTo(target, player, cx * 16 + 8, cz * 16 + 8));
        say(player, "voidexpeditions.run.encounter", run.stage, run.length);
    }

    /**
     * Into a labyrinth's central portal: on with a sigil, or in the last labyrinth once its boss fell. Sneaking in
     * without that gives the run up.
     */
    public static void onPortal(ServerPlayer player) {
        RunState run = run(player);
        if (!run.active) {
            goHome(player);
            return;
        }
        player.setPortalCooldown(60);
        boolean last = run.stage >= run.length;
        if (last ? !run.bossSlain : !hasSigil(player)) {
            if (player.isShiftKeyDown()) {
                abandon(player);
            } else {
                say(player, last ? "voidexpeditions.portal.boss" : "voidexpeditions.portal.no_sigil");
            }
            return;
        }
        if (last) {
            win(player);
            return;
        }
        if (run.offered.isEmpty()) {
            run.offered.addAll(Perks.offer(player.getRandom(), run));
            sync(player);
        }
        PacketDistributor.sendToPlayer(player, new OpenPerkChoicePayload(List.copyOf(run.offered), run.stage, run.length));
    }

    /** The player picked one of the offered perks: the sigil is spent and the next labyrinth awaits. */
    public static void choosePerk(ServerPlayer player, int index) {
        RunState run = run(player);
        if (!run.active || index < 0 || index >= run.offered.size() || !ExpeditionDim.is(player.level())
                || run.stage >= run.length || !takeSigil(player)) {
            return;
        }
        String id = run.offered.get(index);
        run.perks.add(id);
        run.offered.clear();
        run.stage++;
        Perks.refresh(player);
        sync(player);
        Perk perk = Perks.get(id);
        if (perk != null) {
            player.sendSystemMessage(Component.translatable("voidexpeditions.perk.gained", perk.displayName())
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
        }
        PerkEffects.onPortalCrossed(player);
        enterEncounter(player, player.serverLevel());
    }

    /** A boss fell in the expeditions: the runs in their last labyrinth nearby may leave. */
    public static void onBossDeath(ServerLevel level, Vec3 at) {
        for (ServerPlayer p : level.players()) {
            RunState run = run(p);
            if (run.active && run.stage >= run.length && !run.bossSlain && p.position().distanceTo(at) < 192.0) {
                run.bossSlain = true;
                sync(p);
                p.sendSystemMessage(Component.translatable("voidexpeditions.run.boss_slain").withStyle(ChatFormatting.GOLD));
            }
        }
    }

    /** About to die on a run with the extra life: it is spent instead, as a totem would. */
    public static boolean tryExtraLife(ServerPlayer player) {
        RunState run = run(player);
        if (!run.active || !run.extraLife) return false;
        run.extraLife = false;
        sync(player);
        player.setHealth(player.getMaxHealth() / 2.0f);
        player.clearFire();
        player.removeAllEffects();
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1));
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 1));
        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 400, 0));
        player.level().broadcastEntityEvent(player, (byte) 35);
        player.sendSystemMessage(Component.translatable("voidexpeditions.run.extra_life").withStyle(ChatFormatting.GOLD));
        return true;
    }

    /** Through the last portal: the run is won. */
    private static void win(ServerPlayer player) {
        RunState run = run(player);
        Difficulty difficulty = run.difficulty;
        int length = run.length;
        int shards = difficulty.shardsPerEncounter * length;
        end(player);
        goHome(player);
        player.sendSystemMessage(Component.translatable("voidexpeditions.run.won", difficulty.displayName(), shards)
                .withStyle(ChatFormatting.GOLD));
        CompoundTag home = player.getData(VEAttachments.HOME);
        home.putInt("Won", home.getInt("Won") + 1);
        rewardChest(player, Math.min(5, length - 2 + difficulty.lootBonus));
        ItemStack s = new ItemStack(VEItems.VOID_SHARD.get(), shards);
        if (!player.getInventory().add(s)) player.drop(s, false);
    }

    /** Sneaking into a portal without a sigil: the run is given up, the perks with it. */
    private static void abandon(ServerPlayer player) {
        end(player);
        goHome(player);
        player.sendSystemMessage(Component.translatable("voidexpeditions.run.abandoned").withStyle(ChatFormatting.GRAY));
    }

    /** Death in a run: the run is lost, but the inventory is kept for the respawn (see {@link #restoreAfterDeath}). */
    public static void onDeath(ServerPlayer player) {
        if (!run(player).active) return;
        CompoundTag home = player.getData(VEAttachments.HOME);
        home.put("Kept", player.getInventory().save(new ListTag()));
        player.getInventory().clearContent();
        end(player);
        player.sendSystemMessage(Component.translatable("voidexpeditions.run.lost").withStyle(ChatFormatting.DARK_RED));
    }

    public static void restoreAfterDeath(ServerPlayer player) {
        CompoundTag home = player.getData(VEAttachments.HOME);
        if (home.contains("Kept", Tag.TAG_LIST)) {
            player.getInventory().load(home.getList("Kept", Tag.TAG_COMPOUND));
            home.remove("Kept");
            stripSigils(player);
        }
        Perks.refresh(player);
    }

    /** Whatever way a run ends: no more perks or sigils. */
    private static void end(ServerPlayer player) {
        run(player).clear();
        stripSigils(player);
        Perks.refresh(player);
        sync(player);
    }

    /** Back where the run started (the overworld spawn if that is lost). */
    private static void goHome(ServerPlayer player) {
        CompoundTag home = player.getData(VEAttachments.HOME);
        ServerLevel back = null;
        if (home.contains("Dim")) {
            ResourceLocation rl = ResourceLocation.tryParse(home.getString("Dim"));
            if (rl != null) back = player.server.getLevel(ResourceKey.create(Registries.DIMENSION, rl));
        }
        Vec3 pos;
        if (back == null) {
            back = player.server.overworld();
            BlockPos spawn = back.getSharedSpawnPos();
            pos = Vec3.atBottomCenterOf(back.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, spawn));
        } else {
            pos = new Vec3(home.getDouble("X"), home.getDouble("Y"), home.getDouble("Z"));
        }
        player.setPortalCooldown(100);
        player.changeDimension(new DimensionTransition(back, pos, Vec3.ZERO, player.getYRot(), player.getXRot(),
                DimensionTransition.PLACE_PORTAL_TICKET));
    }

    /** The final chest, beside the player at home; its loot is dropped instead if there is no room for it. */
    private static void rewardChest(ServerPlayer player, int depth) {
        ServerLevel level = player.serverLevel();
        LootTable table = level.getServer().reloadableRegistries().getLootTable(
                ResourceKey.create(Registries.LOOT_TABLE, Thaumcraft.id("chests/void_encounter_" + Math.max(1, depth))));
        LootParams params = new LootParams.Builder(level).withParameter(LootContextParams.ORIGIN, player.position())
                .withLuck(player.getLuck()).create(LootContextParamSets.CHEST);
        List<ItemStack> loot = table.getRandomItems(params);
        BlockPos at = player.blockPosition().relative(player.getDirection());
        if (level.getBlockState(at).canBeReplaced() && level.setBlock(at, Blocks.CHEST.defaultBlockState(), 3)
                && level.getBlockEntity(at) instanceof ChestBlockEntity chest) {
            Container c = chest;
            for (ItemStack s : loot) {
                boolean placed = false;
                for (int i = 0; i < c.getContainerSize() && !placed; i++) {
                    if (c.getItem(i).isEmpty()) {
                        c.setItem(i, s);
                        placed = true;
                    }
                }
                if (!placed) player.drop(s, false);
            }
        } else {
            for (ItemStack s : loot) player.drop(s, false);
        }
    }

    private static boolean hasSigil(ServerPlayer player) {
        return player.getInventory().contains(new ItemStack(VEItems.VOID_SIGIL.get()));
    }

    private static boolean takeSigil(ServerPlayer player) {
        for (ItemStack s : player.getInventory().items) {
            if (s.is(VEItems.VOID_SIGIL.get())) {
                s.shrink(1);
                return true;
            }
        }
        return false;
    }

    private static void stripSigils(ServerPlayer player) {
        player.getInventory().clearOrCountMatchingItems(s -> s.is(VEItems.VOID_SIGIL.get()), -1,
                player.inventoryMenu.getCraftSlots());
    }
}
