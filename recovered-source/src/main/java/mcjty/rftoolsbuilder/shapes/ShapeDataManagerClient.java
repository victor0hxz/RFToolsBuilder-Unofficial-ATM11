package mcjty.rftoolsbuilder.shapes;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent.AfterLevel;

public class ShapeDataManagerClient {
   static final Map<ShapeID, RenderData> renderDataMap = new HashMap<>();
   private static int cleanupCounter = 20;

   @Nullable
   public static RenderData getRenderData(ShapeID shapeID) {
      return renderDataMap.get(shapeID);
   }

   @Nonnull
   public static RenderData getRenderDataAndCreate(ShapeID shapeID) {
      RenderData data = renderDataMap.get(shapeID);
      if (data == null) {
         data = new RenderData();
         renderDataMap.put(shapeID, data);
      }

      return data;
   }

   public static void cleanupOldRenderers(AfterLevel event) {
      cleanupCounter--;
      if (cleanupCounter < 0) {
         cleanupCounter = 20;
         Set<ShapeID> toRemove = new HashSet<>();

         for (Entry<ShapeID, RenderData> entry : renderDataMap.entrySet()) {
            if (entry.getValue().tooOld()) {
               toRemove.add(entry.getKey());
            }
         }

         for (ShapeID id : toRemove) {
            RenderData data = renderDataMap.get(id);
            data.cleanup();
            renderDataMap.remove(id);
         }
      }
   }
}
