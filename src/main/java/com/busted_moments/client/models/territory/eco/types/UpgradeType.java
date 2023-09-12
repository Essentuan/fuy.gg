package com.busted_moments.client.models.territory.eco.types;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.Map;

public enum UpgradeType {
   DAMAGE(
           "Damage",
           "Increases the damage the tower does",
           "Damage: +%s%",
           Items.IRON_SWORD,
           ResourceType.ORE,
           new Level[]{
                   new Level(0L, 0D),
                   new Level(100L, 40D),
                   new Level(300L, 80D),
                   new Level(600L, 120D),
                   new Level(1200L, 160D),
                   new Level(2400L, 200D),
                   new Level(4800L, 240D),
                   new Level(8400L, 280D),
                   new Level(12000L, 320D),
                   new Level(15600L, 360D),
                   new Level(19200L, 400D),
                   new Level(22800L, 440D)
           }
   ),
   ATTACK(
           "Attack Speed",
           "Increases the rate the tower does an attack",
           "Attacks per Second: +%s%",
           Items.RABBIT_HIDE,
           ResourceType.CROP,
           new Level[]{
                   new Level(0L, 0D),
                   new Level(100L, 50D),
                   new Level(300L, 100D),
                   new Level(600L, 150D),
                   new Level(1200L, 220D),
                   new Level(2400L, 300D),
                   new Level(4800L, 400D),
                   new Level(8400L, 500D),
                   new Level(12000L, 620D),
                   new Level(15600L, 660D),
                   new Level(19200L, 740D),
                   new Level(22800L, 840D)
           }
   ),
   HEALTH(
           "Health",
           "Increases the health the tower has",
           "Health: +%s%",
           Items.FERMENTED_SPIDER_EYE,
           ResourceType.WOOD,
           new Level[]{
                   new Level(0L, 0D),
                   new Level(100L, 50D),
                   new Level(300L, 100D),
                   new Level(600L, 150D),
                   new Level(1200L, 220D),
                   new Level(2400L, 300D),
                   new Level(4800L, 400D),
                   new Level(8400L, 520D),
                   new Level(12000L, 640D),
                   new Level(15600L, 760D),
                   new Level(19200L, 880D),
                   new Level(22800L, 1000D)
           }
   ),
   DEFENCE(
           "Defence",
           "Increases the defense the tower has",
           "Defence: +%s%",
           Items.SHIELD,
           ResourceType.FISH,
           new Level[]{
                   new Level(0L, 0D),
                   new Level(100L, 300D),
                   new Level(300L, 450D),
                   new Level(600L, 525D),
                   new Level(1200L, 600D),
                   new Level(2400L, 650D),
                   new Level(4800L, 690D),
                   new Level(8400L, 720D),
                   new Level(12000L, 740D),
                   new Level(15600L, 760D),
                   new Level(19200L, 780D),
                   new Level(22800L, 800D)
           }
   ),
   STRONGER_MINIONS(
           "Stronger Minions",
           "Buffs the minions that spawn when your territory is attacked",
           "Minion Damage: +%s%",
           Items.SKELETON_SKULL,
           ResourceType.WOOD,
           new Level[]{
                   new Level(0L, 0D),
                   new Level(200L, 150D),
                   new Level(400L, 200D),
                   new Level(800L, 250D),
                   new Level(1600L, 300D)
           }
   ),
   MULTI_ATTACK(
           "Tower Multi-Attacks",
           "Increases the number of players your Guild Tower can attack at once",
           "Max Targets: %s",
           Items.ARROW,
           ResourceType.FISH,
           new Level[]{
                   new Level(0L, 1D),
                   new Level(4800L, 2D)
           }
   ),
   TOWER_AURA(
           "Tower Aura",
           "Cast an outward-moving Aura from the Tower and damaging players between 100% and 200% of the Tower's damage.",
           "Frequency: %ss",
           Items.ENDER_PEARL,
           ResourceType.CROP,
           new Level[]{
                   new Level(0L, 0D),
                   new Level(800L, 24D),
                   new Level(1600L, 18D),
                   new Level(3200L, 12D)
           }
   ),
   TOWER_VOLLEY(
           "Tower Volley",
           "Cast a volley of fireballs from the Tower damaging players between 100% and 200% of the Tower's damage.",
           "Frequency: %ss",
           Items.FIRE_CHARGE,
           ResourceType.ORE,
           new Level[]{
                   new Level(0L, 0D),
                   new Level(200L, 20D),
                   new Level(400L, 15D),
                   new Level(800L, 10D)
           }
   ),
   GATHERING_EXPERIENCE(
           "Gathering Experience",
           "Guild members in this territory will gain bonus gathering XP",
           "Gathering XP: +%s%",
           Items.CARROT,
           ResourceType.WOOD,
           new Level[]{
                   new Level(0L, 0D),
                   new Level(600L, 10D),
                   new Level(1300L, 20D),
                   new Level(2000L, 30D),
                   new Level(2700L, 40D),
                   new Level(3400L, 50D),
                   new Level(5500L, 60D),
                   new Level(10000L, 80D),
                   new Level(20000L, 100D)
           }
   ),
   MOB_EXPERIENCE(
           "Mob Experience",
           "Guild members in this territory will receive more XP from mobs",
           "XP Bonus: +%s%",
           Items.SUNFLOWER,
           ResourceType.FISH,
           new Level[]{
                   new Level(0L, 0D),
                   new Level(600L, 10D),
                   new Level(1200L, 20D),
                   new Level(1800L, 30D),
                   new Level(2400L, 40D),
                   new Level(3000L, 50D),
                   new Level(5000L, 60D),
                   new Level(10000L, 80D),
                   new Level(20000L, 100D)
           }
   ),
   MOB_DAMAGE(
           "Mob Damage",
           "Guild members in this territory will deal more damage to mobs",
           "Damage Bonus: +%s%",
           Items.STONE_SWORD,
           ResourceType.CROP,
           new Level[]{
                   new Level(0L, 0D),
                   new Level(600L, 10D),
                   new Level(1200L, 20D),
                   new Level(1800L, 40D),
                   new Level(2400L, 60D),
                   new Level(3000L, 80D),
                   new Level(5000L, 120D),
                   new Level(10000L, 160D),
                   new Level(20000L, 200D)
           }
   ),
   PVP_DAMAGE(
           "PvP Damage",
           "Guild members in this territory will deal more damage to players",
           "Damage Bonus: +%s%",
           Items.GOLDEN_SWORD,
           ResourceType.ORE,
           new Level[]{
                   new Level(0L, 0D),
                   new Level(600L, 5D),
                   new Level(1200L, 10D),
                   new Level(1800L, 15D),
                   new Level(2400L, 20D),
                   new Level(3000L, 25D),
                   new Level(5000L, 40D),
                   new Level(10000L, 65D),
                   new Level(20000L, 80D)
           }
   ),
   XP_SEEKING(
           "XP Seeking",
           "Your guild will gain XP while holding this territory",
           "Guild XP: +%s/h",
           Items.GLOWSTONE_DUST,
           ResourceType.EMERALDS,
           new Level[]{
                   new Level(0L, 0D),
                   new Level(100L, 36000D),
                   new Level(200L, 66000D),
                   new Level(400L, 120000D),
                   new Level(800L, 228000D),
                   new Level(1600L, 456000D),
                   new Level(3200L, 900000D),
                   new Level(6400L, 1740000D),
                   new Level(9600L, 2580000D),
                   new Level(12800L, 3360000D)
           }
   ),
   TOME_SEEKING(
           "Tome Seeking",
           "Your guild will have a chance to find exclusive tomes while holding this territory",
           "Drop Chance: %s%/h",
           Items.ENCHANTED_BOOK,
           ResourceType.FISH,
           new Level[]{
                   new Level(0L, 0D),
                   new Level(400L, 0.15D),
                   new Level(3200L, 1.2D),
                   new Level(6400L, 2.4D)
           }
   ),
   EMERALD_SEEKING(
           "Emerald Seeking",
           "Your guild will have a chance to find emeralds while holding this territory",
           "Drop Chance: %s%/h",
           Items.EMERALD_ORE,
           ResourceType.WOOD,
           new Level[]{
                   new Level(0L, 0D),
                   new Level(200L, 0.3D),
                   new Level(800L, 3D),
                   new Level(1600L, 6D),
                   new Level(3200L, 12D),
                   new Level(6400L, 24D)
           }
   ),
   RESOURCE_STORAGE(
           "Larger Resource Storage",
           "Increases the storage limit for resources in this territory",
           "Storage Bonus: +%s%",
           Items.BREAD,
           ResourceType.EMERALDS,
           new Level[]{
                   new Level(0L, 0D),
                   new Level(400L, 100D),
                   new Level(800L, 300D),
                   new Level(2000L, 700D),
                   new Level(5000L, 1400D),
                   new Level(16000L, 3300D),
                   new Level(48000L, 7900D)
           }
   ),
   EMERALD_STORAGE(
           "Larger Emerald Storage",
           "Increases the storage limit for emeralds in this territory",
           "Storage Bonus: +%s%",
           Items.EMERALD_BLOCK,
           ResourceType.WOOD,
           new Level[]{
                   new Level(0L, 0D),
                   new Level(200L, 100D),
                   new Level(400L, 300D),
                   new Level(1000L, 700D),
                   new Level(2500L, 1400D),
                   new Level(8000L, 3300D),
                   new Level(24000L, 7900D)
           }
   ),
   EFFICIENT_RESOURCES(
           "Efficient Resources",
           "Increases the amount of resources this territory will produce",
           "Gathering Bonus: +%s%",
           Items.GOLDEN_PICKAXE,
           ResourceType.EMERALDS,
           new Level[]{
                   new Level(0L, 0D),
                   new Level(6000L, 50D),
                   new Level(12000L, 100D),
                   new Level(24000L, 150D),
                   new Level(48000L, 200D),
                   new Level(96000L, 250D),
                   new Level(192000L, 300D)
           }
   ),
   RESOURCE_RATE(
           "Resource Rate",
           "Decreases the time needed to produce resources on this territory",
           "Gathering Rate: %ss",
           Items.MUSHROOM_STEM,
           ResourceType.EMERALDS,
           new Level[]{
                   new Level(0L, 4D),
                   new Level(6000L, 3D),
                   new Level(18000L, 2D),
                   new Level(32000L, 1D)
           }
   ),
   EFFICIENT_EMERALDS(
           "Efficient Emeralds",
           "Increases the amount of emeralds this territory will produce",
           "Emerald Bonus: +%s%",
           Items.EMERALD,
           ResourceType.ORE,
           new Level[]{
                   new Level(0L, 0D),
                   new Level(2000L, 35D),
                   new Level(8000L, 100D),
                   new Level(32000L, 300D)
           }
   ),
   EMERALD_RATE(
           "Emerald Rate",
           "Decreases the time needed to produce emeralds on this territory",
           "Gather Rate: %ss",
           Items.EXPERIENCE_BOTTLE,
           ResourceType.CROP,
           new Level[]{
                   new Level(0L, 4D),
                   new Level(2000L, 3D),
                   new Level(8000L, 2D),
                   new Level(32000L, 1D)
           }
   );

