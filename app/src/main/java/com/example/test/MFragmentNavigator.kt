package com.example.test

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavDestination
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import androidx.navigation.fragment.FragmentNavigator

@Navigator.Name("fragment")
class MFragmentNavigator(
    context: Context,
    fragmentManager: FragmentManager,
    containerId: Int,
) : FragmentNavigator(context,
    fragmentManager,
    containerId) {
    override fun navigate(
        destination: Destination,
        args: Bundle?,
        navOptions: NavOptions?,
        navigatorExtras: Navigator.Extras?,
    ): NavDestination? {
        val mNavOptions = if (destination.id == R.id.fragmentMain) {
            navOptions
        } else {
            wrapNavOptions(navOptions)
        }
        return super.navigate(destination, args, mNavOptions, navigatorExtras)
    }

    private fun wrapNavOptions(navOptions: NavOptions?): NavOptions {
        val enterAnim = navOptions?.enterAnim ?: -1
        val exitAnim = navOptions?.exitAnim ?: -1
        val popEnterAnim = navOptions?.popEnterAnim ?: -1
        val popExitAnim = navOptions?.popExitAnim ?: -1

        return NavOptions.Builder()
            .setEnterAnim(if (enterAnim == -1) R.anim.slide_in_right else enterAnim)
            .setExitAnim(if (exitAnim == -1) R.anim.slide_out_left else exitAnim)
            .setPopEnterAnim(if (popEnterAnim == -1) R.anim.slide_in_left else popEnterAnim)
            .setPopExitAnim(if (popExitAnim == -1) R.anim.slide_out_right else popExitAnim)
            .setLaunchSingleTop(navOptions?.shouldLaunchSingleTop() ?: false)
            .build()
    }
}