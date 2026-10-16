# Etapa 8c: sellos avanzados y logística

Con esta etapa están todos los sellos del original (17 items).

## Sellos avanzados

Hacen lo mismo que el básico, con 9 casilleros de filtro y la pestaña de opciones. Se hacen en el crisol con el sello básico + aspectos, y piden la investigación del sello y **Biothaumaturgia** (`MINDBIOTHAUMIC`).

| Sello | Extra | Necesita | Crisol |
|---|---|---|---|
| Collect avanzado | opciones de filtro | Smart | Collect + 10 Sensus + 10 Cognitio |
| Store avanzado | opciones de filtro | Smart | Store + 10 Sensus + 10 Cognitio |
| Empty avanzado | opciones, "ciclar lista blanca", "dejar 1" | Smart | Empty + 10 Sensus + 10 Cognitio |
| Guard avanzado | atacar mobs, animales o jugadores (con PvP) | Fighter + Smart | Guard + 20 Sensus + 20 Cognitio |
| Breaker avanzado | toque de seda (rompe más lento) | Breaker + Smart | Breaker + 10 Sensus + 10 Cognitio + 20 Instrumentum |

## Provide y Stock

- **Provide** (en un cofre): los golems sacan de ahí lo que se pide y lo llevan.
  - Crisol: Empty avanzado + 10 Permutatio + 10 Desiderium.
  - Opciones: de a un item, dejar al menos 1.
- **Stock** (en un cofre): con lista blanca y cantidades, pide a los Provide lo que falta para llegar a esa cantidad.
  - Crisol: Store + 10 Cognitio + 10 Desiderium.
- Ahora también andan "pedir semillas" de Harvest y "pedir de la lista blanca" de Use.

## Logística

- Con la investigación `GOLEMLOGISTICS`: **shift + click derecho con la campana** (sin apuntar a un sello).
- Muestra todo lo que tienen tus Provide a 32 bloques o menos, con buscador y barra de scroll.
- Elegís un item, la cantidad con la barrita o +/-, y "Request".
  - Si hiciste click en un bloque, el golem lo deja ahí (o adentro, si es un cofre).
  - Si hiciste click al aire, te lo trae a vos.

## Archivos nuevos

- Sellos: `SealPickupAdvanced`, `SealFillAdvanced`, `SealEmptyAdvanced`, `SealGuardAdvanced`, `SealBreakerAdvanced`, `SealProvide`, `SealStock`.
- Logística: `ContainerLogistics`, `InventoryLogistics`, `SlotGhostFull`, `client/gui/GuiLogistics`.
- Paquetes: `PacketLogisticsRequestToServer`, `PacketMiscStringToServer` (texto del buscador).
- `TCMenus.LOGISTICS`, texturas `gui_logistics.png` y las de los sellos.

## Diferencias con 1.12

- Las pilas de más de 64 en la pantalla de logística ya viajan en el paquete normal de 1.21. `PacketItemToClientContainer` no hace falta.
- El pedido de logística solo se acepta con la pantalla abierta y se corta en 1000 pilas, así un cliente no puede colgar el servidor.
- Las barras ya no dan NaN cuando el máximo es igual al mínimo (pasaba con un solo item en el original).
- Al descargar el mundo también se borran los pedidos pendientes, como los sellos y las tareas.

## Para probar

1. Provide en un cofre con items, Stock en otro cofre con un item en la lista blanca y una cantidad. Un golem cerca tiene que llevar hasta llegar a la cantidad.
2. Con `GOLEMLOGISTICS`, shift + click al aire con la campana, pedí algo y esperá al golem.

## Pendiente de los golems

- **10:** partículas propias (la chispa del sello frenado por redstone, los emotes).
