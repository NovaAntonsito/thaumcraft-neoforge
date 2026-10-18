# Etapa 9d-4c: los túmulos

Con esto se cierra la 9d-4. Vuelven los túmulos del original: lomas de pasto con una cripta de piedra adentro.

## Cómo son

- Una loma de 19 x 19 bloques, de pasto y tierra, con pasto alto arriba.
- Del lado oeste hay una entrada con escalones y barrotes de hierro que da a una sala grande de piedra y piedra musgosa.
- En la sala hay dos **generadores de monstruos**: uno de esqueletos y uno de zombies.
- Una escalera de mano baja a una **bóveda** con:
  - Dos **urnas o cajas de botín**. Cada una es rara 10% de las veces, poco común 23% y común el resto, y es caja 30% de las veces y urna el resto.
  - Un **cofre** con el botín de las mazmorras de vanilla. 1 de cada 3 es un **cofre trampa con TNT abajo**.

## Dónde salen

- En el overworld, en 1 de cada 100 chunks se intenta poner uno, en un punto cerca del centro del chunk.
- Solo sale si las cuatro esquinas y el centro de arriba caen sobre piedra, pasto o tierra (o nieve o pasto alto encima), con aire arriba.
- No salen en mundos planos ni si apagás `generateStructure`.
- Los chunks que no tienen túmulo siguen con su chance de 1 en 500 de tener un portal carmesí menor, como antes.

## Diferencias con el original

- **Cómo está guardado**: el original eran 2400 líneas de "poné este bloque acá". Acá son los mismos bloques puestos como capas de letras. El resultado es el mismo.
- **Escalones y barrotes**: en 1.21 guardan su forma (esquinas y conexiones), así que la calculo con los bloques de al lado después de poner todo. En 1.12 se calculaba al dibujarlos.
- **Pasto alto**: es el pasto corto de 1.21. Para chequear el piso también cuento helechos y arbustos secos, que en 1.12 eran el mismo bloque que el pasto alto.

## Cómo probarlo

1. Creá un mundo nuevo y recorrelo en llanuras o bosques. Buscá una loma redonda de pasto con una entrada de piedra.
2. Entrá: tiene que haber dos generadores largando esqueletos y zombies.
3. Bajá por la escalera de mano a la bóveda y rompé las urnas o cajas.
4. Abrí el cofre. Si es trampa (tiene borde rojo), salí rápido.
