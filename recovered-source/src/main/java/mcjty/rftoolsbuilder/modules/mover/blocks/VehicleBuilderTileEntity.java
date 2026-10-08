package mcjty.rftoolsbuilder.modules.mover.blocks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import javax.annotation.Nonnull;
import mcjty.lib.api.container.DefaultContainerProvider;
import mcjty.lib.api.container.ItemInventory;
import mcjty.lib.bindings.GuiValue;
import mcjty.lib.bindings.Value;
import mcjty.lib.blockcommands.Command;
import mcjty.lib.blockcommands.ServerCommand;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.builder.InfoLine;
import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.GenericItemHandler;
import mcjty.lib.container.SlotDefinition;
import mcjty.lib.setup.Registration;
import mcjty.lib.tileentity.Cap;
import mcjty.lib.tileentity.CapType;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.varia.ComponentFactory;
import mcjty.lib.varia.LevelTools;
import mcjty.rftoolsbase.tools.ManualHelper;
import mcjty.rftoolsbuilder.compat.RFToolsBuilderTOPDriver;
import mcjty.rftoolsbuilder.modules.builder.BuilderModule;
import mcjty.rftoolsbuilder.modules.builder.BuilderTools;
import mcjty.rftoolsbuilder.modules.builder.SpaceChamberRepository;
import mcjty.rftoolsbuilder.modules.builder.blocks.RotateMode;
import mcjty.rftoolsbuilder.modules.mover.MoverModule;
import mcjty.rftoolsbuilder.modules.mover.data.VehicleBuilderData;
import mcjty.rftoolsbuilder.modules.mover.items.VehicleCard;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponentMap.Builder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;

public class VehicleBuilderTileEntity extends GenericTileEntity {
   public static final int SLOT_SPACE_CARD = 0;
   public static final int SLOT_VEHICLE_CARD = 1;
   @GuiValue
   private String vehicleName = "";
   @GuiValue
   public static final Value<VehicleBuilderTileEntity, String> VALUE_ROTATE = Value.createEnum(
      "rotate", RotateMode.values(), VehicleBuilderTileEntity::getRotate, VehicleBuilderTileEntity::setRotate
   );
   public static final Lazy<ContainerFactory> CONTAINER_FACTORY = Lazy.of(
      () -> new ContainerFactory(2)
         .slot(SlotDefinition.specific(new Item[]{(Item)BuilderModule.SPACE_CHAMBER_CARD.get()}).in(), 0, 64, 24)
         .slot(SlotDefinition.specific(new Item[]{(Item)MoverModule.VEHICLE_CARD.get()}).in().out(), 1, 154, 24)
         .playerSlots(10, 70)
   );
   private final GenericItemHandler items = GenericItemHandler.create(this, CONTAINER_FACTORY).onUpdate((slot, stack) -> {
      if (stack.getItem() == MoverModule.VEHICLE_CARD.get()) {
         this.vehicleName = VehicleCard.getVehicleName(stack);
      } else {
         this.vehicleName = "";
      }
   }).build();
   @Cap(type = CapType.ITEMS_AUTOMATION)
   private static final Function<VehicleBuilderTileEntity, GenericItemHandler> ITEM_CAP = tile -> tile.items;
   @Cap(type = CapType.CONTAINER)
   private static final Function<VehicleBuilderTileEntity, MenuProvider> screenHandler = tile -> new DefaultContainerProvider("Vehicle Builder")
      .containerSupplier(DefaultContainerProvider.container(MoverModule.CONTAINER_VEHICLE_BUILDER, CONTAINER_FACTORY, tile))
      .itemHandler(() -> tile.items)
      .setupSync(tile);
   private static final int MAXDIM = 16;
   @ServerCommand
   public static final Command<?> CMD_CREATE = Command.create("create", (te, player, params) -> te.copyVehicle(player));

   public static BaseBlock createBlock() {
      return new BaseBlock(
         new BlockBuilder()
            .tileEntitySupplier(VehicleBuilderTileEntity::new)
            .topDriver(RFToolsBuilderTOPDriver.DRIVER)
            .infusable()
            .manualEntry(ManualHelper.create("rftoolsbuilder:mover/vehicle_builder"))
            .info(new InfoLine[]{TooltipBuilder.key("message.rftoolsbuilder.shiftmessage")})
            .infoShift(new InfoLine[]{TooltipBuilder.header()})
      );
   }

