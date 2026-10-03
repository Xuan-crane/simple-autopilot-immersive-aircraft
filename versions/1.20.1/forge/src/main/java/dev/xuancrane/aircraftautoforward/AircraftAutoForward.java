package dev.xuancrane.aircraftautoforward;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
@Mod(AircraftAutoForward.MOD_ID)
public final class AircraftAutoForward {
    public static final String MOD_ID = "aircraft_autoforward";
    public AircraftAutoForward() { DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> AircraftClient::new); }
}
