# Etapa 8b-1: sellos (colocar, sacar, guardar)

## Qué entra

- **Items de sello** (`seal_blank`, `seal_pickup`, `seal_fill`).
  - En 1.12 era un solo item `seal` con metadata según el orden de registro.
  - Ahora hay un item por sello, con los mismos nombres de los modelos originales.
- **Colocar:** click derecho con un sello en cualquier cara de un bloque. En creativo no se gasta.
- **Sacar:** shift + click derecho con la campana sobre el sello. Suelta el item del sello.
- **Ver los sellos:** con un sello, la campana o un golem en la mano, se dibujan los sellos a 16 bloques o menos.
  - Con shift se ven a través de las paredes.
  - El sello de Collect también muestra las esquinas de su área (3x3 por defecto).
- **Guardado:** los sellos se guardan con el chunk y vuelven al cargar el mundo.
  - Al salir de un mundo se borran de memoria, así no aparecen en otro save.
- **Sincronización:** `PacketSealToClient`, que también se manda al entrar a ver un chunk.

## Sellos que funcionan

- **Collect (`thaumcraft:pickup`):** los golems juntan los items tirados en el área.
- **Store (`thaumcraft:fill`):** los golems guardan lo que llevan en el cofre donde está el sello, o lo tiran ahí.
- Sin GUI, usan los valores por defecto del original: lista negra vacía, así que aceptan cualquier item.
- Prueba rápida: Collect en el piso, Store en un cofre, un golem cerca, y tirás items en el área.

## Recetas

- **Sello en blanco:** arcana sin forma, investigación `CONTROLSEALS`. Lleva bola de arcilla, sebo, tinte rojo y nitor, y da 3.
- **Collect:** crisol, sello en blanco + 10 Desiderium (`SEALCOLLECT`).
- **Store:** crisol, sello en blanco + 10 Aversio (`SEALSTORE`).

## Diferencias con 1.12

- Los sellos de un chunk viven en un attachment del chunk (`thaumcraft:seals`) en vez del NBT que escribía `ChunkDataEvent.Save`.
  - `SealHandler.markChunkAsDirty` reescribe el attachment cada vez que se pone o se saca un sello.
- Al descargar el mundo también se borran las tareas de los golems. El original no lo hacía.
- El "ore dictionary" de los filtros ahora compara tags `c:` compartidos.
  - "NBT" pasa a ser cualquier data component menos el daño.
- `countStackInWorld` conserva el bug del original (el chequeo `isEmpty` está al revés y siempre cuenta 0).
  - Solo importa con la lista blanca con cantidades, que necesita la GUI.

## Pendiente

- **8b-2:**
  - GUI del sello (prioridad, color, filtro, área) al hacer click con la campana.
  - Silueta del golem a través de paredes.
  - Resto de los sellos básicos: Empty, Harvest, Butcher, Guard, Lumber, Breaker, Use.
- **8c:** sellos avanzados, Provide, Stock y logística.
- **10:** la chispa roja del sello frenado por redstone.
