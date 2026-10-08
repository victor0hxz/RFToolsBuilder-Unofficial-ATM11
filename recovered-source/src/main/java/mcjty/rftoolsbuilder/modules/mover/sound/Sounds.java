package mcjty.rftoolsbuilder.modules.mover.sound;

import java.util.function.Supplier;
import mcjty.lib.varia.SoundTools;
import mcjty.rftoolsbuilder.setup.Registration;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public class Sounds {
   public static final Supplier<SoundEvent> MOVER_LOOP = Registration.SOUNDS
      .register("mover_loop", () -> SoundTools.createSoundEvent(Identifier.fromNamespaceAndPath("rftoolsbuilder", "mover_loop")));

   public static void init() {
   }
}
