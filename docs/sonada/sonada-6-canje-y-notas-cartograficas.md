# Soñada 6: canje de puntos y notas cartográficas

Le da uso a los puntos de investigación que sobran y agrega notas que llevan a lugares del mundo.

## Canje de puntos (POINTEXCHANGE)

- **Investigación:** POINTEXCHANGE en Básicos [6,1], hija de RESEARCHMASTERY. Pide 1 de teoría de Básicos.
- **Dónde:** en la mesa de investigación aparece un botón a la derecha del inventario, con el ícono de la nota cartográfica. Abre un panel de papel sobre la hoja.
- **Con qué se paga:** solo cuentan los puntos de aspectos compuestos, y siempre te deja al menos 1 de cada uno. Los primales no cuentan porque el piso de `primalAspectFloor` los rellena gratis. El pago se saca de a un punto, del aspecto que más tenga.
- **Ofertas:**

| Oferta | Costo | Qué hace |
|---|---|---|
| Refill Ink | 10 | Llena las herramientas de escritura de la mesa |
| Clear the Mind | 15 | Saca 2 de warp temporal |
| Rare Insight | 20 | Te revela un aspecto que ya podrías descubrir (conocés sus dos partes). Si no hay, suma 3 puntos al aspecto más complejo que conocés |
| Cartographic Note | 30 | Escribe una nota cartográfica; gasta un papel del inventario |

- **Límite diario:** todo menos la tinta se puede comprar una vez por día de Minecraft (24000 ticks), cada oferta por separado.
  - El motivo: como los primales se rellenan solos, combinándolos se pueden fabricar puntos compuestos sin fin. Sin el límite, el warp y las notas serían infinitos.
  - El tiempo se guarda en los datos persistentes del jugador.

## Notas cartográficas

- **Ítem `cartographic_note`:** es una nota de investigación (misma textura, teñida color motus).
  - Se resuelve en la mesa igual que una nota de investigación.
  - La grilla se arma al ponerla en la mesa (`generic`), y la dificultad depende del destino.
- **Destinos (`CartographyTargets`):**

| Destino | Peso | Dificultad | De dónde sale |
|---|---|---|---|
| Barrow | 10 | 3 | `MoundLocations` (ya existía) |
| Anillo eldritch | 4 | 6 | `StructureLocations`, nuevo; se anota al generarse |
| Nodo raro (no normal, o brillante) | 8 | 4 | `AuraNodeRegistry` de la 5c |

- **Al usar la nota resuelta:**
  - Busca el lugar más cercano de ese tipo a 3000 bloques o menos, salteando los que ya mapeaste.
  - Se convierte en un mapa de vanilla, como los de explorador: centrado en el lugar, con los biomas pintados, una X y nombre propio ("Map to a Barrow").
  - Si no encuentra nada, te avisa y la nota queda para probar más tarde.
- **Dónde se consiguen:** en el canje, y en las bolsas de loot poco comunes (peso 4) y raras (peso 8). La nota de una bolsa elige su destino cuando llega a un inventario.
- **Extensible:** Susurros del vacío (grietas) o las bibliotecas abandonadas pueden registrar su propio destino con `CartographyTargets.register`.

## Límites

- Solo se encuentran lugares que el mundo ya generó. Barrows y anillos se anotan desde ahora, así que en mundos viejos solo aparecen los que se generen de acá en adelante.
- No hay cofres de estructuras con loot propio en el port, por eso la nota va en las bolsas de loot.

## Archivos

- `common/lib/research/PointExchange`, `PacketPointExchangeToServer`, el panel en `GuiResearchTable`.
- `common/items/consumables/ItemCartographicNote`, `common/lib/research/CartographyTargets`.
- `common/world/objects/StructureLocations`; `MoundLocations.all()`.
- `ResearchNoteManager.computeNoteData` deriva las notas cartográficas.
- Bolsas de loot en `ConfigLoot`; modelo, color y predicado `complete` del ítem.

Compila. No está probado en el juego.
