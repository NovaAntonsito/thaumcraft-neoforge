# Etapa 5b: notas de investigación (minijuego de TC4)

Reemplaza el theorycraft de TC6 por el minijuego de hexágonos de Thaumcraft 4, como hace TC4 Research Port: Reborn (licencia MIT). El Thaumonomicon y sus etapas siguen siendo los de TC6.

## Cómo cambia la investigación
- Cada etapa que pedía conocimiento de **teoría** ahora pide una **nota de investigación** (`rn_<CLAVE>_<etapa>`). Cuantas más teorías pedía, más difícil es la nota.
- El conocimiento de **observación** se quitó.
- Una etapa que quedó sin requisitos muestra igual el botón para completarla.
- Se quitan **Knowledge Types** y **Theory Research**. **Celestial Observation** pasa a depender de Research y queda oculta.
- Se agregan las investigaciones de Reborn en Basics:
  - **Research**: craftear las herramientas de escritura y la mesa de madera.
  - **Research Expertise**: al borrar un aspecto, 25% de recuperar el punto.
  - **Research Mastery**: 50% de recuperar el punto, 10% de escribir gratis, y shift+clic combina directo.
  - **Research Duplication**: copiar una nota terminada.
  - **Deconstruction** queda para la 5c, con la mesa de deconstrucción.

## Lo nuevo
- **Herramientas de escritura** (`scribing_tools`): 100 usos de tinta. Se recargan con tinte negro. Vacías no se rompen.
- **Mesa de madera** y **mesa de piedra** (`table_wood`, `table_stone`).
- **Mesa de investigación** (`research_table`):
  - Clic derecho sobre la mesa de madera con las herramientas de escritura. También sirve salis mundus sobre la mesa de madera, si conocés Research.
  - Ranura 1: herramientas de escritura. Ranura 2: la nota.
  - Encima se ven el tintero, la pluma y el pergamino con la cinta del color de la nota.
  - Cada 30 segundos el entorno puede regalar un punto de un aspecto primario (oscuridad, altura, fuego, tierra, agua, redstone, bibliotecas). Se ve con un destello sobre el aspecto.
- **Puntos de aspecto**: cada jugador tiene una reserva de puntos por aspecto. Arranca conociendo los seis primarios con 0 puntos.
  - Los puntos se ganan escaneando (llega en la 5c) o con los comandos.
  - Al ganar puntos aparece un aviso abajo a la derecha y los íconos vuelan al Thaumonomicon.
- **Notas en el Thaumonomicon**: en la fila de conocimiento aparece la nota. Un clic te la da y gasta 1 de tinta y 1 papel (gratis en creativo).
- **El minijuego**:
  - A la izquierda están tus aspectos, cinco por columna, con flechas para paginar.
  - Arrastrá un aspecto a un hexágono vacío para escribirlo. Gasta 1 de tinta y 1 punto (o el punto regalado de la mesa).
  - Un clic sobre un aspecto escrito lo borra (también gasta tinta).
  - Dos aspectos están unidos si uno es componente directo del otro. Dos aspectos iguales no se unen. La nota se resuelve cuando todos los aspectos del borde quedan unidos por una sola cadena de líneas.
  - Al pasar el cursor sobre un aspecto (en la lista, en los círculos de abajo o en la nota) se ven su nombre y sus dos componentes.
  - Para combinar, arrastrá dos aspectos a los círculos de abajo y apretá el botón. Gastás 1 de cada uno y ganás 1 del resultado, o 3 si es nuevo.
  - Resuelta, la nota pasa a ser un **Descubrimiento**. Clic derecho con él completa ese requisito.
  - Con Research Duplication aparece una estrella para copiarla. Cuesta papel, tinta y (copias+1) puntos de cada aspecto.

## Comandos
| Comando | Qué hace |
|---|---|
| `/thaumcraft aspects <jugador> research <aspecto\|all>` | Le enseña el aspecto (sin puntos). |
| `/thaumcraft aspects <jugador> add <aspecto\|all> <cantidad>` | Suma puntos, con el aviso en pantalla. |
| `/thaumcraft aspects <jugador> set <aspecto\|all> <cantidad>` | Fija los puntos. |

Se quitó `/thaumcraft knowledge`, que era solo para probar la 5a.

## Config (`thaumcraft-common.toml`, sección `research`)
- `researchDifficultyMultiplier` (0.5): agranda o achica las notas al azar.
- `aspectObtainMultiplier` (0.5): puntos por escaneo (se usa en la 5c).
- `notificationDelay` (2000 ms) y `notificationMax` (10): los avisos de puntos.
- `aspectTotalCap` (10000): pasado este total de un aspecto, se gana menos.
- `aspectBlackList`: aspectos que nunca salen en notas al azar.

## Diferencias con el original
- Las notas con patrón (83 en `research/patterns/`) siempre salen iguales. Las demás se generan al azar con la semilla del mundo.
- Reborn chequeaba las claves de TC4 `RESEARCHER1` y `RESEARCHER2` al escribir y borrar. Acá se usan sus investigaciones reales, Research Expertise y Research Mastery.
- El servidor no deja escribir sin puntos ni sobre los aspectos del borde. Reborn lo permitía con paquetes armados a mano.
- El original mostraba los componentes del aspecto solo con Research Expertise, y solo en la lista. Acá se ven siempre, también sobre la nota.
- Las motas que flotan hacia la mesa usan las partículas de encantamiento de vanilla.
- La pluma se dibuja plana, sin el grosor del ítem 2D de 1.12.
- Sin los cristales de vis ni el cerebro en frasco, la mesa todavía no los usa para regalar puntos.

## Cómo probarlo
1. `/thaumcraft research <jugador> RESEARCH` y `/thaumcraft aspects <jugador> add all 20`.
2. Crafteá una mesa de madera y las herramientas de escritura. Usá las herramientas sobre la mesa.
3. Con papel y herramientas en el inventario, abrí Research Expertise en el Thaumonomicon y hacé clic en la nota.
4. Poné la nota en la mesa y resolvela. Clic derecho con el Descubrimiento y completá la etapa en el libro.
