package mcjty.rftoolsbuilder.modules.builder.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import mcjty.lib.varia.CompositeStreamCodec;
import mcjty.rftoolsbuilder.modules.builder.blocks.AnchorMode;
import mcjty.rftoolsbuilder.modules.builder.blocks.BuilderMode;
import mcjty.rftoolsbuilder.modules.builder.blocks.RotateMode;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record BuilderData(
   String lastError, BuilderMode mode, AnchorMode anchor, RotateMode rotate, BuilderData.Flags flags, BlockPos scan, BlockPos minBox, BlockPos maxBox
) {
   public static final BuilderData.Flags DEFAULT_FLAGS = new BuilderData.Flags(false, false, false, false, true, false);
   public static final Codec<BuilderData.Flags> FLAGS_CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
            Codec.BOOL.fieldOf("silent").forGetter(BuilderData.Flags::silent),
            Codec.BOOL.fieldOf("supportMode").forGetter(BuilderData.Flags::supportMode),
            Codec.BOOL.fieldOf("entityMode").forGetter(BuilderData.Flags::entityMode),
            Codec.BOOL.fieldOf("loopMode").forGetter(BuilderData.Flags::loopMode),
            Codec.BOOL.fieldOf("waitMode").forGetter(BuilderData.Flags::waitMode),
            Codec.BOOL.fieldOf("hilightMode").forGetter(BuilderData.Flags::hilightMode)
         )
         .apply(instance, BuilderData.Flags::new)
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, BuilderData.Flags> FLAGS_STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.BOOL,
      BuilderData.Flags::silent,
      ByteBufCodecs.BOOL,
      BuilderData.Flags::supportMode,
      ByteBufCodecs.BOOL,
      BuilderData.Flags::entityMode,
      ByteBufCodecs.BOOL,
      BuilderData.Flags::loopMode,
      ByteBufCodecs.BOOL,
      BuilderData.Flags::waitMode,
      ByteBufCodecs.BOOL,
      BuilderData.Flags::hilightMode,
      BuilderData.Flags::new
   );
   public static final BuilderData DEFAULT = new BuilderData(
      null, BuilderMode.MODE_COPY, AnchorMode.ANCHOR_SW, RotateMode.ROTATE_0, DEFAULT_FLAGS, null, null, null
   );
   public static final Codec<BuilderData> CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
            Codec.STRING.optionalFieldOf("lasterror").forGetter(data -> Optional.ofNullable(data.lastError())),
            BuilderMode.CODEC.fieldOf("mode").forGetter(BuilderData::mode),
            AnchorMode.CODEC.fieldOf("anchor").forGetter(BuilderData::anchor),
            RotateMode.CODEC.fieldOf("rotate").forGetter(BuilderData::rotate),
            FLAGS_CODEC.fieldOf("flags").forGetter(BuilderData::flags),
            BlockPos.CODEC.optionalFieldOf("scan").forGetter(data -> Optional.ofNullable(data.scan())),
            BlockPos.CODEC.optionalFieldOf("minBox").forGetter(data -> Optional.ofNullable(data.minBox())),
            BlockPos.CODEC.optionalFieldOf("maxBox").forGetter(data -> Optional.ofNullable(data.maxBox()))
         )
         .apply(
            instance,
            (lasterror, mode, anchor, rotate, flags, scan, minBox, maxBox) -> new BuilderData(
               (String)lasterror.orElse(null),
               mode,
               anchor,
               rotate,
               flags,
               (BlockPos)scan.orElse(null),
               (BlockPos)minBox.orElse(null),
               (BlockPos)maxBox.orElse(null)
            )
         )
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, BuilderData> STREAM_CODEC = CompositeStreamCodec.composite(
      ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8),
      data -> Optional.ofNullable(data.lastError()),
      BuilderMode.STREAM_CODEC,
      BuilderData::mode,
      AnchorMode.STREAM_CODEC,
      BuilderData::anchor,
      RotateMode.STREAM_CODEC,
      BuilderData::rotate,
      FLAGS_STREAM_CODEC,
      BuilderData::flags,
      ByteBufCodecs.optional(BlockPos.STREAM_CODEC),
      data -> Optional.ofNullable(data.scan()),
      ByteBufCodecs.optional(BlockPos.STREAM_CODEC),
      data -> Optional.ofNullable(data.minBox()),
      ByteBufCodecs.optional(BlockPos.STREAM_CODEC),
      data -> Optional.ofNullable(data.maxBox()),
      (lastError, mode, anchor, rotate, flags, scan, minBox, maxBox) -> new BuilderData(
         (String)lastError.orElse(null), mode, anchor, rotate, flags, (BlockPos)scan.orElse(null), (BlockPos)minBox.orElse(null), (BlockPos)maxBox.orElse(null)
      )
   );

   public BuilderData withLastError(String lastError) {
      return new BuilderData(lastError, this.mode, this.anchor, this.rotate, this.flags, this.scan, this.minBox, this.maxBox);
   }

   public BuilderData withMode(BuilderMode mode) {
      return new BuilderData(this.lastError, mode, this.anchor, this.rotate, this.flags, this.scan, this.minBox, this.maxBox);
   }

   public BuilderData withAnchor(AnchorMode anchor) {
      return new BuilderData(this.lastError, this.mode, anchor, this.rotate, this.flags, this.scan, this.minBox, this.maxBox);
   }

   public BuilderData withRotate(RotateMode rotate) {
      return new BuilderData(this.lastError, this.mode, this.anchor, rotate, this.flags, this.scan, this.minBox, this.maxBox);
   }

   public BuilderData withSilent(boolean silent) {
      return new BuilderData(
         this.lastError,
         this.mode,
         this.anchor,
         this.rotate,
         new BuilderData.Flags(silent, this.flags.supportMode, this.flags.entityMode, this.flags.loopMode, this.flags.waitMode, this.flags.hilightMode),
         this.scan,
         this.minBox,
         this.maxBox
      );
   }

   public BuilderData withSupportMode(boolean supportMode) {
      return new BuilderData(
         this.lastError,
         this.mode,
         this.anchor,
         this.rotate,
         new BuilderData.Flags(this.flags.silent, supportMode, this.flags.entityMode, this.flags.loopMode, this.flags.waitMode, this.flags.hilightMode),
         this.scan,
         this.minBox,
         this.maxBox
      );
   }

   public BuilderData withEntityMode(boolean entityMode) {
      return new BuilderData(
         this.lastError,
         this.mode,
         this.anchor,
         this.rotate,
         new BuilderData.Flags(this.flags.silent, this.flags.supportMode, entityMode, this.flags.loopMode, this.flags.waitMode, this.flags.hilightMode),
         this.scan,
         this.minBox,
         this.maxBox
      );
   }

   public BuilderData withLoopMode(boolean loopMode) {
      return new BuilderData(
         this.lastError,
         this.mode,
         this.anchor,
         this.rotate,
         new BuilderData.Flags(this.flags.silent, this.flags.supportMode, this.flags.entityMode, loopMode, this.flags.waitMode, this.flags.hilightMode),
         this.scan,
         this.minBox,
         this.maxBox
      );
   }

   public BuilderData withWaitMode(boolean waitMode) {
      return new BuilderData(
         this.lastError,
         this.mode,
         this.anchor,
         this.rotate,
         new BuilderData.Flags(this.flags.silent, this.flags.supportMode, this.flags.entityMode, this.flags.loopMode, waitMode, this.flags.hilightMode),
         this.scan,
         this.minBox,
         this.maxBox
      );
   }

   public BuilderData withHilightMode(boolean hilightMode) {
      return new BuilderData(
         this.lastError,
         this.mode,
         this.anchor,
         this.rotate,
         new BuilderData.Flags(this.flags.silent, this.flags.supportMode, this.flags.entityMode, this.flags.loopMode, this.flags.waitMode, hilightMode),
         this.scan,
         this.minBox,
         this.maxBox
      );
   }

   public BuilderData withScan(BlockPos scan) {
      return new BuilderData(this.lastError, this.mode, this.anchor, this.rotate, this.flags, scan, this.minBox, this.maxBox);
   }

   public BuilderData withMinBox(BlockPos minBox) {
      return new BuilderData(this.lastError, this.mode, this.anchor, this.rotate, this.flags, this.scan, minBox, this.maxBox);
   }

   public BuilderData withMaxBox(BlockPos maxBox) {
      return new BuilderData(this.lastError, this.mode, this.anchor, this.rotate, this.flags, this.scan, this.minBox, maxBox);
   }

   public record Flags(boolean silent, boolean supportMode, boolean entityMode, boolean loopMode, boolean waitMode, boolean hilightMode) {
   }
}
