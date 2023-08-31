package com.busted_moments.client.util;

import com.wynntils.utils.mc.McUtils;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;

public class PacketUtil {
   public static void Action(ServerboundPlayerCommandPacket.Action action) {
      McUtils.sendPacket(new ServerboundPlayerCommandPacket(McUtils.player(), action));
   }
}
