# Uchi

Lightweight home-screen launcher for Android TV (Android 10+), built with Kotlin and Jetpack Compose for TV.

## Build

Requirements: JDK 25 (Gradle daemon toolchain) and Android SDK with platform 37.

```sh
./gradlew :app:assembleRelease
adb install -r app/build/outputs/apk/release/app-release.apk
```

To set it as the default home screen, press HOME on the TV and pick **Uchi**. Some vendor ROMs hide this picker;
in that case disable the stock launcher with `adb shell pm disable-user --user 0 <stock.launcher.package>`
(re-enable with `pm enable`).

See [CLAUDE.md](CLAUDE.md) for architecture and conventions.
