package mcjty.rftoolsbuilder.shapes;

import com.mojang.blaze3d.vertex.BufferBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import mcjty.rftoolsbuilder.modules.scanner.ScannerConfiguration;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.Pair;

public class RenderData {
   public static BufferBuilder vboBuffer = null;
   private RenderData.RenderPlane[] planes = null;
   public String previewMessage = "";
   private long touchTime = 0L;
   private long checksum = -1L;
   private boolean wantData = true;
   private boolean requestInFlight = false;
   private long requestSentAt = 0L;
   private static final long REQUEST_TIMEOUT_MS = 5000L;

   public boolean hasData() {
      if (this.planes == null) {
         return false;
      } else {
         for (RenderData.RenderPlane plane : this.planes) {
            if (plane != null) {
               return true;
            }
         }

         return false;
      }
   }

   public int getBlockCount() {
      if (this.planes != null) {
         int cnt = 0;

         for (RenderData.RenderPlane plane : this.planes) {
            if (plane != null) {
               cnt += plane.getCount();
            }
         }

         return cnt;
      } else {
         return 0;
      }
   }

   public long getChecksum() {
      return this.checksum;
   }

   public void setChecksum(long checksum) {
      this.checksum = checksum;
   }

   public boolean isWantData() {
      return this.planes == null || this.wantData;
   }

   public void setWantData(boolean wantData) {
      this.wantData = wantData;
   }

   public boolean isRequestInFlight() {
      return this.requestInFlight;
   }

   public void markRequestSent() {
      this.requestInFlight = true;
      this.requestSentAt = System.currentTimeMillis();
   }

   public void clearRequest() {
      this.requestInFlight = false;
      this.requestSentAt = 0L;
   }

   public void markRequestProgress() {
      if (this.requestInFlight) {
         this.requestSentAt = System.currentTimeMillis();
      }
   }

   public boolean isRequestTimedOut() {
      return this.requestInFlight && this.requestSentAt + 5000L < System.currentTimeMillis();
   }

   public void clearData() {
      this.cleanup();
      this.planes = null;
      this.previewMessage = "";
   }

   public RenderData.RenderPlane[] getPlanes() {
      return this.planes;
   }

   public void setPlaneData(@Nullable RenderData.RenderPlane plane, int offsetY, int dy) {
      if (dy <= 0) {
         this.clearData();
      } else if (offsetY >= 0 && offsetY < dy) {
         if (this.planes == null) {
            this.planes = new RenderData.RenderPlane[dy];
         } else if (this.planes.length != dy) {
            this.clearData();
            this.planes = new RenderData.RenderPlane[dy];
         }

         if (plane != null) {
            if (this.planes[offsetY] == null) {
               plane.markUpdated();
               this.planes[offsetY] = plane;
            } else {
               this.planes[offsetY].refreshData(plane);
            }
         }
      }
   }

   public void touch() {
      this.touchTime = System.currentTimeMillis();
   }

   public boolean tooOld() {
      return this.touchTime + ((Integer)ScannerConfiguration.clientRenderDataTimeout.get()).intValue() < System.currentTimeMillis();
   }

   public void cleanup() {
      if (this.planes != null) {
         for (RenderData.RenderPlane plane : this.planes) {
            if (plane != null) {
               plane.cleanup();
            }
         }
      }
   }

   public void createRenderList(int y) {
      if (this.planes != null) {
         this.planes[y].createRenderList();
      }
   }

   public void performRenderToList(int y) {
      if (this.planes != null) {
         this.planes[y].performRenderToList();
      }
   }

   public static class RenderElement {
      protected boolean valid = false;

      public void cleanup() {
         this.valid = false;
      }

      public void createRenderList() {
         RenderData.vboBuffer = null;
         this.valid = false;
      }

      public void performRenderToList() {
         RenderData.vboBuffer = null;
         this.valid = false;
      }
   }

   public static class RenderPlane extends RenderData.RenderElement {
      private RenderData.RenderStrip[] strips;
      private int y;
      private int offsety;
      private int startz;
      private boolean dirty = true;
      private int count = 0;
      private long birthtime;
      private long flashBirthtime = 0L;

      public RenderPlane(RenderData.RenderStrip[] strips, int y, int offsety, int startz, int count) {
         this.strips = strips;
         this.y = y;
         this.offsety = offsety;
         this.startz = startz;
         this.count = count;
         this.birthtime = System.currentTimeMillis();
      }

      public void refreshData(RenderData.RenderPlane other) {
         this.strips = other.strips;
         this.y = other.y;
         this.offsety = other.offsety;
         this.startz = other.startz;
         this.count = other.count;
         this.markUpdated();
         super.cleanup();
      }

      public void markUpdated() {
         this.dirty = true;
         this.birthtime = System.currentTimeMillis();
         this.flashBirthtime = 0L;
      }

      public long getBirthtime() {
         return this.birthtime;
      }

      public void markFlashRendered() {
         if (this.flashBirthtime == 0L) {
            this.flashBirthtime = System.currentTimeMillis();
         }
      }

      public boolean isFlashing(long time) {
         return this.flashBirthtime != 0L && this.flashBirthtime > time - ((Integer)ScannerConfiguration.projectorFlashTimeout.get()).intValue();
      }

      public int getCount() {
         return this.count;
      }

      public void markClean() {
         this.dirty = false;
      }

      public boolean isDirty() {
         return this.dirty;
      }

      public RenderData.RenderStrip[] getStrips() {
         return this.strips;
      }

      public int getOffsety() {
         return this.offsety;
      }

      public int getY() {
         return this.y;
      }

      public int getStartz() {
         return this.startz;
      }
   }

   public static class RenderStrip {
      private final List<Pair<Integer, BlockState>> data = new ArrayList<>();
      private final int x;
      private BlockState last;
      private int cnt = 0;

      public RenderStrip(int x) {
         this.x = x;
      }

      public int getX() {
         return this.x;
      }

      public List<Pair<Integer, BlockState>> getData() {
         return this.data;
      }

      public boolean isEmptyAt(int i, Map<BlockState, ShapeBlockInfo> palette) {
         if (i < 0) {
            return true;
         } else if (i >= this.data.size()) {
            return true;
         } else {
            BlockState state = (BlockState)this.data.get(i).getValue();
            return ShapeBlockInfo.getBlockInfo(palette, state).isNonSolid() ? true : state == null;
         }
      }

      public void add(BlockState state) {
         if (this.cnt == 0) {
            this.last = state;
            this.cnt = 1;
         } else if (this.last != state) {
            this.data.add(Pair.of(this.cnt, this.last));
            this.last = state;
            this.cnt = 1;
         } else {
            this.cnt++;
         }
      }

      public void close() {
         if (this.cnt > 0) {
            this.data.add(Pair.of(this.cnt, this.last));
            this.cnt = 0;
         }
      }
   }
}
