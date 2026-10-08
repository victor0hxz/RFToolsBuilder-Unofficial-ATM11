package mcjty.rftoolsbuilder.modules.shield.blocks;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.builder.InfoLine;
import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.crafting.IComponentsToPreserve;
import mcjty.lib.varia.Logging;
import mcjty.rftoolsbase.modules.various.items.SmartWrenchItem;
import mcjty.rftoolsbase.tools.ManualHelper;
import mcjty.rftoolsbuilder.compat.RFToolsBuilderTOPDriver;
import mcjty.rftoolsbuilder.modules.shield.ShieldModule;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType.BlockEntitySupplier;
import net.minecraft.world.level.block.state.BlockState;

public class ShieldProjectorBlock extends BaseBlock implements IComponentsToPreserve {
   public ShieldProjectorBlock(BlockEntitySupplier<BlockEntity> te, Supplier<Integer> max) {
      super(
         new BlockBuilder()
            .manualEntry(ManualHelper.create("rftoolsbase:shield/shield_intro"))
            .topDriver(RFToolsBuilderTOPDriver.DRIVER)
            .infusable()
            .info(new InfoLine[]{TooltipBuilder.key("message.rftoolsbuilder.shiftmessage")})
            .infoShift(new InfoLine[]{TooltipBuilder.header(), TooltipBuilder.gold(), TooltipBuilder.parameter("info", stack -> Integer.toString(max.get()))})
            .tileEntitySupplier(te)
      );
   }

   public RotationType getRotationType() {
      return RotationType.NONE;
   }

   public Collection<DataComponentType<?>> getComponentsToPreserve() {
      return Collections.singleton((DataComponentType<?>)ShieldModule.ITEM_SHIELD_DATA.get());
   }

   public void setPlacedBy(@Nonnull Level world, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nullable LivingEntity placer, @Nonnull ItemStack stack) {
      super.setPlacedBy(world, pos, state, placer, stack);
      this.setOwner(world, pos, placer);
   }

   public void attack(@Nonnull BlockState state, Level world, @Nonnull BlockPos pos, @Nonnull Player player) {
      if (!world.isClientSide()) {
         this.composeDecomposeShield(world, pos, true);
      }
   }

   protected boolean wrenchUse(Level world, BlockPos pos, Direction side, Player player) {
      this.composeDecomposeShield(world, pos, false);
      return true;
   }

   protected boolean wrenchSneakSelect(Level world, BlockPos pos, Player player) {
      if (!world.isClientSide()) {
         Optional<GlobalPos> currentBlock = SmartWrenchItem.getCurrentBlock(player.getItemInHand(InteractionHand.MAIN_HAND));
         if (!currentBlock.isPresent()) {
            SmartWrenchItem.setCurrentBlock(player.getItemInHand(InteractionHand.MAIN_HAND), GlobalPos.of(world.dimension(), pos));
            Logging.message(player, ChatFormatting.YELLOW + "Selected block");
         } else {
            SmartWrenchItem.setCurrentBlock(player.getItemInHand(InteractionHand.MAIN_HAND), null);
            Logging.message(player, ChatFormatting.YELLOW + "Cleared selected block");
         }
      }

      return true;
   }

   private void composeDecomposeShield(Level world, BlockPos pos, boolean ctrl) {
      if (!world.isClientSide()) {
         BlockEntity te = world.getBlockEntity(pos);
         if (te instanceof ShieldProjectorTileEntity) {
            ((ShieldProjectorTileEntity)te).composeDecomposeShield(ctrl);
         }
      }
   }

   public void onRemove(@Nonnull BlockState state, @Nonnull Level world, @Nonnull BlockPos pos, @Nonnull BlockState newstate, boolean isMoving) {
      if (newstate.getBlock() != this) {
         this.removeShield(world, pos);
      }

      super.onRemove(state, world, pos, newstate, isMoving);
   }

   public void wasExploded(@Nonnull ServerLevel world, @Nonnull BlockPos pos, @Nonnull Explosion explosionIn) {
      this.removeShield(world, pos);
      super.wasExploded(world, pos, explosionIn);
   }

   private void removeShield(LevelAccessor world, BlockPos pos) {
      if (world.getBlockEntity(pos) instanceof ShieldProjectorTileEntity shield && !world.isClientSide() && shield.isShieldComposed()) {
         shield.decomposeShield();
      }
   }
}
