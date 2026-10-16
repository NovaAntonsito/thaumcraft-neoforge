# Anomalías, fase 2: tormenta de vis y grieta del vacío gigante

Dos anomalías grandes sobre el núcleo de la fase 1. Por ahora el texto del juego está en inglés, igual que el resto del mod.

## Tormenta de vis (`vis_storm`, mixta)
- Dura entre 5 y 10 minutos. Aparece desde el nivel 1.
- Cada segundo, cada jugador tiene 1 chance en 6 de que caiga un **rayo de vis** a menos de 20 bloques. Es un rayo solo visual: no quema ni lastima.
- 3 de cada 4 rayos suman 15 de vis al chunk, con partículas violetas. La mitad de esos también suelta un **cristal de vis primal** al azar.
- 1 de cada 4 suelta 8 de **flux** al chunk, con partículas negras.
- Mientras dura, el vis se regenera **×3**.

## Grieta del vacío gigante (`giant_rift`, negativa y local)
- Dura entre 8 y 15 minutos. Sale recién **desde el nivel 2** (después de 5 anomalías). Con el comando se puede forzar antes.
- Se abre una grieta de flujo de tamaño 120, unos 16 bloques de largo (las normales rondan 5 a 20), entre 24 y 48 bloques de un jugador al azar. Flota 4 bloques sobre el suelo.
- Cada 10 segundos crece 4 con un sonido de portal, hasta 300 (unos 40 bloques). Mientras dura la anomalía está **anclada**: no colapsa sola, pero sigue comiendo bloques y lastimando lo que la toca.
- **En el HUD** aparece con flecha y distancia, por ejemplo `↗ 34m 9:12`.
- **Cómo cerrarla**: usar **salis mundus en el aire** a menos de 8 bloques de la grieta. Cada uso gasta 1 salis mundus y la achica un cuarto de su tamaño (mínimo 20). Con 20 o menos se cierra. Arriba de la barra rápida aparece cuánto falta.
  - Si la cerrás a tiempo, colapsa como siempre (semillas del vacío y chance de perla) y además suelta **una perla primordial segura**. La anomalía termina en ese momento.
  - El sifón del vacío también la achica, pero así no da la perla extra.
- **Si se acaba el tiempo**, la grieta queda en el mundo como una grieta normal e inestable. Sigue comiendo bloques hasta que colapse por su cuenta.

## Cómo probarlo
1. `/thaumcraft anomaly start vis_storm`: empiezan a caer rayos cerca. Juntá cristales y mirá el aura con `/thaumcraft aura`.
2. `/thaumcraft anomaly start giant_rift`: en el HUD aparece la flecha. Andá hasta la grieta.
3. Con salis mundus en la mano, hacé clic derecho al aire cerca de la grieta. Con unos 5 a 8 usos se cierra (más si ya creció) y cae la perla.
4. Para ver qué pasa si se escapa: `/thaumcraft anomaly start giant_rift 1` y esperá el minuto.

## Registro de objetos Java tocados
**Nuevos**
| Clase | Qué es |
|---|---|
| `common/anomaly/AnomalyVisStorm` | La tormenta de vis: rayos, vis, flux y cristales |
| `common/anomaly/AnomalyGiantRift` | La grieta gigante: aparición, anclaje, crecimiento, cierre con salis mundus y escape |

**Modificados**
| Clase | Cambio |
|---|---|
| `Thaumcraft` | Registra el evento de clic derecho con ítem |
| `common/anomaly/ActiveAnomaly` | Guarda datos propios de cada anomalía, como la posición o el UUID de la grieta |
| `common/anomaly/AnomalyType` | Nuevo gancho `isFinished`, para terminar antes de tiempo |
| `common/anomaly/AnomalyManager` | Termina las anomalías que avisan que ya terminaron y manda la posición de las locales |
| `common/anomaly/PacketAnomalySync` | `Entry` lleva si es local y su x/z |
| `common/anomaly/AnomalyEvents` | `onRightClickItem`: salis mundus cerca de la grieta |
| `common/anomaly/Anomalies` | Registra las dos nuevas |
| `client/gui/AnomalyHud` | Más ancho; flecha y distancia para las anomalías locales |

**Recursos**: `lang/en_us.json` (7 textos nuevos).

No se tocaron `EntityFluxRift` ni `RenderFluxRift`: la grieta gigante es una grieta normal manejada desde afuera. Tampoco hizo falta un rayo propio (`EntityVisBolt`): se usa el rayo vanilla en modo solo visual.

## Arreglo: la grieta no crecía a la vista (2026-10-16)
Antes crecía 2 cada 30 s desde tamaño 60, un cambio invisible. Ahora empieza en 120 y crece 4 cada 10 s hasta 300. Objeto Java tocado: `common/anomaly/AnomalyGiantRift`.

## HUD con estilo Thaumcraft (2026-10-16)
- Fondo: el panel violeta del aviso de investigación (`hud.png`, 0,224).
- Sin ícono: la barra ocupa todo el ancho (se sacó el cerebro a pedido de Anton).
- Barra: la franja de vis que fluye del HUD de la varita, acostada y animada. Naranja al final, gris en pausa.
- Textos en violeta y dorado como `ResearchToast`. Cada anomalía activa tiene un rombo del color de su tipo: verde positiva, rojo negativa, dorado mixta.
- Objeto Java tocado: `client/gui/AnomalyHud`.
