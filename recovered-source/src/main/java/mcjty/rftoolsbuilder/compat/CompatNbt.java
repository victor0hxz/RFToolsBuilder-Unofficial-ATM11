package mcjty.rftoolsbuilder.compat;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;

public final class CompatNbt {
   private CompatNbt() {
   }

   public static CompoundTag encodeItemStack(ItemStack stack, Provider provider) {
      return ItemStack.CODEC
         .encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), stack)
         .result()
         .filter(CompoundTag.class::isInstance)
         .map(CompoundTag.class::cast)
         .orElseGet(CompoundTag::new);
   }

   public static ItemStack decodeItemStack(CompoundTag tag, Provider provider) {
      return ItemStack.CODEC.parse(provider.createSerializationContext(NbtOps.INSTANCE), tag).result().orElse(ItemStack.EMPTY);
   }

   public static ValueInput valueInput(CompoundTag tag, Provider provider) {
      return TagValueInput.create(ProblemReporter.DISCARDING, provider, tag);
   }

   public static void putBlockPos(CompoundTag tag, String key, BlockPos pos) {
      tag.store(key, BlockPos.CODEC, pos);
   }

   public static BlockPos getBlockPos(CompoundTag tag, String key) {
      return (BlockPos)tag.read(key, BlockPos.CODEC).orElse(null);
   }
}
