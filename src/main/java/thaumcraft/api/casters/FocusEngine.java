package thaumcraft.api.casters;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.UUID;
import java.util.function.Supplier;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * The focus element registry and the code that runs a focus package.
 * 1.21: elements are registered with a constructor reference instead of a Class created by reflection.
 */
public class FocusEngine {

    public static LinkedHashMap<String, Supplier<? extends IFocusElement>> elements = new LinkedHashMap<>();
    private static final LinkedHashMap<String, ResourceLocation> elementIcons = new LinkedHashMap<>();
    private static final LinkedHashMap<String, Integer> elementColor = new LinkedHashMap<>();

    public static void registerElement(Supplier<? extends IFocusElement> element, ResourceLocation icon, int color) {
        try {
            IFocusElement fe = element.get();
            elements.put(fe.getKey(), element);
            elementIcons.put(fe.getKey(), icon);
            elementColor.put(fe.getKey(), color);
        } catch (Exception e) {
        }
    }

    public static IFocusElement getElement(String key) {
        try {
            return elements.get(key).get();
        } catch (Exception e) {
        }
        return null;
    }

    public static ResourceLocation getElementIcon(String key) {
        return elementIcons.get(key);
    }

    public static int getElementColor(String key) {
        Integer c = elementColor.get(key);
        return c == null ? 0xFFFFFF : c;
    }

    public static boolean doesPackageContainElement(FocusPackage focusPackage, String key) {
        for (IFocusElement node : focusPackage.nodes) {
            if (node.getKey().equals(key)) return true;
        }
        return false;
    }

    /**
     * @param nocopy set to true only if the focus package passed in is temporary and not attached to an actual focus.
     * Use this to preserve any settings, targets, etc that has been set during package construction
     */
    public static void castFocusPackage(LivingEntity caster, FocusPackage focusPackage, boolean nocopy) {
        FocusPackage focusPackageCopy;
        if (nocopy)
            focusPackageCopy = focusPackage;
        else
            focusPackageCopy = focusPackage.copy(caster);

        focusPackageCopy.initialize(caster);
        focusPackageCopy.setUniqueID(UUID.randomUUID());
        for (FocusEffect effect : focusPackageCopy.getFocusEffects()) {
            effect.onCast(caster);
        }

        runFocusPackage(focusPackageCopy, null, null);
    }

    /**
     * Overrides castFocusPackage(caster, focusPackage, nocopy) with nocopy = false
     */
    public static void castFocusPackage(LivingEntity caster, FocusPackage focusPackage) {
        castFocusPackage(caster, focusPackage, false);
    }

    public static void runFocusPackage(FocusPackage focusPackage, Trajectory[] trajectories, HitResult[] targets) {

        Trajectory[] prevTrajectories = trajectories;
        HitResult[] prevTargets = targets;

        synchronized (focusPackage.nodes) {

            if (!(focusPackage.nodes.get(0) instanceof FocusMediumRoot)) {
                focusPackage.nodes.add(0, new FocusMediumRoot(trajectories, targets));
            }

            for (int idx = 0; idx < focusPackage.nodes.size(); idx++) {

                focusPackage.setExecutionIndex(idx);

                IFocusElement node = focusPackage.nodes.get(idx);
                if (idx > 0 && node instanceof FocusNode fn && fn.getParent() == null) {
                    IFocusElement nodePrev = focusPackage.nodes.get(idx - 1);
                    if (nodePrev instanceof FocusNode prev) {
                        fn.setParent(prev);
                    }
                }

                if (node instanceof FocusNode fn && fn.getPackage() == null) {
                    fn.setPackage(focusPackage);
                }

                if (node instanceof FocusNode fn) {
                    focusPackage.multiplyPower(fn.getPowerMultiplier());
                }

                if (node instanceof FocusPackage fp) {
                    runFocusPackage(fp, prevTrajectories, prevTargets);
                    break;
                } else if (node instanceof FocusMedium medium) {
                    if (prevTrajectories != null)
                        for (Trajectory trajectory : prevTrajectories) {
                            medium.execute(trajectory);
                        }

                    if (medium.hasIntermediary()) break;
                } else if (node instanceof FocusMod mod) {
                    if (node instanceof FocusModSplit split) {
                        for (FocusPackage sp : split.getSplitPackages()) {
                            split.setPackage(sp);
                            sp.multiplyPower(focusPackage.getPower());
                            split.execute();
                            runFocusPackage(sp, split.supplyTrajectories(), split.supplyTargets());
                        }
                        break;
                    } else {
                        mod.execute();
                    }
                } else if (node instanceof FocusEffect effect) {
                    if (prevTargets != null) {
                        int num = 0;
                        for (HitResult target : prevTargets) {
                            if (target instanceof EntityHitResult ehr) {
                                String k = ehr.getEntity().getId() + focusPackage.getUniqueID().toString();
                                if (damageResistList.contains(k) && ehr.getEntity().invulnerableTime > 0) {
                                    ehr.getEntity().invulnerableTime = 0;
                                } else {
                                    if (damageResistList.size() > 10) damageResistList.remove(0);
                                    damageResistList.add(k);
                                }
                            }
                            Trajectory tra = prevTrajectories != null ? ((prevTrajectories.length == prevTargets.length) ? prevTrajectories[num] : prevTrajectories[0]) : null;
                            effect.execute(target, tra, focusPackage.getPower(), num);
                            num++;
                        }
                    }
                }

                if (node instanceof FocusNode fn) {
                    prevTrajectories = fn.supplyTrajectories();
                    prevTargets = fn.supplyTargets();
                }
            }
        }
    }

    private static final ArrayList<String> damageResistList = new ArrayList<>();
}
