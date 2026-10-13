# Etapa 6b: tutoriales con Ponder (piloto)

Prueba piloto de tutoriales animados adentro del juego con **Ponder**, la librería de Create (ahora separada, licencia MIT). Mantenés **W** sobre un ítem y se abre una escena 3D que muestra cómo se usa, con textos paso a paso.

## Qué se agregó
- **Dependencia opcional**: Ponder 1.0.69 (y Flywheel 1.0.6, que Ponder necesita en el cliente) desde `https://maven.createmod.net`. Las versiones están en `gradle.properties` (`ponder_version`, `flywheel_version`).
  - Se compila contra Ponder (`compileOnly`) y en desarrollo se carga solo (`localRuntime`). No se mete en el jar del mod.
  - En `neoforge.mods.toml` figura como dependencia `optional`, del lado del cliente.
  - Sin Ponder instalado el mod carga igual: `ThaumcraftClient` registra el plugin solo si `ModList` dice que está `ponder`.
- **Plugin** en `thaumcraft.client.ponder`:
  - `ThaumcraftPonderPlugin`: registra las escenas (`PonderIndex.addPlugin`).
  - `ThaumcraftPonderScenes`: las tres escenas.
- **Tres escenas**:
  1. **Crisol** (ítem: crisol). Calor abajo (bloque de magma), se llena con agua, hierve, se tiran dos cristales de vis que se disuelven en aspectos, y con un lingote de hierro sale un lingote de taumio.
  2. **Altar de infusión** (ítems: matriz rúnica, pedestal arcano, pilar). Matriz sobre el pedestal, piedras arcanas en las esquinas, salis mundus las vuelve pilares, el guantelete despierta la matriz, pedestales y velas en pares simétricos, qué pasa si falta la pareja, y arranque de la infusión.
  3. **Fundición** (ítems: fundición, alambique, tubo, fuelles). Combustible e ítems, el alambique arriba, un tubo que baja a un frasco que se va llenando, dos fuelles (costado y atrás) y la pérdida de flux.
- **Estructuras** en `src/main/resources/assets/thaumcraft/ponder/` (`crucible.nbt`, `infusion_altar.nbt`, `smelting.nbt`). Las genera `tools/gen_ponder_nbt.py`, no hace falta abrir el juego. Si cambiás una, editá el script y corré `python tools/gen_ponder_nbt.py`.
- **Textos** en inglés en `en_us.json`, con las claves de Ponder: `thaumcraft.ponder.<escena>.header` y `thaumcraft.ponder.<escena>.text_N`.

## Cómo probar
1. `gradlew runClient` (Ponder y Flywheel ya vienen en el entorno de desarrollo).
2. Entrá a un mundo en creativo.
3. Buscá el **crisol** en el inventario, pasá el mouse por arriba y **mantené W**. Se abre la escena.
4. Repetí con la **matriz rúnica** y con la **fundición de esencia**.
5. En la escena, con las flechas o los botones de abajo pasás de paso (los fotogramas clave).

## Diferencias con el original
- TC6 no tenía nada de esto: lo más parecido es el Thaumonomicon. Ponder no lo reemplaza, lo complementa.
- Las escenas no ejecutan la lógica real (el servidor no corre en Ponder). Se simula: los ítems tirados al crisol se borran y se ponen los aspectos a mano, el frasco se llena por pasos, etc.

## Limitaciones
- Ponder hace "tick" a los block entities del lado del cliente. Los efectos del crisol y de la matriz son de cliente, así que deberían andar, pero las partículas de `ClientFX` podrían no verse o salir en el mundo real en vez de la escena. Falta probarlo a ojo.
- Al alambique no se le pone esencia en la escena, solo al frasco.
- Si cambian los textos en el código hay que actualizar `en_us.json` a mano (fuera del modo edición, Ponder lee el archivo de idioma).

## Pendiente
- Revisar las escenas en el juego y ajustar cámara, tiempos y posiciones.
- Traducciones (es_ar y demás).
- Más escenas: mesa arcana, tubos (válvula, filtro, búfer), centrífuga, investigación.
- Etiquetas (tags) de Ponder para agrupar los temas de Thaumcraft en el índice.
