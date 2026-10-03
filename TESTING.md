# Verification and manual testing —1.3.0 Alpha

Ten production Gradle builds passed, totaling 496 JUnit test executions with no failures, errors or skipped tests.1.20.1 Forge/Fabric run 48 each;1.21.1 and 1.21.11 NeoForge/Fabric run 51 each;26.1.2 and 26.2 NeoForge/Fabric run 49 each. Repeated controller tests run independently against each platform's actual compile dependencies.

Checks cover independent V/H toggles, unrelated keys preserving enabled axes, lifecycle resets, rotorcraft/airplane routing, bounded altitude/pitch controllers, missing terrain, narrow obstacles, original-mod pilot-input bytecode and aircraft families. Where applicable, lower-bound 1.5.0 and current 1.5.2 artifacts and compiled mixin injection contracts are checked. Final jars were independently reviewed for the correct loader metadata, namespace, Java bytecode target and client-only entrypoints. Source/installation archives passed integrity and file-identity checks.

No game was launched for this release. Actual loading, flight, multiplayer and modpack combinations remain unverified. Alpha status is intentional; automated checks do not guarantee collision avoidance.

## Run checks

Use Java 17 for 1.20.1, Java 21 for 1.21.x, Java 25 for 26.x. In a selected `versions/<minecraft>/<loader>/` directory run `gradlew.bat build --no-daemon --max-workers=2` (Windows) or `sh ./gradlew build --no-daemon --max-workers=2` (Linux/macOS). Reports are in `build/reports/tests/test/`. The root project is 1.21.1 NeoForge.

## Player test checklist

1. Use a backed-up test world and install only the jar matching the game and loader.
2. Check all seven original aircraft and find all four keybindings. Take off manually before testing altitude assistance on fixed-wing aircraft.
3. Toggle V and H independently; use other keys and chat while confirming enabled modes remain active and steering stays manual.
4. Adjust Page Up/Down by 2 blocks; check default 10 and 4–64 bounds, slopes, trees, water and changes in ground height.
5. Test walls, narrow columns and ceilings with room to recover. Check warnings and mode retention for unknown/unloaded terrain, missing power and world-height limits.
6. Check reset on dismount, death, driver-seat loss, changing aircraft/world and disconnect.
7. Test a multiplayer server with the original mod, while this addon is installed only on the client.

Fixed-wing aircraft retain automatic throttle when V is on; they cannot hover. Turning V off restores manual input, but the original aircraft may retain its engine setting, so use its normal throttle-down control. H alone does not start an engine. High speed, steep terrain, upgrades, latency, rapid turns, automatic takeoff/landing and collisions with other entities are not certified.

Report game/loader/original-mod versions, jar filename, aircraft type, active modes, reproduction steps and a crash report when relevant.
