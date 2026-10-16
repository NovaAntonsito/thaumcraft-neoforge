# Etapa 8b-2b: el resto de los sellos básicos

## Qué entra

Siete sellos nuevos, cada uno con su item, textura, receta e investigación (ya estaban en `golemancy.json`).

| Sello | Qué hace el golem | Necesita | Receta |
|---|---|---|---|
| **Empty** | Saca items del cofre donde está el sello y se los lleva | no Clumsy | crisol: sello en blanco + 10 Vacuos |
| **Harvest** | Cosecha los cultivos maduros del área y los vuelve a plantar | Deft + Smart | infusión: sello en blanco + semillas, caña y cactus |
| **Butcher** | Mata animales adultos del área si hay 3 o más del mismo tipo | Fighter + Smart | infusión: sello Guard + cuero, lana, carnes |
| **Guard** | Ataca a los monstruos del área | Fighter | crisol: sello en blanco + 20 Aversio + 20 Praemunio |
| **Lumber** | Tala los árboles del área, tronco por tronco | Breaker + Smart | crisol: sello Breaker + 40 Herba + 20 Sensus |
| **Breaker** | Rompe los bloques del área que pasan el filtro | Breaker | infusión: sello en blanco + hacha, pico y pala de oro |
| **Use** | Hace click en el bloque con lo que lleva en la mano | Deft + Smart | crisol: sello en blanco + 20 Fabrico + 10 Sensus + 20 Cognitio |

- **Opciones** (pestaña de la GUI):
  - Harvest: replantar, pedir semillas (este último necesita logística, 8c).
  - Use: click izquierdo, click en el aire, mano vacía, agachado, y otras.
  - Breaker: "Use metadata" (ya no hace nada en 1.21, ver abajo).

## Lo que se portó para que funcionen

- `GolemInteractionHelper`: el golem hace click con un jugador falso de NeoForge.
- `CropUtils` completo: listas de cultivos normales, "clickeables" y apilados (caña, cactus), más melón, zapallo y verruga del Nether.
- Semillas: `ThaumcraftApi.registerSeed/getSeed`. Se registran solas las semillas de 1.21 (trigo, zanahoria, verruga, etc.) y el cacao.
- `BlockUtils.breakFurthestBlock` (talar desde la punta), `destroyBlockPartially` (las grietas al romper).
- `Utils.isWoodLog`: el tag `minecraft:logs`.

## Diferencias con 1.12

- **Jugador falso:** en 1.21 necesita un UUID, así que se saca del nombre `FakeThaumcraftGolem`. NeoForge ya trae su propia conexión falsa.
- **"Animales"** (Butcher y la opción de Guard): 1.12 usaba `IAnimals`, que incluía animales, murciélagos, calamares, golems y monstruos. Acá se arma igual con las clases de 1.21.
- **Breaker:** compara el item del bloque, sin metadata. Se mantuvo un bug del original: con lista blanca, todos los items del filtro tienen que coincidir (con un solo item anda bien).
- **Cacao:** no se replanta, igual que en el original.
- **Lámpara de crecimiento:** como comparte `CropUtils`, ahora también trata al melón, al zapallo y a la verruga madura como cultivos listos (igual que el original).

## Para probar

1. Golem con Deft + Smart, sello Harvest en la tierra de un cultivo de trigo maduro. Tiene que cosechar y replantar.
2. Golem Breaker + Smart, sello Lumber al lado de un árbol.
3. Golem Fighter, sello Guard y un zombie en el área.
4. Sello Empty en un cofre con items y uno Store en otro cofre.

## Pendiente

- **8c:** sellos avanzados, Provide, Stock y logística (incluye "pedir semillas" de Harvest y Use).
