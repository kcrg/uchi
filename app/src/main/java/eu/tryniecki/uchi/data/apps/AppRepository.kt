package eu.tryniecki.uchi.data.apps

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

data class LaunchableApp(
    val packageName: String,
    val label: String,
    val component: ComponentName,
    /** True when the app exposes a LEANBACK_LAUNCHER activity (proper TV app). */
    val isTvApp: Boolean,
)

interface AppRepository {
    /** Null until the first load completes. */
    val apps: StateFlow<List<LaunchableApp>?>
    fun launchIntent(app: LaunchableApp): Intent
    /** Banner (TV) or icon. Call off the main thread. */
    fun loadArtwork(app: LaunchableApp): Drawable?
}

class PackageManagerAppRepository(
    private val context: Context,
    scope: CoroutineScope,
) : AppRepository {

    private val pm: PackageManager = context.packageManager

    override val apps: StateFlow<List<LaunchableApp>?> = packageChanges()
        .onStart { emit(Unit) }
        .map { queryApps() }
        .flowOn(Dispatchers.IO)
        .stateIn(scope, SharingStarted.Eagerly, null)

    override fun launchIntent(app: LaunchableApp): Intent =
        Intent(Intent.ACTION_MAIN)
            .addCategory(if (app.isTvApp) Intent.CATEGORY_LEANBACK_LAUNCHER else Intent.CATEGORY_LAUNCHER)
            .setComponent(app.component)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)

    override fun loadArtwork(app: LaunchableApp): Drawable? = runCatching {
        if (app.isTvApp) {
            pm.getActivityBanner(app.component) ?: pm.getApplicationBanner(app.packageName)
                ?: pm.getActivityIcon(app.component)
        } else {
            pm.getActivityIcon(app.component)
        }
    }.getOrNull()

    private fun queryApps(): List<LaunchableApp> {
        val tv = query(Intent.CATEGORY_LEANBACK_LAUNCHER)
        val tvPackages = tv.mapTo(HashSet()) { it.activityInfo.packageName }
        val mobileOnly = query(Intent.CATEGORY_LAUNCHER)
            .filter { it.activityInfo.packageName !in tvPackages }
        return (tv.map { it.toApp(isTvApp = true) } + mobileOnly.map { it.toApp(isTvApp = false) })
            .filter { it.packageName != context.packageName }
            .distinctBy { it.component }
            .sortedBy { it.label.lowercase() }
    }

    private fun query(category: String): List<ResolveInfo> =
        pm.queryIntentActivities(Intent(Intent.ACTION_MAIN).addCategory(category), 0)

    private fun ResolveInfo.toApp(isTvApp: Boolean) = LaunchableApp(
        packageName = activityInfo.packageName,
        label = loadLabel(pm).toString(),
        component = ComponentName(activityInfo.packageName, activityInfo.name),
        isTvApp = isTvApp,
    )

    private fun packageChanges() = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                trySend(Unit)
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addDataScheme("package")
        }
        ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
        awaitClose { context.unregisterReceiver(receiver) }
    }
}
