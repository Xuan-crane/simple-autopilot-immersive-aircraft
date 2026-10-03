package dev.xuancrane.aircraftautoforward;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.Properties;

/** Client preferences only. Never persists flight toggles, identities or world state. */
public final class ClientPreferences {
    public enum HudCorner { TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT }

    public record Settings(int targetHeight, int heightStep, boolean hudVisible, HudCorner corner) {
        public Settings {
            targetHeight = Math.max(4, Math.min(64, targetHeight));
            heightStep = Math.max(1, Math.min(16, heightStep));
            if (corner == null) corner = HudCorner.TOP_LEFT;
        }
    }

    public static final Settings DEFAULTS = new Settings(10, 2, true, HudCorner.TOP_LEFT);
    private final Path file;
    private Settings settings = DEFAULTS;
    private boolean loadFailed;
    private boolean saveFailed;

    public ClientPreferences(Path file) {
        this.file = Objects.requireNonNull(file).toAbsolutePath().normalize();
        load();
    }

    public Settings get() { return settings; }
    public boolean loadFailed() { return loadFailed; }
    public boolean saveFailed() { return saveFailed; }

    private void load() {
        try {
            if (Files.size(file) > 65536) throw new IOException("Configuration is too large");
            Properties values = new Properties();
            try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                values.load(reader);
            }
            HudCorner corner;
            try { corner = HudCorner.valueOf(values.getProperty("hudCorner", DEFAULTS.corner().name()).trim()); }
            catch (IllegalArgumentException ignored) { corner = DEFAULTS.corner(); }
            String visibility = values.getProperty("hudVisible", "true").trim();
            boolean visible = "false".equalsIgnoreCase(visibility) ? false : DEFAULTS.hudVisible();
            settings = new Settings(integer(values, "targetHeight", DEFAULTS.targetHeight()),
                    integer(values, "heightStep", DEFAULTS.heightStep()), visible, corner);
        } catch (NoSuchFileException ignored) {
            // First run: keep defaults without writing an unused configuration file.
        } catch (IOException | IllegalArgumentException | SecurityException ignored) {
            loadFailed = true;
            // Leave an unreadable or malformed file intact until the user explicitly changes settings.
        }
    }

    private static int integer(Properties values, String key, int fallback) {
        try { return Integer.parseInt(values.getProperty(key, Integer.toString(fallback)).trim()); }
        catch (NumberFormatException ignored) { return fallback; }
    }

    /** Applies preferences immediately; false means the session works but saving failed. */
    public boolean update(Settings next) {
        Objects.requireNonNull(next);
        if (settings.equals(next) && !saveFailed && !loadFailed) return true;
        settings = next;
        Path temporary = null;
        try {
            Files.createDirectories(file.getParent());
            temporary = Files.createTempFile(file.getParent(), "aircraft-autopilot-", ".tmp");
            Properties values = new Properties();
            values.setProperty("targetHeight", Integer.toString(next.targetHeight()));
            values.setProperty("heightStep", Integer.toString(next.heightStep()));
            values.setProperty("hudVisible", Boolean.toString(next.hudVisible()));
            values.setProperty("hudCorner", next.corner().name());
            try (Writer writer = Files.newBufferedWriter(temporary, StandardCharsets.UTF_8)) {
                values.store(writer, "Simple Autopilot client preferences; flight toggles are never saved");
            }
            try {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException unsupported) {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
            }
            saveFailed = false;
            loadFailed = false;
            return true;
        } catch (IOException | SecurityException ignored) {
            saveFailed = true;
            return false;
        } finally {
            if (temporary != null) {
                try { Files.deleteIfExists(temporary); }
                catch (IOException | SecurityException ignored) { /* Do not interrupt client input. */ }
            }
        }
    }
}
