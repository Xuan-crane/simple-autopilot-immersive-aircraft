package dev.xuancrane.aircraftautoforward;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import java.util.ArrayList;

/** Renders cached state only: no sensor scans, preferences writes, or shared input mutation. */
public final class AutopilotHud {
    private AutopilotHud() {}
    public static void render(GuiGraphics graphics) {
        Minecraft client = Minecraft.getInstance();
        AircraftAutoForward.HudSnapshot snapshot = AircraftAutoForward.hudSnapshot();
        if (snapshot == null || client.options.hideGui) return;
        var settings = AircraftAutoForward.settings();
        if (!settings.hudVisible()) return;
        var lines = new ArrayList<Component>();
        lines.add(Component.translatable("hud.aircraft_autoforward.title"));
        lines.add(Component.translatable("hud.aircraft_autoforward.modes",
                Component.translatable("hud.aircraft_autoforward." + (snapshot.forward() ? "on" : "off")),
                Component.translatable("hud.aircraft_autoforward." + (snapshot.height() ? "on" : "off"))));
        lines.add(Component.translatable("hud.aircraft_autoforward.target", snapshot.targetHeight()));
        if (snapshot.height() && !snapshot.reason().isEmpty())
            lines.add(Component.translatable("hud.aircraft_autoforward.reason." + snapshot.reason()));
        if (snapshot.preferencesError()) lines.add(Component.translatable("hud.aircraft_autoforward.preferences_error"));
        int desiredWidth = 0;
        for (Component line : lines) desiredWidth = Math.max(desiredWidth, client.font.width(line));
        var box = HudLayout.bounds(graphics.guiWidth(), graphics.guiHeight(), desiredWidth + 12, lines.size() * 11 + 10, settings.corner());
        if (box.width() < 9 || box.height() < 11) return;
        graphics.enableScissor(box.x(), box.y(), box.x() + box.width(), box.y() + box.height());
        try {
            graphics.fill(box.x(), box.y(), box.x() + box.width(), box.y() + box.height(), 0xB0182028);
            int y = box.y() + 5;
            for (Component line : lines) {
                if (y + 9 > box.y() + box.height() - 1) break;
                String fitted = client.font.plainSubstrByWidth(line.getString(), Math.max(0, box.width() - 10));
                graphics.drawString(client.font, fitted, box.x() + 5, y, 0xFFFFFFFF, false);
                y += 11;
            }
        } finally { graphics.disableScissor(); }
    }
}
