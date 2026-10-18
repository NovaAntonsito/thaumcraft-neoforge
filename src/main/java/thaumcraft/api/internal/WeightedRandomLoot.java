package thaumcraft.api.internal;

import java.util.ArrayList;

import net.minecraft.util.random.Weight;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.world.item.ItemStack;

public class WeightedRandomLoot implements WeightedEntry {
    public static final ArrayList<WeightedRandomLoot> lootBagCommon = new ArrayList<>();
    public static final ArrayList<WeightedRandomLoot> lootBagUncommon = new ArrayList<>();
    public static final ArrayList<WeightedRandomLoot> lootBagRare = new ArrayList<>();

    public final ItemStack item;
    private final Weight weight;

    public WeightedRandomLoot(ItemStack stack, int weight) {
        this.item = stack;
        this.weight = Weight.of(weight);
    }

    @Override
    public Weight getWeight() {
        return weight;
    }
}
