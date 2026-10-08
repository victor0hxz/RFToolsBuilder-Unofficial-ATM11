package mcjty.rftoolsbuilder.modules.builder.blocks;

import com.mojang.serialization.Codec;
import mcjty.lib.varia.NamedEnum;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

public enum BuilderMode implements NamedEnum<BuilderMode> {
   MODE_COPY("Copy"),
   MODE_MOVE("Move"),
   MODE_SWAP("Swap"),
   MODE_BACK("Back"),
   MODE_COLLECT("Collect");

   private final String name;
   public static final Codec<BuilderMode> CODEC = StringRepresentable.fromEnum(BuilderMode::values);
   public static final StreamCodec<FriendlyByteBuf, BuilderMode> STREAM_CODEC = NeoForgeStreamCodecs.enumCodec(BuilderMode.class);

   private BuilderMode(String name) {
      this.name = name;
   }

   public String getName() {
      return this.name;
   }

   public String[] getDescription() {
      return new String[]{this.name};
   }

   public String getSerializedName() {
      return this.getName();
   }
}
