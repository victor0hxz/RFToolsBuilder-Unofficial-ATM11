package mcjty.rftoolsbuilder.modules.shield.filters;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import mcjty.lib.blockcommands.ISerializer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;

public interface ShieldFilter<T extends ShieldFilter<?>> {
   int ACTION_PASS = 0;
   int ACTION_SOLID = 1;
   int ACTION_DAMAGE = 2;
   Map<String, MapCodec<? extends ShieldFilter<?>>> CODECS = Map.of(
      "animal", AnimalFilter.CODEC, "default", DefaultFilter.CODEC, "hostile", HostileFilter.CODEC, "item", ItemFilter.CODEC, "player", PlayerFilter.CODEC
   );
   Map<String, StreamCodec> STREAM_CODECS = Map.of(
      "animal",
      AnimalFilter.STREAM_CODEC,
      "default",
      DefaultFilter.STREAM_CODEC,
      "hostile",
      HostileFilter.STREAM_CODEC,
      "item",
      ItemFilter.STREAM_CODEC,
      "player",
      PlayerFilter.STREAM_CODEC
   );
   Codec<ShieldFilter<?>> CODEC = Codec.lazyInitialized(() -> Codec.STRING.dispatch("type", ShieldFilter::getFilterName, s -> CODECS.get(s)));
   StreamCodec<FriendlyByteBuf, ShieldFilter<?>> STREAM_CODEC = StreamCodec.of((buf, shieldFilter) -> {
      buf.writeUtf(shieldFilter.getFilterName());
      StreamCodec streamCodec = shieldFilter.getStreamCodec();
      streamCodec.encode(buf, shieldFilter);
   }, buf -> {
      String id = buf.readUtf();
      StreamCodec streamCodec = STREAM_CODECS.get(id);
      return (ShieldFilter)streamCodec.decode(buf);
   });

   MapCodec<T> getCodec();

   StreamCodec<FriendlyByteBuf, T> getStreamCodec();

   boolean match(Entity var1);

   int getAction();

   T setAction(int var1);

   String getFilterName();

   public static class Serializer implements ISerializer<ShieldFilter<?>> {
      public Function<RegistryFriendlyByteBuf, ShieldFilter<?>> getDeserializer() {
         return buf -> buf.readBoolean() ? (ShieldFilter)ShieldFilter.STREAM_CODEC.decode(buf) : null;
      }

      public BiConsumer<RegistryFriendlyByteBuf, ShieldFilter<?>> getSerializer() {
         return (buf, info) -> {
            if (info == null) {
               buf.writeBoolean(false);
            } else {
               buf.writeBoolean(true);
               ShieldFilter.STREAM_CODEC.encode(buf, info);
            }
         };
      }
   }
}
