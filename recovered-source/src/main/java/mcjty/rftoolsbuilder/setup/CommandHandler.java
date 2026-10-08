package mcjty.rftoolsbuilder.setup;

import mcjty.lib.McJtyLib;
import mcjty.lib.typed.Key;
import mcjty.lib.typed.Type;
import mcjty.rftoolsbuilder.modules.builder.BuilderTools;
import mcjty.rftoolsbuilder.modules.mover.blocks.MoverTileEntity;
import mcjty.rftoolsbuilder.shapes.ShaperTools;
import net.minecraft.core.BlockPos;

public class CommandHandler {
   public static final String CMD_REQUEST_SHAPE_DATA = "requestShapeData";
   public static final String CMD_REQUEST_SCAN_DIRTY = "requestScanDirty";
   public static final String CMD_REQUEST_LOCATOR_ENERGY = "requestLocatorEnergy";
   public static final String CMD_GET_CHAMBER_INFO = "getChamberInfo";
   public static final Key<BlockPos> PARAM_POS = new Key("pos", Type.BLOCKPOS);
   public static final String CMD_UNMOUNT = "unmount";
   public static final String CMD_GET_SECURITY_INFO = "getSecurityInfo";
   public static final Key<Integer> PARAM_ID = new Key("id", Type.INTEGER);

   public static void registerCommands() {
      McJtyLib.registerCommand("rftoolsbuilder", "requestShapeData", (player, arguments) -> {
         ShaperTools.requestExtraShapeData(player, (Integer)arguments.get(PARAM_ID));
         return true;
      });
      McJtyLib.registerCommand("rftoolsbuilder", "requestScanDirty", (player, arguments) -> {
         ShaperTools.requestScanDirty(player, (Integer)arguments.get(PARAM_ID));
         return true;
      });
      McJtyLib.registerCommand("rftoolsbuilder", "requestLocatorEnergy", (player, arguments) -> {
         ShaperTools.requestLocatorEnergyConsumption(player, (BlockPos)arguments.get(PARAM_POS));
         return true;
      });
      McJtyLib.registerCommand("rftoolsbuilder", "getChamberInfo", (player, arguments) -> {
         BuilderTools.returnChamberInfo(player);
         return true;
      });
      McJtyLib.registerCommand("rftoolsbuilder", "unmount", (player, arguments) -> {
         MoverTileEntity.wantUnmount.add(player.getId());
         return true;
      });
   }
}
