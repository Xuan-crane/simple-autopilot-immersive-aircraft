package dev.xuancrane.aircraftautoforward;

import immersive_aircraft.entity.VehicleEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class TerrainScanner {
    private TerrainScanner() {}
    public record Reading(String failure, double bottom, double ground, double aheadGround, boolean obstacle) {
        public boolean valid() { return failure == null; }
    }

    public static Reading scan(VehicleEntity vehicle, int clearance) {
        Level level = vehicle.level();
        var shapes = vehicle.getShapes();
        AABB hull = vehicle.getBoundingBox();
        for (AABB shape : shapes) hull = hull.minmax(shape);
        double bottom = hull.minY;
        double radius = Math.max(hull.getXsize(), hull.getZsize()) * 0.5;
        Vec3 velocity = vehicle.getDeltaMovement();
        var heading = vehicle.getForwardDirection();
        // Follow the actual drift direction when moving, the nose when nearly stationary.
        double speed = velocity.horizontalDistance();
        double dx = speed > 0.05 ? velocity.x : heading.x;
        double dz = speed > 0.05 ? velocity.z : heading.z;
        TerrainProfile.Reading profile = TerrainProfile.scan((x, z, top, low) -> {
            if (top <= low || !level.hasChunk(BlockPos.containing(x, 0, z).getX() >> 4,
                    BlockPos.containing(x, 0, z).getZ() >> 4)) return Double.NaN;
            var hit = level.clip(new ClipContext(new Vec3(x, top, z), new Vec3(x, low, z),
                    ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, vehicle));
            return hit.getType() == HitResult.Type.MISS || hit.isInside() ? Double.NaN : hit.getLocation().y;
        }, hull.getCenter().x, hull.getCenter().z, bottom, radius, dx, dz, speed,
                level.getMinY(), level.getMaxY() + 1.0);
        if (!profile.known()) return new Reading("unknown_ground", bottom, 0, 0, false);
        double directionLength = Math.hypot(dx, dz);
        final double ux = directionLength > 1e-6 ? dx / directionLength : 0;
        final double uz = directionLength > 1e-6 ? dz / directionLength : 0;
        ObstacleForecast.Reading obstacle = ObstacleForecast.scan(speed, radius, (start, end) -> {
            double top = Double.NEGATIVE_INFINITY;
            for (AABB shape : shapes) {
                AABB swept = shape.move(ux * start, 0.1, uz * start)
                        .expandTowards(ux * (end - start), 0, uz * (end - start)).deflate(0.02);
                if (!loaded(level, swept)) return new ObstacleForecast.Slice(false, 0);
                for (var collision : level.getBlockCollisions(vehicle, swept)) {
                    if (!collision.isEmpty()) top = Math.max(top, collision.max(Direction.Axis.Y));
                }
            }
            return new ObstacleForecast.Slice(true, top);
        });
        if (!obstacle.known()) return new Reading("unknown_ground", bottom, 0, 0, false);
        if (obstacle.urgent()) return new Reading("obstacle_near", bottom, 0, 0, true);
        double aheadGround = Math.max(profile.aheadGround(), obstacle.top());
        double targetBottom = Math.max(profile.ground(), aheadGround) + clearance;
        if (targetBottom + hull.getYsize() + 2 >= level.getMaxY() + 1.0) {
            return new Reading("height_limit", bottom, 0, 0, false);
        }
        // Do not try to climb through a roof. Avoid broad filled boxes between separate hull parts.
        for (AABB shape : shapes) {
            AABB upward = shape.move(0, 0.1, 0).expandTowards(0, Math.max(2, velocity.y * 30), 0).deflate(0.02);
            if (!loaded(level, upward)) return new Reading("unknown_ground", bottom, 0, 0, false);
            if (level.getBlockCollisions(vehicle, upward).iterator().hasNext()) {
                return new Reading("ceiling", bottom, 0, 0, false);
            }
        }
        return new Reading(null, bottom, profile.ground(), aheadGround, obstacle.found());
    }

    private static boolean loaded(Level level, AABB box) {
        // Collision iteration checks neighboring cells too; verify that border before querying.
        for (int cx = (int) Math.floor(box.minX - 1) >> 4; cx <= ((int) Math.floor(box.maxX + 1) >> 4); cx++) {
            for (int cz = (int) Math.floor(box.minZ - 1) >> 4; cz <= ((int) Math.floor(box.maxZ + 1) >> 4); cz++) {
                if (!level.hasChunk(cx, cz)) return false;
            }
        }
        return true;
    }
}
