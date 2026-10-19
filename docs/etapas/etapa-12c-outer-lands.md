# Etapa 12c: las Outer Lands, el laberinto y el portal (de TC4)

Sigo con lo eldritch de Thaumcraft 4, sacado de [OpenTC4](https://github.com/IgnoreLicensesCN/OpenTC4). Ahora el altar abre un portal a una dimensión nueva, las Outer Lands, con un laberinto adentro.

## Qué hay

- **Dimensión "Outer Lands"** (`thaumcraft:outer_lands`):
  - Es todo vacío, con el bioma eldritch de la etapa 12a.
  - No tiene cielo ni nubes, la hora está fija, y hay una niebla violeta oscura.
  - No se puede dormir ni usar ancla de reaparición, y la brújula gira.
- **Laberinto**:
  - Cada altar arma su laberinto alrededor de su chunk, de 15 a 29 celdas por lado. Cada celda es un chunk, a la altura 50.
  - Lo arma cuando ponés ojos en el altar, o cuando aparece un anillo natural (en ese caso de 11 a 21 celdas).
  - Las celdas son pasillos, la sala del portal, la sala de la llave, nidos, bibliotecas y la sala del jefe (2x2 celdas).
  - Algunos pasillos tienen variantes:
    - con trampas;
    - con piedra incrustada;
    - con fibras de taint;
    - con telarañas y un spawner de arañas mentales.
  - Alrededor de todo hay "Nada":
    - no se rompe;
    - te lastima si la tocás;
    - si queda a la vista, muestra las estrellas como el obelisco.
  - Las celdas se guardan en el mundo (en los datos del overworld, como hacía `labyrinth.dat`).
- **Ritual del portal** (lo que elegiste):
  - Con los 4 ojos en la piedra clave, hacé clic con el guantelete.
  - Consume 100 de vis del aura del chunk y abre el portal arriba de la piedra clave.
  - Hace falta que el laberinto ya exista. Se arma al poner los ojos, así que en la práctica siempre está.
- **Portal**:
  - Un remolino animado entre la piedra clave y el obelisco.
  - Al pararte adentro te lleva a las Outer Lands, al lado del portal de la sala del centro. Desde ahí te devuelve al overworld.
  - El primer viaje completa la investigación "The Outer Lands" (ENTEROUTER).
- **Bloques nuevos**, todos con las texturas de TC4:
  - Piedra antigua (con su versión donde no aparecen mobs), roca antigua y piedra con costra.
  - Pedestal de piedra antigua, y escaleras y losa de piedra antigua.
  - Piedra con glifos, umbral antiguo, mecanismo de cierre antiguo, abertura con costra y piedra rúnica.
  - Nada, "Impassable" (la pared invisible de las puertas del jefe) y el portal. Estos tres no tienen ítem.
- **Mobs en las Outer Lands**:
  - Los guardianes eldritch aparecen con absorción de media vida, que se les va recargando de a poco.
  - Los guardianes no traen la niebla allá.
  - Hay más chance de campeones, y mucha más en las salas de la llave y en las bibliotecas.
- **Vuelo**: en las Outer Lands no se puede volar fuera de creativo. Te avisa con "Something disrupts your ability to fly."

## Bugs del original que quedan igual

- **Cuartos en los callejones**: el generador quiere poner nidos o bibliotecas en la mitad de los callejones sin salida, pero el contador del bucle nunca sube. Termina llenando todos menos uno.

## Diferencias con TC4

- **Ritual**:
  - TC4 pedía un nodo oscuro encima de la piedra clave, y 100 de cada vis primal sacados de la varita.
  - TC6 no tiene nodos ni varitas, así que va con el guantelete y 100 de vis del aura (tu elección).
  - El texto de OCULUS en inglés ahora dice eso.
- **Cristales del laberinto**: TC4 tenía un racimo de cristal mezclado. En TC6 hay uno por aspecto, así que va uno de los seis primales al azar. Tampoco cuelgan para un lado, se pegan a lo que tengan al lado.
- **Fibras de taint**: TC4 además cambiaba el bioma a taint. TC6 no tiene bioma de taint, así que eso no está.
- **Texturas al azar**:
  - En TC4 la piedra antigua elegía una de cuatro texturas por cara, y la roca antigua armaba un dibujo de 2x2 bloques.
  - Acá cada bloque elige una de las cuatro texturas para todas sus caras.
- **Nada**: en TC4 era un solo bloque con dos metas. Acá son dos bloques: la Nada tapada y la Nada a la vista, que es la que dibuja las estrellas.
- **Búsqueda del portal**: TC4 revisaba cada bloque en 128 bloques a la redonda. Acá revisa los chunks cargados en ese radio, después de cargar el chunk de llegada.
- **Absorción de los guardianes**: en 1.21 la absorción tiene tope por atributo. El tope del guardián es su vida entera, para que entren la de las Outer Lands y la del campeón protegido.
- **Textura de lejos del obelisco**: faltaba `particlefield32.png` desde la 12b. Ya está.

## Lo que sigue (12d)

- **Mecanismo de cierre**: abrirlo con la tablilla rúnica, que también aparece en la sala de la llave.
- **Jefes de la sala grande**: hoy la sala existe, pero no tiene jefe.
- **Bloques que todavía no hacen nada**:
  - la abertura con costra, que suelta cangrejos;
  - la piedra rúnica, que es una trampa.
- **Piedra con glifos**: que suelte el fragmento de conocimiento y que dé la investigación OUTERREV.
- **Bloquear construcción cerca del jefe**: no se puede poner bloques cerca de un jefe activo.

## Cómo probarlo

1. En creativo, usá el colocador de obelisco sobre el piso.
2. Poné los 4 ojos eldritch en la piedra clave.
3. Asegurate de que el chunk tenga 100 de vis o más, y hacé clic en la piedra clave con el guantelete. Se abre el portal.
4. Metete en el portal: aparecés en la sala del centro del laberinto, en las Outer Lands.
5. Recorré:
   - Probá volar en supervivencia: te baja.
   - Tocá la Nada detrás de una pared rota: te lastima.
6. Volvé a meterte en el portal: volvés al overworld, al lado del altar.
