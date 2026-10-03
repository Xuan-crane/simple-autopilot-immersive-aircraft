package dev.xuancrane.aircraftautoforward;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class CruiseStateTest {
    private final CruiseState state = new CruiseState();
    private final Object world = new Object();
    private final UUID aircraft = UUID.randomUUID();

    @Test void heightSwitchNeverEnablesForward() {
        assertTrue(state.toggleHeight(world, aircraft));
        assertTrue(state.isActive());
        assertTrue(state.heightEnabled());
        assertFalse(state.forwardEnabled());
        assertFalse(state.toggleHeight(world, aircraft));
        assertFalse(state.isActive());
        assertFalse(state.heightEnabled());
    }

    @Test void dismountCancelsBothModesWithoutAutomaticResume() {
        state.toggleHeight(world, aircraft);
        state.validate(world, null, false);
        assertFalse(state.isActive());
        assertFalse(state.heightEnabled());
        state.toggle(world, aircraft);
        assertFalse(state.heightEnabled());
    }

    @Test void targetHeightIsClampedAndSurvivesToggleButNotNewSession() {
        assertEquals(10, state.targetHeight());
        assertEquals(12, state.adjustHeight(2));
        assertEquals(64, state.adjustHeight(Integer.MAX_VALUE));
        assertEquals(4, state.adjustHeight(Integer.MIN_VALUE));
        state.clear();
        assertEquals(4, state.targetHeight());
        assertEquals(10, new CruiseState().targetHeight());
    }

    @Test void forwardAndHeightSwitchesAreIndependentInBothDirections() {
        state.toggleHeight(world, aircraft);
        state.toggle(world, aircraft);
        assertTrue(state.heightEnabled());
        assertTrue(state.forwardEnabled());
        state.toggle(world, aircraft);
        assertTrue(state.heightEnabled());
        assertFalse(state.forwardEnabled());
        state.toggle(world, aircraft);
        state.toggleHeight(world, aircraft);
        assertTrue(state.forwardEnabled());
        assertFalse(state.heightEnabled());
    }

    @Test void otherVerticalKeysCannotOverrideOrPauseAutomaticHeight() {
        state.toggle(world, aircraft);
        state.toggleHeight(world, aircraft);
        assertEquals(0.7F, state.vertical(-1, 0.7F));
        assertEquals(0.7F, state.vertical(0, 0.7F));
        assertEquals(0.7F, state.vertical(1, 0.7F));
        assertTrue(state.heightEnabled());
        assertTrue(state.forwardEnabled());
    }

    @Test void heightOnlyLeavesForwardEntirelyManual() {
        state.toggleHeight(world, aircraft);
        assertEquals(0, state.forward(0));
        assertEquals(0.5F, state.forward(0.5F));
        assertEquals(-1, state.forward(-1));
    }

    @Test void togglesOnAndOffWithoutChangingManualInputWhenOff() {
        assertEquals(0.4F, state.forward(0.4F));
        assertTrue(state.toggle(world, aircraft));
        assertEquals(1.0F, state.forward(0.0F));
        assertFalse(state.toggle(world, aircraft));
        assertEquals(-1.0F, state.forward(-1.0F));
    }

    @Test void continuesOnlyForSameWorldAndVehicle() {
        state.toggle(world, aircraft);
        assertFalse(state.validate(world, aircraft, true));
        assertTrue(state.isActive());
    }

    @Test void switchingAircraftCancelsEvenWithoutAnUnmountedTick() {
        state.toggle(world, aircraft);
        assertTrue(state.validate(world, UUID.randomUUID(), true));
        assertFalse(state.isActive());
    }

    @Test void switchingWorldCancelsEvenWithSameVehicleUuid() {
        state.toggle(world, aircraft);
        assertTrue(state.validate(new Object(), aircraft, true));
        assertFalse(state.isActive());
    }

    @Test void dismountDisconnectAndMissingWorldCancel() {
        state.toggle(world, aircraft);
        assertTrue(state.validate(null, null, false));
        assertFalse(state.isActive());
        assertFalse(state.validate(world, aircraft, true));
        assertFalse(state.isActive());
    }

    @Test void losingEligibilityCancelsAndDoesNotResume() {
        state.toggle(world, aircraft);
        assertTrue(state.validate(world, aircraft, false));
        state.validate(world, aircraft, true);
        assertFalse(state.isActive());
    }

    @Test void reverseAndZeroInputCannotPauseAutomaticForward() {
        state.toggle(world, aircraft);
        assertEquals(1.0F, state.forward(-1.0F));
        assertEquals(1, state.forward(0));
        assertTrue(state.forwardEnabled());
        assertEquals(1, state.forward(0));
    }

    @Test void manualControlReturnsOnlyAfterItsOwnToggleIsOff() {
        state.toggleHeight(world, aircraft);
        state.toggle(world, aircraft);
        state.toggleHeight(world, aircraft);
        assertEquals(-1, state.vertical(-1, 0.7F));
        assertEquals(1, state.forward(-1));
        state.toggle(world, aircraft);
        assertEquals(-1, state.forward(-1));
    }

    @Test void explicitClearIsIdempotentAndCannotResume() {
        state.toggle(world, aircraft);
        assertTrue(state.clear());
        assertFalse(state.clear());
        state.validate(world, aircraft, true);
        assertFalse(state.isActive());
    }

    @Test void nullVehicleCannotEnable() {
        assertFalse(state.toggle(world, null));
        assertFalse(state.isActive());
    }

    @Test void nullWorldCannotEnable() {
        assertFalse(state.toggle(null, aircraft));
        assertFalse(state.isActive());
    }
}
