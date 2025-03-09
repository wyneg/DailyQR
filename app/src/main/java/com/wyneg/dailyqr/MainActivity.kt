package com.wyneg.dailyqr

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.material3.Surface
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.wyneg.dailyqr.ui.theme.QRProductTheme
import com.wyneg.dailyqr.ui.theme.screens.ButtonsScreen
import com.wyneg.dailyqr.ui.theme.screens.FourthScreen
import com.wyneg.dailyqr.ui.theme.screens.MainScreen
import com.wyneg.dailyqr.ui.theme.screens.QRScreen
import com.wyneg.dailyqr.ui.theme.screens.SecondScreen
import com.wyneg.dailyqr.ui.theme.screens.ThirdScreen

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val context = applicationContext

        enableEdgeToEdge()
        setContent {
            QRProductTheme {
                val navController = rememberNavController()

                val density = LocalDensity.current

                Surface {
                    NavHost(
                        navController = navController,
                        startDestination = "splash",
                        enterTransition = {
                            expandHorizontally {
                                with(density) { -40.dp.roundToPx() }
                            }
                        },
                        exitTransition = {
                            shrinkHorizontally {
                                with(density) { -40.dp.roundToPx() }
                            }
                        }
                    ) {
                        composable("splash") {
                            ButtonsScreen(navController, context)
                        }

                        composable("main") {
                            MainScreen(navController, context)
                        }

                        composable("qr/{producto}/{precio}") { backStackEntry ->
                            QRScreen(
                                navController,
                                context,
                                producto = backStackEntry.arguments?.getString("producto"),
                                precio = backStackEntry.arguments?.getString("precio")
                            )
                        }

                        composable("second") {
                            SecondScreen(navController, context)
                        }

                        composable("third") {
                            ThirdScreen(navController, context)
                        }

                        composable("fourth") {
                            FourthScreen(navController, context)
                        }
                    }
                }
            }
        }
    }
}
