package com.busted_moments.client.models.territories.eco

object EcoConstants {
    object Tower {
        const val DAMAGE_MAX: Long = 1500
        const val DAMAGE_MIN: Long = 1000
        const val ATTACK_SPEED: Double = 0.5
        const val HEALTH: Long = 300000
        const val DEFENSE: Double = 10.0
    }

    const val HQ_EMERALD_STORAGE = 5000
    const val NORMAL_EMERALD_STORAGE = 3000

    const val HQ_RESOURCE_STORAGE = 1500
    const val NORMAL_RESOURCE_STORAGE = 300

//    fun getStorage(type: ResourceType, eco: TerritoryEco?): Long {
//        if (eco == null) return 0
//        val storage =
//            if ((type == ResourceType.EMERALDS)) getEmeraldStorage(eco.isHQ()) else getResourceStorage(eco.isHQ())
//
//        return (((eco.getUpgrade(if (type == ResourceType.EMERALDS) UpgradeType.EMERALD_STORAGE else UpgradeType.RESOURCE_STORAGE)
//            .bonus() / 100) + 1) * storage)
//    }
}