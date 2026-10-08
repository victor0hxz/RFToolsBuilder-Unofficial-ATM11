package mcjty.rftoolsbuilder.modules.builder.items;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import mcjty.lib.varia.ComponentFactory;
import mcjty.rftoolsbuilder.modules.builder.BuilderConfiguration;
import mcjty.rftoolsbuilder.modules.builder.blocks.BuilderTileEntity;
import mcjty.rftoolsbuilder.shapes.Shape;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;

public enum ShapeCardType {
   CARD_UNKNOWN,
   CARD_SPACE,
   CARD_SHAPE(
      "def",
      false,
      false,
      false,
      BuilderTileEntity::buildBlock,
      null,
      () -> (Integer)BuilderConfiguration.builderRfPerOperation.get(),
      () -> new String[]{
         "This item can be configured as a shape. You",
         "can then use it in the shield projector to make",
         "a shield of that shape or in the builder to",
         "actually build the shape"
      }
   ) {
      @Override
      public void addHudLog(List<String> list, IItemHandler inventory) {
         list.add("    Shape card");
         ItemStack shapeCard = inventory.getStackInSlot(0);
         if (!shapeCard.isEmpty()) {
            Shape shape = ShapeCardItem.getShape(shapeCard);
            if (shape != null) {
               list.add("    " + shape.getDescription());
            }
         }
      }
   },
   CARD_VOID(
      "void",
      false,
      false,
      false,
      BuilderTileEntity::voidBlock,
      "    Void mode",
      () -> (Integer)BuilderConfiguration.builderRfPerQuarry.get() * (int)((Double)BuilderConfiguration.voidShapeCardFactor.get()).doubleValue(),
      () -> new String[]{"This item will cause the builder to void", "all blocks in the configured space."}
   ),
   CARD_QUARRY(
      "quarry",
      true,
      false,
      false,
      BuilderTileEntity::quarryBlock,
      "    Normal quarry",
      () -> (Integer)BuilderConfiguration.builderRfPerQuarry.get(),
      () -> new String[]{
         "This item will cause the builder to quarry", "all blocks in the configured space and replace", "them with " + getDirtOrCobbleName() + "."
      }
   ),
   CARD_QUARRY_SILK(
      "quarry_silk",
      true,
      false,
      false,
      BuilderTileEntity::silkQuarryBlock,
      "    Silktouch quarry",
      () -> (int)(((Integer)BuilderConfiguration.builderRfPerQuarry.get()).intValue() * (Double)BuilderConfiguration.silkquarryShapeCardFactor.get()),
      () -> new String[]{
         "This item will cause the builder to quarry",
         "all blocks in the configured space and replace",
         "them with " + getDirtOrCobbleName() + ".",
         "Blocks are harvested with silk touch"
      }
   ),
   CARD_QUARRY_FORTUNE(
      "quarry_fortune",
      true,
      false,
      true,
      BuilderTileEntity::quarryBlock,
      "    Fortune quarry",
      () -> (int)(((Integer)BuilderConfiguration.builderRfPerQuarry.get()).intValue() * (Double)BuilderConfiguration.fortunequarryShapeCardFactor.get()),
      () -> new String[]{
         "This item will cause the builder to quarry",
         "all blocks in the configured space and replace",
         "them with " + getDirtOrCobbleName() + ".",
         "Blocks are harvested with fortune"
      }
   ),
   CARD_QUARRY_CLEAR(
      "quarry_clear",
      true,
      true,
      false,
      BuilderTileEntity::quarryBlock,
      "    Normal quarry",
      () -> (Integer)BuilderConfiguration.builderRfPerQuarry.get(),
      () -> new String[]{"This item will cause the builder to quarry", "all blocks in the configured space"}
   ),
   CARD_QUARRY_CLEAR_SILK(
      "quarry_clear_silk",
      true,
      true,
      false,
      BuilderTileEntity::silkQuarryBlock,
      "    Silktouch quarry",
      () -> (int)(((Integer)BuilderConfiguration.builderRfPerQuarry.get()).intValue() * (Double)BuilderConfiguration.silkquarryShapeCardFactor.get()),
      () -> new String[]{"This item will cause the builder to quarry", "all blocks in the configured space.", "Blocks are harvested with silk touch"}
   ),
   CARD_QUARRY_CLEAR_FORTUNE(
      "quarry_clear_fortune",
      true,
      true,
      true,
      BuilderTileEntity::quarryBlock,
      "    Fortune quarry",
      () -> (int)(((Integer)BuilderConfiguration.builderRfPerQuarry.get()).intValue() * (Double)BuilderConfiguration.fortunequarryShapeCardFactor.get()),
      () -> new String[]{"This item will cause the builder to quarry", "all blocks in the configured space.", "Blocks are harvested with fortune"}
   ),
   CARD_PUMP(
      "pump",
      false,
      false,
      false,
      BuilderTileEntity::pumpBlock,
      "    Pump",
      () -> (Integer)BuilderConfiguration.builderRfPerLiquid.get(),
      () -> new String[]{
         "This item will cause the builder to collect",
         "all liquids in the configured space.",
         "The liquid will be replaced with " + getDirtOrCobbleName() + "."
      }
   ),
   CARD_PUMP_CLEAR(
      "pump_clear",
      false,
      true,
      false,
      BuilderTileEntity::pumpBlock,
      "    Pump",
      () -> (Integer)BuilderConfiguration.builderRfPerLiquid.get(),
      () -> new String[]{"This item will cause the builder to collect", "all liquids in the configured space.", "The liquid will be removed from the world"}
   ),
   CARD_PUMP_LIQUID(
      "liquid",
      false,
      false,
      false,
      BuilderTileEntity::placeLiquidBlock,
      "    Place liquids",
      () -> (Integer)BuilderConfiguration.builderRfPerLiquid.get(),
      () -> new String[]{"This item will cause the builder to place", "liquids from an tank on top/bottom into the world."}
   );

