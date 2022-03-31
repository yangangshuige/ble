package com.didi.bike.applicationholder

import android.content.Context
import androidx.startup.Initializer

/**
 * CreatedBy    xiaodong liu(mike)
 * CreateDate   3/29/21
 * Android Application Context工具
 */
internal class AppContextHolderInitializer: Initializer<AppContextHolder> {
    override fun create(context: Context): AppContextHolder {
        AppContextHolder.initWithApplication(context)

        return AppContextHolder.instance!!
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }
}