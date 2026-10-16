# Anomalías fase 4: Foresight, Thaumonomicon y escaneo

Estado: **hecha, compila, sin probar en el juego.** Con esto se cierra la etapa 9c.

## Cómo se juega
1. **Ver la próxima con el taumómetro**: con el taumómetro en la mano, el HUD muestra "Próxima: Positiva / Negativa / Mixta". Las próximas anomalías se sortean al empezar cada ciclo.
2. **Escanear anomalías**: mientras hay una activa, apuntá el taumómetro al cielo (a nada) y escaneá. Las locales (grieta gigante, vena de ámbar) hay que escanearlas a menos de 32 bloques.
   - Desbloquea la entrada de esa anomalía en el Thaumonomicon (pestaña **Auromancia**, hexágonos arriba a la izquierda).
   - Da puntos de aspecto: positivas auram 6 + praecantatio 4, negativas vitium 6 + alienis 4, mixtas auram 4 + vitium 4 + praecantatio 2.
   - La primera vez desbloquea **Anomalías del mundo**.
3. **Foresight** (en español será Presagio): investigación nueva en Auromancia, hija de Anomalías del mundo. Pide 1 de observación y 1 de teoría de Auromancia, y da 1 de warp.
   - Cuando se llena el medidor y alguien en la dimensión tiene Foresight, le aparecen **3 cartas** con nombre, tipo, descripción y duración.
   - Clic en una: empieza esa y se avisa a todos en el chat.
   - Tiene 60 segundos. Si no elige, sale una de las 3 al azar. Mientras elige, el reloj se pausa y el HUD muestra la cuenta.
   - Si cerrás la pantalla, la volvés a abrir agachado + clic derecho con el taumómetro (apuntando al aire).
   - Los demás jugadores ven "Un vidente está eligiendo".

## Decisiones
- Las entradas van en **Auromancia** y no en Eldritch, porque Eldritch se abre muy tarde y las anomalías salen desde el principio.
- Las cartas son siempre 3 (constante en `AnomalyManager.CHOICES`); no agregué opción en la config para no tocar `Config`.

## Para probar
1. `/thaumcraft research <jugador> FORESIGHT` (o completarla en el libro).
2. `/thaumcraft anomaly timer 1` y esperar 1 minuto.

## Objetos Java tocados
| Clase | Cambio |
|---|---|
| `common/anomaly/PacketAnomalyChoice` | **Nueva.** Servidor → vidente: las cartas |
| `common/anomaly/PacketAnomalyChoose` | **Nueva.** Vidente → servidor: la carta elegida |
| `common/anomaly/ScanAnomaly` | **Nueva.** Escanear el cielo durante una anomalía |
| `client/gui/GuiAnomalyChoice` | **Nueva.** Pantalla de las 3 cartas |
| `client/gui/AnomalyClientEvents` | **Nueva.** Abre las cartas; agachado + taumómetro las reabre |
| `common/anomaly/AnomalyManager` | Próximas anomalías, elección con Foresight, sorteo excluyendo ids |
| `common/anomaly/PacketAnomalySync` | Codec a mano; suma tipo de la próxima y cuenta de elección |
| `common/anomaly/AnomalyClientState` | Próxima, cuenta de elección y cartas |
| `client/gui/AnomalyHud` | Línea "Próxima" con taumómetro y línea de elección; además se saca el cerebro (el cambio anterior no había llegado a tu carpeta) |
| `common/network/TCNetwork` | Registra los 2 paquetes |
| `common/config/ConfigResearch` | Carga `research/anomalies.json` y registra `ScanAnomaly` |

Más:
- `research/anomalies.json` (nuevo): Anomalías del mundo, Foresight y 16 hexágonos `!anomaly_<id>`.
- `en_us.json`: 16 textos nuevos.
