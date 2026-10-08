package mcjty.rftoolsbuilder.setup;

import java.util.function.Supplier;
import mcjty.lib.blocks.RBlockRegistry;
import mcjty.lib.setup.DeferredBlocks;
import mcjty.lib.setup.DeferredItems;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.builder.BuilderModule;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredRegister.DataComponents;
import net.neoforged.neoforge.registries.NeoForgeRegistries.Keys;

public class Registration {
   public static final RBlockRegistry RBLOCKS = new RBlockRegistry("rftoolsbuilder", RFToolsBuilder.setup::addTabItem);
   public static final DeferredBlocks BLOCKS = DeferredBlocks.create("rftoolsbuilder");
   public static final DeferredItems ITEMS = DeferredItems.create("rftoolsbuilder");
   public static final DeferredRegister<BlockEntityType<?>> TILES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, "rftoolsbuilder");
   public static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(BuiltInRegistries.MENU, "rftoolsbuilder");
   public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, "rftoolsbuilder");
   public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, "rftoolsbuilder");
   public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, "rftoolsbuilder");
   public static final DataComponents COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, "rftoolsbuilder");
   public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(Keys.ATTACHMENT_TYPES, "rftoolsbuilder");
   public static Supplier<CreativeModeTab> TAB = TABS.register(
      "rftoolsbuilder",
      () -> CreativeModeTab.builder()
         .title(Component.translatable("itemGroup.rftoolsbuilder"))
         .icon(() -> new ItemStack((ItemLike)BuilderModule.BUILDER.block().get()))
         .withTabsBefore(new ResourceKey[]{CreativeModeTabs.SPAWN_EGGS})
         .displayItems((featureFlags, output) -> RFToolsBuilder.setup.populateTab(output))
         .build()
   );

   public static void register(IEventBus bus) {
      RBLOCKS.register(bus);
      BLOCKS.register(bus);
      ITEMS.register(bus);
      TILES.register(bus);
      CONTAINERS.register(bus);
      SOUNDS.register(bus);
      ENTITIES.register(bus);
      TABS.register(bus);
      COMPONENTS.register(bus);
      ATTACHMENT_TYPES.register(bus);
   }

   public static Properties createStandardProperties() {
      return RFToolsBuilder.setup.defaultProperties();
   }
}
