package com.busted_moments.client.features.raids;

import com.busted_moments.core.Default;
import com.busted_moments.core.Feature;
import com.busted_moments.core.State;
import com.busted_moments.core.config.Config;

@Config.Category("Raids")
@Default(State.ENABLED)
@Feature.Definition(name = "The Nameless Anomaly", description = "")
public class TNARaid extends Feature {
    @Hidden("TNA personal best")
    private static double TNAPB = 999.99;

}
