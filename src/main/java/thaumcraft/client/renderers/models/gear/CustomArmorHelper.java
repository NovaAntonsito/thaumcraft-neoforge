package thaumcraft.client.renderers.models.gear;

import java.util.function.Function;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

/**
 * Port of CustomArmorHelper. Chest and boots use the full model (model1, f = 1), helmet and leggings
 * the other one (model2, f = 0.5). Part visibility, sneaking, riding, child and arm poses are now copied
 * from the wearer by NeoForge.
 */
public final class CustomArmorHelper {
    private CustomArmorHelper() {
    }

    public static IClientItemExtensions extensions(Function<Boolean, LayerDefinition> layer, Function<ModelPart, ? extends HumanoidModel<?>> factory) {
        return new IClientItemExtensions() {
            private HumanoidModel<?> model1;
            private HumanoidModel<?> model2;

            @Override
            public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entity, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> original) {
                ArmorItem.Type type = stack.getItem() instanceof ArmorItem armor ? armor.getType() : ArmorItem.Type.HELMET;
                if (type == ArmorItem.Type.CHESTPLATE || type == ArmorItem.Type.BOOTS) {
                    if (model1 == null) {
                        model1 = factory.apply(layer.apply(true).bakeRoot());
                    }
                    return model1;
                }
                if (model2 == null) {
                    model2 = factory.apply(layer.apply(false).bakeRoot());
                }
                return model2;
            }
        };
    }
}
