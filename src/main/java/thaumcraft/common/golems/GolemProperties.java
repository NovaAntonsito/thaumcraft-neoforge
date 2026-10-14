package thaumcraft.common.golems;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.golems.EnumGolemTrait;
import thaumcraft.api.golems.IGolemProperties;
import thaumcraft.api.golems.parts.GolemAddon;
import thaumcraft.api.golems.parts.GolemArm;
import thaumcraft.api.golems.parts.GolemHead;
import thaumcraft.api.golems.parts.GolemLeg;
import thaumcraft.api.golems.parts.GolemMaterial;
import thaumcraft.api.golems.parts.PartModel;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.golems.client.PartModelBreakers;
import thaumcraft.common.golems.client.PartModelClaws;
import thaumcraft.common.golems.client.PartModelDarts;
import thaumcraft.common.golems.client.PartModelHauler;
import thaumcraft.common.golems.client.PartModelWheel;
import thaumcraft.common.golems.parts.GolemArmDart;
import thaumcraft.common.golems.parts.GolemLegLevitator;
import thaumcraft.common.golems.parts.GolemLegWheels;

/**
 * Port of GolemProperties: a golem's parts packed in a long, one byte each (material, head, arms, legs, addon, rank).
 * The default parts were registered in a static block; they need the registered items, so here
 * {@link #registerDefaults} runs at common setup.
 */
public class GolemProperties implements IGolemProperties {
    private long data;
    private Set<EnumGolemTrait> traitCache;

    public GolemProperties() {
        data = 0L;
        traitCache = null;
    }

    @Override
    public Set<EnumGolemTrait> getTraits() {
        if (traitCache == null) {
            traitCache = new HashSet<>();
            for (EnumGolemTrait trait : getMaterial().traits) {
                addTraitSmart(trait);
            }
            for (EnumGolemTrait trait : getHead().traits) {
                addTraitSmart(trait);
            }
            for (EnumGolemTrait trait : getArms().traits) {
                addTraitSmart(trait);
            }
            for (EnumGolemTrait trait : getLegs().traits) {
                addTraitSmart(trait);
            }
            for (EnumGolemTrait trait : getAddon().traits) {
                addTraitSmart(trait);
            }
        }
        return traitCache;
    }

    private void addTraitSmart(EnumGolemTrait trait) {
        if (trait.opposite != null && traitCache.contains(trait.opposite)) {
            traitCache.remove(trait.opposite);
        } else {
            traitCache.add(trait);
        }
    }

    @Override
    public boolean hasTrait(EnumGolemTrait trait) {
        return getTraits().contains(trait);
    }

    @Override
    public void setMaterial(GolemMaterial mat) {
        data = ThaumcraftApiHelper.setByteInLong(data, mat.id, 0);
        traitCache = null;
    }

    @Override
    public GolemMaterial getMaterial() {
        return GolemMaterial.getMaterials()[ThaumcraftApiHelper.getByteInLong(data, 0)];
    }

    @Override
    public void setHead(GolemHead mat) {
        data = ThaumcraftApiHelper.setByteInLong(data, mat.id, 1);
        traitCache = null;
    }

    @Override
    public GolemHead getHead() {
        return GolemHead.getHeads()[ThaumcraftApiHelper.getByteInLong(data, 1)];
    }

    @Override
    public void setArms(GolemArm mat) {
        data = ThaumcraftApiHelper.setByteInLong(data, mat.id, 2);
        traitCache = null;
    }

    @Override
    public GolemArm getArms() {
        return GolemArm.getArms()[ThaumcraftApiHelper.getByteInLong(data, 2)];
    }

    @Override
    public void setLegs(GolemLeg mat) {
        data = ThaumcraftApiHelper.setByteInLong(data, mat.id, 3);
        traitCache = null;
    }

    @Override
    public GolemLeg getLegs() {
        return GolemLeg.getLegs()[ThaumcraftApiHelper.getByteInLong(data, 3)];
    }

    @Override
    public void setAddon(GolemAddon mat) {
        data = ThaumcraftApiHelper.setByteInLong(data, mat.id, 4);
        traitCache = null;
    }

    @Override
    public GolemAddon getAddon() {
        return GolemAddon.getAddons()[ThaumcraftApiHelper.getByteInLong(data, 4)];
    }

    @Override
    public void setRank(int rank) {
        data = ThaumcraftApiHelper.setByteInLong(data, (byte) rank, 5);
    }

    @Override
    public int getRank() {
        return ThaumcraftApiHelper.getByteInLong(data, 5);
    }

