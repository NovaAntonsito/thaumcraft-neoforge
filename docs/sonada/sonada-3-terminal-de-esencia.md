# Versión soñada 3: terminal de esencia

Tercer agregado del branch `sonada`. No está en Thaumcraft: es lo que daba Thaumic Energistics, pero sin AE2. El segundo agregado era Curios, que ya estaba hecho en el port fiel (bolsa de focos en el cinturón y descuento de vis).

## Qué hace

- **Terminal de esencia** (`essentia_terminal`): un bloque que se conecta a los tubos por cualquier lado.
  - Ve todos los frascos a los que llegan los tubos, sin importar hacia dónde apuntan.
  - Una válvula cerrada corta la red.
  - Busca como mucho 1024 tubos y frascos.
  - Nunca mueve esencia por los tubos solo: no tiene succión.
- **Pantalla**: el mismo fondo y los mismos botones que la de logística de los gólems.
  - Arriba están los aspectos que hay en la red, con el total de cada uno. Pasando el mouse ves cuánto hay y cuántas fiolas alcanza.
  - El buscador filtra por nombre o por descripción del aspecto.
  - Abajo a la izquierda dice cuántos frascos ve.
- **Llenar**:
  1. Hacé clic en un aspecto para elegirlo.
  2. Elegí cuántas fiolas con la barra o con + y −.
  3. Apretá **Fill**.
  - Las fiolas vacías salen de tu inventario y vuelven llenas (10 de esencia cada una).
  - Shift + Fill llena todas las que se pueda.
- **Depositar**: el botón **Deposit** vacía en los frascos todas las fiolas llenas que tengas encima, y te devuelve las fiolas vacías. El orden para guardar es este:
  1. los frascos que ya tienen esa esencia;
  2. los frascos vacíos con etiqueta de ese aspecto;
  3. los frascos vacíos sin etiqueta.

  En cada uno de esos pasos, los frascos del vacío van al final. Una fiola se vacía solo si entra entera.
- **Investigación**: *Essentia Terminal* (ESSENTIATERMINAL), en Alquimia al lado de los tubos. Pide TUBES, 1 de observación de alquimia y 1 de teoría de artificio.
- **Receta arcana**: 50 de vis, 1 cristal de aqua y 1 de ordo.
  ```
  B T B     B = placa de latón, T = tubo
  P G P     P = fiola, G = vidrio espejado
  B M B     M = metal alquímico
  ```

## Assets

No hay assets nuevos. El frente del bloque es la tapa del dióptra y los costados son los del auxiliar del fundidor. La pantalla usa `gui_logistics.png` y `gui_base.png`.

## Para tener en cuenta

- Solo cuenta los frascos de esencia comunes y los del vacío. El frasco con cerebro no, porque guarda experiencia.
- Los totales se actualizan dos veces por segundo.
- Compila, pero no lo probé en el juego.

## Archivos

- **Bloque y tile**: `BlockEssentiaTerminal` y `TileEssentiaTerminal`. La búsqueda por la red está en `findJars`.
- **Pantalla**: `ContainerEssentiaTerminal` (los botones llenar y depositar) y `GuiEssentiaTerminal`.
- **Registros**: `ConfigBlocks`, `TCBlockEntities`, `TCMenus` y `ThaumcraftClient`.
- **Datos**:
  - blockstate y modelos;
  - loot table;
  - tag de pico;
  - receta `arcane/essentia_terminal.json`;
  - la entrada en `research/alchemy.json`;
  - los textos en `en_us.json`.

## Cómo probarlo

1. Poné dos frascos con esencia, un tubo encima de cada uno y conectalos con tubos hasta la terminal.
2. Abrila: aparecen los dos aspectos con su cantidad.
3. Llevá fiolas vacías, elegí un aspecto y apretá Fill.
4. Apretá Deposit: las fiolas llenas vuelven a los frascos.
5. Cerrá una válvula del medio: la terminal deja de ver los frascos del otro lado.
