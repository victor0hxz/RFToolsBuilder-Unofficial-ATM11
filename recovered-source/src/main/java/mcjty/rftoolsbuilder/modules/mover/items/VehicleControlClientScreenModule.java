package mcjty.rftoolsbuilder.modules.mover.items;

import mcjty.lib.client.RenderHelper;
import mcjty.rftoolsbase.api.screens.IClientScreenModule;
import mcjty.rftoolsbase.api.screens.IModuleRenderHelper;
import mcjty.rftoolsbase.api.screens.ITextRenderHelper;
import mcjty.rftoolsbase.api.screens.ModuleRenderInfo;
import mcjty.rftoolsbase.api.screens.IClientScreenModule.TransformMode;
import mcjty.rftoolsbase.tools.ScreenTextHelper;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class VehicleControlClientScreenModule implements IClientScreenModule<VehicleControlScreenModule.EmptyData> {
   private boolean activated = false;
   private final ITextRenderHelper labelCache = new ScreenTextHelper();
   private final ITextRenderHelper buttonCache = new ScreenTextHelper();

   public TransformMode getTransformMode(ItemStack moduleItem) {
      return TransformMode.TEXT;
   }

   public int getHeight(ItemStack moduleItem) {
      return 14;
   }

   public void render(
      GuiGraphicsExtractor graphics,
      MultiBufferSource buffer,
      IModuleRenderHelper renderHelper,
      Font fontRenderer,
      int currenty,
      VehicleControlScreenModule.EmptyData screenData,
      ModuleRenderInfo renderInfo
   ) {
      VehicleControlScreenModule data = VehicleControlModuleItem.data(renderInfo.moduleStack);
      int xoffset;
      int buttonWidth;
      if (!data.getLine().isEmpty()) {
         this.labelCache.setup(data.getLine(), 316, renderInfo);
         this.labelCache.align(data.getAlign());
         this.labelCache.renderText(graphics, buffer, 0, currenty + 2, data.getColor(), renderInfo);
         xoffset = 47;
         buttonWidth = 300;
      } else {
         xoffset = 12;
         buttonWidth = 490;
      }

      boolean act = this.activated;
      RenderHelper.drawBeveledBox(
         graphics,
         buffer,
         xoffset - 5,
         currenty,
         123,
         currenty + 12,
         act ? -13421773 : -1118482,
         act ? -1118482 : -13421773,
         -10066330,
         renderInfo.getLightmapValue()
      );
      this.buttonCache.setup(data.getButton(), buttonWidth, renderInfo);
      this.buttonCache.renderText(graphics, buffer, xoffset - 10 + (act ? 1 : 0), currenty + 2, data.getButtonColor(), renderInfo);
   }

   public void mouseClick(ItemStack moduleStack, Level world, int x, int y, boolean clicked) {
      VehicleControlScreenModule data = VehicleControlModuleItem.data(moduleStack);
      int xoffset;
      if (!data.getLine().isEmpty()) {
         xoffset = 40;
      } else {
         xoffset = 5;
      }

      this.activated = false;
      if (x >= xoffset) {
         this.activated = clicked;
      }
   }

   public boolean needsServerData() {
      return false;
   }
}
