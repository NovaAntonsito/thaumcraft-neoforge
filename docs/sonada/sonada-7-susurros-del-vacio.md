# Soñada 7: Susurros del vacío

El evento con historia del diseño (`investigacion/susurros-del-vacio.md`). Los laberintos son los del Outer Lands de TC4, chicos y en una dimensión propia.

## Cómo arranca

- **Requisitos:** saber la etapa 2 de BASEAUROMANCY (la que da el Manipulador Focal) y haber hecho un caster o una varita.
  - El arma se marca al craftearla (`!void_weapon`).
  - Si ya tenías una de antes, cuenta si está en el inventario cuando te despertás.
- **Al despertarte** (dormir toda la noche):
  - sale el mensaje de susurros;
  - se abre el capítulo "Whispers of the Void" en el Thaumonomicon (investigación VOIDWHISPERS);
  - se abre la primera grieta.
- **El primer encuentro es seguro:** arranca en el acto. Si el sistema de anomalías está activo, va como anomalía y tiene el marcador del HUD; si no, la grieta sale igual y el chat te da las coordenadas.

## Encuentros 2 a 5

- **Anomalía nueva "Call of the Void"** (`void_calling`, mixta, local, peso 6, 20–30 min).
  - Solo se puede sortear en el overworld, y solo si hay un jugador que tenga el evento, no lo haya terminado y no tenga una grieta abierta.
  - Para eso agregué `AnomalyType.canStartIn(level)`, y el sorteo y la lista de próximas anomalías lo respetan.
- **Si nadie entra antes de que termine,** la grieta se cierra y el jugador puede volver a ser llamado.
- **Si alguien entró,** la grieta queda hasta que su dueño lee la página.
- **Grieta perdida:** si pasan 3 días de juego con la grieta abierta, se considera perdida y el vacío puede volver a llamar.
- La anomalía tiene su entrada `!anomaly_void_calling`, como las demás.

## La grieta

- **`void_rift`:** es una `EntityFluxRift` estable que usa el mismo render.
  - No come bloques, no crece, no come flux y no colapsa; el sifón del vacío no la puede drenar.
  - Larga partículas violetas y susurra.
- Sale a 150–300 bloques del jugador, sobre el suelo.
- **Entrar:** la tocás y vas al laberinto de su encuentro, al lado del portal del centro. Suma **warp del vacío**.
- **Cierre:** se cierra sola cuando su dueño lee la página de ese encuentro.

## La dimensión y los laberintos

- **Dimensión:** `thaumcraft:void`. El diseño decía `vacio`, pero lo dejé en inglés por la directiva de no españolizar.
  - Es plana, de aire, con el cielo y la niebla del Outer Lands y un bioma `void` (copia de eldritch con niebla violeta).
- **Laberintos:** usan el generador de laberintos de TC4 (portal, sala de la llave, jefe, nido, biblioteca).
  - Cada encuentro arma el suyo en el mapa de laberintos compartido, lejos de los del Outer Lands (chunk x ≥ 100000).
  - La z del laberinto dice de qué encuentro es.
  - Cada dimensión solo construye sus propias celdas.
- **Tamaño por encuentro:** 9, 11, 11, 13 y 15 celdas.
- **Sala de la llave, además de lo de TC4:**
  - la página del diario del encuentro (ítem permanente);
  - un cofre con loot `chests/void_encounter_N`;
  - un guardián eldritch más por cada encuentro, y desde el 3 algunos son campeones.
- **Volver:** el portal del centro te devuelve a donde estaba la grieta.
- **Reglas del Outer Lands:** no se vuela, hay más campeones y no se construye cerca del jefe. Para eso agregué `OuterLands.isMazeWorld`.

## Loot

`loot_table/chests/void_encounter_1..5`, solo con cosas de TC, y mejora con cada encuentro:
- cristales de vis, ámbar, quicksilver, cinabrio, sal mundus y pepitas de vacío;
- fragmentos de conocimiento, semillas del vacío, lingotes de vacío, curios y bolsas de loot;
- perla primordial a veces desde el 3, y segura en el 5.

## Páginas del diario y capítulo

- **Ítem `void_journal_page`** (páginas I a V). Al leerla:
  - se completa VOIDPAGEn, y con la V también VOIDEXPLORER;
  - se cierra la grieta.
- **Capítulo VOID:** VOIDWHISPERS, VOIDPAGE1–5 y VOIDEXPLORER.
- **Textos:** el de VOIDWHISPERS lo escribí yo y explica cómo funciona.
  - **Las 5 páginas y el texto final quedan con la clave vacía** (`research.VOIDPAGEn.stage.1`, `research.VOIDEXPLORER.stage.1`) para que vos escribas el lore.

## Warp del vacío

- Es un cuarto tipo en `PlayerWarp` (`VOID`): se guarda, se sincroniza y se ve en `/thaumcraft warp`.
- **Al entrar:** sube `voidWarpGain` (25).
- **Umbral:** hasta `voidWarpSafe` (25) no hace nada. Lo que pase de ahí cuenta para los eventos de warp, así que entrar dos veces seguidas trae efectos.
- **Baja sola:** 1 punto cada `voidWarpDecayMinutes` (2). Nada lo limpia.
- **Config nueva** en `[void]`: `voidWhispers` (apaga todo), `voidWarpGain`, `voidWarpSafe` y `voidWarpDecayMinutes`.

## Arreglo de la 6

Los mapas de las notas cartográficas y los tiempos del canje se guardaban en datos persistentes, que se pierden al morir. Ahora van en un attachment nuevo, `dream`, que se conserva al morir. La 7 guarda ahí la grieta abierta y el camino de vuelta.

## Lo que falta del diseño

- Mostrar el warp del vacío en el Thaumonomicon o en el medidor de cordura.
- Mobs del vacío propios: por ahora los laberintos tienen guardianes eldritch, cangrejos y el jefe opcional de TC4.
- Mejoras de foco que se aprenden en el vacío, y bloques con mucho vacuos y alienis.
- El "explorador" del final como algo más que la última entrada.

Compila. No está probado en el juego. Lo más incierto: el tamaño mínimo del laberinto (9) con el generador de TC4, y caer bien al lado del portal.
