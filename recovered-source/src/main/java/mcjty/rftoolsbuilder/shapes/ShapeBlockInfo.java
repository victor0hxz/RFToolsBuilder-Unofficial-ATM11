package mcjty.rftoolsbuilder.shapes;

import com.mojang.blaze3d.vertex.BufferBuilder;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import mcjty.lib.varia.TagTools;
import mcjty.rftoolsbuilder.modules.builder.BuilderModule;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.material.MapColor;

public class ShapeBlockInfo {
   private static final ShapeBlockInfo.Col COL_DEFAULT = new ShapeBlockInfo.Col(0.5F, 0.3F, 0.5F);
   private static final ShapeBlockInfo.Col COL_LAVA = new ShapeBlockInfo.Col(0.83137256F, 0.3529412F, 0.07058824F);
   private static final ShapeBlockInfo.Col COL_NETHERBRICK = new ShapeBlockInfo.Col(0.1764706F, 0.09019608F, 0.105882354F);
   private static final ShapeBlockInfo.Col COL_SCANNER = new ShapeBlockInfo.Col(0.0F, 0.0F, 0.8862745F);
   private static final ShapeBlockInfo.IBlockRender BD_RAIL = new ShapeBlockInfo.DefaultRender(0.1F, 0.2F);
   private static final ShapeBlockInfo.IBlockRender BD_GRASS = new ShapeBlockInfo.DefaultRender(0.1F, 0.2F);
   private static final ShapeBlockInfo.IBlockRender BD_TORCH = new ShapeBlockInfo.DefaultRender(0.4F, 0.7F);
   private static final ShapeBlockInfo.IBlockRender BD_FLOWER = new ShapeBlockInfo.DefaultRender(0.4F, 0.6F);
   private static final ShapeBlockInfo.IBlockRender BD_MUSHROOM = new ShapeBlockInfo.DefaultRender(0.3F, 0.5F);
   private static final ShapeBlockInfo.IBlockRender BD_BARS = new ShapeBlockInfo.DefaultRender(0.4F, 1.0F);
   private static final ShapeBlockInfo.IBlockRender BD_VINE = new ShapeBlockInfo.DefaultRender(0.4F, 1.0F);
   private static final ShapeBlockInfo.IBlockRender BD_WALL = new ShapeBlockInfo.DefaultRender(0.25F, 0.9F);
   private static final ShapeBlockInfo.IBlockRender BD_FENCE = new ShapeBlockInfo.DefaultRender(0.3F, 0.9F);
   private static final ShapeBlockInfo.IBlockRender BD_SLAB = new ShapeBlockInfo.DefaultRender(0.05F, 0.5F);
   private static final ShapeBlockInfo.IBlockRender BD_SLAB_UPPER = new ShapeBlockInfo.UpperslabRender(0.05F, 0.5F);
   private static final ShapeBlockInfo.IBlockRender BD_SNOWLAYER = new ShapeBlockInfo.DefaultRender(0.0F, 0.3F);
   private static final ShapeBlockInfo.IBlockRender BD_FIRE = new ShapeBlockInfo.DefaultRender(0.1F, 0.3F);
   private static final ShapeBlockInfo.IBlockRender BD_REDSTONE = new ShapeBlockInfo.DefaultRender(0.3F, 0.1F);
   private static final ShapeBlockInfo.IBlockRender BD_CHEST = new ShapeBlockInfo.DefaultRender(0.05F, 0.8F);
   private static final ShapeBlockInfo.IBlockRender BD_TRAPDOOR = new ShapeBlockInfo.DefaultRender(0.05F, 0.1F);
   private static final ShapeBlockInfo.IBlockRender BD_BUTTON = new ShapeBlockInfo.DefaultRender(0.4F, 0.1F);
   private final ShapeBlockInfo.Col col;
   private final ShapeBlockInfo.IBlockRender render;
   private static final Set<Block> nonSolidBlocks = new HashSet<>();

   public static boolean isNonSolidBlock(Block b) {
      return nonSolidBlocks.contains(b);
   }

   public ShapeBlockInfo(ShapeBlockInfo.Col col, ShapeBlockInfo.IBlockRender render) {
      this.col = col;
      this.render = render;
   }

