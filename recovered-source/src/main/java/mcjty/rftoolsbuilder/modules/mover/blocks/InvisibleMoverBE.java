package mcjty.rftoolsbuilder.modules.mover.blocks;

import mcjty.rftoolsbuilder.modules.mover.MoverModule;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

public class InvisibleMoverBE extends BlockEntity {
   private BlockState originalState;

   public InvisibleMoverBE(BlockPos pos, BlockState state) {
      super(MoverModule.TYPE_INVISIBLE_MOVER.get(), pos, state);
   }

   public void onDataPacket(Connection net, ValueInput input) {
      super.onDataPacket(net, input);
      this.originalState = (BlockState)input.read("originalState", BlockState.CODEC).orElse(null);
   }

   @Nullable
   public Packet<ClientGamePacketListener> getUpdatePacket() {
      CompoundTag tag = this.getUpdateTag(this.level.registryAccess());
      return ClientboundBlockEntityDataPacket.create(this, (entity, access) -> tag);
   }

   public CompoundTag getUpdateTag(Provider provider) {
      return this.saveInt(super.getUpdateTag(provider));
   }

   public BlockState getOriginalState() {
      return this.originalState;
   }

   public void setOriginalState(BlockState originalState) {
      this.originalState = originalState;
      this.setChanged();
   }

   protected void loadAdditional(ValueInput input) {
      super.loadAdditional(input);
      this.originalState = (BlockState)input.read("originalState", BlockState.CODEC).orElse(null);
   }

   private void loadInt(CompoundTag tag, Provider provider) {
      this.originalState = tag.contains("originalState")
         ? NbtUtils.readBlockState(provider.lookupOrThrow(Registries.BLOCK), tag.getCompoundOrEmpty("originalState"))
         : null;
   }

   protected void saveAdditional(ValueOutput output) {
      super.saveAdditional(output);
      if (this.originalState != null) {
         output.store("originalState", BlockState.CODEC, this.originalState);
      }
   }

   private CompoundTag saveInt(CompoundTag tag) {
      if (this.originalState != null) {
         CompoundTag tagState = NbtUtils.writeBlockState(this.originalState);
         tag.put("originalState", tagState);
      }

      return tag;
   }
}
