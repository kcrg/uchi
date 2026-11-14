# Uchi — lightweight Android TV launcher

Home-screen replacement for Android TV, Android 10+ (minSdk 29). Kotlin + Jetpack Compose for TV (`androidx.tv:tv-material`).
Main test device: Xiaomi Mi TV (`MiTV-AESP0`), Android 10 / API 29, weak ARM Cortex-A9 CPU, so performance on low-end hardware matters most.

## Commands

```sh
./gradlew :app:assembleDebug                # debug APK  (appId eu.tryniecki.uchi.debug)
./gradlew :app:assembleRelease              # R8-optimized, signed with the debug key (appId eu.tryniecki.uchi)
./gradlew :app:testDebugUnitTest            # JVM unit tests
./gradlew :app:lintDebug

adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n eu.tryniecki.uchi.debug/eu.tryniecki.uchi.MainActivity
adb exec-out screencap -p > screen.png      # visual check
adb shell am start -W -n eu.tryniecki.uchi/.MainActivity   # cold start time (use the release build)
adb shell input keyevent KEYCODE_DPAD_RIGHT|KEYCODE_DPAD_CENTER|KEYCODE_HOME|KEYCODE_BACK
```

Debug Compose runs unoptimized and visibly janks on the TV. Judge performance on the **release** build only.
Release cold start right now: about 650–900 ms.

## Architecture

Single module `:app`, package `eu.tryniecki.uchi`:

- `UchiApp`: Application. Owns `AppContainer` and the Coil `ImageLoader`. StrictMode is on in debug builds.
- `di/AppContainer`: manual DI with lazy singletons and `appScope`. **No Hilt/Koin.**
- `data/apps/AppRepository`: queries `LEANBACK_LAUNCHER` and `LAUNCHER` activities, observes package broadcasts, exposes `StateFlow<List<LaunchableApp>?>` (null = still loading).
- `ui/image/AppArtwork*`: Coil 3 `Fetcher` + `Keyer` for app banners/icons, rasterized to the requested size. Memory cache only, no disk cache, no network module.
- `ui/home/`: `HomeViewModel` (`HomeUiState`) + `HomeScreen` (grid of 16:9 cards).
- `ui/theme/`: dark-only TV theme. Keep `Color.kt` in sync with `res/values/colors.xml`.

Manifest: `MainActivity` is `HOME` + `DEFAULT` (launcher role) and also `LEANBACK_LAUNCHER`, `singleTask`, landscape.
BACK on the home screen is swallowed. Package visibility goes through `<queries>`, not `QUERY_ALL_PACKAGES`.

## Rules

- **Lightweight first.** Before adding a dependency, ask whether a few lines of platform code would do. Not allowed without a strong reason: AppCompat, Material (phone) components, Hilt, Retrofit/OkHttp, Coil network modules.
- UI uses `androidx.tv.material3` components (Card, Surface, Text, ...). Don't mix in `androidx.compose.material3`. Use `androidx.compose.foundation` lazy lists/grids (tv-foundation lazy lists are deprecated).
- Everything must work with **D-pad only**: visible focus, sensible initial focus, no touch-only interactions.
- Keep TV overscan-safe margins: 48dp horizontal, 27dp vertical.
- Never touch `PackageManager` or disk on the main thread. Use the repository, `Dispatchers.IO`, or a Coil fetcher.
- State lives in ViewModels as `StateFlow`. Collect with `collectAsStateWithLifecycle`. Settings go in DataStore Preferences (already a dependency).
- Versions live only in `gradle/libs.versions.toml`.
- Verify UI changes on the real TV (install + screencap), not just by compiling.
