/**
 * Class used to create the view of the roulette wheel
 */

package com.thejiltedalchemist.lunchroulette

import android.content.Context
import android.graphics.Canvas
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
    private var colorDark = resources.getColor(R.color.colorPrimaryDark)
    private var colorLight = resources.getColor(R.color.colorPrimary)
    private var colorAccent = resources.getColor(R.color.design_default_color_secondary_variant)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = measuredWidth.coerceAtMost(measuredHeight) //Math.min
        padding = if (paddingLeft == 0) 10f else paddingLeft.toFloat()
        radius = width - (padding * 2)
        setMeasuredDimension(width, width)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (itemList.isEmpty()) {
            return
        }

        var currentAngle = 0F
        var currentColor = colorDark
        arcAngle = 360F/itemList.size
        arcPaint.isAntiAlias = true
        arcPaint.isDither = true

        rectangle = RectF(padding, padding, padding + radius, padding + radius)
        for(i in itemList) {
            println("Drawing item ${i.name} in $currentColor at angle $currentAngle")
            arcPaint.setColor(currentColor)
            canvas.drawArc(rectangle, currentAngle, arcAngle, true, arcPaint)
            writeText(canvas, currentAngle, arcAngle, i.name)
            when (currentColor) {
                colorDark -> currentColor = colorLight
                colorLight -> currentColor = colorAccent
                colorAccent -> currentColor = colorDark
            }

            currentAngle += arcAngle
        }
        drawCenter(canvas)
    }

    /**
     * Setter for the list of roulette items to be shown
     * @param items The list of the roulette items
     */
    fun addRouletteItems(items: List<RestaurantsModel>) {
        itemList = items
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
        canvas.drawCircle(radius/2, radius/2, radius/11f, centerPaintOutline)
        canvas.drawCircle(radius/2, radius/2, radius/12f, centerPaint)
        canvas.drawCircle(radius/2, radius/2, radius/16f, center2Paint)
    }
}