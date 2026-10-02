# Verification and manual testing

Version 1.2.1 is an Alpha release. Automatic checks pass; actual flight in a game or multiplayer server remains unverified.

## Automated checks

The build targets Minecraft 1.21.1, NeoForge 21.1.1 and Immersive Aircraft 1.5.0. The 48 JUnit tests cover:

- Independent forward and altitude state, reset behavior and control routing for rotorcraft and airplanes.
- Height correction, damping, bounded airplane pitch and simulated controller convergence.
- Terrain sampling, missing terrain and obstacle prediction, including narrow obstacles.
- The input injection point and required methods in the actual Immersive Aircraft dependency, plus the seven original aircraft families.

The supported metadata ranges are NeoForge `[21.1.1,21.2)` and Immersive Aircraft `[1.5.0,1.6)`. Relevant NeoForge APIs were compared between 21.1.1 and 21.1.243; the inspected pilot/control methods were compared between Immersive Aircraft 1.5.0 and 1.5.2. These checks do not replace gameplay tests of each version or modpack.

Run `gradlew.bat build` on Windows, or `sh ./gradlew build` on Linux/macOS, using JDK 21. Reports appear in `build/reports/tests/test/index.html`.

## Gameplay checks still needed

Use a separate creative test world with an open flight area before relying on altitude assistance.

1. Check all seven aircraft. For airplanes, take off manually before enabling altitude hold.
2. Toggle V and H independently. Confirm unrelated keys do not interrupt enabled axes and turning still works.
3. Adjust clearance with Page Up/Down; fly across flat terrain, a hill, a drop, trees and water.
4. Approach a wall, a narrow column and an overhang at a safe speed; verify warning behavior and manual takeover.
5. Check unloaded terrain, insufficient power, overhead clearance and the world height limit. Airplanes retain auto throttle during altitude suspension when V is enabled.
6. Confirm both toggles reset on dismount, death, aircraft/world changes and disconnect.
7. Join a server with only the original Immersive Aircraft installed. Check responsiveness and control synchronization.

High speeds, steep terrain, rapid turns, aircraft upgrades, server latency, other entities, automatic takeoff and landing have not been validated. Terrain scans only use loaded chunks and look ahead at most 48 blocks. This release does not plan routes or guarantee collision avoidance.

When reporting a problem, include Minecraft, NeoForge and Immersive Aircraft versions, aircraft type, active toggles and steps to reproduce. Review logs for private information before attaching them.
