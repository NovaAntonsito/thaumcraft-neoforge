package thaumcraft.common.lib.events;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;
import java.util.concurrent.LinkedBlockingQueue;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.event.EventHooks;
import org.joml.Vector3f;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aura.AuraHelper;
import thaumcraft.common.lib.utils.BlockUtils;

/**
 * Partial port of ServerEvents: the delayed runnable queue, the virtual block breaker and the block swapper used by foci.
 * TODO: the client runnable queue.
 * The aura thread and chunk regeneration were already ported elsewhere (AuraEvents).
 */
@EventBusSubscriber(modid = Thaumcraft.MODID)
public final class ServerEvents {
    private ServerEvents() {
    }

    public static final HashMap<ResourceKey<Level>, LinkedBlockingQueue<BreakData>> breakList = new HashMap<>();
    public static final HashMap<ResourceKey<Level>, LinkedBlockingQueue<VirtualSwapper>> swapList = new HashMap<>();
    public static final Predicate<SwapperPredicate> DEFAULT_PREDICATE = pred -> true;
    private static final HashMap<ResourceKey<Level>, LinkedBlockingQueue<RunnableEntry>> serverRunList = new HashMap<>();

    @SubscribeEvent
    public static void worldTick(LevelTickEvent.Post event) {
        Level world = event.getLevel();
        if (world.isClientSide) {
            return;
        }
        LinkedBlockingQueue<RunnableEntry> rlist = serverRunList.get(world.dimension());
        if (rlist == null) {
            serverRunList.put(world.dimension(), new LinkedBlockingQueue<>());
        } else if (!rlist.isEmpty()) {
            LinkedBlockingQueue<RunnableEntry> temp = new LinkedBlockingQueue<>();
            while (!rlist.isEmpty()) {
                RunnableEntry current = rlist.poll();
                if (current != null) {
                    if (current.delay > 0) {
                        --current.delay;
                        temp.offer(current);
                    } else {
                        try {
                            current.runnable.run();
                        } catch (Exception ex) {
                        }
                    }
                }
            }
            while (!temp.isEmpty()) {
                rlist.offer(temp.poll());
            }
        }
        tickBlockSwap(world);
        tickBlockBreak(world);
    }

