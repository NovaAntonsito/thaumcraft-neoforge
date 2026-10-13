# Etapa 2: aura (vis y flux)

## Qué hay
| Archivo | Qué es | Qué cambió respecto de 1.12.2 |
|---|---|---|
| `common/world/aura/AuraChunk.java` | Vis, flux y base de un chunk | Ahora es un *data attachment* del chunk: se guarda solo, sin escribir NBT en `ChunkDataEvent` |
| `common/world/aura/AuraWorld.java`, `AuraHandler.java` | Caché de chunks cargados y lógica de vis/flux | La dimensión pasó de `int` a `ResourceKey<Level>` |
| `common/world/aura/AuraThread.java` | Regeneración, propagación entre chunks y fases lunares | **Misma matemática**, pero corre en el hilo del servidor cada 20 ticks en vez de en un hilo propio |
| `common/world/aura/AuraEvents.java` | Carga y descarga de mundos y chunks, y el tick | Junta las partes de aura de `WorldEvents`, `ChunkEvents` y `ServerEvents` |
| `common/world/biomes/BiomeHandler.java` | Nivel de aura por tipo de bioma | `BiomeDictionary` → tags `c:is_*` de NeoForge, con los valores originales |
| `api/aura/AuraHelper.java` | La API pública | Llama directo a `AuraHandler` |

## Cómo probarlo
1. `runClient` y crear un mundo **nuevo**. Los chunks que ya existían también reciben aura cuando se cargan.
2. `/thaumcraft aura`: muestra vis, flux y base del chunk donde estás. Un bosque debería tener más base que un desierto.
3. `/thaumcraft aura drain vis 100` y repetir `/thaumcraft aura` cada tanto: el vis se recupera de a poco (más rápido en luna llena).
4. `/thaumcraft aura add flux 200`: el flux se reparte a los chunks vecinos con el tiempo.
5. Salir y volver a entrar: los valores se guardaron.

## Pendiente
- Las grietas de flux (`EntityFluxRift`): el disparador ya existe y está marcado con TODO.
- El efecto visual de `polluteAura` y el HUD de aura (llegan con el thaumómetro).
- Las listas negras de biomas y dimensiones de la config original.
