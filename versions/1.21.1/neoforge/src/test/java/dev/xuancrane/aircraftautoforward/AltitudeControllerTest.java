package dev.xuancrane.aircraftautoforward;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AltitudeControllerTest {
    @Test void belowTargetClimbsAndAboveTargetDescends() {
        assertTrue(AltitudeController.control(4, 0, 0, 10, 0).vertical() > 0);
        assertTrue(AltitudeController.control(16, 0, 0, 10, 0).vertical() < 0);
    }
    @Test void nearTargetBrakesVerticalMomentum() {
        assertTrue(AltitudeController.control(10, 0, 0, 10, 0.2).vertical() < 0);
        assertTrue(AltitudeController.control(10, 0, 0, 10, -0.2).vertical() > 0);
        assertEquals(0, AltitudeController.control(10, 0, 0, 10, 0).vertical());
    }
    @Test void approachingHillStartsClimbAndReducesForwardInput() {
        var output = AltitudeController.control(10, 0, 7, 10, 0);
        assertTrue(output.vertical() > 0);
        assertTrue(output.forward() < 0.2);
    }
    @Test void groundImmediatelyBelowPreventsDescentIntoACliffEdge() {
        assertEquals(0, AltitudeController.control(30, 20, 0, 10, 0).vertical());
    }
    @Test void outputsAlwaysRemainWithinVanillaInputRange() {
        for (double y : new double[]{-500, 0, 10, 500}) {
            for (double v : new double[]{-3, 0, 3}) {
                var output = AltitudeController.control(y, 0, 20, 10, v);
                assertTrue(output.vertical() >= -1 && output.vertical() <= 1);
                assertTrue(output.forward() >= 0 && output.forward() <= 1);
            }
        }
    }
    @Test void unknownGroundCannotProduceControl() {
        assertThrows(IllegalArgumentException.class, () -> AltitudeController.control(10, Double.NaN, 0, 10, 0));
        assertThrows(IllegalArgumentException.class, () -> AltitudeController.control(10, 0, 0, 0, 0));
    }
    @Test void delayedActuatorConvergesForDifferentVehicleVerticalResponses() {
        // Numerical approximation of the upstream 10-tick exponential input smoothing,
        // vertical thrust, and drag. This is NOT a claim of in-game validation.
        for (double thrust : new double[]{0.015, 0.025, 0.04, 0.08}) {
            for (double initial : new double[]{0, 35}) {
                double y = initial, velocity = 0, smoothed = 0;
                double worstFinalError = 0;
                for (int tick = 0; tick < 1800; tick++) {
                    float input = AltitudeController.control(y, 0, 0, 10, velocity).vertical();
                    velocity = velocity * 0.925 + thrust * smoothed;
                    y += velocity;
                    smoothed = smoothed * 0.9 + input * 0.1;
                    assertTrue(Double.isFinite(y) && Math.abs(velocity) < 0.6);
                    if (tick > 1500) worstFinalError = Math.max(worstFinalError, Math.abs(y - 10));
                }
                assertTrue(worstFinalError < 0.75, "thrust=" + thrust + " initial=" + initial + " error=" + worstFinalError);
            }
        }
    }
}
