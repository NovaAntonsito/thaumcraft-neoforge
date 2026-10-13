"""Translates ConfigAspects.registerItemAspects (TC 6.1.BETA26, 1.12.2) into 1.21 aspect JSON.

Usage: python3 gen_aspects.py <ConfigAspects.java> <ids.txt> <ctags.txt> <out vanilla.json> <out thaumcraft.json>
"""
import json
import re
import sys

src, ids_file, ctags_file, out_vanilla, out_tc = sys.argv[1:6]
VALID_IDS = set(open(ids_file).read().split())
C_TAGS = set(open(ctags_file).read().split())

ASPECTS = {
    "AIR": "aer", "EARTH": "terra", "FIRE": "ignis", "WATER": "aqua", "ORDER": "ordo", "ENTROPY": "perditio",
    "VOID": "vacuos", "LIGHT": "lux", "MOTION": "motus", "COLD": "gelum", "CRYSTAL": "vitreus", "METAL": "metallum",
    "LIFE": "victus", "DEATH": "mortuus", "ENERGY": "potentia", "EXCHANGE": "permutatio", "MAGIC": "praecantatio",
    "AURA": "auram", "ALCHEMY": "alkimia", "FLUX": "vitium", "DARKNESS": "tenebrae", "ELDRITCH": "alienis",
    "FLIGHT": "volatus", "PLANT": "herba", "TOOL": "instrumentum", "CRAFT": "fabrico", "MECHANISM": "machina",
    "TRAP": "vinculum", "SOUL": "spiritus", "MIND": "cognitio", "SENSES": "sensus", "AVERSION": "aversio",
    "PROTECT": "praemunio", "DESIRE": "desiderium", "UNDEAD": "exanimis", "BEAST": "bestia", "MAN": "humanus",
}

COLORS = ["white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray", "light_gray", "cyan",
          "purple", "blue", "brown", "green", "red", "black"]

# OreDictionary name -> 1.21 item ids / tags.
OREDICT = {
    "stone": ["#c:stones"], "stoneGranite": ["minecraft:granite"], "stoneDiorite": ["minecraft:diorite"],
    "stoneAndesite": ["minecraft:andesite"], "cobblestone": ["#c:cobblestones/normal", "#c:cobblestones/deepslate"],
    "dirt": ["minecraft:dirt", "minecraft:coarse_dirt"], "sand": ["#c:sands"], "grass": ["minecraft:grass_block"],
    "endstone": ["#c:end_stones"], "gravel": ["#c:gravels"], "netherrack": ["#c:netherracks"],
    "obsidian": ["minecraft:obsidian"], "blockGlass": ["#c:glass_blocks"], "logWood": ["#minecraft:logs"],
    "treeSapling": ["#minecraft:saplings"], "treeLeaves": ["#minecraft:leaves"], "vine": ["minecraft:vine"],
    "cropNetherWart": ["#c:crops/nether_wart"], "blockCactus": ["minecraft:cactus"],
    "sugarcane": ["minecraft:sugar_cane"], "cropWheat": ["#c:crops/wheat"], "cropCarrot": ["#c:crops/carrot"],
    "cropPotato": ["#c:crops/potato"], "string": ["#c:strings"], "slimeball": ["#c:slime_balls"],
    "leather": ["#c:leathers"], "feather": ["#c:feathers"], "bone": ["#c:bones"], "egg": ["#c:eggs"],
    "gunpowder": ["#c:gunpowders"], "enderpearl": ["#c:ender_pearls"], "netherStar": ["#c:nether_stars"],
    "dustRedstone": ["#c:dusts/redstone"], "dustGlowstone": ["#c:dusts/glowstone"],
    "glowstone": ["minecraft:glowstone"], "ingotBrickNether": ["#c:bricks/nether"],
    "quicksilver": ["thaumcraft:quicksilver"],
    # Not in the c: conventions: TC's native clusters (not ported yet), rubber, uranium drops, gear*.
    "clusterIron": None, "clusterGold": None, "clusterCopper": None, "clusterTin": None, "clusterSilver": None,
    "clusterLead": None, "clusterCinnabar": None, "clusterQuartz": None, "itemRubber": None,
    "itemDropUranium": None, "gear*": None,
}
ORE_PREFIX = {"ore": "ores", "ingot": "ingots", "dust": "dusts", "gem": "gems"}

