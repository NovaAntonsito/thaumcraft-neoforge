# Etapa 9d-4b: el warden eldritch y el constructo eldritch

Llegan los dos jefes eldritch. Los dos usan la base de jefes de la 9d-4a: barra violeta, se enojan con golpes de más de 35, sueltan perla primordial y un tesoro raro.

## Warden eldritch (`eldritch_warden`)

- 400 de vida, 10 de daño, velocidad 0.33, 4 de armadura. Es el guardián eldritch en grande, con su propia textura y un ojo que brilla adentro de la capucha.
- **Aparece saliendo del piso** durante 7,5 segundos, rodeado de humo. Mientras tanto no se mueve y no le hacen nada.
- **Escudo**: arranca con 264 de absorción (dos tercios de la vida), que se ve en una **segunda barra azul**. Si no le pegan, recupera 1 cada 25 ticks.
- Va dejando **campo de savia** (la del focus de maldición) por donde camina. A él no le hace nada.
- De lejos:
  - 80% de las veces tira un **orbe eldritch**, alternando de mano.
  - Si no, y te ve, **grita**: te empuja lejos, wither y debilidad por 20 segundos y 3 a 5 de warp temporal.
- De cerca pega con los brazos.
- **Frenesí**: la primera vez que le rompés el escudo, vuelve teletransportado a su casa y durante 7,5 segundos no se mueve ni le podés hacer nada. Mientras, larga anillos de savia cada vez más grandes, con chispas violetas, que se van solos en 12 a 20 segundos.
- No le hace daño ahogarse ni el wither.
- Ataca jugadores y cultistas, no al guardián eldritch.
- Si es campeón se llama "[título] the [modificador]", con títulos como Nyogtha o Shudde M'ell.
- Se escanea como `!ORBOSS`. Tiene Alienis 40, Mortuus 40 y Exanimis 40.

## Constructo eldritch (`eldritch_golem`)

- 400 de vida, 10 de daño, velocidad 0.3, 6 de armadura. No se quema. Es grande (1.7 veces el modelo).
- Se despierta durante 5 segundos, curándose 2 por tick.
- Al caminar rompe las cajas y urnas de botín (sueltan su tesoro) y cualquier cosa blanda bajo sus pies, como pasto o flores.
- Pega 7,5 (tres cuartos de su daño) y te levanta un poco. Suenan los golpes del golem de hierro.
- **Sin cabeza**: el golpe que lo mataría le vuela la cabeza. Explota (sin romper bloques), queda con el cuello abierto y se despierta de nuevo. Desde ahí:
  - Sus golpes además te mandan lejos.
  - Carga energía (150) y la gasta tirando **orbes de energía** desde el cuello (15 a 19 por tiro). Cuando se vacía vuelve a cargar.
  - Echa chispas y humo del cuello, y le saltan **arcos eléctricos** al piso con su sonido.
- Si es campeón se llama "[modificador] Eldritch Construct".
- Se escanea como `!ORBOSS`. Tiene Alienis 40, Potentia 40 y Machina 40.

## Otros cambios

- Huevos de los dos jefes.
- El campo de savia ahora también se va cuando le toca un tick programado, no solo el aleatorio (lo usa el frenesí del warden).
- Sonido nuevo `jacobs` (los arcos del constructo).

## Diferencias con el original

- **Cabeza del constructo al despertar**: el modelo baja la cabeza mientras se despierta, pero el cliente nunca se entera del tiempo (el aviso se manda en 150 y el constructo cuenta desde 100). Así que en el original no se veía. Lo dejé igual.
- **Arcos del constructo**: en el original el arco busca el piso siempre desde el mismo bloque, así que a veces no sale. Lo dejé igual.
- **Teletransporte del warden**: si su casa no está libre, los reintentos cambian de columna pero siguen mirando el mismo bloque, así que no se mueve. Como en el original.
- **Frenesí una sola vez**: no se guarda al salir del mundo. Si cargás el mundo, puede volver a pasar. Como en el original.
- **Partículas del teletransporte**: el original las creaba en el servidor, donde no se dibujan. No las puse.
- **Savia bajo el warden**: el original también la ponía del lado del cliente, lo que dejaba bloques fantasma. Acá solo la pone el servidor.
- **Escudo**: en 1.21 la absorción tiene tope. Le subí el tope del warden a 264 para que entre el escudo.
- **Ojo del warden**: se dibuja sumando luz encima del modelo, con el brillo que late como en el original.
- **Partículas**: siguen siendo de vanilla hasta la etapa 10 (humo, polvo de color y chispas).
- **Tierras exteriores**: no están portadas.

## Cómo probarlo

1. En supervivencia, tirá un huevo de Eldritch Warden en un lugar plano. Mirá cómo sale del piso.
2. Pegale hasta que se vacíe la barra azul. Se tiene que teletransportar a donde apareció y largar los anillos de savia.
3. Alejate para que te tire orbes o te grite.
4. Tirá un huevo de Eldritch Construct cerca de unas cajas de botín (las del portal mayor) y fijate que las rompe al pasar.
5. Bajale la vida hasta que pierda la cabeza. Mirá los arcos y los orbes que tira desde el cuello.
6. Matá a los dos y agarrá las perlas.
