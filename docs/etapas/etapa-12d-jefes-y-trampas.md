# Etapa 12d: cierre, jefes, trampas y glifos (de TC4)

Cierro lo eldritch de Thaumcraft 4 sacado de [OpenTC4](https://github.com/IgnoreLicensesCN/OpenTC4). Con esto el laberinto de las Outer Lands ya se juega entero: buscás la tablilla, abrís la sala grande y peleás con el jefe.

## Qué hay

- **Tablilla rúnica** (`runed_tablet`):
  - Flota en el centro de la sala de la llave, sobre la piedra clave, y nunca desaparece.
  - Las explosiones no la rompen.
  - Dice "It seems to be part of something larger."
- **Mecanismo de cierre antiguo** (la cerradura de la puerta de la sala del jefe):
  - Muestra la cerradura del lado que da al pasillo, y el resto es piedra de umbral.
  - Cuatro brazos de cubos eldritch cruzan la puerta y laten.
  - La puerta muestra las estrellas, como la Nada.
  - Clic con la tablilla: la tablilla queda puesta en la cerradura y saltan chispas.
  - Bombea cada 5 ticks y los brazos se retraen de a un cubo.
  - A los 100 ticks se abre la puerta y aparece el jefe.
- **Jefes**: rotan en este orden: gólem, guardián, portal carmesí y taintáculos. Con 25% de chance se saltea uno. La cuenta es del mundo.
  - **Gólem eldritch**:
    - en el centro, sobre un pedestal de escaleras;
    - tres obeliscos alrededor;
    - urnas y cajas de botín por la sala.
  - **Guardián eldritch (Warden)**:
    - sobre un pedestal en la esquina opuesta a la entrada, mirando a la cerradura;
    - dos obeliscos con urnas alrededor;
    - cuatro piedras rúnicas sobre pedestales alrededor del centro.
  - **Gran portal carmesí**:
    - un piso de umbral roto;
    - un anillo de columnas de piedra antigua y piedra con glifos.
  - **Taintáculos**:
    - cinco taintáculos campeones, mezcla de normales y gigantes; el del centro es gigante solo en difícil;
    - la sala se llena de fibras, costra y tierra de taint.
  - Al abrir, los jugadores cerca reciben el aviso del jefe que viene (los textos `tc.boss.*` de TC6).
- **Construcción bloqueada**: en una sala de jefe de las Outer Lands no se puede poner bloques si hay un jefe a 32 bloques o menos. En creativo sí se puede.
- **Abertura con costra**: piedra con costra con una ventila del lado del pasillo. Cuando hay un jugador a 16 bloques o menos:
  - echa vapor y escupe un cangrejo eldritch sin casco;
  - repite cada 150 a 250 ticks, hasta que haya 6 cangrejos a 32 bloques.
- **Piedra rúnica** (trampa):
  - cada 10 a 35 ticks electrocuta al jugador más cercano a 3 bloques o menos;
  - hace 2 de daño mágico, y la mitad de las veces suma 1-2 de warp temporal;
  - tiene runas flotando alrededor y elige una de cuatro texturas.
- **Piedra con glifos**: al romperla suelta un **fragmento de conocimiento**.
- **Investigación OUTERREV** ("Revelations in the Outer Lands"):
  - aparece al escanear piedra con glifos o piedra rúnica, si ya tenés ENTEROUTER;
  - pide 1 de observación eldritch y después muestra el texto de TC4.

## Bugs del original que quedan igual

- **Construir en salas sin jefe "de verdad"**: el bloqueo solo mira jefes que son `EntityThaumcraftBoss`. El portal carmesí y los taintáculos no lo son, así que en esas dos salas se puede construir igual.
- **Gólem sin salida**: si la sala no encuentra su celda de entrada, los tres obeliscos del gólem caen en el mismo lugar.

## Diferencias con TC4

- **Fragmento de conocimiento**:
  - En TC4 daba 1-2 puntos de cada aspecto primal para investigar.
  - TC6 no tiene esos puntos, así que da entre media y una observación en una categoría al azar, como el extra de las curiosidades.
- **Hacia dónde miran la cerradura y la ventila**: TC4 lo guardaba en la tile entity. Acá va en el estado del bloque (FACING), así el modelo muestra el lado correcto.
- **Ventila de la abertura**: el modelo OBJ de TC4 va dentro del modelo del bloque, no en un renderer aparte.
- **Rayo de la trampa**: TC4 usaba su rayo violeta. Acá es el rayo de TC6, con el mismo color y el sonido de zap.
- **Taint en la sala del jefe**: TC4 cambiaba el bioma a taint. TC6 no tiene bioma de taint, así que eso no está. Las fibras no tienen variantes: va siempre la fibra común.
- **Cuenta de jefes**: TC4 la guardaba en un archivo de datos compartido por todas las dimensiones. Acá va en los datos del overworld.

## Cómo probarlo

1. Entrá a las Outer Lands por el portal (etapa 12c).
2. Buscá la sala de la llave (la de los guardianes): la tablilla flota en el centro. Agarrala.
3. Buscá la sala grande, que tiene una puerta de 5x5 con estrellas y la cerradura en el medio.
4. Hacé clic en la cerradura con la tablilla. Esperá 5 segundos y aparece el jefe.
5. Probá poner un bloque en la sala del gólem o del guardián en supervivencia: no se puede.
6. Acercate a una piedra rúnica: te electrocuta. Acercate a una abertura con costra: salen cangrejos.
7. Rompé una piedra con glifos: sale el fragmento. Escaneala con el taumómetro: aparece OUTERREV.
