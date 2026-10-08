package mcjty.rftoolsbuilder.modules.builder.blocks;

import java.util.ArrayDeque;
import java.util.Deque;
import javax.annotation.Nonnull;
import mcjty.lib.setup.RegistrationContext;
import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;

public class SupportBlock extends Block {
   public static final EnumProperty<SupportBlock.SupportStatus> STATUS = EnumProperty.create("status", SupportBlock.SupportStatus.class);

   public SupportBlock() {
      super(RegistrationContext.prepareBlockProperties(Properties.of().replaceable().noOcclusion().isRedstoneConductor((state, world, pos) -> false)));
   }

   protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult result) {
      if (!level.isClientSide()) {
         Deque<BlockPos> todo = new ArrayDeque<>();
         todo.add(pos);
         this.removeBlock(level, todo);
      }

      return super.useWithoutItem(state, level, pos, player, result);
   }

   private void removeBlock(Level world, Deque<BlockPos> todo) {
      while (!todo.isEmpty()) {
         BlockPos c = todo.pollFirst();
         world.setBlockAndUpdate(c, Blocks.AIR.defaultBlockState());

         for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
               for (int dz = -1; dz <= 1; dz++) {
                  if (dx != 0 || dy != 0 || dz != 0) {
                     BlockPos offset = c.offset(dx, dy, dz);
                     if (world.getBlockState(offset).getBlock() == this) {
                        todo.push(offset);
                     }
                  }
               }
            }
         }
      }
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{STATUS});
   }

   public static enum SupportStatus implements StringRepresentable {
      STATUS_OK("ok"),
      STATUS_WARN("warn"),
      STATUS_ERROR("error");

      private final String name;

      private SupportStatus(String name) {
         this.name = name;
      }

      public static SupportBlock.SupportStatus max(SupportBlock.SupportStatus error1, SupportBlock.SupportStatus error2) {
         if (error1 == STATUS_ERROR || error2 == STATUS_ERROR) {
            return STATUS_ERROR;
         } else {
            return error1 != STATUS_WARN && error2 != STATUS_WARN ? STATUS_OK : STATUS_WARN;
         }
      }

      public String getName() {
         return this.name;
      }

      @Nonnull
      public String getSerializedName() {
         return this.name;
      }
   }
}
