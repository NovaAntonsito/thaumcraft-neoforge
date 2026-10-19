# Versión soñada 4: varitas con núcleo y casquete (de TC4)

Cuarto agregado del branch `sonada`. Vuelven las varitas de Thaumcraft 4, montadas sobre el caster de TC6: usan los mismos focos, pero lanzan con el vis que guardan adentro en vez de sacarlo del aura.

## Cómo funcionan

- **Varita** (`wand`): un solo ítem. El núcleo y el casquete se guardan en un data component (`wand_parts`).
- **Vis guardado**:
  - Lanza los focos con el vis que tiene, no con el del aura.
  - Mientras la tenés en la mano, saca 1 de vis por segundo del aura y se recarga sola.
  - También la recargan el pedestal de recarga y el amuleto de vis, porque es un ítem recargable de TC6.
  - Si no le alcanza el vis, dice "The wand does not have enough charge to do that." (el texto de TC4).
- **Núcleo**: define cuánto vis guarda (los mismos números que TC4) y casi toda la complejidad de foco que acepta.
- **Casquete**: cambia el costo de vis de cada lanzamiento y suma complejidad.
- **Complejidad máxima** = núcleo + casquete. Si el foco montado es más complejo, la varita no lo lanza y avisa en rojo. El tooltip también lo avisa.
- **El guantelete sigue siendo lo máximo**: saca vis directo del aura y acepta cualquier foco.
- **Núcleos primales** (obsidiana, blaze, hielo, cuarzo, hueso, junco): si les queda menos del 10%, ganan 1 de vis cada 200 ticks sin tocar el aura, como `WandRodPrimalOnUpdate` de TC4.
- **Núcleo de blaze**: brilla, como en TC4.
- **HUD**: con una varita en la mano, el dial muestra el vis de la varita en vez del aura.
- **Barra del ítem**: violeta, muestra cuánto vis tiene.

## Números

| Núcleo | Vis | Complejidad |
|---|---|---|
| Madera (palo) | 25 | 6 |
| Greatwood | 50 | 10 |
| Primales (×6) | 75 | 16 |
| Silverwood | 100 | 26 |

| Casquete | Costo | Complejidad |
|---|---|---|
| Hierro | +10% | 2 |
| Cobre | +5% | 3 |
| Oro | igual | 5 |
| Plata | −5% | 8 |
| Thaumium | −10% | 12 |
| Vacío | −20% | 16 |

Para comparar, los focos aceptan hasta 15, 25 y 50 de complejidad. Una varita de madera con hierro (8) solo lanza focos simples, y la mejor (silverwood con vacío) llega a 42.

## Cómo se consiguen

- **Casquete de hierro y varita de madera con hierro**: con crafteo normal, como en TC4.
- **Casquetes de oro, cobre, plata, thaumium y vacío**: en la mesa arcana, con 7 pepitas en U.
- **Plata, thaumium y vacío**: salen inertes y hay que cargarlos por infusión con sal mundus, como en TC4.
- **Núcleo de greatwood**: en la mesa arcana, con 2 troncos de greatwood en diagonal.
- **Núcleos primales y de silverwood**: por infusión.
  - Los primales llevan cristales de vis de praecantatio y de su primal en vez de los fragmentos de TC4.
  - El de silverwood lleva un cristal de praecantatio y uno de cada primal.
- **Armar la varita**: en la mesa arcana, con un casquete, el núcleo y otro casquete en diagonal. Las 54 combinaciones son recetas JSON.
  - Vis = costo del núcleo × costo del casquete × 2, con los costos de TC4. La de silverwood con vacío sale 162.
  - TC4 cobraba eso mismo en cada uno de los 6 primales, o sea 486 en total, y eso no entra en el aura de TC6.
- **Investigación**: 9 entradas nuevas en Auromancia, a la izquierda del guantelete:
  - WANDS;
  - CAP_gold, CAP_copper, CAP_silver, CAP_thaumium y CAP_void;
  - ROD_greatwood, ROD_primal y ROD_silverwood.

  Los textos son los de TC4, cambiados donde hablaban de "cada tipo de vis" o de nodos.

## Diferencias con TC4

- **Un solo vis**: TC4 guardaba los 6 primales por separado. Acá el vis es uno solo, como en TC6.
- **Cobre y plata**: en TC4 hacían descuento solo en algunos primales. Como acá hay un solo vis, quedan en +5% y −5%, que es más o menos el promedio.
- **Los 6 núcleos primales van juntos** en una investigación (ROD_primal), no en una cada uno.
- **Falta**:
  - los bastones (staves) y el cetro;
  - el ornamento del foco: el foco se ve como el cubo de color de TC4, sin el adorno de cada foco.
- **Recarga en la mano**: en TC4 se cargaban con los nodos. Hasta que estén los nodos (agregado 5), se cargan solas del aura.

## Assets

Las texturas son todas de TC4:
- `textures/models/wand_rod_*.png`, `wand_cap_*.png` y `wand.png` para el modelo 3D;
- `textures/items/wand_rod_*.png` y `wand_cap_*.png`, con las animaciones de los casquetes cargados.

El modelo es `ModelWand` de TC4, pasado a `ModelPart`.

## Para ajustar en el juego

- Las posiciones de la varita en la mano, en el inventario y en el suelo están en `models/item/wand.json` y las puse a ojo.
- Compila, pero no lo probé en el juego.

## Archivos

- **Código**:
  - `common/items/casters/wands/`: `WandParts` (núcleos y casquetes) e `ItemWand`;
  - `client/renderers/item/WandRenderer.java`;
  - `CasterHud`: muestra el vis de la varita;
  - `ConfigItems`, `TCDataComponents` y `ThaumcraftClient`: los registros.
- **Datos**:
  - las recetas: `wand_*.json`, `arcane/wand_*.json` e `infusion/wand_*.json`;
  - las entradas en `research/auromancy.json`;
  - los textos en `en_us.json`.

## Cómo probarlo

1. En creativo, en la pestaña de Thaumcraft, están las varitas llenas: una por núcleo y la de silverwood con vacío.
2. Montale un foco con la F y lanzalo: baja el vis de la varita, no el del aura.
3. Probá un foco complejo en la de madera: no lo lanza y avisa.
4. Sostenela un rato: se recarga del aura.
