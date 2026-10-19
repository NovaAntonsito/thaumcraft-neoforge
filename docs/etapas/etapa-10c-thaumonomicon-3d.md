# Etapa 10c: Thaumonomicon 3D animado

El Thaumonomicon en la mano ahora es un libro 3D, como el de la mesa de encantamientos pero con la tapa de Thaumcraft. Sin GeckoLib: usa el `BookModel` de vanilla.

## Qué hace

- **En la mano**: se ve el libro cerrado.
- **Clic derecho**:
  - suena la página;
  - el libro sube, gira y se abre (unos 8 ticks);
  - las hojas pasan solas mientras se abre;
  - al terminar de abrirse aparece la GUI del Thaumonomicon.
- **Al cerrar la GUI**: el libro se cierra con la misma animación.
- **Si soltás el libro o cambiás de slot**: la animación se corta y queda cerrado.
- **Inventario, suelo y marcos**: siguen con el ícono plano de siempre.
- El Thaumonomicon de creativo (cheat) usa su propia textura.

## Archivos

- `client/renderers/item/ThaumonomiconRenderer.java`: el renderer del ítem (BEWLR).
- `client/lib/ThaumonomiconAnimation.java`: el estado de la animación (cerrado, abriendo, leyendo, cerrando).
- `ItemThaumonomicon.use`: en el cliente arranca la animación en vez de abrir la GUI directo.
- `models/item/thaumonomicon*.json`: `neoforge:separate_transforms`, 3D en la mano y plano en el resto.
- `textures/models/thaumonomicon_book.png` y `thaumonomicon_cheat_book.png`: salen de la tapa del ítem con `tools/gen_thaumonomicon_book.py`.

## Diferencias con TC6

- En TC6 el clic abría la GUI al toque, sin libro 3D. Esto estaba en el plan del port.
- La GUI abre unos 8 ticks después del clic.
- Solo se anima en primera persona. En tercera persona se ve el libro cerrado.

## Para ajustar en el juego

- Las posiciones y escalas de la mano (en el JSON y en el renderer) son a ojo. Hay que mirarlas en el juego.
- La textura del libro es generada; si querés una pintada a mano, reemplazá los PNG.

## Cómo probarlo

1. Agarrá el Thaumonomicon.
2. Mirá que en la mano sea un libro 3D y en el inventario el ícono plano.
3. Clic derecho: se abre el libro y después la GUI.
4. Cerrá con Esc: el libro se cierra.
