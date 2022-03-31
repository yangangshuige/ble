package com.example.test.biz.pages.main

import androidx.activity.OnBackPressedCallback
import androidx.navigation.NavController
import androidx.navigation.Navigator
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.NavHostFragment
import com.example.test.R

class MainNavHostFragment : NavHostFragment() {
    private val backPressedCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            navController.popBackStack()
        }
    }

    override fun onCreateNavController(navController: NavController) {
        super.onCreateNavController(navController)

        try {
            val fragmentNavigator = navController.navigatorProvider
                .getNavigator<FragmentNavigator>(S_FRAGMENT_NAVIGATOR_NAME)

            navController.navigatorProvider
                .addNavigator(
                    S_FRAGMENT_NAVIGATOR_NAME,
                    FragmentNavigatorWrapper(
                        fragmentNavigator
                    )
                )
        } catch (e: IllegalStateException) {
            // navigator not found, no op
        }

        navController.setGraph(R.navigation.main_nav_graph)
        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            // 主页不处理回退事件
            backPressedCallback.isEnabled = destination.id != R.id.fragmentMain
        }

        requireActivity().onBackPressedDispatcher.addCallback(this, backPressedCallback)
    }

    companion object {
        private fun getNameForNavigator(navigatorClass: Class<out Navigator<*>?>): String {
            val annotation =
                navigatorClass.getAnnotation(
                    Navigator.Name::class.java
                )

            return annotation?.value ?: ""
        }

        private val S_FRAGMENT_NAVIGATOR_NAME: String by lazy {
            getNameForNavigator(FragmentNavigator::class.java)
        }
    }
}