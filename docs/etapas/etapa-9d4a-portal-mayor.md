# Etapa 9d-4a: el portal carmesí mayor y el pretor

Llega el primer jefe: el portal carmesí mayor, que larga oleadas de cultistas hasta que sale el pretor carmesí. Con él vienen la base de todos los jefes, la armadura de pretor, las cajas y urnas de botín y los estandartes.

## El portal mayor (`cultist_portal_greater`)

- 500 de vida, 5 de armadura, no se mueve, no le hacen nada las pociones ni el fuego. Barra de jefe roja en 6 partes que oscurece el cielo.
- Solo sale del huevo, como en el original.
- Al aparecer:
  - A los 2 segundos pone **4 estandartes del culto** a 6 bloques, uno por lado, con un rayo rojo.
  - Durante unos 6 segundos va tirando **cajas de botín** alrededor (5% raras, 15% poco comunes, el resto comunes).
- Después, mientras haya un jugador a 48 bloques, larga oleadas:
  - Las primeras 5 salen rápido. Después tarda 1 segundo más por cada cultista que ya haya cerca (32 bloques).
  - Cada oleada es un caballero (67%) o un clérigo, con casa en el portal (32 bloques).
  - Hasta la oleada 12 se cura 1 por tick. En la 12 sale el **pretor carmesí**.
  - De ahí en adelante cada cultista que larga le saca 5 a 9 de vida.
- Si lo tocás te pega 8 de daño mágico.
- Al morir explota (sin romper bloques) y suelta una **perla primordial**.

## El pretor carmesí (`cultist_leader`)

- 150 de vida, 5 de daño, velocidad 0.32. Un poco más grande que un cultista (1.15).
- Sale con la armadura de pretor, botas carmesí y la espada carmesí, que puede venir encantada.
- De lejos (más de 16 bloques) tira orbes de energía rojos. De cerca pega con la espada.
- Le da regeneración II por 3 segundos a los cultistas a 8 bloques.
- Los cultistas son sus aliados. Suelta un tesoro raro.
- Se escanea como `!ORBOSS`. Tiene Alienis 40, Aversio 40 y Humanus 40.

## Lo que comparten los jefes

Esto es la base `EntityThaumcraftBoss`, que van a usar también el golem y el warden de la 9d-4b:

- Barra de jefe violeta que oscurece el cielo. Se queda a 24 bloques de donde apareció.
- Se cura 1 cada 1,5 segundos.
- Ningún golpe le saca más de 35. Si le pegás más fuerte se **enoja** 10 segundos (regeneración, fuerza y prisa) y te avisa: "is enraged by your powerful blows."
- No desaparece, respira bajo el agua, no lo frenan las telarañas y es amigo de los bichos eldritch.
- Suelta una perla primordial y un tesoro raro (el pretor solo el tesoro).
- No suelta el tesoro extra de los campeones.
- Si es campeón, en vez de los bonus de campeón recibe un nombre. Para el pretor es "Praetor [título] the [modificador]".

## Armadura de pretor

- Casco, pechera y grebas (`crimson_praetor_helm`, `_chest`, `_legs`). Protección 3/7/6, dureza 1, encantabilidad 20, rara. Se repara con hierro.
- Usa el modelo y la textura del original. Las telas de las piernas y la capa se mueven al caminar.
- Aspectos: los del hierro más Alienis 10.

## Cajas y urnas de botín

- `loot_crate_common/uncommon/rare` (madera) y `loot_urn_common/uncommon/rare` (piedra, con su sonido de romperse).
- Se rompen al toque. Sueltan 1 + rareza + 0 a 2 tesoros al azar, o se sueltan a sí mismas con toque de seda.
- Aspectos: Desiderium 10/20/30, más Terra 2 (urnas) o Herba 2 (cajas).
- Las urnas las va a usar la 9d-4c (túmulos).

## Estandartes

- 16 de colores (`banner_<color>`) y el del culto carmesí (`banner_crimson_cult`). Sin colisión.
- Se ponen como los de vanilla: arriba de un bloque miran hacia vos (16 direcciones), en una pared quedan colgados.
- En uno de color, una **phial** le pone su aspecto (se dibuja en la tela y se gasta la phial). Agachado y con la mano vacía se lo sacás.
- Al romperlo o con el pick block guarda el aspecto, y el tooltip lo muestra.
- Receta arcana (investigación BASEINFUSION, 10 de vis): 3 lanas del color, 2 palos y 1 losa de madera.
- Aspectos: Alienis 5 además de lo que da la receta.

## Otros cambios

- Los cultistas ahora son aliados del pretor y no lo atacan.
- El cangrejo eldritch y el zombie habitado ahora pueden salir de campeón, como en el original (estaban pendientes de la 9d-3).
- Huevos del portal mayor y del pretor.

## Diferencias con el original

- **Barra del portal**: en el original no bajaba nunca, porque se actualizaba con la IA y el portal no tiene IA. Acá baja con la vida.
- **Cambio de objetivo de los jefes**: el original quería cambiar al que más daño le hizo, pero buscaba al objetivo actual en vez del nuevo, así que nunca cambiaba. El bonus por jugadores extra solo cuenta a los que pasan ese chequeo, así que casi nunca se aplica. Lo dejé igual.
- **Espada del pretor**: en fácil llevaba la espada del vacío, que llega en la 9e. Hasta entonces usa la carmesí siempre.
- **Perla primordial**: en el original flotaba (EntitySpecialItem). Por ahora es un ítem común que sale para arriba, como en el tentáculo gigante.
- **Nombre del pretor**: el texto original tenía un `%d` que hacía fallar el nombre. Lo puse con `%s` en una clave nueva (`entity.thaumcraft.cultist_leader.custom`).
- **Ícono de los estandartes en la mano**: los ajustes de cómo se ve en la mano eran del formato viejo y 1.21 no los lee. Ahora se ve como cualquier ítem plano.
- **El estandarte del culto en el inventario** sale teñido de rojo, como en el original (la capa del dibujo toma el color del bloque).
- **Sacar el aspecto**: en el original alcanzaba con estar agachado. En 1.21, agachado con algo en la mano no se usa el bloque, así que hay que tener la mano vacía.
- **Partículas**: siguen siendo de vanilla hasta la etapa 10.

## Cómo probarlo

1. En supervivencia, tirá un huevo de Greater Crimson Portal en un lugar abierto y plano.
2. Mirá cómo aparecen los 4 estandartes y las cajas. Rompé las cajas para sacar tesoros.
3. Aguantá las oleadas hasta que salga el pretor. Fijate que cura a los cultistas cercanos.
4. Pegale con algo de más de 35 de daño (o `/effect` de fuerza) para verlo enojado.
5. Matá el portal y agarrá la perla.
6. Fabricá un estandarte en la mesa arcana, ponelo y usá una phial llena encima.
