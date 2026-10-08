package mcjty.rftoolsbuilder.modules.builder;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.varia.LevelTools;
import mcjty.lib.worlddata.AbstractWorldData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public class SpaceChamberRepository extends AbstractWorldData<SpaceChamberRepository> {
   private static final String SPACECHAMBER_CHANNELS_NAME = "RFToolsSpaceChambers";
   private int lastId = 0;
   private final Map<Integer, SpaceChamberRepository.SpaceChamberChannel> channels = new HashMap<>();

   public SpaceChamberRepository() {
   }

   public SpaceChamberRepository(CompoundTag tag) {
      ListTag lst = tag.getListOrEmpty("channels");

      for (int i = 0; i < lst.size(); i++) {
         CompoundTag tc = lst.getCompoundOrEmpty(i);
         int channel = tc.getIntOr("channel", 0);
         SpaceChamberRepository.SpaceChamberChannel value = new SpaceChamberRepository.SpaceChamberChannel();
         value.setDimension(LevelTools.getId(tc.getStringOr("dimension", "")));
         value.setMinCorner(BlockPosTools.read(tc, "minCorner"));
         value.setMaxCorner(BlockPosTools.read(tc, "maxCorner"));
         this.channels.put(channel, value);
      }

      this.lastId = tag.getIntOr("lastId", 0);
   }

   public static SpaceChamberRepository get(Level world) {
      return (SpaceChamberRepository)getData(world, SpaceChamberRepository::new, SpaceChamberRepository::new, "RFToolsSpaceChambers");
   }

   public SpaceChamberRepository.SpaceChamberChannel getOrCreateChannel(int id) {
      SpaceChamberRepository.SpaceChamberChannel channel = this.channels.get(id);
      if (channel == null) {
         channel = new SpaceChamberRepository.SpaceChamberChannel();
         this.channels.put(id, channel);
      }

      return channel;
   }

   public SpaceChamberRepository.SpaceChamberChannel getChannel(int id) {
      return this.channels.get(id);
   }

   public void deleteChannel(int id) {
      this.channels.remove(id);
   }

   public int newChannel() {
      this.lastId++;
      return this.lastId;
   }

   @Nonnull
   public CompoundTag save(@Nonnull CompoundTag tagCompound, Provider provider) {
      ListTag lst = new ListTag();

      for (Entry<Integer, SpaceChamberRepository.SpaceChamberChannel> entry : this.channels.entrySet()) {
         CompoundTag tc = new CompoundTag();
         tc.putInt("channel", entry.getKey());
         tc.putString("dimension", entry.getValue().getDimension().identifier().toString());
         BlockPosTools.write(tc, "minCorner", entry.getValue().getMinCorner());
         BlockPosTools.write(tc, "maxCorner", entry.getValue().getMaxCorner());
         lst.add(tc);
      }

      tagCompound.put("channels", lst);
      tagCompound.putInt("lastId", this.lastId);
      return tagCompound;
   }

   public static class SpaceChamberChannel {
      private ResourceKey<Level> dimension;
      private BlockPos minCorner = null;
      private BlockPos maxCorner = null;

      public ResourceKey<Level> getDimension() {
         return this.dimension;
      }

      public void setDimension(ResourceKey<Level> dimension) {
         this.dimension = dimension;
      }

      public BlockPos getMinCorner() {
         return this.minCorner;
      }

      public void setMinCorner(BlockPos minCorner) {
         this.minCorner = minCorner;
      }

      public BlockPos getMaxCorner() {
         return this.maxCorner;
      }

      public void setMaxCorner(BlockPos maxCorner) {
         this.maxCorner = maxCorner;
      }
   }
}
