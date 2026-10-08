package mcjty.rftoolsbuilder.modules.mover;

import java.util.function.Supplier;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.datagen.DataGen;
import mcjty.lib.items.BaseBlockItem;
import mcjty.lib.modules.IModule;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.mover.blocks.InvisibleMoverBE;
import mcjty.rftoolsbuilder.modules.mover.blocks.InvisibleMoverBlock;
import mcjty.rftoolsbuilder.modules.mover.blocks.MoverControlBlock;
import mcjty.rftoolsbuilder.modules.mover.blocks.MoverControllerTileEntity;
import mcjty.rftoolsbuilder.modules.mover.blocks.MoverStatusBlock;
import mcjty.rftoolsbuilder.modules.mover.blocks.MoverTileEntity;
import mcjty.rftoolsbuilder.modules.mover.blocks.VehicleBuilderTileEntity;
import mcjty.rftoolsbuilder.modules.mover.client.ClientSetup;
import mcjty.rftoolsbuilder.modules.mover.client.GuiMover;
import mcjty.rftoolsbuilder.modules.mover.client.GuiMoverController;
import mcjty.rftoolsbuilder.modules.mover.client.GuiVehicleBuilder;
import mcjty.rftoolsbuilder.modules.mover.data.MoverControllerData;
import mcjty.rftoolsbuilder.modules.mover.data.MoverData;
import mcjty.rftoolsbuilder.modules.mover.data.VehicleBuilderData;
import mcjty.rftoolsbuilder.modules.mover.data.VehicleData;
import mcjty.rftoolsbuilder.modules.mover.items.VehicleCard;
import mcjty.rftoolsbuilder.modules.mover.items.VehicleControlModuleItem;
import mcjty.rftoolsbuilder.modules.mover.items.VehicleControlScreenModule;
import mcjty.rftoolsbuilder.modules.mover.items.VehicleStatusModuleItem;
import mcjty.rftoolsbuilder.modules.mover.items.VehicleStatusScreenModule;
import mcjty.rftoolsbuilder.modules.mover.sound.Sounds;
import mcjty.rftoolsbuilder.setup.Config;
import mcjty.rftoolsbuilder.setup.Registration;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

