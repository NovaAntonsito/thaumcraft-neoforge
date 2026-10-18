# Etapa 9b-2: los bichos del taint

Segunda parte del taint. Cierra los pendientes de la 9b-1.

## Bichos nuevos

| Bicho | Id | Qué hace |
|---|---|---|
| Tainted Crawler | `taint_crawler` | Gusanito (modelo de lepisma al 70%). Sale de las bolas de taint al romperlas (1 de cada 3 veces, si no sueltan flux). Cada 2 segundos deja una fibra de taint donde está. En normal y difícil su mordida puede dar Taint Poison. 8 de vida. Cuenta como artrópodo (Bane of Arthropods le pega más). 1 en 8 de soltar un cristal de flux. |
| Taintacle | `taintacle` | Tentáculo de 3 bloques, no se mueve. 50 de vida, 7 de daño. Se muere de hambre fuera del taint. Si su objetivo está a entre 4 y 16 bloques parado sobre taint, le hace salir tentáculos chicos al lado. Suelta un cristal de flux. |
| Taint Tendril | `taintacle_tiny` | Tentáculo chico (5 de vida, 2 de daño). Lo sacan los taintacles y las semillas. Se muere solo a los 10 segundos. No suelta nada. |
| Taint Swarm | `taint_swarm` | Enjambre volador. Sale de los géiseres (20% de las veces, si hay un jugador a 32 bloques y ningún enjambre a 32). Da vueltas cerca de las semillas y va por los jugadores a 8 bloques. Muerde con Weakness y sin empujar. 30 de vida. Mitad de chance de soltar un cristal de flux. |
| Giant Taintacle | `taintacle_giant` | Jefe: tentáculo de 6 bloques, 175 de vida, 9 de daño, barra violeta que oscurece el cielo. Se cura 1 cada 30 ticks. Un golpe de más de 35 se corta en 35 y lo enfurece 10 segundos (regeneración, fuerza y prisa). El último vivo en 48 bloques suelta una Primordial Pearl. No desaparece y respira bajo el agua. |

Todos tienen huevo de spawn menos el Taint Tendril (igual que el original).

## Otras cosas

- Las **semillas de taint** ahora sí sacan tentáculos chicos bajo su objetivo.
- El enjambre es invisible: lo que se ve son sus partículas (`FXSwarm`), portadas con su movimiento original. Zumban cerca del jugador. Las 8 imágenes salen de `textures/misc/particles.png`. La primera está vacía en el original, por eso el bicho titila.
- Sonidos nuevos: `swarm`, `swarmattack` y `fly` (con sus ogg del original).
- Escaneos: `!TaintCrawler`, `!Taintacle`, `!TaintSwarm` y el enjambre cuenta para `f_FLY`.
- Aspectos de los bichos como en el original. El crawler no tiene (tampoco en el original).
- Tags: el gigante en `c:bosses` y `can_breathe_under_water`; el crawler en `arthropod` y `sensitive_to_bane_of_arthropods`.

## Diferencias con el original

- El modelo del taintacle escalaba cada pedazo dentro del anterior. `ModelPart` no puede, así que la cadena se dibuja a mano con las mismas cuentas (igual que la semilla).
- El crawler en 1.12 comparaba posiciones mal y siempre pasaba el chequeo. Acá no está ese chequeo, que es lo mismo.
- Las partículas al nacer un tentáculo son pedacitos de slime y del piso (sin el tinte violeta hasta la etapa 10).
- La perla del gigante sale como ítem común que sube un poco. En el original era un ítem especial que flotaba.

## Pendiente

- El gigante nace campeón (`makeChampion`) y no tiene dónde aparecer: van con la 9c y las Outer Lands.
- En el bioma eldritch los tentáculos salen aunque no haya taint (Outer Lands).
- Un enjambre "invocado" marcaba a la víctima como golpeada por un jugador (no lo invoca nada todavía).

## Cómo probarlo

1. Mundo de prueba **en fácil o más** (en pacífico los monstruos desaparecen).
2. `/thaumcraft aura add flux 100` y poné una Taint Seed. Acercate parado sobre taint: te salen tentáculos chicos.
3. Rompé bolas de taint (Swollen Taint) hasta que salga un crawler. Fijate que deja fibras.
4. Poné huevos de Taintacle, Taint Swarm y Giant Taintacle cerca del taint.
5. Al gigante pegale fuerte (más de 35) para verlo enojarse.
