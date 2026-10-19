package thaumcraft.common.items.consumables;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.lib.research.CartographyTargets;
import thaumcraft.common.lib.research.ResearchNoteData;
import thaumcraft.common.lib.research.ResearchNoteManager;
import thaumcraft.common.registry.TCDataComponents;

/**
 * Not in Thaumcraft (dream branch): a cartographic note. It is solved at the research table like a research note;
 * its grid is drawn when it first goes on the table. Using the solved note turns it into a map marking the closest
 * place of its kind the world knows of. Notes found as loot get their kind when they first reach an inventory.
 */
public class ItemCartographicNote extends ItemResearchNote {
    public static final String PREFIX = "carto:";
    private static final String MAPPED = "thaumcraft.cartography";

    public ItemCartographicNote(Item.Properties properties) {
        super(properties);
    }

    public static boolean isCartographic(String key) {
        return key.startsWith(PREFIX);
    }

    /** A blank note leading to the target; the table draws its grid. */
    public static ItemStack create(CartographyTargets.Target target, RandomSource rand) {
        ItemStack stack = new ItemStack(thaumcraft.common.config.ConfigItems.cartographicNote.get());
        setData(stack, blank(target, rand));
        return stack;
    }

    private static ResearchNoteData blank(CartographyTargets.Target target, RandomSource rand) {
        ResearchNoteData data = new ResearchNoteData();
        data.key = PREFIX + target.id() + ":" + rand.nextInt(Integer.MAX_VALUE);
        data.color = Aspect.MOTION.getColor();
        data.generic = true;
        return data;
    }

    @Nullable
    public static CartographyTargets.Target target(String key) {
        if (!isCartographic(key)) return null;
        String rest = key.substring(PREFIX.length());
        int i = rest.indexOf(':');
        return CartographyTargets.get(i < 0 ? rest : rest.substring(0, i));
    }

    /** ResearchNoteManager.computeNoteData for a cartographic note: a random grid as hard as its target. */
    @Nullable
    public static ResearchNoteData computeNoteData(Level world, String key) {
        CartographyTargets.Target target = target(key);
        if (target == null) return null;
        ResearchNoteData data = new ResearchNoteData();
        data.key = key;
        data.color = Aspect.MOTION.getColor();
        long worldSeed = world instanceof ServerLevel sl ? sl.getSeed() : 0L;
        Random rand = new Random(31 * worldSeed + key.hashCode());
        int complexity = Mth.clamp(target.complexity(), 0, 12);
        AspectList aspects = ResearchNoteManager.getRandomAspects(rand, complexity, Math.min(11, complexity + 2));
        data.generateHexesFromComp(rand, aspects, complexity);
        return data;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!level.isClientSide && stack.get(TCDataComponents.RESEARCH_NOTE) == null) {
            setData(stack, blank(CartographyTargets.random(level.random), level.random));
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        ResearchNoteData data = getData(stack);
        CartographyTargets.Target target = data != null ? target(data.key) : null;
        if (target == null || !data.isComplete()) return InteractionResultHolder.fail(stack);
        if (!(world instanceof ServerLevel level)) return InteractionResultHolder.success(stack);
        Set<Long> mapped = new HashSet<>();
        for (long l : player.getPersistentData().getLongArray(MAPPED)) mapped.add(l);
        BlockPos pos = CartographyTargets.find(target, level, player.blockPosition(), mapped);
        if (pos == null) {
            player.displayClientMessage(Component.translatable("tc.carto.none").withStyle(ChatFormatting.GRAY), true);
            return InteractionResultHolder.fail(stack);
        }
        mapped.add(pos.asLong());
        player.getPersistentData().putLongArray(MAPPED, mapped.stream().mapToLong(Long::longValue).toArray());
        // As an explorer map: centered on the place, the biomes filled in and an X on it.
        ItemStack map = MapItem.create(level, pos.getX(), pos.getZ(), (byte) 1, true, true);
        MapItem.renderBiomePreviewMap(level, map);
        MapItemSavedData.addTargetDecoration(map, pos, "+", MapDecorationTypes.TARGET_X);
        map.set(DataComponents.ITEM_NAME, Component.translatable("item.thaumcraft.cartographic_map." + target.id()));
        world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundsTC.WRITE.get(), SoundSource.PLAYERS, 0.75f, 1.0f);
        return InteractionResultHolder.consume(map);
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        return isComplete(stack) ? "item.thaumcraft.cartographic_discovery" : getDescriptionId();
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        ResearchNoteData rd = stack.get(TCDataComponents.RESEARCH_NOTE);
        CartographyTargets.Target target = rd != null ? target(rd.key) : null;
        if (target != null) {
            tooltip.add(Component.translatable("tc.carto.leads", Component.translatable("tc.carto.target." + target.id()))
                    .withStyle(ChatFormatting.YELLOW));
        }
    }
}
