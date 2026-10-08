package mcjty.rftoolsbuilder.modules.shield;

import com.mojang.serialization.Codec;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

public enum ShieldTexture implements StringRepresentable {
   SHIELD("Shield", "shield"),
   STRIPES("Rain", "shieldstripes");

   private static final Map<String, ShieldTexture> MODE_TO_MODE = new HashMap<>();
   private final String description;
   private final String path;
   public static final Codec<ShieldTexture> CODEC = StringRepresentable.fromEnum(ShieldTexture::values);
   public static final StreamCodec<FriendlyByteBuf, ShieldTexture> STREAM_CODEC = NeoForgeStreamCodecs.enumCodec(ShieldTexture.class);

   private ShieldTexture(String description, String path) {
      this.description = description;
      this.path = path;
   }

   public String getDescription() {
      return this.description;
   }

   public String getPath() {
      return this.path;
   }

   public static ShieldTexture getMode(String mode) {
      return MODE_TO_MODE.get(mode);
   }

   @Nonnull
   public String getSerializedName() {
      return this.name().toLowerCase();
   }

   static {
      for (ShieldTexture mode : values()) {
         MODE_TO_MODE.put(mode.description, mode);
      }
   }
}
