package com.thejiltedalchemist.lunchroulette

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.core.content.ContextCompat

class RouletteView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private var itemList: List<RestaurantsModel> = emptyList()
    private val rectangle = RectF()
    private val arcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { isDither = true }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER }
    private val centerOutlinePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val centerLightPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val centerDarkPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var padding = 10F
    private var radius = 0F
    private var cachedTextSizePx = 0f

    private val textColor = ContextCompat.getColor(context, R.color.colorAccent)
    private val colorDark = ContextCompat.getColor(context, R.color.colorPrimaryDark)
    private val colorLight = ContextCompat.getColor(context, R.color.colorPrimary)
    private val colorAccent = ContextCompat.getColor(context, R.color.design_default_color_secondary_variant)
    private val sliceColors = listOf(colorDark, colorLight, colorAccent)

    init {
        centerOutlinePaint.color = textColor
        centerLightPaint.color = colorLight
        centerDarkPaint.color = colorDark
        textPaint.color = textColor
        textPaint.letterSpacing = 0.1f
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = measuredWidth.coerceAtMost(measuredHeight)
        padding = if (paddingLeft == 0) 10f else paddingLeft.toFloat()
        radius = width - (padding * 2)
        setMeasuredDimension(width, width)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (itemList.isEmpty()) return

        val arcAngle = 360F / itemList.size
        rectangle.set(padding, padding, padding + radius, padding + radius)

        itemList.forEachIndexed { index, item ->
            arcPaint.color = colorForSlice(index, itemList.size)
            canvas.drawArc(rectangle, index * arcAngle, arcAngle, true, arcPaint)
            writeText(canvas, index * arcAngle, arcAngle, item.name)
        }
        drawCenter(canvas)
    }

    fun addRouletteItems(items: List<RestaurantsModel>) {
        itemList = items
        cachedTextSizePx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            (120f / items.size.coerceAtLeast(1)).coerceIn(8f, 20f),
            resources.displayMetrics
        )
        invalidate()
    }

    // Ensures no two adjacent slices share a color, including the wrap-around (last → first).
    // Simple i % 3 fails when total % 3 == 1 (last slice gets color 0, same as first).
    private fun colorForSlice(index: Int, total: Int): Int {
        var colorIndex = index % 3
        if (index == total - 1 && colorIndex == 0 && total > 1) colorIndex = 1
        return sliceColors[colorIndex]
    }

    private fun writeText(canvas: Canvas, startAngle: Float, arcAngle: Float, text: String) {
        val cx = padding + radius / 2
        val cy = padding + radius / 2

        textPaint.textSize = cachedTextSizePx
        val verticalCenter = -(textPaint.ascent() + textPaint.descent()) / 2f

        canvas.save()
        canvas.rotate(startAngle + arcAngle / 2, cx, cy)
        canvas.drawText(text, cx + radius * 0.35f, cy + verticalCenter, textPaint)
        canvas.restore()
    }

    private fun drawCenter(canvas: Canvas) {
        val cx = padding + radius / 2
        val cy = padding + radius / 2
        canvas.drawCircle(cx, cy, radius / 11f, centerOutlinePaint)
        canvas.drawCircle(cx, cy, radius / 12f, centerLightPaint)
        canvas.drawCircle(cx, cy, radius / 16f, centerDarkPaint)
    }
}
