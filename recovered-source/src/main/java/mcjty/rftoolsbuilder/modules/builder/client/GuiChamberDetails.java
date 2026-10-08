package mcjty.rftoolsbuilder.modules.builder.client;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import mcjty.lib.base.StyleConfig;
import mcjty.lib.client.RenderHelper;
import mcjty.lib.gui.GuiItemScreen;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.layout.HorizontalAlignment;
import mcjty.lib.gui.widgets.BlockRender;
import mcjty.lib.gui.widgets.Label;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.Slider;
import mcjty.lib.gui.widgets.Widget;
import mcjty.lib.gui.widgets.WidgetList;
import mcjty.lib.gui.widgets.Widgets;
import mcjty.rftoolsbase.tools.ManualHelper;
import mcjty.rftoolsbuilder.compat.CompatNbt;
import mcjty.rftoolsbuilder.setup.RFToolsBuilderMessages;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class GuiChamberDetails extends GuiItemScreen {
   private static final int CHAMBER_XSIZE = 390;
   private static final int CHAMBER_YSIZE = 210;
   private static Map<BlockState, Integer> items = null;
   private static Map<BlockState, Integer> costs = null;
   private static Map<BlockState, ItemStack> stacks = null;
   private static Map<String, Integer> entities = null;
   private static Map<String, Integer> entityCosts = null;
   private static Map<String, CompoundTag> realEntities = null;
   private static Map<String, String> playerNames = null;
   private WidgetList blockList;
   private Label infoLabel;
   private Label info2Label;

   public GuiChamberDetails() {
      super(390, 210, ManualHelper.create("rftoolsbuilder:builder/chamber_details"));
      this.requestChamberInfoFromServer();
   }

   public static void setItemsWithCount(
      Map<BlockState, Integer> items,
      Map<BlockState, Integer> costs,
      Map<BlockState, ItemStack> stacks,
      Map<String, Integer> entities,
      Map<String, Integer> entityCosts,
      Map<String, CompoundTag> realEntities,
      Map<String, String> playerNames
   ) {
      GuiChamberDetails.items = new HashMap<>(items);
      GuiChamberDetails.costs = new HashMap<>(costs);
      GuiChamberDetails.stacks = new HashMap<>(stacks);
      GuiChamberDetails.entities = new HashMap<>(entities);
      GuiChamberDetails.entityCosts = new HashMap<>(entityCosts);
      GuiChamberDetails.realEntities = new HashMap<>(realEntities);
      GuiChamberDetails.playerNames = new HashMap<>(playerNames);
   }

   private void requestChamberInfoFromServer() {
      RFToolsBuilderMessages.sendToServer("getChamberInfo");
   }

   public void init() {
      super.init();
      this.blockList = (WidgetList)new WidgetList().name("blocks");
      Slider listSlider = ((Slider)new Slider().desiredWidth(10)).vertical().scrollableName("blocks");
      Panel listPanel = (Panel)Widgets.horizontal(3, 1).children(new Widget[]{this.blockList, listSlider});
      this.infoLabel = (Label)new Label().horizontalAlignment(HorizontalAlignment.ALIGN_LEFT);
      ((Label)this.infoLabel.desiredWidth(380)).desiredHeight(14);
      this.info2Label = (Label)new Label().horizontalAlignment(HorizontalAlignment.ALIGN_LEFT);
      ((Label)this.info2Label.desiredWidth(380)).desiredHeight(14);
      Panel toplevel = (Panel)((Panel)Widgets.vertical(3, 1).filledRectThickness(2)).children(new Widget[]{listPanel, this.infoLabel, this.info2Label});
      toplevel.bounds(this.guiLeft, this.guiTop, this.xSize, this.ySize);
      this.window = new Window(this, toplevel);
   }

   private void populateLists() {
      this.blockList.removeChildren();
      if (items != null) {
         int totalCost = 0;

         for (Entry<BlockState, Integer> entry : items.entrySet()) {
            BlockState bm = entry.getKey();
            int count = entry.getValue();
            int cost = costs.get(bm);
            Panel panel = (Panel)Widgets.horizontal().desiredHeight(16);
            ItemStack stack;
            if (stacks.containsKey(bm)) {
               stack = stacks.get(bm);
            } else {
               stack = bm.getBlock().getCloneItemStack(Minecraft.getInstance().level, BlockPos.ZERO, bm, true, Minecraft.getInstance().player);
               if (stack.isEmpty()) {
                  stack = new ItemStack(bm.getBlock(), 0);
               }
            }

            BlockRender blockRender = new BlockRender().renderItem(stack).offsetX(-1).offsetY(-1);
            Label nameLabel = (Label)((Label)new Label().horizontalAlignment(HorizontalAlignment.ALIGN_LEFT)).color(StyleConfig.colorTextInListNormal);
            stack.getItem();
            ((Label)nameLabel.text(stack.getHoverName().getString())).desiredWidth(160);
            Label countLabel = (Label)Widgets.label(String.valueOf(count)).color(StyleConfig.colorTextInListNormal);
            ((Label)countLabel.horizontalAlignment(HorizontalAlignment.ALIGN_LEFT)).desiredWidth(50);
            Label costLabel = (Label)new Label().color(StyleConfig.colorTextInListNormal);
            costLabel.horizontalAlignment(HorizontalAlignment.ALIGN_LEFT);
            if (cost == -1) {
               costLabel.text("NOT MOVABLE!");
            } else {
               costLabel.text("Move Cost " + cost + " RF");
               totalCost += cost;
            }

            panel.children(new Widget[]{blockRender, nameLabel, countLabel, costLabel});
            this.blockList.children(new Widget[]{panel});
         }

         int totalCostEntities = 0;
         RenderHelper.rot += 0.5F;

         for (Entry<String, Integer> entry : entities.entrySet()) {
            String id = entry.getKey();
            int countx = entry.getValue();
            int costx = entityCosts.get(id);
            Panel panelx = (Panel)Widgets.horizontal().desiredHeight(16);
            String entityName = "<?>";
            Entity entity = null;
            if (realEntities.containsKey(id)) {
               CompoundTag tag = realEntities.get(id);
               EntityType<?> value = (EntityType<?>)BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.parse(id));
               entity = value.create(this.minecraft.level, EntitySpawnReason.LOAD);
               entity.load(CompatNbt.valueInput(tag, this.minecraft.level.registryAccess()));
               entityName = entity.getDisplayName().getString();
               if (entity instanceof ItemEntity entityItem && !entityItem.getItem().isEmpty()) {
                  String displayName = entityItem.getItem().getDisplayName().getString();
                  entityName = entityName + " (" + displayName + ")";
               }
            } else {
               EntityType<?> value = (EntityType<?>)BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.parse(id));
               entity = value.create(this.minecraft.level, EntitySpawnReason.LOAD);
               entityName = entity.getDisplayName().getString();
            }

            if (playerNames.containsKey(id)) {
               entityName = playerNames.get(id);
            }

            BlockRender blockRender = new BlockRender().renderItem(entity).offsetX(-1).offsetY(-1);
            Label nameLabel = (Label)((Label)Widgets.label(entityName).horizontalAlignment(HorizontalAlignment.ALIGN_LEFT)).desiredWidth(160);
            Label countLabel = Widgets.label(String.valueOf(countx));
            ((Label)countLabel.horizontalAlignment(HorizontalAlignment.ALIGN_LEFT)).desiredWidth(50);
            Label costLabel = new Label();
            costLabel.horizontalAlignment(HorizontalAlignment.ALIGN_LEFT);
            if (costx == -1) {
               costLabel.text("NOT MOVABLE!");
            } else {
               costLabel.text("Move Cost " + costx + " RF");
               totalCostEntities += costx;
            }

            panelx.children(new Widget[]{blockRender, nameLabel, countLabel, costLabel});
            this.blockList.children(new Widget[]{panelx});
         }

         this.infoLabel.text("Total cost blocks: " + totalCost + " RF");
         this.info2Label.text("Total cost entities: " + totalCostEntities + " RF");
      }
   }

   protected void renderInternal(GuiGraphicsExtractor graphics, int pMouseX, int pMouseY, float partialTick) {
      this.populateLists();
      this.drawWindow(graphics, pMouseX, pMouseY, partialTick);
   }

   public static void open() {
      Minecraft.getInstance().setScreen(new GuiChamberDetails());
   }
}
