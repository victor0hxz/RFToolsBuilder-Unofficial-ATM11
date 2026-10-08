package mcjty.rftoolsbuilder.modules.mover.blocks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mcjty.lib.api.container.DefaultContainerProvider;
import mcjty.lib.api.container.ItemInventory;
import mcjty.lib.bindings.GuiValue;
import mcjty.lib.bindings.Value;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.builder.InfoLine;
import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.client.DelayedRenderer;
import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.GenericItemHandler;
import mcjty.lib.container.SlotDefinition;
import mcjty.lib.setup.Registration;
import mcjty.lib.tileentity.Cap;
import mcjty.lib.tileentity.CapType;
import mcjty.lib.tileentity.TickingTileEntity;
import mcjty.lib.typed.Type;
import mcjty.lib.varia.OrientationTools;
import mcjty.lib.varia.SafeClientTools;
import mcjty.rftoolsbase.tools.ManualHelper;
import mcjty.rftoolsbuilder.compat.CompatNbt;
import mcjty.rftoolsbuilder.compat.RFToolsBuilderTOPDriver;
import mcjty.rftoolsbuilder.modules.mover.MoverModule;
import mcjty.rftoolsbuilder.modules.mover.client.MoverRenderer;
import mcjty.rftoolsbuilder.modules.mover.data.MoverData;
import mcjty.rftoolsbuilder.modules.mover.items.VehicleCard;
import mcjty.rftoolsbuilder.modules.mover.logic.EntityMovementLogic;
import mcjty.rftoolsbuilder.modules.mover.network.PacketClickMover;
import mcjty.rftoolsbuilder.modules.mover.network.PacketSyncVehicleInformationToClient;
import mcjty.rftoolsbuilder.modules.mover.sound.MoverSoundController;
import mcjty.rftoolsbuilder.setup.RFToolsBuilderMessages;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponentMap.Builder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.Lazy;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

public class MoverTileEntity extends TickingTileEntity {
   public static final int SLOT_VEHICLE_CARD = 0;
   public static final Lazy<ContainerFactory> CONTAINER_FACTORY = Lazy.of(
      () -> new ContainerFactory(1).slot(SlotDefinition.specific(new Item[]{(Item)MoverModule.VEHICLE_CARD.get()}).in().out(), 0, 154, 11).playerSlots(10, 70)
   );
   private final GenericItemHandler items = GenericItemHandler.create(this, CONTAINER_FACTORY).onUpdate((slot, stack) -> this.updateVehicle()).build();
   @Cap(type = CapType.ITEMS_AUTOMATION)
   private static final Function<MoverTileEntity, GenericItemHandler> ITEM_CAP = tile -> tile.items;
   @Cap(type = CapType.CONTAINER)
   private static final Function<MoverTileEntity, MenuProvider> screenHandler = tile -> new DefaultContainerProvider("Mover")
      .containerSupplier(DefaultContainerProvider.container(MoverModule.CONTAINER_MOVER, CONTAINER_FACTORY, tile))
      .itemHandler(() -> tile.items)
      .data(MoverModule.MOVER_DATA, MoverData.STREAM_CODEC, MoverData.CODEC)
      .setupSync(tile);
   @GuiValue
   public static final Value<?, String> VALUE_CONNECTIONS = Value.create(
      "connections", Type.STRING, MoverTileEntity::getConnectionCount, MoverTileEntity::setConnectionCount
   );
   private String connections = "";
   public static final Set<Integer> wantUnmount = new HashSet<>();
   private BlockPos offset = new BlockPos(1, 1, 1);
   private BlockPos controller;
   private int cnt;
   private int clientUpdateCnt;
   private boolean enoughPower = false;
   private List<String> platformsFromServer = Collections.emptyList();
   private String currentPlatform = "";
   private BlockPos cursorBlock;
   private double cursorX;
   private double cursorY;
   private String highlightedMover;
   private boolean moverValid = false;
   private int currentPage = 0;
   private int renderCopyTimer = 0;
   private BlockPos lastDestination;
   private Map<BlockPos, BlockState> invisibleMoverBlocks = null;
   private final EntityMovementLogic logic = new EntityMovementLogic(this);
   private final Map<Direction, BlockPos> network = new EnumMap<>(Direction.class);

