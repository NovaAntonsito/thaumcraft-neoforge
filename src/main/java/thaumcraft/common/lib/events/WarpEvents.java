package thaumcraft.common.lib.events;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import thaumcraft.Config;
import thaumcraft.api.capabilities.PlayerWarp;
import thaumcraft.api.capabilities.ThaumcraftCapabilities;
import thaumcraft.common.entities.monster.EntityEldritchGuardian;
import thaumcraft.common.entities.monster.EntityMindSpider;
import thaumcraft.common.entities.monster.cult.EntityCultistPortalLesser;
import thaumcraft.common.lib.InternalMethodHandler;
import thaumcraft.common.lib.network.misc.PacketMiscEvent;
import thaumcraft.common.lib.research.ResearchManager;
import thaumcraft.common.lib.utils.EntityUtils;
import thaumcraft.common.registry.TCEntities;
import thaumcraft.common.registry.TCMobEffects;

/**
 * Port of WarpEvents: what warp does to a player. Every 2000 ticks (PlayerEvents) the player loses 1 temporary warp
 * and, with a chance that grows with the warp counter, something happens: the stronger the warp (plus the warp of
 * held and worn gear), the worse it can get. Sounds, whispers, potion effects, fog with eldritch guardians, mind
 * spiders (fake or real) or a crimson portal. Warp above 10 also hints at bath salts.
 * As in the original, the creeper and explosion sounds are played to everyone near the player but the player.
 */
public final class WarpEvents {
    private WarpEvents() {
    }