   public VehicleBuilderTileEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType)MoverModule.VEHICLE_BUILDER.be().get(), pos, state);
   }

   public GenericItemHandler getItems() {
      return this.items;
   }

   public RotateMode getRotate() {
      return ((VehicleBuilderData)this.getData((AttachmentType)MoverModule.VEHICLE_BUILDER_DATA.get())).rotate();
   }

   public void setRotate(RotateMode rotate) {
      VehicleBuilderData data = ((VehicleBuilderData)this.getData((AttachmentType)MoverModule.VEHICLE_BUILDER_DATA.get())).withRotate(rotate);
      this.setData((AttachmentType)MoverModule.VEHICLE_BUILDER_DATA.get(), data);
   }

   private void copyVehicle(Player player) {
      ItemStack spaceCard = this.items.getStackInSlot(0);
      ItemStack vehicleCard = this.items.getStackInSlot(1);
      if (isUsableSpaceCard(spaceCard) && isVehicleCard(vehicleCard)) {
         SpaceChamberRepository.SpaceChamberChannel chamberChannel = BuilderTools.getSpaceChamberChannel(this.level, spaceCard);
         if (chamberChannel != null) {
            BlockPos minCorner = chamberChannel.getMinCorner();
            BlockPos maxCorner = chamberChannel.getMaxCorner();
            if (this.checkValid(player, minCorner, maxCorner)) {
               ResourceKey<Level> dimension = chamberChannel.getDimension();
               ServerLevel world = LevelTools.getLevel(this.level, dimension);
               Map<BlockState, List<Integer>> blocks = this.getBlocks(minCorner, maxCorner, world);
               VehicleCard.storeVehicleInCard(vehicleCard, blocks, this.vehicleName);
            }
         }
      }
   }

   @NotNull
   private Map<BlockState, List<Integer>> getBlocks(BlockPos minCorner, BlockPos maxCorner, ServerLevel world) {
      Map<BlockState, List<Integer>> blocks = new HashMap<>();
      MutableBlockPos mpos = new MutableBlockPos(0, 0, 0);

      Rotation rotation = switch (this.getRotate()) {
         case ROTATE_0 -> Rotation.NONE;
         case ROTATE_90 -> Rotation.CLOCKWISE_90;
         case ROTATE_180 -> Rotation.CLOCKWISE_180;
         case ROTATE_270 -> Rotation.COUNTERCLOCKWISE_90;
      };
      BlockPos realMin;
      if (rotation == Rotation.NONE) {
         realMin = minCorner;
      } else {
         realMin = new BlockPos(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);

         for (int x = minCorner.getX(); x <= maxCorner.getX(); x++) {
            mpos.setX(x);

            for (int y = minCorner.getY(); y <= maxCorner.getY(); y++) {
               mpos.setY(y);

               for (int z = minCorner.getZ(); z <= maxCorner.getZ(); z++) {
                  mpos.setZ(z);
                  BlockPos rotated = mpos.rotate(rotation);
                  realMin = new BlockPos(
                     Math.min(realMin.getX(), rotated.getX()), Math.min(realMin.getY(), rotated.getY()), Math.min(realMin.getZ(), rotated.getZ())
                  );
               }
            }
         }
      }

      for (int x = minCorner.getX(); x <= maxCorner.getX(); x++) {
         mpos.setX(x);

         for (int y = minCorner.getY(); y <= maxCorner.getY(); y++) {
            mpos.setY(y);

            for (int z = minCorner.getZ(); z <= maxCorner.getZ(); z++) {
               mpos.setZ(z);
               BlockState state = world.getBlockState(mpos);
               if (!state.isAir()) {
                  BlockPos p;
                  if (rotation != Rotation.NONE) {
                     if (state.getBlock() instanceof MoverControlBlock) {
                        System.out.println("Before: " + state + ", Rotation: " + rotation);
                     }

                     state = state.rotate(rotation);
                     if (state.getBlock() instanceof MoverControlBlock) {
                        System.out.println("After: " + state);
                     }

                     p = mpos.rotate(rotation);
                  } else {
                     p = mpos;
                  }

                  blocks.computeIfAbsent(state, s -> new ArrayList<>()).add(VehicleCard.convertPosToInt(realMin, p));
               }
            }
         }
      }

      return blocks;
   }

   private void rotatePos(MutableBlockPos pos, Rotation rotation) {
      switch (rotation) {
         case NONE:
         default:
            break;
         case CLOCKWISE_90:
            pos.set(-pos.getZ(), pos.getY(), pos.getX());
            break;
         case CLOCKWISE_180:
            pos.set(-pos.getX(), pos.getY(), -pos.getZ());
            break;
         case COUNTERCLOCKWISE_90:
            pos.set(pos.getZ(), pos.getY(), -pos.getX());
      }
   }

   private boolean checkValid(Player player, BlockPos minCorner, BlockPos maxCorner) {
      if (maxCorner.getX() - minCorner.getX() >= 16) {
         player.sendSystemMessage(ComponentFactory.literal("Space chamber too large (max 16x16x16)!"));
         return false;
      } else if (maxCorner.getY() - minCorner.getY() >= 16) {
         player.sendSystemMessage(ComponentFactory.literal("Space chamber too large (max 16x16x16)!"));
         return false;
      } else if (maxCorner.getZ() - minCorner.getZ() >= 16) {
         player.sendSystemMessage(ComponentFactory.literal("Space chamber too large (max 16x16x16)!"));
         return false;
      } else {
         return true;
      }
   }

   public static boolean isUsableSpaceCard(ItemStack stack) {
      return stack.getItem() != BuilderModule.SPACE_CHAMBER_CARD.get() ? false : BuilderTools.getChannel(stack) != null;
   }

   public static boolean isVehicleCard(ItemStack stack) {
      return stack.getItem() == MoverModule.VEHICLE_CARD.get();
   }

   public void saveAdditional(@Nonnull ValueOutput tag) {
      super.saveAdditional(tag);
      this.items.save(tag, "items");
   }

   public void loadAdditional(@Nonnull ValueInput tag) {
      super.loadAdditional(tag);
      this.items.load(tag, "items");
   }

   protected void applyImplicitComponents(DataComponentGetter input) {
      super.applyImplicitComponents(input);
      this.items.applyImplicitComponents((ItemInventory)input.get((DataComponentType)Registration.ITEM_INVENTORY.get()));
      VehicleBuilderData vehicleBuilderData = (VehicleBuilderData)input.get(MoverModule.ITEM_VEHICLE_BUILDER_DATA);
      if (vehicleBuilderData != null) {
         this.setData(MoverModule.VEHICLE_BUILDER_DATA, vehicleBuilderData);
      }
   }

   protected void collectImplicitComponents(Builder builder) {
      super.collectImplicitComponents(builder);
      this.items.collectImplicitComponents(builder);
      builder.set(MoverModule.ITEM_VEHICLE_BUILDER_DATA, (VehicleBuilderData)this.getData(MoverModule.VEHICLE_BUILDER_DATA));
   }
}