   public static BaseBlock createBlock() {
      return new BaseBlock(
         new BlockBuilder()
            .tileEntitySupplier(MoverTileEntity::new)
            .topDriver(RFToolsBuilderTOPDriver.DRIVER)
            .manualEntry(ManualHelper.create("rftoolsbuilder:mover/mover"))
            .info(new InfoLine[]{TooltipBuilder.key("message.rftoolsbuilder.shiftmessage")})
            .infoShift(new InfoLine[]{TooltipBuilder.header(), TooltipBuilder.gold()})
      ) {
         public RotationType getRotationType() {
            return RotationType.NONE;
         }
      };
   }

   public MoverTileEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType)MoverModule.MOVER.be().get(), pos, state);
   }

   public BlockPos getLastDestination() {
      return this.lastDestination;
   }

   public void setLastDestination(BlockPos lastDestination) {
      this.lastDestination = lastDestination;
   }

   public Map<Direction, BlockPos> getNetwork() {
      return this.network;
   }

   public EntityMovementLogic getLogic() {
      return this.logic;
   }

   public String getName() {
      return ((MoverData)this.getData(MoverModule.MOVER_DATA)).name();
   }

   public void setRemoved() {
      super.setRemoved();
      if (this.invisibleMoverBlocks != null) {
         this.removeInvisibleBlocks();
      }
   }

   protected void tickServer() {
      if (!this.isMoving()) {
         this.logic.clearGrabbedEntities();
      }

      this.updateVehicleStatus();
      this.logic.tryMoveVehicleServer();
      this.syncVehicleStatus();
      MoverControllerTileEntity controller = this.getController();
      this.enoughPower = false;
      if (controller != null) {
         this.enoughPower = controller.hasEnoughPower();
      }
   }

   protected void tickClient() {
      this.handleRender();
      this.handleSound();
      this.setCursor();
   }

   public void setHighlightedMover(String highlightedMover) {
      this.highlightedMover = highlightedMover;
   }

   public BlockPos getCursorBlock() {
      return this.cursorBlock;
   }

   public double getCursorX() {
      return this.cursorX;
   }

   public double getCursorY() {
      return this.cursorY;
   }

   public boolean isMoverValid() {
      return this.moverValid;
   }

   public boolean hasEnoughPower() {
      return this.enoughPower;
   }

   public int getCurrentPage() {
      return this.currentPage;
   }

   public void setCurrentPage(int currentPage) {
      this.currentPage = currentPage;
   }

   private void setCursor() {
      HitResult mouseOver = SafeClientTools.getClientMouseOver();
      if (mouseOver instanceof BlockHitResult blockResult) {
         BlockPos pos = blockResult.getBlockPos();
         List<InvisibleMoverBlock.MD> list = ((InvisibleMoverBlock)MoverModule.INVISIBLE_MOVER_BLOCK.get()).getData(this.worldPosition);
         if (list != null) {
            for (InvisibleMoverBlock.MD data : list) {
               if (pos.equals(data.controlPos())) {
                  Pair<Double, Double> cursor = this.getCursor(
                     mouseOver.getLocation().x - pos.getX(),
                     mouseOver.getLocation().y - pos.getY(),
                     mouseOver.getLocation().z - pos.getZ(),
                     data.horizDirection(),
                     data.direction()
                  );
                  this.cursorBlock = pos;
                  this.cursorX = (Double)cursor.getLeft();
                  this.cursorY = (Double)cursor.getRight();
                  return;
               }
            }
         }
      }
   }

   public boolean hasDirectContectionTo(BlockPos destination) {
      return this.network.containsValue(destination);
   }

   public void setClientRenderInfo(List<String> platforms, String currentPlatform, boolean valid, boolean enoughPower) {
      this.platformsFromServer = platforms;
      this.currentPlatform = currentPlatform;
      this.moverValid = valid;
      this.enoughPower = enoughPower;
   }

   public List<String> getPlatformsFromServer() {
      return this.platformsFromServer;
   }

   public String getCurrentPlatform() {
      return this.currentPlatform;
   }

   private void syncVehicleStatus() {
      this.clientUpdateCnt--;
      if (this.clientUpdateCnt <= 0) {
         this.clientUpdateCnt = 20;
         if (!this.getCard().isEmpty()) {
            boolean valid = this.isValid();
            List<String> platforms;
            if (valid) {
               platforms = this.traverseAndCollect().values().stream().map(MoverTileEntity::getName).sorted().collect(Collectors.toList());
            } else {
               platforms = Collections.emptyList();
            }

            RFToolsBuilderMessages.sendToChunk(
               PacketSyncVehicleInformationToClient.create(this.worldPosition, platforms, this.getName(), valid, this.hasEnoughPower()),
               (ServerLevel)this.level,
               this.worldPosition
            );
         }
      }
   }

   private void handleSound() {
      if (this.controller != null) {
         long starttick = this.getLogic().getStarttick();
         long totalTicks = this.getLogic().getTotalTicks();
         long current = this.level.getGameTime();
         long endtick = starttick + totalTicks;
         if (current >= starttick && current <= endtick) {
            Vec3 currentPos = this.getLogic().getMovingPosition(0.0F, this.level.getGameTime());
            if (MoverSoundController.isPlaying(this.level, this.controller, this.worldPosition)) {
               MoverSoundController.move(this.level, this.controller, this.worldPosition, currentPos);
            } else {
               MoverSoundController.play(this.level, this.controller, this.worldPosition, currentPos);
            }
         } else {
            MoverSoundController.stop(this.level, this.controller, this.worldPosition);
         }
      }
   }

   private void handleRender() {
      ItemStack vehicle = this.getCard();
      if (VehicleBuilderTileEntity.isVehicleCard(vehicle)) {
         this.renderCopyTimer = 1;
         MoverRenderer.addPreRender(this.worldPosition, () -> {
            float partialTicks = MoverRenderer.getPartialTicks();
            this.logic.tryMoveVehicleClientEntities(partialTicks);
         }, this::isMoverThere);
         DelayedRenderer.addRender(
            this.worldPosition,
            (poseStack, cameraVec, renderType) -> MoverRenderer.delayedRenderer(this, vehicle, poseStack, cameraVec, renderType),
            this::isMoverThere
         );
      } else if (this.renderCopyTimer > 0) {
         this.renderCopyTimer--;
      }
   }

   @NotNull
   private Boolean isMoverThere(Level level, BlockPos pos) {
      if (level.getBlockEntity(pos) instanceof MoverTileEntity mover) {
         return this.renderCopyTimer > 0 ? true : !mover.getCard().isEmpty();
      } else {
         return false;
      }
   }

   @Nonnull
   public Map<BlockPos, MoverTileEntity> traverseAndCollect() {
      Map<BlockPos, MoverTileEntity> alreadyHandled = new HashMap<>();
      alreadyHandled.put(this.worldPosition, this);
      this.traverseAndCollectInt(alreadyHandled);
      return alreadyHandled;
   }

   private void traverseAndCollectInt(Map<BlockPos, MoverTileEntity> alreadyHandled) {
      for (Entry<Direction, BlockPos> entry : this.getNetwork().entrySet()) {
         BlockPos p = entry.getValue();
         if (!alreadyHandled.containsKey(p) && this.level.getBlockEntity(p) instanceof MoverTileEntity child) {
            alreadyHandled.put(p, child);
            child.traverseAndCollectInt(alreadyHandled);
         }
      }
   }

   @Nullable
   public <T> T traverseDepthFirst(BiFunction<BlockPos, MoverTileEntity, T> function) {
      Set<BlockPos> alreadyHandled = new HashSet<>();
      alreadyHandled.add(this.worldPosition);
      return this.traverseDepthFirstInt(alreadyHandled, function);
   }

   @Nullable
   private <T> T traverseDepthFirstInt(Set<BlockPos> alreadyHandled, BiFunction<BlockPos, MoverTileEntity, T> function) {
      T result = function.apply(this.worldPosition, this);
      if (result != null) {
         return result;
      } else {
         for (Entry<Direction, BlockPos> entry : this.getNetwork().entrySet()) {
            BlockPos p = entry.getValue();
            if (!alreadyHandled.contains(p)) {
               alreadyHandled.add(p);
               if (this.level.getBlockEntity(p) instanceof MoverTileEntity child) {
                  result = child.traverseDepthFirstInt(alreadyHandled, function);
                  if (result != null) {
                     return result;
                  }
               }
            }
         }

         return null;
      }
   }

   @Nullable
   public <T> T traverseBreadthFirst(BiFunction<BlockPos, MoverTileEntity, T> function) {
      Set<BlockPos> alreadyHandled = new HashSet<>();
      List<Pair<BlockPos, MoverTileEntity>> todo = new ArrayList<>();
      todo.add(Pair.of(this.worldPosition, this));
      alreadyHandled.add(this.worldPosition);
      int toProcess = 0;

      for (int toExpand = 0; toExpand < todo.size(); toExpand++) {
         while (toProcess < todo.size()) {
            Pair<BlockPos, MoverTileEntity> pair = todo.get(toProcess);
            T result = function.apply((BlockPos)pair.getLeft(), (MoverTileEntity)pair.getRight());
            if (result != null) {
               return result;
            }

            toProcess++;
         }

         for (Entry<Direction, BlockPos> entry : ((MoverTileEntity)todo.get(toExpand).getRight()).getNetwork().entrySet()) {
            BlockPos childPos = entry.getValue();
            if (!alreadyHandled.contains(childPos) && this.level.getBlockEntity(childPos) instanceof MoverTileEntity childMover) {
               alreadyHandled.add(childPos);
               todo.add(Pair.of(childPos, childMover));
            }
         }
      }

      return null;
   }

   @Nullable
   public <T> T traverseBreadthFirstWithPath(BiFunction<List<BlockPos>, MoverTileEntity, T> function) {
      Set<BlockPos> alreadyHandled = new HashSet<>();
      List<Pair<List<BlockPos>, MoverTileEntity>> todo = new ArrayList<>();
      todo.add(Pair.of(new ArrayList(), this));
      alreadyHandled.add(this.worldPosition);
      int toProcess = 0;

      for (int toExpand = 0; toExpand < todo.size(); toExpand++) {
         while (toProcess < todo.size()) {
            Pair<List<BlockPos>, MoverTileEntity> pair = todo.get(toProcess);
            T result = function.apply((List<BlockPos>)pair.getLeft(), (MoverTileEntity)pair.getRight());
            if (result != null) {
               return result;
            }

            toProcess++;
         }

         for (Entry<Direction, BlockPos> entry : ((MoverTileEntity)todo.get(toExpand).getRight()).getNetwork().entrySet()) {
            BlockPos childPos = entry.getValue();
            if (!alreadyHandled.contains(childPos) && this.level.getBlockEntity(childPos) instanceof MoverTileEntity childMover) {
               alreadyHandled.add(childPos);
               if (childMover.isAvailable()) {
                  List<BlockPos> path = new ArrayList<>((Collection<? extends BlockPos>)todo.get(toExpand).getLeft());
                  path.add(childPos);
                  todo.add(Pair.of(path, childMover));
               }
            }
         }
      }

      return null;
   }

   public boolean isValid() {
      return this.controller == null ? false : this.level.getBlockEntity(this.controller) instanceof MoverControllerTileEntity;
   }

   public boolean isMoving() {
      return this.getCard().isEmpty() ? false : this.logic.getDestination() != null;
   }

   private void updateVehicleStatus() {
      if (this.logic.getDestination() != null) {
         if (this.invisibleMoverBlocks != null) {
            BlockState invisibleState = ((InvisibleMoverBlock)MoverModule.INVISIBLE_MOVER_BLOCK.get()).defaultBlockState();
            this.invisibleMoverBlocks.forEach((p, st) -> {
               BlockState state = this.level.getBlockState(p);
               if (state == invisibleState) {
                  this.level.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
               }
            });
            this.invisibleMoverBlocks = null;
         }
      } else {
         if (this.invisibleMoverBlocks == null) {
            this.updateVehicle();
            this.cnt = 0;
         }

         this.cnt--;
         if (this.cnt <= 0) {
            this.cnt = 4;
            BlockState invisibleState = ((InvisibleMoverBlock)MoverModule.INVISIBLE_MOVER_BLOCK.get()).defaultBlockState();
            this.invisibleMoverBlocks.forEach((p, originalState) -> {
               BlockState state = this.level.getBlockState(p);
               if (state != invisibleState && state.canBeReplaced()) {
                  this.level.setBlock(p, invisibleState, 3);
                  if (this.level.getBlockEntity(p) instanceof InvisibleMoverBE invisibleMover) {
                     invisibleMover.setOriginalState(originalState);
                     this.level.sendBlockUpdated(p, invisibleState, invisibleState, 3);
                  }
               }
            });
         }
      }
   }

   private void updateVehicle() {
      ItemStack vehicle = this.items.getStackInSlot(0);
      if (this.invisibleMoverBlocks == null) {
         this.invisibleMoverBlocks = new HashMap<>();
      }

      this.cnt = 0;
      this.removeInvisibleBlocks();
      if (!vehicle.isEmpty()) {
         Map<BlockState, List<BlockPos>> blocks = VehicleCard.getBlocks(vehicle, this.worldPosition.offset(this.offset));

         for (Entry<BlockState, List<BlockPos>> entry : blocks.entrySet()) {
            for (BlockPos pos : entry.getValue()) {
               this.invisibleMoverBlocks.put(pos, entry.getKey());
            }
         }
      } else {
         this.logic.setDestination(null);
      }

      this.markDirtyClient();
   }

   private void removeInvisibleBlocks() {
      BlockState invisibleState = ((InvisibleMoverBlock)MoverModule.INVISIBLE_MOVER_BLOCK.get()).defaultBlockState();
      this.invisibleMoverBlocks.forEach((p, st) -> {
         BlockState state = this.level.getBlockState(p);
         if (state == invisibleState) {
            this.level.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
         }
      });
      this.invisibleMoverBlocks.clear();
   }

   public void arriveAtDestination() {
      if (this.level.getBlockEntity(this.logic.getDestination()) instanceof MoverTileEntity destMover) {
         this.getLogic().endMoveServer();
         destMover.items.setStackInSlot(0, this.getCard());
         this.items.setStackInSlot(0, ItemStack.EMPTY);
         destMover.setSource(null);
         destMover.getLogic().setWaitABit(5);
         destMover.updateVehicle();
         destMover.updateVehicleStatus();
      }

      this.logic.setDestination(null);
      this.cnt = 0;
      this.updateVehicle();
      this.updateVehicleStatus();
      this.markDirtyClient();
   }

   public void setSource(BlockPos pos) {
      this.logic.setSource(pos);
      this.setChanged();
   }

   public boolean isAvailable() {
      ItemStack vehicle = this.items.getStackInSlot(0);
      if (!vehicle.isEmpty()) {
         return false;
      } else {
         return this.logic.getDestination() != null ? false : this.logic.getSource() == null;
      }
   }

   public String getConnectionCount() {
      return this.connections;
   }

   public void setConnectionCount(String v) {
      this.connections = v;
   }

   public ItemStack getCard() {
      return this.items.getStackInSlot(0);
   }

   public void clearNetwork() {
      this.network.clear();
      this.connections = "";
      this.setChanged();
   }

   public boolean canConnect(Direction direction) {
      MoverData data = (MoverData)this.getData(MoverModule.MOVER_DATA);

      return switch (direction) {
         case DOWN -> data.down();
         case UP -> data.up();
         case NORTH -> data.north();
         case SOUTH -> data.south();
         case WEST -> data.west();
         case EAST -> data.east();
         default -> throw new MatchException(null, null);
      };
   }

   public void hitScreenClient(BlockPos pos, double x, double y, double z, Direction hitDirection, Direction horizDirection, Direction direction) {
      if (hitDirection == direction) {
         Pair<Double, Double> pair = this.getCursor(x, y, z, horizDirection, direction);
         this.cursorBlock = pos;
         this.cursorX = (Double)pair.getLeft();
         this.cursorY = (Double)pair.getRight();
         if (this.highlightedMover != null && !this.highlightedMover.isEmpty()) {
            if ("___<___".equals(this.highlightedMover)) {
               if (this.currentPage > 0) {
                  this.currentPage--;
               }
            } else if ("___>___".equals(this.highlightedMover)) {
               this.currentPage++;
               int pages = (this.platformsFromServer.size() + 9 - 1) / 9;
               if (this.currentPage >= pages) {
                  this.currentPage = pages - 1;
               }
            } else {
               RFToolsBuilderMessages.sendToServer(PacketClickMover.create(this.worldPosition, this.highlightedMover));
            }
         }
      }
   }

   public void startMove(String mover) {
      if (this.controller != null
         && !this.getCard().isEmpty()
         && this.level.getBlockEntity(this.controller) instanceof MoverControllerTileEntity controllerTile) {
         controllerTile.setupMovement(mover, VehicleCard.getVehicleName(this.getCard()));
      }
   }

   public void setController(MoverControllerTileEntity controller) {
      this.controller = controller.getBlockPos();
      this.setChanged();
   }

   public MoverControllerTileEntity getController() {
      if (this.controller == null) {
         return null;
      } else {
         return this.level.getBlockEntity(this.controller) instanceof MoverControllerTileEntity controller ? controller : null;
      }
   }

   @NotNull
   private Pair<Double, Double> getCursor(double x, double y, double z, Direction horizDirection, Direction direction) {
      return switch (direction) {
         case DOWN -> {
            switch (horizDirection) {
               case DOWN:
                  yield Pair.of(1.0 - x, z);
               case UP:
                  yield Pair.of(1.0 - x, z);
               case NORTH:
                  yield Pair.of(1.0 - x, z);
               case SOUTH:
                  yield Pair.of(x, 1.0 - z);
               case WEST:
                  yield Pair.of(z, 1.0 - x);
               case EAST:
                  yield Pair.of(1.0 - z, x);
               default:
                  throw new MatchException(null, null);
            }
         }
         case UP -> {
            switch (horizDirection) {
               case DOWN:
                  yield Pair.of(1.0 - x, 1.0 - z);
               case UP:
                  yield Pair.of(1.0 - x, 1.0 - z);
               case NORTH:
                  yield Pair.of(1.0 - x, 1.0 - z);
               case SOUTH:
                  yield Pair.of(x, z);
               case WEST:
                  yield Pair.of(1.0 - z, 1.0 - x);
               case EAST:
                  yield Pair.of(z, x);
               default:
                  throw new MatchException(null, null);
            }
         }
         case NORTH -> Pair.of(1.0 - x, 1.0 - y);
         case SOUTH -> Pair.of(x, 1.0 - y);
         case WEST -> Pair.of(z, 1.0 - y);
         case EAST -> Pair.of(1.0 - z, 1.0 - y);
         default -> throw new MatchException(null, null);
      };
   }

   public void addConnection(Direction direction, BlockPos pos) {
      this.network.put(direction, pos);
      this.connections = this.connections + direction.name().toUpperCase().charAt(0);
      this.setChanged();
   }

   public void loadAdditional(CompoundTag tag, Provider provider) {
      super.loadAdditional(tag, provider);
      this.items.load(tag, "items", provider);

      for (Direction direction : OrientationTools.DIRECTION_VALUES) {
         if (tag.contains(direction.name())) {
            Optional.ofNullable(CompatNbt.getBlockPos(tag, direction.name())).ifPresent(p -> this.addConnection(direction, p));
         }
      }

      this.logic.load(tag);
      int[] controller = tag.getIntArray("controller").orElseGet(() -> new int[0]);
      if (controller.length >= 3) {
         this.controller = new BlockPos(controller[0], controller[1], controller[2]);
      } else {
         this.controller = null;
      }

      this.offset = new BlockPos(tag.getIntOr("offsetX", 0), tag.getIntOr("offsetY", 0), tag.getIntOr("offsetZ", 0));
   }

   public void saveAdditional(@Nonnull CompoundTag tag, Provider provider) {
      super.saveAdditional(tag, provider);
      this.items.save(tag, "items", provider);

      for (Direction direction : OrientationTools.DIRECTION_VALUES) {
         if (this.network.containsKey(direction)) {
            CompatNbt.putBlockPos(tag, direction.name(), this.network.get(direction));
         }
      }

      this.logic.save(tag);
      if (this.controller != null) {
         tag.putIntArray("controller", new int[]{this.controller.getX(), this.controller.getY(), this.controller.getZ()});
      }

      tag.putInt("offsetX", this.offset.getX());
      tag.putInt("offsetY", this.offset.getY());
      tag.putInt("offsetZ", this.offset.getZ());
   }

   protected void applyImplicitComponents(DataComponentGetter input) {
      super.applyImplicitComponents(input);
      this.items.applyImplicitComponents((ItemInventory)input.get((DataComponentType)Registration.ITEM_INVENTORY.get()));
      MoverData moverData = (MoverData)input.get(MoverModule.ITEM_MOVER_DATA);
      if (moverData != null) {
         this.setData(MoverModule.MOVER_DATA, moverData);
      }
   }

   protected void collectImplicitComponents(Builder builder) {
      super.collectImplicitComponents(builder);
      this.items.collectImplicitComponents(builder);
      builder.set(MoverModule.ITEM_MOVER_DATA, (MoverData)this.getData(MoverModule.MOVER_DATA));
   }

   public void saveClientDataToNBT(CompoundTag tagCompound, Provider provider) {
      ItemStack card = this.items.getStackInSlot(0);
      if (!card.isEmpty()) {
         new CompoundTag();
         tagCompound.put("card", CompatNbt.encodeItemStack(card, provider));
      }

      this.logic.saveClientDataToNBT(tagCompound);
      if (this.controller != null) {
         tagCompound.putIntArray("controller", new int[]{this.controller.getX(), this.controller.getY(), this.controller.getZ()});
      }

      tagCompound.putInt("offsetX", this.offset.getX());
      tagCompound.putInt("offsetY", this.offset.getY());
      tagCompound.putInt("offsetZ", this.offset.getZ());
   }

   public void loadClientDataFromNBT(CompoundTag tagCompound, Provider provider) {
      CompoundTag tag = tagCompound.getCompoundOrEmpty("card");
      this.items.setStackInSlot(0, CompatNbt.decodeItemStack(tag, provider));
      this.logic.loadClientDataFromNBT(tagCompound);
      int[] controller = tagCompound.getIntArray("controller").orElseGet(() -> new int[0]);
      if (controller.length >= 3) {
         this.controller = new BlockPos(controller[0], controller[1], controller[2]);
      } else {
         this.controller = null;
      }

      this.offset = new BlockPos(tagCompound.getIntOr("offsetX", 0), tagCompound.getIntOr("offsetY", 0), tagCompound.getIntOr("offsetZ", 0));
   }

   public void setOffset(int x, int y, int z) {
      this.offset = new BlockPos(x, y, z);
      this.updateVehicle();
      this.markDirtyClient();
   }

   public BlockPos getOffset() {
      return this.offset;
   }
}
