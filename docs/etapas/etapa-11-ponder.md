# Etapa 11: más tutoriales de Ponder

Sigue lo pendiente de la etapa 6b. Ahora hay ocho escenas y dos etiquetas en el índice de Ponder.

## Escenas nuevas

Mantené apretada la W sobre el ítem para verlas.

- **Mesa arcana**: sal de mundus sobre una mesa de crafteo, el vis que sale del aura del chunk y los cristales en los seis huecos.
- **Mesa de investigación**: herramientas de escritura sobre la mesa de madera, la nota, los hexágonos, la tinta y los aspectos gratis que dan los alrededores (agua, tierra, redstone).
- **Tubos de esencia**: qué es la succión, el frasco con etiqueta que tira más fuerte, la válvula (clic o redstone) y el filtro (clic con una ampolla llena, agachado para borrarlo).
- **Tubos especiales**: el tubo restringido (corta la succión a la mitad), el direccional (se gira con el guantelete) y el búfer (guarda hasta 10 y cada lado se ahoga con el guantelete).
- **Centrífuga**: chupa esencia compuesta desde abajo y saca por arriba uno de sus dos componentes al azar. Los primales no se parten, y la redstone la frena.

Las dos escenas de tubos usan la misma estructura. Solo cambia el tubo del medio.

## Etiquetas del índice

- **Arcane Crafting**: mesa arcana, mesa de investigación, crisol y altar de infusión.
- **Essentia Handling**: fundidora, alambique, fuelle, frascos, todos los tubos y la centrífuga.

## Datos que salen del original

Los números de las escenas salen del código original, no los inventé:

- el búfer guarda 10,
- el restringido corta a la mitad,
- la centrífuga elige al azar entre los dos componentes,
- el filtro se pone con una ampolla y se borra agachado.

## Compilación

Ahora compilo contra el Ponder real, que traje de tu caché de Gradle. Antes las escenas no se chequeaban.

## Diferencias con el original

- Thaumcraft 6 no tenía Ponder: todo esto es un agregado del port. Los textos están en inglés, en `en_us.json`, bajo `thaumcraft.ponder.*`.
- **Mesa de investigación**: la escena explica la mesa del port, que es la de TC4 (Research Port: Reborn), con notas de hexágonos.

## Pendiente

- Revisarlas en el juego y ajustar cámara y tiempos.
- Los frascos de la escena se llenan "a mano" (la escena les cambia la cantidad). Si el mundo de Ponder también corre la lógica de los tubos, quizás se vea algún salto. Avisame si lo notás.
- Traducciones.
