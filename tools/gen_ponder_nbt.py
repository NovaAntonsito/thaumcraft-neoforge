"""
Generates the Ponder scene structures (src/main/resources/assets/thaumcraft/ponder/*.nbt).

Each file is a gzipped vanilla StructureTemplate: size, palette, blocks, entities.
Ponder loads them with PonderSceneRegistry.loadSchematic (no DataFixer pass), so the
block states written here must match the 1.21.1 registry exactly.

Run from the project root:  python tools/gen_ponder_nbt.py
"""
import gzip
import os
import struct

DATA_VERSION = 3955  # Minecraft 1.21.1
OUT = os.path.join(os.path.dirname(__file__), "..", "src", "main", "resources", "assets", "thaumcraft", "ponder")

# ---- tiny NBT writer ----
TAG_END, TAG_INT, TAG_STRING, TAG_LIST, TAG_COMPOUND = 0, 3, 8, 9, 10


class Int(int):
    pass


def _name(s):
    b = s.encode("utf-8")
    return struct.pack(">H", len(b)) + b


def _tag_id(v):
    if isinstance(v, int):
        return TAG_INT
    if isinstance(v, str):
        return TAG_STRING
    if isinstance(v, list):
        return TAG_LIST
    if isinstance(v, dict):
        return TAG_COMPOUND
    raise TypeError(v)


def _payload(v):
    t = _tag_id(v)
    if t == TAG_INT:
        return struct.pack(">i", v)
    if t == TAG_STRING:
        return _name(v)
    if t == TAG_LIST:
        inner = _tag_id(v[0]) if v else TAG_END
        return struct.pack(">bi", inner, len(v)) + b"".join(_payload(x) for x in v)
    out = b""
    for k, x in v.items():
        out += struct.pack(">b", _tag_id(x)) + _name(k) + _payload(x)
    return out + struct.pack(">b", TAG_END)


def write_nbt(path, root):
    data = struct.pack(">b", TAG_COMPOUND) + _name("") + _payload(root)
    with gzip.open(path, "wb") as f:
        f.write(data)


# ---- structure builder ----
class Structure:
    def __init__(self, sx, sy, sz):
        self.size = (sx, sy, sz)
        self.blocks = {}

    def set(self, x, y, z, name, **props):
        self.blocks[(x, y, z)] = (name, tuple(sorted((k, str(v).lower()) for k, v in props.items())))

    def floor(self, name="minecraft:smooth_stone"):
        for x in range(self.size[0]):
            for z in range(self.size[2]):
                self.set(x, 0, z, name)

    def save(self, file):
        palette, index, blocks = [], {}, []
        # Every position gets a block (air included) so the template is a full box.
        for y in range(self.size[1]):
            for z in range(self.size[2]):
                for x in range(self.size[0]):
                    state = self.blocks.get((x, y, z), ("minecraft:air", ()))
                    if state not in index:
                        index[state] = len(palette)
                        entry = {"Name": state[0]}
                        if state[1]:
                            entry["Properties"] = dict(state[1])
                        palette.append(entry)
                    blocks.append({"pos": [x, y, z], "state": index[state]})
        root = {
            "DataVersion": DATA_VERSION,
            "size": list(self.size),
            "palette": palette,
            "blocks": blocks,
            "entities": [],
        }
        write_nbt(os.path.join(OUT, file), root)
        print("wrote", file, len(blocks), "blocks,", len(palette), "states")


def tube(**sides):
    props = {d: False for d in ("north", "east", "south", "west", "up", "down")}
    props.update(sides)
    return props


def crucible():
    s = Structure(5, 3, 5)
    s.floor()
    s.set(2, 1, 2, "minecraft:magma_block")
    s.set(2, 2, 2, "thaumcraft:crucible")
    s.save("crucible.nbt")


