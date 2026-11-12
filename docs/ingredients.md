# Lista de ingredientes de Brasil: de dónde sale

La lista de ingredientes que el usuario puede marcar se contrasta con los **alimentos más
consumidos en Brasil** según el IBGE (*Pesquisa de Orçamentos Familiares 2017-2018: análise do
consumo alimentar pessoal no Brasil*, tabla 2: porcentaje de personas que consumieron cada
alimento en un día).

Norma: todo alimento de uso doméstico habitual (aprox. 3 % o más) debe poder marcarse y tener
recetas, **también en su versión corriente** (arroz blanco, pan francés), no solo la integral. La
app orienta con el semáforo y el tamaño de la ración; no da por hecho que la persona compra
productos especiales.

| Alimento (IBGE) | % personas | En DiaBem |
|---|---|---|
| Café | 78,1 | Café |
| Arroz | 76,1 | Arroz branco (y Arroz integral, 2,3 %) |
| Feijão | 60,0 | Feijão carioca; Feijão-de-corda (4,1 %) |
| Pão de sal | 50,9 | Pão francês (y Pão integral, 3,7 %) |
| Óleos e gorduras | 46,8 | Óleo (básico), Azeite, Margarina |
| Carne bovina | 38,2 | Carne bovina, Carne moída |
| Aves | 30,8 | Frango |
| Salada crua / verduras / legumes | 21,8 y otros | Alface, Tomate, Repolho, Couve, Cenoura, Chuchu, Abóbora, Pepino… |
| Macarrão | 18,5 | Macarrão |
| Biscoito salgado | 17,1 | Biscoito de água e sal |
| Banana | 14,7 | Banana |
| Ovos | 13,9 | Ovos |
| Milho e preparações | 11,9 | Flocão de milho (cuscuz) |
| Queijos | 10,5 | Queijo minas frescal, Ricota |
| Farinha de mandioca | 10,4 | Farinha de mandioca |
| Batata inglesa | 8,1 | Batata |
| Carne suína | 6,7 | Carne de porco |
| Leite integral | 6,5 | Leite, Leite em pó desnatado |
| Peixes frescos | 5,7 | Peixe |
| Maçã, Laranja, Mamão | 5,5 / 4,1 / 3,1 | Maçã, Laranja, Mamão |
| Mandioca | 4,0 | Mandioca |
| Iogurtes | 3,6 | Iogurte natural |

## Alimentos frecuentes que NO se incluyen (a propósito)

Decisión provisional, **pendiente de revisión por nutricionista**:

- **Sucos (33,1 %), refrigerantes (15,4 %), bolos (11,1 %), biscoito doce (9,2 %), doces**: aportan
  azúcar de absorción rápida; la directriz de la SBD recomienda reducir las bebidas con azúcares
  (R3) y los hidratos refinados (R5).
- **Linguiça (5,8 %), mortadela, presunto, salsicha**: muy altos en sodio (más de 1000 mg por 100 g
  en TACO).
- **Macarrão instantâneo**: 1515 mg de sodio por 100 g en TACO.
- **Sanduíches y salgados**: son comidas preparadas fuera de casa, no ingredientes.

## Cómo añadir un ingrediente

1. `foods.json`: alimento con su fila de TACO.
2. `measures.json`: medida casera con su fila del IBGE (o gramos).
3. `pantry.json`: entrada que el usuario marca.
4. Al menos una receta que lo use (si no, el compilador no lo incluye en la app).
