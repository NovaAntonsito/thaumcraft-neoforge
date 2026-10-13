# Etapa 4c: esencia (horno, alambique, frascos)

La infusión saca la esencia de frascos cercanos, así que antes de portarla hacía falta poder producir y guardar esencia.

## Qué se agregó
- **Fundición de esencia** (`smelter_basic`): funciona como un horno. Quema combustible y deshace el ítem en sus aspectos (hasta 256).
  - Pierde cerca del 20 % como flux, que va al aura.
  - Se abre con clic derecho. Tiene una casilla para el ítem y otra para el combustible.
  - Las tolvas meten ítems por los costados y combustible por abajo.
- **Alambique arcano** (`alembic`): va encima de la fundición y saca un aspecto por vez (hasta 128).
  - Se pueden apilar varios.
  - Con Shift y la mano vacía se vacía, y la esencia va al aura.
- **Frasco sellado** (`jar_normal`) y **frasco del vacío** (`jar_void`): guardan 250 de un aspecto.
  - Al romperlos conservan la esencia y la etiqueta.
  - El del vacío acepta esencia de más y la destruye.
  - Con Shift y la mano vacía se vacían. Sobre el lado de la etiqueta, se la sacan.
- **Redoma** (`phial`): lleva 10 de esencia de un alambique o frasco a otro frasco.
- **Etiqueta** (`label`): fija un frasco o alambique a un aspecto.
  - Se marca crafteándola con una redoma llena.
  - Sola en la mesa vuelve a quedar en blanco.
- **Abrazadera de latón** (`jar_brace`): bloquea el frasco y se recupera al romperlo.
- **Filtro de esencia** (`filter`).
- **Madera de gran árbol y de árbol plateado** (troncos y tablones): solo los bloques, sin árboles todavía.

## Cómo se usa
1. Poné la fundición, con un alambique arriba.
2. Cargá combustible y un ítem con aspectos.
3. El alambique se llena de a un aspecto.
4. Para sacar la esencia, hacé clic en el alambique con un frasco en la mano (se llena el ítem) o con una redoma vacía.

## Recetas
| Resultado | Dónde | Vis | Cristales |
|---|---|---|---|
| 8 redomas | mesa normal (arcilla y vidrio) | — | — |
| 4 etiquetas | mesa normal (tinte negro, bola de slime, 4 papeles) | — | — |
| 2 abrazaderas | mesa normal (pepitas de latón y palos) | — | — |
| 4 tablones | mesa normal (1 tronco) | — | — |
| Frasco sellado | mesa arcana | 5 | — |
| Frasco del vacío | mesa arcana (con un frasco sellado) | 50 | perditio |
| 2 filtros | mesa arcana (oro y tablón plateado) | 15 | aqua |
| Alambique | mesa arcana | 50 | aqua |
| Fundición de esencia | mesa arcana (con un crisol y un horno) | 50 | ignis |

El alambique y el filtro piden tablones de gran árbol y plateado. Por ahora solo se consiguen en creativo, hasta que estén los árboles.

## Cómo se pasó de 1.12.2 a 1.21.1
| Original | Port |
|---|---|
| metadata 0/1 de redoma y etiqueta (vacía/llena) | el componente `thaumcraft:aspects`: llena si lo tiene |
| NBT `AspectFilter` del frasco | el componente `thaumcraft:aspect_filter` |
| `BlockJar.spawnFilledJar` | loot table con `copy_components` desde el tile |
| `BlockJarItem.placeBlockAt` | `applyImplicitComponents` del tile al colocarlo |
| `onItemUseFirst(EntityPlayer, World, …)` | `onItemUseFirst(ItemStack, UseOnContext)` de NeoForge |
| `ISidedInventory` | `WorldlyContainer` y la capability de ítems con `SidedInvWrapper` |
| `sendWindowProperty` 0-4 | un `ContainerData` con 5 valores |
| `ModelJar` y GL11 | `ModelPart` armado en código y `PoseStack` |
| `IItemPropertyGetter "fill"` | `ItemProperties` `thaumcraft:fill` y `thaumcraft:filled` |
| receta `label_<aspecto>` con `IngredientNBTTC` | ingrediente `neoforge:components` |
| `ShapedArcaneVoidJar` | serializador propio `thaumcraft:arcane_void_jar` |

Dos cosas del original que se corrigieron:
- Con alumentum, la velocidad del horno quedaba en 0 (`speed *= (int) 0.8`). Ahora es el 80 %.
- En el código decompilado, el bucle que reparte el flux entre los respiraderos no termina si no hay ninguno. Se portó lo que busca: sin respiraderos, todo el flux va al aura.

## Pendiente (TODO)
- Fundiciones de thaumium y de metal del vacío, bomba auxiliar, respiradero y fuelles.
- Alumentum (acelera la fundición).
- Tubos de esencia y las boquillas del alambique hacia ellos.
- Cerebro en un frasco.
- Árboles de gran árbol y plateado.
- Partículas propias (etapa 10).
