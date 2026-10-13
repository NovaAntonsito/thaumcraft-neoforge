# Etapa 6a: tubos de esencia y fundición mejorada

Con esto la esencia se mueve sola: los alambiques, los frascos y los tubos se conectan entre sí. También llegan los fuelles, las fundiciones de taumio y de vacío, la bomba auxiliar, el respiradero, la centrífuga y el alumentum.

## Tubos (investigación Essentia Tubes)
- **Tubo normal**: lleva un punto de esencia a la vez hacia la succión más fuerte. Se conecta solo a tubos, frascos, alambiques, el búfer y la centrífuga.
- **Válvula**: se cierra con clic derecho o con una señal de redstone. Cerrada no pasa succión y la manija se enrosca.
- **Tubo con filtro**: clic derecho con un frasco, una etiqueta o un vial con esencia y solo deja pasar esa esencia; el centro toma su color. Agachado y con la mano vacía se quita el filtro.
- **Tubo restringido**: deja pasar la mitad de la succión.
- **Tubo direccional**: solo acepta succión de un lado, marcado con una banda azul.
- **Búfer**: guarda hasta 10 puntos de cualquier mezcla, con succión 1. Con fuelles apuntándolo, la succión pasa a 32 por fuelle. Con el comparador da una señal según lo lleno que está.
- **Con el lanzador (caster)**: clic en un brazo del tubo lo conecta o lo desconecta (también en el tubo vecino). Clic en el centro gira la válvula o el tubo direccional. En el búfer, clic agachado en un brazo lo estrangula a succión 1 (banda azul) o 0 (banda roja).
- **Resaltado**: con el lanzador o el resonador en la mano, el contorno marca solo la parte que estás mirando.
- **Gases**: si dos succiones de distinta esencia chocan, el tubo larga gas del color de la esencia (gris si no tiene tipo).
- Al romper un tubo con esencia adentro, esa esencia va al aura como flux.

## Resonador de esencia
Clic derecho sobre un tubo o cualquier aparato de esencia: dice en el chat cuánta esencia tiene y qué succión aplica. En un tubo mide el brazo que estás mirando; en el búfer lista todo lo que guarda.

## Alambiques
Ahora sueltan esencia a un tubo pegado a su costado, y se dibuja la boquilla de bronce hacia ese lado.

## Fundición
- **Fuelles** (investigación Arcane Bellows): se ponen apuntando al bloque que clickeás. Contra una fundición, cada fuelle en un costado o atrás acelera la fundición un 12,5 %. Contra un horno común lo acelera. Se apagan con redstone. Contra un búfer se dibuja una boquilla.
- **Fundición de taumio**: pierde menos esencia como flux (aprovecha el 90 % contra el 80 % de la básica) y la pasa a los alambiques un 50 % más rápido. **Fundición de vacío**: aprovecha el 95 %.
- **Bomba auxiliar** (Improved Smelting): pegala a un costado de la fundición (no al frente). Los alambiques apilados sobre la bomba también destilan.
- **Respiradero** (Improved Smelting 2): pegado a un costado, cada uno tiene 1 de 3 de soltar al aire un punto de flux en vez de mandarlo al aura.
- **Alumentum** (investigación Alumentum): crisol con carbón + 10 de Potentia, 10 de Ignis y 5 de Perditio. Como combustible dura 4800 ticks y acelera la fundición. Si lo tirás con clic derecho, o lo dispara un dispensador, explota y prende fuego.
- **Construcción alquímica**: el bloque que piden las fundiciones mejoradas, la bomba, el respiradero y la centrífuga.

## Centrífuga de esencia (Essentia Centrifuge)
Toma una esencia compuesta desde abajo, gira dos segundos y deja arriba uno de sus dos componentes al azar. Con redstone se detiene.

## Diferencias con el original
- Las conexiones de los tubos se guardan en el bloque y se recalculan al cambiar algo cerca (en 1.12 se calculaban en cada dibujado).
- La velocidad extra del alumentum tenía un error en TC6 (`speed *= (int) 0.8`, que daba 0). Se usa lo que se quería: 80 % del tiempo.
- La investigación de la fundición de taumio pedía fabricar `thaumcraft:metal;1;2`, que no existía. Ahora pide la construcción alquímica.
- Las partículas de gas y del alumentum son provisorias (polvo de color, llamas y humo) hasta la etapa de efectos.

## Pendiente
- **Construcción alquímica avanzada**: su receta lleva la perla primordial, que no está portada. Por eso la fundición de vacío todavía no se puede fabricar.
- El condensador (el resonador mostraría su costo).

## Cómo probar
1. `/thaumcraft research @s all` para tener las recetas (o seguí las investigaciones).
2. Fundición con alambique arriba, un tubo al costado del alambique y un frasco en la punta del tubo: la esencia tiene que llegar al frasco.
3. Agregá una válvula en el medio y cerrala con clic derecho: se corta el paso.
4. Usá el resonador sobre el tubo y sobre el frasco.
5. Poné dos fuelles a los costados de la fundición y mirá que funda más rápido.
6. Centrífuga encima de un frasco con una esencia compuesta, y otro frasco arriba con un tubo.
