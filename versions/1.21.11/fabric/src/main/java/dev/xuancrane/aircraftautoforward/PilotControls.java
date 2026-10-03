package dev.xuancrane.aircraftautoforward;

/** Native input layout differs between airplanes and rotorcraft. X steering stays untouched. */
public final class PilotControls {
    private PilotControls() {}
    public record Input(float y, float z) {}

    public static Input route(boolean airplane, boolean forwardEnabled, boolean heightEnabled,
                              float manualY, float manualZ, float autoHeight, float autoForward) {
        if (airplane) {
            return new Input(forwardEnabled ? 1 : manualY, heightEnabled ? autoHeight : manualZ);
        }
        return new Input(heightEnabled ? autoHeight : manualY, forwardEnabled ? autoForward : manualZ);
    }
}
