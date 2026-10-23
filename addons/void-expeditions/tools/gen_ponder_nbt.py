"""
Generates the Ponder scene structures of Void Expeditions
(addons/void-expeditions/src/main/resources/assets/voidexpeditions/ponder/*.nbt).

Reuses Thaumcraft's NBT writer and Structure builder (tools/gen_ponder_nbt.py) and only points its output here.

Run from the project root:  python addons/void-expeditions/tools/gen_ponder_nbt.py
"""
import os
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.join(HERE, "..", "..", "..", "tools"))
import gen_ponder_nbt as tc  # noqa: E402

tc.OUT = os.path.join(HERE, "..", "src", "main", "resources", "assets", "voidexpeditions", "ponder")

# The ring of GateStructure.FRAME, standing along x with its middle at (4, 4, 4).
FRAME = [(-1, 3), (0, 3), (1, 3), (-2, 2), (2, 2), (-3, 1), (3, 1), (-3, 0), (3, 0),
         (-3, -1), (3, -1), (-2, -2), (2, -2), (-1, -3), (0, -3), (1, -3)]
CENTER = (4, 4, 4)
TERMINAL = (4, 1, 6)


def void_gate():
    """The gate as built: an unlit ring and its terminal. The scene lights it up."""
    s = tc.Structure(9, 8, 9)
    s.floor("minecraft:polished_blackstone")
    cx, cy, cz = CENTER
    for u, v in FRAME:
        s.set(cx + u, cy + v, cz, "voidexpeditions:void_gate_ring", formed=False)
    s.set(*TERMINAL, "voidexpeditions:expedition_terminal")
    s.save("void_gate.nbt")


if __name__ == "__main__":
    os.makedirs(tc.OUT, exist_ok=True)
    void_gate()
