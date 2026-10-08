package mcjty.rftoolsbuilder.modules.scanner.network;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import javax.annotation.Nullable;
import mcjty.lib.varia.RLE;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.modules.builder.BuilderModule;
import mcjty.rftoolsbuilder.modules.builder.blocks.SupportBlock;
import mcjty.rftoolsbuilder.modules.scanner.ScannerConfiguration;
import mcjty.rftoolsbuilder.shapes.RenderData;
import mcjty.rftoolsbuilder.shapes.ShapeID;
import mcjty.rftoolsbuilder.shapes.ShapeRenderer;
import mcjty.rftoolsbuilder.shapes.StatePalette;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketReturnShapeData(
   ShapeID shapeID,
   int checksum,
   @Nullable byte[] positionData,
   PacketReturnShapeData.PositionCodec positionCodec,
   int positionDataLength,
   StatePalette statePalette,
   BlockPos dimension,
   int count,
   int offsetY,
   String msg
) implements CustomPacketPayload {
   public static final Identifier ID = Identifier.fromNamespaceAndPath("rftoolsbuilder", "returnshapedata");
   public static final Type<PacketReturnShapeData> TYPE = new Type(ID);
   private static final int COMPRESSION_MIN_BYTES = 256;
   private static final int COMPRESSION_MIN_GAIN = 32;
   private static final int COMPRESSION_LEVEL = 9;
   public static final StreamCodec<RegistryFriendlyByteBuf, PacketReturnShapeData> CODEC = StreamCodec.of(
      PacketReturnShapeData::write, PacketReturnShapeData::read
   );

   private static int varIntSize(int value) {
      for (int i = 1; i < 5; i++) {
         if ((value & -1 << i * 7) == 0) {
            return i;
         }
      }

      return 5;
   }

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   private static void write(RegistryFriendlyByteBuf buf, PacketReturnShapeData packet) {
      ShapeID.STREAM_CODEC.encode(buf, packet.shapeID);
      buf.writeVarInt(packet.checksum);
      buf.writeVarInt(packet.count);
      buf.writeVarInt(packet.offsetY);
      buf.writeUtf(packet.msg);
      BlockPos.STREAM_CODEC.encode(buf, packet.dimension);
      StatePalette.OPTIONAL_STREAM_CODEC.encode(buf, packet.statePalette);
      if (packet.positionData != null && packet.positionData.length != 0) {
         buf.writeBoolean(true);
         buf.writeByte(packet.positionCodec.id);
         if (packet.positionCodec.isCompressed()) {
            buf.writeVarInt(packet.positionDataLength);
         }

         buf.writeVarInt(packet.positionData.length);
         buf.writeBytes(packet.positionData);
      } else {
         buf.writeBoolean(false);
      }
   }

   private static PacketReturnShapeData read(RegistryFriendlyByteBuf buf) {
      ShapeID shapeID = (ShapeID)ShapeID.STREAM_CODEC.decode(buf);
      int checksum = buf.readVarInt();
      int count = buf.readVarInt();
      int offsetY = buf.readVarInt();
      String msg = buf.readUtf();
      BlockPos dimension = (BlockPos)BlockPos.STREAM_CODEC.decode(buf);
      StatePalette statePalette = (StatePalette)StatePalette.OPTIONAL_STREAM_CODEC.decode(buf);
      byte[] positionData = null;
      PacketReturnShapeData.PositionCodec positionCodec = PacketReturnShapeData.PositionCodec.RLE;
      int positionDataLength = 0;
      if (buf.readBoolean()) {
         positionCodec = PacketReturnShapeData.PositionCodec.byId(buf.readByte());
         positionDataLength = positionCodec.isCompressed() ? buf.readVarInt() : -1;
         int size = buf.readVarInt();
         positionData = new byte[size];
         buf.readBytes(positionData);
      }

      return new PacketReturnShapeData(shapeID, checksum, positionData, positionCodec, positionDataLength, statePalette, dimension, count, offsetY, msg);
   }

   public static PacketReturnShapeData create(
      ShapeID id, int checksum, RLE positions, StatePalette statePalette, BlockPos dimension, int count, int offsetY, String msg
   ) {
      PacketReturnShapeData.EncodedPayload payload = encodePositions(positions, statePalette, dimension);
      return new PacketReturnShapeData(id, checksum, payload.payload(), payload.codec(), payload.decodedLength(), statePalette, dimension, count, offsetY, msg);
   }

   public void handle(IPayloadContext ctx) {
      ctx.enqueueWork(() -> {
         RenderData.RenderPlane plane = this.decodePlane();
         ShapeRenderer.setRenderData(this.shapeID, this.checksum, plane, this.offsetY, this.dimension.getY(), this.msg);
      });
   }

   @Nullable
   private RenderData.RenderPlane decodePlane() {
      int dx = this.dimension.getX();
      int dy = this.dimension.getY();
      int dz = this.dimension.getZ();
      if (this.positionData == null) {
         return null;
      } else {
         BlockState dummy = ((SupportBlock)BuilderModule.SUPPORT.get()).defaultBlockState();
         List<BlockState> palette = this.statePalette == null ? List.of() : this.statePalette.getPalette();
         PacketReturnShapeData.PositionReader reader = this.createPositionReader(dx * dz);
         int oy = this.offsetY;
         int y = oy - dy / 2;
         RenderData.RenderStrip[] strips = new RenderData.RenderStrip[dx];

         for (int ox = 0; ox < dx; ox++) {
            int x = ox - dx / 2;
            RenderData.RenderStrip strip = new RenderData.RenderStrip(x);
            strips[ox] = strip;

            for (int oz = 0; oz < dz; oz++) {
               int data = reader.read();
               if (data < 255) {
                  if (data == 0) {
                     strip.add(dummy);
                  } else {
                     int index = data - 1;
                     strip.add(index >= 0 && index < palette.size() ? palette.get(index) : dummy);
                  }
               } else {
                  strip.add(null);
               }
            }

            strip.close();
         }

         return new RenderData.RenderPlane(strips, y, oy, -dz / 2, this.count);
      }
   }

   private PacketReturnShapeData.PositionReader createPositionReader(int expectedCount) {
      byte[] data = this.positionCodec.isCompressed() ? decompress(this.positionData, this.positionDataLength) : this.positionData;

      return (PacketReturnShapeData.PositionReader)(switch (this.positionCodec) {
         case RLE, RLE_DEFLATE -> new PacketReturnShapeData.RleReader(data, expectedCount);
         case PACKED_BITS, PACKED_BITS_DEFLATE -> new PacketReturnShapeData.PackedBitsReader(data, expectedCount);
      });
   }

   private static PacketReturnShapeData.EncodedPayload encodePositions(RLE positions, StatePalette statePalette, BlockPos dimension) {
      ScannerConfiguration.ProjectorCompressionCodec selectedCodec = (ScannerConfiguration.ProjectorCompressionCodec)ScannerConfiguration.projectorCompressionCodec
         .get();
      boolean compareCodecs = (Boolean)ScannerConfiguration.projectorCompressionLogging.get();
      byte[] rle = positions.getData();
      if (rle.length == 0) {
         return new PacketReturnShapeData.EncodedPayload(PacketReturnShapeData.PositionCodec.RLE, rle, 0);
      } else {
         int rawLength = dimension.getX() * dimension.getZ();
         int paletteSize = statePalette == null ? 0 : statePalette.getPalette().size();
         PacketReturnShapeData.CodecFamilyStats legacy = encodeLegacyFamily(rle);
         PacketReturnShapeData.CodecFamilyStats packed = null;
         if (compareCodecs || selectedCodec == ScannerConfiguration.ProjectorCompressionCodec.PACKED_BITS) {
            packed = encodePackedFamily(rle, rawLength, paletteSize);
         }
         PacketReturnShapeData.EncodedPayload best = switch (selectedCodec) {
            case LEGACY_RLE -> legacy.best();
            case PACKED_BITS -> packed == null ? legacy.best() : packed.best();
         };
         PacketReturnShapeData.CompressionLog.record(rawLength, paletteSize, legacy, packed, best);
         return best;
      }
   }

   private static PacketReturnShapeData.CodecFamilyStats encodeLegacyFamily(byte[] rle) {
      long start = System.nanoTime();
      PacketReturnShapeData.EncodedPayload best = new PacketReturnShapeData.EncodedPayload(PacketReturnShapeData.PositionCodec.RLE, rle, rle.length);
      int deflatedBytes = -1;
      if (rle.length >= 256) {
         byte[] packed = compress(rle);
         deflatedBytes = packed.length;
         if (packed.length + 32 < rle.length) {
            best = pickBest(best, new PacketReturnShapeData.EncodedPayload(PacketReturnShapeData.PositionCodec.RLE_DEFLATE, packed, rle.length));
         }
      }

      return new PacketReturnShapeData.CodecFamilyStats(best, rle.length, deflatedBytes, System.nanoTime() - start, 8);
   }

   private static PacketReturnShapeData.CodecFamilyStats encodePackedFamily(byte[] rle, int rawLength, int paletteSize) {
      long start = System.nanoTime();
      byte[] packed = packPositions(rle, rawLength, paletteSize);
      PacketReturnShapeData.EncodedPayload best = new PacketReturnShapeData.EncodedPayload(
         PacketReturnShapeData.PositionCodec.PACKED_BITS, packed, packed.length
      );
      int deflatedBytes = -1;
      if (packed.length >= 256) {
         byte[] deflated = compress(packed);
         deflatedBytes = deflated.length;
         if (deflated.length + 32 < packed.length) {
            best = pickBest(best, new PacketReturnShapeData.EncodedPayload(PacketReturnShapeData.PositionCodec.PACKED_BITS_DEFLATE, deflated, packed.length));
         }
      }

      return new PacketReturnShapeData.CodecFamilyStats(best, packed.length, deflatedBytes, System.nanoTime() - start, packed[0] & 255);
   }

   private static byte[] packPositions(byte[] rle, int rawLength, int paletteSize) {
      int bitsPerValue = bitsRequired(paletteSize + 2);
      byte[] packed = new byte[1 + (rawLength * bitsPerValue + 7 >> 3)];
      packed[0] = (byte)bitsPerValue;
      PacketReturnShapeData.PackedBitsWriter writer = new PacketReturnShapeData.PackedBitsWriter(packed, 1);
      int produced = 0;

      for (int i = 0; i < rle.length; i += 2) {
         if (i + 1 >= rle.length) {
            throw new IllegalStateException("Corrupt RLE plane payload");
         }

         int count = rle[i] & 255;
         int symbol = packSymbol(rle[i + 1] & 255);
         writer.writeRepeated(symbol, count, bitsPerValue);
         produced += count;
      }

      writer.finish();
      if (produced != rawLength) {
         throw new IllegalStateException("Unexpected packed plane length: got " + produced + ", expected " + rawLength);
      } else {
         return packed;
      }
   }

   private static int bitsRequired(int symbolCount) {
      return symbolCount <= 1 ? 1 : 32 - Integer.numberOfLeadingZeros(symbolCount - 1);
   }

   private static int packSymbol(int value) {
      if (value == 255) {
         return 0;
      } else {
         return value == 0 ? 1 : value + 1;
      }
   }

   private static int unpackSymbol(int packedValue) {
      if (packedValue == 0) {
         return 255;
      } else {
         return packedValue == 1 ? 0 : packedValue - 1;
      }
   }

   private static PacketReturnShapeData.EncodedPayload pickBest(PacketReturnShapeData.EncodedPayload current, PacketReturnShapeData.EncodedPayload candidate) {
      return candidate.wireSize() < current.wireSize() ? candidate : current;
   }

   private static byte[] compress(byte[] data) {
      Deflater deflater = new Deflater(9);
      deflater.setInput(data);
      deflater.finish();
      byte[] buffer = new byte[1024];
      ByteArrayOutputStream out = new ByteArrayOutputStream(data.length);

      while (!deflater.finished()) {
         int len = deflater.deflate(buffer);
         out.write(buffer, 0, len);
      }

      deflater.end();
      return out.toByteArray();
   }

   private static byte[] decompress(byte[] payload, int expectedLength) {
      if (expectedLength <= 0) {
         throw new IllegalStateException("Invalid expected decompressed length for shape packet: " + expectedLength);
      } else {
         Inflater inflater = new Inflater();
         inflater.setInput(payload);
         byte[] buffer = new byte[Math.max(1024, Math.min(65536, expectedLength))];
         ByteArrayOutputStream out = new ByteArrayOutputStream(expectedLength);

         try {
            while (!inflater.finished()) {
               int len = inflater.inflate(buffer);
               if (len == 0) {
                  if (inflater.needsInput()) {
                     break;
                  }

                  if (inflater.needsDictionary()) {
                     throw new IllegalStateException("Unable to decompress shape packet (dictionary required)");
                  }
               } else {
                  out.write(buffer, 0, len);
               }
            }
         } catch (DataFormatException var9) {
            throw new IllegalStateException("Unable to decompress shape packet", var9);
         } finally {
            inflater.end();
         }

         byte[] data = out.toByteArray();
         if (data.length != expectedLength) {
            throw new IllegalStateException("Unexpected decompressed length for shape packet: got " + data.length + ", expected " + expectedLength);
         } else {
            return data;
         }
      }
   }

   private record CodecFamilyStats(PacketReturnShapeData.EncodedPayload best, int encodedBytes, int deflatedBytes, long prepNanos, int bitsPerValue) {
   }

   private static class CompressionLog {
      private static long planeCount = 0L;
      private static long totalRawBytes = 0L;
      private static long totalLegacyBytes = 0L;
      private static long totalLegacyDeflatedBytes = 0L;
      private static long totalPackedBytes = 0L;
      private static long totalPackedDeflatedBytes = 0L;
      private static long totalWireBytes = 0L;
      private static long legacyDeflatedCount = 0L;
      private static long packedCount = 0L;
      private static long packedDeflatedCount = 0L;
      private static long totalLegacyPrepNanos = 0L;
      private static long totalPackedPrepNanos = 0L;
      private static long totalPaletteSize = 0L;
      private static long totalPackedBits = 0L;
      private static final long[] wins = new long[PacketReturnShapeData.PositionCodec.values().length];

      private static synchronized void record(
         int rawBytes,
         int paletteSize,
         PacketReturnShapeData.CodecFamilyStats legacy,
         PacketReturnShapeData.CodecFamilyStats packed,
         PacketReturnShapeData.EncodedPayload best
      ) {
         if ((Boolean)ScannerConfiguration.projectorCompressionLogging.get()) {
            planeCount++;
            totalRawBytes += rawBytes;
            totalWireBytes = totalWireBytes + best.wireSize();
            if (legacy != null) {
               totalLegacyBytes = totalLegacyBytes + legacy.encodedBytes();
               totalLegacyPrepNanos = totalLegacyPrepNanos + legacy.prepNanos();
               if (legacy.deflatedBytes() >= 0) {
                  totalLegacyDeflatedBytes = totalLegacyDeflatedBytes + legacy.deflatedBytes();
                  legacyDeflatedCount++;
               }
            }

            if (packed != null) {
               totalPackedBytes = totalPackedBytes + packed.encodedBytes();
               totalPackedPrepNanos = totalPackedPrepNanos + packed.prepNanos();
               totalPaletteSize += paletteSize;
               totalPackedBits = totalPackedBits + packed.bitsPerValue();
               packedCount++;
               if (packed.deflatedBytes() >= 0) {
                  totalPackedDeflatedBytes = totalPackedDeflatedBytes + packed.deflatedBytes();
                  packedDeflatedCount++;
               }
            }

            wins[best.codec().ordinal()]++;
            int interval = Math.max(1, (Integer)ScannerConfiguration.projectorCompressionLogInterval.get());
            if (planeCount % interval == 0L) {
               long avgRaw = totalRawBytes / planeCount;
               long avgLegacy = totalLegacyBytes / planeCount;
               long avgLegacyDeflate = legacyDeflatedCount == 0L ? -1L : totalLegacyDeflatedBytes / legacyDeflatedCount;
               long avgPacked = packedCount == 0L ? -1L : totalPackedBytes / packedCount;
               long avgPackedDeflate = packedDeflatedCount == 0L ? -1L : totalPackedDeflatedBytes / packedDeflatedCount;
               long avgWire = totalWireBytes / planeCount;
               long avgLegacyPrep = totalLegacyPrepNanos / planeCount / 1000L;
               long avgPackedPrep = packedCount == 0L ? -1L : totalPackedPrepNanos / packedCount / 1000L;
               long avgPalette = packedCount == 0L ? -1L : totalPaletteSize / packedCount;
               long avgBits = packedCount == 0L ? -1L : totalPackedBits / packedCount;
               double savingsVsRaw = totalRawBytes == 0L ? 0.0 : 100.0 * (totalRawBytes - totalWireBytes) / totalRawBytes;
               RFToolsBuilder.setup
                  .getLogger()
                  .info(
                     "Projector compression over {} planes using {}: avg raw={}B, avg legacy={}B, avg legacy+deflate={}B, avg packed={}B, avg packed+deflate={}B, avg chosen wire={}B, avg palette={}, avg bits={}, savings vs raw={}%, avg prep[legacy={}us, packed={}us], wins[legacy={}, legacy+deflate={}, packed={}, packed+deflate={}]",
                     new Object[]{
                        planeCount,
                        ScannerConfiguration.projectorCompressionCodec.get(),
                        avgRaw,
                        avgLegacy,
                        avgLegacyDeflate < 0L ? "n/a" : Long.toString(avgLegacyDeflate),
                        avgPacked < 0L ? "n/a" : Long.toString(avgPacked),
                        avgPackedDeflate < 0L ? "n/a" : Long.toString(avgPackedDeflate),
                        avgWire,
                        avgPalette < 0L ? "n/a" : Long.toString(avgPalette),
                        avgBits < 0L ? "n/a" : Long.toString(avgBits),
                        String.format("%.1f", savingsVsRaw),
                        avgLegacyPrep,
                        avgPackedPrep < 0L ? "n/a" : Long.toString(avgPackedPrep),
                        wins[PacketReturnShapeData.PositionCodec.RLE.ordinal()],
                        wins[PacketReturnShapeData.PositionCodec.RLE_DEFLATE.ordinal()],
                        wins[PacketReturnShapeData.PositionCodec.PACKED_BITS.ordinal()],
                        wins[PacketReturnShapeData.PositionCodec.PACKED_BITS_DEFLATE.ordinal()]
                     }
                  );
               planeCount = 0L;
               totalRawBytes = 0L;
               totalLegacyBytes = 0L;
               totalLegacyDeflatedBytes = 0L;
               totalPackedBytes = 0L;
               totalPackedDeflatedBytes = 0L;
               totalWireBytes = 0L;
               legacyDeflatedCount = 0L;
               packedCount = 0L;
               packedDeflatedCount = 0L;
               totalLegacyPrepNanos = 0L;
               totalPackedPrepNanos = 0L;
               totalPaletteSize = 0L;
               totalPackedBits = 0L;

               for (int i = 0; i < wins.length; i++) {
                  wins[i] = 0L;
               }
            }
         }
      }
   }

   private record EncodedPayload(PacketReturnShapeData.PositionCodec codec, byte[] payload, int decodedLength) {
      private int wireSize() {
         int size = 1 + PacketReturnShapeData.varIntSize(this.payload.length) + this.payload.length;
         if (this.codec.isCompressed()) {
            size += PacketReturnShapeData.varIntSize(this.decodedLength);
         }

         return size;
      }
   }

   private static class PackedBitsReader implements PacketReturnShapeData.PositionReader {
      private final byte[] data;
      private final int expectedCount;
      private final int bitsPerValue;
      private final int mask;
      private int index = 1;
      private long buffer = 0L;
      private int bufferedBits = 0;
      private int produced = 0;

      private PackedBitsReader(byte[] data, int expectedCount) {
         if (data.length == 0) {
            throw new IllegalStateException("Corrupt packed plane payload");
         } else {
            this.data = data;
            this.expectedCount = expectedCount;
            this.bitsPerValue = data[0] & 255;
            if (this.bitsPerValue > 0 && this.bitsPerValue <= 30) {
               this.mask = (1 << this.bitsPerValue) - 1;
            } else {
               throw new IllegalStateException("Invalid packed plane bit width: " + this.bitsPerValue);
            }
         }
      }

      @Override
      public int read() {
         if (this.produced >= this.expectedCount) {
            return 0;
         } else {
            while (this.bufferedBits < this.bitsPerValue) {
               if (this.index >= this.data.length) {
                  throw new IllegalStateException("Corrupt packed plane payload");
               }

               this.buffer = this.buffer | (long)(this.data[this.index++] & 255) << this.bufferedBits;
               this.bufferedBits += 8;
            }

            int symbol = (int)(this.buffer & this.mask);
            this.buffer = this.buffer >>> this.bitsPerValue;
            this.bufferedBits = this.bufferedBits - this.bitsPerValue;
            this.produced++;
            return PacketReturnShapeData.unpackSymbol(symbol);
         }
      }
   }

   private static class PackedBitsWriter {
      private final byte[] data;
      private int index;
      private long buffer = 0L;
      private int bufferedBits = 0;

      private PackedBitsWriter(byte[] data, int index) {
         this.data = data;
         this.index = index;
      }

      private void writeRepeated(int value, int count, int bitsPerValue) {
         for (int i = 0; i < count; i++) {
            this.write(value, bitsPerValue);
         }
      }

      private void write(int value, int bitsPerValue) {
         this.buffer = this.buffer | (long)value << this.bufferedBits;

         for (this.bufferedBits += bitsPerValue; this.bufferedBits >= 8; this.bufferedBits -= 8) {
            this.data[this.index++] = (byte)(this.buffer & 255L);
            this.buffer >>>= 8;
         }
      }

      private void finish() {
         if (this.bufferedBits > 0) {
            this.data[this.index++] = (byte)(this.buffer & 255L);
         }
      }
   }

   public static enum PositionCodec {
      RLE(0, false),
      RLE_DEFLATE(1, true),
      PACKED_BITS(2, false),
      PACKED_BITS_DEFLATE(3, true);

      private final int id;
      private final boolean compressed;

      private PositionCodec(int id, boolean compressed) {
         this.id = id;
         this.compressed = compressed;
      }

      public boolean isCompressed() {
         return this.compressed;
      }

      public static PacketReturnShapeData.PositionCodec byId(int id) {
         for (PacketReturnShapeData.PositionCodec codec : values()) {
            if (codec.id == id) {
               return codec;
            }
         }

         throw new IllegalArgumentException("Unknown position codec id: " + id);
      }
   }

   private interface PositionReader {
      int read();
   }

   private static class RleReader implements PacketReturnShapeData.PositionReader {
      private final byte[] data;
      private final int expectedCount;
      private int index = 0;
      private int count = 0;
      private int value = 0;
      private int produced = 0;

      private RleReader(byte[] data, int expectedCount) {
         this.data = data;
         this.expectedCount = expectedCount;
      }

      @Override
      public int read() {
         if (this.produced >= this.expectedCount) {
            return 0;
         } else {
            if (this.count == 0) {
               if (this.index + 1 >= this.data.length) {
                  throw new IllegalStateException("Corrupt RLE plane payload");
               }

               this.count = this.data[this.index++] & 255;
               this.value = this.data[this.index++] & 255;
            }

            this.count--;
            this.produced++;
            return this.value;
         }
      }
   }
}
