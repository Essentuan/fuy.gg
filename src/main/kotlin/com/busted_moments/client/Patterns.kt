package com.busted_moments.client

import java.util.regex.Pattern

object Patterns {
    val PRODUCTION_PATTERN: Pattern = Pattern.compile("(?:[ⒷⒸⓀⒿ] )?\\+(?<amount>[0-9]*) (?<resource>Emeralds|Ore|Wood|Fish|Crops) per Hour");
    val UPGRADE_PATTERN: Pattern = Pattern.compile("- (?<upgrade>.+) \\[Lv. (?<level>.+)\\]")
    val STORAGE_PATTERN: Pattern = Pattern.compile("((?<type>[ⒷⒸⓀⒿ]) )?(?<stored>[0-9]+)\\/(?<capacity>[0-9]+) stored")
    val TREASURY_PATTERN: Pattern = Pattern.compile(". Treasury Bonus: (?<treasury>.+)%")
    val GUILD_MANAGE_MENU: Pattern = Pattern.compile("^(?<guild>.+): Manage$")

    val TERRITORY_MENU_PATTERN: Pattern = Pattern.compile("^(?<guild>.+): Territories$")
    val SELECT_TERRITORIES_MENU: Pattern = Pattern.compile("Select Territories")
}