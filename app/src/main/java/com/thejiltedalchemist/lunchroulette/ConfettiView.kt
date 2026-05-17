package com.thejiltedalchemist.lunchroulette

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class ConfettiView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private class Particle(
        val startX: Float, val startY: Float,
        val vx: Float, val vy: Float,
        val color: Int, val size: Float,
        val startRotation: Float, val rotSpeed: Float,
        val isRect: Boolean
    )

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var particles: List<Particle> = emptyList()
    private var elapsed = 0f
    private var animator: ValueAnimator? = null

    private val palette = intArrayOf(
        0xFFE74C3C.toInt(), 0xFFF39C12.toInt(), 0xFF2ECC71.toInt(),
        0xFF3498DB.toInt(), 0xFF9B59B6.toInt(), 0xFFE91E8C.toInt(),
        0xFF1ABC9C.toInt()
    )

    fun burst(cx: Float, cy: Float) {
        animator?.cancel()
        particles = List(80) {
            val angle = (Random.nextFloat() * 2 * PI).toFloat()
            val speed = Random.nextFloat() * 16f + 8f
            Particle(
                startX = cx, startY = cy,
                vx = cos(angle) * speed,
                vy = sin(angle) * speed - 22f,
                color = palette[Random.nextInt(palette.size)],
                size = Random.nextFloat() * 10f + 6f,
                startRotation = Random.nextFloat() * 360f,
                rotSpeed = (Random.nextFloat() - 0.5f) * 720f,
                isRect = Random.nextBoolean()
            )
        }
        elapsed = 0f
        visibility = VISIBLE
        animator = ValueAnimator.ofFloat(0f, 2.5f).apply {
            duration = 2500
            interpolator = LinearInterpolator()
            addUpdateListener {
                elapsed = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    override fun onDraw(canvas: Canvas) {
        if (particles.isEmpty()) return
        val gravity = 30f
        val t = elapsed
        val alpha = ((1f - t / 2.5f) * 255).toInt().coerceIn(0, 255)
        if (alpha <= 0) {
            visibility = GONE
            particles = emptyList()
            return
        }
        particles.forEach { p ->
            val x = p.startX + p.vx * t
            val y = p.startY + p.vy * t + 0.5f * gravity * t * t
            val rot = p.startRotation + p.rotSpeed * t
            paint.color = p.color
            paint.alpha = alpha
            canvas.save()
            canvas.translate(x, y)
            canvas.rotate(rot)
            if (p.isRect) {
                canvas.drawRect(-p.size / 2, -p.size / 4, p.size / 2, p.size / 4, paint)
            } else {
                canvas.drawCircle(0f, 0f, p.size / 2f, paint)
            }
            canvas.restore()
        }
    }
}
