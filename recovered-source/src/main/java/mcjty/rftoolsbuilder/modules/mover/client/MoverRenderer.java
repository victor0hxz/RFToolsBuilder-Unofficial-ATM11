package mcjty.rftoolsbuilder.modules.mover.client;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.function.BiFunction;
import mcjty.rftoolsbuilder.modules.mover.blocks.MoverTileEntity;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class MoverRenderer {
   public static final int LINES_SUPPORTED = 9;
   public static final Identifier BLACK = Identifier.fromNamespaceAndPath("rftoolsbuilder", "block/effects/black");

   private MoverRenderer() {
   }

   public static void addPreRender(BlockPos pos, Runnable renderer, BiFunction<Level, BlockPos, Boolean> validator) {
   }

   public static float getPartialTicks() {
      return 0.0F;
   }

   public static void delayedRenderer(MoverTileEntity mover, ItemStack vehicle, PoseStack poseStack, Vec3 cameraVec, RenderType renderType) {
   }
}
