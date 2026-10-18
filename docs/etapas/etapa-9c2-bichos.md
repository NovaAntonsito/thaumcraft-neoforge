# Etapa 9c-2: bichos del overworld

Segunda parte de la 9c. Llegan seis mobs nuevos, con sus huevos, sonidos, aspectos y escaneos, y empiezan a aparecer solos en el mundo.

## Los mobs

| Mob | Id | Qué hace |
|---|---|---|
| Thaumic Slime | `thaum_slime` | Sale del flux goo. Te mira, y si estás a más de 4 bloques te escupe slimes chiquitos (y él se achica). Al morir se parte en slimes de tamaño 1. Suelta un cristal de flux si era grande. |
| Angry Zombie | `brainy_zombie` | Zombie de 25 de vida y 5 de daño, con +1 de armadura. No se ahoga en zombie. A veces suelta un cerebro. |
| Furious Zombie | `giant_brainy_zombie` | Zombie de 60 de vida que salta. Cada golpe que le das lo enoja más: crece, se pone rojo y pega más fuerte. Se le pasa de a poco. |
| Wisp | `wisp` | Bola de luz que vuela y te tira rayos. Tiene un aspecto al azar (casi siempre primal) que define su color y el cristal que suelta. |
| Firebat | `firebat` | Murciélago de fuego. Se cuelga de los techos y baja cuando te acercás. A veces explota al pegarte. Le hace daño el agua y la lluvia. Suelta pólvora. |
| Mind Spider | `mind_spider` | Araña casi invisible, chiquita, que vive poco. Solo la ve el jugador al que "pertenece". |

Los aspectos y los escaneos (`!Wisp`, `!ThaumSlime`, `!Firebat`, `!BrainyZombie`, `f_BAT`, `f_FLY`, `f_BRAIN`) son los del original. `f_BRAIN` también sale de escanear un cerebro de zombie.

## Dónde aparecen

Como en el original:

- **Angry Zombie**: en todos los biomas del overworld que ya tienen monstruos (peso 10). En el bioma Eerie también, junto con el Furious Zombie.
- **Firebat**: en el nether (peso 10, de a 1 o 2). El 31 de octubre también en el overworld.
- **Wisp**: en el nether (peso 5), en el Magical Forest (peso 20) y en el Eerie. Necesitan oscuridad y no más de 8 juntos.
- **Thaumic Slime**: cuando el flux goo tiene nivel 2 o más y aire arriba, cada tanto se convierte en un slime (más grande si el goo está lleno). Los slimes que pasan por goo lo absorben y crecen.
- Las grietas de flux ahora también pueden soltar wisps (uno de cada cinco es de flux).

Configs nuevas (hace falta reiniciar el mundo): `allowSpawnAngryZombie`, `allowSpawnFireBat` y `allowSpawnWisp`, todas `true` por defecto.

Los spawns son biome modifiers propios (`thaumcraft:config_spawns`) en `data/thaumcraft/neoforge/biome_modifier/spawn_*.json`, así respetan la config y la fecha.

## Diferencias con el original

- La Mind Spider no aparece sola: la creaban los eventos de warp, que todavía no están. Por ahora solo con el huevo.
- El Furious Zombie tenía un drop propio que en 1.12 nunca se usaba (los zombies usan su loot table). Lo dejé igual: suelta lo de un zombie.
- Las alas del Firebat quedan un poco abiertas después de descolgarse, igual que en el original.
- Las partículas del wisp y de los rayos son de vanilla hasta la etapa 10.

## Cómo probarlo

1. Modo creativo, buscá los huevos nuevos en la pestaña de Thaumcraft.
2. Pegale varias veces a un Furious Zombie: crece y se pone rojo.
3. Andá al nether de noche: deberían salir Firebats colgados del techo y algún Wisp.
4. Poné flux goo (por ejemplo con mucha contaminación) y esperá: cada tanto sale un Thaumic Slime.
5. Escaneá cada bicho con el thaumometer.
