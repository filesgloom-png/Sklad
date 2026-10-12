package ua.oblik.sklad

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.ZipInputStream
import javax.xml.parsers.DocumentBuilderFactory

class AccountingCardWorkbookTest {
    @Test
    fun generatedWorkbookIsWellFormedAndKeepsDocumentFieldsAndBalancesInTheirOwnCells() {
        val output = ByteArrayOutputStream()
        AccountingCardWorkbook.write(
            output,
            listOf("Найменування майна" to "Тестовий матеріал", "Розпочато" to "2026-10-12"),
            listOf(AccountingCardWorkbook.Movement(
                date = "2026-10-12", document = "Надходження", documentNumber = "DOC-42",
                supplier = "Склад А", recipient = "Склад Б", receipt = 5.0, issue = 0.0,
                balance = 12.0, note = "Примітка тесту"
            ))
        )
        val entries = linkedMapOf<String, String>()
        ZipInputStream(ByteArrayInputStream(output.toByteArray())).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                entries[entry.name] = zip.readBytes().toString(Charsets.UTF_8)
                zip.closeEntry()
            }
        }
        assertTrue("Workbook parts exist", entries.keys.containsAll(listOf(
            "[Content_Types].xml", "_rels/.rels", "xl/workbook.xml",
            "xl/_rels/workbook.xml.rels", "xl/worksheets/sheet1.xml", "xl/styles.xml"
        )))
        entries.filterKeys { it.endsWith(".xml") || it.endsWith(".rels") }.values.forEach { xml ->
            DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(ByteArrayInputStream(xml.toByteArray(Charsets.UTF_8)))
        }
        val sheet = entries.getValue("xl/worksheets/sheet1.xml")
        val styles = entries.getValue("xl/styles.xml")
        assertTrue("Official font table is retained", styles.contains("<fonts count=\"8\""))
        assertTrue("Official border table is retained", styles.contains("<borders count=\"18\""))
        assertTrue("Official cell style table is retained", styles.contains("<cellXfs count=\"97\""))
        assertTrue("Body rows use official template typography", sheet.contains("r=\"C30\" s=\"91\""))
        assertTrue("Column-number row retains a template cell style", Regex("r=\\\"B29\\\" s=\\\"\\d+\\\"").containsMatchIn(sheet))
        assertTrue("Blank template cells retain their border/style definitions", sheet.contains("r=\"X93\""))
        assertTrue("Official MVO instruction stays in B4", sheet.contains("посада, військове звання, підпис, прізвище, ініціали"))
        assertTrue("MVO value is written on its designated signature line", sheet.contains("r=\"B9\"") && sheet.contains("Матеріально відповідальна особа"))
        assertTrue("Unit price is written in the merged value cell O12, not the label merge", sheet.contains("r=\"O12\"") && sheet.contains("Не визначена"))
        assertTrue("Precious-metals value is written outside the merged label L14:N14", sheet.contains("r=\"O14\""))
        assertTrue("Official drawing/index note is retained", sheet.contains("індекс, номер креслення"))
        assertTrue("Template category caption wording is retained", sheet.contains("закатегоріями (сортами)"))
        assertTrue("Blank start date preserves the official placeholder", sheet.contains("______") && sheet.contains("20______."))
        assertTrue("Document name must stay in column C", sheet.contains("r=\"C30\"") && sheet.contains("Надходження"))
        assertTrue("Document number must stay in column D", sheet.contains("r=\"D30\"") && sheet.contains("DOC-42"))
        assertTrue("Receipt quantity must be in receipt total cell", sheet.contains("r=\"I30\"") && sheet.contains("<v>5</v>"))
        assertTrue("Running balance must be in balance total cell", sheet.contains("r=\"S30\"") && sheet.contains("<v>12</v>"))
        assertTrue("Note must be kept in note column", sheet.contains("r=\"W30\"") && sheet.contains("Примітка тесту"))
        assertTrue("All official merge ranges are retained", sheet.contains("<mergeCells count=\"322\">"))
        assertTrue("Date and year are split into template cells", sheet.contains("r=\"T12\"") && sheet.contains("12.10.") && sheet.contains("r=\"W12\"") && sheet.contains("2026."))
        assertFalse("Date cells must not be merged beyond template", sheet.contains("ref=\"T12:V12\"") || sheet.contains("ref=\"T14:V14\""))
        assertTrue("Official default row height is retained", sheet.contains("defaultRowHeight=\"15\""))
        assertTrue("Official column B width is retained", sheet.contains("min=\"2\" max=\"2\" width=\"11.28515625\""))
    }

    @Test
    fun runningBalanceStartsWithImportedStockAndTransfersDoNotChangeTotal() {
        fun move(receipt: Double = 0.0, issue: Double = 0.0, name: String) =
            AccountingCardWorkbook.Movement("2026-10-12", name, name, "", "", receipt, issue, 0.0, "")
        val rows = AccountingCardWorkbook.withRunningBalances(5.0, listOf(
            move(name = "Початковий залишок"),
            move(receipt = 4.0, name = "Надходження"),
            move(name = "Переміщення: вибуття"),
            move(name = "Переміщення: прибуття"),
            move(issue = 3.0, name = "Видача"),
            move(issue = 1.0, name = "Списання")
        ))
        assertEquals(listOf(5.0, 9.0, 9.0, 9.0, 6.0, 5.0), rows.map { it.balance })
    }
}
