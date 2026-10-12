package ua.oblik.sklad

import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/** Editable Excel card following the field order and movement columns of the supplied form. */
object AccountingCardWorkbook {
    data class Movement(
        val date: String, val document: String, val supplier: String, val recipient: String,
        val receipt: Double, val issue: Double, val balance: Double, val note: String
    )

    fun write(output: OutputStream, fields: List<Pair<String, String>>, movements: List<Movement>) {
        val rows = mutableListOf<List<String>>()
        rows.add(listOf("Додаток 31", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "до Інструкції з обліку військового майна у Збройних Силах України"))
        rows.add(listOf("КАРТКА ОБЛІКУ ВІЙСЬКОВОГО МАЙНА (категорійного)"))
        fields.forEach { rows.add(listOf(it.first, it.second)) }
        rows.add(emptyList())
        rows.add(listOf("Дата запису", "Документ / номер", "Постачальник", "Одержувач", "Надійшло", "Вибуло", "Залишок", "Примітка"))
        var balance = 0.0
        movements.take(56).forEach { move ->
            balance = move.balance
            rows.add(listOf(move.date, move.document, move.supplier, move.recipient, qty(move.receipt), qty(move.issue), qty(balance), move.note))
        }
        val xmlRows = rows.mapIndexed { rowIndex, values ->
            val rowNum = rowIndex + 1
            val cells = values.mapIndexed { colIndex, value ->
                val ref = colName(colIndex + 1) + rowNum
                "<c r=\"" + ref + "\" t=\"inlineStr\"><is><t xml:space=\"preserve\">" + esc(value) + "</t></is></c>"
            }.joinToString("")
            "<row r=\"" + rowNum + "\">" + cells + "</row>"
        }.joinToString("")
        val sheet = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
            "<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\"><sheetViews><sheetView workbookViewId=\"0\"/></sheetViews><sheetFormatPr defaultRowHeight=\"22\"/><cols><col min=\"1\" max=\"1\" width=\"22\" customWidth=\"1\"/><col min=\"2\" max=\"2\" width=\"30\" customWidth=\"1\"/><col min=\"3\" max=\"8\" width=\"20\" customWidth=\"1\"/></cols><sheetData>" + xmlRows + "</sheetData><autoFilter ref=\"A" + (fields.size + 4) + ":H" + rows.size + "\"/><pageMargins left=\"0.25\" right=\"0.25\" top=\"0.5\" bottom=\"0.5\" header=\"0.2\" footer=\"0.2\"/><pageSetup orientation=\"landscape\" paperSize=\"9\" fitToWidth=\"1\" fitToHeight=\"0\"/></worksheet>"
        val styles = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><styleSheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\"><fonts count=\"2\"><font><sz val=\"10\"/><name val=\"Arial\"/></font><font><b/><sz val=\"11\"/><name val=\"Arial\"/></font></fonts><fills count=\"2\"><fill><patternFill patternType=\"none\"/></fill><fill><patternFill patternType=\"solid\"><fgColor rgb=\"FFE2C36F\"/><bgColor indexed=\"64\"/></patternFill></fill></fills><borders count=\"2\"><border><left/><right/><top/><bottom/><diagonal/></border><border><left style=\"thin\"/><right style=\"thin\"/><top style=\"thin\"/><bottom style=\"thin\"/><diagonal/></border></borders><cellStyleXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/></cellStyleXfs><cellXfs count=\"2\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyBorder=\"1\"/><xf numFmtId=\"0\" fontId=\"1\" fillId=\"1\" borderId=\"1\" xfId=\"0\" applyAlignment=\"1\" applyBorder=\"1\"><alignment horizontal=\"center\" vertical=\"center\" wrapText=\"1\"/></xf></cellXfs><cellStyles count=\"1\"><cellStyle name=\"Normal\" xfId=\"0\" builtinId=\"0\"/></cellStyles></styleSheet>"
        val contentTypes = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\"><Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/><Default Extension=\"xml\" ContentType=\"application/xml\"/><Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/><Override PartName=\"/xl/worksheets/sheet1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/><Override PartName=\"/xl/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml\"/></Types>"
        val rootRels = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\"><Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/></Relationships>"
        val workbook = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\"><sheets><sheet name=\"Картка обліку\" sheetId=\"1\" r:id=\"rId1\"/></sheets></workbook>"
        val rels = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\"><Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet1.xml\"/><Relationship Id=\"rId2\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles\" Target=\"styles.xml\"/></Relationships>"
        ZipOutputStream(output).use { zip ->
            fun put(name: String, text: String) {
                zip.putNextEntry(ZipEntry(name))
                zip.write(text.toByteArray(Charsets.UTF_8))
                zip.closeEntry()
            }
            put("[Content_Types].xml", contentTypes)
            put("_rels/.rels", rootRels)
            put("xl/workbook.xml", workbook)
            put("xl/_rels/workbook.xml.rels", rels)
            put("xl/worksheets/sheet1.xml", sheet)
            put("xl/styles.xml", styles)
        }
    }

    private fun qty(value: Double): String = String.format(java.util.Locale.US, "%.3f", value).trimEnd('0').trimEnd('.')
    private fun colName(value: Int): String {
        var n = value
        val out = StringBuilder()
        while (n > 0) {
            out.append(('A'.code + (n - 1) % 26).toChar())
            n = (n - 1) / 26
        }
        return out.reverse().toString()
    }
    private fun esc(value: String): String = value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&apos;")
}
