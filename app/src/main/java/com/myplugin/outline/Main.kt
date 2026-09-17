package com.myplugin.outline

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF

object Main {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rect = RectF()
    private var lastColor: Int = 0
    private var lastAlpha: Int = -1
    private var lastWidth: Float = -1f
    private var lastRadius: Float = -1f

    @JvmStatic
    fun start() {
    }

    @JvmStatic
    fun version(): Int = 1

    @JvmStatic
    fun drawOutline(cell: Any, canvas: Canvas, colorHex: String, widthDp: Float, opacityPct: Int, forceEverywhere: Boolean) {
        try {
            val left = getInt(cell, "getBackgroundDrawableLeft")
            val top = getInt(cell, "getBackgroundDrawableTop")
            val right = getInt(cell, "getBackgroundDrawableRight")
            val bottom = getInt(cell, "getBackgroundDrawableBottom")
            val w = right - left
            val h = bottom - top
            if (w <= 0 || h <= 0) {
                return
            }
            val color = parseColor(colorHex)
            val alpha = (opacityPct.coerceIn(0, 100) * 255 / 100).coerceIn(0, 255)
            val density = android.content.res.Resources.getSystem().displayMetrics.density
            val width = widthDp.coerceAtLeast(0.5f) * density
            val radius = bubbleRadius() * density
            if (color != lastColor || alpha != lastAlpha || width != lastWidth || radius != lastRadius) {
                paint.style = Paint.Style.STROKE
                paint.color = (color and 0x00FFFFFF) or (alpha shl 24)
                paint.strokeWidth = width
                lastColor = color
                lastAlpha = alpha
                lastWidth = width
                lastRadius = radius
            }
            rect.set(left + width / 2f, top + width / 2f, right - width / 2f, bottom - width / 2f)
            canvas.drawRoundRect(rect, radius, radius, paint)
        } catch (_: Throwable) {
        }
    }

    private fun getInt(cell: Any, methodName: String): Int {
        return cell.javaClass.getMethod(methodName).invoke(cell) as Int
    }

    private fun bubbleRadius(): Float {
        try {
            val radius = Class.forName("org.telegram.messenger.SharedConfig").getField("bubbleRadius").getInt(null)
            return radius.coerceIn(0, 60).toFloat()
        } catch (_: Throwable) {
            return 18f
        }
    }

    private fun parseColor(hex: String): Int {
        try {
            val cleaned = hex.trim().removePrefix("#")
            if (cleaned.length == 6) {
                return cleaned.toInt(16) or 0xff000000.toInt()
            }
            if (cleaned.length == 8) {
                return cleaned.toInt(16)
            }
        } catch (_: Throwable) {
        }
        return 0xff000000.toInt()
    }
}