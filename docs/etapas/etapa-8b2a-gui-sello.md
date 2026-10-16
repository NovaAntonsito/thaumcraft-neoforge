# Etapa 8b-2a: GUI del sello y silueta del golem

## Qué entra

- **GUI del sello:** click derecho con la campana sobre un sello (sin shift) abre su configuración.
  - Shift + click lo sigue sacando, como en 8b-1.
- **Pestañas** (botones en el arco de la izquierda, solo si el sello tiene más de una):
  - **Prioridad/Bloqueo:** prioridad (de -5 a 5), color del golem que lo atiende (todos o uno de los 16 tintes), candado (solo golems del dueño) y "You own this seal".
  - **Filtro:** 1 casillero fantasma en los sellos básicos (9 en los avanzados, que vienen en 8c). Click con un item lo copia sin gastarlo. Botón de lista negra / lista blanca. En lista blanca se ve la cantidad (o una `*` dorada si es cualquiera).
  - **Área:** alto, ancho y largo de 1 a 8.
  - **Opciones:** casillas de cada sello (para los sellos de 8b-2b).
  - **Requisitos:** rasgos que el golem necesita o no puede tener, con tooltip.
- **Redstone:** el botón rojo, al final del arco, hace que el sello se frene con señal de redstone (solo el dueño).
- **Silueta del golem:** con shift y la campana o un sello en la mano, los golems que no ves se dibujan a través de las paredes, semitransparentes.

## Archivos

- `common/golems/client/gui/SealBaseContainer.java`: el menú, con los mismos ids de botón del original.
- `common/golems/client/gui/SealBaseGUI.java`: la pantalla. Los botones (`GuiGolemCategoryButton`, `GuiPlusMinusButton`, etc.) se dibujan adentro, como en `GuiGolemBuilder`.
- `common/container/InventoryFake.java` y `common/container/slot/SlotGhost.java`.
- `TCMenus.SEAL`, registrado en `ThaumcraftClient`.
- `ItemGolemBell`: abre la GUI.
- `RenderThaumcraftGolem` + `TCRenderTypes.entityXray`: la silueta.

## Diferencias con 1.12

- En 1.12 el container rehacía la lista de slots al cambiar de pestaña. En 1.21 no se puede, así que los casilleros del filtro siempre existen y solo están activos en la pestaña Filtro.
- `sendEnchantPacket` pasa a ser `handleInventoryButtonClick` / `clickMenuButton`.
- Al cerrar la GUI se guarda el sello en el chunk y se manda a los clientes. El original no lo guardaba hasta que el chunk se grababa por otra razón.

## Para probar

1. Poné un sello de Collect y uno de Store.
2. Click con la campana: cambiá prioridad, color y área. Cerrá y abrí de nuevo, tiene que quedar igual (también tras salir y entrar al mundo).
3. En Filtro, poné lista blanca con un item: el golem solo junta ese.
4. Con shift y la campana, mirá un golem detrás de una pared.

## Pendiente

- **8b-2b:** Empty, Harvest, Butcher, Guard, Lumber, Breaker, Use.
- **8c:** sellos avanzados, Provide, Stock, logística (shift + campana sin sello).
