# Etapa 4a (extra): aspectos de los objetos

## Qué se agregó
- **Toda la lista de `ConfigAspects.registerItemAspects`** del original, pasada a datos:
  - `data/thaumcraft/thaumcraft/aspects/vanilla.json`: 294 entradas (vanilla y metales/gemas por tag `c:`).
  - `data/thaumcraft/thaumcraft/aspects/thaumcraft.json`: los ítems de Thaumcraft que ya existen en el port.
- **Aspectos generados por receta** (`ThaumcraftCraftingManager.getObjectTags`), como en TC6. Un ítem sin entrada saca sus aspectos de:
  1. su receta de crisol (aspectos del catalizador + raíz cuadrada de los de la receta);
  2. si no hay, su receta de mesa de crafteo: la suma de los ingredientes × 0,75 ÷ la cantidad que sale. Si hay varias recetas, gana la de menos aspectos.
- **Aspectos extra** (`getBonusTags`):

| Objeto | Extra |
|---|---|
| Armadura | praemunio = defensa × 4 |
| Espada | aversio = (daño + 1) × 4 |
| Arco | aversio 10, volatus 5 |
| Pico, hacha, pala | instrumentum = (nivel de minado + 1) × 4 |
| Tijeras, azada | instrumentum 4, 8, 12 o 16 según la durabilidad |
| Tintes | sensus 5 |
| Encantamientos | un aspecto por encantamiento (nivel × 3) y praecantatio |

  Al final se dejan como mucho 7 aspectos (`AspectHelper.cullTags`) y ninguno pasa de 500.
- **Pociones** (`common/config/ConfigAspects.getPotionTags`): recorre la cadena de destilería hasta el agua y suma los ingredientes, como el original.

## Ejemplos
| Objeto | Aspectos |
|---|---|
| Pico de hierro | metallum 33, instrumentum 12, herba 1 |
| Pedernal | terra 5, instrumentum 5 |
| Mechero (flint and steel) | lo de la receta + ignis 10, instrumentum 5 |

## Cómo se pasó de 1.12.2 a 1.21.1
| Original | Port |
|---|---|
| `registerObjectTag("ingotIron", ...)` | `"items": ["#c:ingots/iron"]` |
| `new ItemStack(Blocks.STONEBRICK, 1, 1)` | `minecraft:mossy_stone_bricks` (un ítem por metadata) |
| `registerComplexObjectTag(...)` | `"complex": true`: se suman a los generados por receta |
| `new AspectList(new ItemStack(X))` | `"base": "minecraft:x"`: arranca de los aspectos de X |
| `.merge(...)` sobre una base | `"merge": {...}` |
| `CraftingManager.REGISTRY` | `RecipeManager` del servidor, o el que recibe el cliente |
| `PotionHelper.POTION_TYPE_CONVERSIONS` (reflexión) | `PotionBrewing.potionMixes` (también por reflexión) |
| rareza del encantamiento | peso (`weight`) de 1.21: 10, 5, 2, 1 son común, poco común, raro, muy raro |
| nivel de minado del material | el `Tier` (madera y oro 0, piedra 1, hierro 2, diamante 3, netherite 4) |

Cuando una tag de 1.21 cubre lo que el original listaba a mano (todas las puertas, botes o vallas de madera), se usa la tag. Así las maderas nuevas también tienen aspectos.

El script `tools/gen_aspects.py` lee `ConfigAspects.java` y genera los dos JSON. Si cambia algo del original, se vuelve a correr.

Los aspectos generados se calculan la primera vez que se piden y quedan en memoria. Se recalculan con `/reload` o cuando cambian las tags.

## Pendiente (TODO)
- 90 líneas del original son ítems de Thaumcraft que todavía no existen: gafas, taumómetro, curios, árboles, cristales, etc. Se agregan a `thaumcraft.json` cuando se porten esos ítems.
- Recetas de infusión y de la mesa arcana como fuente de aspectos: llegan con esas mecánicas.
- Ítems que no existían en 1.12 y no tienen receta (hierro en bruto, cobre en bruto, etc.) no tienen aspectos. TC6 tampoco los conocía.
