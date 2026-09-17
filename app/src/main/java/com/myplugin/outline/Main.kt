package com.myplugin.outline

import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.TypedValue
import java.lang.reflect.Field

class Main {
    companion object {
        @JvmStatic
        private var paint: Paint? = null

        @JvmStatic
        private val fieldCache = HashMap<String, Field?>()

        @JvmStatic
        fun start() {
        }

        @JvmStatic
        fun version(): String = "1.0.0"

        @JvmStatic
        fun drawOutline(
            cell: Any,
            canvas: Canvas,
            colorHex: String,
            widthDp: Float?,
            opacityPct: Int?,
            forceEverywhere: Boolean?
        ) {
            try {
                if (forceEverywhere != true && readBoolean(cell, "drawOutbounds") == false) {
                    return
                }
                val rect = bubbleRect(cell) ?: return
                val stroke = cachedPaint()
                stroke.color = withOpacity(parseColor(colorHex), (opacityPct ?: 100).coerceIn(0, 100))
                stroke.strokeWidth = dp(widthDp ?: 1.5f)
                val radius = dp(18f)
                canvas.drawRoundRect(rect, radius, radius, stroke)
            } catch (_: Throwable) {
            }
        }

        @JvmStatic
        fun getThemeColor(key: String): Int {
            return try {
                val themeClass = Class.forName("org.telegram.ui.ActionBar.Theme")
                val method = themeClass.getDeclaredMethod("getColor", String::class.java)
                method.invoke(null, key) as Int
            } catch (_: Throwable) {
                0
            }
        }

        @JvmStatic
        fun parseColor(hex: String): Int {
            var value = hex.trim()
            if (value.startsWith("#")) {
                value = value.substring(1)
            }
            return when (value.length) {
                6 -> 0xFF000000.toInt() or (java.lang.Long.decode("0x$value").toInt())
                8 -> java.lang.Long.decode("0x$value").toInt()
                else -> 0xFF000000.toInt()
            }
        }

        @JvmStatic
        fun dp(value: Float): Float {
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value,
                Resources.getSystem().displayMetrics
            )
        }

        @JvmStatic
        fun versionCode(): Int = 1

        private fun cachedPaint(): Paint {
            paint?.let { return it }
            val stroke = Paint(Paint.ANTI_ALIAS_FLAG)
            stroke.style = Paint.Style.STROKE
            paint = stroke
            return stroke
        }

        private fun withOpacity(color: Int, opacity: Int): Int {
            val alpha = ((color ushr 24) and 0xFF) * opacity / 100
            val r = (color ushr 16) and 0xFF
            val g = (color ushr 8) and 0xFF
            val b = color and 0xFF
            return (alpha shl 24) or (r shl 16) or (g shl 8) or b
        }

        private fun bubbleRect(cell: Any): RectF? {
            val width = readNumber(cell, "backgroundWidth") ?: return null
            val height = readNumber(cell, "backgroundHeight") ?: return null
            if (width <= 0f || height <= 0f) return null
            val x = firstNumber(cell, "layoutX", "backgroundDrawX") ?: 0f
            val y = firstNumber(cell, "layoutY", "backgroundDrawY") ?: 0f
            return RectF(x, y, x + width, y + height)
        }

        private fun firstNumber(cell: Any, vararg names: String): Float? {
            for (name in names) {
                val value = readNumber(cell, name)
                if (value != null) {
                    return value
                }
            }
            return null
        }

        private fun readNumber(cell: Any, name: String): Float? {
            val field = fieldFor(cell, name) ?: return null
            return try {
                when (val value = field.get(cell)) {
                    is Float -> value
                    is Double -> value.toFloat()
                    is Number -> value.toFloat()
                    else -> null
                }
            } catch (_: Throwable) {
                null
            }
        }

        private fun readBoolean(cell: Any, name: String): Boolean? {
            val field = fieldFor(cell, name) ?: return null
            return try {
                when (val value = field.get(cell)) {
                    is Boolean -> value
                    is Int -> value != 0
                    else -> null
                }
            } catch (_: Throwable) {
                null
            }
        }

        private fun fieldFor(cell: Any, name: String): Field? {
            val key = cell.javaClass.name + "#" + name
            fieldCache[key]?.let { return it }
            var cls: Class<*>? = cell.javaClass
            while (cls != null) {
                try {
                    val field = cls.getDeclaredField(name)
                    field.isAccessible = true
                    fieldCache[key] = field
                    return field
                } catch (_: Throwable) {
                }
                cls = cls.superclass
            }
            fieldCache[key] = null
            return null
        }
    }
}