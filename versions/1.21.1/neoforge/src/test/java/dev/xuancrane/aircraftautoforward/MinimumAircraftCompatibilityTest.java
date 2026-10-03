package dev.xuancrane.aircraftautoforward;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.*;
import java.nio.file.Path;
import java.util.zip.ZipFile;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

/** Inspect published artifacts for the claimed lower dependency bound without initializing MC. */
class MinimumAircraftCompatibilityTest {
    private ClassNode read(String jar, String name) throws Exception {
        try (var zip = new ZipFile(Path.of(jar).toFile())) {
            var entry = zip.getEntry("immersive_aircraft/" + name + ".class");
            assertNotNull(entry);
            var node = new ClassNode();
            new ClassReader(zip.getInputStream(entry)).accept(node, 0);
            return node;
        }
    }
    private java.util.List<String> calls(ClassNode node, String method) {
        var result = new ArrayList<String>();
        var body=node.methods.stream().filter(m -> m.name.equals(method)).findFirst().orElseThrow();
        for(var insn:body.instructions) {
            if(insn instanceof MethodInsnNode m) result.add(m.owner+"."+m.name+m.desc);
            if(insn instanceof FieldInsnNode f) result.add(f.owner+"."+f.name+f.desc);
        }
        return result;
    }
    @Test void lowerBoundAndCurrentHaveIdenticalPilotCallsAndKeyFamilies() throws Exception {
        String low=System.getProperty("upstream.low"), current=System.getProperty("upstream.current");
        var lower=read(low,"entity/VehicleEntity");
        var newer=read(current,"entity/VehicleEntity");
        assertEquals(calls(lower,"tickPilot"),calls(newer,"tickPilot"));
        assertEquals(2,calls(lower,"tickPilot").stream().filter(s -> s.equals("immersive_aircraft/entity/VehicleEntity.setInputs(FFF)V")).count());
        for(String type:new String[]{"Biplane","BambooHopper","Gyrodyne","Airship","Quadrocopter","CargoAirship","Warship"}) {
            assertEquals(read(low,"entity/"+type+"Entity").superName,read(current,"entity/"+type+"Entity").superName);
        }
    }
    @Test void lowerBoundRetainsEveryCustomAccessorUsedByTheAddon() throws Exception {
        String low=System.getProperty("upstream.low");
        String[][] required={{"entity/VehicleEntity","getShapes","()Ljava/util/List;"},
            {"entity/VehicleEntity","getForwardDirection","()Lorg/joml/Vector3f;"},
            {"entity/EngineVehicle","getFuelUtilization","()F"},
            {"entity/EngineVehicle","getEngineTarget","()F"},
            {"entity/InventoryVehicleEntity","getProperties","()Limmersive_aircraft/entity/misc/VehicleProperties;"},
            {"entity/misc/VehicleProperties","get","(Limmersive_aircraft/item/upgrade/VehicleStat;)F"},
            {"entity/misc/VehicleProperties","getAdditive","(Limmersive_aircraft/item/upgrade/VehicleStat;)F"}};
        for(var api:required) assertTrue(read(low,api[0]).methods.stream().anyMatch(m -> m.name.equals(api[1])&&m.desc.equals(api[2])),String.join(" ",api));
    }
}
