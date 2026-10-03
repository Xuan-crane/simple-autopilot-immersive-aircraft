package dev.xuancrane.aircraftautoforward;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HudTelemetryTest {
    @Test void actualSuspensionReasonPersistsUntilAnObservedRecovery() {
        var telemetry=new FlightTelemetry();
        telemetry.paused("ceiling",1_000_000_000L);
        assertEquals("ceiling",telemetry.reason(1_500_000_000L));
        telemetry.tracking(1_600_000_000L);
        assertEquals("",telemetry.reason(1_700_000_000L));
    }
    @Test void staleOrResetTelemetryNeverClaimsThatAltitudeIsTracking() {
        var telemetry=new FlightTelemetry();
        assertEquals("waiting",telemetry.reason(100));
        telemetry.tracking(100);
        assertEquals("waiting",telemetry.reason(2_000_000_101L));
        assertEquals("waiting",telemetry.reason(99));
        telemetry.paused("no_power",200);
        telemetry.reset();
        assertEquals("waiting",telemetry.reason(201));
    }
    @Test void everyCornerStaysInsideTinyAndLargeScaledScreens() {
        for(var corner:ClientPreferences.HudCorner.values()) {
            for(int[] size:new int[][]{{0,0},{1,1},{30,20},{320,180},{1920,1080}}) {
                var box=HudLayout.bounds(size[0],size[1],800,100,corner);
                assertTrue(box.x()>=0 && box.y()>=0 && box.width()>=0 && box.height()>=0);
                assertTrue(box.x()+box.width()<=size[0]);
                assertTrue(box.y()+box.height()<=size[1]);
            }
        }
    }
    @Test void cornerAnchorsRespectGuiCoordinatesAndMargins() {
        var top=HudLayout.bounds(320,180,100,30,ClientPreferences.HudCorner.TOP_LEFT);
        var bottom=HudLayout.bounds(320,180,100,30,ClientPreferences.HudCorner.BOTTOM_RIGHT);
        assertEquals(new HudLayout.Box(8,8,100,30),top);
        assertEquals(new HudLayout.Box(212,142,100,30),bottom);
    }
}
