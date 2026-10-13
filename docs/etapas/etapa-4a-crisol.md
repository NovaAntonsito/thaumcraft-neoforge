# Etapa 4a: el crisol

## Cómo se usa (igual que en TC6)
1. Craftea **Salis Mundus**: bowl + flint + redstone + 3 cristales de vis de aspectos distintos (sin forma). El bowl y el flint se devuelven.
2. Usa la Salis Mundus sobre un **caldero** (vacío o con agua): se convierte en **Crucible**.
3. Llénalo con un **balde de agua** (1000 mB).
4. Ponle debajo **lava, fuego o magma** (tag `thaumcraft:crucible_heaters`). Cuando el calor pasa de 150, hierve.
5. Tira ítems adentro (con Q o clic derecho desde arriba): se derriten en sus aspectos.
6. Si tiras un **catalizador** y hay aspectos suficientes, sale el resultado flotando.
7. Shift + clic derecho con la mano vacía lo vacía; los aspectos se vuelven flux en el aura.

Cada 5 segundos se evapora 1 punto de un aspecto al azar (flux al aura). Con más de 500 aspectos rebalsa.

## Archivos
| Original (1.12) | Port |
|---|---|
| `BlockCrucible` | `common/blocks/crafting/BlockCrucible.java` (`BaseEntityBlock`, `entityInside`, `useItemOn`) |
| `TileCrucible` (`ITickable`, `FluidTank`) | `common/tiles/crafting/TileCrucible.java` (`BlockEntity` + ticker, `FluidTank` de NeoForge) |
| `TileCrucibleRenderer` (GL11) | `client/renderers/tile/TileCrucibleRenderer.java` (`BlockEntityRenderer`, `VertexConsumer`) |
| `CrucibleRecipe` + `ThaumcraftApi.addCrucibleRecipe` | `api/crafting/CrucibleRecipe.java` (`Recipe` + `RecipeSerializer` con Codec) y JSON en `data/thaumcraft/recipe/crucible/` |
| `ItemMagicDust`, `IDustTrigger`, `DustTriggerSimple` | mismos nombres; `onItemUseFirst` |
| `RecipeMagicDust` (IRecipe) | `CustomRecipe` + `SimpleCraftingRecipeSerializer` (`thaumcraft:salis_mundus`) |
| `IEssentiaContainerItem` | igual; los cristales de vis devuelven su aspecto al derretirse |
| `EntitySpecialItem` | `ItemEntity` sin gravedad con el tag `thaumcraft.special_item` (no se vuelve a derretir) |
| `IFluidHandler` del tile | capability `Capabilities.FluidHandler.BLOCK` |

## Recetas portadas (48)
- 37 cristales de vis (2 del aspecto + quartz sliver).
- Latón (`instrumentum` 5 + lingote de hierro) y thaumium (`praecantatio` 5, `terra` 5 + lingote de hierro).
- Alquimia casera: cuero, lava, pólvora, slime, glowstone, tinta, arcilla, hilo y telaraña.

Las demás (nitor, alumentum, sellos de golem, etc.) llegan cuando existan sus ítems.

## Pendiente (TODO)
- **Investigación**: las recetas guardan su clave (`research`) pero todavía no se comprueba. El caldero → crisol tampoco.
- **Aspectos de objetos**: solo hay ~45 ítems con aspectos. Hay que portar el resto de `ConfigAspects` (427 entradas) para que casi todo se pueda derretir.
- Efectos visuales originales (espuma, burbujas, "bamf", chispas de la sal): por ahora partículas vanilla (etapa 10).
- El cambio caldero → crisol es inmediato; el original espera 50 ticks con efecto.
- Nitor como fuente de calor.
