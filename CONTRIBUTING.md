Looking to report a bug or suggest a feature? Please use the [issue tracker](https://github.com/IMSharman/archive/issues) or check the [README](https://github.com/IMSharman/archive#readme).

---

Thanks for your interest in contributing to **Archive**!

# Code Contributions

Pull requests are welcome!

If you're interested in taking on [an open issue](https://github.com/IMSharman/archive/issues), please comment on it so others are aware. You do not need to ask for permission or assignment.

## Prerequisites

Before starting, familiarity with the following technologies is expected:

- [Android development](https://developer.android.com/) (Jetpack Compose, Kotlin Coroutines, Gradle)
- [Kotlin](https://kotlinlang.org/)

### Tools

- [Android Studio](https://developer.android.com/studio)
- JDK 17
- An emulator or physical Android device (Android 8.0+) for testing changes

### Local Verification

Before opening a pull request, please verify formatting, builds, and unit tests:

```bash
./gradlew spotlessCheck
./gradlew testReleaseUnitTest
./gradlew assembleDebug
```

# Forks

Forks are allowed so long as they abide by [the project's LICENSE](https://github.com/IMSharman/archive/blob/main/LICENSE).

When creating a fork, remember to:

- Avoid confusion with the main app:
    - Change the app name and app icon
    - Change or disable the [app update checker](https://github.com/IMSharman/archive/blob/main/app/src/main/java/eu/kanade/tachiyomi/data/updater/AppUpdateChecker.kt)
- Avoid installation conflicts:
    - Change the `applicationId` in [`app/build.gradle.kts`](https://github.com/IMSharman/archive/blob/main/app/build.gradle.kts)
- Avoid sending telemetry to another project's services:
    - Replace [`google-services.json`](https://github.com/IMSharman/archive/blob/main/app/src/standard/google-services.json) or ACRA config if you enable analytics or crash reporting
