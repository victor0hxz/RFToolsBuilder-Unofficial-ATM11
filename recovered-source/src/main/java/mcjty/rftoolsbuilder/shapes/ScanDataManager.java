package mcjty.rftoolsbuilder.shapes;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import mcjty.lib.varia.ComponentFactory;
import mcjty.lib.varia.LevelTools;
import mcjty.lib.varia.Logging;
import mcjty.lib.worlddata.AbstractWorldData;
import mcjty.rftoolsbuilder.modules.scanner.ScannerConfiguration;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;

public class ScanDataManager extends AbstractWorldData<ScanDataManager> {
   private static final String SCANDATA_NETWORK_NAME = "RFToolsScanData";
   private int lastId = 0;
   private final Map<Integer, Scan> scans = new HashMap<>();
   private final Map<Integer, ScanExtraData> scanData = new HashMap<>();

   public ScanDataManager() {
   }

   public ScanDataManager(CompoundTag tag) {
      this.scans.clear();
      ListTag lst = tag.getListOrEmpty("scans");

      for (int i = 0; i < lst.size(); i++) {
         CompoundTag tc = lst.getCompoundOrEmpty(i);
         int id = tc.getIntOr("scan", 0);
         Scan scan = new Scan();
         scan.readFromNBT(tc);
         this.scans.put(id, scan);
      }

      this.lastId = tag.getIntOr("lastId", 0);
   }

   public void save(Level w, int scanId) {
      Level world = LevelTools.getOverworld(w);
      File dataDir = world.getServer().getWorldPath(LevelResource.ROOT).resolve("rftoolsscans").toFile();
      dataDir.mkdirs();
      File file = new File(dataDir, "scan" + scanId);
      Scan scan = this.getOrCreateScan(scanId);
      CompoundTag tc = new CompoundTag();
      scan.writeToNBTExternal(tc);

      try (DataOutputStream dataoutputstream = new DataOutputStream(new FileOutputStream(file))) {
         NbtIo.writeCompressed(tc, dataoutputstream);
      } catch (IOException var13) {
         throw new UncheckedIOException("Error writing to file 'scan" + scan + "'!", var13);
      }

      this.save();
   }

   public ScanExtraData getExtraData(int id) {
      ScanExtraData data = this.scanData.get(id);
      if (data == null) {
         data = new ScanExtraData();
         this.scanData.put(id, data);
      } else if (data.getBirthTime() + (Integer)ScannerConfiguration.ticksPerLocatorScan.get() * 100 < System.currentTimeMillis()) {
         data = new ScanExtraData();
         this.scanData.put(id, data);
      }

      return data;
   }

   public static ScanDataManager get(Level world) {
      return (ScanDataManager)getData(world, ScanDataManager::new, ScanDataManager::new, "RFToolsScanData");
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

   @Nonnull
   public Scan loadScan(Level w, int id) {
      Level world = LevelTools.getOverworld(w);
      Scan scan = this.scans.get(id);
      if (scan == null || scan.getDataInt() == null) {
         if (scan == null) {
            scan = new Scan();
         }

         File dataDir = world.getServer().getWorldPath(LevelResource.ROOT).resolve("rftoolsscans").toFile();
         dataDir.mkdirs();
         File file = new File(dataDir, "scan" + id);
         if (file.exists()) {
            try (DataInputStream datainputstream = new DataInputStream(new FileInputStream(file))) {
               CompoundTag tag = NbtIo.readCompressed(datainputstream, NbtAccounter.unlimitedHeap());
               scan.readFromNBTExternal(tag, world.registryAccess());
            } catch (IOException var12) {
               Logging.log("Error reading scan file for id: " + id);
            }
         }
      }

      return scan;
   }

   public static void listScans(Player sender) {
      ScanDataManager scans = get(sender.level());

      for (Entry<Integer, Scan> entry : scans.scans.entrySet()) {
         Integer scanid = entry.getKey();
         scans.loadScan(sender.level(), scanid);
         Scan scan = entry.getValue();
         BlockPos dim = scan.getDataDim();
         if (dim == null) {
            sender.sendSystemMessage(
               ComponentFactory.literal(ChatFormatting.YELLOW + "Scan: " + ChatFormatting.WHITE + scanid + ChatFormatting.RED + "   Invalid")
            );
         } else {
            sender.sendSystemMessage(
               ComponentFactory.literal(
                  ChatFormatting.YELLOW
                     + "Scan: "
                     + ChatFormatting.WHITE
                     + scanid
                     + ChatFormatting.YELLOW
                     + "   Dim: "
                     + ChatFormatting.WHITE
                     + dim.getX()
                     + ","
                     + dim.getY()
                     + ","
                     + dim.getZ()
                     + ChatFormatting.YELLOW
                     + "   Size: "
                     + ChatFormatting.WHITE
                     + scan.getRledata().length
                     + " bytes"
               )
            );
         }
      }
   }

   public int newScan(Level world) {
      this.lastId++;
      this.save();
      return this.lastId;
   }

   public CompoundTag save(CompoundTag tagCompound, Provider provider) {
      ListTag lst = new ListTag();

      for (Entry<Integer, Scan> entry : this.scans.entrySet()) {
         CompoundTag tc = new CompoundTag();
         tc.putInt("scan", entry.getKey());
         entry.getValue().writeToNBT(tc);
         lst.add(tc);
      }

      tagCompound.put("scans", lst);
      tagCompound.putInt("lastId", this.lastId);
      return tagCompound;
   }
}
