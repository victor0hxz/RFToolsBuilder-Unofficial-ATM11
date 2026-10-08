package mcjty.rftoolsbuilder.modules.mover.client;

import javax.annotation.Nonnull;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.rftoolsbuilder.modules.mover.MoverModule;
import mcjty.rftoolsbuilder.modules.mover.blocks.MoverTileEntity;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public class GuiMover extends GenericGuiContainer<MoverTileEntity, GenericContainer> {
   public GuiMover(GenericContainer container, Inventory inventory, Component title) {
      super(container, inventory, title, ((BaseBlock)MoverModule.MOVER.block().get()).getManualEntry());
   }

   public static void register(RegisterMenuScreensEvent event) {
      event.register(MoverModule.CONTAINER_MOVER.get(), GuiMover::new);
   }

   public void init() {
      this.window = new Window(this, this.getBE(), Identifier.fromNamespaceAndPath("rftoolsbuilder", "gui/mover.gui"));
      super.init();
      this.initializeFields();
      this.setupEvents();
   }

   private void setupEvents() {
   }

   private void initializeFields() {
      this.updateFields();
   }

   private void updateFields() {
   }

   public void extractBackground(@Nonnull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
      this.updateFields();
      this.drawWindow(graphics, partialTicks, mouseX, mouseY);
   }
}
