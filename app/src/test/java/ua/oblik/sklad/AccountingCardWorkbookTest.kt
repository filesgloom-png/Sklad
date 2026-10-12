package ua.oblik.sklad

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
            listOf("Найменування майна" to "Тестовий матеріал"),
            listOf(
                AccountingCardWorkbook.Movement(
                    date = "2026-10-12",
                    document = "Надходження",
                    documentNumber = "DOC-42",
                    supplier = "Склад А",
                    recipient = "Склад Б",
                    receipt = 5.0,
                    issue = 0.0,
                    balance = 12.0,
                    note = "Примітка тесту"
                )
            )
        )

        val entries = linkedMapOf<String, String>()
        ZipInputStream(ByteArrayInputStream(output.toByteArray())).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                entries[entry.name] = zip.readBytes().toString(Charsets.UTF_8)
                zip.closeEntry()
            }
        }
        assertTrue("Workbook parts exist", entries.keys.containsAll(
            listOf("[Content_Types].xml", "_rels/.rels", "xl/workbook.xml", "xl/_rels/workbook.xml.rels", "xl/worksheets/sheet1.xml", "xl/styles.xml")
        ))
        entries.filterKeys { it.endsWith(".xml") || it.endsWith(".rels") }.values.forEach { xml ->
            DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(ByteArrayInputStream(xml.toByteArray(Charsets.UTF_8)))
        }

        val sheet = entries.getValue("xl/worksheets/sheet1.xml")
        assertTrue("Document name must stay in column C", sheet.contains("r=\"C30\"") && sheet.contains("Надходження"))
        assertTrue("Document number must stay in column D", sheet.contains("r=\"D30\"") && sheet.contains("DOC-42"))
        assertTrue("Receipt quantity must be in the receipt total cell", sheet.contains("r=\"I30\"") && sheet.contains("<v>5</v>"))
        assertTrue("Running balance must be in the balance total cell", sheet.contains("r=\"S30\"") && sheet.contains("<v>12</v>"))
        assertTrue("Note must be kept in the note column", sheet.contains("r=\"W30\"") && sheet.contains("Примітка тесту"))
        assertTrue("First numbering row must have its merged category cells", sheet.contains("ref=\"G29:H29\"") && sheet.contains("ref=\"V57:X57\""))
    }
}
