package mcjty.rftoolsbuilder.modules.shield;

import java.util.function.Supplier;
import javax.annotation.Nonnull;
import mcjty.lib.blocks.RBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.datagen.DataGen;
import mcjty.lib.items.BaseBlockItem;
import mcjty.lib.modules.IModule;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.shield.blocks.ShieldProjectorBlock;
import mcjty.rftoolsbuilder.modules.shield.blocks.ShieldProjectorTileEntity;
import mcjty.rftoolsbuilder.modules.shield.blocks.ShieldTemplateBlock;
import mcjty.rftoolsbuilder.modules.shield.blocks.ShieldingBlock;
import mcjty.rftoolsbuilder.modules.shield.blocks.ShieldingTileEntity;
import mcjty.rftoolsbuilder.modules.shield.client.GuiShield;
import mcjty.rftoolsbuilder.modules.shield.data.ShieldData;
import mcjty.rftoolsbuilder.setup.Config;
import mcjty.rftoolsbuilder.setup.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

public class ShieldModule implements IModule {
   public static final RBlock<ShieldProjectorBlock, BlockItem, ShieldProjectorTileEntity> SHIELD_BLOCK1 = Registration.RBLOCKS
      .registerBlock(
         "shield_block1",
         ShieldProjectorTileEntity.class,
         () -> new ShieldProjectorBlock(ShieldModule::createProjector1, ShieldConfiguration.maxShieldSize),
         block -> new BaseBlockItem((Block)block.get(), Registration.createStandardProperties()),
         ShieldModule::createProjector1
      );
   public static final RBlock<ShieldProjectorBlock, BlockItem, ShieldProjectorTileEntity> SHIELD_BLOCK2 = Registration.RBLOCKS
      .registerBlock(
         "shield_block2",
         ShieldProjectorTileEntity.class,
         () -> new ShieldProjectorBlock(ShieldModule::createProjector2, () -> (Integer)ShieldConfiguration.maxShieldSize.get() * 4),
         block -> new BaseBlockItem((Block)block.get(), Registration.createStandardProperties()),
         ShieldModule::createProjector2
      );
   public static final RBlock<ShieldProjectorBlock, BlockItem, ShieldProjectorTileEntity> SHIELD_BLOCK3 = Registration.RBLOCKS
      .registerBlock(
         "shield_block3",
         ShieldProjectorTileEntity.class,
         () -> new ShieldProjectorBlock(ShieldModule::createProjector3, () -> (Integer)ShieldConfiguration.maxShieldSize.get() * 16),
         block -> new BaseBlockItem((Block)block.get(), Registration.createStandardProperties()),
         ShieldModule::createProjector3
      );
   public static final RBlock<ShieldProjectorBlock, BlockItem, ShieldProjectorTileEntity> SHIELD_BLOCK4 = Registration.RBLOCKS
      .registerBlock(
         "shield_block4",
         ShieldProjectorTileEntity.class,
         () -> new ShieldProjectorBlock(ShieldModule::createProjector4, () -> (Integer)ShieldConfiguration.maxShieldSize.get() * 128),
         block -> new BaseBlockItem((Block)block.get(), Registration.createStandardProperties()),
         ShieldModule::createProjector4
      );
   public static final Supplier<MenuType<GenericContainer>> CONTAINER_SHIELD = Registration.CONTAINERS
      .register("shield", GenericContainer::createContainerType);
   public static final DeferredBlock<ShieldTemplateBlock> TEMPLATE_BLUE = Registration.BLOCKS
      .register("blue_shield_template_block", () -> new ShieldTemplateBlock(ShieldTemplateBlock.TemplateColor.BLUE));
   public static final DeferredBlock<ShieldTemplateBlock> TEMPLATE_RED = Registration.BLOCKS
      .register("red_shield_template_block", () -> new ShieldTemplateBlock(ShieldTemplateBlock.TemplateColor.RED));
   public static final DeferredBlock<ShieldTemplateBlock> TEMPLATE_GREEN = Registration.BLOCKS
      .register("green_shield_template_block", () -> new ShieldTemplateBlock(ShieldTemplateBlock.TemplateColor.GREEN));
   public static final DeferredBlock<ShieldTemplateBlock> TEMPLATE_YELLOW = Registration.BLOCKS
      .register("yellow_shield_template_block", () -> new ShieldTemplateBlock(ShieldTemplateBlock.TemplateColor.YELLOW));
   public static final DeferredItem<Item> TEMPLATE_BLUE_ITEM = Registration.ITEMS
      .register("blue_shield_template_block", RFToolsBuilder.tab(() -> new BlockItem((Block)TEMPLATE_BLUE.get(), Registration.createStandardProperties())));
   public static final DeferredItem<Item> TEMPLATE_RED_ITEM = Registration.ITEMS
      .register("red_shield_template_block", RFToolsBuilder.tab(() -> new BlockItem((Block)TEMPLATE_RED.get(), Registration.createStandardProperties())));
   public static final DeferredItem<Item> TEMPLATE_GREEN_ITEM = Registration.ITEMS
      .register("green_shield_template_block", RFToolsBuilder.tab(() -> new BlockItem((Block)TEMPLATE_GREEN.get(), Registration.createStandardProperties())));
   public static final DeferredItem<Item> TEMPLATE_YELLOW_ITEM = Registration.ITEMS
      .register("yellow_shield_template_block", RFToolsBuilder.tab(() -> new BlockItem((Block)TEMPLATE_YELLOW.get(), Registration.createStandardProperties())));
   public static final DeferredBlock<ShieldingBlock> SHIELDING_SOLID = Registration.BLOCKS.register("shielding_solid", ShieldingBlock::new);
   public static final DeferredBlock<ShieldingBlock> SHIELDING_TRANSLUCENT = Registration.BLOCKS.register("shielding_translucent", ShieldingBlock::new);
   public static final DeferredBlock<ShieldingBlock> SHIELDING_CUTOUT = Registration.BLOCKS.register("shielding_cutout", ShieldingBlock::new);
   public static final Supplier<BlockEntityType<?>> TYPE_SHIELDING = Registration.TILES
      .register(
         "shielding",
         () -> new BlockEntityType(
            ShieldingTileEntity::new, new Block[]{(Block)SHIELDING_SOLID.get(), (Block)SHIELDING_TRANSLUCENT.get(), (Block)SHIELDING_CUTOUT.get()}
         )
      );
   public static final DeferredHolder<AttachmentType<?>, AttachmentType<ShieldData>> SHIELD_DATA = Registration.ATTACHMENT_TYPES
      .register("shield_data", () -> AttachmentType.builder(() -> ShieldData.DEFAULT).serialize(ShieldData.CODEC.fieldOf("data")).build());
   public static final DeferredHolder<DataComponentType<?>, DataComponentType<ShieldData>> ITEM_SHIELD_DATA = Registration.COMPONENTS
      .registerComponentType("shield_data", builder -> builder.persistent(ShieldData.CODEC).networkSynchronized(ShieldData.STREAM_CODEC));

