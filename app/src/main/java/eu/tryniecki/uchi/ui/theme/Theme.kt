package eu.tryniecki.uchi.ui.theme

import androidx.compose.runtime.Composable
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme

/** TV launchers are dark-only; light mode on a TV in a dark room is hostile. */
private val UchiColors = darkColorScheme(
    primary = Amber,
    onPrimary = Ink,
    secondary = Slate,
    background = Ink,
    surface = Ink,
    surfaceVariant = Graphite,
)

@Composable
fun UchiTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = UchiColors,
        typography = Typography,
        content = content,
    )
}
