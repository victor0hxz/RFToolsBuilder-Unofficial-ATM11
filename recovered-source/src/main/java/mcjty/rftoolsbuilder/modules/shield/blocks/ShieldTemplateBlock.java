package mcjty.rftoolsbuilder.modules.shield.blocks;

import java.util.function.Consumer;
import javax.annotation.Nonnull;
import mcjty.lib.builder.InfoLine;
import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.setup.RegistrationContext;
import mcjty.lib.tooltips.ITooltipSettings;
import mcjty.lib.varia.Tools;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.neoforged.neoforge.common.util.Lazy;

public class ShieldTemplateBlock extends Block implements ITooltipSettings {
   private final Lazy<TooltipBuilder> tooltipBuilder = Lazy.of(
      () -> new TooltipBuilder()
         .info(new InfoLine[]{TooltipBuilder.key("message.rftoolsbuilder.shiftmessage")})
         .infoShift(new InfoLine[]{TooltipBuilder.header()})
   );
   private final ShieldTemplateBlock.TemplateColor color;

   public ShieldTemplateBlock(ShieldTemplateBlock.TemplateColor color) {
      super(RegistrationContext.prepareBlockProperties(Properties.of().noOcclusion().sound(SoundType.GLASS)));
      this.color = color;
   }

   public ShieldTemplateBlock.TemplateColor getColor() {
      return this.color;
   }

   public void appendHoverText(
      @Nonnull ItemStack stack, TooltipContext context, TooltipDisplay display, @Nonnull Consumer<Component> tooltip, @Nonnull TooltipFlag flagIn
   ) {
      ((TooltipBuilder)this.tooltipBuilder.get()).makeTooltip(Tools.getId(this), stack, tooltip, flagIn);
   }

   public static enum TemplateColor {
      BLUE,
      RED,
      GREEN,
      YELLOW;
   }
}
