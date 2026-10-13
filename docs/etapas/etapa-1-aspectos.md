# Etapa 1: aspectos

## Qué hay
| Archivo | Qué es | Qué reemplaza de 1.12.2 |
|---|---|---|
| `api/aspects/Aspect.java` | Los 37 aspectos (6 primales, 10 secundarios, 21 terciarios) | Igual al original, más `CODEC` y `STREAM_CODEC` |
| `api/aspects/AspectList.java` | Lista de aspectos con cantidades | `readFromNBT`/`writeToNBT` → Codecs |
| `api/aspects/AspectHelper.java` | `getObjectAspects`, `reduceToPrimals` | Port parcial |
| `common/aspects/ObjectAspects.java` | Qué aspectos tiene cada ítem, cargado de datapack | `ConfigAspects` + OreDictionary → JSON + tags `c:` |
| `common/aspects/SyncObjectAspectsPayload.java` | Le manda los aspectos al cliente | `SimpleNetworkWrapper` → `CustomPacketPayload` |
| `api/capabilities/PlayerWarp.java`, `PlayerKnowledge.java` | Warp e investigación del jugador | Capabilities → Data Attachments |
| `common/registry/TCAttachments.java` | Registro de los attachments | `CapabilityManager.register` + `PlayerEvent.Clone` |
| `common/command/ThaumcraftCommand.java` | Comandos de prueba | — |
| `data/thaumcraft/thaumcraft/aspects/vanilla.json` | 43 entradas de ítems vanilla, sacadas de `ConfigAspects` | — |

## Cómo probarlo
1. `runClient` en IntelliJ (o `gradlew runClient`) y crear un mundo con trucos.
2. Pasar el mouse por un tronco, una manzana o un lingote de hierro y **mantener Shift**: aparecen los aspectos.
3. `/thaumcraft aspects` con algo en la mano: muestra los aspectos y su reducción a primales.
4. `/thaumcraft warp add @s normal 5`, después morir y `/thaumcraft warp get`: el warp sigue ahí (copyOnDeath).
5. Salir y volver a entrar: el warp se guardó.

## Pendiente para más adelante
- Los "complex tags" de TC6 (aspectos calculados a partir de las recetas). Por ahora solo hay asignaciones directas.
- Aspectos de entidades y el escaneo con el thaumómetro.
- El tooltip con Shift es temporal: en TC6 los aspectos se ven con gafas o thaumómetro, y solo si están investigados.

## Convenciones (pedido de Anton)
- El juego queda en **inglés y fiel al original**: nombres, claves de traducción y textos de TC6. La traducción al español la hace Anton después.
- Se usan los **assets originales** de TC6. Los idiomas se convirtieron de `.lang` a `.json` (con `%d` cambiado a `%s`) y los íconos de aspectos están en `textures/aspects/`.
- Los comentarios del código están en inglés. Las notas de `docs/` siguen en español.
