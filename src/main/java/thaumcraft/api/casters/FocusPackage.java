package thaumcraft.api.casters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

/**
 * A chain of focus nodes, as built in the focal manipulator and stored on the focus.
 * 1.21: the dimension is saved by its key instead of its number, and is only resolved on the server.
 */
public class FocusPackage implements IFocusElement {

    @Override
    public String getResearch() {
        return null;
    }

    public Level world;
    private LivingEntity caster;
    private UUID casterUUID;

    private float power = 1;
    private int complexity = 0;

    int index;
    UUID uid;

    public List<IFocusElement> nodes = Collections.synchronizedList(new ArrayList<>());

    public FocusPackage() {
    }

    public FocusPackage(LivingEntity caster) {
        super();
        world = caster.level();
        this.caster = caster;
        casterUUID = caster.getUUID();
    }

    @Override
    public String getKey() {
        return "thaumcraft.PACKAGE";
    }

    @Override
    public EnumUnitType getType() {
        return EnumUnitType.PACKAGE;
    }

    public int getComplexity() {
        return complexity;
    }

    public void setComplexity(int complexity) {
        this.complexity = complexity;
    }

    public UUID getUniqueID() {
        return uid;
    }

    public void setUniqueID(UUID id) {
        uid = id;
    }

    public int getExecutionIndex() {
        return index;
    }

    public void setExecutionIndex(int idx) {
        index = idx;
    }

    public void addNode(IFocusElement e) {
        nodes.add(e);
    }

    public UUID getCasterUUID() {
        if (caster != null) casterUUID = caster.getUUID();
        return casterUUID;
    }

    public void setCasterUUID(UUID casterUUID) {
        this.casterUUID = casterUUID;
    }

    public LivingEntity getCaster() {
        try {
            if (caster == null && world != null && getCasterUUID() != null) {
                caster = world.getPlayerByUUID(getCasterUUID());
                if (caster == null && world instanceof ServerLevel sl && sl.getEntity(getCasterUUID()) instanceof LivingEntity le && le.isAlive()) {
                    caster = le;
                }
            }
        } catch (Exception e) {
        }
        return caster;
    }

    public FocusEffect[] getFocusEffects() {
        return getFocusEffectsPackage(this);
    }

    private FocusEffect[] getFocusEffectsPackage(FocusPackage fp) {
        ArrayList<FocusEffect> out = new ArrayList<>();
        for (IFocusElement el : fp.nodes) {
            if (el instanceof FocusEffect fe) {
                out.add(fe);
            } else if (el instanceof FocusPackage p) {
                Collections.addAll(out, getFocusEffectsPackage(p));
            } else if (el instanceof FocusModSplit split) {
                for (FocusPackage fsp : split.getSplitPackages()) {
                    Collections.addAll(out, getFocusEffectsPackage(fsp));
                }
            }
        }
        return out.toArray(new FocusEffect[] {});
    }

    public void deserialize(CompoundTag nbt) {
        uid = nbt.hasUUID("uid") ? nbt.getUUID("uid") : null;
        index = nbt.getInt("index");
        world = null;
        if (nbt.contains("dim")) {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            ResourceLocation dim = ResourceLocation.tryParse(nbt.getString("dim"));
            if (server != null && dim != null) {
                world = server.getLevel(ResourceKey.create(Registries.DIMENSION, dim));
            }
        }
        setCasterUUID(nbt.hasUUID("casterUUID") ? nbt.getUUID("casterUUID") : null);
        power = nbt.getFloat("power");
        complexity = nbt.getInt("complexity");

        ListTag nodelist = nbt.getList("nodes", Tag.TAG_COMPOUND);
        nodes.clear();
        for (int x = 0; x < nodelist.size(); x++) {
            CompoundTag nodenbt = nodelist.getCompound(x);
            EnumUnitType ut;
            try {
                ut = EnumUnitType.valueOf(nodenbt.getString("type"));
            } catch (IllegalArgumentException e) {
                continue;
            }
            if (ut == EnumUnitType.PACKAGE) {
                FocusPackage fp = new FocusPackage();
                fp.deserialize(nodenbt.getCompound("package"));
                nodes.add(fp);
                break;
            } else {
                IFocusElement fn = FocusEngine.getElement(nodenbt.getString("key"));
                if (fn != null) {
                    if (fn instanceof FocusNode node) {
                        node.initialize();
                        if (node.getSettingList() != null) {
                            for (String ns : node.getSettingList()) {
                                node.getSetting(ns).setValue(nodenbt.getInt("setting." + ns));
                            }
                        }
                        if (fn instanceof FocusModSplit split) {
                            split.deserialize(nodenbt.getCompound("packages"));
                        }
                    }
                    addNode(fn);
                }
            }
        }
    }

    public CompoundTag serialize() {
        CompoundTag nbt = new CompoundTag();
        if (uid != null) nbt.putUUID("uid", uid);
        nbt.putInt("index", index);
        if (getCasterUUID() != null) nbt.putUUID("casterUUID", getCasterUUID());
        if (world != null) nbt.putString("dim", world.dimension().location().toString());
        nbt.putFloat("power", power);
        nbt.putInt("complexity", complexity);

        ListTag nodelist = new ListTag();
        synchronized (nodes) {
            for (IFocusElement node : nodes) {
                if (node == null || node.getType() == null) continue;
                CompoundTag nodenbt = new CompoundTag();
                nodenbt.putString("type", node.getType().name());
                nodenbt.putString("key", node.getKey());
                if (node.getType() == EnumUnitType.PACKAGE) {
                    nodenbt.put("package", ((FocusPackage) node).serialize());
                    nodelist.add(nodenbt);
                    break;
                } else {
                    if (node instanceof FocusNode fn && fn.getSettingList() != null) {
                        for (String ns : fn.getSettingList()) {
                            nodenbt.putInt("setting." + ns, fn.getSettingValue(ns));
                        }
                    }
                    if (node instanceof FocusModSplit split) {
                        nodenbt.put("packages", split.serialize());
                    }
                    nodelist.add(nodenbt);
                }
            }
        }
        nbt.put("nodes", nodelist);
        return nbt;
    }

    public float getPower() {
        return power;
    }

    public void multiplyPower(float pow) {
        power *= pow;
    }

    public FocusPackage copy(LivingEntity caster) {
        FocusPackage fp = new FocusPackage(caster);
        fp.deserialize(serialize());
        fp.world = caster.level();
        return fp;
    }

    public void initialize(LivingEntity caster) {
        world = caster.level();
        IFocusElement node = nodes.get(0);
        if (node instanceof FocusMediumRoot root && root.supplyTargets() == null) {
            root.setupFromCaster(caster);
        }
    }

    public int getSortingHelper() {
        StringBuilder s = new StringBuilder();
        for (IFocusElement k : nodes) {
            s.append(k.getKey());
            if (k instanceof FocusNode fn && fn.getSettingList() != null) {
                for (String ns : fn.getSettingList()) {
                    s.append(fn.getSettingValue(ns));
                }
            }
        }
        return s.toString().hashCode();
    }

}
