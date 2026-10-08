package mcjty.rftoolsbuilder.modules.shield.network;

import mcjty.rftoolsbuilder.modules.shield.blocks.ShieldProjectorTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketNotifyServerClientReady(BlockPos pos) implements CustomPacketPayload {
   public static final Identifier ID = Identifier.fromNamespaceAndPath("rftoolsbuilder", "notify_server_client_ready");
   public static final Type<PacketNotifyServerClientReady> TYPE = new Type(ID);
   public static final StreamCodec<RegistryFriendlyByteBuf, PacketNotifyServerClientReady> CODEC = StreamCodec.composite(
      BlockPos.STREAM_CODEC, PacketNotifyServerClientReady::pos, PacketNotifyServerClientReady::new
   );

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static PacketNotifyServerClientReady create(BlockPos worldPosition) {
      return new PacketNotifyServerClientReady(worldPosition);
   }

   public void handle(IPayloadContext ctx) {
      ctx.enqueueWork(() -> {
         Player player = ctx.player();
         if (player.level().getBlockEntity(this.pos) instanceof ShieldProjectorTileEntity projector) {
            projector.clientIsReady();
         }
      });
   }
}
