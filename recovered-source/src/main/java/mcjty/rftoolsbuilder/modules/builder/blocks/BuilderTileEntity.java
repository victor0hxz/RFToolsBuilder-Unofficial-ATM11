package mcjty.rftoolsbuilder.modules.builder.blocks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mcjty.lib.api.container.DefaultContainerProvider;
import mcjty.lib.api.container.ItemInventory;
import mcjty.lib.api.infusable.DefaultInfusable;
import mcjty.lib.api.infusable.IInfusable;
import mcjty.lib.api.infusable.ItemInfusable;
import mcjty.lib.api.module.DefaultModuleSupport;
import mcjty.lib.api.module.IModuleSupport;
import mcjty.lib.api.power.ItemEnergy;
import mcjty.lib.blockcommands.Command;
import mcjty.lib.blockcommands.ListCommand;
import mcjty.lib.blockcommands.ServerCommand;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.builder.InfoLine;
import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.GenericItemHandler;
import mcjty.lib.container.SlotDefinition;
import mcjty.lib.setup.Registration;
import mcjty.lib.tileentity.BaseBEData;
import mcjty.lib.tileentity.Cap;
import mcjty.lib.tileentity.CapType;
import mcjty.lib.tileentity.GenericEnergyStorage;
import mcjty.lib.tileentity.TickingTileEntity;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.Broadcaster;
import mcjty.lib.varia.Cached;
import mcjty.lib.varia.CapabilityTools;
import mcjty.lib.varia.FakePlayerGetter;
import mcjty.lib.varia.FluidTools;
import mcjty.lib.varia.InventoryTools;
import mcjty.lib.varia.LazyList;
import mcjty.lib.varia.LevelTools;
import mcjty.lib.varia.Logging;
import mcjty.lib.varia.OrientationTools;
import mcjty.lib.varia.RedstoneMode;
import mcjty.lib.varia.SoundTools;
import mcjty.lib.varia.Sync;
import mcjty.lib.varia.TagTools;
import mcjty.lib.varia.TeleportationTools;
import mcjty.lib.varia.Tools;
import mcjty.rftoolsbase.api.client.IHudSupport;
import mcjty.rftoolsbase.modules.filter.items.FilterModuleItem;
import mcjty.rftoolsbase.tools.ManualHelper;
import mcjty.rftoolsbuilder.RFToolsBuilder;
import mcjty.rftoolsbuilder.compat.RFToolsBuilderTOPDriver;
import mcjty.rftoolsbuilder.modules.builder.BlockInformation;
import mcjty.rftoolsbuilder.modules.builder.BuilderConfiguration;
import mcjty.rftoolsbuilder.modules.builder.BuilderModule;
import mcjty.rftoolsbuilder.modules.builder.BuilderTileEntityMode;
import mcjty.rftoolsbuilder.modules.builder.SpaceChamberRepository;
import mcjty.rftoolsbuilder.modules.builder.data.BuilderData;
import mcjty.rftoolsbuilder.modules.builder.data.ShapeCardData;
import mcjty.rftoolsbuilder.modules.builder.items.ShapeCardItem;
import mcjty.rftoolsbuilder.modules.builder.items.ShapeCardType;
import mcjty.rftoolsbuilder.modules.builder.items.SpaceChamberCardItem;
import mcjty.rftoolsbuilder.setup.ClientCommandHandler;
import mcjty.rftoolsbuilder.setup.RFToolsBuilderMessages;
import mcjty.rftoolsbuilder.shapes.Shape;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueOutput.TypedOutputList;
import net.minecraft.world.level.storage.loot.LootParams.Builder;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.SpecialPlantable;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;

public class BuilderTileEntity extends TickingTileEntity implements IHudSupport {
   public static final int SLOT_TAB = 0;
   public static final int SLOT_FILTER = 1;
   public static final Identifier DONT_REMOVE_ME = Identifier.fromNamespaceAndPath("rftoolsbuilder", "dontremoveme");
   public static final TagKey<Block> DONT_REMOVE_ME_TAG = TagTools.createBlockTagKey(DONT_REMOVE_ME);
   public static final Lazy<ContainerFactory> CONTAINER_FACTORY = Lazy.of(
      () -> new ContainerFactory(2)
         .slot(SlotDefinition.specific(s -> s.getItem() instanceof ShapeCardItem || s.getItem() instanceof SpaceChamberCardItem).in().out(), 0, 100, 10)
         .slot(SlotDefinition.specific(s -> s.getItem() instanceof FilterModuleItem).in().out(), 1, 84, 46)
         .playerSlots(10, 70)
   );
   private static int currentLevel = 0;
   private int scanLocCnt = 0;
   private static Map<BlockPos, Pair<Long, BlockPos>> scanLocClient = new HashMap<>();
   private int collectCounter = (Integer)BuilderConfiguration.collectTimer.get();
   private int collectXP = 0;
   private boolean boxValid = false;
   private int projDx;
   private int projDy;
   private int projDz;
   private long lastHudTime = 0L;
   private List<String> clientHudLog = new ArrayList<>();
   private ShapeCardType cardType = ShapeCardType.CARD_UNKNOWN;
   private static ItemStack TOOL_NORMAL;
   private static ItemStack TOOL_SILK;
   private static ItemStack TOOL_FORTUNE;
   private final Cached<Predicate<ItemStack>> filterCache = Cached.of(this::createFilterCache);
   private ChunkPos forcedChunk = null;
   private Map<BlockPos, BlockState> cachedBlocks = null;
   private ChunkPos cachedChunk = null;
   private final Cached<Set<Block>> cachedVoidableBlocks = Cached.of(this::getCachedVoidableBlocks);
   private final LazyList<ItemStack> overflowItems = new LazyList();
   private final FakePlayerGetter harvester = new FakePlayerGetter(this, "rftools_builder");
   private final GenericItemHandler items = this.createItemHandler();
   @Cap(type = CapType.ITEMS_AUTOMATION)
   private static final Function<BuilderTileEntity, GenericItemHandler> ITEM_CAP = tile -> tile.items;
   private final GenericEnergyStorage energyStorage = new GenericEnergyStorage(
      this, true, ((Integer)BuilderConfiguration.BUILDER_MAXENERGY.get()).intValue(), ((Integer)BuilderConfiguration.BUILDER_RECEIVEPERTICK.get()).intValue()
   );
   @Cap(type = CapType.ENERGY)
   private static final Function<BuilderTileEntity, GenericEnergyStorage> ENERGY_CAP = tile -> tile.energyStorage;
   @Cap(type = CapType.CONTAINER)
   private static final Function<BuilderTileEntity, MenuProvider> SCREEN_CAP = tile -> new DefaultContainerProvider("Builder")
      .containerSupplier(DefaultContainerProvider.container(BuilderModule.CONTAINER_BUILDER, CONTAINER_FACTORY, tile))
      .itemHandler(() -> tile.items)
      .energyHandler(() -> tile.energyStorage)
      .shortListener(Sync.integer(() -> tile.getScan() == null ? -1 : tile.getScan().getY(), v -> currentLevel = v))
      .data(BuilderModule.BUILDER_DATA, BuilderData.STREAM_CODEC, BuilderData.CODEC)
      .data(Registration.BASE_BE_DATA, BaseBEData.STREAM_CODEC, BaseBEData.CODEC)
      .setupSync(tile);
   private final DefaultInfusable infusable = new DefaultInfusable(this);
   @Cap(type = CapType.INFUSABLE)
   private static final Function<BuilderTileEntity, IInfusable> INFUSABLE_CAP = tile -> tile.infusable;
   @Cap(type = CapType.MODULE)
   private static final Function<BuilderTileEntity, IModuleSupport> MODULE_CAP = tile -> new DefaultModuleSupport(0) {
      public boolean isModule(ItemStack itemStack) {
         return itemStack.getItem() instanceof ShapeCardItem || itemStack.getItem() == BuilderModule.SPACE_CHAMBER_CARD.get();
      }
   };
   private static final Random random = new Random();
   private final LazyList<ItemStack> couldntHandle1 = new LazyList();
   private final LazyList<ItemStack> couldntHandle2 = new LazyList();
   @ServerCommand
   public static final Command<?> CMD_RESTART = Command.create("restart", (te, player, params) -> {
      BuilderData data = (BuilderData)te.getData(BuilderModule.BUILDER_DATA);
      data = te.restartScan(data);
      te.setData(BuilderModule.BUILDER_DATA, data);
   });
   @ServerCommand
   public static final ListCommand<?, ?> CMD_GETHUDLOG = ListCommand.create(
      "getHudLog", (te, player, params) -> te.getHudLog(), (te, player, params, list) -> te.clientHudLog = list
   );

