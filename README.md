# Simple Autopilot for Immersive Aircraft — 1.4.0 Alpha

Client-side independent auto-forward and experimental terrain-relative altitude assistance for all seven original Immersive Aircraft aircraft. Players steer manually. Fuel and physics remain under the original mod's rules.

## Install

Install ONE jar matching your Minecraft version AND loader. Replace the previous addon jar. Do not install all jars or a sources/development jar. Fabric needs Fabric API for the same Minecraft version. Install Immersive Aircraft and its required dependencies as usual; the server does not need this addon.

| Minecraft | Loaders | Java | Immersive Aircraft versions |
|---|---|---|---|
|1.20.1|Forge / Fabric|17+|1.5.0|
|1.21.1|NeoForge / Fabric|21+|1.5.0 and 1.5.2|
|1.21.11|NeoForge / Fabric|21+|1.5.0 and 1.5.2|
|26.1.2|NeoForge / Fabric|25+|1.5.2|
|26.2|NeoForge / Fabric|25+|1.5.2|

Exact loader bounds are in release-manifest.json and each jar's metadata. The 1.21.1 NeoForge build retains the 21.1.1 lower bound, including 21.1.243.

## Controls

V toggles auto-forward. H independently toggles terrain-relative altitude. Page Up/Down adjust clearance using the saved step (default 2 blocks, adjustable from 1 to 16); default target 10, range 4–64. Change bindings in Minecraft's Controls menu. Height is clearance from terrain to the bottom of the aircraft, not absolute world Y. The target clearance, adjustment step and HUD preferences now persist across game restarts. Enabled flight modes are never saved.

Unrelated keys, screens and focus changes do not cancel enabled modes or override enabled axes. Manual steering remains available. Typing in chat does not trigger new toggles. Dismounting, death, changing aircraft/world or disconnecting resets the modes. A genuinely paused single-player simulation still pauses physics.

Terrain and swept-hull scans look ahead for ground, obstacles and ceilings. Unknown terrain/chunks, power failure, urgent hazards and world-height limits pause affected corrections with a warning while retaining the mode. Fixed-wing aircraft use throttle/pitch and cannot hover. This does not provide route planning, automatic turns or guaranteed collision avoidance.

## New in 1.4.0

While piloting a supported aircraft, a small status panel shows auto-forward, altitude assistance and target clearance. It shows a persistent reason when altitude assistance cannot act, and reports waiting when no recent flight reading is available. F1 hides it along with the rest of the HUD. You can hide the panel or move it to any of the four corners.

The new **Open autopilot settings** key is **unbound by default**. Assign it in Minecraft's Options → Controls → Key Binds, in this mod's category, then press it in-game with other menus closed. The settings screen is available outside an aircraft too. It lets you change target clearance, Page Up/Down step, panel visibility and panel corner. Save applies the draft; Cancel or Escape discards unsaved edits; Reset changes only the draft until saved. This screen does not pause the game.

Settings are stored in `config/aircraft_autoforward.properties` inside the Minecraft instance folder. Missing settings use defaults. Unreadable or malformed files do not prevent startup; a configuration error is shown when appropriate. If a save fails, the values still apply for the current session and the screen reports that they could not be saved. You can retry after fixing write access. Page Up/Down also save the target immediately. No world, aircraft identity or enabled mode is persisted.

The flight controllers, existing four key bindings and lifecycle reset rules are unchanged from 1.3.0. This update does not add navigation, absolute-world-Y mode or automatic turns.

## Source and license

Each game/loader directory in the source archive is a separate Gradle project with its own wrapper. Run `gradlew.bat build --no-daemon --max-workers=2` using the Java version above. 1.20.1 Forge uses reobfJar; Fabric 1.x uses remapJar; 26.x builds use the official unobfuscated toolchain. Publish only production jars from build/libs.

GPL-3.0-only. [Source](https://github.com/Xuan-crane/simple-autopilot-immersive-aircraft). Requires [Immersive Aircraft](https://modrinth.com/mod/immersive-aircraft) by Luke100000. The addon does not bundle Minecraft, a loader or the original mod. See README_ZH.md for Chinese instructions.

## Repository layout

The root builds Minecraft 1.21.1 NeoForge. Each complete variant is in `versions/<minecraft>/<loader>/`; select one directory before building. The portable source archive uses `<minecraft>/<loader>/` directly. Previous release tags remain available.
