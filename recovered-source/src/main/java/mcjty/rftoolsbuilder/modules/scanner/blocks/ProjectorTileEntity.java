package mcjty.rftoolsbuilder.modules.scanner.blocks;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mcjty.lib.api.container.DefaultContainerProvider;
import mcjty.lib.api.container.ItemInventory;
import mcjty.lib.bindings.GuiValue;
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
import mcjty.lib.varia.OrientationTools;
import mcjty.lib.varia.RedstoneMode;
import mcjty.rftoolsbuilder.compat.CompatNbt;
import mcjty.rftoolsbuilder.modules.builder.items.ShapeCardItem;
import mcjty.rftoolsbuilder.modules.scanner.ProjectorOpcode;
import mcjty.rftoolsbuilder.modules.scanner.ProjectorOperation;
import mcjty.rftoolsbuilder.modules.scanner.ScannerModule;
import mcjty.rftoolsbuilder.shapes.RenderData;
import mcjty.rftoolsbuilder.shapes.ShapeID;
import mcjty.rftoolsbuilder.shapes.ShapeRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponentMap.Builder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.Lazy;

public class ProjectorTileEntity extends TickingTileEntity {
   public static final String CMD_RSSETTINGS_ID = "projector.rsSettings";
   public static final String CMD_SETTINGS_ID = "projector.settings";
   public static final Key<String> PARAM_OPON_N = new Key("opOn_n", Type.STRING);
   public static final Key<String> PARAM_OPON_S = new Key("opOn_s", Type.STRING);
   public static final Key<String> PARAM_OPON_W = new Key("opOn_w", Type.STRING);
   public static final Key<String> PARAM_OPON_E = new Key("opOn_e", Type.STRING);
   public static final List<Key<String>> PARAM_OPON = Arrays.asList(PARAM_OPON_N, PARAM_OPON_S, PARAM_OPON_W, PARAM_OPON_E);
   public static final Key<String> PARAM_OPOFF_N = new Key("opOff_n", Type.STRING);
   public static final Key<String> PARAM_OPOFF_S = new Key("opOff_s", Type.STRING);
   public static final Key<String> PARAM_OPOFF_W = new Key("opOff_w", Type.STRING);
   public static final Key<String> PARAM_OPOFF_E = new Key("opOff_e", Type.STRING);
   public static final List<Key<String>> PARAM_OPOFF = Arrays.asList(PARAM_OPOFF_N, PARAM_OPOFF_S, PARAM_OPOFF_W, PARAM_OPOFF_E);
   public static final Key<Double> PARAM_VALON_N = new Key("valOn_n", Type.DOUBLE);
   public static final Key<Double> PARAM_VALON_S = new Key("valOn_s", Type.DOUBLE);
   public static final Key<Double> PARAM_VALON_W = new Key("valOn_w", Type.DOUBLE);
   public static final Key<Double> PARAM_VALON_E = new Key("valOn_e", Type.DOUBLE);
   public static final List<Key<Double>> PARAM_VALON = Arrays.asList(PARAM_VALON_N, PARAM_VALON_S, PARAM_VALON_W, PARAM_VALON_E);
   public static final Key<Double> PARAM_VALOFF_N = new Key("valOff_n", Type.DOUBLE);
   public static final Key<Double> PARAM_VALOFF_S = new Key("valOff_s", Type.DOUBLE);
   public static final Key<Double> PARAM_VALOFF_W = new Key("valOff_w", Type.DOUBLE);
   public static final Key<Double> PARAM_VALOFF_E = new Key("valOff_e", Type.DOUBLE);
   public static final List<Key<Double>> PARAM_VALOFF = Arrays.asList(PARAM_VALOFF_N, PARAM_VALOFF_S, PARAM_VALOFF_W, PARAM_VALOFF_E);
   public static final Key<Integer> PARAM_SCALE = new Key("scale", Type.INTEGER);
   public static final Key<Integer> PARAM_OFFSET = new Key("offset", Type.INTEGER);
   public static final Key<Integer> PARAM_ANGLE = new Key("angle", Type.INTEGER);
   public static final Key<Boolean> PARAM_AUTO = new Key("auto", Type.BOOLEAN);
   public static final Key<Boolean> PARAM_SCAN = new Key("scan", Type.BOOLEAN);
   public static final Key<Boolean> PARAM_SOUND = new Key("sound", Type.BOOLEAN);
   public static final Key<Boolean> PARAM_GRAY = new Key("gray", Type.BOOLEAN);
   public static final Key<Boolean> PARAM_RENDERMODELS = new Key("render_models", Type.BOOLEAN);
   public static final int SLOT_CARD = 0;
   private static final int SIDE_PANEL_WIDTH = 80;
   private static final int SHAPE_CARD_SLOT_X = 95;
   private static final int PLAYER_SLOTS_X = 165;
   public static final Lazy<ContainerFactory> CONTAINER_FACTORY = Lazy.of(
      () -> new ContainerFactory(1).slot(SlotDefinition.specific(s -> s.getItem() instanceof ShapeCardItem).in().out(), 0, 95, 7).playerSlots(165, 142)
   );
   private final GenericItemHandler items = GenericItemHandler.create(this, CONTAINER_FACTORY)
      .itemValid((slot, stack) -> stack.getItem() instanceof ShapeCardItem)
      .onUpdate((slot, stack) -> this.onCardSlotUpdated())
      .build();
   @Cap(type = CapType.ITEMS_AUTOMATION)
   private static final Function<ProjectorTileEntity, GenericItemHandler> ITEM_CAP = tile -> tile.items;
   @Cap(type = CapType.CONTAINER)
   private static final Function<ProjectorTileEntity, MenuProvider> SCREEN_CAP = tile -> new DefaultContainerProvider("Projector")
      .containerSupplier(DefaultContainerProvider.container(ScannerModule.CONTAINER_PROJECTOR, CONTAINER_FACTORY, tile))
      .itemHandler(() -> tile.items)
      .setupSync(tile);
   private final ProjectorOperation[] operations = new ProjectorOperation[4];
   private ShapeRenderer shapeRenderer;
   private boolean active = false;
   private boolean projecting = false;
   @GuiValue
   private float verticalOffset = 0.2F;
   @GuiValue
   private float scale = 0.01F;
   @GuiValue
   private float angle = 0.0F;
   @GuiValue
   private boolean autoRotate = false;
   @GuiValue
   private boolean scanline = true;
   @GuiValue
   private boolean sound = true;
   @GuiValue
   private boolean grayscale = false;
   @GuiValue
   private boolean renderBlockModels = false;
   private int counter = 0;
   private int powerLevel = 0;
   private int prevPowerLevel = 0;
   private int clientCounter = 0;
   private boolean loadingClientData = false;
   @ServerCommand
   public static final Command<?> CMD_RSSETTINGS = Command.create("projector.rsSettings", (te, player, params) -> te.applyRsSettings(params));
   @ServerCommand
   public static final Command<?> CMD_SETTINGS = Command.create("projector.settings", (te, player, params) -> te.applySettings(params));

