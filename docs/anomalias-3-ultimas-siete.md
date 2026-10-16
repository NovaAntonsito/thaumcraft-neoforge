# Anomalías fase 3b: las últimas 7

Estado: **hecha, compila, sin probar en el juego.** Con esto están las 16 de la lista.

## Las 7 nuevas
| Anomalía | Tipo | Qué hace |
|---|---|---|
| **Lluvia de cristales** (`crystal_rain`) | Positiva | Cada segundo, 1 de cada 4 por jugador: cae un cristal primal a menos de 10 bloques |
| **Lluvia de vitium** (`vitium_rain`) | Negativa | Cada segundo, 1 de cada 3 por jugador: cae un cristal de vitium. Si queda 30 s en el suelo, revienta y suelta 5 de flux |
| **Inversión** (`inversion`) | Mixta | Al empezar y al terminar, el vis y el flux se intercambian en los 5×5 chunks alrededor de cada jugador |
| **Vena de ámbar** (`amber_vein`) | Positiva, local | A 24–48 bloques de un jugador: esfera de radio 4 bajo la superficie con menas de ámbar (30 %) y cinabrio (15 %), +150 de vis. Columna de luz encima y flecha en el HUD. Las menas quedan |
| **Eclipse eldritch** (`eldritch_eclipse`) | Negativa, desde nivel 2 | La noche dura el doble. Cada minuto de noche a cielo abierto: +1 warp temporal. Warp Ward protege |
| **Murmullos** (`whispers`) | Negativa | Sonidos y humo que solo ve cada jugador. ~1 vez cada 3 min se olvida 1 punto de un aspecto al azar. Warp Ward protege |
| **Foco desbocado** (`runaway_focus`) | Mixta | Los focos cuestan la mitad. Cada lanzamiento suma un efecto al azar: rayo inofensivo donde mirás, levitación corta, prende fuego a los bichos cerca, velocidad, empuja a los bichos, o 3 de flux |

## Diferencias con el diseño
- Lluvia de vitium: no hay bloques de cristal de vitium portados, así que caen como ítems y revientan si no se juntan.
- Eclipse eldritch: los eventos de warp todavía no están portados, así que no se duplican. Queda un TODO.

## Objetos Java tocados
| Clase | Cambio |
|---|---|
| `common/anomaly/AnomalyCrystalRain` | **Nueva.** Lluvia de cristales y de vitium |
| `common/anomaly/AnomalyInversion` | **Nueva** |
| `common/anomaly/AnomalyAmberVein` | **Nueva** |
| `common/anomaly/AnomalyEldritchEclipse` | **Nueva** |
| `common/anomaly/AnomalyWhispers` | **Nueva** |
| `common/anomaly/AnomalyRunawayFocus` | **Nueva** |
| `common/anomaly/Anomalies` | Registra las 7 |
| `common/items/casters/ItemCaster` | Llama a `AnomalyRunawayFocus.afterCast` después de lanzar |

Más `en_us.json`: 16 textos nuevos.