    public static IGolemProperties fromLong(long d) {
        GolemProperties out = new GolemProperties();
        out.data = d;
        return out;
    }

    @Override
    public long toLong() {
        return data;
    }

    @Override
    public ItemStack[] generateComponents() {
        ArrayList<ItemStack> comps = new ArrayList<>();
        ItemStack base = getMaterial().componentBase;
        ItemStack mech = getMaterial().componentMechanism;
        addToList(comps, base, 2);
        addToList(comps, mech, 1);
        addToListFromComps(comps, getArms().components, getMaterial());
        addToListFromComps(comps, getLegs().components, getMaterial());
        addToListFromComps(comps, getHead().components, getMaterial());
        addToListFromComps(comps, getAddon().components, getMaterial());
        return comps.toArray(new ItemStack[0]);
    }

    private static void addToListFromComps(ArrayList<ItemStack> comps, Object[] objs, GolemMaterial mat) {
        for (Object o : objs) {
            if (o instanceof ItemStack stack) {
                addToList(comps, stack, 1);
            } else if (o instanceof String s) {
                if (s.equalsIgnoreCase("base")) {
                    addToList(comps, mat.componentBase, 1);
                } else if (s.equalsIgnoreCase("mech")) {
                    addToList(comps, mat.componentMechanism, 1);
                }
            }
        }
    }

    private static void addToList(ArrayList<ItemStack> comps, ItemStack newItem, int mult) {
        for (ItemStack stack : comps) {
            if (ItemStack.isSameItemSameComponents(stack, newItem)) {
                stack.grow(newItem.getCount() * mult);
                return;
            }
        }
        ItemStack stack2 = newItem.copy();
        stack2.setCount(stack2.getCount() * mult);
        comps.add(stack2);
    }

    private static ResourceLocation tc(String path) {
        return ResourceLocation.fromNamespaceAndPath("thaumcraft", path);
    }

    private static PartModel model(String obj, String texture, PartModel.EnumAttachPoint point) {
        return new PartModel(tc("models/obj/" + obj + ".obj"), texture == null ? null : tc(texture), point);
    }

    private static ItemStack stack(net.minecraft.world.level.ItemLike item, int count) {
        return new ItemStack(item, count);
    }

    private static boolean registered = false;

