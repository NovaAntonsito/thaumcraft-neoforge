# Etapa 9d-2: los cultistas y el portal menor

Llega el Culto Carmesí. Salen de portales rojos que aparecen en la superficie, usan el equipo de la 9d-1 y a veces sueltan su libro, el Crimson Rites.

## Los bichos

| Bicho | Id | Vida | Qué hace |
|---|---|---|---|
| Crimson Knight | `cultist_knight` | 30 | Cuerpo a cuerpo con espada de hierro. Lleva el set de placas y las botas carmesí. |
| Crimson Cleric | `cultist_cleric` | 24 | Lleva la túnica (y a veces las botas). De lejos tira 3 bolas de fuego o un orbe rojo que te persigue. Canta cada tanto. |
| Lesser Crimson Portal | `cultist_portal_lesser` | 100 | No se mueve. Ver abajo. |

Todos los cultistas:
- Tienen 4 de daño base, 32 bloques de rango y abren puertas.
- Atacan a jugadores e illagers. Si le pegás a uno, todos los que estén cerca se te tiran encima.
- No se atacan entre ellos.
- Las piezas de armadura que llevan tienen 5% de chance de caer.
- En difícil el caballero a veces sale con capucha de clérigo o sin casco.
- Se pueden escanear (`!CrimsonCultist`). Tienen Alienis 5, Humanus 15 y Aversio 5.

**Orbe de energía** (`golem_orb`): va siguiendo al objetivo y hace daño mágico igual al ataque de quien lo tiró. Si le pegás, sale para donde estás mirando. Se apaga solo a los 12 segundos.

## El portal menor

- Aparece en 1 de cada 500 chunks del overworld, sobre tierra, piedra, arena o nieve. No aparece en mundos planos. Config nueva: `generateStructure`.
- Se activa cuando un jugador se acerca a 32 bloques (suena y se abre).
- Mientras te ve, cada 2 a 5 segundos larga un cultista (2 de cada 3 son caballeros). Para cuando hay 2, 4 o 6 cultistas cerca, según la dificultad.
- Cada cultista que sale le saca 5 a 9 de vida.
- Si lo tocás te da una descarga de 4.
- No le hacen nada las pociones ni el fuego. Al morir explota sin romper bloques.
- Se pone más inestable (titila) mientras pierde vida.

## Crimson Rites y las curiosidades

El Crimson Rites es el libro del culto, y llegó junto con las otras seis curiosidades del original. Las seis también se usan con click derecho y dan conocimiento:

| Ítem | Categoría |
|---|---|
| Arcane Curiosity | Auromancy |
| Preserved Curiosity | Alchemy |
| Ancient Curiosity | Golemancy |
| Eldritch Curiosity | Eldritch (+1 warp, +5 temporal) |
| Illuminating Curiosity | Infusion |
| Twisted Curiosity | Artifice |
| Crimson Rites | Eldritch |

Todas dan también algo de conocimiento en dos categorías al azar y se gastan al usarlas.

**Crimson Rites**:
- Con más de 20 de warp desbloquea la investigación Crimson Rites. También da +1 warp, +5 temporal y 50% de chance de +1 permanente.
- Con menos de 20 de warp solo dice "This book contains nothing but crazed ravings." y no se gasta.
- Se consigue matando cultistas: 1 de cada 4. Baja a 1 de cada 20 cuando ya investigaste el culto, y a 1 de cada 50 si ya llevás uno encima.
- Además está en el loot del cultista, junto con 0 a 2 pepitas de oro.

Con esto los Pech ya tradean la Illuminating Curiosity (valor 5), como en el original.

Otro cambio: la Crimson Blade ahora se repara con lingotes de metal del vacío, que ya existían.

## Diferencias con el original

- **Particulas**: son de vanilla hasta la etapa 10. La nube al salir del portal es polvo de blanco a rojo.
- **Portal**: el original estiraba el dibujo en vertical en el mundo; ahora se estira según la cámara. Mirándolo de frente se ve igual.
- **Espadas del caballero**: el 1% (5% en difícil) de caballeros con espada de thaumium o del vacío sale por ahora con espada de hierro, porque esas espadas no existen todavía.
- **Puertas**: en 1.21 no existe la IA que los mantenía adentro de las casas de noche.
- **Ritualistas**: el clérigo ritualista, que flota frente a un altar con un hilo violeta, quedó portado. En el original nada lo crea, así que no se ve en el juego. El altar eldritch tampoco existe todavía.
- **Montículos**: el generador del original también hacía montículos (1 en 100 chunks). Llegan con los jefes, en la 9d-4.
- **Pendientes**: el portal como ayuda de teoría y el guardián eldritch como enemigo de los cultistas (9d-3) todavía no están.

## Cómo probarlo

1. Creativo, sacá un huevo de Lesser Crimson Portal y alejate unos bloques en supervivencia: se abre y empiezan a salir cultistas.
2. Pegale a un caballero con otros cerca: van todos.
3. Alejate de un clérigo: te tira fuego y orbes rojos. Pegale al orbe para devolverlo.
4. Matá cultistas hasta que caiga un Crimson Rites y usalo con menos y con más de 20 de warp.
5. En un mundo nuevo, explorá: cada tanto hay un portal en la superficie.
