package mcjty.rftoolsbuilder.modules.mover.sound;

import mcjty.lib.varia.SafeClientTools;
import mcjty.rftoolsbuilder.modules.mover.MoverModule;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance.Attenuation;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;

public class MoverSound extends AbstractTickableSoundInstance {
   private final Level world;
   private final SoundEvent sound;
   private final BlockPos pos;

   public MoverSound(SoundEvent event, Level world, BlockPos pos) {
      super(event, SoundSource.BLOCKS, world.getRandom());
      this.world = world;
      this.pos = pos;
      this.x = pos.getX();
      this.y = pos.getY();
      this.z = pos.getZ();
      this.attenuation = Attenuation.LINEAR;
      this.looping = true;
      this.volume = 1.0F;
      this.delay = 0;
      this.sound = event;
      this.relative = false;
   }

   private static double distToCenterSqr(double x1, double y1, double z1, double x2, double y2, double z2) {
      double d0 = x2 - x1;
      double d1 = y2 - y1;
      double d2 = x2 - z1;
      return d0 * d0 + d1 * d1 + d2 * d2;
   }

   public void tick() {
      Block block = this.world.getBlockState(this.pos).getBlock();
      if (block != MoverModule.MOVER.block().get()) {
         this.stop();
      } else {
         Player player = SafeClientTools.getClientPlayer();
         double distance = Math.sqrt(distToCenterSqr(this.x, this.y, this.z, player.getX(), player.getY(), player.getZ()));
         if (distance > 40.0) {
            this.volume = 0.0F;
         } else {
            this.volume = (float)(1.0 * (40.0 - distance) / 40.0);
         }
      }
   }

   protected boolean isSoundType(SoundEvent event) {
      return this.sound == event;
   }

   public void setPosition(Vec3 pos) {
      this.x = pos.x;
      this.y = pos.y;
      this.z = pos.z;
   }
}
