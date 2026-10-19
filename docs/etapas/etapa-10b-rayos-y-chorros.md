# Etapa 10b: rayos, chorros y escudos

Segunda parte de los efectos. Ahora los rayos, los chorros de esencia y los escudos de runas se dibujan como en el original, con sus texturas propias.

## Qué cambió

- **Tubos**: el original usaba una librería aparte (GLE) para dibujar tubos que siguen una curva. Acá hay un reemplazo propio que hace lo mismo: un tubo por los puntos, con su ancho y su color en cada punto, y la textura corriendo a lo largo.
- **Arco eléctrico**: el arco que cae como tirado desde el origen hasta el blanco, con chispas en el camino. Lo usan:
  - la matriz de infusión inestable,
  - el gólem eldritch,
  - el guardián eldritch jefe,
  - el portal mayor de los cultistas.
- **Rayo**: el rayo ondulado entre dos puntos, que tiembla más mientras se apaga. Lo usan el zap del wisp y el foco de rayo.
- **Chorro de esencia**: el tubo de esencia que sale de la fuente, crece hacia el destino, gotea y después se mete adentro. Si llega otro chorro igual mientras uno corre, el que corre se alarga. Se ve en:
  - frascos, alambique y tubos que mandan esencia a la matriz o a otros aparatos,
  - la matriz de infusión tomando esencia (sale de abajo de la matriz, como en el original).
- **Chorro de agua**: el de la jarra sin fondo, con el mismo tubo en celeste.
- **Chorro del vacío**: el hilo oscuro que el sifón del vacío le saca a una grieta de flux, con las estrellas del portal del End.
- **Escudo de runas**: el destello de runas sobre media esfera, mirando de dónde vino el golpe.
  - Los campeones protegidos (warded) y los mobs eldritch lo muestran cuando les pegan y todavía tienen absorción.
  - El pedestal estabilizador también lo muestra (en violeta).
- **Grito sónico**: la onda que tira el guardián eldritch desde la cabeza, siguiendo hacia dónde mira.

## Bugs del original que quedan igual

- **Sonido de carga del escudo**: en el `sounds.json` original el sonido está escrito "runicshieldecharge", pero el juego lo pide como "runicshieldcharge". Por eso nunca sonaba. Acá tampoco suena.

## Diferencias con el original

- **Chorro del vacío**: el original le pasaba su shader del End a las dos capas. Acá el brillo de afuera usa la textura de estrellas sumada, y el centro usa el portal del End de vanilla.
- **Texturas animadas**: las de runas y ondas se ven suavizadas, como pedía su `.mcmeta`.

## Lo que no se portó

- **Rayo del bastón, rayo del perforador, chorro del perforador y protección de bloques**: Thaumcraft 6 tenía el código, pero nada los usaba. Además faltan sus texturas (`beam.png`, `beam2.png`, `beam3.png`) en los assets originales. Quedan afuera.
- **Inicio del chorro**: el original corría un poquito el inicio del chorro cuando salía de un bloque de entrada o salida de esencia. Esos bloques todavía no están portados.
- **Escudo rúnico del jugador**: la carga del escudo rúnico en la armadura no está portada todavía. Por ahora solo lo muestran los mobs.
- **Chispas dentro de menús** (mesa arcana y similares): siguen pendientes.

## Cómo probarlo

1. `/thaumcraft research @s all`.
2. Armá una infusión que pida esencia, con frascos cerca: tienen que salir tubos de color desde los frascos hacia abajo de la matriz.
3. Poné una jarra sin fondo al lado de un caldero: sale un chorro celeste.
4. Poné un sifón del vacío cerca de una grieta de flux: aparece el hilo oscuro con estrellas.
5. Pelealo a un guardián eldritch: grita con la onda, y su escudo destella cuando le pegás si todavía tiene absorción.
6. Spawneá un gólem eldritch y alejate: tira arcos eléctricos.
7. Hacé que un wisp te ataque: el zap es un rayo ondulado de su color.
