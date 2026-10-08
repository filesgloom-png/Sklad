package ua.oblik.sklad

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class AppDb(context: Context) : SQLiteOpenHelper(context, "oblik_sklad.db", null, 2) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE warehouses(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                address TEXT DEFAULT '',
                note TEXT DEFAULT '',
                responsible_person_id INTEGER,
                FOREIGN KEY(responsible_person_id) REFERENCES responsible_persons(id)
            )
        """.trimIndent())
        db.execSQL("""
            CREATE TABLE responsible_persons(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                full_name TEXT NOT NULL,
                position TEXT DEFAULT '',
                phone TEXT DEFAULT '',
                note TEXT DEFAULT ''
            )
        """.trimIndent())
        db.execSQL("""
            CREATE TABLE storage_locations(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                warehouse_id INTEGER NOT NULL,
                name TEXT NOT NULL,
                note TEXT DEFAULT '',
                FOREIGN KEY(warehouse_id) REFERENCES warehouses(id)
            )
        """.trimIndent())
        db.execSQL("""
            CREATE TABLE materials(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nsn TEXT DEFAULT '',
                nomenclature_no TEXT DEFAULT '',
                name TEXT NOT NULL,
                unit TEXT NOT NULL,
                batch TEXT DEFAULT '',
                price REAL DEFAULT 0,
                note TEXT DEFAULT ''
            )
        """.trimIndent())
        db.execSQL("""
            CREATE TABLE movements(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                material_id INTEGER NOT NULL,
                type TEXT NOT NULL,
                quantity REAL NOT NULL,
                from_warehouse_id INTEGER,
                to_warehouse_id INTEGER,
                document_no TEXT DEFAULT '',
                movement_date TEXT NOT NULL,
                note TEXT DEFAULT '',
                FOREIGN KEY(material_id) REFERENCES materials(id),
                FOREIGN KEY(from_warehouse_id) REFERENCES warehouses(id),
                FOREIGN KEY(to_warehouse_id) REFERENCES warehouses(id)
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX idx_mov_material ON movements(material_id)")
        db.execSQL("CREATE INDEX idx_mov_date ON movements(movement_date)")
        db.execSQL("CREATE INDEX idx_mov_from_warehouse ON movements(from_warehouse_id)")
        db.execSQL("CREATE INDEX idx_mov_to_warehouse ON movements(to_warehouse_id)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE warehouses ADD COLUMN responsible_person_id INTEGER")
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS storage_locations(
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    warehouse_id INTEGER NOT NULL,
                    name TEXT NOT NULL,
                    note TEXT DEFAULT '',
                    FOREIGN KEY(warehouse_id) REFERENCES warehouses(id)
                )
            """.trimIndent())
        }
    }

    fun insertWarehouse(name: String, address: String, note: String, responsiblePersonId: Long?) =
        writableDatabase.insert("warehouses", null, ContentValues().apply {
            put("name", name)
            put("address", address)
            put("note", note)
            if (responsiblePersonId == null) putNull("responsible_person_id")
            else put("responsible_person_id", responsiblePersonId)
        })

    fun insertPerson(name: String, position: String, phone: String) =
        writableDatabase.insert("responsible_persons", null, ContentValues().apply {
            put("full_name", name)
            put("position", position)
            put("phone", phone)
        })

    fun insertLocation(warehouseId: Long, name: String, note: String) =
        writableDatabase.insert("storage_locations", null, ContentValues().apply {
            put("warehouse_id", warehouseId)
            put("name", name)
            put("note", note)
        })

    fun insertMaterial(nsn: String, nomenclature: String, name: String, unit: String, batch: String, price: Double) =
        writableDatabase.insert("materials", null, ContentValues().apply {
            put("nsn", nsn)
            put("nomenclature_no", nomenclature)
            put("name", name)
            put("unit", unit)
            put("batch", batch)
            put("price", price)
        })

    fun insertMovement(
        materialId: Long,
        type: String,
        quantity: Double,
        fromWarehouse: Long?,
        toWarehouse: Long?,
        documentNo: String,
        date: String,
        note: String
    ) = writableDatabase.insert("movements", null, ContentValues().apply {
        put("material_id", materialId)
        put("type", type)
        put("quantity", quantity)
        if (fromWarehouse == null) putNull("from_warehouse_id") else put("from_warehouse_id", fromWarehouse)
        if (toWarehouse == null) putNull("to_warehouse_id") else put("to_warehouse_id", toWarehouse)
        put("document_no", documentNo)
        put("movement_date", date)
        put("note", note)
    })

    fun list(table: String): List<Array<String>> {
        val result = mutableListOf<Array<String>>()
        val columns = when (table) {
            "responsible_persons" -> arrayOf("id", "full_name", "position")
            "materials" -> arrayOf("id", "nsn", "nomenclature_no", "name", "unit", "batch", "price")
            else -> arrayOf("id", "name", "address")
        }
        readableDatabase.query(table, columns, null, null, null, null, "id DESC").use { c ->
            while (c.moveToNext()) result += Array(c.columnCount) { i -> c.getString(i) ?: "" }
        }
        return result
    }

    fun warehouseRows(): List<Array<String>> {
        val rows = mutableListOf<Array<String>>()
        val sql = """
            SELECT w.id, w.name, w.address, COALESCE(p.full_name, 'Не призначено')
            FROM warehouses w
            LEFT JOIN responsible_persons p ON p.id=w.responsible_person_id
            ORDER BY w.id DESC
        """.trimIndent()
        readableDatabase.rawQuery(sql, null).use { c ->
            while (c.moveToNext()) rows += Array(c.columnCount) { i -> c.getString(i) ?: "" }
        }
        return rows
    }

    fun locationRows(warehouseId: Long): List<Array<String>> {
        val rows = mutableListOf<Array<String>>()
        readableDatabase.query(
            "storage_locations",
            arrayOf("id", "name", "note"),
            "warehouse_id=?",
            arrayOf(warehouseId.toString()),
            null, null, "id DESC"
        ).use { c ->
            while (c.moveToNext()) rows += Array(c.columnCount) { i -> c.getString(i) ?: "" }
        }
        return rows
    }

    fun materialBalance(materialId: Long): Double {
        var balance = 0.0
        readableDatabase.rawQuery(
            "SELECT type, quantity FROM movements WHERE material_id=?",
            arrayOf(materialId.toString())
        ).use { c ->
            while (c.moveToNext()) {
                when (c.getString(0)) {
                    "RECEIPT" -> balance += c.getDouble(1)
                    "ISSUE", "WRITE_OFF" -> balance -= c.getDouble(1)
                }
            }
        }
        return balance
    }

    fun warehouseBalance(materialId: Long, warehouseId: Long): Double {
        var balance = 0.0
        val sql = """
            SELECT type, quantity,
                   COALESCE(from_warehouse_id, -1),
                   COALESCE(to_warehouse_id, -1)
            FROM movements
            WHERE material_id=?
        """.trimIndent()
        readableDatabase.rawQuery(sql, arrayOf(materialId.toString())).use { c ->
            while (c.moveToNext()) {
                val type = c.getString(0)
                val qty = c.getDouble(1)
                val from = c.getLong(2)
                val to = c.getLong(3)
                when (type) {
                    "RECEIPT", "TRANSFER_IN" -> if (to == warehouseId) balance += qty
                    "ISSUE", "WRITE_OFF", "TRANSFER_OUT" -> if (from == warehouseId) balance -= qty
                }
            }
        }
        return balance
    }

    fun movementRows(): List<Array<String>> {
        val rows = mutableListOf<Array<String>>()
        val sql = """
            SELECT m.movement_date,
                   p.name,
                   m.type,
                   m.quantity,
                   m.document_no,
                   COALESCE(wf.name, '—'),
                   COALESCE(wt.name, '—')
            FROM movements m
            JOIN materials p ON p.id=m.material_id
            LEFT JOIN warehouses wf ON wf.id=m.from_warehouse_id
            LEFT JOIN warehouses wt ON wt.id=m.to_warehouse_id
            ORDER BY m.id DESC
        """.trimIndent()
        readableDatabase.rawQuery(sql, null).use { c ->
            while (c.moveToNext()) rows += Array(c.columnCount) { i -> c.getString(i) ?: "" }
        }
        return rows
    }
}
