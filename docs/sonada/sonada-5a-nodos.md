# Soñada 5a: nodos de aura en el mundo

Vuelven los nodos de Thaumcraft 4. Esta parte trae el nodo suelto en el mundo. El frasco, el estabilizador, el transductor y la anomalía de convergencia quedan para la 5b, y el atlas para la 5c.

## Qué hay

- **Bloque `aura_node`** (sin ítem). Es invisible, no tiene colisión y no se rompe. Solo se puede apuntar si tenés una varita o un taumómetro en la mano, o si llevás algo que revele nodos (las gafas). Así no molesta al construir.
- **TileAuraNode**, portado de `TileNode` de TC4:
  - Aspectos actuales y base, tipo (normal, inestable, siniestro, corrupto, hambriento, puro) y brillo (brillante, pálido, desvaneciente).
  - Se regenera un punto cada 600 ticks: 400 si es brillante, 900 si es pálido, nunca si se desvanece. Al volver a cargar el chunk recupera lo que regeneró mientras estuvo descargado.
  - Cada 1200 ticks, un aspecto vacío pierde base y puede desaparecer, y el nodo se degrada. Sin aspectos, el nodo muere.
  - Un nodo grande le chupa aspectos a uno más chico que esté a 5 bloques o menos, con el rayito de TC4.
- **Efectos por tipo:**
  - **Hambriento:** atrae criaturas, lastima a menos de 2 bloques y rompe bloques blandos (dureza menor a 5) cada 50 ticks. Con lo que mata gana aspectos.
  - **Siniestro:** en el overworld esparce el bioma eerie y llama zombis cerebrales gigantes (hasta 3).
  - **Corrupto:** esparce fibras de taint. Un nodo se vuelve corrupto si su chunk tiene más flux que la mitad de su base de aura.
  - **Inestable:** suelta puntos primales. TC4 los soltaba como orbes de aspecto, que no existen en 1.21, así que acá van al aura como vis.
  - **Puro:** limpia flux del chunk (ver abajo).
  - Los efectos agresivos se apagan con `hardNode=false`.
- **Aura (no está en TC4):** cada 5 segundos el nodo le da al chunk un centésimo de sus aspectos como vis, hasta la base del aura más la mitad de sus aspectos. Además, según el tipo:
  - El hambriento come vis en vez de dar.
  - El corrupto suma flux.
  - El puro saca flux.
- **Render:** es el `TileNodeRenderer` de TC4 con `nodes.png` (copiado del TC4). Cada aspecto dibuja un anillo de su color que late con su cantidad, alrededor de un núcleo según el tipo.
  - Con gafas se ve a través de las paredes, hasta 64 bloques.
  - Con el taumómetro en la mano se ve hasta 48 bloques.
  - Sin nada, se ve un brillito tenue.
- **Gafas:** muestran los aspectos del nodo y su nombre ("Bright Sinister Node"), con los textos de TC4.
- **Varita:** mantené clic derecho apuntando al nodo. Cada 5 ticks pasa de 1 a 3 puntos de un aspecto al azar a la carga de la varita, a razón de 1 punto por 1 vis.
  - NODETAPPER1 y NODETAPPER2 suman un punto cada una.
  - NODEPRESERVE deja siempre el último punto de cada aspecto, salvo que estés agachado. No anda con varitas de madera y hierro.
- **Taumómetro:** escanear un nodo enseña un quinto de sus aspectos. Cada tipo de nodo se aprende una vez (`!node_<tipo>`), y el primero completa NODES.
- **Investigación en Auromancia:** NODES (oculta y redonda, se completa al escanear), NODETAPPER1, NODETAPPER2 y NODEPRESERVE. Los textos son los de TC4.

## Dónde aparecen

- **Nodos salvajes:** un chunk de cada 36 (`nodeRarity`). El nodo aparece en el primer aire desde 5 bloques sobre el fondo del mundo, así que muchos quedan en cuevas, como en TC4.
- **Barrow:** un nodo siniestro flota sobre cada uno.
- **Anillo de altar eldritch:** un nodo siniestro arriba del obelisco.
- **Probabilidad de tipo especial:** 1 de cada 18 (`specialNodeRarity`). Un brillo especial sale el doble de seguido.
- **Aspectos:**
  - Salen del aspecto del bioma más algunos primales al azar.
  - Suman agua, fuego, tierra o planta si hay mucha agua, lava, piedra o follaje alrededor.
  - El total escala con el nivel de aura del bioma (de 0 a 1 en TC6) × 400.
- **Config nueva** en `[world]`: `generateNodes`, `nodeRarity`, `specialNodeRarity` y `hardNode`.

## Qué cambia respecto de TC4

- **No hay bioma de taint:** se pierden la conversión a taint y el refuerzo de aura en ese bioma.
- **El nodo puro no convierte bioma.** TC4 solo lo hacía dentro del tronco de silverwood, que acá no tiene nodo.
- **Sin nodos en silverwood ni en tótems/menhires,** porque esas estructuras no están.
- **Hambriento:** fames y lucrum no existen en TC6, así que usa desiderium y vacuos.
- **Estabilizador:** todavía no hay (`checkLock` siempre da 0). Llega en la 5b.
- **La línea de drenaje de TC4** acá son chispas de vis del nodo al jugador.

## Arreglo de la etapa 4

El árbol de varitas pisaba ANOMALIES, FORESIGHT y las anomalías en Auromancia. Lo moví abajo a la izquierda: WANDS queda en [-4,6], con las tapas y los núcleos alrededor.

## Archivos

- `api/nodes/` (`NodeType`, `NodeModifier`, `INode`)
- `common/blocks/world/BlockAuraNode`, `common/tiles/misc/TileAuraNode`
- `client/renderers/tile/TileAuraNodeRenderer`, `TCRenderTypes.node`
- `common/lib/research/ScanAuraNode`, `Utils.setBiomeAt`
- `ThaumcraftWorldGenerator.createRandomNodeAt`/`createNodeAt`/`NodesFeature`, más el feature `nodes` y su biome modifier
- `ItemWand`: `onItemUseFirst`, `onUseTick`, `getRoom` y `addCharge`

Compila. No está probado en el juego.
