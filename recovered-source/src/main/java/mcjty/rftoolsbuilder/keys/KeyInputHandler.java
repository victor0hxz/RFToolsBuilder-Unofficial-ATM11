package mcjty.rftoolsbuilder.keys;

import mcjty.lib.typed.TypedMap;
import mcjty.rftoolsbuilder.setup.RFToolsBuilderMessages;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.InputEvent.Key;

public class KeyInputHandler {
   @SubscribeEvent
   public void onKeyInput(Key event) {
      if (KeyBindings.unmountVehicle.consumeClick()) {
         RFToolsBuilderMessages.sendToServer("unmount", TypedMap.builder());
      }
   }
}
