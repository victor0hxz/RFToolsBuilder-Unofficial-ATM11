package mcjty.rftoolsbuilder.setup;

import javax.annotation.Nonnull;
import mcjty.lib.network.Networking;
import mcjty.lib.network.PacketSendClientCommand;
import mcjty.lib.network.PacketSendServerCommand;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.typed.TypedMap.Builder;
import mcjty.rftoolsbuilder.modules.builder.network.PacketChamberInfoReady;
import mcjty.rftoolsbuilder.modules.builder.network.PacketCloseContainerAndOpenCardGui;
import mcjty.rftoolsbuilder.modules.builder.network.PacketOpenBuilderGui;
import mcjty.rftoolsbuilder.modules.builder.network.PacketOpenCardGuiFromBuilder;
import mcjty.rftoolsbuilder.modules.builder.network.PacketUpdateCardInInventory;
import mcjty.rftoolsbuilder.modules.builder.network.PacketUpdateCardInPlayer;
import mcjty.rftoolsbuilder.modules.mover.network.PacketClickMover;
import mcjty.rftoolsbuilder.modules.mover.network.PacketGrabbedEntitiesToClient;
import mcjty.rftoolsbuilder.modules.mover.network.PacketSyncVehicleInformationToClient;
import mcjty.rftoolsbuilder.modules.scanner.network.PacketRequestShapeData;
import mcjty.rftoolsbuilder.modules.scanner.network.PacketReturnExtraData;
import mcjty.rftoolsbuilder.modules.scanner.network.PacketReturnShapeData;
import mcjty.rftoolsbuilder.modules.shield.network.PacketNotifyServerClientReady;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class RFToolsBuilderMessages {
   public static void registerMessages(RegisterPayloadHandlersEvent event) {
      PayloadRegistrar registrar = event.registrar("rftoolsbuilder").versioned("1.0").optional();
      registrar.playToServer(PacketUpdateCardInInventory.TYPE, PacketUpdateCardInInventory.CODEC, PacketUpdateCardInInventory::handle);
      registrar.playToServer(PacketUpdateCardInPlayer.TYPE, PacketUpdateCardInPlayer.CODEC, PacketUpdateCardInPlayer::handle);
      registrar.playToServer(PacketCloseContainerAndOpenCardGui.TYPE, PacketCloseContainerAndOpenCardGui.CODEC, PacketCloseContainerAndOpenCardGui::handle);
      registrar.playToServer(PacketOpenBuilderGui.TYPE, PacketOpenBuilderGui.CODEC, PacketOpenBuilderGui::handle);
      registrar.playToServer(PacketNotifyServerClientReady.TYPE, PacketNotifyServerClientReady.CODEC, PacketNotifyServerClientReady::handle);
      registrar.playToServer(PacketClickMover.TYPE, PacketClickMover.CODEC, PacketClickMover::handle);
      registrar.playToServer(PacketRequestShapeData.TYPE, PacketRequestShapeData.CODEC, PacketRequestShapeData::handle);
      registrar.playToClient(PacketOpenCardGuiFromBuilder.TYPE, PacketOpenCardGuiFromBuilder.CODEC);
      registrar.playToClient(PacketChamberInfoReady.TYPE, PacketChamberInfoReady.CODEC);
      registrar.playToClient(PacketSyncVehicleInformationToClient.TYPE, PacketSyncVehicleInformationToClient.CODEC);
      registrar.playToClient(PacketGrabbedEntitiesToClient.TYPE, PacketGrabbedEntitiesToClient.CODEC);
      registrar.playToClient(PacketReturnShapeData.TYPE, PacketReturnShapeData.CODEC);
      registrar.playToClient(PacketReturnExtraData.TYPE, PacketReturnExtraData.CODEC);
   }

   public static void registerClientMessages(RegisterClientPayloadHandlersEvent event) {
      event.register(PacketOpenCardGuiFromBuilder.TYPE, PacketOpenCardGuiFromBuilder::handle);
      event.register(PacketChamberInfoReady.TYPE, PacketChamberInfoReady::handle);
      event.register(PacketSyncVehicleInformationToClient.TYPE, PacketSyncVehicleInformationToClient::handle);
      event.register(PacketGrabbedEntitiesToClient.TYPE, PacketGrabbedEntitiesToClient::handle);
      event.register(PacketReturnShapeData.TYPE, PacketReturnShapeData::handle);
      event.register(PacketReturnExtraData.TYPE, PacketReturnExtraData::handle);
   }

   public static void sendToServer(String command, @Nonnull Builder argumentBuilder) {
      Networking.sendToServer(new PacketSendServerCommand("rftoolsbuilder", command, argumentBuilder.build()));
   }

   public static void sendToServer(String command) {
      Networking.sendToServer(new PacketSendServerCommand("rftoolsbuilder", command, TypedMap.EMPTY));
   }

   public static void sendToClient(Player player, String command, @Nonnull Builder argumentBuilder) {
      Networking.sendToPlayer(new PacketSendClientCommand("rftoolsbuilder", command, argumentBuilder.build()), player);
   }

   public static void sendToClient(Player player, String command) {
      Networking.sendToPlayer(new PacketSendClientCommand("rftoolsbuilder", command, TypedMap.EMPTY), player);
   }

   public static <T extends CustomPacketPayload> void sendToPlayer(T packet, Player player) {
      PacketDistributor.sendToPlayer((ServerPlayer)player, packet, new CustomPacketPayload[0]);
   }

   public static <T extends CustomPacketPayload> void sendToServer(T packet) {
      ClientPacketDistributor.sendToServer(packet, new CustomPacketPayload[0]);
   }

   public static <T extends CustomPacketPayload> void sendToChunk(T packet, ServerLevel level, BlockPos pos) {
      PacketDistributor.sendToPlayersTrackingChunk(level, ChunkPos.containing(pos), packet, new CustomPacketPayload[0]);
   }

   public static <T extends CustomPacketPayload> void sendToChunk(T packet, LevelChunk chunk) {
      PacketDistributor.sendToPlayersTrackingChunk((ServerLevel)chunk.getLevel(), chunk.getPos(), packet, new CustomPacketPayload[0]);
   }
}
