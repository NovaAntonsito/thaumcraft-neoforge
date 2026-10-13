# Etapa 5c: escaneo con el taumómetro y mesa de deconstrucción

Con esto los puntos de aspecto se consiguen jugando. El escaneo sigue a TC4 Research Port: Reborn: mantener apretado el taumómetro y ganar puntos por cada cosa nueva.

## Taumómetro
- **Escanear**: mantené clic derecho un segundo apuntando a una criatura, un bloque o un ítem tirado. Suena el tic-tac y al final el sonido de escaneo. Mientras escaneás salen runas.
- **Qué da**: la primera vez que escaneás algo ganás la mitad (redondeada) de cada uno de sus aspectos (`aspectObtainMultiplier`). Un aspecto que no conocías da +2 y se descubre. Si ya lo habías escaneado sale "Nothing new can be learned from this.".
- **Si no podés**: si a la cosa le falta un componente que no conocés, aparece "To understand this you need to study ..." y no se escanea. Primero conseguí ese aspecto.
- **Bloques con inventario** (cofres, hornos, la mesa de deconstrucción): no se pueden escanear, igual que en Reborn. Escaneá el ítem tirado en el piso.
- **Resaltado**: con el taumómetro en la mano, las criaturas y bloques que todavía te dan algo brillan en celeste.
- **Investigaciones por escaneo**: escanear ciertas cosas desbloquea requisitos de TC6, por ejemplo menas, troncos, portales, perlas de ender, dispensadores, arcilla, placas de metal, murciélagos y endermen. También cada encantamiento y efecto de poción.

## Ver los aspectos
- **Tooltip**: en cualquier inventario, con **Shift** se ven los íconos de los aspectos del ítem, pero solo si ya lo escaneaste. Con `showTags = true` se invierte: se ven siempre y Shift los oculta.
- **En el mundo**: con el taumómetro en la mano, mirando una criatura o un bloque ya escaneados, flotan sus aspectos con las cantidades.
- Se quitó el tooltip temporal de las etapas anteriores y la opción `debugAspectTooltips`.

## Barra de aura
Con el taumómetro en cualquier mano aparece arriba a la izquierda la barra del chunk: violeta para vis, morado oscuro para flux y una marca en el nivel base. Agachado se ven los números. Se actualiza una vez por segundo.

## Mesa de deconstrucción
- **Receta** (mesa arcana, 50 de vis, 1 cristal de Perditio):
  ```
   . T .
   A W P
  ```
  T = taumómetro, A = hacha de oro, W = mesa de madera, P = pico de oro.
- **Investigación Deconstruction** en Basics, debajo de Research Expertise. Pide una nota de investigación (Perditio, Cognitio, Fabrico).
- **Uso**: poné un ítem con aspectos en la ranura. Cada 2 segundos se rompe uno. Cuanta más vis primaria tiene, más chance de dejar un aspecto primario sobre la mesa (hasta 80 de vis es seguro).
- Un clic sobre el aspecto en la pantalla, o clic derecho agachado sobre la mesa, te da 1 punto de ese aspecto.
- Encima de la mesa se ven el taumómetro, el ítem girando y el aspecto que quedó.
- Las tolvas pueden meter ítems por los costados y por abajo, no por arriba.

## Config (`thaumcraft-common.toml`)
- `graphics.showTags` (false): aspectos visibles siempre en el tooltip.
- `research.instantScans` (false): escaneo instantáneo como en TC6.

## Diferencias con el original
- Los aspectos del tooltip se dibujan como un componente del tooltip, no sobre líneas vacías.
- Los aspectos flotantes no se ven a través de las paredes.
- La mesa de deconstrucción es un modelo de bloque; antes se dibujaba entera desde el renderer. El ítem en la mano no muestra el taumómetro.
- El ítem que se está rompiendo no es translúcido.
- Las placas usan los tags `c:plates/...` en vez del diccionario de menas.

## Pendiente
- Observación del cielo (escaneo del sol, la luna y las estrellas) y las curiosidades de Reborn.
- Aspectos y escaneos de criaturas, bloques e ítems de Thaumcraft que todavía no están portados.
- El efecto de "escaneo de minerales" de Reborn.

## Cómo probar
1. `/thaumcraft aspects @s research all` para que no te frene ningún componente (o probá sin eso para ver el aviso).
2. Craftea el taumómetro y mantené clic derecho sobre una vaca: tenés que ganar puntos y oír el sonido. Repetí: "Nothing new can be learned".
3. Abrí el inventario y apretá Shift sobre un ítem escaneado y otro sin escanear.
4. Mirá la barra de aura arriba a la izquierda y agachate para ver los números.
5. Completá Research Expertise y la nota de Deconstruction, craftea la mesa, poné hierro y tomá el aspecto que deja.
