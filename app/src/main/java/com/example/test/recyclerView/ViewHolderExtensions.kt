package com.example.test.recyclerView

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Resources
import android.view.View
import androidx.annotation.IdRes
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView


/**
 * 类似　Activity.findViewById(int)
 * 后期可以使用ViewBinding代替
 */
fun <T : View> RecyclerView.ViewHolder.findViewById(@IdRes viewId: Int): T {
    return itemView.findViewById(viewId)
}

/**
 * ViewHolder所属RecyclerView所在的宿主Activity
 * 必需和FragmentActivity用其子类一起使用
 * <p>
 *     ViewHolder和RecyclerView建立连接后，此Activity一定存在
 */
val RecyclerView.ViewHolder.activityHost: FragmentActivity get() {
    var context = itemView.context
    while (null != context) {
        if (context is FragmentActivity) {
            return context
        }

        if (context !is ContextWrapper) {
            break
        }

        context = context.baseContext
    }

    throw IllegalStateException("ViewHolder[${this.hashCode()}] should use with FragmentActivity")
}

/**
 * ViewHolder　View的Resources
 */
val RecyclerView.ViewHolder.resources: Resources get() {
    return itemView.resources
}

/**
 * ViewHolder　View的Context
 */
val RecyclerView.ViewHolder.context: Context get() {
    return itemView.context
}

