package com.example.test.widget

import android.app.AlertDialog
import android.content.Context
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import com.example.test.R
import java.lang.Exception

object LoadingDialogHelper {
    private var mLoadingManager: LoadingManager? = null

    fun showLoading(context: Context?, title: String = "正在加载") {
        if (mLoadingManager != null && mLoadingManager!!.isShowing()) {
            return
        }

        mLoadingManager = LoadingManager(context, title)
        mLoadingManager?.apply {
            this.show()
        }
    }

    fun hideLoading(context: Context?) {
        mLoadingManager?.apply {
            this.dismiss()
        }
        mLoadingManager = null
    }

    internal class LoadingManager(val context: Context?, private val message: String?) {
        private var mDialog: AlertDialog? = null
        fun isShowing(): Boolean = mDialog?.isShowing ?: false

        fun show() {
            if (context == null) return

            if (mDialog != null && mDialog!!.isShowing) {
                return
            }
            val loadingView = LoadingView(context).apply { setMessage(message) }
            mDialog = AlertDialog.Builder(context, R.style.PlrDialogNoBg)
                .setCancelable(false)
                .setView(loadingView)
                .show()
            // 去除默认默认背景
            if (mDialog != null && mDialog!!.window != null) {
                mDialog!!.window!!.setBackgroundDrawable(null)
                val window = mDialog!!.window
                window!!.decorView.setPadding(0, 0, 0, 0)
                val layoutParams = window.attributes
                layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT
                layoutParams.gravity = Gravity.CENTER
                window.attributes = layoutParams
            }
        }

        fun dismiss() {
            if (context == null) {
                return
            }

            try {
                // TODO 查找真正的原因修复这个问题
                //  java.lang.IllegalArgumentException: View=DecorView@c3f104b[DetailActivity] not attached to window manager
                mDialog?.dismiss()
                mDialog = null
            } catch (e: Exception) {
                // ignore
            }
        }
    }
}