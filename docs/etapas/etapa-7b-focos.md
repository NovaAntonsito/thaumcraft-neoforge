# Etapa 7b: los focos que faltaban y la bolsa de focos

Esta etapa completa el sistema de focos de Thaumcraft 6. Entran los cuatro medios y los dos efectos que quedaban, el área del foco Plan con la tecla G, el bloque elegido del Exchange y la bolsa de focos.

## Qué entra

- **Medio Cloud** (`EntityFocusCloud`): deja una nube que cada 5 ticks aplica el resto del hechizo a lo que está adentro y a algunos bloques al azar alrededor. Radio de 1 a 3 y duración de 5 a 30 segundos.
- **Medio Mine** (`EntityFocusMine`): una mina que cae despacio, se arma al tocar algo y, dos segundos después, explota sobre enemigos (o amigos, según el ajuste) a menos de un bloque. Se dibuja con `ModelGrappler` y parpadea en rojo.
- **Medio Spell Bat** (`EntitySpellBat`): un murciélago que caza al enemigo (o amigo) más cercano y le aplica el hechizo cada 2 segundos, perdiendo medio corazón cada vez. Dura 30 segundos y toma el color promedio de los efectos.
- **Medio Plan** (`FocusMediumPlan`): apunta a todos los bloques de un área alrededor del bloque que mirás, en volumen completo o solo la superficie expuesta del mismo bloque.
  - **Tecla G**: agranda el área por el eje actual (0 a 3 bloques, después vuelve a 0). **Ctrl + G** cambia de eje (todos, X, Z, Y).
  - **Overlay**: con el caster en la mano ves los bloques marcados a través de las paredes, con flechas en los ejes que crecen.
- **Efecto Rift** (`BlockHole` y `TileHole`): abre un túnel de 3x3 temporal, tan profundo como la profundidad del ajuste por la potencia. Las paredes muestran el cielo del End. Después vuelve el bloque original.
- **Efecto Exchange**: cambia el bloque golpeado por el bloque elegido, pagando uno de tu inventario y dándote lo que suelta el viejo.
  - Para elegir el bloque: **shift + clic derecho** sobre un bloque con el caster.
  - El HUD del caster muestra el bloque elegido y cuántos tenés.
- **Bolsa de focos** (`focus_pouch`): 18 espacios para focos. Clic derecho para abrirla. La tecla F busca primero en las bolsas del inventario y guarda ahí el foco que sacás.
- **Intercambiador de bloques** en `ServerEvents` (`addSwapper`), que también van a usar otras herramientas más adelante.

## Diferencias con el original

- **Cloud**: el original buscaba el enfriamiento de 2 segundos de cada criatura con un `Integer` en un mapa de `Long`, así que nunca lo encontraba y golpeaba cada 5 ticks. Lo dejé igual, porque así se jugaba.
- **Exchange**: la complejidad tiene una comparación rara en el original, que siempre da `(fortuna + 1) * 3` y hace gratis el toque de seda. Está copiada tal cual.
- **Spell Bat**: el modelo es el murciélago de 1.12 (`ModelFireBat`), porque la textura usa ese formato y el murciélago vanilla cambió en 1.20.3. No tiene huevo de spawn porque sin lanzador desaparece.
- **Rift**: la lista negra del agujero usa tags para camas y puertas (`#minecraft:beds`, `#minecraft:doors`) en vez de los nombres viejos de cada color. La revisión del Outer Lands usa el id que va a tener cuando se porte.
- **Intercambiador**: solo compara estados de bloque (los materiales ya no existen en 1.21). El efecto "bamf" es una nube de polvo de color hasta la etapa 10.
- **Bolsa**: solo se buscan bolsas en el inventario. El espacio de cinturón de Baubles necesita Curios, que no es dependencia todavía.
- **Partículas**: todo usa partículas vanilla hasta la etapa 10.

## Pendiente

- **Receta de la bolsa**: pide el cinturón de Baubles (`baubles` meta 2), que todavía no está portado. Por ahora la sacás de la pestaña creativa.
- **Descuento de vis y bolsa en Curios**: cuando se agregue Curios como dependencia.
- **CardFocus**: es una carta de theorycraft, y theorycraft no está portado.
- **Pociones de agotamiento de vis**: etapa 9.
- **Orientación de la pala elemental con G**: cuando se porten las herramientas elementales.

## Cómo probar

1. Entrá en creativo y desbloqueá todo con `/thaumcraft research <jugador> all`.
2. Sacá un caster, un `Blank Lesser Focus` y un `Focal Manipulator`. Armá estos focos en el manipulador:
   - **Cloud + Fire**: lanzalo y fijate que la nube queme lo que entra.
   - **Mine + Frost**: tirá la mina al piso, esperá que se arme y hacé pasar un mob.
   - **Spell Bat + Curse**: el murciélago tiene que perseguir a un mob.
3. **Rift**: armá Touch + Rift y usalo contra una pared de piedra. Tiene que abrirse un túnel de 3x3 con el cielo del End en las paredes, y cerrarse solo.
4. **Exchange**: armá Touch + Exchange. Con shift + clic derecho elegí un bloque de piedra. Tené piedra en el inventario y lanzalo contra tierra.
5. **Plan**: armá Plan + Break. Mirá un bloque y vas a ver el área marcada. Apretá G para agrandarla y Ctrl + G para cambiar de eje. Lanzalo para romper toda el área.
6. **Bolsa**: sacá una `Focus Pouch`, abrila y meté varios focos. Con el caster en la mano apretá F y tienen que aparecer en el menú radial.
