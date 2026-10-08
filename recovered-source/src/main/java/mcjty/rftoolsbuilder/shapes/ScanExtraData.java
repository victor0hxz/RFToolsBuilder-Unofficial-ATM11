package mcjty.rftoolsbuilder.shapes;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class ScanExtraData {
   private final List<ScanExtraData.Beacon> beacons = new ArrayList<>();
   private long birthTime = System.currentTimeMillis();
   public static final StreamCodec<FriendlyByteBuf, ScanExtraData> OPTIONAL_STREAM_CODEC = StreamCodec.of((buf, data) -> {
      if (data != null) {
         buf.writeBoolean(true);
         buf.writeInt(data.beacons.size());

         for (ScanExtraData.Beacon beacon : data.beacons) {
            buf.writeBlockPos(beacon.getPos());
            buf.writeByte(beacon.getType().ordinal());
            buf.writeBoolean(beacon.isDoBeacon());
         }

         buf.writeLong(data.birthTime);
      } else {
         buf.writeBoolean(false);
      }
   }, buf -> {
      if (!buf.readBoolean()) {
         return null;
      } else {
         ScanExtraData data = new ScanExtraData();
         int size = buf.readInt();

         for (int i = 0; i < size; i++) {
            BlockPos pos = buf.readBlockPos();
            BeaconType type = BeaconType.values()[buf.readByte()];
            boolean doBeacon = buf.readBoolean();
            data.addBeacon(pos, type, doBeacon);
         }

         data.birthTime = buf.readLong();
         return data;
      }
   });

   public void clear() {
      this.beacons.clear();
   }

   public void addBeacon(BlockPos beacon, BeaconType type, boolean doBeacon) {
      this.beacons.add(new ScanExtraData.Beacon(beacon, type, doBeacon));
   }

   public void touch() {
      this.birthTime = System.currentTimeMillis();
   }

   public long getBirthTime() {
      return this.birthTime;
   }

   public List<ScanExtraData.Beacon> getBeacons() {
      return this.beacons;
   }

   public static class Beacon {
      private final BlockPos pos;
      private final BeaconType type;
      private final boolean doBeacon;

      public Beacon(BlockPos pos, BeaconType type, boolean doBeacon) {
         this.pos = pos;
         this.type = type;
         this.doBeacon = doBeacon;
      }

      public BlockPos getPos() {
         return this.pos;
      }

      public BeaconType getType() {
         return this.type;
      }

      public boolean isDoBeacon() {
         return this.doBeacon;
      }
   }
}
