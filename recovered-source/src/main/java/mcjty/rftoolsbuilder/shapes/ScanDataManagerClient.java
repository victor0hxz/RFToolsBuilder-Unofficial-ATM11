package mcjty.rftoolsbuilder.shapes;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import mcjty.lib.typed.TypedMap;
import mcjty.rftoolsbuilder.setup.CommandHandler;
import mcjty.rftoolsbuilder.setup.RFToolsBuilderMessages;

public class ScanDataManagerClient {
   private static ScanDataManagerClient instance = null;
   private final Map<Integer, Scan> scans = new HashMap<>();
   private final Map<Integer, ScanExtraData> scanDataClient = new HashMap<>();

   public static ScanDataManagerClient getScansClient() {
      if (instance == null) {
         instance = new ScanDataManagerClient();
      }

      return instance;
   }

   @Nonnull
   public Scan getOrCreateScan(int id) {
      Scan scan = this.scans.get(id);
      if (scan == null) {
         scan = new Scan();
         this.scans.put(id, scan);
      }

      return scan;
   }

   public ScanExtraData getExtraDataClient(int id) {
      ScanExtraData data = this.scanDataClient.get(id);
      if (data == null) {
         data = new ScanExtraData();
         this.scanDataClient.put(id, data);
      }

      return data;
   }

   public void requestExtraDataClient(int id) {
      RFToolsBuilderMessages.sendToServer("requestShapeData", TypedMap.builder().put(CommandHandler.PARAM_ID, id));
   }

   public void registerExtraDataFromServer(int id, ScanExtraData extraData) {
      this.scanDataClient.put(id, extraData);
   }

   public int getScanDirtyCounterClient(int id) {
      Scan scan;
      if (!this.scans.containsKey(id)) {
         scan = new Scan();
         this.scans.put(id, scan);
      } else {
         scan = this.scans.get(id);
      }

      scan.dirtyRequestTimeout--;
      if (scan.dirtyRequestTimeout <= 0) {
         RFToolsBuilderMessages.sendToServer("requestScanDirty", TypedMap.builder().put(CommandHandler.PARAM_ID, id));
         scan.dirtyRequestTimeout = 20;
      }

      return scan.getDirtyCounter();
   }
}
