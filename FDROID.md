# F-Droid inclusion

This app is not yet on F-Droid. Getting it there is a separate, non-blocking step that happens
outside this repository, whenever a maintainer chooses to do it — nothing about the app's
distribution via GitHub Releases depends on it.

## Why this app is already F-Droid-friendly

F-Droid builds every app itself, directly from source — it never accepts a pre-built APK. This
project was set up from the start to make that possible:

- **Open-source license** — MIT (see [LICENSE](LICENSE)).
- **No proprietary or Google-Play-Services dependencies.** Check `gradle/libs.versions.toml`:
  everything is AndroidX, Compose, Room, DataStore, and their transitive open-source deps. No
  Firebase, no `google-services.json`, no ads/analytics SDKs.
- **Reproducible build from a single command**: `./gradlew assembleRelease` (see the
  [Building from source](README.md#building-from-source) section of the README).
- **Semantic versioning** (`versionName` / incrementing `versionCode` in `app/build.gradle.kts`),
  which F-Droid's auto-update checker relies on.
- **AI-transparency notice** present in three places, consistently: the GitHub README, the
  in-app About screen, and the draft F-Droid metadata below — so it's visible regardless of
  where someone encounters the app.

## What's already prepared in this repo

- **`fastlane/metadata/android/{en-US,nl-NL,es-ES}/`** — title, short description, and full
  description in all three languages the app ships with. F-Droid (and other tools that follow
  the fastlane metadata convention) can pick these up directly from this repo.
- **`metadata/com.github.cookiesmartart.monopolybank.yml`** — a *draft* F-Droid build recipe, at
  the exact path (`metadata/<applicationId>.yml`) it needs to have inside the separate
  `fdroiddata` repository. It is not consumed from here; it's a ready-to-copy starting point.

## How to actually submit it (when you're ready)

1. Make sure at least one tagged release exists (`git tag v1.0.0 && git push origin v1.0.0`) —
   the GitHub Actions release workflow (`.github/workflows/release.yml`) will build a signed APK
   for it automatically. F-Droid doesn't use that APK, but the tag gives F-Droid's build server a
   fixed commit to build from.
2. Fork [`fdroiddata`](https://gitlab.com/fdroid/fdroiddata) on GitLab.
3. Copy `metadata/com.github.cookiesmartart.monopolybank.yml` from this repo into your fork's
   `metadata/` directory, keeping the same filename (it must match the app's `applicationId`
   exactly).
4. Update the `commit:` field to the actual tag you pushed in step 1, and confirm
   `CurrentVersion`/`CurrentVersionCode` match.
5. Open a merge request against `fdroiddata`. F-Droid's CI will attempt a clean build from source
   using the recipe; fix anything it flags.
6. Once merged, the app typically appears in F-Droid's repo within a day or two of the next
   build run, and picks up future updates automatically based on new tags, per
   `AutoUpdateMode: Version`.

Full details and edge cases: the official
[F-Droid inclusion how-to](https://f-droid.org/docs/Inclusion_How-To/).
