#!/usr/bin/env python3
"""Convert the IBGE "Tabela de Medidas Referidas para os Alimentos Consumidos no Brasil"
(POF 2008-2009, liv50000.pdf) into a CSV file.

Usage:
    pdftotext -layout liv50000.pdf medidas.txt
    python3 ibge_measures_to_csv.py medidas.txt out.csv

Each output row is copied from the official table. Nothing is estimated.
"""
import csv
import re
import sys

ROW = re.compile(
    r"^\s*(?P<food_code>\d{7})\s+(?P<food>.+?)\s{2,}"
    r"(?P<prep_code>\d{1,3})\s+(?P<prep>.+?)\s{2,}"
    r"(?P<measure_code>\d{1,3})\s+(?P<measure>.+?)\s{2,}"
    r"(?P<std_code>\d{1,3})\s+(?P<std>.+?)\s{2,}"
    r"(?P<grams>\d+(?:\.\d{3})*(?:,\d+)?)\s+"
    r"(?P<source_code>\d{1,3})\s+(?P<source_desc>.+?)\s*$"
)


def main(src: str, dst: str) -> None:
    rows, skipped = [], 0
    with open(src, encoding="utf-8") as f:
        for line in f:
            if not re.match(r"^\s*\d{7}\s", line):
                continue
            m = ROW.match(line)
            if not m:
                skipped += 1
                print("SKIPPED:", line.rstrip(), file=sys.stderr)
                continue
            d = m.groupdict()
            d["grams"] = d["grams"].replace(".", "").replace(",", ".")
            rows.append(d)
    with open(dst, "w", newline="", encoding="utf-8") as f:
        w = csv.DictWriter(f, fieldnames=list(rows[0].keys()))
        w.writeheader()
        w.writerows(rows)
    print(f"{len(rows)} rows written, {skipped} skipped", file=sys.stderr)


if __name__ == "__main__":
    main(sys.argv[1], sys.argv[2])
