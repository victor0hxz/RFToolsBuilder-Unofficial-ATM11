package mcjty.rftoolsbuilder.shapes;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class Scan {
   private byte[] rledata;
   private List<BlockState> materialPalette = new ArrayList<>();
   private BlockPos dataDim;
   private BlockPos dataOffset = new BlockPos(0, 0, 0);
   private int dirtyCounter = 0;
   public int dirtyRequestTimeout = 0;
   public static final byte[] EMPTY = new byte[0];

   public byte[] getRledata() {
      return this.rledata == null ? EMPTY : this.rledata;
   }

   public byte[] getDataInt() {
      return this.rledata;
   }

   public void setData(byte[] data, List<BlockState> materialPalette, BlockPos dim, BlockPos offset) {
      this.rledata = data;
      this.materialPalette = materialPalette;
      this.dataDim = dim;
      this.dataOffset = offset;
      this.dirtyCounter++;
   }

   public void setDirtyCounter(int dirtyCounter) {
      this.dirtyCounter = dirtyCounter;
   }

   public int getDirtyCounter() {
      return this.dirtyCounter;
   }

   public List<BlockState> getMaterialPalette() {
      return this.materialPalette;
   }

   public BlockPos getDataDim() {
      return this.dataDim;
   }

   public BlockPos getDataOffset() {
      return this.dataOffset;
   }

   public void writeToNBT(CompoundTag tagCompound) {
      tagCompound.putInt("dirty", this.dirtyCounter);
   }

   public void writeToNBTExternal(CompoundTag tagCompound) {
      tagCompound.putByteArray("data", this.rledata == null ? new byte[0] : this.rledata);
      ListTag pal = new ListTag();

      for (BlockState state : this.materialPalette) {
         CompoundTag tc = NbtUtils.writeBlockState(state);
         pal.add(tc);
      }

      tagCompound.put("scanpal", pal);
      if (this.dataDim != null) {
         tagCompound.putInt("scandimx", this.dataDim.getX());
         tagCompound.putInt("scandimy", this.dataDim.getY());
         tagCompound.putInt("scandimz", this.dataDim.getZ());
      }

      if (this.dataOffset != null) {
         tagCompound.putInt("scanoffx", this.dataOffset.getX());
         tagCompound.putInt("scanoffy", this.dataOffset.getY());
         tagCompound.putInt("scanoffz", this.dataOffset.getZ());
      }
   }

   public void readFromNBT(CompoundTag tagCompound) {
      this.dirtyCounter = tagCompound.getIntOr("dirty", 0);
   }

   public void readFromNBTExternal(CompoundTag tagCompound, Provider provider) {
      ListTag list = tagCompound.getListOrEmpty("scanpal");

      for (int i = 0; i < list.size(); i++) {
         CompoundTag tc = list.getCompoundOrEmpty(i);
         BlockState state = NbtUtils.readBlockState(provider.lookupOrThrow(Registries.BLOCK), tc);
         if (state == null) {
            state = Blocks.STONE.defaultBlockState();
         }

         this.materialPalette.add(state);
      }

      this.rledata = tagCompound.getByteArray("data").orElseGet(() -> new byte[0]);
      this.dataDim = new BlockPos(tagCompound.getIntOr("scandimx", 0), tagCompound.getIntOr("scandimy", 0), tagCompound.getIntOr("scandimz", 0));
      this.dataOffset = new BlockPos(tagCompound.getIntOr("scanoffx", 0), tagCompound.getIntOr("scanoffy", 0), tagCompound.getIntOr("scanoffz", 0));
   }
}
