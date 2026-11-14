package eu.tryniecki.uchi

import android.app.Application
import android.os.StrictMode
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.memory.MemoryCache
import eu.tryniecki.uchi.di.AppContainer
import eu.tryniecki.uchi.ui.image.AppArtworkFetcher
import eu.tryniecki.uchi.ui.image.AppArtworkKeyer

class UchiApp : Application(), SingletonImageLoader.Factory {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) enableStrictMode()
        container = AppContainer(this)
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader =
        ImageLoader.Builder(context)
            .components {
                add(AppArtworkKeyer())
                add(AppArtworkFetcher.Factory(container.appRepository))
            }
            // Artwork comes from PackageManager, never the network: memory cache only.
            .memoryCache { MemoryCache.Builder().maxSizePercent(context, 0.15).build() }
            .diskCache(null)
            .build()

    private fun enableStrictMode() {
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build()
        )
        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder().detectLeakedClosableObjects().detectActivityLeaks().penaltyLog().build()
        )
    }
}
