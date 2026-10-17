# Etapa 9b-1: el taint (bloques, contagio y semillas)

Primera parte del taint. Los bichos (crawler, enjambre, tentáculos) van en la 9b-2.

## Cómo funciona

- Todo el taint gira alrededor de las **semillas de taint**. Cada semilla se anota en una lista (`TaintHelper`) y el taint sólo vive dentro de su radio (`taintSpreadArea`, 32 bloques).
- Lejos de una semilla, el taint se muere: la roca pasa a piedra porosa, la tierra a tierra común, la costra, el géiser, el tronco y las bolas a flux goo, y las fibras desaparecen.
- La semilla usa el flux del aura para tirar fibras alrededor. Sin flux se muere de hambre. Envenena con taint a todo lo que tenga a 4 bloques (8 la gigante).
- Las fibras cubren las caras sólidas. Cuando un bloque queda rodeado de taint se convierte: troncos en tronco de taint, tierra/arena/grava/arcilla en tierra infectada, roca en roca infectada, madera/calabazas/cactus/coral/esponja/hongos gigantes en costra. Las hojas pasan a fibra o, pegadas a un tronco de taint, a **bolas** (Swollen Taint).
- Una bola arriba de un tronco de taint vertical puede volverse **géiser**, que larga flux al aura.
- En el borde del radio, con 5 o más de flux, la tierra o roca infectada puede hacer nacer una semilla nueva.
- La costra suelta se cae como bloque que cae (`EntityFallingTaint`).

## Bloques nuevos

| Bloque | Notas |
|---|---|
| `taint_fibre` | Fibras. A veces tiene un brote; el bulbo (brote 3) da luz 12, suelta un cristal de flux y larga flux al romperse. Se prende fuego. |
| `taint_crust`, `taint_soil`, `taint_rock`, `taint_geyser` | Dureza 10. Con toque de seda se sacan. La roca a veces da un cristal de flux (más con fortuna). |
| `taint_log` | Tronco de taint. Se rompe con hacha y cuenta como tronco (tag `minecraft:logs`). |
| `taint_feature` | Swollen Taint: bolas que brillan (luz 10). Al romperlas sueltan flux. |
| `flux_goo` | Flux goo: líquido finito de 8 niveles. Frena y da Flux Flu. Se evapora en flux y a veces deja fibras. |
| `stone_porous` | Piedra porosa: da grava o, a veces, algo de la lista del original (cristales, ámbar, diamante, esmeralda, redstone, prismarina, arcilla, cuarzo). |

## Efectos

- **Taint Poison** (`flux_taint`): 1 de daño de taint cada 2 segundos (más rápido con nivel). Cura a los bichos del taint, no le hace nada a los no-muertos.
- **Flux Flu** (`vis_exhaust`): los focos cuestan 10% más de vis por nivel.
- **Flux Phage** (`infectious_vis_exhaust`): igual que Flux Flu y se contagia a lo que esté a 4 bloques, un nivel menos cada vez.
- Daño nuevo `thaumcraft:taint`: ignora la armadura y es mágico.

## Otras cosas que se conectaron

- **Grieta de flux**: ahora tiene los eventos 1 (se convierte en semilla gigante, con un empujón de crecimiento igual al tamaño de la grieta), 2 (Flux Phage a todos a 16 bloques) y 3 (una nube de flux sobre el jugador más cercano). Cerca de una semilla no puede hacer el evento 1. El colapso muy inestable ahora también da Taint Poison.
- **Matriz de infusión**: los eventos de inestabilidad ponen flux goo sobre los pedestales y dan Taint Poison o Flux Flu.
- **Bottle of Taint** (`bottle_taint`): se tira como un huevo, envenena a 5 bloques y salpica goo. Receta de crisol (investigación BOTTLETAINT): frasco de Vitium + 30 Vitium + 30 Aqua.
- Huevos de spawn para las dos semillas.
- Escanear una semilla da `!TaintSeed`. Aspectos de los bloques y de las semillas como en el original.
- Config nueva (sección `world`): `taintSpreadRate` (100, de 0 a 500) y `taintSpreadArea` (32, de 8 a 256). `wussMode` frena el contagio (menos el que hace la semilla).

## Diferencias con el original

- En 1.12 el flux goo era un fluido finito de Forge. Vanilla no tiene fluidos finitos, así que es un bloque que hace la misma cuenta: cae, después se reparte con los vecinos que tienen menos, cada 30 ticks. No se puede agarrar con balde ni romper a mano (tampoco en el original).
- Los "materiales" de bloque ya no existen. Taint es una lista de clases; madera es el instrumento de bajo, roca el de bombo, y tierra/arena son tags.
- Las fibras guardan sus conexiones en el estado del bloque (en 1.12 se calculaban al dibujar). La caja de selección es la unión de las caras, no la cara que apuntás.
- El sonido al infectar un bloque ahora suena. En 1.12 el código lo pedía mal y nunca sonaba.
- La piedra porosa da un cristal de flux con 100 de flux, igual que el original (parece un error de ellos, lo dejé igual).
- Faltan las pepitas/clusters de mena en la lista de la piedra porosa (los clusters no están portados).
- Partículas aproximadas con las de vanilla hasta la etapa 10.

## Pendiente (9b-2 y 9c)

- Enjambre del géiser, crawler al romper bolas, tentáculos de la semilla.
- Slimes taumáticos del goo.
- Mobs que mueren con Taint Poison y se vuelven taint (campeones).
- Los wisps del evento 0 de la grieta.

## Cómo probarlo

1. Creativo, mundo de prueba.
2. Subí el flux del chunk: `/thaumcraft aura add flux 100`.
3. Poné una **Taint Seed** con el huevo en un bosque. Esperá: van a salir fibras, la tierra se infecta, los árboles se vuelven taint.
4. Matá la semilla: el taint de alrededor se va muriendo de a poco.
5. Tirá una Bottle of Taint al piso para ver el goo.
