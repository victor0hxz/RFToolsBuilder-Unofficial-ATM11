package mcjty.rftoolsbuilder.modules.shield;

import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.neoforge.common.ModConfigSpec.Builder;
import net.neoforged.neoforge.common.ModConfigSpec.DoubleValue;
import net.neoforged.neoforge.common.ModConfigSpec.IntValue;

public class ShieldConfiguration {
   public static final String CATEGORY_SHIELD = "shield";
   public static IntValue MAXENERGY;
   public static IntValue RECEIVEPERTICK;
   public static IntValue rfBase;
   public static IntValue rfCamo;
   public static IntValue rfShield;
   public static IntValue rfDamage;
   public static IntValue rfDamagePlayer;
   public static DoubleValue damage;
   public static IntValue maxShieldSize;
   public static IntValue maxShieldOffset;
   public static IntValue maxShieldDimension;
   public static IntValue maxDisjointShieldDistance;
   public static IntValue shardsPerLootingKill;
   public static IntValue lootingKillBonus;
   public static BooleanValue allowInvisibleShield;

   public static void init(Builder SERVER_BUILDER, Builder CLIENT_BUILDER) {
      SERVER_BUILDER.comment("Settings for the shield system").push("shield");
      CLIENT_BUILDER.comment("Settings for the shield system").push("shield");
      MAXENERGY = SERVER_BUILDER.comment("Maximum RF storage that the shield block can hold").defineInRange("shieldMaxRF", 200000, 0, Integer.MAX_VALUE);
      RECEIVEPERTICK = SERVER_BUILDER.comment("RF per tick that the shield block can receive").defineInRange("shieldRFPerTick", 5000, 0, Integer.MAX_VALUE);
      maxShieldSize = SERVER_BUILDER.comment("Maximum size (in blocks) of a tier 1 shield").defineInRange("shieldMaxSize", 256, 0, 1000000);
      maxShieldOffset = SERVER_BUILDER.comment("Maximum offset of the shape when a shape card is used").defineInRange("maxShieldOffset", 128, 0, 100000);
      maxShieldDimension = SERVER_BUILDER.comment("Maximum dimension of the shape when a shape card is used")
         .defineInRange("maxShieldDimension", 256, 0, 1000000);
      maxDisjointShieldDistance = SERVER_BUILDER.comment("Maximum distance at which you can add disjoint shield sections to a composed shield")
         .defineInRange("maxDisjointShieldDistance", 64, 0, 10000);
      rfBase = SERVER_BUILDER.comment("Base amount of RF/tick for every 10 blocks in the shield (while active)")
         .defineInRange("shieldRfBase", 8, 0, Integer.MAX_VALUE);
      rfCamo = SERVER_BUILDER.comment("RF/tick for every 10 blocks added in case of camo mode").defineInRange("shieldRfCamo", 2, 0, Integer.MAX_VALUE);
      rfShield = SERVER_BUILDER.comment("RF/tick for every 10 block addeds in case of shield mode").defineInRange("shieldRfShield", 2, 0, Integer.MAX_VALUE);
      rfDamage = SERVER_BUILDER.comment("The amount of RF to consume for a single spike of damage for one entity")
         .defineInRange("shieldRfDamage", 1000, 0, Integer.MAX_VALUE);
      rfDamagePlayer = SERVER_BUILDER.comment("The amount of RF to consume for a single spike of damage for one entity (used in case of player-type damage)")
         .defineInRange("shieldRfDamagePlayer", 2000, 0, Integer.MAX_VALUE);
      damage = SERVER_BUILDER.comment("The amount of damage to do for a single spike on one entity").defineInRange("shieldDamage", 5.0, 0.0, 1.0E9);
      allowInvisibleShield = SERVER_BUILDER.comment("Set this to false if you don't want invisible shield rendering mode to be possible")
         .define("allowInvisibleShield", true);
      shardsPerLootingKill = SERVER_BUILDER.comment("Amount of dimensional shards per looting kill. Remember that this is per block that does damage")
         .defineInRange("shardsPerLootingKill", 2, 0, 256);
      lootingKillBonus = SERVER_BUILDER.comment("The looting kill bonus").defineInRange("lootingKillBonus", 3, 0, 256);
      SERVER_BUILDER.pop();
      CLIENT_BUILDER.pop();
   }
}
