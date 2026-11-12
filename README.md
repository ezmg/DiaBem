# DiaBem

**Receitas simples para quem tem diabetes tipo 2 ou pressão alta.**
Gratuita, sem anúncios, sem cadastro e funciona sem internet.

DiaBem (Brasil) · DiaBien (España) · DiaWell (English) — un proyecto de código abierto de **Velkyss**.

---

## Qué es

Una app para personas mayores con diabetes tipo 2 y/o tensión alta. La persona indica:

1. qué tipo de comida quiere preparar (desayuno, almuerzo, merienda o cena), y
2. qué ingredientes tiene en casa,

y la app le propone varias recetas adecuadas a su perfil, con un semáforo sencillo para los
**hidratos de carbono** y la **sal**.

> ⚠️ **Aviso**: las recetas son orientativas. No sustituyen al médico ni al nutricionista.
> La app **nunca** calcula ni sugiere dosis de insulina ni de medicamentos.

## Principios

- **Coste cero**: sin servidores ni servicios de pago. Todo funciona dentro del teléfono.
- **Privacidad total**: el perfil de salud no sale del dispositivo. Sin analítica ni rastreo.
  La app de Android ni siquiera pide permiso de acceso a internet.
- **Datos oficiales**: los nutrientes se calculan con la Tabla Brasileña de Composición de
  Alimentos (TACO, NEPA/Unicamp). Ver [DATA_SOURCES.md](DATA_SOURCES.md).
- **Fácil de usar**: letra grande, alto contraste, botones grandes y lenguaje sencillo.
- **Preparada para más países**: cada país es un paquete de datos independiente.

## Pruébala

- **Web** (también desde el iPhone con Safari 18.2 o superior): se publica en GitHub Pages en cada
  cambio de la rama `main` → `https://<usuario>.github.io/diabem/`
- **Android**: en la pestaña *Actions* de GitHub, abre la última ejecución correcta de **CI** y
  descarga el artefacto `diabem-debug-apk`.

## Tecnología

- Kotlin Multiplatform + Compose Multiplatform.
- Destinos: Android (principal) y web (Kotlin/Wasm). iOS preparado en la estructura, aún sin compilar.
- Datos en archivos JSON legibles dentro del repositorio, validados al compilar.
- Integración continua con GitHub Actions; tareas con [Backlog.md](backlog/).

```bash
./gradlew check                                  # pruebas
./gradlew :composeApp:assembleDebug              # APK de depuración
./gradlew :composeApp:wasmJsBrowserDistribution  # versión web
```

## Estructura

```
core/         lógica y modelos comunes (sin interfaz)
composeApp/   interfaz para Android y web
data/         paquetes de país: alimentos, medidas caseras, recetas, criterios de salud
backlog/      tareas del proyecto
docs/         documentación
```

## Cómo colaborar

- **Nutricionistas**: cada receta tiene el campo `reviewedByNutritionist`. Las revisiones son muy
  bienvenidas: abre una *issue* o una *pull request* sobre el archivo de la receta en `data/`.
- **Desarrolladores**: lee [CLAUDE.md](CLAUDE.md) para conocer las normas del proyecto.

## Licencia

Código bajo licencia [Apache 2.0](LICENSE). Los datos de composición de alimentos pertenecen a sus
autores y se usan según sus condiciones (ver [DATA_SOURCES.md](DATA_SOURCES.md)).
