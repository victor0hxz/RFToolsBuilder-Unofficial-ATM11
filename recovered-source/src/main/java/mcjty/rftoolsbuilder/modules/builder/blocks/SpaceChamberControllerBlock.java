package mcjty.rftoolsbuilder.modules.builder.blocks;

import javax.annotation.Nonnull;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.builder.InfoLine;
import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.varia.SoundTools;
import mcjty.rftoolsbase.tools.ManualHelper;
import mcjty.rftoolsbuilder.compat.RFToolsBuilderTOPDriver;
import mcjty.rftoolsbuilder.modules.builder.BuilderModule;
import mcjty.rftoolsbuilder.modules.builder.SpaceChamberRepository;
import mcjty.rftoolsbuilder.modules.builder.data.ChamberControllerData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class SpaceChamberControllerBlock extends BaseBlock {
   public SpaceChamberControllerBlock() {
      super(
         new BlockBuilder()
            .properties(Properties.of().strength(2.0F).sound(SoundType.METAL).noOcclusion())
            .topDriver(RFToolsBuilderTOPDriver.DRIVER)
            .tileEntitySupplier(SpaceChamberControllerTileEntity::new)
            .manualEntry(ManualHelper.create("rftoolsbuilder:builder/space_chambers"))
            .info(new InfoLine[]{TooltipBuilder.key("message.rftoolsbuilder.shiftmessage")})
            .infoShift(new InfoLine[]{TooltipBuilder.header(), TooltipBuilder.parameter("channel", SpaceChamberControllerBlock::getChannelDescription)})
      );
   }

   private static String getChannelDescription(ItemStack stack) {
      ChamberControllerData data = (ChamberControllerData)stack.getOrDefault(BuilderModule.ITEM_CHAMBER_DATA, ChamberControllerData.DEFAULT);
      return data.channel() == -1 ? "Channel is not set!" : "Channel: " + data.channel();
   }

   public RotationType getRotationType() {
      return RotationType.NONE;
   }

   protected boolean wrenchUse(Level level, BlockPos pos, Direction side, Player player) {
      if (level.isClientSide()) {
         SoundEvent pling = SoundTools.findSound(Identifier.fromNamespaceAndPath("minecraft", "block.note_block.bell"));
         level.playSound(player, pos, pling, SoundSource.BLOCKS, 1.0F, 1.0F);
      } else {
         SpaceChamberControllerTileEntity chamberControllerTileEntity = (SpaceChamberControllerTileEntity)level.getBlockEntity(pos);
         chamberControllerTileEntity.createChamber(player);
      }

      return true;
   }

   public void onPlace(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState state2, boolean p_220082_5_) {
      super.onPlace(state, level, pos, state2, p_220082_5_);
      if (!level.isClientSide()) {
         SpaceChamberRepository chamberRepository = SpaceChamberRepository.get(level);
         SpaceChamberControllerTileEntity te = (SpaceChamberControllerTileEntity)level.getBlockEntity(pos);
         if (te.getChannel() == -1) {
            int id = chamberRepository.newChannel();
            te.setChannel(id);
            chamberRepository.save();
         }
      }
   }

   public void onRemove(@Nonnull BlockState state, @Nonnull Level world, @Nonnull BlockPos pos, @Nonnull BlockState newstate, boolean isMoving) {
      if (!world.isClientSide()) {
         SpaceChamberRepository chamberRepository = SpaceChamberRepository.get(world);
         SpaceChamberControllerTileEntity te = (SpaceChamberControllerTileEntity)world.getBlockEntity(pos);
         if (te.getChannel() != -1) {
            chamberRepository.deleteChannel(te.getChannel());
            chamberRepository.save();
         }
      }

      super.onRemove(state, world, pos, newstate, isMoving);
   }
}
