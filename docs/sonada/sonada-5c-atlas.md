# Soñada 5c: atlas del aura

Un ítem nuevo que muestra el aura alrededor tuyo en un mapa. No existe en TC4 ni en TC6; usa assets que ya estaban.

## El ítem

- **`aura_atlas`:** es la nota de investigación con el overlay teñido de violeta vis. Clic derecho abre el mapa.
- **Receta (arcana, AURAATLAS):** 5 papeles, quicksilver arriba, el taumómetro al medio y una bolsa de tinta abajo. Cuesta vis 25 más aer y ordo.
- **Investigación:** AURAATLAS en Auromancia [-8,0], hija de NODES. Pide 1 de observación de Auromancia.

## El mapa

- **Fondo:** `paper.png` de la GUI.
- **Cuadrícula:** 17×17 chunks centrada en vos (8 para cada lado), 12 píxeles por chunk.
- **Color de cada chunk:** violeta según la vis comparada con la base (llega al máximo con 1,5 veces la base). Encima, morado oscuro según el flux. Los chunks no cargados quedan en gris.
- **Nodos:** se dibujan donde están, con el núcleo de `nodes.png` del tipo de nodo, teñido con el color de su aspecto más fuerte. Los energizados se ven más grandes.
- **Anomalías locales:** una estrella del color de su polaridad (verde positiva, roja negativa, dorada mixta).
- **Vos:** la flechita de jugador de los mapas de vanilla, girada hacia donde mirás.
- **Tooltips:**
  - en un chunk, sus coordenadas, la vis sobre la base y el flux;
  - en un nodo, su nombre, posición y aspectos;
  - en una anomalía, su nombre y el tiempo que le queda.
- **Actualización:** cada 2 segundos mientras lo tenés abierto. Se cierra si soltás el atlas.

## Registro de nodos

- **`AuraNodeRegistry`:** guarda por dimensión cada nodo que cargó alguna vez (posición, tipo, brillo, aspectos y si está energizado).
  - El nodo se anota al cargar y cada 10 segundos.
  - El bloque se borra del registro cuando se rompe, explota o se mete en un frasco.
- Así el atlas muestra nodos de chunks descargados, aunque esos datos pueden estar viejos.
- Un nodo que nunca cargó (chunk sin explorar) no aparece.

## Archivos

- `common/items/tools/ItemAuraAtlas`, `client/gui/GuiAuraAtlas`.
- `common/world/aura/AuraNodeRegistry`.
- `PacketAuraAtlasToClient` (la primera abre la pantalla) y `PacketAuraAtlasRequestToServer`.
- Ganchos en `TileAuraNode.onLoad`/`tick` y `BlockAuraNode.onRemove`.
- Color del ítem en `ThaumcraftClient`, modelo `models/item/aura_atlas.json`.

Compila. No está probado en el juego. Lo más incierto es cómo se ve el frame de `nodes.png` con mezcla normal en vez de aditiva.
