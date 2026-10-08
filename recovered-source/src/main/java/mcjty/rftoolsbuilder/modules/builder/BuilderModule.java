package mcjty.rftoolsbuilder.modules.builder;

import java.util.function.Supplier;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RBlock;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.datagen.DataGen;
import mcjty.lib.items.BaseBlockItem;
import mcjty.lib.modules.IModule;
import mcjty.rftoolsbase.tools.ManualHelper;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.builder.blocks.BuilderTileEntity;
import mcjty.rftoolsbuilder.modules.builder.blocks.SpaceChamberControllerBlock;
import mcjty.rftoolsbuilder.modules.builder.blocks.SpaceChamberControllerTileEntity;
import mcjty.rftoolsbuilder.modules.builder.blocks.SupportBlock;
import mcjty.rftoolsbuilder.modules.builder.client.GuiBuilder;
import mcjty.rftoolsbuilder.modules.builder.data.BuilderData;
import mcjty.rftoolsbuilder.modules.builder.data.ChamberControllerData;
import mcjty.rftoolsbuilder.modules.builder.data.ShapeCardData;
import mcjty.rftoolsbuilder.modules.builder.items.ShapeCardItem;
import mcjty.rftoolsbuilder.modules.builder.items.ShapeCardType;
import mcjty.rftoolsbuilder.modules.builder.items.SpaceChamberCardItem;
import mcjty.rftoolsbuilder.modules.builder.items.SuperHarvestingTool;
import mcjty.rftoolsbuilder.setup.Config;
import mcjty.rftoolsbuilder.setup.Registration;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.world.chunk.RegisterTicketControllersEvent;
import net.neoforged.neoforge.common.world.chunk.TicketController;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

