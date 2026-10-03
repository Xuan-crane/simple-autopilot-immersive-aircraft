package dev.xuancrane.aircraftautoforward;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.function.Predicate;
import dev.xuancrane.aircraftautoforward.ClientPreferences.Settings;
import dev.xuancrane.aircraftautoforward.ClientPreferences.HudCorner;

/** Edits a private draft. ESC/Cancel never save; this screen never pauses flight. */
public final class AutopilotSettingsScreen extends Screen {
    private final Screen parent;
    private final Predicate<Settings> save;
    private final boolean loadFailed;
    private Settings draft;
    private boolean saveFailed;
    private Button lowerHeight, higherHeight, lowerStep, higherStep, visibility, corner;

    public AutopilotSettingsScreen(Screen parent, Settings initial, Predicate<Settings> save, boolean loadFailed) {
        super(text("title"));
        this.parent = parent;
        this.draft = initial;
        this.save = save;
        this.loadFailed = loadFailed;
    }
    private static Component text(String key, Object... args) {
        return Component.translatable("settings.aircraft_autoforward." + key, args);
    }
    private void height(int delta) {
        draft = new Settings(Math.max(4, Math.min(64, draft.targetHeight() + delta)), draft.heightStep(), draft.hudVisible(), draft.corner());
        refresh();
    }
    private void step(int delta) {
        draft = new Settings(draft.targetHeight(), Math.max(1, Math.min(16, draft.heightStep() + delta)), draft.hudVisible(), draft.corner());
        refresh();
    }
    @Override protected void init() {
        int panelWidth = Math.min(280, Math.max(100, width - 20));
        int left = (width - panelWidth) / 2;
        int top = Math.max(4, (height - 170) / 2);
        int half = (panelWidth - 4) / 2;
        lowerHeight = addRenderableWidget(Button.builder(text("height_minus", draft.targetHeight()), b -> height(-1)).bounds(left, top + 24, half, 20).build());
        higherHeight = addRenderableWidget(Button.builder(text("height_plus", draft.targetHeight()), b -> height(1)).bounds(left + half + 4, top + 24, half, 20).build());
        lowerStep = addRenderableWidget(Button.builder(text("step_minus", draft.heightStep()), b -> step(-1)).bounds(left, top + 48, half, 20).build());
        higherStep = addRenderableWidget(Button.builder(text("step_plus", draft.heightStep()), b -> step(1)).bounds(left + half + 4, top + 48, half, 20).build());
        visibility = addRenderableWidget(Button.builder(text("hud", state(draft.hudVisible())), b -> {
            draft = new Settings(draft.targetHeight(), draft.heightStep(), !draft.hudVisible(), draft.corner()); refresh();
        }).bounds(left, top + 72, panelWidth, 20).build());
        corner = addRenderableWidget(Button.builder(text("corner", cornerName()), b -> {
            HudCorner[] corners = HudCorner.values();
            draft = new Settings(draft.targetHeight(), draft.heightStep(), draft.hudVisible(), corners[(draft.corner().ordinal() + 1) % corners.length]); refresh();
        }).bounds(left, top + 96, panelWidth, 20).build());
        int third = (panelWidth - 8) / 3;
        addRenderableWidget(Button.builder(text("save"), b -> {
            if (save.test(draft)) onClose(); else saveFailed = true;
        }).bounds(left, top + 124, third, 20).build());
        addRenderableWidget(Button.builder(text("cancel"), b -> onClose()).bounds(left + third + 4, top + 124, third, 20).build());
        addRenderableWidget(Button.builder(text("reset"), b -> { draft = ClientPreferences.DEFAULTS; saveFailed = false; refresh(); }).bounds(left + 2 * (third + 4), top + 124, third, 20).build());
        refresh();
    }
    private Component state(boolean visible) { return text(visible ? "on" : "off"); }
    private Component cornerName() { return text("corner." + draft.corner().name().toLowerCase(java.util.Locale.ROOT)); }
    private void refresh() {
        lowerHeight.setMessage(text("height_minus", draft.targetHeight())); higherHeight.setMessage(text("height_plus", draft.targetHeight()));
        lowerStep.setMessage(text("step_minus", draft.heightStep())); higherStep.setMessage(text("step_plus", draft.heightStep()));
        lowerHeight.active = draft.targetHeight() > 4; higherHeight.active = draft.targetHeight() < 64;
        lowerStep.active = draft.heightStep() > 1; higherStep.active = draft.heightStep() < 16;
        visibility.setMessage(text("hud", state(draft.hudVisible()))); corner.setMessage(text("corner", cornerName()));
    }
    @Override public boolean isPauseScreen() { return false; }
    @Override public void onClose() { if (minecraft != null) minecraft.setScreen(parent); }
    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        graphics.fill(0, 0, width, height, 0xD0182028);
        int top = Math.max(4, (height - 170) / 2);
        graphics.drawCenteredString(font, title, width / 2, top + 6, 0xFFFFFFFF);
        super.render(graphics, mouseX, mouseY, delta);
        if (loadFailed || saveFailed) graphics.drawCenteredString(font,
                text(saveFailed ? "save_error" : "load_error"), width / 2, top + 151, 0xFFFFD080);
    }
}
