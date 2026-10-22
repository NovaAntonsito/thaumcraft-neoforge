# Thaumcraft para Minecraft 1.21.1 (NeoForge)

Esto es Thaumcraft 6 porteado a Minecraft 1.21.1 con NeoForge. Sí, a mano, etapa por etapa, como un boludo. Pero anda.

## Qué hay en cada branch
- `main`: el port fiel a TC6 (etapas 1 a 7b + el fix de primales). Acá no se inventa nada.
- `version-sonada`: lo mismo que `main` más las cosas mías que no están en Thaumcraft (anomalías del mundo y diseños). Si buscás lo original, no es acá, che.

## Cómo está armado
Es un solo proyecto NeoForge (ModDevGradle). Cada etapa del port se metió encima de la anterior, en este orden:
1, 2, 3, 4a, 4a-aspectos, 4b, 4c, 4c-hud, 4d, 5a, 5b, 5c, 6a, 6b (+ Ponder), 6c, 6d, 7a, 7b, fix-primales.

La doc de cada etapa está en `docs/etapas/`. Si algo se rompe, mirá ahí primero antes de putear.

La traducción al español (`es_es.json`) y sus criterios están en `docs/traduccion-es.md`.

## Qué necesitás
- Java 21. Con otra versión no compila, no insistas.
- Gradle lo baja el wrapper solo, no hace falta instalar nada más.
- Versiones: Minecraft 1.21.1, NeoForge 21.1.256 (está todo en `gradle.properties`).

## Compilar y probar
```
./gradlew runClient
```
Te abre el juego con el mod cargado. La primera vez tarda una banda porque baja todo, aguantá.

Cada push a `main` y cada PR corren un build en GitHub Actions (`.github/workflows/build.yml`) que hace `./gradlew compileJava`. Si eso queda en rojo, la cagaste en algo: arreglalo antes de mergear.
