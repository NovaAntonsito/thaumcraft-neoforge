# Etapa 9a-1: cristales, plantas y árboles en el mundo

Primera parte de la generación del mundo. Los biomas (Magical Forest y Eerie, con TerraBlender) van en la 9a-2.

## Bloques nuevos

| Bloque | Qué hace |
|---|---|
| `crystal_aer` … `crystal_vitium` (7) | Cristales de vis pegados a la roca. Crecen (size 0 a 3) si el aura tiene más vis que su base, y se esparcen hasta 4 generaciones. El de flux (vitium) se alimenta de flux. Si el aura baja, achican y devuelven vis. Al romperlos sueltan 1 cristal por etapa de crecimiento. Si les sacás la roca, se caen. |
| `leaves_greatwood`, `leaves_silverwood` | Hojas. Las de silverwood suman 0.01 de vis por tick aleatorio mientras el aura esté bajo su base. Drop: retoño (1/75 con fortuna 1/59), y la silverwood además una pepita de mercurio (1/150 a 1/90). Con tijera o toque de seda dan la hoja. |
| `sapling_greatwood`, `sapling_silverwood` | Retoños. El greatwood necesita 2x2 retoños para crecer. Polvo de hueso: 25% de chance. |
| `cinderpearl` | Planta del desierto, luz 7, tira humo y llamitas. |
| `shimmerleaf` | Planta que sale al pie de los silverwood, luz 6, partículas de wisp. |
| `vishroom` | Hongo, luz 6, a veces da náusea al tocarlo. Se puede poner sobre cualquier cara sólida. Todavía no se genera (va con el Magical Forest). |

Los cristales usan el modelo 3D del original (`models/obj/crystal.obj`) con `CrystalModel`: por cada cara que toca roca saca tantas puntas como su tamaño, giradas para que crezcan desde esa cara.

## Generación

Se agrega con dos features por chunk (`thaumcraft:ores` y `thaumcraft:vegetation`) y dos biome modifiers en `data/thaumcraft/neoforge/biome_modifier`. Reemplazan a los JSON de menas de la etapa 3 (se borraron `ore_*.json` y `add_ores.json`).

| Qué | Por chunk | Dónde |
|---|---|---|
| Cinnabar | 18 intentos | y 0 a 50, en piedra (granito, diorita, andesita también) |
| Quartz | 18 intentos | y 0 a 63 |
| Amber | 20 intentos | hasta 24 bloques bajo la superficie |
| Cristales | hasta 8 grupos 3x3x3, máx. 64 cristales | cualquier altura bajo la superficie, junto a roca. 1 de cada 3 grupos toma el aspecto del bioma |
| Greatwood | 1 en 25 | según el bioma (`BiomeHandler`). 1 de cada 8 es la variante con spawner de arañas, telarañas y cofre |
| Silverwood | 1 en 80 | biomas mágicos, windswept forest, old growth birch forest, o la mitad de la chance de greatwood |
| Cinderpearl | 1 en 30 | desiertos y badlands calientes |

## Config nueva (sección `world`)

`oreDensity` (100, de 1 a 500), `generateCinnabar`, `generateAmber`, `generateQuartz`, `generateCrystals`, `generateTrees`.

## Investigación

- El escaneo de menas ahora incluye los cristales (`!ORECRYSTAL`).
- El de plantas incluye retoños (`!PLANTWOOD`), cinderpearl, shimmerleaf y vishroom.

## Diferencias con el original

- Las listas negras de dimensiones y biomas de la config son las listas de biomas de los biome modifiers.
- No hay "regen": los chunks que ya existen no se vuelven a generar.
- Las alturas son las absolutas del original (mundo de 0 a 256). Bajo y 0 no sale nada.
- Los biomas 18 y 28 de 1.12 (forest hills y birch forest hills) ya no existen. En su lugar van windswept forest y old growth birch forest.
- "Material.ROCK" no existe más. Roca es todo bloque con el instrumento de bombo (`BASEDRUM`), que es lo que usa vanilla para piedra, menas, deepslate, etc.
- El retoño no dispara el evento `TerrainGen.saplingGrowTree` (no existe en NeoForge).
- La semilla con la que los cristales eligen sus puntas sale del random del modelo, así que el dibujo puede no coincidir con un mundo de 1.12.
- El item de las hojas de greatwood tiene el verde de follaje por defecto, como las hojas vanilla. En 1.12 no tenía color.
- Quedan para después: la `grass_ambient` y el Big Magic Tree (9a-2), los montículos y portales de cultistas (9c).

## Cómo probarlo

1. Mundo nuevo (los chunks viejos no cambian).
2. Bajá a una cueva: los cristales brillan un poco y tienen el color de su aspecto.
3. En cualquier bosque (`/locate biome minecraft:forest`) buscá greatwoods (troncos 2x2 enormes). En `minecraft:old_growth_birch_forest` salen silverwoods con shimmerleaf al pie.
4. En un desierto: cinderpearl.
5. Rompé hojas para sacar retoños, plantalos (greatwood en 2x2) y dales polvo de hueso.