   private static ShapeBlockInfo.IBlockRender getBlockRender(BlockState state) {
      if (state == null) {
         return null;
      } else {
         ShapeBlockInfo.IBlockRender render = null;
         Block block = state.getBlock();
         Collection<TagKey<Block>> tags = TagTools.getTags(block);
         if (block == Blocks.TORCH || block == Blocks.REDSTONE_TORCH) {
            render = BD_TORCH;
         } else if (tags.contains(BlockTags.SLABS)) {
            if (state.hasProperty(SlabBlock.TYPE) && state.getValue(SlabBlock.TYPE) == SlabType.BOTTOM) {
               render = BD_SLAB;
            } else {
               render = BD_SLAB_UPPER;
            }
         } else if (block == Blocks.SNOW) {
            render = BD_SNOWLAYER;
         } else if (tags.contains(BlockTags.WALLS)) {
            render = BD_WALL;
         } else if (block == Blocks.IRON_BARS || block == Blocks.LADDER) {
            render = BD_BARS;
         } else if (block == Blocks.VINE) {
            render = BD_VINE;
         } else if (tags.contains(BlockTags.SMALL_FLOWERS)
            || tags.contains(BlockTags.CORAL_PLANTS)
            || block == Blocks.WHEAT
            || block == Blocks.CARROTS
            || block == Blocks.POTATOES
            || block == Blocks.BEETROOTS) {
            render = BD_FLOWER;
         } else if (block == Blocks.TALL_GRASS) {
            render = BD_GRASS;
         } else if (tags.contains(BlockTags.RAILS)) {
            render = BD_RAIL;
         } else if (block == Blocks.RED_MUSHROOM || block == Blocks.BROWN_MUSHROOM) {
            render = BD_MUSHROOM;
         } else if (block == Blocks.FIRE) {
            render = BD_FIRE;
         } else if (block == Blocks.REDSTONE_WIRE) {
            render = BD_REDSTONE;
         } else if (tags.contains(net.neoforged.neoforge.common.Tags.Blocks.CHESTS)) {
            render = BD_CHEST;
         } else if (tags.contains(BlockTags.TRAPDOORS)
            || tags.contains(BlockTags.WOODEN_PRESSURE_PLATES)
            || block == Blocks.OAK_PRESSURE_PLATE
            || block == Blocks.STONE_PRESSURE_PLATE) {
            render = BD_TRAPDOOR;
         } else if (block == Blocks.LEVER || tags.contains(BlockTags.BUTTONS) || tags.contains(BlockTags.WOODEN_BUTTONS)) {
            render = BD_BUTTON;
         } else if (tags.contains(BlockTags.FENCES) || tags.contains(BlockTags.WOODEN_FENCES)) {
            render = BD_FENCE;
         }

         return render;
      }
   }

   private static ShapeBlockInfo.Col getColor(BlockState state) {
      if (state == null) {
         return COL_DEFAULT;
      } else {
         Block block = state.getBlock();
         MapColor mapColor = null;

         try {
            mapColor = state.getMapColor(Minecraft.getInstance().level, new BlockPos(0, 0, 0));
         } catch (Exception var7) {
            mapColor = MapColor.COLOR_RED;
         }

         ShapeBlockInfo.Col col;
         if (block == Blocks.LAVA) {
            col = COL_LAVA;
         } else if (block == Blocks.NETHER_BRICKS || block == Blocks.NETHER_BRICK_FENCE || block == Blocks.NETHER_BRICK_STAIRS) {
            col = COL_NETHERBRICK;
         } else if (block == BuilderModule.SUPPORT.get()) {
            col = COL_DEFAULT;
         } else {
            col = new ShapeBlockInfo.Col((mapColor.col >> 16 & 0xFF) / 255.0F, (mapColor.col >> 8 & 0xFF) / 255.0F, (mapColor.col & 0xFF) / 255.0F);
         }

         float r = col.getR();
         float g = col.getG();
         float b = col.getB();
         if (r * 1.2F > 1.0F) {
            r = 0.825F;
         }

         if (g * 1.2F > 1.0F) {
            g = 0.825F;
         }

         if (b * 1.2F > 1.0F) {
            b = 0.825F;
         }

         return new ShapeBlockInfo.Col(r, g, b);
      }
   }

