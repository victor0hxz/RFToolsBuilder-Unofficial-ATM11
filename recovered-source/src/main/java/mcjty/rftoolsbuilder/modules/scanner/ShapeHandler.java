package mcjty.rftoolsbuilder.modules.scanner;

import mcjty.rftoolsbuilder.shapes.ShapeDataManagerServer;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent.Pre;

public class ShapeHandler {
   @SubscribeEvent
   public void onWorldTick(Pre event) {
      if (event.getLevel().dimension().equals(Level.OVERWORLD)) {
         ShapeDataManagerServer.handleWork();
      }
   }
}
