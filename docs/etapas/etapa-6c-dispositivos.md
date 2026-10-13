# Etapa 6c: lámparas, horno infernal, condensador y dispositivos de vis

Esta etapa suma los dispositivos de artificio que faltaban: las tres lámparas, el horno infernal, el condensador de flux con su enrejado, el estabilizador con la incrustación de redstone, el generador y la batería de vis, el pedestal de recarga y la dioptra.

## Lámparas
- **Lámpara arcana** (ARCANELAMP, mesa arcana): se pega a paredes, piso o techo. Da luz 15 y pone luces invisibles hasta 16 bloques donde esté oscuro. Al sacarla, esas luces desaparecen. Con redstone se apaga.
- **Lámpara de crecimiento** (LAMPGROWTH, infusión): toma Herba por tubos conectados a su soporte. Cada carga apura una planta a 6 bloques. Guarda 20 cargas.
- **Lámpara de fertilidad** (LAMPFERTILITY, infusión): toma Desiderium. Cada 15 segundos enamora a una pareja de animales adultos de la misma especie a 7 bloques. No hace nada si ya hay 8 o más de esa especie.

## Horno infernal (INFERNALFURNACE)
- Se arma con sal mágica: un cubo de 3×3×3 con ladrillos del Nether en las esquinas y obsidiana en los lados, lava en el centro y barrotes de hierro en el medio de una cara (ese es el frente).
- Los bloques del cascarón quedan invisibles. Si rompés uno, el horno se desarma y devuelve ladrillos u obsidiana.
- Lo que tirás por arriba se funde sin combustible y sale por los barrotes, o entra en el cofre que tenga adelante. Si no se puede fundir, se quema.
- Cada fuelle soplando hacia adentro (del otro lado de la pared, máximo 4) acorta el tiempo y tira una vez más el bonus. La vis del aura también lo acelera. A veces suelta un poco de flux.
- **Bonus de fundición**: pepitas al fundir minerales (oro, hierro, cobre, estaño, plata, plomo, cinabrio, cuarzo) y a veces tierra rara.

## Condensador de flux (FLUXCLEANUP)
- Quema esencia que le llega por tubos a los costados para convertir flux del aura en esencia Vitium, que se saca por abajo.
- El **enrejado** va arriba del condensador y entre sí. Más enrejado es más rápido pero más caro. Cada pieza rinde menos cuantos más vecinos tiene, y nada más allá de 8 bloques cuenta.
- A veces una pieza se tapa y deja de contar (junto con todo lo que está arriba). Se limpia con clic derecho usando un filtro, y a veces suelta un cristal de flux.
- El resonador sobre el condensador muestra el costo y el tiempo.

## Estabilizador e incrustación de redstone (INFUSIONSTABLE)
- El **estabilizador** carga una unidad por segundo hasta 15 y suelta un poco de flux cada vez. Da estabilidad a la infusión y también calma las grietas de flux a 8 bloques.
- La **incrustación de redstone** se pone como redstone y lleva la carga del estabilizador hasta 15 bloques. Los pedestales también la conducen; uno conectado muestra runas en la base.
- Cuando una descarga de inestabilidad iba a tirar o romper el ítem de un pedestal protegido, la absorbe el estabilizador, que pierde carga por un rato.
- La incrustación pasó de la etapa 6d a esta, porque el estabilizador no sirve sin ella.

## Generador y batería de vis
- **Generador de vis** (VISGENERATOR): toma 1 de vis del aura y la convierte en 1000 de energía (Forge Energy). La entrega a 20 por tick, solo por la cara a la que apunta. Con redstone se detiene.
- **Batería de vis** (VISBATTERY): guarda hasta 10 de vis. La absorbe cuando el aura pasa el 90% de su base y la devuelve cuando baja del 75%. Con redstone se vacía rápido. La luz y el comparador muestran la carga.

## Pedestal de recarga (RECHARGEPEDESTAL)
Recarga cada medio segundo con vis del aura el ítem recargable que tenga encima. El taumómetro muestra la carga como Potentia. Las tolvas pueden meter y sacar ítems.

## Dioptra (DIOPTRA)
Muestra arriba una grilla de 13×13 chunks con el nivel del aura. Con clic derecho cambia entre vis y flux. El comparador lee el chunk donde está.

## Diferencias con el original
- Los minerales ahora sueltan materiales en bruto. Por eso los bonus del horno también valen para `c:raw_materials/*`, no solo para `c:ores/*`.
- La lámpara de crecimiento llama al tick aleatorio de la planta, porque en 1.21 los cultivos crecen así (antes era un tick programado).
- La altura mínima de las luces de la lámpara arcana es el piso del mundo + 5 (antes era y = 5 fijo).
- Los sonidos y partículas del horno al quemar un ítem ahora llegan a los jugadores. En el original corrían solo en el cliente y no se veían.
- Las partículas (niebla de lámparas, chispas, lava del horno, destellos de vis) usan partículas de vanilla hasta la etapa 10.
- Se mantiene una rareza del original: el condensador acepta esencia pero no la guarda.
- La grilla de la dioptra usa el render aditivo de vanilla (`eyes`); los colores se ajustan para que se vea igual.

## Pendiente
- Bonus del horno para cúmulos nativos y trozos de carne (esos ítems todavía no existen).
- Ítems recargables que usen el pedestal (llegan con los focos y armaduras).
- Partículas propias en la etapa 10.

## Cómo probar
1. `/thaumcraft research @s all` y abrí el Thaumonomicon: las recetas nuevas aparecen en Artificio, Auromancia, Infusión y Básicos.
2. Lámparas: poné una lámpara arcana en una cueva y mirá cómo aparecen luces. Conectá un frasco de Herba con tubos a una de crecimiento junto a trigo recién plantado.
3. Horno: armá el cubo, usá sal mágica en un bloque, tirá mineral de hierro por arriba y mirá salir lingotes (y a veces pepitas) por los barrotes.
4. Condensador: poné enrejado arriba, metele esencia por los costados con flux en el aura (`/thaumcraft aura add flux 200` si hace falta) y sacá Vitium por abajo.
5. Infusión: uní con incrustación un estabilizador y los pedestales, y hacé una infusión inestable. Las descargas deberían ir al estabilizador.
6. Dioptra: ponela y hacé clic derecho para pasar de vis a flux.
