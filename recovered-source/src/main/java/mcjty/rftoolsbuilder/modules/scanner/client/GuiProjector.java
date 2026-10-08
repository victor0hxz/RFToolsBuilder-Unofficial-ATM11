package mcjty.rftoolsbuilder.modules.scanner.client;

import javax.annotation.Nonnull;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.layout.HorizontalAlignment;
import mcjty.lib.gui.layout.PositionalLayout;
import mcjty.lib.gui.widgets.ChoiceLabel;
import mcjty.lib.gui.widgets.Label;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.TextField;
import mcjty.lib.gui.widgets.ToggleButton;
import mcjty.lib.gui.widgets.Widget;
import mcjty.lib.gui.widgets.Widgets;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.typed.TypedMap.Builder;
import mcjty.rftoolsbuilder.modules.scanner.ProjectorOpcode;
import mcjty.rftoolsbuilder.modules.scanner.ProjectorOperation;
import mcjty.rftoolsbuilder.modules.scanner.ScannerModule;
import mcjty.rftoolsbuilder.modules.scanner.blocks.ProjectorBlock;
import mcjty.rftoolsbuilder.modules.scanner.blocks.ProjectorTileEntity;
import mcjty.rftoolsbuilder.shapes.IShapeParentGui;
import mcjty.rftoolsbuilder.shapes.ShapeGuiTools;
import mcjty.rftoolsbuilder.shapes.ShapeRenderer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public class GuiProjector extends GenericGuiContainer<ProjectorTileEntity, GenericContainer> implements IShapeParentGui {
   private static final int SIDEWIDTH = 80;
   private static final int PROJECTOR_WIDTH = 256;
   private static final int PROJECTOR_HEIGHT = 238;
   private static final int PREVIEW_BUTTON_WIDTH = 39;
   private static final Identifier SIDE_BACKGROUND = Identifier.fromNamespaceAndPath("rftoolsbuilder", "textures/gui/sidegui_projector.png");
   private static final Identifier MAIN_BACKGROUND = Identifier.fromNamespaceAndPath("rftoolsbuilder", "textures/gui/projector.png");
   private final ChoiceLabel[] rsLabelOn = new ChoiceLabel[4];
   private final ChoiceLabel[] rsLabelOff = new ChoiceLabel[4];
   private final TextField[] valOn = new TextField[4];
   private final TextField[] valOff = new TextField[4];
   private ShapeRenderer shapeRenderer;
   private ToggleButton showAxis;
   private ToggleButton showOuter;
   private ToggleButton showScan;
   private TextField angle;
   private TextField offset;
   private TextField scale;
   private ToggleButton autoRotate;
   private ToggleButton scanline;
   private ToggleButton sound;
   private ToggleButton grayScale;
   private ToggleButton renderModels;
   private final ProjectorTileEntity tileEntity;

   public GuiProjector(ProjectorTileEntity tileEntity, GenericContainer container, Inventory inventory) {
      super(container, inventory, Component.literal("Projector"), ((ProjectorBlock)ScannerModule.PROJECTOR.block().get()).getManualEntry(), 336, 238);
      this.tileEntity = tileEntity;
   }

   public static void register(RegisterMenuScreensEvent event) {
      register(event, ScannerModule.CONTAINER_PROJECTOR.get(), GuiProjector::new);
   }

   private ShapeRenderer getShapeRenderer() {
      if (this.shapeRenderer != null && this.tileEntity.getShapeID().equals(this.shapeRenderer.getShapeID())) {
         this.shapeRenderer.setShapeID(this.tileEntity.getShapeID());
      } else {
         this.shapeRenderer = new ShapeRenderer(this.tileEntity.getShapeID());
         this.shapeRenderer.initView(this.getPreviewLeft(), this.topPos + 100);
      }

      return this.shapeRenderer;
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
      this.angle = this.createField(8, 30, 22, String.valueOf(this.tileEntity.getAngleInt()), this::updateSettings);
      this.offset = this.createField(8, 62, 22, String.valueOf(this.tileEntity.getOffsetInt()), this::updateSettings);
      this.scale = this.createField(8, 94, 22, String.valueOf(this.tileEntity.getScaleInt()), this::updateSettings);
      this.autoRotate = ((ToggleButton)((ToggleButton)((ToggleButton)new ToggleButton().checkMarker(true).text("Auto"))
               .tooltips(new String[]{"Automatic client-side rotation"}))
            .hint(2, 128, 39, 16))
         .event(this::updateSettings);
      this.autoRotate.pressed(this.tileEntity.isAutoRotate());
      this.scanline = ((ToggleButton)((ToggleButton)((ToggleButton)new ToggleButton().checkMarker(true).text("SL"))
               .tooltips(new String[]{"Enable visual scanlines when the scan refreshes"}))
            .hint(42, 128, 39, 16))
         .event(this::updateSettings);
      this.scanline.pressed(this.tileEntity.isScanline());
      this.sound = ((ToggleButton)((ToggleButton)((ToggleButton)new ToggleButton().checkMarker(true).text("Snd"))
               .tooltips(new String[]{"Enable sound during visual scan"}))
            .hint(2, 146, 39, 16))
         .event(this::updateSettings);
      this.sound.pressed(this.tileEntity.isSound());
      this.grayScale = ((ToggleButton)((ToggleButton)((ToggleButton)new ToggleButton().checkMarker(true).text("Gray"))
               .tooltips(new String[]{"Enable grayscale mode"}))
            .hint(42, 146, 39, 16))
         .event(this::updateSettings);
      this.grayScale.pressed(this.tileEntity.isGrayscale());
      this.renderModels = ((ToggleButton)((ToggleButton)((ToggleButton)new ToggleButton().checkMarker(true).text("M"))
               .tooltips(new String[]{"Render baked block models in the world", "instead of solid projected cubes"}))
            .hint(42, 182, 39, 16))
         .event(this::updateSettings);
      this.renderModels.pressed(this.tileEntity.isRenderBlockModels());
      panel.children(
         new Widget[]{
            ((Label)Widgets.label("Angle").horizontalAlignment(HorizontalAlignment.ALIGN_LEFT)).hint(34, 30, 32, 14),
            ((Label)Widgets.label("Offset").horizontalAlignment(HorizontalAlignment.ALIGN_LEFT)).hint(34, 62, 32, 14),
            ((Label)Widgets.label("Scale").horizontalAlignment(HorizontalAlignment.ALIGN_LEFT)).hint(34, 94, 32, 14),
            this.angle,
            this.offset,
            this.scale,
            this.autoRotate,
            this.scanline,
            this.sound,
            this.grayScale,
            this.renderModels
         }
      );
      this.showAxis = ShapeGuiTools.createAxisButton(this, panel, 2, 164);
      this.showOuter = ShapeGuiTools.createBoxButton(this, panel, 42, 164);
      this.showScan = ShapeGuiTools.createScanButton(this, panel, 2, 182);
   }

   private void initSidePanel(Panel panel) {
      this.initRsPanel(panel, 0, "S");
      this.initRsPanel(panel, 1, "N");
      this.initRsPanel(panel, 2, "E");
      this.initRsPanel(panel, 3, "W");
   }

   private void initRsPanel(Panel root, int index, String labelText) {
      int dy = index * 53;
      root.children(new Widget[]{((Label)Widgets.label(labelText).horizontalAlignment(HorizontalAlignment.ALIGN_LEFT)).hint(8, dy + 8, 12, 13)});
      this.rsLabelOn[index] = ((ChoiceLabel)new ChoiceLabel().hint(8, dy + 26, 32, 14)).choices(ProjectorOpcode.getChoices()).event(choice -> this.updateRs());
      this.rsLabelOff[index] = ((ChoiceLabel)new ChoiceLabel().hint(42, dy + 26, 32, 14))
         .choices(ProjectorOpcode.getChoices())
         .event(choice -> this.updateRs());

      for (ProjectorOpcode opcode : ProjectorOpcode.values()) {
         this.rsLabelOn[index].choiceTooltip(opcode.getCode(), new String[]{opcode.getDescription()});
         this.rsLabelOff[index].choiceTooltip(opcode.getCode(), new String[]{opcode.getDescription()});
      }

      this.valOn[index] = this.createField(8, dy + 41, 32, "", this::updateRs);
      this.valOff[index] = this.createField(42, dy + 41, 32, "", this::updateRs);
      ProjectorOperation op = this.tileEntity.getOperations()[index];
      this.rsLabelOn[index].choice(op.getOpcodeOn().getCode());
      this.rsLabelOff[index].choice(op.getOpcodeOff().getCode());
      this.valOn[index].text(op.getValueOn() == null ? "" : op.getValueOn().toString());
      this.valOff[index].text(op.getValueOff() == null ? "" : op.getValueOff().toString());
      root.children(new Widget[]{this.rsLabelOn[index], this.rsLabelOff[index], this.valOn[index], this.valOff[index]});
   }

   private TextField createField(int x, int y, int w, String value, Runnable runnable) {
      return ((TextField)new TextField().hint(x, y, w, 14)).text(value).event(newText -> runnable.run());
   }

   private int parse(TextField field, int defaultValue) {
      try {
         return Integer.parseInt(field.getText());
      } catch (NumberFormatException var4) {
         return defaultValue;
      }
   }

   private Double parseDouble(TextField field) {
      String text = field.getText().trim();
      if (text.isEmpty()) {
         return null;
      } else {
         try {
            return Double.parseDouble(text);
         } catch (NumberFormatException var4) {
            return null;
         }
      }
   }

   private void updateSettings() {
      this.sendServerCommandTyped(
         "projector.settings",
         TypedMap.builder()
            .put(ProjectorTileEntity.PARAM_SCALE, this.parse(this.scale, this.tileEntity.getScaleInt()))
            .put(ProjectorTileEntity.PARAM_OFFSET, this.parse(this.offset, this.tileEntity.getOffsetInt()))
            .put(ProjectorTileEntity.PARAM_ANGLE, this.parse(this.angle, this.tileEntity.getAngleInt()))
            .put(ProjectorTileEntity.PARAM_AUTO, this.autoRotate.isPressed())
            .put(ProjectorTileEntity.PARAM_SCAN, this.scanline.isPressed())
            .put(ProjectorTileEntity.PARAM_SOUND, this.sound.isPressed())
            .put(ProjectorTileEntity.PARAM_GRAY, this.grayScale.isPressed())
            .put(ProjectorTileEntity.PARAM_RENDERMODELS, this.renderModels.isPressed())
            .build()
      );
   }

   private void updateRs() {
      Builder builder = TypedMap.builder();

      for (int i = 0; i < 4; i++) {
         builder.put(ProjectorTileEntity.PARAM_OPON.get(i), this.rsLabelOn[i].getCurrentChoice());
         builder.put(ProjectorTileEntity.PARAM_OPOFF.get(i), this.rsLabelOff[i].getCurrentChoice());
         Double on = this.parseDouble(this.valOn[i]);
         Double off = this.parseDouble(this.valOff[i]);
         if (on != null) {
            builder.put(ProjectorTileEntity.PARAM_VALON.get(i), on);
         }

         if (off != null) {
            builder.put(ProjectorTileEntity.PARAM_VALOFF.get(i), off);
         }
      }

      this.sendServerCommandTyped("projector.rsSettings", builder.build());
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
      for (int i = 0; i < 4; i++) {
         ProjectorOperation op = this.tileEntity.getOperations()[i];
         this.valOn[i].enabled(op.getOpcodeOn().isNeedsValue());
         this.valOff[i].enabled(op.getOpcodeOff().isNeedsValue());
      }

      this.sound.enabled(this.scanline.isPressed());
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
