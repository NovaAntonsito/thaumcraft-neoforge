package thaumcraft.common.items.resources;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import thaumcraft.api.crafting.IDustTrigger;
import thaumcraft.common.lib.SoundsTC;

/** Port of ItemMagicDust (salis mundus). */
public class ItemMagicDust extends Item {
    public ItemMagicDust(Properties properties) {
        super(properties.rarity(Rarity.UNCOMMON));
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Player player = context.getPlayer();
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (player == null || !player.mayUseItemAt(pos, context.getClickedFace(), stack)) {
            return InteractionResult.FAIL;
        }
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        for (IDustTrigger trigger : IDustTrigger.triggers) {
            IDustTrigger.Placement place = trigger.getValidFace(world, player, pos, context.getClickedFace());
            if (place != null) {
                player.swing(context.getHand());
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                trigger.execute(world, player, pos, place, context.getClickedFace());
                if (world.isClientSide) {
                    doSparkles(world, trigger.sparkle(world, player, pos, place));
                }
                return InteractionResult.sidedSuccess(world.isClientSide);
            }
        }
        return InteractionResult.PASS;
    }

    /** TODO: the original FXDispatcher sparkles (stage 10). Vanilla particles for now. */
    private static void doSparkles(Level world, java.util.List<BlockPos> sparkles) {
        for (BlockPos p : sparkles) {
            for (int a = 0; a < 20; a++) {
                world.addParticle(ParticleTypes.ENCHANT, p.getX() + world.random.nextDouble(), p.getY() + 1.0 + world.random.nextDouble() * 0.5,
                        p.getZ() + world.random.nextDouble(), 0, 0.05, 0);
            }
            world.playLocalSound(p, SoundsTC.DUST.get(), SoundSource.PLAYERS, 0.33f, 1.0f + (float) world.random.nextGaussian() * 0.05f, false);
        }
    }
}
