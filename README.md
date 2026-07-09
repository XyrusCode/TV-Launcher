# Xyrus TV

A native **Google TV launcher and Jellyfin media hub** for the Walmart **onn** box, written in
Kotlin with Jetpack Compose for TV.

Google TV's stock home only lists apps that declare a `LEANBACK_LAUNCHER` intent filter, so
sideloaded apps disappear — and store launchers can't replace the system home on these boxes.
Xyrus TV registers itself as the HOME activity and enumerates apps from **both** the leanback and
the standard launcher categories, bringing the hidden apps back. On top of that it's a media hub
(Steam Big Picture / Jellyfin style): your Jellyfin library sits up top, installed apps below.

## Features

- **Every app, including sideloaded ones** — merges `LEANBACK_LAUNCHER` + `LAUNCHER` results.
- **Jellyfin rows** — Continue Watching, Next Up, Recently Added, and your Libraries, via the
  official [Jellyfin Kotlin SDK](https://github.com/jellyfin/jellyfin-sdk-kotlin).
- **Playback handoff** — selecting an item deep-links into the official Jellyfin Android TV app
  (with graceful fallbacks — see [Notes](#notes)).
- **D-pad first** — Compose for TV cards with focus scaling; no touchscreen required.

## Building

Builds run in **GitHub Actions** (there is no local JDK requirement). Every push builds a signed
release APK and uploads it as the `tv-launcher-release-apks` artifact. To cut a versioned release:

1. Bump `versionCode` / `versionName` in [`app/build.gradle.kts`](app/build.gradle.kts).
2. Tag and push:
   ```bash
   git tag v1.0.0 && git push origin v1.0.0
   ```
   CI builds `app-release.apk` and attaches it to the GitHub Release.

The app is signed with a **shared, committed key** (`keystore/tvlauncher.jks`, PKCS12, all
passwords `tvlauncher`). It's intentionally public: fine for a personally self-distributed app,
not Play-grade. Because the key is stable, updates install straight over a previous install.

To build locally instead, install JDK 17 + the Android SDK and run `./gradlew assembleRelease`.

## Installing on the onn box

1. On the box: **Settings → System → About → Android TV OS build** (click 7×) to enable
   Developer options, then **Settings → System → Developer options → USB/Network debugging → On**.
2. From your computer:
   ```bash
   adb connect <onn-ip>:5555
   adb install -r app-release.apk
   ```

### Make it the default launcher

Google TV is sticky about the home app. Set it explicitly over adb:

```bash
adb shell cmd package set-home-activity xyruscode.tv.launcher/.MainActivity
```

Press the Home button — Xyrus TV should come up. To revert, run the same command with the stock
launcher's component (e.g. `com.google.android.apps.tv.launcherx/.MainActivity`).

## Connecting Jellyfin

On first launch, choose **Connect** (or **Settings → Connect**), enter your server address
(`http://192.168.x.x:8096`), username, and password. The session is stored locally; playback
opens the official Jellyfin app.

## Notes

- The Jellyfin **deep-link is undocumented** (per jellyfin-androidtv discussion #3452). If a future
  Jellyfin update breaks it, Xyrus TV falls back to simply opening the Jellyfin app, then to the
  Play Store if it isn't installed.
- `QUERY_ALL_PACKAGES` is required to enumerate sideloaded apps. This app is not published to Play.
- In-app playback (Media3/ExoPlayer) is intentionally out of scope for now.
