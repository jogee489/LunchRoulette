/**
 * Class used to create the view of the roulette wheel
 */

package com.thejiltedalchemist.lunchroulette

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View

class RouletteView(context: Context,attrs: AttributeSet) : View(context, attrs) {

    private lateinit var itemList: List<RestaurantsModel>
    private var rectangle = RectF()
    private var arcPaint = Paint()
    private var textPaint = Paint()
    private var padding = 10F
    private var radius = 0F
    private var arcAngle = 0F

    private val textColor = resources.getColor(R.color.colorAccent)
    private val colorDark = resources.getColor(R.color.colorPrimaryDark)
    private val colorLight = resources.getColor(R.color.colorPrimary)

    private var winnerIndex = -1
    private var highlightAlpha = 0f

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = measuredWidth.coerceAtMost(measuredHeight)
        padding = if (paddingLeft == 0) 10f else paddingLeft.toFloat()
        radius = width - (padding * 2)
        setMeasuredDimension(width, width)
    }

    private val sliceColors = listOf(
        resources.getColor(R.color.wheel_slice_1),
        resources.getColor(R.color.wheel_slice_2),
        resources.getColor(R.color.wheel_slice_3),
    )

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (itemList.isEmpty()) {
            return
        }

        arcAngle = 360F / itemList.size
        arcPaint.isAntiAlias = true
        arcPaint.isDither = true
        rectangle = RectF(padding, padding, padding + radius, padding + radius)

        itemList.forEachIndexed { index, item ->
            arcPaint.color = colorForSlice(index, itemList.size)
            canvas.drawArc(rectangle, index * arcAngle, arcAngle, true, arcPaint)
            writeText(canvas, index * arcAngle, arcAngle, item.name)
        }
        if (winnerIndex >= 0) {
            arcPaint.color = Color.WHITE
            arcPaint.alpha = (highlightAlpha * 120).toInt()
            canvas.drawArc(rectangle, winnerIndex * arcAngle, arcAngle, true, arcPaint)
            arcPaint.alpha = 255
        }
        drawCenter(canvas)
    }

    // For the last slice, pick whichever palette entry conflicts with neither the first
    // slice (wrap-around adjacency) nor the previous slice (direct adjacency).
    private fun colorForSlice(index: Int, total: Int): Int {
        val n = sliceColors.size
        var colorIndex = index % n
        if (index == total - 1 && total > 1) {
            val firstColorIndex = 0
            val prevColorIndex = (total - 2) % n
            if (colorIndex == firstColorIndex || colorIndex == prevColorIndex) {
                colorIndex = (0 until n).first { it != firstColorIndex && it != prevColorIndex }
            }
        }
        return sliceColors[colorIndex]
    }

    /**
     * Setter for the list of roulette items to be shown
     * @param items The list of the roulette items
     */
    fun addRouletteItems(items: List<RestaurantsModel>) {
        itemList = items
        invalidate()
    }

    fun setWinner(index: Int) {
        winnerIndex = index
        ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 600
            repeatMode = ValueAnimator.REVERSE
            repeatCount = 5
            addUpdateListener {
                highlightAlpha = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    fun clearWinner() {
        winnerIndex = -1
        highlightAlpha = 0f
        invalidate()
    }

    /**
     * Add the text to the arc of the roulette
     */
    private fun writeText(canvas: Canvas, currentAngle: Float, arcAngle :Float, text: String) {
        val path = Path()
        path.addArc(rectangle,currentAngle, arcAngle)
        val textWidth = textPaint.measureText(text)
        val hOffset = radius * Math.PI / itemList.size / 2 - (textWidth / 2)
        val vOffset = radius/8

        textPaint.setColor(textColor)
        textPaint.textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
            20f, resources.displayMetrics)
        textPaint.letterSpacing = 0.1f
        canvas.drawTextOnPath(text, path, hOffset.toFloat(), vOffset-35, textPaint)
    }

    /**
     * Creates the circle in the center of the roulette. It will be two
     * circles with the primary dark on the outside and the primary dark
     * on the inside. Not perfectly center but makes the spin more obvious
     * @param canvas The canvas where the center is drawn
     */
    private fun drawCenter(canvas: Canvas) {
        val centerPaint = Paint()
        val center2Paint = Paint()
        val centerPaintOutline = Paint()
        centerPaint.setColor(colorLight)
        center2Paint.setColor(colorDark)
        centerPaintOutline.setColor(textColor)
        val cx = padding + radius / 2
        val cy = padding + radius / 2
        canvas.drawCircle(cx, cy, radius/11f, centerPaintOutline)
        canvas.drawCircle(cx, cy, radius/12f, centerPaint)
        canvas.drawCircle(cx, cy, radius/16f, center2Paint)
    }
}
