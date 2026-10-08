package mcjty.rftoolsbuilder.shapes;

import java.util.ArrayList;
import java.util.List;
import mcjty.lib.varia.Check32;
import mcjty.lib.varia.Tools;
import mcjty.rftoolsbuilder.modules.builder.data.ShapeCardData;
import mcjty.rftoolsbuilder.modules.builder.items.ShapeCardItem;
import mcjty.rftoolsbuilder.modules.scanner.ScannerConfiguration;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class Formulas {
   private static float squaredDistance3D(float cx, float cy, float cz, float x1, float y1, float z1, float dx2, float dy2, float dz2) {
      return (x1 - cx) * (x1 - cx) / dx2 + (y1 - cy) * (y1 - cy) / dy2 + (z1 - cz) * (z1 - cz) / dz2;
   }

   private static float squaredDistance2D(float cx, float cz, float x1, float z1, float dx2, float dz2) {
      return (x1 - cx) * (x1 - cx) / dx2 + (z1 - cz) * (z1 - cz) / dz2;
   }

   public static class Bounds {
      private BlockPos p1;
      private BlockPos p2;
      private BlockPos offset;

      public Bounds(BlockPos p1, BlockPos p2, BlockPos offset) {
         this.p1 = p1;
         this.p2 = p2;
         this.offset = offset;
      }

      public BlockPos getP1() {
         return this.p1;
      }

      public BlockPos getP2() {
         return this.p2;
      }

      public BlockPos getOffset() {
         return this.offset;
      }

      public boolean in(BlockPos p) {
         int x = p.getX();
         int y = p.getY();
         int z = p.getZ();
         return this.in(x, y, z);
      }

      public boolean in(int x, int y, int z) {
         return x >= this.p1.getX() && x < this.p2.getX() && y >= this.p1.getY() && y < this.p2.getY() && z >= this.p1.getZ() && z < this.p2.getZ();
      }
   }

   static class FormulaBottomDome implements IFormula {
      private float centerx;
      private float centery;
      private float centerz;
      private float dx2;
      private float dy2;
      private float dz2;
      private int davg;

      @Override
      public void setup(Level world, BlockPos thisCoord, BlockPos dimension, BlockPos offset, ItemStack card) {
         int dx = dimension.getX();
         int dy = dimension.getY();
         int dz = dimension.getZ();
         int xCoord = thisCoord.getX();
         int yCoord = thisCoord.getY();
         int zCoord = thisCoord.getZ();
         this.centerx = xCoord + offset.getX() + (dx % 2 != 0 ? 0.0F : -0.5F);
         this.centery = yCoord + offset.getY() + (dy % 2 != 0 ? 0.0F : -0.5F);
         this.centerz = zCoord + offset.getZ() + (dz % 2 != 0 ? 0.0F : -0.5F);
         float factor = 1.8F;
         this.dx2 = dx == 0 ? 0.5F : (dx + factor) * (dx + factor) / 4.0F;
         this.dy2 = dy == 0 ? 0.5F : (dy + factor) * (dy + factor) / 4.0F;
         this.dz2 = dz == 0 ? 0.5F : (dz + factor) * (dz + factor) / 4.0F;
         this.davg = (int)((dx + dy + dz + factor * 3.0F) / 3.0F);
      }

      @Override
      public boolean isInside(int x, int y, int z) {
         if (y > this.centery) {
            return false;
         } else {
            double distance = Math.sqrt(
               Formulas.squaredDistance3D(this.centerx, this.centery, this.centerz, (float)x, (float)y, (float)z, this.dx2, this.dy2, this.dz2)
            );
            return (int)(distance * (this.davg / 2 + 1)) <= this.davg / 2 - 1;
         }
      }
   }

   static class FormulaBox implements IFormula {
      private int x1;
      private int y1;
      private int z1;
      private int x2;
      private int y2;
      private int z2;

      @Override
      public void setup(Level world, BlockPos thisCoord, BlockPos dimension, BlockPos offset, ItemStack card) {
         int dx = dimension.getX();
         int dy = dimension.getY();
         int dz = dimension.getZ();
         int xCoord = thisCoord.getX();
         int yCoord = thisCoord.getY();
         int zCoord = thisCoord.getZ();
         BlockPos tl = new BlockPos(xCoord - dx / 2 + offset.getX(), yCoord - dy / 2 + offset.getY(), zCoord - dz / 2 + offset.getZ());
         this.x1 = tl.getX();
         this.y1 = tl.getY();
         this.z1 = tl.getZ();
         this.x2 = this.x1 + dx;
         this.y2 = this.y1 + dy;
         this.z2 = this.z1 + dz;
      }

      @Override
      public boolean isInside(int x, int y, int z) {
         return x >= this.x1 && x < this.x2 && y >= this.y1 && y < this.y2 && z >= this.z1 && z < this.z2;
      }

      @Override
      public boolean isBorder(int x, int y, int z) {
         return x == this.x1 || x == this.x2 - 1 || y == this.y1 || y == this.y2 - 1 || z == this.z1 || z == this.z2 - 1;
      }
   }

   static class FormulaCappedCylinder implements IFormula {
      private float centerx;
      private float centerz;
      private float dx2;
      private float dz2;
      private int davg;
      private int y1;
      private int y2;

      @Override
      public void setup(Level world, BlockPos thisCoord, BlockPos dimension, BlockPos offset, ItemStack card) {
         int dx = dimension.getX();
         int dy = dimension.getY();
         int dz = dimension.getZ();
         int xCoord = thisCoord.getX();
         int yCoord = thisCoord.getY();
         int zCoord = thisCoord.getZ();
         this.centerx = xCoord + offset.getX() + (dx % 2 != 0 ? 0.0F : -0.5F);
         this.centerz = zCoord + offset.getZ() + (dz % 2 != 0 ? 0.0F : -0.5F);
         float factor = 1.7F;
         this.dx2 = dx == 0 ? 0.5F : (dx + factor) * (dx + factor) / 4.0F;
         this.dz2 = dz == 0 ? 0.5F : (dz + factor) * (dz + factor) / 4.0F;
         this.davg = (int)((dx + dz + factor * 2.0F) / 2.0F);
         this.y1 = yCoord - dy / 2 + offset.getY();
         this.y2 = this.y1 + dy;
      }

      @Override
      public boolean isInside(int x, int y, int z) {
         if (y >= this.y1 && y < this.y2) {
            double distance = Math.sqrt(Formulas.squaredDistance2D(this.centerx, this.centerz, (float)x, (float)z, this.dx2, this.dz2));
            return (int)(distance * (this.davg / 2 + 1)) <= this.davg / 2 - 1;
         } else {
            return false;
         }
      }
   }

   static class FormulaComposition implements IFormula {
      private BlockPos thisCoord;
      private BlockState blockState;
      private List<IFormula> formulas = new ArrayList<>();
      private List<Formulas.Bounds> bounds = new ArrayList<>();
      private List<ShapeModifier> modifiers = new ArrayList<>();
      private List<BlockState> blockStates = new ArrayList<>();

      @Override
      public void setup(Level world, BlockPos thisCoord, BlockPos dimension, BlockPos offset, ItemStack card) {
         this.thisCoord = thisCoord;
         if (card != null) {
            int dx = dimension.getX();
            int dy = dimension.getY();
            int dz = dimension.getZ();
            if (dx > 0 && dy > 0 && dz > 0) {
               for (ShapeCardData.ShapeCardChild child : ShapeCardItem.getChildren(card)) {
                  ItemStack childStack = child.stack();
                  IFormula formula = ShapeCardItem.createCorrectFormula(childStack);
                  ShapeModifier modifier = ShapeCardItem.getModifier(childStack);
                  ShapeRotation rotation = modifier.getRotation();
                  this.modifiers.add(modifier);
                  BlockPos dim = ShapeCardItem.getClampedDimension(childStack, (Integer)ScannerConfiguration.maxScannerDimension.get());
                  BlockPos off = ShapeCardItem.getClampedOffset(childStack, (Integer)ScannerConfiguration.maxScannerOffset.get());
                  BlockPos o = off.offset(offset);
                  formula.setup(world, thisCoord, dim, o, childStack);
                  this.formulas.add(formula);
                  dim = rotation.transformDimension(dim);
                  BlockPos tl = new BlockPos(o.getX() - dim.getX() / 2, o.getY() - dim.getY() / 2, o.getZ() - dim.getZ() / 2);
                  this.bounds.add(new Formulas.Bounds(tl, tl.offset(dim), o));
                  BlockState state = null;
                  if (child.ghostBlock().isPresent()) {
                     Block block = Tools.getBlock(child.ghostBlock().get());
                     if (block != null) {
                        state = block.defaultBlockState();
                     }
                  }

                  this.blockStates.add(state);
               }
            }
         }
      }

      @Override
      public void getCheckSumClient(ItemStack card, Check32 crc) {
         ShapeCardItem.getLocalChecksum(card, crc);

         for (ShapeCardData.ShapeCardChild child : ShapeCardItem.getChildren(card)) {
            ItemStack childStack = child.stack();
            IFormula formula = ShapeCardItem.createCorrectFormula(childStack);
            formula.getCheckSumClient(childStack, crc);
            ShapeModifier modifier = ShapeCardItem.getModifier(childStack);
            crc.add(modifier.isFlipY() ? 1 : 0);
            ShapeRotation rotation = modifier.getRotation();
            crc.add(rotation.ordinal());
            ShapeOperation operation = modifier.getOperation();
            crc.add(operation.ordinal());
            if (child.ghostBlock().isPresent()) {
               Block block = Tools.getBlock(child.ghostBlock().get());
               if (block != null) {
                  crc.add(Block.getId(block.defaultBlockState()));
               }
            }
         }
      }

      @Override
      public BlockState getLastState() {
         return this.blockState;
      }

      @Override
      public boolean isInside(int x, int y, int z) {
         this.blockState = null;
         x -= this.thisCoord.getX();
         y -= this.thisCoord.getY();
         z -= this.thisCoord.getZ();
         boolean ok = false;

         for (int i = 0; i < this.formulas.size(); i++) {
            IFormula formula = this.formulas.get(i);
            Formulas.Bounds bounds = this.bounds.get(i);
            ShapeModifier modifier = this.modifiers.get(i);
            boolean inside = false;
            if (bounds.in(x, y, z)) {
               int tx = x;
               int ty = y;
               int tz = z;
               BlockPos o = bounds.getOffset();
               switch (modifier.getRotation()) {
                  case X:
                     tx = x;
                     ty = z - o.getZ() + o.getY();
                     tz = y - o.getY() + o.getZ();
                     break;
                  case Y:
                     tx = z - o.getZ() + o.getX();
                     ty = y;
                     tz = x - o.getX() + o.getZ();
                     break;
                  case Z:
                     tx = y - o.getY() + o.getX();
                     ty = x - o.getX() + o.getY();
                     tz = z;
                  case NONE:
               }

               if (modifier.isFlipY()) {
                  ty = bounds.getP1().getY() + bounds.getP2().getY() - 1 - ty;
               }

               inside = formula.isInside(tx + this.thisCoord.getX(), ty + this.thisCoord.getY(), tz + this.thisCoord.getZ());
            }

            switch (modifier.getOperation()) {
               case UNION:
                  if (inside) {
                     ok = true;
                     this.blockState = this.blockStates.get(i);
                     if (this.blockState == null) {
                        this.blockState = formula.getLastState();
                     }
                  }
                  break;
               case SUBTRACT:
                  if (inside) {
                     ok = false;
                  }
                  break;
               case INTERSECT:
                  if (!inside || !ok) {
                     ok = false;
                  } else if (this.blockState == null) {
                     this.blockState = this.blockStates.get(i);
                     if (this.blockState == null) {
                        this.blockState = formula.getLastState();
                     }
                  }
            }
         }

         return ok;
      }

      @Override
      public boolean isCustom() {
         return true;
      }
   }

   static class FormulaCone implements IFormula {
      private float centerx;
      private float centerz;
      private float dx2;
      private float dz2;
      private float dy;
      private float topy;
      private int davg;

      @Override
      public void setup(Level world, BlockPos thisCoord, BlockPos dimension, BlockPos offset, ItemStack card) {
         int dx = dimension.getX();
         int dy = dimension.getY();
         int dz = dimension.getZ();
         int xCoord = thisCoord.getX();
         int yCoord = thisCoord.getY();
         int zCoord = thisCoord.getZ();
         this.centerx = xCoord + offset.getX() + (dx % 2 != 0 ? 0.0F : -0.5F);
         this.centerz = zCoord + offset.getZ() + (dz % 2 != 0 ? 0.0F : -0.5F);
         float factor = 1.7F;
         this.dx2 = dx == 0 ? 0.5F : (dx + factor) * (dx + factor) / 4.0F;
         this.dz2 = dz == 0 ? 0.5F : (dz + factor) * (dz + factor) / 4.0F;
         this.davg = (int)((dx + dz + factor * 2.0F) / 2.0F);
         this.dy = dy + 0.5F;
         this.topy = yCoord + offset.getY() + dy / 2;
      }

      @Override
      public boolean isInside(int x, int y, int z) {
         double distance = Math.sqrt(Formulas.squaredDistance2D(this.centerx, this.centerz, (float)x, (float)z, this.dx2, this.dz2));
         return (int)(distance * (this.davg / 2 + 1)) <= (this.davg / 2 - 1) * (this.topy - y) / this.dy;
      }
   }

   static class FormulaCylinder implements IFormula {
      private float centerx;
      private float centerz;
      private float dx2;
      private float dz2;
      private int davg;

      @Override
      public void setup(Level world, BlockPos thisCoord, BlockPos dimension, BlockPos offset, ItemStack card) {
         int dx = dimension.getX();
         int dy = dimension.getY();
         int dz = dimension.getZ();
         int xCoord = thisCoord.getX();
         int yCoord = thisCoord.getY();
         int zCoord = thisCoord.getZ();
         this.centerx = xCoord + offset.getX() + (dx % 2 != 0 ? 0.0F : -0.5F);
         this.centerz = zCoord + offset.getZ() + (dz % 2 != 0 ? 0.0F : -0.5F);
         float factor = 1.7F;
         this.dx2 = dx == 0 ? 0.5F : (dx + factor) * (dx + factor) / 4.0F;
         this.dz2 = dz == 0 ? 0.5F : (dz + factor) * (dz + factor) / 4.0F;
         this.davg = (int)((dx + dz + factor * 2.0F) / 2.0F);
      }

      @Override
      public boolean isInside(int x, int y, int z) {
         double distance = Math.sqrt(Formulas.squaredDistance2D(this.centerx, this.centerz, (float)x, (float)z, this.dx2, this.dz2));
         return (int)(distance * (this.davg / 2 + 1)) <= this.davg / 2 - 1;
      }
   }

   static class FormulaHeart implements IFormula {
      private float centerx;
      private float centery;
      private float centerz;
      private int dx;
      private int dy;
      private int dz;

      @Override
      public void setup(Level world, BlockPos thisCoord, BlockPos dimension, BlockPos offset, ItemStack card) {
         this.dx = dimension.getX();
         this.dy = dimension.getY();
         this.dz = dimension.getZ();
         int xCoord = thisCoord.getX();
         int yCoord = thisCoord.getY();
         int zCoord = thisCoord.getZ();
         this.centerx = xCoord + offset.getX() + (this.dx % 2 != 0 ? 0.0F : -0.5F);
         this.centery = yCoord + offset.getY() + (this.dy % 2 != 0 ? 0.0F : -0.5F);
         this.centerz = zCoord + offset.getZ() + (this.dz % 2 != 0 ? 0.0F : -0.5F);
      }

      @Override
      public boolean isInside(int x, int y, int z) {
         double xx = (x - this.centerx) * 2.6 / this.dx + 0.1;
         double zz = (y - this.centery) * 2.4 / this.dy + 0.2;
         double yy = (z - this.centerz) * 1.6 / this.dz + 0.1;
         double f1 = Math.pow(xx * xx + 2.25 * yy * yy + zz * zz - 1.0, 3.0);
         double f2 = xx * xx * zz * zz * zz;
         double f3 = 0.1125 * yy * yy * zz * zz * zz;
         double f = f1 - f2 - f3;
         return f < 0.0;
      }
   }

   static class FormulaPrism implements IFormula {
      private int x1;
      private int y1;
      private int z1;
      private int x2;
      private int y2;
      private int z2;

      @Override
      public void setup(Level world, BlockPos thisCoord, BlockPos dimension, BlockPos offset, ItemStack card) {
         int dx = dimension.getX();
         int dy = dimension.getY();
         int dz = dimension.getZ();
         int xCoord = thisCoord.getX();
         int yCoord = thisCoord.getY();
         int zCoord = thisCoord.getZ();
         BlockPos tl = new BlockPos(xCoord - dx / 2 + offset.getX(), yCoord - dy / 2 + offset.getY(), zCoord - dz / 2 + offset.getZ());
         this.x1 = tl.getX();
         this.y1 = tl.getY();
         this.z1 = tl.getZ();
         this.x2 = this.x1 + dx;
         this.y2 = this.y1 + dy;
         this.z2 = this.z1 + dz;
      }

      @Override
      public boolean isInside(int x, int y, int z) {
         if (y >= this.y1 && y < this.y2) {
            int dy = y - this.y1;
            return x >= this.x1 + dy && x < this.x2 - dy && z >= this.z1 + dy && z < this.z2 - dy;
         } else {
            return false;
         }
      }
   }

   static class FormulaScan implements IFormula {
      private byte[] data;
      private List<BlockState> palette = new ArrayList<>();
      private int x1;
      private int y1;
      private int z1;
      private int dx;
      private int dy;
      private int dz;
      private BlockState lastState = null;

      @Override
      public void getCheckSumClient(ItemStack tc, Check32 crc) {
         ShapeCardItem.getLocalChecksum(tc, crc);
         int scanId = ShapeCardItem.getScanId(tc);
         crc.add(scanId);
         if (scanId != 0) {
            crc.add(ScanDataManagerClient.getScansClient().getScanDirtyCounterClient(scanId));
         }
      }

      @Override
      public void setup(Level world, BlockPos thisCoord, BlockPos dimension, BlockPos offset, ItemStack card) {
         this.data = null;
         if (card != null) {
            this.dx = dimension.getX();
            this.dy = dimension.getY();
            this.dz = dimension.getZ();
            if (this.dx > 0 && this.dy > 0 && this.dz > 0) {
               int xCoord = thisCoord.getX();
               int yCoord = thisCoord.getY();
               int zCoord = thisCoord.getZ();
               BlockPos tl = new BlockPos(xCoord - this.dx / 2 + offset.getX(), yCoord - this.dy / 2 + offset.getY(), zCoord - this.dz / 2 + offset.getZ());
               this.x1 = tl.getX();
               this.y1 = tl.getY();
               this.z1 = tl.getZ();
               this.palette.clear();
               int scanId = ShapeCardItem.getScanId(card);
               if (scanId != 0) {
                  Scan scan = ScanDataManager.get(world).loadScan(world, scanId);
                  this.palette = new ArrayList<>(scan.getMaterialPalette());
                  byte[] datas = scan.getRledata();
                  this.data = new byte[this.dx * this.dy * this.dz];
                  int j = 0;

                  for (int i = 0; i < datas.length / 2; i++) {
                     int cnt = datas[i * 2] & 255;
                     int c = datas[i * 2 + 1] & 255;
                     if (c == 255) {
                        c = 0;
                     }

                     while (cnt > 0 && j < this.data.length) {
                        this.data[j++] = (byte)c;
                        cnt--;
                     }

                     if (j >= this.data.length) {
                        break;
                     }
                  }
               }
            }
         }
      }

      @Override
      public boolean isBorder(int x, int y, int z) {
         if (x <= this.x1 || x >= this.x1 + this.dx - 1 || y <= this.y1 || y >= this.y1 + this.dy - 1 || z <= this.z1 || z >= this.z1 + this.dz - 1) {
            return this.isInsideSafe(x, y, z);
         } else if (this.data == null) {
            return false;
         } else {
            int index = (x - this.x1) * this.dy * this.dz + (z - this.z1) * this.dy + (y - this.y1);
            return this.isInsideInternal(index - 1)
                  && this.isInsideInternal(index + 1)
                  && this.isInsideInternal(index - this.dy)
                  && this.isInsideInternal(index + this.dy)
                  && this.isInsideInternal(index - this.dy * this.dz)
                  && this.isInsideInternal(index + this.dy * this.dz)
               ? false
               : this.isInsideInternal(index);
         }
      }

      @Override
      public boolean isVisible(int x, int y, int z) {
         int index = (x - this.x1) * this.dy * this.dz + (z - this.z1) * this.dy + (y - this.y1);
         return this.isClear(index - 1)
            || this.isClear(index + 1)
            || this.isClear(index - this.dy)
            || this.isClear(index + this.dy)
            || this.isClear(index - this.dy * this.dz)
            || this.isClear(index + this.dy * this.dz);
      }

      public boolean isClear(int index) {
         if (!this.isInsideInternal(index)) {
            return true;
         } else {
            BlockState state = this.getLastState();
            return state != null ? ShapeBlockInfo.isNonSolidBlock(state.getBlock()) : false;
         }
      }

      private boolean isInsideInternal(int index) {
         if (index < 0 || index >= this.data.length) {
            return false;
         } else if (this.data[index] == 0) {
            return false;
         } else {
            int idx = (this.data[index] & 255) - 1;
            this.lastState = this.palette.get(idx);
            return true;
         }
      }

      @Override
      public boolean isInsideSafe(int x, int y, int z) {
         return x >= this.x1 && x < this.x1 + this.dx && y >= this.y1 && y < this.y1 + this.dy && z >= this.z1 && z < this.z1 + this.dz
            ? this.isInside(x, y, z)
            : false;
      }

      @Override
      public boolean isInside(int x, int y, int z) {
         if (this.data == null) {
            return false;
         } else {
            int index = (x - this.x1) * this.dy * this.dz + (z - this.z1) * this.dy + (y - this.y1);
            return this.isInsideInternal(index);
         }
      }

      @Override
      public BlockState getLastState() {
         return this.lastState;
      }
   }

   static class FormulaSphere implements IFormula {
      private float centerx;
      private float centery;
      private float centerz;
      private float dx2;
      private float dy2;
      private float dz2;
      private int davg;

      @Override
      public void setup(Level world, BlockPos thisCoord, BlockPos dimension, BlockPos offset, ItemStack card) {
         int dx = dimension.getX();
         int dy = dimension.getY();
         int dz = dimension.getZ();
         int xCoord = thisCoord.getX();
         int yCoord = thisCoord.getY();
         int zCoord = thisCoord.getZ();
         this.centerx = xCoord + offset.getX() + (dx % 2 != 0 ? 0.0F : -0.5F);
         this.centery = yCoord + offset.getY() + (dy % 2 != 0 ? 0.0F : -0.5F);
         this.centerz = zCoord + offset.getZ() + (dz % 2 != 0 ? 0.0F : -0.5F);
         float factor = 1.8F;
         this.dx2 = dx == 0 ? 0.5F : (dx + factor) * (dx + factor) / 4.0F;
         this.dy2 = dy == 0 ? 0.5F : (dy + factor) * (dy + factor) / 4.0F;
         this.dz2 = dz == 0 ? 0.5F : (dz + factor) * (dz + factor) / 4.0F;
         this.davg = (int)((dx + dy + dz + factor * 3.0F) / 3.0F);
      }

      @Override
      public boolean isInside(int x, int y, int z) {
         double distance = Math.sqrt(
            Formulas.squaredDistance3D(this.centerx, this.centery, this.centerz, (float)x, (float)y, (float)z, this.dx2, this.dy2, this.dz2)
         );
         return (int)(distance * (this.davg / 2 + 1)) <= this.davg / 2 - 1;
      }
   }

   static class FormulaTopDome implements IFormula {
      private float centerx;
      private float centery;
      private float centerz;
      private float dx2;
      private float dy2;
      private float dz2;
      private int davg;

      @Override
      public void setup(Level world, BlockPos thisCoord, BlockPos dimension, BlockPos offset, ItemStack card) {
         int dx = dimension.getX();
         int dy = dimension.getY();
         int dz = dimension.getZ();
         int xCoord = thisCoord.getX();
         int yCoord = thisCoord.getY();
         int zCoord = thisCoord.getZ();
         this.centerx = xCoord + offset.getX() + (dx % 2 != 0 ? 0.0F : -0.5F);
         this.centery = yCoord + offset.getY() + (dy % 2 != 0 ? 0.0F : -0.5F);
         this.centerz = zCoord + offset.getZ() + (dz % 2 != 0 ? 0.0F : -0.5F);
         float factor = 1.8F;
         this.dx2 = dx == 0 ? 0.5F : (dx + factor) * (dx + factor) / 4.0F;
         this.dy2 = dy == 0 ? 0.5F : (dy + factor) * (dy + factor) / 4.0F;
         this.dz2 = dz == 0 ? 0.5F : (dz + factor) * (dz + factor) / 4.0F;
         this.davg = (int)((dx + dy + dz + factor * 3.0F) / 3.0F);
      }

      @Override
      public boolean isInside(int x, int y, int z) {
         if (y < this.centery) {
            return false;
         } else {
            double distance = Math.sqrt(
               Formulas.squaredDistance3D(this.centerx, this.centery, this.centerz, (float)x, (float)y, (float)z, this.dx2, this.dy2, this.dz2)
            );
            return (int)(distance * (this.davg / 2 + 1)) <= this.davg / 2 - 1;
         }
      }
   }

   static class FormulaTorus implements IFormula {
      private float smallRadius;
      private float bigRadius;
      private float centerx;
      private float centery;
      private float centerz;

      @Override
      public void setup(Level world, BlockPos thisCoord, BlockPos dimension, BlockPos offset, ItemStack card) {
         int dx = dimension.getX();
         int dy = dimension.getY();
         int dz = dimension.getZ();
         this.smallRadius = (dy - 2) / 2.0F;
         this.bigRadius = (dx - 2) / 2.0F - this.smallRadius;
         int xCoord = thisCoord.getX();
         int yCoord = thisCoord.getY();
         int zCoord = thisCoord.getZ();
         this.centerx = xCoord + offset.getX() + (dx % 2 != 0 ? 0.0F : -0.5F);
         this.centery = yCoord + offset.getY() + (dy % 2 != 0 ? 0.0F : -0.5F);
         this.centerz = zCoord + offset.getZ() + (dz % 2 != 0 ? 0.0F : -0.5F);
      }

      @Override
      public boolean isInside(int x, int y, int z) {
         double rr = this.bigRadius - Math.sqrt((x - this.centerx) * (x - this.centerx) + (z - this.centerz) * (z - this.centerz));
         double f = rr * rr + (y - this.centery) * (y - this.centery) - this.smallRadius * this.smallRadius;
         return f < 0.0;
      }
   }
}
