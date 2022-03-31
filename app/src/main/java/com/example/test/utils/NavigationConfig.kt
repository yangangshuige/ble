package com.example.test.utils

import android.app.Activity
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import com.example.test.R

/**
 * CreatedBy    xiaodong liu(mike)
 * CreateDate   6/1/21
 * Navigation配置
 */
object NavigationConfig {
    private const val mainNavHostViewId = R.id.nav_host_fragment_container

    /**
     * 寻找主NavController
     */
    @JvmStatic
    fun findNavController(activity: Activity?): NavController? {
        activity ?: return null

        return Navigation.findNavController(activity, mainNavHostViewId)
    }

    /**
     * 寻找主NavController
     */
    @JvmStatic
    fun findNavControllerInActivity(activity: FragmentActivity?): NavController? {
        activity ?: return null
        val navHostFragment =
            activity.supportFragmentManager.findFragmentById(mainNavHostViewId) as NavHostFragment
        return navHostFragment.navController
    }
}