package com.dev.nastv.uttils

import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.dev.nastv.R

class GradientTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    private var gradientShader: Shader? = null

    override fun onDraw(canvas: Canvas) {
        if (gradientShader == null) {
            val width = paint.measureText(text.toString())
            val height = textSize
            gradientShader = LinearGradient(
                0f, 0f, 0f, height,
                intArrayOf(
                //    context.getColor(R.color.golden_light),
                    context.getColor(R.color.pink),
                    context.getColor(R.color.golden)
                ),
                null,
                Shader.TileMode.CLAMP
            )
            paint.shader = gradientShader
        }
        super.onDraw(canvas)
    }

//    override fun onDraw(canvas: Canvas) {
//        val textPaint: Paint = paint
//        val width = measuredWidth.toFloat()
//        val height = measuredHeight.toFloat()
//
//        // Clear any existing shader to ensure it's not reused for subsequent lines
//        textPaint.shader = null
//
//        // Calculate gradient start and end points for each line
//        var y = 0f
//        val lineHeight = lineHeight.toFloat()
//        val totalLines = lineCount
//        for (i in 0 until totalLines) {
//            val lineBottom = y + lineHeight
//            val gradientShader = LinearGradient(
//                0f, y, 0f, lineBottom,
//                intArrayOf(
//                    context.getColor(R.color.pink),
//                    context.getColor(R.color.golden)
//                ),
//                null,
//                Shader.TileMode.CLAMP
//            )
//            textPaint.shader = gradientShader
//
//            // Draw each line with gradient
//            canvas.drawText(
//                text,
//                layout.getLineStart(i),
//                layout.getLineEnd(i),
//                0f,
//                lineBottom,
//                textPaint
//            )
//
//            // Move to the next line
//            y = lineBottom
//        }
   // }


}