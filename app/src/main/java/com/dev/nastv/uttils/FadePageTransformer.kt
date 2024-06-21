package com.dev.nastv.uttils

import android.view.View
import androidx.viewpager2.widget.ViewPager2

class FadePageTransformer: ViewPager2.PageTransformer {
    override fun transformPage(page: View, position: Float) {
        page.apply {
            alpha = when {
                position < -1 -> 0f
                position <= 0 -> 1f + position
                position <= 1 -> 1f - position
                else -> 0f
            }
        }
    }
}