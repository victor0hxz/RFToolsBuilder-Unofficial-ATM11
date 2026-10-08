package mcjty.rftoolsbuilder.modules.scanner;

import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.neoforge.common.ModConfigSpec.Builder;
import net.neoforged.neoforge.common.ModConfigSpec.DoubleValue;
import net.neoforged.neoforge.common.ModConfigSpec.EnumValue;
import net.neoforged.neoforge.common.ModConfigSpec.IntValue;

public class ScannerConfiguration {
   public static final String CATEGORY_SCANNER = "scanner";
   public static IntValue SCANNER_MAXENERGY;
   public static IntValue SCANNER_RECEIVEPERTICK;
   public static IntValue SCANNER_PERTICK;
   public static IntValue REMOTE_SCANNER_PERTICK;
   public static IntValue LOCATOR_MAXENERGY;
   public static IntValue LOCATOR_RECEIVEPERTICK;
   public static IntValue LOCATOR_PERSCAN_BASE;
   public static DoubleValue LOCATOR_PERSCAN_CHUNK;
   public static DoubleValue LOCATOR_PERSCAN_HOSTILE;
   public static DoubleValue LOCATOR_PERSCAN_PASSIVE;
   public static DoubleValue LOCATOR_PERSCAN_PLAYER;
   public static DoubleValue LOCATOR_PERSCAN_ENERGY;
   public static DoubleValue LOCATOR_FILTER_COST;
   public static IntValue ticksPerLocatorScan;
   public static IntValue locatorBeaconHeight;
   public static IntValue locatorEntitySafety;
   public static IntValue locatorMaxEnergyChunks;
   public static IntValue PROJECTOR_MAXENERGY;
   public static IntValue PROJECTOR_RECEIVEPERTICK;
   public static IntValue PROJECTOR_USEPERTICK;
   public static IntValue maxScannerDimension;
   public static IntValue maxScannerOffset;
   public static IntValue surfaceAreaPerTick;
   public static IntValue planeSurfacePerTick;
   public static IntValue projectorPlaneSendInterval;
   public static IntValue clientRenderDataTimeout;
   public static IntValue projectorFlashTimeout;
   public static BooleanValue projectorCompressionLogging;
   public static IntValue projectorCompressionLogInterval;
   public static EnumValue<ScannerConfiguration.ProjectorCompressionCodec> projectorCompressionCodec;
   public static DoubleValue baseProjectorVolume;

