# Verification — 1.4.0 Alpha

Ten production builds passed with 636 test executions, zero failures, errors or skips. This total includes tests repeated across platforms, not 636 distinct gameplay scenarios. 1.20.1 Forge/Fabric run 62 each; 1.21.1 and 1.21.11 NeoForge/Fabric run 65 each; 26.1.2 and 26.2 NeoForge/Fabric run 63 each.

Tests cover controller/lifecycle behavior, settings persistence, invalid or unwritable configuration and retries, target bounds, HUD corner bounds, cached warning expiry/reset, and actual dependency API/compiled wiring checks. Original controllers, terrain/obstacle logic and mixin source remain identical to 1.3.0; the flight-input computation only adds telemetry recording. Deployment jars were checked for loader metadata, Java target, resources and remapping. Both archives were independently compared byte-for-byte with their staging files.

No Minecraft instance was launched. Real startup, HUD appearance, settings interaction, flight, multiplayer and modpack compatibility remain unverified. This is an Alpha release; automated tests do not guarantee collision avoidance.

## Run checks

Use Java 17 for 1.20.1, Java 21 for 1.21.x, Java 25 for 26.x. In the selected project run `gradlew.bat build --no-daemon --max-workers=2` on Windows, or `sh ./gradlew build --no-daemon --max-workers=2` on Linux/macOS. The root project is 1.21.1 NeoForge. Test reports are in `build/reports/tests/test`.

## Player checklist

1. Install one matching jar in a test instance. Bind the initially unbound settings key in Controls.
2. Check Save, Cancel, Escape and Reset; check target/step/panel settings survive a game restart.
3. Check the driver-only HUD, all four corners, different GUI scales and F1 hiding.
4. Toggle V/H independently, adjust Page Up/Down with the selected step, and check unrelated keys/chat/settings do not cancel flight. Steering remains manual.
5. Check real pause reasons for missing power, unknown terrain and nearby ceilings; verify recovery clears stale reasons.
6. Check modes reset on dismount/death/aircraft/world/connection changes while the target remains saved.
7. Test rotorcraft and fixed-wing aircraft, then client-only addon installation on multiplayer. Fixed-wing aircraft cannot hover; use normal throttle-down controls after disabling V if the original engine retains its setting.

Report game/loader/original-mod versions, jar filename, aircraft, enabled modes, steps and any crash report.
