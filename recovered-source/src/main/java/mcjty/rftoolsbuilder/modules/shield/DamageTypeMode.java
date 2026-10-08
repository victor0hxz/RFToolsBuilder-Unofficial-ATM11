package mcjty.rftoolsbuilder.modules.shield;

import com.mojang.serialization.Codec;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

public enum DamageTypeMode implements StringRepresentable {
   DAMAGETYPE_GENERIC("Generic"),
   DAMAGETYPE_PLAYER("Player");

   private static final Map<String, DamageTypeMode> modeToMode = new HashMap<>();
   private final String description;
   public static final Codec<DamageTypeMode> CODEC = StringRepresentable.fromEnum(DamageTypeMode::values);
   public static final StreamCodec<FriendlyByteBuf, DamageTypeMode> STREAM_CODEC = NeoForgeStreamCodecs.enumCodec(DamageTypeMode.class);

   private DamageTypeMode(String description) {
      this.description = description;
   }

   public String getDescription() {
      return this.description;
   }

   public static DamageTypeMode getMode(String mode) {
      return modeToMode.get(mode);
   }

   public String getSerializedName() {
      return this.name();
   }

   static {
      for (DamageTypeMode mode : values()) {
         modeToMode.put(mode.description, mode);
      }
   }
}
