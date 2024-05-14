package com.busted_moments.framework

import net.fabricmc.loader.api.FabricLoader

typealias IFabricLoader = FabricLoader

object FabricLoader : IFabricLoader by FabricLoader.getInstance()