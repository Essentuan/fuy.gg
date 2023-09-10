package com.busted_moments.client.models.territory.eco;

import java.util.regex.Pattern;

public interface Patterns {
   Pattern PRODUCITON_PATTERN = Pattern.compile("(. )?(?<sign>[-+])(?<production>.+) (?<resource>.+) per Hour");
   Pattern GUILD_MANAGE_MENU = Pattern.compile("^(?<guild>.+): Manage$");
}
