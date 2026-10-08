package mcjty.rftoolsbuilder.modules.scanner.blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
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
import mcjty.rftoolsbuilder.modules.builder.BuilderModule;
import mcjty.rftoolsbuilder.modules.builder.data.ShapeCardData;
import mcjty.rftoolsbuilder.modules.builder.items.ShapeCardItem;
import mcjty.rftoolsbuilder.modules.scanner.ScannerModule;
import mcjty.rftoolsbuilder.shapes.Shape;
import mcjty.rftoolsbuilder.shapes.ShapeModifier;
import mcjty.rftoolsbuilder.shapes.ShapeOperation;
import mcjty.rftoolsbuilder.shapes.ShapeRotation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponentMap.Builder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.Lazy;

public class ComposerTileEntity extends TickingTileEntity {
   public static final String CMD_SETTINGS_ID = "composer.settings";
   public static final int SLOT_COUNT = 9;
   public static final int SLOT_OUT = 0;
   public static final int SLOT_TABS = 1;
   public static final int SLOT_GHOSTS = 10;
   private static final int SIDE_PANEL_WIDTH = 80;
   private static final int CARD_SLOT_X = 98;
   private static final int MATERIAL_SLOT_X = 116;
   private static final int PLAYER_SLOTS_X = 165;
   public static final List<Key<String>> PARAM_OPS = List.of(
      new Key("op0", Type.STRING),
      new Key("op1", Type.STRING),
      new Key("op2", Type.STRING),
      new Key("op3", Type.STRING),
      new Key("op4", Type.STRING),
      new Key("op5", Type.STRING),
      new Key("op6", Type.STRING),
      new Key("op7", Type.STRING),
      new Key("op8", Type.STRING)
   );
   public static final List<Key<Boolean>> PARAM_FLIPS = List.of(
      new Key("flip0", Type.BOOLEAN),
      new Key("flip1", Type.BOOLEAN),
      new Key("flip2", Type.BOOLEAN),
      new Key("flip3", Type.BOOLEAN),
      new Key("flip4", Type.BOOLEAN),
      new Key("flip5", Type.BOOLEAN),
      new Key("flip6", Type.BOOLEAN),
      new Key("flip7", Type.BOOLEAN),
      new Key("flip8", Type.BOOLEAN)
   );
   public static final List<Key<String>> PARAM_ROTS = List.of(
      new Key("rot0", Type.STRING),
      new Key("rot1", Type.STRING),
      new Key("rot2", Type.STRING),
      new Key("rot3", Type.STRING),
      new Key("rot4", Type.STRING),
      new Key("rot5", Type.STRING),
      new Key("rot6", Type.STRING),
      new Key("rot7", Type.STRING),
      new Key("rot8", Type.STRING)
   );
   public static final Lazy<ContainerFactory> CONTAINER_FACTORY = Lazy.of(
      () -> {
         ContainerFactory factory = new ContainerFactory(19)
            .slot(SlotDefinition.specific(s -> s.getItem() instanceof ShapeCardItem).in().out(), 0, 98, 200)
            .playerSlots(165, 142);

         for (int i = 0; i < 9; i++) {
            factory.slot(SlotDefinition.specific(s -> s.getItem() instanceof ShapeCardItem).in().out(), 1 + i, 98, 7 + i * 18);
            factory.slot(SlotDefinition.specific(s -> true).in().out(), 10 + i, 116, 7 + i * 18);
         }

         return factory;
      }
   );
   private final GenericItemHandler items = GenericItemHandler.create(this, CONTAINER_FACTORY)
      .itemValid((slot, stack) -> slot != 0 && (slot < 1 || slot >= 10) ? slot >= 10 && slot < 19 : stack.getItem() instanceof ShapeCardItem)
      .onUpdate((slot, stack) -> this.markComposerDirty())
      .build();
   @Cap(type = CapType.ITEMS_AUTOMATION)
   private static final Function<ComposerTileEntity, GenericItemHandler> ITEM_CAP = tile -> tile.items;
   @Cap(type = CapType.CONTAINER)
   private static final Function<ComposerTileEntity, MenuProvider> SCREEN_CAP = tile -> new DefaultContainerProvider("Composer")
      .containerSupplier(DefaultContainerProvider.container(ScannerModule.CONTAINER_COMPOSER, CONTAINER_FACTORY, tile))
      .itemHandler(() -> tile.items)
      .setupSync(tile);
   private final ShapeModifier[] modifiers = new ShapeModifier[9];
   private boolean composeDirty = true;
   @ServerCommand
   public static final Command<?> CMD_SETTINGS = Command.create("composer.settings", (te, player, params) -> te.applySettings(params));

   public ComposerTileEntity(BlockPos pos, BlockState state) {
      this(ScannerModule.TYPE_COMPOSER.get(), pos, state);
   }

   public ComposerTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
      super(type, pos, state);

