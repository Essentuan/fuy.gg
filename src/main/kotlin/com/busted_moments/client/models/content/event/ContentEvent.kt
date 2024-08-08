package com.busted_moments.client.models.content.event

import com.busted_moments.client.models.content.ContentTimer
import com.busted_moments.client.models.content.Stage
import net.neoforged.bus.api.Event

abstract class ContentEvent(
    val timer: ContentTimer
) : Event() {
    class Enter(
        tracker: ContentTimer
    ) : ContentEvent(tracker)
    
    class StageStart(
        tracker: ContentTimer,
        val stage: Stage
    ) : ContentEvent(tracker)
    
    class StageEnd(
        tracker: ContentTimer,
        val stage: Stage
    ) : ContentEvent(tracker)
    
    class Finish(
        tracker : ContentTimer
    ) : ContentEvent(tracker)
    
    class Fail(
        tracker: ContentTimer
    ) : ContentEvent(tracker)
}