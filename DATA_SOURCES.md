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

<!-- Más fuentes (medidas caseras, criterios de salud) se añaden en la Fase 1. -->

## Recetas

Las recetas de DiaBem son **originales**, redactadas para este proyecto a partir de platos
tradicionales brasileños de dominio común. No se copian recetas de webs con derechos de autor
(TudoGostoso y similares). Todas empiezan con `reviewedByNutritionist: false`.
