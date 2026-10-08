package mcjty.rftoolsbuilder.modules.builder.items;

import mcjty.lib.setup.RegistrationContext;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.Item.Properties;

public class SuperHarvestingTool extends Item {
   public SuperHarvestingTool() {
      super(
         ToolMaterial.NETHERITE
            .applyToolProperties(RegistrationContext.prepareItemProperties(new Properties()), BlockTags.MINEABLE_WITH_PICKAXE, 1.0F, -2.8F, 0.0F)
      );
   }
}
