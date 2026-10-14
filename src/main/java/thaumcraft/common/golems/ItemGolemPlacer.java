package thaumcraft.common.golems;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.api.golems.EnumGolemTrait;
import thaumcraft.api.golems.IGolemProperties;
import thaumcraft.api.golems.ISealDisplayer;
import thaumcraft.api.golems.parts.GolemArm;
import thaumcraft.api.golems.parts.GolemHead;
import thaumcraft.api.golems.parts.GolemMaterial;
import thaumcraft.common.registry.TCDataComponents;
import thaumcraft.common.registry.TCEntities;

/** Port of ItemGolemPlacer: a packed golem. Its parts and rank xp are the GOLEM_PROPS and GOLEM_XP components. */
public class ItemGolemPlacer extends Item implements ISealDisplayer {
    public ItemGolemPlacer(Properties properties) {
        super(properties);
    }

    public static ItemStack withProps(Item item, long props) {
        ItemStack is = new ItemStack(item);
        is.set(TCDataComponents.GOLEM_PROPS.get(), props);
        return is;
    }

    /** getSubItems: a basic golem and three sample builds. */
    public static List<ItemStack> creativeStacks(Item item) {
        IGolemProperties p1 = new GolemProperties();
        p1.setHead(GolemHead.getHeads()[1]);
        p1.setArms(GolemArm.getArms()[1]);
        IGolemProperties p2 = new GolemProperties();
        p2.setMaterial(GolemMaterial.getMaterials()[1]);
        p2.setHead(GolemHead.getHeads()[1]);
        p2.setArms(GolemArm.getArms()[2]);
        IGolemProperties p3 = new GolemProperties();
        p3.setMaterial(GolemMaterial.getMaterials()[4]);
        p3.setHead(GolemHead.getHeads()[1]);
        p3.setArms(GolemArm.getArms()[3]);
        return List.of(withProps(item, 0L), withProps(item, p1.toLong()), withProps(item, p2.toLong()), withProps(item, p3.toLong()));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        Long l = stack.get(TCDataComponents.GOLEM_PROPS.get());
        if (l != null && GolemMaterial.getMaterials()[0] != null) {
            IGolemProperties props = GolemProperties.fromLong(l);
            if (props.hasTrait(EnumGolemTrait.SMART)) {
                if (props.getRank() >= 10) {
                    tooltip.add(Component.translatable("golem.rank").append(" " + props.getRank()).withStyle(ChatFormatting.GOLD));
                } else {
                    int rx = stack.getOrDefault(TCDataComponents.GOLEM_XP.get(), 0);
                    int xn = (props.getRank() + 1) * (props.getRank() + 1) * 1000;
                    tooltip.add(Component.translatable("golem.rank").append(" " + props.getRank()).withStyle(ChatFormatting.GOLD)
                            .append(Component.literal(" (" + rx + "/" + xn + ")").withStyle(ChatFormatting.DARK_GREEN)));
                }
            }
            tooltip.add(props.getMaterial().getLocalizedName().copy().withStyle(ChatFormatting.GREEN));
            for (EnumGolemTrait tag : props.getTraits()) {
                tooltip.add(Component.literal("-").append(tag.getLocalizedName()).withStyle(ChatFormatting.BLUE));
            }
        }
        super.appendHoverText(stack, context, tooltip, flag);
    }

    /** onItemUseFirst: spawns the golem on the clicked face of a solid block, owned by the player. */
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState bs = world.getBlockState(pos);
        if (!bs.isSolid()) {
            return InteractionResult.FAIL;
        }
        if (world.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        pos = pos.relative(context.getClickedFace());
        if (player == null || !player.mayUseItemAt(pos, context.getClickedFace(), stack)) {
            return InteractionResult.FAIL;
        }
        EntityThaumcraftGolem golem = TCEntities.GOLEM.get().create(world);
        if (golem != null) {
            golem.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0.0f, 0.0f);
            golem.setOwned(true);
            golem.setValidSpawn();
            golem.setOwnerId(player.getUUID());
            Long props = stack.get(TCDataComponents.GOLEM_PROPS.get());
            if (props != null) {
                golem.setProperties(GolemProperties.fromLong(props));
            }
            Integer xp = stack.get(TCDataComponents.GOLEM_XP.get());
            if (xp != null) {
                golem.rankXp = xp;
            }
            golem.onPlaced();
            if (((ServerLevel) world).addFreshEntity(golem) && !player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        return InteractionResult.SUCCESS;
    }
}
