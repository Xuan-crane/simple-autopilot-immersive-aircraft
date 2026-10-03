package dev.xuancrane.aircraftautoforward;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PilotControlsTest {
    @Test void independentSwitchesRespectBothNativeLayouts() {
        for (boolean airplane : new boolean[]{false, true}) {
            for (boolean forward : new boolean[]{false, true}) {
                for (boolean height : new boolean[]{false, true}) {
                    for (float manual : new float[]{-1, 0, 1}) {
                        var input = PilotControls.route(airplane, forward, height, manual, manual, -0.25F, 0.8F);
                        assertEquals(airplane ? (forward ? 1 : manual) : (height ? -0.25F : manual), input.y());
                        assertEquals(airplane ? (height ? -0.25F : manual) : (forward ? 0.8F : manual), input.z());
                    }
                }
            }
        }
    }
    @Test void forwardOnlyAirplaneNeverForcesNoseDown() {
        var input = PilotControls.route(true, true, false, -1, -0.4F, 0, 1);
        assertEquals(1, input.y());
        assertEquals(-0.4F, input.z());
    }
    @Test void altitudeOnlyAirplaneLeavesThrottleManual() {
        var input = PilotControls.route(true, false, true, -0.5F, 1, -0.2F, 1);
        assertEquals(-0.5F, input.y());
        assertEquals(-0.2F, input.z());
    }
    @Test void sensorFailureRetainsAirplaneThrottleButStopsRotorcraftPropulsion() {
        assertEquals(new PilotControls.Input(1, 0), PilotControls.route(true, true, true, -1, -1, 0, 0));
        assertEquals(new PilotControls.Input(0, 0), PilotControls.route(false, true, true, -1, -1, 0, 0));
    }
}
