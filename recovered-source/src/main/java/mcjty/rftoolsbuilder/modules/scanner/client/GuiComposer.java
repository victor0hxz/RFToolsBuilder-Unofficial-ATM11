package mcjty.rftoolsbuilder.modules.scanner.client;

import javax.annotation.Nonnull;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.layout.HorizontalAlignment;
import mcjty.lib.gui.layout.PositionalLayout;
import mcjty.lib.gui.widgets.Button;
import mcjty.lib.gui.widgets.ChoiceLabel;
import mcjty.lib.gui.widgets.Label;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.ToggleButton;
import mcjty.lib.gui.widgets.Widget;
import mcjty.lib.gui.widgets.Widgets;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.typed.TypedMap.Builder;
import mcjty.rftoolsbuilder.modules.builder.client.GuiShapeCard;
import mcjty.rftoolsbuilder.modules.builder.items.ShapeCardItem;
import mcjty.rftoolsbuilder.modules.builder.network.PacketCloseContainerAndOpenCardGui;
import mcjty.rftoolsbuilder.modules.scanner.ScannerModule;
import mcjty.rftoolsbuilder.modules.scanner.blocks.ComposerBlock;
import mcjty.rftoolsbuilder.modules.scanner.blocks.ComposerTileEntity;
import mcjty.rftoolsbuilder.setup.RFToolsBuilderMessages;
import mcjty.rftoolsbuilder.shapes.IShapeParentGui;
import mcjty.rftoolsbuilder.shapes.ShapeGuiTools;
import mcjty.rftoolsbuilder.shapes.ShapeID;
import mcjty.rftoolsbuilder.shapes.ShapeModifier;
import mcjty.rftoolsbuilder.shapes.ShapeOperation;
import mcjty.rftoolsbuilder.shapes.ShapeRenderer;
import mcjty.rftoolsbuilder.shapes.ShapeRotation;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public class GuiComposer extends GenericGuiContainer<ComposerTileEntity, GenericContainer> implements IShapeParentGui {
   private static final int SIDEWIDTH = 80;
   private static final int SHAPER_WIDTH = 256;
   private static final int SHAPER_HEIGHT = 238;
   private static final Identifier SIDE_BACKGROUND = Identifier.fromNamespaceAndPath("rftoolsbuilder", "textures/gui/sidegui_projector.png");
   private static final Identifier MAIN_BACKGROUND = Identifier.fromNamespaceAndPath("rftoolsbuilder", "textures/gui/composer.png");
   private final ChoiceLabel[] operationLabels = new ChoiceLabel[9];
   private final ChoiceLabel[] rotationLabels = new ChoiceLabel[9];
   private final ToggleButton[] flipButtons = new ToggleButton[9];
   private ShapeRenderer shapeRenderer;
   private ToggleButton showAxis;
   private ToggleButton showOuter;
   private ToggleButton showScan;
   private final ComposerTileEntity tileEntity;

   public GuiComposer(ComposerTileEntity tileEntity, GenericContainer container, Inventory inventory) {
      super(container, inventory, Component.literal("Composer"), ((ComposerBlock)ScannerModule.COMPOSER.block().get()).getManualEntry(), 336, 238);
      this.tileEntity = tileEntity;
   }

   public static void register(RegisterMenuScreensEvent event) {
      register(event, ScannerModule.CONTAINER_COMPOSER.get(), GuiComposer::new);
   }

   public void init() {
      super.init();
      Panel toplevel = new Panel().layout(new PositionalLayout());
      Panel sidePanel = (Panel)new Panel().layout(new PositionalLayout()).background(SIDE_BACKGROUND);
      sidePanel.bounds(0, 0, 80, this.imageHeight);
      this.initSidePanel(sidePanel);
      Panel mainPanel = (Panel)new Panel().layout(new PositionalLayout()).background(MAIN_BACKGROUND);
      mainPanel.bounds(80, 0, 256, this.imageHeight);
      this.initMainPanel(mainPanel);
      toplevel.children(new Widget[]{sidePanel, mainPanel});
      toplevel.bounds(this.leftPos, this.topPos, this.imageWidth, this.imageHeight);
      this.window = new Window(this, toplevel);
   }

   private void initMainPanel(Panel panel) {
      ShapeModifier[] modifiers = this.tileEntity.getModifiers();

      for (int i = 0; i < 9; i++) {
         int y = 7 + i * 18;
         int index = i;
         Button config = ((Button)((Button)((Button)new Button().text("?")).tooltips(new String[]{"Open the shape card editor"})).hint(3, y + 2, 13, 12))
            .event(() -> this.openCardGui(index));
         ChoiceLabel op = ((ChoiceLabel)new ChoiceLabel().hint(55, y, 26, 14))
            .choices(new String[]{ShapeOperation.UNION.getCode(), ShapeOperation.SUBTRACT.getCode(), ShapeOperation.INTERSECT.getCode()})
            .event(choice -> this.updateSettings());

         for (ShapeOperation operation : ShapeOperation.values()) {
            op.choiceTooltip(operation.getCode(), new String[]{operation.getDescription()});
         }

         op.choice(modifiers[i].getOperation().getCode());
         this.operationLabels[i] = op;
         panel.children(new Widget[]{config, op});
      }

      this.operationLabels[0].enabled(false);
      Button outConfig = ((Button)((Button)((Button)new Button().text("?")).tooltips(new String[]{"Open the shape card editor"})).hint(3, 202, 13, 12))
         .event(() -> this.openCardGui(-1));
      this.showAxis = ShapeGuiTools.createAxisButton(this, panel, 5, 176);
      this.showOuter = ShapeGuiTools.createBoxButton(this, panel, 31, 176);
      this.showScan = ShapeGuiTools.createScanButton(this, panel, 57, 176);
      panel.children(new Widget[]{outConfig, this.showAxis, this.showOuter, this.showScan});
   }

   private void initSidePanel(Panel panel) {
      String[] help = new String[]{
         "Drag left mouse button to rotate", "Shift drag left mouse to pan", "Use mouse wheel to zoom in/out", "Use middle click to reset rotation"
      };
      panel.children(
         new Widget[]{
            ((Label)((Label)((Label)Widgets.label("E").horizontalAlignment(HorizontalAlignment.ALIGN_LEFT)).color(-65536)).tooltips(help))
               .hint(10, 170, 15, 15),
            ((Label)((Label)((Label)Widgets.label("W").horizontalAlignment(HorizontalAlignment.ALIGN_LEFT)).color(-65536)).tooltips(help))
               .hint(50, 170, 15, 15),
            ((Label)((Label)((Label)Widgets.label("U").horizontalAlignment(HorizontalAlignment.ALIGN_LEFT)).color(-16729344)).tooltips(help))
               .hint(10, 185, 15, 15),
            ((Label)((Label)((Label)Widgets.label("D").horizontalAlignment(HorizontalAlignment.ALIGN_LEFT)).color(-16729344)).tooltips(help))
               .hint(50, 185, 15, 15),
            ((Label)((Label)((Label)Widgets.label("N").horizontalAlignment(HorizontalAlignment.ALIGN_LEFT)).color(-16776961)).tooltips(help))
               .hint(10, 200, 15, 15),
            ((Label)((Label)((Label)Widgets.label("S").horizontalAlignment(HorizontalAlignment.ALIGN_LEFT)).color(-16776961)).tooltips(help))
               .hint(50, 200, 15, 15)
         }
      );
      ShapeModifier[] modifiers = this.tileEntity.getModifiers();

      for (int i = 0; i < 9; i++) {
         int y = 9 + i * 17 + i / 3 * 2;
         ToggleButton flip = ((ToggleButton)((ToggleButton)new ToggleButton().checkMarker(true).text("Flip")).hint(9, y, 35, 14)).event(this::updateSettings);
         flip.pressed(modifiers[i].isFlipY());
         this.flipButtons[i] = flip;
         ChoiceLabel rotation = ((ChoiceLabel)new ChoiceLabel().hint(46, y, 28, 14))
            .choices(new String[]{ShapeRotation.NONE.getCode(), ShapeRotation.X.getCode(), ShapeRotation.Y.getCode(), ShapeRotation.Z.getCode()})
            .event(choice -> this.updateSettings());
         rotation.choice(modifiers[i].getRotation().getCode());
         this.rotationLabels[i] = rotation;
         panel.children(new Widget[]{flip, rotation});
      }
   }

   private ShapeRenderer getShapeRenderer() {
      ItemStack stack = ((GenericContainer)this.menu).getSlot(0).getItem();
      ShapeID shapeID = new ShapeID(Level.OVERWORLD, null, ShapeCardItem.getScanIdRecursive(stack), false, ShapeCardItem.isSolid(stack));
      if (this.shapeRenderer != null && shapeID.equals(this.shapeRenderer.getShapeID())) {
         this.shapeRenderer.setShapeID(shapeID);
      } else {
         this.shapeRenderer = new ShapeRenderer(shapeID);
         this.shapeRenderer.initView(this.getPreviewLeft(), this.topPos + 100);
      }

      return this.shapeRenderer;
   }

   private void openCardGui(int index) {
      int slot = index == -1 ? 0 : 1 + index;
      ItemStack cardStack = ((GenericContainer)this.menu).getSlot(slot).getItem();
      if (!cardStack.isEmpty()) {
         GuiShapeCard.fromTEPos = this.tileEntity.getBlockPos();
         GuiShapeCard.fromTEStackSlot = slot;
         RFToolsBuilderMessages.sendToServer(PacketCloseContainerAndOpenCardGui.create(this.tileEntity.getBlockPos()));
      }
   }

   private void updateSettings() {
      Builder builder = TypedMap.builder();

      for (int i = 0; i < 9; i++) {
         builder.put(ComposerTileEntity.PARAM_OPS.get(i), this.operationLabels[i].getCurrentChoice());
         builder.put(ComposerTileEntity.PARAM_FLIPS.get(i), this.flipButtons[i].isPressed());
         builder.put(ComposerTileEntity.PARAM_ROTS.get(i), this.rotationLabels[i].getCurrentChoice());
      }

      this.sendServerCommandTyped("composer.settings", builder.build());
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
      return this.leftPos + 80;
   }

   @Override
   public int getPreviewTop() {
      return this.topPos;
   }

   public void extractBackground(@Nonnull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
      this.drawWindow(graphics, partialTicks, mouseX, mouseY);
      ItemStack stack = ((GenericContainer)this.menu).getSlot(0).getItem();
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
               true
            );
      }
   }
}
