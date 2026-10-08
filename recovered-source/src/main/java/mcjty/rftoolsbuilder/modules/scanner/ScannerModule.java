package mcjty.rftoolsbuilder.modules.scanner;

import java.util.function.Supplier;
import mcjty.lib.blocks.RBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.datagen.DataGen;
import mcjty.lib.items.BaseBlockItem;
import mcjty.lib.modules.IModule;
import mcjty.rftoolsbuilder.modules.scanner.blocks.ComposerBlock;
import mcjty.rftoolsbuilder.modules.scanner.blocks.ComposerTileEntity;
import mcjty.rftoolsbuilder.modules.scanner.blocks.ProjectorBlock;
import mcjty.rftoolsbuilder.modules.scanner.blocks.ProjectorTileEntity;
import mcjty.rftoolsbuilder.modules.scanner.blocks.ScannerBlock;
import mcjty.rftoolsbuilder.modules.scanner.blocks.ScannerTileEntity;
import mcjty.rftoolsbuilder.modules.scanner.client.GuiComposer;
import mcjty.rftoolsbuilder.modules.scanner.client.GuiProjector;
import mcjty.rftoolsbuilder.modules.scanner.client.GuiScanner;
import mcjty.rftoolsbuilder.setup.Config;
import mcjty.rftoolsbuilder.setup.Registration;
import mcjty.rftoolsbuilder.shapes.ShapeDataManagerClient;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;

public class ScannerModule implements IModule {
   public static final RBlock<ScannerBlock, BlockItem, ScannerTileEntity> SCANNER = Registration.RBLOCKS
      .registerBlock(
         "scanner",
         ScannerTileEntity.class,
         ScannerBlock::new,
         block -> new BaseBlockItem((Block)block.get(), Registration.createStandardProperties()),
         ScannerTileEntity::new
      );
   public static final Supplier<BlockEntityType<ScannerTileEntity>> TYPE_SCANNER = SCANNER.be();
   public static final Supplier<MenuType<GenericContainer>> CONTAINER_SCANNER = Registration.CONTAINERS
      .register("scanner", GenericContainer::createContainerType);
   public static final RBlock<ComposerBlock, BlockItem, ComposerTileEntity> COMPOSER = Registration.RBLOCKS
      .registerBlock(
         "composer",
         ComposerTileEntity.class,
         ComposerBlock::new,
         block -> new BaseBlockItem((Block)block.get(), Registration.createStandardProperties()),
         ComposerTileEntity::new
      );
   public static final Supplier<BlockEntityType<ComposerTileEntity>> TYPE_COMPOSER = COMPOSER.be();
   public static final Supplier<MenuType<GenericContainer>> CONTAINER_COMPOSER = Registration.CONTAINERS
      .register("composer", GenericContainer::createContainerType);
   public static final RBlock<ProjectorBlock, BlockItem, ProjectorTileEntity> PROJECTOR = Registration.RBLOCKS
      .registerBlock(
         "projector",
         ProjectorTileEntity.class,
         ProjectorBlock::new,
         block -> new BaseBlockItem((Block)block.get(), Registration.createStandardProperties()),
         ProjectorTileEntity::new
      );
   public static final Supplier<BlockEntityType<ProjectorTileEntity>> TYPE_PROJECTOR = PROJECTOR.be();
   public static final Supplier<MenuType<GenericContainer>> CONTAINER_PROJECTOR = Registration.CONTAINERS
      .register("projector", GenericContainer::createContainerType);

   public ScannerModule(IEventBus bus) {
      bus.addListener(this::registerMenuScreens);
   }

   public void init(FMLCommonSetupEvent event) {
      NeoForge.EVENT_BUS.register(new ShapeHandler());
   }

   public void initClient(FMLClientSetupEvent event) {
      NeoForge.EVENT_BUS.addListener(ShapeDataManagerClient::cleanupOldRenderers);
   }

   public void registerMenuScreens(RegisterMenuScreensEvent event) {
      GuiComposer.register(event);
      GuiProjector.register(event);
      GuiScanner.register(event);
   }

   public void initConfig(IEventBus bus) {
      ScannerConfiguration.init(Config.SERVER_BUILDER, Config.CLIENT_BUILDER);
   }

   public void initDatagen(DataGen dataGen, Provider provider) {
   }
}
