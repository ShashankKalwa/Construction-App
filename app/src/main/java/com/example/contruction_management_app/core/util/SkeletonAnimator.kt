package com.example.contruction_management_app.core.util

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.view.View

object SkeletonAnimator {

    fun startPulsing(view: View): ObjectAnimator {
        val animator = ObjectAnimator.ofFloat(view, "alpha", 0.35f, 1.0f).apply {
            duration = 850
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
        }
        animator.start()
        view.setTag(com.example.contruction_management_app.R.id.skeletonContainer, animator)
        return animator
    }

    fun stopPulsing(view: View) {
        val animator = view.getTag(com.example.contruction_management_app.R.id.skeletonContainer) as? ObjectAnimator
        animator?.cancel()
        view.alpha = 1.0f
    }
}
