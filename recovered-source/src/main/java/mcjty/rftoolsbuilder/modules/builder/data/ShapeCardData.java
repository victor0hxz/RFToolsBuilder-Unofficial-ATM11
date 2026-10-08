package mcjty.rftoolsbuilder.modules.builder.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import mcjty.rftoolsbuilder.shapes.Shape;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public record ShapeCardData(
   int channel,
   ShapeCardData.ShapeCardDimensions dimensions,
   boolean tagMatching,
   boolean solid,
   Set<String> voiding,
   Shape shape,
   int scanId,
   ShapeCardData.ShapeModifierData modifier,
   Optional<Identifier> ghostBlock,
   List<ShapeCardData.ShapeCardChild> children
) {
   public static final int MODE_NONE = 0;
   public static final int MODE_CORNER1 = 1;
   public static final int MODE_CORNER2 = 2;
   public static final ShapeCardData.ShapeCardDimensions DEFAULT_DIMENSIONS = new ShapeCardData.ShapeCardDimensions(
      new BlockPos(5, 5, 5), BlockPos.ZERO, 0, null, null
   );
   public static final ShapeCardData DEFAULT = new ShapeCardData(
      -1, DEFAULT_DIMENSIONS, false, true, new HashSet<>(), Shape.SHAPE_BOX, 0, ShapeCardData.ShapeModifierData.DEFAULT, Optional.empty(), List.of()
   );
   public static final Codec<ShapeCardData.ShapeCardDimensions> DIMENSIONS_CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
            BlockPos.CODEC.fieldOf("dimension").forGetter(ShapeCardData.ShapeCardDimensions::dimension),
            BlockPos.CODEC.fieldOf("offset").forGetter(ShapeCardData.ShapeCardDimensions::offset),
            Codec.INT.fieldOf("mode").forGetter(ShapeCardData.ShapeCardDimensions::mode),
            BlockPos.CODEC.optionalFieldOf("corner1").forGetter(o -> Optional.ofNullable(o.corner1)),
            GlobalPos.CODEC.optionalFieldOf("selected").forGetter(o -> Optional.ofNullable(o.selected))
         )
         .apply(
            instance, (dim, offs, m, c1, sel) -> new ShapeCardData.ShapeCardDimensions(dim, offs, m, (BlockPos)c1.orElse(null), (GlobalPos)sel.orElse(null))
         )
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, ShapeCardData.ShapeCardDimensions> DIMENSIONS_STREAM_CODEC = StreamCodec.composite(
      BlockPos.STREAM_CODEC,
      ShapeCardData.ShapeCardDimensions::dimension,
      BlockPos.STREAM_CODEC,
      ShapeCardData.ShapeCardDimensions::offset,
      ByteBufCodecs.INT,
      ShapeCardData.ShapeCardDimensions::mode,
      ByteBufCodecs.optional(BlockPos.STREAM_CODEC),
      s -> Optional.ofNullable(s.corner1()),
      ByteBufCodecs.optional(GlobalPos.STREAM_CODEC),
      s -> Optional.ofNullable(s.selected()),
      (dimension, offset, mode, corner1, selected) -> new ShapeCardData.ShapeCardDimensions(
         dimension, offset, mode, (BlockPos)corner1.orElse(null), (GlobalPos)selected.orElse(null)
      )
   );
   public static final Codec<ShapeCardData> CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
            Codec.INT.fieldOf("channel").forGetter(ShapeCardData::channel),
            DIMENSIONS_CODEC.fieldOf("dimensions").forGetter(ShapeCardData::dimensions),
            Codec.BOOL.fieldOf("tagMatching").forGetter(ShapeCardData::tagMatching),
            Codec.BOOL.optionalFieldOf("solid", true).forGetter(ShapeCardData::solid),
            Codec.STRING.listOf().fieldOf("voiding").forGetter(card -> new ArrayList<>(card.voiding)),
            Shape.CODEC.fieldOf("shape").forGetter(ShapeCardData::shape),
            Codec.INT.optionalFieldOf("scanId", 0).forGetter(ShapeCardData::scanId),
            ShapeCardData.ShapeModifierData.CODEC.optionalFieldOf("modifier", ShapeCardData.ShapeModifierData.DEFAULT).forGetter(ShapeCardData::modifier),
            Identifier.CODEC.optionalFieldOf("ghostBlock").forGetter(ShapeCardData::ghostBlock),
            ShapeCardData.ShapeCardChild.CODEC.listOf().optionalFieldOf("children", List.of()).forGetter(ShapeCardData::children)
         )
         .apply(
            instance,
            (channel, dimensions, tagMatching, solid, voiding, shape, scanId, modifier, ghostBlock, children) -> new ShapeCardData(
               channel, dimensions, tagMatching, solid, new HashSet<>(voiding), shape, scanId, modifier, ghostBlock, List.copyOf(children)
            )
         )
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, ShapeCardData> STREAM_CODEC = StreamCodec.of((buf, data) -> {
      buf.writeVarInt(data.channel);
      DIMENSIONS_STREAM_CODEC.encode(buf, data.dimensions);
      buf.writeBoolean(data.tagMatching);
      buf.writeBoolean(data.solid);
      buf.writeVarInt(data.voiding.size());

      for (String voiding : data.voiding) {
         buf.writeUtf(voiding);
      }

      Shape.STREAM_CODEC.encode(buf, data.shape);
      buf.writeVarInt(data.scanId);
      ShapeCardData.ShapeModifierData.STREAM_CODEC.encode(buf, data.modifier);
      buf.writeOptional(data.ghostBlock, (b, value) -> Identifier.STREAM_CODEC.encode(b, value));
      buf.writeVarInt(data.children.size());

      for (ShapeCardData.ShapeCardChild child : data.children) {
         ShapeCardData.ShapeCardChild.STREAM_CODEC.encode(buf, child);
      }
   }, buf -> {
      int channel = buf.readVarInt();
      ShapeCardData.ShapeCardDimensions dimensions = (ShapeCardData.ShapeCardDimensions)DIMENSIONS_STREAM_CODEC.decode(buf);
      boolean tagMatching = buf.readBoolean();
      boolean solid = buf.readBoolean();
      int voidingSize = buf.readVarInt();
      Set<String> voiding = new HashSet<>();

      for (int i = 0; i < voidingSize; i++) {
         voiding.add(buf.readUtf());
      }

      Shape shape = (Shape)Shape.STREAM_CODEC.decode(buf);
      int scanId = buf.readVarInt();
      ShapeCardData.ShapeModifierData modifier = (ShapeCardData.ShapeModifierData)ShapeCardData.ShapeModifierData.STREAM_CODEC.decode(buf);
      Optional<Identifier> ghostBlock = buf.readOptional(b -> (Identifier)Identifier.STREAM_CODEC.decode(b));
      int childSize = buf.readVarInt();
      List<ShapeCardData.ShapeCardChild> children = new ArrayList<>(childSize);

      for (int i = 0; i < childSize; i++) {
         children.add((ShapeCardData.ShapeCardChild)ShapeCardData.ShapeCardChild.STREAM_CODEC.decode(buf));
      }

      return new ShapeCardData(channel, dimensions, tagMatching, solid, voiding, shape, scanId, modifier, ghostBlock, List.copyOf(children));
   });

   public ShapeCardData withChannel(int channel) {
      return new ShapeCardData(
         channel, this.dimensions, this.tagMatching, this.solid, this.voiding, this.shape, this.scanId, this.modifier, this.ghostBlock, this.children
      );
   }

   public ShapeCardData withDimension(BlockPos dimension) {
      return new ShapeCardData(
         this.channel,
         new ShapeCardData.ShapeCardDimensions(dimension, this.dimensions.offset, this.dimensions.mode, this.dimensions.corner1, this.dimensions.selected),
         this.tagMatching,
         this.solid,
         this.voiding,
         this.shape,
         this.scanId,
         this.modifier,
         this.ghostBlock,
         this.children
      );
   }

   public ShapeCardData withOffset(BlockPos offset) {
      return new ShapeCardData(
         this.channel,
         new ShapeCardData.ShapeCardDimensions(this.dimensions.dimension, offset, this.dimensions.mode, this.dimensions.corner1, this.dimensions.selected),
         this.tagMatching,
         this.solid,
         this.voiding,
         this.shape,
         this.scanId,
         this.modifier,
         this.ghostBlock,
         this.children
      );
   }

   public ShapeCardData withMode(int mode) {
      return new ShapeCardData(
         this.channel,
         new ShapeCardData.ShapeCardDimensions(this.dimensions.dimension, this.dimensions.offset, mode, this.dimensions.corner1, this.dimensions.selected),
         this.tagMatching,
         this.solid,
         this.voiding,
         this.shape,
         this.scanId,
         this.modifier,
         this.ghostBlock,
         this.children
      );
   }

   public ShapeCardData withCorner(BlockPos corner) {
      return new ShapeCardData(
         this.channel,
         new ShapeCardData.ShapeCardDimensions(this.dimensions.dimension, this.dimensions.offset, this.dimensions.mode, corner, this.dimensions.selected),
         this.tagMatching,
         this.solid,
         this.voiding,
         this.shape,
         this.scanId,
         this.modifier,
         this.ghostBlock,
         this.children
      );
   }

   public ShapeCardData withSelected(GlobalPos selected) {
      return new ShapeCardData(
         this.channel,
         new ShapeCardData.ShapeCardDimensions(this.dimensions.dimension, this.dimensions.offset, this.dimensions.mode, this.dimensions.corner1, selected),
         this.tagMatching,
         this.solid,
         this.voiding,
         this.shape,
         this.scanId,
         this.modifier,
         this.ghostBlock,
         this.children
      );
   }

   public ShapeCardData withTagMatching(boolean tagMatching) {
      return new ShapeCardData(
         this.channel, this.dimensions, tagMatching, this.solid, this.voiding, this.shape, this.scanId, this.modifier, this.ghostBlock, this.children
      );
   }

   public ShapeCardData withSolid(boolean solid) {
      return new ShapeCardData(
         this.channel, this.dimensions, this.tagMatching, solid, this.voiding, this.shape, this.scanId, this.modifier, this.ghostBlock, this.children
      );
   }

   public ShapeCardData withShape(Shape shape) {
      return new ShapeCardData(
         this.channel, this.dimensions, this.tagMatching, this.solid, this.voiding, shape, this.scanId, this.modifier, this.ghostBlock, this.children
      );
   }

   public ShapeCardData addVoiding(String voiding) {
      Set<String> newVoiding = new HashSet<>(this.voiding);
      newVoiding.add(voiding);
      return new ShapeCardData(
         this.channel, this.dimensions, this.tagMatching, this.solid, newVoiding, this.shape, this.scanId, this.modifier, this.ghostBlock, this.children
      );
   }

   public ShapeCardData withVoiding(Set<String> voiding) {
      return new ShapeCardData(
         this.channel, this.dimensions, this.tagMatching, this.solid, voiding, this.shape, this.scanId, this.modifier, this.ghostBlock, this.children
      );
   }

   public ShapeCardData withScanId(int scanId) {
      return new ShapeCardData(
         this.channel, this.dimensions, this.tagMatching, this.solid, this.voiding, this.shape, scanId, this.modifier, this.ghostBlock, this.children
      );
   }

   public ShapeCardData withModifier(ShapeCardData.ShapeModifierData modifier) {
      return new ShapeCardData(
         this.channel, this.dimensions, this.tagMatching, this.solid, this.voiding, this.shape, this.scanId, modifier, this.ghostBlock, this.children
      );
   }

   public ShapeCardData withGhostBlock(Optional<Identifier> ghostBlock) {
      return new ShapeCardData(
         this.channel, this.dimensions, this.tagMatching, this.solid, this.voiding, this.shape, this.scanId, this.modifier, ghostBlock, this.children
      );
   }

   public ShapeCardData withChildren(List<ShapeCardData.ShapeCardChild> children) {
      return new ShapeCardData(
         this.channel,
         this.dimensions,
         this.tagMatching,
         this.solid,
         this.voiding,
         this.shape,
         this.scanId,
         this.modifier,
         this.ghostBlock,
         List.copyOf(children)
      );
   }

   public record ShapeCardChild(ItemStack stack, ShapeCardData.ShapeModifierData modifier, Optional<Identifier> ghostBlock) {
      public static final Codec<ShapeCardData.ShapeCardChild> CODEC = RecordCodecBuilder.create(
         instance -> instance.group(
               ItemStack.CODEC.fieldOf("stack").forGetter(ShapeCardData.ShapeCardChild::stack),
               ShapeCardData.ShapeModifierData.CODEC
                  .optionalFieldOf("modifier", ShapeCardData.ShapeModifierData.DEFAULT)
                  .forGetter(ShapeCardData.ShapeCardChild::modifier),
               Identifier.CODEC.optionalFieldOf("ghostBlock").forGetter(ShapeCardData.ShapeCardChild::ghostBlock)
            )
            .apply(instance, ShapeCardData.ShapeCardChild::new)
      );
      public static final StreamCodec<RegistryFriendlyByteBuf, ShapeCardData.ShapeCardChild> STREAM_CODEC = StreamCodec.composite(
         ItemStack.STREAM_CODEC,
         ShapeCardData.ShapeCardChild::stack,
         ShapeCardData.ShapeModifierData.STREAM_CODEC,
         ShapeCardData.ShapeCardChild::modifier,
         ByteBufCodecs.optional(Identifier.STREAM_CODEC),
         ShapeCardData.ShapeCardChild::ghostBlock,
         ShapeCardData.ShapeCardChild::new
      );
   }

   public record ShapeCardDimensions(BlockPos dimension, BlockPos offset, int mode, BlockPos corner1, GlobalPos selected) {
   }

   public record ShapeModifierData(String operation, boolean flipY, String rotation) {
      public static final ShapeCardData.ShapeModifierData DEFAULT = new ShapeCardData.ShapeModifierData("U", false, "0");
      public static final Codec<ShapeCardData.ShapeModifierData> CODEC = RecordCodecBuilder.create(
         instance -> instance.group(
               Codec.STRING.fieldOf("operation").forGetter(ShapeCardData.ShapeModifierData::operation),
               Codec.BOOL.fieldOf("flipY").forGetter(ShapeCardData.ShapeModifierData::flipY),
               Codec.STRING.fieldOf("rotation").forGetter(ShapeCardData.ShapeModifierData::rotation)
            )
            .apply(instance, ShapeCardData.ShapeModifierData::new)
      );
      public static final StreamCodec<RegistryFriendlyByteBuf, ShapeCardData.ShapeModifierData> STREAM_CODEC = StreamCodec.composite(
         ByteBufCodecs.STRING_UTF8,
         ShapeCardData.ShapeModifierData::operation,
         ByteBufCodecs.BOOL,
         ShapeCardData.ShapeModifierData::flipY,
         ByteBufCodecs.STRING_UTF8,
         ShapeCardData.ShapeModifierData::rotation,
         ShapeCardData.ShapeModifierData::new
      );
   }
}
