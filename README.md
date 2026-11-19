<div align="center">

<a href="https://github.com/IMSharman/archive">
    <img src="./.github/assets/archive_logo.png" alt="Archive logo" title="Archive logo" width="100"/>
</a>

# Archive

### Full-featured anime player and manga reader for Android
Discover, watch, and read anime, manga, and comics in one unified app.

[![GitHub release](https://img.shields.io/github/v/release/IMSharman/archive?label=Release&labelColor=06599d&color=043b69)](https://github.com/IMSharman/archive/releases)
[![GitHub downloads](https://img.shields.io/github/downloads/IMSharman/archive/total?label=Downloads&labelColor=27303D&color=0D1117&logo=github&logoColor=FFFFFF&style=flat)](https://github.com/IMSharman/archive/releases)
[![CI](https://img.shields.io/github/actions/workflow/status/IMSharman/archive/build_push.yml?labelColor=27303D)](https://github.com/IMSharman/archive/actions/workflows/build_push.yml)
[![License: Apache-2.0](https://img.shields.io/github/license/IMSharman/archive?labelColor=27303D&color=818cf8)](/LICENSE)

</div>

## Download

Get the latest APK from the **[Releases](https://github.com/IMSharman/archive/releases)** page.

- **Universal** (`archive-vX.Y.Z.apk`) works on all supported devices.
- **ABI-specific** (`arm64-v8a`, `armeabi-v7a`, `x86`, `x86_64`) builds offer smaller download sizes; most modern phones use `arm64-v8a`.
- *Requires Android 8.0 (Oreo) or higher.*

## Features

* **Configurable Video Player**: Built on `mpv-android` (`libmpv`) with hardware acceleration, audio/subtitle track selection, intro/outro skip integration, gesture controls, thumbnail seek previews, and picture-in-picture (PiP).
* **Torrent Streaming**: Stream anime and videos directly via embedded `TorrServer`.
* **Configurable Manga Reader**: Multiple viewer modes (Paged LTR/RTL, Webtoon, Continuous Vertical), parallel page preloading with Wi-Fi awareness, color filters, crop borders, and dual-page split.
* **Extension Ecosystem**:
  * Support for `.pb` extension repository indexes and `index_v2`.
  * Compatibility with `extensions-lib` v16/v17 sources (including `getMangaUpdate` and `memo` fields).
* **Built-in WebView & Resilient Networking**: Integrated WebView popup/ad blocker with a live menu toggle, plus fail-safe DNS resolution (System DNS with automatic DoH failover) and connection-reset retry handling.
* **Tracker Integration**: Synchronize your progress with [AniList](https://anilist.co/), [MyAnimeList](https://myanimelist.net/), [Kitsu](https://kitsu.app/), [MangaUpdates](https://mangaupdates.com), [Shikimori](https://shikimori.one), [Bangumi](https://bgm.tv/), and [Simkl](https://simkl.com/).
* **Local Media**: Read CBZ, ZIP, and EPUB files and play local video files with `ComicInfo.xml` metadata parsing.
* **Library & Backups**: Organize entries into custom categories with automated update checks, offline download indexing, and local/cloud-synced backups.
* **Customization**: Material 3 dynamic theming with light, dark, and AMOLED modes.

## Building from Source

Requires **JDK 17** and the **Android SDK** (compile/target SDK 36, min SDK 26):

```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease

# Code formatting & unit tests
./gradlew spotlessCheck testReleaseUnitTest
```

## Issues & Contributing

Contributions, bug reports, and feature requests are welcome—please see [CONTRIBUTING.md](CONTRIBUTING.md) and the [issue tracker](https://github.com/IMSharman/archive/issues).

## Disclaimer

The developer(s) of **Archive** have no affiliation with any content providers, and this application hosts zero content. All content is accessed via user-installed third-party extensions and local files.

## Acknowledgments & License

Archive incorporates open-source components from `mpv-android`, `TorrServer`, and the Tachiyomi/Mihon/Aniyomi open-source ecosystem.

```
Copyright © 2015 Javier Tomás
Copyright © 2024 Mihon Open Source Project
Copyright © 2024 Aniyomi Open Source Project
Copyright © 2026 Archive Open Source Project

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
