# Etapa 4b: mesa arcana

## Qué se agregó
- **Mesa arcana** (`arcane_workbench`): se hace tirando salis mundus sobre una mesa de crafteo, como en el original.
  - Grilla de 3x3 y 6 casillas de cristal (aer, ignis, aqua, terra, ordo, perditio).
  - Guarda lo que tiene adentro. Si se rompe, suelta todo.
  - También sirve como mesa de crafteo normal.
- **Recetas arcanas** (`thaumcraft:arcane_shaped` y `thaumcraft:arcane_shapeless`): una receta de crafteo con costo de vis y cristales.
  - El vis sale del aura del chunk donde está la mesa.
  - La pantalla muestra el vis disponible y el costo.
  - Las casillas de los cristales que pide la receta brillan.
  - Si no alcanza el vis, el resultado se ve gris y no se puede sacar.
- **Ítems nuevos**: placas (latón, hierro, thaumium, metal del vacío), mecanismo arcano simple y complejo, resonador de vis, vidrio espejado, resonador mórfico y tela encantada.

## Recetas
| Resultado | Dónde | Vis | Cristales |
|---|---|---|---|
| 3 placas (de cada metal) | mesa normal, 3 lingotes en fila | — | — |
| Mecanismo arcano simple | mesa arcana | 10 | ignis, aqua |
| Mecanismo arcano complejo | mesa arcana | 50 | ignis, aqua |
| Resonador de vis | mesa arcana | 50 | aer, aqua |
| Vidrio espejado | mesa arcana | 50 | aqua, ordo |
| Resonador mórfico | mesa arcana | 50 | aer, ignis |
| Tela encantada | mesa arcana | 5 | — |

## Cómo se pasó de 1.12.2 a 1.21.1
| Original | Port |
|---|---|
| `IArcaneRecipe` dentro del registro de crafteo | tipo de receta propio `thaumcraft:arcane_crafting` |
| `InventoryArcaneWorkbench` (InventoryCrafting 5x3) | `ArcaneCraftingInput` (15 casillas) y un `SimpleContainer` en la mesa |
| `ShapedOreRecipe` / `ShapelessOreRecipe` | una `ShapedRecipe` / `ShapelessRecipe` vanilla adentro, con tags `c:` |
| GUI id 13 de `ProxyGUI` | `MenuType` `thaumcraft:arcane_workbench` |
| `sendWindowProperty(0, vis)` | un `DataSlot` |
| GL11 en la pantalla | `GuiGraphics` (misma textura y posiciones) |

Las recetas arcanas también generan aspectos: lo de los ingredientes más praecantatio por el costo de vis, como en TC6.

## Pendiente (TODO)
- Investigación: el original pide conocer la investigación de cada receta (`BASEARTIFICE`, etc.). Por ahora todas están abiertas.
- Cargador de la mesa (`arcane_workbench_charger`): toma vis de los 9 chunks de alrededor.
- Descuento de vis por túnicas y casters.
- El resto de las recetas arcanas: llegan con sus ítems (taumómetro, gafas, tubos…).
