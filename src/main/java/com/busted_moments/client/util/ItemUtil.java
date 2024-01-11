package com.busted_moments.client.util;

import com.busted_moments.core.text.TextBuilder;
import com.wynntils.core.text.StyledText;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.world.item.ItemStack;

public class ItemUtil {
   public static StyledText forChat(ItemStack item) {
       return TextBuilder.empty()
               .append(item.getHoverName())
               .onPartHover(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackInfo(item))
               .build();
   }
}
