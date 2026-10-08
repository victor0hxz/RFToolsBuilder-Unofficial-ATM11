package mcjty.rftoolsbuilder.modules.mover.client;

import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.GuiPopupTools;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.events.SelectionEvent;
import mcjty.lib.gui.widgets.Button;
import mcjty.lib.gui.widgets.EnergyBar;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.Widget;
import mcjty.lib.gui.widgets.WidgetList;
import mcjty.lib.gui.widgets.Widgets;
import mcjty.lib.network.Networking;
import mcjty.lib.network.PacketGetListFromServer;
import mcjty.lib.typed.TypedMap;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.mover.MoverModule;
import mcjty.rftoolsbuilder.modules.mover.blocks.MoverControllerTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import org.apache.commons.lang3.tuple.Pair;

public class GuiMoverController extends GenericGuiContainer<MoverControllerTileEntity, GenericContainer> {
   private EnergyBar energyBar;
   private SyncedList<String> vehicleList;
   private SyncedList<Pair<BlockPos, String>> nodeList;

   public GuiMoverController(GenericContainer container, Inventory inventory, Component title) {
      super(container, inventory, title, ((BaseBlock)MoverModule.MOVER_CONTROLLER.block().get()).getManualEntry());
   }

   public static void register(RegisterMenuScreensEvent event) {
      event.register(MoverModule.CONTAINER_MOVER_CONTROLLER.get(), GuiMoverController::new);
   }

   public void init() {
      this.window = new Window(this, this.getBE(), Identifier.fromNamespaceAndPath("rftoolsbuilder", "gui/mover_controller.gui"));
      super.init();
      this.initializeFields();
      this.setupEvents();
      this.vehicleList.refresh();
      this.nodeList.refresh();
   }

   private void setupEvents() {
      ((Button)this.window.findChild("scan")).event(this::doScan);
      ((Button)this.window.findChild("move")).event(this::doMove);
      this.nodeList.getList().event(new SelectionEvent() {
         {
            Objects.requireNonNull(GuiMoverController.this);
         }

         public void select(int index) {
         }

         public void doubleClick(int index) {
            GuiMoverController.this.selectNode();
         }
      });
   }

   private void doMove() {
      if (this.nodeList.getSelected() == null) {
         GuiPopupTools.showMessage(this.minecraft, this, this.getWindowManager(), 100, 100, "Please select a node!");
      } else if (this.vehicleList.getSelected() == null) {
         GuiPopupTools.showMessage(this.minecraft, this, this.getWindowManager(), 100, 100, "Please select a vehicle!");
      } else {
         this.sendServerCommandTyped(
            MoverControllerTileEntity.CMD_MOVE,
            TypedMap.builder()
               .put(MoverControllerTileEntity.SELECTED_NODE, (BlockPos)this.nodeList.getSelected().getLeft())
               .put(MoverControllerTileEntity.SELECTED_VEHICLE, this.vehicleList.getSelected())
               .put(MoverControllerTileEntity.SELECTED_DESTINATION, (String)this.nodeList.getSelected().getRight())
               .build()
         );
         this.vehicleList.refresh();
         this.nodeList.refresh();
      }
   }

   private void doScan() {
      this.sendServerCommandTyped(MoverControllerTileEntity.CMD_SCAN, TypedMap.EMPTY);
      this.vehicleList.refresh();
      this.nodeList.refresh();
   }

   private void selectNode() {
      Pair<BlockPos, String> selected = this.nodeList.getSelected();
      if (selected != null) {
         this.sendServerCommandTyped(
            MoverControllerTileEntity.CMD_SELECTNODE, TypedMap.builder().put(MoverControllerTileEntity.SELECTED_NODE, (BlockPos)selected.getLeft()).build()
         );
      }
   }

   private void initializeFields() {
      this.energyBar = (EnergyBar)this.window.findChild("energybar");
      this.vehicleList = new SyncedList<>((WidgetList)this.window.findChild("vehicles"), this::requestVehicles, this::makeVehicleLine, 10);
      this.nodeList = new SyncedList<>((WidgetList)this.window.findChild("nodes"), this::requestNodes, this::makeNodeLine, 20);
      this.updateFields();
   }

   private void updateFields() {
      this.updateEnergyBar(this.energyBar);
      this.vehicleList.populateLists();
      this.nodeList.populateLists();
   }

   public static void setVehiclesFromServer(List<String> vehicles) {
      if (Minecraft.getInstance().screen instanceof GuiMoverController gui) {
         gui.vehicleList.setFromServerList(vehicles);
      } else {
         RFToolsBuilder.setup.getLogger().warn("This is not a gui for the mover controller!");
      }
   }

   public static void setNodesFromServer(List<Pair<BlockPos, String>> nodes) {
      if (Minecraft.getInstance().screen instanceof GuiMoverController gui) {
         gui.nodeList.setFromServerList(nodes);
      } else {
         RFToolsBuilder.setup.getLogger().warn("This is not a gui for the mover controller!");
      }
   }

   public static void setSelectedVehicle(String vehicle) {
      if (Minecraft.getInstance().screen instanceof GuiMoverController gui) {
         gui.vehicleList.select(vehicle);
      } else {
         RFToolsBuilder.setup.getLogger().warn("This is not a gui for the mover controller!");
      }
   }

   private void requestVehicles() {
      Networking.sendToServer(PacketGetListFromServer.create(this.getBE().getBlockPos(), MoverControllerTileEntity.CMD_GETVEHICLES.name()));
   }

   private void requestNodes() {
      Networking.sendToServer(PacketGetListFromServer.create(this.getBE().getBlockPos(), MoverControllerTileEntity.CMD_GETNODES.name()));
   }

   private Panel makeVehicleLine(String vehicle) {
      Panel panel = (Panel)Widgets.horizontal(0, 0).hint(0, 0, 130, 14);
      panel.children(new Widget[]{Widgets.label(vehicle)});
      return panel;
   }

   private Panel makeNodeLine(Pair<BlockPos, String> node) {
      Panel panel = (Panel)Widgets.horizontal(0, 0).hint(0, 0, 100, 14);
      panel.children(new Widget[]{Widgets.label((String)node.getRight())});
      return panel;
   }

   public void extractBackground(@Nonnull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
      this.updateFields();
      this.drawWindow(graphics, partialTicks, mouseX, mouseY);
   }
}
