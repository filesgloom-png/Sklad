package ua.oblik.sklad

import android.app.Activity
import android.app.AlertDialog
import android.database.sqlite.SQLiteException
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {
    private lateinit var db: AppDb
    private lateinit var content: LinearLayout

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        db = AppDb(this)
        showHome()
    }

    private fun base(title: String): LinearLayout {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
            setPadding(24, 28, 24, 24)
        }
        val bar = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
        val back = Button(this).apply {
            text = "‹"
            setOnClickListener { showHome() }
        }
        bar.addView(back, LinearLayout.LayoutParams(52, 52))
        bar.addView(TextView(this).apply {
            text = title
            textSize = 24f
            setTextColor(Color.rgb(20, 20, 20))
            setPadding(12, 0, 0, 0)
        }, LinearLayout.LayoutParams(0, -2, 1f))
        root.addView(bar)
        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 20, 0, 0)
        }
        root.addView(ScrollView(this).apply { addView(content) }, LinearLayout.LayoutParams(-1, 0, 1f))
        return root
    }

    private fun showHome() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 34, 24, 24)
            setBackgroundColor(Color.WHITE)
        }
        root.addView(TextView(this).apply {
            text = "Облік-Склад"
            textSize = 30f
            setTextColor(Color.rgb(25, 25, 25))
        })
        root.addView(TextView(this).apply {
            text = "Облік речового майна • офлайн"
            textSize = 17f
            setPadding(0, 6, 0, 22)
        })
        val buttons = listOf(
            "📦 Склади" to { showWarehouses() },
            "👤 МВО" to { showPersons() },
            "📍 Комірки" to { chooseWarehouseForLocations() },
            "📋 Номенклатура" to { showMaterials() },
            "➕ Надходження" to { showMovement("RECEIPT", "Надходження") },
            "📤 Видача" to { showMovement("ISSUE", "Видача") },
            "🔀 Переміщення" to { showMovement("TRANSFER", "Переміщення") },
            "🗑 Списання" to { showMovement("WRITE_OFF", "Списання") },
            "🗂 Картки обліку" to { showCards() },
            "📜 Журнал руху" to { showJournal() }
        )
        buttons.forEach { (label, action) ->
            root.addView(Button(this).apply {
                text = label
                textSize = 17f
                setOnClickListener { action() }
            }, LinearLayout.LayoutParams(-1, 58).apply { bottomMargin = 10 })
        }
        setContentView(root)
    }

    private fun showWarehouses() {
        val root = base("Склади")
        addAction(root, "Додати склад") {
            val persons = db.list("responsible_persons")
            val labels = mutableListOf("Без призначеного МВО")
            labels += persons.map { it[1] }
            AlertDialog.Builder(this)
                .setTitle("МВО складу")
                .setItems(labels.toTypedArray()) { _, selected ->
                    val responsibleId = if (selected == 0) null else persons[selected - 1][0].toLongOrNull()
                    formDialog("Новий склад", listOf("Назва", "Адреса", "Примітка")) { v ->
                        if (v[0].isBlank()) {
                            showError("Назва складу не може бути порожньою.")
                        } else {
                            db.insertWarehouse(v[0], v[1], v[2], responsibleId)
                            showWarehouses()
                        }
                    }
                }
                .show()
        }
        addAction(root, "Керувати комірками") { chooseWarehouseForLocations() }
        val rows = db.warehouseRows()
        rows.forEach {
            val id = it[0].toLong()
            val address = if (it[2].isBlank()) "Адресу не вказано" else it[2]
            addManageRow(root, "№$id  ${it[1]}", "${address} • МВО: ${it[3]}") {
                showWarehouseActions(id, it[1], it[2], it[4].toLongOrNull(), it.getOrNull(5) ?: "")
            }
        }
        if (rows.isEmpty()) addRow(root, "Складів ще немає", "Додайте перший склад перед проведенням руху.")
        setContentView(root)
    }

    private fun chooseWarehouseForLocations() {
        val warehouses = db.warehouseRows()
        if (warehouses.isEmpty()) {
            showError("Спочатку додайте хоча б один склад.")
            return
        }
        val names = warehouses.map { "${it[1]} • МВО: ${it[3]}" }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("Оберіть склад")
            .setItems(names) { _, which ->
                showLocations(warehouses[which][0].toLong(), warehouses[which][1])
            }
            .show()
    }

    private fun showLocations(warehouseId: Long, warehouseName: String) {
        val root = base("Комірки • ${warehouseName}")
        addAction(root, "Додати комірку") {
            formDialog("Нова комірка", listOf("Назва", "Примітка")) { v ->
                if (v[0].isBlank()) {
                    showError("Назва комірки не може бути порожньою.")
                } else {
                    db.insertLocation(warehouseId, v[0], v[1])
                    showLocations(warehouseId, warehouseName)
                }
            }
        }
        val rows = db.locationRows(warehouseId)
        rows.forEach {
            val id = it[0].toLong()
            addManageRow(root, "№$id  ${it[1]}", it.getOrNull(2) ?: "") {
                showLocationActions(warehouseId, warehouseName, id, it[1], it.getOrNull(2) ?: "")
            }
        }
        if (rows.isEmpty()) addRow(root, "Комірок ще немає", "Додайте місце зберігання для цього складу.")
        setContentView(root)
    }

    private fun showPersons() {
        val root = base("Матеріально відповідальні особи")
        addAction(root, "Додати МВО") {
            formDialog("Нова МВО", listOf("ПІБ", "Посада", "Телефон")) { v ->
                if (v[0].isBlank()) {
                    showError("ПІБ не може бути порожнім.")
                } else {
                    db.insertPerson(v[0], v[1], v[2])
                    showPersons()
                }
            }
        }
        val rows = db.list("responsible_persons")
        rows.forEach {
            val id = it[0].toLong()
            addManageRow(root, it[1], "${it.getOrNull(2) ?: ""}${if (it.getOrNull(3).orEmpty().isNotBlank()) " • ${it[3]}" else ""}") {
                showPersonActions(id, it[1], it.getOrNull(2) ?: "", it.getOrNull(3) ?: "")
            }
        }
        if (rows.isEmpty()) addRow(root, "МВО ще немає", "Додайте відповідальну особу перед призначенням на склад.")
        setContentView(root)
    }

    private fun showMaterials() {
        val root = base("Номенклатура")
        addAction(root, "Додати матеріал") {
            formDialog("Новий матеріал", listOf("NSN", "Номенклатурний номер", "Назва", "Одиниця", "Партія", "Ціна")) { v ->
                val price = v[5].replace(',', '.').toDoubleOrNull()
                if (v[2].isBlank() || v[3].isBlank()) {
                    showError("Заповніть назву та одиницю виміру.")
                } else if (price == null || !price.isFinite() || price < 0) {
                    showError("Ціна має бути числом не менше 0.")
                } else {
                    db.insertMaterial(v[0], v[1], v[2], v[3], v[4], price)
                    showMaterials()
                }
            }
        }
        val rows = db.list("materials")
        rows.forEach { row ->
            val id = row[0].toLongOrNull() ?: return@forEach
            val balance = db.materialBalance(id)
            addManageRow(root, "${row[3]}  •  ${row[4]}", "NSN: ${row[1]}  |  Загальний залишок: ${formatQty(balance)}") {
                showMaterialActions(id, row)
            }
        }
        if (rows.isEmpty()) addRow(root, "Номенклатура порожня", "Додайте матеріали перед створенням документів.")
        setContentView(root)
    }

    private fun showMovement(type: String, title: String) {
        val root = base(title)
        addAction(root, "Створити документ") {
            val mats = db.list("materials")
            val warehouses = db.warehouseRows()
            if (mats.isEmpty()) {
                showError("Спочатку додайте матеріал у Номенклатурі.")
                return@addAction
            }
            if (warehouses.isEmpty()) {
                showError("Спочатку додайте хоча б один склад.")
                return@addAction
            }
            val names = mats.map { "${it[3]} (${it[4]})" }.toTypedArray()
            AlertDialog.Builder(this)
                .setTitle("Оберіть матеріал")
                .setItems(names) { _, which ->
                    when (type) {
                        "TRANSFER" -> selectTransferWarehouses(mats[which][0].toLong(), title, warehouses)
                        else -> selectSingleWarehouse(mats[which][0].toLong(), type, title, warehouses)
                    }
                }
                .show()
        }

        val wanted = if (type == "TRANSFER") listOf("TRANSFER_OUT", "TRANSFER_IN") else listOf(type)
        db.movementRows().filter { it[2] in wanted }.forEach {
            val route = if (it[2].startsWith("TRANSFER")) " • ${it[5]} → ${it[6]}" else " • Склад: ${if (it[6] != "—") it[6] else it[5]}"
            val location = if (it[2].startsWith("TRANSFER")) " • Комірки: ${it[7]} → ${it[8]}" else if (it[2] == "RECEIPT") " • Комірка: ${it[8]}" else " • Комірка: ${it[7]}"
            addRow(root, "${it[0]} • ${it[1]}", "Кількість: ${formatQty(it[3].toDoubleOrNull() ?: 0.0)}  |  Документ: ${it[4]}${route}${location}")
        }
        setContentView(root)
    }

    private fun selectSingleWarehouse(
        materialId: Long,
        type: String,
        title: String,
        warehouses: List<Array<String>>
    ) {
        val action = if (type == "RECEIPT") "Куди оприбуткувати" else "З якого складу списати"
        val names = warehouses.map { it[1] }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle(action)
            .setItems(names) { _, which ->
                val warehouseId = warehouses[which][0].toLong()
                chooseLocation(warehouseId, "Комірка", true) { locationId ->
                    showMovementForm(materialId, type, title, warehouseId, null, locationId, null)
                }
            }
            .show()
    }

    private fun selectTransferWarehouses(
        materialId: Long,
        title: String,
        warehouses: List<Array<String>>
    ) {
        val names = warehouses.map { it[1] }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("З якого складу")
            .setItems(names) { _, fromIndex ->
                AlertDialog.Builder(this)
                    .setTitle("На який склад")
                    .setItems(names) { _, toIndex ->
                        if (fromIndex == toIndex) {
                            showError("Склад-відправник і склад-отримувач мають бути різними.")
                        } else {
                            val fromWarehouse = warehouses[fromIndex][0].toLong()
                            val toWarehouse = warehouses[toIndex][0].toLong()
                            chooseLocation(fromWarehouse, "Комірка-відправник", true) { fromLocation ->
                                chooseLocation(toWarehouse, "Комірка-отримувач", true) { toLocation ->
                                    showMovementForm(materialId, "TRANSFER", title, fromWarehouse, toWarehouse, fromLocation, toLocation)
                                }
                            }
                        }
                    }
                    .show()
            }
            .show()
    }

    private fun chooseLocation(warehouseId: Long, title: String, allowNone: Boolean, onSelected: (Long?) -> Unit) {
        val locations = db.locationRows(warehouseId)
        if (locations.isEmpty()) { onSelected(null); return }
        val labels = mutableListOf<String>()
        if (allowNone) labels += "Без комірки"
        labels += locations.map { it[1] }
        AlertDialog.Builder(this).setTitle(title).setItems(labels.toTypedArray()) { _, which ->
            onSelected(if (allowNone && which == 0) null else locations[if (allowNone) which - 1 else which][0].toLong())
        }.show()
    }

    private fun showMovementForm(
        materialId: Long,
        type: String,
        title: String,
        fromWarehouse: Long,
        toWarehouse: Long?,
        fromLocation: Long?,
        toLocation: Long?
    ) {
        formDialog(
            title,
            listOf("Кількість", "Номер документа", "Дата (РРРР-ММ-ДД)", "Примітка"),
            initialValues = listOf("", "", today(), "")
        ) { v ->
            val qty = v[0].replace(',', '.').toDoubleOrNull()
            val documentNo = v[1]
            val date = v[2].ifBlank { today() }
            if (qty == null || !qty.isFinite() || qty <= 0) {
                showError("Кількість має бути числом більше 0.")
                return@formDialog
            }
            if (!validDate(date)) {
                showError("Дата має бути у форматі РРРР-ММ-ДД.")
                return@formDialog
            }
            if (documentNo.isBlank()) {
                showError("Вкажіть номер документа.")
                return@formDialog
            }

            if (type == "RECEIPT") {
                safeDb { db.insertMovement(materialId, type, qty, null, fromWarehouse, null, toLocation, documentNo, date, v[3]) }
            } else if (type == "TRANSFER") {
                val current = if (fromLocation != null) db.locationBalance(materialId, fromLocation) else db.warehouseBalance(materialId, fromWarehouse)
                if (qty > current) {
                    val scope = if (fromLocation != null) "комірці" else "складі-відправнику"
                    showError("Недостатньо залишку на $scope. Доступно: ${formatQty(current)}.")
                    return@formDialog
                }
                safeDb { val targetWarehouse = toWarehouse
                if (targetWarehouse == null) {
                    showError("Не обрано склад-отримувач.")
                    return@safeDb
                }
                db.insertTransfer(materialId, qty, fromWarehouse, targetWarehouse, fromLocation, toLocation, documentNo, date, v[3]) }
            } else {
                val current = if (fromLocation != null) db.locationBalance(materialId, fromLocation) else db.warehouseBalance(materialId, fromWarehouse)
                if (qty > current) {
                    val scope = if (fromLocation != null) "комірці" else "складі"
                    showError("Недостатньо залишку на $scope. Доступно: ${formatQty(current)}.")
                    return@formDialog
                }
                safeDb { db.insertMovement(materialId, type, qty, fromWarehouse, null, fromLocation, null, documentNo, date, v[3]) }
            }
            showMovement(type, title)
        }
    }

    private fun showCards() {
        val root = base("Картки обліку")
        val materials = db.list("materials")
        val warehouses = db.warehouseRows()
        materials.forEach { material ->
            val materialId = material[0].toLongOrNull() ?: return@forEach
            addRow(
                root,
                material[3],
                "NSN ${material[1]} • Загальний залишок: ${formatQty(db.materialBalance(materialId))}"
            )
            warehouses.forEach { warehouse ->
                val balance = db.warehouseBalance(materialId, warehouse[0].toLong())
                if (balance != 0.0) {
                    addRow(root, "  ${warehouse[1]}", "Залишок: ${formatQty(balance)} ${material[4]}")
                    db.locationRows(warehouse[0].toLong()).forEach { location ->
                        val locationBalance = db.locationBalance(materialId, location[0].toLong())
                        if (locationBalance != 0.0) {
                            addRow(root, "    ↳ ${location[1]}", "Комірка: ${formatQty(locationBalance)} ${material[4]}")
                        }
                    }
                }
            }
        }
        if (materials.isEmpty()) addRow(root, "Карток ще немає", "Додайте матеріали в Номенклатурі.")
        setContentView(root)
    }

    private fun showJournal() {
        val root = base("Журнал руху")
        val rows = db.movementRows()
        rows.forEach {
            val route = if (it[2].startsWith("TRANSFER")) " • ${it[5]} → ${it[6]}"
            else if (it[2] == "RECEIPT") " • ${it[6]}"
            else " • ${it[5]}"
            addRow(
                root,
                "${it[0]} • ${it[1]}",
                "${typeLabel(it[2])} • ${formatQty(it[3].toDoubleOrNull() ?: 0.0)} • Документ ${it[4]}${route}${if (it[9].isNotBlank()) " • ${it[9]}" else ""}"
            )
        }
        if (rows.isEmpty()) addRow(root, "Журнал порожній", "Документи руху з’являться після першої операції.")
        setContentView(root)
    }

    private fun showWarehouseActions(id: Long, name: String, address: String, responsibleId: Long?, note: String) {
        AlertDialog.Builder(this).setTitle(name)
            .setItems(arrayOf("Редагувати", "Видалити")) { _, which ->
                if (which == 0) editWarehouse(id, name, address, responsibleId, note) else {
                    if (db.warehouseHasMovements(id)) showError("Склад має документи руху і не може бути видалений.")
                    else if (db.warehouseHasLocations(id)) showError("Спочатку видаліть комірки цього складу.")
                    else { db.deleteWarehouse(id); showWarehouses() }
                }
            }.show()
    }

    private fun editWarehouse(id: Long, name: String, address: String, responsibleId: Long?, note: String) {
        val persons = db.list("responsible_persons")
        val labels = mutableListOf("Без призначеного МВО")
        labels += persons.map { it[1] }
        val current = persons.indexOfFirst { it[0].toLongOrNull() == responsibleId } + 1
        AlertDialog.Builder(this).setTitle("МВО складу")
            .setSingleChoiceItems(labels.toTypedArray(), current.coerceAtLeast(0)) { dialog, selected ->
                val rid = if (selected == 0) null else persons[selected - 1][0].toLongOrNull()
                dialog.dismiss()
                formDialog("Редагувати склад", listOf("Назва", "Адреса", "Примітка"), initialValues = listOf(name, address, note)) { v ->
                    if (v[0].isBlank()) showError("Назва складу не може бути порожньою.")
                    else { db.updateWarehouse(id, v[0], v[1], v[2], rid); showWarehouses() }
                }
            }.show()
    }

    private fun showPersonActions(id: Long, name: String, position: String, phone: String) {
        AlertDialog.Builder(this).setTitle(name)
            .setItems(arrayOf("Редагувати", "Видалити")) { _, which ->
                if (which == 0) {
                    formDialog("Редагувати МВО", listOf("ПІБ", "Посада", "Телефон"), initialValues = listOf(name, position, phone)) { v ->
                        if (v[0].isBlank()) showError("ПІБ не може бути порожнім.")
                        else { db.updatePerson(id, v[0], v[1], v[2]); showPersons() }
                    }
                } else if (db.personAssigned(id)) showError("МВО призначена на склад і не може бути видалена.")
                else { db.deletePerson(id); showPersons() }
            }.show()
    }

    private fun showLocationActions(warehouseId: Long, warehouseName: String, id: Long, name: String, note: String) {
        AlertDialog.Builder(this).setTitle(name)
            .setItems(arrayOf("Редагувати", "Видалити")) { _, which ->
                if (which == 0) {
                    formDialog("Редагувати комірку", listOf("Назва", "Примітка"), initialValues = listOf(name, note)) { v ->
                        if (v[0].isBlank()) showError("Назва комірки не може бути порожньою.")
                        else { db.updateLocation(id, v[0], v[1]); showLocations(warehouseId, warehouseName) }
                    }
                } else if (db.locationHasMovements(id)) {
                    showError("Комірка має документи руху і не може бути видалена.")
                } else {
                    db.deleteLocation(id)
                    showLocations(warehouseId, warehouseName)
                }
            }.show()
    }

    private fun showMaterialActions(id: Long, row: Array<String>) {
        AlertDialog.Builder(this).setTitle(row[3])
            .setItems(arrayOf("Редагувати", "Видалити")) { _, which ->
                if (which == 0) {
                    formDialog("Редагувати матеріал", listOf("NSN", "Номенклатурний номер", "Назва", "Одиниця", "Партія", "Ціна"), initialValues = row.sliceArray(1..6).toList()) { v ->
                        val price = v[5].replace(',', '.').toDoubleOrNull()
                        if (v[2].isBlank() || v[3].isBlank()) showError("Заповніть назву та одиницю виміру.")
                        else if (price == null || !price.isFinite() || price < 0) showError("Ціна має бути кінцевим числом не менше 0.")
                        else { db.updateMaterial(id, v[0], v[1], v[2], v[3], v[4], price); showMaterials() }
                    }
                } else if (db.materialHasMovements(id)) showError("Матеріал має документи руху і не може бути видалений.")
                else { db.deleteMaterial(id); showMaterials() }
            }.show()
    }

    private fun typeLabel(type: String): String = when (type) {
        "RECEIPT" -> "Надходження"
        "ISSUE" -> "Видача"
        "TRANSFER_OUT" -> "Переміщення: вибуття"
        "TRANSFER_IN" -> "Переміщення: прибуття"
        "WRITE_OFF" -> "Списання"
        else -> type
    }

    private fun today(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    private fun validDate(value: String): Boolean = try {
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = false }
        val parsed = parser.parse(value) ?: return false
        parser.format(parsed) == value
    } catch (_: Exception) {
        false
    }

    private fun safeDb(action: () -> Unit) {
        try { action() } catch (e: SQLiteException) { showError("Не вдалося виконати операцію: ${e.message ?: "помилка бази даних"}.") }
    }

    private fun formatQty(value: Double): String =
        String.format(Locale.US, "%.2f", value).trimEnd('0').trimEnd('.')

    private fun showError(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Перевірка даних")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun addAction(root: LinearLayout, text: String, action: () -> Unit) {
        root.addView(Button(this).apply {
            this.text = text
            setOnClickListener { action() }
        }, LinearLayout.LayoutParams(-1, 58).apply { bottomMargin = 12 })
    }

    private fun addManageRow(root: LinearLayout, title: String, subtitle: String, action: () -> Unit) {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 14, 16, 10)
            setBackgroundColor(Color.rgb(245, 245, 245))
        }
        box.addView(TextView(this).apply { text = title; textSize = 17f })
        box.addView(TextView(this).apply {
            text = subtitle
            textSize = 14f
            setPadding(0, 6, 0, 8)
        })
        box.addView(Button(this).apply {
            text = "⚙ Керувати"
            setOnClickListener { action() }
        }, LinearLayout.LayoutParams(-1, 50))
        root.addView(box, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = 8 })
    }

    private fun addRow(root: LinearLayout, title: String, subtitle: String) {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 14, 16, 14)
            setBackgroundColor(Color.rgb(245, 245, 245))
        }
        box.addView(TextView(this).apply { text = title; textSize = 17f })
        box.addView(TextView(this).apply {
            text = subtitle
            textSize = 14f
            setPadding(0, 6, 0, 0)
        })
        root.addView(box, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = 8 })
    }

    private fun formDialog(title: String, labels: List<String>, initialValues: List<String> = emptyList(), onSave: (List<String>) -> Unit) {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(28, 8, 28, 0)
        }
        val fields = labels.mapIndexed { index, label ->
            EditText(this).apply {
                hint = label
                if (index < initialValues.size) setText(initialValues[index])
                setSingleLine(true)
                layout.addView(this, LinearLayout.LayoutParams(-1, 58))
            }
        }
        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(layout)
            .setNegativeButton("Скасувати", null)
            .setPositiveButton("Зберегти") { _, _ ->
                onSave(fields.map { it.text.toString().trim() })
            }
            .show()
    }
}
