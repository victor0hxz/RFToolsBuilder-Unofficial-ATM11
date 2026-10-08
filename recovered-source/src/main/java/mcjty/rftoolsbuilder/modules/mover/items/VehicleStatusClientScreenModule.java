package mcjty.rftoolsbuilder.modules.mover.items;

import mcjty.rftoolsbase.api.screens.IClientScreenModule;
import mcjty.rftoolsbase.api.screens.IModuleRenderHelper;
import mcjty.rftoolsbase.api.screens.ITextRenderHelper;
import mcjty.rftoolsbase.api.screens.ModuleRenderInfo;
import mcjty.rftoolsbase.api.screens.IClientScreenModule.TransformMode;
import mcjty.rftoolsbase.api.screens.data.IModuleDataString;
import mcjty.rftoolsbase.tools.ScreenTextHelper;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class VehicleStatusClientScreenModule implements IClientScreenModule<IModuleDataString> {
   private final ITextRenderHelper labelCache = new ScreenTextHelper();
   private final ITextRenderHelper cache = new ScreenTextHelper();

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
      IModuleDataString screenData,
      ModuleRenderInfo renderInfo
   ) {
      VehicleStatusScreenModule data = VehicleStatusModuleItem.data(renderInfo.moduleStack);
      int xoffset;
      int buttonWidth;
      if (!data.getLabel().isEmpty()) {
         this.labelCache.setup(data.getLabel(), 316, renderInfo);
         this.labelCache.align(data.getAlign());
         this.labelCache.renderText(graphics, buffer, 0, currenty + 2, data.getLabelColor(), renderInfo);
         xoffset = 47;
         buttonWidth = 300;
      } else {
         xoffset = 12;
         buttonWidth = 490;
      }

      String line = screenData == null ? null : screenData.get();
      if (line != null) {
         this.cache.setup(line, buttonWidth, renderInfo);
         this.cache.setDirty();
         this.cache.renderText(graphics, buffer, xoffset - 10, currenty + 2, data.getColor(), renderInfo);
      }
   }

   public void mouseClick(ItemStack moduleStack, Level world, int x, int y, boolean clicked) {
   }

   public boolean needsServerData() {
      return true;
   }
}
