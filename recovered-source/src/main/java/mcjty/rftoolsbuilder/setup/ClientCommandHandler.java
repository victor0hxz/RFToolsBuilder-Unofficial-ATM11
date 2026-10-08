package mcjty.rftoolsbuilder.setup;

import java.util.List;
import mcjty.lib.McJtyLib;
import mcjty.lib.typed.Key;
import mcjty.lib.typed.Type;
import mcjty.lib.varia.SafeClientTools;
import mcjty.rftoolsbuilder.modules.builder.blocks.BuilderTileEntity;
import mcjty.rftoolsbuilder.shapes.ScanDataManagerClient;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ClientCommandHandler {
   public static final String CMD_RETURN_SCANNER_CONTENTS = "returnScannerContents";
   public static final Key<List<ItemStack>> PARAM_STACKS = new Key("stacks", Type.ITEMSTACK_LIST);
   public static final String CMD_RETURN_SCANNER_SEARCH = "returnScannerSearch";
   public static final Key<List<BlockPos>> PARAM_INVENTORIES = new Key("inventories", Type.POS_LIST);
   public static final Key<Integer> PARAM_ID = new Key("id", Type.INTEGER);
   public static final Key<String> PARAM_NAME = new Key("name", Type.STRING);
   public static final String CMD_RETURN_SCAN_DIRTY = "returnScanDirty";
   public static final Key<Integer> PARAM_SCANID = new Key("scanid", Type.INTEGER);
   public static final Key<Integer> PARAM_COUNTER = new Key("counter", Type.INTEGER);
   public static final String CMD_RETURN_ENERGY_CONSUMPTION = "returnEnergyConsumption";
   public static final Key<Integer> PARAM_ENERGY = new Key("energy", Type.INTEGER);
   public static final String CMD_POSITION_TO_CLIENT = "positionToClient";
   public static final Key<BlockPos> PARAM_POS = new Key("pos", Type.BLOCKPOS);
   public static final Key<BlockPos> PARAM_SCAN = new Key("scan", Type.BLOCKPOS);

   public static void registerCommands() {
      McJtyLib.registerClientCommand("rftoolsbuilder", "returnScanDirty", (player, arguments) -> {
         ScanDataManagerClient.getScansClient().getOrCreateScan((Integer)arguments.get(PARAM_SCANID)).setDirtyCounter((Integer)arguments.get(PARAM_COUNTER));
         return true;
      });
      McJtyLib.registerClientCommand("rftoolsbuilder", "positionToClient", (player, arguments) -> {
         BlockPos tePos = (BlockPos)arguments.get(PARAM_POS);
         BlockPos scanPos = (BlockPos)arguments.get(PARAM_SCAN);
         BlockEntity te = SafeClientTools.getClientWorld().getBlockEntity(tePos);
         if (te instanceof BuilderTileEntity) {
            BuilderTileEntity.setScanLocationClient(tePos, scanPos);
         }

         return true;
      });
   }
}
