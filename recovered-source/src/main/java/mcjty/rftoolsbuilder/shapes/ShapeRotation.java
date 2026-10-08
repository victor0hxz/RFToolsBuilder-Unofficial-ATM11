package mcjty.rftoolsbuilder.shapes;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;

public enum ShapeRotation {
   NONE("None"),
   X("X"),
   Y("Y"),
   Z("Z");

   private final String code;
   private static final Map<String, ShapeRotation> MAP = new HashMap<>();

   private ShapeRotation(String code) {
      this.code = code;
   }

   public String getCode() {
      return this.code;
   }

   public static ShapeRotation getByName(String name) {
      return MAP.get(name);
   }

   public BlockPos transformDimension(BlockPos p) {
      switch (this) {
         case NONE:
            return p;
         case X:
            return new BlockPos(p.getX(), p.getZ(), p.getY());
         case Y:
            return new BlockPos(p.getZ(), p.getY(), p.getX());
         case Z:
            return new BlockPos(p.getY(), p.getX(), p.getZ());
         default:
            return p;
      }
   }

   static {
      for (ShapeRotation operation : values()) {
         MAP.put(operation.getCode(), operation);
      }
   }
}
