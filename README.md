# Thaumcraft para Minecraft 1.21.1 (NeoForge)

Port de Thaumcraft 6 a Minecraft 1.21.1 con NeoForge.

## Branches
- `main`: port fiel a TC6 (etapas 1 a 7b + fix de primales).
- `version-sonada`: `main` más lo propio que no está en Thaumcraft (anomalías del mundo y diseños).

## Estructura
Un solo proyecto NeoForge (ModDevGradle). Cada etapa del port se aplicó encima de la anterior, en este orden:
1, 2, 3, 4a, 4a-aspectos, 4b, 4c, 4c-hud, 4d, 5a, 5b, 5c, 6a, 6b (+ Ponder), 6c, 6d, 7a, 7b, fix-primales.
La documentación de cada etapa está en `docs/etapas/`.

## Compilar
```
./gradlew runClient
```
