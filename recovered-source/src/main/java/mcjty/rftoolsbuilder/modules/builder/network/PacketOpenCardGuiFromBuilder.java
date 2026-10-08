package mcjty.rftoolsbuilder.modules.builder.network;

import mcjty.rftoolsbuilder.modules.builder.client.GuiShapeCard;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketOpenCardGuiFromBuilder() implements CustomPacketPayload {
   public static final Identifier ID = Identifier.fromNamespaceAndPath("rftoolsbuilder", "opencardguifrombuilder");
   public static final Type<PacketOpenCardGuiFromBuilder> TYPE = new Type(ID);
   public static final PacketOpenCardGuiFromBuilder INSTANCE = new PacketOpenCardGuiFromBuilder();
   public static final StreamCodec<FriendlyByteBuf, PacketOpenCardGuiFromBuilder> CODEC = StreamCodec.unit(INSTANCE);

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public void handle(IPayloadContext ctx) {
      ctx.enqueueWork(() -> GuiShapeCard.open(true));
   }
}