   @Nonnull
   public static ShieldProjectorTileEntity createProjector1(BlockPos pos, BlockState state) {
      return new ShieldProjectorTileEntity(
         (BlockEntityType<?>)SHIELD_BLOCK1.be().get(),
         pos,
         state,
         (Integer)ShieldConfiguration.maxShieldSize.get(),
         (Integer)ShieldConfiguration.MAXENERGY.get(),
         (Integer)ShieldConfiguration.RECEIVEPERTICK.get()
      );
   }

   @Nonnull
   public static ShieldProjectorTileEntity createProjector2(BlockPos pos, BlockState state) {
      return new ShieldProjectorTileEntity(
         (BlockEntityType<?>)SHIELD_BLOCK2.be().get(),
         pos,
         state,
         (Integer)ShieldConfiguration.maxShieldSize.get() * 4,
         (Integer)ShieldConfiguration.MAXENERGY.get(),
         (Integer)ShieldConfiguration.RECEIVEPERTICK.get()
      );
   }

   @Nonnull
   public static ShieldProjectorTileEntity createProjector3(BlockPos pos, BlockState state) {
      return new ShieldProjectorTileEntity(
            (BlockEntityType<?>)SHIELD_BLOCK3.be().get(),
            pos,
            state,
            (Integer)ShieldConfiguration.maxShieldSize.get() * 16,
            (Integer)ShieldConfiguration.MAXENERGY.get() * 3,
            (Integer)ShieldConfiguration.RECEIVEPERTICK.get() * 2
         )
         .setDamageFactor(4.0F)
         .setCostFactor(2.0F);
   }

   @Nonnull
   public static ShieldProjectorTileEntity createProjector4(BlockPos pos, BlockState state) {
      return new ShieldProjectorTileEntity(
            (BlockEntityType<?>)SHIELD_BLOCK4.be().get(),
            pos,
            state,
            (Integer)ShieldConfiguration.maxShieldSize.get() * 128,
            (Integer)ShieldConfiguration.MAXENERGY.get() * 6,
            (Integer)ShieldConfiguration.RECEIVEPERTICK.get() * 6
         )
         .setDamageFactor(4.0F)
         .setCostFactor(2.0F);
   }

   public ShieldModule(IEventBus bus, Dist dist) {
      bus.addListener(this::registerMenuScreens);
   }

   public void init(FMLCommonSetupEvent event) {
   }

   public void initClient(FMLClientSetupEvent event) {
   }

   public void registerMenuScreens(RegisterMenuScreensEvent event) {
      GuiShield.register(event);
   }

   public void initConfig(IEventBus bus) {
      ShieldConfiguration.init(Config.SERVER_BUILDER, Config.CLIENT_BUILDER);
   }

   public void initDatagen(DataGen dataGen, Provider provider) {
   }
}
