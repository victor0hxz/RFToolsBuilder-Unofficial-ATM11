package mcjty.rftoolsbuilder.modules.shield.blocks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mcjty.lib.setup.RegistrationContext;
import mcjty.rftoolsbuilder.modules.shield.ShieldRenderingMode;
import mcjty.rftoolsbuilder.modules.shield.filters.PlayerFilter;
import mcjty.rftoolsbuilder.modules.shield.filters.ShieldFilter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ShieldingBlock extends Block implements EntityBlock {
   public static final BooleanProperty BLOCKED_ITEMS = BooleanProperty.create("bi");
   public static final BooleanProperty BLOCKED_PASSIVE = BooleanProperty.create("bp");
   public static final BooleanProperty BLOCKED_HOSTILE = BooleanProperty.create("bh");
   public static final BooleanProperty BLOCKED_PLAYERS = BooleanProperty.create("bplay");
   public static final BooleanProperty DAMAGE_ITEMS = BooleanProperty.create("di");
   public static final BooleanProperty DAMAGE_PASSIVE = BooleanProperty.create("dp");
   public static final BooleanProperty DAMAGE_HOSTILE = BooleanProperty.create("dh");
   public static final BooleanProperty DAMAGE_PLAYERS = BooleanProperty.create("dplay");
   public static final BooleanProperty FLAG_OPAQUE = BooleanProperty.create("opaque");
   public static final EnumProperty<ShieldRenderingMode> RENDER_MODE = EnumProperty.create("render", ShieldRenderingMode.class);
   public static final VoxelShape COLLISION_SHAPE = Shapes.box(0.002, 0.002, 0.002, 0.998, 0.998, 0.998);

   public ShieldingBlock() {
      super(
         RegistrationContext.prepareBlockProperties(
            Properties.of()
               .forceSolidOn()
               .sound(SoundType.GLASS)
               .noOcclusion()
               .isRedstoneConductor((state, world, pos) -> false)
               .pushReaction(PushReaction.BLOCK)
               .strength(-1.0F, 3600000.0F)
               .noLootTable()
         )
      );
      this.registerDefaultState(
         (BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.defaultBlockState()
                                    .setValue(BLOCKED_ITEMS, false))
                                 .setValue(BLOCKED_PASSIVE, false))
                              .setValue(BLOCKED_HOSTILE, false))
                           .setValue(BLOCKED_PLAYERS, false))
                        .setValue(DAMAGE_ITEMS, false))
                     .setValue(DAMAGE_PASSIVE, false))
                  .setValue(DAMAGE_HOSTILE, false))
               .setValue(DAMAGE_PLAYERS, false))
            .setValue(FLAG_OPAQUE, true)
      );
   }

   @Nullable
   public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
      return new ShieldingTileEntity(pPos, pState);
   }

   protected void createBlockStateDefinition(@Nonnull Builder<Block, BlockState> builder) {
      super.createBlockStateDefinition(builder);
      builder.add(
         new Property[]{
            BLOCKED_ITEMS,
            BLOCKED_HOSTILE,
            BLOCKED_PASSIVE,
            BLOCKED_PLAYERS,
            DAMAGE_ITEMS,
            DAMAGE_HOSTILE,
            DAMAGE_PASSIVE,
            DAMAGE_PLAYERS,
            FLAG_OPAQUE,
            RENDER_MODE
         }
      );
   }

   public int getLightBlock(BlockState state, @Nonnull BlockGetter world, @Nonnull BlockPos pos) {
      return state.getValue(FLAG_OPAQUE) ? 0 : 255;
   }

   @Nonnull
   public RenderShape getRenderShape(BlockState state) {
      return state.getValue(RENDER_MODE) == ShieldRenderingMode.INVISIBLE ? RenderShape.INVISIBLE : RenderShape.MODEL;
   }

   public float getShadeBrightness(@Nonnull BlockState state, @Nonnull BlockGetter worldIn, @Nonnull BlockPos pos) {
      return super.getShadeBrightness(state, worldIn, pos);
   }

   public boolean canEntityDestroy(BlockState state, BlockGetter world, BlockPos pos, Entity entity) {
      return false;
   }

   @Nonnull
   public VoxelShape getShape(@Nonnull BlockState state, BlockGetter world, @Nonnull BlockPos pos, @Nonnull CollisionContext context) {
      BlockEntity te = world.getBlockEntity(pos);
      if (te instanceof ShieldingTileEntity) {
         BlockState mimic = ((ShieldingTileEntity)te).getMimic();
         if (mimic != null) {
            return mimic.getShape(world, pos, context);
         }
      }

      return state.getValue(RENDER_MODE) == ShieldRenderingMode.INVISIBLE ? Shapes.empty() : super.getShape(state, world, pos, context);
   }

   @Nonnull
   public VoxelShape getInteractionShape(@Nonnull BlockState state, BlockGetter world, @Nonnull BlockPos pos) {
      BlockEntity te = world.getBlockEntity(pos);
      if (te instanceof ShieldingTileEntity) {
         BlockState mimic = ((ShieldingTileEntity)te).getMimic();
         if (mimic != null) {
            return mimic.getVisualShape(world, pos, CollisionContext.empty());
         }
      }

      return state.getValue(RENDER_MODE) == ShieldRenderingMode.INVISIBLE ? Shapes.empty() : super.getInteractionShape(state, world, pos);
   }

   @Nonnull
   public VoxelShape getOcclusionShape(@Nonnull BlockState state) {
      return super.getOcclusionShape(state);
   }

   public static boolean isHostile(Entity entity) {
      return entity instanceof Enemy;
   }

   public static boolean isPassive(Entity entity) {
      if (entity instanceof Enemy) {
         return false;
      } else {
         return entity instanceof Player ? false : entity instanceof Mob;
      }
   }

   public static boolean isItem(Entity entity) {
      return !(entity instanceof LivingEntity);
   }

   @Nonnull
   public VoxelShape getCollisionShape(BlockState state, @Nonnull BlockGetter world, @Nonnull BlockPos pos, CollisionContext context) {
      if (context instanceof EntityCollisionContext ctxt) {
         Entity entity = ctxt.getEntity();
         if ((Boolean)state.getValue(BLOCKED_HOSTILE) && isHostile(entity)) {
            if (this.checkEntityCD(world, pos, "hostile")) {
               return COLLISION_SHAPE;
            }

            return Shapes.empty();
         }

         if ((Boolean)state.getValue(BLOCKED_PASSIVE) && isPassive(entity)) {
            if (this.checkEntityCD(world, pos, "animal")) {
               return COLLISION_SHAPE;
            }

            return Shapes.empty();
         }

         if ((Boolean)state.getValue(BLOCKED_PLAYERS) && entity instanceof Player) {
            if (this.checkPlayerCD(world, pos, (Player)entity)) {
               return COLLISION_SHAPE;
            }

            return Shapes.empty();
         }

         if ((Boolean)state.getValue(BLOCKED_ITEMS) && isItem(entity)) {
            if (this.checkEntityCD(world, pos, "item")) {
               return COLLISION_SHAPE;
            }

            return Shapes.empty();
         }
      }

      return Shapes.empty();
   }

   private boolean checkEntityCD(BlockGetter world, BlockPos pos, String filterName) {
      ShieldProjectorTileEntity projector = this.getShieldProjector(world, pos);
      if (projector != null) {
         for (ShieldFilter<?> filter : projector.getFilters()) {
            if ("default".equals(filter.getFilterName())) {
               return (filter.getAction() & 1) != 0;
            }

            if (filterName.equals(filter.getFilterName())) {
               return (filter.getAction() & 1) != 0;
            }
         }
      }

      return false;
   }

   private boolean checkPlayerCD(BlockGetter world, BlockPos pos, Player entity) {
      ShieldProjectorTileEntity projector = this.getShieldProjector(world, pos);
      if (projector != null) {
         for (ShieldFilter<?> filter : projector.getFilters()) {
            if ("default".equals(filter.getFilterName())) {
               return (filter.getAction() & 1) != 0;
            }

            if ("player".equals(filter.getFilterName())) {
               PlayerFilter playerFilter = (PlayerFilter)filter;
               String name = playerFilter.getName();
               if (name == null || name.isEmpty()) {
                  return (filter.getAction() & 1) != 0;
               }

               if (name.equals(entity.getName().getString())) {
                  return (filter.getAction() & 1) != 0;
               }
            }
         }
      }

      return false;
   }

   public boolean skipRendering(@Nonnull BlockState state, BlockState adjacentBlockState, @Nonnull Direction side) {
      return adjacentBlockState.getBlock() == this ? true : super.skipRendering(state, adjacentBlockState, side);
   }

   protected void entityInside(
      @Nonnull BlockState state,
      @Nonnull Level world,
      @Nonnull BlockPos pos,
      @Nonnull Entity entity,
      @Nonnull InsideBlockEffectApplier effectApplier,
      boolean isPrecise
   ) {
      if (!(entity instanceof LivingEntity) && !(Boolean)state.getValue(BLOCKED_ITEMS)) {
         entity.setPos(entity.getX(), entity.getY() - 1.0, entity.getZ());
      }

      this.handleDamage(state, world, pos, entity);
   }

   @Nullable
   private ShieldProjectorTileEntity getShieldProjector(BlockGetter world, BlockPos shieldingPos) {
      BlockEntity te = world.getBlockEntity(shieldingPos);
      if (te instanceof ShieldingTileEntity) {
         BlockPos projectorPos = ((ShieldingTileEntity)te).getShieldProjector();
         if (projectorPos != null) {
            BlockEntity tileEntity = world.getBlockEntity(projectorPos);
            if (tileEntity instanceof ShieldProjectorTileEntity) {
               return (ShieldProjectorTileEntity)tileEntity;
            }
         }
      }

      return null;
   }

   public void handleDamage(BlockState state, Level world, BlockPos pos, Entity entity) {
      Boolean dmgHostile = (Boolean)state.getValue(DAMAGE_HOSTILE);
      Boolean dmgPassive = (Boolean)state.getValue(DAMAGE_PASSIVE);
      Boolean dmgPlayer = (Boolean)state.getValue(DAMAGE_PLAYERS);
      Boolean dmgItems = (Boolean)state.getValue(DAMAGE_ITEMS);
      if ((dmgHostile || dmgPassive || dmgPlayer || dmgItems) && !world.isClientSide() && world.getGameTime() % 10L == 0L) {
         int xCoord = pos.getX();
         int yCoord = pos.getY();
         int zCoord = pos.getZ();
         AABB beamBox = new AABB(xCoord - 0.4, yCoord - 0.4, zCoord - 0.4, xCoord + 1.4, yCoord + 2.0, zCoord + 1.4);
         if (entity.getBoundingBox().intersects(beamBox)) {
            ShieldProjectorTileEntity projector = this.getShieldProjector(world, pos);
            if (projector != null) {
               if (dmgItems && entity instanceof ItemEntity) {
                  if (this.checkEntityDamage(projector, "item")) {
                     projector.applyDamageToEntity(entity);
                  }
               } else if (dmgHostile && isHostile(entity)) {
                  if (this.checkEntityDamage(projector, "hostile")) {
                     projector.applyDamageToEntity(entity);
                  }
               } else if (dmgPassive && isPassive(entity)) {
                  if (this.checkEntityDamage(projector, "animal")) {
                     projector.applyDamageToEntity(entity);
                  }
               } else if (dmgPlayer && entity instanceof Player && this.checkPlayerDamage(projector, (Player)entity)) {
                  projector.applyDamageToEntity(entity);
               }
            }
         }
      }
   }

   private boolean checkEntityDamage(@Nonnull ShieldProjectorTileEntity shieldTileEntity, String filterName) {
      for (ShieldFilter<?> filter : shieldTileEntity.getFilters()) {
         if ("default".equals(filter.getFilterName())) {
            return (filter.getAction() & 2) != 0;
         }

         if (filterName.equals(filter.getFilterName())) {
            return (filter.getAction() & 2) != 0;
         }
      }

      return false;
   }

   private boolean checkPlayerDamage(@Nonnull ShieldProjectorTileEntity shieldTileEntity, Player entity) {
      for (ShieldFilter<?> filter : shieldTileEntity.getFilters()) {
         if ("default".equals(filter.getFilterName())) {
            return (filter.getAction() & 2) != 0;
         }

         if ("player".equals(filter.getFilterName())) {
            PlayerFilter playerFilter = (PlayerFilter)filter;
            String name = playerFilter.getName();
            if (name == null || name.isEmpty()) {
               return (filter.getAction() & 2) != 0;
            }

            if (name.equals(entity.getName().getString())) {
               return (filter.getAction() & 2) != 0;
            }
         }
      }

      return false;
   }
}
