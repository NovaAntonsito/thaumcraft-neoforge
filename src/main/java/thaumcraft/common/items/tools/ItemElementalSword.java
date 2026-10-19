package thaumcraft.common.items.tools;

import java.util.List;
import java.util.function.Consumer;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import thaumcraft.api.ThaumcraftMaterials;
import thaumcraft.client.fx.ClientFX;
import thaumcraft.common.items.ISubItems;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.lib.enchantment.EnumInfusionEnchantment;
import thaumcraft.common.lib.utils.EntityUtils;

/**
 * The sword of the zephyr: hold right-click to rise on a whirlwind that throws the mobs around you away. It wears 1
 * every second. Made with Arcing II.
 */
public class ItemElementalSword extends SwordItem implements ISubItems {
    public ItemElementalSword(Properties properties) {
        super(ThaumcraftMaterials.TOOLMAT_ELEMENTAL, properties.attributes(SwordItem.createAttributes(ThaumcraftMaterials.TOOLMAT_ELEMENTAL, 3, -2.4f)));
    }

    @Override
    public void fillSubItems(Consumer<ItemStack> output) {
        ItemStack w1 = new ItemStack(this);
        EnumInfusionEnchantment.addInfusionEnchantment(w1, EnumInfusionEnchantment.ARCING, 2);
        output.accept(w1);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
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
        super.onUseTick(level, player, stack, count);
        int ticks = getUseDuration(stack, player) - count;
        Vec3 m = player.getDeltaMovement();
        double my = m.y;
        if (my < 0.0) {
            my /= 1.2000000476837158;
            player.fallDistance /= 1.2f;
        }
        my += 0.07999999821186066;
        if (my > 0.5) {
            my = 0.20000000298023224;
        }
        player.setDeltaMovement(m.x, my, m.z);
        if (player instanceof ServerPlayer sp) {
            EntityUtils.resetFloatCounter(sp);
        }
        List<Entity> targets = level.getEntities(player, player.getBoundingBox().inflate(2.5, 2.5, 2.5));
        for (Entity entity : targets) {
            if (entity instanceof Player || !(entity instanceof LivingEntity) || !entity.isAlive()) continue;
            if (player.getVehicle() != null && player.getVehicle() == entity) continue;
            Vec3 p = player.position();
            Vec3 t = entity.position();
            double distance = p.distanceTo(t) + 0.1;
            Vec3 r = t.subtract(p);
            entity.setDeltaMovement(entity.getDeltaMovement().add(r.x / 2.5 / distance, r.y / 2.5 / distance, r.z / 2.5 / distance));
        }
        if (level.isClientSide) {
            int miny = (int) (player.getBoundingBox().minY - 2.0);
            if (player.onGround()) {
                miny = Mth.floor(player.getBoundingBox().minY);
            }
            for (int a = 0; a < 5; ++a) {
                thaumcraft.client.fx.FXDispatcher.smokeSpiral(player.getX(), player.getBoundingBox().minY + player.getBbHeight() / 2.0f, player.getZ(), 1.5f,
                        level.random.nextInt(360), miny, 14540253);
            }
            if (player.onGround()) {
                float r2 = level.random.nextFloat() * 360.0f;
                float mx = -Mth.sin(r2 / 180.0f * 3.1415927f) / 5.0f;
                float mz = Mth.cos(r2 / 180.0f * 3.1415927f) / 5.0f;
                level.addParticle(ParticleTypes.SMOKE, player.getX(), player.getBoundingBox().minY + 0.10000000149011612, player.getZ(), mx, 0.0, mz);
            }
        } else if (ticks == 0 || ticks % 20 == 0) {
            player.playSound(SoundsTC.WIND.get(), 0.5f, 0.9f + level.random.nextFloat() * 0.2f);
        }
        if (ticks % 20 == 0) {
            stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(player.getUsedItemHand()));
        }
    }
}
