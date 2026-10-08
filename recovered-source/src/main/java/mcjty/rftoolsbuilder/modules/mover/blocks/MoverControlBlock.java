package mcjty.rftoolsbuilder.modules.mover.blocks;

import javax.annotation.Nullable;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.builder.InfoLine;
import mcjty.lib.builder.TooltipBuilder;
import mcjty.rftoolsbase.tools.ManualHelper;
import mcjty.rftoolsbuilder.compat.RFToolsBuilderTOPDriver;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;

public class MoverControlBlock extends BaseBlock {
   public static final EnumProperty<Direction> HORIZ_FACING = EnumProperty.create("horizfacing", Direction.class, d -> d.getStepY() == 0);
   private final int page;

   public MoverControlBlock(int page) {
      super(
         new BlockBuilder()
            .topDriver(RFToolsBuilderTOPDriver.DRIVER)
            .manualEntry(ManualHelper.create("rftoolsbuilder:mover/vehicle_modules"))
            .info(new InfoLine[]{TooltipBuilder.key("message.rftoolsbuilder.shiftmessage")})
            .infoShift(new InfoLine[]{TooltipBuilder.header(), TooltipBuilder.gold()})
      );
      this.page = page;
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      return (BlockState)super.getStateForPlacement(context).setValue(HORIZ_FACING, context.getPlayer().getDirection().getOpposite());
   }

   public int getPage() {
      return this.page;
   }

   public BlockState rotate(BlockState state, Rotation rot) {
      state = super.rotate(state, rot);
      Direction facing = (Direction)state.getValue(BlockStateProperties.FACING);
      if (facing.getStepY() == 0) {
         return (BlockState)state.setValue(HORIZ_FACING, facing);
      } else {
         Direction horizFacing = (Direction)state.getValue(HORIZ_FACING);
         return (BlockState)state.setValue(HORIZ_FACING, rot.rotate(horizFacing));
      }
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      super.createBlockStateDefinition(builder);
      builder.add(new Property[]{HORIZ_FACING});
   }
}