    /** The original static block: the default materials and parts, in the original order (their ids depend on it). */
    public static void registerDefaults() {
        if (registered) return;
        registered = true;
        ItemStack mechanism = new ItemStack(ConfigItems.mechanismSimple.get());
        ItemStack mindClockwork = new ItemStack(ConfigItems.mind.get("clockwork").get());
        ItemStack mindBiothaumic = new ItemStack(ConfigItems.mind.get("biothaumic").get());
        ItemStack moduleVision = new ItemStack(ConfigItems.moduleVision.get());
        ItemStack moduleAggression = new ItemStack(ConfigItems.moduleAggression.get());
        GolemMaterial.register(new GolemMaterial("WOOD", new String[]{"MATSTUDWOOD"}, tc("textures/entity/golems/mat_wood.png"), 5059370, 6, 2, 1,
                new ItemStack(ConfigBlocks.plankGreatwood.get()), mechanism, new EnumGolemTrait[]{EnumGolemTrait.LIGHT}));
        GolemMaterial.register(new GolemMaterial("IRON", new String[]{"MATSTUDIRON"}, tc("textures/entity/golems/mat_iron.png"), 16777215, 20, 8, 3,
                new ItemStack(ConfigItems.plate.get("iron").get()), mechanism,
                new EnumGolemTrait[]{EnumGolemTrait.HEAVY, EnumGolemTrait.FIREPROOF, EnumGolemTrait.BLASTPROOF}));
        // Blocks.HARDENED_CLAY is plain terracotta.
        GolemMaterial.register(new GolemMaterial("CLAY", new String[]{"MATSTUDCLAY"}, tc("textures/entity/golems/mat_clay.png"), 13071447, 10, 4, 2,
                new ItemStack(Blocks.TERRACOTTA), mechanism, new EnumGolemTrait[]{EnumGolemTrait.FIREPROOF}));
        GolemMaterial.register(new GolemMaterial("BRASS", new String[]{"MATSTUDBRASS"}, tc("textures/entity/golems/mat_brass.png"), 15638812, 16, 6, 3,
                new ItemStack(ConfigItems.plate.get("brass").get()), mechanism, new EnumGolemTrait[]{EnumGolemTrait.LIGHT}));
        GolemMaterial.register(new GolemMaterial("THAUMIUM", new String[]{"MATSTUDTHAUMIUM"}, tc("textures/entity/golems/mat_thaumium.png"), 5257074, 24, 10, 4,
                new ItemStack(ConfigItems.plate.get("thaumium").get()), mechanism,
                new EnumGolemTrait[]{EnumGolemTrait.HEAVY, EnumGolemTrait.FIREPROOF, EnumGolemTrait.BLASTPROOF}));
        GolemMaterial.register(new GolemMaterial("VOID", new String[]{"MATSTUDVOID"}, tc("textures/entity/golems/mat_void.png"), 1445161, 20, 6, 4,
                new ItemStack(ConfigItems.plate.get("void").get()), mechanism, new EnumGolemTrait[]{EnumGolemTrait.REPAIR}));

        GolemHead.register(new GolemHead("BASIC", new String[]{"MINDCLOCKWORK"}, tc("textures/misc/golem/head_basic.png"),
                model("golem_head_basic", null, PartModel.EnumAttachPoint.HEAD), new Object[]{mindClockwork}, new EnumGolemTrait[0]));
        GolemHead.register(new GolemHead("SMART", new String[]{"MINDBIOTHAUMIC"}, tc("textures/misc/golem/head_smart.png"),
                model("golem_head_smart", "textures/entity/golems/golem_head_other.png", PartModel.EnumAttachPoint.HEAD), new Object[]{mindBiothaumic},
                new EnumGolemTrait[]{EnumGolemTrait.SMART, EnumGolemTrait.FRAGILE}));
        // new ItemStack(ItemsTC.plate) is metadata 0, a brass plate; Blocks.WOOL is white wool.
        GolemHead.register(new GolemHead("SMART_ARMORED", new String[]{"MINDBIOTHAUMIC", "GOLEMCOMBATADV"}, tc("textures/misc/golem/head_smartarmor.png"),
                model("golem_head_smart_armor", null, PartModel.EnumAttachPoint.HEAD),
                new Object[]{mindBiothaumic, new ItemStack(ConfigItems.plate.get("brass").get()), "base", new ItemStack(Blocks.WHITE_WOOL)},
                new EnumGolemTrait[]{EnumGolemTrait.SMART}));
        GolemHead.register(new GolemHead("SCOUT", new String[]{"GOLEMVISION"}, tc("textures/misc/golem/head_scout.png"),
                model("golem_head_scout", "textures/entity/golems/golem_head_other.png", PartModel.EnumAttachPoint.HEAD),
                new Object[]{mindClockwork, moduleVision}, new EnumGolemTrait[]{EnumGolemTrait.SCOUT, EnumGolemTrait.FRAGILE}));
        GolemHead.register(new GolemHead("SMART_SCOUT", new String[]{"GOLEMVISION", "MINDBIOTHAUMIC"}, tc("textures/misc/golem/head_smartscout.png"),
                model("golem_head_scout_smart", "textures/entity/golems/golem_head_other.png", PartModel.EnumAttachPoint.HEAD),
                new Object[]{mindBiothaumic, moduleVision}, new EnumGolemTrait[]{EnumGolemTrait.SCOUT, EnumGolemTrait.SMART, EnumGolemTrait.FRAGILE}));

        GolemArm.register(new GolemArm("BASIC", new String[]{"MINDCLOCKWORK"}, tc("textures/misc/golem/arms_basic.png"),
                model("golem_arms_basic", null, PartModel.EnumAttachPoint.ARMS), new Object[0], new EnumGolemTrait[0]));
        GolemArm.register(new GolemArm("FINE", new String[]{"MATSTUDBRASS"}, tc("textures/misc/golem/arms_fine.png"),
                model("golem_arms_fine", null, PartModel.EnumAttachPoint.ARMS), new Object[]{mechanism, "base"},
                new EnumGolemTrait[]{EnumGolemTrait.DEFT, EnumGolemTrait.FRAGILE}));
        GolemArm.register(new GolemArm("CLAWS", new String[]{"GOLEMCOMBATADV"}, tc("textures/misc/golem/arms_claws.png"),
                new PartModelClaws(tc("models/obj/golem_arms_claws.obj"), tc("textures/entity/golems/golem_arms_claws.png"), PartModel.EnumAttachPoint.ARMS),
                new Object[]{moduleAggression, stack(Items.SHEARS, 2), "base"},
                new EnumGolemTrait[]{EnumGolemTrait.FIGHTER, EnumGolemTrait.CLUMSY, EnumGolemTrait.BRUTAL}));
        GolemArm.register(new GolemArm("BREAKERS", new String[]{"GOLEMBREAKER"}, tc("textures/misc/golem/arms_breakers.png"),
                new PartModelBreakers(tc("models/obj/golem_arms_breakers.obj"), tc("textures/entity/golems/golem_arms_breakers.png"), PartModel.EnumAttachPoint.ARMS),
                new Object[]{stack(Items.DIAMOND, 2), "base", stack(Blocks.PISTON, 2)},
                new EnumGolemTrait[]{EnumGolemTrait.BREAKER, EnumGolemTrait.CLUMSY, EnumGolemTrait.BRUTAL}));
        GolemArm.register(new GolemArm("DARTS", new String[]{"GOLEMCOMBATADV"}, tc("textures/misc/golem/arms_darts.png"),
                new PartModelDarts(tc("models/obj/golem_arms_darter.obj"), tc("textures/entity/golems/golem_arms_darter.png"), PartModel.EnumAttachPoint.ARMS),
                new Object[]{moduleAggression, stack(Blocks.DISPENSER, 2), stack(Items.ARROW, 32), "mech"}, new GolemArmDart(),
                new EnumGolemTrait[]{EnumGolemTrait.FIGHTER, EnumGolemTrait.CLUMSY, EnumGolemTrait.RANGED, EnumGolemTrait.FRAGILE}));

        GolemLeg.register(new GolemLeg("WALKER", new String[]{"MINDCLOCKWORK"}, tc("textures/misc/golem/legs_walker.png"),
                model("golem_legs_walker", null, PartModel.EnumAttachPoint.LEGS), new Object[]{"base", "mech"}, new EnumGolemTrait[0]));
        GolemLeg.register(new GolemLeg("ROLLER", new String[]{"MINDCLOCKWORK"}, tc("textures/misc/golem/legs_roller.png"),
                new PartModelWheel(tc("models/obj/golem_legs_wheel.obj"), tc("textures/entity/golems/golem_legs_wheel.png"), PartModel.EnumAttachPoint.BODY),
                new Object[]{stack(Items.BOWL, 2), new ItemStack(Items.LEATHER), "mech"}, new GolemLegWheels(), new EnumGolemTrait[]{EnumGolemTrait.WHEELED}));
        GolemLeg.register(new GolemLeg("CLIMBER", new String[]{"GOLEMCLIMBER"}, tc("textures/misc/golem/legs_climber.png"),
                model("golem_legs_climber", "textures/blocks/base_metal.png", PartModel.EnumAttachPoint.LEGS),
                new Object[]{stack(Items.FLINT, 4), "base", "mech", "mech"}, new EnumGolemTrait[]{EnumGolemTrait.CLIMBER}));
        GolemLeg.register(new GolemLeg("FLYER", new String[]{"GOLEMFLYER"}, tc("textures/misc/golem/legs_flyer.png"),
                model("golem_legs_floater", "textures/entity/golems/golem_legs_floater.png", PartModel.EnumAttachPoint.BODY),
                new Object[]{new ItemStack(ConfigBlocks.levitator.get()), stack(ConfigItems.plate.get("brass").get(), 4), new ItemStack(Items.SLIME_BALL), "mech"},
                new GolemLegLevitator(), new EnumGolemTrait[]{EnumGolemTrait.FLYER, EnumGolemTrait.FRAGILE}));

        GolemAddon.register(new GolemAddon("NONE", new String[]{"MINDCLOCKWORK"}, tc("textures/blocks/blank.png"), null, new Object[0], new EnumGolemTrait[0]));
        GolemAddon.register(new GolemAddon("ARMORED", new String[]{"GOLEMCOMBATADV"}, tc("textures/misc/golem/addon_armored.png"),
                model("golem_armor", null, PartModel.EnumAttachPoint.BODY), new Object[]{"base", "base", "base", "base"},
                new EnumGolemTrait[]{EnumGolemTrait.ARMORED, EnumGolemTrait.HEAVY}));
        GolemAddon.register(new GolemAddon("FIGHTER", new String[]{"SEALGUARD"}, tc("textures/misc/golem/addon_fighter.png"), null,
                new Object[]{moduleAggression, "mech"}, new EnumGolemTrait[]{EnumGolemTrait.FIGHTER}));
        GolemAddon.register(new GolemAddon("HAULER", new String[]{"MINDCLOCKWORK"}, tc("textures/misc/golem/addon_hauler.png"),
                new PartModelHauler(tc("models/obj/golem_hauler.obj"), tc("textures/entity/golems/golem_hauler.png"), PartModel.EnumAttachPoint.BODY),
                new Object[]{new ItemStack(Items.LEATHER), new ItemStack(Blocks.CHEST)}, new EnumGolemTrait[]{EnumGolemTrait.HAULER}));
    }
}
