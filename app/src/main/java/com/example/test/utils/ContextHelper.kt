package com.example.test.utils

import android.content.Context
import android.content.ContextWrapper
import android.view.View
import androidx.core.content.pm.PackageInfoCompat
import androidx.fragment.app.FragmentActivity

/**
 * CreatedBy    xiaodong liu(mike)
 * CreateDate   4/20/21
 * 应用上下文帮助工具类
 */
class ContextHelper private constructor() {
    companion object{
        /**
         * 在Context继承关系中查找FragmentActivity
         * @param context Context
         */
        @JvmStatic
        fun findFragmentActivityContext(context: Context): FragmentActivity? {
            var baseContext: Context? = context
            while (null != baseContext) {
                if (baseContext is FragmentActivity) return baseContext

                baseContext = if (baseContext is ContextWrapper) {
                    baseContext.baseContext
                } else {
                    null
                }
            }

            return null
        }

        /**
         * 查找View所在的FragmentActivity
         * @param view View
         */
        @JvmStatic
        fun findFragmentActivityContext(view: View): FragmentActivity? {
            return findFragmentActivityContext(view.context)
        }

        /**
         * 获取App版本名称
         * @param context app context
         *
         * @return App的版本名称
         */
        @JvmStatic
        fun getAppVersionName(context: Context): String {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)

            return packageInfo.versionName
        }

        /**
         * 获取App版本号
         * @param context app context
         *
         * @return App的长版本号
         */
        @JvmStatic
        fun getAppVersionCode(context: Context): Long {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)

            return PackageInfoCompat.getLongVersionCode(packageInfo)
        }
    }
}