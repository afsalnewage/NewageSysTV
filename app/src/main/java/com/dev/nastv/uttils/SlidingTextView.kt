package com.dev.nastv.uttils
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextUtils
import android.text.style.ReplacementSpan
import android.util.AttributeSet
import android.util.Log
import androidx.appcompat.widget.AppCompatTextView

class SlidingTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    private var slidingDuration: Long = 2000 // Duration for the sliding animation
    private val slidingSpans = mutableListOf<SlidingSpan>()
    private val spannableStringBuilder = SpannableStringBuilder()

//    init {
//        post { startSlidingAnimation() }
//    }

    fun startAnimation() {
        post {
            val textWidth = paint.measureText(text.toString())
            val viewWidth = width.toFloat()
               Log.d("RRR3","text width $textWidth")
               Log.d("RRR3","text width ${text.length}") //textWidth > viewWidth
            if (text.length>25) {
                // Text is too long, use marquee animation
                ellipsize = TextUtils.TruncateAt.MARQUEE
                marqueeRepeatLimit = -1
                isSelected = true
                isSingleLine = true
                setHorizontallyScrolling(true)
            } else {
                // Text fits, use sliding animation
                startSlidingAnimation()
            }
        }
    }
     fun startSlidingAnimation() {
        val text = text.toString()
        spannableStringBuilder.clear()
        spannableStringBuilder.append(text)

        slidingSpans.clear()
        text.forEachIndexed { index, _ ->
            val span = SlidingSpan()
            slidingSpans.add(span)
            spannableStringBuilder.setSpan(span, index, index + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        setText(spannableStringBuilder, BufferType.SPANNABLE)

        slidingSpans.forEachIndexed { index, span ->
            val animator = ValueAnimator.ofFloat(1f, 0f).apply {
                duration = slidingDuration
                startDelay = index * 100L // Adjust delay for each character
                addUpdateListener {
                    span.translationX = it.animatedValue as Float
                    invalidate()
                }
            }
            animator.start()
        }


    }

    private class SlidingSpan : ReplacementSpan() {
        var translationX: Float = 1f

        override fun getSize(
            paint: Paint, text: CharSequence, start: Int, end: Int, fm: Paint.FontMetricsInt?
        ): Int {
            return paint.measureText(text, start, end).toInt()
        }

        override fun draw(
            canvas: Canvas, text: CharSequence, start: Int, end: Int,
            x: Float, top: Int, y: Int, bottom: Int, paint: Paint
        ) {
            val width = paint.measureText(text, start, end)
            canvas.drawText(text, start, end, x + width * translationX, y.toFloat(), paint)
        }
    }
}
