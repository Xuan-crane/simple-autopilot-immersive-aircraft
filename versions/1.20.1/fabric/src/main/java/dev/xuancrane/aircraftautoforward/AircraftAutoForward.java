package dev.xuancrane.aircraftautoforward;

import com.mojang.blaze3d.platform.InputConstants;
import immersive_aircraft.entity.VehicleEntity;
import immersive_aircraft.entity.EngineVehicle;
import immersive_aircraft.entity.GyrodyneEntity;
import immersive_aircraft.entity.AirplaneEntity;
import immersive_aircraft.item.upgrade.VehicleStat;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import org.lwjgl.glfw.GLFW;

import java.util.Set;

public final class AircraftAutoForward implements ClientModInitializer {
    public static final String MOD_ID = "aircraft_autoforward";
    private static final Set<String> SUPPORTED = Set.of("airship", "cargo_airship", "warship", "gyrodyne",
            "biplane", "quadrocopter", "bamboo_hopper");
    private static final CruiseState STATE = new CruiseState();
    private static String lastWarning = "";
    private static long lastWarningNanos;
    private static final KeyMapping TOGGLE = new KeyMapping(
            "key.aircraft_autoforward.toggle", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, "key.categories.aircraft_autoforward");
    private static final KeyMapping HEIGHT = key("height", GLFW.GLFW_KEY_H);
    private static final KeyMapping HIGHER = key("higher", GLFW.GLFW_KEY_PAGE_UP);
    private static final KeyMapping LOWER = key("lower", GLFW.GLFW_KEY_PAGE_DOWN);

    private static KeyMapping key(String name, int code) {
        return new KeyMapping("key.aircraft_autoforward." + name,     InputConstants.Type.KEYSYM, code, "key.categories.aircraft_autoforward");
    }

