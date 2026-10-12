package ua.oblik.sklad

import java.io.ByteArrayInputStream
import java.io.OutputStream
import java.util.Base64
import java.util.zip.InflaterInputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/** Generates an editable XLSX using the supplied military-property card's coordinates and two movement blocks. */
object AccountingCardWorkbook {
    // Style table and cell-style coordinates extracted from the official accounting-card template.
    private val officialStylesXml: String by lazy {
        String(inflateTemplateData("eNrdXN2OozYUvq/Ud0DcZ4wJEBKFrDYzE2mlrVR1plJvHWISa/mJjDNLtur1PkJfo+rV7kXfIX2j2kAmTOePECBmbgI29nc+H59jHxPs8bsk8JU7TGMShY4KLzRVwaEbLUi4dNRfb2c9W1VihsIF8qMQO+oWx+q7yY8/jGO29fHNCmOmcIgwdtQVY+sRALG7wgGKL6I1DvkTL6IBYjxJlyBeU4wWsagU+EDXNAsEiIRqhjAK3DIgAaKfNuueGwVrxMic+IRtUyxVCdzRh2UYUTT3OdUEGshVEmhRXUnoXkia+0hOQFwaxZHHLjguiDyPuPgx3SEYAuQekDhyNSRoAk1/0PaEVkQyAMV3RHSfOhl7UchixY02IXNU3nNpY0efwuhzOBOPeA/npSbj+Ityh3yeA1UwGbuRH1GFcclYFOI5IQpwVuIS+WROicj0UED8bZati4yUbF4uIFz3IhNkErLfOahLlrtCNOb2luVoRjn5G9BeW/dy7HbEDNsRM2jRQIz2DeSBfL1N+ekl5kDE9+/dVlezjMmYj28M03DGE0p+f7tdc0ohH4ozmLTcK6WXFG2hbhYqpBcudx7RBR/695Khre7zJmMfe4zXoGS5ElcWrYF4yBgfiybjBUHLKES+wNzXKFFTSWcNR2UrIoarTM8kXOAELxzVShUGsqLHyigFzcsd0Yg9ZIAXZBM8CyqKPknllXoFMiVrHKuZkrBpWbmYP2M3NUk5vmNb0CSQgjeQlE6jdiZJZ4EWeqDBBgOpfAXI6QGgFbN+M37W0MD/YpDwxHj1YvnHhlw2Bjk5HMpveCjnYt+/EWi/eYd4jmMmnhJuglnAPnBEvrgXEef+lgeB+W0GkyUEfhEtwy7ADgeVcJXEuxfwXG399doKWq/9rVjN5qH5sUzg/7Gm6aP0QZp+75NlGODKArSmBbzQgjp1W4L8ZIz2SWUVUfKFVxULHeFCqnitxIgr0tzmU7tKvFrVWUfnlO2MiuorpS6XZ2BaWUNmrRqCsF44vV44o164enVn1YpmV0Izn2vqoAFHzy234Oon2vJjlqf17+CowfhEB4VlB+z6NF+VkgTD1stDcUNC9Tbtock5I51i21V9nbM6bLMfNPn8sl/WPmUcpPsydJ4hhQXVasc1TqO6pLw0SXn1JeVlSMrLlJRXw/6ofKZofYuT/WpRSuesh6TWBZL9LpA0ukDSlIgkLO3d54yOYGn3lpKl0QmWZhfiYasDDj7oAEe7AxwtGZYd9ttb+1RlMZRCFboULDQZWEhBArb6isI4wxxblZMpHyV4Rk7tBO1yROUN/L/YHommYyzG44JfIoZYuhFiqEkbaNVGtHWNdiKObYY1tN5crFTf8unM7xJh2X85zv+v7WMzOj8nQz5KVhdebdid+BwDdnO41btJW+sm7b7UtGHZ2VDKF6BaF1h2giTsd4Gl2QWSrU6wDbn/oENtONMSTtI1Wkd7/DXa3f5w98wf9lidUN7pLIFcKnitwQP51tYD+daxg6a/E37mo12Q7+wqbB97sHnsPlcR5x846u7P3d+7b/9+3f21+777Z/dt971AcL4hPiPhE5vIOP4iOewfS58ycUbLQ4kcY4E9tPHZ7f1DRz3c/5TuAdTvS/1M7iKWlzrcfxS78aAlZPAB+GPM0quyocRRf7+eDoZX1zO9Z2tTu2f0sdkbmtOrnmlcTq+uZkNN1y7/KJwUc8I5MenhLrzLoTGKfV6K5o3Nyd8c8hy1kMjop/rjtIvch7qlvTeh1pv1NdgzLGT3bKtv9mYm1K8sY3ptzswCd7PieTIagPBA3hwxEmCfhPgh/dtiLu8knnyhEWDfE+BwatDkP2bZ3Ro="), Charsets.UTF_8)
    }
    private val officialStyleGrid: ByteArray by lazy {
        inflateTemplateData("eNrtkmlXgkAUhisNs13ItMVhaxVpA7IMSMT2lJAWLCz//5+YBkTkqNTnOjzn8N4779zLzD1nIBxmBQHXfQZ2FgE3fFyj202N9HpV2X71VIIgiOmEt4FhGIzEbxrLms/IxioC/sjgyFQu530Q5vPw8wtOwJl0ejKqL1ydTyZ/qY7gAJCCKIoSIFG2tY3Cpq7rhUOKPi2Xy2cUjbKdXRRAnQTkEcOeVyqVC4ZF2d6+G65Yhj0WJO8ngrQ4O1fkBOlOcBcnsiKrqqrIytL8QomXlXvZXUTc5PL65lZ7eCwahsEFYnBVTdNqjeaTaZqtQMxWtYZoNEuWZfGBWLxvP7u3sdEkBdsbx7dfwCsgbQB6Uif7NvVG0TZF9eSdXnZpt4eknflwHKfTCUsGx/HY/oP2P3qDzvgpYzu2YztsfwNaNBk8")
    }

