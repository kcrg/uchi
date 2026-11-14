package eu.tryniecki.uchi.di

import android.content.Context
import eu.tryniecki.uchi.data.apps.AppRepository
import eu.tryniecki.uchi.data.apps.PackageManagerAppRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Manual DI. Kept deliberately simple: no Hilt/Koin, everything is a lazy singleton.
 */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    /** App-lifetime scope for hot flows shared between screens. */
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val appRepository: AppRepository by lazy {
        PackageManagerAppRepository(appContext, appScope)
    }
}
