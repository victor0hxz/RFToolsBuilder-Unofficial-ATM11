package mcjty.rftoolsbuilder.modules.scanner.client;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

record BlockEntry(BlockPos pos, BlockState state) {
   public static BlockEntry of(BlockPos pos, BlockState state) {
      return new BlockEntry(pos, state);
   }

   public static class Builder {
      private final List<BlockEntry> list = new ArrayList<>();

      public BlockEntry.Builder add(BlockPos pos, BlockState state) {
         this.list.add(BlockEntry.of(pos, state));
         return this;
      }

      public List<BlockEntry> build() {
         return this.list;
      }
   }
}
