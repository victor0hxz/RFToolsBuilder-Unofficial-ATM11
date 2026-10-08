package mcjty.rftoolsbuilder.modules.shield.blocks;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.IntStream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mcjty.lib.api.container.DefaultContainerProvider;
import mcjty.lib.api.container.ItemInventory;
import mcjty.lib.api.information.IPowerInformation;
import mcjty.lib.api.infusable.DefaultInfusable;
import mcjty.lib.api.infusable.IInfusable;
import mcjty.lib.api.infusable.ItemInfusable;
import mcjty.lib.api.power.ItemEnergy;
import mcjty.lib.api.smartwrench.ISmartWrenchSelector;
import mcjty.lib.bindings.GuiValue;
import mcjty.lib.bindings.Value;
import mcjty.lib.blockcommands.Command;
import mcjty.lib.blockcommands.ListCommand;
import mcjty.lib.blockcommands.ServerCommand;
import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.GenericItemHandler;
import mcjty.lib.container.SlotDefinition;
import mcjty.lib.setup.Registration;
import mcjty.lib.tileentity.Cap;
import mcjty.lib.tileentity.CapType;
import mcjty.lib.tileentity.GenericEnergyStorage;
import mcjty.lib.tileentity.TickingTileEntity;
import mcjty.lib.typed.Key;
import mcjty.lib.typed.Type;
import mcjty.lib.varia.DamageTools;
import mcjty.lib.varia.FakePlayerGetter;
import mcjty.lib.varia.Logging;
import mcjty.lib.varia.OrientationTools;
import mcjty.lib.varia.Tools;
import mcjty.rftoolsbase.modules.various.VariousModule;
import mcjty.rftoolsbuilder.modules.builder.items.ShapeCardItem;
import mcjty.rftoolsbuilder.modules.shield.DamageTypeMode;
import mcjty.rftoolsbuilder.modules.shield.RelCoordinateShield;
import mcjty.rftoolsbuilder.modules.shield.ShieldConfiguration;
import mcjty.rftoolsbuilder.modules.shield.ShieldModule;
import mcjty.rftoolsbuilder.modules.shield.ShieldRenderingMode;
import mcjty.rftoolsbuilder.modules.shield.ShieldTexture;
import mcjty.rftoolsbuilder.modules.shield.client.GuiShield;
import mcjty.rftoolsbuilder.modules.shield.client.ShieldRenderData;
import mcjty.rftoolsbuilder.modules.shield.data.ShieldData;
import mcjty.rftoolsbuilder.modules.shield.filters.AbstractShieldFilter;
import mcjty.rftoolsbuilder.modules.shield.filters.PlayerFilter;
import mcjty.rftoolsbuilder.modules.shield.filters.ShieldFilter;
import mcjty.rftoolsbuilder.modules.shield.network.PacketNotifyServerClientReady;
import mcjty.rftoolsbuilder.setup.RFToolsBuilderMessages;
import mcjty.rftoolsbuilder.shapes.Shape;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponentMap.Builder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments.Mutable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.Lazy;