# 1.12 Blocks/Items constant (and metadata) -> 1.21 ids. None = no item in 1.21 (or not an item in 1.12 either).
CONST = {
    "Blocks.BEDROCK": ["minecraft:bedrock"], "Blocks.BEACON": ["minecraft:beacon"],
    "Blocks.BOOKSHELF": ["minecraft:bookshelf"], "Blocks.BROWN_MUSHROOM": ["minecraft:brown_mushroom"],
    "Blocks.BROWN_MUSHROOM_BLOCK": ["minecraft:brown_mushroom_block"], "Blocks.CACTUS": ["minecraft:cactus"],
    "Blocks.CHEST": ["minecraft:chest"], "Blocks.CHORUS_FLOWER": ["minecraft:chorus_flower"],
    "Blocks.CHORUS_PLANT": ["minecraft:chorus_plant"], "Blocks.CLAY": ["minecraft:clay"],
    "Blocks.COAL_ORE": ["minecraft:coal_ore", "minecraft:deepslate_coal_ore"],
    "Blocks.CONCRETE": ["#c:concretes"], "Blocks.CONCRETE_POWDER": ["minecraft:white_concrete_powder"],
    "Blocks.CRAFTING_TABLE": ["minecraft:crafting_table"], "Blocks.DAYLIGHT_DETECTOR": ["minecraft:daylight_detector"],
    "Blocks.DEADBUSH": ["minecraft:dead_bush"], "Blocks.DETECTOR_RAIL": ["minecraft:detector_rail"],
    "Blocks.DIRT:2": ["minecraft:podzol"], "Blocks.DISPENSER": ["minecraft:dispenser"],
    "Blocks.DOUBLE_PLANT:0": ["minecraft:sunflower"], "Blocks.DOUBLE_PLANT:1": ["minecraft:lilac"],
    "Blocks.DOUBLE_PLANT:2": ["minecraft:tall_grass"], "Blocks.DOUBLE_PLANT:3": ["minecraft:large_fern"],
    "Blocks.DOUBLE_PLANT:4": ["minecraft:rose_bush"], "Blocks.DOUBLE_PLANT:5": ["minecraft:peony"],
    "Blocks.DRAGON_EGG": ["minecraft:dragon_egg"], "Blocks.DROPPER": ["minecraft:dropper"],
    "Blocks.ENCHANTING_TABLE": ["minecraft:enchanting_table"], "Blocks.ENDER_CHEST": ["minecraft:ender_chest"],
    "Blocks.END_PORTAL": None, "Blocks.END_PORTAL_FRAME": ["minecraft:end_portal_frame"],
    "Blocks.END_ROD": ["minecraft:end_rod"], "Blocks.FARMLAND": ["minecraft:farmland"], "Blocks.FIRE": None,
    "Blocks.FURNACE": ["minecraft:furnace"], "Blocks.GLOWSTONE": ["minecraft:glowstone"],
    "Blocks.GOLDEN_RAIL": ["minecraft:powered_rail"], "Blocks.GRASS": ["minecraft:grass_block"],
    "Blocks.GRASS_PATH": ["minecraft:dirt_path"], "Blocks.HARDENED_CLAY": ["minecraft:terracotta"],
    "Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE": ["minecraft:heavy_weighted_pressure_plate"],
    "Blocks.HOPPER": ["minecraft:hopper"], "Blocks.ICE": ["minecraft:ice"], "Blocks.JUKEBOX": ["minecraft:jukebox"],
    "Blocks.LEVER": ["minecraft:lever"],
    "Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE": ["minecraft:light_weighted_pressure_plate"],
    "Blocks.MAGMA": ["minecraft:magma_block"], "Blocks.MELON_BLOCK": ["minecraft:melon"],
    "Blocks.MOB_SPAWNER": ["minecraft:spawner"], "Blocks.MOSSY_COBBLESTONE": ["minecraft:mossy_cobblestone"],
    "Blocks.MYCELIUM": ["minecraft:mycelium"], "Blocks.NETHERRACK": ["minecraft:netherrack"],
    "Blocks.NOTEBLOCK": ["minecraft:note_block"], "Blocks.PACKED_ICE": ["minecraft:packed_ice"],
    "Blocks.PISTON": ["minecraft:piston"], "Blocks.PORTAL": None, "Blocks.PUMPKIN": ["minecraft:pumpkin"],
    "Blocks.RAIL": ["minecraft:rail"], "Blocks.RED_FLOWER": ["#minecraft:small_flowers"],
    "Blocks.RED_MUSHROOM": ["minecraft:red_mushroom"], "Blocks.RED_MUSHROOM_BLOCK": ["minecraft:red_mushroom_block"],
    "Blocks.SANDSTONE": ["minecraft:sandstone"], "Blocks.SANDSTONE:1": ["minecraft:chiseled_sandstone"],
    "Blocks.SANDSTONE:2": ["minecraft:cut_sandstone"], "Blocks.SOUL_SAND": ["minecraft:soul_sand"],
    "Blocks.SPONGE:0": ["minecraft:sponge"], "Blocks.SPONGE:1": ["minecraft:wet_sponge"],
    "Blocks.STAINED_HARDENED_CLAY": ["minecraft:white_terracotta"],
    "Blocks.STAINED_HARDENED_CLAY:*": ["minecraft:%s_terracotta" % c for c in COLORS],
    "Blocks.STICKY_PISTON": ["minecraft:sticky_piston"], "Blocks.STONEBRICK": ["minecraft:stone_bricks"],
    "Blocks.STONEBRICK:1": ["minecraft:mossy_stone_bricks"], "Blocks.STONEBRICK:2": ["minecraft:cracked_stone_bricks"],
    "Blocks.STONEBRICK:3": ["minecraft:chiseled_stone_bricks"], "Blocks.STONE_BUTTON": ["minecraft:stone_button"],
    "Blocks.STONE_PRESSURE_PLATE": ["minecraft:stone_pressure_plate"],
    "Blocks.TALLGRASS": ["minecraft:short_grass", "minecraft:fern"], "Blocks.TORCH": ["minecraft:torch"],
    "Blocks.TRAPDOOR": ["#minecraft:wooden_trapdoors"], "Blocks.TRAPPED_CHEST": ["minecraft:trapped_chest"],
    "Blocks.TRIPWIRE": None, "Blocks.TRIPWIRE_HOOK": ["minecraft:tripwire_hook"],
    "Blocks.WATERLILY": ["minecraft:lily_pad"], "Blocks.WEB": ["minecraft:cobweb"],
    "Blocks.WOODEN_BUTTON": ["#minecraft:wooden_buttons"],
    "Blocks.WOODEN_PRESSURE_PLATE": ["#minecraft:wooden_pressure_plates"], "Blocks.WOOL": ["minecraft:white_wool"],
    "Blocks.YELLOW_FLOWER": ["minecraft:dandelion"],
    "Items.APPLE": ["minecraft:apple"], "Items.ARROW": ["minecraft:arrow"],
    "Items.BAKED_POTATO": ["minecraft:baked_potato"], "Items.BEEF": ["minecraft:beef"],
    "Items.BEETROOT": ["minecraft:beetroot"], "Items.BEETROOT_SEEDS": ["minecraft:beetroot_seeds"],
    "Items.BLAZE_POWDER": ["minecraft:blaze_powder"], "Items.BLAZE_ROD": ["minecraft:blaze_rod"],
    "Items.BOAT": ["#minecraft:boats"], "Items.BOOK": ["minecraft:book"], "Items.BOWL": ["minecraft:bowl"],
    "Items.BREWING_STAND": ["minecraft:brewing_stand"], "Items.BRICK": ["minecraft:brick"],
    "Items.BUCKET": ["minecraft:bucket"], "Items.CAKE": ["minecraft:cake"],
    "Items.CARROT_ON_A_STICK": ["minecraft:carrot_on_a_stick"], "Items.CAULDRON": ["minecraft:cauldron"],
    "Items.CHICKEN": ["minecraft:chicken"], "Items.CHORUS_FRUIT": ["minecraft:chorus_fruit"],
    "Items.CHORUS_FRUIT_POPPED": ["minecraft:popped_chorus_fruit"], "Items.CLAY_BALL": ["minecraft:clay_ball"],
    "Items.CLOCK": ["minecraft:clock"], "Items.COAL": ["minecraft:coal", "minecraft:charcoal"],
    "Items.COMPARATOR": ["minecraft:comparator"], "Items.COOKED_BEEF": ["minecraft:cooked_beef"],
    "Items.COOKED_CHICKEN": ["minecraft:cooked_chicken"],
    "Items.COOKED_FISH": ["minecraft:cooked_cod", "minecraft:cooked_salmon"],
    "Items.COOKED_MUTTON": ["minecraft:cooked_mutton"], "Items.COOKED_PORKCHOP": ["minecraft:cooked_porkchop"],
    "Items.COOKED_RABBIT": ["minecraft:cooked_rabbit"], "Items.COOKIE": ["minecraft:cookie"],
    "Items.DIAMOND_HORSE_ARMOR": ["minecraft:diamond_horse_armor"], "Items.DRAGON_BREATH": ["minecraft:dragon_breath"],
    "Items.DYE:0": ["minecraft:ink_sac"], "Items.DYE:2": ["minecraft:green_dye"],
    "Items.DYE:3": ["minecraft:cocoa_beans"], "Items.DYE:4": ["minecraft:lapis_lazuli"],
    "Items.DYE:15": ["minecraft:bone_meal"], "Items.ELYTRA": ["minecraft:elytra"],
    "Items.ENCHANTED_BOOK": ["minecraft:enchanted_book"], "Items.ENDER_EYE": ["minecraft:ender_eye"],
    "Items.EXPERIENCE_BOTTLE": ["minecraft:experience_bottle"],
    "Items.FERMENTED_SPIDER_EYE": ["minecraft:fermented_spider_eye"],
    "Items.FISH": ["minecraft:cod", "minecraft:salmon", "minecraft:tropical_fish", "minecraft:pufferfish"],
    "Items.FISHING_ROD": ["minecraft:fishing_rod"], "Items.FLINT": ["minecraft:flint"],
    "Items.FLINT_AND_STEEL": ["minecraft:flint_and_steel"], "Items.FLOWER_POT": ["minecraft:flower_pot"],
    "Items.GHAST_TEAR": ["minecraft:ghast_tear"], "Items.GLASS_BOTTLE": ["minecraft:glass_bottle"],
    "Items.GOLDEN_APPLE:0": ["minecraft:golden_apple"], "Items.GOLDEN_APPLE:1": ["minecraft:enchanted_golden_apple"],
    "Items.GOLDEN_CARROT": ["minecraft:golden_carrot"], "Items.GOLDEN_HORSE_ARMOR": ["minecraft:golden_horse_armor"],
    "Items.IRON_BOOTS": ["minecraft:iron_boots"], "Items.IRON_CHESTPLATE": ["minecraft:iron_chestplate"],
    "Items.IRON_DOOR": ["minecraft:iron_door"], "Items.IRON_HELMET": ["minecraft:iron_helmet"],
    "Items.IRON_HORSE_ARMOR": ["minecraft:iron_horse_armor"], "Items.IRON_LEGGINGS": ["minecraft:iron_leggings"],
    "Items.IRON_SWORD": ["minecraft:iron_sword"], "Items.LAVA_BUCKET": ["minecraft:lava_bucket"],
    "Items.LEATHER_CHESTPLATE": ["minecraft:leather_chestplate"], "Items.LEATHER_HELMET": ["minecraft:leather_helmet"],
    "Items.LEATHER_LEGGINGS": ["minecraft:leather_leggings"], "Items.LINGERING_POTION": ["minecraft:lingering_potion"],
    "Items.MAGMA_CREAM": ["minecraft:magma_cream"], "Items.MELON": ["minecraft:melon_slice"],
    "Items.MELON_SEEDS": ["minecraft:melon_seeds"], "Items.MILK_BUCKET": ["minecraft:milk_bucket"],
    "Items.MINECART": ["minecraft:minecart"], "Items.MUSHROOM_STEW": ["minecraft:mushroom_stew"],
    "Items.MUTTON": ["minecraft:mutton"], "Items.NAME_TAG": ["minecraft:name_tag"], "Items.PAPER": ["minecraft:paper"],
    "Items.POISONOUS_POTATO": ["minecraft:poisonous_potato"], "Items.PORKCHOP": ["minecraft:porkchop"],
    "Items.POTIONITEM": ["minecraft:potion"], "Items.PRISMARINE_CRYSTALS": ["minecraft:prismarine_crystals"],
    "Items.PRISMARINE_SHARD": ["minecraft:prismarine_shard"], "Items.PUMPKIN_PIE": ["minecraft:pumpkin_pie"],
    "Items.PUMPKIN_SEEDS": ["minecraft:pumpkin_seeds"], "Items.RABBIT": ["minecraft:rabbit"],
    "Items.RABBIT_FOOT": ["minecraft:rabbit_foot"], "Items.RABBIT_HIDE": ["minecraft:rabbit_hide"],
    "Items.REPEATER": ["minecraft:repeater"], "Items.ROTTEN_FLESH": ["minecraft:rotten_flesh"],
    "Items.SADDLE": ["minecraft:saddle"], "Items.SHIELD": ["minecraft:shield"],
    "Items.SHULKER_SHELL": ["minecraft:shulker_shell"],
    "Items.SKULL:0": ["minecraft:skeleton_skull"], "Items.SKULL:1": ["minecraft:wither_skeleton_skull"],
    "Items.SKULL:2": ["minecraft:zombie_head"], "Items.SKULL:3": ["minecraft:player_head"],
    "Items.SKULL:4": ["minecraft:creeper_head"], "Items.SKULL:5": ["minecraft:dragon_head"],
    "Items.SNOWBALL": ["minecraft:snowball"], "Items.SPECKLED_MELON": ["minecraft:glistering_melon_slice"],
    "Items.SPECTRAL_ARROW": ["minecraft:spectral_arrow"], "Items.SPIDER_EYE": ["minecraft:spider_eye"],
    "Items.SPLASH_POTION": ["minecraft:splash_potion"], "Items.SUGAR": ["minecraft:sugar"],
    "Items.TIPPED_ARROW": ["minecraft:tipped_arrow"], "Items.TOTEM_OF_UNDYING": ["minecraft:totem_of_undying"],
    "Items.WATER_BUCKET": ["minecraft:water_bucket"], "Items.WHEAT_SEEDS": ["minecraft:wheat_seeds"],
    # Thaumcraft items already ported. The rest of ItemsTC/BlocksTC is added when those items exist.
    "ItemsTC.nuggets:10": ["thaumcraft:nugget_rareearth"], "ItemsTC.salisMundus": ["thaumcraft:salis_mundus"],
    "BlocksTC.crucible": ["thaumcraft:crucible"],
    "Blocks.LIT_REDSTONE_ORE": None, "Blocks.ACTIVATOR_RAIL": ["minecraft:activator_rail"],
    "Items.CHAINMAIL_HELMET": ["minecraft:chainmail_helmet"], "Items.CHAINMAIL_CHESTPLATE": ["minecraft:chainmail_chestplate"],
    "Items.CHAINMAIL_LEGGINGS": ["minecraft:chainmail_leggings"], "Items.CHAINMAIL_BOOTS": ["minecraft:chainmail_boots"],
}
for c in COLORS:
    old = "SILVER" if c == "light_gray" else c.upper()
    CONST["Blocks.%s_GLAZED_TERRACOTTA" % old] = ["minecraft:%s_glazed_terracotta" % c]
    CONST["Blocks.%s_SHULKER_BOX" % old] = ["minecraft:%s_shulker_box" % c]
