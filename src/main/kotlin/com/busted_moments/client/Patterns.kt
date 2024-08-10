package com.busted_moments.client

import java.util.regex.Pattern

object Patterns {
    //Level Up
    val LEVEL_UP: Pattern = Pattern.compile("^\\[!] Congratulations to (?<player>.+) for reaching (?<type>.+) level (?<level>.+)!")
    val PROF_LEVEL_UP: Pattern = Pattern.compile("\\[!] Congratulations to (?<player>.+) for reaching level (?<level>.+) in (. )?(?<type>.+)!")

    //Territory Menu
    val PRODUCTION: Pattern = Pattern.compile("(?:[ⒷⒸⓀⒿ] )?\\+(?<amount>[0-9]*) (?<resource>Emeralds|Ore|Wood|Fish|Crops) per Hour");
    val UPGRADE: Pattern = Pattern.compile("- (?<upgrade>.+) \\[Lv. (?<level>.+)]")
    val STORAGE: Pattern = Pattern.compile("((?<type>[ⒷⒸⓀⒿ]) )?(?<stored>[0-9]+)/(?<capacity>[0-9]+) stored")
    val TREASURY: Pattern = Pattern.compile(". Treasury Bonus: (?<treasury>.+)%")
    val GUILD_MANAGE_MENU_TITLE: Pattern = Pattern.compile("^(?<guild>.+): Manage$")

    val TERRITORY_MENU_TITLE: Pattern = Pattern.compile("^(?<guild>.+): Territories$")
    val SELECT_TERRITORIES_MENU_TITLE: Pattern = Pattern.compile("Select Territories")

    //Timers
    val ATTACK_SCREEN_TITLE: Pattern = Pattern.compile("Attacking: (?<territory>.+)")
    val TIMER_START: Pattern = Pattern.compile("^§cThe war for (?<territory>.+) will start in (?<timer>.+).$")
    val TERRITORY_DEFENSE: Pattern = Pattern.compile("^§3.+§b (?<territory>.+) defense is (?<defense>.+)")

    //War
    val TOWER_STATS: Pattern =
        Pattern.compile("\\[(?<guild>.+)] (?<territory>.+) Tower - . (?<health>.+) \\((?<defense>.+)%\\) - .{1,2} (?<damagemin>.+)-(?<damagemax>.+) \\((?<attackspeed>.+)x\\)")

    //Territories
    val TERRITORY_CAPTURED: Pattern = Pattern.compile("^§c\\[(?<guild>.+)] captured the territory (?<territory>.+)\\.")
    val TERRITORY_CONTROL: Pattern = Pattern.compile("^§c\\[(?<guild>.+)] has taken control of (?<territory>.+)!")
    val WAR_SUCCESS = Pattern.compile("^§cYou have taken control of (?<territory>.+) from \\[(?<guild>.+)]!.*")

    //Legendary Island
    val LI_REWARD: Pattern = Pattern.compile(" *\\+(?<amount>[0-9]*) (?<type>((Bronze|Silver|Gold|Diamond) Token(s?))|Experience Points)")

    //Dungeons
    val DUNGEON_COMPLETION: Pattern = Pattern.compile("^Great job! You've completed the (.*) Dungeon!")
    val DUNGEON_REWARD: Pattern = Pattern.compile("^\\[\\+(?<reward>.*)]")

    //Raids
    val RAID_COMPLETION: Pattern = Pattern.compile("^Raid Completed!")
    val RAID_FAIL: Pattern = Pattern.compile("^Raid Failed!")
    val HOVER_FOR_MORE: Pattern = Pattern.compile("^Hover for more")
    val TIME_ELAPSED: Pattern = Pattern.compile("^Time Elapsed:.+")

    //Lootruns
    val REWARD_PULLS: Pattern = Pattern.compile("You have (?<pulls>\\d*) rewards to pull");
}