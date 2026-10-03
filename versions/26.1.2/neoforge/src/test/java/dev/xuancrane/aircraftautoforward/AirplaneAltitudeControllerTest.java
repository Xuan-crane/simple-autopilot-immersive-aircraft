package dev.xuancrane.aircraftautoforward;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AirplaneAltitudeControllerTest {
    private float command(double y, double ahead, double vy, double speed, double pitch) {
        return AirplaneAltitudeController.control(y, 0, ahead, 10, vy, speed, pitch, 4, 0);
    }
    @Test void raisesNoseBelowTargetAndLowersItAboveTarget() {
        assertTrue(command(4, 0, 0, 0.6, 0) < 0);
        assertTrue(command(16, 0, 0, 0.6, 0) > 0);
        assertEquals(0, command(10, 0, 0, 0.6, 0), 0.0001);
    }
    @Test void predictsTerrainAndDampsVerticalMomentum() {
        assertTrue(command(10, 7, 0, 0.6, 0) < 0);
        assertTrue(command(10, 0, 0.2, 0.6, 0) > 0);
        assertTrue(command(10, 0, -0.2, 0.6, 0) < 0);
    }
    @Test void lowAirspeedRestrictsPitchAndExtremeAttitudesRecover() {
        assertEquals(0, command(0, 0, 0, 0.1, -8), 0.0001);
        assertEquals(0, command(100, 0, 0, 0.1, 4), 0.0001);
        assertTrue(command(10, 0, 0, 0.6, 80) < 0);
        assertTrue(command(10, 0, 0, 0.6, -80) > 0);
    }
    @Test void stabilizerUpgradeReceivesPitchFeedForward() {
        assertTrue(AirplaneAltitudeController.control(0, 0, 0, 10, 0, 0.6, -18, 4, 0.3) < 0);
    }
    @Test void invalidTelemetryFailsAndCommandsAreBounded() {
        assertThrows(IllegalArgumentException.class, () -> command(10, Double.NaN, 0, 0.6, 0));
        assertThrows(IllegalArgumentException.class, () -> command(10, 0, 0, -1, 0));
        for (double y : new double[]{-500, 0, 500}) {
            for (double pitch : new double[]{-180, 0, 180}) {
                assertTrue(Math.abs(command(y, 0, 0, 0.6, pitch)) <= 1);
            }
        }
    }
    @Test void smoothedPitchAndVerticalResponseConvergeWithoutStabilizer() {
        // Approximate upstream pitch integration, ten-tick input smoothing and lift response.
        // Ground takeoff, water buoyancy, fuel and real collisions require gameplay testing.
        for (double speed : new double[]{0.35, 0.6, 0.9}) {
            for (double start : new double[]{4, 25}) {
                double y = start, vy = 0, pitch = 0, smoothed = 0, finalError = 0;
                for (int tick = 0; tick < 1800; tick++) {
                    float input = command(y, 0, vy, speed, pitch);
                    pitch += 4 * smoothed;
                    vy += (-Math.sin(Math.toRadians(pitch)) * speed - vy) * 0.15;
                    y += vy;
                    smoothed = smoothed * 0.9 + input * 0.1;
                    assertTrue(Math.abs(pitch) < 25 && Double.isFinite(y));
                    if (tick > 1500) finalError = Math.max(finalError, Math.abs(y - 10));
                }
                assertTrue(finalError < 0.75, "speed=" + speed + " start=" + start + " error=" + finalError);
            }
        }
    }
}
