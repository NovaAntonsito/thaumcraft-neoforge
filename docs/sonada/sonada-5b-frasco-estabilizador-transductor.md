# Soñada 5b: frasco de nodo, estabilizadores, transductor y convergencia

Segunda parte de los nodos de TC4. Ya se puede mover un nodo de un lado a otro, calmarlo y convertirlo en una fuente de vis.

## Frasco de nodo (NODEJAR)

- **Armado:** un cubo de 3×3×3 de vidrio común con el nodo en el centro y losas de madera arriba (las 9).
- **Captura:** golpeás cualquier vidrio o losa con una varita que tenga 70 vis. TC4 pedía 70 de cada primal; acá hay un solo vis. El cubo desaparece y queda el frasco (`jar_node`, luz 11).
- **Daño al nodo:** 3 de cada 4 veces pierde un escalón de brillo (brillante pasa a normal, normal a pálido, pálido a desvaneciente). La base pasa a ser lo que tenía en ese momento, igual que en TC4.
- **Congelado:** dentro del frasco el nodo no regenera, no drena y no hace nada.
- **Transporte:** al romperlo te queda el ítem con el nodo adentro (componente `node_data`). El tooltip muestra el tipo, el brillo y los aspectos.
- **Liberar:** lo ponés en el mundo y le hacés clic derecho con una varita. El frasco se rompe y el nodo vuelve.
- **Render:** el nodo se dibuja adentro del frasco al 70 % del tamaño. El ítem se ve como un frasco con brillo; TC4 dibujaba el nodo también en el ítem.
- **Creativo:** el menú trae un frasco con un nodo normal de los 6 primales (20 de cada uno), para probar.

## Estabilizadores (NODESTABILIZER, NODESTABILIZERADV)

- Van justo debajo del nodo y se apagan con una señal de redstone.
- **Estabilizador:** el nodo regenera a la mitad de velocidad, no drena a otros ni lo drenan, el inestable deja de perder puntos y a la larga se puede volver normal, y el desvaneciente se puede volver pálido.
- **Avanzado:** regenera 20 veces más lento, pero el nodo sí puede drenar a otros más débiles. Las chances de mejorar son más altas.
- **Modelo:** el obj de TC4. La base es un modelo de bloque; los 4 pistones salen, brillan y se ponen rojos en el avanzado. Arriba se ve la burbuja `node_bubble`.
- **Recetas:**
  - Estabilizador (arcana): oro, cuarzo, pistón, ladrillos de piedra arcana y nitor. Cuesta vis 50 más aqua, terra y ordo.
  - Avanzado (infusión): sobre el estabilizador, con nitor, alumentum y bloques de redstone.

## Transductor y nodo energizado (VISPOWER)

- **Conversión:** va justo encima de un nodo estabilizado y se activa con redstone.
  - Durante 1000 ticks drena el nodo con rayitos.
  - Al terminar, el nodo queda energizado. Sus aspectos pasan a vis según `setupNode` de TC4: la raíz cuadrada de cada primal, ajustada por el brillo.
- **Nodo energizado:** se ve siempre, con el anillo de rayos de TC4. No se puede drenar con la varita.
- **El cambio respecto de TC4:** TC4 repartía la vis en centivis por relés. Acá no hay relés: cada segundo el nodo vuelca su vis al aura del chunk, hasta el doble de la base. Es una fuente de vis para la zona.
- **Explosión:** si sacás el estabilizador, lo apagás o sacás el transductor, el nodo explota. Deja una explosión de 3, flux goo abajo y 25 de flux en el aura; TC6 no tiene gas de flux.
- **Revertir:** cortás la redstone y el transductor cuenta para atrás. El nodo vuelve a ser normal, con sus aspectos base pero todos vacíos.
- **Color de la base:** verde en reposo, naranja convirtiendo, rojo energizado.
- **Receta (arcana):** el estabilizador rodeado de bloques de redstone, comparador, hierro y nitor. Cuesta vis 50 más ignis, aer y perditio.

## Convergencia de nodos (anomalía nueva)

- Es positiva y local, y aparece a 24–48 bloques de un jugador. La marca una columna de luz.
- **Mientras dura:** hay un nodo brillante con el doble de aspectos y hasta 3 nodos chicos alrededor, y todos regeneran un punto por segundo.
- **Al terminar:** los chicos caen en el grande con un rayito, y el grande se queda con sus aspectos base.
- **Escaneo:** se escanea como cualquier anomalía y desbloquea su entrada en Auromancia.

## Investigación en Auromancia

| Entrada | Posición | Viene de |
|---|---|---|
| NODEJAR | [-12,4] | NODEPRESERVE |
| NODESTABILIZER | [-10,0] | NODEPRESERVE |
| NODESTABILIZERADV | [-12,-1] | NODESTABILIZER e INFUSION |
| VISPOWER | [-10,-2] | NODESTABILIZER (1 de warp) |

Los textos son los de TC4. Cambié lo que hablaba de relés y CV, y agregué cómo se arma el frasco.

## Archivos

- **API:** `api/nodes/NodeData`.
- **Bloques:** `BlockJarNode`, `BlockJarNodeItem`, `blocks/misc/BlockNodeStabilizer` (estabilizador, avanzado y transductor).
- **Tiles:** `TileJarNode`, `TileNodeStabilizer`, `TileNodeConverter`. En `TileAuraNode` se agregaron `checkLock`, la parte energizada y `explodify`.
- **Varita:** `NodeJarHelper` y los ganchos en `ItemWand.onItemUseFirst`.
- **Renderers:** `TileJarNodeRenderer`, `TileNodeStabilizerRenderer` y la parte energizada de `TileAuraNodeRenderer`.
- **Anomalía:** `anomaly/AnomalyNodeConvergence`.
- **Modelos:** `models/block/node_stabilizer_block.obj`, que es la base del obj de TC4 pasada a coordenadas de bloque. El transductor usa el mismo, rotado 180°.

Compila. No está probado en el juego. Lo más incierto es la orientación y las UV del obj y el brillo de los pistones.
