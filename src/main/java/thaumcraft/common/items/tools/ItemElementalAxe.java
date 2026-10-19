package thaumcraft.common.items.tools;

import java.util.List;
import java.util.function.Consumer;

import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import thaumcraft.api.ThaumcraftMaterials;
import thaumcraft.client.fx.ClientFX;
import thaumcraft.common.items.ISubItems;
import thaumcraft.common.lib.enchantment.EnumInfusionEnchantment;
import thaumcraft.common.lib.utils.EntityUtils;

/**
 * The axe of the stream: hold right-click to pull the items within 10 blocks to you. Made with Burrowing and Collector.
 * 1.12 set the axe damage to 8 without the material bonus; 1.21 adds the bonus, so the base is 8 minus it.
 */
public class ItemElementalAxe extends AxeItem implements ISubItems {
    public ItemElementalAxe(Properties properties) {
        super(ThaumcraftMaterials.TOOLMAT_ELEMENTAL, properties.attributes(AxeItem.createAttributes(ThaumcraftMaterials.TOOLMAT_ELEMENTAL, 5.0f, -3.0f)));
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }

    @Override
    public void onUseTick(Level level, LivingEntity player, ItemStack stack, int count) {
        List<ItemEntity> stuff = EntityUtils.getEntitiesInRange(level, player.getX(), player.getY(), player.getZ(), player, ItemEntity.class, 10.0);
        for (ItemEntity e : stuff) {
            if (!e.isAlive()) continue;
            double d6 = e.getX() - player.getX();
            double d7 = e.getY() - player.getY() + player.getBbHeight() / 2.0f;
            double d8 = e.getZ() - player.getZ();
            double d9 = Math.sqrt(d6 * d6 + d7 * d7 + d8 * d8);
            d6 /= d9;
            d7 /= d9;
            d8 /= d9;
            double d10 = 0.3;
            Vec3 m = e.getDeltaMovement();
            e.setDeltaMovement(Mth.clamp(m.x - d6 * d10, -0.25, 0.25), Mth.clamp(m.y - (d7 * d10 - 0.1), -0.25, 0.25),
                    Mth.clamp(m.z - d8 * d10, -0.25, 0.25));
            if (level.isClientSide) {
                thaumcraft.client.fx.FXDispatcher.crucibleBubble((float) e.getX() + (level.random.nextFloat() - level.random.nextFloat()) * 0.2f,
                        (float) e.getY() + e.getBbHeight() + (level.random.nextFloat() - level.random.nextFloat()) * 0.2f,
                        (float) e.getZ() + (level.random.nextFloat() - level.random.nextFloat()) * 0.2f, 0.33f, 0.33f, 1.0f);
            }
        }
    }

    @Override
    public void fillSubItems(Consumer<ItemStack> output) {
        ItemStack w1 = new ItemStack(this);
        EnumInfusionEnchantment.addInfusionEnchantment(w1, EnumInfusionEnchantment.BURROWING, 1);
        EnumInfusionEnchantment.addInfusionEnchantment(w1, EnumInfusionEnchantment.COLLECTOR, 1);
        output.accept(w1);
    }
}
