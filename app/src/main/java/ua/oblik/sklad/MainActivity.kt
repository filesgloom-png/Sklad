package ua.oblik.sklad

import android.app.Activity
import android.app.AlertDialog
import android.database.sqlite.SQLiteException
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import java.io.File
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {
    private val blue = Color.rgb(30, 91, 150)
    private val surface = Color.rgb(246, 248, 251)
    private val textPrimary = Color.rgb(28, 35, 43)
    private val textSecondary = Color.rgb(96, 108, 120)

    private class DashboardIconView(
        context: android.content.Context,
        private val kind: String,
        private val tint: Int
    ) : View(context) {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            strokeWidth = 3.2f
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            color = tint
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val w = width.toFloat()
            val h = height.toFloat()
            val cx = w / 2f
            val cy = h / 2f
            val s = minOf(w, h) * 0.31f
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = s * 0.11f

            when (kind) {
                "home" -> {
                    val p = Path().apply {
                        moveTo(cx - s, cy - s * 0.05f)
                        lineTo(cx, cy - s)
                        lineTo(cx + s, cy - s * 0.05f)
                        lineTo(cx + s, cy + s)
                        lineTo(cx - s, cy + s)
                        close()
                    }
                    canvas.drawPath(p, paint)
                    canvas.drawRect(cx - s * .28f, cy + s * .15f, cx + s * .28f, cy + s, paint)
                }
                "person" -> {
                    paint.style = Paint.Style.FILL
                    canvas.drawCircle(cx, cy - s * .52f, s * .32f, paint)
                    canvas.drawOval(cx - s * .78f, cy - s * .02f, cx + s * .78f, cy + s * .88f, paint)
                }
                "boxes" -> {
                    paint.style = Paint.Style.FILL
                    canvas.drawRoundRect(cx - s, cy - s * .35f, cx - s * .08f, cy + s * .55f, 5f, 5f, paint)
                    canvas.drawRoundRect(cx + s * .08f, cy - s * .35f, cx + s, cy + s * .55f, 5f, 5f, paint)
                    canvas.drawRoundRect(cx - s * .45f, cy - s * .92f, cx + s * .45f, cy - s * .02f, 5f, 5f, paint)
                    paint.color = Color.WHITE
                    canvas.drawCircle(cx, cy - s * .48f, s * .10f, paint)
                    paint.color = tint
                }
                "clipboard" -> {
                    canvas.drawRoundRect(cx - s * .72f, cy - s, cx + s * .72f, cy + s, 7f, 7f, paint)
                    canvas.drawRoundRect(cx - s * .30f, cy - s * 1.18f, cx + s * .30f, cy - s * .78f, 5f, 5f, paint)
                    canvas.drawLine(cx - s * .35f, cy - s * .30f, cx + s * .38f, cy - s * .30f, paint)
                    canvas.drawLine(cx - s * .35f, cy + s * .12f, cx + s * .38f, cy + s * .12f, paint)
                    canvas.drawLine(cx - s * .35f, cy + s * .54f, cx + s * .15f, cy + s * .54f, paint)
                }
                "cube" -> {
                    val p = Path().apply {
                        moveTo(cx, cy - s)
                        lineTo(cx + s * .86f, cy - s * .5f)
                        lineTo(cx + s * .86f, cy + s * .38f)
                        lineTo(cx, cy + s * .88f)
                        lineTo(cx - s * .86f, cy + s * .38f)
                        lineTo(cx - s * .86f, cy - s * .5f)
                        close()
                    }
                    canvas.drawPath(p, paint)
                    canvas.drawLine(cx, cy - s, cx, cy + s * .88f, paint)
                    canvas.drawLine(cx - s * .86f, cy - s * .5f, cx, cy, paint)
                    canvas.drawLine(cx + s * .86f, cy - s * .5f, cx, cy, paint)
                }
                "plus" -> {
                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = s * .22f
                    canvas.drawLine(cx - s, cy, cx + s, cy, paint)
                    canvas.drawLine(cx, cy - s, cx, cy + s, paint)
                }
                "issue" -> {
                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = s * .16f
                    canvas.drawLine(cx, cy + s, cx, cy - s * .82f, paint)
                    canvas.drawLine(cx - s * .42f, cy - s * .38f, cx, cy - s * .82f, paint)
                    canvas.drawLine(cx + s * .42f, cy - s * .38f, cx, cy - s * .82f, paint)
                    canvas.drawLine(cx - s * .65f, cy + s, cx + s * .65f, cy + s, paint)
                }
                "transfer" -> {
                    paint.strokeWidth = s * .16f
                    canvas.drawLine(cx - s, cy - s * .35f, cx + s * .55f, cy - s * .35f, paint)
                    canvas.drawLine(cx + s * .15f, cy - s * .75f, cx + s * .55f, cy - s * .35f, paint)
                    canvas.drawLine(cx + s * .55f, cy - s * .35f, cx + s * .15f, cy + s * .05f, paint)
                    canvas.drawLine(cx + s, cy + s * .35f, cx - s * .55f, cy + s * .35f, paint)
                    canvas.drawLine(cx - s * .15f, cy - s * .05f, cx - s * .55f, cy + s * .35f, paint)
                    canvas.drawLine(cx - s * .55f, cy + s * .35f, cx - s * .15f, cy + s * .75f, paint)
                }
                "trash" -> {
                    paint.style = Paint.Style.STROKE
                    canvas.drawRoundRect(cx - s * .62f, cy - s * .55f, cx + s * .62f, cy + s, 5f, 5f, paint)
                    canvas.drawLine(cx - s * .82f, cy - s * .78f, cx + s * .82f, cy - s * .78f, paint)
                    canvas.drawLine(cx - s * .30f, cy - s * 1.08f, cx + s * .30f, cy - s * 1.08f, paint)
                    canvas.drawLine(cx - s * .25f, cy - s * .25f, cx - s * .25f, cy + s * .65f, paint)
                    canvas.drawLine(cx, cy - s * .25f, cx, cy + s * .65f, paint)
                    canvas.drawLine(cx + s * .25f, cy - s * .25f, cx + s * .25f, cy + s * .65f, paint)
                }
                "journal" -> {
                    paint.style = Paint.Style.FILL
                    canvas.drawRoundRect(cx - s * .72f, cy - s, cx + s * .72f, cy + s, 7f, 7f, paint)
                    paint.color = Color.WHITE
                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = s * .11f
                    canvas.drawLine(cx - s * .38f, cy - s * .42f, cx + s * .35f, cy - s * .42f, paint)
                    canvas.drawLine(cx - s * .38f, cy, cx + s * .35f, cy, paint)
                    canvas.drawLine(cx - s * .38f, cy + s * .42f, cx + s * .20f, cy + s * .42f, paint)
                    paint.color = tint
                }
                "chart" -> {
                    paint.style = Paint.Style.FILL
                    canvas.drawRoundRect(cx - s, cy + s * .25f, cx - s * .42f, cy + s, 4f, 4f, paint)
                    canvas.drawRoundRect(cx - s * .25f, cy - s * .20f, cx + s * .32f, cy + s, 4f, 4f, paint)
                    canvas.drawRoundRect(cx + s * .50f, cy - s, cx + s, cy + s, 4f, 4f, paint)
                }
                else -> {
                    paint.style = Paint.Style.STROKE
                    canvas.drawCircle(cx, cy, s * .75f, paint)
                }
            }
        }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private fun rounded(color: Int, radius: Int = 14): GradientDrawable =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius).toFloat()
        }

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
            setBackgroundColor(surface)
            setPadding(dp(20), dp(18), dp(20), dp(16))
        }
        val bar = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
        val back = Button(this).apply {
            text = "‹"
            textSize = 28f
            setTextColor(blue)
            setAllCaps(false)
            minHeight = 0
            minimumHeight = 0
            background = rounded(Color.WHITE, 14)
            setPadding(0, 0, 0, dp(3))
            setOnClickListener { showHome() }
        }
        bar.addView(back, LinearLayout.LayoutParams(dp(52), dp(52)))
        bar.addView(TextView(this).apply {
            text = title
            textSize = 24f
            setTextColor(Color.rgb(20, 20, 20))
            setPadding(dp(12), 0, 0, 0)
        }, LinearLayout.LayoutParams(0, -2, 1f))
        root.addView(bar)
        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(18), 0, 0)
        }
        root.addView(ScrollView(this).apply { addView(content) }, LinearLayout.LayoutParams(-1, 0, 1f))
        return root
    }

    private fun showHome() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(247, 249, 253))
        }

        val scroll = ScrollView(this).apply {
            isFillViewport = true
            setPadding(0, 0, 0, dp(8))
        }
        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(22), dp(20), dp(8))
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val titleBox = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        titleBox.addView(TextView(this).apply {
            text = "Облік-Склад"
            textSize = 31f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(Color.rgb(16, 29, 48))
        })
        titleBox.addView(TextView(this).apply {
            text = "Облік речового майна"
            textSize = 16f
            setTextColor(textSecondary)
            setPadding(0, dp(4), 0, 0)
        })
        header.addView(titleBox, LinearLayout.LayoutParams(0, -2, 1f))
        header.addView(TextView(this).apply {
            text = "●  Офлайн"
            textSize = 15f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(Color.rgb(190, 32, 44))
            gravity = Gravity.CENTER
            setPadding(dp(15), dp(10), dp(15), dp(10))
            background = rounded(Color.rgb(255, 226, 229), 24)
        })
        page.addView(header)

        val stats = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, dp(22), 0, dp(22))
            background = rounded(Color.WHITE, 22)
            elevation = dp(2).toFloat()
        }
        val warehouseCount = db.warehouseRows().size
        val personCount = db.list("responsible_persons").size
        val materialCount = db.list("materials").size
        listOf(
            Triple("home", "Склади", warehouseCount.toString()),
            Triple("♟", "МВО", personCount.toString()),
            Triple("▦", "Позиції", materialCount.toString())
        ).forEachIndexed { index, item ->
            val box = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
            }
            val statIcon = when (index) {
                0 -> "home"
                1 -> "person"
                else -> "boxes"
            }
            box.addView(DashboardIconView(this, statIcon, Color.rgb(18, 91, 177)), LinearLayout.LayoutParams(-1, dp(34)))
            box.addView(TextView(this).apply {
                text = item.second
                textSize = 15f
                setTextColor(Color.rgb(91, 99, 111))
                gravity = Gravity.CENTER
                setPadding(0, dp(3), 0, 0)
            })
            box.addView(TextView(this).apply {
                text = item.third
                textSize = 20f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setTextColor(Color.rgb(20, 29, 43))
                gravity = Gravity.CENTER
                setPadding(0, dp(3), 0, 0)
            })
            stats.addView(box, LinearLayout.LayoutParams(0, dp(112), 1f))
            if (index < 2) stats.addView(View(this).apply {
                setBackgroundColor(Color.rgb(225, 228, 234))
            }, LinearLayout.LayoutParams(dp(1), dp(76)))
        }
        page.addView(stats)

        fun sectionTitle(text: String) {
            page.addView(TextView(this).apply {
                this.text = text
                textSize = 17f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setTextColor(Color.rgb(101, 109, 121))
                setPadding(dp(3), dp(18), 0, dp(10))
            })
        }

        fun card(
            icon: String,
            title: String,
            subtitle: String,
            iconBg: Int,
            iconColor: Int,
            action: () -> Unit
        ): LinearLayout {
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(12), dp(12), dp(8), dp(12))
                background = rounded(Color.WHITE, 18)
                elevation = dp(2).toFloat()
                setOnClickListener { action() }
            }

            val iconHolder = FrameLayout(this).apply {
                background = rounded(iconBg, 14)
            }
            iconHolder.addView(DashboardIconView(this, icon, iconColor), FrameLayout.LayoutParams(-1, -1))
            card.addView(iconHolder, LinearLayout.LayoutParams(dp(64), dp(64)))

            val labels = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(12), 0, dp(5), 0)
                minimumWidth = dp(105)
            }

            labels.addView(TextView(this).apply {
                text = title
                textSize = 16f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setTextColor(Color.rgb(25, 34, 47))
                maxLines = 2
                ellipsize = android.text.TextUtils.TruncateAt.END
                includeFontPadding = false
            }, LinearLayout.LayoutParams(-1, -2))

            labels.addView(TextView(this).apply {
                text = subtitle
                textSize = 13f
                setTextColor(Color.rgb(111, 120, 132))
                maxLines = 2
                ellipsize = android.text.TextUtils.TruncateAt.END
                includeFontPadding = false
                setPadding(0, dp(5), 0, 0)
            }, LinearLayout.LayoutParams(-1, -2))

            card.addView(labels, LinearLayout.LayoutParams(0, -1, 1f))

            card.addView(TextView(this).apply {
                text = "›"
                textSize = 30f
                setTextColor(Color.rgb(90, 100, 112))
                gravity = Gravity.CENTER
            }, LinearLayout.LayoutParams(dp(28), dp(70)))

            return card
        }

        fun grid(
            left: Triple<String, String, String>,
            right: Triple<String, String, String>,
            leftBg: Int,
            rightBg: Int,
            leftColor: Int,
            rightColor: Int,
            leftAction: () -> Unit,
            rightAction: () -> Unit
        ) {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
            }
            row.addView(
                card(left.first, left.second, left.third, leftBg, leftColor, leftAction),
                LinearLayout.LayoutParams(0, dp(132), 1f).apply { rightMargin = dp(7) }
            )
            row.addView(
                card(right.first, right.second, right.third, rightBg, rightColor, rightAction),
                LinearLayout.LayoutParams(0, dp(132), 1f).apply { leftMargin = dp(7) }
            )
            page.addView(row)
        }

        sectionTitle("ОБЛІК")
        grid(
            Triple("⌂", "Склади", "Список складів та залишки"),
            Triple("person", "МВО", "Матеріально відповідальні особи"),
            Color.rgb(224, 236, 255), Color.rgb(224, 248, 238),
            Color.rgb(20, 91, 176), Color.rgb(0, 160, 94),
            { showWarehouses() },
            { showPersons() }
        )
        grid(
            Triple("clipboard", "Картки обліку", "Облік по позиціях"),
            Triple("cube", "Номенклатура", "Довідник майна"),
            Color.rgb(255, 238, 211), Color.rgb(242, 232, 255),
            Color.rgb(205, 111, 0), Color.rgb(103, 42, 194),
            { showCards() },
            { showMaterials() }
        )

        sectionTitle("РУХ МАЙНА")
        grid(
            Triple("plus", "Надходження", "Приймання майна"),
            Triple("issue", "Видача", "Видача зі складу"),
            Color.rgb(220, 248, 235), Color.rgb(255, 226, 229),
            Color.rgb(0, 165, 92), Color.rgb(194, 35, 48),
            { showMovement("RECEIPT", "Надходження") },
            { showMovement("ISSUE", "Видача") }
        )
        grid(
            Triple("transfer", "Переміщення", "Між складами"),
            Triple("trash", "Списання", "Списання майна"),
            Color.rgb(224, 238, 255), Color.rgb(255, 226, 229),
            Color.rgb(21, 91, 176), Color.rgb(194, 35, 48),
            { showMovement("TRANSFER", "Переміщення") },
            { showMovement("WRITE_OFF", "Списання") }
        )

        sectionTitle("КОНТРОЛЬ")
        grid(
            Triple("journal", "Журнал руху", "Всі операції"),
            Triple("chart", "Звіти", "Аналіз та звітність"),
            Color.rgb(255, 246, 196), Color.rgb(242, 232, 255),
            Color.rgb(184, 145, 0), Color.rgb(91, 39, 191),
            { showJournal() },
            { showCards() }
        )

        scroll.addView(page)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        val nav = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(dp(4), dp(4), dp(4), dp(5))
            background = rounded(Color.WHITE, 22)
            elevation = dp(5).toFloat()
        }
        fun navItem(icon: String, label: String, active: Boolean, action: () -> Unit): LinearLayout {
            val item = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(dp(1), dp(3), dp(1), dp(2))
                setOnClickListener { action() }
            }
            item.addView(TextView(this).apply {
                text = icon
                textSize = 22f
                gravity = Gravity.CENTER
                setTextColor(if (active) Color.rgb(18, 91, 177) else Color.rgb(88, 96, 106))
            }, LinearLayout.LayoutParams(-1, dp(30)))
            item.addView(TextView(this).apply {
                text = label
                textSize = 10f
                gravity = Gravity.CENTER
                maxLines = 1
                ellipsize = android.text.TextUtils.TruncateAt.END
                setTextColor(if (active) Color.rgb(18, 91, 177) else Color.rgb(88, 96, 106))
            }, LinearLayout.LayoutParams(-1, dp(20)))
            return item
        }
        nav.addView(navItem("⌂", "Головна", true) { showHome() }, LinearLayout.LayoutParams(0, dp(58), 1f))
        nav.addView(navItem("▣", "Майно", false) { showWarehouses() }, LinearLayout.LayoutParams(0, dp(58), 1f))
        nav.addView(navItem("↔", "Рух", false) { showMovement("TRANSFER", "Переміщення") }, LinearLayout.LayoutParams(0, dp(58), 1f))
        nav.addView(navItem("▤", "Журнал", false) { showJournal() }, LinearLayout.LayoutParams(0, dp(58), 1f))
        nav.addView(navItem("⚙", "Налашт.", false) { showBackupMenu() }, LinearLayout.LayoutParams(0, dp(58), 1f))
        root.addView(nav, LinearLayout.LayoutParams(-1, dp(68)).apply {
            leftMargin = dp(12); rightMargin = dp(12); bottomMargin = dp(6)
        })
        setContentView(root)
    }

    private fun showBackupMenu() {
        AlertDialog.Builder(this)
            .setTitle("Налаштування")
            .setItems(arrayOf("Резервна копія", "Імпорт резервної копії", "Комірки")) { _, which ->
                when (which) {
                    0 -> exportBackup()
                    1 -> importBackup()
                    2 -> chooseWarehouseForLocations()
                }
            }
            .show()
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

    private fun showMaterials(query: String = "") {
        val root = base("Номенклатура")
        addAction(root, "🔎 Пошук / фільтр") { searchDialog("Пошук номенклатури", query) { q -> showMaterials(q) } }
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
        val rows = db.list("materials").filter { row ->
            query.isBlank() || row[1].contains(query, true) || row[2].contains(query, true) || row[3].contains(query, true) || row[4].contains(query, true) || row[5].contains(query, true)
        }
        rows.forEach { row ->
            val id = row[0].toLongOrNull() ?: return@forEach
            val balance = db.materialBalance(id)
            addManageRow(root, "${row[3]}  •  ${row[4]}", "NSN: ${row[1]}  |  Загальний залишок: ${formatQty(balance)}") {
                showMaterialActions(id, row)
            }
        }
        if (rows.isEmpty()) addRow(root, if (query.isBlank()) "Номенклатура порожня" else "Нічого не знайдено", if (query.isBlank()) "Додайте матеріали перед створенням документів." else "Змініть пошуковий запит.")
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

    private fun showJournal(query: String = "") {
        val root = base("Журнал руху")
        addAction(root, "🔎 Пошук / фільтр") { searchDialog("Пошук у журналі", query) { q -> showJournal(q) } }
        val rows = db.movementRows().filter { row -> query.isBlank() || row.any { value -> value.contains(query, true) } }
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
        if (rows.isEmpty()) addRow(root, if (query.isBlank()) "Журнал порожній" else "Нічого не знайдено", if (query.isBlank()) "Документи руху з’являться після першої операції." else "Змініть пошуковий запит.")
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

    private val BACKUP_CREATE = 4101
    private val BACKUP_OPEN = 4102

    private fun searchDialog(title: String, current: String, onSearch: (String) -> Unit) {
        val input = EditText(this).apply {
            hint = "Назва, NSN, документ, склад..."
            setSingleLine(true)
            setText(current)
            setSelection(text.length)
        }
        AlertDialog.Builder(this).setTitle(title).setView(input)
            .setNegativeButton("Скасувати", null)
            .setNeutralButton("Очистити") { _, _ -> onSearch("") }
            .setPositiveButton("Знайти") { _, _ -> onSearch(input.text.toString().trim()) }.show()
    }

    private fun databaseFile(): File = applicationContext.getDatabasePath("oblik_sklad.db")

    private fun exportBackup() {
        startActivityForResult(Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            type = "application/octet-stream"
            putExtra(Intent.EXTRA_TITLE, "oblik-sklad-backup-${today()}.db")
        }, BACKUP_CREATE)
    }

    private fun importBackup() {
        AlertDialog.Builder(this).setTitle("Імпорт резервної копії")
            .setMessage("Поточна локальна база буде замінена вибраною копією. Продовжити?")
            .setNegativeButton("Скасувати", null)
            .setPositiveButton("Продовжити") { _, _ ->
                startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    type = "*/*"
                    addCategory(Intent.CATEGORY_OPENABLE)
                }, BACKUP_OPEN)
            }.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK || data?.data == null) {
            if (requestCode == BACKUP_CREATE || requestCode == BACKUP_OPEN) {
                db = AppDb(this)
            }
            return
        }
        val uri = data.data ?: return
        try {
            when (requestCode) {
                BACKUP_CREATE -> {
                    db.writableDatabase.rawQuery("PRAGMA wal_checkpoint(FULL)", null).use { it.moveToFirst() }
                    db.close()
                    contentResolver.openOutputStream(uri)?.use { output ->
                        databaseFile().inputStream().use { input -> input.copyTo(output) }
                    } ?: throw IllegalStateException("Не вдалося створити резервну копію.")
                    db = AppDb(this)
                    Toast.makeText(this, "Резервну копію створено.", Toast.LENGTH_LONG).show()
                }
                BACKUP_OPEN -> {
                    val temp = File.createTempFile("oblik-sklad-import-", ".db", cacheDir)
                    try {
                        contentResolver.openInputStream(uri)?.use { input ->
                            temp.outputStream().use { output -> input.copyTo(output) }
                        } ?: throw IllegalStateException("Не вдалося прочитати резервну копію.")

                        android.database.sqlite.SQLiteDatabase.openDatabase(
                            temp.absolutePath,
                            null,
                            android.database.sqlite.SQLiteDatabase.OPEN_READONLY
                        ).use { imported ->
                            imported.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='materials'", null).use { cursor ->
                                if (!cursor.moveToFirst()) {
                                    throw IllegalArgumentException("Вибраний файл не є резервною копією «Облік-Склад».")
                                }
                            }
                        }

                        db.close()
                        temp.copyTo(databaseFile(), overwrite = true)
                        db = AppDb(this)
                        showHome()
                        Toast.makeText(this, "Резервну копію імпортовано.", Toast.LENGTH_LONG).show()
                    } finally {
                        temp.delete()
                    }
                }
            }
        } catch (e: Exception) {
            db = AppDb(this)
            showError("Помилка резервної копії: ${e.message ?: "невідома помилка"}.")
        }
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
            textSize = 15f
            setTextColor(Color.WHITE)
            setAllCaps(false)
            minHeight = 0
            minimumHeight = 0
            gravity = Gravity.CENTER
            background = rounded(blue, 14)
            elevation = dp(2).toFloat()
            setOnClickListener { action() }
        }, LinearLayout.LayoutParams(-1, dp(56)).apply { bottomMargin = dp(12) })
    }

    private fun addManageRow(root: LinearLayout, title: String, subtitle: String, action: () -> Unit) {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(14), dp(16), dp(10))
            background = rounded(Color.WHITE, 16)
            elevation = dp(1).toFloat()
        }
        box.addView(TextView(this).apply { text = title; textSize = 17f })
        box.addView(TextView(this).apply {
            text = subtitle
            textSize = 14f
            setPadding(0, dp(6), 0, dp(8))
        })
        box.addView(Button(this).apply {
            text = "⚙ Керувати"
            setOnClickListener { action() }
        }, LinearLayout.LayoutParams(-1, dp(48)))
        root.addView(box, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(10) })
    }

    private fun addRow(root: LinearLayout, title: String, subtitle: String) {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(14), dp(16), dp(14))
            background = rounded(Color.WHITE, 16)
            elevation = dp(1).toFloat()
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
            setPadding(dp(24), dp(8), dp(24), 0)
        }
        val fields = labels.mapIndexed { index, label ->
            EditText(this).apply {
                hint = label
                if (index < initialValues.size) setText(initialValues[index])
                setSingleLine(true)
                layout.addView(this, LinearLayout.LayoutParams(-1, dp(58)))
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
