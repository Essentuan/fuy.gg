package com.busted_moments.client.features.raids;


import com.busted_moments.client.models.raids.Raid;
import com.busted_moments.client.models.raids.events.RaidEvent;
import com.busted_moments.client.util.ChatUtil;
import com.busted_moments.core.Default;
import com.busted_moments.core.Feature;
import com.busted_moments.core.State;
import com.busted_moments.core.config.Config;
import com.busted_moments.core.text.TextBuilder;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static net.minecraft.ChatFormatting.*;

@Default(State.ENABLED)
@Config.Category("Raids")
@Feature.Definition(name = "Print completion times")
public class PrintTimesFeature extends Feature {
   @SubscribeEvent
   public void onRaidComplete(RaidEvent.Complete event) {
      TextBuilder builder = Raid.format(event.getRaid());
      builder.appendIf(event::isPB, "\n\nNEW PERSONAL BEST!", YELLOW, UNDERLINE, BOLD);

      ChatUtil.send(builder);
   }
}