    private fun inflateTemplateData(encoded: String): ByteArray =
        InflaterInputStream(ByteArrayInputStream(Base64.getDecoder().decode(encoded))).use { it.readBytes() }

    private fun officialStyleId(ref: String): Int {
        val row = ref.dropWhile { it.isLetter() }.toIntOrNull() ?: return 0
        val col = colIndex(ref)
        if (row !in 2..93 || col !in 2..24) return 0
        return officialStyleGrid[(row - 2) * 23 + (col - 2)].toInt() and 0xff
    }
    data class Movement(
        val date: String, val document: String, val documentNumber: String,
        val supplier: String, val recipient: String,
        val receipt: Double, val issue: Double, val balance: Double, val note: String
    )

    private fun dateParts(value: String): Pair<String, String> {
        val iso = Regex("^(\\d{4})-(\\d{2})-(\\d{2})$").matchEntire(value)
        if (iso != null) return (iso.groupValues[3] + "." + iso.groupValues[2] + ".") to (iso.groupValues[1] + ".")
        val local = Regex("^(\\d{2})\\.(\\d{2})\\.(\\d{4})$").matchEntire(value)
        if (local != null) return (local.groupValues[1] + "." + local.groupValues[2] + ".") to (local.groupValues[3] + ".")
        return value to ""
    }

    fun withRunningBalances(openingBalance: Double, movements: List<Movement>): List<Movement> {
        var balance = openingBalance
        return movements.map { movement ->
            balance += movement.receipt - movement.issue
            movement.copy(balance = balance)
        }
    }

