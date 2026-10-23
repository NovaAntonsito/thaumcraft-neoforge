# Void Expeditions (addon)

Un mod aparte que depende de Thaumcraft: corridas rogue-lite por cadenas de laberintos del vacío. Vive en `addons/void-expeditions` como subproyecto de Gradle (`:void-expeditions`), con mod id `voidexpeditions`.

Para probarlo: `./gradlew :void-expeditions:runClient`. Gradle necesita `JAVA_HOME` apuntando a un JDK 21; el de la terminal es Java 8.

## Cómo se desbloquea

- Al terminar Susurros del vacío (VOIDEXPLORER) se revela la investigación secreta **VOIDGATE**, en el capítulo VOID.
- Esa investigación da dos recetas arcanas:
  - **Segmento de la Puerta del Vacío** (sale x4): obsidiana llorosa, placas de vacío y una semilla del vacío.
  - **Terminal de expedición:** placas de vacío, cristal espejado, semillas del vacío y una tablilla rúnica.

## La Puerta (multibloque, estilo Stargate)

- **Anillo:** 16 segmentos de pie, en un círculo de 7×7, vacío por dentro (`GateStructure.FRAME`).
- **Terminal:** en el suelo, a 2 bloques delante o detrás del segmento más bajo, como el DHD del Stargate.
- **Activación:** clic con un caster o una varita en un segmento o en la Terminal.
  - El anillo se enciende con chevrones violetas.
  - El centro se llena con el **horizonte del vacío**, que usa la textura animada del portal del Nether y es emisivo.
  - Suena una explosión, el "kawoosh".
  - La Terminal queda vinculada a esa puerta.
- **Colapso:** si se rompe un segmento o la Terminal, el horizonte desaparece, el anillo se apaga y la Terminal se desvincula.

## Ponder

Es opcional: se registra solo si Ponder está cargado (`VoidExpeditionsClient`). Se abre con W sobre un segmento, la Terminal, el sello o un fragmento.

- **"Levantar la Puerta del Vacío":**
  - Los 16 segmentos aparecen uno por uno alrededor del círculo y después la Terminal.
  - El caster enciende el anillo.
  - Muestra el clic derecho que abre la Terminal y cómo se rompe el anillo.
- **"Partir en una expedición":** el horizonte, el sello, los dones, el jefe final y el premio.
- **Estructura:** `addons/void-expeditions/tools/gen_ponder_nbt.py`, que reutiliza el generador de Thaumcraft.
- **Textos:** en `tools/gen_resources.py`, como el resto de los JSON del addon.

## Cómo se juega

- **Terminal:** clic derecho. Se elige la dificultad (**Inquieto**, **Abismal** o **Primordial**) y el largo (**3, 5 o 7** encuentros). También tiene la tienda.
- **Horizonte:** al entrar, empieza una corrida con la configuración de su Terminal.
- **Límite:** **2 corridas por día de Minecraft** (`Expeditions.RUNS_PER_DAY`).
- **Cada encuentro** es un laberinto del Outer Lands en la dimensión `voidexpeditions:expedition`.
  - El tamaño va de 9 celdas a 15.
  - En la sala de la llave hay un **Sello del Vacío**, la tablilla rúnica de TC4 y un cofre con el loot de los encuentros del vacío. La profundidad del loot sube con el encuentro y la dificultad.
  - Los guardianes y campeones aumentan con la dificultad y el encuentro.
- **Portal del centro:**
  - **Con sello:** se abre la elección de **1 perk entre 3**. Al elegir, se gasta el sello y vas al siguiente encuentro. Si cerrás la pantalla, la elección queda guardada.
  - **Último encuentro:** no pide sello. Hay que abrir la sala del jefe con la tablilla y **matar al jefe** (guardián, gólem, líder cultista o taintacle gigante). Ahí el portal te lleva a casa, con un **cofre final** al lado y **Fragmentos del Vacío** (dificultad × largo).
  - **Agachado y con el portal cerrado:** abandonás la corrida.
- **Muerte:** se pierde la corrida y los perks, pero se conserva el inventario. Si compraste vida extra, la primera muerte la gasta en vez de terminar la corrida.
- Las expediciones **no suman warp**.

## Tienda (Terminal, con Fragmentos del Vacío)

Se compra antes de cada corrida y se gasta al empezarla. La tienda está cerrada durante una corrida.

- **Vida extra (6):** la primera muerte de la próxima corrida no la termina. Funciona como un tótem.
- **Perk inicial (4):** empezás la próxima corrida con el perk que elijas (cualquiera menos los malditos).
- **Foco del vacío (15):** un foco avanzado con un medio y uno o dos efectos al azar, con ajustes al azar. **Te lo quedás.** Uno por corrida.

## Perks

Los numéricos se acumulan si salen de nuevo. Los que hacen algo especial no se vuelven a ofrecer si ya los tenés, y en cada oferta sale como mucho un maldito.

- **De jugador:** Vitalidad, Baluarte, Presteza, Fuerza, Firmeza y Fortuna.
- **De caster:** Ahorro de vis, Potencia y Lanzamiento rápido.
- **De aura:**
  - Sifón de vis: el aura a tu alrededor se recarga.
  - Guarda de flux: limpia el flux a tu alrededor.
- **De warp:**
  - Guarda del warp: sin eventos de warp.
  - Mente clara: cada portal borra el warp temporal.
- **De utilidad:**
  - Ojo nocturno: visión nocturna.
  - Sentir el sello: los sellos brillan.
  - Brújula del vacío: flecha y distancia al portal en la barra de acción.
- **Malditos:**
  - Pacto de sangre: +50% de poder, −2 corazones.
  - Cañón de cristal: +4 de daño, −4 de armadura.
  - Frenesí: −35% de enfriamiento, +20% de vis.

## Lo que se agregó a Thaumcraft

- **`thaumcraft.api.maze`** (`MazeWorlds` e `IMazeWorld`): una dimensión puede reutilizar los laberintos del Outer Lands. Decide qué celdas son suyas, qué va en la sala de la llave y adónde lleva el portal.
  - El vacío de Susurros ahora se registra por acá (`VoidMazeWorld`).
  - Los addons usan las celdas desde `MazeWorlds.ADDON_START`.
- **`thaumcraft.api.casters.CasterModifiers`:** descuento de vis, multiplicador de poder y de enfriamiento que no vienen del equipo.
- **`thaumcraft.api.events.WarpEventCheck`:** un evento cancelable antes de cada chequeo de eventos de warp.
- **Investigaciones de otros mods:** `ResearchManager` ahora encuentra los archivos de investigación de otros mods (antes solo veía los de Thaumcraft).
- **Arreglo:** `PacketWarpMessage` impedía arrancar un servidor dedicado (nombraba al jugador del cliente).

## Lo que falta

- Texturas y modelos propios. El anillo usa obsidiantile más el chevrón generado (`void_gate_chevron.png`); la Terminal es un cubo de obsidiantile y metal del vacío; el sello y la moneda usan los objetos eldritch.
- Probarlo jugando. En un servidor dedicado arranca, carga la investigación y crea la dimensión. Lo más incierto: caer bien al lado del portal, que el jefe del último laberinto se cuente bien y el balance de precios y perks.
- Los ítems de Curios se caen al morir: solo se conserva el inventario normal.
