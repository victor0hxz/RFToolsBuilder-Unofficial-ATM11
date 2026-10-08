package mcjty.rftoolsbuilder.modules.shield;

import com.mojang.serialization.Codec;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.annotation.Nonnull;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

public enum ShieldRenderingMode implements StringRepresentable {
   INVISIBLE("Invisible", true),
   SHIELD("Shield", true),
   MIMIC("Mimic", false),
   TRANSP("Transp", true),
   SOLID("Solid", false);

   private static final Map<String, ShieldRenderingMode> MODE_TO_MODE = new HashMap<>();
   private final String description;
   private final boolean translucent;
   public static final Codec<ShieldRenderingMode> CODEC = StringRepresentable.fromEnum(ShieldRenderingMode::values);
   public static final StreamCodec<FriendlyByteBuf, ShieldRenderingMode> STREAM_CODEC = NeoForgeStreamCodecs.enumCodec(ShieldRenderingMode.class);

   private ShieldRenderingMode(String description, boolean translucent) {
      this.description = description;
      this.translucent = translucent;
   }

   public String getDescription() {
      return this.description;
   }

   public boolean isTranslucent() {
      return this.translucent;
   }

   public static ShieldRenderingMode getMode(String mode) {
      return MODE_TO_MODE.get(mode);
   }

   @Nonnull
   public String getSerializedName() {
      return this.name().toLowerCase(Locale.ENGLISH);
   }

   static {
      for (ShieldRenderingMode mode : values()) {
         MODE_TO_MODE.put(mode.description, mode);
      }
   }
}