def infusion():
    s = Structure(7, 4, 7)
    s.floor()
    # Final state of the altar: the scene shows the arcane stones first and swaps them for pillars.
    # Pillar facings follow Thaumcraft.registerInfusionAltar with no rotation ([x][z] of the blueprint).
    s.set(2, 1, 2, "thaumcraft:pillar_arcane", facing="east")
    s.set(2, 1, 4, "thaumcraft:pillar_arcane", facing="north")
    s.set(4, 1, 2, "thaumcraft:pillar_arcane", facing="south")
    s.set(4, 1, 4, "thaumcraft:pillar_arcane", facing="west")
    s.set(3, 1, 3, "thaumcraft:pedestal_arcane")
    s.set(3, 3, 3, "thaumcraft:infusion_matrix")
    # Symmetric outer pedestals and candles.
    for x, z in ((3, 0), (3, 6), (0, 3), (6, 3)):
        s.set(x, 1, z, "thaumcraft:pedestal_arcane")
    for x, z in ((1, 1), (5, 5), (1, 5), (5, 1)):
        s.set(x, 1, z, "thaumcraft:candle_white")
    s.save("infusion_altar.nbt")


def smelting():
    s = Structure(5, 3, 5)
    s.floor()
    s.set(2, 1, 2, "thaumcraft:smelter_basic", facing="south", enabled=False)
    s.set(2, 2, 2, "thaumcraft:alembic")
    # Bellows on the west side and at the back, pointing at the smeltery.
    s.set(1, 1, 2, "thaumcraft:bellows", facing="east", enabled=True)
    s.set(2, 1, 1, "thaumcraft:bellows", facing="south", enabled=True)
    # Alembic -> tube -> tube going down -> jar.
    s.set(3, 2, 2, "thaumcraft:tube", **tube(west=True, east=True))
    s.set(4, 2, 2, "thaumcraft:tube", **tube(west=True, down=True))
    s.set(4, 1, 2, "thaumcraft:jar_normal")
    s.save("smelting.nbt")


def arcane_workbench():
    s = Structure(5, 2, 5)
    s.floor()
    # The scene starts with a crafting table here and swaps it.
    s.set(2, 1, 2, "thaumcraft:arcane_workbench")
    s.save("arcane_workbench.nbt")


def research_table():
    s = Structure(5, 2, 5)
    s.floor()
    # The scene starts with a wooden table here and swaps it.
    s.set(2, 1, 2, "thaumcraft:research_table", facing="north")
    # Surroundings that can give bonus aspects: water (Aqua), dirt (Terra), a circuit block (Ordo).
    s.set(0, 1, 4, "minecraft:water", level=0)
    s.set(4, 1, 4, "minecraft:grass_block", snowy=False)
    s.set(0, 1, 0, "minecraft:redstone_torch", lit=True)
    s.save("research_table.nbt")


def essentia_tubes():
    s = Structure(7, 3, 5)
    s.floor()
    # Jar -> tube up -> line of tubes -> tube down -> jar. The scenes swap the middle tube (3,2,2) for each variant.
    s.set(1, 1, 2, "thaumcraft:jar_normal")
    s.set(1, 2, 2, "thaumcraft:tube", **tube(down=True, east=True))
    for x in (2, 3, 4):
        s.set(x, 2, 2, "thaumcraft:tube", **tube(west=True, east=True))
    s.set(5, 2, 2, "thaumcraft:tube", **tube(west=True, down=True))
    s.set(5, 1, 2, "thaumcraft:jar_normal")
    s.save("essentia_tubes.nbt")


def centrifuge():
    s = Structure(5, 4, 5)
    s.floor()
    # Source jar under the centrifuge; the output splits to one jar on each side.
    s.set(2, 1, 2, "thaumcraft:jar_normal")
    s.set(2, 2, 2, "thaumcraft:centrifuge")
    s.set(2, 3, 2, "thaumcraft:tube", **tube(down=True, west=True, east=True))
    s.set(1, 3, 2, "thaumcraft:tube", **tube(east=True, down=True))
    s.set(3, 3, 2, "thaumcraft:tube", **tube(west=True, down=True))
    s.set(1, 2, 2, "thaumcraft:tube", **tube(up=True, down=True))
    s.set(3, 2, 2, "thaumcraft:tube", **tube(up=True, down=True))
    s.set(1, 1, 2, "thaumcraft:jar_normal")
    s.set(3, 1, 2, "thaumcraft:jar_normal")
    s.save("centrifuge.nbt")


if __name__ == "__main__":
    os.makedirs(OUT, exist_ok=True)
    crucible()
    infusion()
    smelting()
    arcane_workbench()
    research_table()
    essentia_tubes()
    centrifuge()
