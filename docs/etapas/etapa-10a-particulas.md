# Etapa 10a: partículas originales

Hasta ahora casi todos los efectos usaban partículas de vanilla de relleno. Ahora se dibujan con la hoja de partículas original de Thaumcraft (`textures/misc/particles.png`) y con el mismo motor que tenía el mod.

## Qué cambió

- **Motor propio de partículas**, igual al de 1.12:
  - 4 capas: dos aditivas (brillan) y dos normales, y de cada una una versión que se ve a través de los bloques.
  - Límite de partículas por capa según tu ajuste de partículas (500 / 1000 / 2000). Con menos de 30 FPS dibuja menos.
  - Soporta efectos con demora, como el escaneo del taumómetro.
  - Un shader propio, porque el de vanilla corta las partículas muy transparentes.
- **Partículas portadas**: genérica (la de casi todo), mota de fuego, chispa de vis, vapor de ventilación, voluta, espiral de humo, runas de bloque, planos, burbujas de baba, chispa y pedacitos del perforador, y bloques que se rompen y se desvanecen.
- **Paquetes nuevos**: el "bamf" (la nube de humo de magia), la polución de flux que sale de los aparatos, y el corte del encantamiento de arco.

## Dónde se nota

- **Crisol**: espuma, burbujas del color del aspecto, espuma que rebalsa con más de 500 de vis, hervor al tirar items y el bamf al crear algo.
- **Matriz de infusión**: runas sobre el pedestal central, los items y bloques que vuelan de los pedestales a la matriz, la esencia del jugador, y las chispas violetas cuando está inestable o hay bloques mal puestos.
- **Pedestal**: bamf violeta cuando la inestabilidad tira el item, bamf normal al terminar.
- **Nitor**: la llama y el núcleo brillante, del color del bloque.
- **Mesa de investigación**: motitas que caen sobre la nota.
- **Focos**: las estelas de aire, tierra, fuego, intercambio, flux, rotura, curación, grieta, escarcha y maldición.
- **Golems**: los iconitos de tarea tomada, tarea soltada, quedate quieto y subida de rango.
- **Sellos**: un sello apagado por redstone tira una chispa roja de vez en cuando.
- **Agujero del portal**: chispitas azules en los bordes.
- **Vis pura (fluido)**: motitas blancas que suben.
- **Savia (efecto del foco)**: chispas violetas y el sonido eléctrico.
- **Polución**: cada vez que un aparato contamina el aura sale humo de flux.
- **Encantamiento de arco**: el corte entre los enemigos.
- Y además: herramientas elementales, plantas mágicas, frascos, tubos, horno infernal, condensador, levitador, rociador de pociones, espejos, manipulador focal, polvo mágico, taint, cultistas, guardianes, el gólem eldritch y los modificadores de campeón.

## Bugs del original que quedan igual

- La grieta de flux tira un bamf **negro**: el original le pasa color 0.
- El burbujeo de la baba de flux usa un índice de textura corrido, como en el original.
- La partícula genérica en modo con ángulo extrapola mal el giro entre frames. Se ve igual que en 1.12.
- Las partículas siguen avanzando con el juego en pausa, igual que antes.

## Diferencias con el original

- Los bloques que se rompen (perforador, desvanecer) usan el atlas de bloques de vanilla en vez del de 1.12. Se ven igual.
- Los planos se dibujan de los dos lados en vez de apagar el culling.

## Lo que todavía falta (etapa 10b)

- Rayos y chorros: arcos eléctricos, rayo del bastón, rayo del perforador, chorros de esencia, de vacío y de agua, el grito sónico del guardián.
- El escudo de runas (encantamiento rúnico) y el escudo del pedestal estabilizador.
- La protección de bloques (warding).
- Las chispas dentro de menús (la mesa arcana y similares).
- Por ahora esos siguen con el reemplazo provisorio.

## Cómo probarlo

1. `/thaumcraft research @s all`.
2. Prendé un crisol con agua y tirale items: tiene que hacer espuma y burbujas de colores.
3. Armá una infusión y mirá los items volando hacia la matriz. Sacá un pilar para ver las chispas de inestabilidad.
4. Poné un nitor en el piso y mirá la llama.
5. Tirá hechizos con focos de aire, fuego y tierra.
6. Usá el taumómetro mirando el piso: tienen que aparecer las runas de los minerales con demora según la distancia.
