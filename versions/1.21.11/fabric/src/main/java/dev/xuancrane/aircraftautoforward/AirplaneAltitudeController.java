package dev.xuancrane.aircraftautoforward;

/** Terrain-relative pitch command; negative Minecraft pitch raises the nose. */
public final class AirplaneAltitudeController {
    private AirplaneAltitudeController() {}

    public static float control(double bottom, double ground, double ahead, int clearance,
                                double velocityY, double horizontalSpeed, double pitch,
                                double pitchSpeed, double stabilizer) {
        if (!Double.isFinite(bottom + ground + ahead + velocityY + horizontalSpeed + pitch
                + pitchSpeed + stabilizer) || clearance < 4 || horizontalSpeed < 0 || pitchSpeed <= 0) {
            throw new IllegalArgumentException("Invalid airplane telemetry");
        }
        double error = Math.max(ground, ahead) + clearance - bottom;
        double wantedVelocity = clamp(error * 0.018, -0.10, 0.18);
        double correctedVelocity = wantedVelocity + (wantedVelocity - velocityY) * 0.35;
        double wantedPitch = Math.toDegrees(Math.atan2(-correctedVelocity, Math.max(0.15, horizontalSpeed)));
        // Slow airplanes must gain airspeed before making steep climbs.
        double climbLimit = horizontalSpeed < 0.25 ? 8 : 18;
        wantedPitch = clamp(wantedPitch, -climbLimit, horizontalSpeed < 0.25 ? 4 : 10);
        double stability = clamp(stabilizer, 0, 0.9);
        // Compensate upstream pitch *= (1 - stabilizer), including installed upgrades.
        double feedForward = wantedPitch * stability / ((1 - stability) * pitchSpeed);
        double correction = (wantedPitch - pitch) / (pitchSpeed * 20);
        return (float) clamp(feedForward + correction, -1, 1);
    }

    private static double clamp(double value, double low, double high) {
        return Math.max(low, Math.min(high, value));
    }
}
