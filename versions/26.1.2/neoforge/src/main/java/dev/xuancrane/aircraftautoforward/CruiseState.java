package dev.xuancrane.aircraftautoforward;

import java.util.UUID;

/** Owns only the toggle; never mutates Minecraft's shared key states. */
public final class CruiseState {
    private Object world;
    private UUID vehicle;
    private boolean heightEnabled;
    private boolean forwardEnabled;
    private int targetHeight = 10;

    public boolean heightEnabled() { return heightEnabled; }
    public boolean forwardEnabled() { return forwardEnabled; }
    public int targetHeight() { return targetHeight; }
    public int setTargetHeight(int height) {
        targetHeight = Math.max(4, Math.min(64, height));
        return targetHeight;
    }
    public boolean belongsTo(Object currentWorld, UUID currentVehicle) {
        return isActive() && world == currentWorld && vehicle != null && vehicle.equals(currentVehicle);
    }
    public int adjustHeight(int delta) {
        targetHeight = (int) Math.max(4L, Math.min(64L, (long) targetHeight + delta));
        return targetHeight;
    }

    public boolean toggleHeight(Object currentWorld, UUID currentVehicle) {
        if (currentWorld == null || currentVehicle == null) return false;
        validate(currentWorld, currentVehicle, true);
        world = currentWorld;
        vehicle = currentVehicle;
        heightEnabled = !heightEnabled;
        return heightEnabled;
    }

    public boolean isActive() {
        return forwardEnabled || heightEnabled;
    }

    public boolean clear() {
        boolean wasActive = isActive();
        world = null;
        vehicle = null;
        heightEnabled = false;
        forwardEnabled = false;
        return wasActive;
    }

    public boolean validate(Object currentWorld, UUID currentVehicle, boolean eligible) {
        if (!eligible || currentWorld == null || currentVehicle == null
                || (isActive() && (world != currentWorld || !vehicle.equals(currentVehicle)))) {
            return clear();
        }
        return false;
    }

    public boolean toggle(Object currentWorld, UUID currentVehicle) {
        if (currentWorld == null || currentVehicle == null) return false;
        validate(currentWorld, currentVehicle, true);
        world = currentWorld;
        vehicle = currentVehicle;
        forwardEnabled = !forwardEnabled;
        return forwardEnabled;
    }

    public float forward(float manualInput) {
        return forwardEnabled ? 1.0F : manualInput;
    }

    public float vertical(float manualInput, float automaticInput) {
        return heightEnabled ? automaticInput : manualInput;
    }
}