   @Nonnull
   public static ShapeBlockInfo getBlockInfo(Map<BlockState, ShapeBlockInfo> palette, BlockState state) {
      ShapeBlockInfo info = palette.get(state);
      if (info != null) {
         return info;
      } else {
         info = new ShapeBlockInfo(getColor(state), getBlockRender(state));
         palette.put(state, info);
         return info;
      }
   }

   public ShapeBlockInfo.Col getCol() {
      return this.col;
   }

   public ShapeBlockInfo.IBlockRender getRender() {
      return this.render;
   }

   public boolean isNonSolid() {
      return this.render != null;
   }

   static class BlockRender implements ShapeBlockInfo.IBlockRender {
      private final float height = 1.0F;
      private final float xoffset;
      private final float zoffset;

      public BlockRender(float xoffset, float zoffset, float height) {
         this.xoffset = xoffset;
         this.zoffset = zoffset;
      }

      @Override
      public void render(BufferBuilder buffer, int z, float r, float g, float b) {
         float a = 0.5F;
         buffer.addVertex(this.xoffset, this.height, 1.0F - this.zoffset + z).setColor(r * 0.8F, g * 0.8F, b * 0.8F, a);
         buffer.addVertex(1.0F - this.xoffset, this.height, 1.0F - this.zoffset + z).setColor(r * 0.8F, g * 0.8F, b * 0.8F, a);
         buffer.addVertex(1.0F - this.xoffset, this.height, this.zoffset + z).setColor(r * 0.8F, g * 0.8F, b * 0.8F, a);
         buffer.addVertex(this.xoffset, this.height, this.zoffset + z).setColor(r * 0.8F, g * 0.8F, b * 0.8F, a);
         buffer.addVertex(this.xoffset, 0.0F, this.zoffset + z).setColor(r * 0.8F, g * 0.8F, b * 0.8F, a);
         buffer.addVertex(1.0F - this.xoffset, 0.0F, this.zoffset + z).setColor(r * 0.8F, g * 0.8F, b * 0.8F, a);
         buffer.addVertex(1.0F - this.xoffset, 0.0F, 1.0F - this.zoffset + z).setColor(r * 0.8F, g * 0.8F, b * 0.8F, a);
         buffer.addVertex(this.xoffset, 0.0F, 1.0F - this.zoffset + z).setColor(r * 0.8F, g * 0.8F, b * 0.8F, a);
         buffer.addVertex(1.0F - this.xoffset, this.height, this.zoffset + z).setColor(r * 1.2F, g * 1.2F, b * 1.2F, a);
         buffer.addVertex(1.0F - this.xoffset, 0.0F, this.zoffset + z).setColor(r * 1.2F, g * 1.2F, b * 1.2F, a);
         buffer.addVertex(this.xoffset, 0.0F, this.zoffset + z).setColor(r * 1.2F, g * 1.2F, b * 1.2F, a);
         buffer.addVertex(this.xoffset, this.height, this.zoffset + z).setColor(r * 1.2F, g * 1.2F, b * 1.2F, a);
         buffer.addVertex(1.0F - this.xoffset, 0.0F, 1.0F - this.zoffset + z).setColor(r * 1.2F, g * 1.2F, b * 1.2F, a);
         buffer.addVertex(1.0F - this.xoffset, this.height, 1.0F - this.zoffset + z).setColor(r * 1.2F, g * 1.2F, b * 1.2F, a);
         buffer.addVertex(this.xoffset, this.height, 1.0F - this.zoffset + z).setColor(r * 1.2F, g * 1.2F, b * 1.2F, a);
         buffer.addVertex(this.xoffset, 0.0F, 1.0F - this.zoffset + z).setColor(r * 1.2F, g * 1.2F, b * 1.2F, a);
         buffer.addVertex(this.xoffset, 0.0F, 1.0F - this.zoffset + z).setColor(r, g, b, a);
         buffer.addVertex(this.xoffset, this.height, 1.0F - this.zoffset + z).setColor(r, g, b, a);
         buffer.addVertex(this.xoffset, this.height, this.zoffset + z).setColor(r, g, b, a);
         buffer.addVertex(this.xoffset, 0.0F, this.zoffset + z).setColor(r, g, b, a);
         buffer.addVertex(1.0F - this.xoffset, 0.0F, this.zoffset + z).setColor(r, g, b, a);
         buffer.addVertex(1.0F - this.xoffset, this.height, this.zoffset + z).setColor(r, g, b, a);
         buffer.addVertex(1.0F - this.xoffset, this.height, 1.0F - this.zoffset + z).setColor(r, g, b, a);
         buffer.addVertex(1.0F - this.xoffset, 0.0F, 1.0F - this.zoffset + z).setColor(r, g, b, a);
      }
   }

