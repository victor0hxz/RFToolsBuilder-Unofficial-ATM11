package mcjty.rftoolsbuilder.shapes;

import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.ToggleButton;
import mcjty.lib.gui.widgets.Widget;
import net.minecraft.client.gui.screens.Screen;

public class ShapeGuiTools {
   public static ToggleButton createAxisButton(Screen gui, Panel toplevel, int x, int y) {
      ToggleButton showAxis = (ToggleButton)((ToggleButton)((ToggleButton)new ToggleButton()
               .checkMarker(true)
               .tooltips(new String[]{"Enable axis rendering", "in the preview"}))
            .text("A"))
         .hint(x, y, 24, 16);
      showAxis.pressed(true);
      toplevel.children(new Widget[]{showAxis});
      return showAxis;
   }

   public static ToggleButton createBoxButton(Screen gui, Panel toplevel, int x, int y) {
      ToggleButton showAxis = (ToggleButton)((ToggleButton)((ToggleButton)new ToggleButton()
               .checkMarker(true)
               .tooltips(new String[]{"Enable preview of the", "outer bounds"}))
            .text("B"))
         .hint(x, y, 24, 16);
      showAxis.pressed(true);
      toplevel.children(new Widget[]{showAxis});
      return showAxis;
   }

   public static ToggleButton createScanButton(Screen gui, Panel toplevel, int x, int y) {
      ToggleButton showAxis = (ToggleButton)((ToggleButton)((ToggleButton)new ToggleButton()
               .checkMarker(true)
               .tooltips(new String[]{"Show a visual scanline", "wherever the preview", "is updated"}))
            .text("S"))
         .hint(x, y, 24, 16);
      showAxis.pressed(true);
      toplevel.children(new Widget[]{showAxis});
      return showAxis;
   }
}