public class ShieldProjectorTileEntity extends TickingTileEntity implements ISmartWrenchSelector {
   public static final String COMPONENT_NAME = "shield_projector";
   @GuiValue
   public static final Value<?, Integer> VALUE_SHIELDVISMODE = Value.create(
      "shieldVisMode", Type.INTEGER, te -> te.getShieldRenderingMode().ordinal(), (te, v) -> te.setShieldRenderingMode(ShieldRenderingMode.values()[v])
   );
   @GuiValue
   public static final Value<?, Integer> VALUE_SHIELDTEXTURE = Value.create(
      "shieldTexture", Type.INTEGER, te -> te.getShieldTexture().ordinal(), (te, v) -> te.setShieldTexture(ShieldTexture.values()[v])
   );
   @GuiValue
   public static final Value<?, Integer> VALUE_DAMAGEMODE = Value.create(
      "damageMode", Type.INTEGER, te -> te.getDamageMode().ordinal(), (te, v) -> te.setDamageMode(DamageTypeMode.values()[v])
   );
   @GuiValue
   public static final Value<?, Integer> VALUE_COLOR = Value.create(
      "color", Type.INTEGER, ShieldProjectorTileEntity::getShieldColor, ShieldProjectorTileEntity::setShieldColor
   );
   @GuiValue
   public static final Value<?, Boolean> VALUE_LIGHT = Value.create(
      "light", Type.BOOLEAN, ShieldProjectorTileEntity::isBlockLight, ShieldProjectorTileEntity::setBlockLight
   );
   private ShieldRenderData renderData;
   private boolean shieldComposed = false;
   private BlockState templateState = Blocks.AIR.defaultBlockState();
   private boolean shieldActive = false;
   private int powerTimeout = 0;
   private int updateTimeout = 0;
   private int supportedBlocks;
   private float damageFactor = 1.0F;
   private float costFactor = 1.0F;
   private final List<RelCoordinateShield> shieldBlocks = new ArrayList<>();
   private final List<BlockState> blockStateTable = new ArrayList<>();
   private final FakePlayerGetter fakePlayer = new FakePlayerGetter(this, "rftools_shield");
   public static final int SLOT_BUFFER = 0;
   public static final int SLOT_SHAPE = 1;
   public static final int SLOT_SHARD = 2;
   public static final int BUFFER_SIZE = 3;
   public static final Lazy<ContainerFactory> CONTAINER_FACTORY = Lazy.of(
      () -> new ContainerFactory(3)
         .slot(SlotDefinition.generic().in(), 0, 26, 142)
         .slot(SlotDefinition.specific(s -> s.getItem() instanceof ShapeCardItem).in().out(), 1, 26, 200)
         .slot(SlotDefinition.specific(s -> s.getItem() == VariousModule.DIMENSIONALSHARD.get()).in().out(), 2, 229, 118)
         .playerSlots(85, 142)
   );
   private final GenericItemHandler items = GenericItemHandler.create(this, CONTAINER_FACTORY).itemValid((slot, stack) -> {
      if (slot == 1) {
         return stack.getItem() instanceof ShapeCardItem;
      } else {
         return slot == 2 ? stack.getItem() == VariousModule.DIMENSIONALSHARD.get() : true;
      }
   }).onUpdate((index, stack) -> {
      if (index == 1 && !stack.isEmpty()) {
         this.decomposeShield();
      }
   }).build();
   @Cap(type = CapType.ITEMS_AUTOMATION)
   private static final Function<ShieldProjectorTileEntity, GenericItemHandler> ITEM_CAP = te -> te.items;
   private final GenericEnergyStorage energyStorage;
   @Cap(type = CapType.ENERGY)
   private static final Function<ShieldProjectorTileEntity, GenericEnergyStorage> ENERGY_CAP = te -> te.energyStorage;
   @Cap(type = CapType.CONTAINER)
   private static final Function<ShieldProjectorTileEntity, MenuProvider> SCREEN_CAP = tile -> new DefaultContainerProvider("Shield")
      .containerSupplier(DefaultContainerProvider.container(ShieldModule.CONTAINER_SHIELD, CONTAINER_FACTORY, tile))
      .energyHandler(() -> tile.getEnergyStorage())
      .itemHandler(() -> tile.items)
      .data(ShieldModule.SHIELD_DATA, ShieldData.STREAM_CODEC, ShieldData.CODEC)
      .setupSync(tile);
   private final DefaultInfusable infusable = new DefaultInfusable(this);
   @Cap(type = CapType.INFUSABLE)
   private static final Function<ShieldProjectorTileEntity, IInfusable> INFUSABLE_CAP = te -> te.infusable;
   private final IPowerInformation powerInfoHandler = this.createPowerInfo();
   @Cap(type = CapType.POWER_INFO)
   private static final Function<ShieldProjectorTileEntity, IPowerInformation> POWER_INFO_CAP = te -> te.powerInfoHandler;
   private final int maxEnergy;
   private final int rfPerTick;
   private ItemStack lootingSword = ItemStack.EMPTY;
   public static final Key<Integer> PARAM_ACTION = new Key("action", Type.INTEGER);
   public static final Key<String> PARAM_TYPE = new Key("type", Type.STRING);
   public static final Key<String> PARAM_PLAYER = new Key("player", Type.STRING);
   public static final Key<Integer> PARAM_SELECTED = new Key("selected", Type.INTEGER);
   @ServerCommand
   public static final Command<?> CMD_ADDFILTER = Command.create(
      "shield.addFilter",
      (te, player, params) -> te.addFilter(
         (Integer)params.get(PARAM_ACTION), (String)params.get(PARAM_TYPE), (String)params.get(PARAM_PLAYER), (Integer)params.get(PARAM_SELECTED)
      )
   );
   @ServerCommand
   public static final Command<?> CMD_DELFILTER = Command.create("shield.delFilter", (te, player, params) -> te.delFilter((Integer)params.get(PARAM_SELECTED)));
   @ServerCommand
   public static final Command<?> CMD_UPFILTER = Command.create("shield.upFilter", (te, player, params) -> te.upFilter((Integer)params.get(PARAM_SELECTED)));
   @ServerCommand
   public static final Command<?> CMD_DOWNFILTER = Command.create(
      "shield.downFilter", (te, player, params) -> te.downFilter((Integer)params.get(PARAM_SELECTED))
   );
   @ServerCommand(type = ShieldFilter.class, serializer = ShieldFilter.Serializer.class)
   public static final ListCommand<?, ?> CMD_GETFILTERS = ListCommand.create(
      "rftoolsbuilder.shield.getFilters", (te, player, params) -> te.getFilters(), (te, player, params, list) -> GuiShield.storeFiltersForClient(list)
   );