   private final String name;
   private final String description;
   private final String label;
   private final ItemStack icon;

   private final ResourceType resourceType;
   private final Level[] levels;

   private static final Map<String, UpgradeType> UPGRADES = new HashMap<>();

   UpgradeType(String name, String description, String label, Item icon, ResourceType resourceType, Level[] levels) {
      this(
              name,
              description,
              label,
              new ItemStack(icon),
              resourceType,
              levels
      );
   }

   UpgradeType(String name, String description, String label, ItemStack icon, ResourceType resourceType, Level[] levels) {
      this.name = name;
      this.description = description;
      this.label = label;
      this.icon = icon;
      this.resourceType = resourceType;
      this.levels = levels;
   }

   static {
      for (UpgradeType upgrade : values()) {
         UPGRADES.put(upgrade.name.toLowerCase(), upgrade);
         UPGRADES.put(upgrade.toString().toLowerCase(), upgrade);
      }
   }

   public String getName() {
      return name;
   }

   public String getDescription() {
      return description;
   }

   public String getLabelFormat() {
      return label;
   }

   public ItemStack getIcon() {
      return icon;
   }

   public ResourceType getResourceType() {
      return resourceType;
   }

   public int size() {
      return levels.length;
   }

   public Level getLevel(int i) {
      return levels[i];
   }

   public static UpgradeType get(String name) {
      return UPGRADES.get(name.toLowerCase());
   }

   public record Level(long cost, double bonus) {};
}