    @Override public void onInitializeClient() {
        for (KeyMapping key : new KeyMapping[]{TOGGLE, HEIGHT, HIGHER, LOWER}) KeyBindingHelper.registerKeyBinding(key);
        ClientTickEvents.START_CLIENT_TICK.register(client -> validate());
        ClientTickEvents.END_CLIENT_TICK.register(this::afterTick);
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> { STATE.clear(); drainKeys(); });
    }

    private static VehicleEntity eligibleVehicle(Minecraft client) {
        if (client.level == null || client.player == null || !client.player.isAlive()
                || client.player.isSpectator()) {
            return null;
        }
        if (!(client.player.getVehicle() instanceof VehicleEntity vehicle)
                || !vehicle.isAlive() || vehicle.isRemoved()
                || vehicle.getControllingPassenger() != client.player) {
            return null;
        }
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(vehicle.getType());
        return id.getNamespace().equals("immersive_aircraft") && SUPPORTED.contains(id.getPath())
                ? vehicle : null;
    }

    private static void show(String key, Object... args) {
        Minecraft client = Minecraft.getInstance();
        if (client.player != null) {
            client.player.displayClientMessage(Component.translatable("message.aircraft_autoforward." + key, args), true);
        }
    }

    private static VehicleEntity validate() {
        Minecraft client = Minecraft.getInstance();
        VehicleEntity vehicle = eligibleVehicle(client);
        if (STATE.validate(client.level, vehicle == null ? null : vehicle.getUUID(), vehicle != null)) {
            show("reset");
        }
        return vehicle;
    }

    // Only gates NEW toggle clicks (e.g. typing V in chat). Never gates flight output.
    private static boolean toggleInputUnavailable() {
        Minecraft client = Minecraft.getInstance();
        return client.screen != null || client.getOverlay() != null || client.isPaused() || !client.isWindowActive();
    }

    private static void warning(String key) {
        long now = System.nanoTime();
        if (!key.equals(lastWarning) || now - lastWarningNanos > 3_000_000_000L) {
            show(key);
            lastWarning = key;
            lastWarningNanos = now;
        }
    }

    private void afterTick(Minecraft clientTick) {
        VehicleEntity vehicle = validate();
        if (toggleInputUnavailable()) {
            drainKeys();
            return;
        }
        while (TOGGLE.consumeClick()) {
            if (vehicle != null) {
                show(STATE.toggle(Minecraft.getInstance().level, vehicle.getUUID()) ? "on" : "off");
            } else if (Minecraft.getInstance().screen == null && Minecraft.getInstance().player != null) {
                show("unavailable");
            }
        }
        while (HEIGHT.consumeClick()) {
            if (vehicle != null) {
                boolean active = STATE.toggleHeight(Minecraft.getInstance().level, vehicle.getUUID());
                show(active ? "height_on" : "height_off", STATE.targetHeight());
            } else if (Minecraft.getInstance().screen == null && Minecraft.getInstance().player != null) {
                show("unavailable");
            }
        }
        while (HIGHER.consumeClick()) {
            if (vehicle != null) show("target_height", STATE.adjustHeight(2));
        }
        while (LOWER.consumeClick()) {
            if (vehicle != null) show("target_height", STATE.adjustHeight(-2));
        }
    }

    private static void drainKeys() {
        for (KeyMapping mapping : new KeyMapping[]{TOGGLE, HEIGHT, HIGHER, LOWER}) {
            while (mapping.consumeClick()) {
                // A click from an old connection must never enable cruise in the next world.
            }
        }
    }

    /** Called only at the original mod's local pilot input site. */
    public static PilotControls.Input flightInput(Entity aircraft, float manualY, float manualZ) {
        PilotControls.Input manual = new PilotControls.Input(manualY, manualZ);
        VehicleEntity current = validate();
        if (current != aircraft) {
            return manual;
        }
        if (!STATE.isActive()) return manual;
        boolean airplane = current instanceof AirplaneEntity;
        if (!STATE.heightEnabled()) return PilotControls.route(airplane, STATE.forwardEnabled(), false,
                manualY, manualZ, 0, 1);
        // On genuine sensor/power failures, do not let unrelated keys control an enabled axis.
        // Airplanes cannot hover: retain V's throttle even when altitude telemetry is unavailable.
        PilotControls.Input blocked = PilotControls.route(airplane, STATE.forwardEnabled(), true,
                manualY, manualZ, 0, 0);
        if (current instanceof EngineVehicle engine && engine.getFuelUtilization() <= 0) {
            warning("no_power");
            return blocked;
        }
        TerrainScanner.Reading terrain = TerrainScanner.scan(current, STATE.targetHeight());
        if (!terrain.valid()) {
            warning(terrain.failure());
            return blocked;
        }
        if (current instanceof AirplaneEntity plane) {
            float pitch = AirplaneAltitudeController.control(terrain.bottom(), terrain.ground(),
                    terrain.aheadGround(), STATE.targetHeight(), current.getDeltaMovement().y,
                    current.getDeltaMovement().horizontalDistance(), current.getXRot(),
                    plane.getProperties().get(VehicleStat.PITCH_SPEED),
                    plane.getProperties().getAdditive(VehicleStat.STABILIZER));
            return PilotControls.route(true, STATE.forwardEnabled(), true, manualY, manualZ, pitch, 1);
        }
        float forward = 1;
        AltitudeController.Output output = AltitudeController.control(terrain.bottom(), terrain.ground(),
                terrain.aheadGround(), STATE.targetHeight(), current.getDeltaMovement().y);
        // The human-powered rotor needs continuous forward input to spin up before takeoff.
        boolean spinningUp = current instanceof GyrodyneEntity gyro && gyro.getEngineTarget() < 1.0F;
        if (spinningUp && terrain.obstacle()) {
            warning("obstacle_near");
            return blocked;
        }
        if (STATE.forwardEnabled()) {
            forward = spinningUp ? 1.0F : terrain.obstacle() ? 0.0F : output.forward();
        }
        return PilotControls.route(false, STATE.forwardEnabled(), true, manualY, manualZ, output.vertical(), forward);
    }
}
