package mcjty.rftoolsbuilder.modules.builder.blocks;

import javax.annotation.Nonnull;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.varia.Logging;
import mcjty.rftoolsbuilder.modules.builder.BuilderConfiguration;
import mcjty.rftoolsbuilder.modules.builder.BuilderModule;
import mcjty.rftoolsbuilder.modules.builder.SpaceChamberRepository;
import mcjty.rftoolsbuilder.modules.builder.data.ChamberControllerData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class SpaceChamberControllerTileEntity extends GenericTileEntity {
   private BlockPos minCorner;
   private BlockPos maxCorner;

   public SpaceChamberControllerTileEntity(BlockPos pos, BlockState state) {
      super(BuilderModule.TYPE_SPACE_CHAMBER_CONTROLLER.get(), pos, state);
   }

   public BlockPos getMinCorner() {
      return this.minCorner;
   }

   public BlockPos getMaxCorner() {
      return this.maxCorner;
   }

   public void createChamber(Player player) {
      BlockPos pos = this.getBlockPos();
      int x1 = pos.getX();
      int y1 = pos.getY();
      int z1 = pos.getZ();
      int x2 = x1;
      int y2 = y1;
      int z2 = z1;

      for (int i = 1; i < BuilderConfiguration.maxSpaceChamberDimension.get(); i++) {
         if (x2 == x1) {
            if (this.getLevel().getBlockState(new BlockPos(x1 - i, y1, z1)).getBlock() == BuilderModule.SPACE_CHAMBER.get()) {
               x2 = x1 - i;
            } else if (this.level.getBlockState(new BlockPos(x1 + i, y1, z1)).getBlock() == BuilderModule.SPACE_CHAMBER.get()) {
               x2 = x1 + i;
            }
         }

         if (z2 == z1) {
            if (this.level.getBlockState(new BlockPos(x1, y1, z1 - i)).getBlock() == BuilderModule.SPACE_CHAMBER.get()) {
               z2 = z1 - i;
            } else if (this.level.getBlockState(new BlockPos(x1, y1, z1 + i)).getBlock() == BuilderModule.SPACE_CHAMBER.get()) {
               z2 = z1 + i;
            }
         }
      }

      if (x1 != x2 && z2 != z1) {
         if (this.level.getBlockState(new BlockPos(x2, y1, z2)).getBlock() != BuilderModule.SPACE_CHAMBER.get()) {
            Logging.message(player, ChatFormatting.RED + "Not a valid chamber shape!");
         } else {
            for (int i = 1; i < BuilderConfiguration.maxSpaceChamberDimension.get(); i++) {
               if (this.level.getBlockState(new BlockPos(x1, y1 - i, z1)).getBlock() == BuilderModule.SPACE_CHAMBER.get()) {
                  y2 = y1 - i;
                  break;
               }

               if (this.level.getBlockState(new BlockPos(x1, y1 + i, z1)).getBlock() == BuilderModule.SPACE_CHAMBER.get()) {
                  y2 = y1 + i;
                  break;
               }
            }

            if (y1 == y2) {
               Logging.message(player, ChatFormatting.RED + "Not a valid chamber shape!");
            } else if (this.level.getBlockState(new BlockPos(x2, y2, z2)).getBlock() != BuilderModule.SPACE_CHAMBER.get()) {
               Logging.message(player, ChatFormatting.RED + "Not a valid chamber shape!");
            } else if (this.level.getBlockState(new BlockPos(x1, y2, z2)).getBlock() != BuilderModule.SPACE_CHAMBER.get()) {
               Logging.message(player, ChatFormatting.RED + "Not a valid chamber shape!");
            } else if (this.level.getBlockState(new BlockPos(x2, y2, z1)).getBlock() != BuilderModule.SPACE_CHAMBER.get()) {
               Logging.message(player, ChatFormatting.RED + "Not a valid chamber shape!");
            } else {
               this.minCorner = new BlockPos(Math.min(x1, x2) + 1, Math.min(y1, y2) + 1, Math.min(z1, z2) + 1);
               this.maxCorner = new BlockPos(Math.max(x1, x2) - 1, Math.max(y1, y2) - 1, Math.max(z1, z2) - 1);
               if (this.minCorner.getX() <= this.maxCorner.getX()
                  && this.minCorner.getY() <= this.maxCorner.getY()
                  && this.minCorner.getZ() <= this.maxCorner.getZ()) {
                  Logging.message(player, ChatFormatting.WHITE + "Chamber succesfully created!");
                  SpaceChamberRepository chamberRepository = SpaceChamberRepository.get(this.level);
                  SpaceChamberRepository.SpaceChamberChannel chamberChannel = chamberRepository.getOrCreateChannel(this.getChannel());
                  chamberChannel.setDimension(this.level.dimension());
                  chamberChannel.setMinCorner(this.minCorner);
                  chamberChannel.setMaxCorner(this.maxCorner);
                  chamberRepository.save();
                  this.setChanged();
               } else {
                  Logging.message(player, ChatFormatting.RED + "Chamber is too small!");
                  this.minCorner = null;
                  this.maxCorner = null;
               }
            }
         }
      } else {
         Logging.message(player, ChatFormatting.RED + "Not a valid chamber shape!");
      }
   }

   public int getChannel() {
      return ((ChamberControllerData)this.getData(BuilderModule.CHAMBER_DATA)).channel();
   }

   public int getChamberSize() {
      if (this.getChannel() == -1) {
         return -1;
      } else {
         return this.minCorner == null
            ? -1
            : (this.maxCorner.getX() - this.minCorner.getX())
               * (this.maxCorner.getY() - this.minCorner.getY())
               * (this.maxCorner.getZ() - this.minCorner.getZ());
      }
   }

   public void setChannel(int channel) {
      ChamberControllerData data = ((ChamberControllerData)this.getData(BuilderModule.CHAMBER_DATA)).withChannel(channel);
      this.setData(BuilderModule.CHAMBER_DATA, data);
   }

   public void loadAdditional(ValueInput tagCompound) {
      super.loadAdditional(tagCompound);
      this.minCorner = BlockPosTools.read(tagCompound, "minCorner");
      this.maxCorner = BlockPosTools.read(tagCompound, "maxCorner");
   }

   public void saveAdditional(@Nonnull ValueOutput tagCompound) {
      super.saveAdditional(tagCompound);
      BlockPosTools.write(tagCompound, "minCorner", this.minCorner);
      BlockPosTools.write(tagCompound, "maxCorner", this.maxCorner);
   }
}
