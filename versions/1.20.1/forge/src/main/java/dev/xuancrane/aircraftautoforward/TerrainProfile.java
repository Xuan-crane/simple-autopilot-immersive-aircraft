package dev.xuancrane.aircraftautoforward;

/** Bounded terrain sampling, independent of Minecraft so unknown/edge cases can be tested. */
public final class TerrainProfile {
    @FunctionalInterface
    public interface Probe {
        // NaN means unloaded, void, or no surface within the bounded vertical scan.
        double surface(double x, double z, double fromY, double toY);
    }
    public record Reading(boolean known, double ground, double aheadGround) {}

    public static Reading scan(Probe probe, double x, double z, double bottom, double radius,
                               double directionX, double directionZ, double speed, double minY, double maxY) {
        double floor = Math.max(minY, bottom - 256);
        double current = Double.NEGATIVE_INFINITY;
        // A cross under the hull includes both its center and its edges.
        double[][] offsets = {{0, 0}, {-radius, 0}, {radius, 0}, {0, -radius}, {0, radius}};
        for (double[] offset : offsets) {
            double ground = probe.surface(x + offset[0], z + offset[1], bottom + 0.25, floor);
            if (!Double.isFinite(ground)) return new Reading(false, 0, 0);
            current = Math.max(current, ground);
        }
        double ahead = current;
        double length = Math.hypot(directionX, directionZ);
        if (length < 1e-6) return new Reading(true, current, current);
        double dx = directionX / length;
        double dz = directionZ / length;
        double distance = Math.min(48, Math.max(8, radius + speed * 60 + 6));
        int steps = (int) Math.ceil(distance / 3.0);
        for (int i = 1; i <= steps; i++) {
            double d = distance * i / steps;
            for (int lane = -1; lane <= 1; lane++) {
                double ground = probe.surface(x + dx * d - dz * radius * lane,
                        z + dz * d + dx * radius * lane, Math.min(maxY - 0.01, bottom + 48), floor);
                if (!Double.isFinite(ground)) return new Reading(false, 0, 0);
                ahead = Math.max(ahead, ground);
            }
        }
        return new Reading(true, current, ahead);
    }
}
