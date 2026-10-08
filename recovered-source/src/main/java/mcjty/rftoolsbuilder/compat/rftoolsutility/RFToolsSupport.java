package mcjty.rftoolsbuilder.compat.rftoolsutility;

import java.util.function.Function;
import javax.annotation.Nullable;
import mcjty.rftoolsbase.api.screens.IScreenModuleRegistry;
import mcjty.rftoolsbuilder.modules.mover.items.VehicleControlScreenModule;

public class RFToolsSupport {
   public static class GetScreenModuleRegistry implements Function<IScreenModuleRegistry, Void> {
      @Nullable
      public Void apply(IScreenModuleRegistry manager) {
         manager.registerModuleDataFactory("rftoolsbuilder:vehicle_control", VehicleControlScreenModule.EmptyData::new);
         return null;
      }
   }
}
