#!/usr/bin/env python3
"""Convert the IBGE "Tabelas de Composição Nutricional dos Alimentos Consumidos no Brasil"
(POF 2008-2009, liv50002.pdf) into one CSV file.

Usage:
    pdftotext -layout liv50002.pdf composicao.txt
    python3 ibge_composition_to_csv.py composicao.txt out.csv

Reads Table 1 (energy, macronutrients, fibre), Table 2 (sugars), Table 3 (minerals) and
Table 5 (where each row comes from) and joins them by food code + preparation code. Values are copied as printed; "-" means zero (IBGE convention).
The script stops with an error if the three tables do not list exactly the same rows.
"""
import csv
import re
import sys

NUM = r"(?:-|\d+(?:\.\d{3})*(?:,\d+)?)"
ROW = re.compile(
    r"^\s*(?P<food_code>\d{7})\s+(?P<food>.+?)\s{2,}(?P<prep_code>\d{1,3})\s+(?P<prep>.+?)\s{2,}"
    r"(?P<values>" + NUM + r"(?:\s+" + NUM + r")*)\s*$"
)
TABLES = {
    "Tabela 1 - Energia": (1, ["energy_kcal", "protein_g", "lipids_g", "carbohydrate_g", "fiber_g"]),
    "Tabela 2 - Gorduras": (2, ["cholesterol_mg", "sat_g", "mono_g", "poly_g", "linoleic_g", "linolenic_g", "trans_g", "sugar_total_g", "added_sugar_g"]),
    "Tabela 3 - Minerais": (3, ["calcium_mg", "magnesium_mg", "manganese_mg", "phosphorus_mg", "iron_mg", "sodium_mg", "added_sodium_mg", "potassium_mg", "copper_mg", "zinc_mg", "selenium_mcg"]),
}
SOURCE_ROW = re.compile(
    r"^\s*(?P<food_code>\d{7})\s+(?P<food>.+?)\s{2,}(?P<prep_code>\d{1,3})\s+(?P<prep>.+?)\s{2,}"
    r"(?P<source_code>\d{1,2})\s+(?P<source_desc>\S.*?)\s*$"
)
KEEP = ["energy_kcal", "protein_g", "lipids_g", "carbohydrate_g", "fiber_g", "sugar_total_g", "added_sugar_g", "sodium_mg", "added_sodium_mg", "potassium_mg"]


def number(text):
    # IBGE convention (page "Convenções"): "-" is a value equal to zero, not a missing value.
    return "0" if text == "-" else text.replace(".", "").replace(",", ".")


def main(src, dst):
    table, category, problems = None, "", []
    data = {1: {}, 2: {}, 3: {}}
    sources = {}
    order, names = [], {}
    for raw in open(src, encoding="utf-8"):
        line = raw.rstrip("\n")
        title = next((t for t in TABLES if t in line and "na composição" in line), None)
        if title:
            table = TABLES[title][0]
            continue
        if "Tabela 4 - Vitaminas" in line and "na composição" in line:
            table = None
            continue
        if "Tabela 5 - Código e descrição da fonte" in line:
            table = 5
            continue
        if table == 5:
            if re.match(r"^\s*\d{7}\s", line):
                m = SOURCE_ROW.match(line)
                if not m:
                    problems.append(f"table 5: {line.strip()[:140]}")
                else:
                    sources[(m.group("food_code"), m.group("prep_code"))] = (m.group("source_code"), m.group("source_desc"))
            continue
        if table is None:
            continue
        if re.match(r"^\s*\d{7}\s", line):
            m = ROW.match(line)
            columns = next(c for n, c in TABLES.values() if n == table)
            values = m.group("values").split() if m else []
            if not m or len(values) != len(columns):
                problems.append(f"table {table}: {line.strip()[:140]}")
                continue
            key = (m.group("food_code"), m.group("prep_code"))
            if key in data[table]:
                problems.append(f"table {table}: duplicate {key}")
            data[table][key] = dict(zip(columns, map(number, values)))
            if table == 1:
                order.append(key)
                names[key] = (m.group("food").strip(), m.group("prep").strip(), category)
        elif table == 1 and line.strip() and not re.search(r"\d", line) and len(line.strip()) < 60 \
                and not any(w in line for w in ("Código", "Energia", "Lipídios", "totais", "(g)", "continua", "conclusão", "Fonte", "Pesquisa", "Tabelas")):
            category = line.strip()
    keys = [set(data[t]) for t in (1, 2, 3)]
    if not (keys[0] == keys[1] == keys[2]):
        problems.append(f"tables differ: {len(keys[0])} / {len(keys[1])} / {len(keys[2])} rows; "
                        f"only in 1: {sorted(keys[0] - keys[1] - keys[2])[:5]}; missing in 1: {sorted((keys[1] | keys[2]) - keys[0])[:5]}")
    # A few rows have no line in Table 5 of the PDF itself: they keep an empty source.
    no_source = sorted(keys[0] - set(sources))
    if len(no_source) > 10 or set(sources) - keys[0]:
        problems.append(f"table 5 differs too much: {len(no_source)} rows without source, {len(set(sources) - keys[0])} unknown rows")
    if problems:
        print("\n".join(problems[:40]), file=sys.stderr)
        print(f"{len(problems)} problem(s): nothing written", file=sys.stderr)
        sys.exit(1)
    with open(dst, "w", newline="", encoding="utf-8") as f:
        w = csv.writer(f)
        w.writerow(["food_code", "food", "prep_code", "prep", "category"] + KEEP + ["source_code", "source_desc"])
        for key in order:
            merged = {**data[1][key], **data[2][key], **data[3][key]}
            w.writerow([key[0], names[key][0], key[1], names[key][1], names[key][2]] + [merged[c] for c in KEEP] + list(sources.get(key, ("", ""))))
    print(f"{len(order)} rows, {len({k[0] for k in order})} foods written; without source in Table 5: {no_source}", file=sys.stderr)


if __name__ == "__main__":
    main(sys.argv[1], sys.argv[2])
