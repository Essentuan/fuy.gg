package com.busted_moments.core.config.entry;

import com.busted_moments.core.config.Buildable;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Field;

public interface ConfigConstructor {
   ConfigEntry<?> create(Component title, Object ref, Field field, Buildable<?, ?> parent);
}
