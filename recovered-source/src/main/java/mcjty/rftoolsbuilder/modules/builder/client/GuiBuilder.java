package mcjty.rftoolsbuilder.modules.builder.client;

import javax.annotation.Nonnull;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.widgets.Button;
import mcjty.lib.gui.widgets.EnergyBar;
import mcjty.lib.gui.widgets.ImageChoiceLabel;
import mcjty.lib.varia.LegacyCapabilities;
import mcjty.rftoolsbuilder.modules.builder.BuilderModule;
import mcjty.rftoolsbuilder.modules.builder.blocks.AnchorMode;
import mcjty.rftoolsbuilder.modules.builder.blocks.BuilderTileEntity;
import mcjty.rftoolsbuilder.modules.builder.data.BuilderData;
import mcjty.rftoolsbuilder.modules.builder.items.ShapeCardItem;
import mcjty.rftoolsbuilder.modules.builder.network.PacketCloseContainerAndOpenCardGui;
import mcjty.rftoolsbuilder.setup.RFToolsBuilderMessages;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.items.IItemHandler;

public class GuiBuilder extends GenericGuiContainer<BuilderTileEntity, GenericContainer> {
   private EnergyBar energyBar;
   private Button currentLevel;
   private final ImageChoiceLabel[] anchor = new ImageChoiceLabel[4];

   public GuiBuilder(GenericContainer container, Inventory inventory, Component title) {
      super(container, inventory, title, ((BaseBlock)BuilderModule.BUILDER.block().get()).getManualEntry());
   }

   public static void register(RegisterMenuScreensEvent event) {
      event.register(BuilderModule.CONTAINER_BUILDER.get(), GuiBuilder::new);
   }

   public void init() {
      this.window = new Window(this, this.getBE(), Identifier.fromNamespaceAndPath("rftoolsbuilder", "gui/builder.gui"));
      super.init();
      this.initializeFields();
      this.setupEvents();
   }

   private void setupEvents() {
      this.window.event("cardgui", (source, params) -> this.openCardGui());
      this.window.event("anchor", (source, params) -> this.selectAnchor(source.getName()));
   }

   private void initializeFields() {
      this.energyBar = (EnergyBar)this.window.findChild("energybar");
      this.currentLevel = (Button)this.window.findChild("level");
      this.anchor[0] = (ImageChoiceLabel)this.window.findChild("anchor0");
      this.anchor[1] = (ImageChoiceLabel)this.window.findChild("anchor1");
      this.anchor[2] = (ImageChoiceLabel)this.window.findChild("anchor2");
      this.anchor[3] = (ImageChoiceLabel)this.window.findChild("anchor3");
      this.updateFields();
   }

   private void updateFields() {
      if (this.window != null) {
         int cury = BuilderTileEntity.getCurrentLevelClientSide();
         this.currentLevel.text("Y: " + (cury == -1 ? "stop" : cury));
         BuilderTileEntity tileEntity = (BuilderTileEntity)this.getBE();
         IItemHandler capability = (IItemHandler)this.minecraft.level.getCapability(LegacyCapabilities.ITEM_BLOCK, tileEntity.getBlockPos(), null);
         ItemStack card = capability == null ? ItemStack.EMPTY : capability.getStackInSlot(0);
         if (card.isEmpty()) {
            this.window.setFlag("!validcard");
         } else if (card.getItem() instanceof ShapeCardItem) {
            this.window.setFlag("!validcard");
         } else {
            this.window.setFlag("validcard");
         }

         this.updateAnchorSettings(tileEntity.getAnchor());
         this.updateEnergyBar(this.energyBar);
      }
   }

   private void openCardGui() {
      ItemStack cardStack = ((GenericContainer)this.menu).getSlot(0).getItem();
      if (!cardStack.isEmpty()) {
         BuilderTileEntity tileEntity = (BuilderTileEntity)this.getBE();
         GuiShapeCard.fromTEPos = tileEntity.getBlockPos();
         GuiShapeCard.fromTEStackSlot = 0;
         RFToolsBuilderMessages.sendToServer(PacketCloseContainerAndOpenCardGui.create(tileEntity.getBlockPos()));
      }
   }

   private void selectAnchor(String name) {
      int index = name.charAt(name.length() - 1) - '0';
      this.updateAnchorSettings(AnchorMode.values()[index]);
      BuilderData data = ((BuilderData)this.getBE().getData((AttachmentType)BuilderModule.BUILDER_DATA.get())).withAnchor(AnchorMode.values()[index]);
      this.getBE().setData((AttachmentType)BuilderModule.BUILDER_DATA.get(), data);
      this.window.syncDataToServer((AttachmentType)BuilderModule.BUILDER_DATA.get(), this.getBE());
   }

   private void updateAnchorSettings(AnchorMode index) {
      for (int i = 0; i < this.anchor.length; i++) {
         if (this.isShapeCard()) {
            this.anchor[i].setCurrentChoice(0);
         } else {
            this.anchor[i].setCurrentChoice(i == index.ordinal() ? 1 : 0);
         }
      }
   }

   private boolean isShapeCard() {
      BuilderTileEntity tileEntity = (BuilderTileEntity)this.getBE();
      IItemHandler capability = (IItemHandler)tileEntity.getLevel().getCapability(LegacyCapabilities.ITEM_BLOCK, tileEntity.getBlockPos(), null);
      ItemStack card = capability == null ? ItemStack.EMPTY : capability.getStackInSlot(0);
      return !card.isEmpty() && card.getItem() instanceof ShapeCardItem;
   }

   public void extractBackground(@Nonnull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
      this.updateFields();
      this.drawWindow(graphics, partialTicks, mouseX, mouseY);
   }
}
