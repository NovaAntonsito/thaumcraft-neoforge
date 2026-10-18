# Etapa 9c-1: campeones, mobs con taint y bolsas de botín

Primera parte de la 9c. Los monstruos ahora pueden nacer **campeones**, los mobs que se mueren de Taint Poison pueden quedar **tainted**, y los campeones sueltan **bolsas de botín**.

## Campeones

Cada mob tiene dos atributos nuevos: `thaumcraft:mobmod` (el modificador de campeón) y `thaumcraft:mobmodtaint`. El primero vale -2 al nacer, -1 si es un mob normal, de 0 a 12 si es campeón y 13 si está tainted. Se sincroniza con el cliente para mostrar las partículas.

Cuando un monstruo aparece, se tira la chance (igual que el original):

- Base: un número de 0 a 99. Tiene que llegar a 0 o menos.
- En fácil (o con los campeones apagados) +2. En difícil -2.
- En biomas spooky, del nether o del end -2 (-1 con los campeones apagados).
- Según el mob se resta (nivel - 1): zombie, araña, blaze, enderman y esqueleto nivel 0; bruja 1; taintacle 2.
- Además tiene que estar en esa lista y tener 10 o más de vida base.

Un campeón tiene +100 de vida, el doble de daño, se cura 25 al nacer y se llama "Modificador Nombre" (por ejemplo *Fiery Zombie*). Los creepers siempre salen *Bold*. El Giant Taintacle ahora nace siempre campeón.

| Id | Modificador | Qué hace |
|---|---|---|
| 0 | Bold | +30% de velocidad |
| 1 | Spined | Al que le pega le devuelve 1 a 3 de daño de espinas |
| 2 | Armored | Recibe 24% menos de daño (salvo daño que atraviesa armadura) |
| 3 | Mighty | Otro doble de daño |
| 4 | Grim | 40% de dar Wither 10 segundos |
| 5 | Warded | Nace con absorción (media vida base) y se recarga 1 cada 25 ticks |
| 6 | Warped | 33% de darte 1 a 3 de warp temporal |
| 7 | Undying | Se cura 1 por segundo |
| 8 | Fiery | 40% de prenderte fuego 4 segundos |
| 9 | Sickly | 40% de darte Hunger 25 segundos |
| 10 | Venomous | 40% de darte Poison 5 segundos |
| 11 | Vampiric | Se cura la mitad del daño que hace (mínimo 2) |
| 12 | Infested | 40% de soltar un Tainted Crawler cuando le pegás |
| 13 | Tainted | Ver abajo |

Cada uno tiene sus partículas alrededor del mob.

Al morir por un jugador, un campeón suelta 5 a 7 de experiencia y una bolsa de botín. La rareza sale de `(0..8 + looting) / 5`, con máximo rara.

Config nueva: `allowChampionMobs` (por defecto `true`).

## Mobs tainted

Si un mob que no es jugador, no-muerto, constructo ni bicho del taint tiene Taint Poison y queda con menos de 2 de vida, tiene 50% de chance de no morirse y quedar **tainted**:

- +25 de vida, +0.5 de daño, se cura 25.
- El Taint Poison lo cura en vez de lastimarlo.
- Se le dibujan fibras de taint encima (`LayerTainted`, con la textura `models/taint_fibres.png`).
- Si es un bicho pacífico (vaca, oveja, cerdo...) se vuelve agresivo: le pega a los jugadores. El daño es su atributo de ataque o, si no tiene, `max(2, (alto + ancho) * 2)`.
- No suelta bolsa de botín.

## Bolsas de botín

Tres ítems nuevos: `loot_bag_common`, `loot_bag_uncommon` y `loot_bag_rare` (de a 16). Click derecho: se abren y tiran de 8 a 12 cosas, con el sonido de monedas. Aspectos: Desiderium 10, 20 y 30.

Lo que traen es la lista del original: pepitas de oro (lo más común), salis mundus, frutas chorus, brújula, galletas, perlas primordiales gastadas, estrella del nether, diamantes, esmeraldas, lingotes de oro, perlas de ender, frascos de experiencia, manzanas doradas (normales y encantadas), libros encantados y todas las pociones. Las bolsas no comunes y raras a veces traen armadura o un arma encantada.

También: los zombies que matás tienen 10% de soltar un cerebro (más con looting).

## Diferencias con el original

- Los tintes de color que algunos modificadores le ponían al mob (Fiery, Grim, Venomous, Vampiric) no están todavía. Se ven solo las partículas, que son polvo de color hasta la etapa 10. Fiery tira llamas de vanilla.
- Las fibras de taint no se mueven ni se repiten como en el original: se dibujan quietas sobre el modelo.
- Los 1.12 usaban UUIDs para los modificadores de atributo; ahora son ids `thaumcraft:champion_*`.
- El ataque de los bichos tainted no suma encantamientos ni rompe escudos.
- Como en el original, los efectos al pegar (Grim, Fiery, etc.) solo se aplican cuando el campeón le pega a otro monstruo. A los jugadores no les llegan. Lo dejé igual.
- El taintacle y el crab estaban en la lista del original con un nombre que 1.12 probablemente no reconocía. Acá el taintacle sí puede salir campeón.

## Pendiente

- El escudo rúnico que se ve al pegarle a un Warded (etapa 10).
- Baubles, amuleto de vis y equipo de thaumium/void metal en las bolsas (vienen con esos ítems).
- Los jefes (`EntityThaumcraftBoss`) con su propio nombre, y el bonus de campeón en las Outer Lands.
- El Brainy Zombie no debería soltar el cerebro extra (llega en la 9c-2).

## Cómo probarlo

1. Mundo en **difícil**.
2. Andá de noche a un bioma con muchos monstruos. Más o menos 1 de cada 30 zombies o esqueletos sale con nombre (por ejemplo *Vampiric Skeleton*) y partículas.
3. Matalo: suelta una bolsa. Click derecho para abrirla.
4. Para ver un mob tainted: `/effect give @e[type=cow,limit=1,sort=nearest] thaumcraft:flux_taint 60 0` cerca de una vaca. Cuando le queda poca vida puede quedar con fibras violetas y empezar a perseguirte.
5. Probá `allowChampionMobs = false` en la config: casi no salen campeones.
