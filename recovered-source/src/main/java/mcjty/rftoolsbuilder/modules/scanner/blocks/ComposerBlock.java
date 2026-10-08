package mcjty.rftoolsbuilder.modules.scanner.blocks;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.builder.InfoLine;
import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.container.GenericItemHandler;
import mcjty.rftoolsbase.tools.ManualHelper;
import mcjty.rftoolsbuilder.compat.RFToolsBuilderTOPDriver;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class ComposerBlock extends BaseBlock {
   public ComposerBlock() {
      super(
         new BlockBuilder()
            .tileEntitySupplier(ComposerTileEntity::new)
            .topDriver(RFToolsBuilderTOPDriver.DRIVER)
            .infusable()
            .manualEntry(ManualHelper.create("rftoolsbuilder:projector/composer"))
            .info(new InfoLine[]{TooltipBuilder.key("message.rftoolsbuilder.shiftmessage")})
            .infoShift(new InfoLine[]{TooltipBuilder.header(), TooltipBuilder.gold()})
      );
   }

   public RotationType getRotationType() {
      return RotationType.HORIZROTATION;
   }

   public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
      if (state.getBlock() != newState.getBlock() && level.getBlockEntity(pos) instanceof ComposerTileEntity composer) {
         GenericItemHandler h = composer.getItems();

         for (int i = 0; i < h.getSlots(); i++) {
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), h.getStackInSlot(i));
         }
      }

      super.onRemove(state, level, pos, newState, isMoving);
   }
}
