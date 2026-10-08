package mcjty.rftoolsbuilder.modules.shield.blocks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mcjty.rftoolsbuilder.modules.shield.ShieldModule;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class ShieldingTileEntity extends BlockEntity {
   private BlockPos shieldProjector;
   private BlockState mimic;

   public ShieldingTileEntity(BlockPos pos, BlockState state) {
      super(ShieldModule.TYPE_SHIELDING.get(), pos, state);
   }

   @Nullable
   public ClientboundBlockEntityDataPacket getUpdatePacket() {
      CompoundTag nbtTag = new CompoundTag();
      this.saveClient(nbtTag);
      return ClientboundBlockEntityDataPacket.create(this, (blockEntity, provider) -> nbtTag);
   }

   @Nonnull
   public CompoundTag getUpdateTag(Provider provider) {
      CompoundTag tag = new CompoundTag();
      this.saveClient(tag);
      return tag;
   }

   public void onDataPacket(Connection net, ValueInput input) {
      super.onDataPacket(net, input);
      this.shieldProjector = new BlockPos(input.getIntOr("sx", 0), input.getIntOr("sy", 0), input.getIntOr("sz", 0));
      this.mimic = (BlockState)input.read("mimic", BlockState.CODEC).orElse(null);
      BlockState state = this.level.getBlockState(this.worldPosition);
      this.level.sendBlockUpdated(this.worldPosition, state, state, 3);
   }

   public BlockPos getShieldProjector() {
      return this.shieldProjector;
   }

   public void setShieldProjector(BlockPos shieldProjector) {
      this.shieldProjector = shieldProjector;
      this.setChanged();
   }

   public BlockState getMimic() {
      return this.mimic;
   }

   public void setMimic(BlockState mimic) {
      this.mimic = mimic;
      this.setChanged();
   }

   protected void loadAdditional(ValueInput input) {
      super.loadAdditional(input);
      this.shieldProjector = new BlockPos(input.getIntOr("sx", 0), input.getIntOr("sy", 0), input.getIntOr("sz", 0));
      this.mimic = (BlockState)input.read("mimic", BlockState.CODEC).orElse(null);
   }

   protected void saveAdditional(ValueOutput output) {
      super.saveAdditional(output);
      if (this.shieldProjector != null) {
         output.putInt("sx", this.shieldProjector.getX());
         output.putInt("sy", this.shieldProjector.getY());
         output.putInt("sz", this.shieldProjector.getZ());
      }

      if (this.mimic != null) {
         output.store("mimic", BlockState.CODEC, this.mimic);
      }
   }

   private void saveClient(CompoundTag tag) {
      if (this.shieldProjector != null) {
         tag.putInt("sx", this.shieldProjector.getX());
         tag.putInt("sy", this.shieldProjector.getY());
         tag.putInt("sz", this.shieldProjector.getZ());
      }

      if (this.mimic != null) {
         CompoundTag camoNbt = NbtUtils.writeBlockState(this.mimic);
         tag.put("mimic", camoNbt);
      }
   }

   private void loadClient(CompoundTag tag, Provider provider) {
      this.shieldProjector = new BlockPos(tag.getIntOr("sx", 0), tag.getIntOr("sy", 0), tag.getIntOr("sz", 0));
      if (tag.contains("mimic")) {
         this.mimic = NbtUtils.readBlockState((HolderGetter)provider.lookup(Registries.BLOCK).get(), tag.getCompoundOrEmpty("mimic"));
      } else {
         this.mimic = null;
      }
   }
}
