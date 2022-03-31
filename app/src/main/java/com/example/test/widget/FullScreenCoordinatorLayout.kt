package com.example.test.widget

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.util.ObjectsCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.test.R

class FullScreenCoordinatorLayout @JvmOverloads constructor(
    context: Context, attributeSet: AttributeSet? = null, defStyleAttr: Int = 0
) : CoordinatorLayout(context, attributeSet, defStyleAttr) {

    private var lastInset: WindowInsetsCompat? = null

    private var consumeStatusBarHeight = false

    private var consumeNavigationBarHeight = false

    private var editModeStatusBarHeight = 0

    private var editModeNavigationBarHeight = 0

    private val editModeStatusBarView: View by lazy {
        View(context).apply { elevation = 999f }
    }
    private val editModeNavigationBarView: View by lazy {
        View(context).apply { elevation = 999f }
    }
    private var statusBarColor = Color.TRANSPARENT

    private var navigationBarColor = Color.TRANSPARENT

    private val statusBarHeight: Int
        get() {
            if (isInEditMode) return editModeStatusBarHeight

            return lastInset?.systemWindowInsetTop ?: 0
        }
    private val navigationBarHeight: Int
        get() {
            if (isInEditMode) return editModeNavigationBarHeight

            return lastInset?.systemWindowInsetBottom ?: 0
        }
    private val onApplyWindowInsetsListener: androidx.core.view.OnApplyWindowInsetsListener by lazy {
        androidx.core.view.OnApplyWindowInsetsListener { view, windowInsets ->
            if (!ObjectsCompat.equals(lastInset, windowInsets)) {
                lastInset = windowInsets
                requestLayout()
                onWindowInsetsUpdated()
            }
            windowInsets
        }
    }

    private fun onWindowInsetsUpdated() {
        val activity = activityContext
        activity?.window?.statusBarColor = statusBarColor
        activity?.window?.navigationBarColor = navigationBarColor
        addEdiTModeStatusViewAndNavigationBarView()
    }

    private fun addEdiTModeStatusViewAndNavigationBarView() {
        if (!isInEditMode) {
            return
        }
        editModeStatusBarView.setBackgroundColor(statusBarColor)
        addView(
            editModeStatusBarView,
            LayoutParams(LayoutParams.MATCH_PARENT, statusBarHeight).apply {
                gravity = Gravity.TOP
            })
        editModeNavigationBarView.setBackgroundColor(navigationBarColor)
        addView(
            editModeNavigationBarView,
            LayoutParams(LayoutParams.MATCH_PARENT, navigationBarHeight).apply {
                gravity = Gravity.BOTTOM
            })
    }

    private val activityContext: Activity?
        get() {
            var context: Context? = context
            while (null != context) {
                if (context is Activity) return context

                if (context is ContextWrapper) {
                    context = context.baseContext
                    continue
                }
                context = null
            }
            return null
        }

    init {
        val a =
            context.obtainStyledAttributes(attributeSet, R.styleable.FullScreenCoordinatorLayout)
        try {
            if (a.hasValue(R.styleable.FullScreenCoordinatorLayout_android_statusBarColor)) {
                statusBarColor = a.getColor(
                    R.styleable.FullScreenCoordinatorLayout_android_statusBarColor,
                    Color.TRANSPARENT
                )
            }
            if (a.hasValue(R.styleable.FullScreenCoordinatorLayout_android_navigationBarColor)) {
                navigationBarColor = a.getColor(
                    R.styleable.FullScreenCoordinatorLayout_android_navigationBarColor,
                    Color.TRANSPARENT
                )
            }
            if (a.hasValue(R.styleable.FullScreenCoordinatorLayout_consumeStatusBarHeight)) {
                consumeStatusBarHeight = a.getBoolean(
                    R.styleable.FullScreenCoordinatorLayout_consumeStatusBarHeight,
                    false
                )
            }
            if (a.hasValue(R.styleable.FullScreenCoordinatorLayout_consumeNavigationBarHeight)) {
                consumeNavigationBarHeight = a.getBoolean(
                    R.styleable.FullScreenCoordinatorLayout_consumeNavigationBarHeight,
                    false
                )
            }
            if (isInEditMode) {
                editModeStatusBarHeight = a.getDimensionPixelOffset(
                    R.styleable.FullScreenCoordinatorLayout_editModeStatusBarHeight,
                    0
                )
                editModeNavigationBarHeight = a.getDimensionPixelOffset(
                    R.styleable.FullScreenCoordinatorLayout_editModeNavigationBarHeight,
                    0
                )
            }
        } finally {
            a.recycle()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        ViewCompat.setOnApplyWindowInsetsListener(this, onApplyWindowInsetsListener)
    }

    override fun getPaddingTop(): Int {
        val superPaddingTop = super.getPaddingTop()
        if (consumeStatusBarHeight || isInEditMode) {
            return superPaddingTop + statusBarHeight
        }
        return superPaddingTop
    }

    override fun getPaddingBottom(): Int {
        val superPaddingBottom = super.getPaddingBottom()
        if (consumeNavigationBarHeight || isInEditMode) {
            return superPaddingBottom + navigationBarHeight
        }
        return superPaddingBottom
    }

    override fun onViewAdded(child: View?) {
        super.onViewAdded(child)
        if (isInEditMode) {
            if (child != editModeStatusBarView || child != editModeNavigationBarView) {
                editModeStatusBarView.bringToFront()
                editModeNavigationBarView.bringToFront()
            }
        }
    }

    override fun onLayoutChild(child: View, layoutDirection: Int) {
        when (child) {
            editModeStatusBarView -> {
                child.layout(paddingLeft, paddingTop, width - paddingRight, statusBarHeight)
            }
            editModeNavigationBarView -> {
                child.layout(
                    paddingLeft,
                    height - navigationBarHeight,
                    width - paddingRight,
                    height
                )
            }
            else -> {
                super.onLayoutChild(child, layoutDirection)
            }
        }

    }
}