package dev.xuancrane.aircraftautoforward;

/** A damped velocity controller: outputs normal controls, never changes position/velocity. */
public final class AltitudeController {
    private AltitudeController() {}

    public record Output(float vertical, float forward) {}

    public static Output control(double bottom, double ground, double aheadGround,
                                 double clearance, double verticalVelocity) {
        if (!Double.isFinite(bottom) || !Double.isFinite(ground) || !Double.isFinite(aheadGround)
                || !Double.isFinite(clearance) || !Double.isFinite(verticalVelocity) || clearance < 4) {
            throw new IllegalArgumentException("Invalid terrain/control input");
        }
        double target = Math.max(ground, aheadGround) + clearance;
        double error = target - bottom;
        // 0.16 blocks/tick ascent (3.2 blocks/sec), slower descent over drops.
        double wantedVelocity = clamp(error * 0.035, -0.10, 0.16);
        double vertical = clamp((wantedVelocity - verticalVelocity) * 6.0, -1, 1);
        if (Math.abs(error) < 0.25 && Math.abs(verticalVelocity) < 0.015) vertical = 0;
        // Reduce thrust before climbing terrain; removing thrust is not instant braking.
        double clearanceAhead = bottom - Math.max(ground, aheadGround);
        double forward = clamp((clearanceAhead - 2.0) / Math.max(2.0, clearance - 2.0), 0, 1);
        return new Output((float) vertical, (float) forward);
    }

    private static double clamp(double value, double low, double high) {
        return Math.max(low, Math.min(high, value));
    }
}
