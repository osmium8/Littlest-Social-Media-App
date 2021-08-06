package com.example.snplc.ui

import android.content.Context
import android.view.View
import android.view.animation.AnimationUtils
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.example.snplc.R

/**
 * Extension function to slide-up a SINGLE view
 *
 * @startOffset
 * how long to wait for anim to start
 */
fun View.slideUp(context: Context, animTime: Long, startOffset: Long) {
    val slideUp = AnimationUtils.loadAnimation(context, R.anim.slide_up).apply {
        duration = animTime
        interpolator = FastOutSlowInInterpolator()
        this.startOffset = startOffset
    }
    startAnimation(slideUp)
}


/**
 * function to slide-up a MUTLIPLE view
 *
 * delay * i
 * later view needs more delay
 */
fun slideUpViews(context: Context, vararg views: View, animTime: Long = 300L, delay: Long = 150L) {
    for(i in views.indices) {
        views[i].slideUp(context, animTime, delay * i)
    }
}