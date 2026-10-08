package mcjty.rftoolsbuilder.modules.mover.items;

import com.mojang.serialization.Codec;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import javax.annotation.Nonnull;
import mcjty.lib.crafting.IComponentsToPreserve;
import mcjty.lib.gui.ManualEntry;
import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.varia.Logging;
import mcjty.lib.varia.ModuleTools;
import mcjty.lib.varia.Tools;
import mcjty.rftoolsbase.api.screens.IClientScreenModule;
import mcjty.rftoolsbase.api.screens.IModuleGuiBuilder;
import mcjty.rftoolsbase.api.screens.IScreenModule;
import mcjty.rftoolsbase.api.screens.TextAlign;
import mcjty.rftoolsbase.tools.GenericModuleItem;
import mcjty.rftoolsbase.tools.ManualHelper;
import mcjty.rftoolsbuilder.modules.mover.MoverConfiguration;
import mcjty.rftoolsbuilder.modules.mover.MoverModule;
import mcjty.rftoolsbuilder.modules.mover.blocks.MoverControllerTileEntity;
import mcjty.rftoolsbuilder.setup.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class VehicleControlModuleItem extends GenericModuleItem implements IComponentsToPreserve {
   protected int getUses(ItemStack stack) {
      return (Integer)MoverConfiguration.VEHICLE_CONTROL_RFPERTICK.get();
   }

   protected boolean hasGoldMessage(ItemStack stack) {
      return !BlockPosTools.isValid(data(stack).getPos().pos());
   }

   protected String getInfoString(ItemStack stack) {
      VehicleControlScreenModule data = data(stack);
      return ModuleTools.getTargetString(data.getMonitor(), data.getPos());
   }

   @Nullable
   public Codec<? extends IScreenModule<?, ?>> codec() {
      return VehicleControlScreenModule.CODEC;
   }

   @Nullable
   public StreamCodec<RegistryFriendlyByteBuf, ? extends IScreenModule<?, ?>> streamCodec() {
      return VehicleControlScreenModule.STREAM_CODEC;
   }

   @Nullable
   public DataComponentType<? extends IScreenModule<?, ?>> componentType() {
      return (DataComponentType<? extends IScreenModule<?, ?>>)MoverModule.MODULE_VEHICLECONTROL_DATA.get();
   }

   public IScreenModule<?, ?> createServerScreenModule() {
      return VehicleControlScreenModule.DEFAULT;
   }

   public IClientScreenModule<?> createClientScreenModule() {
      return new VehicleControlClientScreenModule();
   }

   public VehicleControlModuleItem() {
      super(Registration.createStandardProperties().stacksTo(1));
   }

   @Nonnull
   public InteractionResult useOn(UseOnContext context) {
      ItemStack stack = context.getItemInHand();
      Level world = context.getLevel();
      Player player = context.getPlayer();
      BlockPos pos = context.getClickedPos();
      BlockEntity te = world.getBlockEntity(pos);
      VehicleControlScreenModule data = data(stack);
      if (te instanceof MoverControllerTileEntity) {
         BlockState state = world.getBlockState(pos);
         Block block = state.getBlock();
         String name = "<invalid>";
         if (block != null && !world.getBlockState(pos).isAir()) {
            name = Tools.getReadableName(world, pos);
         }

         data = data.withPos(GlobalPos.of(world.dimension(), pos));
         data = data.withMonitor(name);
         if (world.isClientSide()) {
            Logging.message(player, "Vehicle control module is set to block '" + name + "'");
         }
      } else {
         data = data.withPos(GlobalPos.of(Level.OVERWORLD, BlockPosTools.INVALID));
         data = data.withMonitor("");
         if (world.isClientSide()) {
            Logging.message(player, "Vehicle control module is cleared");
         }
      }

      stack.set(MoverModule.MODULE_VEHICLECONTROL_DATA, data);
      return InteractionResult.SUCCESS;
   }

   public String getModuleName() {
      return "Veh";
   }

   public static VehicleControlScreenModule data(ItemStack stack) {
      VehicleControlScreenModule data = (VehicleControlScreenModule)stack.get(MoverModule.MODULE_VEHICLECONTROL_DATA);
      if (data == null) {
         data = VehicleControlScreenModule.DEFAULT;
      }

      return data;
   }

   public static void data(ItemStack stack, Function<VehicleControlScreenModule, VehicleControlScreenModule> setter) {
      VehicleControlScreenModule data = data(stack);
      data = setter.apply(data);
      stack.set(MoverModule.MODULE_VEHICLECONTROL_DATA, data);
   }

   public void createGui(IModuleGuiBuilder guiBuilder) {
      guiBuilder.label("Label:")
         .text((stack, s) -> data(stack).withLine(s), stack -> data(stack).getLine(), new String[]{"Label text"})
         .color((stack, c) -> data(stack).withColor(c), stack -> data(stack).getColor(), new String[]{"Label color"})
         .nl()
         .label("Button:")
         .text((stack, s) -> data(stack).withButton(s), stack -> data(stack).getButton(), new String[]{"Button text"})
         .color((stack, c) -> data(stack).withButtonColor(c), stack -> data(stack).getButtonColor(), new String[]{"Button color"})
         .nl()
         .label("Mover:")
         .text((stack, s) -> data(stack).withMover(s), stack -> data(stack).getMover(), new String[]{"Name of the mover"})
         .nl()
         .label("Vehicle:")
         .text((stack, s) -> data(stack).withVehicle(s), stack -> data(stack).getVehicle(), new String[]{"Name of the vehicle"})
         .nl()
         .choices(
            (stack, c) -> data(stack, d -> d.withAlign(TextAlign.get(c))),
            stack -> data(stack).getAlign().getSerializedName(),
            "Label alignment",
            new String[]{"Left", "Center", "Right"}
         )
         .nl();
   }

   public Collection<DataComponentType<?>> getComponentsToPreserve() {
      return List.of((DataComponentType<?>)MoverModule.MODULE_VEHICLECONTROL_DATA.get());
   }

   public ManualEntry getManualEntry() {
      return ManualHelper.create("rftoolsbuilder:mover/vehicle_modules");
   }
}
