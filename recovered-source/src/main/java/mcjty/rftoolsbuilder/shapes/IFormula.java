package mcjty.rftoolsbuilder.shapes;

import java.util.Objects;
import mcjty.lib.varia.Check32;
import mcjty.rftoolsbuilder.modules.builder.items.ShapeCardItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface IFormula {
   void setup(Level var1, BlockPos var2, BlockPos var3, BlockPos var4, ItemStack var5);

   default void getCheckSumClient(ItemStack card, Check32 crc) {
      ShapeCardItem.getLocalChecksum(card, crc);
   }

   boolean isInside(int var1, int var2, int var3);

   default boolean isInsideSafe(int x, int y, int z) {
      return this.isInside(x, y, z);
   }

   default BlockState getLastState() {
      return null;
   }

   default boolean isBorder(int x, int y, int z) {
      return this.isInsideSafe(x - 1, y, z)
            && this.isInsideSafe(x + 1, y, z)
            && this.isInsideSafe(x, y, z - 1)
            && this.isInsideSafe(x, y, z + 1)
            && this.isInsideSafe(x, y - 1, z)
            && this.isInsideSafe(x, y + 1, z)
         ? false
         : this.isInside(x, y, z);
   }

   default boolean isVisible(int x, int y, int z) {
      return this.isClear(x - 1, y, z)
         || this.isClear(x + 1, y, z)
         || this.isClear(x, y - 1, z)
         || this.isClear(x, y + 1, z)
         || this.isClear(x, y, z - 1)
         || this.isClear(x, y, z + 1);
   }

   default boolean isClear(int x, int y, int z) {
      if (!this.isInside(x, y, z)) {
         return true;
      } else {
         BlockState state = this.getLastState();
         return state != null ? ShapeBlockInfo.isNonSolidBlock(state.getBlock()) : false;
      }
   }

   default boolean isCustom() {
      return false;
   }

   default IFormula correctFormula(boolean solid) {
      return solid ? this : new IFormula() {
         {
            Objects.requireNonNull(IFormula.this);
         }

         @Override
         public void setup(Level world, BlockPos thisCoord, BlockPos dimension, BlockPos offset, ItemStack card) {
            IFormula.this.setup(world, thisCoord, dimension, offset, card);
         }

         @Override
         public void getCheckSumClient(ItemStack card, Check32 crc) {
            IFormula.this.getCheckSumClient(card, crc);
         }

         @Override
         public BlockState getLastState() {
            return IFormula.this.getLastState();
         }

         @Override
         public boolean isInside(int x, int y, int z) {
            return IFormula.this.isBorder(x, y, z);
         }

         @Override
         public boolean isBorder(int x, int y, int z) {
            return IFormula.this.isBorder(x, y, z);
         }
      };
   }
}
