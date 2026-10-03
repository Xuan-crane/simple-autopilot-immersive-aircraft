package dev.xuancrane.aircraftautoforward;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TerrainProfileTest {
    private TerrainProfile.Reading scan(TerrainProfile.Probe probe) {
        return TerrainProfile.scan(probe, 0, 0, 20, 2, 1, 0, 0.4, -64, 320);
    }
    @Test void flatSurfaceAndWaterSurfaceHaveStableReference() {
        var result = scan((x, z, from, to) -> 10);
        assertTrue(result.known());
        assertEquals(10, result.ground());
        assertEquals(10, result.aheadGround());
    }
    @Test void risingGroundIsDetectedBeforeReachingIt() {
        var result = scan((x, z, from, to) -> x > 8 ? 25 : 10);
        assertEquals(10, result.ground());
        assertEquals(25, result.aheadGround());
    }
    @Test void unknownChunkAheadFailsClosed() {
        assertFalse(scan((x, z, from, to) -> x > 8 ? Double.NaN : 10).known());
    }
    @Test void voidUnderOneHullEdgeFailsClosed() {
        assertFalse(scan((x, z, from, to) -> z > 1 ? Double.NaN : 10).known());
    }
    @Test void sidewaysHullEdgeIsIncluded() {
        var result = scan((x, z, from, to) -> z > 1 ? 15 : 10);
        assertEquals(15, result.ground());
    }
    @Test void boundsAndProbeCountRemainLimitedAtHighSpeed() {
        int[] count = {0};
        TerrainProfile.scan((x, z, top, low) -> {
            count[0]++;
            assertTrue(Math.abs(x) <= 48 && top < 320 && low >= -64);
            return 10;
        }, 0, 0, 310, 2, 1, 0, 100, -64, 320);
        assertTrue(count[0] <= 53);
    }
}
