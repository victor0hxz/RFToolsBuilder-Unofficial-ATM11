package mcjty.rftoolsbuilder.shapes;

import java.util.Optional;
import javax.annotation.Nullable;
import mcjty.lib.varia.LevelTools;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public final class ShapeID {
   private final ResourceKey<Level> dimension;
   @Nullable
   private final BlockPos pos;
   private final int scanId;
   private final boolean grayscale;
   private final boolean solid;
   public static final StreamCodec<FriendlyByteBuf, ShapeID> STREAM_CODEC = StreamCodec.composite(
      ResourceKey.streamCodec(Registries.DIMENSION),
      s -> s.dimension,
      ByteBufCodecs.optional(BlockPos.STREAM_CODEC),
      s -> Optional.ofNullable(s.pos),
      ByteBufCodecs.INT,
      s -> s.scanId,
      ByteBufCodecs.BOOL,
      s -> s.grayscale,
      ByteBufCodecs.BOOL,
      s -> s.solid,
      (dimension, pos, scanId, grayscale, solid) -> new ShapeID(dimension, (BlockPos)pos.orElse(null), scanId, grayscale, solid)
   );

   public ShapeID(ResourceKey<Level> dimension, @Nullable BlockPos pos, int scanId, boolean grayscale, boolean solid) {
      this.dimension = dimension;
      this.pos = pos;
      this.scanId = scanId;
      this.grayscale = grayscale;
      this.solid = solid;
   }

   public ShapeID(FriendlyByteBuf buf) {
      ResourceKey<Level> dim = Level.OVERWORLD;
      BlockPos p = null;
      if (buf.readBoolean()) {
         dim = LevelTools.getId((Identifier)Identifier.STREAM_CODEC.decode(buf));
         p = buf.readBlockPos();
      }

      this.scanId = buf.readInt();
      this.grayscale = buf.readBoolean();
      this.solid = buf.readBoolean();
      this.pos = p;
      this.dimension = dim;
   }

   public void toBytes(FriendlyByteBuf buf) {
      if (this.getPos() == null) {
         buf.writeBoolean(false);
      } else {
         buf.writeBoolean(true);
         Identifier.STREAM_CODEC.encode(buf, this.getDimension().identifier());
         buf.writeBlockPos(this.getPos());
      }

      buf.writeInt(this.scanId);
      buf.writeBoolean(this.grayscale);
      buf.writeBoolean(this.solid);
   }

   public ResourceKey<Level> getDimension() {
      return this.dimension;
   }

   @Nullable
   public BlockPos getPos() {
      return this.pos;
   }

   public int getScanId() {
      return this.scanId;
   }

   public boolean isGrayscale() {
      return this.grayscale;
   }

   public boolean isSolid() {
      return this.solid;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         ShapeID shapeID = (ShapeID)o;
         if (!this.dimension.equals(shapeID.dimension)) {
            return false;
         } else if (this.scanId != shapeID.scanId) {
            return false;
         } else if (this.grayscale != shapeID.grayscale) {
            return false;
         } else if (this.solid != shapeID.solid) {
            return false;
         } else {
            return this.pos != null ? this.pos.equals(shapeID.pos) : shapeID.pos == null;
         }
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      int result = this.dimension.hashCode();
      result = 31 * result + (this.pos != null ? this.pos.hashCode() : 0);
      result = 31 * result + this.scanId;
      result = 31 * result + (this.grayscale ? 1 : 0);
      return 31 * result + (this.solid ? 1 : 0);
   }

   @Override
   public String toString() {
      return "ShapeID{dimension="
         + this.dimension
         + ", pos="
         + this.pos
         + ", scanId="
         + this.scanId
         + ", grayscale="
         + this.grayscale
         + ", solid="
         + this.solid
         + "}";
   }
}
