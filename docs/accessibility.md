# Accesibilidad

DiaBem está pensada para personas mayores. Estas son las normas que cumple la interfaz y cómo se
comprueban.

| Norma | Cómo se cumple | Cómo se comprueba |
|---|---|---|
| Letra grande | Texto de cuerpo de 18–20 sp; títulos de 24–30 sp. | `ui/theme/Theme.kt` |
| Tamaño ajustable | Ajustes → Tamaño de letra: 100 %, 115 %, 130 %, 150 %. Se suma al tamaño de letra del sistema. | Prueba `settingsChangeTextSizeAndContrastAndEraseData` |
| Alto contraste | Ajustes → Alto contraste: negro sobre blanco, bordes negros. | `ContrastTest` (mínimo 7:1, WCAG AAA) |
| Contraste normal | Todos los pares texto/fondo ≥ 4,5:1 (WCAG AA). | `ContrastTest` |
| Botones grandes | Altura mínima de 56 dp (botones principales, 64 dp). | Constante `MinTouch` |
| No depender del color | El semáforo siempre lleva icono y palabra ("pouco", "médio", "muito"). | Prueba `pickIngredientsAndSeeRecipesWithTrafficLights` |
| Lector de pantalla (TalkBack) | Títulos marcados como encabezados; semáforo leído como "Carboidratos: pouco"; casillas, opciones e interruptores con su papel; iconos decorativos sin descripción; el número de raciones se anuncia al cambiar. | Prueba `everyClickableElementHasANameForScreenReaders` |
| Pocos pasos | Tipo de comida → ingredientes → recetas → receta (3 toques hasta ver recetas). | — |
| Lenguaje sencillo | Frases cortas, sin tecnicismos ("sal" en vez de "sódio" en el semáforo). | Revisión manual |

## Pendiente de probar con un teléfono real

Las pruebas automáticas comprueban la información que recibe el lector de pantalla, pero no
sustituyen una prueba con TalkBack en un teléfono Android. Lista para esa prueba:

1. Activar TalkBack y recorrer el primer uso (aviso → perfil → inicio).
2. Marcar tres ingredientes y abrir una receta solo con gestos.
3. Cambiar las raciones y comprobar que se anuncia el nuevo número.
4. Poner el tamaño de letra del sistema al máximo y la app al 150 %: nada debe cortarse.

## Recordatorios de comidas (solo Android)

- Se configuran en *Lembretes das refeições* (inicio y ajustes): un interruptor y la hora por comida.
- Son avisos locales: los programa el propio teléfono, sin servidor ni internet.
- Suenan con el sonido de notificaciones del teléfono; en silencio no suenan.
- Android puede retrasarlos unos minutos (se usan alarmas inexactas para no pedir un permiso especial).
- Se vuelven a programar solos al reiniciar el teléfono y al actualizar la app.
- Algunos fabricantes (Xiaomi, Samsung con ahorro de batería agresivo) pueden bloquear avisos de
  apps poco usadas: si no llegan, hay que excluir DiaBem del ahorro de batería.
- Pendiente de probar en un teléfono real: que suene a su hora con la app cerrada y tras reiniciar.
