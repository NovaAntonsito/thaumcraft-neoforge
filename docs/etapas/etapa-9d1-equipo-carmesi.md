# Etapa 9d-1: el equipo del Culto Carmesí

Arranca la etapa eldritch. Antes de los cultistas va su equipo, porque lo usan ellos y lo sueltan al morir. En el original no tiene receta: se consigue solo como loot.

## Las piezas

| Ítem | Id | Defensa | Extras |
|---|---|---|---|
| Crimson Cult Helm | `crimson_plate_helm` | 2 | |
| Crimson Cult Chestplate | `crimson_plate_chest` | 6 | |
| Crimson Cult Greaves | `crimson_plate_legs` | 5 | |
| Crimson Cult Hood | `crimson_robe_helm` | 2 | 1% de descuento de vis, 1 de warp |
| Crimson Cult Robe | `crimson_robe_chest` | 5 | 1% de descuento de vis, 1 de warp |
| Crimson Cult Leggings | `crimson_robe_legs` | 4 | 1% de descuento de vis, 1 de warp |
| Crimson Cult Boots | `crimson_boots` | 2 (como las de hierro) | 1% de descuento de vis, 1 de warp |
| Crimson Blade | `crimson_blade` | 7.5 de daño | Ver abajo |

- **Placas**: durabilidad factor 18, encantabilidad 13, suenan a hierro.
- **Túnica**: durabilidad factor 17, encantabilidad 13, suena a cota de malla.
- Todo se repara con lingotes de hierro y es raro (nombre amarillo).
- Las placas y la túnica tienen su modelo 3D propio: la de placas con hombreras, tabardo y capa, y la túnica con capucha, libro, pergamino y faldones. La capa y los faldones se mueven al caminar.

## Crimson Blade

Épica (nombre violeta). Tiene 200 usos y encantabilidad 20.

- Se repara sola: 1 punto por segundo mientras la tengas encima.
- A lo que le pegás le da Weakness 3 segundos y Hunger 6 segundos. Si es otro jugador, solo con PvP activado.
- Da 2 de warp.
- En el tooltip dice "Greater Sapping" en dorado.

## Warp del equipo y descuento de vis

Hay una API nueva, `IWarpingGear`, para los ítems que dan warp mientras los tenés puestos o en la mano.

- El tooltip ahora muestra el warp de esos ítems ("Warping 1"), sumado al warp que te dejó la infusión.
- También muestra el descuento de vis ("Vis discount: 1%").
- El warp del equipo todavía no hace nada: lo usan los eventos de warp (etapa 9d-5).
- El descuento ya se aplica al lanzar focos.

Los aspectos son los del original. Cada pieza tiene los aspectos de su equivalente de vanilla (hierro para las placas y las botas, cuero para la túnica) más Alienis 5. La túnica suma Praecantatio 5. La espada tiene los de la espada de hierro más Alienis 10 y Mortuus 10.

## Diferencias con el original

- En 1.12 el modelo copiaba a mano toda la animación del jugador. Ahora NeoForge le pasa la pose del que lo lleva puesto, así que el modelo solo agrega lo propio: la cabeza un 1% más grande y la capa y los faldones que se mueven con las piernas.
- La espada se reparaba con lingotes de metal del vacío. Todavía no existen, así que por ahora no se puede reparar en el yunque (se repara sola igual).
- La armadura de placas usa otra textura cuando la lleva un zombie habitado. Ese bicho llega en la 9d-3; mientras tanto siempre usa la textura del culto.

## Cómo probarlo

1. Entrá en creativo y buscá las piezas en la pestaña de Thaumcraft.
2. Ponete el set de placas y caminá en tercera persona (F5): la capa se mueve.
3. Ponete el set de túnica y mirá el tooltip: dice el warp y el descuento.
4. Pegale a un mob con la Crimson Blade: le aparecen Weakness y Hunger.
5. Gastá un poco la espada y esperá: se va reparando.
