package eu.tryniecki.uchi.ui.home

import android.content.ActivityNotFoundException
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.material3.Card
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import eu.tryniecki.uchi.data.apps.AppRepository
import eu.tryniecki.uchi.data.apps.LaunchableApp
import eu.tryniecki.uchi.ui.image.AppArtwork
import coil3.compose.AsyncImage

@Composable
fun HomeScreen(viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory)) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    when (val s = state) {
        HomeUiState.Loading -> Box(Modifier.fillMaxSize())
        is HomeUiState.Ready -> AppGrid(s.apps, viewModel.repository)
    }
}

@Composable
private fun AppGrid(apps: List<LaunchableApp>, repository: AppRepository) {
    val context = LocalContext.current
    LazyVerticalGrid(
        columns = GridCells.Fixed(5),
        // TV overscan-safe margins: 48dp horizontal, 27dp vertical.
        contentPadding = PaddingValues(horizontal = 48.dp, vertical = 27.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        items(apps, key = { it.component.flattenToShortString() }) { app ->
            AppCard(
                app = app,
                onClick = {
                    try {
                        context.startActivity(repository.launchIntent(app))
                    } catch (_: ActivityNotFoundException) {
                        // App vanished between query and click; the package receiver will refresh the list.
                    }
                },
            )
        }
    }
}

@Composable
private fun AppCard(app: LaunchableApp, onClick: () -> Unit) {
    var failed by remember(app.component) { mutableStateOf(false) }
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f),
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            AsyncImage(
                model = AppArtwork(app),
                contentDescription = app.label,
                contentScale = if (app.isTvApp) ContentScale.Crop else ContentScale.Fit,
                onError = { failed = true },
                modifier = if (app.isTvApp) Modifier.fillMaxSize() else Modifier.fillMaxSize().padding(16.dp),
            )
            if (failed) {
                Text(
                    text = app.label,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(8.dp),
                )
            }
        }
    }
}
