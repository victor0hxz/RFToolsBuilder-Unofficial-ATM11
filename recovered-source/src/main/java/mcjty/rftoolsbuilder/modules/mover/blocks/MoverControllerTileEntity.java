package mcjty.rftoolsbuilder.modules.mover.blocks;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import javax.annotation.Nullable;
import mcjty.lib.api.container.DefaultContainerProvider;
import mcjty.lib.api.infusable.DefaultInfusable;
import mcjty.lib.api.infusable.ItemInfusable;
import mcjty.lib.api.power.ItemEnergy;
import mcjty.lib.bindings.GuiValue;
import mcjty.lib.bindings.Value;
import mcjty.lib.blockcommands.Command;
import mcjty.lib.blockcommands.ISerializer;
import mcjty.lib.blockcommands.ListCommand;
import mcjty.lib.blockcommands.ServerCommand;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.builder.InfoLine;
import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.setup.Registration;
import mcjty.lib.tileentity.Cap;
import mcjty.lib.tileentity.CapType;
import mcjty.lib.tileentity.GenericEnergyStorage;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.typed.Key;
import mcjty.lib.typed.Type;
import mcjty.lib.varia.OrientationTools;
import mcjty.rftoolsbase.tools.ManualHelper;
import mcjty.rftoolsbuilder.compat.RFToolsBuilderTOPDriver;
import mcjty.rftoolsbuilder.modules.mover.MoverConfiguration;
import mcjty.rftoolsbuilder.modules.mover.MoverModule;
import mcjty.rftoolsbuilder.modules.mover.client.GuiMoverController;
import mcjty.rftoolsbuilder.modules.mover.data.MoverControllerData;
import mcjty.rftoolsbuilder.modules.mover.items.VehicleCard;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponentMap.Builder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.attachment.AttachmentType;
import org.apache.commons.lang3.tuple.Pair;

public class MoverControllerTileEntity extends GenericTileEntity {
   private final GenericEnergyStorage energyStorage = new GenericEnergyStorage(
      this, true, ((Integer)MoverConfiguration.MAXENERGY.get()).intValue(), ((Integer)MoverConfiguration.RECEIVEPERTICK.get()).intValue()
   );
   @Cap(type = CapType.ENERGY)
   private static final Function<MoverControllerTileEntity, GenericEnergyStorage> ENERGY_CAP = tile -> tile.energyStorage;
   @Cap(type = CapType.CONTAINER)
   private static final Function<MoverControllerTileEntity, MenuProvider> SCREEN_CAP = tile -> new DefaultContainerProvider("Mover")
      .containerSupplier(DefaultContainerProvider.empty(MoverModule.CONTAINER_MOVER_CONTROLLER, tile))
      .energyHandler(() -> tile.energyStorage)
      .data(MoverModule.MOVER_CONTROLLER_DATA, MoverControllerData.STREAM_CODEC, MoverControllerData.CODEC)
      .setupSync(tile);
   private final DefaultInfusable infusable = new DefaultInfusable(this);
   @Cap(type = CapType.INFUSABLE)
   private static final Function<MoverControllerTileEntity, DefaultInfusable> INFUSABLE_CAP = tile -> tile.infusable;
   public static final int MAXSCAN = 128;
   @GuiValue
   public static final Value<?, String> VALUE_SELECTED_VEHICLE = Value.create(
      "selectedVehicle", Type.STRING, MoverControllerTileEntity::getSelectedVehicle, MoverControllerTileEntity::setSelectedVehicle
   );
   private String selectedVehicle;
   public static final Key<BlockPos> SELECTED_NODE = new Key("node", Type.BLOCKPOS);
   public static final Key<String> SELECTED_VEHICLE = new Key("vehicle", Type.STRING);
   public static final Key<String> SELECTED_DESTINATION = new Key("destination", Type.STRING);
   @ServerCommand
   public static final Command<?> CMD_SCAN = Command.create("scan", (te, player, params) -> te.doScan());
   @ServerCommand
   public static final Command<?> CMD_MOVE = Command.create(
      "move",
      (te, player, params) -> te.startMove((BlockPos)params.get(SELECTED_NODE), (String)params.get(SELECTED_VEHICLE), (String)params.get(SELECTED_DESTINATION))
   );
   @ServerCommand
   public static final Command<?> CMD_SELECTNODE = Command.create("selectNode", (te, player, params) -> te.selectNode((BlockPos)params.get(SELECTED_NODE)));
   @ServerCommand(type = String.class)
   public static final ListCommand<?, ?> CMD_GETVEHICLES = ListCommand.create(
      "rftoolsbuilder.movercontroller.getVehicles",
      (te, player, params) -> te.getVehicles(),
      (te, player, params, list) -> GuiMoverController.setVehiclesFromServer(list)
   );
   @ServerCommand(type = Pair.class, serializer = MoverControllerTileEntity.NodePairSerializer.class)
   public static final ListCommand<?, ?> CMD_GETNODES = ListCommand.create(
      "rftoolsbuilder.movercontroller.getNodes",
      (te, player, params) -> te.getNodes(),
      (te, player, params, list) -> GuiMoverController.setNodesFromServer(list)
   );