    private static void message(Player player, String key) {
        player.displayClientMessage(Component.translatable(key).withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC), true);
    }

    /** An effect milk does not cure, as the original cleared the curative items. */
    private static MobEffectInstance uncurable(Holder<MobEffect> effect, int duration, int amplifier, boolean ambient, boolean visible) {
        MobEffectInstance pe = new MobEffectInstance(effect, duration, amplifier, ambient, visible);
        pe.getCures().clear();
        return pe;
    }

    public static void checkWarpEvent(Player player) {
        PlayerWarp wc = ThaumcraftCapabilities.getWarp(player);
        InternalMethodHandler.addWarpToPlayer(player, -1, PlayerWarp.EnumWarpType.TEMPORARY);
        int tw = wc.get(PlayerWarp.EnumWarpType.TEMPORARY);
        int nw = wc.get(PlayerWarp.EnumWarpType.NORMAL);
        int pw = wc.get(PlayerWarp.EnumWarpType.PERMANENT);
        // Dream branch: void warp past its safe amount.
        int warp = tw + nw + pw + wc.getVoidExcess();
        int actualwarp = pw + nw;
        int gearWarp = getWarpFromGear(player);
        warp += gearWarp;
        int warpCounter = wc.getCounter();
        Level world = player.level();
        int r = world.random.nextInt(100);
        if (warpCounter > 0 && warp > 0 && r <= Math.sqrt(warpCounter)) {
            warp = Math.min(100, (warp + warp + warpCounter) / 3);
            warpCounter -= (int) Math.max(5.0, Math.sqrt(warpCounter) * 2.0 - gearWarp * 2);
            wc.setCounter(warpCounter);
            int eff = world.random.nextInt(warp) + gearWarp;
            // The grinning devil mask softens the event.
            if (thaumcraft.common.items.armor.ItemFortressArmor.getMask(player.getItemBySlot(EquipmentSlot.HEAD)) == 0) {
                eff -= 2 + world.random.nextInt(4);
            }
            if (player instanceof ServerPlayer sp) {
                PacketDistributor.sendToPlayer(sp, new PacketMiscEvent(PacketMiscEvent.WARP_EVENT));
            }
            if (eff > 0) {
                if (eff <= 4) {
                    if (!Config.NOSTRESS.get()) {
                        world.playSound(player, player.blockPosition(), SoundEvents.CREEPER_PRIMED, SoundSource.AMBIENT, 1.0f, 0.5f);
                    }
                } else if (eff <= 8) {
                    if (!Config.NOSTRESS.get()) {
                        world.playSound(player, player.getX() + (world.random.nextFloat() - world.random.nextFloat()) * 10.0f,
                                player.getY() + (world.random.nextFloat() - world.random.nextFloat()) * 10.0f,
                                player.getZ() + (world.random.nextFloat() - world.random.nextFloat()) * 10.0f, SoundEvents.GENERIC_EXPLODE.value(),
                                SoundSource.AMBIENT, 4.0f, (1.0f + (world.random.nextFloat() - world.random.nextFloat()) * 0.2f) * 0.7f);
                    }
                } else if (eff <= 12) {
                    message(player, "warp.text.11");
                } else if (eff <= 16) {
                    player.addEffect(uncurable(TCMobEffects.VIS_EXHAUST, 5000, Math.min(3, warp / 15), true, true));
                    message(player, "warp.text.1");
                } else if (eff <= 20) {
                    player.addEffect(uncurable(TCMobEffects.THAUMARHIA, Math.min(32000, 10 * warp), 0, true, true));
                    message(player, "warp.text.15");
                } else if (eff <= 24) {
                    player.addEffect(uncurable(TCMobEffects.UNNATURAL_HUNGER, 5000, Math.min(3, warp / 15), true, true));
                    message(player, "warp.text.2");
                } else if (eff <= 28) {
                    message(player, "warp.text.12");
                } else if (eff <= 32) {
                    spawnMist(player, warp, 1);
                } else if (eff <= 36) {
                    player.addEffect(new MobEffectInstance(TCMobEffects.BLURRED_VISION, Math.min(32000, 10 * warp), 0, true, true));
                } else if (eff <= 40) {
                    player.addEffect(uncurable(TCMobEffects.SUN_SCORNED, 5000, Math.min(3, warp / 15), true, true));
                    message(player, "warp.text.5");
                } else if (eff <= 44) {
                    player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 1200, Math.min(3, warp / 15), true, true));
                    message(player, "warp.text.9");
                } else if (eff <= 48) {
                    MobEffectInstance pe = new MobEffectInstance(TCMobEffects.INFECTIOUS_VIS_EXHAUST, 6000, Math.min(3, warp / 15));
                    pe.getCures().clear();
                    player.addEffect(pe);
                    message(player, "warp.text.1");
                } else if (eff <= 52) {
                    player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, Math.min(40 * warp, 6000), 0, true, true));
                    message(player, "warp.text.10");
                } else if (eff <= 56) {
                    player.addEffect(uncurable(TCMobEffects.DEATH_GAZE, 6000, Math.min(3, warp / 15), true, true));
                    message(player, "warp.text.4");
                } else if (eff <= 60) {
                    suddenlySpiders(player, warp, false);
                } else if (eff <= 64) {
                    message(player, "warp.text.13");
                } else if (eff <= 68) {
                    spawnMist(player, warp, warp / 30);
                } else if (eff <= 72) {
                    player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, Math.min(32000, 5 * warp), 0, true, true));
                } else if (eff == 76) {
                    if (nw > 0) {
                        InternalMethodHandler.addWarpToPlayer(player, -1, PlayerWarp.EnumWarpType.NORMAL);
                    }
                    message(player, "warp.text.14");
                } else if (eff <= 80) {
                    player.addEffect(uncurable(TCMobEffects.UNNATURAL_HUNGER, 6000, Math.min(3, warp / 15), true, true));
                    message(player, "warp.text.2");
                } else if (eff <= 88) {
                    spawnPortal(player);
                } else if (eff <= 92) {
                    suddenlySpiders(player, warp, true);
                } else {
                    spawnMist(player, warp, warp / 15);
                }
            }
            if (actualwarp > 10 && !ThaumcraftCapabilities.knowsResearch(player, "BATHSALTS") && !ThaumcraftCapabilities.knowsResearch(player, "!BATHSALTS")) {
                message(player, "warp.text.8");
                ResearchManager.completeResearch(player, "!BATHSALTS");
            }
            if (actualwarp > 25 && !ThaumcraftCapabilities.knowsResearch(player, "ELDRITCHMINOR")) {
                ResearchManager.completeResearch(player, "ELDRITCHMINOR");
            }
            if (actualwarp > 50 && !ThaumcraftCapabilities.knowsResearch(player, "ELDRITCHMAJOR")) {
                ResearchManager.completeResearch(player, "ELDRITCHMAJOR");
            }
        }
    }

    /** The long eldritch fog, with up to 8 eldritch guardians in it. */
    private static void spawnMist(Player player, int warp, int guardian) {
        if (player instanceof ServerPlayer sp) {
            PacketDistributor.sendToPlayer(sp, new PacketMiscEvent(PacketMiscEvent.MIST_EVENT));
        }
        if (guardian > 0) {
            guardian = Math.min(8, guardian);
            for (int a = 0; a < guardian; ++a) {
                spawnGuardian(player);
            }
        }
        message(player, "warp.text.6");
    }

    /** A random spot 7 to 24 blocks away on each axis (or level with the player on it), as MathHelper.getInt did. */
    private static int offset(Player player) {
        return Mth.nextInt(player.level().random, 7, 24) * Mth.nextInt(player.level().random, -1, 1);
    }

    private static boolean isClear(Level world, Entity e) {
        return world.isUnobstructed(e) && world.noCollision(e) && !world.containsAnyLiquid(e.getBoundingBox());
    }

    /** A lesser crimson portal on solid ground somewhere around the player. */
    private static void spawnPortal(Player player) {
        Level world = player.level();
        EntityCultistPortalLesser eg = TCEntities.CULTIST_PORTAL_LESSER.get().create(world);
        if (eg == null) {
            return;
        }
        int i = Mth.floor(player.getX());
        int j = Mth.floor(player.getY());
        int k = Mth.floor(player.getZ());
        for (int l = 0; l < 50; ++l) {
            int i2 = i + offset(player);
            int j2 = j + offset(player);
            int k2 = k + offset(player);
            eg.setPos(i2 + 0.5, j2 + 1.0, k2 + 0.5);
            BlockPos below = new BlockPos(i2, j2 - 1, k2);
            if (world.getBlockState(below).isSolidRender(world, below) && isClear(world, eg)) {
                if (world instanceof ServerLevel sl) {
                    eg.finalizeSpawn(sl, sl.getCurrentDifficultyAt(eg.blockPosition()), MobSpawnType.EVENT, null);
                }
                world.addFreshEntity(eg);
                message(player, "warp.text.16");
                break;
            }
        }
    }

    /** An eldritch guardian on a full block somewhere around the player, after them. */
    private static void spawnGuardian(Player player) {
        Level world = player.level();
        EntityEldritchGuardian eg = TCEntities.ELDRITCH_GUARDIAN.get().create(world);
        if (eg == null) {
            return;
        }
        int i = Mth.floor(player.getX());
        int j = Mth.floor(player.getY());
        int k = Mth.floor(player.getZ());
        for (int l = 0; l < 50; ++l) {
            int i2 = i + offset(player);
            int j2 = j + offset(player);
            int k2 = k + offset(player);
            BlockPos below = new BlockPos(i2, j2 - 1, k2);
            if (world.getBlockState(below).isCollisionShapeFullBlock(world, below)) {
                eg.setPos(i2, j2, k2);
                if (isClear(world, eg)) {
                    eg.setTarget(player);
                    world.addFreshEntity(eg);
                    break;
                }
            }
        }
    }

    /** As many mind spiders as the warp (up to 50) around the player. Unless real, only the player sees them and they do no harm. */
    private static void suddenlySpiders(Player player, int warp, boolean real) {
        Level world = player.level();
        for (int spawns = Math.min(50, warp), a = 0; a < spawns; ++a) {
            EntityMindSpider spider = TCEntities.MIND_SPIDER.get().create(world);
            if (spider == null) {
                continue;
            }
            int i = Mth.floor(player.getX());
            int j = Mth.floor(player.getY());
            int k = Mth.floor(player.getZ());
            boolean success = false;
            for (int l = 0; l < 50; ++l) {
                int i2 = i + offset(player);
                int j2 = j + offset(player);
                int k2 = k + offset(player);
                BlockPos below = new BlockPos(i2, j2 - 1, k2);
                if (world.getBlockState(below).isCollisionShapeFullBlock(world, below)) {
                    spider.setPos(i2, j2, k2);
                    if (isClear(world, spider)) {
                        success = true;
                        break;
                    }
                }
            }
            if (success) {
                spider.setTarget(player);
                if (!real) {
                    spider.setViewer(player.getName().getString());
                    spider.setHarmless(true);
                }
                world.addFreshEntity(spider);
            }
        }
        message(player, "warp.text.7");
    }

    /** Deadly Gaze: what the player looks at within 8 blocks (3 more a level, up to 24) withers and turns on them. */
    public static void checkDeathGaze(Player player) {
        MobEffectInstance pe = player.getEffect(TCMobEffects.DEATH_GAZE);
        if (pe == null) {
            return;
        }
        int level = pe.getAmplifier();
        int range = Math.min(8 + level * 3, 24);
        List<Entity> list = player.level().getEntities(player, player.getBoundingBox().inflate(range, range, range));
        for (Entity entity : list) {
            if (entity.isPickable() && entity instanceof LivingEntity le && le.isAlive() && EntityUtils.isVisibleTo(0.75f, player, entity, range)
                    && player.hasLineOfSight(entity)
                    && (!(entity instanceof Player) || (player.getServer() != null && player.getServer().isPvpAllowed()))
                    && !le.hasEffect(MobEffects.WITHER)) {
                le.setLastHurtByMob(player);
                le.setLastHurtMob(player);
                if (entity instanceof PathfinderMob creature) {
                    creature.setTarget(player);
                }
                le.addEffect(new MobEffectInstance(MobEffects.WITHER, 80));
            }
        }
    }

    /** The warp of the held item and the armor worn. */
    private static int getWarpFromGear(Player player) {
        int w = PlayerEvents.getFinalWarp(player.getMainHandItem(), player);
        for (EquipmentSlot slot : new EquipmentSlot[] { EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD }) {
            w += PlayerEvents.getFinalWarp(player.getItemBySlot(slot), player);
        }
        for (net.minecraft.world.item.ItemStack bauble : thaumcraft.common.lib.utils.BaublesHelper.getBaubles(player)) {
            w += PlayerEvents.getFinalWarp(bauble, player);
        }
        return w;
    }
}
