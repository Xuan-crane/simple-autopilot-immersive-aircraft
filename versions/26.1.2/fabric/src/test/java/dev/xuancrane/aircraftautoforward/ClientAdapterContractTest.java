package dev.xuancrane.aircraftautoforward;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import static org.junit.jupiter.api.Assertions.*;

/** Reads the actual loader/Minecraft dependencies and compiled adapters without initializing them. */
class ClientAdapterContractTest {
    private ClassNode read(String name) throws Exception {
        try (var stream = getClass().getClassLoader().getResourceAsStream(name + ".class")) {
            assertNotNull(stream, name); var node = new ClassNode(); new ClassReader(stream).accept(node, 0); return node;
        }
    }
    private List<MethodInsnNode> calls(MethodNode method) {
        var result = new ArrayList<MethodInsnNode>();
        for (var instruction : method.instructions) if (instruction instanceof MethodInsnNode call) result.add(call);
        return result;
    }
    @Test void hudCallbackAndDrawingMethodsExistInActualPlatformDependencies() throws Exception {
        String graphics = "net/minecraft/client/gui/GuiGraphicsExtractor";
        var drawing = read(graphics);
        var options = read("net/minecraft/client/Options");
        assertTrue(options.fields.stream().anyMatch(f -> f.name.equals("hideGui") && f.desc.equals("Z")));
        var ownHud = read("dev/xuancrane/aircraftautoforward/AutopilotHud");
        var render = ownHud.methods.stream().filter(m -> m.name.equals("render")).findFirst().orElseThrow();
        boolean respectsF1 = false;
        for (var instruction : render.instructions) if (instruction instanceof FieldInsnNode field
                && field.owner.equals("net/minecraft/client/Options") && field.name.equals("hideGui")) respectsF1 = true;
        assertTrue(respectsF1);

        assertTrue(drawing.methods.stream().anyMatch(m -> m.name.equals("text") && m.desc.equals("(Lnet/minecraft/client/gui/Font;Ljava/lang/String;IIIZ)V")));
        assertTrue(drawing.methods.stream().anyMatch(m -> m.name.equals("enableScissor") && m.desc.equals("(IIII)V")));
        assertTrue(drawing.methods.stream().anyMatch(m -> m.name.equals("disableScissor") && m.desc.equals("()V")));
        if (System.getProperty("autopilot.loader").equals("neoforge")) {
            var event = read("net/neoforged/neoforge/client/event/RenderGuiEvent");
            assertTrue(event.methods.stream().anyMatch(m -> m.name.equals("getGuiGraphics") && m.desc.equals("()L" + graphics + ";")));
            var client = read("dev/xuancrane/aircraftautoforward/AircraftAutoForward");
            var hook = client.methods.stream().filter(m -> m.name.equals("renderHud")).findFirst().orElseThrow();
            assertEquals("(Lnet/neoforged/neoforge/client/event/RenderGuiEvent$Post;)V", hook.desc);
            assertTrue(calls(hook).stream().anyMatch(c -> c.owner.endsWith("/AutopilotHud") && c.name.equals("render") && c.desc.equals("(L" + graphics + ";)V")));
        } else {
            var registry = read("net/fabricmc/fabric/api/client/rendering/v1/hud/HudElementRegistry");
            assertTrue(registry.methods.stream().anyMatch(m -> m.name.equals("addLast") && m.desc.equals("(Lnet/minecraft/resources/Identifier;Lnet/fabricmc/fabric/api/client/rendering/v1/hud/HudElement;)V")));
            var element = read("net/fabricmc/fabric/api/client/rendering/v1/hud/HudElement");
            assertTrue(element.methods.stream().anyMatch(m -> m.name.equals("extractRenderState") && m.desc.equals("(L" + graphics + ";Lnet/minecraft/client/DeltaTracker;)V")));
            var client = read("dev/xuancrane/aircraftautoforward/AircraftAutoForward");
            var hook = client.methods.stream().filter(m -> m.name.equals("onInitializeClient")).findFirst().orElseThrow();
            assertTrue(calls(hook).stream().anyMatch(c -> c.owner.endsWith("/HudElementRegistry") && c.name.equals("addLast")));
        }
    }
    @Test void renderingCannotScanOrPersistAndWarningsRefreshBeforeRateLimiting() throws Exception {
        var hud = read("dev/xuancrane/aircraftautoforward/AutopilotHud");
        var client = read("dev/xuancrane/aircraftautoforward/AircraftAutoForward");
        for (var method : List.of(hud.methods.stream().filter(m -> m.name.equals("render")).findFirst().orElseThrow(),
                client.methods.stream().filter(m -> m.name.equals("hudSnapshot")).findFirst().orElseThrow(),
                client.methods.stream().filter(m -> m.name.equals("settings")).findFirst().orElseThrow())) {
            assertFalse(calls(method).stream().anyMatch(c -> c.owner.endsWith("/TerrainScanner") || c.owner.startsWith("java/nio/file/")
                    || c.owner.endsWith("/ClientPreferences") && (c.name.equals("update") || c.name.equals("<init>"))));
        }
        var warning = client.methods.stream().filter(m -> m.name.equals("warning")).findFirst().orElseThrow();
        boolean refreshed = false;
        for (var instruction : warning.instructions) {
            if (instruction instanceof MethodInsnNode c && c.owner.endsWith("/FlightTelemetry") && c.name.equals("paused")) refreshed = true;
            if (instruction instanceof JumpInsnNode) { assertTrue(refreshed, "Persistent HUD reason must update outside the actionbar throttle"); break; }
        }
        assertTrue(refreshed);
        var screen = read("dev/xuancrane/aircraftautoforward/AutopilotSettingsScreen");
        var pause = screen.methods.stream().filter(m -> m.name.equals("isPauseScreen")).findFirst().orElseThrow();
        var instructions = new ArrayList<Integer>();
        for (var instruction : pause.instructions) if (instruction.getOpcode() >= 0) instructions.add(instruction.getOpcode());
        assertEquals(List.of(Opcodes.ICONST_0, Opcodes.IRETURN), instructions);
        assertTrue(screen.methods.stream().anyMatch(m -> m.name.equals("extractRenderState") && m.desc.equals("(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V")));
    }
    @Test void pageAdjustmentCannotOverwriteAnUnsuccessfulSaveWarning() throws Exception {
        var client = read("dev/xuancrane/aircraftautoforward/AircraftAutoForward");
        var tick = client.methods.stream().filter(m -> m.name.equals("afterTick")).findFirst().orElseThrow();
        for (var instruction : tick.instructions) if (instruction instanceof LdcInsnNode constant)
            assertNotEquals("target_height", constant.cst, "Tick must not overwrite the adjustment's save result");
        var adjust = client.methods.stream().filter(m -> m.name.equals("adjustClearance")).findFirst().orElseThrow();
        assertEquals("(I)V", adjust.desc);
        int apply = -1, show = -1;
        JumpInsnNode savedOnly = null;
        for (int i = 0; i < adjust.instructions.size(); i++) {
            var instruction = adjust.instructions.get(i);
            if (instruction instanceof MethodInsnNode call && call.name.equals("applySettings")) apply = i;
            if (apply >= 0 && instruction instanceof JumpInsnNode jump) savedOnly = jump;
            if (instruction instanceof MethodInsnNode call && call.name.equals("show")) show = i;
        }
        assertTrue(apply >= 0); assertTrue(show > apply); assertNotNull(savedOnly);
        assertEquals(Opcodes.IFEQ, savedOnly.getOpcode(), "False save result must skip target feedback");
        assertTrue(adjust.instructions.indexOf(savedOnly) < show);
        assertTrue(adjust.instructions.indexOf(savedOnly.label) > show);
    }

}
