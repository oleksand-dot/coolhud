# CoolHUD

A lightweight, cosmetic Fabric HUD mod for Minecraft 1.21.4. No ESP, no combat
assist, no hidden-info features — just a nicer-looking, fully toggleable HUD.

## Features
- Coordinates panel (toggle)
- FPS counter, color-coded (toggle)
- Potion effect timers with amplifier + countdown (toggle)
- Armor bar (toggle)
- Hunger bar (toggle)
- Optional biome name display (toggle)
- Soft "glassmorphism" panel style with a cyan accent strip
- In-game settings screen — default keybind `]` to open it (rebindable in
  Minecraft's Controls menu under "CoolHUD")
- Settings persist to `.minecraft/config/coolhud.json`

## Requirements
- Minecraft 1.21.4
- Fabric Loader 0.16.9+
- Fabric API 0.119.0+1.21.4 (must be installed alongside the mod jar)
- Java 21

## Building
This is a standard Fabric Loom Gradle project.

```bash
./gradlew build
```

The compiled jar will be at `build/libs/coolhud-1.0.0.jar`.
If you don't have a `gradlew` wrapper yet, generate one with:

```bash
gradle wrapper --gradle-version 8.10
```

(any recent Gradle 8.x works with Loom 1.9)

## Installing on Pojav / FCL
1. Build the jar (above) — or compile it in Android Studio / Termux if you're
   doing everything on-device.
2. Make sure you're using a **Fabric** installation in Pojav/FCL for
   Minecraft 1.21.4 (Loader 0.16.9+).
3. Drop `coolhud-1.0.0.jar` into `.minecraft/mods/`.
4. Also drop in `fabric-api-0.119.0+1.21.4.jar` (download from Modrinth or
   CurseForge) — CoolHUD depends on it.
5. Launch. Press `]` in-game to open the CoolHUD settings screen and toggle
   whatever you want on/off.

## Notes on versions
Mod version pins (`fabric-api`, `yarn mappings`, `loader`) are accurate as of
this writing but Fabric ships frequent point releases. If a build fails with
a "could not resolve" error, check https://fabricmc.net/develop/ and
https://modrinth.com/mod/fabric-api/versions for the current numbers for
1.21.4 and swap them into `gradle.properties`.
