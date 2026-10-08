package mcjty.rftoolsbuilder.modules.mover.client;

import javax.annotation.Nonnull;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.widgets.Button;
import mcjty.rftoolsbuilder.modules.mover.MoverModule;
import mcjty.rftoolsbuilder.modules.mover.blocks.VehicleBuilderTileEntity;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public class GuiVehicleBuilder extends GenericGuiContainer<VehicleBuilderTileEntity, GenericContainer> {
   private Button createButton;

   public GuiVehicleBuilder(GenericContainer container, Inventory inventory, Component title) {
      super(container, inventory, title, ((BaseBlock)MoverModule.VEHICLE_BUILDER.block().get()).getManualEntry());
   }

   public static void register(RegisterMenuScreensEvent event) {
      event.register(MoverModule.CONTAINER_VEHICLE_BUILDER.get(), GuiVehicleBuilder::new);
   }

   public void init() {
      this.window = new Window(this, this.getBE(), Identifier.fromNamespaceAndPath("rftoolsbuilder", "gui/vehicle_builder.gui"));
      super.init();
      this.initializeFields();
   }

   private void initializeFields() {
      this.createButton = (Button)this.window.findChild("create");
   }

   private void updateFields() {
      if (this.window != null) {
         VehicleBuilderTileEntity tileEntity = (VehicleBuilderTileEntity)this.getBE();
         ItemStack spaceCard = tileEntity.getItems().getStackInSlot(0);
         ItemStack vehicleCard = tileEntity.getItems().getStackInSlot(1);
         this.createButton.enabled(VehicleBuilderTileEntity.isUsableSpaceCard(spaceCard) && VehicleBuilderTileEntity.isVehicleCard(vehicleCard));
      }
   }

   public void extractBackground(@Nonnull GuiGraphicsExtractor graphics, int x, int y, float partialTicks) {
      this.updateFields();
      this.drawWindow(graphics, partialTicks, x, y);
   }
}