   static class Col {
      private final float r;
      private final float g;
      private final float b;

      public Col(float r, float g, float b) {
         this.r = r;
         this.g = g;
         this.b = b;
      }

      public float getR() {
         return this.r;
      }

      public float getG() {
         return this.g;
      }

      public float getB() {
         return this.b;
      }
   }

   static class DefaultRender implements ShapeBlockInfo.IBlockRender {
      private final float height;
      private final float offset;

      public DefaultRender(float offset, float height) {
         this.height = height;
         this.offset = offset;
      }

      @Override
      public void render(BufferBuilder buffer, int z, float r, float g, float b) {
         float a = 0.5F;
         buffer.addVertex(this.offset, this.height, 1.0F - this.offset + z).setColor(r * 0.8F, g * 0.8F, b * 0.8F, a);
         buffer.addVertex(1.0F - this.offset, this.height, 1.0F - this.offset + z).setColor(r * 0.8F, g * 0.8F, b * 0.8F, a);
         buffer.addVertex(1.0F - this.offset, this.height, this.offset + z).setColor(r * 0.8F, g * 0.8F, b * 0.8F, a);
         buffer.addVertex(this.offset, this.height, this.offset + z).setColor(r * 0.8F, g * 0.8F, b * 0.8F, a);
         buffer.addVertex(this.offset, 0.0F, this.offset + z).setColor(r * 0.8F, g * 0.8F, b * 0.8F, a);
         buffer.addVertex(1.0F - this.offset, 0.0F, this.offset + z).setColor(r * 0.8F, g * 0.8F, b * 0.8F, a);
         buffer.addVertex(1.0F - this.offset, 0.0F, 1.0F - this.offset + z).setColor(r * 0.8F, g * 0.8F, b * 0.8F, a);
         buffer.addVertex(this.offset, 0.0F, 1.0F - this.offset + z).setColor(r * 0.8F, g * 0.8F, b * 0.8F, a);
         buffer.addVertex(1.0F - this.offset, this.height, this.offset + z).setColor(r * 1.2F, g * 1.2F, b * 1.2F, a);
         buffer.addVertex(1.0F - this.offset, 0.0F, this.offset + z).setColor(r * 1.2F, g * 1.2F, b * 1.2F, a);
         buffer.addVertex(this.offset, 0.0F, this.offset + z).setColor(r * 1.2F, g * 1.2F, b * 1.2F, a);
         buffer.addVertex(this.offset, this.height, this.offset + z).setColor(r * 1.2F, g * 1.2F, b * 1.2F, a);
         buffer.addVertex(1.0F - this.offset, 0.0F, 1.0F - this.offset + z).setColor(r * 1.2F, g * 1.2F, b * 1.2F, a);
         buffer.addVertex(1.0F - this.offset, this.height, 1.0F - this.offset + z).setColor(r * 1.2F, g * 1.2F, b * 1.2F, a);
         buffer.addVertex(this.offset, this.height, 1.0F - this.offset + z).setColor(r * 1.2F, g * 1.2F, b * 1.2F, a);
         buffer.addVertex(this.offset, 0.0F, 1.0F - this.offset + z).setColor(r * 1.2F, g * 1.2F, b * 1.2F, a);
         buffer.addVertex(this.offset, 0.0F, 1.0F - this.offset + z).setColor(r, g, b, a);
         buffer.addVertex(this.offset, this.height, 1.0F - this.offset + z).setColor(r, g, b, a);
         buffer.addVertex(this.offset, this.height, this.offset + z).setColor(r, g, b, a);
         buffer.addVertex(this.offset, 0.0F, this.offset + z).setColor(r, g, b, a);
         buffer.addVertex(1.0F - this.offset, 0.0F, this.offset + z).setColor(r, g, b, a);
         buffer.addVertex(1.0F - this.offset, this.height, this.offset + z).setColor(r, g, b, a);
         buffer.addVertex(1.0F - this.offset, this.height, 1.0F - this.offset + z).setColor(r, g, b, a);
         buffer.addVertex(1.0F - this.offset, 0.0F, 1.0F - this.offset + z).setColor(r, g, b, a);
      }
   }