   public ShieldProjectorTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int supportedBlocks, int maxEnergy, int rfPerTick) {
      super(type, pos, state);
      this.supportedBlocks = supportedBlocks;
      this.maxEnergy = maxEnergy;
      this.rfPerTick = rfPerTick;
      this.energyStorage = new GenericEnergyStorage(this, true, this.getConfigMaxEnergy(), this.getConfigRfPerTick());
   }

   @Nonnull
   public GenericEnergyStorage getEnergyStorage() {
      return this.energyStorage;
   }

   private int getConfigMaxEnergy() {
      return this.maxEnergy;
   }

   private int getConfigRfPerTick() {
      return this.rfPerTick;
   }

   protected boolean needsRedstoneMode() {
      return true;
   }

   public ShieldProjectorTileEntity setDamageFactor(float factor) {
      this.damageFactor = factor;
      return this;
   }

   public ShieldProjectorTileEntity setCostFactor(float factor) {
      this.costFactor = factor;
      return this;
   }

   public ShieldRenderData getRenderData() {
      if (this.renderData == null) {
         int shieldColor = this.getShieldColor();
         float r = (shieldColor >> 16 & 0xFF) / 255.0F;
         float g = (shieldColor >> 8 & 0xFF) / 255.0F;
         float b = (shieldColor & 0xFF) / 255.0F;
         this.renderData = new ShieldRenderData(r, g, b, 1.0F, this.getShieldTexture());
      }

      return this.renderData;
   }

   public void onDataPacket(Connection net, ValueInput input) {
      int oldColor = this.getShieldColor();
      ShieldTexture oldTexture = this.getShieldTexture();
      super.onDataPacket(net, input);
      if (oldColor != this.getShieldColor() || oldTexture != this.getShieldTexture()) {
         this.renderData = null;
         BlockState state = this.level.getBlockState(this.worldPosition);
         this.level.sendBlockUpdated(this.worldPosition, state, state, 3);
      }
   }

   public boolean isPowered() {
      return this.powerLevel > 0;
   }

   public List<ShieldFilter<?>> getFilters() {
      return ((ShieldData)this.getData(ShieldModule.SHIELD_DATA)).filters();
   }

   public boolean isBlockLight() {
      return ((ShieldData)this.getData(ShieldModule.SHIELD_DATA)).blockLight();
   }

   public void setBlockLight(boolean blockLight) {
      ShieldData data = ((ShieldData)this.getData(ShieldModule.SHIELD_DATA)).withBlockLight(blockLight);
      this.setData(ShieldModule.SHIELD_DATA, data);
      this.updateTimeout = 10;
   }

   public int getShieldColor() {
      return ((ShieldData)this.getData(ShieldModule.SHIELD_DATA)).shieldColor();
   }

   public void setShieldColor(int shieldColor) {
      ShieldData data = ((ShieldData)this.getData(ShieldModule.SHIELD_DATA)).withShieldColor(shieldColor);
      this.setData(ShieldModule.SHIELD_DATA, data);
      this.updateTimeout = 10;
   }

   private void delFilter(int selected) {
      ShieldData data = ((ShieldData)this.getData(ShieldModule.SHIELD_DATA)).removeFilter(selected);
      this.setData(ShieldModule.SHIELD_DATA, data);
      this.updateTimeout = 10;
   }

   private void upFilter(int selected) {
      ShieldData data = ((ShieldData)this.getData(ShieldModule.SHIELD_DATA)).moveSelectedFilterUp(selected);
      this.setData(ShieldModule.SHIELD_DATA, data);
   }

   private void downFilter(int selected) {
      ShieldData data = ((ShieldData)this.getData(ShieldModule.SHIELD_DATA)).moveSelectedFilterDown(selected);
      this.setData(ShieldModule.SHIELD_DATA, data);
   }

   private void addFilter(int action, String type, String player, int selected) {
      ShieldFilter filter = AbstractShieldFilter.createFilter(type);
      filter.setAction(action);
      if (filter instanceof PlayerFilter) {
         ((PlayerFilter)filter).setName(player);
      }

      ShieldData data = (ShieldData)this.getData(ShieldModule.SHIELD_DATA);
      if (selected == -1) {
         data = data.addFilter(filter);
      } else {
         data = data.addFilter(filter, selected);
      }

      this.setData(ShieldModule.SHIELD_DATA, data);
      this.updateTimeout = 10;
   }

   public DamageTypeMode getDamageMode() {
      return ((ShieldData)this.getData(ShieldModule.SHIELD_DATA)).damageMode();
   }

   public void setDamageMode(DamageTypeMode damageMode) {
      ShieldData data = ((ShieldData)this.getData(ShieldModule.SHIELD_DATA)).withDamageMode(damageMode);
      this.setData(ShieldModule.SHIELD_DATA, data);
   }

   public ShieldRenderingMode getShieldRenderingMode() {
      return ((ShieldData)this.getData(ShieldModule.SHIELD_DATA)).renderMode();
   }

   public void setShieldRenderingMode(ShieldRenderingMode shieldRenderingMode) {
      ShieldData data = ((ShieldData)this.getData(ShieldModule.SHIELD_DATA)).withRenderMode(shieldRenderingMode);
      this.setData(ShieldModule.SHIELD_DATA, data);
      this.updateTimeout = 10;
   }

   public ShieldTexture getShieldTexture() {
      return ((ShieldData)this.getData(ShieldModule.SHIELD_DATA)).shieldTexture();
   }

   public void setShieldTexture(ShieldTexture shieldTexture) {
      ShieldData data = ((ShieldData)this.getData(ShieldModule.SHIELD_DATA)).withShieldTexture(shieldTexture);
      this.setData(ShieldModule.SHIELD_DATA, data);
      this.updateTimeout = 10;
   }

   @Nonnull
   private BlockState getStateFromItem(ItemStack stack) {
      if (stack.getItem() instanceof BlockItem blockItem) {
         Player player = this.fakePlayer.get();
         player.setItemInHand(InteractionHand.MAIN_HAND, stack);
         BlockHitResult result = new BlockHitResult(new Vec3(0.5, 0.0, 0.5), Direction.UP, this.worldPosition, false);
         BlockPlaceContext context = new BlockPlaceContext(new UseOnContext(player, InteractionHand.MAIN_HAND, result));
         BlockState stateForPlacement = blockItem.getBlock().getStateForPlacement(context);
         return stateForPlacement == null ? blockItem.getBlock().defaultBlockState() : stateForPlacement;
      } else {
         return Blocks.AIR.defaultBlockState();
      }
   }

   @Nullable
   private BlockState calculateMimic() {
      if (!ShieldRenderingMode.MIMIC.equals(this.getShieldRenderingMode())) {
         return null;
      } else {
         ItemStack stackInSlot = this.items.getStackInSlot(0);
         return stackInSlot.isEmpty() ? null : this.getStateFromItem(stackInSlot);
      }
   }

   private BlockState calculateShieldBlock(BlockState mimic, boolean blockLight) {
      if (this.shieldActive && this.powerTimeout <= 0) {
         ShieldRenderingMode render = this.getShieldRenderingMode();
         if (!(Boolean)ShieldConfiguration.allowInvisibleShield.get() && ShieldRenderingMode.INVISIBLE.equals(render)) {
            render = ShieldRenderingMode.SOLID;
         }

         if (mimic != null) {
            render = ShieldRenderingMode.MIMIC;
         }

         BlockState shielding = this.getShieldingBlock(render, mimic).defaultBlockState();
         shielding = (BlockState)shielding.setValue(ShieldingBlock.FLAG_OPAQUE, !blockLight);
         shielding = (BlockState)shielding.setValue(ShieldingBlock.RENDER_MODE, render);
         shielding = this.calculateShieldCollisionData(shielding);
         return this.calculateDamageBits(shielding);
      } else {
         return Blocks.AIR.defaultBlockState();
      }
   }

   private Block getShieldingBlock(ShieldRenderingMode render, BlockState mimic) {
      if (mimic != null) {
         return (Block)ShieldModule.SHIELDING_SOLID.get();
      } else {
         return render.isTranslucent() ? (Block)ShieldModule.SHIELDING_TRANSLUCENT.get() : (Block)ShieldModule.SHIELDING_SOLID.get();
      }
   }

   private BlockState calculateDamageBits(BlockState shielding) {
      for (ShieldFilter<?> filter : this.getFilters()) {
         if ((filter.getAction() & 2) != 0) {
            if ("item".equals(filter.getFilterName())) {
               shielding = (BlockState)shielding.setValue(ShieldingBlock.DAMAGE_ITEMS, true);
            } else if ("animal".equals(filter.getFilterName())) {
               shielding = (BlockState)shielding.setValue(ShieldingBlock.DAMAGE_PASSIVE, true);
            } else if ("hostile".equals(filter.getFilterName())) {
               shielding = (BlockState)shielding.setValue(ShieldingBlock.DAMAGE_HOSTILE, true);
            } else if ("player".equals(filter.getFilterName())) {
               shielding = (BlockState)shielding.setValue(ShieldingBlock.DAMAGE_PLAYERS, true);
            } else if ("default".equals(filter.getFilterName())) {
               shielding = (BlockState)((BlockState)((BlockState)((BlockState)shielding.setValue(ShieldingBlock.DAMAGE_ITEMS, true))
                        .setValue(ShieldingBlock.DAMAGE_PASSIVE, true))
                     .setValue(ShieldingBlock.DAMAGE_HOSTILE, true))
                  .setValue(ShieldingBlock.DAMAGE_PLAYERS, true);
            }
         }
      }

      return shielding;
   }

   private BlockState calculateShieldCollisionData(BlockState shielding) {
      for (ShieldFilter<?> filter : this.getFilters()) {
         if ((filter.getAction() & 1) != 0) {
            if ("item".equals(filter.getFilterName())) {
               shielding = (BlockState)shielding.setValue(ShieldingBlock.BLOCKED_ITEMS, true);
            } else if ("animal".equals(filter.getFilterName())) {
               shielding = (BlockState)shielding.setValue(ShieldingBlock.BLOCKED_PASSIVE, true);
            } else if ("hostile".equals(filter.getFilterName())) {
               shielding = (BlockState)shielding.setValue(ShieldingBlock.BLOCKED_HOSTILE, true);
            } else if ("player".equals(filter.getFilterName())) {
               shielding = (BlockState)shielding.setValue(ShieldingBlock.BLOCKED_PLAYERS, true);
            } else if ("default".equals(filter.getFilterName())) {
               shielding = (BlockState)((BlockState)((BlockState)((BlockState)shielding.setValue(ShieldingBlock.BLOCKED_ITEMS, true))
                        .setValue(ShieldingBlock.BLOCKED_PASSIVE, true))
                     .setValue(ShieldingBlock.BLOCKED_HOSTILE, true))
                  .setValue(ShieldingBlock.BLOCKED_PLAYERS, true);
            }
         }
      }

      return shielding;
   }

   private int calculateRfPerTick() {
      if (!this.shieldActive) {
         return 0;
      } else {
         int s = this.shieldBlocks.size() - 50;
         if (s < 10) {
            s = 10;
         }

         int rf = (Integer)ShieldConfiguration.rfBase.get() * s / 10;
         if (ShieldRenderingMode.SHIELD.equals(this.getShieldRenderingMode())) {
            rf += ShieldConfiguration.rfShield.get() * s / 10;
         } else if (ShieldRenderingMode.MIMIC.equals(this.getShieldRenderingMode())) {
            rf += ShieldConfiguration.rfCamo.get() * s / 10;
         }

         return rf;
      }
   }

   public boolean isShieldComposed() {
      return this.shieldComposed;
   }

   public boolean isShieldActive() {
      return this.shieldActive;
   }

   public void applyDamageToEntity(Entity entity) {
      DamageSource source;
      int rf;
      if (DamageTypeMode.DAMAGETYPE_GENERIC.equals(this.getDamageMode())) {
         rf = (Integer)ShieldConfiguration.rfDamage.get();
         source = DamageTools.getGenericDamageSource(entity);
      } else {
         rf = (Integer)ShieldConfiguration.rfDamagePlayer.get();
         ServerPlayer killer = this.fakePlayer.get();
         killer.setPos(this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ());
         ItemStack shards = this.items.getStackInSlot(2);
         if (!shards.isEmpty() && shards.getCount() >= (Integer)ShieldConfiguration.shardsPerLootingKill.get()) {
            this.items.extractItem(2, (Integer)ShieldConfiguration.shardsPerLootingKill.get(), false);
            if (this.lootingSword.isEmpty()) {
               this.lootingSword = createEnchantedItem(
                  entity.level(), Items.DIAMOND_SWORD, Enchantments.LOOTING, (Integer)ShieldConfiguration.lootingKillBonus.get()
               );
            }

            this.lootingSword.setDamageValue(0);
            killer.setItemInHand(InteractionHand.MAIN_HAND, this.lootingSword);
         } else {
            killer.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
         }

         source = DamageTools.getPlayerAttackDamageSource(killer, killer);
      }

      float factor = this.infusable.getInfusedFactor();
      rf = (int)(rf * this.costFactor * (4.0F - factor) / 4.0F);
      if (this.energyStorage.getEnergyStored() >= rf) {
         this.energyStorage.consumeEnergy(rf);
         float damage = (float)((Double)ShieldConfiguration.damage.get()).doubleValue();
         damage *= this.damageFactor;
         damage *= 1.0F + factor / 2.0F;
         entity.hurt(source, damage);
      }
   }

   public static ItemStack createEnchantedItem(Level level, Item item, ResourceKey<Enchantment> effectId, int amount) {
      ItemStack stack = new ItemStack(item);
      Mutable enchant = new Mutable(ItemEnchantments.EMPTY);
      Optional<Reference<Enchantment>> holder = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(effectId);
      holder.ifPresent(h -> enchant.set(h, amount));
      EnchantmentHelper.setEnchantments(stack, enchant.toImmutable());
      return stack;
   }

   protected void tickServer() {
      if (this.level != null) {
         if (this.shieldComposed) {
            if (this.updateTimeout > 0) {
               this.updateTimeout--;
               if (this.updateTimeout <= 0) {
                  this.updateShield();
               }
            }

            boolean checkPower = false;
            if (this.powerTimeout > 0) {
               this.powerTimeout--;
               this.setChanged();
               if (this.powerTimeout > 0) {
                  return;
               }

               checkPower = true;
            }

            boolean needsUpdate = false;
            int rf = this.getRfPerTick();
            if (rf > 0) {
               if (this.energyStorage.getEnergyStored() < rf) {
                  this.powerTimeout = 100;
                  needsUpdate = true;
               } else {
                  if (checkPower) {
                     needsUpdate = true;
                  }

                  this.energyStorage.consumeEnergy(rf);
               }
            }

            boolean newShieldActive = this.isMachineEnabled();
            if (newShieldActive != this.shieldActive) {
               needsUpdate = true;
               this.shieldActive = newShieldActive;
            }

            if (needsUpdate) {
               this.updateShield();
               this.markDirtyClient();
            }
         }
      }
   }

   public void clientIsReady() {
      this.updateShield();
   }

   private int getRfPerTick() {
      int rf = this.calculateRfPerTick();
      float factor = this.infusable.getInfusedFactor();
      return (int)(rf * (2.0F - factor) / 2.0F);
   }

   public void composeDecomposeShield(boolean ctrl) {
      if (this.shieldComposed) {
         this.decomposeShield();
      } else {
         this.composeShield(ctrl);
      }
   }

   public void composeShield(boolean ctrl) {
      this.shieldBlocks.clear();
      this.blockStateTable.clear();
      Map<BlockPos, BlockState> coordinates;
      if (this.isShapedShield()) {
         this.templateState = Blocks.AIR.defaultBlockState();
         ItemStack shapeItem = this.items.getStackInSlot(1);
         Shape shape = ShapeCardItem.getShape(shapeItem);
         boolean solid = ShapeCardItem.isSolid(shapeItem);
         BlockPos dimension = ShapeCardItem.getClampedDimension(shapeItem, (Integer)ShieldConfiguration.maxShieldDimension.get());
         BlockPos offset = ShapeCardItem.getClampedOffset(shapeItem, (Integer)ShieldConfiguration.maxShieldOffset.get());
         Map<BlockPos, BlockState> col = new HashMap<>();
         ShapeCardItem.composeFormula(
            shapeItem, shape.getFormulaFactory().get(), this.getLevel(), this.getBlockPos(), dimension, offset, col, this.supportedBlocks, solid, false, null
         );
         coordinates = col;
      } else {
         if (!this.findTemplateState()) {
            return;
         }

         Map<BlockPos, BlockState> col = new HashMap<>();
         this.findTemplateBlocks(col, this.templateState, ctrl, this.getBlockPos());
         coordinates = col;
      }

      int xCoord = this.getBlockPos().getX();
      int yCoord = this.getBlockPos().getY();
      int zCoord = this.getBlockPos().getZ();

      for (Entry<BlockPos, BlockState> entry : coordinates.entrySet()) {
         BlockPos c = entry.getKey();
         BlockState state = entry.getValue();
         int st = -1;
         if (state != null) {
            for (int i = 0; i < this.blockStateTable.size(); i++) {
               if (state.equals(this.blockStateTable.get(i))) {
                  st = i;
                  break;
               }
            }

            if (st == -1) {
               st = this.blockStateTable.size();
               this.blockStateTable.add(state);
            }
         }

         this.shieldBlocks.add(new RelCoordinateShield(c.getX() - xCoord, c.getY() - yCoord, c.getZ() - zCoord, st));
         this.getLevel().setBlockAndUpdate(c, Blocks.AIR.defaultBlockState());
      }

      this.shieldComposed = true;
      this.updateShield();
   }

   private boolean isShapedShield() {
      return !this.items.getStackInSlot(1).isEmpty();
   }

   private boolean findTemplateState() {
      for (Direction dir : OrientationTools.DIRECTION_VALUES) {
         BlockPos p = this.getBlockPos().relative(dir);
         if (p.getY() >= this.level.getMinY() && p.getY() < this.level.getMaxY()) {
            BlockState state = this.getLevel().getBlockState(p);
            if (state.getBlock() instanceof ShieldTemplateBlock) {
               this.templateState = state;
               return true;
            }
         }
      }

      return false;
   }

   public void selectBlock(Player player, BlockPos pos) {
      if (!this.shieldComposed) {
         Logging.message(player, ChatFormatting.YELLOW + "Shield is not composed. Nothing happens!");
      } else {
         float squaredDistance = (float)this.getBlockPos().distSqr(pos);
         if (squaredDistance > (Integer)ShieldConfiguration.maxDisjointShieldDistance.get() * (Integer)ShieldConfiguration.maxDisjointShieldDistance.get()) {
            Logging.message(player, ChatFormatting.YELLOW + "This template is too far to connect to the shield!");
         } else {
            int xCoord = this.getBlockPos().getX();
            int yCoord = this.getBlockPos().getY();
            int zCoord = this.getBlockPos().getZ();
            Block origBlock = this.getLevel().getBlockState(pos).getBlock();
            if (origBlock instanceof ShieldTemplateBlock) {
               if (this.isShapedShield()) {
                  Logging.message(player, ChatFormatting.YELLOW + "You cannot add template blocks to a shaped shield (using a shape card)!");
                  return;
               }

               Map<BlockPos, BlockState> templateBlocks = new HashMap<>();
               BlockState state = this.getLevel().getBlockState(pos);
               templateBlocks.put(pos, null);
               this.findTemplateBlocks(templateBlocks, state, false, pos);
               BlockState mimic = this.calculateMimic();
               BlockState shielding = this.calculateShieldBlock(mimic, this.isBlockLight());

               for (Entry<BlockPos, BlockState> entry : templateBlocks.entrySet()) {
                  BlockPos templateBlock = entry.getKey();
                  RelCoordinateShield relc = new RelCoordinateShield(
                     templateBlock.getX() - xCoord, templateBlock.getY() - yCoord, templateBlock.getZ() - zCoord, -1
                  );
                  this.shieldBlocks.add(relc);
                  this.updateShieldBlock(mimic, shielding, relc);
               }
            } else {
               if (!(origBlock instanceof ShieldingBlock)) {
                  Logging.message(player, ChatFormatting.YELLOW + "The selected shield can't do anything with this block!");
                  return;
               }

               int dx = pos.getX() - xCoord;
               int dy = pos.getY() - yCoord;
               int dz = pos.getZ() - zCoord;
               int idx = IntStream.range(0, this.shieldBlocks.size()).filter(i -> this.shieldBlocks.get(i).matches(dx, dy, dz)).findFirst().orElse(-1);
               if (idx != -1) {
                  this.shieldBlocks.remove(idx);
               }

               this.getLevel().setBlock(pos, this.templateState, 2);
            }

            this.setChanged();
         }
      }
   }

   private void updateShield() {
      BlockState mimic = this.calculateMimic();
      BlockState shielding = this.calculateShieldBlock(mimic, this.isBlockLight());
      int xCoord = this.getBlockPos().getX();
      int yCoord = this.getBlockPos().getY();
      int zCoord = this.getBlockPos().getZ();
      MutableBlockPos pos = new MutableBlockPos();

      for (RelCoordinateShield c : this.shieldBlocks) {
         if (Blocks.AIR.equals(shielding.getBlock())) {
            pos.set(xCoord + c.dx(), yCoord + c.dy(), zCoord + c.dz());
            BlockState oldState = this.getLevel().getBlockState(pos);
            if (oldState.getBlock() instanceof ShieldingBlock) {
               this.getLevel().setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            }
         } else {
            this.updateShieldBlock(mimic, shielding, c);
         }
      }

      this.setChanged();
   }

   private void updateShieldBlock(BlockState mimic, BlockState shielding, RelCoordinateShield c) {
      int xCoord = this.getBlockPos().getX();
      int yCoord = this.getBlockPos().getY();
      int zCoord = this.getBlockPos().getZ();
      BlockPos pp = new BlockPos(xCoord + c.dx(), yCoord + c.dy(), zCoord + c.dz());
      BlockState oldState = this.getLevel().getBlockState(pp);
      if (oldState.getBlock() instanceof ShieldingBlock || oldState.canBeReplaced() || oldState.getBlock() instanceof ShieldTemplateBlock) {
         this.level.setBlockAndUpdate(pp, Blocks.AIR.defaultBlockState());
         this.level.setBlock(pp, shielding, 1);
         if (this.getLevel().getBlockEntity(pp) instanceof ShieldingTileEntity shieldingTE) {
            if (c.state() != -1) {
               BlockState state = this.blockStateTable.get(c.state());
               shieldingTE.setMimic(state);
            } else {
               shieldingTE.setMimic(mimic);
            }

            shieldingTE.setShieldProjector(this.worldPosition);
         }
      }
   }

   public void decomposeShield() {
      int xCoord = this.getBlockPos().getX();
      int yCoord = this.getBlockPos().getY();
      int zCoord = this.getBlockPos().getZ();
      MutableBlockPos pp = new MutableBlockPos();

      for (RelCoordinateShield c : this.shieldBlocks) {
         int cx = xCoord + c.dx();
         int cy = yCoord + c.dy();
         int cz = zCoord + c.dz();
         pp.set(cx, cy, cz);
         Block block = this.getLevel().getBlockState(pp).getBlock();
         if (this.getLevel().isEmptyBlock(pp) || block instanceof ShieldingBlock) {
            this.getLevel().setBlock(new BlockPos(pp), this.templateState, 2);
         } else if (!this.templateState.isAir() && !this.isShapedShield()) {
            Containers.dropItemStack(
               this.getLevel(),
               cx,
               cy,
               cz,
               this.templateState.getBlock().getCloneItemStack(this.getLevel(), new BlockPos(cx, cy, cz), this.templateState, true, null)
            );
         }
      }

      this.shieldComposed = false;
      this.shieldActive = false;
      this.shieldBlocks.clear();
      this.blockStateTable.clear();
      this.setChanged();
   }

   private void findTemplateBlocks(Map<BlockPos, BlockState> coordinateSet, BlockState templateState, boolean ctrl, BlockPos start) {
      Deque<BlockPos> todo = new ArrayDeque<>();
      if (ctrl) {
         this.addToTodoCornered(coordinateSet, todo, start, templateState);

         while (!todo.isEmpty() && coordinateSet.size() < this.supportedBlocks) {
            BlockPos coordinate = todo.pollFirst();
            coordinateSet.put(coordinate, null);
            this.addToTodoCornered(coordinateSet, todo, coordinate, templateState);
         }
      } else {
         this.addToTodoStraight(coordinateSet, todo, start, templateState);

         while (!todo.isEmpty() && coordinateSet.size() < this.supportedBlocks) {
            BlockPos coordinate = todo.pollFirst();
            coordinateSet.put(coordinate, null);
            this.addToTodoStraight(coordinateSet, todo, coordinate, templateState);
         }
      }
   }

   private void addToTodoStraight(Map<BlockPos, BlockState> coordinateSet, Deque<BlockPos> todo, BlockPos coordinate, BlockState templateState) {
      for (Direction dir : OrientationTools.DIRECTION_VALUES) {
         BlockPos pp = coordinate.relative(dir);
         if (pp.getY() >= this.level.getMinY() && pp.getY() < this.level.getMaxY() && !coordinateSet.containsKey(pp)) {
            BlockState state = this.getLevel().getBlockState(pp);
            if (state == templateState && !todo.contains(pp)) {
               todo.addLast(pp);
            }
         }
      }
   }

   private void addToTodoCornered(Map<BlockPos, BlockState> coordinateSet, Deque<BlockPos> todo, BlockPos coordinate, BlockState templateState) {
      int x = coordinate.getX();
      int y = coordinate.getY();
      int z = coordinate.getZ();
      MutableBlockPos c = new MutableBlockPos();

      for (int xx = x - 1; xx <= x + 1; xx++) {
         for (int yy = y - 1; yy <= y + 1; yy++) {
            for (int zz = z - 1; zz <= z + 1; zz++) {
               if ((xx != x || yy != y || zz != z) && yy >= this.getLevel().getMinY() && yy < this.getLevel().getMaxY()) {
                  c.set(xx, yy, zz);
                  if (!coordinateSet.containsKey(c)) {
                     BlockState state = this.getLevel().getBlockState(c);
                     if (state == templateState && !todo.contains(c)) {
                        todo.addLast(c.immutable());
                     }
                  }
               }
            }
         }
      }
   }

   private static short bytesToShort(byte b1, byte b2) {
      short s1 = (short)(b1 & 255);
      short s2 = (short)(b2 & 255);
      return (short)(s1 * 256 + s2);
   }

   private static byte shortToByte1(short s) {
      return (byte)((s & '\uff00') >> 8);
   }

   private static byte shortToByte2(short s) {
      return (byte)(s & 255);
   }

   public void loadClientDataFromNBT(CompoundTag tagCompound, Provider provider) {
      this.powerLevel = tagCompound.getByteOr("powered", (byte)0);
      this.shieldComposed = tagCompound.getBooleanOr("composed", false);
      this.shieldActive = tagCompound.getBooleanOr("active", false);
      this.powerTimeout = tagCompound.getIntOr("powerTimeout", 0);
      if (tagCompound.contains("templateColor")) {
         int templateColor = tagCompound.getIntOr("templateColor", 0);
         ShieldTemplateBlock.TemplateColor color = ShieldTemplateBlock.TemplateColor.values()[templateColor];
         switch (color) {
            case BLUE:
               this.templateState = ((ShieldTemplateBlock)ShieldModule.TEMPLATE_BLUE.get()).defaultBlockState();
               break;
            case RED:
               this.templateState = ((ShieldTemplateBlock)ShieldModule.TEMPLATE_RED.get()).defaultBlockState();
               break;
            case GREEN:
               this.templateState = ((ShieldTemplateBlock)ShieldModule.TEMPLATE_GREEN.get()).defaultBlockState();
               break;
            case YELLOW:
               this.templateState = ((ShieldTemplateBlock)ShieldModule.TEMPLATE_YELLOW.get()).defaultBlockState();
         }
      } else {
         this.templateState = Blocks.AIR.defaultBlockState();
      }

      ShieldData.CODEC
         .decode(NbtOps.INSTANCE, tagCompound.get("data"))
         .result()
         .ifPresent(data -> this.setData(ShieldModule.SHIELD_DATA, (ShieldData)data.getFirst()));
      this.renderData = null;
      RFToolsBuilderMessages.sendToServer(PacketNotifyServerClientReady.create(this.worldPosition));
   }

   public void saveClientDataToNBT(CompoundTag tagCompound, Provider provider) {
      tagCompound.putByte("powered", this.powerLevel);
      tagCompound.putBoolean("composed", this.shieldComposed);
      tagCompound.putBoolean("active", this.shieldActive);
      tagCompound.putInt("powerTimeout", this.powerTimeout);
      if (!this.templateState.isAir()) {
         tagCompound.putInt("templateColor", ((ShieldTemplateBlock)this.templateState.getBlock()).getColor().ordinal());
      }

      ShieldData.CODEC
         .encodeStart(NbtOps.INSTANCE, (ShieldData)this.getData(ShieldModule.SHIELD_DATA))
         .result()
         .ifPresent(data -> tagCompound.put("data", data));
   }

   public void loadAdditional(CompoundTag tag, Provider provider) {
      super.loadAdditional(tag, provider);
      this.energyStorage.load(tag, "energy", provider);
      this.items.load(tag, "items", provider);
      this.infusable.load(tag, "infusable");
      this.shieldComposed = tag.getBooleanOr("composed", false);
      this.shieldActive = tag.getBooleanOr("active", false);
      this.powerTimeout = tag.getIntOr("powerTimeout", 0);
      if (!this.isShapedShield()) {
         if (tag.contains("templateColor")) {
            int templateColor = tag.getIntOr("templateColor", 0);
            ShieldTemplateBlock.TemplateColor color = ShieldTemplateBlock.TemplateColor.values()[templateColor];
            switch (color) {
               case BLUE:
                  this.templateState = ((ShieldTemplateBlock)ShieldModule.TEMPLATE_BLUE.get()).defaultBlockState();
                  break;
               case RED:
                  this.templateState = ((ShieldTemplateBlock)ShieldModule.TEMPLATE_RED.get()).defaultBlockState();
                  break;
               case GREEN:
                  this.templateState = ((ShieldTemplateBlock)ShieldModule.TEMPLATE_GREEN.get()).defaultBlockState();
                  break;
               case YELLOW:
                  this.templateState = ((ShieldTemplateBlock)ShieldModule.TEMPLATE_YELLOW.get()).defaultBlockState();
            }
         } else {
            this.templateState = Blocks.AIR.defaultBlockState();
         }
      } else {
         this.templateState = Blocks.AIR.defaultBlockState();
      }

      this.shieldBlocks.clear();
      this.blockStateTable.clear();
      if (tag.contains("relcoordsNew")) {
         byte[] byteArray = tag.getByteArray("relcoordsNew").orElseGet(() -> new byte[0]);
         int j = 0;

         for (int i = 0; i < byteArray.length / 8; i++) {
            short dx = bytesToShort(byteArray[j + 0], byteArray[j + 1]);
            short dy = bytesToShort(byteArray[j + 2], byteArray[j + 3]);
            short dz = bytesToShort(byteArray[j + 4], byteArray[j + 5]);
            short st = bytesToShort(byteArray[j + 6], byteArray[j + 7]);
            j += 8;
            this.shieldBlocks.add(new RelCoordinateShield(dx, dy, dz, st));
         }

         for (Tag inbt : tag.getListOrEmpty("gstates")) {
            CompoundTag tc = (CompoundTag)inbt;
            String b = tc.getStringOr("b", "");
            int m = tc.getIntOr("m", 0);
            Block block = Tools.getBlock(Identifier.parse(b));
            if (block == null) {
               block = Blocks.STONE;
               int var26 = false;
            }

            BlockState state = block.defaultBlockState();
            this.blockStateTable.add(state);
         }
      } else {
         byte[] byteArray = tag.getByteArray("relcoords").orElseGet(() -> new byte[0]);
         int j = 0;

         for (int i = 0; i < byteArray.length / 6; i++) {
            short dx = bytesToShort(byteArray[j + 0], byteArray[j + 1]);
            short dy = bytesToShort(byteArray[j + 2], byteArray[j + 3]);
            short dz = bytesToShort(byteArray[j + 4], byteArray[j + 5]);
            j += 6;
            this.shieldBlocks.add(new RelCoordinateShield(dx, dy, dz, -1));
         }
      }
   }

   protected void applyImplicitComponents(DataComponentGetter input) {
      super.applyImplicitComponents(input);
      this.energyStorage.applyImplicitComponents((ItemEnergy)input.get((DataComponentType)Registration.ITEM_ENERGY.get()));
      this.items.applyImplicitComponents((ItemInventory)input.get((DataComponentType)Registration.ITEM_INVENTORY.get()));
      this.infusable.applyImplicitComponents((ItemInfusable)input.get((DataComponentType)Registration.ITEM_INFUSABLE.get()));
      ShieldData shieldData = (ShieldData)input.get(ShieldModule.ITEM_SHIELD_DATA);
      if (shieldData != null) {
         this.setData(ShieldModule.SHIELD_DATA, shieldData);
      }
   }

   protected void collectImplicitComponents(Builder builder) {
      super.collectImplicitComponents(builder);
      this.energyStorage.collectImplicitComponents(builder);
      this.items.collectImplicitComponents(builder);
      this.infusable.collectImplicitComponents(builder);
      builder.set(ShieldModule.ITEM_SHIELD_DATA, (ShieldData)this.getData(ShieldModule.SHIELD_DATA));
   }

   public void saveAdditional(@Nonnull CompoundTag tag, Provider provider) {
      super.saveAdditional(tag, provider);
      this.energyStorage.save(tag, "energy", provider);
      this.items.save(tag, "items", provider);
      this.infusable.save(tag, "infusable");
      tag.putBoolean("composed", this.shieldComposed);
      tag.putBoolean("active", this.shieldActive);
      tag.putInt("powerTimeout", this.powerTimeout);
      if (!this.templateState.isAir()) {
         tag.putInt("templateColor", ((ShieldTemplateBlock)this.templateState.getBlock()).getColor().ordinal());
      }

      byte[] blocks = new byte[this.shieldBlocks.size() * 8];
      int j = 0;

      for (RelCoordinateShield c : this.shieldBlocks) {
         blocks[j + 0] = shortToByte1((short)c.dx());
         blocks[j + 1] = shortToByte2((short)c.dx());
         blocks[j + 2] = shortToByte1((short)c.dy());
         blocks[j + 3] = shortToByte2((short)c.dy());
         blocks[j + 4] = shortToByte1((short)c.dz());
         blocks[j + 5] = shortToByte2((short)c.dz());
         blocks[j + 6] = shortToByte1((short)c.state());
         blocks[j + 7] = shortToByte2((short)c.state());
         j += 8;
      }

      tag.putByteArray("relcoordsNew", blocks);
      ListTag list = new ListTag();

      for (BlockState state : this.blockStateTable) {
         CompoundTag tc = new CompoundTag();
         tc.putString("b", Tools.getId(state).toString());
         list.add(tc);
      }

      tag.put("gstates", list);
   }

   @Nonnull
   private IPowerInformation createPowerInfo() {
      return new IPowerInformation() {
         {
            Objects.requireNonNull(ShieldProjectorTileEntity.this);
         }

         public long getEnergyDiffPerTick() {
            return ShieldProjectorTileEntity.this.shieldActive ? ShieldProjectorTileEntity.this.getRfPerTick() : 0L;
         }

         public String getEnergyUnitName() {
            return "RF";
         }

         public boolean isMachineActive() {
            return ShieldProjectorTileEntity.this.shieldActive;
         }

         public boolean isMachineRunning() {
            return ShieldProjectorTileEntity.this.shieldActive;
         }

         public String getMachineStatus() {
            return ShieldProjectorTileEntity.this.shieldActive ? "active" : "idle";
         }
      };
   }
}
