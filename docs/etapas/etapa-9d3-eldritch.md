# Etapa 9d-3: el zombie habitado, el cangrejo y el guardián eldritch

Llegan los primeros bichos eldritch: el guardián fantasma del bioma Eerie y el cangrejo con casco que se mete adentro de los zombies.

## Los bichos

| Bicho | Id | Vida | Qué hace |
|---|---|---|---|
| Eldritch Guardian | `eldritch_guardian` | 50 | Pega fuerte (7) de cerca. De lejos tira orbes o grita. Ver abajo. |
| Eldritch Crab | `eldritch_crab` | 20 | Salta a la cabeza del objetivo y lo garra desde ahí. |
| Shambling Husk | `inhabited_zombie` | 30 | Zombie con armadura carmesí. Cuando muere sale el cangrejo. |

Los tres son eldritch: no se atacan entre ellos (el guardián no ataca a ningún eldritch) y atacan a jugadores y cultistas. Los cultistas ahora también atacan al guardián.

## Guardián eldritch

- Aparece en el bioma Eerie, peso 1, de a uno. Puede aparecer con cualquier luz, pero nunca con otro guardián a menos de 32 bloques. Config nueva: `allowSpawnElder`.
- Es semitransparente: como mucho 60% opaco, y se va borrando con la distancia (desaparece a 32 bloques, 24 en difícil).
- De lejos (más de 8 bloques):
  - 85% de las veces tira un **orbe eldritch** desde un costado o el otro, y levanta ese brazo.
  - Si no, y te ve, **grita**: wither por 20 segundos y 1 a 3 de warp temporal.
- El orbe explota al tocar algo. Hace daño mágico (2/3 del ataque del guardián) y debilidad por 8 segundos a todo lo vivo a 2 bloques, menos a los no-muertos.
- Recibe la mitad del daño mágico. Es no-muerto (le pega Smite, la curación lo daña).
- Mientras está cerca (16 bloques, 24 en difícil, no en fácil) **mete niebla**: cada 5 segundos la renueva por al menos 10 segundos. La niebla sube de a poco y se va en los últimos 5 segundos.
- Se puede escanear (`!EldritchGuardian`). Tiene Alienis 20, Mortuus 20 y Exanimis 20.

## Cangrejo y zombie habitado

- El cangrejo con casco tiene 5 de armadura. Cuando baja a la mitad de vida el casco se rompe (suena).
- Sin casco salta a la cabeza del que ataca y le pega cada medio a un segundo. Tiene 20% de chance de soltarse en cada golpe.
- Sale con casco siempre en difícil, si no 1 de cada 3. En difícil a veces sale con un efecto, como las arañas.
- Es artrópodo (Bane of Arthropods) y no le hace nada el veneno.
- Si lo mata un jugador, 1 de cada 3 suelta una perla de ender (más con Looting).
- El zombie habitado sale con el casco de placas y 60% (90% en difícil) de pechera y grebas. No suelta nada. Al morir sale el cangrejo con casco a la altura de la cabeza, larga la experiencia y desaparece en un puf.
- La armadura de placas se dibuja con la textura de zombie cuando la lleva él.
- Ambos se escanean como `!EldritchCrab`. Cangrejo: Alienis 10, Bestia 10, Vinculum 10. Zombie: Alienis 10, Exanimis 10, Humanus 5.

## Otros cambios

- **Packets nuevos**: `PacketMiscEvent` (latido de warp y niebla) y `PacketFXSonic` (el grito). El latido ya funciona, pero nada lo manda hasta la 9d-5. Config nueva de gráficos: `nostress` (apaga el latido).
- **Escaneo `!ORMOB`**: cualquier bicho eldritch cuenta, como en el original.
- **Savia (focus de maldición)**: ya no daña a los bichos eldritch, como en el original.
- **Zombies furiosos**: ahora son no-muertos para Smite y las pociones de curación, que en el original lo eran y en el port se había perdido.
- Huevos de los tres bichos.

## Diferencias con el original

- **Niebla**: en 1.12 era niebla exponencial. En 1.21 la niebla es lineal, así que acerco el final de la niebla a donde la original ya tapaba todo. Se ve parecido, no igual. Bajo el agua o la lava no cambia nada.
- **Partículas**: son de vanilla hasta la etapa 10. Las estelas oscuras del guardián son humo y el grito es la onda del Warden.
- **Rayos del orbe**: los dibujo con los rayos del dragón de ender, que tienen la misma mezcla. En 1.12 las puntas quedaban casi transparentes por un desborde de colores; lo mantuve.
- **Velocidad del cangrejo**: el original quería 0.275 con casco y 0.3 sin, pero el valor se calculaba antes de ponerle el casco, así que siempre era 0.3. Lo dejé en 0.3.
- **Fuego del guardián**: el original iba a prender fuego al objetivo cuando el guardián se quemaba sin nada en la mano, pero comparaba la mano con null y eso nunca pasa. No lo porté.
- **Casco roto**: en el original solo se escuchaba el sonido; así quedó.
- **Montado**: en 1.21 el cangrejo ya se sienta arriba de la cabeza, así que no hace falta el ajuste de altura del original.
- **Muerte del zombie**: como en el original, no dispara el evento de muerte. Por eso no cuenta para las estadísticas de bichos matados.
- **Tierras exteriores**: el guardián tenía absorción extra y no metía niebla en esa dimensión. La dimensión no está portada.
- **Warden**: la textura y el ojo brillante del modelo quedan listos para el jefe de la 9d-4.

## Cómo probarlo

1. Creativo, tirá un huevo de Eldritch Guardian en supervivencia y alejate más de 8 bloques: te tira orbes y a veces grita (wither y warp temporal).
2. Quedate cerca: aparece la niebla. Matalo y en unos segundos se va.
3. Tirá un huevo de Shambling Husk y matalo: sale el cangrejo con casco. Pegale hasta que se rompa el casco y mirá cómo te salta a la cabeza.
4. Andá a un bioma Eerie de noche y buscá guardianes.
5. Escaneá los tres con el taumómetro.
