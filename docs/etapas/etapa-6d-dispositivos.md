# Etapa 6d: oído arcano, relé, levitador, cofre hambriento, cerebro en frasco, spa, rociador y urna

Con esta etapa se cierran los dispositivos de la etapa 6. Para que funcionen hizo falta sumar algunas cosas más: las sales de baño, el fluido purificador con su balde, el efecto Warp Ward y el cerebro de zombi.

## Dispositivos
- **Oído arcano** (ARCANEEAR, mesa arcana): se pega a cualquier cara sólida. Cuando suena un bloque de notas a menos de 64 bloques, con su misma nota y su mismo instrumento, da señal 15 por 10 ticks. Con clic derecho subís la nota. El instrumento sale del bloque que tiene detrás.
- **Oído arcano de palanca**: se fabrica sin forma, con un oído y una palanca. En vez de dar un pulso, cambia de estado con cada nota.
- **Relé de redstone** (REDSTONERELAY, mesa arcana): un repetidor configurable. La antorcha de atrás fija la señal mínima de entrada (1 a 15) y la de adelante la señal que sale. Para cambiarlas hacés clic sobre cada antorcha. Si mirás el relé con el taumómetro en la mano, aparecen los dos números flotando.
- **Levitador** (LEVITATOR, mesa arcana): empuja entidades hacia donde mira. Con clic en la palanca chiquita cambiás el alcance (4, 8, 16 o 32), y cuanto más alcance, más vis gasta del aura. Se apaga con redstone. Si lo ponés agachado queda mirando al revés.
- **Cofre hambriento** (HUNGRYCHEST, mesa arcana): se traga los ítems que caen arriba. No se une con otros cofres.
- **Cerebro en frasco** (JARBRAIN, infusión): atrae orbes de experiencia a 8 bloques y los guarda hasta 2000 xp. Con clic derecho suelta una parte. Cuando está lleno brilla, cuenta como 5 libreros para la mesa de encantamiento y el comparador mide cuánto tiene. Al romperlo guarda la xp en el ítem.
- **Spa arcano** (ARCANESPA, mesa arcana): un tanque de 5000 mB que llenás con baldes. Si está en modo mezcla y tiene sales de baño, pone fluido purificador a su alrededor. Si no, pone el fluido que tenga. Funciona cada 2 segundos mientras no tenga redstone.
- **Rociador de pociones** (POTIONSPRAYER, mesa arcana): le ponés una poción y él calcula qué esencia necesita, que recibe por tubos. Cada pulso de redstone dispara la poción como nube hacia adelante.
- **Urna siempre llena** (EVERFULLURN, crisol): se rellena sola con vis del aura. Llena calderos, tanques y cualquier bloque con fluidos a 2 bloques. Con un balde le sacás agua y con un frasco vacío te llevás una botella de agua. Por arriba se conecta a tuberías.

## Nuevos ítems y fluido
- **Sales de baño** (BATHSALTS, crisol): si las tirás en agua quieta, cuando desaparecen convierten esa agua en fluido purificador.
- **Fluido purificador**: si entrás en una fuente te da Warp Ward y la fuente se consume. Mientras más warp permanente tengas, menos dura el efecto (como mucho 32000 ticks).
- **Cerebro de zombi**: se come y da warp, con chance de Hambre. Los lobos también lo comen.

## Diferencias con 1.12
- El tono del oído sale de `BlockState.instrument()` y no de una tabla de materiales.
- El relé extiende `DiodeBlock`. Lo que antes se llamaba "enabled" ahora es `POWERED`. Las antorchas forman parte del contorno del bloque, así que el clic les acierta bien.
- La palanca del levitador también forma parte del contorno.
- El cofre hambriento extiende `ChestBlock`. Se abre aunque tenga un bloque arriba.
- El spa no usa "quanta", porque en 1.21 los fluidos ya no tienen niveles parciales.
- La urna también llena calderos vacíos. La lista de bloques que estaba llenando ahora se guarda de verdad, porque en el original se perdía. La macetera de Botania no está soportada.
- El cerebro solo se come los orbes en el servidor. En el cliente los atrae pero no los toca.
- Las partículas son de vanilla hasta la etapa 10.
- Por ahora Warp Ward no hace nada, porque los eventos de warp llegan en una etapa posterior.

## Pendientes
- Mostrar la nota del oído con las gafas de revelación.
- Que Warp Ward bloquee los eventos de warp.
- Que los zombis cerebrudos suelten el cerebro.
- Partículas propias (etapa 10).

## Cómo probar
1. Corré `/thaumcraft research @s all`.
2. Poné un bloque de notas y un oído a menos de 64 bloques. Ajustá el oído a la misma nota (cada clic sube una) y tocá el bloque de notas. Una lámpara de redstone pegada al oído se prende un instante.
3. Con un relé, hacé clic en cada antorcha y miralo con el taumómetro para ver los números.
4. Poné un levitador mirando arriba, pisalo y hacé clic en su palanca para cambiar el alcance.
5. Tirá ítems arriba del cofre hambriento.
6. Poné el cerebro en frasco cerca de donde matás mobs, y después sacale xp con clic derecho.
7. Llená el spa con un balde de agua, ponele sales de baño y activá el modo mezcla.
8. Ponele una poción al rociador, conectale esencia por tubos y dale un pulso de redstone.
9. Poné la urna al lado de un caldero vacío.
10. Tirá sales de baño en una fuente de agua, esperá unos 10 segundos y metete en la fuente.
