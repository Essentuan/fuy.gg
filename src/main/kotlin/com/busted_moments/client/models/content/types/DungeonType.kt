package com.busted_moments.client.models.content.types

import com.busted_moments.client.Patterns
import com.busted_moments.client.framework.text.RED
import com.wynntils.core.text.type.StyleType
import com.busted_moments.client.models.content.ContentTimer
import com.busted_moments.client.models.content.ContentType
import com.busted_moments.client.models.content.Stage
import com.busted_moments.client.models.content.Trigger
import com.busted_moments.client.models.content.stages.Stages
import com.busted_moments.client.models.content.triggers.Triggers
import net.essentuan.esl.collections.builders.list
import net.minecraft.world.entity.EntityType
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3

private val TL_HUB = Triggers.enters(
    Vec2(-4704f, 2240f),
    Vec2(-4642f, 2712f)
)

private val TL_OPTIONS = Stages.multi(
    Stages.entity(
        "Courtyard",
        "the center of the Courtyard.",
        type = EntityType.ARMOR_STAND
    ),
    Stages.entity(
        "Observatory",
        "Scale the Observatory to",
        type = EntityType.ARMOR_STAND
    ),
    Stages.entity(
        "Research Lab",
        "Destabilize the Research Lab's experiments",
        type = EntityType.ARMOR_STAND
    ),
    Stages.entity(
        "Cathedral",
        "to conquer the Cathedral.",
        type = EntityType.ARMOR_STAND
    )
)

