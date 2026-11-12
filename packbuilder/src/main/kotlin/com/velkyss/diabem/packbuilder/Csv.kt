package com.velkyss.diabem.packbuilder

import java.io.File

/** Minimal RFC 4180 CSV reader (quoted fields, commas inside quotes, "" escapes). */
object Csv {
    fun read(file: File): List<Map<String, String>> {
        val rows = parse(file.readText(Charsets.UTF_8))
        if (rows.isEmpty()) return emptyList()
        val header = rows.first()
        return rows.drop(1).filter { it.size > 1 || it.firstOrNull()?.isNotEmpty() == true }.map { cells ->
            header.indices.associate { header[it] to (cells.getOrNull(it) ?: "") }
        }
    }

    fun parse(text: String): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        var row = mutableListOf<String>()
        val cell = StringBuilder()
        var quoted = false
        var i = 0
        while (i < text.length) {
            val c = text[i]
            when {
                quoted && c == '"' && i + 1 < text.length && text[i + 1] == '"' -> { cell.append('"'); i++ }
                c == '"' -> quoted = !quoted
                !quoted && c == ',' -> { row.add(cell.toString()); cell.clear() }
                !quoted && (c == '\n' || c == '\r') -> {
                    if (c == '\r' && i + 1 < text.length && text[i + 1] == '\n') i++
                    row.add(cell.toString()); cell.clear()
                    rows.add(row); row = mutableListOf()
                }
                else -> cell.append(c)
            }
            i++
        }
        if (cell.isNotEmpty() || row.isNotEmpty()) { row.add(cell.toString()); rows.add(row) }
        return rows
    }
}
