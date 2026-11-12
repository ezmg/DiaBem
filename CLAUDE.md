# CLAUDE.md — Normas del proyecto DiaBem

DiaBem (pt-BR) / DiaBien (es) / DiaWell (en) es una app gratuita, sin ánimo de lucro y de código
abierto (Apache 2.0), de la marca Velkyss. Ayuda a personas con diabetes tipo 2 y/o tensión alta a
decidir qué cocinar con los ingredientes que tienen.

## Principios innegociables (no cambiarlos sin permiso explícito del responsable)

1. **Coste cero**: sin servidor, sin IA en tiempo de ejecución, sin APIs de pago. Funciona sin internet.
2. **Privacidad**: el perfil de salud se queda solo en el dispositivo. Sin analítica, sin rastreo, sin
   cuentas. La app Android no pide el permiso `INTERNET`.
3. **Seguridad sanitaria**: NUNCA calcular ni sugerir dosis de insulina ni de medicamentos. Mostrar
   siempre el aviso de que las recetas no sustituyen al médico o nutricionista.
4. **Datos fiables**: los nutrientes se calculan SIEMPRE a partir de tablas oficiales de composición
   (TACO para Brasil). Nunca inventar valores. Si falta un dato, se deja vacío y se marca.
5. **Accesibilidad para mayores**: letra grande, alto contraste, botones grandes (mínimo 56 dp),
   pocos pasos, lenguaje sencillo.
6. **Varios países**: todo lo que depende del país va en un "paquete de país" (`data/<país>/`).

## Idiomas

- Código, identificadores, comentarios y mensajes de commit: **inglés**.
- Interfaz: **portugués de Brasil** primero (`values/`), luego `values-es/` y `values-en/`.
- Documentación para el responsable y resúmenes: **español de España**, sencillo, sin anglicismos
  innecesarios.
- La interfaz se muestra SIEMPRE en el idioma del paquete de país (`pack.json` → `locale`), no en el
  del dispositivo: recetas e interfaz deben ir en el mismo idioma (`ui/AppLocale.kt`).
- El nombre de la app va SIEMPRE en recursos de texto traducidos (`app_name`), nunca en el código.

## Estructura

| Carpeta | Contenido |
|---|---|
| `core/` | Lógica y modelos en `commonMain` (KMP: jvm, wasmJs; iOS preparado). Sin dependencias de UI. |
| `composeApp/` | Interfaz Compose Multiplatform. Destinos: Android (principal) y web (wasmJs). |
| `data/` | Paquetes de país (tablas de alimentos, medidas, recetas, criterios de salud). |
| `iosApp/` | Reservado para iOS (requiere Mac). |
| `backlog/` | Tareas con Backlog.md. |
| `docs/` | Documentación adicional. |

iOS está declarado pero desactivado: `diabem.ios.enabled=false` en `gradle.properties`.

## Comandos

```bash
./gradlew check                                  # todas las pruebas
./gradlew :core:jvmTest                          # pruebas rápidas de la lógica
./gradlew :composeApp:desktopTest                # pruebas de interfaz (sin dispositivo)
./gradlew :composeApp:assembleDebug              # APK de depuración
./gradlew :composeApp:wasmJsBrowserDistribution  # web en composeApp/build/dist/wasmJs/productionExecutable
```

## Forma de trabajar

- Commits pequeños con mensajes claros (en inglés, en imperativo).
- Cada tarea del plan está en `backlog/tasks/`. Actualizar su estado al empezar y al terminar
  (`backlog task edit <id> -s "In Progress"` / `-s Done`).
- Ante varias opciones razonables: elegir la más sencilla y justificarla en el resumen.
- **Parar y preguntar** antes de: cualquier cosa con coste, publicar en tiendas, o tocar un principio
  innegociable.
- No copiar recetas de webs con derechos de autor. Las recetas originales llevan
  `"reviewedByNutritionist": false`.
- Umbrales de salud: solo con fuente oficial documentada; si no hay fuente clara, marcar como
  pendiente de revisión por nutricionista.
- Toda fuente de datos nueva se documenta en `DATA_SOURCES.md` con su licencia.
