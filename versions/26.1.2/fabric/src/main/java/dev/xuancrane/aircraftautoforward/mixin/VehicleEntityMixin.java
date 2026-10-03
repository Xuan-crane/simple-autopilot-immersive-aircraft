package dev.xuancrane.aircraftautoforward.mixin;

import dev.xuancrane.aircraftautoforward.AircraftAutoForward;
import dev.xuancrane.aircraftautoforward.PilotControls;
import immersive_aircraft.entity.VehicleEntity;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(value = VehicleEntity.class, remap = false)
public abstract class VehicleEntityMixin {
    @ModifyArgs(
            method = "tickPilot()V",
            at = @At(value = "INVOKE", target = "Limmersive_aircraft/entity/VehicleEntity;setInputs(FFF)V", ordinal = 0),
            require = 1,
            allow = 1
    )
    private void aircraftAutoForward$flight(Args args) {
        PilotControls.Input input = AircraftAutoForward.flightInput(
                (Entity) (Object) this, args.<Float>get(1), args.<Float>get(2));
        args.set(1, input.y());
        args.set(2, input.z());
    }
}
