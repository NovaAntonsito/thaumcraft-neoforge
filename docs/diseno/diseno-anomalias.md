# Diseño: anomalías del mundo (modificadores rogue-lite)

Estado: **diseño, sin código todavía.** Se puede apagar entero desde la config.

Decidido: salen **al azar**; con la investigación **Presagio** se elige 1 de 3.

## La idea en una línea
Cada cierto tiempo el mundo "tiene una idea": sale una **anomalía** (positiva, negativa o mixta) que dura un rato y cambia las reglas del aura, el warp o la investigación. Arriba en la pantalla hay un medidor que se llena hasta la próxima.

## De dónde sale: las cartas de TC6
En TC6 el theorycraft repartía **cartas de ideas** al azar (Analyze, Inspired, Rethink, Curio…), cada una con su costo y su premio, y podías elegir cuál jugar. En la etapa 5b ese theorycraft se cambió por el minijuego de TC4, así que esas cartas quedaron sin uso. Las reutilizamos:

| Carta de TC6 | Anomalía |
|---|---|
| Se reparten 3 cartas al azar, con peso | Se sortean 3 anomalías con peso |
| Elegís 1 | Si tenés la investigación **Presagio**, elegís 1 de 3. Si no, sale una al azar |
| Las cartas de "aids" (bloques cerca) suman opciones | El estado del mundo cambia los pesos (ver abajo) |
| Cartas con costo (pierdo X, gano Y) | Anomalías mixtas |

## Cómo funciona
1. **Reloj**: por dimensión, guardado en `SavedData`. Intervalo por defecto **40 min de juego**, con ±20 % de variación para que no sea predecible.
2. **Sorteo**: al llenarse el medidor se sortean 3 anomalías. Los pesos dependen del mundo:
   - **Mucho flux** en los chunks cargados: más negativas (grietas, marea de flux).
   - **Mucho vis**: más positivas.
   - **Jugadores con warp alto**: más anomalías eldritch.
   - **Luna llena / luna nueva**: igual que el aura de la etapa 2, sube el vis o el flux.
   - **Ciclo** (cuántas anomalías van): cada 5 ciclos sube un nivel y se habilitan las grandes. Desde el nivel 3 pueden salir **dos a la vez**. Esa es la escalada rogue-lite.
3. **Duración**: cada anomalía dura entre 5 y 20 min. Mientras dura, el medidor se pausa.
4. **Alcance**: global en la dimensión, o **local** (un punto del mundo cerca de un jugador, marcado en el HUD con distancia y dirección).
5. **Wuss mode**: solo positivas.

## Medidor arriba (HUD)
```
        ┌───────────────────────────────┐
        │ [?] ▓▓▓▓▓▓▓▓▓▓░░░░░░  12:40    │   ← próxima anomalía
        │ [ícono] Tormenta de vis  04:10 │   ← activa, con tiempo restante
        └───────────────────────────────┘
```
- Arriba al centro, debajo de la barra de jefes. Ícono `?` hasta que sale.
- La barra cambia de color en los últimos 60 s y suena un aviso.
- Si es local: flecha y distancia al centro de la anomalía.
- Con el **taumómetro** en la mano se ve el nombre de la que *viene* (solo la categoría: aura, vacío, eldritch). Premio por investigar.
- Config: mostrar u ocultar, posición (arriba al centro / arriba a la izquierda).

## Cómo encaja con lo ya portado
| Sistema | Cómo se usa |
|---|---|
| **Aura** (etapa 2, `AuraThread`, `AuraHandler`) | Multiplicadores de regeneración de vis y de disipación de flux |
| **Grietas de flujo** (6b, `EntityFluxRift`) | La grieta gigante es una `EntityFluxRift` con tamaño grande y estable por la duración |
| **Warp** (`PlayerWarp`, `PotionWarpWard` de 6d) | Anomalías eldritch dan warp temporal; Warp Ward protege |
| **Investigación** (5a–5c) | Investigación nueva en Eldritch para cada anomalía vista (`!anomaly_<id>`), escanearlas da puntos de aspecto, y **Presagio** permite elegir |
| **Mesa arcana, infusión, focos** (4b, 4d, 7a) | Multiplicadores de costo de vis e inestabilidad |
| **Notificaciones** (`NotificationsHud`, `ResearchToast`) | Aviso al empezar y terminar |

Para no meter `if` por todo el código, una sola clase **`AnomalyModifiers.get(level, pos, Stat)`** devuelve 1.0 si no hay nada activo. Los sistemas existentes solo multiplican por eso.

