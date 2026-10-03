# Simple Autopilot for Immersive Aircraft — 1.3.0 Alpha

Client-side independent auto-forward and experimental terrain-relative altitude assistance for all seven original Immersive Aircraft aircraft. Players steer manually. Fuel and physics remain under the original mod's rules.

## Install

Install ONE jar matching your Minecraft version AND loader. Replace the previous addon jar. Do not install all jars or a sources/development jar. Fabric needs Fabric API for the same Minecraft version. Install Immersive Aircraft and its required dependencies as usual; the server does not need this addon.

| Minecraft | Loaders | Java | Verified Immersive Aircraft versions |
|---|---|---|---|
|1.20.1|Forge / Fabric|17+|1.5.0|
|1.21.1|NeoForge / Fabric|21+|1.5.0 and 1.5.2|
|1.21.11|NeoForge / Fabric|21+|1.5.0 and 1.5.2|
|26.1.2|NeoForge / Fabric|25+|1.5.2|
|26.2|NeoForge / Fabric|25+|1.5.2|

Exact loader bounds are in release-manifest.json and each jar's metadata. The 1.21.1 NeoForge build retains the 21.1.1 lower bound, including 21.1.243.

## Controls

V toggles auto-forward. H independently toggles terrain-relative altitude. Page Up/Down adjust clearance by 2 blocks; default 10, range4–64. Change bindings in Minecraft's Controls menu. Height is clearance from terrain to the bottom of the aircraft, not absolute world Y. The setting does not persist across game restarts.

Unrelated keys, screens and focus changes do not cancel enabled modes or override enabled axes. Manual steering remains available. Typing in chat does not trigger new toggles. Dismounting, death, changing aircraft/world or disconnecting resets the modes. A genuinely paused single-player simulation still pauses physics.

Terrain and swept-hull scans look ahead for ground, obstacles and ceilings. Unknown terrain/chunks, power failure, urgent hazards and world-height limits pause affected corrections with a warning while retaining the mode. Fixed-wing aircraft use throttle/pitch and cannot hover. This does not provide route planning, automatic turns or guaranteed collision avoidance.

## Validation

Ten actual production builds and 496 automated test cases passed, with independent artifact/source checks. No game was launched for this release; actual loading, flight and multiplayer behavior need player testing. Use a backed-up test world first. Report the game/loader/original-mod versions, jar filename, aircraft type, enabled modes, reproduction steps and crash report if relevant.

## Source and license

Each game/loader directory in the source archive is a separate Gradle project with its own wrapper. Run `gradlew.bat build --no-daemon --max-workers=2` using the Java version above. 1.20.1 Forge uses reobfJar; Fabric 1.x uses remapJar; 26.x builds use the official unobfuscated toolchain. Publish only production jars from build/libs.

GPL-3.0-only. [Source](https://github.com/Xuan-crane/simple-autopilot-immersive-aircraft). Requires [Immersive Aircraft](https://modrinth.com/mod/immersive-aircraft) by Luke100000. The addon does not bundle Minecraft, a loader or the original mod. See README_ZH.md for Chinese instructions and the flight test checklist.

## Repository layout

The repository root builds the default Minecraft 1.21.1 NeoForge variant. All ten complete projects are under `versions/<minecraft>/<loader>/`; select one directory before building. The portable source archive uses `<minecraft>/<loader>/` directly. Each variant has its own pinned wrapper and dependencies. The v1.2.1 tag and earlier release remain available.
