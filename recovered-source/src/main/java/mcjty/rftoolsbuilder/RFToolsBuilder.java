package mcjty.rftoolsbuilder;

import java.util.function.Supplier;
import mcjty.lib.datagen.DataGen;
import mcjty.lib.modules.Modules;
import mcjty.rftoolsbuilder.modules.builder.BuilderModule;
import mcjty.rftoolsbuilder.modules.mover.MoverModule;
import mcjty.rftoolsbuilder.modules.scanner.ScannerModule;
import mcjty.rftoolsbuilder.modules.shield.ShieldModule;
import mcjty.rftoolsbuilder.setup.ClientSetup;
import mcjty.rftoolsbuilder.setup.Config;
import mcjty.rftoolsbuilder.setup.ModSetup;
import mcjty.rftoolsbuilder.setup.RFToolsBuilderMessages;
import mcjty.rftoolsbuilder.setup.Registration;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.data.event.GatherDataEvent.Client;

@Mod("rftoolsbuilder")
public class RFToolsBuilder {
   public static final String MODID = "rftoolsbuilder";
   public static final ModSetup setup = new ModSetup();
   private final Modules modules = new Modules();
   public static RFToolsBuilder instance;

   public RFToolsBuilder(ModContainer mod, IEventBus bus, Dist dist) {
      instance = this;
      this.setupModules(bus, dist);
      Config.register(mod, bus, this.modules);
      Registration.register(bus);
      bus.addListener(setup::init);
      bus.addListener(this.modules::init);
      bus.addListener(this::onDataGen);
      bus.addListener(RFToolsBuilderMessages::registerMessages);
      bus.addListener(setup.getBlockCapabilityRegistrar(Registration.RBLOCKS));
      if (dist.isClient()) {
         bus.addListener(RFToolsBuilderMessages::registerClientMessages);
         bus.addListener(ClientSetup::init);
         bus.addListener(ClientSetup::registerKeyBinds);
         bus.addListener(this.modules::initClient);
      }
   }

   public static <T extends Item> Supplier<T> tab(Supplier<T> supplier) {
      return setup.tab(supplier);
   }

   private void onDataGen(Client event) {
      DataGen datagen = new DataGen("rftoolsbuilder", event);
      this.modules.datagen(datagen, event.getLookupProvider());
      datagen.generate();
   }

   private void setupModules(IEventBus bus, Dist dist) {
      this.modules.register(new BuilderModule(bus));
      this.modules.register(new ShieldModule(bus, dist));
      this.modules.register(new ScannerModule(bus));
      this.modules.register(new MoverModule(bus, dist));
   }
}
