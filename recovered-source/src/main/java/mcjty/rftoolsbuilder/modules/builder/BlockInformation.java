package mcjty.rftoolsbuilder.modules.builder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import mcjty.lib.varia.Tools;
import mcjty.rftoolsbuilder.modules.builder.blocks.SupportBlock;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

public class BlockInformation {
   private static Map<Identifier, BlockInformation> blockInformationMap = null;
   private static Map<Identifier, Boolean> blackWhiteListedNBTBlocksMap = null;
   private final Identifier blockName;
   private final SupportBlock.SupportStatus blockLevel;
   private final double costFactor;
   private final int rotateInfo;
   public static final int ROTATE_invalid = -1;
   public static final int ROTATE_mmmm = 0;
   public static final int ROTATE_mfff = 1;
   public static final BlockInformation INVALID = new BlockInformation(null, SupportBlock.SupportStatus.STATUS_ERROR, 1.0);
   public static final BlockInformation OK = new BlockInformation(null, SupportBlock.SupportStatus.STATUS_OK, 1.0, 0);
   public static final BlockInformation FREE = new BlockInformation(null, SupportBlock.SupportStatus.STATUS_OK, 0.0, 0);

   private static int rotateStringToId(String rotateString) {
      if ("mmmm".equals(rotateString)) {
         return 0;
      } else {
         return "mfff".equals(rotateString) ? 1 : -1;
      }
   }

   private static void initBlackWhiteListedNBTBlocksMap() {
      if (blackWhiteListedNBTBlocksMap == null) {
         blackWhiteListedNBTBlocksMap = new HashMap<>();
         List<? extends String> blocks = (List<? extends String>)BuilderConfiguration.blackWhiteListedNBTBlocks.get();
         Boolean whitelist = false;

         for (String block : blocks) {
            if (block.contains("=")) {
               String[] split = block.split("=");
               block = split[0];
               whitelist = Boolean.valueOf(split[1]);
            }

            Identifier id = Identifier.parse(block);
            blackWhiteListedNBTBlocksMap.put(id, whitelist);
         }
      }
   }

   private static void initMap() {
      if (blockInformationMap == null) {
         blockInformationMap = new HashMap<>();

         for (String block : (List)BuilderConfiguration.blackWhiteListedBlocks.get()) {
            String costS = "1.0f";
            if (block.contains("=")) {
               String[] split = block.split("=");
               block = split[0];
               costS = split[1];
            }

            double cost = Double.parseDouble(costS);
            Identifier id = Identifier.parse(block);
            if (BuilderConfiguration.teMode.get() == BuilderTileEntityMode.MOVE_BLACKLIST) {
               blockInformationMap.put(id, new BlockInformation(id, SupportBlock.SupportStatus.STATUS_ERROR, cost));
            } else if (BuilderConfiguration.teMode.get() == BuilderTileEntityMode.MOVE_WHITELIST) {
               blockInformationMap.put(id, new BlockInformation(id, SupportBlock.SupportStatus.STATUS_OK, cost));
            }
         }
      }
   }

   public BlockInformation(Identifier blockName, SupportBlock.SupportStatus blockLevel, double costFactor) {
      this.blockName = blockName;
      this.blockLevel = blockLevel;
      this.costFactor = costFactor;
      this.rotateInfo = 0;
   }

   public BlockInformation(Identifier blockName, SupportBlock.SupportStatus blockLevel, double costFactor, int rotateInfo) {
      this.blockName = blockName;
      this.blockLevel = blockLevel;
      this.costFactor = costFactor;
      this.rotateInfo = rotateInfo;
   }

   public BlockInformation(BlockInformation other, String rotateInfo) {
      this(other.blockName, other.blockLevel, other.costFactor, rotateStringToId(rotateInfo));
   }

   public BlockInformation(BlockInformation other, Identifier blockName, SupportBlock.SupportStatus blockLevel, double costFactor) {
      this(blockName, blockLevel, costFactor, other.rotateInfo);
   }

   @Nullable
   public static BlockInformation getBlockInformation(Block block) {
      initMap();
      return blockInformationMap.get(Tools.getId(block));
   }

   public static boolean shouldTransferNBT(Block block) {
      initBlackWhiteListedNBTBlocksMap();
      return blackWhiteListedNBTBlocksMap.getOrDefault(Tools.getId(block), false);
   }

   public SupportBlock.SupportStatus getBlockLevel() {
      return this.blockLevel;
   }

   public Identifier getBlockName() {
      return this.blockName;
   }

   public double getCostFactor() {
      return this.costFactor;
   }

   public int getRotateInfo() {
      return this.rotateInfo;
   }
}
