package ua.oblik.sklad

import android.app.Application
import android.database.sqlite.SQLiteException
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class AppDbMovementTest {
    private lateinit var db: AppDb
    private var warehouseA = 0L
    private var warehouseB = 0L
    private var locationA = 0L
    private var locationB = 0L
    private var material = 0L

    @Before
    fun setUp() {
        val context = RuntimeEnvironment.getApplication()
        context.deleteDatabase("oblik_sklad.db")
        db = AppDb(context)
        db.writableDatabase

        warehouseA = db.insertWarehouse("Тестовий склад А", "", "", null)
        warehouseB = db.insertWarehouse("Тестовий склад Б", "", "", null)
        locationA = db.insertLocation(warehouseA, "Комірка А", "")
        locationB = db.insertLocation(warehouseB, "Комірка Б", "")
        material = db.insertMaterial("TEST-001", "TEST-001", "Тестовий матеріал", "шт.", "", 1.0)
    }

    @After
    fun tearDown() {
        db.close()
        RuntimeEnvironment.getApplication().deleteDatabase("oblik_sklad.db")
    }

    @Test
    fun receiptThenTransferAndIssueKeepAllBalancesConsistent() {
        receipt(10.0)

        assertEquals(10.0, db.materialBalance(material), 0.000001)
        assertEquals(10.0, db.warehouseBalance(material, warehouseA), 0.000001)
        assertEquals(10.0, db.locationBalance(material, locationA), 0.000001)

        db.insertTransfer(material, 4.0, warehouseA, warehouseB, locationA, locationB, "TEST-TRANSFER", "2026-10-09", "")

        assertEquals(10.0, db.materialBalance(material), 0.000001)
        assertEquals(6.0, db.warehouseBalance(material, warehouseA), 0.000001)
        assertEquals(4.0, db.warehouseBalance(material, warehouseB), 0.000001)
        assertEquals(6.0, db.locationBalance(material, locationA), 0.000001)
        assertEquals(4.0, db.locationBalance(material, locationB), 0.000001)

        db.insertMovement(material, "ISSUE", 3.0, warehouseB, null, locationB, null, "TEST-ISSUE", "2026-10-09", "")

        assertEquals(7.0, db.materialBalance(material), 0.000001)
        assertEquals(1.0, db.warehouseBalance(material, warehouseB), 0.000001)
        assertEquals(1.0, db.locationBalance(material, locationB), 0.000001)
    }

    @Test
    fun issueGreaterThanAvailableBalanceIsRejectedWithoutChangingStock() {
        receipt(5.0)

        var rejected = false
        try {
            db.insertMovement(material, "ISSUE", 6.0, warehouseA, null, locationA, null, "TEST-OVERISSUE", "2026-10-09", "")
        } catch (_: SQLiteException) {
            rejected = true
        }

        assertTrue("Выдача сверх остатка должна отклоняться", rejected)
        assertEquals(5.0, db.materialBalance(material), 0.000001)
        assertEquals(5.0, db.warehouseBalance(material, warehouseA), 0.000001)
        assertEquals(5.0, db.locationBalance(material, locationA), 0.000001)
    }

    @Test
    fun failedTransferInRollsBackEarlierTransferOut() {
        receipt(10.0)
        db.writableDatabase.execSQL("""
            CREATE TRIGGER fail_test_transfer_in
            BEFORE INSERT ON movements
            WHEN NEW.type = 'TRANSFER_IN'
            BEGIN
                SELECT RAISE(ABORT, 'forced transfer-in failure');
            END
        """.trimIndent())

        var rejected = false
        try {
            db.insertTransfer(material, 4.0, warehouseA, warehouseB, locationA, locationB, "TEST-ROLLBACK", "2026-10-09", "")
        } catch (_: SQLiteException) {
            rejected = true
        }

        assertTrue("Искусственная ошибка второго этапа должна возникнуть", rejected)
        assertEquals(0, db.writableDatabase.rawQuery(
            "SELECT COUNT(*) FROM movements WHERE type IN ('TRANSFER_OUT','TRANSFER_IN')",
            null
        ).use { cursor -> cursor.moveToFirst(); cursor.getInt(0) })
        assertEquals(10.0, db.materialBalance(material), 0.000001)
        assertEquals(10.0, db.warehouseBalance(material, warehouseA), 0.000001)
        assertEquals(0.0, db.warehouseBalance(material, warehouseB), 0.000001)
        assertEquals(10.0, db.locationBalance(material, locationA), 0.000001)
        assertEquals(0.0, db.locationBalance(material, locationB), 0.000001)
    }

    private fun receipt(quantity: Double) {
        db.insertMovement(material, "RECEIPT", quantity, null, warehouseA, null, locationA, "TEST-RECEIPT", "2026-10-09", "")
    }
}
