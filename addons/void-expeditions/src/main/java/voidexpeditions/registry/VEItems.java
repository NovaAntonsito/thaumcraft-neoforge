package voidexpeditions.registry;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import voidexpeditions.VoidExpeditions;

public final class VEItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(VoidExpeditions.MODID);

    /** Found in each labyrinth's key room; spent at its portal to go on. Gone when the run ends. */
    public static final DeferredItem<Item> VOID_SIGIL = ITEMS.register("void_sigil",
            () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));

    /** Won at the end of a run: the currency of the terminal's lasting upgrades. */
    public static final DeferredItem<Item> VOID_SHARD = ITEMS.register("void_shard",
            () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)));

    public static final DeferredItem<BlockItem> EXPEDITION_TERMINAL = ITEMS.registerSimpleBlockItem(VEBlocks.EXPEDITION_TERMINAL,
            new Item.Properties().rarity(Rarity.RARE));
    public static final DeferredItem<BlockItem> VOID_GATE_RING = ITEMS.registerSimpleBlockItem(VEBlocks.VOID_GATE_RING,
            new Item.Properties().rarity(Rarity.RARE));

    private VEItems() {
    }
}
