# Etapa 9d-5: los eventos de warp

El warp ahora hace cosas. Hasta acá se juntaba pero no pasaba nada. Ahora, como en el original, cuanto más warp tenés, peores cosas te pasan.

## Cómo funciona

- Cada 2000 ticks (unos 100 segundos) perdés 1 de warp temporal y se tira la chance de un evento.
- La chance crece con el **contador de warp**, que sube cada vez que ganás warp y baja con cada evento.
- La fuerza del evento depende de tu warp total, más el warp de lo que tenés en la mano y puesto.
- No pasa nada en `wussMode` ni con **Warp Ward** activo.
- Suena el latido, salvo con `nostress`.

## Los eventos, de más suave a más fuerte

- Ruidos: un creeper a punto de explotar, una explosión lejos.
- Mensajes raros: "Something is behind you", "Something is following you", "Something is watching you".
- **Flux Flu** o **Flux Phage**.
- **Thaumorrhea** (nueva): vas dejando charcos de flux goo.
- **Unnatural Hunger** (nueva): te da hambre todo el tiempo y la pantalla se pone roja. La comida normal no sirve. La carne podrida o un cerebro de zombie le sacan un nivel y 30 segundos.
- **Niebla eldritch**, a veces con guardianes eldritch adentro (hasta 8).
- **Blurred Vision** (nueva): la pantalla se ve borrosa.
- **Sun Scorned** (nueva): la luz fuerte al aire libre te prende fuego y la oscuridad te cura un poco. La pantalla brilla.
- Fatiga minera.
- Visión nocturna ("Your perception suddenly expands").
- **Deadly Gaze** (nueva):
  - Lo que mirás a 8 bloques (3 más por nivel, hasta 24) se marchita y te ataca.
  - La pantalla pierde color y se oscurece en los bordes, latiendo.
- **Arañas mentales**: falsas (solo las ves vos y no hacen daño) o, más fuerte, reales.
- Ceguera.
- Un momento de claridad: perdés 1 de warp normal.
- Un **portal carmesí menor** cerca tuyo.

## Otras cosas

- Con más de 10 de warp permanente y normal te aparece la pista de las sales de baño.
- Ahora te avisa cuando ganás o perdés warp, y susurra.
- Los efectos nuevos tienen sus íconos del original, y la leche no los cura, como en el original.
- Config nueva `disableShaders` (en gráficos) para apagar los efectos de pantalla.

## Diferencias con el original

- **Ruidos**: el original los tocaba para todos los que están cerca **menos vos**. Así que vos no los escuchás. Lo dejé igual.
- **Warp permanente**: al ganarlo solo susurra, el texto nunca se mostraba. Perder warp permanente o temporal tampoco avisa. Como en el original.
- **Shaders**: en 1.21 los efectos de pantalla son cadenas de post-proceso propias. Pasé los shaders al formato nuevo (GLSL 150) y al namespace `thaumcraft`, así no pisan los de vanilla. Se aplican antes de dibujar el HUD, igual que antes.
- **Viñeta**: como en el original, se dibuja dos veces por frame, así que también aparece el doble de rápido.
- **Pendiente para la 9e**:
  - La máscara de la armadura de fortaleza, que suaviza los eventos.
  - El warp de los baubles puestos (con Curios).

## Cómo probarlo

1. Date warp con `/thaumcraft warp add @s permanent 50` y esperá unos 100 segundos (puede tardar varios ciclos en tocarte uno).
2. Para probar un efecto directo: `/effect give @s thaumcraft:death_gaze 60`, y lo mismo con `blurred_vision`, `unnatural_hunger`, `sun_scorned` y `thaumarhia`.
3. Con Unnatural Hunger, comé pan (no sirve) y después carne podrida.
4. Con Deadly Gaze, mirá a un bicho: se tiene que marchitar y venir por vos.
