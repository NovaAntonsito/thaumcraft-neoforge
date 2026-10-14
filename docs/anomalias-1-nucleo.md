# Anomalías, fase 1: reloj, medidor y las dos primeras

Las anomalías son modificadores del mundo estilo rogue-lite. No están en Thaumcraft. El diseño completo está en `diseno-anomalias.md` en la carpeta del proyecto. Esta fase trae el núcleo y dos anomalías para probarlo.

## Qué entra
- **Reloj por dimensión**: se guarda con el mundo (`data/thaumcraft_anomalies.dat`). Por defecto sale una anomalía cada **40 min** de juego, con ±20 % de variación.
- **Sorteo con peso**: mucho flux en los chunks cargados hace más probables las negativas; mucho vis, las positivas. La luna llena favorece las positivas y la luna nueva, las negativas. En wuss mode solo salen positivas.
- **Ciclos y niveles**: cada 5 anomalías sube un nivel. Desde el nivel 3 (10 anomalías) pueden correr **dos a la vez**.
- **Pausa**: mientras están ocupados todos los lugares, el reloj no avanza.
- **Medidor arriba al centro**:
  - Muestra `?`, la barra que se llena y el tiempo que falta.
  - En el último minuto la barra se pone naranja y suena un aviso.
  - En pausa la barra se ve gris y muestra `||`.
  - Debajo aparecen las anomalías activas con su color (verde = positiva, rojo = negativa) y el tiempo que les queda.
- **Aviso en el chat** al empezar (nombre y qué hace) y al terminar, con sonido de faro.

## Las dos anomalías
| Id | Nombre | Tipo | Qué hace |
|---|---|---|---|
| `aurora_arcana` | Arcane Aurora | Positiva | Vis se regenera ×2 · recetas arcanas −25 % de vis · destellos en el cielo |
| `aura_drought` | Aura Drought | Negativa | Vis se regenera ×0,5 · recetas arcanas y focos +50 % de vis · ceniza en el aire |

Las dos duran entre 5 y 15 minutos.

## Comandos (nivel de permiso 2)
- `/thaumcraft anomaly status`: tiempo para la próxima, ciclo, nivel y activas.
- `/thaumcraft anomaly list`: todas las anomalías.
- `/thaumcraft anomaly start <id> [minutos]`: arranca una ya.
- `/thaumcraft anomaly stop [id]`: frena una o todas.
- `/thaumcraft anomaly skip`: la próxima sale ya.
- `/thaumcraft anomaly timer <minutos>`: cuánto falta para la próxima.

## Config (`thaumcraft-common.toml`, sección `[anomalies]`)
`enabled`, `intervalMinutes`, `intervalJitter`, `maxActive`, `positiveWeight`, `negativeWeight`, `blacklist`, `dimensions` (por defecto solo `minecraft:overworld`), `showHud`, `hudPosition` (`top_center` o `top_left`).

## Cómo probarlo
1. `runClient` y entrar a un mundo con trucos. Arriba al centro aparece el medidor.
2. `/thaumcraft anomaly timer 1`: en un minuto la barra se llena y sale una anomalía. En los últimos segundos se pone naranja.
3. `/thaumcraft anomaly start aura_drought`. En la mesa arcana, una receta de 50 de vis ahora pide 75. Con el guantelete, los focos gastan más.
4. `/thaumcraft anomaly stop` y `/thaumcraft anomaly start aurora_arcana`: la misma receta pide 38. `/thaumcraft aura drain vis 100` y repetir `/thaumcraft aura`: el vis vuelve el doble de rápido.
5. Salir y volver a entrar: el tiempo y la anomalía activa se mantienen.

## Registro de objetos Java tocados
**Nuevos**
| Clase | Qué es |
|---|---|
| `common/anomaly/AnomalyStat` | Lo que puede cambiar una anomalía: regen de vis, vis de recetas arcanas, vis de focos |
| `common/anomaly/AnomalyType` | Definición de una anomalía: id, polaridad, peso, duración, nivel mínimo, multiplicadores y ganchos `onStart`/`onTick`/`onEnd` |
| `common/anomaly/Anomalies` | Registro y las dos anomalías |
| `common/anomaly/ActiveAnomaly` | Una anomalía corriendo (id, inicio, fin) |
| `common/anomaly/AnomalyManager` | `SavedData` por dimensión: reloj, sorteo, pausa, niveles, inicio y fin, sync |
| `common/anomaly/AnomalyModifiers` | `get(level, stat)`: el multiplicador que leen los demás sistemas (1.0 si no hay nada) |
| `common/anomaly/AnomalyClientState` | Copia del estado en el cliente |
| `common/anomaly/PacketAnomalySync` | Paquete servidor → cliente con el reloj y las activas |
| `common/anomaly/AnomalyEvents` | Tick del mundo, login, cambio de dimensión y respawn |
| `common/anomaly/AnomalyCommand` | `/thaumcraft anomaly ...` |
| `client/gui/AnomalyHud` | El medidor de arriba |

**Modificados**
| Clase | Cambio |
|---|---|
| `Config` | Sección `anomalies` |
| `Thaumcraft` | Registra los eventos y el comando |
| `ThaumcraftClient` | Registra el HUD sobre la barra de jefes |
| `common/network/TCNetwork` | Registra `PacketAnomalySync` |
| `common/world/aura/AuraThread` | La regeneración de vis se multiplica por `VIS_REGEN` |
| `common/items/casters/ItemCaster` | `getConsumptionModifier` multiplica por `FOCUS_VIS_COST` (no al craftear) |
| `common/container/ContainerArcaneWorkbench` | El chequeo de vis usa el costo con anomalías |
| `common/container/slot/SlotCraftingArcaneWorkbench` | Gasta el vis con anomalías |
| `client/gui/GuiArcaneWorkbench` | Muestra el costo con anomalías |

**Recursos**: `lang/en_us.json` (6 textos nuevos).

## Pendiente (próximas fases)
- Fase 2: tormenta de vis y grieta del vacío gigante.
- Fase 3: el resto de la lista.
- Fase 4: la investigación **Presagio** (elegir 1 de 3 cartas), entradas en el Thaumonomicon y escaneo de anomalías.