    fun write(output: OutputStream, fields: List<Pair<String, String>>, movements: List<Movement>) {
        val cells = linkedMapOf<String, Cell>()
        fun put(ref: String, value: String, style: Int = 0) { cells[ref] = Cell(value, style, false) }
        fun number(ref: String, value: Double, style: Int = 0) { cells[ref] = Cell(qty(value), style, true) }
        val f = fields.toMap()
        fun field(label: String, fallback: String = "") = f[label] ?: fallback

        put("T2", "Додаток 31", 1)
        put("T3", "до Інструкції з обліку військового майна у", 1)
        put("B4", "(посада, військове звання, підпис, прізвище, ініціали)", 0)
        put("B9", field("Матеріально відповідальна особа", field("Посада та звання МВО")), 0)
        put("N4", "Картка № " + field("Номер картки"), 1)
        put("L5", "обліку військового майна (категорійного)", 1)
        put("T5", "Збройних Силах України", 1)
        put("B6", "\"         \"", 0)
        put("G6", "20              .", 0)
        put("T7", "(пункт 1 розділу  2)", 0)
        put("D10", "(матеріально відповідальна особа)", 0)
        put("B12", "Сховище №", 1)
        put("C12", field("Сховище / місце зберігання").ifBlank { "             ." })
        put("F12", "Полиця №", 1)
        put("G12", field("Полиця").ifBlank { "             ." })
        put("L12", "Ціна за одиницю", 1)
        put("O12", field("Ціна за одиницю"))
        put("R12", "Розпочато", 1)
        val startedDate = dateParts(field("Розпочато"))
        put("T12", startedDate.first.ifBlank { "\"______\"" })
        put("W12", startedDate.second.ifBlank { "20______." })
        put("B14", "Стелаж  №", 1)
        put("C14", field("Стелаж").ifBlank { "             ." })
        put("F14", "Сховище № ", 1)
        put("G14", field("Сховище / місце зберігання").ifBlank { "             ." })
        put("L14", "Вміст дорогоцінних металів", 1)
        put("O14", field("Вміст дорогоцінних металів"))
        put("R14", "Закінчено", 1)
        val endedDate = dateParts(field("Закінчено"))
        put("T14", endedDate.first.ifBlank { "\"______\"" })
        put("W14", endedDate.second.ifBlank { "20______." })
        put("B17", "Реєстраційний номер", 1)
        put("C17", "Номер аркуша", 1)
        put("E17", "Найменування військового майна", 1)
        put("J17", "Код номенклатури", 1)
        put("L17", "Одиниця виміру", 1)
        put("M17", "Номер парії", 1)
        put("O17", "Служба забезпечення", 1)
        put("Q17", "Центр забезпечення", 1)
        put("S17", "Військова частина", 1)
        put("T17", "Нормальний запас", 1)
        put("T18", "максимальний")
        put("W18", "мінімальний")
        put("B20", field("Реєстраційний номер / NSN"))
        put("C20", "1")
        put("E19", "(індекс, номер креслення)")
        put("E20", field("Найменування майна"))
        put("J20", field("Номенклатурний номер"))
        put("L20", field("Одиниця виміру"))
        put("M20", field("Номер партії"))
        put("O20", field("Служба забезпечення").ifBlank { "Речова" })
        put("Q20", field("Центр забезпечення"))
        put("S20", field("Військова частина").ifBlank { "А2756" })
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
            put("J${row + 3}", "з них закатегоріями (сортами)", 1)
            put("N${row + 3}", "Усього", 1)
            put("O${row + 3}", "з них закатегоріями (сортами)", 1)
            put("S${row + 3}", "Усього", 1)
            put("T${row + 3}", "з них закатегоріями (сортами)", 1)
            val cat = row + 4
            listOf("J" to "1", "K" to "2", "L" to "3", "O" to "1", "P" to "2", "Q" to "3", "T" to "1", "U" to "2", "V" to "3").forEach { (col, label) -> put("$col$cat", label, 1) }
            val nums = row + 6
            listOf("B" to "1", "C" to "2", "D" to "3", "E" to "4", "F" to "5", "G" to "6", "I" to "7", "J" to "8", "K" to "9", "L" to "10", "N" to "11", "O" to "12").forEach { (col, label) -> put("$col$nums", label, 1) }
        }
        headers(23)
        headers(51)

