# Etapa 8a: golems base

## Qué entra

- **API de golems** (`api/golems`): materiales, cabezas, brazos, piernas, addons, traits, `IGolemAPI`, `Task`, seals (solo las interfaces) y `GolemHelper`.
- **Las partes por defecto** (`GolemProperties.registerDefaults()`): madera, hierro, arcilla, latón, thaumium y void, con todas las cabezas, brazos, piernas y addons del original y en el mismo orden.
- **Golem** (`EntityThaumcraftGolem`):
  - Sus stats salen de las partes.
  - Sube de rango.
  - Lleva ítems en las dos manos y vuela, rueda o trepa según las piernas.
  - Pelea si tiene el trait FIGHTER y dispara dardos con los brazos de dardos.
  - Muestra emotes y se cura solo.
- **IA básica**:
  - Seguir al dueño.
  - Volver a casa.
  - Ir a un bloque o a una entidad.
  - Atacar a quien ataca al dueño.
  - Ataque a distancia.
- **Campana del golemante**:
  - Click en un golem: alterna entre seguirte y quedarse quieto.
  - Shift + click en un seal: lo saca.
- **Golem empaquetado** (ítem `golem`):
  - Lo colocás con click derecho.
  - Shift + click a tu golem lo vuelve a empaquetar, con su XP.
  - Con un tinte le cambiás el color a la bandera.
- **Prensa de golems**: multibloque con polvo de sal (research `MINDCLOCKWORK`).
  - **Armado:**
    - Abajo van un pistón mirando arriba, un caldero, un yunque y una mesa de piedra.
    - Sobre el pistón van barrotes de hierro.
  - **GUI:** elegís las partes, ves traits, stats y componentes (en gris si te faltan), y prensás.
  - **Componentes:** los toma de cofres al lado o abajo de la prensa, o de tu inventario.
  - **Esencia:** después chupa Machina por tubos, una por punto de costo.
- **Render OBJ**:
  - Loader propio en `client/lib/obj/ObjModel`.
  - Golem armado por piezas con la animación procedural del original.
  - Prensa con el pistón animado y la lava en el caldero.
- **Extras**: módulos de visión y agresión (recetas arcanas), receta de la campana y dardo de golem.
- **Integración**: mergeé los cambios de anomalías que ya estaban en tu carpeta en `Thaumcraft.java`, `ThaumcraftClient.java`, `Config.java`, `TCNetwork.java` y `en_us.json`. No pisé nada.

## Diferencias con el original

- **Datos del ítem:** el long de partes y la XP ahora son data components (`golem_props`, `golem_xp`), no NBT.
- **Dimensiones:** se guardan como `ResourceKey<Level>` en vez del int.
- **Respirar bajo el agua:** `canBreatheUnderwater` es final en 1.21, así que el golem está en el tag `minecraft:can_breathe_under_water`.
- **Fix de un bug del decompilado:**
  - El trait FRAGILE multiplicaba por `(int) 0.75`, o sea 0. Lo dejé en 75% de vida y armadura.
  - Eso afecta tanto a la entidad como a la GUI.
- **Ítems en las manos:** el golem nunca los dropea como equipo, para que no se dupliquen con el inventario que carga.
- **Partículas:** los emotes usan partículas vanilla. Las del original llegan en la etapa 10.
- **Pendiente para 8b:** la silueta gris del golem a través de paredes cuando tenés un seal en la mano.

## Pendiente

- **8b:**
  - Seals y su GUI.
  - Guardar seals y tareas.
  - `PacketSealToClient`.
  - Las funciones de seal de la campana.
- **8c:** logística y seals avanzados.
- **Navegación:** la de suelo usa la vanilla. El `GolemNodeProcessor` del original queda como TODO.

## Cómo probar

1. **Research:** desbloqueá `MINDCLOCKWORK` (golemancia) en el thaumonomicon.
2. **Armar la prensa:**
   - Pistón mirando arriba con barrotes encima.
   - Al lado: caldero, yunque y mesa de piedra (2x2 en el piso).
   - Polvo de sal sobre el pistón.
3. **Prensar un golem:**
   - Abrí la prensa, elegí madera + cabeza básica + brazos + piernas.
   - Poné los componentes en un cofre al lado o en tu inventario y apretá el botón.
   - Conectá un jarro de Machina con tubos y esperá el golem en el slot.
4. **Probar el golem:**
   - Colocalo.
   - Con la campana, click para que te siga o se quede.
   - Con un tinte, click para cambiar la bandera.
   - Shift + click para empaquetarlo.
5. **Creativo:** en la pestaña de Thaumcraft hay 4 golems de muestra para probar sin prensa.
