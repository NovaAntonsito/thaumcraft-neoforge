# Etapa 3: primeros bloques e ítems

## Qué se agregó
- **Bloques** (`common/config/ConfigBlocks.java`): `ore_amber`, `ore_cinnabar`, `ore_quartz`, `amber_block`, `amber_brick`, `metal_thaumium`, `metal_void`, `metal_brass`. Cada uno registra su `BlockItem`.
- **Ítems** (`common/config/ConfigItems.java`): `amber`, `quicksilver`, `ingot_*` (thaumium, void, brass), `nugget_*` (iron, copper, tin, silver, lead, quicksilver, thaumium, void, brass, quartz, rareearth) y `crystal_essence`.
- **Cristal de vis** (`ItemCrystalEssence`): guarda su aspecto en el Data Component `thaumcraft:aspects` (`TCDataComponents`). El color se pinta en el cliente con el color del aspecto.
- **Pestaña creativa** "Thaumcraft" (`itemGroup.thaumcraft`), con un cristal por aspecto.

## Cómo se pasó de 1.12.2 a 1.21.1
| Original | Port |
|---|---|
| `ItemTCBase("nugget", ...)` con metadata | un ítem por variante: `nugget_iron`, ... |
| NBT `Aspects` en el cristal | Data Component `thaumcraft:aspects` |
| blockstates `forge_marker` | blockstates y modelos vanilla (`cube_all`, `cube_bottom_top`) |
| texturas en `textures/blocks` y `textures/items` | se mantienen; `assets/minecraft/atlases/blocks.json` agrega esas carpetas al atlas |
| `getDrops`/`quantityDroppedWithBonus` | loot tables con `ore_drops` (misma fórmula) y silk touch |
| `OreDictionary` | tags `c:` (`c:ores/amber`, `c:gems/amber`, `c:ingots/thaumium`, ...) |
| `GameRegistry.addShapedRecipe` / `addSmelting` | recetas JSON en `data/thaumcraft/recipe` (mismos nombres) |
| `tile.x.name` / `item.x.name` | se copian a `block.thaumcraft.x` / `item.thaumcraft.x` en los 9 idiomas |

## Generación de minerales
Desde la etapa 9a va por código (`ThaumcraftWorldGenerator`, ver `etapa-9a1-mundo.md`), en todos los biomas del overworld. Copia `ThaumcraftWorldGenerator.generateOres`:
| Mineral | Intentos por chunk | Altura |
|---|---|---|
| Cinnabar | 18 | y 0 a 50 (la altura del mundo / 5 en 1.12) |
| Quartz | 18 | y 0 a 63 (la altura / 4) |
| Amber | 20 | de 0 a 24 bloques bajo la superficie |

Cada intento coloca **un solo bloque** (`replace_single_block`), y solo si ahí hay piedra (incluye granito, diorita y andesita, como el `Blocks.STONE` original). Por eso son raros. Solo aparecen en chunks **nuevos**.

Comando de debug: `/thaumcraft findore <amber|cinnabar|quartz> [radio en chunks, por defecto 8]`. Te teletransporta al lado del más cercano.

## Pendiente (TODO)
- Ámbar: 6,6% de soltar un curio en vez de ámbar (falta el ítem curio).
- Ícono de la pestaña: el original usa las gafas (goggles).
- Recetas de crisol (latón, thaumium, cristales): etapa 4.
- `metal_alchemical` y `metal_alchemical_advanced`: llegan con las máquinas de esencia.
