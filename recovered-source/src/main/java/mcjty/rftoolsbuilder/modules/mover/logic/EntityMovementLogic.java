package mcjty.rftoolsbuilder.modules.mover.logic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import mcjty.lib.varia.SafeClientTools;
import mcjty.rftoolsbuilder.compat.CompatNbt;
import mcjty.rftoolsbuilder.modules.mover.blocks.MoverTileEntity;
import mcjty.rftoolsbuilder.modules.mover.items.VehicleCard;
import mcjty.rftoolsbuilder.modules.mover.network.PacketGrabbedEntitiesToClient;
import mcjty.rftoolsbuilder.setup.RFToolsBuilderMessages;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class EntityMovementLogic {
   private final MoverTileEntity mover;
   private BlockPos destination = null;
   private long starttick;
   private float totalDist;
   private BlockPos source = null;
   private Map<Integer, Vec3> grabbedEntities = new HashMap<>();
   private int grabTimeout = 0;
   private int waitABit = 0;

   public EntityMovementLogic(MoverTileEntity mover) {
      this.mover = mover;
   }

   public void setGrabbedEntitiesClient(Set<Integer> grabbedEntities) {
      Level level = this.mover.getLevel();
      this.grabbedEntities = new HashMap<>();

      for (Integer id : grabbedEntities) {
         Entity entity = level.getEntity(id);
         if (entity != null) {
            this.grabbedEntities.put(id, entity.position());
         }
      }
   }

   public Vec3 tryMoveVehicleThisPlayer(float partialTicks) {
      Vec3 result = Vec3.ZERO;
      if (this.destination != null) {
         Player clientPlayer = SafeClientTools.getClientPlayer();
         Level level = this.mover.getLevel();
         Vec3 startPos = this.getMovingPosition(0.0F, this.starttick);
         Vec3 currentPos = this.getMovingPosition(partialTicks, level.getGameTime());
         double dx = currentPos.x - startPos.x;
         double dy = currentPos.y - startPos.y;
         double dz = currentPos.z - startPos.z;
         if (this.grabbedEntities.containsKey(clientPlayer.getId())) {
            Vec3 basePos = this.grabbedEntities.get(clientPlayer.getId());
            double desiredX = basePos.x + dx;
            double desiredY = basePos.y + dy;
            double desiredZ = basePos.z + dz;
            result = new Vec3(desiredX - clientPlayer.getX(), desiredY - clientPlayer.getY(), desiredZ - clientPlayer.getZ());
            clientPlayer.setPos(desiredX, desiredY, desiredZ);
            clientPlayer.setOldPosAndRot();
            clientPlayer.fallDistance = 0.0;
            clientPlayer.setDeltaMovement(Vec3.ZERO);
            clientPlayer.setOnGround(true);
         }
      }

      return result;
   }

   public void tryMoveVehicleClientEntities(float partialTicks) {
      if (this.destination != null) {
         Player clientPlayer = SafeClientTools.getClientPlayer();
         Level level = this.mover.getLevel();
         Vec3 startPos = this.getMovingPosition(0.0F, this.starttick);
         Vec3 currentPos = this.getMovingPosition(partialTicks, level.getGameTime());
         double dx = currentPos.x - startPos.x;
         double dy = currentPos.y - startPos.y;
         double dz = currentPos.z - startPos.z;

         for (Entry<Integer, Vec3> pair : this.grabbedEntities.entrySet()) {
            Entity entity = level.getEntity(pair.getKey());
            if (entity != null && entity != clientPlayer) {
               Vec3 basePos = pair.getValue();
               double desiredX = basePos.x + dx;
               double desiredY = basePos.y + dy;
               double desiredZ = basePos.z + dz;
               entity.setPos(desiredX, desiredY, desiredZ);
               entity.setOldPosAndRot();
               entity.fallDistance = 0.0;
               entity.setDeltaMovement(Vec3.ZERO);
               entity.setOnGround(true);
            }
         }
      }
   }

   public void setWaitABit(int waitABit) {
      this.waitABit = waitABit;
   }

   public void tryMoveVehicleServer() {
      ItemStack vehicle = this.mover.getCard();
      if (this.destination != null) {
         this.checkUnmounts();
         this.actualMoveServer();
      } else if (!vehicle.isEmpty()) {
         if (this.grabTimeout > 0) {
            this.grabTimeout--;
            this.doGrab(0.0, 0.0, 0.0);
         }

         if (this.waitABit > 0) {
            this.waitABit--;
            return;
         }

         BlockPos destination = VehicleCard.getDesiredDestination(vehicle);
         if (destination != null) {
            if (destination.equals(this.mover.getBlockPos())) {
               VehicleCard.clearDesiredDestination(vehicle);
               return;
            }

            if (this.mover.hasDirectContectionTo(destination)) {
               this.setupMovementTo(destination);
            } else {
               BlockPos dest = this.mover.traverseBreadthFirstWithPath((p, child) -> destination.equals(child.getBlockPos()) ? p.get(0) : null);
               if (dest != null) {
                  this.setupMovementTo(dest);
               }
            }
         }
      }
   }

   private void checkUnmounts() {
      if (!MoverTileEntity.wantUnmount.isEmpty()) {
         Set<Integer> toRemove = new HashSet<>();

         for (Integer id : MoverTileEntity.wantUnmount) {
            if (this.grabbedEntities.containsKey(id)) {
               toRemove.add(id);
               this.grabbedEntities.remove(id);
            }
         }

         if (!toRemove.isEmpty()) {
            MoverTileEntity.wantUnmount.removeAll(toRemove);
            this.syncGrabbedToClient();
         }
      }
   }

   public void setupMovementTo(BlockPos dest) {
      Level level = this.mover.getLevel();
      if (level.getBlockEntity(dest) instanceof MoverTileEntity destMover && destMover.isAvailable()) {
         BlockPos worldPosition = this.mover.getBlockPos();
         destMover.setSource(worldPosition);
         this.destination = dest;
         this.totalDist = (float)Math.sqrt(this.destination.distToCenterSqr(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ()));
         this.starttick = level.getGameTime();
         this.grabEntities();
         this.mover.markDirtyClient();
      }
   }

   public int getGrabTimeout() {
      return this.grabTimeout;
   }

   public void setGrabTimeout(int grabTimeout) {
      this.grabTimeout = grabTimeout;
   }

   public void clearGrabbedEntities() {
      if (!this.grabbedEntities.isEmpty()) {
         this.grabbedEntities.clear();
         this.mover.setChanged();
      }
   }

   public void grabEntities() {
      this.grabbedEntities.clear();
      AABB aabb = this.getVehicleAABB();
      Level level = this.mover.getLevel();

      for (Entity entity : level.getEntitiesOfClass(Entity.class, aabb)) {
         this.grabbedEntities.put(entity.getId(), entity.position());
         MoverTileEntity.wantUnmount.remove(entity.getId());
      }

      this.syncGrabbedToClient();
   }

   public void syncGrabbedToClient() {
      Level level = this.mover.getLevel();
      BlockPos worldPosition = this.mover.getBlockPos();
      PacketGrabbedEntitiesToClient packet = PacketGrabbedEntitiesToClient.create(worldPosition, this.grabbedEntities.keySet());
      ChunkPos cp = ChunkPos.containing(worldPosition);
      RFToolsBuilderMessages.sendToChunk(packet, level.getChunk(cp.x(), cp.z()));
   }

   private AABB getVehicleAABB() {
      BlockPos blockPos = this.mover.getBlockPos();
      Map<BlockState, List<BlockPos>> blocks = VehicleCard.getBlocks(this.mover.getCard(), blockPos.offset(this.mover.getOffset()));
      int minx = Integer.MAX_VALUE;
      int miny = Integer.MAX_VALUE;
      int minz = Integer.MAX_VALUE;
      int maxx = Integer.MIN_VALUE;
      int maxy = Integer.MIN_VALUE;
      int maxz = Integer.MIN_VALUE;

      for (List<BlockPos> value : blocks.values()) {
         for (BlockPos pos : value) {
            minx = Math.min(minx, pos.getX());
            miny = Math.min(miny, pos.getY());
            minz = Math.min(minz, pos.getZ());
            maxx = Math.max(maxx, pos.getX() + 1);
            maxy = Math.max(maxy, pos.getY() + 1);
            maxz = Math.max(maxz, pos.getZ() + 1);
         }
      }

      return new AABB(minx, miny, minz, maxx, maxy, maxz);
   }

   public void doGrab(double dx, double dy, double dz) {
      Level level = this.mover.getLevel();
      this.grabbedEntities.forEach((id, basePos) -> {
         Entity entity = level.getEntity(id);
         if (entity != null && entity.isAlive()) {
            entity.setPos(basePos.x + dx, basePos.y + dy, basePos.z + dz);
            entity.setOldPosAndRot();
            entity.fallDistance = 0.0;
            entity.setDeltaMovement(Vec3.ZERO);
            entity.setOnGround(true);
         }
      });
   }

   public void endMoveServer() {
      Vec3 startPos = this.getMovingPosition(0.0F, this.starttick);
      Vec3 currentPos = this.getMovingPosition(0.0F, this.starttick + this.getTotalTicks());
      double dx = currentPos.x - startPos.x;
      double dy = currentPos.y - startPos.y;
      double dz = currentPos.z - startPos.z;
      this.doGrab(dx, dy, dz);
   }

   private void actualMoveServer() {
      Level level = this.mover.getLevel();
      long totalTicks = this.getTotalTicks();
      long currentTick = level.getGameTime() - this.starttick;
      Vec3 startPos = this.getMovingPosition(0.0F, this.starttick);
      Vec3 currentPos = this.getMovingPosition(0.0F, level.getGameTime() + 1L);
      double dx = currentPos.x - startPos.x;
      double dy = currentPos.y - startPos.y;
      double dz = currentPos.z - startPos.z;
      this.doGrab(dx, dy, dz);
      if (currentTick >= totalTicks) {
         this.mover.arriveAtDestination();
      }
   }

   public long getStarttick() {
      return this.starttick;
   }

   public long getTotalTicks() {
      return (long)this.totalDist;
   }

   public BlockPos getDestination() {
      return this.destination;
   }

   public void setDestination(BlockPos destination) {
      this.destination = destination;
   }

   public BlockPos getSource() {
      return this.source;
   }

   public void setSource(BlockPos source) {
      this.source = source;
   }

   @NotNull
   public Vec3 getMovingPosition(float partialTicks, long gameTick) {
      BlockPos blockPos = this.mover.getBlockPos();
      BlockPos destination = this.getDestination();
      Vec3 current = new Vec3(blockPos.getX(), blockPos.getY(), blockPos.getZ());
      if (destination != null) {
         long totalTicks = this.getTotalTicks();
         long currentTick = gameTick - this.starttick;
         Vec3 dest = new Vec3(destination.getX(), destination.getY(), destination.getZ());
         current = current.lerp(dest, (double)((float)currentTick + partialTicks) / totalTicks);
      }

      return current;
   }

   public void load(CompoundTag tagCompound) {
      if (tagCompound.contains("source")) {
         this.source = CompatNbt.getBlockPos(tagCompound, "source");
      } else {
         this.source = null;
      }

      if (tagCompound.contains("destination")) {
         this.destination = CompatNbt.getBlockPos(tagCompound, "destination");
      } else {
         this.destination = null;
      }

      this.starttick = tagCompound.getLongOr("starttick", 0L);
      this.totalDist = tagCompound.getFloatOr("totalDist", 0.0F);
   }

   public void loadClientDataFromNBT(CompoundTag tagCompound) {
      if (tagCompound.contains("destination")) {
         this.destination = CompatNbt.getBlockPos(tagCompound, "destination");
      } else {
         this.destination = null;
      }

      this.starttick = tagCompound.getLongOr("starttick", 0L);
      this.totalDist = tagCompound.getFloatOr("totalDist", 0.0F);
   }

   public void save(@Nonnull CompoundTag tagCompound) {
      if (this.source != null) {
         CompatNbt.putBlockPos(tagCompound, "source", this.source);
      }

      if (this.destination != null) {
         CompatNbt.putBlockPos(tagCompound, "destination", this.destination);
      }

      tagCompound.putLong("starttick", this.starttick);
      tagCompound.putFloat("totalDist", this.totalDist);
   }

   public void saveClientDataToNBT(CompoundTag tagCompound) {
      tagCompound.putLong("starttick", this.starttick);
      tagCompound.putFloat("totalDist", this.totalDist);
      if (this.destination != null) {
         CompatNbt.putBlockPos(tagCompound, "destination", this.destination);
      }
   }
}
