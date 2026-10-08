package mcjty.rftoolsbuilder.modules.shield.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import mcjty.lib.base.StyleConfig;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.events.DefaultSelectionEvent;
import mcjty.lib.gui.layout.HorizontalAlignment;
import mcjty.lib.gui.widgets.Button;
import mcjty.lib.gui.widgets.ChoiceLabel;
import mcjty.lib.gui.widgets.ColorSelector;
import mcjty.lib.gui.widgets.EnergyBar;
import mcjty.lib.gui.widgets.ImageChoiceLabel;
import mcjty.lib.gui.widgets.Label;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.Slider;
import mcjty.lib.gui.widgets.TextField;
import mcjty.lib.gui.widgets.ToggleButton;
import mcjty.lib.gui.widgets.Widget;
import mcjty.lib.gui.widgets.WidgetList;
import mcjty.lib.gui.widgets.Widgets;
import mcjty.lib.network.Networking;
import mcjty.lib.network.PacketGetListFromServer;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.RedstoneMode;
import mcjty.rftoolsbuilder.modules.shield.DamageTypeMode;
import mcjty.rftoolsbuilder.modules.shield.ShieldConfiguration;
import mcjty.rftoolsbuilder.modules.shield.ShieldModule;
import mcjty.rftoolsbuilder.modules.shield.ShieldRenderingMode;
import mcjty.rftoolsbuilder.modules.shield.ShieldTexture;
import mcjty.rftoolsbuilder.modules.shield.blocks.ShieldProjectorBlock;
import mcjty.rftoolsbuilder.modules.shield.blocks.ShieldProjectorTileEntity;
import mcjty.rftoolsbuilder.modules.shield.filters.PlayerFilter;
import mcjty.rftoolsbuilder.modules.shield.filters.ShieldFilter;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public class GuiShield extends GenericGuiContainer<ShieldProjectorTileEntity, GenericContainer> {
   public static final int SHIELD_WIDTH = 256;
   public static final int SHIELD_HEIGHT = 224;
   public static final String ACTION_PASS = "Pass";
   public static final String ACTION_SOLID = "Solid";
   public static final String ACTION_DAMAGE = "Damage";
   public static final String ACTION_SOLIDDAMAGE = "SolDmg";
   public static final String DAMAGETYPE_GENERIC = DamageTypeMode.DAMAGETYPE_GENERIC.getDescription();
   public static final String DAMAGETYPE_PLAYER = DamageTypeMode.DAMAGETYPE_PLAYER.getDescription();
   private EnergyBar energyBar;
   private ChoiceLabel shieldTextures;
   private ChoiceLabel visibilityOptions;
   private ChoiceLabel actionOptions;
   private ChoiceLabel typeOptions;
   private ChoiceLabel damageType;
   private WidgetList filterList;
   private TextField player;
   private Button addFilter;
   private Button delFilter;
   private Button upFilter;
   private Button downFilter;
   private ColorSelector colorSelector;
   private List<ShieldFilter> filters = null;
   private int listDirty = 0;
   private static List<ShieldFilter> fromServer_filters = new ArrayList<>();
   private static final Identifier iconLocation = Identifier.fromNamespaceAndPath("rftoolsbuilder", "textures/gui/shieldprojector.png");
   private static final Identifier iconGuiElements = Identifier.fromNamespaceAndPath("rftoolsbase", "textures/gui/guielements.png");

   public static void storeFiltersForClient(List<ShieldFilter<?>> filters) {
      fromServer_filters = new ArrayList<>(filters);
   }

   public GuiShield(GenericContainer container, Inventory inventory, Component title) {
      super(container, inventory, title, ((ShieldProjectorBlock)ShieldModule.SHIELD_BLOCK1.block().get()).getManualEntry(), 256, 224);
   }

   public static void register(RegisterMenuScreensEvent event) {
      event.register(ShieldModule.CONTAINER_SHIELD.get(), GuiShield::new);
   }

   public void init() {
      super.init();
      this.energyBar = ((EnergyBar)new EnergyBar().vertical().hint(12, 141, 10, 76)).showText(false);
      this.initVisibilityMode();
      this.initShieldTextures();
      this.initActionOptions();
      this.initTypeOptions();
      ImageChoiceLabel redstoneMode = this.initRedstoneMode();
      this.initDamageType();
      this.filterList = ((WidgetList)((WidgetList)new WidgetList().name("filters")).desiredHeight(120)).event(new DefaultSelectionEvent() {
         {
            Objects.requireNonNull(GuiShield.this);
         }

         public void select(int index) {
            GuiShield.this.selectFilter();
         }
      });
      Slider filterSlider = (Slider)((Slider)new Slider().vertical().scrollableName("filters").desiredWidth(11)).desiredHeight(120);
      Panel filterPanel = (Panel)((Panel)((Panel)Widgets.horizontal(3, 1).hint(12, 10, 154, 124)).children(new Widget[]{this.filterList, filterSlider}))
         .filledBackground(-6381922);
      this.colorSelector = (ColorSelector)((ColorSelector)((ColorSelector)new ColorSelector().name("color")).tooltips(new String[]{"Color for the shield"}))
         .hint(25, 177, 30, 16);
      ToggleButton light = (ToggleButton)((ToggleButton)((ToggleButton)((ToggleButton)new ToggleButton().name("light")).checkMarker(true).text("L"))
            .tooltips(new String[]{"If pressed, light is blocked", "by the shield"}))
         .hint(56, 177, 23, 16);
      this.player = (TextField)Widgets.textfield(170, 44, 80, 14).tooltips(new String[]{"Optional player name"});
      this.addFilter = (Button)((Button)Widgets.button(4, 6, 36, 14, "Add").channel("addfilter")).tooltips(new String[]{"Add selected filter"});
      this.delFilter = (Button)((Button)Widgets.button(39, 6, 36, 14, "Del").channel("delfilter")).tooltips(new String[]{"Delete selected filter"});
      this.upFilter = (Button)((Button)Widgets.button(4, 22, 36, 14, "Up").channel("upfilter")).tooltips(new String[]{"Move filter up"});
      this.downFilter = (Button)((Button)Widgets.button(39, 22, 36, 14, "Down").channel("downfilter")).tooltips(new String[]{"Move filter down"});
      Panel controlPanel = (Panel)((Panel)((Panel)((Panel)Widgets.positional().hint(170, 58, 80, 43))
               .children(new Widget[]{this.addFilter, this.delFilter, this.upFilter, this.downFilter}))
            .filledRectThickness(-2))
         .filledBackground(StyleConfig.colorListBackground);
      Label lootingBonus = (Label)Widgets.label(160, 118, 60, 18, "Looting:").horizontalAlignment(HorizontalAlignment.ALIGN_RIGHT);
      lootingBonus.tooltips(new String[]{"Insert dimensional shards", "for looting bonus"});
      Panel toplevel = (Panel)((Panel)Widgets.positional().background(iconLocation))
         .children(
            new Widget[]{
               this.energyBar,
               this.visibilityOptions,
               this.shieldTextures,
               redstoneMode,
               filterPanel,
               this.actionOptions,
               this.typeOptions,
               this.player,
               controlPanel,
               this.damageType,
               this.colorSelector,
               lootingBonus,
               light
            }
         );
      toplevel.bounds(this.leftPos, this.topPos, this.imageWidth, this.imageHeight);
      this.window = new Window(this, toplevel);
      ShieldProjectorTileEntity tileEntity = (ShieldProjectorTileEntity)this.getBE();
      this.window.bind("redstone", tileEntity, GenericTileEntity.VALUE_RSMODE.name());
      this.window.bind("visibility", tileEntity, ShieldProjectorTileEntity.VALUE_SHIELDVISMODE.key().name());
      this.window.bind("shieldtextures", tileEntity, ShieldProjectorTileEntity.VALUE_SHIELDTEXTURE.key().name());
      this.window.bind("damage", tileEntity, ShieldProjectorTileEntity.VALUE_DAMAGEMODE.key().name());
      this.window.bind("color", tileEntity, ShieldProjectorTileEntity.VALUE_COLOR.key().name());
      this.window.bind("light", tileEntity, ShieldProjectorTileEntity.VALUE_LIGHT.key().name());
      this.window.event("addfilter", (source, params) -> this.addNewFilter());
      this.window.event("delfilter", (source, params) -> this.removeSelectedFilter());
      this.window.event("upfilter", (source, params) -> this.moveFilterUp());
      this.window.event("downfilter", (source, params) -> this.moveFilterDown());
      this.listDirty = 0;
      this.requestFilters();
   }

   private void selectFilter() {
      int selected = this.filterList.getSelected();
      if (selected != -1) {
         ShieldFilter shieldFilter = this.filters.get(selected);
         boolean solid = (shieldFilter.getAction() & 1) != 0;
         boolean damage = (shieldFilter.getAction() & 2) != 0;
         if (solid && damage) {
            this.actionOptions.choice("SolDmg");
         } else if (solid) {
            this.actionOptions.choice("Solid");
         } else if (damage) {
            this.actionOptions.choice("Damage");
         } else {
            this.actionOptions.choice("Pass");
         }

         String type = shieldFilter.getFilterName();
         if ("default".equals(type)) {
            this.typeOptions.choice("All");
         } else if ("animal".equals(type)) {
            this.typeOptions.choice("Passive");
         } else if ("hostile".equals(type)) {
            this.typeOptions.choice("Hostile");
         } else if ("player".equals(type)) {
            this.typeOptions.choice("Player");
         } else if ("item".equals(type)) {
            this.typeOptions.choice("Item");
         }

         if (shieldFilter instanceof PlayerFilter) {
            this.player.text(((PlayerFilter)shieldFilter).getName());
         } else {
            this.player.text("");
         }
      }
   }

   private void requestFilters() {
      Networking.sendToServer(PacketGetListFromServer.create(this.getBE().getBlockPos(), ShieldProjectorTileEntity.CMD_GETFILTERS.name()));
   }

   private void requestListsIfNeeded() {
      this.listDirty--;
      if (this.listDirty <= 0) {
         this.requestFilters();
         this.listDirty = 20;
      }
   }

   private void populateFilters() {
      List<ShieldFilter> newFilters = new ArrayList<>(fromServer_filters);
      if (!newFilters.equals(this.filters)) {
         this.filters = new ArrayList<>(newFilters);
         this.filterList.removeChildren();

         for (ShieldFilter filter : this.filters) {
            String n;
            if ("player".equals(filter.getFilterName())) {
               PlayerFilter playerFilter = (PlayerFilter)filter;
               if (playerFilter.getName() != null && !playerFilter.getName().isEmpty()) {
                  n = "player " + playerFilter.getName();
               } else {
                  n = "players";
               }
            } else {
               n = filter.getFilterName();
            }

            Panel panel = Widgets.horizontal();
            panel.children(
               new Widget[]{
                  ((Label)((Label)Widgets.label(n).color(StyleConfig.colorTextInListNormal)).horizontalAlignment(HorizontalAlignment.ALIGN_LEFT))
                     .desiredWidth(85)
               }
            );
            boolean solid = (filter.getAction() & 1) != 0;
            boolean damage = (filter.getAction() & 2) != 0;
            String actionName;
            if (solid && damage) {
               actionName = "SolDmg";
            } else if (solid) {
               actionName = "Solid";
            } else if (damage) {
               actionName = "Damage";
            } else {
               actionName = "Pass";
            }

            panel.children(
               new Widget[]{((Label)Widgets.label(actionName).color(StyleConfig.colorTextInListNormal)).horizontalAlignment(HorizontalAlignment.ALIGN_LEFT)}
            );
            this.filterList.children(new Widget[]{panel});
         }
      }
   }

   private void moveFilterUp() {
      this.sendServerCommandTyped(
         ShieldProjectorTileEntity.CMD_UPFILTER, TypedMap.builder().put(ShieldProjectorTileEntity.PARAM_SELECTED, this.filterList.getSelected()).build()
      );
      this.listDirty = 0;
   }

   private void moveFilterDown() {
      this.sendServerCommandTyped(
         ShieldProjectorTileEntity.CMD_DOWNFILTER, TypedMap.builder().put(ShieldProjectorTileEntity.PARAM_SELECTED, this.filterList.getSelected()).build()
      );
      this.listDirty = 0;
   }

   private void addNewFilter() {
      String actionName = this.actionOptions.getCurrentChoice();
      int action;
      if ("Pass".equals(actionName)) {
         action = 0;
      } else if ("Solid".equals(actionName)) {
         action = 1;
      } else if ("SolDmg".equals(actionName)) {
         action = 3;
      } else {
         action = 2;
      }

      String filterName = this.typeOptions.getCurrentChoice();
      String type;
      if ("All".equals(filterName)) {
         type = "default";
      } else if ("Passive".equals(filterName)) {
         type = "animal";
      } else if ("Hostile".equals(filterName)) {
         type = "hostile";
      } else if ("Item".equals(filterName)) {
         type = "item";
      } else {
         type = "player";
      }

      String playerName = this.player.getText();
      this.sendServerCommandTyped(
         ShieldProjectorTileEntity.CMD_ADDFILTER,
         TypedMap.builder()
            .put(ShieldProjectorTileEntity.PARAM_ACTION, action)
            .put(ShieldProjectorTileEntity.PARAM_TYPE, type)
            .put(ShieldProjectorTileEntity.PARAM_PLAYER, playerName)
            .put(ShieldProjectorTileEntity.PARAM_SELECTED, this.filterList.getSelected())
            .build()
      );
      this.listDirty = 0;
   }

   private void removeSelectedFilter() {
      this.sendServerCommandTyped(
         ShieldProjectorTileEntity.CMD_DELFILTER, TypedMap.builder().put(ShieldProjectorTileEntity.PARAM_SELECTED, this.filterList.getSelected()).build()
      );
      this.listDirty = 0;
   }

   private ImageChoiceLabel initRedstoneMode() {
      ImageChoiceLabel redstoneMode = ((ImageChoiceLabel)new ImageChoiceLabel().name("redstone"))
         .choice(RedstoneMode.REDSTONE_IGNORED.getDescription(), "Redstone mode:\nIgnored", iconGuiElements, 0, 0)
         .choice(RedstoneMode.REDSTONE_OFFREQUIRED.getDescription(), "Redstone mode:\nOff to activate", iconGuiElements, 16, 0)
         .choice(RedstoneMode.REDSTONE_ONREQUIRED.getDescription(), "Redstone mode:\nOn to activate", iconGuiElements, 32, 0);
      redstoneMode.hint(62, 200, 16, 16);
      redstoneMode.setCurrentChoice(this.getBE().getRSMode().ordinal());
      return redstoneMode;
   }

   private void initVisibilityMode() {
      this.visibilityOptions = (ChoiceLabel)((ChoiceLabel)new ChoiceLabel().name("visibility")).hint(25, 161, 54, 14);

      for (ShieldRenderingMode m : ShieldRenderingMode.values()) {
         if ((Boolean)ShieldConfiguration.allowInvisibleShield.get() || m != ShieldRenderingMode.INVISIBLE) {
            this.visibilityOptions.choices(new String[]{m.getDescription()});
         }
      }

      if ((Boolean)ShieldConfiguration.allowInvisibleShield.get()) {
         this.visibilityOptions.choiceTooltip(ShieldRenderingMode.INVISIBLE.getDescription(), new String[]{"Shield is completely invisible"});
      }

      this.visibilityOptions.choiceTooltip(ShieldRenderingMode.SHIELD.getDescription(), new String[]{"Default shield texture"});
      this.visibilityOptions.choiceTooltip(ShieldRenderingMode.TRANSP.getDescription(), new String[]{"Transparent shield texture"});
      this.visibilityOptions.choiceTooltip(ShieldRenderingMode.SOLID.getDescription(), new String[]{"Solid shield texture"});
      this.visibilityOptions.choiceTooltip(ShieldRenderingMode.MIMIC.getDescription(), new String[]{"Use the texture from the supplied block"});
   }

   private void initShieldTextures() {
      this.shieldTextures = (ChoiceLabel)((ChoiceLabel)new ChoiceLabel().name("shieldtextures")).hint(45, 143, 34, 14);

      for (ShieldTexture m : ShieldTexture.values()) {
         this.shieldTextures.choices(new String[]{m.getDescription()});
      }
   }

   private void initActionOptions() {
      this.actionOptions = (ChoiceLabel)new ChoiceLabel().hint(170, 12, 80, 14);
      this.actionOptions.choices(new String[]{"Pass", "Solid", "Damage", "SolDmg"});
      this.actionOptions.choiceTooltip("Pass", new String[]{"Entity that matches this filter", "can pass through"});
      this.actionOptions.choiceTooltip("Solid", new String[]{"Entity that matches this filter", "cannot pass"});
      this.actionOptions.choiceTooltip("Damage", new String[]{"Entity that matches this filter", "can pass but gets damage"});
      this.actionOptions.choiceTooltip("SolDmg", new String[]{"Entity that matches this filter", "cannot pass and gets damage"});
   }

   private void initTypeOptions() {
      this.typeOptions = (ChoiceLabel)new ChoiceLabel().hint(170, 28, 80, 14);
      this.typeOptions.choices(new String[]{"All", "Passive", "Hostile", "Item", "Player"});
      this.typeOptions.choiceTooltip("All", new String[]{"Matches everything"});
      this.typeOptions.choiceTooltip("Passive", new String[]{"Matches passive mobs"});
      this.typeOptions.choiceTooltip("Hostile", new String[]{"Matches hostile mobs"});
      this.typeOptions.choiceTooltip("Item", new String[]{"Matches items"});
      this.typeOptions.choiceTooltip("Player", new String[]{"Matches players", "(optionally named)"});
   }

   private void initDamageType() {
      this.damageType = (ChoiceLabel)((ChoiceLabel)new ChoiceLabel().name("damage")).hint(170, 102, 80, 14);
      this.damageType.choices(new String[]{DAMAGETYPE_GENERIC, DAMAGETYPE_PLAYER});
      this.damageType.choiceTooltip(DAMAGETYPE_GENERIC, new String[]{"Generic damage type"});
      this.damageType.choiceTooltip(DAMAGETYPE_PLAYER, new String[]{"Damage as done by a player"});
   }

   private void enableButtons() {
      int sel = this.filterList.getSelected();
      int cnt = this.filterList.getMaximum();
      this.delFilter.enabled(sel != -1 && cnt > 0);
      this.upFilter.enabled(sel > 0 && cnt > 0);
      this.downFilter.enabled(sel < cnt - 1 && sel != -1 && cnt > 0);
      if (sel == -1) {
         this.addFilter.text("Add");
      } else {
         this.addFilter.text("Ins");
      }

      this.player.enabled("Player".equals(this.typeOptions.getCurrentChoice()));
   }

   public void extractBackground(@Nonnull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
      this.requestListsIfNeeded();
      this.populateFilters();
      this.enableButtons();
      this.drawWindow(graphics, partialTicks, mouseX, mouseY);
      ShieldProjectorTileEntity tileEntity = (ShieldProjectorTileEntity)this.getBE();
      this.colorSelector.currentColor(tileEntity.getShieldColor());
      this.updateEnergyBar(this.energyBar);
   }
}
