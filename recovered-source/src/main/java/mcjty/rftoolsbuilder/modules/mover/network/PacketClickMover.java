package mcjty.rftoolsbuilder.modules.mover.network;

import mcjty.rftoolsbuilder.modules.mover.blocks.MoverTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketClickMover(BlockPos pos, String mover) implements CustomPacketPayload {
   public static final Identifier ID = Identifier.fromNamespaceAndPath("rftoolsbuilder", "click_mover");
   public static final Type<PacketClickMover> TYPE = new Type(ID);
   public static final StreamCodec<FriendlyByteBuf, PacketClickMover> CODEC = StreamCodec.composite(
      BlockPos.STREAM_CODEC, PacketClickMover::pos, ByteBufCodecs.STRING_UTF8, PacketClickMover::mover, PacketClickMover::create
   );

   public static PacketClickMover create(BlockPos worldPosition, String highlightedMover) {
      return new PacketClickMover(worldPosition, highlightedMover);
   }

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public void handle(IPayloadContext ctx) {
      ctx.enqueueWork(() -> {
         Player player = ctx.player();
         if (player.level().getBlockEntity(this.pos) instanceof MoverTileEntity mover) {
            mover.startMove(this.mover);
         }
      });
   }
}
