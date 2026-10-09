package ua.oblik.sklad

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class AppDb(context: Context) : SQLiteOpenHelper(context, "oblik_sklad.db", null, 7) {
    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
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
            CREATE TABLE warehouses(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                address TEXT DEFAULT '',
                note TEXT DEFAULT '',
                responsible_person_id INTEGER,
                warehouse_number TEXT DEFAULT '',
                property_type TEXT DEFAULT '',
                is_active INTEGER DEFAULT 1,
                FOREIGN KEY(responsible_person_id) REFERENCES responsible_persons(id)
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
                type TEXT NOT NULL CHECK(type IN ('RECEIPT','ISSUE','TRANSFER_OUT','TRANSFER_IN','WRITE_OFF')),
                quantity REAL NOT NULL CHECK(quantity > 0),
                from_warehouse_id INTEGER,
                to_warehouse_id INTEGER,
                from_location_id INTEGER,
                to_location_id INTEGER,
                document_no TEXT DEFAULT '',
                movement_date TEXT NOT NULL,
                note TEXT DEFAULT '',
                FOREIGN KEY(material_id) REFERENCES materials(id),
                FOREIGN KEY(from_warehouse_id) REFERENCES warehouses(id),
                FOREIGN KEY(to_warehouse_id) REFERENCES warehouses(id),
                FOREIGN KEY(from_location_id) REFERENCES storage_locations(id),
                FOREIGN KEY(to_location_id) REFERENCES storage_locations(id)
            )
        """.trimIndent())
        createIndexes(db)
        db.execSQL("""
            CREATE TABLE initial_stock(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                source_key TEXT NOT NULL UNIQUE,
                warehouse_code TEXT NOT NULL,
                app_warehouse_code TEXT NOT NULL,
                storage_type TEXT DEFAULT '',
                storage_location TEXT DEFAULT '',
                material_code TEXT DEFAULT '',
                description TEXT DEFAULT '',
                nsn TEXT DEFAULT '',
                size TEXT DEFAULT '',
                batch TEXT DEFAULT '',
                unit TEXT DEFAULT '',
                quantity REAL DEFAULT 0,
                price REAL DEFAULT 0
            )
        """.trimIndent())
        seedReferenceData(db)
    }

    private fun createIndexes(db: SQLiteDatabase) {
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_mov_material ON movements(material_id)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_mov_date ON movements(movement_date)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_mov_from_warehouse ON movements(from_warehouse_id)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_mov_to_warehouse ON movements(to_warehouse_id)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_mov_from_location ON movements(from_location_id)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_mov_to_location ON movements(to_location_id)")
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
        if (oldVersion < 4) {
            db.execSQL("ALTER TABLE movements ADD COLUMN from_location_id INTEGER")
            db.execSQL("ALTER TABLE movements ADD COLUMN to_location_id INTEGER")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_mov_from_location ON movements(from_location_id)")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_mov_to_location ON movements(to_location_id)")
        }
        if (oldVersion < 5) {
            db.execSQL("ALTER TABLE warehouses ADD COLUMN warehouse_number TEXT DEFAULT ''")
            db.execSQL("ALTER TABLE warehouses ADD COLUMN property_type TEXT DEFAULT ''")
            db.execSQL("ALTER TABLE warehouses ADD COLUMN is_active INTEGER DEFAULT 1")
        }
        if (oldVersion < 6) seedReferenceData(db)
        if (oldVersion < 7) db.execSQL("""CREATE TABLE IF NOT EXISTS initial_stock(
            id INTEGER PRIMARY KEY AUTOINCREMENT, source_key TEXT NOT NULL UNIQUE,
            warehouse_code TEXT NOT NULL, app_warehouse_code TEXT NOT NULL,
            storage_type TEXT DEFAULT '', storage_location TEXT DEFAULT '',
            material_code TEXT DEFAULT '', description TEXT DEFAULT '', nsn TEXT DEFAULT '',
            size TEXT DEFAULT '', batch TEXT DEFAULT '', unit TEXT DEFAULT '',
            quantity REAL DEFAULT 0, price REAL DEFAULT 0
        )""".trimIndent())
    }


    /**
     * Import the supplied warehouse and responsible-person reference lists once.
     * Existing user records are preserved; warehouse codes and exact person names
     * are used to avoid duplicate imports.
     */
    private fun seedReferenceData(db: SQLiteDatabase) {
        val people = listOf(
            "Гілявська Т.А. мол.сержант" to "мол.сержант",
            "Гладун Р.М. штаб-сержант" to "штаб-сержант",
            "Душин К.В солдат" to "солдат",
            "Бачинський Є.Л. штаб-сержант" to "штаб-сержант",
            "Іванішина Н.Д. пр.ЗСУ" to "пр.ЗСУ",
            "Журавський О.В. головний сержант" to "головний сержант",
            "Ковальська Т.І. пр.ЗСУ" to "пр.ЗСУ",
            "Лиса Г.В. мол.сержант" to "мол.сержант",
            "Робул С.В. пр.ЗСУ" to "пр.ЗСУ",
            "Зюбін С.В. пр.ЗСУ" to "пр.ЗСУ",
            "Стінська В.В. пр.ЗСУ" to "пр.ЗСУ",
            "Цюцькома Л.П. пр.ЗСУ" to "пр.ЗСУ",
            "Яриновська М.Ф. пр.ЗСУ" to "пр.ЗСУ",
            "Гончар" to ""
        )
        val personIds = mutableMapOf<String, Long>()
        people.forEach { (fullName, position) ->
            var id: Long? = null
            db.rawQuery("SELECT id FROM responsible_persons WHERE full_name=?", arrayOf(fullName)).use { c ->
                if (c.moveToFirst()) id = c.getLong(0)
            }
            if (id == null) {
                val values = ContentValues().apply {
                    put("full_name", fullName)
                    put("position", position)
                    put("phone", "")
                    put("note", "")
                }
                id = db.insertOrThrow("responsible_persons", null, values)
            }
            personIds[fullName] = id!!
        }

        val warehouses = listOf(
            "202A" to "Р№25 Гілявська",
            "20CA" to "КЕУ28-81 Гілявсь",
            "21DA" to "№44Ковальська",
            "22CA" to "№95 PRD Бачинськ",
            "22DA" to "Бачинський(L)",
            "22MA" to "Душин №36(L)",
            "22NA" to "Душин №29(3)",
            "22QA" to "Душин №CR",
            "22RA" to "Гладун №88(Прд)",
            "22UA" to "Журав (CR)",
            "22VA" to "№19 Гілявська",
            "24BA" to "Душин(ROZD)",
            "24CA" to "CR Лиса",
            "24DA" to "№39 Ковальська",
            "253A" to "Р№14 Гладун",
            "254A" to "Р№19 Душин",
            "255A" to "№22 Душин",
            "256A" to "№23 Душин",
            "257A" to "Р№24 Журав.",
            "258A" to "Стінська №14",
            "25BA" to "Журавський(ROZD)",
            "25CA" to "№19 Цюцькома Л.П",
            "260A" to "Р№29 Яриновська",
            "261A" to "№96Прод Гладун",
            "262A" to "№92Прод Гладун",
            "263A" to "Р№40 Цюцькома",
            "264A" to "№1 Цив-ий Душин",
            "265A" to "Р№42 Робул",
            "266A" to "№90 PRD Бачинськ",
            "267A" to "Р№42 Душин",
            "268A" to "№43 Журав",
            "269A" to "№26 ПММ Душин",
            "26BA" to "Гончар(ROZD)",
            "26CA" to "№1KRN Бачинський",
            "26DA" to "26DA",
            "270A" to "Р№46 Ковальська",
            "271A" to "Р№47 Лиса",
            "272A" to "Р№41 Яриновська",
            "273A" to "№41 Зюбін",
            "274A" to "№48 Зюбін",
            "275A" to "№49 Душин",
            "276A" to "Р№50 Душин",
            "27BA" to "Зюбін ROZD",
            "27CA" to "№1SLA Душин",
            "27DA" to "UMAN Гладун",
            "28BA" to "28BA",
            "28CA" to "№41РМ Душин",
            "28DA" to "ROZD Чернега",
            "29BA" to "Лиса ROZD",
            "29CA" to "№47Гілявська"
        )
        val ownerMatchers = listOf(
            "Гілявськ" to "Гілявська Т.А. мол.сержант",
            "Гладун" to "Гладун Р.М. штаб-сержант",
            "Душин" to "Душин К.В солдат",
            "Бачинськ" to "Бачинський Є.Л. штаб-сержант",
            "Журав" to "Журавський О.В. головний сержант",
            "Ковальськ" to "Ковальська Т.І. пр.ЗСУ",
            "Лис" to "Лиса Г.В. мол.сержант",
            "Робул" to "Робул С.В. пр.ЗСУ",
            "Зюбін" to "Зюбін С.В. пр.ЗСУ",
            "Стінськ" to "Стінська В.В. пр.ЗСУ",
            "Цюцьком" to "Цюцькома Л.П. пр.ЗСУ",
            "Яриновськ" to "Яриновська М.Ф. пр.ЗСУ",
            "Гончар" to "Гончар"
        )
        warehouses.forEach { (code, description) ->
            var exists = false
            db.rawQuery("SELECT 1 FROM warehouses WHERE warehouse_number=?", arrayOf(code)).use { c ->
                exists = c.moveToFirst()
            }
            if (!exists) {
                val owner = ownerMatchers.firstOrNull { description.contains(it.first, ignoreCase = true) }?.second
                val values = ContentValues().apply {
                    put("name", description)
                    put("address", "")
                    put("note", "")
                    put("warehouse_number", code)
                    put("property_type", "")
                    put("is_active", 1)
                    val ownerId = owner?.let { personIds[it] }
                    if (ownerId == null) putNull("responsible_person_id") else put("responsible_person_id", ownerId)
                }
                db.insertOrThrow("warehouses", null, values)
            }
        }
    }

    fun replaceInitialStock(rows: List<Array<String>>): Int {
        val database = writableDatabase
        database.beginTransaction()
        try {
            database.delete("initial_stock", null, null)
            rows.forEach { row ->
                database.insertOrThrow("initial_stock", null, ContentValues().apply {
                    put("source_key", row[0])
                    put("warehouse_code", row[1])
                    put("app_warehouse_code", row[2])
                    put("storage_type", row[3])
                    put("storage_location", row[4])
                    put("material_code", row[5])
                    put("description", row[6])
                    put("nsn", row[7])
                    put("size", row[8])
                    put("batch", row[9])
                    put("unit", row[10])
                    put("quantity", row[11].replace(',', '.').toDoubleOrNull() ?: 0.0)
                    put("price", row[12].replace(',', '.').toDoubleOrNull() ?: 0.0)
                })
            }
            database.setTransactionSuccessful()
        } finally { database.endTransaction() }
        return rows.size
    }

    fun initialStockRows(appWarehouseCode: String, type: String = "ALL", query: String = ""): List<Array<String>> {
        val result = mutableListOf<Array<String>>()
        val sql = StringBuilder("SELECT warehouse_code, app_warehouse_code, storage_type, storage_location, material_code, description, nsn, size, batch, unit, quantity, price FROM initial_stock WHERE app_warehouse_code=?")
        val args = mutableListOf(appWarehouseCode)
        if (type != "ALL") { sql.append(" AND storage_type=?"); args.add(type) }
        if (query.isNotBlank()) {
            sql.append(" AND (description LIKE ? OR material_code LIKE ? OR nsn LIKE ? OR size LIKE ? OR batch LIKE ? OR storage_location LIKE ?)")
            repeat(6) { args.add("%$query%") }
        }
        sql.append(" ORDER BY storage_type, description, size, batch")
        readableDatabase.rawQuery(sql.toString(), args.toTypedArray()).use { c ->
            while (c.moveToNext()) result += Array(c.columnCount) { i -> c.getString(i) ?: "" }
        }
        return result
    }

    fun initialStockCount(): Int = readableDatabase.rawQuery("SELECT COUNT(*) FROM initial_stock", null).use { c -> if (c.moveToFirst()) c.getInt(0) else 0 }

    fun insertWarehouse(name: String, address: String, note: String, responsiblePersonId: Long?, number: String = "", propertyType: String = "", active: Boolean = true) =
        writableDatabase.insertOrThrow("warehouses", null, ContentValues().apply {
            put("name", name)
            put("address", address)
            put("note", note)
            put("warehouse_number", number)
            put("property_type", propertyType)
            put("is_active", if (active) 1 else 0)
            if (responsiblePersonId == null) putNull("responsible_person_id") else put("responsible_person_id", responsiblePersonId)
        })

    fun insertPerson(name: String, position: String, phone: String) =
        writableDatabase.insertOrThrow("responsible_persons", null, ContentValues().apply {
            put("full_name", name)
            put("position", position)
            put("phone", phone)
        })

    fun insertLocation(warehouseId: Long, name: String, note: String) =
        writableDatabase.insertOrThrow("storage_locations", null, ContentValues().apply {
            put("warehouse_id", warehouseId)
            put("name", name)
            put("note", note)
        })

    fun insertMaterial(nsn: String, nomenclature: String, name: String, unit: String, batch: String, price: Double) =
        writableDatabase.insertOrThrow("materials", null, ContentValues().apply {
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
        fromLocation: Long?,
        toLocation: Long?,
        documentNo: String,
        date: String,
        note: String
    ) = writableDatabase.insertOrThrow("movements", null, ContentValues().apply {
        put("material_id", materialId)
        put("type", type)
        put("quantity", quantity)
        if (fromWarehouse == null) putNull("from_warehouse_id") else put("from_warehouse_id", fromWarehouse)
        if (toWarehouse == null) putNull("to_warehouse_id") else put("to_warehouse_id", toWarehouse)
        if (fromLocation == null) putNull("from_location_id") else put("from_location_id", fromLocation)
        if (toLocation == null) putNull("to_location_id") else put("to_location_id", toLocation)
        put("document_no", documentNo)
        put("movement_date", date)
        put("note", note)
    })

    fun insertTransfer(
        materialId: Long,
        quantity: Double,
        fromWarehouse: Long,
        toWarehouse: Long,
        fromLocation: Long?,
        toLocation: Long?,
        documentNo: String,
        date: String,
        note: String
    ) {
        writableDatabase.beginTransaction()
        try {
            insertMovement(materialId, "TRANSFER_OUT", quantity, fromWarehouse, toWarehouse, fromLocation, toLocation, documentNo, date, note)
            insertMovement(materialId, "TRANSFER_IN", quantity, fromWarehouse, toWarehouse, fromLocation, toLocation, documentNo, date, note)
            writableDatabase.setTransactionSuccessful()
        } finally {
            writableDatabase.endTransaction()
        }
    }

    fun updateWarehouse(
        id: Long, name: String, address: String, note: String, responsiblePersonId: Long?,
        number: String? = null, propertyType: String? = null, active: Boolean? = null
    ) = writableDatabase.update("warehouses", ContentValues().apply {
        put("name", name); put("address", address); put("note", note)
        if (responsiblePersonId == null) putNull("responsible_person_id") else put("responsible_person_id", responsiblePersonId)
        if (number != null) put("warehouse_number", number)
        if (propertyType != null) put("property_type", propertyType)
        if (active != null) put("is_active", if (active) 1 else 0)
    }, "id=?", arrayOf(id.toString()))

    fun updatePerson(id: Long, name: String, position: String, phone: String) =
        writableDatabase.update("responsible_persons", ContentValues().apply {
            put("full_name", name); put("position", position); put("phone", phone)
        }, "id=?", arrayOf(id.toString()))

    fun updateLocation(id: Long, name: String, note: String) =
        writableDatabase.update("storage_locations", ContentValues().apply {
            put("name", name); put("note", note)
        }, "id=?", arrayOf(id.toString()))

    fun updateMaterial(id: Long, nsn: String, nomenclature: String, name: String, unit: String, batch: String, price: Double) =
        writableDatabase.update("materials", ContentValues().apply {
            put("nsn", nsn); put("nomenclature_no", nomenclature); put("name", name)
            put("unit", unit); put("batch", batch); put("price", price)
        }, "id=?", arrayOf(id.toString()))

    fun deleteWarehouse(id: Long): Boolean = writableDatabase.delete("warehouses", "id=?", arrayOf(id.toString())) > 0
    fun deletePerson(id: Long): Boolean = writableDatabase.delete("responsible_persons", "id=?", arrayOf(id.toString())) > 0
    fun deleteLocation(id: Long): Boolean = writableDatabase.delete("storage_locations", "id=?", arrayOf(id.toString())) > 0
    fun deleteMaterial(id: Long): Boolean = writableDatabase.delete("materials", "id=?", arrayOf(id.toString())) > 0

    fun warehouseHasMovements(id: Long): Boolean = exists("SELECT 1 FROM movements WHERE from_warehouse_id=? OR to_warehouse_id=? LIMIT 1", arrayOf(id.toString(), id.toString()))
    fun warehouseHasLocations(id: Long): Boolean = exists("SELECT 1 FROM storage_locations WHERE warehouse_id=? LIMIT 1", arrayOf(id.toString()))
    fun personAssigned(id: Long): Boolean = exists("SELECT 1 FROM warehouses WHERE responsible_person_id=? LIMIT 1", arrayOf(id.toString()))
    fun materialHasMovements(id: Long): Boolean = exists("SELECT 1 FROM movements WHERE material_id=? LIMIT 1", arrayOf(id.toString()))
    fun locationHasMovements(id: Long): Boolean = exists("SELECT 1 FROM movements WHERE from_location_id=? OR to_location_id=? LIMIT 1", arrayOf(id.toString(), id.toString()))
    private fun exists(sql: String, args: Array<String>): Boolean = readableDatabase.rawQuery(sql, args).use { it.moveToFirst() }

    fun list(table: String): List<Array<String>> {
        val result = mutableListOf<Array<String>>()
        val columns = when (table) {
            "responsible_persons" -> arrayOf("id", "full_name", "position", "phone")
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
            SELECT w.id, w.name, w.address, COALESCE(p.full_name, 'Не призначено'), w.responsible_person_id, w.note, COALESCE(w.warehouse_number, ''), COALESCE(w.property_type, ''), COALESCE(w.is_active, 1)
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

    fun locationsForWarehouse(warehouseId: Long): List<Array<String>> = locationRows(warehouseId)

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

    fun locationBalance(materialId: Long, locationId: Long): Double {
        var balance = 0.0
        val sql = """
            SELECT type, quantity,
                   COALESCE(from_location_id, -1),
                   COALESCE(to_location_id, -1)
            FROM movements
            WHERE material_id=?
              AND (from_location_id=? OR to_location_id=?)
        """.trimIndent()
        readableDatabase.rawQuery(sql, arrayOf(materialId.toString(), locationId.toString(), locationId.toString())).use { cursor ->
            while (cursor.moveToNext()) {
                val type = cursor.getString(0)
                val qty = cursor.getDouble(1)
                val from = cursor.getLong(2)
                val to = cursor.getLong(3)
                when (type) {
                    "RECEIPT", "TRANSFER_IN" -> if (to == locationId) balance += qty
                    "ISSUE", "WRITE_OFF", "TRANSFER_OUT" -> if (from == locationId) balance -= qty
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

    fun unassignedWarehouseBalance(materialId: Long, warehouseId: Long): Double {
        val total = warehouseBalance(materialId, warehouseId)
        var assigned = 0.0
        readableDatabase.rawQuery(
            "SELECT id FROM storage_locations WHERE warehouse_id=?",
            arrayOf(warehouseId.toString())
        ).use { cursor ->
            while (cursor.moveToNext()) {
                assigned += locationBalance(materialId, cursor.getLong(0))
            }
        }
        return total - assigned
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
                   COALESCE(wt.name, '—'),
                   COALESCE(lf.name, '—'),
                   COALESCE(lt.name, '—'),
                   m.note
            FROM movements m
            JOIN materials p ON p.id=m.material_id
            LEFT JOIN warehouses wf ON wf.id=m.from_warehouse_id
            LEFT JOIN warehouses wt ON wt.id=m.to_warehouse_id
            LEFT JOIN storage_locations lf ON lf.id=m.from_location_id
            LEFT JOIN storage_locations lt ON lt.id=m.to_location_id
            ORDER BY m.id DESC
        """.trimIndent()
        readableDatabase.rawQuery(sql, null).use { c ->
            while (c.moveToNext()) rows += Array(c.columnCount) { i -> c.getString(i) ?: "" }
        }
        return rows
    }
}
