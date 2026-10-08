package mcjty.rftoolsbuilder.modules.scanner.blocks;

import javax.annotation.Nonnull;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.builder.InfoLine;
import mcjty.lib.builder.TooltipBuilder;
import mcjty.rftoolsbase.tools.ManualHelper;
import mcjty.rftoolsbuilder.compat.RFToolsBuilderTOPDriver;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class ProjectorBlock extends BaseBlock {
   public ProjectorBlock() {
      super(
         new BlockBuilder()
            .tileEntitySupplier(ProjectorTileEntity::new)
            .topDriver(RFToolsBuilderTOPDriver.DRIVER)
            .infusable()
            .manualEntry(ManualHelper.create("rftoolsbuilder:projector/projector_intro"))
            .info(new InfoLine[]{TooltipBuilder.key("message.rftoolsbuilder.shiftmessage")})
            .infoShift(new InfoLine[]{TooltipBuilder.header(), TooltipBuilder.gold()})
      );
   }

   public RotationType getRotationType() {
      return RotationType.HORIZROTATION;
   }

   protected InteractionResult useItemOn(
      @Nonnull ItemStack stack,
      @Nonnull BlockState state,
      @Nonnull Level level,
      @Nonnull BlockPos pos,
      @Nonnull Player player,
      @Nonnull InteractionHand hand,
      @Nonnull BlockHitResult hit
   ) {
      if (level.getBlockEntity(pos) instanceof ProjectorTileEntity projector) {
         InteractionResult result = projector.interact(level, player, hand);
         if (result.consumesAction()) {
            return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
         } else {
            return super.useItemOn(stack, state, level, pos, player, hand, hit);
         }
      } else {
         return super.useItemOn(stack, state, level, pos, player, hand, hit);
      }
   }

   protected InteractionResult useWithoutItem(
      @Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, @Nonnull Player player, @Nonnull BlockHitResult hit
   ) {
      if (level.getBlockEntity(pos) instanceof ProjectorTileEntity projector) {
         InteractionResult result = projector.interact(level, player, InteractionHand.MAIN_HAND);
         if (result.consumesAction()) {
            return result;
         }
      }

      return super.useWithoutItem(state, level, pos, player, hit);
   }

   public void onRemove(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState newState, boolean isMoving) {
      if (state.getBlock() != newState.getBlock() && level.getBlockEntity(pos) instanceof ProjectorTileEntity projector) {
         ItemStack stack = projector.removeCard();
         if (!stack.isEmpty()) {
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
         }
      }

      super.onRemove(state, level, pos, newState, isMoving);
   }
}
