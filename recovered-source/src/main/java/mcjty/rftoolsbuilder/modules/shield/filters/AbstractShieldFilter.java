package mcjty.rftoolsbuilder.modules.shield.filters;

import mcjty.lib.varia.Logging;

public abstract class AbstractShieldFilter<T extends ShieldFilter<?>> implements ShieldFilter<T> {
   private int action = 0;

   public AbstractShieldFilter(int action) {
      this.action = action;
   }

   @Override
   public int getAction() {
      return this.action;
   }

   @Override
   public T setAction(int action) {
      this.action = action;
      return (T)this;
   }

   public static ShieldFilter<?> createFilter(String type) {
      ShieldFilter<?> filter;
      if ("animal".equals(type)) {
         filter = new AnimalFilter(0);
      } else if ("hostile".equals(type)) {
         filter = new HostileFilter(0);
      } else if ("player".equals(type)) {
         filter = new PlayerFilter("", 0);
      } else if ("item".equals(type)) {
         filter = new ItemFilter(0);
      } else if ("default".equals(type)) {
         filter = new DefaultFilter(0);
      } else {
         Logging.log("Unknown filter type = " + type);
         filter = new DefaultFilter(0);
      }

      return filter;
   }
}
