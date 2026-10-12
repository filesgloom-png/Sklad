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

        warehouseA = db.insertWarehouse("Тестовий склад А", "", "", null, number = "WH-A")
        warehouseB = db.insertWarehouse("Тестовий склад Б", "", "", null, number = "WH-B")
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
    fun importedOpeningBalanceIsMatchedToCatalogAndUsedWithRealMovements() {
        db.writableDatabase.execSQL(
            """INSERT INTO initial_stock(
                source_key, warehouse_code, app_warehouse_code, storage_type, storage_location,
                material_code, description, nsn, size, batch, unit, quantity, price
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""".trimIndent(),
            arrayOf<Any>(
                "OPENING-LEDGER-TEST", "SRC-A", "WH-A", "Основний", "Комірка А",
                "TEST-001", "Тестовий матеріал", "TEST-001", "", "", "шт.", 5.0, 1.0
            )
        )
        assertEquals(5.0, db.initialStockBalanceForMaterial(material), 0.000001)
        db.insertMovement(material, "RECEIPT", 4.0, null, warehouseA, null, locationA, "TEST-LEDGER-RECEIPT", "2026-10-12", "")
        db.insertTransfer(material, 2.0, warehouseA, warehouseB, locationA, locationB, "TEST-LEDGER-TRANSFER", "2026-10-12", "")
        db.insertMovement(material, "ISSUE", 1.0, warehouseB, null, locationB, null, "TEST-LEDGER-ISSUE", "2026-10-12", "")
        db.insertMovement(material, "WRITE_OFF", 1.0, warehouseA, null, locationA, null, "TEST-LEDGER-WRITEOFF", "2026-10-12", "")
        assertEquals("Movement-only net excludes transfers", 2.0, db.materialBalance(material), 0.000001)
        assertEquals("Opening stock plus posted movements gives the real total", 7.0, db.initialStockBalanceForMaterial(material) + db.materialBalance(material), 0.000001)
        assertEquals(1.0, db.warehouseBalance(material, warehouseB), 0.000001)
        assertEquals(1.0, db.locationBalance(material, locationB), 0.000001)
    }

    @Test
    fun importedOpeningStockCanBeIssuedAndCannotBeOverIssued() {
        db.writableDatabase.execSQL(
            """INSERT INTO initial_stock(
                source_key, warehouse_code, app_warehouse_code, storage_type, storage_location,
                material_code, description, nsn, size, batch, unit, quantity, price
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""".trimIndent(),
            arrayOf<Any>(
                "OPENING-TEST-001", "SRC-A", "WH-A", "Основний", "Комірка А",
                "TEST-001", "Тестовий матеріал", "TEST-001", "", "", "шт.", 5.0, 1.0
            )
        )

        // Five units exist in Excel opening stock even though no movement rows exist yet.
        db.insertMovement(material, "ISSUE", 3.0, warehouseA, null, locationA, null,
            "TEST-OPENING-ISSUE-1", "2026-10-12", "")
        db.insertMovement(material, "ISSUE", 2.0, warehouseA, null, locationA, null,
            "TEST-OPENING-ISSUE-2", "2026-10-12", "")

        var rejected = false
        try {
            db.insertMovement(material, "ISSUE", 1.0, warehouseA, null, locationA, null,
                "TEST-OPENING-OVERISSUE", "2026-10-12", "")
        } catch (_: SQLiteException) {
            rejected = true
        }

        assertTrue("Нельзя выдать больше остатка Excel с учётом проведённых операций", rejected)
        assertEquals(-5.0, db.locationBalance(material, locationA), 0.000001)
        assertEquals(0, db.writableDatabase.rawQuery(
            "SELECT COUNT(*) FROM movements WHERE document_no='TEST-OPENING-OVERISSUE'",
            null
        ).use { cursor -> cursor.moveToFirst(); cursor.getInt(0) })
    }

    @Test
    fun openingStockFromUnknownExcelCellCannotBeIssuedAsUnassignedStock() {
        db.writableDatabase.execSQL(
            """INSERT INTO initial_stock(
                source_key, warehouse_code, app_warehouse_code, storage_type, storage_location,
                material_code, description, nsn, size, batch, unit, quantity, price
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""".trimIndent(),
            arrayOf<Any>(
                "OPENING-UNKNOWN-CELL", "SRC-A", "WH-A", "Основний", "Комірка якої немає в довіднику",
                "TEST-001", "Тестовий матеріал", "TEST-001", "", "", "шт.", 5.0, 1.0
            )
        )

        var rejected = false
        try {
            db.insertMovement(material, "ISSUE", 1.0, warehouseA, null, null, null,
                "TEST-UNKNOWN-CELL-ISSUE", "2026-10-12", "")
        } catch (_: SQLiteException) {
            rejected = true
        }

        assertTrue("Залишок із невідомої комірки Excel не можна видати без уточнення місця", rejected)
        assertEquals(0, db.writableDatabase.rawQuery(
            "SELECT COUNT(*) FROM movements WHERE document_no='TEST-UNKNOWN-CELL-ISSUE'",
            null
        ).use { cursor -> cursor.moveToFirst(); cursor.getInt(0) })
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


    @Test
    fun movementWithLocationFromAnotherWarehouseIsRejected() {
        var rejected = false
        try {
            db.insertMovement(material, "RECEIPT", 2.0, null, warehouseA, null, locationB, "TEST-WRONG-LOCATION", "2026-10-09", "")
        } catch (_: SQLiteException) {
            rejected = true
        }

        assertTrue("Комірка іншого складу не повинна приймати надходження", rejected)
        assertEquals(0.0, db.materialBalance(material), 0.000001)
        assertEquals(0.0, db.warehouseBalance(material, warehouseA), 0.000001)
        assertEquals(0.0, db.warehouseBalance(material, warehouseB), 0.000001)
    }

    @Test
    fun unknownMovementTypeIsRejectedWithoutChangingStock() {
        var rejected = false
        try {
            db.insertMovement(material, "CORRECTION", 2.0, null, warehouseA, null, null, "TEST-UNKNOWN-TYPE", "2026-10-09", "")
        } catch (_: SQLiteException) {
            rejected = true
        }

        assertTrue("Невідомий тип руху має відхилятися", rejected)
        assertEquals(0.0, db.materialBalance(material), 0.000001)
        assertEquals(0.0, db.warehouseBalance(material, warehouseA), 0.000001)
    }

    @Test
    fun invalidMovementDirectionIsRejectedWithoutChangingStock() {
        var rejected = false
        try {
            db.insertMovement(
                material, "RECEIPT", 2.0,
                warehouseA, warehouseB, null, locationB,
                "TEST-INVALID-DIRECTION", "2026-10-09", ""
            )
        } catch (_: SQLiteException) {
            rejected = true
        }

        assertTrue("Надходження не повинно мати склад-відправник", rejected)
        assertEquals(0.0, db.materialBalance(material), 0.000001)
        assertEquals(0.0, db.warehouseBalance(material, warehouseA), 0.000001)
        assertEquals(0.0, db.warehouseBalance(material, warehouseB), 0.000001)
        assertEquals(0, db.writableDatabase.rawQuery("SELECT COUNT(*) FROM movements", null)
            .use { cursor -> cursor.moveToFirst(); cursor.getInt(0) })
    }

    @Test
    fun invalidQuantitiesAreRejectedWithoutChangingStock() {
        val invalidQuantities = doubleArrayOf(0.0, -1.0, Double.NaN, Double.POSITIVE_INFINITY)

        invalidQuantities.forEachIndexed { index, quantity ->
            var rejected = false
            try {
                db.insertMovement(
                    material, "RECEIPT", quantity,
                    null, warehouseA, null, locationA,
                    "TEST-INVALID-QUANTITY-$index", "2026-10-09", ""
                )
            } catch (_: SQLiteException) {
                rejected = true
            }
            assertTrue("Некоректна кількість $quantity має відхилятися", rejected)
        }

        assertEquals(0.0, db.materialBalance(material), 0.000001)
        assertEquals(0.0, db.warehouseBalance(material, warehouseA), 0.000001)
        assertEquals(0.0, db.locationBalance(material, locationA), 0.000001)
        assertEquals(0, db.writableDatabase.rawQuery("SELECT COUNT(*) FROM movements", null)
            .use { cursor -> cursor.moveToFirst(); cursor.getInt(0) })
    }

    @Test
    fun openingStockInOneCellCannotBeIssuedFromAnotherCellOfSameWarehouse() {
        val secondCell = db.insertLocation(warehouseA, "Комірка А-2", "")
        db.writableDatabase.execSQL(
            """INSERT INTO initial_stock(
                source_key, warehouse_code, app_warehouse_code, storage_type, storage_location,
                material_code, description, nsn, size, batch, unit, quantity, price
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""".trimIndent(),
            arrayOf<Any>(
                "OPENING-CELL-ISOLATION", "SRC-A", "WH-A", "Основний", "Комірка А",
                "TEST-001", "Тестовий матеріал", "TEST-001", "", "", "шт.", 5.0, 2.0
            )
        )

        var rejected = false
        try {
            db.insertMovement(material, "ISSUE", 1.0, warehouseA, null, secondCell, null,
                "TEST-CROSS-CELL-ISSUE", "2026-10-12", "")
        } catch (_: SQLiteException) {
            rejected = true
        }

        assertTrue("Залишок першої комірки не можна витрачати з іншої комірки того самого складу", rejected)
        assertEquals(0.0, db.locationBalance(material, secondCell), 0.000001)
        assertEquals(0, db.writableDatabase.rawQuery(
            "SELECT COUNT(*) FROM movements WHERE document_no='TEST-CROSS-CELL-ISSUE'",
            null
        ).use { cursor -> cursor.moveToFirst(); cursor.getInt(0) })
    }

    private fun receipt(quantity: Double) {
        db.insertMovement(material, "RECEIPT", quantity, null, warehouseA, null, locationA, "TEST-RECEIPT", "2026-10-09", "")
    }
}
