package thaumcraft.api.aspects;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.common.aspects.ObjectAspects;

/** Partial port of thaumcraft.api.aspects.AspectHelper. */
public final class AspectHelper {
    private AspectHelper() {
    }

    /** The aspects assigned to an item (a copy). Empty if it has none. */
    public static AspectList getObjectAspects(ItemStack stack) {
        return ObjectAspects.get(stack);
    }

    /**
     * The aspects registered for an entity. A player gets Humanus and three aspects picked from their name.
     * Null if the entity has none.
     */
    @Nullable
    public static AspectList getEntityAspects(Entity entity) {
        AspectList tags = null;
        if (entity instanceof Player) {
            tags = new AspectList();
            tags.add(Aspect.MAN, 4);
            Random rand = new Random(entity.getName().getString().hashCode());
            List<Aspect> posa = new ArrayList<>(Aspect.aspects.values());
            tags.add(posa.get(rand.nextInt(posa.size())), 15);
            tags.add(posa.get(rand.nextInt(posa.size())), 15);
            tags.add(posa.get(rand.nextInt(posa.size())), 15);
        } else {
            CompoundTag tc = null;
            f1:
            for (ThaumcraftApi.EntityTags et : ThaumcraftApi.scanEntities) {
                if (et.entityType != entity.getType()) continue;
                if (et.nbts == null || et.nbts.length == 0) {
                    tags = et.aspects;
                } else {
                    if (tc == null) tc = entity.saveWithoutId(new CompoundTag());
                    for (ThaumcraftApi.EntityTagsNBT nbt : et.nbts) {
                        if (!nbt.matches(tc)) continue f1;
                    }
                    tags = et.aspects;
                }
            }
        }
        return tags;
    }

    /** Breaks every compound down into its components, recursively, until only primals remain. */
    public static AspectList reduceToPrimals(AspectList in) {
        AspectList out = new AspectList();
        for (Aspect aspect : in.getAspects()) {
            int amount = in.getAmount(aspect);
            if (aspect.isPrimal()) {
                out.add(aspect, amount);
            } else {
                AspectList parts = new AspectList();
                parts.add(aspect.getComponents()[0], amount);
                parts.add(aspect.getComponents()[1], amount);
                out.add(reduceToPrimals(parts));
            }
        }
        return out;
    }

    public static AspectList cullTags(AspectList temp) {
        return cullTags(temp, 7);
    }

    /**
     * Drops the weakest aspects until at most {@code cap} remain. Primals count a bit less (so they go first)
     * and deep compounds a bit more, as in TC6.
     */
    public static AspectList cullTags(AspectList temp, int cap) {
        AspectList temp2 = new AspectList();
        for (Aspect tag : temp.getAspects()) {
            if (tag != null) temp2.add(tag, temp.getAmount(tag));
        }
        while (temp2.size() > cap) {
            Aspect lowest = null;
            float low = Short.MAX_VALUE;
            for (Aspect tag : temp2.getAspects()) {
                float ta = temp2.getAmount(tag);
                if (tag.isPrimal()) {
                    ta *= .9f;
                } else {
                    if (!tag.getComponents()[0].isPrimal()) {
                        ta *= 1.1f;
                        if (!tag.getComponents()[0].getComponents()[0].isPrimal()) ta *= 1.05f;
                        if (!tag.getComponents()[0].getComponents()[1].isPrimal()) ta *= 1.05f;
                    }
                    if (!tag.getComponents()[1].isPrimal()) {
                        ta *= 1.1f;
                        if (!tag.getComponents()[1].getComponents()[0].isPrimal()) ta *= 1.05f;
                        if (!tag.getComponents()[1].getComponents()[1].isPrimal()) ta *= 1.05f;
                    }
                }
                if (ta < low) {
                    low = ta;
                    lowest = tag;
                }
            }
            temp2.aspects.remove(lowest);
        }
        return temp2;
    }

    /** Same as {@link Aspect#getCombinationResult}, kept for API parity. */
    public static Aspect getCombinationResult(Aspect a, Aspect b) {
        return Aspect.getCombinationResult(a, b);
    }
}
