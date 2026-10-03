package dev.xuancrane.aircraftautoforward;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class ClientPreferencesTest {
    @TempDir Path directory;

    @Test void firstRunUsesDefaultsWithoutWritingOrEnablingFlight() {
        Path file = directory.resolve("config/prefs.properties");
        var preferences = new ClientPreferences(file);
        assertEquals(ClientPreferences.DEFAULTS, preferences.get());
        assertFalse(Files.exists(file));
        assertFalse(preferences.loadFailed());
        var state = new CruiseState();
        state.setTargetHeight(preferences.get().targetHeight());
        assertFalse(state.isActive());
    }

    @Test void restartRestoresPreferencesButNeverFlightModesOrWorldIdentities() throws Exception {
        Path file = directory.resolve("config/prefs.properties");
        var saved = new ClientPreferences.Settings(38, 5, false, ClientPreferences.HudCorner.BOTTOM_RIGHT);
        assertTrue(new ClientPreferences(file).update(saved));
        var loaded = new ClientPreferences(file);
        assertEquals(saved, loaded.get());
        var state = new CruiseState();
        state.setTargetHeight(loaded.get().targetHeight());
        assertEquals(38, state.targetHeight());
        assertFalse(state.isActive());
        String content = Files.readString(file);
        assertFalse(content.contains("forwardEnabled"));
        assertFalse(content.contains("heightEnabled"));
        assertFalse(content.contains("vehicle="));
    }

    @Test void badIndividualValuesFallbackAndBoundsAreClamped() throws Exception {
        Path file = directory.resolve("prefs.properties");
        Files.writeString(file,"targetHeight=999\nheightStep=nonsense\nhudVisible=invalid\nhudCorner=ELSEWHERE\n");
        var preferences = new ClientPreferences(file);
        assertEquals(new ClientPreferences.Settings(64,2,true,ClientPreferences.HudCorner.TOP_LEFT),preferences.get());
        assertEquals(new ClientPreferences.Settings(4,1,true,ClientPreferences.HudCorner.TOP_LEFT),
                new ClientPreferences.Settings(Integer.MIN_VALUE,Integer.MIN_VALUE,true,null));
        assertEquals(16,new ClientPreferences.Settings(10,Integer.MAX_VALUE,true,null).heightStep());
    }

    @Test void malformedFileIsPreservedUntilExplicitSave() throws Exception {
        Path file = directory.resolve("prefs.properties");
        String broken="targetHeight=\\uZZZZ\n";
        Files.writeString(file,broken);
        var preferences = new ClientPreferences(file);
        assertTrue(preferences.loadFailed());
        assertEquals(ClientPreferences.DEFAULTS,preferences.get());
        assertEquals(broken,Files.readString(file));
        assertTrue(preferences.update(ClientPreferences.DEFAULTS));
        assertFalse(preferences.loadFailed());
        assertEquals(ClientPreferences.DEFAULTS,new ClientPreferences(file).get());
    }

    @Test void oversizedFileIsIgnoredAndNotTruncated() throws Exception {
        Path file=directory.resolve("prefs.properties");
        Files.writeString(file,"x".repeat(65537));
        assertTrue(new ClientPreferences(file).loadFailed());
        assertEquals(65537,Files.size(file));
    }

    @Test void failedSaveKeepsSessionValueAndCanRecoverAfterFilesystemIsFixed() throws Exception {
        Path blocker=directory.resolve("not-a-directory");
        Files.writeString(blocker,"keep");
        var preferences=new ClientPreferences(blocker.resolve("prefs.properties"));
        var changed=new ClientPreferences.Settings(24,3,true,ClientPreferences.HudCorner.TOP_RIGHT);
        assertFalse(preferences.update(changed));
        assertEquals(changed,preferences.get());
        assertTrue(preferences.saveFailed());
        assertEquals("keep",Files.readString(blocker));
        // Remove only this test's own deliberate temporary obstruction.
        Files.delete(blocker);
        assertTrue(preferences.update(changed));
        assertFalse(preferences.saveFailed());
        assertEquals(changed,new ClientPreferences(blocker.resolve("prefs.properties")).get());
    }

    @Test void lifecycleResetRetainsClearanceAndDoesNotAuthorizeAnotherVehicle() {
        var state=new CruiseState();
        Object world=new Object(); UUID aircraft=UUID.randomUUID();
        state.setTargetHeight(28);
        state.toggle(world,aircraft); state.toggleHeight(world,aircraft);
        assertTrue(state.belongsTo(world,aircraft));
        assertFalse(state.belongsTo(new Object(),aircraft));
        assertFalse(state.belongsTo(world,UUID.randomUUID()));
        assertFalse(state.belongsTo(world,null));
        state.validate(null,null,false);
        assertFalse(state.belongsTo(world,aircraft));
        assertFalse(state.isActive());
        assertEquals(28,state.targetHeight());
    }
}
