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
import java.util.zip.ZipFile
import org.xmlpull.v1.XmlPullParserFactory

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
        preloadBundledStockIfNeeded()
        window.statusBarColor = Color.rgb(7, 18, 21)
        window.navigationBarColor = Color.rgb(7, 18, 21)
        window.decorView.systemUiVisibility = 0
        showHome()
    }

    private fun preloadBundledStockIfNeeded() {
        val prefs = getSharedPreferences("stock_import", MODE_PRIVATE)
        if (prefs.getBoolean("bundled_stock_attempted", false) || db.initialStockCount() > 0) return
        try {
            val temp = File.createTempFile("bundled-stock-", ".xlsx", cacheDir)
            try {
                assets.open("Залишки.XLSX").use { input ->
                    temp.outputStream().use { output -> input.copyTo(output) }
                }
                val rows = parseInitialStock(Uri.fromFile(temp))
                val saved = db.replaceInitialStock(rows)
                prefs.edit().putBoolean("bundled_stock_attempted", true).apply()
                Toast.makeText(this, "Завантажено початкові залишки: $saved позицій", Toast.LENGTH_LONG).show()
            } finally {
                temp.delete()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Не вдалося завантажити вбудовані залишки: ${e.message ?: "помилка файлу"}", Toast.LENGTH_LONG).show()
        }
    }

    private fun base(title: String): LinearLayout {
        window.statusBarColor = Color.rgb(7, 18, 21)
        window.navigationBarColor = Color.rgb(7, 18, 21)
        window.decorView.systemUiVisibility = 0
        val root = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setBackgroundColor(Color.rgb(7,18,21)) }
        root.setOnApplyWindowInsetsListener { view, insets ->
            val bars=insets.getInsets(android.view.WindowInsets.Type.systemBars())
            view.setPadding(dp(14),bars.top+dp(5),dp(14),bars.bottom);insets
        }
        val bar=LinearLayout(this).apply{gravity=Gravity.CENTER_VERTICAL;setPadding(0,dp(5),0,dp(10))}
        bar.addView(TextView(this).apply{text="←";textSize=27f;gravity=Gravity.CENTER;setTextColor(Color.rgb(226,195,111));background=rounded(Color.rgb(18,29,29),12);setOnClickListener{showHome()}},LinearLayout.LayoutParams(dp(46),dp(46)))
        bar.addView(TextView(this).apply{text=title;textSize=21f;setTypeface(null,Typeface.BOLD);setTextColor(Color.WHITE);maxLines=2;setPadding(dp(12),0,0,0)},LinearLayout.LayoutParams(0,-2,1f))
        root.addView(bar)
        content=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(0,dp(3),0,dp(12))}
        root.addView(ScrollView(this).apply{isFillViewport=false;addView(content)},LinearLayout.LayoutParams(-1,0,1f))
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
            setPadding(dp(20), dp(14), dp(20), dp(12))
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
            text = "●  Офлайн"
            textSize = 12f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.rgb(179, 226, 205))
            gravity = Gravity.CENTER
            setPadding(dp(12), dp(9), dp(12), dp(9))
            background = GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                intArrayOf(Color.rgb(19, 66, 53), Color.rgb(16, 42, 37))
            ).apply {
                cornerRadius = dp(24).toFloat()
                setStroke(dp(1), Color.rgb(48, 117, 91))
            }
        })
        page.addView(header)

        val warehouseRows = db.warehouseRows()
        val warehouseCount = warehouseRows.count { it.getOrNull(8) != "0" }
        val personCount = db.list("responsible_persons").size
        val stockRows = db.initialStockRowsForAll()
        val nomenclatureCount = stockRows
            .map { listOf(it.getOrNull(4).orEmpty(), it.getOrNull(5).orEmpty(), it.getOrNull(6).orEmpty(), it.getOrNull(7).orEmpty(), it.getOrNull(8).orEmpty()).joinToString("|") }
            .distinct()
            .size

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
            Triple("boxes", "Номенкл.", nomenclatureCount.toString()),
            Triple("database", "Рядки Excel", stockRows.size.toString())
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
                textSize = 10.5f
                setTextColor(Color.rgb(205, 214, 215))
                gravity = Gravity.CENTER
                maxLines = 1
                ellipsize = android.text.TextUtils.TruncateAt.END
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
                setTextColor(Color.rgb(235, 240, 241))
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
                setPadding(dp(8), dp(6), dp(6), dp(6))
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
                textSize = 13.5f
                setTypeface(null, Typeface.BOLD)
                setTextColor(Color.WHITE)
                maxLines = 2
                ellipsize = android.text.TextUtils.TruncateAt.END
                includeFontPadding = false
            })
            labels.addView(TextView(this).apply {
                text = subtitle
                textSize = 10.5f
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
                LinearLayout.LayoutParams(0, dp(82), 1f).apply { rightMargin = dp(4) }
            )
            row.addView(
                darkCard(right.first, right.second, right.third, rightAccent, Color.WHITE, rightAction),
                LinearLayout.LayoutParams(0, dp(82), 1f).apply { leftMargin = dp(4) }
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

        grid(
            Triple("boxes", "Залишки", "Імпортовані залишки Excel"),
            Triple("database", "Імпорт Excel", "Завантажити таблицю залишків"),
            Color.rgb(18, 115, 108), Color.rgb(171, 127, 40),
            { showAllInitialStock() }, { importInitialStock() }
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
            Triple("chart", "Звіти", "Залишки по складах та типах"),
            Color.rgb(112, 55, 157), Color.rgb(13, 122, 133),
            { showJournal() }, { showReports() }
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
        val root = base("Налаштування")
        addScreenSummary(root, "СИСТЕМА", "Налаштування та безпека", "Резервні копії • Місця зберігання")
        addAction(root, "Імпортувати залишки з Excel") { importInitialStock() }
        addAction(root, "Резервна копія бази даних") { exportBackup() }
        addAction(root, "Відновити з резервної копії") {
            AlertDialog.Builder(this).setTitle("Відновлення даних")
                .setMessage("База даних буде замінена вибраною резервною копією. Перед продовженням переконайтеся, що маєте актуальну копію.")
                .setNegativeButton("Скасувати", null)
                .setPositiveButton("Обрати файл") { _, _ -> importBackup() }
                .show()
        }
        addAction(root, "Комірки та місця зберігання") { chooseWarehouseForLocations() }
        addEmptyState(root, "Дані залишаються локально", "Резервне копіювання допомагає перенести облік на інший пристрій. Реальні дані не створюються автоматично.")
        setContentView(root)
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

    private fun showAddWarehouseForm(query: String = "", filter: String = "ALL") {
        val gold = Color.rgb(226,195,111)
        val muted = Color.rgb(166,178,181)
        val root = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setBackgroundColor(Color.rgb(7,18,21)) }
        root.setOnApplyWindowInsetsListener { v, i -> val b=i.getInsets(android.view.WindowInsets.Type.systemBars()); v.setPadding(0,b.top,0,b.bottom); i }
        val scroll = ScrollView(this).apply { isFillViewport=true }
        val page = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(dp(20),dp(5),dp(20),dp(24)) }
        val hero = FrameLayout(this).apply { clipToOutline=true; background=rounded(Color.rgb(15,26,27),16) }
        hero.addView(WarehouseBannerView(this),FrameLayout.LayoutParams(-1,dp(94)))
        hero.addView(View(this).apply { background=GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,intArrayOf(Color.TRANSPARENT,Color.argb(225,7,18,21))) },FrameLayout.LayoutParams(-1,dp(94)))
        hero.addView(TextView(this).apply { text="←"; textSize=30f; setTextColor(gold); gravity=Gravity.CENTER; setOnClickListener{showWarehouses(query,filter)} },FrameLayout.LayoutParams(dp(48),dp(52)).apply{leftMargin=dp(2);topMargin=dp(4)})
        hero.addView(TextView(this).apply { text="Додати склад"; textSize=27f; setTypeface(null,Typeface.BOLD); setTextColor(Color.WHITE) },FrameLayout.LayoutParams(-2,-2).apply{leftMargin=dp(56);topMargin=dp(18)})
        hero.addView(TextView(this).apply { text="Створення нового складу"; textSize=15f; setTextColor(Color.rgb(190,198,200)) },FrameLayout.LayoutParams(-2,-2).apply{leftMargin=dp(56);topMargin=dp(56)})
        page.addView(hero,LinearLayout.LayoutParams(-1,dp(94)).apply{bottomMargin=dp(7)})
        val card=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(13),dp(9),dp(13),dp(12));background=GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,intArrayOf(Color.rgb(17,29,29),Color.rgb(10,21,23))).apply{cornerRadius=dp(18).toFloat();setStroke(dp(1),Color.rgb(38,50,48))}}
        fun fieldLabel(label:String, required:Boolean=false) { card.addView(TextView(this).apply{text=if(required)"$label *" else label;textSize=14f;setTextColor(Color.rgb(202,210,211));setPadding(dp(2),dp(4),0,dp(3))}) }
        fun input(hintText:String, icon:String, multiline:Boolean=false): EditText {
            val wrap=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL;background=GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,intArrayOf(Color.rgb(47,47,35),Color.rgb(18,29,28))).apply{cornerRadius=dp(10).toFloat();setStroke(dp(1),Color.rgb(47,59,55))}}
            wrap.addView(TextView(this).apply{text=icon;textSize=22f;gravity=Gravity.CENTER;setTextColor(gold);setPadding(dp(12),dp(10),dp(12),dp(10))},LinearLayout.LayoutParams(dp(50),if(multiline)dp(62)else dp(44)))
            val e=EditText(this).apply{hint=hintText;textSize=14f;setTextColor(Color.WHITE);setHintTextColor(Color.rgb(121,136,139));background=null;setPadding(dp(12),dp(8),dp(10),dp(8));if(multiline){minLines=2;gravity=Gravity.TOP}else setSingleLine(true)}
            wrap.addView(e,LinearLayout.LayoutParams(0,if(multiline)dp(72)else dp(48),1f))
            card.addView(wrap,LinearLayout.LayoutParams(-1,if(multiline)dp(62)else dp(44)).apply{bottomMargin=dp(3)})
            return e
        }
        fun choice(hintText:String, icon:String, values:List<String>, selected:(Int)->Unit) {
            val wrap=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL;background=GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,intArrayOf(Color.rgb(47,47,35),Color.rgb(18,29,28))).apply{cornerRadius=dp(10).toFloat();setStroke(dp(1),Color.rgb(47,59,55))}}
            wrap.addView(TextView(this).apply{text=icon;textSize=22f;gravity=Gravity.CENTER;setTextColor(gold)},LinearLayout.LayoutParams(dp(50),dp(44)))
            val label=TextView(this).apply{text=hintText;textSize=14f;setTextColor(muted);gravity=Gravity.CENTER_VERTICAL;setPadding(dp(12),0,0,0)}
            wrap.addView(label,LinearLayout.LayoutParams(0,dp(44),1f))
            wrap.addView(TextView(this).apply{text="⌄";textSize=22f;setTextColor(gold);gravity=Gravity.CENTER},LinearLayout.LayoutParams(dp(38),dp(44)))
            wrap.setOnClickListener{AlertDialog.Builder(this@MainActivity).setTitle(hintText).setItems(values.toTypedArray()){_,which->label.text=values[which];label.setTextColor(Color.WHITE);selected(which)}.show()}
            card.addView(wrap,LinearLayout.LayoutParams(-1,dp(44)).apply{bottomMargin=dp(3)})
        }
        fieldLabel("Назва складу",true); val name=input("Наприклад, Склад №1","⌂")
        fieldLabel("Номер складу",true); val number=input("Введіть номер","＃")
        fieldLabel("Тип майна"); var propertyType=""
        val types=listOf("Речове майно","Обмундирування","Взуття","Спорядження","Палатки, інвентар","Господарське майно","Резерв","Інше")
        choice("Оберіть тип майна","◇",types){propertyType=types[it]}
        fieldLabel("Місце розташування"); val location=input("Вкажіть місце розташування","●")
        val persons=db.list("responsible_persons"); var responsibleId:Long?=null
        fieldLabel("Матеріально відповідальна особа (МВО)")
        choice("Оберіть МВО","●",listOf("Не призначено")+persons.map{it[1]}){responsibleId=if(it==0)null else persons[it-1][0].toLongOrNull()}
        fieldLabel("Примітка"); val note=input("Додаткова інформація (необов’язково)","▤",true)
        var active=true
        val activeRow=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL;setPadding(dp(12),dp(9),dp(10),dp(9));background=rounded(Color.rgb(18,29,29),12)}
        val activeText=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL}
        activeText.addView(TextView(this).apply{text="Склад активний";textSize=14f;setTextColor(Color.WHITE)})
        activeText.addView(TextView(this).apply{text="Склад буде відображатися у списку";textSize=11f;setTextColor(muted);setPadding(0,dp(3),0,0)})
        activeRow.addView(activeText,LinearLayout.LayoutParams(0,-2,1f))
        val activeSwitch=Switch(this).apply{text="";isChecked=true;buttonTintList=android.content.res.ColorStateList.valueOf(gold);setOnCheckedChangeListener{_,checked->active=checked}}
        activeRow.addView(activeSwitch,LinearLayout.LayoutParams(-2,-2))
        card.addView(activeRow,LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(6);bottomMargin=dp(8)})
        val save=TextView(this).apply{text="▣   Зберегти склад";textSize=15f;setTypeface(null,Typeface.BOLD);setTextColor(Color.rgb(20,22,17));gravity=Gravity.CENTER;background=GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,intArrayOf(Color.rgb(246,218,133),Color.rgb(179,143,56))).apply{cornerRadius=dp(11).toFloat();setStroke(dp(1),Color.rgb(247,219,139))};setOnClickListener{
            val n=name.text.toString().trim();val no=number.text.toString().trim()
            if(n.isBlank())showError("Введіть назву складу.")
            else if(no.isBlank())showError("Введіть номер складу.")
            else {try{db.insertWarehouse(n,location.text.toString().trim(),note.text.toString().trim(),responsibleId,no,propertyType,active);showWarehouses(query,filter)}catch(e:Exception){showError("Не вдалося зберегти склад: "+e.message)}}
        }}
        card.addView(save,LinearLayout.LayoutParams(-1,dp(50)).apply{bottomMargin=dp(6)})
        card.addView(TextView(this).apply{text="Скасувати";textSize=14f;setTextColor(gold);gravity=Gravity.CENTER;background=GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,intArrayOf(Color.rgb(17,29,29),Color.rgb(12,22,23))).apply{cornerRadius=dp(11).toFloat();setStroke(dp(1),Color.rgb(86,75,44))};setOnClickListener{showWarehouses(query,filter)}},LinearLayout.LayoutParams(-1,dp(44)))
        page.addView(card)
        scroll.addView(page);root.addView(scroll,LinearLayout.LayoutParams(-1,0,1f))
        val nav=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER;background=rounded(Color.rgb(17,27,29),18);setPadding(dp(2),dp(2),dp(2),dp(2))}
        fun navItem(icon:String,label:String,activeItem:Boolean,action:()->Unit)=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER;setOnClickListener{action()};addView(DashboardIconView(this@MainActivity,icon,if(activeItem)gold else Color.rgb(174,184,188)),LinearLayout.LayoutParams(dp(25),dp(27)));addView(TextView(this@MainActivity).apply{text=label;textSize=7.5f;gravity=Gravity.CENTER;maxLines=1;ellipsize=android.text.TextUtils.TruncateAt.END;setTextColor(if(activeItem)gold else Color.rgb(174,184,188))},LinearLayout.LayoutParams(-1,dp(16)))}
        nav.addView(navItem("home","Головна",false){showHome()},LinearLayout.LayoutParams(0,dp(58),1f));nav.addView(navItem("home","Склади",true){showWarehouses(query,filter)},LinearLayout.LayoutParams(0,dp(58),1f));nav.addView(navItem("cube","Номенкл.",false){showMaterials()},LinearLayout.LayoutParams(0,dp(58),1f));nav.addView(navItem("transfer","Рух майна",false){showMovement("TRANSFER","Переміщення")},LinearLayout.LayoutParams(0,dp(58),1f));nav.addView(navItem("chart","Звіти",false){showCards()},LinearLayout.LayoutParams(0,dp(58),1f));nav.addView(navItem("settings","Налаштув.",false){showBackupMenu()},LinearLayout.LayoutParams(0,dp(58),1f))
        root.addView(nav,LinearLayout.LayoutParams(-1,dp(64)).apply{leftMargin=dp(1);rightMargin=dp(1);bottomMargin=dp(2)})
        setContentView(root)
    }

    private fun showWarehouses(query:String="",filter:String="ALL",sortMode:Int=0){
        window.statusBarColor=Color.rgb(7,18,21); window.navigationBarColor=Color.rgb(7,18,21); window.decorView.systemUiVisibility=0
        val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setBackgroundColor(Color.rgb(7,18,21))}
        root.setOnApplyWindowInsetsListener{v,i->val b=i.getInsets(android.view.WindowInsets.Type.systemBars());v.setPadding(0,b.top,0,b.bottom);i}
        val scroll=ScrollView(this).apply{isFillViewport=true}
        val page=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(16),dp(12),dp(16),dp(14))}
        val banner=FrameLayout(this).apply{background=rounded(Color.rgb(15,25,25),18);clipToOutline=true}
        banner.addView(WarehouseBannerView(this),FrameLayout.LayoutParams(-1,dp(118)))
        banner.addView(View(this).apply{background=GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,intArrayOf(Color.TRANSPARENT,Color.argb(220,7,18,21)))},FrameLayout.LayoutParams(-1,dp(118)))
        banner.addView(TextView(this).apply{text="←";textSize=30f;setTextColor(Color.rgb(226,195,111));gravity=Gravity.CENTER;setOnClickListener{showHome()}},FrameLayout.LayoutParams(dp(48),dp(52)).apply{leftMargin=dp(6);topMargin=dp(8)})
        banner.addView(TextView(this).apply{text="Склади";textSize=26f;setTypeface(null,Typeface.BOLD);setTextColor(Color.WHITE);includeFontPadding=false},FrameLayout.LayoutParams(-2,-2).apply{leftMargin=dp(55);topMargin=dp(27)})
        banner.addView(TextView(this).apply{text="Керування місцями зберігання";textSize=13f;setTextColor(Color.rgb(205,214,215));maxLines=1;ellipsize=android.text.TextUtils.TruncateAt.END},FrameLayout.LayoutParams(-1,-2).apply{leftMargin=dp(55);rightMargin=dp(142);topMargin=dp(76)})
        banner.addView(TextView(this).apply{text="+ Додати";textSize=12f;setTypeface(null,Typeface.BOLD);setTextColor(Color.rgb(20,22,17));gravity=Gravity.CENTER;background=GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,intArrayOf(Color.rgb(246,218,133),Color.rgb(179,143,56))).apply{cornerRadius=dp(12).toFloat()};setPadding(dp(8),0,dp(8),0);setOnClickListener{showAddWarehouseForm(query,filter)}},FrameLayout.LayoutParams(dp(108),dp(42)).apply{rightMargin=dp(8);topMargin=dp(20);gravity=Gravity.RIGHT})
        page.addView(banner)
        val rows=db.warehouseRows(); val materials=db.list("materials")
        var totalQty=0.0; rows.forEach{w->materials.forEach{m->totalQty+=db.warehouseBalance(m[0].toLongOrNull()?:return@forEach,w[0].toLong())}}
        val stats=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;background=rounded(Color.rgb(17,27,29),14);setPadding(dp(4),dp(5),dp(4),dp(5))}
        listOf(Triple("home","Складів",rows.size.toString()),Triple("cube","Номенклатура",materials.size.toString()),Triple("boxes","Кількість",formatQty(totalQty)),Triple("database","Вартість","—")).forEachIndexed{i,x->
            val b=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER};b.addView(DashboardIconView(this,x.first,Color.rgb(226,195,111)),LinearLayout.LayoutParams(-1,dp(23)));b.addView(TextView(this).apply{text=x.second;textSize=10f;setTextColor(Color.rgb(194,204,205));gravity=Gravity.CENTER;maxLines=1;ellipsize=android.text.TextUtils.TruncateAt.END});b.addView(TextView(this).apply{text=x.third;textSize=17f;setTypeface(null,Typeface.BOLD);setTextColor(Color.WHITE);gravity=Gravity.CENTER;maxLines=1;ellipsize=android.text.TextUtils.TruncateAt.END});stats.addView(b,LinearLayout.LayoutParams(0,dp(64),1f));if(i<3)stats.addView(View(this).apply{setBackgroundColor(Color.rgb(65,73,72))},LinearLayout.LayoutParams(dp(1),dp(42))) }
        page.addView(stats,LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(8);bottomMargin=dp(8)})
        val search=EditText(this).apply{hint="⌕  Пошук за номером, назвою або місцем";setText(query);textSize=14f;setSingleLine();setTextColor(Color.WHITE);setHintTextColor(Color.rgb(154,168,171));setPadding(dp(14),0,dp(10),0);background=GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,intArrayOf(Color.rgb(22,35,36),Color.rgb(14,25,27))).apply{cornerRadius=dp(13).toFloat();setStroke(dp(1),Color.rgb(55,75,72))}}
        page.addView(search,LinearLayout.LayoutParams(-1,dp(52)).apply{bottomMargin=dp(7)})
        val chips=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL}
        fun chip(t:String,sel:Boolean,act:()->Unit)=TextView(this).apply{text=t;textSize=10.5f;setTypeface(null,Typeface.BOLD);setTextColor(if(sel)Color.rgb(245,221,151)else Color.rgb(180,190,194));gravity=Gravity.CENTER;background=rounded(if(sel)Color.rgb(75,61,30)else Color.rgb(17,28,31),10);if(sel)(background as GradientDrawable).setStroke(dp(1),Color.rgb(180,145,61));setOnClickListener{act()}}
        chips.addView(chip("Всі (${rows.size})",filter=="ALL"){showWarehouses(query,"ALL")},LinearLayout.LayoutParams(0,dp(42),1f).apply{rightMargin=dp(4)})
        chips.addView(chip("●  Активні (${rows.count{it.getOrNull(8) != "0"}})",filter=="ACTIVE"){showWarehouses(query,"ACTIVE")},LinearLayout.LayoutParams(0,dp(42),1.15f).apply{rightMargin=dp(4)})
        chips.addView(chip("●  Неактивні (${rows.count{it.getOrNull(8) == "0"}})",filter=="INACTIVE"){showWarehouses(query,"INACTIVE")},LinearLayout.LayoutParams(0,dp(42),1.15f))
                page.addView(chips,LinearLayout.LayoutParams(-1,dp(42)).apply{bottomMargin=dp(6)})
        val sort=TextView(this).apply{text="⇅  За номером ⌄";textSize=10.5f;setTextColor(Color.rgb(180,190,194));gravity=Gravity.CENTER;background=rounded(Color.rgb(17,28,31),10);setOnClickListener{AlertDialog.Builder(this@MainActivity).setTitle("Сортування").setItems(arrayOf("За номером","За назвою")){_,which->showWarehouses(query,filter,which)}.show()}}
        page.addView(sort,LinearLayout.LayoutParams(dp(125),dp(38)).apply{gravity=Gravity.RIGHT;bottomMargin=dp(7)})
        val visible=rows.filter{(filter=="ALL" || (filter=="ACTIVE" && it.getOrNull(8)!="0") || (filter=="INACTIVE" && it.getOrNull(8)=="0")) && (query.isBlank() || it.any{v->v.contains(query.trim(),true)})}.let{if(sortMode==1)it.sortedBy{row->row[1].lowercase()}else it.sortedWith(compareBy({row->row.getOrNull(6)?.toIntOrNull()?:Int.MAX_VALUE},{row->row[0].toLongOrNull()?:0L}))}
        visible.groupBy { it[3].ifBlank { "Не призначено" } }.toSortedMap().forEach { (ownerName, ownerRows) ->
            val ownerHeader = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(12), dp(10), dp(12), dp(10))
                background = rounded(Color.rgb(34, 39, 32), 11)
            }
            ownerHeader.addView(TextView(this).apply {
                text = "♙  $ownerName"
                textSize = 15f
                setTypeface(null, Typeface.BOLD)
                setTextColor(Color.rgb(226, 195, 111))
            }, LinearLayout.LayoutParams(0, -2, 1f))
            ownerHeader.addView(TextView(this).apply {
                text = "${ownerRows.size} складів"
                textSize = 11f
                setTextColor(Color.rgb(180, 190, 194))
            })
            page.addView(ownerHeader, LinearLayout.LayoutParams(-1, -2).apply {
                topMargin = dp(8)
                bottomMargin = dp(6)
            })
            ownerRows.forEach{row->
            val id=row[0].toLong();val name=row[1];var qty=0.0;var pos=0;materials.forEach{m->val b=db.warehouseBalance(m[0].toLongOrNull()?:return@forEach,id);if(b!=0.0){pos++;qty+=b}}
            val card=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL;setPadding(dp(8),dp(8),dp(6),dp(8));background=GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,intArrayOf(Color.rgb(25,32,31),Color.rgb(11,21,23))).apply{cornerRadius=dp(15).toFloat();setStroke(dp(1),Color.rgb(47,60,59))};setOnClickListener{showWarehouseDetail(id)}}
            val thumb=FrameLayout(this).apply{background=rounded(Color.rgb(43,51,49),9);addView(WarehouseBannerView(this@MainActivity),FrameLayout.LayoutParams(-1,-1))};card.addView(thumb,LinearLayout.LayoutParams(dp(92),dp(92)))
            val mid=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(9),0,dp(3),0)};mid.addView(TextView(this).apply{text=name;textSize=16f;setTypeface(null,Typeface.BOLD);setTextColor(Color.WHITE);maxLines=1});mid.addView(TextView(this).apply{text=if(row.getOrNull(7).orEmpty().isNotBlank())row[7] else "Тип майна не вказано";textSize=12f;setTextColor(Color.rgb(172,184,187));maxLines=1;ellipsize=android.text.TextUtils.TruncateAt.END;setPadding(0,dp(3),0,0)});mid.addView(TextView(this).apply{text="⌖  "+(if(row[2].isNotBlank())row[2] else "Місце не вказано");textSize=10.5f;setTextColor(Color.rgb(164,176,179));setPadding(0,dp(3),0,0)});card.addView(mid,LinearLayout.LayoutParams(0,dp(92),1f))
            val posLabel=if(pos==0) "—" else pos.toString()
            val qtyLabel=if(qty==0.0) "—" else formatQty(qty)
            val right=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER_VERTICAL;minimumWidth=dp(88)}
            right.addView(TextView(this@MainActivity).apply{text=if(row.getOrNull(8)=="0")"●  Неактивний" else "●  Активний";textSize=9.5f;setTypeface(null,Typeface.BOLD);setTextColor(if(row.getOrNull(8)=="0")Color.rgb(174,184,188) else Color.rgb(23,220,151));gravity=Gravity.CENTER;background=rounded(if(row.getOrNull(8)=="0")Color.rgb(35,43,47) else Color.rgb(10,56,46),10);setPadding(dp(7),dp(5),dp(7),dp(5))})
            right.addView(TextView(this@MainActivity).apply{text="▦  Позиції  "+posLabel;textSize=10.5f;setTextColor(Color.rgb(188,197,199));setPadding(0,dp(6),0,0)})
            right.addView(TextView(this@MainActivity).apply{text="▦  Кількість  "+qtyLabel;textSize=10.5f;setTextColor(Color.rgb(188,197,199));setPadding(0,dp(3),0,0)})
            card.addView(right,LinearLayout.LayoutParams(dp(88),dp(92)))
            card.addView(TextView(this@MainActivity).apply{text="›";textSize=29f;setTextColor(Color.rgb(226,195,111));gravity=Gravity.CENTER},LinearLayout.LayoutParams(dp(18),dp(92)))
            page.addView(card,LinearLayout.LayoutParams(-1,dp(108)).apply{bottomMargin=dp(7)})
            }
        }
        if(visible.isEmpty())page.addView(TextView(this).apply{text="Складів не знайдено";textSize=16f;setTextColor(Color.LTGRAY);gravity=Gravity.CENTER;setPadding(0,dp(30),0,dp(30))})
        scroll.addView(page);root.addView(scroll,LinearLayout.LayoutParams(-1,0,1f))
        val nav=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER;background=rounded(Color.rgb(17,27,29),18);setPadding(dp(2),dp(2),dp(2),dp(2))}
        fun navItem(icon:String,label:String,active:Boolean,act:()->Unit)=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER;setOnClickListener{act()};addView(DashboardIconView(this@MainActivity,icon,if(active)Color.rgb(226,195,111)else Color.rgb(174,184,188)),LinearLayout.LayoutParams(dp(25),dp(27)));addView(TextView(this@MainActivity).apply{text=label;textSize=7.5f;gravity=Gravity.CENTER;maxLines=1;ellipsize=android.text.TextUtils.TruncateAt.END;setTextColor(if(active)Color.rgb(226,195,111)else Color.rgb(174,184,188))},LinearLayout.LayoutParams(-1,dp(16)))}
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
        val root = base("Місця зберігання")
        val rows = db.locationRows(warehouseId)
        addScreenSummary(root, "СКЛАД • ${warehouseName}", "Комірки та місця зберігання", "${rows.size} місць")
        addAction(root, "＋  Додати комірку") {
            formDialog("Нова комірка", listOf("Назва місця зберігання", "Примітка")) { v ->
                if (v[0].isBlank()) showError("Назва комірки не може бути порожньою.")
                else { db.insertLocation(warehouseId, v[0], v[1]); showLocations(warehouseId, warehouseName) }
            }
        }
        rows.forEach {
            val id = it[0].toLong()
            addRecordCard(root, it[1], it.getOrNull(2).orEmpty().ifBlank { "Без примітки" }, "КОМІРКА") {
                showLocationActions(warehouseId, warehouseName, id, it[1], it.getOrNull(2) ?: "")
            }
        }
        if (rows.isEmpty()) addEmptyState(root, "Місць зберігання ще немає", "Додайте стелаж, комірку або інше місце для обліку майна на цьому складі.")
        setContentView(root)
    }

    private fun showPersons(query: String = "") {
        val root = base("Матеріально відповідальні особи")
        val all = db.list("responsible_persons")
        val rows = all.filter { query.isBlank() || it.any { value -> value.contains(query, true) } }
        addScreenSummary(content, "ОБЛІКОВИЙ СКЛАД", "Відповідальні особи", "${all.size} осіб")
        addActionRow(content, listOf(
            "⌕  Пошук МВО" to { searchDialog("Пошук МВО", query) { q -> showPersons(q) } },
            "＋  Додати МВО" to {
                formDialog("Нова МВО", listOf("ПІБ", "Посада", "Телефон")) { v ->
                    if (v[0].isBlank()) showError("ПІБ не може бути порожнім.")
                    else { db.insertPerson(v[0], v[1], v[2]); showPersons(query) }
                }
            }
        ))
        rows.forEach {
            val id = it[0].toLong()
            val subtitle = listOf(it.getOrNull(2).orEmpty(), it.getOrNull(3).orEmpty())
                .filter { value -> value.isNotBlank() }
                .joinToString("  • ")
                .ifBlank { "Посаду та телефон не вказано" }
            addRecordCard(content, it[1], subtitle, "МВО") {
                showPersonActions(id, it[1], it.getOrNull(2) ?: "", it.getOrNull(3) ?: "")
            }
        }
        if (rows.isEmpty()) addEmptyState(content, if (query.isBlank()) "МВО ще немає" else "Нічого не знайдено", if (query.isBlank()) "Додайте відповідальну особу перед призначенням на склад." else "Спробуйте змінити пошуковий запит.")
        setContentView(root)
    }

    private fun showMaterials(query: String = "") {
        val root = base("Номенклатура")
        val all = db.list("materials")
        val rows = all.filter { row -> query.isBlank() || row.any { it.contains(query, true) } }
        val importedRows = db.initialStockRowsForAll("ALL", query)
        val importedGroups = importedRows.groupBy { row ->
            listOf(row.getOrElse(4) { "" }, row.getOrElse(5) { "" }, row.getOrElse(6) { "" },
                row.getOrElse(7) { "" }, row.getOrElse(8) { "" }, row.getOrElse(9) { "" }).joinToString("|")
        }.values.sortedBy { group -> group.firstOrNull()?.getOrElse(5) { "" }?.lowercase(Locale.ROOT) ?: "" }
        val totalBalance = all.sumOf { row -> db.materialBalance(row[0].toLongOrNull() ?: 0L) }
        val importedQty = importedRows.sumOf { it.getOrElse(10) { "0" }.toDoubleOrNull() ?: 0.0 }
        addScreenSummary(root, "ДОВІДНИК МАЙНА", "Номенклатурні позиції",
            "${all.size + importedGroups.size} позицій  •  Excel: ${formatQty(importedQty)} од.")
        addActionRow(root, listOf(
            "⌕  Пошук" to { searchDialog("Пошук номенклатури", query) { q -> showMaterials(q) } },
            "＋  Додати матеріал" to {
                formDialog("Новий матеріал", listOf("NSN", "Номенклатурний номер", "Назва", "Одиниця", "Партія", "Ціна")) { v ->
                    val price = v[5].replace(',', '.').toDoubleOrNull()
                    if (v[2].isBlank() || v[3].isBlank()) showError("Заповніть назву та одиницю виміру.")
                    else if (price == null || !price.isFinite() || price < 0) showError("Ціна має бути числом не менше 0.")
                    else { db.insertMaterial(v[0], v[1], v[2], v[3], v[4], price); showMaterials(query) }
                }
            }
        ))

        addScreenSummary(root, "ЗАЛИШКИ З EXCEL", "Номенклатура на складах",
            "${importedGroups.size} позицій • ${importedRows.map { it.getOrElse(1) { "" } }.distinct().size} складів")
        if (importedGroups.isEmpty()) {
            addRow(root, "Імпортовану номенклатуру не знайдено", "Перевірте пошуковий запит або імпорт Excel.")
        } else {
            importedGroups.forEach { group ->
                val first = group.first()
                val qty = group.sumOf { it.getOrElse(10) { "0" }.toDoubleOrNull() ?: 0.0 }
                val warehouseCount = group.map { it.getOrElse(1) { "" } }.distinct().size
                val title = first.getOrElse(5) { "" }.ifBlank { first.getOrElse(4) { "Без назви" } }
                val identifiers = listOf(first.getOrElse(4) { "" }, first.getOrElse(6) { "" })
                    .filter { it.isNotBlank() }.distinct().joinToString(" • ")
                val details = "${warehouseCount} складів" +
                    if (identifiers.isBlank()) "" else " • $identifiers"
                addNomenclatureCard(root, title, details, "${formatQty(qty)} ${first.getOrElse(9) { "" }}") {
                    showImportedNomenclatureDetail(group)
                }
            }
        }

        addScreenSummary(root, "РУЧНИЙ ДОВІДНИК", "Створені позиції", "${rows.size} позицій • ${formatQty(totalBalance)} од. за рухами")
        rows.forEach { row ->
            val id = row[0].toLongOrNull() ?: return@forEach
            val balance = db.materialBalance(id)
            addNomenclatureCard(root, row[3], "${row[4]}  •  NSN ${row[1].ifBlank { "—" }}", "${formatQty(balance)} ${row[4]}") {
                showMaterialActions(id, row)
            }
        }
        if (rows.isEmpty() && importedGroups.isEmpty()) addEmptyState(root,
            if (query.isBlank()) "Номенклатура порожня" else "Нічого не знайдено",
            if (query.isBlank()) "Після імпорту Excel або додавання матеріалів номенклатура з'явиться тут." else "Змініть пошуковий запит.")
        setContentView(root)
    }

    private fun showImportedNomenclatureDetail(group: List<Array<String>>) {
        if (group.isEmpty()) return
        val first = group.first()
        val title = first.getOrElse(5) { "" }.ifBlank { first.getOrElse(4) { "Номенклатура" } }
        val total = group.sumOf { it.getOrElse(10) { "0" }.toDoubleOrNull() ?: 0.0 }
        val unit = first.getOrElse(9) { "" }
        val warehouses = db.warehouseRows()
        val byWarehouse = group.groupBy { it.getOrElse(1) { "" } }.toSortedMap()
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(8), dp(18), dp(8))
            setBackgroundColor(Color.rgb(14, 25, 27))
        }
        fun line(text: String, emphasis: Boolean = false) {
            layout.addView(TextView(this).apply {
                this.text = text
                textSize = if (emphasis) 14f else 12f
                setTextColor(if (emphasis) Color.WHITE else Color.rgb(174, 187, 188))
                if (emphasis) setTypeface(null, Typeface.BOLD)
                setPadding(0, dp(7), 0, dp(7))
            })
        }
        line("Загальний залишок: ${formatQty(total)} $unit", true)
        line("Код: ${first.getOrElse(4) { "" }.ifBlank { "—" }} • NSN: ${first.getOrElse(6) { "" }.ifBlank { "—" }}")
        line("Розмір: ${first.getOrElse(7) { "" }.ifBlank { "—" }} • Партія: ${first.getOrElse(8) { "" }.ifBlank { "—" }}")
        line("ЗАЛИШКИ ЗА СКЛАДАМИ", true)
        var detailDialog: AlertDialog? = null
        byWarehouse.forEach { (code, entries) ->
            val qty = entries.sumOf { it.getOrElse(10) { "0" }.toDoubleOrNull() ?: 0.0 }
            val warehouse = warehouses.firstOrNull { it.getOrElse(6) { "" }.equals(code, true) }
            val warehouseName = warehouse?.getOrElse(1) { "" }?.ifBlank { "Склад $code" } ?: "Склад $code"
            val responsible = warehouse?.getOrElse(3) { "" }?.takeIf { it.isNotBlank() } ?: "МВО не вказано"
            val types = entries.groupBy { it.getOrElse(2) { "" } }
                .entries.sortedBy { it.key }
                .joinToString(" • ") { (type, values) ->
                    "$type: ${formatQty(values.sumOf { it.getOrElse(10) { "0" }.toDoubleOrNull() ?: 0.0 })}"
                }
            line("$warehouseName ($code)", true)
            line("МВО: $responsible")
            line("Кількість: ${formatQty(qty)} $unit")
            line("Тип зберігання: $types")
            val locations = entries.map { it.getOrElse(3) { "" } }.filter { it.isNotBlank() }.distinct()
            if (locations.isNotEmpty()) line("Місця: ${locations.joinToString(", ")}")
            val openStock = TextView(this).apply {
                text = if (warehouse == null) "Переглянути залишки за кодом →" else "Відкрити склад і залишки →"
                textSize = 12f
                setTypeface(null, Typeface.BOLD)
                setTextColor(Color.rgb(226, 195, 111))
                setPadding(0, dp(8), 0, dp(8))
                setOnClickListener {
                    detailDialog?.dismiss()
                    val id = warehouse?.getOrNull(0)?.toLongOrNull()
                    if (id != null) showWarehouseDetail(id, "Залишки")
                    else showAllInitialStock("ALL", "", code, "ALL")
                }
            }
            layout.addView(openStock)
            layout.addView(View(this).apply { setBackgroundColor(Color.rgb(43, 57, 54)) },
                LinearLayout.LayoutParams(-1, dp(1)).apply { topMargin = dp(4); bottomMargin = dp(4) })
        }
        detailDialog = AlertDialog.Builder(this)
            .setTitle(title)
            .setView(ScrollView(this).apply { addView(layout) })
            .setPositiveButton("Закрити", null)
            .create()
        detailDialog?.show()
    }

    private fun showMovement(type: String, title: String) {
        val root = base(title)
        val allRows = db.movementRows()
        val wanted = if (type == "TRANSFER") listOf("TRANSFER_OUT", "TRANSFER_IN") else listOf(type)
        val rows = allRows.filter { it[2] in wanted }
        addScreenSummary(root, "РУХ МАЙНА", title, "${rows.size} записів у журналі")
        addAction(root, "＋  Створити документ") {
            val mats = db.list("materials")
            val warehouses = db.warehouseRows().filter { it.getOrNull(8) != "0" }
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
        rows.forEach {
            val route = if (it[2].startsWith("TRANSFER")) " • ${it[5]} → ${it[6]}" else " • Склад: ${if (it[6] != "—") it[6] else it[5]}"
            val location = if (it[2].startsWith("TRANSFER")) " • Комірки: ${it[7]} → ${it[8]}" else if (it[2] == "RECEIPT") " • Комірка: ${it[8]}" else " • Комірка: ${it[7]}"
            addRow(root, "${typeLabel(it[2])}  ·  ${it[1]}", "${it[0]}  •  Кількість: ${formatQty(it[3].toDoubleOrNull() ?: 0.0)}  •  Документ: ${it[4]}${route}${location}")
        }
        if (rows.isEmpty()) addEmptyState(root, "Операцій ще немає", "Створіть перший документ руху майна. Реальні залишки не підставляються автоматично.")
        setContentView(root)
    }

    private fun selectSingleWarehouse(
        materialId: Long,
        type: String,
        title: String,
        warehouses: List<Array<String>>
    ) {
        val action = if (type == "RECEIPT") "Куди оприбуткувати" else "З якого складу списати"
        val names = warehouses.map { "${it[1]}${it.getOrNull(6)?.takeIf { n -> n.isNotBlank() }?.let { n -> " • №$n" } ?: ""}" }.toTypedArray()
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
        val names = warehouses.map { warehouse ->
            "${warehouse[1]}${warehouse.getOrNull(6)?.takeIf { it.isNotBlank() }?.let { " • №$it" } ?: ""}"
        }.toTypedArray()
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

            val saved = if (type == "RECEIPT") {
                safeDb { db.insertMovement(materialId, type, qty, null, fromWarehouse, null, toLocation, documentNo, date, v[3]) }
            } else if (type == "TRANSFER") {
                val targetWarehouse = toWarehouse
                if (targetWarehouse == null) {
                    showError("Не обрано склад-отримувач.")
                    return@formDialog
                }
                val current = if (fromLocation != null) db.locationBalance(materialId, fromLocation) else db.unassignedWarehouseBalance(materialId, fromWarehouse)
                if (qty > current) {
                    val scope = if (fromLocation != null) "комірці" else "складі-відправнику"
                    showError("Недостатньо залишку на $scope. Доступно: ${formatQty(current)}.")
                    return@formDialog
                }
                safeDb { db.insertTransfer(materialId, qty, fromWarehouse, targetWarehouse, fromLocation, toLocation, documentNo, date, v[3]) }
            } else {
                val current = if (fromLocation != null) db.locationBalance(materialId, fromLocation) else db.unassignedWarehouseBalance(materialId, fromWarehouse)
                if (qty > current) {
                    val scope = if (fromLocation != null) "комірці" else "складі"
                    showError("Недостатньо залишку на $scope. Доступно: ${formatQty(current)}.")
                    return@formDialog
                }
                safeDb { db.insertMovement(materialId, type, qty, fromWarehouse, null, fromLocation, null, documentNo, date, v[3]) }
            }
            if (saved) showMovement(type, title)
        }
    }

    private fun showReports() {
        val root = base("Звіти")
        val rows = db.initialStockRowsForAll()
        val warehouses = db.warehouseRows()
        val totalQty = rows.sumOf { it.getOrNull(10)?.replace(',', '.')?.toDoubleOrNull() ?: 0.0 }
        val totalValue = rows.sumOf {
            (it.getOrNull(10)?.replace(',', '.')?.toDoubleOrNull() ?: 0.0) *
                (it.getOrNull(11)?.replace(',', '.')?.toDoubleOrNull() ?: 0.0)
        }
        val hasPrices = rows.any { (it.getOrNull(11)?.replace(',', '.')?.toDoubleOrNull() ?: 0.0) > 0.0 }
        val positionCount = rows.map {
            listOf(it.getOrNull(4).orEmpty(), it.getOrNull(5).orEmpty(), it.getOrNull(6).orEmpty(),
                it.getOrNull(7).orEmpty(), it.getOrNull(8).orEmpty()).joinToString("|")
        }.distinct().size

        addScreenSummary(root, "АНАЛІТИКА", "Зведення залишків",
            "${rows.size} рядків • ${positionCount} позицій • ${formatQty(totalQty)} од.")
        addAction(root, "▦  Відкрити всі залишки") { showAllInitialStock() }
        addAction(root, "⌕  Знайти номенклатуру") { showMaterials() }
        addAction(root, "▤  Журнал руху") { showJournal() }

        addScreenSummary(root, "ВАРТІСТЬ", "Оцінка за Excel",
            if (hasPrices) String.format(Locale.US, "%.2f", totalValue) + " • за вказаними цінами"
            else "У вихідному Excel немає цін для достовірного розрахунку.")

        addScreenSummary(root, "ЗА ТИПОМ ЗБЕРІГАННЯ", "Структура залишків",
            "Кількість рядків та обсяг за типами")
        val typeGroups = rows.groupBy { it.getOrNull(2).orEmpty().ifBlank { "Не вказано" } }
            .map { (type, items) ->
                Triple(type, items.size, items.sumOf { it.getOrNull(10)?.replace(',', '.')?.toDoubleOrNull() ?: 0.0 })
            }.sortedBy { it.first }
        typeGroups.forEach { item ->
            addManageRow(root, item.first, "${item.second} рядків • ${formatQty(item.third)} од.") {
                showAllInitialStock()
            }
        }

        addScreenSummary(root, "РОЗПОДІЛ ПО СКЛАДАХ", "Найбільші залишки",
            "Підсумок згруповано за кодом складу з Excel")
        val warehouseGroups = rows.groupBy { it.getOrNull(1).orEmpty().ifBlank { "Без коду" } }
            .map { (code, items) ->
                val quantity = items.sumOf { it.getOrNull(10)?.replace(',', '.')?.toDoubleOrNull() ?: 0.0 }
                val positions = items.map {
                    listOf(it.getOrNull(4).orEmpty(), it.getOrNull(5).orEmpty(), it.getOrNull(6).orEmpty(),
                        it.getOrNull(7).orEmpty(), it.getOrNull(8).orEmpty()).joinToString("|")
                }.distinct().size
                val warehouse = warehouses.firstOrNull { it.getOrNull(6).orEmpty().equals(code, true) }
                val name = warehouse?.getOrNull(1)?.takeIf { it.isNotBlank() } ?: "Склад $code"
                val responsible = warehouse?.getOrNull(3)?.takeIf { it.isNotBlank() } ?: "МВО не вказано"
                arrayOf(code, name, responsible, items.size.toString(), positions.toString(), quantity.toString())
            }.sortedByDescending { it[5].toDoubleOrNull() ?: 0.0 }
        warehouseGroups.take(30).forEach { item ->
            addManageRow(root, "${item[1]} • ${item[0]}",
                "МВО: ${item[2]} • ${item[3]} рядків • ${item[4]} позицій • ${formatQty(item[5].toDoubleOrNull() ?: 0.0)} од.") {
                showAllInitialStock()
            }
        }
        if (rows.isEmpty()) addEmptyState(root, "Дані для звіту відсутні",
            "Спочатку імпортуйте файл «Залишки.XLSX». Звіт не створює та не вигадує складські дані.")
        setContentView(root)
    }

    private fun showCards() {
        val root = base("Картки обліку")
        val materials = db.list("materials")
        val warehouses = db.warehouseRows()
        val stockLines = materials.sumOf { material ->
            val materialId = material[0].toLongOrNull() ?: 0L
            warehouses.count { warehouse -> db.warehouseBalance(materialId, warehouse[0].toLongOrNull() ?: 0L) != 0.0 }
        }
        addScreenSummary(root, "КОНТРОЛЬ ТА ЗВІТНІСТЬ", "Картки обліку майна", "${materials.size} позицій  •  ${warehouses.size} складів  •  ${stockLines} складських залишків")
        materials.forEach { material ->
            val materialId = material[0].toLongOrNull() ?: return@forEach
            val balance = db.materialBalance(materialId)
            addAccountCard(root, material[3], "NSN ${material[1].ifBlank { "—" }}  •  ${material[4]}", balance) {
                val detail = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(8), dp(4), dp(8), dp(4)) }
                addRow(detail, "Картка майна", material[3])
                addRow(detail, "Одиниця виміру", material[4])
                addBalanceLine(detail, "Загальний залишок", balance, material[4], 0)
                warehouses.forEach { warehouse ->
                    val warehouseId = warehouse[0].toLongOrNull() ?: return@forEach
                    val warehouseBalance = db.warehouseBalance(materialId, warehouseId)
                    if (warehouseBalance != 0.0) addBalanceLine(detail, warehouse[1], warehouseBalance, material[4], 1)
                }
                AlertDialog.Builder(this).setTitle("Картка обліку").setView(detail).setPositiveButton("Готово", null).show()
            }
            warehouses.forEach { warehouse ->
                val warehouseId = warehouse[0].toLongOrNull() ?: return@forEach
                val warehouseBalance = db.warehouseBalance(materialId, warehouseId)
                if (warehouseBalance != 0.0) {
                    addBalanceLine(root, warehouse[1], warehouseBalance, material[4], 1)
                    val unassigned = db.unassignedWarehouseBalance(materialId, warehouseId)
                    if (unassigned != 0.0) addBalanceLine(root, "Без комірки", unassigned, material[4], 2)
                    db.locationRows(warehouseId).forEach { location ->
                        val locationId = location[0].toLongOrNull() ?: return@forEach
                        val locationBalance = db.locationBalance(materialId, locationId)
                        if (locationBalance != 0.0) addBalanceLine(root, location[1], locationBalance, material[4], 2)
                    }
                }
            }
        }
        if (materials.isEmpty()) addEmptyState(root, "Карток ще немає", "Додайте матеріали в Номенклатурі. Реальні складські дані не підставляються автоматично.")
        setContentView(root)
    }

    private fun showJournal(query: String = "", filter: String = "ALL") {
        val root = base("Журнал руху")
        val all = db.movementRows()
        val rows = all.filter { row ->
            val matchesType = when (filter) {
                "RECEIPT" -> row[2] == "RECEIPT"
                "ISSUE" -> row[2] == "ISSUE"
                "TRANSFER" -> row[2].startsWith("TRANSFER")
                "WRITE_OFF" -> row[2] == "WRITE_OFF"
                else -> true
            }
            matchesType && (query.isBlank() || row.any { value -> value.contains(query, true) })
        }
        val filterLabel = when (filter) {
            "RECEIPT" -> "Надходження"
            "ISSUE" -> "Видача"
            "TRANSFER" -> "Переміщення"
            "WRITE_OFF" -> "Списання"
            else -> "Усі операції"
        }
        addScreenSummary(root, "ІСТОРІЯ ОПЕРАЦІЙ", "Журнал руху майна", "${rows.size} записів • ${filterLabel}")
        addAction(root, "⌕  Пошук у журналі") { searchDialog("Пошук у журналі", query) { q -> showJournal(q, filter) } }
        val filterScroll = HorizontalScrollView(this).apply { isHorizontalScrollBarEnabled = false }
        val filterRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        listOf(
            "ALL" to "Усі",
            "RECEIPT" to "↓ Надходження",
            "ISSUE" to "↑ Видача",
            "TRANSFER" to "⇄ Переміщення",
            "WRITE_OFF" to "× Списання"
        ).forEach { option ->
            val selected = option.first == filter
            filterRow.addView(TextView(this).apply {
                text = option.second
                textSize = 11f
                setTypeface(null, Typeface.BOLD)
                gravity = Gravity.CENTER
                setTextColor(if (selected) Color.rgb(25, 25, 19) else Color.rgb(188, 198, 198))
                background = GradientDrawable(
                    GradientDrawable.Orientation.TOP_BOTTOM,
                    if (selected) intArrayOf(Color.rgb(246, 218, 133), Color.rgb(179, 143, 56))
                    else intArrayOf(Color.rgb(20, 32, 32), Color.rgb(12, 23, 25))
                ).apply {
                    cornerRadius = dp(10).toFloat()
                    setStroke(dp(1), if (selected) Color.rgb(238, 207, 122) else Color.rgb(43, 57, 54))
                }
                setPadding(dp(12), dp(10), dp(12), dp(10))
                setOnClickListener { showJournal(query, option.first) }
            }, LinearLayout.LayoutParams(-2, dp(38)).apply { rightMargin = dp(6) })
        }
        filterScroll.addView(filterRow)
        root.addView(filterScroll, LinearLayout.LayoutParams(-1, -2).apply {
            bottomMargin = dp(10)
        })
        rows.forEach {
            val route = if (it[2].startsWith("TRANSFER")) " • ${it[5]} → ${it[6]}" else if (it[2] == "RECEIPT") " • ${it[6]}" else " • ${it[5]}"
            addRow(root, "${typeLabel(it[2])}  ·  ${it[1]}", "${it[0]}  •  Кількість: ${formatQty(it[3].toDoubleOrNull() ?: 0.0)}  •  Документ: ${it[4]}${route}${if (it[9].isNotBlank()) " • ${it[9]}" else ""}")
        }
        if (rows.isEmpty()) addEmptyState(root, if (all.isEmpty()) "Журнал порожній" else "Нічого не знайдено", if (all.isEmpty()) "Документи руху з’являться після першої операції." else "Змініть пошук або виберіть інший тип операції.")
        setContentView(root)
    }

    private fun showWarehouseStockTab(warehouseId: Long, type: String, query: String) {
        val w = db.warehouseRows().firstOrNull { it[0].toLongOrNull() == warehouseId } ?: run { showWarehouses(); return }
        val code = w.getOrNull(6).orEmpty().ifBlank { w[1] }
        val rows = db.initialStockRows(code, type, query)
        val root = base("Залишки • " + code)
        addScreenSummary(root, "ІМПОРТОВАНІ ЗАЛИШКИ", "Тип зберігання: " + type, rows.size.toString() + " рядків")
        val filterScroll = HorizontalScrollView(this).apply { isHorizontalScrollBarEnabled = false }
        val filterRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        listOf(
            "ALL" to "Усі типи",
            "005" to "005 • Можна виписувати",
            "902" to "902 • Не розміщене",
            "922" to "922 • Заблоковано"
        ).forEach { option ->
            val selected = option.first == type
            filterRow.addView(TextView(this).apply {
                text = option.second
                textSize = 11f
                setTypeface(null, Typeface.BOLD)
                gravity = Gravity.CENTER
                setTextColor(if (selected) Color.rgb(25, 25, 19) else Color.rgb(188, 198, 198))
                background = GradientDrawable(
                    GradientDrawable.Orientation.TOP_BOTTOM,
                    if (selected) intArrayOf(Color.rgb(246, 218, 133), Color.rgb(179, 143, 56))
                    else intArrayOf(Color.rgb(20, 32, 32), Color.rgb(12, 23, 25))
                ).apply {
                    cornerRadius = dp(10).toFloat()
                    setStroke(dp(1), if (selected) Color.rgb(238, 207, 122) else Color.rgb(43, 57, 54))
                }
                setPadding(dp(12), dp(10), dp(12), dp(10))
                setOnClickListener { showWarehouseStockTab(warehouseId, option.first, query) }
            }, LinearLayout.LayoutParams(-2, dp(38)).apply { rightMargin = dp(6) })
        }
        filterScroll.addView(filterRow)
        root.addView(filterScroll, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(8) })
        addAction(root, "⌕  Пошук залишків") {
            searchDialog("Пошук залишків", query) { q -> showWarehouseStockTab(warehouseId, type, q) }
        }
        rows.take(500).forEachIndexed { i, r ->
            addManageRow(root, (i + 1).toString() + ". " + r[5], "Матеріал " + r[4] + " • NSN " + r[6] + " • Тип " + r[2] + " • Місце " + r[3] + " • Розмір " + r[7] + " • Партія " + r[8] + " • " + r[10] + " " + r[9] + " • Ціна " + r[11]) {
                val identity = (4..9).map { r.getOrElse(it) { "" } }
                val matching = db.initialStockRowsForAll("ALL", "").filter { candidate ->
                    (4..9).map { candidate.getOrElse(it) { "" } } == identity
                }
                if (matching.isNotEmpty()) showImportedNomenclatureDetail(matching)
                else showError("Деталізацію цієї позиції не знайдено.")
            }
        }
        if (rows.isEmpty()) addEmptyState(root, "Залишків немає", "Оберіть інший тип зберігання або пошуковий запит.")
        setContentView(root)
    }

    private fun showWarehouseDetail(warehouseId: Long, selectedTab: String = "Номенклатура") {
        val w = db.warehouseRows().firstOrNull { it[0].toLongOrNull() == warehouseId } ?: run { showWarehouses(); return }
        val title = w[1].ifBlank { w.getOrNull(6).orEmpty() }
        val type = w.getOrNull(7).orEmpty().ifBlank { "Тип майна не вказано" }
        val active = w.getOrNull(8) != "0"
        val gold=Color.rgb(226,195,111); val muted=Color.rgb(174,184,188)
        window.statusBarColor=Color.rgb(7,18,21);window.navigationBarColor=Color.rgb(7,18,21)
        val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setBackgroundColor(Color.rgb(7,18,21))}
        root.setOnApplyWindowInsetsListener{v,i->val b=i.getInsets(android.view.WindowInsets.Type.systemBars());v.setPadding(0,b.top,0,b.bottom);i}
        val scroll=ScrollView(this).apply{isFillViewport=true}
        val page=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(14),dp(5),dp(14),dp(10))}
        val hero=FrameLayout(this).apply{clipToOutline=true;background=rounded(Color.rgb(15,25,25),16)}
        hero.addView(WarehouseBannerView(this),FrameLayout.LayoutParams(-1,dp(116)))
        hero.addView(View(this).apply{background=GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,intArrayOf(Color.TRANSPARENT,Color.argb(225,7,18,21)))},FrameLayout.LayoutParams(-1,dp(116)))
        hero.addView(TextView(this).apply{text="←";textSize=30f;gravity=Gravity.CENTER;setTextColor(gold);setOnClickListener{showWarehouses()}},FrameLayout.LayoutParams(dp(45),dp(52)).apply{leftMargin=dp(2);topMargin=dp(8)})
        hero.addView(DashboardIconView(this,"home",gold),FrameLayout.LayoutParams(dp(46),dp(46)).apply{leftMargin=dp(50);topMargin=dp(18)})
        hero.addView(TextView(this).apply{text="Склад №"+title;textSize=19f;setTypeface(null,Typeface.BOLD);setTextColor(Color.WHITE);maxLines=1},FrameLayout.LayoutParams(-1,-2).apply{leftMargin=dp(102);rightMargin=dp(108);topMargin=dp(17)})
        hero.addView(TextView(this).apply{text=type;textSize=12f;setTextColor(Color.LTGRAY)},FrameLayout.LayoutParams(-2,-2).apply{leftMargin=dp(102);topMargin=dp(50)})
        hero.addView(TextView(this).apply{text=if(active)"● Активний" else "● Неактивний";textSize=10f;setTextColor(if(active)Color.rgb(23,220,151) else muted);background=rounded(if(active)Color.rgb(10,56,46) else Color.rgb(35,43,47),9);setPadding(dp(8),dp(4),dp(8),dp(4))},FrameLayout.LayoutParams(-2,-2).apply{leftMargin=dp(102);topMargin=dp(76)})
        hero.addView(TextView(this).apply{text="✎ Редагувати";textSize=10f;setTextColor(Color.WHITE);gravity=Gravity.CENTER;background=rounded(Color.rgb(65,54,29),9);setOnClickListener{showWarehouseActions(warehouseId,w[1],w[2],w[4].toLongOrNull(),w.getOrNull(5).orEmpty())}},FrameLayout.LayoutParams(dp(96),dp(38)).apply{rightMargin=dp(7);topMargin=dp(16);gravity=Gravity.RIGHT})
        page.addView(hero,LinearLayout.LayoutParams(-1,dp(116)).apply{bottomMargin=dp(8)})
        val info=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;background=rounded(Color.rgb(14,25,27),12);setPadding(dp(12),dp(8),dp(12),dp(8))}
        fun infoLine(label:String,value:String){info.addView(LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL;addView(TextView(this@MainActivity).apply{text=label;textSize=11f;setTextColor(muted)},LinearLayout.LayoutParams(dp(135),-2));addView(TextView(this@MainActivity).apply{text=value.ifBlank{"—"};textSize=12f;setTextColor(Color.WHITE)},LinearLayout.LayoutParams(0,-2,1f))},LinearLayout.LayoutParams(-1,dp(27)))}
        infoLine("Номер складу",w.getOrNull(6).orEmpty().ifBlank { title });infoLine("Тип майна",type);infoLine("Місце розташування",w[2]);infoLine("МВО",w[3]);infoLine("Примітка",w.getOrNull(5).orEmpty())
        page.addView(info,LinearLayout.LayoutParams(-1,-2).apply{bottomMargin=dp(8)})
        val materials=db.list("materials");var pos=0;var qty=0.0
        materials.forEach{m->val balance=db.warehouseBalance(m[0].toLongOrNull()?:return@forEach,warehouseId);if(balance!=0.0){pos++;qty+=balance}}
        val stockCode=w.getOrNull(6).orEmpty().ifBlank{title}
        val importedStock=db.initialStockRows(stockCode)
        val importedQty=importedStock.sumOf{it.getOrNull(10)?.toDoubleOrNull()?:0.0}
        val importedValue=importedStock.sumOf{(it.getOrNull(10)?.toDoubleOrNull()?:0.0)*(it.getOrNull(11)?.toDoubleOrNull()?:0.0)}
        val stats=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL}
        listOf(Triple("cube","Всього позицій",(pos+importedStock.size).toString()),Triple("boxes","Загальна кількість",formatQty(qty+importedQty)),Triple("database","Загальна вартість",if(importedStock.isEmpty())"—" else String.format(Locale.US,"%.2f",importedValue))).forEachIndexed{index,item->
            val box=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER;background=rounded(Color.rgb(16,28,30),10);setPadding(dp(3),dp(7),dp(3),dp(7))}
            box.addView(DashboardIconView(this,item.first,if(index==0)gold else Color.LTGRAY),LinearLayout.LayoutParams(dp(24),dp(24)))
            box.addView(TextView(this).apply{text=item.second;textSize=9f;gravity=Gravity.CENTER;setTextColor(muted)})
            box.addView(TextView(this).apply{
                text=when(index){
                    1 -> java.text.NumberFormat.getNumberInstance(Locale("uk","UA")).format(qty+importedQty)
                    2 -> if(importedStock.isEmpty()) "—" else java.text.NumberFormat.getNumberInstance(Locale.US).apply{minimumFractionDigits=2;maximumFractionDigits=2}.format(importedValue)
                    else -> item.third
                }
                textSize=when { text.length>12 -> 10f; text.length>8 -> 12f; else -> 16f }
                setTypeface(null,Typeface.BOLD);gravity=Gravity.CENTER;setTextColor(Color.WHITE);maxLines=1;includeFontPadding=false
                setPadding(dp(1),dp(2),dp(1),0)
            })
            stats.addView(box,LinearLayout.LayoutParams(0,dp(78),1f).apply{if(index<2)rightMargin=dp(5)})
        }
        page.addView(stats,LinearLayout.LayoutParams(-1,-2).apply{bottomMargin=dp(8)})
        val tabs=listOf("Номенклатура","Залишки","Рух майна","Документи","Інформація")
        val tabRow=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL}
        tabs.forEach{tab->tabRow.addView(TextView(this).apply{
            text=tab;textSize=9.5f;maxLines=2;ellipsize=android.text.TextUtils.TruncateAt.END;gravity=Gravity.CENTER
            setTypeface(null,if(tab==selectedTab)Typeface.BOLD else Typeface.NORMAL)
            setTextColor(if(tab==selectedTab)gold else muted)
            background=GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                if(tab==selectedTab) intArrayOf(Color.rgb(63,51,27),Color.rgb(43,36,23)) else intArrayOf(Color.rgb(19,31,33),Color.rgb(13,23,25))
            ).apply{cornerRadius=dp(9).toFloat();setStroke(dp(1),if(tab==selectedTab)Color.rgb(126,101,48) else Color.rgb(31,45,45))}
            setPadding(dp(2),dp(5),dp(2),dp(5));setOnClickListener{showWarehouseDetail(warehouseId,tab)}
        },LinearLayout.LayoutParams(0,dp(42),1f).apply{if(tab!=tabs.last())rightMargin=dp(3)})}
        page.addView(tabRow,LinearLayout.LayoutParams(-1,-2).apply{bottomMargin=dp(7)})
        if(selectedTab=="Залишки" && db.initialStockCount()>0){
            val stockRows=db.initialStockRows(w.getOrNull(6).orEmpty().ifBlank{title})
            val filters=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL}
            listOf("ALL","005","902","922").forEach{t->
                filters.addView(TextView(this).apply{text=if(t=="ALL")"Усі" else t;textSize=10f;gravity=Gravity.CENTER;setTextColor(gold);background=rounded(Color.rgb(63,51,27),8);setOnClickListener{showWarehouseStockTab(warehouseId,t,"")}},LinearLayout.LayoutParams(0,dp(36),1f).apply{rightMargin=dp(3)})
            }
            page.addView(TextView(this).apply{text="Імпортовані залишки: "+stockRows.size+" рядків";textSize=12f;setTextColor(muted);setPadding(dp(2),dp(6),dp(2),dp(6))})
            page.addView(filters,LinearLayout.LayoutParams(-1,-2).apply{bottomMargin=dp(6)})
            stockRows.take(500).forEachIndexed{index,r->
                val card=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;background=rounded(Color.rgb(14,25,27),9);setPadding(dp(10),dp(9),dp(10),dp(9))}
                card.addView(TextView(this).apply{text=(index+1).toString()+". "+r[5];textSize=12f;setTypeface(null,Typeface.BOLD);setTextColor(Color.WHITE)})
                card.addView(TextView(this).apply{text="Матеріал: "+r[4]+" • NSN: "+r[6];textSize=10f;setTextColor(muted);setPadding(0,dp(4),0,0)})
                card.addView(TextView(this).apply{text="Тип: "+r[2]+" • Місце: "+r[3]+" • Розмір: "+r[7];textSize=10f;setTextColor(gold);setPadding(0,dp(3),0,0)})
                card.addView(TextView(this).apply{text="Партія: "+r[8]+" • Залишок: "+r[10]+" "+r[9]+" • Ціна: "+r[11];textSize=10f;setTextColor(muted);setPadding(0,dp(3),0,0)})
                page.addView(card,LinearLayout.LayoutParams(-1,-2).apply{bottomMargin=dp(5)})
            }
        }else if(selectedTab=="Номенклатура"||selectedTab=="Залишки"){
            val action=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL}
            action.addView(TextView(this).apply{text=if(selectedTab=="Номенклатура")"Номенклатура на складі" else "Фактичні залишки";textSize=12f;setTextColor(muted)},LinearLayout.LayoutParams(0,dp(40),1f))
            action.addView(TextView(this).apply{text="+ Додати";textSize=11f;setTextColor(Color.rgb(20,22,17));gravity=Gravity.CENTER;background=rounded(gold,8);setOnClickListener{showMovement("RECEIPT","Надходження")}},LinearLayout.LayoutParams(dp(88),dp(36)))
            page.addView(action)
            val items=materials.mapNotNull{m->val balance=db.warehouseBalance(m[0].toLongOrNull()?:return@mapNotNull null,warehouseId);if(balance==0.0)null else Pair(m,balance)}
            val warehouseCode=w.getOrNull(6).orEmpty().ifBlank{w[1]}
            val importedRows=db.initialStockRows(warehouseCode,"ALL","")
            if(importedRows.isNotEmpty()){
                page.addView(TextView(this).apply{text="ПОЧАТКОВІ ЗАЛИШКИ З EXCEL  •  ${importedRows.size} рядків";textSize=11f;setTypeface(null,Typeface.BOLD);setTextColor(gold);setPadding(dp(2),dp(10),dp(2),dp(6))})
                importedRows.take(500).forEachIndexed{index,r->
                    val card=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;background=rounded(Color.rgb(14,25,27),9);setPadding(dp(10),dp(9),dp(10),dp(9))}
                    card.addView(TextView(this).apply{text=(index+1).toString()+". "+r[5].ifBlank{"Найменування не вказано"};textSize=12f;setTypeface(null,Typeface.BOLD);setTextColor(Color.WHITE)})
                    card.addView(TextView(this).apply{text="Матеріал: "+r[4]+" • NSN: "+r[6];textSize=10f;setTextColor(muted);setPadding(0,dp(4),0,0)})
                    card.addView(TextView(this).apply{text="Тип: "+r[2]+" • Місце: "+r[3]+" • Розмір: "+r[7];textSize=10f;setTextColor(gold);setPadding(0,dp(3),0,0)})
                    card.addView(TextView(this).apply{text="Партія: "+r[8]+" • Залишок: "+r[10]+" "+r[9]+" • Ціна: "+r[11];textSize=10f;setTextColor(muted);setPadding(0,dp(3),0,0)})
                    page.addView(card,LinearLayout.LayoutParams(-1,-2).apply{bottomMargin=dp(5)})
                }
                if(importedRows.size>500)page.addView(TextView(this).apply{text="Показано перші 500 рядків. Повний список доступний у вкладці «Залишки».";textSize=10f;setTextColor(muted);setPadding(dp(4),dp(4),dp(4),dp(8))})
            }
            if(items.isNotEmpty()){
                page.addView(TextView(this).apply{text="ПРОВЕДЕНІ ОПЕРАЦІЇ";textSize=11f;setTypeface(null,Typeface.BOLD);setTextColor(gold);setPadding(dp(2),dp(10),dp(2),dp(6))})
                items.forEachIndexed{index,item->page.addView(TextView(this).apply{text=(index+1).toString()+".  "+item.first[3]+"     "+formatQty(item.second)+" "+item.first[4];textSize=12f;setTextColor(Color.WHITE);setPadding(dp(10),dp(12),dp(10),dp(12));background=rounded(Color.rgb(14,25,27),8)},LinearLayout.LayoutParams(-1,-2).apply{bottomMargin=dp(3)})}
            }
            if(importedRows.isEmpty()&&items.isEmpty())page.addView(TextView(this).apply{text="У цьому складі поки немає імпортованих або проведених залишків.";textSize=13f;gravity=Gravity.CENTER;textAlignment=View.TEXT_ALIGNMENT_CENTER;setTextColor(muted);setPadding(dp(12),dp(30),dp(12),dp(30));background=rounded(Color.rgb(14,25,27),12)},LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(5)})
        }else if(selectedTab=="Рух майна"){
            val rows=db.movementRows().filter{it.getOrNull(5)==w[1]||it.getOrNull(6)==w[1]}
            if(rows.isEmpty())page.addView(TextView(this).apply{text="Документів руху поки немає.";textSize=13f;gravity=Gravity.CENTER;setTextColor(muted);setPadding(0,dp(28),0,dp(28))})
            else rows.forEach{r->page.addView(TextView(this).apply{text=r[0]+" · "+r[1]+" · "+r[3]+" · "+r[5]+" → "+r[6];textSize=11f;setTextColor(Color.WHITE);setPadding(dp(8),dp(10),dp(8),dp(10));background=rounded(Color.rgb(14,25,27),8)},LinearLayout.LayoutParams(-1,-2).apply{bottomMargin=dp(3)})}
        }else page.addView(TextView(this).apply{text=if(selectedTab=="Документи")"Документи з'являться після проведення операцій." else "Інформація взята з картки складу.";textSize=13f;setTextColor(muted);setPadding(dp(8),dp(22),dp(8),dp(22))})
        scroll.addView(page);root.addView(scroll,LinearLayout.LayoutParams(-1,0,1f))
        val nav=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER;background=rounded(Color.rgb(17,27,29),18)}
        fun navItem(icon:String,label:String,activeItem:Boolean,action:()->Unit)=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER;setOnClickListener{action()};addView(DashboardIconView(this@MainActivity,icon,if(activeItem)gold else muted),LinearLayout.LayoutParams(dp(24),dp(25)));addView(TextView(this@MainActivity).apply{text=label;textSize=8f;gravity=Gravity.CENTER;maxLines=1;ellipsize=android.text.TextUtils.TruncateAt.END;setTextColor(if(activeItem)gold else muted)},LinearLayout.LayoutParams(-1,dp(16)))}
        nav.addView(navItem("home","Головна",false){showHome()},LinearLayout.LayoutParams(0,dp(56),1f));nav.addView(navItem("home","Склади",true){showWarehouses()},LinearLayout.LayoutParams(0,dp(56),1f));nav.addView(navItem("cube","Номенкл.",false){showMaterials()},LinearLayout.LayoutParams(0,dp(56),1f));nav.addView(navItem("transfer","Рух майна",false){showMovement("TRANSFER","Переміщення")},LinearLayout.LayoutParams(0,dp(56),1f));nav.addView(navItem("chart","Звіти",false){showReports()},LinearLayout.LayoutParams(0,dp(56),1f));nav.addView(navItem("settings","Налаштув.",false){showBackupMenu()},LinearLayout.LayoutParams(0,dp(56),1f))
        root.addView(nav,LinearLayout.LayoutParams(-1,dp(60)).apply{bottomMargin=dp(2)})
        setContentView(root)
    }

    private fun showWarehouseActions(id: Long, name: String, address: String, responsibleId: Long?, note: String) {
        AlertDialog.Builder(this).setTitle(name)
            .setItems(arrayOf("Редагувати", "Комірки та місця зберігання", "Видалити")) { _, which ->
                when (which) {
                    0 -> editWarehouse(id, name, address, responsibleId, note)
                    1 -> showLocations(id, name)
                    else -> {
                        if (db.warehouseHasMovements(id)) showError("Склад має документи руху і не може бути видалений.")
                        else if (db.warehouseHasLocations(id)) showError("Спочатку видаліть комірки цього складу.")
                        else { db.deleteWarehouse(id); showWarehouses() }
                    }
                }
            }.show()
    }

    private fun editWarehouse(id: Long, name: String, address: String, responsibleId: Long?, note: String) {
        val row = db.warehouseRows().firstOrNull { it[0].toLongOrNull() == id }
        val number = row?.getOrNull(6).orEmpty()
        val propertyType = row?.getOrNull(7).orEmpty()
        val active = row?.getOrNull(8) != "0"
        val persons = db.list("responsible_persons")
        val labels = mutableListOf("Не призначено")
        labels += persons.map { it[1] }
        val current = persons.indexOfFirst { it[0].toLongOrNull() == responsibleId } + 1
        AlertDialog.Builder(this).setTitle("Матеріально відповідальна особа")
            .setItems(labels.toTypedArray()) { _, selected ->
                val rid = if (selected == 0) null else persons[selected - 1][0].toLongOrNull()
                formDialog(
                    "Редагувати склад",
                    listOf("Назва складу", "Номер складу", "Тип майна", "Місце розташування", "Примітка", "Активність: так / ні"),
                    initialValues = listOf(name, number, propertyType, address, note, if (active) "так" else "ні")
                ) { v ->
                    if (v[0].isBlank()) showError("Назва складу не може бути порожньою.")
                    else if (v[1].isBlank()) showError("Номер складу не може бути порожнім.")
                    else if (v[5].lowercase() !in listOf("так", "ні", "yes", "no", "1", "0")) showError("Активність вкажіть як «так» або «ні».")
                    else {
                        val enabled = v[5].lowercase() in listOf("так", "yes", "1")
                        db.updateWarehouse(id, v[0], v[3], v[4], rid, v[1], v[2], enabled)
                        showWarehouseDetail(id)
                    }
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


    private fun showAllInitialStock(
        type: String = "ALL",
        query: String = "",
        warehouseCode: String = "ALL",
        mvo: String = "ALL"
    ) {
        val sourceRows = db.initialStockRowsForAll(type, query)
        val warehouseRows = db.warehouseRows()
        val warehouseByCode = warehouseRows.associateBy { it.getOrNull(6).orEmpty().uppercase(Locale.ROOT) }
        val all = sourceRows.filter { row ->
            val code = row.getOrNull(1).orEmpty().uppercase(Locale.ROOT)
            val warehouseMatches = warehouseCode == "ALL" || code == warehouseCode.uppercase(Locale.ROOT)
            val responsible = warehouseByCode[code]?.getOrNull(3).orEmpty()
            warehouseMatches && (mvo == "ALL" || responsible.equals(mvo, true))
        }
        val root = base("Залишки")
        val qty = all.sumOf { it.getOrNull(10)?.replace(',', '.')?.toDoubleOrNull() ?: 0.0 }
        val value = all.sumOf {
            (it.getOrNull(10)?.replace(',', '.')?.toDoubleOrNull() ?: 0.0) *
                (it.getOrNull(11)?.replace(',', '.')?.toDoubleOrNull() ?: 0.0)
        }
        val warehouseLabel = if (warehouseCode == "ALL") "Усі склади" else {
            warehouseByCode[warehouseCode.uppercase(Locale.ROOT)]?.getOrNull(1)?.let { "$it • $warehouseCode" } ?: warehouseCode
        }
        val mvoLabel = if (mvo == "ALL") "Усі МВО" else mvo
        addScreenSummary(root, "ЗАЛИШКИ", "Імпортоване майно",
            "Рядків: ${all.size} • ${warehouseLabel} • ${mvoLabel} • Кількість: ${formatQty(qty)} • Вартість: ${String.format(Locale.US, "%.2f", value)}")

        val filterHeader = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        filterHeader.addView(TextView(this).apply {
            text = "ТИП ЗБЕРІГАННЯ"
            textSize = 10f
            setTypeface(null, Typeface.BOLD)
            letterSpacing = 0.08f
            setTextColor(Color.rgb(146, 165, 161))
        }, LinearLayout.LayoutParams(0, dp(24), 1f))
        filterHeader.addView(TextView(this).apply {
            text = "${all.size} ${if (all.size == 1) "позиція" else if (all.size in 2..4) "позиції" else "позицій"}"
            textSize = 10f
            setTextColor(Color.rgb(226, 195, 111))
            background = rounded(Color.rgb(43, 39, 27), 8)
            setPadding(dp(8), dp(4), dp(8), dp(4))
        })
        root.addView(filterHeader, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(5) })
        val typeFilterScroll = HorizontalScrollView(this).apply { isHorizontalScrollBarEnabled = false }
        val typeFilterRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        listOf("ALL", "005", "902", "922").forEach { t ->
            val selected = t == type
            val label = if (t == "ALL") "Усі типи" else "Тип $t"
            typeFilterRow.addView(TextView(this).apply {
                text = label
                textSize = 11f
                setTypeface(null, if (selected) Typeface.BOLD else Typeface.NORMAL)
                gravity = Gravity.CENTER
                setTextColor(if (selected) Color.rgb(25, 25, 19) else Color.rgb(188, 198, 198))
                background = GradientDrawable(
                    GradientDrawable.Orientation.TOP_BOTTOM,
                    if (selected) intArrayOf(Color.rgb(246, 218, 133), Color.rgb(179, 143, 56))
                    else intArrayOf(Color.rgb(20, 32, 32), Color.rgb(12, 23, 25))
                ).apply {
                    cornerRadius = dp(10).toFloat()
                    setStroke(dp(1), if (selected) Color.rgb(238, 207, 122) else Color.rgb(43, 57, 54))
                }
                setPadding(dp(13), dp(8), dp(13), dp(8))
                setOnClickListener { showAllInitialStock(t, query, warehouseCode, mvo) }
            }, LinearLayout.LayoutParams(-2, dp(38)).apply { rightMargin = dp(6) })
        }
        typeFilterScroll.addView(typeFilterRow)
        root.addView(typeFilterScroll, LinearLayout.LayoutParams(-1, dp(40)).apply {
            bottomMargin = dp(8)
        })
        val codes = sourceRows.map { it.getOrNull(1).orEmpty() }.filter { it.isNotBlank() }.distinct().sorted()
        val warehouseOptions = listOf("Усі склади") + codes.map { code ->
            val name = warehouseByCode[code.uppercase(Locale.ROOT)]?.getOrNull(1).orEmpty()
            if (name.isBlank()) code else "$name • $code"
        }
        val openWarehouseFilter = {
            AlertDialog.Builder(this).setTitle("Фільтр за складом")
                .setItems(warehouseOptions.toTypedArray()) { _, which ->
                    val selected = if (which == 0) "ALL" else codes[which - 1]
                    showAllInitialStock(type, query, selected, mvo)
                }.setNegativeButton("Скасувати", null).show()
        }
        val mvoNames = codes.mapNotNull { code ->
            warehouseByCode[code.uppercase(Locale.ROOT)]?.getOrNull(3)?.takeIf { it.isNotBlank() && it != "Не призначено" }
        }.distinct().sorted()
        val mvoOptions = listOf("Усі МВО") + mvoNames
        val openMvoFilter = {
            AlertDialog.Builder(this).setTitle("Фільтр за МВО")
                .setItems(mvoOptions.toTypedArray()) { _, which ->
                    val selected = if (which == 0) "ALL" else mvoNames[which - 1]
                    showAllInitialStock(type, query, warehouseCode, selected)
                }.setNegativeButton("Скасувати", null).show()
        }
        addActionRow(root, listOf(
            "Склад: $warehouseLabel" to { openWarehouseFilter() },
            "МВО: $mvoLabel" to { openMvoFilter() }
        ))
        val searchAction: () -> Unit = {
            searchDialog("Пошук залишків", query) { q -> showAllInitialStock(type, q.trim(), warehouseCode, mvo) }
        }
        if (type != "ALL" || warehouseCode != "ALL" || mvo != "ALL" || query.isNotBlank()) {
            addActionRow(root, listOf(
                "⌕  ${query.takeIf { it.isNotBlank() }?.let { "Пошук: ${it.take(22)}${if (it.length > 22) "…" else ""}" } ?: "Пошук залишків"}" to { searchAction() },
                "↺  Скинути фільтри" to { showAllInitialStock() }
            ))
        } else {
            addActionRow(root, listOf("⌕  Пошук залишків" to { searchAction() }))
        }
        if (all.isEmpty()) addEmptyState(root, "Залишки не знайдено",
            "Змініть склад, МВО, тип зберігання або пошуковий запит.")
        all.take(1000).forEachIndexed { i, r ->
            val responsible = warehouseByCode[r.getOrNull(1).orEmpty().uppercase(Locale.ROOT)]?.getOrNull(3).orEmpty()
            val quantity = r.getOrNull(10).orEmpty().ifBlank { "—" }
            val unit = r.getOrNull(9).orEmpty()
            val price = r.getOrNull(11).orEmpty().ifBlank { "—" }
            val item = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(13), dp(12), dp(13), dp(12))
                background = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                    intArrayOf(Color.rgb(23, 36, 36), Color.rgb(13, 25, 27))).apply {
                    cornerRadius = dp(14).toFloat()
                    setStroke(dp(1), Color.rgb(48, 65, 62))
                }
            }
            val titleRow = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }
            titleRow.addView(TextView(this).apply {
                text = String.format(Locale.ROOT, "%03d", i + 1)
                textSize = 10f
                setTypeface(null, Typeface.BOLD)
                setTextColor(Color.rgb(224, 194, 119))
                gravity = Gravity.CENTER
                background = rounded(Color.rgb(48, 42, 28), 7)
                setPadding(dp(7), dp(5), dp(7), dp(5))
            }, LinearLayout.LayoutParams(-2, -2).apply { rightMargin = dp(9) })
            titleRow.addView(TextView(this).apply {
                text = "${r.getOrNull(4).orEmpty()} • ${r.getOrNull(6).orEmpty()}"
                textSize = 14f
                setTypeface(null, Typeface.BOLD)
                setTextColor(Color.WHITE)
                maxLines = 2
                ellipsize = android.text.TextUtils.TruncateAt.END
            }, LinearLayout.LayoutParams(0, -2, 1f))
            item.addView(titleRow)
            val locationMeta = TextView(this).apply {
                text = "СКЛАД  ${r.getOrNull(1).orEmpty()}  •  ТИП  ${r.getOrNull(2).orEmpty()}  •  МІСЦЕ  ${r.getOrNull(3).orEmpty()}"
                textSize = 10.5f
                setTypeface(null, Typeface.BOLD)
                setTextColor(Color.rgb(174, 190, 191))
                maxLines = 2
                ellipsize = android.text.TextUtils.TruncateAt.END
                setPadding(0, dp(8), 0, dp(4))
                letterSpacing = 0.01f
            }
            item.addView(locationMeta)
            item.addView(TextView(this).apply {
                text = "МВО: ${responsible.ifBlank { "Не вказано" }}  •  Розмір ${r.getOrNull(7).orEmpty()}  •  Партія ${r.getOrNull(8).orEmpty()}"
                textSize = 11f
                setTextColor(Color.rgb(154, 171, 173))
                maxLines = 2
                ellipsize = android.text.TextUtils.TruncateAt.END
            })
            val values = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, dp(11), 0, 0)
            }
            val qtyBadge = TextView(this).apply {
                text = "КІЛЬКІСТЬ\n$quantity $unit".trim()
                textSize = 12f
                setTypeface(null, Typeface.BOLD)
                setTextColor(Color.rgb(185, 239, 215))
                gravity = Gravity.CENTER
                setPadding(dp(8), dp(8), dp(8), dp(8))
                background = rounded(Color.rgb(18, 62, 49), 10)
                minHeight = dp(48)
                maxLines = 2
                setLineSpacing(dp(2).toFloat(), 1f)
            }
            values.addView(qtyBadge, LinearLayout.LayoutParams(0, -2, 1f).apply { rightMargin = dp(7) })
            val priceBadge = TextView(this).apply {
                text = "ЦІНА\n$price"
                textSize = 12f
                setTypeface(null, Typeface.BOLD)
                setTextColor(Color.rgb(247, 221, 157))
                gravity = Gravity.CENTER
                setPadding(dp(8), dp(8), dp(8), dp(8))
                background = rounded(Color.rgb(66, 54, 28), 10)
                minHeight = dp(48)
                maxLines = 2
                setLineSpacing(dp(2).toFloat(), 1f)
            }
            values.addView(priceBadge, LinearLayout.LayoutParams(0, -2, 1f))
            item.addView(values)
            root.addView(item, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(8) })
        }
        setContentView(root)
    }

    private val STOCK_OPEN = 4103

    private fun importInitialStock() {
        AlertDialog.Builder(this).setTitle("Імпорт залишків Excel")
            .setMessage("Оберіть файл .xlsx з аркушем «Залишки». Повторний імпорт замінить попередній імпортований список, не змінюючи документи руху.")
            .setNegativeButton("Скасувати", null)
            .setPositiveButton("Обрати файл") { _, _ ->
                startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                    addCategory(Intent.CATEGORY_OPENABLE)
                }, STOCK_OPEN)
            }.show()
    }

    private fun parseInitialStock(uri: Uri): List<Array<String>> {
        val temp = File.createTempFile("stock-import-", ".xlsx", cacheDir)
        try {
            contentResolver.openInputStream(uri)?.use { input ->
                temp.outputStream().use { output -> input.copyTo(output) }
            } ?: throw IllegalArgumentException("Не вдалося прочитати файл.")
            ZipFile(temp).use { zip ->
                val strings = mutableListOf<String>()
                zip.getEntry("xl/sharedStrings.xml")?.let { entry ->
                    val parser = XmlPullParserFactory.newInstance().newPullParser()
                    parser.setInput(zip.getInputStream(entry), "UTF-8")
                    var current: StringBuilder? = null
                    var event = parser.eventType
                    while (event != org.xmlpull.v1.XmlPullParser.END_DOCUMENT) {
                        if (event == org.xmlpull.v1.XmlPullParser.START_TAG && parser.name == "si") current = StringBuilder()
                        else if (event == org.xmlpull.v1.XmlPullParser.TEXT && current != null) current!!.append(parser.text)
                        else if (event == org.xmlpull.v1.XmlPullParser.END_TAG && parser.name == "si") {
                            strings.add(current?.toString().orEmpty())
                            current = null
                        }
                        event = parser.next()
                    }
                }
                val sheet = zip.getEntry("xl/worksheets/sheet1.xml")
                    ?: throw IllegalArgumentException("Не знайдено аркуш Excel.")
                val parser = XmlPullParserFactory.newInstance().newPullParser()
                parser.setInput(zip.getInputStream(sheet), "UTF-8")
                val imported = mutableListOf<Array<String>>()
                var cells = mutableMapOf<Int, String>()
                var column = 0
                var cellType = ""
                var value = StringBuilder()
                var readingValue = false
                fun columnIndex(ref: String): Int {
                    var result = 0
                    for (ch in ref.takeWhile { it.isLetter() }.uppercase()) result = result * 26 + (ch - 'A' + 1)
                    return result - 1
                }
                var event = parser.eventType
                while (event != org.xmlpull.v1.XmlPullParser.END_DOCUMENT) {
                    if (event == org.xmlpull.v1.XmlPullParser.START_TAG) {
                        when (parser.name) {
                            "row" -> cells = mutableMapOf()
                            "c" -> {
                                column = columnIndex(parser.getAttributeValue(null, "r") ?: "A1")
                                cellType = parser.getAttributeValue(null, "t") ?: ""
                                value = StringBuilder()
                            }
                            "v", "t" -> readingValue = true
                        }
                    } else if (event == org.xmlpull.v1.XmlPullParser.TEXT && readingValue) {
                        value.append(parser.text)
                    } else if (event == org.xmlpull.v1.XmlPullParser.END_TAG) {
                        when (parser.name) {
                            "v", "t" -> readingValue = false
                            "c" -> {
                                val raw = value.toString()
                                cells[column] = if (cellType == "s") strings.getOrNull(raw.toIntOrNull() ?: -1).orEmpty() else raw
                            }
                            "row" -> {
                                val first = cells[0].orEmpty().trim()
                                if (first.isNotBlank() && !first.contains("склад", true) && !first.contains("warehouse", true) && cells.size >= 10) {
                                    val values = (0..10).map { cells[it].orEmpty().trim() }
                                    val warehouseCode = values[0]
                                    fun normCode(code: String): String = code.trim().uppercase(Locale.ROOT)
                                        .replace('А', 'A').replace('В', 'B').replace('Е', 'E').replace('К', 'K')
                                        .replace('М', 'M').replace('Н', 'H').replace('О', 'O').replace('Р', 'P')
                                        .replace('С', 'C').replace('Т', 'T').replace('Х', 'X')
                                        .replace(" ", "").replace("-", "")
                                    val normalizedRaw = normCode(warehouseCode)
                                    // Codes in the Excel file omit the final A for these warehouse IDs.
                                    // 20C is the same warehouse as 20CA; 25C is the same as 25CA.
                                    val canonicalCode = when (normalizedRaw) {
                                        "20C" -> "20CA"
                                        "25C" -> "25CA"
                                        else -> if (normalizedRaw.endsWith("A")) normalizedRaw else normalizedRaw + "A"
                                    }
                                    val appCodes = db.warehouseRows().mapNotNull { row -> row.getOrNull(6)?.takeIf { it.isNotBlank() } }
                                    val appCode = appCodes.firstOrNull { normCode(it) == canonicalCode }
                                        ?: canonicalCode
                                    val sourceKey = imported.size.toString() + "|" + values.joinToString("|")
                                    imported.add(arrayOf(sourceKey, warehouseCode, appCode, values[1], values[2], values[3], values[4], values[5], values[6], values[7], values[8], values[9], values[10]))
                                }
                            }
                        }
                    }
                    event = parser.next()
                }
                if (imported.isEmpty()) throw IllegalArgumentException("У першому аркуші не знайдено рядків залишків.")
                return imported
            }
        } finally {
            temp.delete()
        }
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
                STOCK_OPEN -> {
                    val rows = parseInitialStock(uri)
                    val count = db.replaceInitialStock(rows)
                    val types = rows.groupingBy { it.getOrNull(3).orEmpty() }.eachCount().entries.sortedBy { it.key }
                        .joinToString("\\n") { it.key + ": " + it.value + " рядків" }
                    val warehouses = rows.mapNotNull { it.getOrNull(2) }.distinct().size
                    showAllInitialStock()
                    AlertDialog.Builder(this).setTitle("Імпорт завершено")
                        .setMessage("Збережено рядків: " + count + "\\nСкладів у файлі: " + warehouses + "\\n\\n" + types + "\\n\\nВідкрито загальний список залишків.")
                        .setPositiveButton("Гаразд", null).show()
                }
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

    private fun safeDb(action: () -> Unit): Boolean {
        return try {
            action()
            true
        } catch (e: SQLiteException) {
            showError("Не вдалося виконати операцію: ${e.message ?: "помилка бази даних"}.")
            false
        }
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

    private fun addScreenSummary(root: LinearLayout, eyebrow: String, title: String, value: String) {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(15), dp(13), dp(15), dp(13))
            background = GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf(Color.rgb(35, 35, 27), Color.rgb(15, 27, 29))).apply {
                cornerRadius = dp(14).toFloat()
                setStroke(dp(1), Color.rgb(65, 59, 39))
            }
        }
        box.addView(TextView(this).apply { text = eyebrow; textSize = 10f; setTypeface(null, Typeface.BOLD); setTextColor(Color.rgb(226, 195, 111)); letterSpacing = .08f })
        box.addView(TextView(this).apply { text = title; textSize = 18f; setTypeface(null, Typeface.BOLD); setTextColor(Color.WHITE); setPadding(0, dp(4), 0, dp(3)) })
        box.addView(TextView(this).apply { text = value; textSize = 12f; setTextColor(Color.rgb(173, 185, 186)) })
        root.addView(box, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(10) })
    }

    private fun addEmptyState(root: LinearLayout, title: String, description: String) {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(18), dp(28), dp(18), dp(28))
            background = rounded(Color.rgb(14, 25, 27), 14)
        }
        box.addView(TextView(this).apply { text = title; textSize = 16f; setTypeface(null, Typeface.BOLD); gravity = Gravity.CENTER; setTextColor(Color.WHITE) })
        box.addView(TextView(this).apply { text = description; textSize = 12f; gravity = Gravity.CENTER; setTextColor(Color.rgb(157, 172, 174)); setPadding(0, dp(7), 0, 0) })
        root.addView(box, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(6); bottomMargin = dp(8) })
    }

    private fun addActionRow(root: LinearLayout, actions: List<Pair<String, () -> Unit>>) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        actions.forEachIndexed { index, item ->
            val button = TextView(this).apply {
                text = item.first
                textSize = 13f
                setTypeface(null, Typeface.BOLD)
                setTextColor(Color.rgb(24, 25, 20))
                gravity = Gravity.CENTER
                maxLines = 1
                ellipsize = android.text.TextUtils.TruncateAt.END
                background = GradientDrawable(
                    GradientDrawable.Orientation.TOP_BOTTOM,
                    intArrayOf(Color.rgb(246, 218, 133), Color.rgb(179, 143, 56))
                ).apply {
                    cornerRadius = dp(11).toFloat()
                    setStroke(dp(1), Color.rgb(238, 207, 122))
                }
                setPadding(dp(8), 0, dp(8), 0)
                setOnClickListener { item.second() }
            }
            row.addView(button, LinearLayout.LayoutParams(0, dp(44), 1f).apply {
                if (index > 0) leftMargin = dp(8)
            })
        }
        root.addView(row, LinearLayout.LayoutParams(-1, -2).apply {
            bottomMargin = dp(10)
        })
    }

    private fun addAction(root: LinearLayout, text: String, action: () -> Unit) {
        root.addView(TextView(this).apply {
            this.text=text;textSize=14f;setTypeface(null,Typeface.BOLD);setTextColor(Color.rgb(24,25,20));gravity=Gravity.CENTER
            background=GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,intArrayOf(Color.rgb(246,218,133),Color.rgb(179,143,56))).apply{cornerRadius=dp(12).toFloat();setStroke(dp(1),Color.rgb(238,207,122))}
            setPadding(dp(12),0,dp(12),0);setOnClickListener{action()}
        },LinearLayout.LayoutParams(-1,dp(50)).apply{bottomMargin=dp(10)})
    }

    private fun addRecordCard(root: LinearLayout, title: String, subtitle: String, badge: String, action: () -> Unit) {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(13), dp(12), dp(12), dp(12))
            background = GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                intArrayOf(Color.rgb(22, 35, 34), Color.rgb(12, 24, 26))
            ).apply {
                cornerRadius = dp(12).toFloat()
                setStroke(dp(1), Color.rgb(43, 59, 54))
            }
            isClickable = true
            isFocusable = true
            setOnClickListener { action() }
        }
        val details = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        details.addView(TextView(this).apply {
            text = title
            textSize = 14f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.WHITE)
            maxLines = 2
            ellipsize = android.text.TextUtils.TruncateAt.END
        })
        details.addView(TextView(this).apply {
            text = subtitle
            textSize = 12f
            setTextColor(Color.rgb(164, 179, 178))
            setPadding(0, dp(4), 0, 0)
            maxLines = 2
            ellipsize = android.text.TextUtils.TruncateAt.END
        })
        card.addView(details, LinearLayout.LayoutParams(0, -2, 1f))
        card.addView(TextView(this).apply {
            text = badge
            textSize = 10f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.rgb(226, 195, 111))
            gravity = Gravity.CENTER
            background = rounded(Color.rgb(43, 40, 28), 8)
            setPadding(dp(8), dp(7), dp(8), dp(7))
        }, LinearLayout.LayoutParams(-2, -2).apply { leftMargin = dp(8) })
        root.addView(card, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(8) })
    }

    private fun addNomenclatureCard(root: LinearLayout, title: String, subtitle: String, metric: String, action: () -> Unit) {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(13), dp(12), dp(13), dp(11))
            background = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(Color.rgb(25, 36, 34), Color.rgb(12, 24, 26))
            ).apply {
                cornerRadius = dp(13).toFloat()
                setStroke(dp(1), Color.rgb(45, 62, 56))
            }
            isClickable = true
            isFocusable = true
            setOnClickListener { action() }
        }
        val top = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        top.addView(TextView(this).apply {
            text = title
            textSize = 14f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.WHITE)
            maxLines = 3
            ellipsize = android.text.TextUtils.TruncateAt.END
        }, LinearLayout.LayoutParams(0, -2, 1f))
        top.addView(TextView(this).apply {
            text = metric.ifBlank { "—" }
            textSize = 13f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.rgb(226, 195, 111))
            gravity = Gravity.CENTER
            background = rounded(Color.rgb(46, 42, 28), 9)
            setPadding(dp(9), dp(7), dp(9), dp(7))
        }, LinearLayout.LayoutParams(-2, -2).apply { leftMargin = dp(8) })
        card.addView(top)
        card.addView(TextView(this).apply {
            text = subtitle
            textSize = 12f
            setTextColor(Color.rgb(164, 179, 178))
            setPadding(0, dp(7), 0, dp(8))
            maxLines = 2
            ellipsize = android.text.TextUtils.TruncateAt.END
        })
        val footer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            background = rounded(Color.rgb(19, 31, 31), 9)
            setPadding(dp(10), dp(8), dp(10), dp(8))
        }
        footer.addView(TextView(this).apply {
            text = "▦  КІЛЬКІСТЬ / БАЛАНС"
            textSize = 9f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.rgb(143, 164, 160))
            letterSpacing = 0.04f
        }, LinearLayout.LayoutParams(0, -2, 1f))
        footer.addView(TextView(this).apply {
            text = "Відкрити  ↗"
            textSize = 11f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.rgb(226, 195, 111))
            gravity = Gravity.CENTER
        })
        card.addView(footer)
        root.addView(card, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(8) })
    }

    private fun addAccountCard(root: LinearLayout, title: String, subtitle: String, balance: Double, action: () -> Unit) {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(13), dp(14), dp(12))
            background = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(Color.rgb(31, 39, 34), Color.rgb(13, 25, 27))
            ).apply {
                cornerRadius = dp(14).toFloat()
                setStroke(dp(1), Color.rgb(77, 78, 53))
            }
            isClickable = true
            isFocusable = true
            setOnClickListener { action() }
        }
        val heading = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        heading.addView(TextView(this).apply {
            text = title
            textSize = 15f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.WHITE)
            maxLines = 2
            ellipsize = android.text.TextUtils.TruncateAt.END
        }, LinearLayout.LayoutParams(0, -2, 1f))
        heading.addView(TextView(this).apply {
            text = formatQty(balance)
            textSize = 16f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.rgb(226, 195, 111))
            gravity = Gravity.CENTER
            background = rounded(Color.rgb(48, 43, 28), 9)
            setPadding(dp(10), dp(7), dp(10), dp(7))
        })
        card.addView(heading)
        card.addView(TextView(this).apply {
            text = subtitle
            textSize = 12f
            setTextColor(Color.rgb(164, 179, 178))
            setPadding(0, dp(7), 0, dp(9))
            maxLines = 2
            ellipsize = android.text.TextUtils.TruncateAt.END
        })
        card.addView(TextView(this).apply {
            text = "Відкрити картку  ›"
            textSize = 12f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.rgb(226, 195, 111))
            gravity = Gravity.CENTER_VERTICAL
            background = rounded(Color.rgb(35, 39, 29), 8)
            setPadding(dp(10), dp(8), dp(10), dp(8))
        })
        root.addView(card, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(9) })
    }

    private fun addBalanceLine(root: LinearLayout, title: String, balance: Double, unit: String, level: Int) {
        val nested = level > 1
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(if (nested) 10 else 12), dp(if (nested) 9 else 11), dp(11), dp(if (nested) 9 else 11))
            background = rounded(
                when (level) {
                    0 -> Color.rgb(40, 39, 28)
                    1 -> Color.rgb(17, 31, 31)
                    else -> Color.rgb(13, 25, 27)
                }, 9
            )
        }
        row.addView(TextView(this).apply {
            text = (if (nested) "↳  " else if (level == 1) "▦  " else "") + title
            textSize = if (nested) 12f else 13f
            setTypeface(null, if (level == 0 || level == 1) Typeface.BOLD else Typeface.NORMAL)
            setTextColor(if (level == 0) Color.rgb(226, 195, 111) else Color.rgb(220, 229, 227))
            maxLines = 2
            ellipsize = android.text.TextUtils.TruncateAt.END
        }, LinearLayout.LayoutParams(0, -2, 1f))
        row.addView(TextView(this).apply {
            text = "${formatQty(balance)} ${unit}".trim()
            textSize = if (nested) 12f else 13f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.WHITE)
            gravity = Gravity.END or Gravity.CENTER_VERTICAL
        })
        root.addView(row, LinearLayout.LayoutParams(-1, -2).apply {
            leftMargin = dp(if (nested) 12 else 0)
            bottomMargin = dp(5)
        })
    }

    private fun addManageRow(root: LinearLayout, title: String, subtitle: String, action: () -> Unit) {
        val box=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(14),dp(12),dp(14),dp(12));background=GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,intArrayOf(Color.rgb(20,32,32),Color.rgb(12,23,25))).apply{cornerRadius=dp(13).toFloat();setStroke(dp(1),Color.rgb(43,57,54))};setOnClickListener{action()}}
        box.addView(TextView(this).apply{text=title;textSize=15f;setTypeface(null,Typeface.BOLD);setTextColor(Color.WHITE)})
        box.addView(TextView(this).apply{text=subtitle;textSize=12f;setTextColor(Color.rgb(164,177,180));setPadding(0,dp(4),0,dp(9))})
        box.addView(TextView(this).apply{text="⚙  Керувати   ›";textSize=12f;setTypeface(null,Typeface.BOLD);setTextColor(Color.rgb(226,195,111));gravity=Gravity.CENTER_VERTICAL;setPadding(dp(10),0,dp(10),0);background=rounded(Color.rgb(45,39,25),9)},LinearLayout.LayoutParams(-1,dp(36)))
        root.addView(box,LinearLayout.LayoutParams(-1,-2).apply{bottomMargin=dp(8)})
    }

    private fun addRow(root: LinearLayout, title: String, subtitle: String) {
        val box=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(14),dp(12),dp(14),dp(12));background=GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,intArrayOf(Color.rgb(19,31,32),Color.rgb(12,23,25))).apply{cornerRadius=dp(12).toFloat();setStroke(dp(1),Color.rgb(38,51,50))}}
        box.addView(TextView(this).apply{text=title;textSize=14f;setTypeface(null,Typeface.BOLD);setTextColor(Color.rgb(245,247,247))})
        box.addView(TextView(this).apply{text=subtitle;textSize=12f;setTextColor(Color.rgb(162,176,179));setPadding(0,dp(4),0,0)})
        root.addView(box,LinearLayout.LayoutParams(-1,-2).apply{bottomMargin=dp(7)})
    }

    private fun formDialog(title: String, labels: List<String>, initialValues: List<String> = emptyList(), onSave: (List<String>) -> Unit) {
        val gold=Color.rgb(226,195,111)
        val layout=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(20),dp(8),dp(20),dp(2));setBackgroundColor(Color.rgb(14,25,27))}
        val fields=labels.mapIndexed{index,label->EditText(this).apply{
            hint=label;setTextColor(Color.WHITE);setHintTextColor(Color.rgb(126,141,144));textSize=14f
            background=GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,intArrayOf(Color.rgb(42,42,31),Color.rgb(18,29,29))).apply{cornerRadius=dp(10).toFloat();setStroke(dp(1),Color.rgb(48,61,57))}
            setPadding(dp(12),dp(8),dp(12),dp(8));if(index<initialValues.size)setText(initialValues[index]);setSingleLine(true)
            layout.addView(this,LinearLayout.LayoutParams(-1,dp(48)).apply{bottomMargin=dp(8)})
        }}
        val dialog=AlertDialog.Builder(this).setTitle(title).setView(layout).setNegativeButton("Скасувати",null).setPositiveButton("Зберегти"){_,_->onSave(fields.map{it.text.toString().trim()})}.create()
        dialog.setOnShowListener{
            dialog.window?.setBackgroundDrawable(rounded(Color.rgb(14,25,27),16))
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(gold)
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(Color.rgb(177,188,190))
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.isAllCaps=false
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.isAllCaps=false
            dialog.window?.setLayout((resources.displayMetrics.widthPixels*0.92f).toInt(),-2)
        }
        dialog.show()
    }

}
