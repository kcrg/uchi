package eu.tryniecki.uchi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.tv.material3.Surface
import eu.tryniecki.uchi.ui.home.HomeScreen
import eu.tryniecki.uchi.ui.theme.UchiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // A home screen must not close on BACK.
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = Unit
        })
        setContent {
            UchiTheme {
                Surface(modifier = Modifier.fillMaxSize(), shape = RectangleShape) {
                    HomeScreen()
                }
            }
        }
    }
}
