# Pixel Capsule — native Android

Independent Android application written in Kotlin and Jetpack Compose. This is not a WebView wrapper.

## Features in the source project

- Floating capsule overlay with expand/collapse animations and touch gestures.
- Display cutout positioning using Android's public display APIs.
- MediaSession playback controls (where the media app exposes a controllable session).
- Notification listener and conditional quick replies using RemoteInput.
- Flashlight, brightness, volume, battery, Bluetooth connection and call-state integrations.
- Material 3 styling with Android dynamic colors.

## Source archive

The complete initial source project is provided as a separate ZIP download in the project handoff. It contains the Android Studio project, Kotlin sources, resources, manifest and implementation notes.

## Build requirements

- Android Studio with JDK 17
- Android Gradle Plugin 9.3.1 / Gradle 9.5.0
- Android SDK Platform 37 and Build Tools 36.0.0

Open the `PixelCapsuleAndroid` folder in Android Studio, sync Gradle, then build the debug APK with `assembleDebug`.

## Important status

The source has **not yet been compiled or tested on a physical device**. No APK is published from this repository at this stage. Some functionality depends on Android permissions, hardware support, and what other apps expose through public Android APIs. See `IMPLEMENTATION_STATUS.md` in the source archive for details.

This is an early open-source development project, not a finished production release.
