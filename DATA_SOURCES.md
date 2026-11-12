# Fuentes de datos y licencias

Toda fuente usada por DiaBem se documenta aquí con su licencia. Si una fuente no permite la
reutilización, **no se usa**.

## Brasil

### TACO — Tabela Brasileira de Composição de Alimentos, 4ª edição revisada e ampliada

- **Autor**: NEPA – Núcleo de Estudos e Pesquisas em Alimentação, UNICAMP (Campinas, 2011).
- **Dónde**: https://www.nepa.unicamp.br/ (PDF y hoja de cálculo oficiales).
- **Licencia / condiciones**: el propio documento dice (página iv):
  *"É permitida a reprodução parcial ou total desta obra, desde que citada a fonte."*
  → Se puede reproducir total o parcialmente **citando la fuente**. La cita aparece en este archivo,
  en el README y dentro de la app.
- **Cita**: NÚCLEO DE ESTUDOS E PESQUISAS EM ALIMENTAÇÃO – NEPA/UNICAMP. *Tabela Brasileira de
  Composição de Alimentos – TACO*. 4. ed. rev. e ampl. Campinas: NEPA/UNICAMP, 2011. 161 p.
- **Uso en DiaBem**: valores por 100 g de parte comestible (energía, hidratos, fibra, proteínas,
  grasas, sodio, potasio). Solo se incluyen los alimentos que usan las recetas.
- **Limitación conocida**: TACO no tiene columna de azúcares. Los azúcares añadidos se calculan
  sumando los ingredientes que son azúcar o miel en la receta (dato de la propia receta, no inventado).
- **Valores "Tr" (trazas) y "NA" (no aplicable)**: se guardan como 0. Los valores en blanco (no
  analizados) se guardan como "sin dato" y nunca como 0.

- **Archivo en el repositorio**: `data/br/sources/taco4.csv`, convertido de la hoja de cálculo
  oficial con `tools/importers/taco_xlsx_to_csv.py` (SHA-256 del original:
  `a66b8ec528daeabc63bc2b015fc9bd8c6d76b941c2fc0ed93a4311d449302d14`).
- **Huecos conocidos de TACO y cómo se tratan**:
  - La leche líquida (entera y desnatada) no tiene hidratos analizados ("*"). Por eso las recetas
    usan **leche en polvo desnatada**, que sí tiene todos los datos.
  - No existe "sal refinada": se usa "Sal, grosso" (39 943 mg de sodio/100 g). Pendiente de que lo
    confirme un nutricionista.
  - Los aceites no tienen sodio ni potasio analizados: se completan con USDA (ver abajo).
  - La goma de tapioca se calcula como "Fécula de mandioca" (seca). Como la goma hidratada lleva
    agua, el cálculo **sobrestima** los hidratos: es un error del lado seguro.
  - El limón se cuenta como fruta entera aunque solo se use el zumo (sobrestima un poco los hidratos).

### IBGE — Tabela de Medidas Referidas para os Alimentos Consumidos no Brasil (POF 2008-2009)

- **Autor**: Instituto Brasileiro de Geografia e Estatística (IBGE), con el Ministerio de Salud, 2011.
- **Dónde**: https://biblioteca.ibge.gov.br/visualizacao/livros/liv50000.pdf
- **Licencia / condiciones**: publicación estadística oficial del Gobierno de Brasil. El IBGE permite
  reutilizar sus datos **citando la fuente** (no hay una licencia única para todas sus
  publicaciones; las que la indican piden citar al IBGE). Usamos solo datos factuales (gramos por
  medida casera) y citamos la fuente. ⚠️ Conviene confirmarlo por escrito con el IBGE antes de
  publicar en tiendas.
- **Uso en DiaBem**: gramos de cada medida casera (colher de sopa, concha, unidade…). Cada medida
  de `data/br/measures.json` apunta a una fila de la tabla; el compilador busca el valor, no se
  copia a mano.
- **Archivo en el repositorio**: `data/br/sources/ibge_pof2008_medidas.csv`, convertido con
  `tools/importers/ibge_measures_to_csv.py` (11 801 filas; SHA-256 del PDF:
  `317ace59e53e25f3fc4913c5f552adf181bf001e6272edb5ae7a46804aa360b0`).
- **Limitación**: el IBGE no registra la sal. Por eso la sal se indica **en gramos** en las recetas.

### USDA FoodData Central — SR Legacy (2018)

- **Autor**: Departamento de Agricultura de EE. UU. (USDA).
- **Licencia**: dominio público (CC0 1.0).
- **Uso en DiaBem**: SOLO para rellenar valores que TACO deja en blanco. El compilador rechaza
  cualquier intento de sustituir un valor que exista en TACO. Hoy se usa en:
  - Aceite de oliva (FDC 171413): sodio 2 mg, potasio 1 mg por 100 g.
  - Aceite de soja (FDC 171411): sodio 0 mg, potasio 0 mg por 100 g.

### Agua

- Por definición, el agua potable no aporta energía ni hidratos; 1 ml = 1 g.

## Criterios de salud

Ver [docs/health-criteria.md](docs/health-criteria.md). Fuentes citadas (no se reproducen):
Sociedade Brasileira de Diabetes (Diretriz oficial, terapia nutricional en DM2), OMS (sodio 2012,
azúcares 2015), ANVISA (RDC 429/2020 e IN 75/2020) y guía británica de etiquetado frontal (2016).

### Fuentes revisadas y descartadas

- **Manual de Contagem de Carboidratos (SBD)**: solo se descarga tras un formulario de registro;
  no se ha usado.
- **Tabela para avaliação de consumo alimentar em medidas caseiras (Pinheiro et al.)**: libro con
  derechos de autor; usamos el IBGE, que la cita como una de sus fuentes.

## Recetas

Las recetas de DiaBem son **originales**, redactadas para este proyecto a partir de platos
tradicionales brasileños de dominio común. No se copian recetas de webs con derechos de autor
(TudoGostoso y similares). Todas empiezan con `reviewedByNutritionist: false`.
