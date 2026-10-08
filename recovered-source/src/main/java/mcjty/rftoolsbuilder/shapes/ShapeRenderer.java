package mcjty.rftoolsbuilder.shapes;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;

public class ShapeRenderer {
   private ShapeID shapeID;
   private float scale = 3.0F;

   public ShapeRenderer(ShapeID shapeID) {
      this.shapeID = shapeID;
   }

   public void setShapeID(ShapeID shapeID) {
      this.shapeID = shapeID;
   }

   public ShapeID getShapeID() {
      return this.shapeID;
   }

   public int getCount() {
      RenderData data = ShapeDataManagerClient.getRenderData(this.shapeID);
      return data == null ? 0 : data.getBlockCount();
   }

   public static RenderData getRenderDataAndCreate(ShapeID shapeID) {
      RenderData data = ShapeDataManagerClient.getRenderDataAndCreate(shapeID);
      data.touch();
      return data;
   }

   public static void setRenderData(ShapeID id, int checksum, @Nullable RenderData.RenderPlane plane, int offsetY, int dy, String msg) {
      RenderData data = getRenderDataAndCreate(id);
      if (data.getChecksum() == checksum) {
         data.setPlaneData(plane, offsetY, dy);
         data.previewMessage = msg;
         data.markRequestProgress();
         if (offsetY >= dy - 1) {
            data.clearRequest();
         }
      }
   }

   public void initView(int dx, int dy) {
   }

   public void handleShapeDragging(int x, int y, boolean[] buttons) {
   }

   public void handleMouseWheel(double dwheelX, double dwheelY) {
      if (dwheelY < 0.0) {
         this.scale = Math.max(0.1F, this.scale * 0.6F);
      } else if (dwheelY > 0.0) {
         this.scale *= 1.4F;
      }
   }

   public boolean renderShapeInWorld(
      PoseStack poseStack, ItemStack stack, float offset, float scale, float angle, boolean scan, ShapeID shape, boolean renderBlockModels
   ) {
      RenderData data = ShapeDataManagerClient.getRenderData(this.shapeID);
      return data != null && data.hasData();
   }

   public void renderShape(
      GuiGraphicsExtractor graphics,
      IShapeParentGui gui,
      ItemStack stack,
      int x,
      int y,
      boolean showAxis,
      boolean showOuter,
      boolean showScan,
      boolean showGuidelines
   ) {
   }
}