      for (int i = 0; i < 9; i++) {
         this.modifiers[i] = new ShapeModifier(ShapeOperation.UNION, false, ShapeRotation.NONE);
      }
   }

   protected void tickServer() {
      if (this.composeDirty) {
         this.updateOutput();
         this.composeDirty = false;
      }
   }

   public ShapeModifier[] getModifiers() {
      return this.modifiers;
   }

   private void markComposerDirty() {
      this.composeDirty = true;
      this.setChanged();
      this.markDirtyClient();
   }

   private void updateOutput() {
      ItemStack output = this.items.getStackInSlot(0);
      if (!output.isEmpty()) {
         List<ShapeCardData.ShapeCardChild> children = new ArrayList<>();

         for (int i = 0; i < 9; i++) {
            ItemStack card = this.items.getStackInSlot(1 + i);
            if (!card.isEmpty()) {
               ItemStack childStack = card.copy();
               ShapeCardItem.setModifier(childStack, this.modifiers[i]);
               ShapeCardItem.setGhostMaterial(childStack, this.items.getStackInSlot(10 + i));
               children.add(
                  new ShapeCardData.ShapeCardChild(
                     childStack,
                     ((ShapeCardData)childStack.getOrDefault((DataComponentType)BuilderModule.ITEM_SHAPECARD_DATA.get(), ShapeCardData.DEFAULT)).modifier(),
                     ShapeCardItem.getGhostBlock(childStack)
                  )
               );
            }
         }

         ShapeCardItem.setChildren(output, children);
         if (!ShapeCardItem.getShape(output).isComposition()) {
            ShapeCardItem.setShape(output, Shape.SHAPE_COMPOSITION, true);
         }

         this.setChanged();
      }
   }

   protected void loadAdditional(@Nonnull CompoundTag tag, Provider provider) {
      super.loadAdditional(tag, provider);
      this.items.load(tag, "items", provider);
      ListTag list = tag.getListOrEmpty("ops");

      for (int i = 0; i < Math.min(9, list.size()); i++) {
         CompoundTag tc = list.getCompoundOrEmpty(i);
         ShapeOperation operation = ShapeOperation.getByName(tc.getStringOr("mod_op", ""));
         if (operation == null) {
            operation = ShapeOperation.UNION;
         }

         ShapeRotation rotation = ShapeRotation.getByName(tc.getStringOr("mod_rot", ""));
         if (rotation == null) {
            rotation = ShapeRotation.NONE;
         }

         this.modifiers[i] = new ShapeModifier(operation, tc.getBooleanOr("mod_flipy", false), rotation);
      }

      this.composeDirty = true;
   }

   protected void saveAdditional(@Nonnull CompoundTag tag, Provider provider) {
      super.saveAdditional(tag, provider);
      this.items.save(tag, "items", provider);
      ListTag list = new ListTag();

      for (ShapeModifier modifier : this.modifiers) {
         CompoundTag tc = new CompoundTag();
         tc.putString("mod_op", modifier.getOperation().getCode());
         tc.putBoolean("mod_flipy", modifier.isFlipY());
         tc.putString("mod_rot", modifier.getRotation().getCode());
         list.add(tc);
      }

      tag.put("ops", list);
   }

   protected void applyImplicitComponents(DataComponentGetter input) {
      super.applyImplicitComponents(input);
      this.items.applyImplicitComponents((ItemInventory)input.get((DataComponentType)Registration.ITEM_INVENTORY.get()));
   }

   protected void collectImplicitComponents(Builder builder) {
      super.collectImplicitComponents(builder);
      this.items.collectImplicitComponents(builder);
   }

   public void saveClientDataToNBT(CompoundTag tag, Provider provider) {
      ListTag list = new ListTag();

      for (ShapeModifier modifier : this.modifiers) {
         CompoundTag tc = new CompoundTag();
         tc.putString("mod_op", modifier.getOperation().getCode());
         tc.putBoolean("mod_flipy", modifier.isFlipY());
         tc.putString("mod_rot", modifier.getRotation().getCode());
         list.add(tc);
      }

      tag.put("ops", list);
   }

   public void loadClientDataFromNBT(CompoundTag tag, Provider provider) {
      ListTag list = tag.getListOrEmpty("ops");

      for (int i = 0; i < Math.min(9, list.size()); i++) {
         CompoundTag tc = list.getCompoundOrEmpty(i);
         ShapeOperation operation = ShapeOperation.getByName(tc.getStringOr("mod_op", ""));
         if (operation == null) {
            operation = ShapeOperation.UNION;
         }

         ShapeRotation rotation = ShapeRotation.getByName(tc.getStringOr("mod_rot", ""));
         if (rotation == null) {
            rotation = ShapeRotation.NONE;
         }

         this.modifiers[i] = new ShapeModifier(operation, tc.getBooleanOr("mod_flipy", false), rotation);
      }
   }

   private void applySettings(TypedMap params) {
      for (int i = 0; i < 9; i++) {
         ShapeOperation operation = ShapeOperation.getByName((String)params.get(PARAM_OPS.get(i)));
         if (operation == null) {
            operation = ShapeOperation.UNION;
         }

         ShapeRotation rotation = ShapeRotation.getByName((String)params.get(PARAM_ROTS.get(i)));
         if (rotation == null) {
            rotation = ShapeRotation.NONE;
         }

         this.modifiers[i] = new ShapeModifier(operation, (Boolean)params.get(PARAM_FLIPS.get(i)), rotation);
      }

      this.markComposerDirty();
   }

   public GenericItemHandler getItems() {
      return this.items;
   }
}
