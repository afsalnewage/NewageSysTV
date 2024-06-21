package com.dev.nastv.uttils
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.widget.TextView
class SparkleTextView  @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : androidx.appcompat.widget.AppCompatTextView(context, attrs, defStyleAttr) {

    private var sparklePaint: Paint = Paint()
    private var sparkleAnimatorSet: AnimatorSet? = null

    init {
        sparklePaint.color = 0xFFFFFF00.toInt() // Yellow sparkle color
        sparklePaint.style = Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        // Draw sparkle effect here
        drawSparkles(canvas)
    }

    private fun drawSparkles(canvas: Canvas?) {
        canvas?.let {
            // Example sparkle positions and sizes, customize as needed
            val sparkleSize = 10f
            val sparklePositions = listOf(
                Pair(0.2f, 0.3f),
                Pair(0.5f, 0.6f),
                Pair(0.7f, 0.2f)
            )

            for (position in sparklePositions) {
                val x = width * position.first
                val y = height * position.second
                it.drawCircle(x, y, sparkleSize, sparklePaint)
            }
        }
    }

    fun startSparkleAnimation() {
        val sparkleAnimators = listOf(
            createSparkleAnimator(0.2f, 0.3f),
            createSparkleAnimator(0.5f, 0.6f),
            createSparkleAnimator(0.7f, 0.2f)
        )

        sparkleAnimatorSet = AnimatorSet().apply {
            playTogether(sparkleAnimators)
            duration = 1000
            start()
        }
    }

    private fun createSparkleAnimator(xFactor: Float, yFactor: Float): ObjectAnimator {
        return ObjectAnimator.ofFloat(this, "sparkleAlpha", 0f, 1f).apply {
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }
    }

    fun setSparkleAlpha(alpha: Float) {
        sparklePaint.alpha = (alpha * 255).toInt()
        invalidate()
    }
}