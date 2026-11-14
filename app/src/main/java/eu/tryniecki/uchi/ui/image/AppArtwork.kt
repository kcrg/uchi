package eu.tryniecki.uchi.ui.image

import androidx.core.graphics.drawable.toBitmap
import coil3.ImageLoader
import coil3.asImage
import coil3.decode.DataSource
import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import coil3.fetch.ImageFetchResult
import coil3.key.Keyer
import coil3.request.Options
import coil3.size.pxOrElse
import eu.tryniecki.uchi.data.apps.AppRepository
import eu.tryniecki.uchi.data.apps.LaunchableApp

/** Coil model for an app's banner (TV apps) or icon (mobile apps). */
data class AppArtwork(val app: LaunchableApp)

/**
 * Loads artwork via [AppRepository] on Coil's fetcher dispatcher and rasterizes it to the
 * requested size, so the memory cache holds small bitmaps instead of full drawables.
 */
class AppArtworkFetcher(
    private val data: AppArtwork,
    private val options: Options,
    private val repository: AppRepository,
) : Fetcher {

    override suspend fun fetch(): FetchResult? {
        val drawable = repository.loadArtwork(data.app) ?: return null
        val w = options.size.width.pxOrElse { drawable.intrinsicWidth.coerceIn(1, MAX_PX) }
        val h = options.size.height.pxOrElse { drawable.intrinsicHeight.coerceIn(1, MAX_PX) }
        return ImageFetchResult(
            image = drawable.toBitmap(w.coerceIn(1, MAX_PX), h.coerceIn(1, MAX_PX)).asImage(),
            isSampled = true,
            dataSource = DataSource.DISK,
        )
    }

    class Factory(private val repository: AppRepository) : Fetcher.Factory<AppArtwork> {
        override fun create(data: AppArtwork, options: Options, imageLoader: ImageLoader): Fetcher =
            AppArtworkFetcher(data, options, repository)
    }

    private companion object {
        const val MAX_PX = 640
    }
}

/** Memory-cache key (Coil appends the request size itself). */
class AppArtworkKeyer : Keyer<AppArtwork> {
    override fun key(data: AppArtwork, options: Options): String =
        "app:${data.app.component.flattenToShortString()}:${data.app.isTvApp}"
}
