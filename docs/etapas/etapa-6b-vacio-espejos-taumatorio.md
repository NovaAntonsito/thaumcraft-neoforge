# Etapa 6b: grietas de flujo, espejos y taumatorio

Llega el lado oscuro del flux: cuando el aura de un chunk se llena de flux aparece una grieta que crece, se come bloques y al colapsar deja semillas del vacío y a veces una perla primordial. Con eso se destraban la construcción alquímica avanzada (y la fundición de vacío), los lingotes de vacío del crisol y el sifón del vacío. También están los espejos mágicos y el taumatorio.

## Grieta de flujo (Flux Rift)
- Aparece cuando el flux de un chunk pasa el límite (no aparece en wuss mode). Los jugadores a 32 bloques reciben el aviso "too much flux" y la investigación `f_toomuchflux`.
- Es una cadena de puntos que se arma con su semilla y su tamaño. Sobre esa cadena rompe bloques y lastima entidades (los ítems que toca desaparecen).
- Cada 30 segundos come flux del aura y crece. Con el tiempo pierde estabilidad; si está inestable puede disparar un evento o empezar a colapsar.
- Al colapsar larga vis o flux al aire, hace explosiones chicas que no rompen bloques y al final suelta semillas del vacío (raíz del tamaño máximo) y, con esa misma chance en %, una perla primordial gastada (nódulo o mota).
- Si estaba inestable, debilidad a los que estén cerca. Si no era muy estable, warp normal y temporal a los jugadores cerca.
- Se puede escanear (`!FluxRift`).

## Perla primordial y semilla del vacío
- La **perla** tiene 8 usos: en una receta de mesa vuelve con un uso menos y cambia de dibujo (perla, nódulo, mota).
- La **semilla del vacío** es el catalizador del lingote de vacío en el crisol (10 Metallum + 5 Vitium, investigación BASEELDRITCH).
- **Construcción alquímica avanzada**: mesa arcana, dos construcciones alquímicas, dos placas de vacío y la perla. Con esto ya se puede fabricar la fundición de vacío.

## Sifón del vacío (Void Siphon)
Infusión sobre un bloque de vacío. Mientras no tenga redstone, tira de las grietas que vea a 8 bloques: las achica, las vuelve inestables y cada 2000 de progreso deja una semilla del vacío en su casilla. Las tolvas pueden sacar las semillas.

## Espejos
- **Espejo mágico** (infusión, investigación MIRROR): poné uno, y con otro espejo en la mano hacé clic derecho sobre el primero; te queda un espejo enlazado. Lo que cae o se mete por tubos o tolvas en uno sale por el otro, aunque esté en otra dimensión. Al levantarlo recuerda su pareja.
- **Espejo de esencia** (MIRRORESSENTIA): no se le mete esencia por tubos. Sirve para que algo que chupa esencia (como la matriz de infusión) la tome de los frascos que están cerca del otro espejo.
- Los dos generan flux cuando se usan mucho.
- **Espejo de mano** (MIRRORHAND): clic derecho sobre un espejo mágico para enlazarlo. Después, clic derecho en el aire abre una casilla: lo que pongas ahí aparece en el espejo enlazado. Solo manda, no recibe.

## Taumatorio (investigación Alchemical Automation)
- Se arma con polvo de sal mágica: dos construcciones alquímicas apiladas sobre un crisol. Mira hacia vos.
- Necesita calor dos bloques abajo (debajo del crisol): lava, fuego, nitor o magma. Con redstone se detiene.
- En la interfaz ponés el catalizador arriba a la izquierda; abajo aparecen las recetas de crisol que conocés para ese catalizador. Clic para elegir o quitar una. A la derecha se ven las esencias de la receta y cuánto lleva.
- Tira la esencia por tubos desde los costados y desde arriba (no desde el frente). El resultado sale por el frente, o entra en el cofre que tenga adelante.
- **Matriz mnemónica**: se pega a un costado del taumatorio (no al frente). Cada una suma 2 recetas. Lleva una **mente mecánica** (clockwork mind), que también tiene su receta.
- Al romper cualquiera de las dos mitades, o el crisol, todo vuelve a ser construcciones alquímicas.

## Diferencias con el original
- El taumatorio recuerda las recetas por su id del datapack (`thaumcraft:crucible/...`) en vez del hash de la receta.
- La lista de recetas de la interfaz se recalcula cada medio segundo del lado del cliente (en 1.12 se recalculaba al cambiar la casilla).
- Los efectos de estrellas del vacío (grieta, espejos, sifón) usan el render del portal del End de vanilla en lugar de los shaders del original. Los halos de la grieta son un tubo violeta aditivo.
- La grieta leía su estabilidad guardada como entero, perdiendo los decimales. Se mantiene igual.
- El marco del espejo es un modelo de bloque de un píxel de grosor (antes era un sprite extruido).
- El taumatorio y su parte de arriba no tienen ítem: cada mitad suelta una construcción alquímica, como en el original.

## Pendiente
- Eventos de la grieta: wisps, semilla de taint, agotamiento de vis infeccioso y la nube de flux. Por ahora solo el colapso.
- El efecto "flux taint" al colapsar una grieta muy inestable.
- La explosión visual al colapsar (es provisoria) y el halo de la grieta que se ve a través de las paredes con las gafas.
- La mente biotaúmica tiene ítem pero no receta (necesita el cerebro de zombi).

## Cómo probar
1. `/thaumcraft research @s all`.
2. `/summon thaumcraft:flux_rift ~ ~2 ~ {RiftSize:30,RiftSeed:1234}` y mirala con el taumómetro.
3. Poné un sifón del vacío cerca de la grieta y abrilo: en un rato aparece una semilla. La grieta se achica.
4. Lingote de vacío en el crisol con la semilla.
5. Espejos: poné uno en una pared, enlazá otro, ponelo lejos y tirá un ítem al primero.
6. Taumatorio: crisol sobre lava, dos construcciones alquímicas arriba, sal mágica. Poné hierro de catalizador, elegí el lingote de taumio y conectá frascos con tubos.