# The 1.12 purple box was the base for the other colours; 1.21 also has the undyed box.
CONST["Blocks.BLACK_SHULKER_BOX"] = ["minecraft:black_shulker_box", "minecraft:shulker_box"]
for wood in ["ACACIA", "BIRCH", "DARK_OAK", "JUNGLE", "SPRUCE"]:
    # 1.21 has more woods; the tag covers all of them (the original listed every wood it had).
    CONST["Blocks.%s_FENCE_GATE" % wood] = ["#minecraft:fence_gates"]
    CONST["Items.%s_BOAT" % wood] = ["#minecraft:boats"]
    CONST["Items.%s_DOOR" % wood] = ["#minecraft:wooden_doors"]
CONST["Items.OAK_DOOR"] = ["#minecraft:wooden_doors"]
for rec in ["11", "13", "BLOCKS", "CAT", "CHIRP", "FAR", "MALL", "MELLOHI", "STAL", "STRAD", "WAIT", "WARD"]:
    CONST["Items.RECORD_" + rec] = ["minecraft:music_disc_" + rec.lower()]

problems = []
skipped_tc = []  # Thaumcraft items not ported yet
# Ids the constant-pool scan misses (registered through helpers).
VALID_IDS |= {"melon_seeds", "pumpkin_seeds", "pumpkin", "melon"}


