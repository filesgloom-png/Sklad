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
                "database" -> {
                    paint.style = Paint.Style.FILL
                    canvas.drawOval(cx - s, cy - s * .72f, cx + s, cy - s * .25f, paint)
                    canvas.drawRect(cx - s, cy - s * .48f, cx + s, cy + s * .50f, paint)
                    canvas.drawOval(cx - s, cy + s * .20f, cx + s, cy + s * .70f, paint)
                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = s * .10f
                    canvas.drawOval(cx - s, cy - s * .72f, cx + s, cy - s * .25f, paint)
                    canvas.drawOval(cx - s, cy + s * .20f, cx + s, cy + s * .70f, paint)
                }
                "settings" -> {
                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = s * .22f
                    canvas.drawCircle(cx, cy, s * .65f, paint)
                    paint.strokeWidth = s * .30f
                    canvas.drawCircle(cx, cy, s * .20f, paint)
                    for (i in 0 until 8) {
                        val a = Math.toRadians(i * 45.0)
                        val x1 = cx + kotlin.math.cos(a).toFloat() * s * .82f
                        val y1 = cy + kotlin.math.sin(a).toFloat() * s * .82f
                        val x2 = cx + kotlin.math.cos(a).toFloat() * s * 1.08f
                        val y2 = cy + kotlin.math.sin(a).toFloat() * s * 1.08f
                        canvas.drawLine(x1, y1, x2, y2, paint)
                    }
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
        window.statusBarColor = Color.rgb(7, 18, 21)
        window.navigationBarColor = Color.rgb(7, 18, 21)
        window.decorView.systemUiVisibility = 0
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
        bar.addView(back, LinearLayout.LayoutParams(dp(58), dp(58)))
        bar.addView(TextView(this).apply {
            text = title
            textSize = 21f
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
        window.statusBarColor = Color.rgb(7, 18, 21)
        window.navigationBarColor = Color.rgb(7, 18, 21)
        window.decorView.systemUiVisibility = 0

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(7, 18, 21))
        }
        root.setOnApplyWindowInsetsListener { view, insets ->
            val bars = insets.getInsets(android.view.WindowInsets.Type.systemBars())
            view.setPadding(0, bars.top, 0, bars.bottom)
            insets
        }

        val scroll = ScrollView(this).apply {
            isFillViewport = true
            setPadding(0, 0, 0, dp(3))
        }
        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(8), dp(18), dp(6))
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val titleBox = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val title = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        title.addView(TextView(this).apply {
            text = "Облік-"
            textSize = 29f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.WHITE)
            includeFontPadding = false
        })
        title.addView(TextView(this).apply {
            text = "Склад"
            textSize = 28f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.rgb(226, 195, 111))
            includeFontPadding = false
        })
        titleBox.addView(title)
        titleBox.addView(TextView(this).apply {
            text = "Облік речового майна"
            textSize = 15f
            setTextColor(Color.rgb(174, 184, 190))
            setPadding(0, dp(2), 0, 0)
        })
        header.addView(titleBox, LinearLayout.LayoutParams(0, -2, 1f))
        header.addView(TextView(this).apply {
            text = "●  Офлайн ⌄"
            textSize = 13f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.rgb(255, 221, 221))
            gravity = Gravity.CENTER
            setPadding(dp(14), dp(9), dp(12), dp(9))
            background = GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                intArrayOf(Color.rgb(76, 18, 24), Color.rgb(38, 20, 23))
            ).apply {
                cornerRadius = dp(24).toFloat()
                setStroke(dp(1), Color.rgb(135, 39, 47))
            }
        })
        page.addView(header)

        val warehouseCount = db.warehouseRows().size
        val personCount = db.list("responsible_persons").size
        val materialCount = db.list("materials").size

        val stats = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(dp(5), dp(6), dp(5), dp(6))
            background = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(Color.rgb(35, 42, 41), Color.rgb(16, 23, 22))
            ).apply {
                cornerRadius = dp(20).toFloat()
                setStroke(dp(1), Color.rgb(88, 96, 91))
            }
        }

        val statItems = listOf(
            Triple("home", "Склади", warehouseCount.toString()),
            Triple("person", "МВО", personCount.toString()),
            Triple("boxes", "Позиції", materialCount.toString()),
            Triple("database", "Заг. вартість", "—")
        )
        statItems.forEachIndexed { index, item ->
            val box = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
            }
            box.addView(DashboardIconView(this, item.first, Color.rgb(226, 195, 111)),
                LinearLayout.LayoutParams(-1, dp(23)))
            box.addView(TextView(this).apply {
                text = item.second
                textSize = 11f
                setTextColor(Color.rgb(194, 197, 195))
                gravity = Gravity.CENTER
                setPadding(0, dp(2), 0, 0)
            })
            box.addView(TextView(this).apply {
                text = item.third
                textSize = 18f
                setTypeface(null, Typeface.BOLD)
                setTextColor(Color.WHITE)
                gravity = Gravity.CENTER
                setPadding(0, dp(1), 0, 0)
            })
            stats.addView(box, LinearLayout.LayoutParams(0, dp(62), 1f))
            if (index < 3) stats.addView(View(this).apply {
                setBackgroundColor(Color.rgb(70, 78, 75))
            }, LinearLayout.LayoutParams(dp(1), dp(44)))
        }
        page.addView(stats, LinearLayout.LayoutParams(-1, -2).apply {
            topMargin = dp(8)
            bottomMargin = dp(4)
        })

        fun sectionHeader(titleText: String, hint: String) {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, dp(5), 0, dp(4))
            }
            row.addView(View(this).apply {
                background = rounded(Color.rgb(224, 193, 102), 3)
            }, LinearLayout.LayoutParams(dp(5), dp(27)))
            row.addView(TextView(this).apply {
                text = titleText
                textSize = 16f
                setTypeface(null, Typeface.BOLD)
                setTextColor(Color.rgb(192, 201, 205))
                setPadding(dp(10), 0, 0, 0)
            }, LinearLayout.LayoutParams(0, -2, 1f))
            row.addView(TextView(this).apply {
                text = "$hint  ›"
                textSize = 12f
                setTextColor(Color.rgb(119, 132, 136))
                gravity = Gravity.CENTER_VERTICAL
            })
            page.addView(row)
        }

        fun darkCard(
            icon: String,
            titleText: String,
            subtitle: String,
            accent: Int,
            tint: Int,
            action: () -> Unit
        ): LinearLayout {
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(6), dp(4), dp(3), dp(4))
                background = GradientDrawable(
                    GradientDrawable.Orientation.LEFT_RIGHT,
                    intArrayOf(
                        Color.argb(105, Color.red(accent), Color.green(accent), Color.blue(accent)),
                        Color.rgb(15, 23, 23)
                    )
                ).apply {
                    cornerRadius = dp(14).toFloat()
                    setStroke(dp(1), Color.argb(175, Color.red(accent), Color.green(accent), Color.blue(accent)))
                }
                elevation = dp(2).toFloat()
                setOnClickListener { action() }
            }

            val iconHolder = FrameLayout(this).apply {
                background = GradientDrawable(
                    GradientDrawable.Orientation.TL_BR,
                    intArrayOf(
                        Color.argb(125, Color.red(accent), Color.green(accent), Color.blue(accent)),
                        Color.argb(55, Color.red(accent), Color.green(accent), Color.blue(accent))
                    )
                ).apply {
                    cornerRadius = dp(12).toFloat()
                    setStroke(dp(1), Color.argb(170, Color.red(accent), Color.green(accent), Color.blue(accent)))
                }
            }
            iconHolder.addView(DashboardIconView(this, icon, tint), FrameLayout.LayoutParams(-1, -1))
            card.addView(iconHolder, LinearLayout.LayoutParams(dp(42), dp(42)))

            val labels = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(5), 0, 0, 0)
            }
            labels.addView(TextView(this).apply {
                text = titleText
                textSize = 12.5f
                setTypeface(null, Typeface.BOLD)
                setTextColor(Color.WHITE)
                maxLines = 2
                ellipsize = android.text.TextUtils.TruncateAt.END
                includeFontPadding = false
            })
            labels.addView(TextView(this).apply {
                text = subtitle
                textSize = 9f
                setTextColor(Color.rgb(193, 201, 202))
                maxLines = 2
                ellipsize = android.text.TextUtils.TruncateAt.END
                includeFontPadding = false
                setPadding(0, dp(2), 0, 0)
            })
            card.addView(labels, LinearLayout.LayoutParams(0, -1, 1f))
            card.addView(TextView(this).apply {
                text = "›"
                textSize = 21f
                setTextColor(Color.rgb(225, 195, 111))
                gravity = Gravity.CENTER
            }, LinearLayout.LayoutParams(dp(14), dp(42)))
            return card
        }

        fun grid(
            left: Triple<String, String, String>,
            right: Triple<String, String, String>,
            leftAccent: Int,
            rightAccent: Int,
            leftAction: () -> Unit,
            rightAction: () -> Unit
        ) {
            val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            row.addView(
                darkCard(left.first, left.second, left.third, leftAccent, Color.WHITE, leftAction),
                LinearLayout.LayoutParams(0, dp(72), 1f).apply { rightMargin = dp(4) }
            )
            row.addView(
                darkCard(right.first, right.second, right.third, rightAccent, Color.WHITE, rightAction),
                LinearLayout.LayoutParams(0, dp(72), 1f).apply { leftMargin = dp(4) }
            )
            page.addView(row)
        }

        sectionHeader("ОСНОВНІ РОЗДІЛИ", "Управління довідниками")
        grid(
            Triple("home", "Склади", "Список складів та залишки"),
            Triple("person", "МВО", "Матеріально відповідальні особи"),
            Color.rgb(102, 126, 92), Color.rgb(92, 101, 103),
            { showWarehouses() }, { showPersons() }
        )
        grid(
            Triple("clipboard", "Картки обліку", "Облік по позиціях"),
            Triple("cube", "Номенклатура", "Довідник майна"),
            Color.rgb(171, 127, 40), Color.rgb(112, 64, 160),
            { showCards() }, { showMaterials() }
        )

        sectionHeader("РУХ МАЙНА", "Облік операцій з майном")
        grid(
            Triple("plus", "Надходження", "Приймання майна"),
            Triple("issue", "Видача", "Видача зі складу"),
            Color.rgb(12, 128, 91), Color.rgb(178, 124, 32),
            { showMovement("RECEIPT", "Надходження") }, { showMovement("ISSUE", "Видача") }
        )
        grid(
            Triple("transfer", "Переміщення", "Між складами"),
            Triple("trash", "Списання", "Списання майна"),
            Color.rgb(20, 105, 157), Color.rgb(177, 43, 55),
            { showMovement("TRANSFER", "Переміщення") }, { showMovement("WRITE_OFF", "Списання") }
        )

        sectionHeader("КОНТРОЛЬ", "Журнали та аналітика")
        grid(
            Triple("journal", "Журнал руху", "Всі операції"),
            Triple("chart", "Звіти", "Аналіз та звітність"),
            Color.rgb(112, 55, 157), Color.rgb(13, 122, 133),
            { showJournal() }, { showCards() }
        )

        scroll.addView(page)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        val nav = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(dp(3), dp(2), dp(3), dp(2))
            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(Color.rgb(28, 34, 34), Color.rgb(15, 22, 22))
            ).apply {
                cornerRadius = dp(22).toFloat()
                setStroke(dp(1), Color.rgb(70, 79, 77))
            }
            elevation = dp(5).toFloat()
        }

        fun navItem(icon: String, label: String, active: Boolean, action: () -> Unit): LinearLayout {
            val item = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(dp(1), dp(1), dp(1), dp(1))
                setOnClickListener { action() }
            }
            item.addView(View(this).apply {
                background = if (active) rounded(Color.rgb(226, 195, 111), 2) else null
            }, LinearLayout.LayoutParams(dp(50), dp(2)))
            item.addView(DashboardIconView(this, icon, if (active) Color.rgb(226, 195, 111) else Color.rgb(154, 164, 166)),
                LinearLayout.LayoutParams(dp(24), dp(25)))
            item.addView(TextView(this).apply {
                text = label
                textSize = 9.5f
                gravity = Gravity.CENTER
                maxLines = 1
                ellipsize = android.text.TextUtils.TruncateAt.END
                setTextColor(if (active) Color.rgb(226, 195, 111) else Color.rgb(154, 164, 166))
            }, LinearLayout.LayoutParams(-1, dp(17)))
            return item
        }

        nav.addView(navItem("home", "Головна", true) { showHome() }, LinearLayout.LayoutParams(0, dp(50), 1f))
        nav.addView(navItem("boxes", "Майно", false) { showWarehouses() }, LinearLayout.LayoutParams(0, dp(50), 1f))
        nav.addView(navItem("transfer", "Рух", false) { showMovement("TRANSFER", "Переміщення") }, LinearLayout.LayoutParams(0, dp(50), 1f))
        nav.addView(navItem("journal", "Журнал", false) { showJournal() }, LinearLayout.LayoutParams(0, dp(50), 1f))
        nav.addView(navItem("settings", "Налашт.", false) { showBackupMenu() }, LinearLayout.LayoutParams(0, dp(50), 1f))
        root.addView(nav, LinearLayout.LayoutParams(-1, dp(58)).apply {
            leftMargin = dp(12); rightMargin = dp(12); bottomMargin = dp(5)
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

    private class WarehouseBannerView(context: android.content.Context) : View(context) {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        override fun onDraw(canvas: Canvas) {
            val w=width.toFloat(); val h=height.toFloat()
            paint.style=Paint.Style.FILL
            paint.shader=android.graphics.LinearGradient(0f,0f,w,h,Color.rgb(38,49,47),Color.rgb(8,18,20),android.graphics.Shader.TileMode.CLAMP)
            canvas.drawRect(0f,0f,w,h,paint); paint.shader=null
            paint.color=Color.rgb(19,29,29)
            for(x in 0..7){ val lx=x*w/7.5f; canvas.drawRect(lx,h*.12f,lx+w*.012f,h,paint) }
            paint.color=Color.rgb(74,70,55)
            for(i in 0..5){ val y=h*(.18f+i*.14f); canvas.drawRect(w*.04f,y,w*.32f,y+h*.018f,paint); canvas.drawRect(w*.68f,y,w*.96f,y+h*.018f,paint) }
            paint.color=Color.rgb(104,94,68)
            for(i in 0..9){ val x=if(i%2==0)w*.08f else w*.74f; val y=h*(.24f+(i/2)*.14f); canvas.drawRoundRect(x,y,x+w*.16f,y+h*.09f,5f,5f,paint) }
            paint.color=Color.argb(80,226,195,111); canvas.drawCircle(w*.5f,h*.46f,h*.12f,paint)
        }
    }

    private fun showWarehouses(query:String="",filter:String="ALL"){
        window.statusBarColor=Color.rgb(7,18,21); window.navigationBarColor=Color.rgb(7,18,21); window.decorView.systemUiVisibility=0
        val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setBackgroundColor(Color.rgb(7,18,21))}
        root.setOnApplyWindowInsetsListener{v,i->val b=i.getInsets(android.view.WindowInsets.Type.systemBars());v.setPadding(0,b.top,0,b.bottom);i}
        val scroll=ScrollView(this).apply{isFillViewport=true}
        val page=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(20),dp(8),dp(20),dp(10))}
        val banner=FrameLayout(this).apply{background=rounded(Color.rgb(15,25,25),18);clipToOutline=true}
        banner.addView(WarehouseBannerView(this),FrameLayout.LayoutParams(-1,dp(118)))
        banner.addView(View(this).apply{background=GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,intArrayOf(Color.TRANSPARENT,Color.argb(220,7,18,21)))},FrameLayout.LayoutParams(-1,dp(118)))
        banner.addView(TextView(this).apply{text="‹";textSize=42f;setTextColor(Color.rgb(226,195,111));gravity=Gravity.CENTER;setOnClickListener{showHome()}},FrameLayout.LayoutParams(dp(48),dp(52)).apply{leftMargin=dp(4);topMargin=dp(3)})
        banner.addView(TextView(this).apply{text="Склади";textSize=29f;setTypeface(null,Typeface.BOLD);setTextColor(Color.WHITE);includeFontPadding=false},FrameLayout.LayoutParams(-2,-2).apply{leftMargin=dp(68);topMargin=dp(27)})
        banner.addView(TextView(this).apply{text="Склади нашої частини (ОЦЗ)";textSize=16f;setTextColor(Color.rgb(187,194,198))},FrameLayout.LayoutParams(-2,-2).apply{leftMargin=dp(68);topMargin=dp(66)})
        banner.addView(TextView(this).apply{text="+  Додати склад";textSize=14f;setTypeface(null,Typeface.BOLD);setTextColor(Color.WHITE);gravity=Gravity.CENTER;background=rounded(Color.rgb(65,54,29),12);setPadding(dp(8),0,dp(8),0);setOnClickListener{
            val persons=db.list("responsible_persons"); val labels=mutableListOf("Без призначеного МВО"); labels+=persons.map{it[1]}
            AlertDialog.Builder(this@MainActivity).setTitle("МВО складу").setItems(labels.toTypedArray()){_,selected->val rid=if(selected==0)null else persons[selected-1][0].toLongOrNull();formDialog("Новий склад",listOf("Назва","Адреса","Примітка")){v->if(v[0].isBlank())showError("Назва складу не може бути порожньою.") else{db.insertWarehouse(v[0],v[1],v[2],rid);showWarehouses(query,filter)}}}.show()
        }},FrameLayout.LayoutParams(dp(142),dp(48)).apply{rightMargin=dp(8);topMargin=dp(20);gravity=Gravity.RIGHT})
        page.addView(banner)
        val rows=db.warehouseRows(); val materials=db.list("materials")
        var totalQty=0.0; rows.forEach{w->materials.forEach{m->totalQty+=db.warehouseBalance(m[0].toLongOrNull()?:return@forEach,w[0].toLong())}}
        val stats=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;background=rounded(Color.rgb(17,27,29),14);setPadding(dp(4),dp(5),dp(4),dp(5))}
        listOf(Triple("home","Всього складів",rows.size.toString()),Triple("cube","Всього номенклатури",materials.size.toString()),Triple("boxes","Загальна кількість",formatQty(totalQty)),Triple("database","Загальна вартість","—")).forEachIndexed{i,x->
            val b=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER};b.addView(DashboardIconView(this,x.first,Color.rgb(226,195,111)),LinearLayout.LayoutParams(-1,dp(22)));b.addView(TextView(this).apply{text=x.second;textSize=8.5f;setTextColor(Color.rgb(171,180,183));gravity=Gravity.CENTER});b.addView(TextView(this).apply{text=x.third;textSize=17f;setTypeface(null,Typeface.BOLD);setTextColor(Color.WHITE);gravity=Gravity.CENTER});stats.addView(b,LinearLayout.LayoutParams(0,dp(60),1f));if(i<3)stats.addView(View(this).apply{setBackgroundColor(Color.rgb(65,73,72))},LinearLayout.LayoutParams(dp(1),dp(40))) }
        page.addView(stats,LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(8);bottomMargin=dp(8)})
        val search=EditText(this).apply{hint="⌕  Пошук по номеру, назві, місцю...";setText(query);textSize=14f;setSingleLine();setTextColor(Color.WHITE);setHintTextColor(Color.rgb(128,142,147));setPadding(dp(14),0,dp(10),0);background=rounded(Color.rgb(15,26,29),11)}
        page.addView(search,LinearLayout.LayoutParams(-1,dp(52)).apply{bottomMargin=dp(7)})
        val chips=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL}
        fun chip(t:String,sel:Boolean,act:()->Unit)=TextView(this).apply{text=t;textSize=10.5f;setTypeface(null,Typeface.BOLD);setTextColor(if(sel)Color.rgb(245,221,151)else Color.rgb(180,190,194));gravity=Gravity.CENTER;background=rounded(if(sel)Color.rgb(75,61,30)else Color.rgb(17,28,31),10);if(sel)(background as GradientDrawable).setStroke(dp(1),Color.rgb(180,145,61));setOnClickListener{act()}}
        chips.addView(chip("Всі (${rows.size})",filter=="ALL"){showWarehouses(query,"ALL")},LinearLayout.LayoutParams(0,dp(42),1f).apply{rightMargin=dp(4)})
        chips.addView(chip("●  Активні (${rows.size})",filter=="ACTIVE"){showWarehouses(query,"ACTIVE")},LinearLayout.LayoutParams(0,dp(42),1.15f).apply{rightMargin=dp(4)})
        chips.addView(chip("●  Неактивні (0)",filter=="INACTIVE"){showWarehouses(query,"INACTIVE")},LinearLayout.LayoutParams(0,dp(42),1.15f))
        page.addView(chips,LinearLayout.LayoutParams(-1,dp(42)).apply{bottomMargin=dp(7)})
        val visible=rows.filter{val q=query.trim();q.isBlank()||it.any{v->v.contains(q,true)}}
        visible.forEach{row->
            val id=row[0].toLong();val name=row[1];var qty=0.0;var pos=0;materials.forEach{m->val b=db.warehouseBalance(m[0].toLongOrNull()?:return@forEach,id);if(b!=0.0){pos++;qty+=b}}
            val card=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL;setPadding(dp(8),dp(8),dp(6),dp(8));background=GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,intArrayOf(Color.rgb(25,32,31),Color.rgb(11,21,23))).apply{cornerRadius=dp(15).toFloat();setStroke(dp(1),Color.rgb(47,60,59))};setOnClickListener{showWarehouseActions(id,name,row[2],row[4].toLongOrNull(),row.getOrNull(5)?:"")}}
            val thumb=FrameLayout(this).apply{background=rounded(Color.rgb(43,51,49),9);addView(WarehouseBannerView(this@MainActivity),FrameLayout.LayoutParams(-1,-1))};card.addView(thumb,LinearLayout.LayoutParams(dp(92),dp(92)))
            val mid=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(9),0,dp(3),0)};mid.addView(TextView(this).apply{text=name;textSize=16f;setTypeface(null,Typeface.BOLD);setTextColor(Color.WHITE);maxLines=1});mid.addView(TextView(this).apply{text=if(row[5].isNotBlank())row[5] else if(row[2].isNotBlank())row[2] else "Речове майно";textSize=12f;setTextColor(Color.rgb(172,184,187));maxLines=1;ellipsize=android.text.TextUtils.TruncateAt.END;setPadding(0,dp(3),0,0)});mid.addView(TextView(this).apply{text="⌖  ОЦЗ, територія частини";textSize=10.5f;setTextColor(Color.rgb(164,176,179));setPadding(0,dp(3),0,0)});card.addView(mid,LinearLayout.LayoutParams(0,dp(92),1f))
            val right=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER_VERTICAL;minimumWidth=dp(88)};right.addView(TextView(this).apply{text="●  Активний";textSize=9.5f;setTypeface(null,Typeface.BOLD);setTextColor(Color.rgb(23,220,151));gravity=Gravity.CENTER;background=rounded(Color.rgb(10,56,46),10);setPadding(dp(7),dp(5),dp(7),dp(5))});right.addView(TextView(this).apply{text="▦  Позиції  ${if (pos == 0) "—" else pos}";textSize=10.5f;setTextColor(Color.rgb(188,197,199));setPadding(0,dp(6),0,0)});right.addView(TextView(this).apply{text="▦  Кількість  ${if (qty == 0.0) "—" else formatQty(qty)}";textSize=10.5f;setTextColor(Color.rgb(188,197,199));setPadding(0,dp(3),0,0)});card.addView(right,LinearLayout.LayoutParams(dp(88),dp(92));card.addView(TextView(this).apply{text="›";textSize=29f;setTextColor(Color.rgb(226,195,111));gravity=Gravity.CENTER},LinearLayout.LayoutParams(dp(18),dp(92)))
            page.addView(card,LinearLayout.LayoutParams(-1,dp(108)).apply{bottomMargin=dp(7)})
        }
        if(visible.isEmpty())page.addView(TextView(this).apply{text="Складів не знайдено";textSize=16f;setTextColor(Color.LTGRAY);gravity=Gravity.CENTER;setPadding(0,dp(30),0,dp(30))})
        scroll.addView(page);root.addView(scroll,LinearLayout.LayoutParams(-1,0,1f))
        val nav=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER;background=rounded(Color.rgb(17,27,29),18);setPadding(dp(2),dp(2),dp(2),dp(2))}
        fun navItem(icon:String,label:String,active:Boolean,act:()->Unit)=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER;setOnClickListener{act()};addView(DashboardIconView(this@MainActivity,icon,if(active)Color.rgb(226,195,111)else Color.rgb(174,184,188)),LinearLayout.LayoutParams(dp(25),dp(27)));addView(TextView(this).apply{text=label;textSize=7.5f;gravity=Gravity.CENTER;maxLines=1;ellipsize=android.text.TextUtils.TruncateAt.END;setTextColor(if(active)Color.rgb(226,195,111)else Color.rgb(174,184,188))},LinearLayout.LayoutParams(-1,dp(16)))}
        nav.addView(navItem("home","Головна",false){showHome()},LinearLayout.LayoutParams(0,dp(58),1f));nav.addView(navItem("home","Склади",true){showWarehouses(query,filter)},LinearLayout.LayoutParams(0,dp(58),1f));nav.addView(navItem("cube","Номенкл.",false){showMaterials()},LinearLayout.LayoutParams(0,dp(58),1f));nav.addView(navItem("transfer","Рух майна",false){showMovement("TRANSFER","Переміщення")},LinearLayout.LayoutParams(0,dp(58),1f));nav.addView(navItem("chart","Звіти",false){showCards()},LinearLayout.LayoutParams(0,dp(58),1f));nav.addView(navItem("settings","Налаштув.",false){showBackupMenu()},LinearLayout.LayoutParams(0,dp(58),1f))
        root.addView(nav,LinearLayout.LayoutParams(-1,dp(64)).apply{leftMargin=dp(1);rightMargin=dp(1);bottomMargin=dp(2)});setContentView(root)
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
        box.addView(TextView(this).apply { text = title; textSize = 16f })
        box.addView(TextView(this).apply {
            text = subtitle
            textSize = 13f
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
        box.addView(TextView(this).apply { text = title; textSize = 16f })
        box.addView(TextView(this).apply {
            text = subtitle
            textSize = 13f
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
