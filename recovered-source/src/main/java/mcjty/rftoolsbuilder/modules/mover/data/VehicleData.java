package mcjty.rftoolsbuilder.modules.mover.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public record VehicleData(List<VehicleData.StateWithCount> states, String name, BlockPos desiredPos, String desiredPosName) {
   public static final VehicleData DEFAULT = new VehicleData(Collections.emptyList(), "", BlockPos.ZERO, "");
   private static final Codec<VehicleData.StateWithCount> STATE_CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
            BlockState.CODEC.fieldOf("state").forGetter(VehicleData.StateWithCount::state),
            Codec.INT.listOf().fieldOf("positions").forGetter(VehicleData.StateWithCount::positions)
         )
         .apply(instance, VehicleData.StateWithCount::new)
   );
   private static final StreamCodec<RegistryFriendlyByteBuf, VehicleData.StateWithCount> STATE_STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.idMapper(Block.BLOCK_STATE_REGISTRY),
      VehicleData.StateWithCount::state,
      ByteBufCodecs.INT.apply(ByteBufCodecs.list()),
      VehicleData.StateWithCount::positions,
      VehicleData.StateWithCount::new
   );
   public static final Codec<VehicleData> CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
            STATE_CODEC.listOf().fieldOf("states").forGetter(VehicleData::states),
            Codec.STRING.fieldOf("name").forGetter(VehicleData::name),
            BlockPos.CODEC.fieldOf("desiredPos").forGetter(VehicleData::desiredPos),
            Codec.STRING.fieldOf("desiredPosName").forGetter(VehicleData::desiredPosName)
         )
         .apply(instance, VehicleData::new)
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, VehicleData> STREAM_CODEC = StreamCodec.composite(
      STATE_STREAM_CODEC.apply(ByteBufCodecs.list()),
      VehicleData::states,
      ByteBufCodecs.STRING_UTF8,
      VehicleData::name,
      BlockPos.STREAM_CODEC,
      VehicleData::desiredPos,
      ByteBufCodecs.STRING_UTF8,
      VehicleData::desiredPosName,
      VehicleData::new
   );

   public VehicleData withStates(List<VehicleData.StateWithCount> states) {
      return new VehicleData(states, this.name, this.desiredPos, this.desiredPosName);
   }

   public VehicleData withName(String name) {
      return new VehicleData(this.states, name, this.desiredPos, this.desiredPosName);
   }

   public VehicleData withDesiredPos(BlockPos desiredPos) {
      return new VehicleData(this.states, this.name, desiredPos, this.desiredPosName);
   }

   public VehicleData withDesiredPosName(String desiredPosName) {
      return new VehicleData(this.states, this.name, this.desiredPos, desiredPosName);
   }

   public record StateWithCount(BlockState state, List<Integer> positions) {
   }
}