def resolve_stack(expr):
    """'Blocks.X' / 'Blocks.X, 1, 2' -> list of ids, or None if it has no 1.21 item."""
    parts = [p.strip() for p in expr.split(",")]
    const = parts[0]
    meta = int(parts[2]) if len(parts) == 3 else 0
    keys = []
    if meta == 32767:
        keys = [const + ":*", const]
    else:
        keys = [const + ":" + str(meta)] + ([const] if meta == 0 else [])
    for k in keys:
        if k in CONST:
            return CONST[k], k
    if not const.startswith(("ItemsTC.", "BlocksTC.", "ca")):
        problems.append("unmapped constant " + expr)
    else:
        skipped_tc.append(const)
    return None, None


def resolve_oredict(name):
    if name in OREDICT:
        return OREDICT[name]
    m = re.match(r"(ore|ingot|dust|gem)([A-Z]\w*)$", name)
    if m:
        material = re.sub(r"(?<!^)([A-Z])", r"_\1", m.group(2)).lower()
        return ["#c:%s/%s" % (ORE_PREFIX[m.group(1)], material)]
    problems.append("unmapped oredict " + name)
    return None


def parse_aspect_ops(text):
    ops = []
    for op, asp, amt in re.findall(r"\.(add|merge|remove)\(Aspect\.(\w+), (-?\d+)\)", text):
        ops.append((op, ASPECTS[asp], int(amt)))
    return ops


