package mcjty.rftoolsbuilder.modules.mover.items;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.varia.CompositeStreamCodec;
import mcjty.lib.varia.LevelTools;
import mcjty.rftoolsbase.api.screens.IScreenDataHelper;
import mcjty.rftoolsbase.api.screens.IScreenModule;
import mcjty.rftoolsbase.api.screens.TextAlign;
import mcjty.rftoolsbase.api.screens.data.IModuleDataString;
import mcjty.rftoolsbuilder.modules.mover.MoverConfiguration;
import mcjty.rftoolsbuilder.modules.mover.blocks.MoverControllerTileEntity;
import mcjty.rftoolsbuilder.modules.mover.blocks.MoverTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public record VehicleStatusScreenModule(GlobalPos pos, String label, String vehicle, String monitor, TextAlign align, int labelColor, int color)
   implements IScreenModule<VehicleStatusScreenModule, IModuleDataString> {
   public static final VehicleStatusScreenModule DEFAULT = new VehicleStatusScreenModule(
      GlobalPos.of(Level.OVERWORLD, BlockPosTools.INVALID), "", "", "", TextAlign.ALIGN_LEFT, 16777215, 16777215
   );
   public static final Codec<VehicleStatusScreenModule> CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
            GlobalPos.CODEC.fieldOf("pos").forGetter(module -> module.pos),
            Codec.STRING.fieldOf("label").forGetter(module -> module.label),
            Codec.STRING.fieldOf("vehicle").forGetter(module -> module.vehicle),
            Codec.STRING.fieldOf("monitor").forGetter(module -> module.monitor),
            TextAlign.CODEC.fieldOf("align").forGetter(module -> module.align),
            Codec.INT.fieldOf("labelColor").forGetter(module -> module.labelColor),
            Codec.INT.fieldOf("color").forGetter(module -> module.color)
         )
         .apply(instance, VehicleStatusScreenModule::new)
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, VehicleStatusScreenModule> STREAM_CODEC = CompositeStreamCodec.composite(
      GlobalPos.STREAM_CODEC,
      module -> module.pos,
      ByteBufCodecs.STRING_UTF8,
      module -> module.label,
      ByteBufCodecs.STRING_UTF8,
      module -> module.vehicle,
      ByteBufCodecs.STRING_UTF8,
      module -> module.monitor,
      TextAlign.STREAM_CODEC,
      module -> module.align,
      ByteBufCodecs.INT,
      module -> module.labelColor,
      ByteBufCodecs.INT,
      module -> module.color,
      VehicleStatusScreenModule::new
   );

   public GlobalPos getPos() {
      return this.pos;
   }

   public String getLabel() {
      return this.label;
   }

   public String getVehicle() {
      return this.vehicle;
   }

   public String getMonitor() {
      return this.monitor;
   }

   public TextAlign getAlign() {
      return this.align;
   }

   public int getLabelColor() {
      return this.labelColor;
   }

   public int getColor() {
      return this.color;
   }

   public VehicleStatusScreenModule withLabel(String label) {
      return new VehicleStatusScreenModule(this.pos, label, this.vehicle, this.monitor, this.align, this.labelColor, this.color);
   }

   public VehicleStatusScreenModule withVehicle(String vehicle) {
      return new VehicleStatusScreenModule(this.pos, this.label, vehicle, this.monitor, this.align, this.labelColor, this.color);
   }

   public VehicleStatusScreenModule withMonitor(String monitor) {
      return new VehicleStatusScreenModule(this.pos, this.label, this.vehicle, monitor, this.align, this.labelColor, this.color);
   }

   public VehicleStatusScreenModule withAlign(TextAlign align) {
      return new VehicleStatusScreenModule(this.pos, this.label, this.vehicle, this.monitor, align, this.labelColor, this.color);
   }

   public VehicleStatusScreenModule withLabelColor(int labelColor) {
      return new VehicleStatusScreenModule(this.pos, this.label, this.vehicle, this.monitor, this.align, labelColor, this.color);
   }

   public VehicleStatusScreenModule withColor(int color) {
      return new VehicleStatusScreenModule(this.pos, this.label, this.vehicle, this.monitor, this.align, this.labelColor, color);
   }

   public VehicleStatusScreenModule withPos(GlobalPos pos) {
      return new VehicleStatusScreenModule(pos, this.label, this.vehicle, this.monitor, this.align, this.labelColor, this.color);
   }

   public IModuleDataString getData(IScreenDataHelper helper, Level level, long millis) {
      String mover = getMoverController(level, this.pos.dimension(), this.pos.pos()).map(c -> {
         MoverTileEntity m = c.findVehicle(this.vehicle);
         return m != null ? m.getName() : "<unknown>";
      }).orElse("<unknown>");
      return helper.createString(mover);
   }

   public VehicleStatusScreenModule validate(Level world, BlockPos pos, boolean isPlus) {
      return this;
   }

   public int getRfPerTick() {
      return (Integer)MoverConfiguration.VEHICLE_STATUS_RFPERTICK.get();
   }

   public ItemStack mouseClick(ItemStack moduleStack, Level world, int x, int y, boolean clicked, Player player) {
      return ItemStack.EMPTY;
   }

   public static Optional<MoverControllerTileEntity> getMoverController(Level worldObj, ResourceKey<Level> dim, BlockPos coordinate) {
      Level world = LevelTools.getLevel(worldObj, dim);
      if (world == null) {
         return Optional.empty();
      } else if (!LevelTools.isLoaded(world, coordinate)) {
         return Optional.empty();
      } else {
         BlockEntity te = world.getBlockEntity(coordinate);
         if (te == null) {
            return Optional.empty();
         } else {
            return !(te instanceof MoverControllerTileEntity) ? Optional.empty() : Optional.of((MoverControllerTileEntity)te);
         }
      }
   }
}