   public BuilderTileEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType)BuilderModule.BUILDER.be().get(), pos, state);
      this.setRSMode(RedstoneMode.REDSTONE_ONREQUIRED);
   }

   public static BaseBlock createBlock() {
      return new BaseBlock(
         new BlockBuilder()
            .tileEntitySupplier(BuilderTileEntity::new)
            .topDriver(RFToolsBuilderTOPDriver.DRIVER)
            .infusable()
            .manualEntry(ManualHelper.create("rftoolsbase:builder/builder_intro"))
            .info(new InfoLine[]{TooltipBuilder.key("message.rftoolsbuilder.shiftmessage")})
            .infoShift(new InfoLine[]{TooltipBuilder.header(), TooltipBuilder.gold()})
      ) {
         public RotationType getRotationType() {
            return RotationType.HORIZROTATION;
         }
      };
   }

   protected boolean needsRedstoneMode() {
      return true;
   }

   public Direction getBlockOrientation() {
      BlockState state = this.level.getBlockState(this.worldPosition);
      return state.getBlock() == BuilderModule.BUILDER.block().get() ? OrientationTools.getOrientationHoriz(state) : null;
   }

   public boolean isBlockAboveAir() {
      return this.level.isEmptyBlock(this.worldPosition.above());
   }

   public List<String> getClientLog() {
      return this.clientHudLog;
   }

   public List<String> getHudLog() {
      List<String> list = new ArrayList<>();
      list.add(ChatFormatting.BLUE + "Mode:");
      if (this.isShapeCard()) {
         this.getCardType().addHudLog(list, this.items);
      } else {
         list.add("    Space card: " + this.getMode().getName().toLowerCase());
      }

      BuilderData data = (BuilderData)this.getData(BuilderModule.BUILDER_DATA);
      if (data.scan() != null) {
         list.add(ChatFormatting.BLUE + "Progress:");
         list.add("    Y level: " + data.scan().getY());
         int minChunkX = data.minBox().getX() >> 4;
         int minChunkZ = data.minBox().getZ() >> 4;
         int maxChunkX = data.maxBox().getX() >> 4;
         int maxChunkZ = data.maxBox().getZ() >> 4;
         int curX = data.scan().getX() >> 4;
         int curZ = data.scan().getZ() >> 4;
         int totChunks = (maxChunkX - minChunkX + 1) * (maxChunkZ - minChunkZ + 1);
         int curChunk = (curZ - minChunkZ) * (maxChunkX - minChunkX) + curX - minChunkX;
         list.add("    Chunk:  " + curChunk + " of " + totChunks);
      }

      if (data.lastError() != null && !data.lastError().isEmpty()) {
         String[] errors = StringUtils.split(data.lastError(), "\n");

         for (String error : errors) {
            list.add(ChatFormatting.RED + error);
         }
      }

      return list;
   }

   public BlockPos getHudPos() {
      return this.getBlockPos();
   }

   public long getLastUpdateTime() {
      return this.lastHudTime;
   }

   public void setLastUpdateTime(long t) {
      this.lastHudTime = t;
   }

   private boolean isShapeCard() {
      return this.items.getStackInSlot(0).getItem() instanceof ShapeCardItem;
   }

   private BlockPos getScan() {
      return ((BuilderData)this.getData(BuilderModule.BUILDER_DATA)).scan();
   }

   private void makeSupportBlocksShaped() {
      ItemStack shapeCard = this.items.getStackInSlot(0);
      BlockPos dimension = ShapeCardItem.getClampedDimension(shapeCard, (Integer)BuilderConfiguration.maxBuilderDimension.get());
      BlockPos offset = ShapeCardItem.getClampedOffset(shapeCard, (Integer)BuilderConfiguration.maxBuilderOffset.get());
      Shape shape = ShapeCardItem.getShape(shapeCard);
      Map<BlockPos, BlockState> blocks = new HashMap<>();
      ShapeCardItem.composeFormula(
         shapeCard,
         shape.getFormulaFactory().get(),
         this.level,
         this.getBlockPos(),
         dimension,
         offset,
         blocks,
         (Integer)BuilderConfiguration.maxBuilderDimension.get() * 256 * (Integer)BuilderConfiguration.maxBuilderDimension.get(),
         false,
         false,
         null
      );
      BlockState state = (BlockState)((SupportBlock)BuilderModule.SUPPORT.get())
         .defaultBlockState()
         .setValue(SupportBlock.STATUS, SupportBlock.SupportStatus.STATUS_OK);

      for (Entry<BlockPos, BlockState> entry : blocks.entrySet()) {
         BlockPos p = entry.getKey();
         if (this.level.isEmptyBlock(p)) {
            this.level.setBlock(p, state, 2);
         }
      }
   }

   private BuilderData makeSupportBlocks(BuilderData data) {
      if (this.isShapeCard()) {
         this.makeSupportBlocksShaped();
         return data;
      } else {
         Pair<BuilderData, SpaceChamberRepository.SpaceChamberChannel> result = this.calculateBox(data);
         data = (BuilderData)result.getLeft();
         SpaceChamberRepository.SpaceChamberChannel chamberChannel = (SpaceChamberRepository.SpaceChamberChannel)result.getRight();
         if (chamberChannel != null) {
            ResourceKey<Level> dimension = chamberChannel.getDimension();
            Level world = LevelTools.getLevel(this.level, dimension);
            if (world == null) {
               return data;
            }

            Player player = this.harvester.get();
            MutableBlockPos src = new MutableBlockPos();
            MutableBlockPos dest = new MutableBlockPos();
            BlockPos minBox = data.minBox();
            BlockPos maxBox = data.maxBox();

            for (int x = minBox.getX(); x <= maxBox.getX(); x++) {
               for (int y = minBox.getY(); y <= maxBox.getY(); y++) {
                  for (int z = minBox.getZ(); z <= maxBox.getZ(); z++) {
                     src.set(x, y, z);
                     this.sourceToDest(src, dest);
                     BlockState srcState = world.getBlockState(src);
                     Block srcBlock = srcState.getBlock();
                     BlockState dstState = world.getBlockState(dest);
                     Block dstBlock = dstState.getBlock();
                     SupportBlock.SupportStatus error = SupportBlock.SupportStatus.STATUS_OK;
                     if (this.getMode() != BuilderMode.MODE_COPY) {
                        BlockEntity srcTileEntity = world.getBlockEntity(src);
                        BlockEntity dstTileEntity = world.getBlockEntity(dest);
                        SupportBlock.SupportStatus error1 = this.isMovable(player, world, src, srcBlock, srcTileEntity);
                        SupportBlock.SupportStatus error2 = this.isMovable(player, world, dest, dstBlock, dstTileEntity);
                        error = SupportBlock.SupportStatus.max(error1, error2);
                     }

                     if (isEmpty(srcState, srcBlock) && !isEmpty(dstState, dstBlock)) {
                        world.setBlock(src, (BlockState)((SupportBlock)BuilderModule.SUPPORT.get()).defaultBlockState().setValue(SupportBlock.STATUS, error), 3);
                     }

                     if (isEmpty(dstState, dstBlock) && !isEmpty(srcState, srcBlock)) {
                        world.setBlock(
                           dest, (BlockState)((SupportBlock)BuilderModule.SUPPORT.get()).defaultBlockState().setValue(SupportBlock.STATUS, error), 3
                        );
                     }
                  }
               }
            }
         }

         return data;
      }
   }

   private void clearSupportBlocksShaped() {
      ItemStack shapeCard = this.items.getStackInSlot(0);
      BlockPos dimension = ShapeCardItem.getClampedDimension(shapeCard, (Integer)BuilderConfiguration.maxBuilderDimension.get());
      BlockPos offset = ShapeCardItem.getClampedOffset(shapeCard, (Integer)BuilderConfiguration.maxBuilderOffset.get());
      Shape shape = ShapeCardItem.getShape(shapeCard);
      Map<BlockPos, BlockState> blocks = new HashMap<>();
      ShapeCardItem.composeFormula(
         shapeCard,
         shape.getFormulaFactory().get(),
         this.level,
         this.getBlockPos(),
         dimension,
         offset,
         blocks,
         (Integer)BuilderConfiguration.maxSpaceChamberDimension.get()
            * (Integer)BuilderConfiguration.maxSpaceChamberDimension.get()
            * (Integer)BuilderConfiguration.maxSpaceChamberDimension.get(),
         false,
         false,
         null
      );

      for (Entry<BlockPos, BlockState> entry : blocks.entrySet()) {
         BlockPos p = entry.getKey();
         if (this.level.getBlockState(p).getBlock() == BuilderModule.SUPPORT.get()) {
            this.level.setBlockAndUpdate(p, Blocks.AIR.defaultBlockState());
         }
      }
   }

   public BuilderData clearSupportBlocks(BuilderData data) {
      if (this.level.isClientSide()) {
         return data;
      } else if (this.isShapeCard()) {
         this.clearSupportBlocksShaped();
         return data;
      } else {
         Pair<BuilderData, SpaceChamberRepository.SpaceChamberChannel> result = this.calculateBox(data);
         data = (BuilderData)result.getLeft();
         SpaceChamberRepository.SpaceChamberChannel chamberChannel = (SpaceChamberRepository.SpaceChamberChannel)result.getRight();
         if (chamberChannel != null) {
            ResourceKey<Level> dimension = chamberChannel.getDimension();
            Level world = LevelTools.getLevel(this.level, dimension);
            MutableBlockPos src = new MutableBlockPos();
            MutableBlockPos dest = new MutableBlockPos();
            BlockPos minBox = data.minBox();
            BlockPos maxBox = data.maxBox();

            for (int x = minBox.getX(); x <= maxBox.getX(); x++) {
               for (int y = minBox.getY(); y <= maxBox.getY(); y++) {
                  for (int z = minBox.getZ(); z <= maxBox.getZ(); z++) {
                     src.set(x, y, z);
                     if (world != null) {
                        Block srcBlock = world.getBlockState(src).getBlock();
                        if (srcBlock == BuilderModule.SUPPORT.get()) {
                           world.setBlockAndUpdate(src, Blocks.AIR.defaultBlockState());
                        }
                     }

                     this.sourceToDest(src, dest);
                     Block dstBlock = world.getBlockState(dest).getBlock();
                     if (dstBlock == BuilderModule.SUPPORT.get()) {
                        world.setBlockAndUpdate(dest, Blocks.AIR.defaultBlockState());
                     }
                  }
               }
            }
         }

         return data;
      }
   }

   public void onDataChanged(AttachmentType<?> type, Object oldData, Object newData) {
      if (type == BuilderModule.BUILDER_DATA.get()) {
         this.onDataChanged((BuilderData)oldData, (BuilderData)newData);
      }
   }

   private void onDataChanged(BuilderData oldData, BuilderData newData) {
      if (!this.level.isClientSide()) {
         if (oldData.mode() != newData.mode()) {
            newData = this.restartScan(newData);
         }

         if (oldData.anchor() != newData.anchor()) {
            newData = this.onAnchorChanged(newData);
         }

         if (oldData.rotate() != newData.rotate()) {
            newData = this.onRotateChanged(newData);
         }

         if (oldData.flags().supportMode() != newData.flags().supportMode()) {
            if (newData.flags().supportMode()) {
               newData = this.makeSupportBlocks(newData);
            } else {
               newData = this.clearSupportBlocks(newData);
            }
         }

         this.setData(BuilderModule.BUILDER_DATA, newData);
      }
   }

   public boolean isHilightMode() {
      return ((BuilderData)this.getData(BuilderModule.BUILDER_DATA)).flags().hilightMode();
   }

   public boolean isWaitMode() {
      return ((BuilderData)this.getData(BuilderModule.BUILDER_DATA)).flags().waitMode();
   }

   private String getLastError() {
      return ((BuilderData)this.getData(BuilderModule.BUILDER_DATA)).lastError();
   }

   private boolean waitOrSkip(BuilderData data, String error) {
      if (this.isWaitMode()) {
         data = data.withLastError(error);
         this.setData(BuilderModule.BUILDER_DATA, data);
         return true;
      } else {
         return false;
      }
   }

   private boolean skip(BuilderData data) {
      data = data.withLastError(null);
      this.setData(BuilderModule.BUILDER_DATA, data);
      return false;
   }

   private Pair<BuilderData, Boolean> skip(BuilderData data, String error) {
      data = data.withLastError(error);
      return Pair.of(data, false);
   }

   public boolean suspend(int rfNeeded, BlockPos srcPos, BlockState srcState, BlockState pickState) {
      BuilderData data = (BuilderData)this.getData(BuilderModule.BUILDER_DATA);
      data = data.withLastError(null);
      this.setData(BuilderModule.BUILDER_DATA, data);
      return true;
   }

   private Pair<BuilderData, Boolean> suspend(BuilderData data, String error) {
      data = data.withLastError(error);
      return Pair.of(data, true);
   }

   public boolean hasLoopMode() {
      return ((BuilderData)this.getData(BuilderModule.BUILDER_DATA)).flags().loopMode();
   }

   public boolean hasEntityMode() {
      return ((BuilderData)this.getData(BuilderModule.BUILDER_DATA)).flags().entityMode();
   }

   public boolean hasSupportMode() {
      return ((BuilderData)this.getData(BuilderModule.BUILDER_DATA)).flags().supportMode();
   }

   public void setSupportMode(boolean supportMode) {
      BuilderData data = (BuilderData)this.getData(BuilderModule.BUILDER_DATA);
      if (!this.level.isClientSide()) {
         if (supportMode) {
            data = this.makeSupportBlocks(data);
         } else {
            data = this.clearSupportBlocks(data);
         }

         this.setData(BuilderModule.BUILDER_DATA, data.withSupportMode(supportMode));
      }
   }

   public boolean isSilent() {
      return ((BuilderData)this.getData(BuilderModule.BUILDER_DATA)).flags().silent();
   }

   public BuilderMode getMode() {
      return ((BuilderData)this.getData(BuilderModule.BUILDER_DATA)).mode();
   }

   public void resetBox() {
      this.boxValid = false;
   }

   public AnchorMode getAnchor() {
      return ((BuilderData)this.getData(BuilderModule.BUILDER_DATA)).anchor();
   }

   public BuilderData onAnchorChanged(BuilderData data) {
      if (data.flags().supportMode() && !this.level.isClientSide()) {
         data = this.clearSupportBlocks(data);
      }

      this.boxValid = false;
      if (this.isShapeCard()) {
         ItemStack shapeCard = this.items.getStackInSlot(0);
         BlockPos dimension = ShapeCardItem.getDimension(shapeCard);
         BlockPos minBox = this.positionBox(dimension);
         int dx = dimension.getX();
         int dy = dimension.getY();
         int dz = dimension.getZ();
         BlockPos offset = new BlockPos(minBox.getX() + (int)Math.ceil(dx / 2), minBox.getY() + (int)Math.ceil(dy / 2), minBox.getZ() + (int)Math.ceil(dz / 2));
         ShapeCardItem.setOffset(shapeCard, offset.getX(), offset.getY(), offset.getZ());
         this.setChanged();
      }

      if (data.flags().supportMode() && !this.level.isClientSide()) {
         data = this.makeSupportBlocks(data);
      }

      return data;
   }

   private BlockPos positionBox(BlockPos dimension) {
      BlockState state = this.level.getBlockState(this.getBlockPos());
      Direction direction = (Direction)state.getValue(BlockStateProperties.HORIZONTAL_FACING);
      int spanX = dimension.getX();
      int spanY = dimension.getY();
      int spanZ = dimension.getZ();
      int x = 0;
      int z = 0;
      AnchorMode anchor = this.getAnchor();
      int y = -(anchor != AnchorMode.ANCHOR_NE && anchor != AnchorMode.ANCHOR_NW ? 0 : spanY - 1);
      switch (direction) {
         case SOUTH:
            x = -(anchor != AnchorMode.ANCHOR_NE && anchor != AnchorMode.ANCHOR_SE ? 0 : spanX - 1);
            z = -spanZ;
            break;
         case NORTH:
            x = 1 - spanX + (anchor != AnchorMode.ANCHOR_NE && anchor != AnchorMode.ANCHOR_SE ? 0 : spanX - 1);
            z = 1;
            break;
         case WEST:
            x = 1;
            z = -(anchor != AnchorMode.ANCHOR_NE && anchor != AnchorMode.ANCHOR_SE ? 0 : spanZ - 1);
            break;
         case EAST:
            x = -spanX;
            z = -(anchor != AnchorMode.ANCHOR_NE && anchor != AnchorMode.ANCHOR_SE ? spanZ - 1 : 0);
         case DOWN:
         case UP:
      }

      return new BlockPos(x, y, z);
   }

   public RotateMode getRotate() {
      return ((BuilderData)this.getData(BuilderModule.BUILDER_DATA)).rotate();
   }

   public BuilderData onRotateChanged(BuilderData data) {
      if (data.flags().supportMode() && !this.level.isClientSide()) {
         data = this.clearSupportBlocks(data);
      }

      this.boxValid = false;
      if (data.flags().supportMode() && !this.level.isClientSide()) {
         data = this.makeSupportBlocks(data);
      }

      return data;
   }

   public void setPowerInput(int powered) {
      boolean o = this.isMachineEnabled();
      super.setPowerInput(powered);
      boolean n = this.isMachineEnabled();
      if (o != n) {
         BuilderData data = (BuilderData)this.getData(BuilderModule.BUILDER_DATA);
         if ((this.hasLoopMode() || n && data.scan() == null) && !this.level.isClientSide()) {
            data = this.restartScan(data);
            this.setData(BuilderModule.BUILDER_DATA, data);
         }
      }
   }

   private void createProjection(BuilderData data, SpaceChamberRepository.SpaceChamberChannel chamberChannel) {
      BlockPos minC = this.rotate(chamberChannel.getMinCorner());
      BlockPos maxC = this.rotate(chamberChannel.getMaxCorner());
      BlockPos minCorner = new BlockPos(Math.min(minC.getX(), maxC.getX()), Math.min(minC.getY(), maxC.getY()), Math.min(minC.getZ(), maxC.getZ()));
      BlockPos maxCorner = new BlockPos(Math.max(minC.getX(), maxC.getX()), Math.max(minC.getY(), maxC.getY()), Math.max(minC.getZ(), maxC.getZ()));
      BlockState state = this.level.getBlockState(this.getBlockPos());
      Direction direction = (Direction)state.getValue(BlockStateProperties.HORIZONTAL_FACING);
      int xCoord = this.getBlockPos().getX();
      int yCoord = this.getBlockPos().getY();
      int zCoord = this.getBlockPos().getZ();
      int spanX = maxCorner.getX() - minCorner.getX();
      int spanY = maxCorner.getY() - minCorner.getY();
      int spanZ = maxCorner.getZ() - minCorner.getZ();
      AnchorMode anchor = data.anchor();
      switch (direction) {
         case SOUTH:
            this.projDx = xCoord
               + Direction.NORTH.getUnitVec3i().getX()
               - minCorner.getX()
               - (anchor != AnchorMode.ANCHOR_NE && anchor != AnchorMode.ANCHOR_SE ? 0 : spanX);
            this.projDz = zCoord + Direction.NORTH.getUnitVec3i().getZ() - minCorner.getZ() - spanZ;
            break;
         case NORTH:
            this.projDx = xCoord
               + Direction.SOUTH.getUnitVec3i().getX()
               - minCorner.getX()
               - spanX
               + (anchor != AnchorMode.ANCHOR_NE && anchor != AnchorMode.ANCHOR_SE ? 0 : spanX);
            this.projDz = zCoord + Direction.SOUTH.getUnitVec3i().getZ() - minCorner.getZ();
            break;
         case WEST:
            this.projDx = xCoord + Direction.EAST.getUnitVec3i().getX() - minCorner.getX();
            this.projDz = zCoord
               + Direction.EAST.getUnitVec3i().getZ()
               - minCorner.getZ()
               - (anchor != AnchorMode.ANCHOR_NE && anchor != AnchorMode.ANCHOR_SE ? 0 : spanZ);
            break;
         case EAST:
            this.projDx = xCoord + Direction.WEST.getUnitVec3i().getX() - minCorner.getX() - spanX;
            this.projDz = zCoord
               + Direction.WEST.getUnitVec3i().getZ()
               - minCorner.getZ()
               - spanZ
               + (anchor != AnchorMode.ANCHOR_NE && anchor != AnchorMode.ANCHOR_SE ? 0 : spanZ);
         case DOWN:
         case UP:
      }

      this.projDy = yCoord - minCorner.getY() - (anchor != AnchorMode.ANCHOR_NE && anchor != AnchorMode.ANCHOR_NW ? 0 : spanY);
   }

   private BuilderData calculateBox(BuilderData data, int channel) {
      SpaceChamberRepository repository = SpaceChamberRepository.get(this.level);
      SpaceChamberRepository.SpaceChamberChannel chamberChannel = repository.getChannel(channel);
      BlockPos minCorner = chamberChannel.getMinCorner();
      BlockPos maxCorner = chamberChannel.getMaxCorner();
      if (minCorner == null || maxCorner == null) {
         return data;
      } else if (this.boxValid && minCorner.equals(data.minBox()) && maxCorner.equals(data.maxBox())) {
         return data;
      } else {
         this.boxValid = true;
         this.cardType = ShapeCardType.CARD_SPACE;
         this.createProjection(data, chamberChannel);
         data = data.withMinBox(minCorner).withMaxBox(maxCorner);
         return this.restartScan(data);
      }
   }

   private void checkStateServerShaped() {
      float factor = this.infusable.getInfusedFactor();
      BuilderData data = (BuilderData)this.getData(BuilderModule.BUILDER_DATA);

      for (int i = 0;
         i
            < ((Integer)BuilderConfiguration.quarryBaseSpeed.get()).intValue()
               + factor * ((Integer)BuilderConfiguration.quarryInfusionSpeedFactor.get()).intValue();
         i++
      ) {
         if (data.scan() != null) {
            data = this.handleBlockShaped(data);
         }
      }

      this.setData(BuilderModule.BUILDER_DATA, data);
   }

   public void tickServer() {
      if (!this.overflowItems.isEmpty()) {
         this.insertItems(this.overflowItems.extractList());
      }

      if (this.isMachineEnabled() || !this.hasLoopMode()) {
         BuilderData data = (BuilderData)this.getData(BuilderModule.BUILDER_DATA);
         if (data.scan() != null) {
            if (this.isHilightMode()) {
               this.updateHilight();
            }

            if (this.isShapeCard()) {
               if (!this.isMachineEnabled()) {
                  this.chunkUnload();
               } else {
                  this.checkStateServerShaped();
               }
            } else {
               Pair<BuilderData, SpaceChamberRepository.SpaceChamberChannel> result = this.calculateBox(data);
               data = (BuilderData)result.getLeft();
               SpaceChamberRepository.SpaceChamberChannel chamberChannel = (SpaceChamberRepository.SpaceChamberChannel)result.getRight();
               if (chamberChannel == null) {
                  this.setData(BuilderModule.BUILDER_DATA, data.withScan(null));
               } else {
                  ResourceKey<Level> dimension = chamberChannel.getDimension();
                  Level world = LevelTools.getLevel(this.level, dimension);
                  if (world != null) {
                     if (this.getMode() == BuilderMode.MODE_COLLECT) {
                        this.collectItems(world);
                     } else {
                        float factor = this.infusable.getInfusedFactor();

                        for (int i = 0; i < 2.0F + factor * 40.0F; i++) {
                           if (data.scan() != null) {
                              data = this.handleBlock(data, world);
                           }
                        }
                     }

                     this.setData(BuilderModule.BUILDER_DATA, data);
                  }
               }
            }
         }
      }
   }

   private void updateHilight() {
      this.scanLocCnt--;
      if (this.scanLocCnt <= 0) {
         this.scanLocCnt = 5;
         BuilderData data = (BuilderData)this.getData(BuilderModule.BUILDER_DATA);
         int x = data.scan().getX();
         int y = data.scan().getY();
         int z = data.scan().getZ();
         double sqradius = 900.0;

         for (ServerPlayer player : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) {
            if (Objects.equals(player.level().dimension(), this.level.dimension())) {
               double d0 = x - player.getX();
               double d1 = y - player.getY();
               double d2 = z - player.getZ();
               if (d0 * d0 + d1 * d1 + d2 * d2 < sqradius) {
                  RFToolsBuilderMessages.sendToClient(
                     player,
                     "positionToClient",
                     TypedMap.builder().put(ClientCommandHandler.PARAM_POS, this.getBlockPos()).put(ClientCommandHandler.PARAM_SCAN, data.scan())
                  );
               }
            }
         }
      }
   }

   private void collectItems(Level world) {
      this.collectCounter--;
      if (this.collectCounter <= 0) {
         this.collectCounter = (Integer)BuilderConfiguration.collectTimer.get();
         BuilderData data = (BuilderData)this.getData(BuilderModule.BUILDER_DATA);
         if (!this.hasLoopMode()) {
            data = data.withScan(null);
            this.setData(BuilderModule.BUILDER_DATA, data);
         }

         float factor = this.infusable.getInfusedFactor();
         BlockPos minBox = data.minBox();
         BlockPos maxBox = data.maxBox();
         long rf = this.energyStorage.getEnergyStored();
         float area = (maxBox.getX() - minBox.getX() + 1) * (maxBox.getY() - minBox.getY() + 1) * (maxBox.getZ() - minBox.getZ() + 1);
         float infusedFactor = (4.0F - factor) / 4.0F;
         int rfNeeded = (int)((Double)BuilderConfiguration.collectRFPerTickPerArea.get() * area * infusedFactor)
            * (Integer)BuilderConfiguration.collectTimer.get();
         if (rfNeeded <= rf) {
            this.energyStorage.consumeEnergy(rfNeeded);
            AABB bb = new AABB(minBox.getX() - 0.8, minBox.getY() - 0.8, minBox.getZ() - 0.8, maxBox.getX() + 0.8, maxBox.getY() + 0.8, maxBox.getZ() + 0.8);

            for (Entity entity : world.getEntitiesOfClass(Entity.class, bb)) {
               if (entity instanceof ItemEntity) {
                  if (this.collectItem(world, factor, (ItemEntity)entity)) {
                     return;
                  }
               } else if (entity instanceof ExperienceOrb && this.collectXP(world, factor, (ExperienceOrb)entity)) {
                  return;
               }
            }
         }
      }
   }

   private boolean collectXP(Level world, float infusedFactor, ExperienceOrb orb) {
      int xp = orb.getValue();
      long rf = this.energyStorage.getEnergyStored();
      int rfNeeded = (int)((Double)BuilderConfiguration.collectRFPerXP.get() * infusedFactor * xp);
      if (rfNeeded > rf) {
         return true;
      } else {
         this.collectXP += xp;
         int bottles = this.collectXP / 7;
         if (bottles > 0) {
            if (this.insertItem(new ItemStack(Items.EXPERIENCE_BOTTLE, bottles)).isEmpty()) {
               this.collectXP %= 7;
               orb.kill((ServerLevel)this.level);
               this.energyStorage.consumeEnergy(rfNeeded);
            } else {
               this.collectXP = 0;
            }
         }

         return false;
      }
   }

   private boolean collectItem(Level world, float infusedFactor, ItemEntity item) {
      ItemStack stack = item.getItem();
      Predicate<ItemStack> predicate = (Predicate<ItemStack>)this.filterCache.get();
      if (predicate != null && !predicate.test(stack)) {
         return false;
      } else {
         long rf = this.energyStorage.getEnergyStored();
         int rfNeeded = (int)(((Integer)BuilderConfiguration.collectRFPerItem.get()).intValue() * infusedFactor) * stack.getCount();
         if (rfNeeded > rf) {
            return true;
         } else {
            this.energyStorage.consumeEnergy(rfNeeded);
            item.kill((ServerLevel)this.level);
            stack = this.insertItem(stack);
            if (!stack.isEmpty()) {
               BlockPos position = item.blockPosition();
               ItemEntity entityItem = new ItemEntity(world, position.getX(), position.getY(), position.getZ(), stack);
               world.addFreshEntity(entityItem);
            }

            return false;
         }
      }
   }

   private BuilderData calculateBoxShaped(BuilderData data) {
      ItemStack shapeCard = this.items.getStackInSlot(0);
      if (shapeCard.isEmpty()) {
         return data;
      } else {
         BlockPos dimension = ShapeCardItem.getClampedDimension(shapeCard, (Integer)BuilderConfiguration.maxBuilderDimension.get());
         BlockPos offset = ShapeCardItem.getClampedOffset(shapeCard, (Integer)BuilderConfiguration.maxBuilderOffset.get());
         BlockPos minCorner = ShapeCardItem.getMinCorner(this.getBlockPos(), dimension, offset);
         BlockPos maxCorner = ShapeCardItem.getMaxCorner(this.getBlockPos(), dimension, offset);
         int minHeight = this.level.getMinY();
         int maxHeight = this.level.getMaxY();
         if (minCorner.getY() < minHeight) {
            minCorner = new BlockPos(minCorner.getX(), minHeight, minCorner.getZ());
         } else if (minCorner.getY() > maxHeight) {
            minCorner = new BlockPos(minCorner.getX(), maxHeight, minCorner.getZ());
         }

         if (maxCorner.getY() < minHeight) {
            maxCorner = new BlockPos(maxCorner.getX(), minHeight, maxCorner.getZ());
         } else if (maxCorner.getY() > maxHeight) {
            maxCorner = new BlockPos(maxCorner.getX(), maxHeight, maxCorner.getZ());
         }

         if (this.boxValid && minCorner.equals(data.minBox()) && maxCorner.equals(data.maxBox())) {
            return data;
         } else {
            this.boxValid = true;
            this.cardType = ShapeCardItem.getType(shapeCard);
            this.cachedBlocks = null;
            this.cachedChunk = null;
            this.cachedVoidableBlocks.clear();
            data = data.withMinBox(minCorner).withMaxBox(maxCorner);
            return this.restartScan(data);
         }
      }
   }

   private Pair<BuilderData, SpaceChamberRepository.SpaceChamberChannel> calculateBox(BuilderData data) {
      ItemStack card = this.items.getStackInSlot(0);
      if (card.isEmpty()) {
         return Pair.of(data, null);
      } else {
         ShapeCardData shapeData = (ShapeCardData)card.getOrDefault(BuilderModule.ITEM_SHAPECARD_DATA, ShapeCardData.DEFAULT);
         if (shapeData.channel() == -1) {
            return Pair.of(data, null);
         } else {
            SpaceChamberRepository repository = SpaceChamberRepository.get(this.level);
            SpaceChamberRepository.SpaceChamberChannel chamberChannel = repository.getChannel(shapeData.channel());
            if (chamberChannel == null) {
               return Pair.of(data, null);
            } else {
               data = this.calculateBox(data, shapeData.channel());
               return !this.boxValid ? Pair.of(data, null) : Pair.of(data, chamberChannel);
            }
         }
      }
   }

   private Map<BlockPos, BlockState> getCachedBlocks(ChunkPos chunk) {
      if (chunk != null && !chunk.equals(this.cachedChunk) || chunk == null && this.cachedChunk != null) {
         this.cachedBlocks = null;
      }

      if (this.cachedBlocks == null) {
         this.cachedBlocks = new HashMap<>();
         ItemStack shapeCard = this.items.getStackInSlot(0);
         Shape shape = ShapeCardItem.getShape(shapeCard);
         boolean solid = ShapeCardItem.isSolid(shapeCard);
         BlockPos dimension = ShapeCardItem.getClampedDimension(shapeCard, (Integer)BuilderConfiguration.maxBuilderDimension.get());
         BlockPos offset = ShapeCardItem.getClampedOffset(shapeCard, (Integer)BuilderConfiguration.maxBuilderOffset.get());
         boolean forquarry = !ShapeCardItem.isNormalShapeCard(shapeCard);
         ShapeCardItem.composeFormula(
            shapeCard,
            shape.getFormulaFactory().get(),
            this.level,
            this.getBlockPos(),
            dimension,
            offset,
            this.cachedBlocks,
            (Integer)BuilderConfiguration.maxSpaceChamberDimension.get()
               * (Integer)BuilderConfiguration.maxSpaceChamberDimension.get()
               * (Integer)BuilderConfiguration.maxSpaceChamberDimension.get(),
            solid,
            forquarry,
            chunk
         );
         this.cachedChunk = chunk;
      }

      return this.cachedBlocks;
   }

   private BuilderData handleBlockShaped(BuilderData data) {
      for (int i = 0; i < 100; i++) {
         if (data.scan() == null) {
            return data;
         }

         Map<BlockPos, BlockState> blocks = this.getCachedBlocks(new ChunkPos(data.scan().getX() >> 4, data.scan().getZ() >> 4));
         if (blocks.containsKey(data.scan())) {
            BlockState state = blocks.get(data.scan());
            Pair<BuilderData, Boolean> result = this.handleSingleBlock(data, state);
            data = (BuilderData)result.getLeft();
            if (!(Boolean)result.getRight()) {
               data = this.nextLocation(data);
            }

            return data;
         }

         data = this.nextLocation(data);
      }

      return data;
   }

   private ShapeCardType getCardType() {
      return this.cardType == ShapeCardType.CARD_UNKNOWN ? ShapeCardItem.getType(this.items.getStackInSlot(0)) : this.cardType;
   }

   private Pair<BuilderData, Boolean> handleSingleBlock(BuilderData data, BlockState pickState) {
      if (this.level == null) {
         return Pair.of(data, false);
      } else {
         BlockPos srcPos = data.scan();
         int sx = data.scan().getX();
         int sy = data.scan().getY();
         int sz = data.scan().getZ();
         if (!this.chunkLoad(sx, sz)) {
            return this.suspend(data, "Chunk not available!");
         } else {
            int rfNeeded = this.getCardType().getRfNeeded();
            BlockState state = null;
            if (this.getCardType() != ShapeCardType.CARD_SHAPE && this.getCardType() != ShapeCardType.CARD_PUMP_LIQUID) {
               state = this.level.getBlockState(srcPos);
               Block block = state.getBlock();
               if (!isEmpty(state, block)) {
                  float hardness;
                  if (isFluidBlock(block)) {
                     hardness = 1.0F;
                  } else {
                     if (((Set)this.cachedVoidableBlocks.get()).contains(block)) {
                        rfNeeded = (int)(
                           ((Integer)BuilderConfiguration.builderRfPerQuarry.get()).intValue() * (Double)BuilderConfiguration.voidShapeCardFactor.get()
                        );
                     }

                     hardness = state.getDestroySpeed(this.level, srcPos);
                  }

                  rfNeeded *= (int)((hardness + 1.0F) * 2.0F);
               }
            }

            float factor = this.infusable.getInfusedFactor();
            rfNeeded = (int)(rfNeeded * (3.0F - factor) / 3.0F);
            if (rfNeeded > this.energyStorage.getMaxEnergyStored()) {
               return this.skip(data, "Block exceeds max power!");
            } else if (rfNeeded > this.energyStorage.getEnergyStored()) {
               return this.suspend(data, "Not enough power!");
            } else {
               boolean result = this.getCardType().handleSingleBlock(this, rfNeeded, srcPos, state, pickState);
               return Pair.of(data, result);
            }
         }
      }
   }

   public boolean buildBlock(int rfNeeded, BlockPos srcPos, BlockState srcState, BlockState pickState) {
      if (this.level == null) {
         return false;
      } else {
         BuilderData data = (BuilderData)this.getData(BuilderModule.BUILDER_DATA);
         if (isEmptyOrReplacable(this.level, srcPos)) {
            BuilderTileEntity.TakeableItem item = this.createTakeableItem(this.level, srcPos, pickState);
            ItemStack stack = item.peek();
            if (stack.isEmpty()) {
               return this.waitOrSkip(data, "Cannot find block!\nor missing inventory\non top or below");
            }

            Player fakePlayer = this.harvester.get();
            BlockState newState = Tools.placeStackAt(fakePlayer, stack, this.level, srcPos, pickState);
            if (newState == null) {
               return this.waitOrSkip(data, "Cannot place block!");
            }

            if (!ItemStack.matches(stack, item.peek())) {
               if (!stack.isEmpty()) {
                  stack = item.takeAndReplace(stack);
                  if (!stack.isEmpty()) {
                     stack = this.insertItem(stack);
                     if (!stack.isEmpty()) {
                        this.level
                           .addFreshEntity(new ItemEntity(this.level, this.getBlockPos().getX(), this.getBlockPos().getY(), this.getBlockPos().getZ(), stack));
                     }
                  }
               } else {
                  item.take();
               }
            }

            if (!this.isSilent()) {
               SoundType sound = newState.getBlock().getSoundType(newState, this.level, srcPos, fakePlayer);
               this.playPlaceSoundSafe(sound, this.level, newState, srcPos.getX(), srcPos.getY(), srcPos.getZ());
            }

            this.energyStorage.consumeEnergy(rfNeeded);
         }

         return this.skip(data);
      }
   }

   private void playPlaceSoundSafe(SoundType sound, Level world, BlockState state, int x, int y, int z) {
      try {
         SoundTools.playSound(world, sound.getPlaceSound(), x, y, z, 1.0, 1.0);
      } catch (Exception var8) {
         Logging.getLogger().error("Error getting soundtype from " + Tools.getId(state) + "! Please report to the mod owner!");
      }
   }

   private void playBreakSoundSafe(SoundType sound, Level world, BlockState state, int x, int y, int z) {
      try {
         SoundTools.playSound(world, sound.getBreakSound(), x, y, z, 1.0, 1.0);
      } catch (Exception var8) {
         Logging.getLogger().error("Error getting soundtype from " + Tools.getId(state) + "! Please report to the mod owner!");
      }
   }

   private Set<Block> getCachedVoidableBlocks() {
      ItemStack card = this.items.getStackInSlot(0);
      return !card.isEmpty() && card.getItem() instanceof ShapeCardItem ? ShapeCardItem.getVoidedBlocks(card) : Collections.emptySet();
   }

   private void clearOrDirtBlock(int rfNeeded, BlockPos spos, BlockState srcState, boolean clear) {
      this.energyStorage.consumeEnergy(rfNeeded);
      if (!this.isSilent()) {
         SoundType soundType = srcState.getBlock().getSoundType(srcState, this.level, spos, null);
         this.playBreakSoundSafe(soundType, this.level, srcState, spos.getX(), spos.getY(), spos.getZ());
      }

      if (!srcState.is(DONT_REMOVE_ME_TAG)) {
         if (clear) {
            this.level.setBlock(spos, Blocks.AIR.defaultBlockState(), 2);
         } else {
            this.level.setBlock(spos, this.getReplacementBlock(), 2);
         }
      }
   }

   private BlockState getReplacementBlock() {
      return BuilderConfiguration.getQuarryReplace();
   }

   public boolean silkQuarryBlock(int rfNeeded, BlockPos srcPos, BlockState srcState, BlockState pickState) {
      return this.commonQuarryBlock(true, rfNeeded, srcPos, srcState);
   }

   private Predicate<ItemStack> createFilterCache() {
      return FilterModuleItem.getCache(this.items.getStackInSlot(1));
   }

   private Pair<BuilderData, Boolean> allowedToBreak(BuilderData data, BlockState state, Level world, BlockPos pos, Player player) {
      if (!state.getBlock().canEntityDestroy(state, world, pos, player)) {
         return this.skip(data, "Cannot destroy!\nAre fake players\nallowed?");
      } else {
         BreakBlockEvent event = new BreakBlockEvent(world, pos, state, player);
         NeoForge.EVENT_BUS.post(event);
         return event.isCanceled() ? this.skip(data, "Break was canceled!") : Pair.of(data, true);
      }
   }

   private static boolean allowedToBreakS(BlockState state, Level world, BlockPos pos, Player player) {
      if (!state.getBlock().canEntityDestroy(state, world, pos, player)) {
         return false;
      } else {
         BreakBlockEvent event = new BreakBlockEvent(world, pos, state, player);
         NeoForge.EVENT_BUS.post(event);
         return !event.isCanceled();
      }
   }

   public boolean quarryBlock(int rfNeeded, BlockPos srcPos, BlockState srcState, BlockState pickState) {
      return this.commonQuarryBlock(false, rfNeeded, srcPos, srcState);
   }

   private static ItemStack getHarvesterTool(Level level, boolean silk, int fortune) {
      Registry<Enchantment> enchantments = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
      if (silk) {
         if (TOOL_SILK == null || TOOL_SILK.isEmpty()) {
            TOOL_SILK = new ItemStack((ItemLike)BuilderModule.SUPER_HARVESTING_TOOL.get());
            TOOL_SILK.enchant(enchantments.getOrThrow(Enchantments.SILK_TOUCH), 1);
         }

         return TOOL_SILK;
      } else if (fortune > 0) {
         if (TOOL_FORTUNE == null || TOOL_FORTUNE.isEmpty()) {
            TOOL_FORTUNE = new ItemStack((ItemLike)BuilderModule.SUPER_HARVESTING_TOOL.get());
            TOOL_FORTUNE.enchant(enchantments.getOrThrow(Enchantments.FORTUNE), fortune);
         }

         return TOOL_FORTUNE;
      } else {
         if (TOOL_NORMAL == null || TOOL_NORMAL.isEmpty()) {
            TOOL_NORMAL = new ItemStack((ItemLike)BuilderModule.SUPER_HARVESTING_TOOL.get());
         }

         return TOOL_NORMAL;
      }
   }

   private boolean commonQuarryBlock(boolean silk, int rfNeeded, BlockPos srcPos, BlockState srcState) {
      BuilderData data = (BuilderData)this.getData(BuilderModule.BUILDER_DATA);
      Block block = srcState.getBlock();
      int xCoord = this.getBlockPos().getX();
      int yCoord = this.getBlockPos().getY();
      int zCoord = this.getBlockPos().getZ();
      int sx = srcPos.getX();
      int sy = srcPos.getY();
      int sz = srcPos.getZ();
      if (sx >= xCoord - 1 && sx <= xCoord + 1 && sy >= yCoord - 1 && sy <= yCoord + 1 && sz >= zCoord - 1 && sz <= zCoord + 1) {
         return this.skip(data);
      } else if (isEmpty(srcState, block)) {
         return this.skip(data);
      } else {
         if (srcState.getDestroySpeed(this.level, srcPos) >= 0.0F) {
            boolean clear = this.getCardType().isClearing();
            if (!clear && srcState == this.getReplacementBlock()) {
               return this.skip(data);
            }

            if (!(Boolean)BuilderConfiguration.quarryTileEntities.get() && this.level.getBlockEntity(srcPos) != null) {
               return this.skip(data);
            }

            Player fakePlayer = this.harvester.get();
            Pair<BuilderData, Boolean> result = this.allowedToBreak(data, srcState, this.level, srcPos, fakePlayer);
            data = (BuilderData)result.getLeft();
            this.setData(BuilderModule.BUILDER_DATA, data);
            if (!(Boolean)result.getRight()) {
               return this.waitOrSkip(data, data.lastError());
            }

            ItemStack filter = this.items.getStackInSlot(1);
            if (!filter.isEmpty() && this.filterCache.get() != null) {
               boolean match;
               try {
                  match = ((Predicate)this.filterCache.get()).test(block.getCloneItemStack(this.level, srcPos, srcState, true, null));
               } catch (Exception var20) {
                  match = false;
               }

               if (!match) {
                  this.energyStorage.consumeEnergy(Math.min(rfNeeded, (Integer)BuilderConfiguration.builderRfPerSkipped.get()));
                  return this.skip(data);
               }
            }

            if (!((Set)this.cachedVoidableBlocks.get()).contains(block)) {
               if (!this.overflowItems.isEmpty()) {
                  return this.waitOrSkip(data, "Not enough room!\nor no usable storage\non top or below!");
               }

               int fortune = this.getCardType().isFortune() ? 3 : 0;
               Builder builder = new Builder((ServerLevel)this.level)
                  .withParameter(LootContextParams.ORIGIN, new Vec3(srcPos.getX(), srcPos.getY(), srcPos.getZ()))
                  .withParameter(LootContextParams.TOOL, getHarvesterTool(this.level, silk, fortune))
                  .withParameter(LootContextParams.THIS_ENTITY, fakePlayer)
                  .withOptionalParameter(LootContextParams.BLOCK_ENTITY, this.level.getBlockEntity(srcPos));
               if (fortune > 0) {
                  builder.withLuck(fortune);
               }

               List<ItemStack> drops = srcState.getDrops(builder);
               if (this.checkValidItems(block, drops) && !this.insertItems(drops)) {
                  this.clearOrDirtBlock(rfNeeded, srcPos, srcState, clear);
                  return this.waitOrSkip(data, "Not enough room!\nor no usable storage\non top or below!");
               }
            }

            this.clearOrDirtBlock(rfNeeded, srcPos, srcState, clear);
         }

         return false;
      }
   }

   private static boolean isFluidBlock(Block block) {
      return block instanceof LiquidBlock;
   }

   private static int getFluidLevel(BlockState srcState) {
      return srcState.getBlock() instanceof LiquidBlock ? (Integer)srcState.getValue(LiquidBlock.LEVEL) : -1;
   }

   public boolean placeLiquidBlock(int rfNeeded, BlockPos srcPos, BlockState srcState, BlockState pickState) {
      BuilderData data = (BuilderData)this.getData(BuilderModule.BUILDER_DATA);
      if (isEmptyOrReplacable(this.level, srcPos)) {
         FluidStack stack = this.consumeLiquid(this.level, srcPos);
         if (stack.isEmpty()) {
            return this.waitOrSkip(data, "Cannot find liquid!\nor no usable tank\nabove or below");
         }

         Fluid fluid = stack.getFluid();
         if (fluid.getFluidType().isVaporizedOnPlacement(this.level, srcPos, stack)) {
            fluid.getFluidType().onVaporize(null, this.level, srcPos, stack);
         } else {
            Block block = fluid.defaultFluidState().createLegacyBlock().getBlock();
            Player fakePlayer = this.harvester.get();
            this.level.setBlock(srcPos, block.defaultBlockState(), 11);
            if (!this.isSilent()) {
               SoundType soundType = block.getSoundType(block.defaultBlockState(), this.level, srcPos, fakePlayer);
               this.playPlaceSoundSafe(soundType, this.level, block.defaultBlockState(), srcPos.getX(), srcPos.getY(), srcPos.getZ());
            }
         }

         this.energyStorage.consumeEnergy(rfNeeded);
      }

      return this.skip(data);
   }

   public boolean pumpBlock(int rfNeeded, BlockPos srcPos, BlockState srcState, BlockState pickState) {
      BuilderData data = (BuilderData)this.getData(BuilderModule.BUILDER_DATA);
      Block block = srcState.getBlock();
      FluidState fluidState = this.level.getFluidState(srcPos);
      if (fluidState.isEmpty()) {
         return this.skip(data);
      } else if (!fluidState.isSource()) {
         return this.skip(data);
      } else {
         FluidStack fluidStack = FluidTools.pickupFluidBlock(this.level, srcPos, s -> false, () -> {});
         if (fluidStack.isEmpty()) {
            return this.skip(data);
         } else if (srcState.getDestroySpeed(this.level, srcPos) >= 0.0F) {
            Player fakePlayer = this.harvester.get();
            Pair<BuilderData, Boolean> result = this.allowedToBreak(data, srcState, this.level, srcPos, fakePlayer);
            data = (BuilderData)result.getLeft();
            this.setData(BuilderModule.BUILDER_DATA, data);
            if ((Boolean)result.getRight()) {
               if (this.checkAndInsertFluids(fluidStack)) {
                  this.energyStorage.consumeEnergy(rfNeeded);
                  boolean clear = this.getCardType().isClearing();
                  FluidTools.pickupFluidBlock(this.level, srcPos, s -> true, () -> {
                     if (clear) {
                        this.level.setBlock(srcPos, Blocks.AIR.defaultBlockState(), 2);
                     } else {
                        this.level.setBlock(srcPos, this.getReplacementBlock(), 2);
                     }
                  });
                  if (!this.isSilent()) {
                     SoundType soundType = block.getSoundType(srcState, this.level, srcPos, fakePlayer);
                     this.playBreakSoundSafe(soundType, this.level, srcState, srcPos.getX(), srcPos.getY(), srcPos.getZ());
                  }

                  return this.skip(data);
               } else {
                  return this.waitOrSkip(data, "No room for liquid\nor no usable tank\nabove or below!");
               }
            } else {
               return this.waitOrSkip(data, data.lastError());
            }
         } else {
            return this.skip(data);
         }
      }
   }

   public boolean voidBlock(int rfNeeded, BlockPos srcPos, BlockState srcState, BlockState pickState) {
      BuilderData data = (BuilderData)this.getData(BuilderModule.BUILDER_DATA);
      Block block = srcState.getBlock();
      int xCoord = this.getBlockPos().getX();
      int yCoord = this.getBlockPos().getY();
      int zCoord = this.getBlockPos().getZ();
      int sx = srcPos.getX();
      int sy = srcPos.getY();
      int sz = srcPos.getZ();
      if (sx >= xCoord - 1 && sx <= xCoord + 1 && sy >= yCoord - 1 && sy <= yCoord + 1 && sz >= zCoord - 1 && sz <= zCoord + 1) {
         return this.skip(data);
      } else {
         Player fakePlayer = this.harvester.get();
         Pair<BuilderData, Boolean> result = this.allowedToBreak(data, srcState, this.level, srcPos, fakePlayer);
         data = (BuilderData)result.getLeft();
         this.setData(BuilderModule.BUILDER_DATA, data);
         if ((Boolean)result.getRight()) {
            assert this.level != null;

            if (srcState.getDestroySpeed(this.level, srcPos) >= 0.0F) {
               ItemStack filter = this.items.getStackInSlot(1);
               if (!filter.isEmpty() && this.filterCache.get() != null) {
                  boolean match = ((Predicate)this.filterCache.get()).test(block.getCloneItemStack(this.level, srcPos, srcState, true, null));
                  if (!match) {
                     this.energyStorage.consumeEnergy(Math.min(rfNeeded, (Integer)BuilderConfiguration.builderRfPerSkipped.get()));
                     return this.skip(data);
                  }
               }

               if (!this.isSilent()) {
                  SoundType soundType = block.getSoundType(srcState, this.level, srcPos, fakePlayer);
                  this.playBreakSoundSafe(soundType, this.level, srcState, sx, sy, sz);
               }

               this.level.setBlockAndUpdate(srcPos, Blocks.AIR.defaultBlockState());
               this.energyStorage.consumeEnergy(rfNeeded);
            }

            return this.skip(data);
         } else {
            return this.waitOrSkip(data, data.lastError());
         }
      }
   }

   private BuilderData handleBlock(BuilderData data, Level world) {
      BlockPos srcPos = data.scan();
      BlockPos destPos = this.sourceToDest(data.scan());
      int x = data.scan().getX();
      int y = data.scan().getY();
      int z = data.scan().getZ();
      int destX = destPos.getX();
      int destY = destPos.getY();
      int destZ = destPos.getZ();
      switch (this.getMode()) {
         case MODE_COPY:
            this.copyBlock(world, srcPos, world, destPos);
            break;
         case MODE_MOVE:
            if (this.hasEntityMode()) {
               this.moveEntities(world, x, y, z, world, destX, destY, destZ);
            }

            this.moveBlock(world, srcPos, world, destPos, this.getRotate());
            break;
         case MODE_BACK:
            if (this.hasEntityMode()) {
               this.moveEntities(world, destX, destY, destZ, world, x, y, z);
            }

            this.moveBlock(world, destPos, world, srcPos, this.oppositeRotate());
            break;
         case MODE_SWAP:
            if (this.hasEntityMode()) {
               this.swapEntities(world, x, y, z, world, destX, destY, destZ);
            }

            this.swapBlock(world, srcPos, world, destPos);
      }

      return this.nextLocation(data);
   }

   private BuilderTileEntity.TakeableItem findBlockTakeableItem(IItemHandler inventory, Level srcWorld, BlockPos srcPos, BlockState state) {
      if (state == null) {
         List<Integer> slots = new ArrayList<>();

         for (int i = 0; i < inventory.getSlots(); i++) {
            if (this.isPlacable(inventory.getStackInSlot(i))) {
               slots.add(i);
            }
         }

         if (!slots.isEmpty()) {
            return new BuilderTileEntity.TakeableItem(inventory, slots.get(random.nextInt(slots.size())));
         }
      } else {
         Block block = state.getBlock();
         ItemStack srcItem = block.getCloneItemStack(srcWorld, srcPos, state, true, null);
         if (this.isPlacable(srcItem)) {
            for (int ix = 0; ix < inventory.getSlots(); ix++) {
               ItemStack stack = inventory.getStackInSlot(ix);
               if (!stack.isEmpty() && ItemStack.isSameItem(stack, srcItem)) {
                  return new BuilderTileEntity.TakeableItem(inventory, ix);
               }
            }
         }
      }

      return BuilderTileEntity.TakeableItem.EMPTY;
   }

   private boolean isPlacable(ItemStack stack) {
      if (stack.isEmpty()) {
         return false;
      } else {
         Item item = stack.getItem();
         return item instanceof BlockItem || item instanceof SpecialPlantable;
      }
   }

   private boolean checkValidItems(Block block, List<ItemStack> items) {
      for (ItemStack stack : items) {
         if (!stack.isEmpty() && stack.getItem() == null) {
            Logging.logError("Builder tried to quarry " + Tools.getId(block).toString() + " and it returned null item!");
            Broadcaster.broadcast(
               this.level,
               this.worldPosition.getX(),
               this.worldPosition.getY(),
               this.worldPosition.getZ(),
               "Builder tried to quarry " + Tools.getId(block).toString() + " and it returned null item!\nPlease report to mod author!",
               10.0F
            );
            return false;
         }
      }

      return true;
   }

   private boolean checkAndInsertFluids(FluidStack fluid) {
      return this.checkFluidTank(fluid, this.getBlockPos().above(), Direction.DOWN)
         ? true
         : this.checkFluidTank(fluid, this.getBlockPos().below(), Direction.UP);
   }

   private boolean checkFluidTank(FluidStack fluidStack, BlockPos up, Direction side) {
      BlockEntity te = this.level.getBlockEntity(up);
      if (te != null) {
         IFluidHandler h = CapabilityTools.getFluidCapability(this.level, up, side);
         if (h != null) {
            int amount = h.fill(fluidStack, FluidAction.SIMULATE);
            if (amount == 1000) {
               h.fill(fluidStack, FluidAction.EXECUTE);
               return true;
            }

            return false;
         }
      }

      return false;
   }

   private void handleItemInsertion(@Nullable BlockEntity te, Direction direction, List<ItemStack> items, LazyList<ItemStack> couldntHandle) {
      if (te == null) {
         couldntHandle.copyList(items);
      } else {
         IItemHandler capability = CapabilityTools.getItemCapability(te.getLevel(), te.getBlockPos(), direction);
         if (capability == null) {
            couldntHandle.copyList(items);
         } else {
            couldntHandle.clear();

            for (ItemStack item : items) {
               ItemStack overflow = ItemHandlerHelper.insertItem(capability, item, false);
               if (!overflow.isEmpty()) {
                  couldntHandle.add(overflow);
               }
            }
         }
      }
   }

   private boolean insertItems(List<ItemStack> items) {
      this.handleItemInsertion(this.level.getBlockEntity(this.worldPosition.above()), Direction.DOWN, items, this.couldntHandle1);
      if (this.couldntHandle1.isEmpty()) {
         return true;
      } else {
         this.handleItemInsertion(this.level.getBlockEntity(this.worldPosition.below()), Direction.UP, this.couldntHandle1.getList(), this.couldntHandle2);
         if (this.couldntHandle2.isEmpty()) {
            return true;
         } else {
            this.overflowItems.copyList(this.couldntHandle2.getList());
            return false;
         }
      }
   }

   private ItemStack insertItem(@Nonnull ItemStack s) {
      s = InventoryTools.insertItem(this.level, this.getBlockPos(), Direction.UP, s);
      if (!s.isEmpty()) {
         s = InventoryTools.insertItem(this.level, this.getBlockPos(), Direction.DOWN, s);
      }

      return s;
   }

   private BuilderTileEntity.TakeableItem createTakeableItem(Direction direction, Level srcWorld, BlockPos srcPos, BlockState state) {
      BlockEntity te = this.level.getBlockEntity(this.getBlockPos().relative(direction));
      if (te != null) {
         IItemHandler h = CapabilityTools.getItemCapability(this.level, te.getBlockPos(), direction.getOpposite());
         if (h != null) {
            return this.findBlockTakeableItem(h, srcWorld, srcPos, state);
         }
      }

      return BuilderTileEntity.TakeableItem.EMPTY;
   }

   @Nonnull
   private FluidStack consumeLiquid(Level srcWorld, BlockPos srcPos) {
      FluidStack b = this.consumeLiquid(Direction.UP, srcWorld, srcPos);
      if (b.isEmpty()) {
         b = this.consumeLiquid(Direction.DOWN, srcWorld, srcPos);
      }

      return b;
   }

   @Nonnull
   private FluidStack consumeLiquid(Direction direction, Level srcWorld, BlockPos srcPos) {
      BlockEntity te = this.level.getBlockEntity(this.getBlockPos().relative(direction));
      if (te != null) {
         IFluidHandler fluid = CapabilityTools.getFluidCapability(this.level, te.getBlockPos(), direction.getOpposite());
         if (fluid != null) {
            fluid = CapabilityTools.getFluidCapability(this.level, te.getBlockPos(), null);
         }

         if (fluid != null) {
            return this.findAndConsumeLiquid(fluid, srcWorld, srcPos);
         }
      }

      return FluidStack.EMPTY;
   }

   @Nonnull
   private FluidStack findAndConsumeLiquid(IFluidHandler tank, Level srcWorld, BlockPos srcPos) {
      for (int i = 0; i < tank.getTanks(); i++) {
         FluidStack contents = tank.getFluidInTank(i);
         if (!contents.isEmpty() && contents.getFluid() != null && contents.getAmount() >= 1000) {
            return tank.drain(new FluidStack(contents.getFluid(), 1000), FluidAction.EXECUTE);
         }
      }

      return FluidStack.EMPTY;
   }

   private BuilderTileEntity.TakeableItem createTakeableItem(Level srcWorld, BlockPos srcPos, BlockState state) {
      BuilderTileEntity.TakeableItem b = this.createTakeableItem(Direction.UP, srcWorld, srcPos, state);
      if (b.peek().isEmpty()) {
         b = this.createTakeableItem(Direction.DOWN, srcWorld, srcPos, state);
      }

      return b;
   }

   public static BlockInformation getBlockInformation(Player fakePlayer, Level world, BlockPos pos, Block block, BlockEntity tileEntity) {
      BlockState state = world.getBlockState(pos);
      if (isEmpty(state, block)) {
         return BlockInformation.FREE;
      } else if (!allowedToBreakS(state, world, pos, fakePlayer)) {
         return BlockInformation.INVALID;
      } else {
         BlockInformation blockInformation = BlockInformation.getBlockInformation(block);
         if (tileEntity != null) {
            switch ((BuilderTileEntityMode)BuilderConfiguration.teMode.get()) {
               case MOVE_FORBIDDEN:
                  return BlockInformation.INVALID;
               case MOVE_WHITELIST:
                  if (blockInformation == null || blockInformation.getBlockLevel() == SupportBlock.SupportStatus.STATUS_ERROR) {
                     return BlockInformation.INVALID;
                  }
                  break;
               case MOVE_BLACKLIST:
                  if (blockInformation != null && blockInformation.getBlockLevel() == SupportBlock.SupportStatus.STATUS_ERROR) {
                     return BlockInformation.INVALID;
                  }
               case MOVE_ALLOWED:
            }
         }

         return blockInformation != null ? blockInformation : BlockInformation.OK;
      }
   }

   private SupportBlock.SupportStatus isMovable(Player harvester, Level world, BlockPos pos, Block block, BlockEntity tileEntity) {
      return getBlockInformation(harvester, world, pos, block, tileEntity).getBlockLevel();
   }

   public static boolean isEmptyOrReplacable(Level world, BlockPos pos) {
      BlockState state = world.getBlockState(pos);
      Block block = state.getBlock();
      return state.canBeReplaced() ? true : isEmpty(state, block);
   }

   public static boolean isEmpty(BlockState state, Block block) {
      if (block == null) {
         return true;
      } else {
         return state.isAir() ? true : block == BuilderModule.SUPPORT.get();
      }
   }

   private void clearBlock(Level world, BlockPos pos) {
      if (this.hasSupportMode()) {
         world.setBlock(pos, ((SupportBlock)BuilderModule.SUPPORT.get()).defaultBlockState(), 3);
      } else {
         world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
      }
   }

   private RotateMode oppositeRotate() {
      return switch (this.getRotate()) {
         case ROTATE_90 -> RotateMode.ROTATE_270;
         case ROTATE_270 -> RotateMode.ROTATE_90;
         default -> this.getRotate();
      };
   }

   private void copyBlock(Level srcWorld, BlockPos srcPos, Level destWorld, BlockPos destPos) {
      long rf = this.energyStorage.getEnergy();
      float factor = this.infusable.getInfusedFactor();
      int rfNeeded = (int)(
         ((Integer)BuilderConfiguration.builderRfPerOperation.get()).intValue() * this.getDimensionCostFactor(srcWorld, destWorld) * (4.0F - factor) / 4.0
      );
      if (rfNeeded <= rf) {
         if (isEmptyOrReplacable(destWorld, destPos)) {
            if (srcWorld.isEmptyBlock(srcPos)) {
               return;
            }

            BlockState srcState = srcWorld.getBlockState(srcPos);
            BuilderTileEntity.TakeableItem takeableItem = this.createTakeableItem(srcWorld, srcPos, srcState);
            ItemStack consumedStack = takeableItem.peek();
            if (consumedStack.isEmpty()) {
               return;
            }

            Player fakePlayer = this.harvester.get();
            BlockState newState = Tools.placeStackAt(fakePlayer, consumedStack, destWorld, destPos, srcState);
            if (newState == null) {
               return;
            }

            if (destWorld.getBlockState(destPos).is(newState.getBlock())) {
               BlockEntity srcTileEntity = srcWorld.getBlockEntity(srcPos);
               CompoundTag srcTC = null;
               if (srcTileEntity != null) {
                  Block srcBlock = srcState.getBlock();
                  srcTC = this.copyBlockNBTData(srcBlock, srcTileEntity);
               }

               destWorld.setBlock(destPos, newState, 3);
               if (srcTC != null) {
                  this.transferBlockNBTDataTo(destWorld, srcTC, destPos, srcState);
               }
            }

            if (!ItemStack.matches(consumedStack, takeableItem.peek())) {
               if (!consumedStack.isEmpty()) {
                  consumedStack = takeableItem.takeAndReplace(consumedStack);
                  if (!consumedStack.isEmpty()) {
                     consumedStack = this.insertItem(consumedStack);
                     if (!consumedStack.isEmpty()) {
                        this.level
                           .addFreshEntity(
                              new ItemEntity(this.level, this.getBlockPos().getX(), this.getBlockPos().getY(), this.getBlockPos().getZ(), consumedStack)
                           );
                     }
                  }
               } else {
                  takeableItem.take();
               }
            }

            if (!this.isSilent()) {
               SoundType soundType = newState.getBlock().getSoundType(newState, destWorld, destPos, fakePlayer);
               this.playPlaceSoundSafe(soundType, destWorld, newState, destPos.getX(), destPos.getY(), destPos.getZ());
            }

            this.energyStorage.consumeEnergy(rfNeeded);
         }
      }
   }

   private double getDimensionCostFactor(Level world, Level destWorld) {
      return Objects.equals(destWorld.dimension(), world.dimension()) ? 1.0 : (Double)BuilderConfiguration.dimensionCostFactor.get();
   }

   private boolean consumeEntityEnergy(int rfNeeded, int rfNeededPlayer, Entity entity) {
      long rf = this.energyStorage.getEnergy();
      int rfn;
      if (entity instanceof Player) {
         rfn = rfNeededPlayer;
      } else {
         rfn = rfNeeded;
      }

      if (rfn > rf) {
         return true;
      } else {
         this.energyStorage.consumeEnergy(rfn);
         return false;
      }
   }

   private void moveEntities(Level world, int x, int y, int z, Level destWorld, int destX, int destY, int destZ) {
      float factor = this.infusable.getInfusedFactor();
      int rfNeeded = (int)(
         ((Integer)BuilderConfiguration.builderRfPerEntity.get()).intValue() * this.getDimensionCostFactor(world, destWorld) * (4.0F - factor) / 4.0
      );
      int rfNeededPlayer = (int)(
         ((Integer)BuilderConfiguration.builderRfPerPlayer.get()).intValue() * this.getDimensionCostFactor(world, destWorld) * (4.0F - factor) / 4.0
      );

      for (Entity entity : world.getEntities(null, new AABB(x - 0.1, y - 0.1, z - 0.1, x + 1.1, y + 1.1, z + 1.1))) {
         if (this.consumeEntityEnergy(rfNeeded, rfNeededPlayer, entity)) {
            return;
         }

         double newX = destX + (entity.getX() - x);
         double newY = destY + (entity.getY() - y);
         double newZ = destZ + (entity.getZ() - z);
         this.teleportEntity(world, destWorld, entity, newX, newY, newZ);
      }
   }

   private void swapEntities(Level world, int x, int y, int z, Level destWorld, int destX, int destY, int destZ) {
      float factor = this.infusable.getInfusedFactor();
      int rfNeeded = (int)(
         ((Integer)BuilderConfiguration.builderRfPerEntity.get()).intValue() * this.getDimensionCostFactor(world, destWorld) * (4.0F - factor) / 4.0
      );
      int rfNeededPlayer = (int)(
         ((Integer)BuilderConfiguration.builderRfPerPlayer.get()).intValue() * this.getDimensionCostFactor(world, destWorld) * (4.0F - factor) / 4.0
      );
      List<Entity> entitiesSrc = world.getEntities(null, new AABB(x, y, z, x + 1, y + 1, z + 1));
      List<Entity> entitiesDst = destWorld.getEntities(null, new AABB(destX, destY, destZ, destX + 1, destY + 1, destZ + 1));

      for (Entity entity : entitiesSrc) {
         if (this.isEntityInBlock(x, y, z, entity)) {
            if (this.consumeEntityEnergy(rfNeeded, rfNeededPlayer, entity)) {
               return;
            }

            double newX = destX + (entity.getX() - x);
            double newY = destY + (entity.getY() - y);
            double newZ = destZ + (entity.getZ() - z);
            this.teleportEntity(world, destWorld, entity, newX, newY, newZ);
         }
      }

      for (Entity entityx : entitiesDst) {
         if (this.isEntityInBlock(destX, destY, destZ, entityx)) {
            if (this.consumeEntityEnergy(rfNeeded, rfNeededPlayer, entityx)) {
               return;
            }

            double newX = x + (entityx.getX() - destX);
            double newY = y + (entityx.getY() - destY);
            double newZ = z + (entityx.getZ() - destZ);
            this.teleportEntity(destWorld, world, entityx, newX, newY, newZ);
         }
      }
   }

   private void teleportEntity(Level world, Level destWorld, Entity entity, double newX, double newY, double newZ) {
      TeleportationTools.teleportEntity(entity, destWorld, newX, newY, newZ, null);
   }

   private boolean isEntityInBlock(int x, int y, int z, Entity entity) {
      return entity.getX() >= x && entity.getX() < x + 1 && entity.getY() >= y && entity.getY() < y + 1 && entity.getZ() >= z && entity.getZ() < z + 1;
   }

   private CompoundTag copyBlockNBTData(Block srcBlock, BlockEntity srcTileEntity) {
      if (BlockInformation.shouldTransferNBT(srcBlock)) {
         RegistryAccess access = srcTileEntity.getLevel().registryAccess();
         return srcTileEntity.saveWithFullMetadata(access);
      } else {
         return null;
      }
   }

   private void moveBlock(Level srcWorld, BlockPos srcPos, Level destWorld, BlockPos destPos, RotateMode rotMode) {
      BlockState oldDestState = destWorld.getBlockState(destPos);
      Block oldDestBlock = oldDestState.getBlock();
      if (isEmpty(oldDestState, oldDestBlock)) {
         BlockState srcState = srcWorld.getBlockState(srcPos);
         Block srcBlock = srcState.getBlock();
         if (isEmpty(srcState, srcBlock)) {
            return;
         }

         BlockEntity srcTileEntity = srcWorld.getBlockEntity(srcPos);
         CompoundTag tc = null;
         if (srcTileEntity != null) {
            tc = this.copyBlockNBTData(srcBlock, srcTileEntity);
         }

         BlockInformation srcInformation = getBlockInformation(this.harvester.get(), srcWorld, srcPos, srcBlock, srcTileEntity);
         if (srcInformation.getBlockLevel() == SupportBlock.SupportStatus.STATUS_ERROR) {
            return;
         }

         long rf = this.energyStorage.getEnergy();
         float factor = this.infusable.getInfusedFactor();
         int rfNeeded = (int)(
            ((Integer)BuilderConfiguration.builderRfPerOperation.get()).intValue()
               * this.getDimensionCostFactor(srcWorld, destWorld)
               * srcInformation.getCostFactor()
               * (4.0F - factor)
               / 4.0
         );
         if (rfNeeded > rf) {
            return;
         }

         this.energyStorage.consumeEnergy(rfNeeded);
         if (srcTileEntity != null) {
            srcWorld.removeBlockEntity(srcPos);
         }

         this.clearBlock(srcWorld, srcPos);
         destWorld.setBlock(destPos, srcState, 3);
         if (tc != null) {
            this.configureTileEntityNBT(destWorld, tc, destPos, srcState);
         }

         if (!this.isSilent()) {
            SoundType srcSoundType = srcBlock.getSoundType(srcState, srcWorld, srcPos, null);
            this.playBreakSoundSafe(srcSoundType, srcWorld, srcState, srcPos.getX(), srcPos.getY(), srcPos.getZ());
            SoundType dstSoundtype = srcBlock.getSoundType(srcState, destWorld, destPos, null);
            this.playPlaceSoundSafe(dstSoundtype, destWorld, srcState, destPos.getX(), destPos.getY(), destPos.getZ());
         }
      }
   }

   private void setTileEntityNBT(Level destWorld, CompoundTag tc, BlockPos destpos, BlockState newDestState) {
      tc.putInt("x", destpos.getX());
      tc.putInt("y", destpos.getY());
      tc.putInt("z", destpos.getZ());
      BlockEntity tileEntity = BlockEntity.loadStatic(destpos, newDestState, tc, destWorld.registryAccess());
      if (tileEntity != null) {
         destWorld.getChunk(destpos).setBlockEntity(tileEntity);
         tileEntity.setChanged();
         destWorld.sendBlockUpdated(destpos, newDestState, newDestState, 3);
      }
   }

   private void configureTileEntityNBT(Level destWorld, CompoundTag tc, BlockPos destpos, BlockState newDestState) {
      BlockEntity tileEntity = destWorld.getBlockEntity(destpos);
      if (tileEntity != null) {
         tileEntity.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, destWorld.registryAccess(), tc));
         tileEntity.setChanged();
         destWorld.sendBlockUpdated(destpos, newDestState, newDestState, 3);
      }
   }

   private void transferBlockNBTDataTo(Level destWorld, CompoundTag tc, BlockPos destpos, BlockState newDestState) {
      tc.putInt("x", destpos.getX());
      tc.putInt("y", destpos.getY());
      tc.putInt("z", destpos.getZ());
      this.configureTileEntityNBT(destWorld, tc, destpos, newDestState);
   }

   private void swapBlock(Level srcWorld, BlockPos srcPos, Level destWorld, BlockPos dstPos) {
      BlockState oldSrcState = srcWorld.getBlockState(srcPos);
      Block srcBlock = oldSrcState.getBlock();
      BlockEntity srcTileEntity = srcWorld.getBlockEntity(srcPos);
      BlockState oldDstState = destWorld.getBlockState(dstPos);
      Block dstBlock = oldDstState.getBlock();
      BlockEntity dstTileEntity = destWorld.getBlockEntity(dstPos);
      CompoundTag srcTC = null;
      CompoundTag dstTC = null;
      if (srcTileEntity != null) {
         srcTC = this.copyBlockNBTData(srcBlock, srcTileEntity);
      }

      if (dstTileEntity != null) {
         dstTC = this.copyBlockNBTData(dstBlock, dstTileEntity);
      }

      if (!isEmpty(oldSrcState, srcBlock) || !isEmpty(oldDstState, dstBlock)) {
         BlockInformation srcInformation = getBlockInformation(this.harvester.get(), srcWorld, srcPos, srcBlock, srcTileEntity);
         if (srcInformation.getBlockLevel() != SupportBlock.SupportStatus.STATUS_ERROR) {
            BlockInformation dstInformation = getBlockInformation(this.harvester.get(), destWorld, dstPos, dstBlock, dstTileEntity);
            if (dstInformation.getBlockLevel() != SupportBlock.SupportStatus.STATUS_ERROR) {
               long rf = this.energyStorage.getEnergy();
               float factor = this.infusable.getInfusedFactor();
               int rfNeeded = (int)(
                  ((Integer)BuilderConfiguration.builderRfPerOperation.get()).intValue()
                     * this.getDimensionCostFactor(srcWorld, destWorld)
                     * srcInformation.getCostFactor()
                     * (4.0F - factor)
                     / 4.0
               );
               rfNeeded += (int)(
                  ((Integer)BuilderConfiguration.builderRfPerOperation.get()).intValue()
                     * this.getDimensionCostFactor(srcWorld, destWorld)
                     * dstInformation.getCostFactor()
                     * (4.0F - factor)
                     / 4.0
               );
               if (rfNeeded <= rf) {
                  this.energyStorage.consumeEnergy(rfNeeded);
                  srcWorld.removeBlockEntity(srcPos);
                  srcWorld.setBlockAndUpdate(srcPos, Blocks.AIR.defaultBlockState());
                  destWorld.removeBlockEntity(dstPos);
                  destWorld.setBlockAndUpdate(dstPos, Blocks.AIR.defaultBlockState());
                  destWorld.setBlock(dstPos, oldSrcState, 3);
                  if (srcTC != null) {
                     this.configureTileEntityNBT(destWorld, srcTC, dstPos, oldSrcState);
                  }

                  srcWorld.setBlock(srcPos, oldDstState, 3);
                  if (dstTC != null) {
                     this.configureTileEntityNBT(srcWorld, dstTC, srcPos, oldDstState);
                  }

                  if (!this.isSilent()) {
                     if (!isEmpty(oldSrcState, srcBlock)) {
                        SoundType srcSoundType = srcBlock.getSoundType(oldSrcState, srcWorld, srcPos, null);
                        this.playBreakSoundSafe(srcSoundType, srcWorld, oldSrcState, srcPos.getX(), srcPos.getY(), srcPos.getZ());
                        SoundType dstSoundType = srcBlock.getSoundType(oldSrcState, destWorld, dstPos, null);
                        this.playPlaceSoundSafe(dstSoundType, destWorld, oldSrcState, dstPos.getX(), dstPos.getY(), dstPos.getZ());
                     }

                     if (!isEmpty(oldDstState, dstBlock)) {
                        SoundType srcSoundType = dstBlock.getSoundType(oldDstState, destWorld, dstPos, null);
                        this.playBreakSoundSafe(srcSoundType, destWorld, oldDstState, dstPos.getX(), dstPos.getY(), dstPos.getZ());
                        SoundType dstSoundType = dstBlock.getSoundType(oldDstState, srcWorld, srcPos, null);
                        this.playPlaceSoundSafe(dstSoundType, srcWorld, oldDstState, srcPos.getX(), srcPos.getY(), srcPos.getZ());
                     }
                  }
               }
            }
         }
      }
   }

   private BlockPos sourceToDest(BlockPos source) {
      return this.rotate(source).offset(this.projDx, this.projDy, this.projDz);
   }

   private BlockPos rotate(BlockPos c) {
      switch (this.getRotate()) {
         case ROTATE_90:
            return new BlockPos(-c.getZ(), c.getY(), c.getX());
         case ROTATE_270:
            return new BlockPos(c.getZ(), c.getY(), -c.getX());
         case ROTATE_0:
            return c;
         case ROTATE_180:
            return new BlockPos(-c.getX(), c.getY(), -c.getZ());
         default:
            return c;
      }
   }

   private void sourceToDest(BlockPos source, MutableBlockPos dest) {
      this.rotate(source, dest);
      dest.set(dest.getX() + this.projDx, dest.getY() + this.projDy, dest.getZ() + this.projDz);
   }

   private void rotate(BlockPos c, MutableBlockPos dest) {
      switch (this.getRotate()) {
         case ROTATE_90:
            dest.set(-c.getZ(), c.getY(), c.getX());
            break;
         case ROTATE_270:
            dest.set(c.getZ(), c.getY(), -c.getX());
            break;
         case ROTATE_0:
            dest.set(c);
            break;
         case ROTATE_180:
            dest.set(-c.getX(), c.getY(), -c.getZ());
      }
   }

   private BuilderData restartScan(BuilderData data) {
      data = data.withLastError(null);
      this.chunkUnload();
      if (data.flags().loopMode() || this.isMachineEnabled() && data.scan() == null) {
         if (this.getCardType() == ShapeCardType.CARD_SPACE) {
            data = (BuilderData)this.calculateBox(data).getLeft();
            data = data.withScan(data.minBox());
         } else if (this.getCardType() != ShapeCardType.CARD_UNKNOWN) {
            data = this.calculateBoxShaped(data);
            data = data.withScan(new BlockPos(data.minBox().getX(), data.maxBox().getY(), data.minBox().getZ()));
         }

         this.cachedBlocks = null;
         this.cachedChunk = null;
         this.cachedVoidableBlocks.clear();
      } else {
         data = data.withScan(null);
      }

      return data;
   }

   public void setRemoved() {
      super.setRemoved();
      this.chunkUnload();
   }

   private void chunkUnload() {
      if (this.forcedChunk != null) {
         if (this.getOwnerUUID() != null) {
            RFToolsBuilder.setup
               .ticketController
               .forceChunk((ServerLevel)this.level, this.getOwnerUUID(), this.forcedChunk.x(), this.forcedChunk.z(), false, false);
         }

         this.forcedChunk = null;
      }
   }

   private boolean chunkLoad(int x, int z) {
      int cx = x >> 4;
      int cz = z >> 4;
      if (LevelTools.isLoaded(this.level, new BlockPos(x, 0, z))) {
         return true;
      } else if ((Boolean)BuilderConfiguration.quarryChunkloads.get()) {
         ChunkPos pair = new ChunkPos(cx, cz);
         if (pair.equals(this.forcedChunk)) {
            return true;
         } else {
            if (this.forcedChunk != null && this.getOwnerUUID() != null) {
               RFToolsBuilder.setup
                  .ticketController
                  .forceChunk((ServerLevel)this.level, this.getOwnerUUID(), this.forcedChunk.x(), this.forcedChunk.z(), false, false);
            }

            this.forcedChunk = pair;
            if (this.getOwnerUUID() != null) {
               RFToolsBuilder.setup
                  .ticketController
                  .forceChunk((ServerLevel)this.level, this.getOwnerUUID(), this.forcedChunk.x(), this.forcedChunk.z(), false, false);
            }

            return true;
         }
      } else {
         return false;
      }
   }

   public static void setScanLocationClient(BlockPos tePos, BlockPos scanPos) {
      scanLocClient.put(tePos, Pair.of(System.currentTimeMillis(), scanPos));
   }

   public static Map<BlockPos, Pair<Long, BlockPos>> getScanLocClient() {
      if (scanLocClient.isEmpty()) {
         return scanLocClient;
      } else {
         Map<BlockPos, Pair<Long, BlockPos>> scans = new HashMap<>();
         long time = System.currentTimeMillis();

         for (Entry<BlockPos, Pair<Long, BlockPos>> entry : scanLocClient.entrySet()) {
            if ((Long)entry.getValue().getKey() + 10000L > time) {
               scans.put(entry.getKey(), entry.getValue());
            }
         }

         scanLocClient = scans;
         return scanLocClient;
      }
   }

   private BuilderData nextLocation(BuilderData data) {
      if (data.scan() != null) {
         int x = data.scan().getX();
         int y = data.scan().getY();
         int z = data.scan().getZ();
         if (this.getCardType() == ShapeCardType.CARD_SPACE) {
            data = this.nextLocationNormal(data, x, y, z);
         } else {
            data = this.nextLocationQuarry(data, x, y, z);
         }
      }

      return data;
   }

   private BuilderData nextLocationQuarry(BuilderData data, int x, int y, int z) {
      BlockPos minBox = data.minBox();
      BlockPos maxBox = data.maxBox();
      if (x < maxBox.getX() && (x + 1) % 16 != 0) {
         data = data.withScan(new BlockPos(x + 1, y, z));
      } else if (z < maxBox.getZ() && (z + 1) % 16 != 0) {
         data = data.withScan(new BlockPos(x >> 4 << 4, y, z + 1));
      } else if (y <= minBox.getY()) {
         if (x < maxBox.getX()) {
            x++;
            z = z >> 4 << 4;
            y = maxBox.getY();
            data = data.withScan(new BlockPos(x, y, z));
         } else {
            if (z >= maxBox.getZ()) {
               return this.restartScan(data);
            }

            x = minBox.getX();
            z++;
            y = maxBox.getY();
            data = data.withScan(new BlockPos(x, y, z));
         }
      } else {
         data = data.withScan(new BlockPos(x >> 4 << 4, y - 1, z >> 4 << 4));
      }

      return data;
   }

   private BuilderData nextLocationNormal(BuilderData data, int x, int y, int z) {
      BlockPos minBox = data.minBox();
      BlockPos maxBox = data.maxBox();
      if (x >= maxBox.getX()) {
         if (z >= maxBox.getZ()) {
            if (y >= maxBox.getY()) {
               if (this.getMode() == BuilderMode.MODE_SWAP && !this.isShapeCard()) {
                  data = data.withScan(null);
               } else {
                  data = this.restartScan(data);
               }
            } else {
               data = data.withScan(new BlockPos(minBox.getX(), y + 1, minBox.getZ()));
            }
         } else {
            data = data.withScan(new BlockPos(minBox.getX(), y, z + 1));
         }
      } else {
         data = data.withScan(new BlockPos(x + 1, y, z));
      }

      return data;
   }

   private void refreshSettings() {
      BuilderData data = (BuilderData)this.getData(BuilderModule.BUILDER_DATA);
      data = this.clearSupportBlocks(data);
      this.cachedBlocks = null;
      this.cachedChunk = null;
      this.cachedVoidableBlocks.clear();
      this.boxValid = false;
      this.setData(BuilderModule.BUILDER_DATA, data.withScan(null));
      this.cardType = ShapeCardType.CARD_UNKNOWN;
   }

   public void loadAdditional(ValueInput input) {
      super.loadAdditional(input);
      this.energyStorage.load(input, "energy");
      this.items.load(input, "items");
      this.infusable.load(input, "infusable");
      this.overflowItems.clear();

      for (ItemStack overflow : input.listOrEmpty("overflowItems", ItemStack.CODEC)) {
         if (!overflow.isEmpty()) {
            this.overflowItems.add(overflow);
         }
      }

      this.cachedBlocks = null;
      this.cachedChunk = null;
      this.cachedVoidableBlocks.clear();
      this.boxValid = false;
      this.cardType = ShapeCardType.CARD_UNKNOWN;
   }

   public void saveAdditional(ValueOutput output) {
      super.saveAdditional(output);
      this.energyStorage.save(output, "energy");
      this.items.save(output, "items");
      this.infusable.save(output, "infusable");
      if (!this.overflowItems.isEmpty()) {
         TypedOutputList<ItemStack> list = output.list("overflowItems", ItemStack.CODEC);

         for (ItemStack overflow : this.overflowItems.getList()) {
            if (!overflow.isEmpty()) {
               list.add(overflow);
            }
         }
      } else {
         output.discard("overflowItems");
      }
   }

   protected void applyImplicitComponents(DataComponentGetter input) {
      super.applyImplicitComponents(input);
      this.energyStorage.applyImplicitComponents((ItemEnergy)input.get((DataComponentType)Registration.ITEM_ENERGY.get()));
      this.items.applyImplicitComponents((ItemInventory)input.get((DataComponentType)Registration.ITEM_INVENTORY.get()));
      this.infusable.applyImplicitComponents((ItemInfusable)input.get((DataComponentType)Registration.ITEM_INFUSABLE.get()));
      BuilderData builderData = (BuilderData)input.get(BuilderModule.ITEM_BUILDER_DATA);
      if (builderData != null) {
         this.setData(BuilderModule.BUILDER_DATA, builderData);
      }
   }

   protected void collectImplicitComponents(net.minecraft.core.component.DataComponentMap.Builder builder) {
      super.collectImplicitComponents(builder);
      this.energyStorage.collectImplicitComponents(builder);
      this.items.collectImplicitComponents(builder);
      this.infusable.collectImplicitComponents(builder);
      builder.set(BuilderModule.ITEM_BUILDER_DATA, (BuilderData)this.getData(BuilderModule.BUILDER_DATA));
   }

   public static int getCurrentLevelClientSide() {
      return currentLevel;
   }

   public int getCurrentLevel() {
      BuilderData data = (BuilderData)this.getData(BuilderModule.BUILDER_DATA);
      return data.scan() == null ? -1 : data.scan().getY();
   }

   public void onReplaced(Level world, BlockPos pos, BlockState state, BlockState newstate) {
      if (state.getBlock() != newstate.getBlock()) {
         if (this.hasSupportMode()) {
            BuilderData data = (BuilderData)this.getData(BuilderModule.BUILDER_DATA);
            data = this.clearSupportBlocks(data);
            this.setData(BuilderModule.BUILDER_DATA, data);
         }
      }
   }

   public void rotateBlock(Rotation axis) {
      super.rotateBlock(axis);
      if (!this.level.isClientSide() && this.hasSupportMode()) {
         BuilderData data = (BuilderData)this.getData(BuilderModule.BUILDER_DATA);
         data = this.clearSupportBlocks(data);
         this.setData(BuilderModule.BUILDER_DATA, data);
         this.resetBox();
      }
   }

   private GenericItemHandler createItemHandler() {
      return new GenericItemHandler(this, (ContainerFactory)CONTAINER_FACTORY.get()) {
         {
            Objects.requireNonNull(BuilderTileEntity.this);
         }

         @Nonnull
         public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            this.checkShapeCard(slot, stack);
            return super.insertItem(slot, stack, simulate);
         }

         public void setInventorySlotContents(int stackLimit, int index, ItemStack stack) {
            this.checkShapeCard(index, stack);
            super.setInventorySlotContents(stackLimit, index, stack);
         }

         @Nonnull
         public ItemStack extractItem(int slot, int amount, boolean simulate) {
            this.checkShapeCard(slot, ItemStack.EMPTY);
            return super.extractItem(slot, amount, simulate);
         }

         public ItemStack decrStackSize(int index, int amount) {
            this.checkShapeCard(index, ItemStack.EMPTY);
            return super.decrStackSize(index, amount);
         }

         public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
            this.checkShapeCard(slot, stack);
            super.setStackInSlot(slot, stack);
         }

         protected void onUpdate(int index, ItemStack stack) {
            super.onUpdate(index, stack);
            if (index == 1) {
               BuilderTileEntity.this.filterCache.clear();
            }

            if (index == 0) {
               BuilderTileEntity.this.refreshSettings();
               if (!this.getStackInSlot(0).isEmpty() && BuilderTileEntity.this.isMachineEnabled()) {
                  BuilderData data = (BuilderData)BuilderTileEntity.this.getData(BuilderModule.BUILDER_DATA);
                  data = BuilderTileEntity.this.restartScan(data);
                  BuilderTileEntity.this.setData(BuilderModule.BUILDER_DATA, data);
               }
            }
         }

         private void checkShapeCard(int index, ItemStack newStack) {
            ItemStack stack = this.getStackInSlot(index);
            if (index == 0 && (stack.isEmpty() && !newStack.isEmpty() || !stack.isEmpty() && newStack.isEmpty())) {
               BuilderTileEntity.this.refreshSettings();
            }
         }
      };
   }

   private static class TakeableItem {
      private final IItemHandler itemHandler;
      private final int slot;
      private final ItemStack peekStack;
      public static final BuilderTileEntity.TakeableItem EMPTY = new BuilderTileEntity.TakeableItem();

      private TakeableItem() {
         this.itemHandler = null;
         this.slot = -1;
         this.peekStack = ItemStack.EMPTY;
      }

      public TakeableItem(IItemHandler itemHandler, int slot) {
         Validate.inclusiveBetween(0L, itemHandler.getSlots() - 1, slot);
         this.itemHandler = itemHandler;
         this.slot = slot;
         this.peekStack = itemHandler.extractItem(slot, 1, true);
      }

      public ItemStack peek() {
         return this.peekStack.copy();
      }

      public void take() {
         if (this.itemHandler != null) {
            this.itemHandler.extractItem(this.slot, 1, false);
         }
      }

      public ItemStack takeAndReplace(ItemStack replacement) {
         if (this.itemHandler != null) {
            this.itemHandler.extractItem(this.slot, 1, false);
            return this.itemHandler.insertItem(this.slot, replacement, false);
         } else {
            return replacement;
         }
      }
   }
}
