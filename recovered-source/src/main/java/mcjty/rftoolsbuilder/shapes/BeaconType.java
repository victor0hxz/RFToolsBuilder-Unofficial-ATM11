package mcjty.rftoolsbuilder.shapes;

import java.util.HashMap;
import java.util.Map;

public enum BeaconType {
   BEACON_OFF(0.1F, 0.1F, 0.1F, "off"),
   BEACON_GREEN(0.0F, 1.0F, 0.0F, "green"),
   BEACON_RED(1.0F, 0.0F, 0.0F, "red"),
   BEACON_CYAN(0.0F, 1.0F, 1.0F, "cyan"),
   BEACON_BLUE(0.0F, 0.0F, 1.0F, "blue"),
   BEACON_YELLOW(1.0F, 1.0F, 0.0F, "yellow"),
   BEACON_PURPLE(1.0F, 0.0F, 1.0F, "purple"),
   BEACON_WHITE(1.0F, 1.0F, 1.0F, "white");

   public static final BeaconType[] VALUES = new BeaconType[values().length];
   private static final Map<String, BeaconType> TYPE_BY_CODE = new HashMap<>();
   private final String code;
   private final float r;
   private final float g;
   private final float b;
   private final int color;

   private BeaconType(float r, float g, float b, String code) {
      this.code = code;
      this.b = b;
      this.g = g;
      this.r = r;
      this.color = ((int)(r * 255.0) << 16) + ((int)(g * 255.0) << 8) + (int)(b * 255.0);
   }

   public String getCode() {
      return this.code;
   }

   public int getColor() {
      return this.color;
   }

   public float getR() {
      return this.r;
   }

   public float getG() {
      return this.g;
   }

   public float getB() {
      return this.b;
   }

   public static BeaconType getTypeByCode(String code) {
      if (code != null && !code.isEmpty()) {
         BeaconType type = TYPE_BY_CODE.get(code);
         return type == null ? BEACON_OFF : type;
      } else {
         return BEACON_OFF;
      }
   }

   static {
      for (int i = 0; i < values().length; i++) {
         VALUES[i] = values()[i];
         TYPE_BY_CODE.put(VALUES[i].getCode(), VALUES[i]);
      }
   }
}
