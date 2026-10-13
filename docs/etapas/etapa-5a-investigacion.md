# Etapa 5a: investigación

El Thaumonomicon y el sistema de investigación del original. Ahora las recetas arcanas, del crisol y de infusión solo funcionan si conocés su investigación.

## Qué se agregó
- **Datos de investigación**: las 7 categorías (Basics, Auromancy, Alchemy, Artifice, Infusion, Golemancy, Eldritch) se leen de los JSON originales, en `assets/thaumcraft/research/`.
- **Thaumonomicon** (`thaumonomicon`):
  - Clic derecho abre el mapa de investigación.
  - Arrastrás el mapa con el mouse y hacés zoom con la rueda.
  - A la izquierda están las pestañas de categoría. Abajo a la izquierda está la búsqueda.
  - Un clic en una investigación abre sus páginas. Ahí ves los requisitos de la etapa y el botón para completarla.
  - Los marcadores de la derecha muestran las recetas. Si un ingrediente tiene receta conocida, un clic en él la abre. Retroceso vuelve atrás.
- **Thaumonomicon tramposo** (`thaumonomicon_cheat`): da todas las investigaciones.
  - Solo aparece en creativo si activás `allowCheatSheet` en la config.
- **Aviso al completar**: un cartel "Research complete" en la esquina.
- **Cadena de inicio** del original:
  1. Al levantar un cristal de vis aparece un mensaje y se registra `!gotcrystals`.
  2. Al despertarte después de eso recibís el **diario de sueños**.
     - Con `noSleep` activado lo recibís al levantar el cristal.
  3. Con **salis mundus** sobre una biblioteca obtenés el Thaumonomicon.
  4. Al levantarlo se desbloquea **First Steps**.
- **Recetas con investigación**:
  - Salis mundus sobre una mesa de crafteo necesita First Steps (etapa 1).
  - Salis mundus sobre un caldero necesita Unlock Alchemy (etapa 1).
  - Salis mundus sobre el altar necesita Infusion.
  - La mesa arcana usa la receta normal de crafteo si no conocés la arcana.
  - El crisol y la infusión no hacen nada sin la investigación.
- **Requisitos de etapa**:
  - "Obtener": tener los ítems en el inventario. Se consumen al completar.
  - "Craftear": haberlo crafteado alguna vez (mesa, mesa arcana, crisol o salis mundus).
  - "Investigación previa": conocer otra investigación.
  - "Conocimiento": puntos de teoría u observación de la categoría. Se consumen al completar.
- **Distorsión**: ganar distorsión (warp) por primera vez desbloquea la investigación de Warp, como en el original.

## Comandos
Todos empiezan con `/thaumcraft` y necesitan permisos de operador.

| Comando | Qué hace |
|---|---|
| `research list` | Lista todas las claves de investigación. |
| `research <jugador> list` | Lo que conoce el jugador. |
| `research <jugador> all` | Le da todas las investigaciones. |
| `research <jugador> reset` | Borra todo lo que sabe. |
| `research <jugador> <clave>` | Le da una investigación y las previas que necesita. |
| `research <jugador> revoke <clave>` | Le quita una investigación y las que dependen de ella. |
| `knowledge <jugador> <theory\|observation> <categoría> <puntos>` | Suma conocimiento. Ejemplo: `knowledge @s observation BASICS 2`. |
| `reload` | Vuelve a leer los JSON de investigación. |

El comando `knowledge` no existe en el original. Lo agregué para probar, porque todavía no hay forma de ganar conocimiento: eso llega en 5b (mesa de investigación) y 5c (taumómetro).

## Cómo probarlo
1. Creá un mundo nuevo, o usá `research @s reset`.
2. Conseguí un cristal de vis y levantalo. Dormí en una cama y despertate: aparece el diario de sueños.
3. Crafteá salis mundus y usalo sobre una biblioteca: aparece el Thaumonomicon. Levantalo.
4. Abrilo y entrá en First Steps.
   - Pide craftear la mesa arcana: salis mundus sobre una mesa de crafteo.
   - Volvé al libro y tocá "complete".
5. La etapa 2 pide craftear el taumómetro y 1 punto de observación de Basics.
   - Usá `knowledge @s observation BASICS 1` para el punto, crafteá el taumómetro y completá.
6. Para seguir probando lo de etapas anteriores (crisol, infusión) sin hacer toda la cadena, usá `research @s all`.

## Pendiente
- **Ganar conocimiento** (5b y 5c).
  - La mesa de investigación y sus notas hexagonales llegan en 5b.
  - El escaneo con el taumómetro llega en 5c.
  - Mientras tanto, el comando `knowledge`.
- **Ítems sin portar**: muchas investigaciones nombran ítems y recetas que todavía no existen (golems, focos, armaduras, etc.).
  - En el mapa se dibujan como un signo de pregunta.
  - Sus requisitos se saltan, como hace el original cuando un ítem no existe.
  - Sus recetas no aparecen en los marcadores.
- **Íconos de foco** en el mapa: esperan al sistema de focos.
- **Planos**: solo está el altar de infusión normal. El antiguo, el eldritch y el taumatorio esperan sus bloques.
- **Aviso de conocimiento ganado** (PacketKnowledgeGain): llega con 5b.

## Diferencias con el original
- El original quería mostrar "receta desconocida" al pasar el mouse por un ingrediente cuya receta no conocés. Un error lo impedía; acá sí se muestra.
- Las recetas que el original guardaba por nombre (`thaumcraft:thaumometer`) ahora están en carpetas por tipo (`arcane/thaumometer`). El libro las busca por nombre igual.
