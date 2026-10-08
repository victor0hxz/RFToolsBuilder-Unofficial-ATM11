package mcjty.rftoolsbuilder.shapes;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import mcjty.lib.varia.RLE;
import mcjty.rftoolsbuilder.modules.builder.items.ShapeCardItem;
import mcjty.rftoolsbuilder.modules.scanner.ScannerConfiguration;
import mcjty.rftoolsbuilder.modules.scanner.network.PacketReturnShapeData;
import mcjty.rftoolsbuilder.setup.RFToolsBuilderMessages;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class ShapeDataManagerServer {
   private static final Map<ShapeID, ShapeDataManagerServer.WorkQueue> workQueues = new HashMap<>();

   public static void pushWork(ShapeID shapeID, ItemStack stack, int offsetY, IFormula formula, boolean optimizeRenderShell, int checksum, ServerPlayer player) {
      ShapeDataManagerServer.WorkQueue queue = workQueues.get(shapeID);
      if (queue == null) {
         queue = new ShapeDataManagerServer.WorkQueue();
         workQueues.put(shapeID, queue);
      }

      if (queue.workingOn.containsKey(offsetY)) {
         queue.workingOn.get(offsetY).update(stack, offsetY, formula, optimizeRenderShell, checksum, player);
      } else {
         ShapeDataManagerServer.WorkUnit unit = new ShapeDataManagerServer.WorkUnit(stack, offsetY, formula, optimizeRenderShell, checksum, player);
         queue.workQueue.addLast(unit);
         queue.workingOn.put(offsetY, unit);
      }
   }

   public static void handleWork() {
      Set<ShapeID> toRemove = new HashSet<>();

      for (Entry<ShapeID, ShapeDataManagerServer.WorkQueue> entry : workQueues.entrySet()) {
         ShapeID shapeID = entry.getKey();
         ShapeDataManagerServer.WorkQueue queue = entry.getValue();
         int pertick = (Integer)ScannerConfiguration.planeSurfacePerTick.get();

         while (!queue.workQueue.isEmpty()) {
            ShapeDataManagerServer.WorkUnit unit = queue.workQueue.removeFirst();
            queue.workingOn.remove(unit.getOffsetY());
            ItemStack card = unit.getStack();
            boolean solid = unit.isOptimizeRenderShell();
            BlockPos dimension = ShapeCardItem.getShapeDataDimension(card);
            RLE positions = new RLE();
            StatePalette statePalette = new StatePalette();
            int cnt = ShapeCardItem.getRenderPositions(card, solid, positions, statePalette, unit.getFormula(), unit.getOffsetY());

            for (ServerPlayer player : unit.getPlayers()) {
               RFToolsBuilderMessages.sendToPlayer(
                  PacketReturnShapeData.create(shapeID, unit.getChecksum(), positions, statePalette, dimension, cnt, unit.getOffsetY(), ""), player
               );
            }

            if (cnt > 0) {
               pertick -= dimension.getX() * dimension.getZ();
               if (pertick <= 0) {
                  break;
               }
            }
         }

         if (queue.workQueue.isEmpty()) {
            toRemove.add(shapeID);
         }
      }

      for (ShapeID id : toRemove) {
         workQueues.remove(id);
      }
   }

   private static class WorkQueue {
      private final ArrayDeque<ShapeDataManagerServer.WorkUnit> workQueue = new ArrayDeque<>();
      private final Map<Integer, ShapeDataManagerServer.WorkUnit> workingOn = new HashMap<>();
   }

   private static class WorkUnit {
      private final List<ServerPlayer> players = new ArrayList<>();
      private ItemStack stack;
      private int offsetY;
      private IFormula formula;
      private boolean optimizeRenderShell;
      private int checksum;

      public WorkUnit(ItemStack stack, int offsetY, IFormula formula, boolean optimizeRenderShell, int checksum, ServerPlayer player) {
         this.stack = stack;
         this.offsetY = offsetY;
         this.formula = formula;
         this.optimizeRenderShell = optimizeRenderShell;
         this.checksum = checksum;
         this.players.add(player);
      }

      public void update(ItemStack stack, int offsetY, IFormula formula, boolean optimizeRenderShell, int checksum, ServerPlayer player) {
         this.stack = stack;
         this.offsetY = offsetY;
         this.formula = formula;
         this.optimizeRenderShell = optimizeRenderShell;
         this.checksum = checksum;
         if (!this.players.contains(player)) {
            this.players.add(player);
         }
      }

      public List<ServerPlayer> getPlayers() {
         return this.players;
      }

      public ItemStack getStack() {
         return this.stack;
      }

      public int getOffsetY() {
         return this.offsetY;
      }

      public IFormula getFormula() {
         return this.formula;
      }

      public boolean isOptimizeRenderShell() {
         return this.optimizeRenderShell;
      }

      public int getChecksum() {
         return this.checksum;
      }
   }
}
