package mcjty.rftoolsbuilder.modules.shield.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import mcjty.rftoolsbuilder.modules.shield.DamageTypeMode;
import mcjty.rftoolsbuilder.modules.shield.ShieldRenderingMode;
import mcjty.rftoolsbuilder.modules.shield.ShieldTexture;
import mcjty.rftoolsbuilder.modules.shield.filters.ShieldFilter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ShieldData(
   ShieldRenderingMode renderMode, ShieldTexture shieldTexture, DamageTypeMode damageMode, boolean blockLight, int shieldColor, List<ShieldFilter<?>> filters
) {
   public static final ShieldData DEFAULT = new ShieldData(
      ShieldRenderingMode.SHIELD, ShieldTexture.SHIELD, DamageTypeMode.DAMAGETYPE_GENERIC, false, 9895880, Collections.emptyList()
   );
   public static final Codec<ShieldData> CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
            ShieldRenderingMode.CODEC.fieldOf("render").forGetter(ShieldData::renderMode),
            ShieldTexture.CODEC.fieldOf("texture").forGetter(ShieldData::shieldTexture),
            DamageTypeMode.CODEC.fieldOf("damage").forGetter(ShieldData::damageMode),
            Codec.BOOL.fieldOf("blocklight").forGetter(ShieldData::blockLight),
            Codec.INT.fieldOf("color").forGetter(ShieldData::shieldColor),
            Codec.list(ShieldFilter.CODEC).fieldOf("filters").forGetter(ShieldData::filters)
         )
         .apply(instance, ShieldData::new)
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, ShieldData> STREAM_CODEC = StreamCodec.composite(
      ShieldRenderingMode.STREAM_CODEC,
      ShieldData::renderMode,
      ShieldTexture.STREAM_CODEC,
      ShieldData::shieldTexture,
      DamageTypeMode.STREAM_CODEC,
      ShieldData::damageMode,
      ByteBufCodecs.BOOL,
      ShieldData::blockLight,
      ByteBufCodecs.INT,
      ShieldData::shieldColor,
      ShieldFilter.STREAM_CODEC.apply(ByteBufCodecs.list()),
      ShieldData::filters,
      ShieldData::new
   );

   public ShieldData withRenderMode(ShieldRenderingMode renderMode) {
      return new ShieldData(renderMode, this.shieldTexture, this.damageMode, this.blockLight, this.shieldColor, this.filters);
   }

   public ShieldData withShieldTexture(ShieldTexture shieldTexture) {
      return new ShieldData(this.renderMode, shieldTexture, this.damageMode, this.blockLight, this.shieldColor, this.filters);
   }

   public ShieldData withDamageMode(DamageTypeMode damageMode) {
      return new ShieldData(this.renderMode, this.shieldTexture, damageMode, this.blockLight, this.shieldColor, this.filters);
   }

   public ShieldData withBlockLight(boolean blockLight) {
      return new ShieldData(this.renderMode, this.shieldTexture, this.damageMode, blockLight, this.shieldColor, this.filters);
   }

   public ShieldData withShieldColor(int shieldColor) {
      return new ShieldData(this.renderMode, this.shieldTexture, this.damageMode, this.blockLight, shieldColor, this.filters);
   }

   public ShieldData withFilters(List<ShieldFilter<?>> filters) {
      return new ShieldData(this.renderMode, this.shieldTexture, this.damageMode, this.blockLight, this.shieldColor, filters);
   }

   public ShieldData addFilter(ShieldFilter<?> filter) {
      List<ShieldFilter<?>> newFilters = new ArrayList<>(this.filters);
      newFilters.add(filter);
      return new ShieldData(this.renderMode, this.shieldTexture, this.damageMode, this.blockLight, this.shieldColor, newFilters);
   }

   public ShieldData addFilter(ShieldFilter<?> filter, int index) {
      List<ShieldFilter<?>> newFilters = new ArrayList<>(this.filters);
      newFilters.add(index, filter);
      return new ShieldData(this.renderMode, this.shieldTexture, this.damageMode, this.blockLight, this.shieldColor, newFilters);
   }

   public ShieldData removeFilter(int index) {
      List<ShieldFilter<?>> newFilters = new ArrayList<>(this.filters);
      newFilters.remove(index);
      return new ShieldData(this.renderMode, this.shieldTexture, this.damageMode, this.blockLight, this.shieldColor, newFilters);
   }

   public ShieldData moveSelectedFilterUp(int selected) {
      if (selected <= 0) {
         return this;
      } else {
         List<ShieldFilter<?>> newFilters = new ArrayList<>(this.filters);
         ShieldFilter filter1 = newFilters.get(selected - 1);
         ShieldFilter filter2 = newFilters.get(selected);
         newFilters.set(selected - 1, filter2);
         newFilters.set(selected, filter1);
         return new ShieldData(this.renderMode, this.shieldTexture, this.damageMode, this.blockLight, this.shieldColor, newFilters);
      }
   }

   public ShieldData moveSelectedFilterDown(int selected) {
      if (selected >= this.filters.size() - 1) {
         return this;
      } else {
         List<ShieldFilter<?>> newFilters = new ArrayList<>(this.filters);
         ShieldFilter filter1 = newFilters.get(selected);
         ShieldFilter filter2 = newFilters.get(selected + 1);
         newFilters.set(selected, filter2);
         newFilters.set(selected + 1, filter1);
         return new ShieldData(this.renderMode, this.shieldTexture, this.damageMode, this.blockLight, this.shieldColor, newFilters);
      }
   }
}
