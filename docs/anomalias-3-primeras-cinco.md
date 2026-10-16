# Anomalías fase 3a: las 5 simples

Estado: **hecha, compila, sin probar en el juego.** Van 9 de 16.

## Las 5 nuevas
| Anomalía | Tipo | Qué hace |
|---|---|---|
| **Marea de flux** (`flux_tide`) | Negativa | Cada minuto, +3 de flux en cada chunk cargado. Las grietas salen ×3 más fácil. Partículas de portal |
| **Calma del vacío** (`void_calm`) | Positiva | Cada segundo, cada chunk cargado pierde 1 % de su flux (mínimo 0,05): ~45 % por minuto. No salen grietas |
| **Luna de infusión** (`infusion_moon`) | Positiva | La infusión pierde la mitad de estabilidad por ciclo |
| **Inspiración** (`inspiration`) | Positiva | Escanear da ×2 puntos de aspecto. Escribir en la mesa de investigación y sacar notas no gasta tinta |
| **Convergencia primal** (`primal_convergence`) | Positiva | Se sortea un primal (aparece en el chat). Las recetas de crisol piden 30 % menos de ese primal. Caen cristales de ese primal cerca de los jugadores (1 de cada 40 por segundo, a menos de 8 bloques) |

Todas: peso 10, duran 5 a 15 min.

## Cómo probar
`/thaumcraft anomaly start <id>` con los ids de arriba, y `/thaumcraft anomaly stop` para cortar.

## Objetos Java tocados
| Clase | Cambio |
|---|---|
| `common/anomaly/AnomalyAuraTide` | **Nueva.** Marea de flux y calma del vacío |
| `common/anomaly/AnomalyPrimalConvergence` | **Nueva.** Convergencia primal |
| `common/anomaly/Anomalies` | Registra las 5 |
| `common/anomaly/AnomalyStat` | `RIFT_CHANCE`, `INFUSION_INSTABILITY`, `SCAN_POINTS`, `INK_COST` |
| `common/anomaly/AnomalyModifiers` | `freeInk`, `crucibleCost` |
| `common/world/aura/AuraThread` | Chance de grieta × `RIFT_CHANCE` |
| `common/tiles/crafting/TileInfusionMatrix` | Pérdida por ciclo × `INFUSION_INSTABILITY` |
| `common/lib/research/ScanGeneric` | Puntos × `SCAN_POINTS` |
| `common/tiles/crafting/TileResearchTable` | Tinta gratis con inspiración |
| `common/lib/research/ResearchNoteManager` | Tinta gratis con inspiración |
| `api/crafting/CrucibleRecipe` | `matches` y `removeMatching` con costo rebajado |
| `common/lib/crafting/ThaumcraftCraftingManager` | Pasa el mundo a `matches` |
| `common/tiles/crafting/TileCrucible` | Pasa el mundo a `removeMatching` |

Más `en_us.json`: 11 textos nuevos.

## Quedan de la fase 3
Eclipse eldritch, lluvia de vitium, murmullos, lluvia de cristales, vena de ámbar, foco desbocado, inversión.
