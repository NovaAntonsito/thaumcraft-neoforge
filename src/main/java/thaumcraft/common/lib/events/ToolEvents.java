package thaumcraft.common.lib.events;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import thaumcraft.common.lib.network.fx.PacketFXSlash;
import thaumcraft.Thaumcraft;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.lib.enchantment.EnumInfusionEnchantment;
import thaumcraft.common.lib.network.fx.PacketFXScanSource;

/**
 * Port of ToolEvents: the effects of the infusion enchantments, plus the rare earth nuggets from ores.
 * <p>
 * The 1.12 HarvestDropsEvent work is split: area breaking (Destructive) runs on BreakEvent and the drop changes
 * (Collector, Lamplighter, rare earth) on BlockDropsEvent.
 * TODO: Essence (needs entity aspects); the following item FX.
 */
@EventBusSubscriber(modid = Thaumcraft.MODID)
public final class ToolEvents {
    static final Map<Integer, Direction> lastFaceClicked = new HashMap<>();
    static boolean blockDestructiveRecursion = false;
    static boolean blockBurrowingRecursion = false;

    private ToolEvents() {
    }

    private static boolean has(ItemStack stack, EnumInfusionEnchantment ench) {
        return !stack.isEmpty() && EnumInfusionEnchantment.getInfusionEnchantments(stack).contains(ench);
    }

    /** ForgeHooks.isToolEffective, or a tool that mines this block faster than by hand. */
    private static boolean isToolEffective(ItemStack stack, BlockState state) {
        return stack.isCorrectToolForDrops(state) || stack.getDestroySpeed(state) > 1.0f;
    }

    /** EntityUtils.isFriendly: other players and pets of the attacker are left alone. */
    private static boolean isFriendly(Player player, Entity e) {
        if (e instanceof Player) return true;
        return e instanceof OwnableEntity own && player.getUUID().equals(own.getOwnerUUID());
    }