public class BuilderModule implements IModule {
   public static final DeferredBlock<SupportBlock> SUPPORT = Registration.BLOCKS.register("support_block", SupportBlock::new);
   public static final DeferredBlock<BaseBlock> SPACE_CHAMBER = Registration.BLOCKS
      .register(
         "space_chamber",
         () -> new BaseBlock(
            new BlockBuilder()
               .manualEntry(ManualHelper.create("rftoolsbuilder:builder/space_chambers"))
               .properties(Properties.of().strength(2.0F).sound(SoundType.METAL).noOcclusion())
         )
      );
   public static final DeferredItem<Item> SPACE_CHAMBER_ITEM = Registration.ITEMS
      .register("space_chamber", RFToolsBuilder.tab(() -> new BlockItem((Block)SPACE_CHAMBER.get(), Registration.createStandardProperties())));
   public static final DeferredBlock<SpaceChamberControllerBlock> SPACE_CHAMBER_CONTROLLER = Registration.BLOCKS
      .register("space_chamber_controller", SpaceChamberControllerBlock::new);
   public static final DeferredItem<Item> SPACE_CHAMBER_CONTROLLER_ITEM = Registration.ITEMS
      .register(
         "space_chamber_controller", RFToolsBuilder.tab(() -> new BlockItem((Block)SPACE_CHAMBER_CONTROLLER.get(), Registration.createStandardProperties()))
      );
   public static final Supplier<BlockEntityType<SpaceChamberControllerTileEntity>> TYPE_SPACE_CHAMBER_CONTROLLER = Registration.TILES
      .register(
         "space_chamber_controller", () -> new BlockEntityType(SpaceChamberControllerTileEntity::new, new Block[]{(Block)SPACE_CHAMBER_CONTROLLER.get()})
      );
   public static final DeferredItem<Item> SPACE_CHAMBER_CARD = Registration.ITEMS.register("space_chamber_card", RFToolsBuilder.tab(SpaceChamberCardItem::new));
   public static final RBlock<BaseBlock, BlockItem, BuilderTileEntity> BUILDER = Registration.RBLOCKS
      .registerBlock(
         "builder",
         BuilderTileEntity.class,
         BuilderTileEntity::createBlock,
         block -> new BaseBlockItem((Block)block.get(), Registration.createStandardProperties()),
         BuilderTileEntity::new
      );
   public static final Supplier<MenuType<GenericContainer>> CONTAINER_BUILDER = Registration.CONTAINERS
      .register("builder", GenericContainer::createContainerType);
   public static final DeferredItem<Item> SUPER_HARVESTING_TOOL = Registration.ITEMS.register("superharvestingtool", SuperHarvestingTool::new);
   public static final DeferredItem<ShapeCardItem> SHAPE_CARD_DEF = Registration.ITEMS
      .register("shape_card_def", RFToolsBuilder.tab(() -> new ShapeCardItem(ShapeCardType.CARD_SHAPE)));
   public static final DeferredItem<ShapeCardItem> SHAPE_CARD_LIQUID = Registration.ITEMS
      .register("shape_card_liquid", RFToolsBuilder.tab(() -> new ShapeCardItem(ShapeCardType.CARD_PUMP_LIQUID)));
   public static final DeferredItem<ShapeCardItem> SHAPE_CARD_PUMP = Registration.ITEMS
      .register("shape_card_pump", RFToolsBuilder.tab(() -> new ShapeCardItem(ShapeCardType.CARD_PUMP)));
   public static final DeferredItem<ShapeCardItem> SHAPE_CARD_PUMP_CLEAR = Registration.ITEMS
      .register("shape_card_pump_clear", RFToolsBuilder.tab(() -> new ShapeCardItem(ShapeCardType.CARD_PUMP_CLEAR)));
   public static final DeferredItem<ShapeCardItem> SHAPE_CARD_QUARRY = Registration.ITEMS
      .register("shape_card_quarry", RFToolsBuilder.tab(() -> new ShapeCardItem(ShapeCardType.CARD_QUARRY)));
   public static final DeferredItem<ShapeCardItem> SHAPE_CARD_QUARRY_CLEAR = Registration.ITEMS
      .register("shape_card_quarry_clear", RFToolsBuilder.tab(() -> new ShapeCardItem(ShapeCardType.CARD_QUARRY_CLEAR)));
   public static final DeferredItem<ShapeCardItem> SHAPE_CARD_QUARRY_CLEAR_FORTUNE = Registration.ITEMS
      .register("shape_card_quarry_clear_fortune", RFToolsBuilder.tab(() -> new ShapeCardItem(ShapeCardType.CARD_QUARRY_CLEAR_FORTUNE)));
   public static final DeferredItem<ShapeCardItem> SHAPE_CARD_QUARRY_CLEAR_SILK = Registration.ITEMS
      .register("shape_card_quarry_clear_silk", RFToolsBuilder.tab(() -> new ShapeCardItem(ShapeCardType.CARD_QUARRY_CLEAR_SILK)));
   public static final DeferredItem<ShapeCardItem> SHAPE_CARD_QUARRY_FORTUNE = Registration.ITEMS
      .register("shape_card_quarry_fortune", RFToolsBuilder.tab(() -> new ShapeCardItem(ShapeCardType.CARD_QUARRY_FORTUNE)));
   public static final DeferredItem<ShapeCardItem> SHAPE_CARD_QUARRY_SILK = Registration.ITEMS
      .register("shape_card_quarry_silk", RFToolsBuilder.tab(() -> new ShapeCardItem(ShapeCardType.CARD_QUARRY_SILK)));
   public static final DeferredItem<ShapeCardItem> SHAPE_CARD_VOID = Registration.ITEMS
      .register("shape_card_void", RFToolsBuilder.tab(() -> new ShapeCardItem(ShapeCardType.CARD_VOID)));
   public static final DeferredHolder<DataComponentType<?>, DataComponentType<ShapeCardData>> ITEM_SHAPECARD_DATA = Registration.COMPONENTS
      .registerComponentType("shapecard_data", builder -> builder.persistent(ShapeCardData.CODEC).networkSynchronized(ShapeCardData.STREAM_CODEC));
   public static final DeferredHolder<AttachmentType<?>, AttachmentType<BuilderData>> BUILDER_DATA = Registration.ATTACHMENT_TYPES
      .register("builder_data", () -> AttachmentType.builder(() -> BuilderData.DEFAULT).serialize(BuilderData.CODEC.fieldOf("data")).build());
   public static final DeferredHolder<DataComponentType<?>, DataComponentType<BuilderData>> ITEM_BUILDER_DATA = Registration.COMPONENTS
      .registerComponentType("builder_data", builder -> builder.persistent(BuilderData.CODEC).networkSynchronized(BuilderData.STREAM_CODEC));
   public static final DeferredHolder<AttachmentType<?>, AttachmentType<ChamberControllerData>> CHAMBER_DATA = Registration.ATTACHMENT_TYPES
      .register(
         "chamber_data", () -> AttachmentType.builder(() -> ChamberControllerData.DEFAULT).serialize(ChamberControllerData.CODEC.fieldOf("data")).build()
      );
   public static final DeferredHolder<DataComponentType<?>, DataComponentType<ChamberControllerData>> ITEM_CHAMBER_DATA = Registration.COMPONENTS
      .registerComponentType("chamber_data", builder -> builder.persistent(ChamberControllerData.CODEC).networkSynchronized(ChamberControllerData.STREAM_CODEC));

   public BuilderModule(IEventBus bus) {
      bus.addListener(this::registerMenuScreens);
      bus.addListener(this::onRegisterTicketController);
   }

   public void init(FMLCommonSetupEvent event) {
   }

   private void onRegisterTicketController(RegisterTicketControllersEvent event) {
      RFToolsBuilder.setup.ticketController = new TicketController(Identifier.fromNamespaceAndPath("rftoolsbuilder", "builder"));
      event.register(RFToolsBuilder.setup.ticketController);
   }

   public void initClient(FMLClientSetupEvent event) {
   }

   public void registerMenuScreens(RegisterMenuScreensEvent event) {
      GuiBuilder.register(event);
   }

   public void initConfig(IEventBus bus) {
      BuilderConfiguration.init(Config.SERVER_BUILDER, Config.CLIENT_BUILDER);
   }

   public void initDatagen(DataGen dataGen, Provider provider) {
   }
}
