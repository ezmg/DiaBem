# Publicación

## 1. APK firmado en GitHub Releases (sin coste)

El flujo `.github/workflows/release.yml` compila, pasa las pruebas, firma y publica en GitHub
Releases un **APK** (para instalar directamente) y un **AAB** (el formato que pide Google Play).

### Preparación (una sola vez): la clave de firma

Android exige que todas las versiones de una app estén firmadas con **la misma clave**. Si se
pierde, los teléfonos que ya tengan la app no podrán actualizarla. Por eso:

- La clave **no está en el repositorio**. Se guarda en los *secretos* de GitHub.
- Hay que guardar **una copia de seguridad** fuera de GitHub (los secretos no se pueden volver a leer).

Secretos necesarios (Settings → Secrets and variables → Actions → New repository secret):

| Secreto | Contenido |
|---|---|
| `DIABEM_KEYSTORE_BASE64` | El archivo de claves (`.jks`) codificado en base64 |
| `DIABEM_KEYSTORE_PASSWORD` | Contraseña del archivo de claves |
| `DIABEM_KEY_ALIAS` | Nombre de la clave dentro del archivo |
| `DIABEM_KEY_PASSWORD` | Contraseña de la clave |

Para crear la clave en un ordenador con Java:

```bash
keytool -genkeypair -keystore diabem.jks -alias diabem -keyalg RSA -keysize 4096 \
  -validity 10000 -dname "CN=Velkyss DiaBem"
base64 -w0 diabem.jks   # el resultado es DIABEM_KEYSTORE_BASE64
```

### Publicar una versión

- Desde el móvil: pestaña **Actions** → **Release** → **Run workflow** → escribir la versión
  (por ejemplo `1.0.0`).
- O creando una etiqueta `v1.0.0`.

El resultado aparece en la pestaña **Releases** con `diabem-1.0.0.apk` y `diabem-1.0.0.aab`.
Si falta algún secreto, el flujo se detiene con un mensaje claro y no publica nada.

## 2. Google Play (tiene coste: 25 USD una sola vez)

> ⚠️ No se ha dado ningún paso de pago. Crear la cuenta de desarrollador es decisión del responsable.

Material ya preparado en `docs/store/`:

| Qué pide Google Play | Dónde está |
|---|---|
| Título, descripción corta y completa (pt-BR) | `docs/store/listing-pt-BR.md` |
| Lo mismo en español (para cuando exista el paquete de España) | `docs/store/listing-es.md` |
| Icono 512 × 512 | `docs/store/graphics/icon-512.png` |
| Gráfico de cabecera 1024 × 500 | `docs/store/graphics/feature-1024x500.png` |
| Capturas de teléfono 1080 × 1920 | `docs/store/screenshots/` |
| Política de privacidad (URL pública) | `https://ezmg.github.io/DiaBem/privacy.html` |
| Archivo de la app (AAB) | GitHub Releases |

Las capturas se regeneran con:

```bash
./gradlew :composeApp:desktopTest -Pdiabem.screenshots=docs/store/screenshots
```

(Se generan en escritorio: la letra es algo más ancha que en un teléfono Android real. Conviene
sustituirlas por capturas de un teléfono antes de publicar.)

### Lista de comprobación antes de enviar a Google Play

- [ ] Cuenta de desarrollador creada (25 USD) — **decisión del responsable**.
- [ ] Comprobar el nivel de API mínimo que exige Google Play ese día. La app apunta a la API 35
      (`android-targetSdk` en `gradle/libs.versions.toml`); si piden una superior, hay que subirla.
- [ ] Formulario "Seguridad de los datos": la app **no recoge ni comparte datos**.
- [ ] Declaración de apps de salud: es una app de recetas y nutrición, **no** un dispositivo médico;
      no diagnostica ni trata, y muestra el aviso médico en el primer uso y en cada receta.
- [ ] Clasificación de contenido: cuestionario (sin violencia, sin compras, sin anuncios).
- [ ] Público objetivo: adultos.
- [ ] Recetas revisadas por un nutricionista, o mantener visible el aviso "ainda não revisada".
- [ ] Confirmación del IBGE sobre la reutilización de sus datos (ver `DATA_SOURCES.md`).
- [ ] Prueba con TalkBack en un teléfono real (`docs/accessibility.md`).
- [ ] Las cuentas personales nuevas de Google Play deben hacer una prueba cerrada con varios
      probadores durante unos días antes de poder publicar: comprobar el requisito vigente.
