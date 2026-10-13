# Compatibilidad con Create: investigación

Create 6.0.10 para NeoForge 1.21.1, como dependencia **opcional**. No se implementó nada todavía.

## Resumen
- **Mucho ya funciona gratis** porque las recetas usan tags `c:`. Las placas de latón y de hierro de Create (`c:plates/brass`, `c:plates/iron`) ya sirven en 14 recetas de Thaumcraft cada una. El lingote de latón de Create ya tiene aspectos (`#c:ingots/brass` en `vanilla.json`).
- **Hay un hueco que no depende de Create**: el pedestal arcano no expone `Capabilities.ItemHandler`. Las tolvas andan porque usan `Container`, pero el brazo mecánico, los embudos y las cintas de Create solo usan la capability. Arreglarlo sirve para cualquier mod.
- **Casi todo lo demás se hace con datos** (JSON con condición `neoforge:mod_loaded`), sin compilar contra Create.
- **Lo que necesita Java** (Display Link, brazo sobre el crisol) va aislado en `thaumcraft.compat.create`, compilado con `compileOnly` y cargado solo si `ModList` tiene `create`. Igual que el piloto de Ponder.

## Recomendación por niveles

### Nivel 0: arreglos propios (sin Create)
| Qué | Por qué |
|---|---|
| Registrar `Capabilities.ItemHandler.BLOCK` para `PEDESTAL` (`InvWrapper`, 1 casilla) | El brazo mecánico podría cargar y vaciar los pedestales de infusión. Hoy no puede. |
| Dejar el crisol sin item handler | Si un embudo o cinta tira ítems, el crisol los **derrite pero no craftea**: `findMatchingCrucibleRecipe` devuelve `null` sin jugador (`ThaumcraftCraftingManager.java:63`). Es el comportamiento de TC6 y conviene mantenerlo. |

### Nivel 1: solo datos (recomendado empezar acá)
Todo con `"neoforge:conditions": [{"type": "neoforge:mod_loaded", "modid": "create"}]` o entradas `"required": false` en tags.

| Qué | Archivo |
|---|---|
| **Aspectos de Create**: zinc (ingot, nugget, ore, raw), aleación de andesita, cuarzo rosa (y pulido), menas trituradas (`#create:crushed_raw_materials`), placas (`#c:plates`), mecanismo de precisión, pepita de experiencia, harina de ceniza, tubo de electrones. El resto sale solo de las recetas de mesa de crafteo (ejes, engranajes, carcasas). | `data/thaumcraft/thaumcraft/aspects/create.json` |
| **Prensa mecánica** para placas de taumio y de vacío (`create:pressing`, entrada `#c:ingots/thaumium` → `c:plates/thaumium`) | `data/thaumcraft/recipe/compat/create/pressing/*.json` |
| **Trituradora** para menas de ámbar y cinabrio (más una pepita de mercurio con probabilidad, como el horno infernal) | `.../compat/create/crushing/*.json` |
| **Nitor** como calor pasivo de la caldera | tag `create:passive_boiler_heaters` |
| **Pilares e infusion matrix** no movibles con artilugios (no romper el altar a mitad de infusión) | tag `create:non_movable` |
| **Ponder**: alinear `ponder_version` con la que trae Create (1.0.82 para Create 6.0.10). Create mete Ponder adentro de su jar, así que con Create instalado nuestras escenas aparecen solas. | `gradle.properties` |

**Quemador de blaze como calor del crisol**: con solo agregarlo al tag `thaumcraft:crucible_heaters`, también calentaría apagado. Mejor una línea en el chequeo de calor que mire la propiedad `blaze` del bloque por nombre (sin importar clases de Create).

### Nivel 2: Java aislado (opcional, después)
| Qué | API de Create |
|---|---|
| **Display Link**: mostrar en pantallas la esencia de un frasco, y vis y flux del chunk | `CreateRegistries.DISPLAY_SOURCE` |
| **Brazo mecánico sobre el crisol**: que el brazo "tire" ítems adentro (solo derrite, como arriba) | `CreateRegistries.ARM_INTERACTION_POINT_TYPE` |
| **Procesado con ventilador** propio (por ejemplo, "purificar" con un bloque de flux como catalizador) | `CreateRegistries.FAN_PROCESSING_TYPE` |

### No recomendado
- **Gafas de ingeniero** sobre frascos y tubos: `IHaveGoggleInformation` es una interfaz que tendrían que implementar nuestros block entities, y eso rompe la carga del mod sin Create. Solo con mixin, no vale la pena.
- **Esencia como fluido** para las tuberías de Create: cambia el diseño de los tubos de esencia.
- **Craftear recetas arcanas con el crafter mecánico**: son otro tipo de receta y piden vis e investigación. Dejarlo así mantiene el balance.
- **Energía cinética moviendo máquinas de Thaumcraft**: los fuelles ya cumplen ese papel.

## Cómo se agrega la dependencia
Del wiki oficial (`wiki.createmod.net/developers/depend-on-create/neoforge-1.21.1`):
```
maven { url = "https://maven.createmod.net" }               // Create, Ponder, Flywheel
maven { url = "https://maven.ithundxr.dev/snapshots" }      // Registrate
create_version = 6.0.10-280
ponder_version = 1.0.82
flywheel_version = 1.0.6
registrate_version = MC1.21-1.3.0+67
```
Para el Nivel 1 no hace falta nada de esto. Para el Nivel 2: `compileOnly` del jar `slim` de Create (`transitive = false`), `localRuntime` del jar completo, y en `neoforge.mods.toml` `type="optional"` con `versionRange="[6.0.10,6.1.0)"`.

## Qué falta verificar
- No pude bajar el jar de Create (el proxy de este entorno bloquea `maven.createmod.net`). Los registros, tags e interfaces salen del código fuente de la rama `mc1.21.1/dev` en GitHub. Las versiones salen del wiki.
- Que el brazo mecánico tome bien el ítem del pedestal una vez registrada la capability (prueba en `runClient` con Create).
