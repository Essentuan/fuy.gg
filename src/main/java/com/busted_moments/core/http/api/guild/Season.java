package com.busted_moments.core.http.api.guild;

import java.util.List;

public interface Season extends List<Season.Entry> {
   interface Entry extends GuildType {
      long rating();
      int territories();
   }
}
