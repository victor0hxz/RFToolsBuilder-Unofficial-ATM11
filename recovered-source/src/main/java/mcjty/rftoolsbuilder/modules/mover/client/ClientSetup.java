package mcjty.rftoolsbuilder.modules.mover.client;

import java.util.Collections;
import java.util.List;
import net.minecraft.resources.Identifier;

public class ClientSetup {
   public static void initClient() {
   }

   public static List<Identifier> onTextureStitch() {
      return Collections.singletonList(Identifier.fromNamespaceAndPath("rftoolsbuilder", "block/effects/black"));
   }
}
