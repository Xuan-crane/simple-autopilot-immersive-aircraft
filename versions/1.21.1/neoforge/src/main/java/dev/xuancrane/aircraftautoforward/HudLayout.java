package dev.xuancrane.aircraftautoforward;

/** Bounds are in scaled GUI coordinates; renderers clip text inside this box. */
public final class HudLayout {
    private HudLayout() {}
    public record Box(int x, int y, int width, int height) {}

    public static Box bounds(int screenWidth, int screenHeight, int desiredWidth, int desiredHeight,
                             ClientPreferences.HudCorner corner) {
        int sw = Math.max(0, screenWidth), sh = Math.max(0, screenHeight);
        int mx = Math.min(8, sw / 4), my = Math.min(8, sh / 4);
        int width = Math.max(0, Math.min(desiredWidth, sw - 2 * mx));
        int height = Math.max(0, Math.min(desiredHeight, sh - 2 * my));
        boolean right = corner == ClientPreferences.HudCorner.TOP_RIGHT || corner == ClientPreferences.HudCorner.BOTTOM_RIGHT;
        boolean bottom = corner == ClientPreferences.HudCorner.BOTTOM_LEFT || corner == ClientPreferences.HudCorner.BOTTOM_RIGHT;
        return new Box(right ? sw - mx - width : mx, bottom ? sh - my - height : my, width, height);
    }
}
