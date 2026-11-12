# Criterios de salud (semáforo)

Los umbrales están en `data/br/health-criteria.json`. Cada criterio tiene un **estado**:

| Estado | Significado |
|---|---|
| `official` | Tomado directamente de una fuente oficial. |
| `derived` | Calculado a partir de valores oficiales con una regla documentada. |
| `pending_review` | Sin fuente oficial clara. **Valor provisional: debe revisarlo un nutricionista.** |

## Hidratos de carbono por ración — `pending_review`

| Comida | Verde hasta | Amarillo hasta | Rojo |
|---|---|---|---|
| Desayuno | 30 g | 45 g | más de 45 g |
| Almuerzo | 45 g | 60 g | más de 60 g |
| Merienda | 15 g | 30 g | más de 30 g |
| Cena | 45 g | 60 g | más de 60 g |

La directriz de la Sociedade Brasileira de Diabetes (SBD) **no fija gramos por comida**: dice que
la cantidad debe ser individual (R5, R6). Estos números siguen la práctica habitual de conteo de
hidratos en raciones de 15 g, pero **no tienen fuente oficial** y quedan pendientes de revisión.

Si la persona introduce en su perfil un límite dado por su nutricionista (por ejemplo, "máximo
45 g de hidratos por comida"), la app usa ese límite: verde si no lo supera, rojo si lo supera.

### Opción "recetas con más hidratos primero"

En la pantalla de resultados hay un interruptor para ver primero las recetas con más hidratos
(por ejemplo, si el nutricionista ha pedido tomar más hidratos en el desayuno por hipoglucemias).
Solo cambia el **orden**: las recetas por encima del límite rojo siguen al final y con aviso. La
app no recomienda cantidades ni trata la hipoglucemia; el texto remite siempre al médico.

### Semáforo de un alimento suelto ("Consultar um alimento")

- Se calcula sobre **la cantidad elegida** (los gramos de hidratos que se van a comer), no por 100 g.
- Como un alimento suelto no es una comida completa, se compara con la escala de la **merienda**
  (verde hasta 15 g, amarillo hasta 30 g), la más prudente. Si la persona tiene un límite de su
  nutricionista, se usa ese. `pending_review`, como el resto de límites de hidratos.
- El sodio usa las mismas reglas que las recetas.
- La pantalla nunca dice "puede" o "no puede": remite al médico o nutricionista.
- Si TACO no tiene el dato de un alimento, no se muestra ese semáforo.

## Sodio — `derived`

- **Límite diario: 2000 mg** de sodio (OMS, 2012).
- **Rojo**: 600 mg o más por cada 100 g (límite de "alto en sodio" de la ANVISA, RDC 429/2020), o
  más del 30 % del límite diario (600 mg) en una ración (regla del semáforo británico, 2016).
- **Verde**: hasta 120 mg por cada 100 g (0,3 g de sal; regla británica).
- **Amarillo**: el resto.

Las cifras son oficiales; la combinación de reglas es nuestra y debe confirmarla un nutricionista.

## Fibra — `pending_review`

- Meta diaria **oficial** de la SBD (R10): 14 g por cada 1000 kcal, mínimo 25 g al día.
- Las etiquetas "fuente de fibra" (≥ 2,5 g por ración) y "rica en fibra" (≥ 5 g por ración) son
  provisionales.

## Azúcares añadidos — `official`

- Menos del 10 % de la energía en azúcares libres (OMS, 2015); 50 g/día es el valor de referencia
  de la ANVISA (IN 75/2020) para una dieta de 2000 kcal.
- Las recetas de DiaBem no usan azúcar. El compilador suma los gramos de los ingredientes marcados
  como azúcar añadido (`addedSugar: true`), ya que TACO no tiene columna de azúcares.

## Lo que la app NUNCA hace

- No calcula ni sugiere dosis de insulina ni de medicamentos.
- No diagnostica. El semáforo es orientativo y siempre va acompañado del aviso médico.