        (30..49).forEach { put("C$it", "АКТ") }
        (58..93).forEach { put("C$it", "АКТ") }

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

        val rowHeights = mutableMapOf(
            4 to 15.75, 11 to 15.75, 12 to 16.5, 14 to 15.75, 16 to 15.75,
            17 to 15.75, 19 to 15.75, 21 to 15.75, 22 to 15.75, 23 to 15.75,
            24 to 15.0, 25 to 15.75, 26 to 15.0, 28 to 33.0, 29 to 9.0,
            50 to 15.75, 51 to 15.75, 53 to 15.75, 54 to 15.75, 57 to 9.0
        )
        (30..49).forEach { rowHeights[it] = 18.75 }
        (58..93).forEach { rowHeights[it] = 18.75 }
        val rowNumbers = (2..93).toList()
        val rowXml = rowNumbers.joinToString("") { row ->
            val rowCells = (2..24).mapNotNull { col ->
                val ref = columnName(col) + row
                val cell = cells[ref]
                val style = officialStyleId(ref)
                if (cell == null && style == 255) null
                else if (cell == null) "<c r=\"$ref\" s=\"$style\"/>"
                else {
                    val value = esc(cell.value)
                    if (cell.numeric) "<c r=\"$ref\" s=\"$style\"><v>$value</v></c>"
                    else "<c r=\"$ref\" s=\"$style\" t=\"inlineStr\"><is><t xml:space=\"preserve\">$value</t></is></c>"
                }
            }.joinToString("")
            val height = rowHeights[row]?.let { " ht=\"$it\"" }.orEmpty()
            "<row r=\"$row\"$height>$rowCells</row>"
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
            "<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\"><dimension ref=\"B2:X93\"/><sheetViews><sheetView workbookViewId=\"0\"/></sheetViews><sheetFormatPr defaultRowHeight=\"15\"/><cols><col min=\"2\" max=\"2\" width=\"11.28515625\" customWidth=\"1\"/><col min=\"6\" max=\"6\" width=\"11.7109375\" customWidth=\"1\"/><col min=\"11\" max=\"11\" width=\"8.85546875\" customWidth=\"1\"/><col min=\"13\" max=\"13\" width=\"7\" customWidth=\"1\"/><col min=\"14\" max=\"14\" width=\"9.85546875\" customWidth=\"1\"/><col min=\"18\" max=\"18\" width=\"10.42578125\" customWidth=\"1\"/><col min=\"19\" max=\"19\" width=\"9.85546875\" customWidth=\"1\"/><col min=\"22\" max=\"22\" width=\"10.85546875\" customWidth=\"1\"/></cols><sheetData>$rowXml</sheetData><mergeCells count=\"${merges.size}\">$mergeXml</mergeCells><pageMargins left=\"0.7\" right=\"0.7\" top=\"0.75\" bottom=\"0.75\" header=\"0.3\" footer=\"0.3\"/><pageSetup paperSize=\"9\" scale=\"35\" orientation=\"landscape\" horizontalDpi=\"300\" verticalDpi=\"300\"/></worksheet>"
        val styles = officialStylesXml
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
    private fun columnName(index: Int): String {
        var n = index
        var name = ""
        while (n > 0) {
            val rem = (n - 1) % 26
            name = ('A'.code + rem).toChar() + name
            n = (n - 1) / 26
        }
        return name
    }
    private fun colIndex(ref: String): Int = ref.takeWhile { it.isLetter() }.fold(0) { n, ch -> n * 26 + ch.uppercaseChar().code - 'A'.code + 1 }
    private fun qty(value: Double): String = String.format(java.util.Locale.US, "%.3f", value).trimEnd('0').trimEnd('.')
    private fun esc(value: String): String = value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&apos;")
}