   private final Supplier<Integer> rfNeeded;
   private final ShapeCardType.SingleBlockHandler singleBlockHandler;
   private final String hudLogEntry;
   private final boolean quarry;
   private final boolean clearing;
   private final boolean fortune;
   private final Supplier<String[]> information;
   private final String resourceSuffix;

   private static String getDirtOrCobbleName() {
      BlockState state = BuilderConfiguration.getQuarryReplace();
      Block block = state.getBlock();
      Item item = block.asItem();
      return item == Items.AIR ? block.getDescriptionId() : new ItemStack(item, 1).getHoverName().getString();
   }

   private ShapeCardType() {
      this(null, false, false, false, BuilderTileEntity::suspend, null, () -> 0, () -> new String[0]);
   }

   private ShapeCardType(
      String resourceSuffix,
      boolean quarry,
      boolean clearing,
      boolean fortune,
      ShapeCardType.SingleBlockHandler singleBlockHandler,
      String hudLogEntry,
      Supplier<Integer> rfNeeded,
      Supplier<String[]> information
   ) {
      this.resourceSuffix = resourceSuffix;
      this.quarry = quarry;
      this.clearing = clearing;
      this.fortune = fortune;
      this.rfNeeded = rfNeeded;
      this.singleBlockHandler = singleBlockHandler;
      this.hudLogEntry = hudLogEntry;
      this.information = information;
   }

   public boolean isItem() {
      return this.resourceSuffix != null;
   }

   public String getResourceSuffix() {
      return this.resourceSuffix;
   }

   public boolean isQuarry() {
      return this.quarry;
   }

   public boolean isClearing() {
      return this.clearing;
   }

   public boolean isFortune() {
      return this.fortune;
   }

   public int getRfNeeded() {
      return this.rfNeeded.get();
   }

   public void addHudLog(List<String> list, IItemHandler inventoryHelper) {
      if (this.hudLogEntry != null) {
         list.add(this.hudLogEntry);
      }

      if (this.isClearing()) {
         list.add("    (clearing)");
      }
   }

   public void addInformation(List<Component> list) {
      List<MutableComponent> info = Arrays.stream(this.information.get())
         .map(a -> ComponentFactory.literal(ChatFormatting.WHITE.toString() + a))
         .collect(Collectors.toList());
      list.addAll(info);
      list.add(
         ComponentFactory.literal(
            ChatFormatting.GREEN
               + "Max area: "
               + BuilderConfiguration.maxBuilderDimension.get()
               + "x"
               + Math.min(256, (Integer)BuilderConfiguration.maxBuilderDimension.get())
               + "x"
               + BuilderConfiguration.maxBuilderDimension.get()
         )
      );
      list.add(ComponentFactory.literal(ChatFormatting.GREEN + "Base cost: " + this.rfNeeded.get() + " RF/t per block"));
      list.add(
         ComponentFactory.literal(
            ChatFormatting.GREEN
               + (this == CARD_SHAPE ? "(final cost depends on infusion level)" : "(final cost depends on infusion level and block hardness)")
         )
      );
   }

   public boolean handleSingleBlock(BuilderTileEntity te, int rfNeeded, BlockPos srcPos, BlockState srcState, BlockState pickState) {
      return this.singleBlockHandler.handleSingleBlock(te, rfNeeded, srcPos, srcState, pickState);
   }

   @FunctionalInterface
   public interface SingleBlockHandler {
      boolean handleSingleBlock(BuilderTileEntity var1, int var2, BlockPos var3, BlockState var4, BlockState var5);
   }
}
