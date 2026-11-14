# Google Play Console: qué poner en cada apartado

Guía para rellenar la ficha desde el móvil. Los nombres de los apartados pueden variar un poco,
porque Google cambia la consola con frecuencia: lo importante es la respuesta.

## Crear la app

| Campo | Respuesta |
|---|---|
| Nombre de la app | `DiaBem: Receitas p/ Diabetes` |
| Idioma predeterminado | Portugués (Brasil) – pt-BR |
| App o juego | App |
| Gratis o de pago | Gratis |

## Ficha de Play Store (pt-BR)

| Campo | De dónde sale |
|---|---|
| Descripción breve y completa | `listing-pt-BR.md` |
| Icono de la app (512 × 512) | `graphics/icon-512.png` |
| Gráfico de funciones (1024 × 500) | `graphics/feature-1024x500.png` |
| Capturas de teléfono (mínimo 2) | `screenshots/` (6 imágenes de 1080 × 1920) |
| Categoría | Salud y bienestar (*Saúde e fitness*) |
| Correo de contacto | Un correo tuyo que quieras hacer público (obligatorio) |
| Sitio web (opcional) | `https://ezmg.github.io/DiaBem/` |

## Contenido de la app

| Apartado | Respuesta |
|---|---|
| Política de privacidad | `https://ezmg.github.io/DiaBem/privacy.html` |
| Acceso a la app | Todas las funciones están disponibles sin acceso especial (no hay cuentas) |
| Anuncios | No, la app no contiene anuncios |
| Clasificación de contenido | Cuestionario: categoría "Utilidad / Productividad / Otros"; sin violencia, sexo, lenguaje soez, drogas, apuestas ni contenido generado por usuarios |
| Público objetivo | 18 años o más. No está dirigida a niños |
| App de noticias | No |
| App gubernamental | No |
| Funciones financieras | Ninguna |

### Seguridad de los datos

| Pregunta | Respuesta |
|---|---|
| ¿La app recoge o comparte datos de usuario? | **No** |
| ¿Los datos se cifran en tránsito? | No aplica: no se transmite ningún dato |
| ¿Se puede pedir el borrado de los datos? | No aplica: no hay datos fuera del dispositivo. En la app: Ajustes → "Apagar meus dados deste aparelho" |

Motivo: el perfil de salud, el plan y las favoritas se guardan en el dispositivo. Entran en la copia
de seguridad de Android de la cuenta de Google del propio usuario, que hace el sistema y no la app;
el desarrollador no recibe ni puede ver esos datos, así que no cuenta como recogida de datos. La app no
pide el permiso de internet (se puede comprobar en el manifiesto).

### Apps de salud

| Pregunta | Respuesta |
|---|---|
| Funciones de salud de la app | Nutrición y control del peso (*Nutrition and weight management*) y Gestión de enfermedades y afecciones (*Disease and condition management*: diabetes, hipertensión) |
| ¿Es un dispositivo médico? | No. No diagnostica, no trata y no calcula dosis |
| Aviso médico | Se muestra en el primer uso, en el inicio, en los resultados, en cada receta y en la consulta de alimentos |

## Versión

| Campo | Respuesta |
|---|---|
| Archivo | `diabem-<versión>.aab` de GitHub Releases |
| Firma | Aceptar **Play App Signing**. Nuestra clave queda como "clave de subida" |
| Países | Brasil (el paquete de datos y el idioma son de Brasil) |
| Nombre de la versión | `1.0.0` |

### Notas de la versión (pt-BR)

```
Primeira versão do DiaBem:
• Receitas simples com semáforo de carboidratos e sal.
• Busca por ingredientes que você tem em casa.
• Seu plano da nutricionista, refeição por refeição.
• Consulta de alimentos da Tabela TACO.
• Letra grande, alto contraste e funciona sem internet.
```

## Orden recomendado

1. Guardar los 4 secretos de la clave en GitHub (ver `docs/release.md`).
2. Lanzar el flujo **Release** con la versión `1.0.0` y descargar el `.aab` de Releases.
3. Crear la app en la consola y rellenar "Contenido de la app".
4. Subir el `.aab` a **Prueba interna** (no es público) y probarlo en un teléfono.
5. Solo cuando la lista de `docs/release.md` esté completa: pasar a producción.
