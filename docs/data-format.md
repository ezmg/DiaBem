# Formato de los datos (paquetes de país)

Cada país vive en `data/<código>/`. El compilador (`packbuilder`) lo valida y genera el paquete
que lleva la app. Si algo no cuadra, **la compilación falla** con un mensaje claro.

```
data/br/
  pack.json             id, idioma y lista de fuentes (con licencia)
  pantry.json           categorías e ingredientes que el usuario puede marcar
  foods.json            alimentos: cada uno apunta a una fila de la tabla oficial (TACO)
  measures.json         medidas caseras: cada una apunta a una fila del IBGE
  substitutions.json    sustituciones sencillas ("si no tienes X, usa Y")
  health-criteria.json  umbrales del semáforo, con fuente y estado
  recipes/*.json        una receta por archivo (el nombre del archivo = id)
  sources/              tablas oficiales convertidas a CSV
```

## Receta

```json
{
  "id": "feijao-caseiro",
  "name": "Feijão caseiro com pouco sal",
  "mealTypes": ["lunch", "dinner"],
  "servings": 4,
  "prepMinutes": 20,
  "ingredients": [
    { "food": "feijao-carioca-cozido", "quantity": 6, "unit": "concha", "note": "cozido só com água" },
    { "food": "sal", "quantity": 1.5, "unit": "g" },
    { "food": "cebolinha", "quantity": 2, "unit": "colher_sopa", "optional": true }
  ],
  "steps": ["Paso 1", "Paso 2"],
  "reviewedByNutritionist": false,
  "tips": ["Consejo opcional"]
}
```

- `mealTypes`: `breakfast` (café da manhã), `lunch` (almoço), `snack` (lanche), `dinner` (jantar).
- `unit`: una unidad de `measures.json` o `g`. Debe existir la medida para ese alimento.
- `reviewedByNutritionist` es el campo "revisado por nutricionista". Solo un nutricionista puede
  ponerlo a `true`.
- **No se escriben nutrientes en las recetas**: se calculan a partir de TACO.

El esquema es el propio modelo Kotlin (`core/.../model/Recipe.kt`), leído en modo estricto:
un campo desconocido o mal escrito hace fallar la compilación.

## Qué comprueba el compilador

- Identificadores únicos y bien formados; el id coincide con el nombre del archivo.
- Cada alimento existe en la tabla oficial y tiene energía, hidratos y sodio.
- Cada medida existe en la tabla del IBGE (o lleva una fuente declarada) y no es ambigua.
- Cada ingrediente usa un alimento y una medida que existen.
- Cantidades imposibles: cero o negativas, más de 50 cucharadas, más de 600 g de un ingrediente
  por ración, raciones de menos de 30 g o más de 900 g, menos de 20 o más de 1200 kcal por
  ración, más de 2000 mg de sodio por ración, 1–12 raciones, 1–240 minutos.
- Los complementos (USDA) solo rellenan huecos: nunca sustituyen un valor de TACO.

Tras compilar, se genera un informe legible en
`packbuilder/build/reports/packs/nutrition-br.md` (también como artefacto de la CI).
