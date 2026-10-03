package dev.xuancrane.aircraftautoforward;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ObstacleForecastTest {
    @Test void detectsThinBlockBetweenTerrainRaySamples() {
        var result = ObstacleForecast.scan(0.2, 2, (start, end) ->
                start <= 11.35 && end >= 11.35 ? new ObstacleForecast.Slice(true, 25) : ObstacleForecast.Slice.clear());
        assertTrue(result.known());
        assertTrue(result.found());
        assertFalse(result.urgent());
        assertEquals(25, result.top());
    }
    @Test void nearbyObstacleRequiresTakeover() {
        var result = ObstacleForecast.scan(0.6, 2, (start, end) ->
                start <= 5 && end >= 5 ? new ObstacleForecast.Slice(true, 25) : ObstacleForecast.Slice.clear());
        assertTrue(result.urgent());
    }
    @Test void clearFlightCorridorHasNoObstacle() {
        var result = ObstacleForecast.scan(1, 2, (a, b) -> ObstacleForecast.Slice.clear());
        assertTrue(result.known());
        assertFalse(result.found());
    }
    @Test void unloadedCorridorFailsClosed() {
        assertFalse(ObstacleForecast.scan(1, 2, (a, b) -> new ObstacleForecast.Slice(false, 0)).known());
    }
    @Test void sweptSlicesHaveNoGapsAndStayBounded() {
        double[] previous = {0};
        int[] calls = {0};
        ObstacleForecast.scan(100, 8, (start, end) -> {
            assertEquals(previous[0], start);
            assertTrue(end - start <= 2 && end > start);
            previous[0] = end;
            calls[0]++;
            return ObstacleForecast.Slice.clear();
        });
        assertEquals(48, previous[0]);
        assertEquals(24, calls[0]);
    }
}