   private boolean isLoadingClientData() {
      return this.loadingClientData;
   }

   private void refreshProjectionData() {
      this.counter++;
      this.shapeRenderer = null;
   }

   private void onCardSlotUpdated() {
      if (!this.isLoadingClientData()) {
         if (this.level != null && this.level.isClientSide()) {
            if (this.shapeRenderer != null && !this.getShapeID().equals(this.shapeRenderer.getShapeID())) {
               this.shapeRenderer = null;
            }

            this.updateProjecting();
         } else {
            this.refreshProjectionData();
            this.updateProjecting();
            this.setChanged();
            this.markDirtyClient();
         }
      }
   }

   public ProjectorTileEntity(BlockPos pos, BlockState state) {
      this(ScannerModule.TYPE_PROJECTOR.get(), pos, state);
   }

   public ProjectorTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
      super(type, pos, state);

      for (int i = 0; i < this.operations.length; i++) {
         this.operations[i] = new ProjectorOperation();
      }

      for (ProjectorOperation operation : this.operations) {
         operation.setOpcodeOn(ProjectorOpcode.NONE);
         operation.setOpcodeOff(ProjectorOpcode.NONE);
      }

      this.operations[0].setOpcodeOn(ProjectorOpcode.ON);
      this.operations[0].setOpcodeOff(ProjectorOpcode.ON);
      this.setRSMode(RedstoneMode.REDSTONE_IGNORED);
      this.active = true;
   }

   protected boolean needsRedstoneMode() {
      return false;
   }

   protected void tickServer() {
      this.updateRedstoneOperations();
      this.updateProjecting();
   }

   protected void tickClient() {
      if (this.autoRotate) {
         this.angle++;
         if (this.angle >= 360.0F) {
            this.angle = 0.0F;
         }
      }

      if (this.clientCounter != this.counter) {
         this.clientCounter = this.counter;
         RenderData data = ShapeRenderer.getRenderDataAndCreate(this.getShapeID());
         data.clearRequest();
         data.clearData();
         data.setChecksum(Long.MIN_VALUE);
         data.setWantData(true);
         this.shapeRenderer = null;
      }
   }

   public InteractionResult interact(Level level, Player player, InteractionHand hand) {
      ItemStack held = player.getItemInHand(hand);
      if (player.isCrouching()) {
         if (!held.isEmpty() && held.getItem() instanceof ShapeCardItem) {
            if (!level.isClientSide()) {
               ItemStack previous = this.items.getStackInSlot(0);
               ItemStack inserted = held.copyWithCount(1);
               this.items.setStackInSlot(0, inserted);
               held.shrink(1);
               if (!previous.isEmpty() && !player.addItem(previous)) {
                  player.drop(previous, false);
               }
            }

            return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
         }

         if (held.isEmpty()) {
            ItemStack card = this.items.getStackInSlot(0);
            if (!card.isEmpty()) {
               if (!level.isClientSide()) {
                  player.setItemInHand(hand, card);
                  this.items.setStackInSlot(0, ItemStack.EMPTY);
               }

               return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
            }
         }
      }

      return InteractionResult.PASS;
   }

   private void updateRedstoneOperations() {
      this.prevPowerLevel = this.powerLevel;
      this.powerLevel = this.calculatePowerLevel();
      boolean pulse = this.prevPowerLevel != this.powerLevel;

      for (Direction facing : Plane.HORIZONTAL) {
         int index = facing.get2DDataValue();
         ProjectorOperation op = this.operations[index];
         int pl = index ^ 1;
         if ((this.powerLevel >> pl & 1) != 0) {
            this.handleOpcode(op.getOpcodeOn(), op.getValueOn(), pulse);
         } else {
            this.handleOpcode(op.getOpcodeOff(), op.getValueOff(), pulse);
         }
      }
   }

   private int calculatePowerLevel() {
      Direction horiz = this.getBlockOrientation();
      if (horiz == null) {
         return 0;
      } else {
         Direction north = reorient(Direction.NORTH, horiz);
         Direction south = reorient(Direction.SOUTH, horiz);
         Direction west = reorient(Direction.WEST, horiz);
         Direction east = reorient(Direction.EAST, horiz);
         int powered1 = this.getInputStrength(north) > 0 ? 1 : 0;
         int powered2 = this.getInputStrength(south) > 0 ? 2 : 0;
         int powered3 = this.getInputStrength(west) > 0 ? 4 : 0;
         int powered4 = this.getInputStrength(east) > 0 ? 8 : 0;
         return powered1 + powered2 + powered3 + powered4;
      }
   }

   private void handleOpcode(ProjectorOpcode op, @Nullable Double val, boolean pulse) {
      if (op == null) {
         op = ProjectorOpcode.NONE;
      }

      switch (op) {
         case NONE:
         default:
            break;
         case ON:
            this.active = true;
            break;
         case OFF:
            this.active = false;
            break;
         case SCAN:
            if (pulse) {
               this.refreshProjectionData();
               this.markDirtyClient();
            }
            break;
         case OFFSET:
            double oxx = this.getOffsetDouble();
            if (val != null && Math.abs(oxx - val) > 0.3) {
               if (oxx < val) {
                  oxx++;
               } else {
                  oxx--;
               }

               this.setOffsetInt(oxx);
               this.markDirtyClient();
            }
            break;
         case ROT:
            int ox = this.getAngleInt();
            if (val != null && ox != val.intValue()) {
               if (ox < val) {
                  ox++;
               } else {
                  ox--;
               }

               this.setAngleInt(ox);
               this.markDirtyClient();
            }
            break;
         case SCALE:
            double o = this.getScaleDouble();
            if (val != null && Math.abs(o - val) > 0.3) {
               if (o < val) {
                  o++;
               } else {
                  o--;
               }

               this.setScaleInt(o);
               this.markDirtyClient();
            }
            break;
         case GRAYON:
            if (!this.grayscale) {
               this.grayscale = true;
               this.refreshProjectionData();
               this.markDirtyClient();
            }
            break;
         case GRAYOFF:
            if (this.grayscale) {
               this.grayscale = false;
               this.refreshProjectionData();
               this.markDirtyClient();
            }
      }
   }

   private void updateProjecting() {
      boolean newProjecting = this.active && !this.getRenderStack().isEmpty();
      if (newProjecting != this.projecting) {
         this.projecting = newProjecting;
         this.markDirtyClient();
      }
   }

   @Nullable
   private Direction getBlockOrientation() {
      BlockState state = this.level.getBlockState(this.worldPosition);
      return state.getBlock() == ScannerModule.PROJECTOR.block().get() ? OrientationTools.getOrientationHoriz(state) : null;
   }

   private int getInputStrength(Direction side) {
      BlockPos p = this.worldPosition.relative(side);
      int power = this.level.getSignal(p, side);
      if (power == 0) {
         BlockState blockState = this.level.getBlockState(p);
         if (blockState.getBlock() instanceof RedStoneWireBlock) {
            power = this.level.hasNeighborSignal(p) ? 15 : 0;
         }
      }

      return power;
   }

   private static Direction reorient(Direction side, Direction blockDirection) {
      return switch (blockDirection) {
         case NORTH -> side != Direction.DOWN && side != Direction.UP ? side.getOpposite() : side;
         case SOUTH -> side;
         case WEST -> side != Direction.DOWN && side != Direction.UP
            ? (
               side == Direction.WEST
                  ? Direction.NORTH
                  : (side == Direction.NORTH ? Direction.EAST : (side == Direction.EAST ? Direction.SOUTH : Direction.WEST))
            )
            : side;
         case EAST -> side != Direction.DOWN && side != Direction.UP
            ? (
               side == Direction.WEST
                  ? Direction.SOUTH
                  : (side == Direction.NORTH ? Direction.WEST : (side == Direction.EAST ? Direction.NORTH : Direction.EAST))
            )
            : side;
         default -> side;
      };
   }

   public ItemStack removeCard() {
      ItemStack card = this.items.getStackInSlot(0);
      this.items.setStackInSlot(0, ItemStack.EMPTY);
      return card;
   }

   public ItemStack getRenderStack() {
      return this.items.getStackInSlot(0);
   }

   public ShapeID getShapeID() {
      ItemStack stack = this.getRenderStack();
      int scanId = ShapeCardItem.getScanId(stack);
      boolean solid = this.renderBlockModels ? false : ShapeCardItem.isSolid(stack);
      ResourceKey<Level> dimension = this.level == null ? Level.OVERWORLD : this.level.dimension();
      return scanId == 0
         ? new ShapeID(dimension, this.worldPosition, scanId, this.grayscale, solid)
         : new ShapeID(Level.OVERWORLD, null, scanId, this.grayscale, solid);
   }

   public ShapeRenderer getShapeRenderer() {
      ShapeID shapeID = this.getShapeID();
      if (this.shapeRenderer != null && shapeID.equals(this.shapeRenderer.getShapeID())) {
         this.shapeRenderer.setShapeID(shapeID);
      } else {
         this.shapeRenderer = new ShapeRenderer(shapeID);
      }

      return this.shapeRenderer;
   }

   public ProjectorOperation[] getOperations() {
      return this.operations;
   }

   public float getVerticalOffset() {
      return this.verticalOffset;
   }

   public int getOffsetInt() {
      return Math.round((float)this.getOffsetDouble());
   }

   private double getOffsetDouble() {
      return this.verticalOffset * 20.0F;
   }

   private void setOffsetInt(double o) {
      this.verticalOffset = (float)(o / 20.0);
      this.setChanged();
   }

   public float getScale() {
      return this.scale;
   }

   public int getScaleInt() {
      return Math.round((float)this.getScaleDouble());
   }

   private double getScaleDouble() {
      return 20.0 * Math.log((this.scale - 0.001F) / 0.1F * 147.4131F + 1.0F);
   }

   private void setScaleInt(double s) {
      this.scale = ((float)Math.exp(s / 20.0) - 1.0F) / 147.4131F * 0.1F + 0.001F;
      this.setChanged();
   }

   public float getAngle() {
      return this.angle;
   }

   public int getAngleInt() {
      return Math.round(this.angle);
   }

   private void setAngleInt(int a) {
      this.angle = a;
      this.setChanged();
   }

   public boolean isAutoRotate() {
      return this.autoRotate;
   }

   public boolean isScanline() {
      return this.scanline;
   }

   public boolean isSound() {
      return this.sound;
   }

   public boolean isGrayscale() {
      return this.grayscale;
   }

   public boolean isRenderBlockModels() {
      return this.renderBlockModels;
   }

   public boolean isProjecting() {
      return this.projecting;
   }

   public int getCounter() {
      return this.counter;
   }

   protected void loadAdditional(CompoundTag tag, Provider provider) {
      super.loadAdditional(tag, provider);
      this.items.load(tag, "items", provider);
      this.verticalOffset = tag.contains("offs") ? tag.getFloatOr("offs", 0.0F) : 0.2F;
      this.scale = tag.contains("scale") ? tag.getFloatOr("scale", 0.0F) : 0.01F;
      this.angle = tag.getFloatOr("angle", 0.0F);
      this.autoRotate = tag.getBooleanOr("rot", false);
      this.scanline = !tag.contains("scan") || tag.getBooleanOr("scan", false);
      this.sound = !tag.contains("sound") || tag.getBooleanOr("sound", false);
      this.grayscale = tag.getBooleanOr("grayscale", false);
      this.renderBlockModels = tag.getBooleanOr("render_models", false);
      this.projecting = tag.getBooleanOr("projecting", false);
      this.active = !tag.contains("active") || tag.getBooleanOr("active", false);
      this.counter = tag.getIntOr("counter", 0);

      for (Direction facing : Plane.HORIZONTAL) {
         String key = "op_" + facing.getName();
         if (tag.contains(key)) {
            int index = facing.get2DDataValue();
            CompoundTag tc = tag.getCompoundOrEmpty(key);
            ProjectorOperation op = this.operations[index];
            op.setOpcodeOn(ProjectorOpcode.getByCode(tc.getStringOr("on", "")));
            op.setOpcodeOff(ProjectorOpcode.getByCode(tc.getStringOr("off", "")));
            op.setValueOn(tc.contains("von") ? tc.getDoubleOr("von", 0.0) : null);
            op.setValueOff(tc.contains("voff") ? tc.getDoubleOr("voff", 0.0) : null);
         }
      }
   }

   protected void saveAdditional(@Nonnull CompoundTag tag, Provider provider) {
      super.saveAdditional(tag, provider);
      this.items.save(tag, "items", provider);
      tag.putFloat("offs", this.verticalOffset);
      tag.putFloat("scale", this.scale);
      tag.putFloat("angle", this.angle);
      tag.putBoolean("rot", this.autoRotate);
      tag.putBoolean("scan", this.scanline);
      tag.putBoolean("sound", this.sound);
      tag.putBoolean("grayscale", this.grayscale);
      tag.putBoolean("render_models", this.renderBlockModels);
      tag.putBoolean("projecting", this.projecting);
      tag.putBoolean("active", this.active);
      tag.putInt("counter", this.counter);

      for (Direction facing : Plane.HORIZONTAL) {
         int index = facing.get2DDataValue();
         ProjectorOperation op = this.operations[index];
         CompoundTag tc = new CompoundTag();
         tc.putString("on", op.getOpcodeOn().getCode());
         tc.putString("off", op.getOpcodeOff().getCode());
         if (op.getValueOn() != null) {
            tc.putDouble("von", op.getValueOn());
         }

         if (op.getValueOff() != null) {
            tc.putDouble("voff", op.getValueOff());
         }

         tag.put("op_" + facing.getName(), tc);
      }
   }

   protected void applyImplicitComponents(DataComponentGetter input) {
      super.applyImplicitComponents(input);
      this.items.applyImplicitComponents((ItemInventory)input.get((DataComponentType)Registration.ITEM_INVENTORY.get()));
   }

   protected void collectImplicitComponents(Builder builder) {
      super.collectImplicitComponents(builder);
      this.items.collectImplicitComponents(builder);
   }

   public void handleUpdateTag(ValueInput input) {
      super.handleUpdateTag(input);
   }

   public void saveClientDataToNBT(CompoundTag tag, Provider provider) {
      ItemStack card = this.getRenderStack();
      if (!card.isEmpty()) {
         tag.put("card", CompatNbt.encodeItemStack(card, provider));
      }

      tag.putBoolean("projecting", this.projecting);
      tag.putBoolean("active", this.active);
      tag.putFloat("offs", this.verticalOffset);
      tag.putFloat("scale", this.scale);
      tag.putFloat("angle", this.angle);
      tag.putBoolean("rot", this.autoRotate);
      tag.putBoolean("scan", this.scanline);
      tag.putBoolean("sound", this.sound);
      tag.putBoolean("grayscale", this.grayscale);
      tag.putBoolean("render_models", this.renderBlockModels);
      tag.putInt("counter", this.counter);

      for (Direction facing : Plane.HORIZONTAL) {
         int index = facing.get2DDataValue();
         ProjectorOperation op = this.operations[index];
         CompoundTag tc = new CompoundTag();
         tc.putString("on", op.getOpcodeOn().getCode());
         tc.putString("off", op.getOpcodeOff().getCode());
         if (op.getValueOn() != null) {
            tc.putDouble("von", op.getValueOn());
         }

         if (op.getValueOff() != null) {
            tc.putDouble("voff", op.getValueOff());
         }

         tag.put("op_" + facing.getName(), tc);
      }
   }

   public void loadClientDataFromNBT(CompoundTag tag, Provider provider) {
      ShapeID oldShapeId = this.getShapeID();
      this.loadingClientData = true;
      this.items.setStackInSlot(0, CompatNbt.decodeItemStack(tag.getCompoundOrEmpty("card"), provider));
      this.loadingClientData = false;
      this.projecting = tag.getBooleanOr("projecting", false);
      this.active = !tag.contains("active") || tag.getBooleanOr("active", false);
      this.verticalOffset = tag.contains("offs") ? tag.getFloatOr("offs", 0.0F) : 0.2F;
      this.scale = tag.contains("scale") ? tag.getFloatOr("scale", 0.0F) : 0.01F;
      this.angle = tag.getFloatOr("angle", 0.0F);
      this.autoRotate = tag.getBooleanOr("rot", false);
      this.scanline = !tag.contains("scan") || tag.getBooleanOr("scan", false);
      this.sound = !tag.contains("sound") || tag.getBooleanOr("sound", false);
      this.grayscale = tag.getBooleanOr("grayscale", false);
      this.renderBlockModels = tag.getBooleanOr("render_models", false);
      this.counter = tag.getIntOr("counter", 0);

      for (Direction facing : Plane.HORIZONTAL) {
         String key = "op_" + facing.getName();
         if (tag.contains(key)) {
            int index = facing.get2DDataValue();
            CompoundTag tc = tag.getCompoundOrEmpty(key);
            ProjectorOperation op = this.operations[index];
            op.setOpcodeOn(ProjectorOpcode.getByCode(tc.getStringOr("on", "")));
            op.setOpcodeOff(ProjectorOpcode.getByCode(tc.getStringOr("off", "")));
            op.setValueOn(tc.contains("von") ? tc.getDoubleOr("von", 0.0) : null);
            op.setValueOff(tc.contains("voff") ? tc.getDoubleOr("voff", 0.0) : null);
         }
      }

      if (!Objects.equals(oldShapeId, this.getShapeID())) {
         this.shapeRenderer = null;
      }
   }

   public AABB getRenderBoundingBox() {
      return new AABB(Vec3.atLowerCornerOf(this.worldPosition.offset(-8, 0, -8)), Vec3.atLowerCornerOf(this.worldPosition.offset(9, 9, 9)));
   }

   private void applyRsSettings(TypedMap params) {
      boolean changed = false;

      for (Direction facing : Plane.HORIZONTAL) {
         int idx = facing.get2DDataValue();
         ProjectorOperation operation = this.operations[idx];
         ProjectorOpcode opcodeOn = ProjectorOpcode.getByCode((String)params.get(PARAM_OPON.get(idx)));
         ProjectorOpcode opcodeOff = ProjectorOpcode.getByCode((String)params.get(PARAM_OPOFF.get(idx)));
         Double valueOn = (Double)params.get(PARAM_VALON.get(idx));
         Double valueOff = (Double)params.get(PARAM_VALOFF.get(idx));
         if (operation.getOpcodeOn() != opcodeOn) {
            operation.setOpcodeOn(opcodeOn);
            changed = true;
         }

         if (operation.getOpcodeOff() != opcodeOff) {
            operation.setOpcodeOff(opcodeOff);
            changed = true;
         }

         if (!Objects.equals(operation.getValueOn(), valueOn)) {
            operation.setValueOn(valueOn);
            changed = true;
         }

         if (!Objects.equals(operation.getValueOff(), valueOff)) {
            operation.setValueOff(valueOff);
            changed = true;
         }
      }

      if (changed) {
         this.setChanged();
         this.markDirtyClient();
      }
   }

   private void applySettings(TypedMap params) {
      boolean changed = false;
      int newScale = (Integer)params.get(PARAM_SCALE);
      if (newScale != this.getScaleInt()) {
         this.setScaleInt(newScale);
         changed = true;
      }

      int newOffset = (Integer)params.get(PARAM_OFFSET);
      if (newOffset != this.getOffsetInt()) {
         this.setOffsetInt(newOffset);
         changed = true;
      }

      int newAngle = (Integer)params.get(PARAM_ANGLE);
      if (newAngle != this.getAngleInt()) {
         this.setAngleInt(newAngle);
         changed = true;
      }

      boolean newAutoRotate = (Boolean)params.get(PARAM_AUTO);
      if (this.autoRotate != newAutoRotate) {
         this.autoRotate = newAutoRotate;
         changed = true;
      }

      boolean newScanline = (Boolean)params.get(PARAM_SCAN);
      if (this.scanline != newScanline) {
         this.scanline = newScanline;
         changed = true;
      }

      boolean newSound = (Boolean)params.get(PARAM_SOUND);
      if (this.sound != newSound) {
         this.sound = newSound;
         changed = true;
      }

      boolean gs = (Boolean)params.get(PARAM_GRAY);
      if (this.grayscale != gs) {
         this.grayscale = gs;
         this.refreshProjectionData();
         changed = true;
      }

      boolean newRenderModels = (Boolean)params.get(PARAM_RENDERMODELS);
      if (this.renderBlockModels != newRenderModels) {
         this.renderBlockModels = newRenderModels;
         this.refreshProjectionData();
         changed = true;
      }

      if (changed) {
         this.setChanged();
         this.markDirtyClient();
      }
   }

   public GenericItemHandler getItems() {
      return this.items;
   }
}