   public static BaseBlock createBlock() {
      return new BaseBlock(
         new BlockBuilder()
            .tileEntitySupplier(MoverControllerTileEntity::new)
            .topDriver(RFToolsBuilderTOPDriver.DRIVER)
            .infusable()
            .manualEntry(ManualHelper.create("rftoolsbuilder:mover/mover_controller"))
            .info(new InfoLine[]{TooltipBuilder.key("message.rftoolsbuilder.shiftmessage")})
            .infoShift(new InfoLine[]{TooltipBuilder.header(), TooltipBuilder.gold()})
      );
   }

   public MoverControllerTileEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType)MoverModule.MOVER_CONTROLLER.be().get(), pos, state);
   }

   private void selectNode(BlockPos pos) {
      if (this.level.getBlockEntity(pos) instanceof MoverTileEntity mover) {
         ItemStack card = mover.getCard();
         if (card.isEmpty()) {
            this.selectedVehicle = null;
         } else {
            this.selectedVehicle = VehicleCard.getVehicleName(card);
         }
      } else {
         this.selectedVehicle = null;
      }
   }

   public void onDataChanged(AttachmentType<?> type, Object oldData, Object newData) {
      if (type == MoverModule.MOVER_CONTROLLER_DATA.get()) {
         this.onDataChanged((MoverControllerData)oldData, (MoverControllerData)newData);
      }
   }

   private void onDataChanged(MoverControllerData oldData, MoverControllerData newData) {
      if (oldData.offsetX() != newData.offsetX() || oldData.offsetY() != newData.offsetY() || oldData.offsetZ() != newData.offsetZ()) {
         this.onOffsetChanged(newData.offsetX(), newData.offsetY(), newData.offsetZ());
      }
   }

   private void onOffsetChanged(int x, int y, int z) {
      this.traverseDepthFirst((pos, mover) -> {
         mover.setOffset(x, y, z);
         return null;
      });
   }

   public String getSelectedVehicle() {
      return this.selectedVehicle;
   }

   public void setSelectedVehicle(String vehicle) {
      this.selectedVehicle = vehicle;
      if (this.level.isClientSide()) {
         GuiMoverController.setSelectedVehicle(vehicle);
      }
   }

   @Nullable
   public <T> T traverseBreadthFirst(BiFunction<BlockPos, MoverTileEntity, T> function) {
      for (Direction direction : OrientationTools.DIRECTION_VALUES) {
         BlockPos moverPos = this.worldPosition.relative(direction);
         if (this.level.getBlockEntity(moverPos) instanceof MoverTileEntity mover) {
            return mover.traverseBreadthFirst(function);
         }
      }

      return null;
   }

   @Nullable
   private <T> T traverseDepthFirst(BiFunction<BlockPos, MoverTileEntity, T> function) {
      for (Direction direction : OrientationTools.DIRECTION_VALUES) {
         BlockPos moverPos = this.worldPosition.relative(direction);
         if (this.level.getBlockEntity(moverPos) instanceof MoverTileEntity mover) {
            return mover.traverseDepthFirst(function);
         }
      }

      return null;
   }

   public boolean hasEnoughPower() {
      return this.energyStorage.getEnergyStored() >= this.getPowerPerMove();
   }

   private Integer getPowerPerMove() {
      int power = (Integer)MoverConfiguration.RF_PER_MOVE.get();
      power = (int)(power * (2.0F - this.infusable.getInfusedFactor()) / 2.0F);
      return power;
   }

   public void setupMovement(String moverName, String vehicle) {
      if (this.energyStorage.getEnergyStored() >= this.getPowerPerMove()) {
         MoverTileEntity destinationMover = this.findMover(moverName);
         if (destinationMover != null) {
            if (vehicle == null || vehicle.trim().isEmpty()) {
               vehicle = this.traverseBreadthFirst((p, mover) -> {
                  ItemStack card = mover.getCard();
                  return !card.isEmpty() ? VehicleCard.getVehicleName(card) : null;
               });
            }

            if (vehicle != null) {
               this.energyStorage.consumeEnergy(this.getPowerPerMove().intValue());
               this.startMove(destinationMover.getBlockPos(), vehicle, destinationMover.getName());
            }
         }
      }
   }

   private void startMove(BlockPos destination, String vehicle, String destinationName) {
      if (vehicle.contains(" -> ")) {
         vehicle = vehicle.substring(0, vehicle.indexOf(" -> "));
      }

      MoverTileEntity moverContainingVehicle = this.findVehicle(vehicle);
      if (moverContainingVehicle != null) {
         ItemStack card = moverContainingVehicle.getCard();
         VehicleCard.setDesiredDestination(card, destination, destinationName);
      }
   }

   @Nullable
   private MoverTileEntity findMover(String moverName) {
      return this.traverseDepthFirst((p, mover) -> Objects.equals(moverName, mover.getName()) ? mover : null);
   }

   @Nullable
   public MoverTileEntity findVehicle(String vehicle) {
      return this.traverseDepthFirst((p, mover) -> {
         ItemStack card = mover.getCard();
         String name = VehicleCard.getVehicleName(card);
         return Objects.equals(name, vehicle) ? mover : null;
      });
   }

   private void doScan() {
      this.setChanged();

      for (Direction direction : OrientationTools.DIRECTION_VALUES) {
         BlockPos moverPos = this.worldPosition.relative(direction);
         if (this.level.getBlockEntity(moverPos) instanceof MoverTileEntity mover) {
            Set<BlockPos> alreadyHandled = new HashSet<>();
            alreadyHandled.add(moverPos);
            this.doScan(moverPos, mover, alreadyHandled);
            return;
         }
      }
   }

   private void doScan(BlockPos moverPos, MoverTileEntity mover, Set<BlockPos> alreadyHandled) {
      mover.clearNetwork();
      mover.setController(this);

      for (Direction direction : OrientationTools.DIRECTION_VALUES) {
         if (mover.canConnect(direction)) {
            for (int distance = 1; distance <= 128; distance++) {
               BlockPos destPos = moverPos.relative(direction, distance);
               if (this.level.getBlockEntity(destPos) instanceof MoverTileEntity destMover) {
                  mover.addConnection(direction, destPos);
                  if (!alreadyHandled.contains(destPos)) {
                     alreadyHandled.add(destPos);
                     this.doScan(destPos, destMover, alreadyHandled);
                  }
                  break;
               }
            }
         }
      }
   }

   public List<String> getMovers() {
      List<String> movers = new ArrayList<>();
      this.traverseDepthFirst((p, mover) -> {
         movers.add(mover.getName());
         return null;
      });
      return movers;
   }

   private List<String> getVehicles() {
      List<String> vehicles = new ArrayList<>();
      this.traverseDepthFirst((p, mover) -> {
         ItemStack card = mover.getCard();
         if (!card.isEmpty()) {
            String name = VehicleCard.getVehicleName(card);
            BlockPos destination = VehicleCard.getDesiredDestination(card);
            String destinationName = VehicleCard.getDesiredDestinationName(card);
            if (destination != null) {
               name = name + " -> " + destinationName;
            }

            vehicles.add(name);
         }

         return null;
      });
      return vehicles;
   }

   private List<Pair<BlockPos, String>> getNodes() {
      List<Pair<BlockPos, String>> nodeNames = new ArrayList<>();
      this.traverseDepthFirst((p, mover) -> {
         String name = mover.getName();
         if (name == null || name.trim().isEmpty()) {
            name = p.getX() + "," + p.getY() + "," + p.getZ();
         }

         nodeNames.add(Pair.of(p, name));
         return null;
      });
      return nodeNames;
   }

   protected void saveAdditional(ValueOutput tag) {
      super.saveAdditional(tag);
      this.energyStorage.save(tag, "energy");
      this.infusable.save(tag, "infusable");
   }

   protected void loadAdditional(ValueInput tag) {
      super.loadAdditional(tag);
      this.energyStorage.load(tag, "energy");
      this.infusable.load(tag, "infusable");
   }

   protected void applyImplicitComponents(DataComponentGetter input) {
      super.applyImplicitComponents(input);
      this.energyStorage.applyImplicitComponents((ItemEnergy)input.get((DataComponentType)Registration.ITEM_ENERGY.get()));
      this.infusable.applyImplicitComponents((ItemInfusable)input.get((DataComponentType)Registration.ITEM_INFUSABLE.get()));
      MoverControllerData moverControllerData = (MoverControllerData)input.get(MoverModule.ITEM_MOVER_CONTROLLER_DATA);
      if (moverControllerData != null) {
         this.setData(MoverModule.MOVER_CONTROLLER_DATA, moverControllerData);
      }
   }

   protected void collectImplicitComponents(Builder builder) {
      super.collectImplicitComponents(builder);
      this.energyStorage.collectImplicitComponents(builder);
      this.infusable.collectImplicitComponents(builder);
      builder.set(MoverModule.ITEM_MOVER_CONTROLLER_DATA, (MoverControllerData)this.getData(MoverModule.MOVER_CONTROLLER_DATA));
   }

   public static class NodePairSerializer implements ISerializer<Pair<BlockPos, String>> {
      public Function<RegistryFriendlyByteBuf, Pair<BlockPos, String>> getDeserializer() {
         return buf -> Pair.of(buf.readBlockPos(), buf.readUtf(32767));
      }

      public BiConsumer<RegistryFriendlyByteBuf, Pair<BlockPos, String>> getSerializer() {
         return (buf, pair) -> {
            buf.writeBlockPos((BlockPos)pair.getLeft());
            buf.writeUtf((String)pair.getRight());
         };
      }
   }
}
