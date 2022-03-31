package com.example.test.recyclerView

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.TypedValue
import android.view.View
import androidx.annotation.IntDef
import androidx.recyclerview.widget.RecyclerView

/**
 *
 * RecyclerView列表项的分割线装饰实现
 * 特性：<p>
 * 1、支持横向和纵向
 * 2、支持起始和结束边距（竖直方向边距在左右；水平方向边距在上下）
 * 竖直方向分割线在每一项的下面添加
 * 水平方向分钟线在每一项的右面添加
 */
class LineSeparatorDecoration private constructor(
    @Orientation val orientation: Int,
    val sizeInDp: Float,
    val color: Int,
    val startMarginInDp: Float,
    val endMarginInDp: Float
) : RecyclerView.ItemDecoration() {
    @IntDef(RecyclerView.HORIZONTAL, RecyclerView.VERTICAL)
    @Retention(AnnotationRetention.SOURCE)
    annotation class Orientation

    private var mSize: Int = SIZE_UNDEFINED

    /**
     * 分割线的大小（Pixel）
     */
    val size: Int
        get() {
            return mSize
        }

    private val mPaint: Paint = Paint()

    private var mStartMargin: Int = 0
    private var mEndMargin: Int = 0

    /**
     * 分割线开始边距大小（Pixel）
     */
    val startMargin: Int
        get() {
            return mStartMargin
        }

    /**
     * 分割线结束边距大小（Pixel）
     */
    val endMargin: Int
        get() {
            return mEndMargin
        }

    /**
     * 构建一个LineItemSeparator
     */
    class Builder {
        private var mSizeInDp: Float = 0.0f
        private var mOrientation: Int = RecyclerView.HORIZONTAL
        private var mColor: Int = Color.BLACK

        private var mStartMarginInDp: Float = 0.0f
        private var mEndMarginInDp: Float = 0.0f

        /**
         * 指定分割线的大小（dp）
         * @param dpSize 分割线在大小
         * @return Builder自身
         */
        fun separatorSizeInDp(dpSize: Float): Builder {
            mSizeInDp = dpSize
            return this
        }

        /**
         * 起始边距（Dp）
         * @param dpSize DP value
         * @return Builder自身
         */
        fun startMarginInDp(dpSize: Float): Builder {
            mStartMarginInDp = dpSize
            return this
        }

        /**
         * 结束边距（Dp）
         * 竖直方向边距在左右；水平方向边距在上下
         * @param dpSize DP value
         * @return Builder自身
         */
        fun endMarginInDp(dpSize: Float): Builder {
            mEndMarginInDp = dpSize
            return this
        }

        /**
         * 指定分割线的方向
         * 竖直方向边距在左右；水平方向边距在上下
         * @param orientation 分割线的方向
         * @return Builder自身
         */
        fun separatorOrientation(@Orientation orientation: Int): Builder {
            this.mOrientation = orientation
            return this
        }

        /**
         * 指定分割线的颜色
         * @param color 颜色值
         * @return Builder自身
         */
        fun separatorColor(color: Int): Builder {
            this.mColor = color
            return this
        }

        /**
         * 指定分割线的颜色
         * @param color 颜色16进制值
         * @return Builder自身
         */
        fun separatorColor(color: String): Builder {
            return separatorColor(Color.parseColor(color))
        }

        /**
         * 构建LineItemSeparator实例
         */
        fun build(): LineSeparatorDecoration {
            return LineSeparatorDecoration(
                mOrientation,
                mSizeInDp, mColor, mStartMarginInDp, mEndMarginInDp
            )
        }
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(c, parent, state)

        // 绘制分割线
        // 竖直方向分割线在每一项的下面添加
        // 水平方向分钟线在每一项的右面添加
        val itemCount = parent.childCount
        for (index in 0 until itemCount) {
            val child = parent.getChildAt(index) ?: continue

            drawLineSeparator(child, c)
        }
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)

        ensureSize(parent.context)

        // 竖直方向分割线在每一项的下面添加
        // 水平方向分钟线在每一项的右面添加
        if (orientation == RecyclerView.VERTICAL) {
            outRect.right = mSize
        } else {
            outRect.bottom = mSize
        }
    }

    /**
     * 绘制视图分割线
     * @param child 视图
     * @param canvas 画布
     */
    private fun drawLineSeparator(child: View, canvas: Canvas) {
        val storeCount = canvas.save()

        val startX: Int
        val startY: Int
        val stopX: Int
        val stopY: Int

        val halfSize = mSize.shr(1)

        if (orientation == RecyclerView.HORIZONTAL) {
            startX = child.left + mStartMargin
            startY = child.bottom + halfSize
            stopX = child.right - mEndMargin
            stopY = startY
        } else {
            startX = child.right + halfSize
            startY = child.top + mStartMargin
            stopX = startX
            stopY = child.bottom - mEndMargin
        }

        canvas.drawLine(
            startX.toFloat(), startY.toFloat(),
            stopX.toFloat(), stopY.toFloat(), mPaint
        )

        canvas.restoreToCount(storeCount)
    }

    /**
     * 确保将Dp值转换为屏幕像素值
     * @param context 应用上下文
     */
    private fun ensureSize(context: Context) {
        if (mSize == SIZE_UNDEFINED) {
            val metrics = context.resources.displayMetrics

            mSize = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, sizeInDp, metrics
            ).toInt()
            mPaint.strokeWidth = mSize.toFloat()
            mPaint.color = color

            mStartMargin = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, startMarginInDp, metrics
            ).toInt()
            mEndMargin = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, endMarginInDp, metrics
            ).toInt()
        }
    }

    companion object {
        private const val SIZE_UNDEFINED = -1
    }
}