def apply_ops(ops):
    out = {}
    for op, asp, amt in ops:
        if op == "add":
            out[asp] = out.get(asp, 0) + amt
        elif op == "merge":
            out[asp] = max(out.get(asp, 0), amt)
        elif op == "remove":
            left = out.get(asp, 0) - amt
            if left <= 0:
                out.pop(asp, None)
            else:
                out[asp] = left
    return out


text = open(src).read()
body = text[text.index("registerItemAspects"):]
body = body[:body.index("public static AspectList getPotionAspects")]

line_re = re.compile(r"ThaumcraftApi\.register(Complex)?ObjectTag\((.*)\);\s*$")
entries = []  # (ids, entry dict)
seen_exact = set()
for raw in body.splitlines():
    m = line_re.search(raw)
    if not m:
        continue
    complex_ = bool(m.group(1))
    args = m.group(2)
    # target
    if args.startswith('"'):
        name = args[1:args.index('"', 1)]
        ids = resolve_oredict(name)
        rest = args[args.index('"', 1) + 2:].strip()
        label = name
    elif args.startswith("sis,"):  # the banner-shield loop: same as a plain shield
        ids, label = CONST["Items.SHIELD"], "Items.SHIELD"
        rest = args[4:].strip()
    elif args.startswith(("FluidUtil.", "new ItemStack(ForgeModContainer")):
        continue  # Forge universal bucket and TC fluid buckets: no 1.21 equivalent yet
    elif args.startswith("stack"):  # the potion loop, handled in Java (ObjectAspects.potionAspects)
        continue
    else:
        mm = re.match(r"new ItemStack\(([^()]*)\),\s*(.*)$", args)
        if not mm:
            problems.append("unparsed " + args[:80])
            continue
        ids, label = resolve_stack(mm.group(1))
        rest = mm.group(2)
    if ids is None:
        continue
    entry = {"items": ids}
    base_m = re.match(r"new AspectList\(new ItemStack\(([^()]*)\)\)(.*)$", rest)
    if base_m:
        base_ids, _ = resolve_stack(base_m.group(1))
        if base_ids is None or len(base_ids) != 1 or base_ids[0].startswith("#"):
            problems.append("bad base for %s: %s" % (label, base_m.group(1)))
            continue
        ops = parse_aspect_ops(base_m.group(2))
        if base_ids[0] in ids:
            # "new AspectList(new ItemStack(self))" before self is registered = the recipe-generated aspects.
            complex_ = True
        else:
            entry["base"] = base_ids[0]
        adds = [o for o in ops if o[0] == "add"]
        merges = [o for o in ops if o[0] == "merge"]
        if any(o[0] == "remove" for o in ops) or (merges and adds and ops.index(merges[0]) < ops.index(adds[-1])):
            problems.append("unsupported op order for " + label)
        if adds:
            entry["aspects"] = apply_ops(adds)
        if merges:
            entry["merge"] = apply_ops(merges)
    elif rest.startswith("new AspectList()"):
        entry["aspects"] = apply_ops(parse_aspect_ops(rest))
    else:
        problems.append("unparsed aspects for %s: %s" % (label, rest[:80]))
        continue
    if complex_:
        exact = [i for i in ids if not i.startswith("#")]
        if exact and all(i in seen_exact for i in exact):
            # registerComplexObjectTag on an item that already has aspects does nothing in TC6.
            continue
        entry["complex"] = True
    entry.setdefault("aspects", {})
    for i in ids:
        if not i.startswith("#"):
            seen_exact.add(i)
    if entry not in entries:  # 1.21 tags merge some 1.12 lines (all doors, all boats, ...)
        entries.append(entry)

# Validate ids and tags.
for e in entries:
    for i in e["items"] + ([e["base"]] if "base" in e else []):
        ns, path = i.lstrip("#").split(":")
        if i.startswith("#"):
            if ns == "c" and path not in C_TAGS and not path.startswith(("ores/", "ingots/", "dusts/", "gems/")):
                problems.append("unknown c tag " + i)
        elif ns == "minecraft" and path not in VALID_IDS:
            problems.append("unknown item " + i)

vanilla = [e for e in entries if not any(i.startswith("thaumcraft:") for i in e["items"])]
tc = [e for e in entries if any(i.startswith("thaumcraft:") for i in e["items"])]


def dump(path, data):
    with open(path, "w") as f:
        f.write('{\n  "entries": [\n')
        f.write(",\n".join("    " + json.dumps(e, separators=(", ", ": ")) for e in data))
        f.write("\n  ]\n}\n")


dump(out_vanilla, vanilla)
dump(out_tc, tc)
print("vanilla entries:", len(vanilla), " thaumcraft entries:", len(tc), " TC lines waiting for items:", len(skipped_tc))
for p in problems:
    print("PROBLEM:", p)
