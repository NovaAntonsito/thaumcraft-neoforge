# Etapa 9e-3: baubles con Curios

Última parte del equipo. Los baubles ahora se llevan en las ranuras de Curios: amuleto, anillo, cinturón, cabeza y amuleto de bolsillo (charm).

## Antes de compilar

- **Curios es obligatorio**. Gradle lo baja solo del repo de Illusive Soulworks la primera vez que compilás.
- La versión está en `gradle.properties` (`curios_version=9.5.1+1.21.1`). No la pude probar desde acá, porque ese repo está bloqueado en mi entorno. Si Gradle no la encuentra, cambiá ese número por la última `+1.21.1` que aparezca en https://maven.theillusivec4.top/top/theillusivec4/curios/curios-neoforge/.
- En el juego, la pestaña de Curios aparece en el inventario. Thaumcraft le pide a Curios las 5 ranuras para el jugador.

## Baubles básicos

Mesa de crafteo normal (investigación Basic Infusion los muestra):

| Bauble | Receta | Ranura |
|---|---|---|
| Amuleto mundano | hilo y lingote de latón | amuleto |
| Anillo mundano | 8 pepitas de latón | anillo |
| Cinturón mundano | cuero y lingote de latón | cinturón |
| Amuleto elegante | hilo, diamante y lingote de oro | amuleto |
| Anillo elegante | pepitas de oro y diamante | anillo |
| Cinturón elegante | cuero, diamante y lingote de oro | cinturón |
| Anillo del aprendiz | solo en botín y trueques de pech | anillo |

- El anillo del aprendiz baja 5% el costo de vis. Los demás no hacen nada solos: son la base de las infusiones.

## Baubles mágicos

| Bauble | Cómo se hace | Qué hace |
|---|---|---|
| Piedra de vis | botín y pech | Recarga 1 punto cada 2 segundos al primer item que lo acepte: barra rápida, baubles, armadura. |
| Amuleto de vis | infusión (Vis Amulet) | Igual, pero 4 veces por segundo. |
| Anillo pisanubes | infusión (Cloudstepper Ring) | Un salto extra en el aire. Las caídas lastiman mucho menos. |
| Cinta de la curiosidad | infusión (Headband of Curiosity) | Va en la cabeza y se ve puesta. Te quita la mitad de la experiencia de cada orbe, y a cambio a veces te da conocimiento en una categoría al azar. |
| Amuleto del corazón verde | infusión (Verdant Charms) | Necesita carga (máximo 200). Te saca marchitamiento, veneno y corrupción de flux. |
| Corazón verde dador de vida | otra infusión sobre el anterior | Además te cura 1 de vida. |
| Corazón verde sustentador | otra infusión sobre el anterior | Además te da aire bajo el agua y comida. |
| Perla del vidente del vacío | infusión (Voidseer's Pearl) | Baja el costo de vis hasta 25% según tu warp permanente (100 da el máximo). Da 1 de warp por cada 5%. |
| Amuleto de no-muerte | infusión (Charm of Undying): tótem de la inmortalidad y plancha de latón | Funciona como el tótem pero puesto. Se gasta. |

## Gafas, bolsa de focos y lo demás

- **Gafas de revelación**: también van en la ranura de la cabeza, y se dibujan sobre la cara (más afuera si tenés casco). Funcionan igual que puestas como casco.
- **Bolsa de focos**: va en la ranura del cinturón. Al cambiar de foco, primero busca en la bolsa puesta y después en el inventario, como en el original. El menú radial de focos también la mira.
- Ahora cuentan también los baubles para:
  - el descuento de vis,
  - el warp del equipo,
  - ver con gafas (frascos, matriz, etc.).
- **Encantamientos de infusión**: los que piden "bauble", "amuleto", "anillo", "cinturón" o "recargable" ya aceptan esos items.

## Pepitas de carne y golosina triple

El amuleto sustentador pide una golosina triple de carne, y no estaba portada. Llegó con esta etapa:

- **Pepitas de carne** (vaca, pollo, cerdo, pescado, conejo, oveja): salen de regalo al cocinar carne en el horno infernal.
- **Golosina triple de carne**: azúcar más tres pepitas distintas, en la mesa de crafteo. Llena 6 y a veces da regeneración. Los lobos también la comen.

## Otras cosas

- **Arreglo de la 9e-2**: las botas del viajero no te frenaban el daño por caída. En el original sí. Ya está.
- **Trueques de pech**: ahora están completos, salvo el ojo arcano.
  - Recolector: clústeres nativos y herramientas de thaumium.
  - Mago: túnicas y la piedra de vis.
  - Arcano: el anillo del aprendiz.
- **Bolsas de botín**: ahora pueden traer la piedra de vis y los baubles básicos.

## Diferencias con el original

- **Ranuras**: Baubles tenía 7 ranuras fijas. Curios usa ranuras con nombre. La de "amuleto" es la `necklace` de Curios. El amuleto de bolsillo es la ranura `charm`.
- **Items separados**: en 1.12 los baubles básicos eran un item con 7 variantes, y la piedra de vis y el amuleto de vis eran un mismo item. Acá cada uno es su propio item.
- **Dibujo en la cabeza**: Baubles tenía funciones para ubicar cosas en la cara. Acá se ubican sobre la cabeza del modelo, con el mismo tamaño que en el original.
- **Clústeres de estaño, plata y plomo**: el pech recolector los cambiaba solo si otro mod agregaba esos lingotes. Acá quedan afuera.
- **Botín de mazmorras**: el original metía baubles en los cofres de mazmorras. Eso todavía no está portado.

## Cómo probarlo

1. Date todo con `/thaumcraft research @s all`.
2. Abrí el inventario y la pestaña de Curios. Ponete las gafas en la ranura de la cabeza y mirá un frasco con esencia.
3. Hacé un anillo mundano, infusionalo en anillo pisanubes y ponételo. Saltá y volvé a apretar salto en el aire.
4. Poné una bolsa de focos con focos en la ranura del cinturón. Con el bastón en la mano, apretá F y cambiá de foco.
5. Cociná carne en el horno infernal hasta juntar 3 pepitas distintas. Hacé la golosina triple.
6. Ponete el amuleto de no-muerte y tirate de muy alto en supervivencia.
