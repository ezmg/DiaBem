#!/usr/bin/env python3
"""Convert the official TACO 4th edition spreadsheet (NEPA/UNICAMP) into a CSV file.

Usage:
    python3 taco_xlsx_to_csv.py Taco-4a-Edicao.xlsx out.csv

Reads the sheet with the centesimal composition (first sheet). Values are copied as they
are in the official table, including the markers "Tr" (traces), "NA" (not applicable) and
"*" / empty (not analysed). Nothing is estimated.
Requires: openpyxl
"""
import csv
import sys

import openpyxl

COLUMNS = [
    "taco_id", "description", "moisture_pct", "energy_kcal", "energy_kj", "protein_g",
    "lipids_g", "cholesterol_mg", "carbohydrate_g", "fiber_g", "ash_g", "calcium_mg",
    "magnesium_mg", "_taco_id_repeat", "manganese_mg", "phosphorus_mg", "iron_mg",
    "sodium_mg", "potassium_mg", "copper_mg", "zinc_mg", "retinol_mcg", "re_mcg",
    "rae_mcg", "thiamin_mg", "riboflavin_mg", "pyridoxine_mg", "niacin_mg",
    "vitamin_c_mg",
]


def clean(v):
    if v is None:
        return ""
    if isinstance(v, float):
        return f"{v:.4f}".rstrip("0").rstrip(".")
    return str(v).strip()


def main(src: str, dst: str) -> None:
    wb = openpyxl.load_workbook(src, data_only=True, read_only=True)
    ws = wb.worksheets[0]
    category = ""
    out = []
    for row in ws.iter_rows(values_only=True):
        first = row[0]
        if isinstance(first, str) and row[1] is None and first.strip() and not first.startswith(("Número", "Alimento")):
            category = first.strip()
            continue
        if isinstance(first, (int, float)) and row[1]:
            values = [clean(v) for v in row[: len(COLUMNS)]]
            rec = dict(zip(COLUMNS, values))
            rec.pop("_taco_id_repeat")
            rec["taco_id"] = str(int(first))
            rec["category"] = category
            out.append(rec)
    fields = ["taco_id", "category"] + [c for c in COLUMNS if c not in ("taco_id", "_taco_id_repeat")]
    with open(dst, "w", newline="", encoding="utf-8") as f:
        w = csv.DictWriter(f, fieldnames=fields)
        w.writeheader()
        w.writerows(out)
    print(f"{len(out)} foods written", file=sys.stderr)


if __name__ == "__main__":
    main(sys.argv[1], sys.argv[2])
