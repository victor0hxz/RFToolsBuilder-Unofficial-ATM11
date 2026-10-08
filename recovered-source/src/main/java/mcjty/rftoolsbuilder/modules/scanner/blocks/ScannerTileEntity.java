package mcjty.rftoolsbuilder.modules.scanner.blocks;

import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import mcjty.lib.api.container.DefaultContainerProvider;
import mcjty.lib.api.container.ItemInventory;
import mcjty.lib.blockcommands.Command;
import mcjty.lib.blockcommands.ServerCommand;
import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.GenericItemHandler;
import mcjty.lib.container.SlotDefinition;
import mcjty.lib.setup.Registration;
import mcjty.lib.tileentity.Cap;
import mcjty.lib.tileentity.CapType;
import mcjty.lib.tileentity.TickingTileEntity;
import mcjty.lib.typed.Key;
import mcjty.lib.typed.Type;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.Cached;
import mcjty.lib.varia.RLE;
import mcjty.lib.varia.RedstoneMode;
import mcjty.rftoolsbase.modules.filter.items.FilterModuleItem;
import mcjty.rftoolsbuilder.compat.CompatNbt;
import mcjty.rftoolsbuilder.modules.builder.BuilderModule;
import mcjty.rftoolsbuilder.modules.builder.blocks.SupportBlock;
import mcjty.rftoolsbuilder.modules.builder.items.ShapeCardItem;
import mcjty.rftoolsbuilder.modules.scanner.ScannerConfiguration;
import mcjty.rftoolsbuilder.modules.scanner.ScannerModule;
import mcjty.rftoolsbuilder.shapes.ScanDataManager;
import mcjty.rftoolsbuilder.shapes.Shape;
import mcjty.rftoolsbuilder.shapes.StatePalette;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponentMap.Builder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.Lazy;

public class ScannerTileEntity extends TickingTileEntity {
   public static final String CMD_SCAN_ID = "scanner.scan";
   public static final String CMD_OFFSET_ID = "scanner.offset";
   public static final Key<BlockPos> PARAM_OFFSET = new Key("offset", Type.BLOCKPOS);
   public static final int SLOT_IN = 0;
   public static final int SLOT_OUT = 1;
   public static final int SLOT_FILTER = 2;
   public static final int SLOT_MODIFIER = 3;
   public static final Lazy<ContainerFactory> CONTAINER_FACTORY = Lazy.of(
      () -> new ContainerFactory(4)
         .slot(SlotDefinition.specific(s -> s.getItem() instanceof ShapeCardItem).in().out(), 0, 15, 7)
         .slot(SlotDefinition.specific(s -> s.getItem() instanceof ShapeCardItem).in().out(), 1, 15, 200)
         .slot(SlotDefinition.specific(s -> s.getItem() instanceof FilterModuleItem).in().out(), 2, 35, 7)
         .slot(SlotDefinition.specific(s -> true).in().out(), 3, 55, 7)
         .playerSlots(85, 142)
   );
   private final GenericItemHandler items = GenericItemHandler.create(this, CONTAINER_FACTORY).itemValid((slot, stack) -> {
      return switch (slot) {
         case 0, 1 -> stack.getItem() instanceof ShapeCardItem;
         case 2 -> stack.getItem() instanceof FilterModuleItem;
         case 3 -> true;
         default -> false;
      };
   }).onUpdate(this::handleSlotUpdate).build();
   @Cap(type = CapType.ITEMS_AUTOMATION)
   private static final Function<ScannerTileEntity, GenericItemHandler> ITEM_CAP = tile -> tile.items;
   @Cap(type = CapType.CONTAINER)
   private static final Function<ScannerTileEntity, MenuProvider> SCREEN_CAP = tile -> new DefaultContainerProvider("Scanner")
      .containerSupplier(DefaultContainerProvider.container(ScannerModule.CONTAINER_SCANNER, CONTAINER_FACTORY, tile))
      .itemHandler(() -> tile.items)
      .setupSync(tile);
   private final Cached<Predicate<ItemStack>> filterCache = Cached.of(this::createFilterCache);
   private int scanId = 0;
   private ItemStack renderStack = ItemStack.EMPTY;
   private BlockPos dataDim = new BlockPos(5, 5, 5);
   private BlockPos dataOffset = BlockPos.ZERO;
   private ScannerTileEntity.ScanProgress progress = null;
   private int progressBusy = -1;
   @ServerCommand
   public static final Command<?> CMD_SCAN = Command.create("scanner.scan", (te, player, params) -> te.scan());
   @ServerCommand
   public static final Command<?> CMD_OFFSET = Command.create("scanner.offset", (te, player, params) -> te.applyOffset(params));

   public ScannerTileEntity(BlockPos pos, BlockState state) {
      this(ScannerModule.TYPE_SCANNER.get(), pos, state);
   }