## Lista inicial (16)
| # | Anomalía | Tipo | Qué hace |
|---|---|---|---|
| 1 | **Tormenta de vis** | Mixta | Caen rayos de vis cerca de los jugadores: +vis en el chunk, sueltan cristales, pero 1 de cada 4 libera flux. Regen de vis ×3 |
| 2 | **Grieta del vacío gigante** | Negativa (local) | Una grieta de flujo enorme y estable. Come bloques y crece. Si la cerrás antes de que termine (salis mundus / foco), da semillas del vacío y perla primordial |
| 3 | **Marea de flux** | Negativa | +flux en cada chunk cargado cada minuto. Más fácil que salgan grietas |
| 4 | **Sequía de aura** | Negativa | Regen de vis ×0,5. Recetas arcanas y focos cuestan +50 % |
| 5 | **Eclipse eldritch** | Negativa | La noche dura el doble. Eventos de warp ×2. +1 warp temporal por minuto afuera |
| 6 | **Lluvia de vitium** | Negativa | Caen cristales de vitium; romperlos suelta flux |
| 7 | **Murmullos** | Negativa | Sonidos y partículas falsas, y a veces se te borra 1 punto de aspecto. Warp Ward la anula |
| 8 | **Aurora arcana** | Positiva | Regen de vis ×2. Recetas arcanas −25 % de vis |
| 9 | **Lluvia de cristales** | Positiva | Caen cristales de vis de los 6 primales cerca de los jugadores |
| 10 | **Inspiración** | Positiva | Escanear da ×2 puntos. Las notas de investigación no gastan tinta |
| 11 | **Calma del vacío** | Positiva | El flux se disipa ×3. No salen grietas |
| 12 | **Convergencia primal** | Positiva | Se elige un primal: sus recetas de crisol cuestan −30 % y aparecen sus cristales |
| 13 | **Luna de infusión** | Positiva | Inestabilidad de infusión −50 % |
| 14 | **Vena de ámbar** | Positiva (local) | Un punto con menas de ámbar y cinabrio expuestas y aura alta |
| 15 | **Foco desbocado** | Mixta | Los focos cuestan la mitad pero se suma un efecto al azar al hechizo |
| 16 | **Inversión** | Mixta | En los chunks cercanos el vis y el flux se intercambian al empezar y al terminar |

Quedan para más adelante las que necesitan cosas no portadas: **plaga de taint**, **invasión de guardianes eldritch**, **portal al Outer Lands**.

## Config (`thaumcraft-common.toml`, sección `anomalies`)
| Opción | Por defecto |
|---|---|
| `enabled` | `true` |
| `intervalMinutes` | `40` |
| `intervalJitter` | `0.2` |
| `choices` (cartas a elegir con Presagio) | `3` |
| `maxActive` | `2` |
| `negativeWeight` / `positiveWeight` | `1.0` / `1.0` |
| `blacklist` (ids que nunca salen) | vacío |
| `dimensions` (dónde corre) | `minecraft:overworld` |
| `showHud` / `hudPosition` | `true` / `top_center` |

Y comandos: `/thaumcraft anomaly start <id>`, `stop`, `skip`, `list`, `timer <minutos>`.

## Multijugador
El reloj es del mundo, no de cada jugador. Si alguien tiene **Presagio**, a ese le aparecen las 3 cartas. Si en 60 s no elige, sale una al azar. Todos ven el mismo medidor.

## Plan de implementación
| Etapa | Qué entra | Clases nuevas | Clases que se tocan |
|---|---|---|---|
| **Fase 1 – núcleo** | Reloj, registro, sorteo con pesos, config, comandos, sync, HUD. Aurora arcana y Sequía de aura para probar | `api/anomaly/Anomaly`, `AnomalyType`, `AnomalyContext`, `common/anomaly/AnomalyManager` (SavedData), `AnomalyRegistry`, `AnomalyModifiers`, `AnomalyEvents`, `common/lib/network/PacketAnomalySync`, `client/gui/AnomalyHud` | `Config`, `Thaumcraft`, `ThaumcraftClient`, `TCNetwork`, `ThaumcraftCommand`, `AuraThread` |
| **Fase 2 – las grandes** | Tormenta de vis y grieta gigante | `EntityVisBolt`, `RenderVisBolt`, `anomalies/VisStorm`, `anomalies/GiantRift` | `EntityFluxRift`, `RenderFluxRift`, `AuraHandler` |
| **Fase 3 – el resto** | Las otras 12 de la lista y sus multiplicadores | una clase por anomalía en `common/anomaly/anomalies/` | `TileArcaneWorkbench`/recetas arcanas, `TileInfusionMatrix`, `CasterManager`, `ScanningManager`, `ResearchNoteManager`, `PlayerEvents` (warp), `TileCrucible` |
| **Fase 4 – cartas e investigación** | Pantalla de elegir 1 de 3, Presagio, entradas en el Thaumonomicon, escaneo | `client/gui/GuiAnomalyChoice`, `PacketAnomalyChoice`, `ScanAnomaly` | `ConfigResearch`, JSON de investigación de Eldritch, `en_us.json` y demás idiomas |

Cada etapa se puede probar sola con `/thaumcraft anomaly start <id>` y `/thaumcraft anomaly timer 1`.

## Registro de objetos Java tocados en este pedido
Ninguno. Esto es solo diseño; no se creó ni se modificó ningún `.java`. Las clases de arriba son las que tocaría la implementación.
