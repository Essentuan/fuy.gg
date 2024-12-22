package com.busted_moments.client.models.content.types

import com.busted_moments.client.models.content.ContentTimer
import com.busted_moments.client.models.content.ContentType
import com.busted_moments.client.models.content.Stage
import com.busted_moments.client.models.content.Trigger
import com.busted_moments.client.models.content.stages.Stages
import com.busted_moments.client.models.content.triggers.Triggers
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3

object LegendaryIslandType : ContentType, List<Stage.Builder> by listOf(
    Stages.text {
        +"Bronze Division".gold.bold
    },
    Stages.generic(
        "The Mummyboard",
        Triggers.immediate()
    ),
    Stages.enters(
        "The Virus Doctor",
        Vec2(-5805f, -2998f),
        Vec2(-5954f, -3146f)
    ),
    Stages.enters(
        "Corkus Accipientis",
        Vec2(-6005f, -2999f),
        Vec2(-6154f, -3146f)
    ),
    Stages.text {
        +"Silver Division".white.bold
    },
    Stages.enters(
        "Matrojan Idol",
        Vec2(-6205f, -2998f),
        Vec2(-6354f, -3146f)
    ),
    Stages.enters(
        "Titanium R.A.T. R-4X",
        Vec2(-6800f, -3206f),
        Vec2(-6949f, -3354f)
    ),
    Stages.enters(
        "Death Metal",
        Vec2(-6600f, -2998f),
        Vec2(-6749f, -3146f)
    ),
    Stages.text {
        +"Gold Division".yellow.bold
    },
    Stages.enters(
        "Mechorrupter of Worlds",
        Vec2(-6800f, -2998f),
        Vec2(-6948f, -3146f)
    ),
    Stages.enters(
        "Robob's Reinvention",
        Vec2(-7000f, -2998f),
        Vec2(-7146f, -3146f)
    ),
    Stages.enters(
        "Orange Cybel",
        Vec2(-7198f, -2998f),
        Vec2(-7346f, -3146f)
    ),
    Stages.text {
        +"Diamond Division".aqua.bold
    },
    Stages.enters(
        "Doctor Legendary",
        Vec2(-7398f, -2998f),
        Vec2(-7545f, -3146f)
    )
) {
    private val completion = Triggers.enters(
        Vec2(-7452f, -2891f),
        Vec2(-7500f, -2939f)
    )

    private val failures: List<Trigger.Builder> = listOf(
        Triggers.death(),
        Triggers.entersSphere(
            Vec3(-1114.0, 66.0, -2407.0),
            15.0
        )
    )

    init {
        Triggers.enters(
            Vec2(-5605f, -2998f),
            Vec2(-5754f, -3146f)
        ).build {
            ContentTimer(this, this, completion, failures, ContentTimer.Modifiers.Empty).start()
        }
    }

    override val id: String
        get() = "LEGENDARY_ISLAND"

    override fun print(): String =
        "Legendary Island"
}