   public ScannerTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
      super(type, pos, state);
      this.setRSMode(RedstoneMode.REDSTONE_ONREQUIRED);
   }

   protected void tickServer() {
      if (this.progress != null) {
         int done = 0;

         while (this.progress != null && done < ScannerConfiguration.surfaceAreaPerTick.get()) {
            this.progressScan();
            if (this.progress != null) {
               done += this.progress.dimY * this.progress.dimZ;
            }
         }

         int percent = this.progress == null ? -1 : (this.progress.x - this.progress.tl.getX()) * 100 / Math.max(1, this.progress.dimX);
         if (percent != this.progressBusy) {
            this.progressBusy = percent;
            this.markDirtyClient();
         }
      } else if (this.isMachineEnabled()) {
         this.scan();
      }
   }

   public ItemStack getRenderStack() {
      ItemStack stack = this.items.getStackInSlot(1);
      if (!stack.isEmpty()) {
         return stack;
      } else {
         if (this.renderStack.isEmpty()) {
            this.renderStack = new ItemStack((ItemLike)BuilderModule.SHAPE_CARD_DEF.get());
            this.updateScanCard(this.renderStack);
         }

         return this.renderStack;
      }
   }

   public BlockPos getDataDim() {
      return this.dataDim;
   }

   public BlockPos getDataOffset() {
      return this.dataOffset;
   }

   public int getScanProgress() {
      return this.progressBusy;
   }

   public BlockPos getScanCenter() {
      return this.getBlockPos().offset(this.dataOffset);
   }

   public int getScanId() {
      if (this.scanId == 0 && this.level != null && !this.level.isClientSide()) {
         this.scanId = ScanDataManager.get(this.level).newScan(this.level);
         this.setChanged();
         this.markDirtyClient();
      }

      return this.scanId;
   }

   private void handleSlotUpdate(int slot, ItemStack stack) {
      if (slot == 2) {
         this.filterCache.clear();
      }

      if (slot == 0) {
         if (!stack.isEmpty()) {
            this.dataDim = ShapeCardItem.getDimension(stack);
         }

         if (this.renderStack.isEmpty()) {
            this.renderStack = new ItemStack((ItemLike)BuilderModule.SHAPE_CARD_DEF.get());
         }

         this.updateScanCard(this.renderStack);
      } else if (slot == 1 && !stack.isEmpty()) {
         this.updateScanCard(stack);
      }

      this.setChanged();
      this.markDirtyClient();
   }

   private void updateScanCard(ItemStack card) {
      if (!card.isEmpty()) {
         if (!ShapeCardItem.getShape(card).isScan()) {
            ShapeCardItem.setShape(card, Shape.SHAPE_SCAN, ShapeCardItem.isSolid(card));
         }

         ShapeCardItem.setDimension(card, this.dataDim.getX(), this.dataDim.getY(), this.dataDim.getZ());
         ShapeCardItem.setOffset(card, this.dataOffset.getX(), this.dataOffset.getY(), this.dataOffset.getZ());
         ShapeCardItem.setData(card, this.getScanId());
      }
   }

   private void scan() {
      if (this.progress == null && !this.items.getStackInSlot(0).isEmpty()) {
         ItemStack card = this.items.getStackInSlot(0);
         this.dataDim = ShapeCardItem.getDimension(card);
         this.startScanArea(this.getScanCenter(), this.level.dimension(), this.dataDim.getX(), this.dataDim.getY(), this.dataDim.getZ());
      }
   }

   private void startScanArea(BlockPos center, ResourceKey<Level> dimension, int dimX, int dimY, int dimZ) {
      this.progress = new ScannerTileEntity.ScanProgress();
      this.progress.rle = new RLE();
      this.progress.tl = new BlockPos(center.getX() - dimX / 2, center.getY() - dimY / 2, center.getZ() - dimZ / 2);
      this.progress.materialPalette = new StatePalette();
      this.progress.materialPalette.alloc(((SupportBlock)BuilderModule.SUPPORT.get()).defaultBlockState(), 0);
      this.progress.x = this.progress.tl.getX();
      this.progress.dimX = dimX;
      this.progress.dimY = dimY;
      this.progress.dimZ = dimZ;
      this.progress.dimension = dimension;
      this.progressBusy = 0;
      this.setChanged();
      this.markDirtyClient();
   }

   private void progressScan() {
      if (this.progress != null) {
         Level scanWorld = this.level.getServer().getLevel(this.progress.dimension);
         if (scanWorld == null) {
            this.stopScanArea();
         } else {
            Predicate<ItemStack> filter = (Predicate<ItemStack>)this.filterCache.get();
            MutableBlockPos mpos = this.progress.mpos;
            BlockPos tl = this.progress.tl;

            for (int z = tl.getZ(); z < tl.getZ() + this.progress.dimZ; z++) {
               for (int y = tl.getY(); y < tl.getY() + this.progress.dimY; y++) {
                  mpos.set(this.progress.x, y, z);
                  int c = 0;
                  if (!scanWorld.isEmptyBlock(mpos)) {
                     BlockState state = scanWorld.getBlockState(mpos);
                     if (filter == null || filter.test(state.getBlock().getCloneItemStack(scanWorld, mpos, state, true, null))) {
                        c = this.progress.materialPalette.alloc(state, 0) + 1;
                     }
                  }

                  this.progress.rle.add(c);
               }
            }

            this.progress.x++;
            if (this.progress.x >= tl.getX() + this.progress.dimX) {
               this.stopScanArea();
            }
         }
      }
   }

   private void stopScanArea() {
      if (this.progress != null) {
         this.dataDim = new BlockPos(this.progress.dimX, this.progress.dimY, this.progress.dimZ);
         ScanDataManager manager = ScanDataManager.get(this.level);
         manager.getOrCreateScan(this.getScanId())
            .setData(this.progress.rle.getData(), this.progress.materialPalette.getPalette(), this.dataDim, this.dataOffset);
         manager.save(this.level, this.getScanId());
         if (this.renderStack.isEmpty()) {
            this.renderStack = new ItemStack((ItemLike)BuilderModule.SHAPE_CARD_DEF.get());
         }

         this.updateScanCard(this.renderStack);
         ItemStack out = this.items.getStackInSlot(1);
         if (!out.isEmpty()) {
            this.updateScanCard(out);
         }

         this.progress = null;
         this.progressBusy = -1;
         this.setChanged();
         this.markDirtyClient();
      }
   }

   private Predicate<ItemStack> createFilterCache() {
      return FilterModuleItem.getCache(this.items.getStackInSlot(2));
   }

   protected void loadAdditional(@Nonnull ValueInput tag) {
      super.loadAdditional(tag);
      this.items.load(tag, "items");
      this.scanId = tag.getIntOr("scanid", 0);
      if (tag.getInt("scandimx").isPresent()) {
         this.dataDim = new BlockPos(tag.getIntOr("scandimx", 0), tag.getIntOr("scandimy", 0), tag.getIntOr("scandimz", 0));
      }

      this.dataOffset = new BlockPos(tag.getIntOr("scanoffx", 0), tag.getIntOr("scanoffy", 0), tag.getIntOr("scanoffz", 0));
   }

   protected void saveAdditional(@Nonnull ValueOutput tag) {
      super.saveAdditional(tag);
      this.items.save(tag, "items");
      tag.putInt("scanid", this.scanId);
      tag.putInt("scandimx", this.dataDim.getX());
      tag.putInt("scandimy", this.dataDim.getY());
      tag.putInt("scandimz", this.dataDim.getZ());
      tag.putInt("scanoffx", this.dataOffset.getX());
      tag.putInt("scanoffy", this.dataOffset.getY());
      tag.putInt("scanoffz", this.dataOffset.getZ());
   }

   public void saveClientDataToNBT(CompoundTag tag, Provider provider) {
      ItemStack render = this.getRenderStack();
      if (!render.isEmpty()) {
         tag.put("render", CompatNbt.encodeItemStack(render, provider));
      }

      tag.putInt("scanid", this.scanId);
      tag.putInt("scandimx", this.dataDim.getX());
      tag.putInt("scandimy", this.dataDim.getY());
      tag.putInt("scandimz", this.dataDim.getZ());
      tag.putInt("scanoffx", this.dataOffset.getX());
      tag.putInt("scanoffy", this.dataOffset.getY());
      tag.putInt("scanoffz", this.dataOffset.getZ());
      tag.putInt("progress", this.progressBusy);
   }

   protected void applyImplicitComponents(DataComponentGetter input) {
      super.applyImplicitComponents(input);
      this.items.applyImplicitComponents((ItemInventory)input.get((DataComponentType)Registration.ITEM_INVENTORY.get()));
   }

   protected void collectImplicitComponents(Builder builder) {
      super.collectImplicitComponents(builder);
      this.items.collectImplicitComponents(builder);
   }

   public void loadClientDataFromNBT(CompoundTag tag, Provider provider) {
      this.renderStack = CompatNbt.decodeItemStack(tag.getCompoundOrEmpty("render"), provider);
      this.scanId = tag.getIntOr("scanid", 0);
      this.dataDim = new BlockPos(tag.getIntOr("scandimx", 0), tag.getIntOr("scandimy", 0), tag.getIntOr("scandimz", 0));
      this.dataOffset = new BlockPos(tag.getIntOr("scanoffx", 0), tag.getIntOr("scanoffy", 0), tag.getIntOr("scanoffz", 0));
      this.progressBusy = tag.getIntOr("progress", 0);
   }

   public GenericItemHandler getItems() {
      return this.items;
   }

   private void applyOffset(TypedMap params) {
      this.dataOffset = (BlockPos)params.get(PARAM_OFFSET);
      if (!this.renderStack.isEmpty()) {
         this.updateScanCard(this.renderStack);
      }

      ItemStack out = this.items.getStackInSlot(1);
      if (!out.isEmpty()) {
         this.updateScanCard(out);
      }

      this.setChanged();
      this.markDirtyClient();
   }

   private static class ScanProgress {
      private RLE rle;
      private BlockPos tl;
      private StatePalette materialPalette;
      private final MutableBlockPos mpos = new MutableBlockPos();
      private int dimX;
      private int dimY;
      private int dimZ;
      private int x;
      private ResourceKey<Level> dimension;
   }
}