    @SubscribeEvent
    public static void playerAttack(AttackEntityEvent event) {
        Player player = event.getEntity();
        Level world = player.level();
        ItemStack heldItem = player.getMainHandItem();
        Entity target = event.getTarget();
        if (world.isClientSide || !has(heldItem, EnumInfusionEnchantment.ARCING) || !target.isAlive()) {
            return;
        }
        int rank = EnumInfusionEnchantment.getInfusionEnchantmentLevel(heldItem, EnumInfusionEnchantment.ARCING);
        List<Entity> targets = world.getEntities(player, target.getBoundingBox().inflate(1.5 + rank, 1.0f + rank / 2.0f, 1.5 + rank));
        int count = 0;
        if (targets.size() > 1) {
            for (Entity e : targets) {
                if (!e.isAlive() || isFriendly(player, e)) continue;
                if (e instanceof Mob && e.getId() != target.getId()) {
                    float f = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
                    if (e.hurt(world.damageSources().playerAttack(player), f * 0.5f)) {
                        e.push(-Mth.sin(player.getYRot() * Mth.DEG_TO_RAD) * 0.5f, 0.1, Mth.cos(player.getYRot() * Mth.DEG_TO_RAD) * 0.5f);
                        ++count;
                        PacketDistributor.sendToPlayersNear((ServerLevel) world, null, target.getX(), target.getY(), target.getZ(), 64.0,
                                new PacketFXSlash(target.getId(), e.getId()));
                    }
                }
                if (count >= rank) break;
            }
            if (count > 0) {
                world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundsTC.WIND.get(), SoundSource.PLAYERS, 1.0f,
                        0.9f + world.random.nextFloat() * 0.2f);
                PacketDistributor.sendToPlayersNear((ServerLevel) world, null, player.getX(), player.getY(), player.getZ(), 64.0,
                        new PacketFXSlash(player.getId(), target.getId()));
            }
        }
    }

    /** Sounding: sneak and right-click a block to reveal nearby ores. */
    @SubscribeEvent
    public static void playerRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        if (event.getLevel().isClientSide || event.getHand() != InteractionHand.MAIN_HAND) return;
        ItemStack heldItem = player.getMainHandItem();
        if (has(heldItem, EnumInfusionEnchantment.SOUNDING) && player.isShiftKeyDown()) {
            BlockPos pos = event.getPos();
            heldItem.hurtAndBreak(5, player, LivingEntity.getSlotForHand(InteractionHand.MAIN_HAND));
            event.getLevel().playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundsTC.WANDFAIL.get(), SoundSource.BLOCKS,
                    0.2f, 0.2f + event.getLevel().random.nextFloat() * 0.2f);
            if (player instanceof ServerPlayer sp) {
                PacketDistributor.sendToPlayer(sp, new PacketFXScanSource(pos,
                        EnumInfusionEnchantment.getInfusionEnchantmentLevel(heldItem, EnumInfusionEnchantment.SOUNDING)));
            }
        }
    }

    @SubscribeEvent
    public static void playerInteract(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getFace() != null) {
            lastFaceClicked.put(event.getEntity().getId(), event.getFace());
        }
    }

    private static boolean isValidBurrowBlock(BlockState state) {
        return state.is(BlockTags.LOGS) || state.is(Tags.Blocks.ORES);
    }

    @SubscribeEvent
    public static void breakBlockEvent(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel world) || !(event.getPlayer() instanceof ServerPlayer player)) return;
        ItemStack heldItem = player.getMainHandItem();
        if (heldItem.isEmpty()) return;
        List<EnumInfusionEnchantment> list = EnumInfusionEnchantment.getInfusionEnchantments(heldItem);
        BlockPos pos = event.getPos();
        BlockState state = event.getState();
        if (!blockBurrowingRecursion && isToolEffective(heldItem, state) && list.contains(EnumInfusionEnchantment.BURROWING)
                && !player.isShiftKeyDown() && isValidBurrowBlock(state)) {
            event.setCanceled(true);
            // The original damages the tool here; destroyBlock below already wears it by one.
            breakFurthestBlock(world, pos, state, player);
            return;
        }
        if (!blockDestructiveRecursion && list.contains(EnumInfusionEnchantment.DESTRUCTIVE) && !player.isShiftKeyDown()
                && isToolEffective(heldItem, state)) {
            blockDestructiveRecursion = true;
            Direction face = lastFaceClicked.get(player.getId());
            if (face == null) {
                face = Direction.orderedByNearest(player)[0].getOpposite();
            }
            for (int aa = -1; aa <= 1; ++aa) {
                for (int bb = -1; bb <= 1; ++bb) {
                    if (aa == 0 && bb == 0) continue;
                    int xx = 0, yy = 0, zz = 0;
                    if (face.ordinal() <= 1) {
                        xx = aa;
                        zz = bb;
                    } else if (face.ordinal() <= 3) {
                        xx = aa;
                        yy = bb;
                    } else {
                        zz = aa;
                        yy = bb;
                    }
                    BlockPos p = pos.offset(xx, yy, zz);
                    BlockState bl = world.getBlockState(p);
                    if (!bl.isAir() && bl.getDestroySpeed(world, p) >= 0.0f && isToolEffective(heldItem, bl)) {
                        player.gameMode.destroyBlock(p);
                    }
                }
            }
            blockDestructiveRecursion = false;
        }
    }

    /** BlockUtils.breakFurthestBlock: breaks the connected block of the same kind furthest from pos. */
    private static void breakFurthestBlock(ServerLevel world, BlockPos pos, BlockState block, ServerPlayer player) {
        BlockPos[] last = {pos};
        double[] lastdistance = {0.0};
        int reach = block.is(BlockTags.LOGS) ? 2 : 1;
        findBlocks(world, pos, block, reach, last, lastdistance);
        blockBurrowingRecursion = true;
        try {
            player.gameMode.destroyBlock(last[0]);
        } finally {
            blockBurrowingRecursion = false;
        }
        world.sendBlockUpdated(pos, block, block, 3);
    }

    private static void findBlocks(Level world, BlockPos pos, BlockState block, int reach, BlockPos[] last, double[] lastdistance) {
        for (int xx = -reach; xx <= reach; ++xx) {
            for (int yy = reach; yy >= -reach; --yy) {
                for (int zz = -reach; zz <= reach; ++zz) {
                    BlockPos p = last[0].offset(xx, yy, zz);
                    if (Math.abs(p.getX() - pos.getX()) > 24 || Math.abs(p.getY() - pos.getY()) > 48 || Math.abs(p.getZ() - pos.getZ()) > 24) {
                        return;
                    }
                    BlockState bs = world.getBlockState(p);
                    if (bs.getBlock() == block.getBlock() && bs.getDestroySpeed(world, p) >= 0.0f) {
                        double d = p.distSqr(pos);
                        if (d > lastdistance[0]) {
                            lastdistance[0] = d;
                            last[0] = p;
                            findBlocks(world, pos, block, reach, last, lastdistance);
                            return;
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void harvestBlockEvent(BlockDropsEvent event) {
        ServerLevel world = event.getLevel();
        BlockState state = event.getState();
        BlockPos pos = event.getPos();
        ItemStack tool = event.getTool();
        boolean silk = !tool.isEmpty() && EnchantmentHelper.getItemEnchantmentLevel(
                world.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH), tool) > 0;
        if (!silk && rareEarthChance(state, world)) {
            event.getDrops().add(new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    new ItemStack(ConfigItems.nuggets.get("rareearth").get())));
        }
        if (!(event.getBreaker() instanceof Player player) || tool.isEmpty()) return;
        List<EnumInfusionEnchantment> list = EnumInfusionEnchantment.getInfusionEnchantments(tool);
        if (list.isEmpty() || !(silk || isToolEffective(tool, state))) return;
        if (list.contains(EnumInfusionEnchantment.REFINING)) {
            int fortune = 1 + EnumInfusionEnchantment.getInfusionEnchantmentLevel(tool, EnumInfusionEnchantment.REFINING);
            float chance = fortune * 0.125f;
            boolean b = false;
            for (ItemEntity ei : event.getDrops()) {
                ItemStack is = ei.getItem();
                ItemStack smr = thaumcraft.common.lib.utils.Utils.findSpecialMiningResult(is, chance, world.random);
                if (!ItemStack.isSameItem(is, smr)) {
                    ei.setItem(smr);
                    b = true;
                }
            }
            if (b) {
                world.playSound(null, pos, net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.2f,
                        0.7f + world.random.nextFloat() * 0.2f);
            }
        }
        if (list.contains(EnumInfusionEnchantment.COLLECTOR) && !player.isShiftKeyDown()) {
            // TODO: EntityFollowingItem flies the drops to the player; here they appear at the player's feet.
            for (ItemEntity ei : event.getDrops()) {
                ei.setPos(player.getX(), player.getY(), player.getZ());
                ei.setDeltaMovement(0, 0, 0);
                ei.setNoPickUpDelay();
            }
        }
        if (list.contains(EnumInfusionEnchantment.LAMPLIGHT) && !player.isShiftKeyDown()) {
            MinecraftServer server = world.getServer();
            server.tell(new TickTask(server.getTickCount(), () -> {
                if (world.isEmptyBlock(pos) && !world.getBlockState(pos).is(ConfigBlocks.effectGlimmer.get())
                        && world.getMaxLocalRawBrightness(pos) < 10) {
                    world.setBlock(pos, ConfigBlocks.effectGlimmer.get().defaultBlockState(), 3);
                }
            }));
        }
    }

    /** The original chances per vanilla ore, plus amber and quartz ore. */
    private static boolean rareEarthChance(BlockState s, Level world) {
        float r = world.random.nextFloat();
        if (s.is(Tags.Blocks.ORES_DIAMOND)) return r < 0.05f;
        if (s.is(Tags.Blocks.ORES_EMERALD)) return r < 0.075f;
        if (s.is(Tags.Blocks.ORES_LAPIS)) return r < 0.01f;
        if (s.is(Tags.Blocks.ORES_COAL)) return r < 0.001f;
        if (s.is(Tags.Blocks.ORES_REDSTONE)) return r < 0.01f;
        if (s.is(Tags.Blocks.ORES_QUARTZ) && !s.is(ConfigBlocks.oreQuartz.get())) return r < 0.01f;
        if (s.is(ConfigBlocks.oreAmber.get()) || s.is(ConfigBlocks.oreQuartz.get())) return r < 0.05f;
        return false;
    }

    @SubscribeEvent
    public static void livingDrops(LivingDropsEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        ItemStack heldItem = player.getMainHandItem();
        if (has(heldItem, EnumInfusionEnchantment.COLLECTOR)) {
            for (ItemEntity ei : event.getDrops()) {
                ei.setPos(player.getX(), player.getY(), player.getZ());
                ei.setDeltaMovement(0, 0, 0);
                ei.setNoPickUpDelay();
            }
        }
    }
}