    /**
     * Swaps the queued blocks for the target item's block, paying with one item from the player (unless creative) and
     * giving them the old block's drops. With a lifespan it spreads to touching exposed blocks of the same kind.
     * 1.21: only block state sources (materials are gone); the bamf effect is a puff of colored dust; an item that is
     * not a block drops as a plain floating item (EntitySpecialItem is not ported yet).
     */
    private static void tickBlockSwap(Level world) {
        LinkedBlockingQueue<VirtualSwapper> queue = swapList.get(world.dimension());
        LinkedBlockingQueue<VirtualSwapper> queue2 = new LinkedBlockingQueue<>();
        if (queue == null || !(world instanceof ServerLevel sl)) {
            return;
        }
        while (!queue.isEmpty()) {
            VirtualSwapper vs = queue.poll();
            if (vs == null) {
                continue;
            }
            BlockState bs = world.getBlockState(vs.pos);
            boolean allow = bs.getDestroySpeed(world, vs.pos) >= 0.0f;
            if (vs.source instanceof BlockState && vs.source != bs) {
                allow = false;
            }
            if (vs.visCost > 0.0f && AuraHelper.getVis(world, vs.pos) < vs.visCost) {
                allow = false;
            }
            if (!world.mayInteract(vs.player, vs.pos) || !allow
                    || (vs.target != null && !vs.target.isEmpty() && ItemStack.isSameItem(vs.target, new ItemStack(bs.getBlock())))
                    || EventHooks.onBlockPlace(vs.player, BlockSnapshot.create(world.dimension(), world, vs.pos), Direction.UP)
                    || !vs.allowSwap.test(new SwapperPredicate(world, vs.player, vs.pos))) {
                continue;
            }
            int slot;
            if (!vs.consumeTarget || vs.target == null || vs.target.isEmpty()) {
                slot = 1;
            } else {
                slot = getPlayerSlotFor(vs.player, vs.target);
            }
            if (vs.player.getAbilities().instabuild) {
                slot = 1;
            }
            boolean matches = vs.source instanceof BlockState && bs == vs.source;
            if ((vs.source != null && !matches) || slot < 0) {
                continue;
            }
            if (!vs.player.getAbilities().instabuild) {
                if (vs.consumeTarget) {
                    vs.player.getInventory().removeItem(slot, 1);
                }
                if (vs.pickup) {
                    List<ItemStack> ret = new ArrayList<>();
                    if (vs.silk && bs.getBlock().asItem() != Items.AIR) {
                        ItemStack itemstack = BlockUtils.getSilkTouchDrop(bs);
                        if (!itemstack.isEmpty()) {
                            ret.add(itemstack);
                        }
                    } else {
                        ItemStack tool = ItemStack.EMPTY;
                        if (vs.fortune > 0) {
                            tool = new ItemStack(Items.STICK);
                            Holder<Enchantment> fortune = world.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.FORTUNE);
                            tool.enchant(fortune, vs.fortune);
                        }
                        ret = Block.getDrops(bs, sl, vs.pos, world.getBlockEntity(vs.pos), vs.player, tool);
                    }
                    for (ItemStack is : ret) {
                        if (!vs.player.getInventory().add(is)) {
                            world.addFreshEntity(new ItemEntity(world, vs.pos.getX() + 0.5, vs.pos.getY() + 0.5, vs.pos.getZ() + 0.5, is));
                        }
                    }
                }
                if (vs.visCost > 0.0f) {
                    AuraHelper.drainVis(world, vs.pos, vs.visCost, false);
                }
            }
            if (vs.target == null || vs.target.isEmpty()) {
                world.removeBlock(vs.pos, false);
            } else {
                Block tb = Block.byItem(vs.target.getItem());
                if (tb != Blocks.AIR) {
                    world.setBlock(vs.pos, tb.defaultBlockState(), 3);
                } else {
                    world.removeBlock(vs.pos, false);
                    ItemEntity entityItem = new ItemEntity(world, vs.pos.getX() + 0.5, vs.pos.getY() + 0.1, vs.pos.getZ() + 0.5, vs.target.copy(), 0, 0, 0);
                    entityItem.setNoGravity(true);
                    world.addFreshEntity(entityItem);
                }
            }
            if (vs.fx) {
                Color c = new Color(vs.color);
                sl.sendParticles(new DustParticleOptions(new Vector3f(c.getRed() / 255.0f, c.getGreen() / 255.0f, c.getBlue() / 255.0f), 1.5f),
                        vs.pos.getX() + 0.5, vs.pos.getY() + 0.5, vs.pos.getZ() + 0.5, vs.fancy ? 12 : 6, 0.3, 0.3, 0.3, 0.0);
            }
            if (vs.lifespan <= 0) {
                continue;
            }
            for (int xx = -1; xx <= 1; ++xx) {
                for (int yy = -1; yy <= 1; ++yy) {
                    for (int zz = -1; zz <= 1; ++zz) {
                        BlockPos np = vs.pos.offset(xx, yy, zz);
                        boolean m = vs.source instanceof BlockState && world.getBlockState(np) == vs.source;
                        if ((xx != 0 || yy != 0 || zz != 0) && m && BlockUtils.isBlockExposed(world, np)) {
                            queue2.offer(new VirtualSwapper(np, vs.source, vs.target, vs.consumeTarget, vs.lifespan - 1, vs.player, vs.fx, vs.fancy,
                                    vs.color, vs.pickup, vs.silk, vs.fortune, vs.allowSwap, vs.visCost));
                        }
                    }
                }
            }
        }
        swapList.put(world.dimension(), queue2);
    }

    /** InventoryUtils.getPlayerSlotFor: the main inventory slot holding the item, or -1. */
    private static int getPlayerSlotFor(Player player, ItemStack stack) {
        for (int i = 0; i < player.getInventory().items.size(); ++i) {
            ItemStack is = player.getInventory().items.get(i);
            if (!is.isEmpty() && ItemStack.isSameItemSameComponents(is, stack)) {
                return i;
            }
        }
        return -1;
    }

    public static void addSwapper(Level world, BlockPos pos, Object source, ItemStack target, boolean consumeTarget, int life, Player player, boolean fx,
                                  boolean fancy, int color, boolean pickup, boolean silk, int fortune, Predicate<SwapperPredicate> allowSwap, float visCost) {
        swapList.computeIfAbsent(world.dimension(), k -> new LinkedBlockingQueue<>())
                .offer(new VirtualSwapper(pos, source, target, consumeTarget, life, player, fx, fancy, color, pickup, silk, fortune, allowSwap, visCost));
    }

    private static void tickBlockBreak(Level world) {
        LinkedBlockingQueue<BreakData> queue = breakList.get(world.dimension());
        LinkedBlockingQueue<BreakData> queue2 = new LinkedBlockingQueue<>();
        if (queue != null) {
            while (!queue.isEmpty()) {
                BreakData vs = queue.poll();
                if (vs != null) {
                    BlockState bs = world.getBlockState(vs.pos);
                    if (bs == vs.source) {
                        if (vs.visCost > 0.0f && AuraHelper.getVis(world, vs.pos) < vs.visCost) {
                            continue;
                        }
                        if (!world.mayInteract(vs.player, vs.pos) || bs.getDestroySpeed(world, vs.pos) < 0.0f) {
                            continue;
                        }
                        if (vs.fx) {
                            world.destroyBlockProgress(vs.pos.hashCode(), vs.pos, (int) ((1.0f - vs.durabilityCurrent / vs.durabilityMax) * 10.0f));
                        }
                        vs.durabilityCurrent -= vs.strength;
                        if (vs.durabilityCurrent <= 0.0f) {
                            BlockUtils.harvestBlock(world, vs.player, vs.pos, true, vs.silk, vs.fortune, false);
                            if (vs.fx) {
                                world.destroyBlockProgress(vs.pos.hashCode(), vs.pos, -1);
                            }
                            if (vs.visCost <= 0.0f) {
                                continue;
                            }
                            AuraHelper.drainVis(world, vs.pos, vs.visCost, false);
                        } else {
                            queue2.offer(vs);
                        }
                    } else if (vs.fx) {
                        world.destroyBlockProgress(vs.pos.hashCode(), vs.pos, -1);
                    }
                }
            }
            breakList.put(world.dimension(), queue2);
        }
    }

    public static void addBreaker(Level world, BlockPos pos, BlockState source, Player player, boolean fx, boolean silk, int fortune, float str,
                                  float durabilityCurrent, float durabilityMax, int delay, float vis, Runnable run) {
        if (delay > 0) {
            addRunnableServer(world, () -> addBreaker(world, pos, source, player, fx, silk, fortune, str, durabilityCurrent, durabilityMax, 0, vis, run), delay);
        } else {
            breakList.computeIfAbsent(world.dimension(), k -> new LinkedBlockingQueue<>())
                    .offer(new BreakData(str, durabilityCurrent, durabilityMax, pos, source, player, fx, silk, fortune, vis));
            if (run != null) {
                run.run();
            }
        }
    }

    public static void addRunnableServer(Level world, Runnable runnable, int delay) {
        if (world.isClientSide) {
            return;
        }
        serverRunList.computeIfAbsent(world.dimension(), k -> new LinkedBlockingQueue<>()).add(new RunnableEntry(runnable, delay));
    }

    public static class BreakData {
        float strength;
        float durabilityCurrent;
        float durabilityMax;
        BlockState source;
        BlockPos pos;
        Player player;
        boolean fx;
        boolean silk;
        int fortune;
        float visCost;

        public BreakData(float strength, float durabilityCurrent, float durabilityMax, BlockPos pos, BlockState source, Player player, boolean fx,
                         boolean silk, int fortune, float vis) {
            this.strength = strength;
            this.source = source;
            this.pos = pos;
            this.player = player;
            this.fx = fx;
            this.silk = silk;
            this.fortune = fortune;
            this.durabilityCurrent = durabilityCurrent;
            this.durabilityMax = durabilityMax;
            visCost = vis;
        }
    }

    public static class SwapperPredicate {
        public Level world;
        public Player player;
        public BlockPos pos;

        public SwapperPredicate(Level world, Player player, BlockPos pos) {
            this.world = world;
            this.player = player;
            this.pos = pos;
        }
    }

    public static class VirtualSwapper {
        int color;
        boolean fancy;
        Predicate<SwapperPredicate> allowSwap;
        int lifespan;
        BlockPos pos;
        Object source;
        ItemStack target;
        Player player;
        boolean fx;
        boolean silk;
        boolean pickup;
        boolean consumeTarget;
        int fortune;
        float visCost;

        VirtualSwapper(BlockPos pos, Object source, ItemStack t, boolean consumeTarget, int life, Player p, boolean fx, boolean fancy, int color,
                       boolean pickup, boolean silk, int fortune, Predicate<SwapperPredicate> allowSwap, float cost) {
            this.pos = pos;
            this.source = source;
            target = t;
            lifespan = life;
            player = p;
            this.consumeTarget = consumeTarget;
            this.fx = fx;
            this.fancy = fancy;
            this.allowSwap = allowSwap;
            this.silk = silk;
            this.fortune = fortune;
            this.pickup = pickup;
            this.color = color;
            visCost = cost;
        }
    }

    public static class RunnableEntry {
        Runnable runnable;
        int delay;

        public RunnableEntry(Runnable runnable, int delay) {
            this.runnable = runnable;
            this.delay = delay;
        }
    }
}
