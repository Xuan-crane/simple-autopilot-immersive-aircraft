package dev.xuancrane.aircraftautoforward;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

/** Checks compiled client wiring without loading Minecraft or touching the game. */
class ClientUiContractTest {
    private ClassNode read(String name) throws Exception {
        try (var bytes = getClass().getClassLoader().getResourceAsStream("dev/xuancrane/aircraftautoforward/" + name + ".class")) {
            assertNotNull(bytes); var node = new ClassNode(); new ClassReader(bytes).accept(node, 0); return node;
        }
    }
    private MethodNode method(ClassNode type, String name) { return type.methods.stream().filter(m -> m.name.equals(name)).findFirst().orElseThrow(); }
    @Test void renderPathOnlyUsesCachedInputsAndActualLoaderHookIsRegistered() throws Exception {
        var client = read("AircraftClient");
        var render = method(client, "renderHud");
        boolean hudCalled = false;
        for (var instruction : render.instructions) if (instruction instanceof MethodInsnNode call) {
            assertFalse(call.owner.endsWith("/TerrainScanner") || call.name.equals("validate") || call.name.equals("flightInput"));
            assertFalse(call.owner.startsWith("java/nio/file/") || call.owner.endsWith("/ClientPreferences") && !call.name.equals("get") && !call.name.equals("loadFailed") && !call.name.equals("saveFailed"));
            hudCalled |= call.owner.endsWith("/AutopilotHud") && call.name.equals("render");
        }
        assertTrue(hudCalled);
        boolean hook = false;
        for (var m : client.methods) for (var instruction : m.instructions) {
            if (instruction instanceof FieldInsnNode field && field.owner.endsWith("/HudRenderCallback") && field.name.equals("EVENT")) hook = true;
            if (instruction instanceof MethodInsnNode call && call.name.equals("getGuiGraphics") && call.owner.contains("RenderGuiEvent")) hook = true;
        }
        assertTrue(hook, "Must contain an actual loader HUD callback");
        for (var m : read("AutopilotHud").methods) for (var instruction : m.instructions) if (instruction instanceof MethodInsnNode call) {
            assertFalse(call.owner.endsWith("/TerrainScanner") || call.owner.startsWith("java/nio/file/") || call.owner.endsWith("/ClientPreferences"));
        }
    }
    @Test void escapeNeverSavesAndSettingsScreenDoesNotPauseFlight() throws Exception {
        var screen = read("AutopilotSettingsScreen");
        var pause = method(screen, "isPauseScreen");
        var opcodes = new ArrayList<Integer>();
        for (var instruction : pause.instructions) if (instruction.getOpcode() >= 0) opcodes.add(instruction.getOpcode());
        assertEquals(java.util.List.of(Opcodes.ICONST_0, Opcodes.IRETURN), opcodes);
        boolean parentRestored = false;
        for (var instruction : method(screen, "onClose").instructions) if (instruction instanceof MethodInsnNode call) {
            assertFalse(call.owner.endsWith("/ClientPreferences") || call.owner.endsWith("/CruiseState") || call.owner.equals("java/util/function/Predicate"));
            parentRestored |= call.name.equals("setScreen");
        }
        assertTrue(parentRestored);
    }
    @Test void warningsRefreshTelemetryBeforeActionbarThrottle() throws Exception {
        var warning = method(read("AircraftClient"), "warning");
        int firstBranch = Integer.MAX_VALUE, paused = -1, i = 0;
        for (var instruction : warning.instructions) {
            if (instruction instanceof JumpInsnNode) firstBranch = Math.min(firstBranch, i);
            if (instruction instanceof MethodInsnNode call && call.owner.endsWith("/FlightTelemetry") && call.name.equals("paused")) paused = i;
            i++;
        }
        assertTrue(paused >= 0 && paused < firstBranch, "HUD TTL must not be gated by the 3s actionbar throttle");
    }
}
