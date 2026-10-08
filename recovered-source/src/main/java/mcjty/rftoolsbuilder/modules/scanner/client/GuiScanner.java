package mcjty.rftoolsbuilder.modules.scanner.client;

import javax.annotation.Nonnull;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.layout.HorizontalAlignment;
import mcjty.lib.gui.layout.PositionalLayout;
import mcjty.lib.gui.widgets.Button;
import mcjty.lib.gui.widgets.Label;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.ToggleButton;
import mcjty.lib.gui.widgets.Widget;
import mcjty.lib.gui.widgets.Widgets;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.BlockPosTools;
import mcjty.rftoolsbuilder.modules.scanner.ScannerModule;
import mcjty.rftoolsbuilder.modules.scanner.blocks.ScannerBlock;
import mcjty.rftoolsbuilder.modules.scanner.blocks.ScannerTileEntity;
import mcjty.rftoolsbuilder.shapes.IShapeParentGui;
import mcjty.rftoolsbuilder.shapes.ShapeGuiTools;
import mcjty.rftoolsbuilder.shapes.ShapeID;
import mcjty.rftoolsbuilder.shapes.ShapeRenderer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public class GuiScanner extends GenericGuiContainer<ScannerTileEntity, GenericContainer> implements IShapeParentGui {
   private static final int SCANNER_WIDTH = 256;
   private static final int SCANNER_HEIGHT = 238;
   private static final Identifier BACKGROUND = Identifier.fromNamespaceAndPath("rftoolsbuilder", "textures/gui/scanner.png");
   private ShapeRenderer shapeRenderer;
   private ToggleButton showAxis;
   private ToggleButton showOuter;
   private ToggleButton showScan;
   private Label offsetLabel;
   private Label dimensionLabel;
   private Label progressLabel;
   private final ScannerTileEntity tileEntity;

   public GuiScanner(ScannerTileEntity tileEntity, GenericContainer container, Inventory inventory) {
      super(container, inventory, Component.literal("Scanner"), ((ScannerBlock)ScannerModule.SCANNER.block().get()).getManualEntry(), 256, 238);
      this.tileEntity = tileEntity;
   }

   public static void register(RegisterMenuScreensEvent event) {
      register(event, ScannerModule.CONTAINER_SCANNER.get(), GuiScanner::new);
   }

   public void init() {
      super.init();
      Panel root = (Panel)new Panel().layout(new PositionalLayout()).background(BACKGROUND);
      root.bounds(this.leftPos, this.topPos, this.imageWidth, this.imageHeight);
      this.showAxis = ShapeGuiTools.createAxisButton(this, root, 5, 176);
      this.showOuter = ShapeGuiTools.createBoxButton(this, root, 31, 176);
      this.showScan = ShapeGuiTools.createScanButton(this, root, 57, 176);
      Button scanButton = ((Button)((Button)new Button().text("Scan")).hint(5, 156, 40, 16))
         .event(() -> this.sendServerCommandTyped("scanner.scan", TypedMap.EMPTY));
      root.children(
         new Widget[]{
            scanButton,
            ((Button)((Button)new Button().text("W")).hint(4, 30, 16, 15)).event(() -> this.move(-16, 0, 0)),
            ((Button)((Button)new Button().text("w")).hint(20, 30, 16, 15)).event(() -> this.move(-1, 0, 0)),
            ((Button)((Button)new Button().text("e")).hint(45, 30, 16, 15)).event(() -> this.move(1, 0, 0)),
            ((Button)((Button)new Button().text("E")).hint(61, 30, 16, 15)).event(() -> this.move(16, 0, 0)),
            ((Button)((Button)new Button().text("S")).hint(4, 50, 16, 15)).event(() -> this.move(0, 0, -16)),
            ((Button)((Button)new Button().text("s")).hint(20, 50, 16, 15)).event(() -> this.move(0, 0, -1)),
            ((Button)((Button)new Button().text("n")).hint(45, 50, 16, 15)).event(() -> this.move(0, 0, 1)),
            ((Button)((Button)new Button().text("N")).hint(61, 50, 16, 15)).event(() -> this.move(0, 0, 16)),
            ((Button)((Button)new Button().text("D")).hint(4, 70, 16, 15)).event(() -> this.move(0, -16, 0)),
            ((Button)((Button)new Button().text("d")).hint(20, 70, 16, 15)).event(() -> this.move(0, -1, 0)),
            ((Button)((Button)new Button().text("u")).hint(45, 70, 16, 15)).event(() -> this.move(0, 1, 0)),
            ((Button)((Button)new Button().text("U")).hint(61, 70, 16, 15)).event(() -> this.move(0, 16, 0))
         }
      );
      this.offsetLabel = (Label)((Label)Widgets.label("").horizontalAlignment(HorizontalAlignment.ALIGN_LEFT)).hint(4, 90, 80, 14);
      this.dimensionLabel = (Label)((Label)Widgets.label("").horizontalAlignment(HorizontalAlignment.ALIGN_LEFT)).hint(4, 105, 80, 14);
      this.progressLabel = (Label)((Label)Widgets.label("").horizontalAlignment(HorizontalAlignment.ALIGN_LEFT)).hint(4, 135, 80, 14);
      root.children(new Widget[]{this.offsetLabel, this.dimensionLabel, this.progressLabel});
      this.window = new Window(this, root);
   }

   private void move(int x, int y, int z) {
      this.sendServerCommandTyped(
         "scanner.offset", TypedMap.builder().put(ScannerTileEntity.PARAM_OFFSET, this.tileEntity.getDataOffset().offset(x, y, z)).build()
      );
   }

   private ShapeRenderer getShapeRenderer() {
      ShapeID id = new ShapeID(Level.OVERWORLD, null, this.tileEntity.getScanId(), false, true);
      if (this.shapeRenderer == null || !id.equals(this.shapeRenderer.getShapeID())) {
         this.shapeRenderer = new ShapeRenderer(id);
         this.shapeRenderer.initView(this.getPreviewLeft(), this.topPos + 100);
      }

      return this.shapeRenderer;
   }

   public boolean mouseDragged(MouseButtonEvent event, double scaledX, double scaledY) {
      boolean result = super.mouseDragged(event, scaledX, scaledY);
      boolean[] buttons = new boolean[3];
      int button = event.button();
      if (button >= 0 && button < buttons.length) {
         buttons[button] = true;
      }

      this.getShapeRenderer().handleShapeDragging((int)event.x() - this.getPreviewLeft(), (int)event.y() - this.topPos, buttons);
      return result;
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
      boolean result = super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
      this.getShapeRenderer().handleMouseWheel(scrollX, scrollY);
      return result;
   }

   @Override
   public int getPreviewLeft() {
      return this.leftPos;
   }

   @Override
   public int getPreviewTop() {
      return this.topPos;
   }

   public void extractBackground(@Nonnull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
      this.offsetLabel.text("Off: " + BlockPosTools.toString(this.tileEntity.getDataOffset()));
      this.dimensionLabel.text("Dim: " + BlockPosTools.toString(this.tileEntity.getDataDim()));
      int progress = this.tileEntity.getScanProgress();
      this.progressLabel.text(progress >= 0 ? progress + "%" : "");
      this.drawWindow(graphics, partialTicks, mouseX, mouseY);
      ItemStack stack = this.tileEntity.getRenderStack();
      if (!stack.isEmpty()) {
         this.getShapeRenderer()
            .renderShape(
               graphics,
               this,
               stack,
               this.getPreviewLeft(),
               this.getPreviewTop(),
               this.showAxis.isPressed(),
               this.showOuter.isPressed(),
               this.showScan.isPressed(),
               false
            );
      }
   }
}
