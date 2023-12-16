package com.busted_moments.client.keybinds;

import com.busted_moments.client.features.keybinds.Keybind;
import com.mojang.blaze3d.platform.InputConstants;
import com.wynntils.core.components.Handlers;
import com.wynntils.core.text.PartStyle;
import com.wynntils.core.text.StyledText;
import com.wynntils.mc.event.ContainerSetContentEvent;
import com.wynntils.mc.event.MenuEvent;
import com.wynntils.utils.wynn.ContainerUtils;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

import java.util.regex.Pattern;

import static com.wynntils.utils.mc.McUtils.mc;

@Keybind.Definition(name = "Open guild bank", defaultKey = InputConstants.KEY_Y)
public class GuildBankKeybind extends Keybind {
   private static final Pattern GUILD_BANK_PATTERN = Pattern.compile("^(?<guild>.+): Manage$");

   private boolean OPENING_BANK = false;

   private int BANK_ID = Integer.MIN_VALUE;

   public GuildBankKeybind() {
      super(GuildBankKeybind.class);
   }

   @Override
   protected void onKeyDown() {
      if (!OPENING_BANK && mc().getConnection() != null) {
         OPENING_BANK = true;
         Handlers.Command.sendCommand("gu manage");
      }
   }

   @SubscribeEvent
   public void onContainerOpen(MenuEvent.MenuOpenedEvent event) {
      if (OPENING_BANK && StyledText.fromComponent(event.getTitle()).matches(GUILD_BANK_PATTERN, PartStyle.StyleType.NONE)) {
         BANK_ID = event.getContainerId();
         event.setCanceled(true);
      }
   }

   @SubscribeEvent
   public void onContainerSetContent(ContainerSetContentEvent.Pre event) {
      if (OPENING_BANK && event.getContainerId() == BANK_ID) {
         ContainerUtils.clickOnSlot(15, event.getContainerId(), GLFW.GLFW_MOUSE_BUTTON_LEFT, event.getItems());
         OPENING_BANK = false;
         BANK_ID = Integer.MIN_VALUE;
      }
   }
}
