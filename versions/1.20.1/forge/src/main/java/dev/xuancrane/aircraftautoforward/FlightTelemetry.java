package dev.xuancrane.aircraftautoforward;

/** Last observation from the actual pilot-input calculation, not a second terrain scan. */
public final class FlightTelemetry {
    private static final long MAX_AGE_NANOS = 2_000_000_000L;
    private boolean observed;
    private long observedAt;
    private String reason = "waiting";

    public void reset() {
        observed = false;
        reason = "waiting";
    }

    public void tracking(long now) {
        observed = true;
        observedAt = now;
        reason = "";
    }

    public void paused(String cause, long now) {
        observed = true;
        observedAt = now;
        reason = cause == null || cause.isBlank() ? "waiting" : cause;
    }

    public String reason(long now) {
        long age = now - observedAt;
        return !observed || age < 0 || age > MAX_AGE_NANOS ? "waiting" : reason;
    }
}
