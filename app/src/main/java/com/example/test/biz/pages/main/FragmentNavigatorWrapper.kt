package com.example.test.biz.pages.main

import android.os.Bundle
import androidx.navigation.NavDestination
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import androidx.navigation.fragment.FragmentNavigator
import com.example.test.R

/**
 * CreatedBy    xiaodong liu(mike)
 * CreateDate   4/24/21
 *
 */
@Navigator.Name("fragment")
class FragmentNavigatorWrapper(private val navigator: FragmentNavigator)
    : Navigator<FragmentNavigator.Destination>() {
    override fun navigate(
        destination: FragmentNavigator.Destination,
        args: Bundle?,
        navOptions: NavOptions?,
        navigatorExtras: Extras?
    ): NavDestination? {
        return navigator.navigate(destination, args,
            if (destination.id == R.id.fragmentMain) {
                navOptions
            } else {
                wrapNavOptions(navOptions)
            },
            navigatorExtras)
    }

    override fun createDestination(): FragmentNavigator.Destination {
        return navigator.createDestination()
    }

    override fun popBackStack(): Boolean {
        return navigator.popBackStack()
    }

    private fun wrapNavOptions(navOptions: NavOptions?): NavOptions {
        val enterAnim = navOptions?.enterAnim?: -1
        val exitAnim = navOptions?.exitAnim?: -1
        val popEnterAnim = navOptions?.popEnterAnim?: -1
        val popExitAnim = navOptions?.popExitAnim?: -1

        return NavOptions.Builder()
            .setEnterAnim(if (enterAnim == -1) R.anim.slide_in_right else enterAnim)
            .setExitAnim(if (exitAnim == -1) R.anim.slide_out_left else exitAnim)
            .setPopEnterAnim(if (popEnterAnim == -1) R.anim.slide_in_left else popEnterAnim)
            .setPopExitAnim(if (popExitAnim == -1) R.anim.slide_out_right else popExitAnim)
            .setLaunchSingleTop(navOptions?.shouldLaunchSingleTop()?: false)
            .build()
    }
}