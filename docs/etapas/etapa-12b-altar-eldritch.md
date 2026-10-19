# Etapa 12b: altar eldritch (de TC4)

TC6 había dejado el altar eldritch sin hacer, así que lo traigo de Thaumcraft 4. El código sale de [OpenTC4](https://github.com/IgnoreLicensesCN/OpenTC4).

## Qué hay

- **Anillo eldritch en el mundo**: aparece en el overworld, en 1 de cada 66 chunks que no tienen túmulo.
  - Un piso de 7x7 de obsidiana y baldosa de obsidiana, sin las esquinas.
  - En el centro, la piedra clave del altar, con el obelisco flotando encima.
  - En los bordes, ocho piedras de remate.
- **Obelisco**: una columna de tres bloques que sube y baja con suavidad.
  - Las caras muestran estrellas, como el portal del End. De lejos se ven como una textura plana.
  - Tiene los costados tallados y una piedra de remate en cada punta.
  - A los mobs eldritch que estén a menos de 6 bloques les da Fuerza y Regeneración, con unas chispas que van hacia ellos.
- **Altar**:
  - En 4 de cada 10 es un santuario de cultistas: aparecen cuatro clérigos ritualistas alrededor y, mientras quede alguno vivo, caballeros que los cuidan (hasta 8 cultistas). Tiene estandartes carmesí.
  - En 2 de cada 10 llama guardianes eldritch.
  - El resto está tranquilo.
- **Ojo eldritch**:
  - Se pone en la piedra clave, hasta cuatro, y se ven clavados en sus costados.
  - Desde el tercer ojo, el altar empieza a llamar guardianes eldritch.
  - Se consigue de dos formas:
    - Los guardianes eldritch lo sueltan muy de vez en cuando: 2,5%, más con Saqueo.
    - Se infusiona: un ojo de ender en el centro, y alrededor una semilla del vacío y un lingote de oro.
- **Investigación "Opening the Eye"** (OCULUS) en la pestaña Eldritch: pide Crimson Rites y la base eldritch, y da 6 de warp. Ahí está la receta del ojo.
- **Bloques nuevos**: piedra incrustada brillante (da luz) y baldosa de obsidiana, las dos con su ítem.
- **Colocador de obelisco**: solo para creativo. Clic arriba de un bloque y arma el altar con su obelisco.
- **Romper el altar**: si rompés cualquier pieza, se cae todo lo de alrededor con una explosión chica. No suelta nada.
- **Clérigos ritualistas**: dejan de rezar si se alejan o si el altar ya no está.

## Bugs del original que quedan igual

- **Estandartes**: el original pone un estandarte en cada lado, pero después limpia el aire sobre el piso y borra dos de los cuatro. Solo quedan el del oeste y el del norte.
- **Obelisco**: el contador del obelisco nunca avanza, así que revisa los mobs todos los ticks en vez de cada segundo.

## Diferencias con TC4

- **Nodo de aura**: en TC4 el anillo traía un nodo encima del obelisco. TC6 no tiene nodos, así que no está.
- **Chispas del obelisco**: usan el wisp eldritch de TC6.
- **Bloques**: el `BlockEldritch` de TC4 tenía 11 variantes en un solo bloque. Acá cada una es un bloque propio.
- **Bordes de la piedra brillante**: en TC4 se achicaba un poco en los bordes que no tocaban otro bloque. Acá es un cubo entero.
- **Aspectos de la receta**: "Iter" (viaje) no existe en TC6, así que va Motus.

## Lo que sigue (12c)

- La dimensión de las Outer Lands y su laberinto.
- El ritual que abre el portal con los cuatro ojos. En TC4 hacía falta un nodo oscuro y vis primal con la varita, así que hay que adaptarlo.
- Las piedras con glifos y el resto de los bloques del laberinto.

## Cómo probarlo

1. Agarrá el colocador de obelisco en creativo y usalo sobre el piso.
2. Poné ojos eldritch en la piedra clave: aparecen en sus costados.
3. Spawneá un guardián eldritch cerca del obelisco: le salen chispas y tiene Fuerza y Regeneración.
4. Para el anillo natural, buscá en mundos nuevos. Es raro, más o menos uno cada 66 chunks.
