<div align="center">

<a href="https://github.com/IMSharman/archive">
    <img src="./.github/assets/logo.png" alt="Archive logo" title="Archive logo" width="100"/>
</a>

# Archive

### Full-featured anime player and manga reader for Android
Discover, watch, and read anime, manga, and comics on your Android device.

[![GitHub release](https://img.shields.io/github/v/release/IMSharman/archive?label=Release&labelColor=06599d&color=043b69)](https://github.com/IMSharman/archive/releases)
[![GitHub downloads](https://img.shields.io/github/downloads/IMSharman/archive/total?label=Downloads&labelColor=27303D&color=0D1117&logo=github&logoColor=FFFFFF&style=flat)](https://github.com/IMSharman/archive/releases)
[![CI](https://img.shields.io/github/actions/workflow/status/IMSharman/archive/build_push.yml?labelColor=27303D)](https://github.com/IMSharman/archive/actions/workflows/build_push.yml)
[![License: Apache-2.0](https://img.shields.io/github/license/IMSharman/archive?labelColor=27303D&color=818cf8)](/LICENSE)

## Overview

**Archive** is an enhanced Android reader and video player based on [Aniyomi](https://github.com/aniyomiorg/aniyomi) and [Mihon](https://github.com/mihonapp/mihon) (formerly [Tachiyomi](https://github.com/tachiyomiorg/tachiyomi)).

</div>

## Download

Get the latest version from the **[Releases](https://github.com/IMSharman/archive/releases)** page.

*Requires Android 8.0 (Oreo) or higher.*

## Features

* **Configurable Video Player**: Built on `mpv-android` (libmpv) with hardware acceleration, audio/subtitle selection, Anime Skip integration, gesture controls, and picture-in-picture (PiP).
* **Torrent Streaming**: Stream anime and videos directly via embedded [TorrServer](https://github.com/YouROK/TorrServer).
* **Configurable Manga Reader**: Multiple viewer modes (Paged LTR/RTL, Webtoon, Continuous Vertical), color filters, crop borders, and dual-page split.
* **Extension Ecosystem**:
  * Support for modern Mihon Protobuf (`.pb`) extension repository index format and `index_v2`.
  * Support for Anime extension-lib v17.
* **Tracker Integration**: Synchronize your progress with [AniList](https://anilist.co/), [MyAnimeList](https://myanimelist.net/), [Kitsu](https://kitsu.app/), [MangaUpdates](https://mangaupdates.com), [Shikimori](https://shikimori.one), [Bangumi](https://bgm.tv/), and [Simkl](https://simkl.com/).
* **Local Media**: Read CBZ, ZIP, EPUB files and play local video files with ComicInfo.xml metadata parsing.
* **Categories & Library Management**: Organize entries into custom categories with automated update checks.
* **Backups**: Create local or cloud-synced backups to keep your library safe.
* **Customization**: Material 3 dynamic theming with dark and light modes.

## Credits & Upstream

Archive is built upon the incredible work of the open-source community:

* [Aniyomi](https://github.com/aniyomiorg/aniyomi) - Anime player extensions, torrent support, and UI
* [Mihon](https://github.com/mihonapp/mihon) - Modern Tachiyomi fork & multi-repo extension store architecture
* [Tachiyomi](https://github.com/tachiyomiorg/tachiyomi) - The original open-source Android reader
* [mpv-android](https://github.com/mpv-android/mpv-android) - Video playback engine
* [TorrServer](https://github.com/YouROK/TorrServer) - Torrent streaming core

## Disclaimer

The developer(s) of this application have no affiliation with any content providers, and this application hosts zero content. All content is accessed via user-installed third-party extensions and local files.

## License

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
