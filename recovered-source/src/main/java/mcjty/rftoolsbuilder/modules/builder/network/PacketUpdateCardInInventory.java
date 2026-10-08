package mcjty.rftoolsbuilder.modules.builder.network;

import mcjty.lib.varia.CapabilityTools;
import mcjty.rftoolsbuilder.modules.builder.blocks.BuilderTileEntity;
import mcjty.rftoolsbuilder.modules.builder.items.ShapeCardItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketUpdateCardInInventory(BlockPos pos, int slotIndex, ItemStack stack) implements CustomPacketPayload {
   public static final Identifier ID = Identifier.fromNamespaceAndPath("rftoolsbuilder", "updatecard_inventory");
   public static final Type<PacketUpdateCardInInventory> TYPE = new Type(ID);
   public static final StreamCodec<RegistryFriendlyByteBuf, PacketUpdateCardInInventory> CODEC = StreamCodec.composite(
      BlockPos.STREAM_CODEC,
      PacketUpdateCardInInventory::pos,
      ByteBufCodecs.INT,
      PacketUpdateCardInInventory::slotIndex,
      ItemStack.STREAM_CODEC,
      PacketUpdateCardInInventory::stack,
      PacketUpdateCardInInventory::new
   );

   public static PacketUpdateCardInInventory create(BlockPos pos, int slotIndex, ItemStack stack) {
      return new PacketUpdateCardInInventory(pos, slotIndex, stack);
   }

   private boolean isValidBlock(Level world, BlockPos blockPos, BlockEntity tileEntity) {
      return tileEntity instanceof BuilderTileEntity;
   }

   private boolean isValidItem(ItemStack stack) {
      return stack.getItem() instanceof ShapeCardItem;
   }

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public void handle(IPayloadContext ctx) {
      ctx.enqueueWork(() -> {
         Player player = ctx.player();
         Level world = player.level();
         BlockEntity te = world.getBlockEntity(this.pos);
         if (te != null) {
            if (!this.isValidBlock(world, this.pos, te)) {
               return;
            }

            if (!this.isValidItem(this.stack)) {
               return;
            }

            if (CapabilityTools.getItemCapability(world, this.pos, null) instanceof IItemHandlerModifiable modifiable) {
               modifiable.setStackInSlot(this.slotIndex, this.stack);
            }
         }
      });
   }
}