   public static void init(Builder SERVER_BUILDER, Builder CLIENT_BUILDER) {
      SERVER_BUILDER.comment("Settings for the scanner, composer, and projector").push("scanner");
      CLIENT_BUILDER.comment("Settings for the scanner, composer, and projector").push("scanner");
      SCANNER_MAXENERGY = SERVER_BUILDER.comment("Maximum RF storage that the scanner can hold").defineInRange("scannerMaxRF", 500000, 0, Integer.MAX_VALUE);
      SCANNER_RECEIVEPERTICK = SERVER_BUILDER.comment("RF per tick that the scanner can receive")
         .defineInRange("scannerRFPerTick", 20000, 0, Integer.MAX_VALUE);
      SCANNER_PERTICK = SERVER_BUILDER.comment("Amount of RF needed per tick during the scan").defineInRange("scannerUsePerTick", 1000, 0, Integer.MAX_VALUE);
      REMOTE_SCANNER_PERTICK = SERVER_BUILDER.comment("Amount of RF needed per tick during the scan for a remote scanner")
         .defineInRange("remoteScannerUsePerTick", 2000, 0, Integer.MAX_VALUE);
      LOCATOR_MAXENERGY = SERVER_BUILDER.comment("Maximum RF storage that the locator can hold").defineInRange("locatorMaxRF", 2000000, 0, Integer.MAX_VALUE);
      LOCATOR_RECEIVEPERTICK = SERVER_BUILDER.comment("RF per tick that the locator can receive")
         .defineInRange("locatorRFPerTick", 20000, 0, Integer.MAX_VALUE);
      LOCATOR_PERSCAN_BASE = SERVER_BUILDER.comment("Fixed amount of RF needed for a scan").defineInRange("locatorUsePerTickBase", 5000, 0, Integer.MAX_VALUE);
      LOCATOR_PERSCAN_CHUNK = SERVER_BUILDER.comment("Base amount of RF needed for a scan per 16x16x16 subchunk")
         .defineInRange("locatorUsePerTickChunk", 0.1, 0.0, 1.0E9);
      LOCATOR_PERSCAN_HOSTILE = SERVER_BUILDER.comment("Additional amount of RF per 16x16x16 subchunk needed for a scan for hostile entities")
         .defineInRange("locatorUsePerTickHostile", 1.0, 0.0, 1.0E9);
      LOCATOR_PERSCAN_PASSIVE = SERVER_BUILDER.comment("Additional amount of RF per 16x16x16 subchunk needed for a scan for passive entities")
         .defineInRange("locatorUsePerTickPassive", 0.5, 0.0, 1.0E9);
      LOCATOR_PERSCAN_PLAYER = SERVER_BUILDER.comment("Additional amount of RF per 16x16x16 subchunk needed for a scan for players")
         .defineInRange("locatorUsePerTickPlayer", 2.0, 0.0, 1.0E9);
      LOCATOR_PERSCAN_ENERGY = SERVER_BUILDER.comment("Additional amount of RF per 16x16x16 subchunk needed for a scan for low energy")
         .defineInRange("locatorUsePerTickEnergy", 5.0, 0.0, 1.0E9);
      LOCATOR_FILTER_COST = SERVER_BUILDER.comment("Additional amount of RF per 16x16x16 subchunk needed for a filtered scan")
         .defineInRange("locatorFilterCost", 0.5, 0.0, 1.0E9);
      PROJECTOR_MAXENERGY = SERVER_BUILDER.comment("Maximum RF storage that the projector can hold")
         .defineInRange("projectorMaxRF", 500000, 0, Integer.MAX_VALUE);
      PROJECTOR_RECEIVEPERTICK = SERVER_BUILDER.comment("RF per tick that the projector can receive")
         .defineInRange("projectorRFPerTick", 10000, 0, Integer.MAX_VALUE);
      PROJECTOR_USEPERTICK = SERVER_BUILDER.comment("RF/t for the projector while it is in use")
         .defineInRange("projectorUsePerTick", 1000, 0, Integer.MAX_VALUE);
      ticksPerLocatorScan = SERVER_BUILDER.comment("Number of ticks between every scan of the locator")
         .defineInRange("ticksPerLocatorScan", 40, 0, Integer.MAX_VALUE);
      locatorBeaconHeight = CLIENT_BUILDER.comment("Height of the beacon in case beacons are used")
         .defineInRange("locatorBeaconHeight", 30, 0, Integer.MAX_VALUE);
      locatorEntitySafety = SERVER_BUILDER.comment("Maximum amount of entities in a single block to show markers/beacons for")
         .defineInRange("locatorEntitySafety", 10, 0, Integer.MAX_VALUE);
      locatorMaxEnergyChunks = SERVER_BUILDER.comment("Maximum amount of 16x16 chunks we support for energy scanning")
         .defineInRange("locatorMaxEnergyChunks", 25, 0, Integer.MAX_VALUE);
      maxScannerOffset = SERVER_BUILDER.comment("Maximum offset of the shape when a shape card is used in the scanner/projector")
         .defineInRange("maxScannerOffset", 2048, 0, Integer.MAX_VALUE);
      maxScannerDimension = SERVER_BUILDER.comment("Maximum dimension of the shape when a scanner/projector card is used")
         .defineInRange("maxScannerDimension", 512, 0, 10000);
      surfaceAreaPerTick = SERVER_BUILDER.comment(
            "The amount of surface area the scanner will scan in a tick. Increasing this will increase the speed of the scanner but cause more strain on the server"
         )
         .defineInRange("surfaceAreaPerTick", 262144, 100, 1073741824);
      planeSurfacePerTick = SERVER_BUILDER.comment(
            "The amount of 'surface area' that the server will send to the client for the projector. Increasing this will increase the speed at which projections are ready but also increase the load for server and client"
         )
         .defineInRange("planeSurfacePerTick", 40000, 100, 10000000);
      projectorPlaneSendInterval = SERVER_BUILDER.comment(
            "How many ticks to wait before sending the next projector preview plane to clients. Increase this to spread setup load over time"
         )
         .defineInRange("projectorPlaneSendInterval", 5, 1, 200);
      clientRenderDataTimeout = CLIENT_BUILDER.comment(
            "The amount of milliseconds before the client will remove shape render data that hasn't been used. Decreasing this will free memory faster at the cost of having to update shape renders more often"
         )
         .defineInRange("clientRenderDataTimeout", 10000, 100, 1000000);
      projectorFlashTimeout = CLIENT_BUILDER.comment("The amount of milliseconds that a scanline 'flash' will exist on the client")
         .defineInRange("projectorFlashTimeout", 400, 10, 1000000);
      projectorCompressionLogging = SERVER_BUILDER.comment(
            "Log aggregated statistics about projector preview packet compression. Intended as a diagnostic for scan/blockstate-heavy previews"
         )
         .define("projectorCompressionLogging", true);
      projectorCompressionLogInterval = SERVER_BUILDER.comment("How many projector preview planes to aggregate before logging one compression summary line")
         .defineInRange("projectorCompressionLogInterval", 64, 1, 100000);
      projectorCompressionCodec = SERVER_BUILDER.comment(
            "Compression codec to use for projector preview planes. LEGACY_RLE is the previous path, PACKED_BITS uses palette-sized bit packing for comparison"
         )
         .defineEnum(
            "projectorCompressionCodec", ScannerConfiguration.ProjectorCompressionCodec.LEGACY_RLE, ScannerConfiguration.ProjectorCompressionCodec.values()
         );
      baseProjectorVolume = CLIENT_BUILDER.comment("The volume for the projector sound (0.0 is off)").defineInRange("baseProjectorVolume", 0.4, 0.0, 1.0);
      SERVER_BUILDER.pop();
      CLIENT_BUILDER.pop();
   }

   public static enum ProjectorCompressionCodec {
      LEGACY_RLE,
      PACKED_BITS;
   }
}
