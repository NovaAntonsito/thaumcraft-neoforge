# Etapa 4d: infusión

El altar de infusión junta un ítem central, los ítems de los pedestales de alrededor y esencia de frascos cercanos para crear algo nuevo. También agrega los encantamientos de infusión.

## Qué se agregó
- **Piedra arcana** (`stone_arcane`) y **ladrillo de piedra arcana** (`stone_arcane_brick`), con sus escaleras y losas. Sirven de base para faros.
- **Pedestal arcano** (`pedestal_arcane`): guarda un ítem.
  - Clic derecho con un ítem lo pone. Con la mano vacía lo devuelve.
  - Las tolvas también pueden cargarlo.
- **Matriz rúnica** (`infusion_matrix`): el corazón del altar.
- **Pilar de infusión** (`pillar_arcane`): no se craftea, aparece al armar el altar. Roto devuelve 2 piedras arcanas.
- **Velas de sebo** (16 colores) y **nitor** (16 colores): dan luz. Las velas también estabilizan la infusión.
- **Sebo mágico** (`tallow`), **taumómetro** (`thaumometer`) y **guantelete de lanzador** (`caster_basic`).
  - Por ahora el taumómetro solo sirve para craftear el guantelete. Escanear viene con la investigación.
  - El guantelete solo activa la matriz. Los focos vienen más adelante.
- **Encantamientos de infusión**: Collector, Destructive, Burrowing, Sounding, Arcing y Lamplighter.
- **Pepitas de tierras raras**: a veces caen al picar menas (diamante, esmeralda, ámbar, cuarzo y otras), como en el original.

## Cómo armar el altar
Visto de costado, son tres capas:

```
capa de arriba:   .  .  .        la matriz flota en el centro,
                  .  M  .        dos bloques sobre el pedestal
                  .  .  .

capa del medio:   P  .  P        P = piedra arcana
                  .  .  .
                  P  .  P

capa de abajo:    P  .  P
                  .  D  .        D = pedestal arcano
                  P  .  P
```

1. Usá **salis mundus** sobre cualquier bloque del altar. Las piedras de las esquinas se convierten en pilares.
2. Hacé clic derecho en la matriz con el **guantelete**. Empieza a girar: está activa.
3. Poné el ítem central en el pedestal de abajo de la matriz.
4. Poné los otros ingredientes en pedestales alrededor (hasta 8 bloques de distancia).
5. Poné frascos con la esencia necesaria a menos de 12 bloques.
6. Volvé a hacer clic derecho en la matriz con el guantelete. Arranca la infusión.

Mirando la matriz activa se ve su estabilidad. Mientras craftea, también se ve la esencia que le falta.

## Estabilidad
- Las velas y las calaveras alrededor suben la estabilidad, si están en pares simétricos respecto de la matriz.
- Un bloque sin su pareja del otro lado la baja. Los pedestales cuentan igual: si a uno le falta el ítem de su par, penaliza.
- Cada vela o calavera extra del mismo tipo suma un poco menos que la anterior.
- Con estabilidad negativa pueden pasar cosas: un ítem salta del pedestal, una descarga, una explosión pequeña, warp, flux en el aura.

## Encantamientos de infusión
Se infunden poniendo la herramienta en el centro, y en los pedestales un **libro encantado** (cualquiera) más el componente.

| Encantamiento | Componente | Esencia | Va en | Efecto |
|---|---|---|---|---|
| Collector | rienda | desiderium 80, aqua 100 | hachas, picos, palas, armas | lo que rompés o matás cae a tus pies |
| Destructive | TNT | aversio 200, perditio 250 | hachas, picos, palas | rompe un 3×3 |
| Burrowing | pata de conejo | sensus 80, terra 150 | hachas, picos | en troncos y menas rompe el bloque igual más lejano |
| Sounding (I-IV) | mapa | sensus 40, ignis 60 | picos | Shift + clic derecho en un bloque marca las menas cercanas |
| Arcing (I-IV) | bloque de redstone | potentia 40, aer 60 | armas | el golpe alcanza a otros mobs cercanos |
| Lamplighter | nitor | lux 80, aer 20 | hachas, picos, palas | deja luz donde rompés en zonas oscuras |

Cada rango extra, y cada otro encantamiento de infusión que ya tenga, encarece la esencia. Con varios encantamientos la herramienta puede ganar warp.

## Recetas
| Resultado | Dónde | Vis | Cristales |
|---|---|---|---|
| 9 piedras arcanas | mesa normal (8 piedras + 1 cristal de vis) | — | — |
| 4 ladrillos arcanos | mesa normal (2×2 piedra arcana) | — | — |
| 4 escaleras / 6 losas | mesa normal | — | — |
| 3 velas blancas | mesa normal (hilo + 2 sebo) | — | — |
| Vela o nitor de color | mesa normal (tinte + vela o nitor) | — | — |
| Sebo mágico | crisol: carne podrida | — | ignis 1 |
| Nitor | crisol: polvo de piedra luminosa | — | potentia, ignis, lux 10 |
| Taumómetro | mesa arcana (oro y panel de vidrio) | 20 | los 6 primales |
| Guantelete de lanzador | mesa arcana (con taumómetro y resonador de vis) | 100 | los 6 primales |
| Matriz rúnica | mesa arcana (ladrillo arcano y nitor) | 150 | los 6 primales |
| Pedestal arcano | mesa arcana (losas y piedra arcana) | 10 | — |

## Cómo se pasó de 1.12.2 a 1.21.1
| Original | Port |
|---|---|
| `InfusionRecipe` registrada en código | tipo de receta `thaumcraft:infusion` en JSON |
| `InfusionEnchantmentRecipe` | serializador `thaumcraft:infusion_enchantment` |
| NBT `infench` (lista de id y nivel) | componente `thaumcraft:infusion_enchantments` |
| NBT `TC.WARP` del ítem | componente `thaumcraft:warp` |
| `DustTriggerMultiblock` con `ServerEvents.addSwapper` | cambio de bloques inmediato |
| `Blocks.SKULL` | cualquier `AbstractSkullBlock` (de piso o de pared) |
| `EnumFacing.HORIZONTALS` | `Direction.from2DDataValue(0..3)` (mismo orden) |
| `HarvestDropsEvent` | `BreakEvent` (romper en área) y `BlockDropsEvent` (drops) |
| `ForgeHooks.isToolEffective` | `isCorrectToolForDrops` o velocidad de minado mayor a 1 |
| tool classes "axe", "pickaxe" | `ItemAbilities.AXE_DIG`, `PICKAXE_DIG`, `SHOVEL_DIG` |
| pilar y taumómetro OBJ de Forge 1.12 | cargador `neoforge:obj`; el material apunta a `#texture` |
| `TileInfusionMatrixRenderer` con GL11 | `ModelPart` y render types `eyes` y `dragonRays` |
| paquetes FX (`PacketFX*`) | payloads de NeoForge; partículas vanilla por ahora |

## Pendiente (TODO en el código)
- Investigación: hoy no se exige ninguna. Tampoco se registra `!INSTABILITY`.
- Altares antiguo y eldritch, bloques de velocidad y costo de la matriz, estabilizador de inestabilidad.
- Goo de flux y las pociones Flux Taint y Vis Exhaust en los eventos de inestabilidad.
- Encantamientos Refining y Essence Harvester (necesitan clusters y aspectos de entidades).
- Efectos visuales propios de Thaumcraft (runas, chispas, corriente de esencia): etapa 10.
- Las posiciones en mano del taumómetro se convirtieron a ojo desde el formato de Forge. Puede que haya que ajustarlas.
