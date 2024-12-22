package com.busted_moments.client

import java.util.regex.Pattern

object Patterns {
    //Level Up
    val LEVEL_UP: Pattern =
        Pattern.compile("^\\[!] Congratulations to (?<player>.+) for reaching (?<type>.+) level (?<level>.+)!")
    val PROF_LEVEL_UP: Pattern =
        Pattern.compile("\\[!] Congratulations to (?<player>.+) for reaching level (?<level>.+) in (. )?(?<type>.+)!")

    //Territory Menu
    val PRODUCTION: Pattern =
        Pattern.compile("(?:[ⒷⒸⓀⒿ] )?\\+(?<amount>[0-9]*) (?<resource>Emeralds|Ore|Wood|Fish|Crops) per Hour");
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

    //Players
    val PLAYER_LIST_DELIMITER: Pattern = Pattern.compile(", (and )?")

    //Legendary Island
    val LI_REWARD: Pattern =
        Pattern.compile(" *\\+(?<amount>[0-9]*) (?<type>((Bronze|Silver|Gold|Diamond) Token(s?))|Experience Points)")

    //Dungeons
    val DUNGEON_COMPLETION: Pattern = Pattern.compile("^Great job! You've completed the (.*) Dungeon!")
    val DUNGEON_REWARD: Pattern = Pattern.compile("^\\[\\+(?<reward>.*)]")

    //Raids
    val RAID_COMPLETION: Pattern = Pattern.compile("^Raid Completed!")
    val RAID_FAIL: Pattern = Pattern.compile("^Raid Failed!")
    val RAID_STATISTICS_END: Pattern = Pattern.compile(".*Combat Experience.*")
    val TIME_ELAPSED: Pattern = Pattern.compile("^Time Elapsed:.+")
    val GUILD_RAID: Pattern = Pattern.compile(
        "^((\uDAFF\uDFFC\uE001\uDB00\uDC06)|(\uDAFF\uDFFC\uE006\uDAFF\uDFFF\uE002\uDAFF\uDFFE))\\s*(?<players>[\\w ,]*|(and)) finished (?<raid>.*) and claimed (?<rewards>.*)"
    );

    //Lootruns
    val REWARD_PULLS: Pattern = Pattern.compile("You have (?<pulls>\\d*) rewards to pull");

    //Parties
    const val PARTY_PREFIX: String = "((?:\udaff\udffc\ue005\udaff\udfff\ue002\udaff\udffe|\udaff\udffc\ue001\udb00\udc06)\\s)?"
    val PARTY_LIST_ALL: Pattern = Pattern.compile("${PARTY_PREFIX}Party members: (?<players>.*)")
    val PARTY_LIST_FAILED: Pattern = Pattern.compile("${PARTY_PREFIX}You must be in a party to use this\\.")
    val PARTY_LEAVE_SELF: Pattern = Pattern.compile("${PARTY_PREFIX}You have left your current party")
    val PARTY_LEAVE_OTHER: Pattern = Pattern.compile("${PARTY_PREFIX}(?<player>[\\w ]*) has left the party\\.")
    val PARTY_LEAVE_KICK_OTHER: Pattern = Pattern.compile("${PARTY_PREFIX}(?<player>[\\w ]*) has been kicked from the party!")
    val PARTY_LEAVE_SELF_ALREADY_LEFT: Pattern = Pattern.compile("${PARTY_PREFIX}You must be in a party to use this\\.")
    val PARTY_LEAVE_KICK: Pattern = Pattern.compile("${PARTY_PREFIX}You have been removed from the party\\.")
    val PARTY_JOIN_OTHER: Pattern = Pattern.compile("${PARTY_PREFIX}(?<player>[\\w ]*) has joined your party, say hello!")
    val PARTY_JOIN_OTHER_SWITCH: Pattern = Pattern.compile("${PARTY_PREFIX}Say hello to (?<player>[\\w ]*) which just joined your party!")
    val PARTY_JOIN_SELF: Pattern = Pattern.compile("${PARTY_PREFIX}You have successfully joined the party\\.");
    val PARTY_PROMOTE_OTHER: Pattern = Pattern.compile("${PARTY_PREFIX}(?<player>[\\w ]*) is now the Party Leader!")
    val PARTY_PROMOTE_SELF: Pattern = Pattern.compile("${PARTY_PREFIX}You are now the leader of this party! Type /party for a list of commands\\.")
    val PARTY_DISBAND_ALL: Pattern = Pattern.compile("${PARTY_PREFIX}Your party has been disbanded")
    val PARTY_CREATE_SELF: Pattern = Pattern.compile("${PARTY_PREFIX}You have successfully created a party\\.")
}