package thaumcraft.client.renderers.models.gear;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import thaumcraft.common.items.armor.ItemFortressArmor;

/**
 * Thaumium fortress armor. full (f >= 1) is the chest layer, with the plates, the scroll and the book; the other
 * layer has the belt. The more pieces of the set are worn, the more ornaments show (checkSet). The helm shows its
 * mask and goggles.
 * 1.21: the set is read from the wearer every time the model is set up, not every 20 ticks into static maps.
 */
public class ModelFortressArmor extends ModelCustomArmor {
    private static final String[] SET2 = { "book", "flap_l", "flap_r", "shoulderplate_ltop", "shoulderplate_l1", "shoulderplate_rtop",
            "shoulderplate_r1", "sidepanel_r2", "sidepanel_l2" };
    private static final String[] SET3 = { "scroll", "ornament_l", "ornament_l2", "ornament_r", "ornament_r2", "gemornament", "gem",
            "shoulderplate_l2", "shoulderplate_l3", "shoulderplate_r2", "shoulderplate_r3", "sidepanel_r3", "sidepanel_l3" };

    public ModelFortressArmor(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createLayer(boolean full) {
        MeshDefinition mesh = createEmptyBiped();
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.getChild("head");
        PartDefinition body = root.getChild("body");
        PartDefinition rightArm = root.getChild("right_arm");
        PartDefinition leftArm = root.getChild("left_arm");
        PartDefinition rightLeg = root.getChild("right_leg");
        PartDefinition leftLeg = root.getChild("left_leg");
        head.addOrReplaceChild("mask0", CubeListBuilder.create().texOffs(52, 2).addBox(-4.5f, -5.0f, -4.6f, 9, 5, 1), PartPose.ZERO);
        head.addOrReplaceChild("mask1", CubeListBuilder.create().texOffs(76, 2).addBox(-4.5f, -5.0f, -4.6f, 9, 5, 1), PartPose.ZERO);
        head.addOrReplaceChild("mask2", CubeListBuilder.create().texOffs(100, 2).addBox(-4.5f, -5.0f, -4.6f, 9, 5, 1), PartPose.ZERO);
        head.addOrReplaceChild("goggles", CubeListBuilder.create().texOffs(100, 18).addBox(-4.5f, -5.0f, -4.25f, 9, 5, 1), PartPose.ZERO);
        head.addOrReplaceChild("ornament_l", CubeListBuilder.create().texOffs(78, 8).mirror().addBox(1.5f, -9.0f, -6.5f, 2, 2, 1), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, -0.1396263f, 0.0f, 0.0f));
        head.addOrReplaceChild("ornament_l2", CubeListBuilder.create().texOffs(78, 8).mirror().addBox(3.5f, -10.0f, -6.5f, 1, 2, 1), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, -0.1396263f, 0.0f, 0.0f));
        head.addOrReplaceChild("ornament_r", CubeListBuilder.create().texOffs(78, 8).addBox(-3.5f, -9.0f, -6.5f, 2, 2, 1), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, -0.1396263f, 0.0f, 0.0f));
        head.addOrReplaceChild("ornament_r2", CubeListBuilder.create().texOffs(78, 8).addBox(-4.5f, -10.0f, -6.5f, 1, 2, 1), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, -0.1396263f, 0.0f, 0.0f));
        head.addOrReplaceChild("helmet", CubeListBuilder.create().texOffs(41, 8).addBox(-4.5f, -9.0f, -4.5f, 9, 4, 9), PartPose.ZERO);
        head.addOrReplaceChild("helmet_r", CubeListBuilder.create().texOffs(21, 13).addBox(-6.5f, -3.0f, -4.5f, 1, 5, 9), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.5235988f));
        head.addOrReplaceChild("helmet_l", CubeListBuilder.create().texOffs(21, 13).mirror().addBox(5.5f, -3.0f, -4.5f, 1, 5, 9), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -0.5235988f));
        head.addOrReplaceChild("helmet_b", CubeListBuilder.create().texOffs(41, 21).addBox(-4.5f, -3.0f, 5.5f, 9, 5, 1), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.5235988f, 0.0f, 0.0f));
        head.addOrReplaceChild("capsthingy", CubeListBuilder.create().texOffs(21, 0).addBox(-4.5f, -6.0f, -6.5f, 9, 1, 2), PartPose.ZERO);
        head.addOrReplaceChild("flap_r", CubeListBuilder.create().texOffs(59, 10).addBox(-10.0f, -2.0f, -1.0f, 3, 3, 1), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, -0.5235988f, 0.5235988f));
        head.addOrReplaceChild("flap_l", CubeListBuilder.create().texOffs(59, 10).mirror().addBox(7.0f, -2.0f, -1.0f, 3, 3, 1), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.5235988f, -0.5235988f));
        head.addOrReplaceChild("gemornament", CubeListBuilder.create().texOffs(68, 11).addBox(-1.5f, -9.0f, -7.0f, 3, 3, 2), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, -0.1396263f, 0.0f, 0.0f));
        head.addOrReplaceChild("gem", CubeListBuilder.create().texOffs(72, 8).addBox(-1.0f, -8.5f, -7.5f, 2, 2, 1), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, -0.1396263f, 0.0f, 0.0f));
        if (full) body.addOrReplaceChild("belt_r", CubeListBuilder.create().texOffs(76, 44).addBox(-5.0f, 4.0f, -3.0f, 1, 3, 6), PartPose.ZERO);
        if (!full) body.addOrReplaceChild("mbelt", CubeListBuilder.create().texOffs(56, 55).addBox(-4.0f, 8.0f, -3.0f, 8, 4, 1), PartPose.ZERO);
        if (!full) body.addOrReplaceChild("mbelt_l", CubeListBuilder.create().texOffs(76, 44).addBox(4.0f, 8.0f, -3.0f, 1, 3, 6), PartPose.ZERO);
        if (!full) body.addOrReplaceChild("mbelt_r", CubeListBuilder.create().texOffs(76, 44).addBox(-5.0f, 8.0f, -3.0f, 1, 3, 6), PartPose.ZERO);
        if (full) body.addOrReplaceChild("belt_l", CubeListBuilder.create().texOffs(76, 44).addBox(4.0f, 4.0f, -3.0f, 1, 3, 6), PartPose.ZERO);
        if (full) body.addOrReplaceChild("chestplate", CubeListBuilder.create().texOffs(56, 45).addBox(-4.0f, 1.0f, -4.0f, 8, 7, 2), PartPose.ZERO);
        if (full) body.addOrReplaceChild("scroll", CubeListBuilder.create().texOffs(34, 27).addBox(-2.0f, 9.5f, 4.0f, 8, 3, 3), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.1919862f));
        if (full) body.addOrReplaceChild("backplate", CubeListBuilder.create().texOffs(36, 45).addBox(-4.0f, 1.0f, 2.0f, 8, 11, 2), PartPose.ZERO);
        if (full) body.addOrReplaceChild("book", CubeListBuilder.create().texOffs(100, 8).addBox(1.0f, -0.3f, 4.0f, 5, 7, 2), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.7679449f));
        rightArm.addOrReplaceChild("shoulder_r", CubeListBuilder.create().texOffs(56, 35).addBox(-3.5f, -2.5f, -2.5f, 5, 5, 5), PartPose.ZERO);
        rightArm.addOrReplaceChild("gauntlet_r", CubeListBuilder.create().texOffs(100, 26).addBox(-3.5f, 3.5f, -2.5f, 2, 6, 5), PartPose.ZERO);
        rightArm.addOrReplaceChild("gauntletstrap_r1", CubeListBuilder.create().texOffs(84, 31).addBox(-1.5f, 3.5f, -2.5f, 3, 1, 5), PartPose.ZERO);
        rightArm.addOrReplaceChild("gauntletstrap_r2", CubeListBuilder.create().texOffs(84, 31).addBox(-1.5f, 6.5f, -2.5f, 3, 1, 5), PartPose.ZERO);
        rightArm.addOrReplaceChild("shoulderplate_rtop", CubeListBuilder.create().texOffs(110, 37).addBox(-5.5f, -2.5f, -3.5f, 2, 1, 7), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.4363323f));
        rightArm.addOrReplaceChild("shoulderplate_r1", CubeListBuilder.create().texOffs(110, 45).addBox(-4.5f, -1.5f, -3.5f, 1, 4, 7), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.4363323f));
        rightArm.addOrReplaceChild("shoulderplate_r2", CubeListBuilder.create().texOffs(94, 45).addBox(-3.5f, 1.5f, -3.5f, 1, 3, 7), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.4363323f));
        rightArm.addOrReplaceChild("shoulderplate_r3", CubeListBuilder.create().texOffs(94, 45).addBox(-2.5f, 3.5f, -3.5f, 1, 3, 7), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.4363323f));
        leftArm.addOrReplaceChild("shoulder_l", CubeListBuilder.create().texOffs(56, 35).mirror().addBox(-1.5f, -2.5f, -2.5f, 5, 5, 5), PartPose.ZERO);
        leftArm.addOrReplaceChild("gauntlet_l", CubeListBuilder.create().texOffs(114, 26).addBox(1.5f, 3.5f, -2.5f, 2, 6, 5), PartPose.ZERO);
        leftArm.addOrReplaceChild("gauntletstrapl1", CubeListBuilder.create().texOffs(84, 31).mirror().addBox(-1.5f, 3.5f, -2.5f, 3, 1, 5), PartPose.ZERO);
        leftArm.addOrReplaceChild("gauntletstrap_l2", CubeListBuilder.create().texOffs(84, 31).mirror().addBox(-1.5f, 6.5f, -2.5f, 3, 1, 5), PartPose.ZERO);
        leftArm.addOrReplaceChild("shoulderplate_ltop", CubeListBuilder.create().texOffs(110, 37).mirror().addBox(3.5f, -2.5f, -3.5f, 2, 1, 7), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -0.4363323f));
        leftArm.addOrReplaceChild("shoulderplate_l1", CubeListBuilder.create().texOffs(110, 45).mirror().addBox(3.5f, -1.5f, -3.5f, 1, 4, 7), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -0.4363323f));
        leftArm.addOrReplaceChild("shoulderplate_l2", CubeListBuilder.create().texOffs(94, 45).mirror().addBox(2.5f, 1.5f, -3.5f, 1, 3, 7), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -0.4363323f));
        leftArm.addOrReplaceChild("shoulderplate_l3", CubeListBuilder.create().texOffs(94, 45).mirror().addBox(1.5f, 3.5f, -3.5f, 1, 3, 7), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -0.4363323f));
        rightLeg.addOrReplaceChild("legpanel_r1", CubeListBuilder.create().texOffs(0, 51).addBox(-1.0f, 0.5f, -3.5f, 3, 4, 1), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, -0.4363323f, 0.0f, 0.0f));
        rightLeg.addOrReplaceChild("legpanel_r2", CubeListBuilder.create().texOffs(8, 51).addBox(-1.0f, 3.5f, -2.5f, 3, 4, 1), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, -0.4363323f, 0.0f, 0.0f));
        rightLeg.addOrReplaceChild("legpanel_r3", CubeListBuilder.create().texOffs(0, 56).addBox(-1.0f, 6.5f, -1.5f, 3, 3, 1), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, -0.4363323f, 0.0f, 0.0f));
        rightLeg.addOrReplaceChild("legpanel_r4", CubeListBuilder.create().texOffs(0, 43).addBox(-3.0f, 0.5f, -3.5f, 2, 3, 1), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, -0.4363323f, 0.0f, 0.0f));
        rightLeg.addOrReplaceChild("legpanel_r5", CubeListBuilder.create().texOffs(0, 47).addBox(-3.0f, 2.5f, -2.5f, 2, 3, 1), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, -0.4363323f, 0.0f, 0.0f));
        rightLeg.addOrReplaceChild("legpanel_r6", CubeListBuilder.create().texOffs(6, 43).addBox(-3.0f, 4.5f, -1.5f, 2, 3, 1), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, -0.4363323f, 0.0f, 0.0f));
        rightLeg.addOrReplaceChild("sidepanel_r1", CubeListBuilder.create().texOffs(0, 22).addBox(-2.5f, 0.5f, -2.5f, 1, 4, 5), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.4363323f));
        rightLeg.addOrReplaceChild("sidepanel_r2", CubeListBuilder.create().texOffs(0, 31).addBox(-1.5f, 3.5f, -2.5f, 1, 3, 5), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.4363323f));
        rightLeg.addOrReplaceChild("sidepanel_r3", CubeListBuilder.create().texOffs(12, 31).addBox(-0.5f, 5.5f, -2.5f, 1, 3, 5), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.4363323f));
        rightLeg.addOrReplaceChild("backpanel_r1", CubeListBuilder.create().texOffs(0, 18).addBox(-3.0f, 0.5f, 2.5f, 5, 3, 1), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.4363323f, 0.0f, 0.0f));
        rightLeg.addOrReplaceChild("backpanel_r2", CubeListBuilder.create().texOffs(0, 18).addBox(-3.0f, 2.5f, 1.5f, 5, 3, 1), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.4363323f, 0.0f, 0.0f));
        rightLeg.addOrReplaceChild("backpanel_r3", CubeListBuilder.create().texOffs(0, 18).addBox(-3.0f, 4.5f, 0.5f, 5, 3, 1), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.4363323f, 0.0f, 0.0f));
        leftLeg.addOrReplaceChild("backpanel_l3", CubeListBuilder.create().texOffs(0, 18).mirror().addBox(-2.0f, 4.5f, 0.5f, 5, 3, 1), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.4363323f, 0.0f, 0.0f));
        leftLeg.addOrReplaceChild("legpanel_l1", CubeListBuilder.create().texOffs(0, 51).mirror().addBox(-2.0f, 0.5f, -3.5f, 3, 4, 1), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, -0.4363323f, 0.0f, 0.0f));
        leftLeg.addOrReplaceChild("legpanel_l2", CubeListBuilder.create().texOffs(8, 51).mirror().addBox(-2.0f, 3.5f, -2.5f, 3, 4, 1), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, -0.4363323f, 0.0f, 0.0f));
        leftLeg.addOrReplaceChild("legpanel_l3", CubeListBuilder.create().texOffs(0, 56).mirror().addBox(-2.0f, 6.5f, -1.5f, 3, 3, 1), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, -0.4363323f, 0.0f, 0.0f));
        leftLeg.addOrReplaceChild("legpanel_l4", CubeListBuilder.create().texOffs(0, 43).mirror().addBox(1.0f, 0.5f, -3.5f, 2, 3, 1), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, -0.4363323f, 0.0f, 0.0f));
        leftLeg.addOrReplaceChild("legpanel_l5", CubeListBuilder.create().texOffs(0, 47).mirror().addBox(1.0f, 2.5f, -2.5f, 2, 3, 1), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, -0.4363323f, 0.0f, 0.0f));
        leftLeg.addOrReplaceChild("legpanel_l6", CubeListBuilder.create().texOffs(6, 43).mirror().addBox(1.0f, 4.5f, -1.5f, 2, 3, 1), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, -0.4363323f, 0.0f, 0.0f));
        leftLeg.addOrReplaceChild("sidepanel_l1", CubeListBuilder.create().texOffs(0, 22).mirror().addBox(1.5f, 0.5f, -2.5f, 1, 4, 5), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -0.4363323f));
        leftLeg.addOrReplaceChild("sidepanel_l2", CubeListBuilder.create().texOffs(0, 31).mirror().addBox(0.5f, 3.5f, -2.5f, 1, 3, 5), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -0.4363323f));
        leftLeg.addOrReplaceChild("sidepanel_l3", CubeListBuilder.create().texOffs(12, 31).mirror().addBox(-0.5f, 5.5f, -2.5f, 1, 3, 5), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -0.4363323f));
        leftLeg.addOrReplaceChild("backpanel_l1", CubeListBuilder.create().texOffs(0, 18).mirror().addBox(-2.0f, 0.5f, 2.5f, 5, 3, 1), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.4363323f, 0.0f, 0.0f));
        leftLeg.addOrReplaceChild("backpanel_l2", CubeListBuilder.create().texOffs(0, 18).mirror().addBox(-2.0f, 2.5f, 1.5f, 5, 3, 1), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.4363323f, 0.0f, 0.0f));

        return LayerDefinition.create(mesh, 128, 64);
    }

    /** checkSet and the visibility part of render(). */
    @Override
    public void prepare(LivingEntity entity) {
        int set = 0;
        int mask = -1;
        boolean goggles = false;
        for (EquipmentSlot slot : new EquipmentSlot[] { EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD }) {
            ItemStack piece = entity.getItemBySlot(slot);
            if (piece.getItem() instanceof ItemFortressArmor) {
                ++set;
                if (slot == EquipmentSlot.HEAD) {
                    mask = ItemFortressArmor.getMask(piece);
                    goggles = ItemFortressArmor.hasGoggles(piece);
                }
            }
        }
        if (set == 0) {
            set = -1;
        }
        show("goggles", goggles);
        for (int a = 0; a < 3; ++a) {
            show("mask" + a, mask == a);
        }
        for (String part : SET2) {
            show(part, set >= 2);
        }
        for (String part : SET3) {
            show(part, set >= 3);
        }
    }

    private void show(String name, boolean visible) {
        for (ModelPart part : new ModelPart[] { head, body, rightArm, leftArm, rightLeg, leftLeg }) {
            if (part.hasChild(name)) {
                part.getChild(name).visible = visible;
            }
        }
    }
}