   interface IBlockRender {
      void render(BufferBuilder var1, int var2, float var3, float var4, float var5);
   }

   static class UpperslabRender implements ShapeBlockInfo.IBlockRender {
      private final float height;
      private final float offset;

      public UpperslabRender(float offset, float height) {
         this.height = height;
         this.offset = offset;
      }

      @Override
      public void render(BufferBuilder buffer, int z, float r, float g, float b) {
         float a = 0.5F;
         buffer.addVertex(this.offset, this.height + 0.5F, 1.0F - this.offset + z).setColor(r * 0.8F, g * 0.8F, b * 0.8F, a);
         buffer.addVertex(1.0F - this.offset, this.height + 0.5F, 1.0F - this.offset + z).setColor(r * 0.8F, g * 0.8F, b * 0.8F, a);
         buffer.addVertex(1.0F - this.offset, this.height + 0.5F, this.offset + z).setColor(r * 0.8F, g * 0.8F, b * 0.8F, a);
         buffer.addVertex(this.offset, this.height + 0.5F, this.offset + z).setColor(r * 0.8F, g * 0.8F, b * 0.8F, a);
         buffer.addVertex(this.offset, 0.5F, this.offset + z).setColor(r * 0.8F, g * 0.8F, b * 0.8F, a);
         buffer.addVertex(1.0F - this.offset, 0.5F, this.offset + z).setColor(r * 0.8F, g * 0.8F, b * 0.8F, a);
         buffer.addVertex(1.0F - this.offset, 0.5F, 1.0F - this.offset + z).setColor(r * 0.8F, g * 0.8F, b * 0.8F, a);
         buffer.addVertex(this.offset, 0.5F, 1.0F - this.offset + z).setColor(r * 0.8F, g * 0.8F, b * 0.8F, a);
         buffer.addVertex(1.0F - this.offset, this.height + 0.5F, this.offset + z).setColor(r * 1.2F, g * 1.2F, b * 1.2F, a);
         buffer.addVertex(1.0F - this.offset, 0.5F, this.offset + z).setColor(r * 1.2F, g * 1.2F, b * 1.2F, a);
         buffer.addVertex(this.offset, 0.5F, this.offset + z).setColor(r * 1.2F, g * 1.2F, b * 1.2F, a);
         buffer.addVertex(this.offset, this.height + 0.5F, this.offset + z).setColor(r * 1.2F, g * 1.2F, b * 1.2F, a);
         buffer.addVertex(1.0F - this.offset, 0.5F, 1.0F - this.offset + z).setColor(r * 1.2F, g * 1.2F, b * 1.2F, a);
         buffer.addVertex(1.0F - this.offset, this.height + 0.5F, 1.0F - this.offset + z).setColor(r * 1.2F, g * 1.2F, b * 1.2F, a);
         buffer.addVertex(this.offset, this.height + 0.5F, 1.0F - this.offset + z).setColor(r * 1.2F, g * 1.2F, b * 1.2F, a);
         buffer.addVertex(this.offset, 0.5F, 1.0F - this.offset + z).setColor(r * 1.2F, g * 1.2F, b * 1.2F, a);
         buffer.addVertex(this.offset, 0.5F, 1.0F - this.offset + z).setColor(r, g, b, a);
         buffer.addVertex(this.offset, this.height + 0.5F, 1.0F - this.offset + z).setColor(r, g, b, a);
         buffer.addVertex(this.offset, this.height + 0.5F, this.offset + z).setColor(r, g, b, a);
         buffer.addVertex(this.offset, 0.5F, this.offset + z).setColor(r, g, b, a);
         buffer.addVertex(1.0F - this.offset, 0.5F, this.offset + z).setColor(r, g, b, a);
         buffer.addVertex(1.0F - this.offset, this.height + 0.5F, this.offset + z).setColor(r, g, b, a);
         buffer.addVertex(1.0F - this.offset, this.height + 0.5F, 1.0F - this.offset + z).setColor(r, g, b, a);
         buffer.addVertex(1.0F - this.offset, 0.5F, 1.0F - this.offset + z).setColor(r, g, b, a);
      }
   }
}
