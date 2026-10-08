package mcjty.rftoolsbuilder.modules.builder.items;

import java.util.function.Consumer;
import javax.annotation.Nonnull;
import mcjty.lib.builder.InfoLine;
import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.gui.ManualEntry;
import mcjty.lib.tooltips.ITooltipSettings;
import mcjty.lib.varia.Logging;
import mcjty.lib.varia.Tools;
import mcjty.rftoolsbase.tools.ManualHelper;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.builder.BuilderConfiguration;
import mcjty.rftoolsbuilder.modules.builder.BuilderModule;
import mcjty.rftoolsbuilder.modules.builder.blocks.SpaceChamberControllerTileEntity;
import mcjty.rftoolsbuilder.modules.builder.client.GuiChamberDetails;
import mcjty.rftoolsbuilder.modules.builder.data.ShapeCardData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.common.util.Lazy;

public class SpaceChamberCardItem extends Item implements ITooltipSettings {
   private final Lazy<TooltipBuilder> tooltipBuilder = Lazy.of(
      () -> new TooltipBuilder()
         .info(new InfoLine[]{TooltipBuilder.key("message.rftoolsbuilder.shiftmessage")})
         .infoShift(
            new InfoLine[]{
               TooltipBuilder.header(),
               TooltipBuilder.gold(),
               TooltipBuilder.parameter("cost", this::getCostDescription),
               TooltipBuilder.parameter("channel", this::getChannelDescription),
               TooltipBuilder.general("extra", new ChatFormatting[]{ChatFormatting.GRAY})
            }
         )
   );

   private String getCostDescription(ItemStack stack) {
      return BuilderConfiguration.builderRfPerOperation.get() + " RF/t per block";
   }

   private String getChannelDescription(ItemStack card) {
      ShapeCardData data = (ShapeCardData)card.getOrDefault(BuilderModule.ITEM_SHAPECARD_DATA, ShapeCardData.DEFAULT);
      int channel = data.channel();
      return channel != -1 ? "Channel: " + channel : "Channel is not set!";
   }

   public SpaceChamberCardItem() {
      super(RFToolsBuilder.setup.defaultProperties().stacksTo(1).durability(0));
   }

   public void appendHoverText(
      @Nonnull ItemStack itemStack, TooltipContext context, TooltipDisplay display, @Nonnull Consumer<Component> list, @Nonnull TooltipFlag flag
   ) {
      super.appendHoverText(itemStack, context, display, list, flag);
      ((TooltipBuilder)this.tooltipBuilder.get()).makeTooltip(Tools.getId(this), itemStack, list, flag);
   }

   @Nonnull
   public InteractionResult use(@Nonnull Level world, Player player, @Nonnull InteractionHand hand) {
      if (!player.isCrouching()) {
         this.showDetails(world, player, player.getItemInHand(hand));
      }

      return super.use(world, player, hand);
   }

   @Nonnull
   public InteractionResult useOn(UseOnContext context) {
      Player player = context.getPlayer();
      InteractionHand hand = context.getHand();
      ItemStack stack = player.getItemInHand(hand);
      Level level = context.getLevel();
      BlockPos pos = context.getClickedPos();
      BlockEntity te = level.getBlockEntity(pos);
      int channel = -1;
      if (te instanceof SpaceChamberControllerTileEntity) {
         channel = ((SpaceChamberControllerTileEntity)te).getChannel();
      }

      if (channel == -1) {
         this.showDetails(level, player, stack);
      } else {
         ShapeCardData data = (ShapeCardData)stack.getOrDefault(BuilderModule.ITEM_SHAPECARD_DATA, ShapeCardData.DEFAULT);
         data = data.withChannel(channel);
         stack.set(BuilderModule.ITEM_SHAPECARD_DATA, data);
         if (level.isClientSide()) {
            Logging.message(player, "Card is set to channel '" + channel + "'");
         }
      }

      return InteractionResult.SUCCESS;
   }

   private void showDetails(Level world, Player player, ItemStack stack) {
      ShapeCardData data = (ShapeCardData)stack.getOrDefault(BuilderModule.ITEM_SHAPECARD_DATA, ShapeCardData.DEFAULT);
      int channel = data.channel();
      if (channel != -1) {
         this.showDetailsGui(world, player);
      } else {
         Logging.message(player, ChatFormatting.YELLOW + "Card is not linked!");
      }
   }

   private void showDetailsGui(Level world, Player player) {
      if (world.isClientSide()) {
         GuiChamberDetails.open();
      }
   }

   public ManualEntry getManualEntry() {
      return ManualHelper.create("rftoolsbuilder:builder/space_chambers");
   }
}
