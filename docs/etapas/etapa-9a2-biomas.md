# Etapa 9a-2: biomas (Magical Forest y Eerie)

Segunda parte de la generación del mundo. Necesita **TerraBlender** (ya está en `build.gradle` y como dependencia obligatoria en `neoforge.mods.toml`).

## TerraBlender

- `build.gradle` suma el maven de Forge (`maven.minecraftforge.net`, solo el grupo `com.github.glitchfiend`) y `TerraBlender-neoforge:1.21.1-4.1.0.8`.
- La versión está en `gradle.properties` (`terrablender_version`). Si Gradle no la encuentra, cambiala por otra 4.1.x de 1.21.1.
- Para jugar fuera del entorno de desarrollo, TerraBlender tiene que estar en la carpeta `mods`.

## Magical Forest

- Se ubica con una región de TerraBlender (`TCBiomes.MagicalForestRegion`). En esa región, el bosque común de vanilla pasa a ser Magical Forest.
- El peso sale de la config (`biomeMagicalForestWeight`, 5 por defecto, de 0 a 100). La región vanilla pesa 10, así que con 5 un tercio del mundo usa la región de Thaumcraft.
- `generateMagicForest = false` lo apaga.
- Colores del original: pasto `0x55FF81`, follaje `0x66FFC5`, agua `0x0077EE`.
- Spawns: los normales, más lobos, caballos, brujas, endermen y vex.
- Decoración (`BiomeGenMagicalForest`, igual que el `decorate` del original):
  - un bloque de **grass_ambient** por chunk;
  - de 0 a 2 rocas de piedra musgosa;
  - hongos gigantes (1 en 40 por cada cuadrado de 4x4);
  - 2 árboles por chunk (a veces 3): silverwood 1 en 18, greatwood 1 en 12, si no el **árbol mágico grande** (`WorldGenBigMagicTree`, un roble enorme);
  - hasta 8 **vishrooms** pegados a troncos.
- Flores, pasto, helechos, nenúfares y hongos chicos son features de vanilla puestas en el JSON del bioma.

## Eerie

Está registrado (`data/thaumcraft/worldgen/biome/eerie.json`) pero **no se genera**, igual que en el original, donde nunca se agregaba a la lista de biomas. Cielo violeta oscuro, pasto `0x404840`, sin lluvia.

## grass_ambient

- Pasto que, de noche, saca motas de luz del pasto de alrededor.
- Se rompe con pala. Suelta tierra, o el bloque con toque de seda.
- Se muere a tierra en la oscuridad y esparce pasto común (no se copia a sí mismo).

## Aspectos

Se agregaron los aspectos que faltaban de la 9a-1 (hojas, retoños, plantas y cristales) y el de grass_ambient (pasto + 5 Lux).

## Diferencias con el original

- En 1.12 el Magical Forest entraba en las listas WARM y COOL de biomas. Desde 1.18 los biomas se ubican por clima, así que reemplaza al bosque común dentro de la región de TerraBlender. El relieve es el del bosque, no el del original (altura 0.2, variación 0.3).
- La opción gráfica `blueBiome` (el bosque en azul) no está: los colores ahora son datos del bioma.
- El `decorate` del original usaba un corrimiento de +8 bloques. Acá todo queda dentro del chunk.
- El árbol mágico grande sortea su altura en cada árbol. En 1.12 se usaba una sola instancia que se quedaba con la altura del primero.
- Faltan los spawns de pech y wisp (Magical Forest) y de los zombis cerebrudos, wisps y guardianes (Eerie). Llegan con los mobs en la 9c.
- El tercer bioma, Outer Lands, va con la etapa eldritch.

## Cómo probarlo

1. Mundo **nuevo**.
2. `/locate biome thaumcraft:magical_forest` y `/tp` ahí.
3. Fijate en los colores, los robles enormes, los vishrooms al pie de los troncos y el grass_ambient (de noche tira motas).
4. Si el juego crashea al crear el mundo con "Feature order cycle", avisame: es el orden de las features del bioma.