public class MoverModule implements IModule {
   public static final RBlock<BaseBlock, BlockItem, MoverTileEntity> MOVER = Registration.RBLOCKS
      .registerBlockWIP(
         "mover",
         MoverTileEntity.class,
         MoverTileEntity::createBlock,
         block -> new BaseBlockItem((Block)block.get(), Registration.createStandardProperties()),
         MoverTileEntity::new
      );
   public static final Supplier<MenuType<GenericContainer>> CONTAINER_MOVER = Registration.CONTAINERS.register("mover", GenericContainer::createContainerType);
   public static final RBlock<BaseBlock, BlockItem, MoverControllerTileEntity> MOVER_CONTROLLER = Registration.RBLOCKS
      .registerBlockWIP(
         "mover_controller",
         MoverControllerTileEntity.class,
         MoverControllerTileEntity::createBlock,
         block -> new BaseBlockItem((Block)block.get(), Registration.createStandardProperties()),
         MoverControllerTileEntity::new
      );
   public static final Supplier<MenuType<GenericContainer>> CONTAINER_MOVER_CONTROLLER = Registration.CONTAINERS
      .register("mover_controller", GenericContainer::createContainerType);
   public static final RBlock<BaseBlock, BlockItem, VehicleBuilderTileEntity> VEHICLE_BUILDER = Registration.RBLOCKS
      .registerBlockWIP(
         "vehicle_builder",
         VehicleBuilderTileEntity.class,
         VehicleBuilderTileEntity::createBlock,
         block -> new BaseBlockItem((Block)block.get(), Registration.createStandardProperties()),
         VehicleBuilderTileEntity::new
      );
   public static final Supplier<MenuType<GenericContainer>> CONTAINER_VEHICLE_BUILDER = Registration.CONTAINERS
      .register("vehicle_builder", GenericContainer::createContainerType);
   public static final DeferredBlock<InvisibleMoverBlock> INVISIBLE_MOVER_BLOCK = Registration.BLOCKS.register("invisible_mover", InvisibleMoverBlock::new);
   public static final Supplier<BlockEntityType<?>> TYPE_INVISIBLE_MOVER = Registration.TILES
      .register("invisible_mover", () -> new BlockEntityType(InvisibleMoverBE::new, new Block[]{(Block)INVISIBLE_MOVER_BLOCK.get()}));
   public static final DeferredBlock<Block> MOVER_CONTROL_BLOCK = Registration.BLOCKS.register("mover_control", () -> new MoverControlBlock(0));
   public static final DeferredItem<Item> MOVER_CONTROL_ITEM = Registration.ITEMS
      .register("mover_control", RFToolsBuilder.tab(() -> new BlockItem((Block)MOVER_CONTROL_BLOCK.get(), Registration.createStandardProperties())));
   public static final DeferredBlock<Block> MOVER_CONTROL2_BLOCK = Registration.BLOCKS.register("mover_control2", () -> new MoverControlBlock(1));
   public static final DeferredItem<Item> MOVER_CONTROL2_ITEM = Registration.ITEMS
      .register("mover_control2", RFToolsBuilder.tab(() -> new BlockItem((Block)MOVER_CONTROL2_BLOCK.get(), Registration.createStandardProperties())));
   public static final DeferredBlock<Block> MOVER_CONTROL3_BLOCK = Registration.BLOCKS.register("mover_control3", () -> new MoverControlBlock(2));
   public static final DeferredItem<Item> MOVER_CONTROL3_ITEM = Registration.ITEMS
      .register("mover_control3", RFToolsBuilder.tab(() -> new BlockItem((Block)MOVER_CONTROL3_BLOCK.get(), Registration.createStandardProperties())));
   public static final DeferredBlock<Block> MOVER_CONTROL4_BLOCK = Registration.BLOCKS.register("mover_control4", () -> new MoverControlBlock(3));
   public static final DeferredItem<Item> MOVER_CONTROL4_ITEM = Registration.ITEMS
      .register("mover_control4", RFToolsBuilder.tab(() -> new BlockItem((Block)MOVER_CONTROL4_BLOCK.get(), Registration.createStandardProperties())));
   public static final DeferredBlock<Block> MOVER_STATUS_BLOCK = Registration.BLOCKS.register("mover_status", MoverStatusBlock::new);
   public static final DeferredItem<Item> MOVER_STATUS_ITEM = Registration.ITEMS
      .register("mover_status", RFToolsBuilder.tab(() -> new BlockItem((Block)MOVER_STATUS_BLOCK.get(), Registration.createStandardProperties())));
   public static final DeferredItem<VehicleCard> VEHICLE_CARD = Registration.ITEMS.register("vehicle_card", RFToolsBuilder.tab(VehicleCard::new));
   public static final DeferredItem<VehicleControlModuleItem> VEHICLE_CONTROL_MODULE = Registration.ITEMS
      .register("vehicle_control_module", RFToolsBuilder.tab(VehicleControlModuleItem::new));
   public static final DeferredItem<VehicleStatusModuleItem> VEHICLE_STATUS_MODULE = Registration.ITEMS
      .register("vehicle_status_module", RFToolsBuilder.tab(VehicleStatusModuleItem::new));
   public static final DeferredHolder<AttachmentType<?>, AttachmentType<MoverControllerData>> MOVER_CONTROLLER_DATA = Registration.ATTACHMENT_TYPES
      .register(
         "mover_controller_data", () -> AttachmentType.builder(() -> MoverControllerData.DEFAULT).serialize(MoverControllerData.CODEC.fieldOf("data")).build()
      );
   public static final DeferredHolder<DataComponentType<?>, DataComponentType<MoverControllerData>> ITEM_MOVER_CONTROLLER_DATA = Registration.COMPONENTS
      .registerComponentType(
         "mover_controller_data", builder -> builder.persistent(MoverControllerData.CODEC).networkSynchronized(MoverControllerData.STREAM_CODEC)
      );
   public static final DeferredHolder<AttachmentType<?>, AttachmentType<MoverData>> MOVER_DATA = Registration.ATTACHMENT_TYPES
      .register("mover_data", () -> AttachmentType.builder(() -> MoverData.DEFAULT).serialize(MoverData.CODEC.fieldOf("data")).build());
   public static final DeferredHolder<DataComponentType<?>, DataComponentType<MoverData>> ITEM_MOVER_DATA = Registration.COMPONENTS
      .registerComponentType("mover_data", builder -> builder.persistent(MoverData.CODEC).networkSynchronized(MoverData.STREAM_CODEC));
   public static final DeferredHolder<AttachmentType<?>, AttachmentType<VehicleBuilderData>> VEHICLE_BUILDER_DATA = Registration.ATTACHMENT_TYPES
      .register(
         "vehicle_builder_data", () -> AttachmentType.builder(() -> VehicleBuilderData.DEFAULT).serialize(VehicleBuilderData.CODEC.fieldOf("data")).build()
      );
   public static final DeferredHolder<DataComponentType<?>, DataComponentType<VehicleBuilderData>> ITEM_VEHICLE_BUILDER_DATA = Registration.COMPONENTS
      .registerComponentType(
         "vehicle_builder_data", builder -> builder.persistent(VehicleBuilderData.CODEC).networkSynchronized(VehicleBuilderData.STREAM_CODEC)
      );
   public static final DeferredHolder<DataComponentType<?>, DataComponentType<VehicleData>> ITEM_VEHICLE_DATA = Registration.COMPONENTS
      .registerComponentType("vehicle_data", builder -> builder.persistent(VehicleData.CODEC).networkSynchronized(VehicleData.STREAM_CODEC));
   public static final DeferredHolder<DataComponentType<?>, DataComponentType<VehicleControlScreenModule>> MODULE_VEHICLECONTROL_DATA = Registration.COMPONENTS
      .registerComponentType(
         "module_vehiclecontrol_data",
         builder -> builder.persistent(VehicleControlScreenModule.CODEC).networkSynchronized(VehicleControlScreenModule.STREAM_CODEC)
      );
   public static final DeferredHolder<DataComponentType<?>, DataComponentType<VehicleStatusScreenModule>> MODULE_VEHICLESTATUS_DATA = Registration.COMPONENTS
      .registerComponentType(
         "module_vehiclestatus_data",
         builder -> builder.persistent(VehicleStatusScreenModule.CODEC).networkSynchronized(VehicleStatusScreenModule.STREAM_CODEC)
      );

   public MoverModule(IEventBus bus, Dist dist) {
      Sounds.init();
      bus.addListener(this::registerMenuScreens);
   }

   public void init(FMLCommonSetupEvent event) {
   }

   public void initClient(FMLClientSetupEvent event) {
      ClientSetup.initClient();
   }

   public void registerMenuScreens(RegisterMenuScreensEvent event) {
      GuiMover.register(event);
      GuiMoverController.register(event);
      GuiVehicleBuilder.register(event);
   }

   public void initConfig(IEventBus bus) {
      MoverConfiguration.init(Config.SERVER_BUILDER, Config.CLIENT_BUILDER);
   }

   public void initDatagen(DataGen dataGen, Provider provider) {
   }
}
