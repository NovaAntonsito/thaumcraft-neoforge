# Etapa 9c-3: el Pech

Última parte de la 9c. Llega el **Pech**: el bichito del Magical Forest con la mochila gigante, que junta cosas, se enoja en grupo y, si lo amansás, te tradea.

## Los tres tipos

Según lo que tenga en la mano cuando nace:

| Tipo | Con qué nace | Cómo pelea |
|---|---|---|
| Pech Forager | Espada, hacha, pico, caña o nada | Cuerpo a cuerpo |
| Pech Mage | Varita del Pech (1 de cada 10) | Tira focos al azar: maldición, flux, tierra, aire o fuego |
| Pech Stalker | Arco (1 de cada 4) | Flechas. 1 de cada 5 envenena |

Tienen 30 de vida, 6 de daño, +2 de armadura y se curan 1 cada 2 segundos. Cada tipo tiene su textura y sus aspectos (Man, Aura, Exchange y Desire, Aversion o Magic).

## Cómo se comportan

- **Te esquivan**: si no son mansos se alejan de los jugadores.
- **Juntan todo**: caminan hasta los ítems tirados a 16 bloques y los guardan en la mochila (9 lugares). Si se mueren, cada cosa de la mochila tiene 1 en 3 de caerse. Un Pech con 5 o más cosas en la mochila no desaparece.
- **Se enojan en grupo**: si le pegás a uno, se enojan todos los Pech a 32 bloques (menos en creativo). Se les pasa en 20 a 40 segundos.
- **Murmuran**: se les mueve el cachete cuando hablan, y charlan entre ellos si hay otro cerca.

## Amansarlos y tradear

1. Tirale algo que valore: perlas de ender (valor 15) o cualquier cosa con más de 1 de Desire (oro, diamantes, esmeraldas...). Se lo come, y con una chance de "valor en 10" queda manso (sale una lluvia verde).
2. Click derecho a un Pech manso abre su ventana.
3. Poné algo valioso a la izquierda y tocá los dados (se iluminan cuando se puede).
4. Te da cosas por más o menos lo que vale lo que le diste, de su lista o a veces de su mochila. Hay una chance de valor / 2 en 100 de que se canse y deje de ser manso.
5. Lo que quede en la ventana al cerrarla cae a tus pies, y los Pech no lo vuelven a agarrar.

Lo que tradea cada tipo, como en el original:

- **Forager**: varas de blaze, retoños de greatwood y silverwood, aliento de dragón, brújula, frascos de experiencia, manzanas doradas, flecha espectral y tótem.
- **Mage**: cristales de los 6 primales, de flux y de aura, pociones de regeneración y curación II, frascos de experiencia, manzanas doradas y la varita del Pech.
- **Stalker**: velas de colores, lágrimas de ghast, libros de Power, Flame e Infinity, frascos de experiencia y manzanas doradas.

## Varita del Pech

`pech_wand`, rara. Click derecho: si ya conocés lo básico de auromancia se gasta y te da algo de observación y teoría en categorías al azar. Si no, te dice "You cannot fathom the workings of this yet." Escanearla habilita la investigación del foco de maldición.

## Dónde aparecen

En los biomas mágicos con monstruos (peso 10) y en el Magical Forest (peso 20, de a 1 o 2). Necesitan oscuridad y no más de 4 juntos. Config nueva: `allowSpawnPech` (hace falta reiniciar el mundo).

## Diferencias con el original

- Hay ítems de las listas que todavía no existen: clusters nativos, herramientas de thaumium, túnicas de tela, el fragmento de conocimiento, el amuleto de vis, el bauble y el ojo eldritch. Mientras tanto un Forager no da nada en el valor 1 y un Mage no da nada en el valor 4. El original elegía al azar hasta encontrar algo de ese valor; ahora, si no hay nada, lo saltea (si no se quedaba colgado).
- El loot de la muerte trae las pepitas de oro, pero no el fragmento de conocimiento raro (todavía no existe).
- Las pociones viejas 8193 y 8261 ahora son regeneración y curación II.
- Igual que en el original, si te da dos veces lo mismo a veces aparece repetido en otro lugar. Lo dejé así.

## Cómo probarlo

1. Creativo, huevo de Pech (pestaña de Thaumcraft). Sacá varios: salen con distintas cosas en la mano.
2. Pasate a supervivencia y tirale perlas de ender hasta que salgan partículas verdes.
3. Click derecho, poné una perla en el lugar de la izquierda y tocá los dados.
4. Pegale a uno con otros cerca: se enojan todos.
5. Escaneá el Pech y la varita con el thaumometer.
