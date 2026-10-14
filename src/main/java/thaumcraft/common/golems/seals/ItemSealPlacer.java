package thaumcraft.common.golems.seals;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import thaumcraft.api.golems.ISealDisplayer;
import thaumcraft.api.golems.seals.ISeal;

/**
 * Port of ItemSealPlacer. 1.12 had one "seal" item whose metadata was the seal's place in the registration order
 * (0 = blank). Here each seal has its own item, seal_ + the seal key's path (seal_blank, seal_pickup, ...),
 * the same names as the original item models.
 */
public class ItemSealPlacer extends Item implements ISealDisplayer {
    /** The seal this item places, null for the blank seal. */
    @Nullable
    private final String sealKey;

    public ItemSealPlacer(@Nullable String sealKey, Properties properties) {
        super(properties.stacksTo(64));
        this.sealKey = sealKey;
    }

    @Nullable
    public String getSealKey() {
        return sealKey;
    }

    /** The placer item of a seal key such as "thaumcraft:pickup", or an empty stack. */
    public static ItemStack getSealStack(String sealKey) {
        ResourceLocation key = ResourceLocation.tryParse(sealKey);
        if (key == null) {
            return ItemStack.EMPTY;
        }
        Item item = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(key.getNamespace(), "seal_" + key.getPath()));
        return item instanceof ItemSealPlacer ? new ItemStack(item) : ItemStack.EMPTY;
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Player player = context.getPlayer();
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction side = context.getClickedFace();
        if (world.isClientSide || player == null || sealKey == null || player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (!player.mayUseItemAt(pos, side, stack)) {
            return InteractionResult.FAIL;
        }
        ISeal seal = null;
        try {
            seal = SealHandler.getSeal(sealKey).getClass().getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (seal == null || !seal.canPlaceAt(world, pos, side)) {
            return InteractionResult.FAIL;
        }
        if (SealHandler.addSealEntity(world, pos, side, seal, player) && !player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return InteractionResult.SUCCESS;
    }

    /** doesSneakBypassUse: sneaking still lets the block react to the click. */
    @Override
    public boolean doesSneakBypassUse(ItemStack stack, net.minecraft.world.level.LevelReader level, BlockPos pos, Player player) {
        return true;
    }
}
