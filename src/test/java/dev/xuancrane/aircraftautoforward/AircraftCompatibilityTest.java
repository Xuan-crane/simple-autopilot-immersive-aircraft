package dev.xuancrane.aircraftautoforward;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;

import java.io.IOException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/** Inspects the actual dependency JAR without booting Minecraft or initializing entity classes. */
class AircraftCompatibilityTest {
    private ClassNode read(String name) throws IOException {
        try (var stream = getClass().getClassLoader().getResourceAsStream(name + ".class")) {
            assertNotNull(stream, "Missing dependency class: " + name);
            var node = new ClassNode();
            new ClassReader(stream).accept(node, 0);
            return node;
        }
    }

    @Test void mixinOrdinalZeroStillTargetsTheLocalPilotInput() throws IOException {
        var vehicle = read("immersive_aircraft/entity/VehicleEntity");
        var tick = vehicle.methods.stream().filter(m -> m.name.equals("tickPilot") && m.desc.equals("()V"))
                .findFirst().orElseThrow();
        var calls = new ArrayList<MethodInsnNode>();
        for (var instruction : tick.instructions) {
            if (instruction instanceof MethodInsnNode call) calls.add(call);
        }
        var inputs = calls.stream().filter(c -> c.owner.equals(vehicle.name)
                && c.name.equals("setInputs") && c.desc.equals("(FFF)V")).toList();
        assertEquals(2, inputs.size(), "Local pilot input and remote-zero input sites must remain distinct");
        int firstInput = calls.indexOf(inputs.getFirst());
        assertEquals(3, calls.subList(0, firstInput).stream().filter(c -> c.name.equals("getMovementMultiplier")
                && c.desc.equals("(ZZ)F")).count());
        assertTrue(calls.subList(0, firstInput).stream().anyMatch(c -> c.name.equals("isLocalPlayer")));
        assertTrue(calls.subList(0, firstInput).stream().anyMatch(c -> c.name.equals("useAirplaneControls")));
    }

    @Test void allSevenAircraftHaveTheExpectedNativeControlFamily() throws IOException {
        for (String name : new String[]{"Biplane", "BambooHopper"}) {
            assertEquals("immersive_aircraft/entity/AirplaneEntity", read("immersive_aircraft/entity/" + name + "Entity").superName);
        }
        for (String name : new String[]{"Gyrodyne", "Airship", "Quadrocopter"}) {
            assertEquals("immersive_aircraft/entity/Rotorcraft", read("immersive_aircraft/entity/" + name + "Entity").superName);
        }
        for (String name : new String[]{"CargoAirship", "Warship"}) {
            assertEquals("immersive_aircraft/entity/AirshipEntity", read("immersive_aircraft/entity/" + name + "Entity").superName);
        }
    }

    @Test void terrainAndPowerAccessorsRemainAvailable() throws IOException {
        var vehicle = read("immersive_aircraft/entity/VehicleEntity");
        assertTrue(vehicle.methods.stream().anyMatch(m -> m.name.equals("getShapes") && m.desc.equals("()Ljava/util/List;")));
        var engine = read("immersive_aircraft/entity/EngineVehicle");
        for (String name : new String[]{"getFuelUtilization", "getEngineTarget"}) {
            assertTrue(engine.methods.stream().anyMatch(m -> m.name.equals(name) && m.desc.equals("()F")));
        }
        assertTrue(read("immersive_aircraft/entity/InventoryVehicleEntity").methods.stream()
                .anyMatch(m -> m.name.equals("getProperties") && m.desc.equals("()Limmersive_aircraft/entity/misc/VehicleProperties;")));
    }
}
