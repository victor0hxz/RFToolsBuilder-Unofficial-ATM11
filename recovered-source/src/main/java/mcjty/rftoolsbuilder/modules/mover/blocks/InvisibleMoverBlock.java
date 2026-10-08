package mcjty.rftoolsbuilder.modules.mover.blocks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import mcjty.lib.setup.RegistrationContext;
import mcjty.lib.varia.SafeClientTools;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class InvisibleMoverBlock extends Block implements EntityBlock {
   private final Map<BlockPos, InvisibleMoverBlock.MD> dataByControl = new HashMap<>();
   private final Map<BlockPos, List<InvisibleMoverBlock.MD>> dataByMover = new HashMap<>();

   public InvisibleMoverBlock() {
      super(RegistrationContext.prepareBlockProperties(Properties.of().noLootTable().strength(-1.0F, 3600000.0F).noOcclusion().randomTicks()));
   }

   @Nullable
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new InvisibleMoverBE(pos, state);
   }

   public RenderShape getRenderShape(BlockState state) {
      return RenderShape.INVISIBLE;
   }

   public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      if (level.getBlockEntity(pos) instanceof InvisibleMoverBE invisibleMover) {
         BlockState originalState = invisibleMover.getOriginalState();
         if (originalState != null && !originalState.isAir()) {
            return originalState.getShape(level, pos, context);
         }
      }

      return super.getShape(state, level, pos, context);
   }

   public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
      if (random.nextFloat() < 0.1F) {
         level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
      }
   }

   public void registerData(BlockPos moverPos, BlockPos controlPos, Direction horizDirection, Direction direction) {
      InvisibleMoverBlock.MD data = new InvisibleMoverBlock.MD(moverPos, controlPos, horizDirection, direction);
      this.dataByControl.put(controlPos, data);
      this.dataByMover.computeIfAbsent(moverPos, p -> new ArrayList<>()).add(data);
   }

   public void removeData(BlockPos moverPos) {
      List<InvisibleMoverBlock.MD> set = this.dataByMover.get(moverPos);
      if (set != null) {
         for (InvisibleMoverBlock.MD data : set) {
            this.dataByControl.remove(data.controlPos);
         }

         this.dataByMover.put(moverPos, new ArrayList<>());
      }
   }

   public List<InvisibleMoverBlock.MD> getData(BlockPos moverPos) {
      return this.dataByMover.get(moverPos);
   }

   protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult result) {
      this.activate(level, pos);
      return InteractionResult.SUCCESS;
   }

   public void attack(@Nonnull BlockState state, Level level, @Nonnull BlockPos pos, @Nonnull Player player) {
      this.activate(level, pos);
   }

   private void activate(Level level, BlockPos pos) {
      if (level.isClientSide()) {
         InvisibleMoverBlock.MD data = this.dataByControl.get(pos);
         if (data != null) {
            HitResult mouseOver = SafeClientTools.getClientMouseOver();
            if (mouseOver instanceof BlockHitResult blockResult && level.getBlockEntity(data.mover) instanceof MoverTileEntity mover) {
               mover.hitScreenClient(
                  blockResult.getBlockPos(),
                  mouseOver.getLocation().x - pos.getX(),
                  mouseOver.getLocation().y - pos.getY(),
                  mouseOver.getLocation().z - pos.getZ(),
                  blockResult.getDirection(),
                  data.horizDirection,
                  data.direction
               );
            }
         }
      }
   }

   public record MD(BlockPos mover, BlockPos controlPos, Direction horizDirection, Direction direction) {
   }
}
