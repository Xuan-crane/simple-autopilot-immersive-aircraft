package dev.xuancrane.aircraftautoforward;

/** Scans contiguous swept hull volumes; thin blocks cannot fall between sample points. */
public final class ObstacleForecast {
    public record Slice(boolean known, double top) {
        public static Slice clear() { return new Slice(true, Double.NEGATIVE_INFINITY); }
    }
    @FunctionalInterface public interface Probe { Slice sweep(double start, double end); }
    public record Reading(boolean known, boolean found, boolean urgent, double top) {}

    public static Reading scan(double speed, double radius, Probe probe) {
        double distance = Math.min(48, Math.max(12, speed * 60 + radius + 6));
        int steps = (int) Math.ceil(distance / 2);
        double highest = Double.NEGATIVE_INFINITY;
        boolean urgent = false;
        for (int i = 0; i < steps; i++) {
            double start = distance * i / steps;
            Slice slice = probe.sweep(start, distance * (i + 1) / steps);
            if (!slice.known() || Double.isNaN(slice.top())) return new Reading(false, false, false, 0);
            if (Double.isFinite(slice.top())) {
                highest = Math.max(highest, slice.top());
                urgent |= start < Math.max(3, speed * 20);
            }
        }
        return new Reading(true, Double.isFinite(highest), urgent, highest);
    }
}
