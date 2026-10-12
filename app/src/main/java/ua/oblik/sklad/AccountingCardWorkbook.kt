package ua.oblik.sklad

import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/** Generates an editable XLSX using the supplied military-property card's coordinates and two movement blocks. */
object AccountingCardWorkbook {
    data class Movement(
        val date: String, val document: String, val documentNumber: String,
        val supplier: String, val recipient: String,
        val receipt: Double, val issue: Double, val balance: Double, val note: String
    )

    fun write(output: OutputStream, fields: List<Pair<String, String>>, movements: List<Movement>) {
        val cells = linkedMapOf<String, Cell>()
        fun put(ref: String, value: String, style: Int = 0) { cells[ref] = Cell(value, style, false) }
        fun number(ref: String, value: Double, style: Int = 0) { cells[ref] = Cell(qty(value), style, true) }
        val f = fields.toMap()
        fun field(label: String, fallback: String = "") = f[label] ?: fallback

        put("T2", "Додаток 31", 1)
        put("T3", "до Інструкції з обліку військового майна у", 1)
        put("B4", field("Посада та звання МВО", field("Матеріально відповідальна особа")), 0)
        put("N4", "Картка № " + field("Номер картки"), 1)
        put("L5", "обліку військового майна (категорійного)", 1)
        put("T5", "Збройних Силах України", 1)
        put("D6", "20____", 0)
        put("T7", "(пункт 1 розділу 2)", 0)
        put("D10", field("Матеріально відповідальна особа"), 0)
        put("B12", "Сховище №", 1)
        put("C12", field("Сховище / місце зберігання"))
        put("F12", "Полиця №", 1)
        put("G12", field("Полиця"))
        put("L12", "Ціна за одиницю", 1)
        put("M12", field("Ціна за одиницю", "Не визначена"))
        put("R12", "Розпочато", 1)
        put("T12", field("Розпочато"))
        put("B14", "Стелаж №", 1)
        put("C14", field("Стелаж"))
        put("F14", "Сховище №", 1)
        put("G14", field("Сховище / місце зберігання"))
        put("L14", "Вміст дорогоцінних металів", 1)
        put("M14", "—")
        put("R14", "Закінчено", 1)
        put("T14", field("Закінчено"))
        put("B17", "Реєстраційний номер", 1)
        put("C17", "Номер аркуша", 1)
        put("E17", "Найменування військового майна", 1)
        put("J17", "Код номенклатури", 1)
        put("L17", "Одиниця виміру", 1)
        put("M17", "Номер партії", 1)
        put("O17", "Служба забезпечення", 1)
        put("Q17", "Центр забезпечення", 1)
        put("S17", "Військова частина", 1)
        put("T17", "Нормальний запас", 1)
        put("T18", "Максимальний")
        put("W18", "Мінімальний")
        put("B20", field("Реєстраційний номер / NSN"))
        put("C20", "1")
        put("E20", field("Найменування майна"))
        put("J20", field("Номенклатурний номер"))
        put("L20", field("Одиниця виміру"))
        put("M20", field("Номер партії"))
        put("O20", field("Служба забезпечення"))
        put("Q20", field("Центр забезпечення"))
        put("S20", field("Військова частина"))
        put("T20", field("Максимальний запас"))
        put("W20", field("Мінімальний запас"))

        fun headers(row: Int) {
            put("B$row", "Дата запису", 1)
            put("C$row", "Документ", 1)
            put("F$row", "Постачальник", 1)
            put("G$row", "Одержувач", 1)
            put("I$row", "Надійшло", 1)
            put("N$row", "Вибуло", 1)
            put("S$row", "Залишок", 1)
            val sub = row + 1
            put("C$sub", "Найменування", 1)
            put("D$sub", "Номер", 1)
            put("E$sub", "Дата", 1)
            put("I${row + 3}", "Усього", 1)
            put("J${row + 3}", "з них за категоріями (сортами)", 1)
            put("N${row + 3}", "Усього", 1)
            put("O${row + 3}", "з них за категоріями (сортами)", 1)
            put("S${row + 3}", "Усього", 1)
            put("T${row + 3}", "з них за категоріями (сортами)", 1)
            val cat = row + 4
            listOf("J" to "1", "K" to "2", "L" to "3", "O" to "1", "P" to "2", "Q" to "3", "T" to "1", "U" to "2", "V" to "3").forEach { (col, label) -> put("$col$cat", label, 1) }
            val nums = row + 6
            listOf("B" to "1", "C" to "2", "D" to "3", "E" to "4", "F" to "5", "G" to "6", "I" to "7", "J" to "8", "K" to "9", "L" to "10", "N" to "11", "O" to "12").forEach { (col, label) -> put("$col$nums", label, 1) }
        }
        headers(23)
        headers(51)

        movements.take(56).forEachIndexed { index, move ->
            val row = if (index < 20) 30 + index else 58 + index - 20
            put("B$row", move.date)
            put("C$row", move.document)
            put("D$row", move.documentNumber)
            put("E$row", move.date)
            put("F$row", move.supplier)
            put("G$row", move.recipient)
            if (move.receipt != 0.0) number("I$row", move.receipt)
            if (move.issue != 0.0) number("N$row", move.issue)
            number("S$row", move.balance)
            if (move.note.isNotBlank()) put("W$row", move.note)
        }

        val rowNumbers = cells.keys.mapNotNull { it.dropWhile { c -> c.isLetter() }.toIntOrNull() }.toSet().sorted()
        val rowXml = rowNumbers.joinToString("") { row ->
            val rowCells = cells.filterKeys { it.dropWhile { c -> c.isLetter() }.toIntOrNull() == row }
                .entries.sortedBy { colIndex(it.key) }.joinToString("") { (ref, c) ->
                    val value = esc(c.value)
                    if (c.numeric) "<c r=\"$ref\" s=\"${c.style}\"><v>$value</v></c>"
                    else "<c r=\"$ref\" s=\"${c.style}\" t=\"inlineStr\"><is><t xml:space=\"preserve\">$value</t></is></c>"
                }
            "<row r=\"$row\">$rowCells</row>"
        }
        val merges = mutableListOf(
            "T2:W2","B3:I3","T3:W3","B4:I4","N4:O4","L5:P5","T5:W5","D6:F6","T7:W7","B9:I9","D10:G10",
            "C12:D12","G12:H12","L12:M12","O12:P12","C14:D14","G14:H14","L14:N14",
            "B17:B19","C17:D19","E17:I18","J17:K19","L17:L19","M17:N19","O17:P19","Q17:R19","S17:S19","T17:X17","T18:V19","W18:X19",
            "E19:I19","B20:B21","C20:D21","E20:I21","J20:K21","O20:P21","Q20:R21","S20:S21","T20:V21","W20:X21",
            "B23:B28","C23:E23","F23:F28","G23:H28","I23:M25","N23:R25","S23:X25","C24:C28","D24:D28","E24:E28",
            "I26:I28","J26:M26","N26:N28","O26:R26","S26:S28","T26:X26","J27:J28","K27:K28","L27:M28","O27:O28","P27:P28","Q27:R28","T27:T28","U27:U28","V27:X28",
            "B51:B56","C51:E51","F51:F56","G51:H56","I51:M53","N51:R53","S51:X53","C52:C56","D52:D56","E52:E56",
            "I54:I56","J54:M54","N54:N56","O54:R54","S54:S56","T54:X54","J55:J56","K55:K56","L55:M56","O55:O56","P55:P56","Q55:R56","T55:T56","U55:U56","V55:X56"
        )
        merges += listOf("G29:H29", "L29:M29", "Q29:R29", "V29:X29", "G57:H57", "L57:M57", "Q57:R57", "V57:X57")
        for (r in 30..49) merges += listOf("G$r:H$r","L$r:M$r","Q$r:R$r","V$r:X$r")
        for (r in 58..93) merges += listOf("G$r:H$r","L$r:M$r","Q$r:R$r","V$r:X$r")
        val mergeXml = merges.joinToString("") { "<mergeCell ref=\"$it\"/>" }
        val sheet = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
            "<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\"><dimension ref=\"B2:X93\"/><sheetViews><sheetView workbookViewId=\"0\"/></sheetViews><sheetFormatPr defaultRowHeight=\"20\"/><cols><col min=\"2\" max=\"2\" width=\"13\" customWidth=\"1\"/><col min=\"3\" max=\"5\" width=\"12\" customWidth=\"1\"/><col min=\"6\" max=\"8\" width=\"12\" customWidth=\"1\"/><col min=\"9\" max=\"24\" width=\"9\" customWidth=\"1\"/></cols><sheetData>$rowXml</sheetData><mergeCells count=\"${merges.size}\">$mergeXml</mergeCells><pageMargins left=\"0.25\" right=\"0.25\" top=\"0.4\" bottom=\"0.4\" header=\"0.2\" footer=\"0.2\"/><pageSetup orientation=\"landscape\" paperSize=\"9\" fitToWidth=\"1\" fitToHeight=\"0\"/></worksheet>"
        val styles = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><styleSheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\"><fonts count=\"2\"><font><sz val=\"9\"/><name val=\"Arial\"/></font><font><b/><sz val=\"9\"/><name val=\"Arial\"/></font></fonts><fills count=\"1\"><fill><patternFill patternType=\"none\"/></fill></fills><borders count=\"2\"><border><left/><right/><top/><bottom/><diagonal/></border><border><left style=\"thin\"><color rgb=\"FF000000\"/></left><right style=\"thin\"><color rgb=\"FF000000\"/></right><top style=\"thin\"><color rgb=\"FF000000\"/></top><bottom style=\"thin\"><color rgb=\"FF000000\"/></bottom><diagonal/></border></borders><cellStyleXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/></cellStyleXfs><cellXfs count=\"2\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyBorder=\"1\"><alignment vertical=\"center\" wrapText=\"1\"/></xf><xf numFmtId=\"0\" fontId=\"1\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyBorder=\"1\"><alignment horizontal=\"center\" vertical=\"center\" wrapText=\"1\"/></xf></cellXfs><cellStyles count=\"1\"><cellStyle name=\"Normal\" xfId=\"0\" builtinId=\"0\"/></cellStyles></styleSheet>"
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

    private data class Cell(val value: String, val style: Int, val numeric: Boolean)
    private fun colIndex(ref: String): Int = ref.takeWhile { it.isLetter() }.fold(0) { n, ch -> n * 26 + ch.uppercaseChar().code - 'A'.code + 1 }
    private fun qty(value: Double): String = String.format(java.util.Locale.US, "%.3f", value).trimEnd('0').trimEnd('.')
    private fun esc(value: String): String = value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&apos;")
}