enum class DungeonType(
    private val pretty: String,
    start: Trigger.Builder,
    vararg stages: Stage.Builder,
) : ContentType, List<Stage.Builder> by stages.toList() {
    DECREPIT_SEWERS(
        "Decrepit Sewers",
        Triggers.enters(
            Vec2(-10032f, -10368f),
            Vec2(-10050f, -10390f)
        ),
        Stages.generic(
            "Clearing the Drain",
            Triggers.immediate()
        ),
        Stages.enters(
            "Holding Tank",
            Vec2(-10039f, -10471f),
            Vec2(-10043f, -10520f)
        ),
        Stages.enters(
            "Crumbling Ruin",
            Vec2(-10000f, -10603f),
            Vec2(-10096f, -10699f)
        ),
        Stages.enters(
            "Witherhead",
            Vec3(-10037.0, 47.0, -10716.0),
            Vec3(-10043.0, 55.0, -10722.0)
        )
    ),
    INFESTED_PIT(
        "Infested Pit",
        Triggers.enters(
            Vec3(-10238.0, 45.0, -11135.0),
            Vec3(-10264.0, 69.0, -11157.0)
        ),
        Stages.generic(
            "Breeding Grounds",
            Triggers.immediate()
        ),
        Stages.enters(
            "Web Waltz",
            Vec3(-10204.0, 47.0, -11065.0),
            Vec3(-10264.0, 71.0, -11055.0)
        ),
        Stages.enters(
            "Spider Onslaught",
            Vec3(-10128.0, 82.0, -11054.0),
            Vec3(-10165.0, 71.0, -11091.0)
        ),
        Stages.enters(
            "Eight-Legged Leaps",
            Vec2(-10151f, -10921f),
            Vec2(-10197f, -10981f)
        ),
        Stages.enters(
            "Arakadicus",
            Vec3(-10041.0, 191.0, -11068.0),
            Vec3(-10035.0, 199.0, -11074.0)
        )
    ),
    UNDERWORLD_CRYPT(
        "Underworld Crypt",
        Triggers.enters(
            Vec2(-11788f, -12602f),
            Vec2(-11859f, -12528f)
        ),
        Stages.generic(
            "Forsaken Stronghold",
            Triggers.immediate()
        ),
        Stages.enters(
            "Legionnare Marzban",
            Vec2(-11780f, -12332f),
            Vec2(-11871f, -12257f)
        ),
        Stages.enters(
            "Underworld Ferry",
            Vec2(-11876f, -12247f),
            Vec2(-11779f, -12081f)
        ),
        Stages.enters(
            "Charon",
            Vec2(-11475f, -11887f),
            Vec2(-11536f, -11837f)
        )
    ),
    TIMELOST_SANCTUARY(
        "Timelost Sanctuary",
        Triggers.enters(
            Vec2(-4653f, 2244f),
            Vec2(-4711f, 2302f)
        ),
        Stages.generic(
            "The Hub",
            Triggers.immediate()
        ),
        TL_OPTIONS,
        Stages.lazy(
            "[TBD]",
            Triggers.enters(
                Vec3(-4653.0, 25.0, 2423.0),
                Vec3(-4711.0, 76.0, 2365.0)
            ),
            TL_OPTIONS
        ),
        Stages.lazy(
            "[TBD]",
            Triggers.enters(
                Vec3(-4651.0, 25.0, 2519.0),
                Vec3(-4714.0, 76.0, 2456.0)
            ),
            TL_OPTIONS
        ),
        Stages.lazy(
            "[TBD]",
            Triggers.enters(
                Vec3(-4717.0, 25.0, 2524.0),
                Vec3(-4652.0, 76.0, 2596.0)
            ),
            TL_OPTIONS
        ),
        Stages.enters(
            "The Ruined Hub",
            Vec3(-4647.0, 198.0, 2687.0),
            Vec3(-4715.0, 248.0, 2625.0)
        ),
        Stages.enters(
            "Garoth",
            Vec2(-4897f, 2071f),
            Vec2(-5008f, 1962f)
        )
    ),
    SAND_SWEPT_TOMB(
        "Sand-Swept Tomb",
        Triggers.enters(
            Vec2(-12096f, -11598f),
            Vec2(-12182f, -11704f)
        ),
        Stages.generic(
            "Sand Hall",
            Triggers.immediate()
        ),
        Stages.enters(
            "Perilous Run",
            Vec2(-12157f, -11591f),
            Vec2(-12143f, -11496f)
        ),
        Stages.multi(
            Stages.enters(
                "Pest Control",
                Vec2(-11986f, -11387f),
                Vec2(-12017f, -11450f)
            ),
            Stages.enters(
                "Dust Bunnies",
                Vec2(-12272f, -11419f),
                Vec2(-12241f, -11481f)
            ),
            Stages.enters(
                "Distasteful Dispensers",
                Vec2(-12200f, -11423f),
                Vec2(-12162f, -11482f)
            ),
            Stages.enters(
                "The Lion's Den",
                Vec2(-12105f, -11480f),
                Vec2(-12055f, -11406f)
            )
        ),
        Stages.multi(
            Stages.enters(
                "Sandspire",
                Vec2(-12005f, -11193f),
                Vec2(-12066f, -11134f)
            ),
            Stages.enters(
                "Dune Dashway",
                Vec2(-12284f, -11367f),
                Vec2(-12242f, -11207f)
            ),
            Stages.enters(
                "Shifting Sands",
                Vec2(-12327f, -11048f),
                Vec2(-12343f, -11099f)
            ),
            Stages.enters(
                "Pillar Madness",
                Vec2(-12058f, -11271f),
                Vec2(-12070f, -11281f)
            )
        ),
        Stages.enters(
            "Gladiator Ring",
            Vec2(-12126f, -11447f),
            Vec2(-12159f, -11420f)
        ),
        Stages.enters(
            "Quicksand",
            Vec2(-12156f, -11361f),
            Vec2(-12144f, -11260f)
        ),
        Stages.enters(
            "Hashr",
            Vec3(-11864.0, 3.0, -11492.0),
            Vec3(-11868.0, 7.0, -11496.0)
        )
    ),
    ICE_BARROWS(
        "Ice Barrows",
        Triggers.enters(
            Vec2(316f, 1815f),
            Vec2(289f, 1836f)
        ),
        Stages.generic(
            "Frozen Tomb",
            Triggers.immediate()
        ),
        Stages.enters(
            "Subzero Sliding",
            Vec2(295f, 1863f),
            Vec2(290f, 1869f)
        ),
        Stages.enters(
            "Poltergiced Avalanche",
            Vec2(349f, 1815f),
            Vec2(305f, 1778f)
        ),
        Stages.enters(
            "Phantom Path",
            Vec3(336.0, 109.0, 1883.0),
            Vec3(253.0, 152.0, 1835.0)
        ),
        Stages.enters(
            "Barrows' Frenzy",
            Vec3(256.0, 118.0, 1877.0),
            Vec3(225.0, 143.0, 1808.0)
        ),
        Stages.enters(
            "Chilling Revelation",
            Vec3(234.0, 54.0, 1833.0),
            Vec3(205.0, 60.0, 1864.0)
        ),
        Stages.enters(
            "Theorick",
            Vec2(-117f, 1880f),
            Vec2(-168f, 1928f)
        )
    ),
    UNDERGROWTH_RUINS(
        "Undergrowth Ruins",
        Triggers.enters(
            Vec2(-2574f, -805f),
            Vec2(-2637f, -766f)
        ),
        Stages.generic(
            "Gel Go",
            Triggers.immediate()
        ),
        Stages.enters(
            "Keystone Escort",
            Vec2(-2612f, -684f),
            Vec2(-2663f, -459f)
        ),
        Stages.enters(
            "Unfortified Wall",
            Vec2(-2658f, -629f),
            Vec2(-2590f, -658f)
        ),
        Stages.enters(
            "Slimey Pillars",
            Vec2(-2581f, -612f),
            Vec2(-2525f, -668f)
        ),
        Stages.enters(
            "Aquifer Bounce",
            Vec2(-2517f, -607f),
            Vec2(-2434f, -691f)
        ),
        Stages.enters(
            "Sludge Spire",
            Vec2(-2437f, -674f),
            Vec2(-2481f, -630f)
        ),
        Stages.enters(
            "Slykaar",
            Vec2(-2655f, -956f),
            Vec2(-2585f, -880f)
        )
    ),
    GALLEONS_GRAVEYARD(
        "Galleon's Graveyard",
        Triggers.enters(
            Vec2(5023f, -17948f),
            Vec2(4984f, -17922f)
        ),
        Stages.generic(
            "Cannon Mutiny",
            Triggers.immediate()
        ),
        Stages.enters(
            "Explosive Rush",
            Vec2(4731f, -18113f),
            Vec2(4677f, -18274f)
        ),
        Stages.enters(
            "Megalodon Attack",
            Vec3(4746.0, 42.0, -17955.0),
            Vec3(4675.0, 10.0, -17910.0)
        ),
        Stages.enters(
            "Naval Barrage",
            Vec2(4616f, -17933f),
            Vec2(4665f, -17844f)
        ),
        Stages.enters(
            "Piranha Escape",
            Vec2(4675f, -1774f),
            Vec2(4756f, -17775f)
        ),
        Stages.enters(
            "Le Fishe au Chocolat",
            Vec3(4822.0, 25.0, -17798.0),
            Vec3(4910.0, 53.0, -17725.0)
        ),
        Stages.enters(
            "Redbeard 'n Crew",
            Vec3(4997.0, 160.0, -17775.0),
            Vec3(4894.0, 196.0, -17863.0)
        )
    ),
    FALLEN_FACTORY(
        "Fallen Factory",
        Triggers.enters(
            Vec2(-9011f, -2083f),
            Vec2(-9070f, -2135f)
        ),
        Stages.generic(
            "Tower of Waste",
            Triggers.immediate()
        ),
        Stages.enters(
            "Assembly Line",
            Vec2(-9045f, -2009f),
            Vec2(-9245f, -2066f)
        ),
        Stages.enters(
            "Volatile Workshop",
            Vec2(-9358f, -2109f),
            Vec2(-9297f, -2165f)
        ),
        Stages.enters(
            "Maintenance Mayhem",
            Vec2(-9299f, -2173f),
            Vec2(-9376f, -2246f)
        ),
        Stages.enters(
            "Storage Siege",
            Vec2(-9291f, -2248f),
            Vec2(-9372f, -2328f)
        ),
        Stages.enters(
            "Euthanasia",
            Vec2(-8676f, -2089f),
            Vec2(-8645f, -2115f)
        ),
        Stages.enters(
            "Firewall",
            Vec2(-8859f, -2248f),
            Vec2(-8744f, -2319f)
        ),
        Stages.enters(
            "Antikythera",
            Vec2(-8830f, -2408f),
            Vec2(-8773f, -2445f)
        )
    ),
    ELDRITCH_OUTLOOK(
        "Eldritch Outlook",
        Triggers.enters(
            Vec2(4656f, 2140f),
            Vec2(4726f, 2070f)
        ),
        Stages.generic(
            "Icy Inversions",
            Triggers.immediate()
        ),
        Stages.enters(
            "Maelstrom Drift",
            Vec2(4435f, 2357f),
            Vec2(4609f, 2561f)
        ),
        Stages.enters(
            "Reversing Catalysts",
            Vec2(4773f, 2591f),
            Vec2(4710f, 2662f)
        ),
        Stages.enters(
            "Spectral Trek",
            Vec2(4663f, 2669f),
            Vec2(4569f, 2578f)
        ),
        Stages.enters(
            "The Eye",
            Vec2(4856f, 1780f),
            Vec2(5007f, 1911f)
        )
    ),
    CORRUPTED_DECREPIT_SEWERS(
        "Corrupted Decrepit Sewers",
        Triggers.enters(
            Vec3(3057.0, 114.0, 2509.0),
            Vec3(3051.0, 121.0, 2503.0)
        ),
        Stages.generic(
            "Clearing the Drain",
            Triggers.immediate()
        ),
        Stages.enters(
            "Holding Tank",
            Vec2(3056f, 2413f),
            Vec2(3049f, 2363f)
        ),
        Stages.enters(
            "Crumbling Ruin",
            Vec2(3105f, 2283f),
            Vec2(2997f, 2165f)
        ),
        Stages.enters(
            "Witherhead",
            Vec2(3103f, 2000f),
            Vec2(3000f, 1985f)
        )
    ),
    CORRUPTED_INFESTED_PIT(
        "Corrupted Infested Pit",
        Triggers.enters(
            Vec3(4026.0, 76.0, 3303.0),
            Vec3(4076.0, 40.0, 3332.0)
        ),
        Stages.generic(
            "Breeding Grounds",
            Triggers.immediate()
        ),
        Stages.enters(
            "Web Waltz",
            Vec3(4090.0, 53.0, 3387.0),
            Vec3(4035.0, 80.0, 3397.0)
        ),
        Stages.enters(
            "Spider Onslaught",
            Vec3(4165.0, 78.0, 3394.0),
            Vec3(4110.0, 89.0, 3358.0)
        ),
        Stages.enters(
            "Eight-Legged Leaps",
            Vec2(4097f, 3472f),
            Vec2(4161f, 3525f)
        ),
        Stages.enters(
            "Arakadicus",
            Vec3(4282.0, 34.0, 3415.0),
            Vec3(4173.0, 147.0, 3308.0)
        ),
    ),
    CORRUPTED_LOST_SANCTUARY(
        "Corrupted Lost Sanctuary",
        Triggers.enters(
            Vec3(2731.0, 161.0, 7120.0),
            Vec3(2725.0, 170.0, 7126.0)
        ),
        Stages.generic(
            "Burning Bridge",
            Triggers.immediate()
        ),
        Stages.enters(
            "Explosive Garden",
            Vec3(2824.0, 5.0, 7122.0),
            Vec3(2807.0, 25.0, 7137.0)
        ),
        Stages.enters(
            "Prehistoric Fauna",
            Vec3(2763.0, 6.0, 7145.0),
            Vec3(2806.0, 66.0, 7118.0)
        ),
        Stages.enters(
            "Garoth",
            Vec2(2533f, 7063f),
            Vec2(2443f, 6934f)
        )
    ),
    CORRUPTED_UNDERWORLD_CRYPT(
        "Corrupted Underworld Crypt",
        Triggers.enters(
            Vec3(4318.0, 90.0, 5158.0),
            Vec3(4313.0, 95.0, 5162.0)
        ),
        Stages.generic(
            "Forsaken Stronghold",
            Triggers.immediate()
        ),
        Stages.enters(
            "Legionnare Marzban",
            Vec2(4384f, 5382f),
            Vec2(4307f, 5458f)
        ),
        Stages.enters(
            "Underworld Ferry",
            Vec2(4313f, 5472f),
            Vec2(4361f, 5643f)
        ),
        Stages.enters(
            "Charon",
            Vec2(4276f, 6019f),
            Vec2(4332f, 6063f)
        )
    ),
    CORRUPTED_SAND_SWEPT_TOMB(
        "Corrupted Sand-Swept Tomb",
        Triggers.enters(
            Vec3(4136.0, 92.0, 3891.0),
            Vec3(4143.0, 100.0, 3898.0)
        ),
        Stages.generic(
            "Sand Hall",
            Triggers.immediate()
        ),
        Stages.enters(
            "Perilous Run",
            Vec2(4130.0f, 3989f),
            Vec2(4149f, 4081f)
        ),
        Stages.multi(
            Stages.enters(
                "Pest Control",
                Vec2(4310f, 4192f),
                Vec2(4273f, 4126f)
            ),
            Stages.enters(
                "Dust Bunnies",
                Vec2(4052f, 4160f),
                Vec2(4018f, 4091f)
            ),
            Stages.enters(
                "Distasteful Dispensers",
                Vec2(4129f, 4163f),
                Vec2(4088f, 4095f)
            ),
            Stages.enters(
                "The Lion's Den",
                Vec2(4222f, 4161f),
                Vec2(4182f, 4094f)
            )
        ),
        Stages.multi(
            Stages.enters(
                "Sandspire",
                Vec2(4286f, 43988f),
                Vec2(4234f, 4437f)
            ),
            Stages.enters(
                "Dune Dashway",
                Vec2(4005f, 4210f),
                Vec2(4059f, 4370f)
            ),
            Stages.enters(
                "Shifting Sands",
                Vec2(3949f, 4478f),
                Vec2(3964f, 4492f)
            ),
            Stages.enters(
                "Pillar Madness",
                Vec2(4223f, 4269f),
                Vec2(4292f, 4342f)
            )
        ),
        Stages.enters(
            "Gladiator Ring",
            Vec2(4164f, 4130f),
            Vec2(4132f, 4159f)
        ),
        Stages.enters(
            "Quicksand",
            Vec2(4148f, 4214f),
            Vec2(4117f, 4383f)
        ),
        Stages.enters(
            "Hashr",
            Vec2(3571f, 4258f),
            Vec2(3640f, 4326f)
        )
    ),
    CORRUPTED_ICE_BARROWS(
        "Corrupted Ice Barrows",
        Triggers.enters(
            Vec3(1777.0, 163.0, 7750.0),
            Vec3(1771.0, 169.0, 7743.0)
        ),
        Stages.generic(
            "Frozen Tomb",
            Triggers.immediate()
        ),
        Stages.enters(
            "Subzero Sliding",
            Vec3(1764.0, 72.0, 7807.0),
            Vec3(1755.0, 76.0, 7795.0)
        ),
        Stages.enters(
            "Poltergiced Avalanche",
            Vec2(1818f, 7754f),
            Vec2(1780f, 7716f)
        ),
        Stages.enters(
            "Phantom Path",
            Vec3(1840.0, 109.0, 7824.0),
            Vec3(1739.0, 143.0, 7782.0)
        ),
        Stages.enters(
            "Barrows' Frenzy",
            Vec3(1722.0, 120.0, 7813.0),
            Vec3(1700.0, 143.0, 7759.0)
        ),
        Stages.enters(
            "Chilling Revelation",
            Vec3(1697.0, 58.0, 7734.0),
            Vec3(1595.0, 88.0, 7832.0)
        ),
        Stages.enters(
            "Theorick",
            Vec3(1551.0, 43.0, 7756.0),
            Vec3(1487.0, 84.0, 7819.0)
        )
    ),
    CORRUPTED_UNDERGROWTH_RUINS(
        "Corrupted Undergrowth Ruins",
        Triggers.enters(
            Vec2(1506f, 8733f),
            Vec2(1477f, 8750f)
        ),
        Stages.generic(
            "Gel Go",
            Triggers.immediate()
        ),
        Stages.enters(
            "Keystone Escort",
            Vec2(1458f, 8870f),
            Vec2(1420f, 8891f)
        ),
        Stages.enters(
            "Unfortified Wall",
            Vec2(1417f, 8923f),
            Vec2(1478f, 8893f)
        ),
        Stages.enters(
            "Slimey Pillars",
            Vec2(1496f, 8941f),
            Vec2(1549f, 8888f)
        ),
        Stages.enters(
            "Aquifer Bounce",
            Vec3(1559.0, 163.0, 8947.0),
            Vec3(1643.0, 83.0, 8867.0)
        ),
        Stages.enters(
            "Sludge Spire",
            Vec3(1586.0, 20.0, 8924.0),
            Vec3(1644.0, 75.0, 8809.0)
        ),
        Stages.enters(
            "Slykaar",
            Vec2(1495f, 8592f),
            Vec2(1414f, 8664f)
        )
    ),
    CORRUPTED_GALLEONS_GRAVEYARD(
        "Corrupted Galleon's Graveyard",
        Triggers.enters(
            Vec3(3749.0, 171.0, -17588.0),
            Vec3(3754.0, 177.0, -17583.0)
        ),
        Stages.generic(
            "Cannon Mutiny",
            Triggers.immediate()
        ),
        Stages.enters(
            "Explosive Rush",
            Vec2(3398f, -18063f),
            Vec2(3342f, -18222f)
        ),
        Stages.enters(
            "Megalodon Attack",
            Vec3(3480.0, 101.0, -17600.0),
            Vec3(3417.0, 126.0, -17556.0)
        ),
        Stages.enters(
            "Naval Barrage",
            Vec2(3364f, -17575f),
            Vec2(3389f, -17510f)
        ),
        Stages.enters(
            "Piranha Escape",
            Vec2(3424f, -17383f),
            Vec2(3481f, -17421f)
        ),
        Stages.enters(
            "Le Fishe au Chocolat",
            Vec3(3560.0, 114.0, -17364.0),
            Vec3(3657.0, 148.0, -17442.0)
        ),
        Stages.enters(
            "To the Skies",
            Vec3(3706.0, 110.0, -17390.0),
            Vec3(3744.0, 173.0, -17423.0,)
        ),
        Stages.enters(
            "The Mutineers",
            Vec3(3762.0, 89.0, -17455.0),
            Vec3(3668.0, 43.0, -17376.0)
        ),
        Stages.enters(
            "Captain Redbeard",
            Vec3(3105.0, 127.0, -18159.0),
            Vec3(2978.0, 77.0, -18091.0)
        )
    );

    val corrupted: Boolean = name.startsWith("CORRUPTED")

    private val failures: List<Trigger.Builder> = list {
        +Triggers.death()
        +Triggers.title("${RED}Second Chance", StyleType.DEFAULT)
    }

    private val completion = Triggers.chatMessage(Patterns.DUNGEON_COMPLETION)

    private val start = start.build {
        ContentTimer(this, this, completion, failures, ContentTimer.Modifiers.Empty).start()
    }

    override val id: String
        get() = name

    override fun print(): String =
        pretty

    companion object
}
