package com.sumi.pockon.ui.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.colorResource
import androidx.navigation.Navigation
import com.sumi.pockon.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setContent {
            val lightColorScheme = lightColorScheme(
                primary = colorResource(id = R.color.primary),
                onPrimary = colorResource(id = R.color.onPrimary),
                primaryContainer = colorResource(id = R.color.primaryContainer),
                onPrimaryContainer = colorResource(id = R.color.onPrimary),
                secondary = colorResource(id = R.color.secondary),
                secondaryContainer = colorResource(id = R.color.secondaryContainer),
                tertiary = colorResource(id = R.color.tertiary),
                tertiaryContainer = colorResource(id = R.color.tertiaryContainer),
                outline = colorResource(id = R.color.light_gray),
                error = colorResource(id = R.color.red),
                errorContainer = colorResource(id = R.color.light_gray),
                background = colorResource(id = R.color.background)
            )
            MaterialTheme(
                colorScheme = lightColorScheme  // Light Mode Color Scheme
            ) {
                BottomNavigationBar(
                    movePinScreen = {
                        val navController = Navigation.findNavController(requireView())
                        navController.popBackStack()
                        navController.navigate(R.id.pinFragment)
                    },
                    moveLogInScreen = {
                        val navController = Navigation.findNavController(requireView())
                        navController.popBackStack()
                        navController.navigate(R.id.loginFragment)
                    }
                )
            }
        }
    }
}
