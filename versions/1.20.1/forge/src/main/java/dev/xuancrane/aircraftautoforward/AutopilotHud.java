package dev.xuancrane.aircraftautoforward;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import java.util.ArrayList;

/** Paints cached state only: no terrain probes, file operations or lifecycle mutations. */
public final class AutopilotHud {
    private AutopilotHud() {}
    public static void render(GuiGraphics graphics, boolean forward, boolean height, int target,
                              ClientPreferences.Settings prefs, String reason,
                              boolean loadFailed, boolean saveFailed) {
        Minecraft client = Minecraft.getInstance();
        if (client.options.hideGui || !prefs.hudVisible()) return;
        var lines = new ArrayList<Component>();
        lines.add(Component.translatable("hud.aircraft_autoforward.title"));
        lines.add(Component.translatable("hud.aircraft_autoforward.axes",
                Component.translatable("hud.aircraft_autoforward." + (forward ? "on" : "off")),
                Component.translatable("hud.aircraft_autoforward." + (height ? "on" : "off")), target));
        if (height && !reason.isEmpty()) lines.add(Component.translatable("hud.aircraft_autoforward.reason." + reason));
        if (loadFailed) lines.add(Component.translatable("settings.aircraft_autoforward.load_error"));
        if (saveFailed) lines.add(Component.translatable("settings.aircraft_autoforward.save_error"));
        int desiredWidth = lines.stream().mapToInt(client.font::width).max().orElse(0) + 12;
        var box = HudLayout.bounds(graphics.guiWidth(), graphics.guiHeight(), desiredWidth,
                10 * lines.size() + 10, prefs.corner());
        graphics.enableScissor(box.x(), box.y(), box.x() + box.width(), box.y() + box.height());
        graphics.fill(box.x(), box.y(), box.x() + box.width(), box.y() + box.height(), 0xB0182028);
        for (int i = 0; i < lines.size(); i++) {
            if (5 + 10 * i + client.font.lineHeight > box.height() - 5) break;
            graphics.drawString(client.font, lines.get(i), box.x() + 6, box.y() + 5 + 10 * i,
                    i == 0 ? 0xFF80D8FF : i == 1 ? 0xFFFFFFFF : 0xFFFFD080, true);
        }
        graphics.disableScissor();
    }
}
