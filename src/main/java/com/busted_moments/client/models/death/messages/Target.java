package com.busted_moments.client.models.death.messages;

import com.wynntils.core.text.StyledText;

import java.util.Optional;

public record Target(String username, Optional<String> nickname, StyledText displayName) {

}
