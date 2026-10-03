package dev.xuancrane.aircraftautoforward;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Edits a detached settings draft. Closing or Escape cancels without writing it. */
public final class AutopilotSettingsScreen extends Screen {
    private final Screen parent;
    private int target;
    private int step;
    private boolean visible;
    private ClientPreferences.HudCorner corner;
    private boolean saveFailed;
    private Button targetLabel, stepLabel, visibilityButton, cornerButton;
    public AutopilotSettingsScreen(Screen parent) {
        super(Component.translatable("screen.aircraft_autoforward.title"));
        this.parent = parent;
        draft(AircraftAutoForward.settings());
    }
    private void draft(ClientPreferences.Settings settings) {
        target = settings.targetHeight(); step = settings.heightStep();
        visible = settings.hudVisible(); corner = settings.corner();
    }
    private static Component text(String key, Object... values) { return Component.translatable("screen.aircraft_autoforward." + key, values); }
    @Override protected void init() {
        int w = Math.max(20, Math.min(280, width - 16)), x = Math.max(0, (width - w) / 2);
        int h = Math.max(10, Math.min(20, (height - 40) / 6)), gap = Math.max(1, Math.min(4, (height - 40 - 6 * h) / 6));
        int y = 24, side = Math.max(12, Math.min(30, w / 5));
        addRenderableWidget(Button.builder(Component.literal("-"), b -> { target = Math.max(4, target - 1); refresh(); }).bounds(x, y, side, h).build());
        targetLabel = addRenderableWidget(Button.builder(text("target", target), b -> {}).bounds(x + side + 2, y, Math.max(1, w - 2 * side - 4), h).build());
        targetLabel.active = false;
        addRenderableWidget(Button.builder(Component.literal("+"), b -> { target = Math.min(64, target + 1); refresh(); }).bounds(x + w - side, y, side, h).build());
        y += h + gap;
        addRenderableWidget(Button.builder(Component.literal("-"), b -> { step = Math.max(1, step - 1); refresh(); }).bounds(x, y, side, h).build());
        stepLabel = addRenderableWidget(Button.builder(text("step", step), b -> {}).bounds(x + side + 2, y, Math.max(1, w - 2 * side - 4), h).build());
        stepLabel.active = false;
        addRenderableWidget(Button.builder(Component.literal("+"), b -> { step = Math.min(16, step + 1); refresh(); }).bounds(x + w - side, y, side, h).build());
        y += h + gap;
        visibilityButton = addRenderableWidget(Button.builder(text("hud", Component.translatable("hud.aircraft_autoforward." + (visible ? "on" : "off"))), b -> { visible = !visible; refresh(); }).bounds(x, y, w, h).build());
        y += h + gap;
        cornerButton = addRenderableWidget(Button.builder(text("corner", cornerName()), b -> { var all = ClientPreferences.HudCorner.values(); corner = all[(corner.ordinal() + 1) % all.length]; refresh(); }).bounds(x, y, w, h).build());
        y += h + gap;
        addRenderableWidget(Button.builder(text("reset"), b -> { draft(ClientPreferences.DEFAULTS); saveFailed = false; refresh(); }).bounds(x, y, w, h).build());
        y += h + gap;
        int half = Math.max(1, (w - 4) / 2);
        addRenderableWidget(Button.builder(text("save"), b -> {
            saveFailed = !AircraftAutoForward.applySettings(new ClientPreferences.Settings(target, step, visible, corner));
            if (!saveFailed) onClose();
        }).bounds(x, y, half, h).build());
        addRenderableWidget(Button.builder(text("cancel"), b -> onClose()).bounds(x + half + 4, y, half, h).build());
    }
    private Component cornerName() { return text("corner." + corner.name().toLowerCase(java.util.Locale.ROOT)); }
    private void refresh() {
        targetLabel.setMessage(text("target", target)); stepLabel.setMessage(text("step", step));
        visibilityButton.setMessage(text("hud", Component.translatable("hud.aircraft_autoforward." + (visible ? "on" : "off"))));
        cornerButton.setMessage(text("corner", cornerName()));
    }
    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        String heading = font.plainSubstrByWidth(title.getString(), Math.max(0, width - 8));
        graphics.drawString(font, heading, Math.max(4, (width - font.width(heading)) / 2), 8, 0xFFFFFFFF, false);
        if (saveFailed) {
            String error = font.plainSubstrByWidth(Component.translatable("message.aircraft_autoforward.save_failed").getString(), Math.max(0, width - 8));
            graphics.drawString(font, error, 4, Math.max(0, height - 11), 0xFFFF7777, false);
        }
    }
    @Override public boolean isPauseScreen() { return false; }
    @Override public void onClose() { Minecraft.getInstance().setScreen(parent); }
}
