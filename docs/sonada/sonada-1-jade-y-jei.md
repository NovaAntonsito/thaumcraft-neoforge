# Versión soñada 1: Jade y aspectos en JEI/EMI

Primer agregado del branch `sonada`. No está en Thaumcraft 6: es lo que hacían Waila y NEI con TC4. Las dos integraciones son opcionales: si no tenés JEI o Jade, el mod carga igual.

## JEI (y EMI)

- **Aspectos como ingredientes**:
  - Los 48 aspectos aparecen en la lista de JEI, con su ícono y su color.
  - Se buscan por nombre: escribí `ignis` y aparece.
  - Al pasar el mouse muestran el nombre, la descripción y de qué dos aspectos están hechos (o "Primal aspect").
- **"¿Qué tiene Ignis?"**: con R (recetas) sobre un aspecto aparece la categoría **Aspect Sources**:
  - todos los ítems que tienen ese aspecto, ordenados de más a menos;
  - la cantidad está en chiquito en la esquina de cada ítem;
  - 45 ítems por página, y si hay más se parte en varias.
- **Aspectos de un ítem**: con U (usos) sobre un ítem aparecen las páginas de los aspectos que tiene.
- **Combinaciones**: con U sobre un aspecto aparece **Aspect Combination**, con los compuestos que forma ("Ignis + Aer = Lux"). Con R sobre un compuesto ves de qué está hecho.
- **Catalizadores**: el taumómetro abre Aspect Sources y el Thaumonomicon abre las combinaciones.
- **EMI**: EMI carga los plugins de JEI, así que lo mismo se ve en EMI sin hacer nada más.

## Jade

- **Esencia**:
  - Frascos, tubos, crisol, alambiques y todo lo que guarda o mueve esencia muestra sus aspectos con la cantidad, como el tooltip de los ítems.
  - Si el frasco tiene etiqueta, dice cuál ("Label: Ignis").
  - Agachado (o con la tecla de detalles de Jade), los tubos muestran la succión y de qué aspecto.
- **Con goggles de revelación puestas**:
  - El vis y el vis base del chunk ("Vis: 245.3 / 300"), y el flux si hay.
  - Los aspectos del bloque que mirás, solo si ya lo escaneaste.
- Las tres cosas se prenden y apagan en la config de Jade (Essentia, Vis and Flux, Block Aspects).

## Para tener en cuenta

- **Spoilers en JEI**: JEI muestra los aspectos de todos los ítems, aunque no los hayas escaneado. El tooltip de los ítems sigue escondiéndolos hasta que escanees. JEI arma su lista una sola vez al entrar al mundo, por eso no puede seguir lo que vas escaneando.
- **Versiones**: en `gradle.properties` puse `jei_version=19.21.0.247` y `jade_version=15.10.3+neoforge`. No pude chequear que existan esos números exactos. Si Gradle no los encuentra, cambialos por los que figuran en la página de cada mod para 1.21.1.
- Lo compilé contra las APIs de JEI y Jade, pero no lo probé en el juego.

## Archivos

- `compat/jei/`: ThaumcraftJeiPlugin, AspectIngredient, AspectSourcesCategory y AspectCombinationCategory.
- `compat/jade/`: ThaumcraftJadePlugin, EssentiaData y EssentiaComponent, AuraData y AuraComponent, BlockAspectsComponent y AspectsElement.
- `ThaumcraftClient`: los aspectos de los ítems se calculan antes de que cargue JEI.
- `build.gradle`, `gradle.properties` y `neoforge.mods.toml`: JEI y Jade como dependencias opcionales.

## Cómo probarlo

1. Abrí el inventario y buscá `ignis` en JEI: aparece el aspecto.
2. Apretá R sobre Ignis: ves los ítems que lo tienen. Apretá U: ves los compuestos que forma.
3. Mirá un frasco con esencia: Jade muestra el aspecto y la cantidad.
4. Ponete las goggles: Jade muestra el vis y el flux del chunk.
