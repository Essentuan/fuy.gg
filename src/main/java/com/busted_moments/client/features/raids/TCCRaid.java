package com.busted_moments.client.features.raids;

import com.busted_moments.core.Default;
import com.busted_moments.core.Feature;
import com.busted_moments.core.State;
import com.busted_moments.core.config.Config;

@Config.Category("Raids")
@Default(State.ENABLED)
@Feature.Definition(name = "The Canyon Colossus", description = "")
public class TCCRaid extends Feature {
    @Hidden("tcc personal best")
    private static double TCCPB = 999.99;

}
