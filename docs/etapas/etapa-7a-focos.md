# Etapa 7a: focos, caster y Manipulador Focal

Esta etapa porta el sistema de focos de Thaumcraft 6: el motor de hechizos, los focos en sus tres niveles, el guantelete (caster) completo, el cambio de foco con la tecla F y el Manipulador Focal para diseñar hechizos.

## Qué entra

- **API de focos**: `FocusEngine`, `FocusPackage`, `FocusNode`, medios, efectos, mods, `Trajectory` y `NodeSetting`.
- **Focos**: `focus_1`, `focus_2` y `focus_3` (complejidad 15, 25 y 50). El color del ítem sale del hechizo que lleva.
- **Caster** (`ItemCaster` y `CasterManager`): consume vis del aura, aplica descuentos de armadura, tiene enfriamiento y guarda el foco montado en un data component.
- **Medios**: Touch, Bolt y Projectile (con `EntityFocusProjectile`).
- **Efectos**: Fire, Frost, Air, Earth, Break, Flux, Heal y Curse.
- **Mods**: Scatter, Split Target y Split Trajectory.
- **Campo de drenaje** (`effect_sap`): el bloque invisible que deja Curse.
- **Tecla F**: abre el menú radial de focos; con shift saca el foco.
- **HUD del caster**: el dial con la barra de vis. El HUD del taumómetro baja cuando tenés un caster en la mano.
- **Manipulador Focal** (`wand_workbench`): bloque, contenedor, GUI completa (árbol de nodos, lista de partes, ajustes, costos y cristales) y renderer (el foco girando y los cristales en órbita mientras se crea).
- **Recetas**: `focus_1` (crisol), `wand_workbench` (arcana), `focus_2` y `focus_3` (infusión).
- **Íconos `focus:`** en el navegador de investigación, ahora dibujados con las partes reales.

## Diferencias con el original

- **Costo de XP en la GUI**: el original usaba `Math.min(1, ...)`, así que siempre mostraba 0 o 1, y validaba con el costo del último crafteo. Ahora muestra `Math.max(1, ...)`, que es lo que la mesa cobra de verdad, y valida con ese número. En creativo no pide XP, igual que la mesa.
- **Reinicio de la GUI**: lo dispara solo el slot del contenedor (como en el original). Así, al cargar el mundo no se borra un diseño a medio hacer.
- **Partículas**: los efectos usan partículas vanilla hasta la etapa 10.
- **Tecla F**: choca con "cambiar a la otra mano" de vanilla, igual que en el original. La podés cambiar en Controles > Thaumcraft.
- **Clic derecho en un nodo**: lo borra (deja el padre como estaba), igual que el original.

## Pendiente (7b y después)

- Medios Cloud, Mine, Spell Bat y Plan (con tecla G y overlay), efecto Exchange y Rift.
- Bolsa de focos, CardFocus y descuento de Curios.
- Recarga del manipulador desde un cargador de vis.
- `getPickedBlock` del caster (lo usa Plan).

## Cómo probar

1. Entrá en creativo y sacá un caster, un `Blank Lesser Focus` y un `Focal Manipulator` de la pestaña de Thaumcraft.
2. Desbloqueá todo con `/thaumcraft research <jugador> all`.
3. Poné el manipulador y abrilo. Meté el foco en el slot.
4. Clic en el nodo vacío, elegí un medio (Touch) y después un efecto (Fire) de la lista de la izquierda.
5. Cambiá los ajustes con las flechas de abajo a la derecha. Ponele nombre arriba.
6. Tené los cristales que pide y tocá el botón de arriba a la derecha. Vas a ver los cristales girando sobre la mesa.
7. Sacá el foco, agarrá el caster y apretá F para montarlo. Clic derecho para lanzar.
8. Probá Projectile con Split Trajectory y Scatter para ver el árbol con ramas.
