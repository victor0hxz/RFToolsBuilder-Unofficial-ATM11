package mcjty.rftoolsbuilder.modules.mover.client;

import java.util.List;
import java.util.function.Function;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.Widget;
import mcjty.lib.gui.widgets.WidgetList;

public class SyncedList<T> {
   private final WidgetList list;
   private List<T> fromServerList = null;
   private boolean needsRefresh = true;
   private int listDirty = 10;
   private final Runnable listRequester;
   private final Function<T, Panel> panelCreator;
   private final int refreshRate;

   public SyncedList(WidgetList list, Runnable listRequester, Function<T, Panel> panelCreator, int refreshRate) {
      this.list = list;
      this.listRequester = listRequester;
      this.panelCreator = panelCreator;
      this.refreshRate = refreshRate;
   }

   public void setFromServerList(List<T> fromServerList) {
      this.fromServerList = fromServerList;
   }

   private void requestListIfNeeded() {
      if (this.refreshRate > 0 || this.fromServerList == null) {
         this.listDirty--;
         if (this.listDirty <= 0) {
            this.listRequester.run();
            this.listDirty = this.refreshRate;
         }
      }
   }

   public void refresh() {
      this.fromServerList = null;
      this.needsRefresh = true;
      this.listDirty = 3;
      this.requestListIfNeeded();
   }

   private boolean listReady() {
      return this.fromServerList != null;
   }

   public WidgetList getList() {
      return this.list;
   }

   public T getSelected() {
      int idx = this.list.getSelected();
      return (T)(idx >= 0 && idx < this.list.getChildCount() ? this.list.getChild(idx).getUserObject() : null);
   }

   public void select(T value) {
      for (int i = 0; i < this.list.getChildCount(); i++) {
         if (this.list.getChild(i).getUserObject().equals(value)) {
            this.list.selected(i);
            return;
         }
      }

      this.list.selected(-1);
   }

   public void populateLists() {
      this.requestListIfNeeded();
      if (this.listReady()) {
         if (this.needsRefresh) {
            this.needsRefresh = false;
            this.list.removeChildren();
            int sel = this.list.getSelected();

            for (T vehicle : this.fromServerList) {
               Panel panel = this.panelCreator.apply(vehicle);
               panel.userObject(vehicle);
               this.list.children(new Widget[]{panel});
            }

            this.list.selected(sel);
         }
      }
   }
}
