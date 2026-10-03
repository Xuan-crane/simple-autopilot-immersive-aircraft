package dev.xuancrane.aircraftautoforward;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import java.util.List;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

class InjectionContractTest {
    private Object value(AnnotationNode annotation, String key) {
        for (int i=0;i<annotation.values.size();i+=2) if(annotation.values.get(i).equals(key)) return annotation.values.get(i+1);
        return null;
    }
    @Test void compiledMixinChangesOnlyPilotAxesOneAndTwoAtTheCustomLocalInputSite() throws Exception {
        try(var bytes=getClass().getClassLoader().getResourceAsStream("dev/xuancrane/aircraftautoforward/mixin/VehicleEntityMixin.class")) {
            assertNotNull(bytes);
            var mixin=new ClassNode(); new ClassReader(bytes).accept(mixin,0);
            var annotation=mixin.invisibleAnnotations.stream().filter(a->a.desc.endsWith("/Mixin;")).findFirst().orElseThrow();
            assertEquals(false,value(annotation,"remap"));
            var handler=mixin.methods.stream().filter(m->m.name.equals("aircraftAutoForward$flight")).findFirst().orElseThrow();
            var inject=handler.visibleAnnotations.stream().filter(a->a.desc.endsWith("/ModifyArgs;")).findFirst().orElseThrow();
            assertEquals(List.of("tickPilot()V"),value(inject,"method"));
            assertEquals(1,value(inject,"require")); assertEquals(1,value(inject,"allow"));
            var rawAt=value(inject,"at");
            var at=rawAt instanceof AnnotationNode node ? node : (AnnotationNode)((List<?>)rawAt).getFirst();
            assertEquals("INVOKE",value(at,"value"));
            assertEquals("Limmersive_aircraft/entity/VehicleEntity;setInputs(FFF)V",value(at,"target"));
            assertEquals(0,value(at,"ordinal"));
            var indices=new ArrayList<Integer>();
            for(var insn:handler.instructions) if(insn instanceof MethodInsnNode call && call.owner.endsWith("/Args") && call.name.equals("set")) {
                var previous=insn.getPrevious();
                while(previous!=null && previous.getOpcode()!=Opcodes.ALOAD) previous=previous.getPrevious();
                assertNotNull(previous);
                var index=previous.getPrevious();
                while(index.getOpcode()<0) index=index.getPrevious();
                assertTrue(index.getOpcode()==Opcodes.ICONST_1 || index.getOpcode()==Opcodes.ICONST_2);
                indices.add(index.getOpcode()-Opcodes.ICONST_0);
            }
            assertEquals(List.of(1,2),indices,"Manual steering axis zero must remain untouched");
        }
    }
}
