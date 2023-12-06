package com.busted_moments.client.features;

import com.busted_moments.client.FuyMain;
import com.busted_moments.client.util.ChatUtil;
import com.busted_moments.core.Default;
import com.busted_moments.core.Feature;
import com.busted_moments.core.Promise;
import com.busted_moments.core.State;
import com.busted_moments.core.api.requests.Update;
import com.busted_moments.core.time.ChronoUnit;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.utils.FileUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Default(State.ENABLED)
@Feature.Definition(name = "Auto Update")
public class AutoUpdateFeature extends Feature {
   private static final Path TEMP_DIRECTORY = FabricLoader.getInstance().getGameDir().resolve("temp").resolve("fuy_gg-update.jar");

   @Override
   protected void onInit() {
      Runtime.getRuntime().addShutdownHook(new Thread(() -> {
         File temp = getTempFile();
         if (!temp.exists()) return;
         else if (temp.isDirectory()) {
            temp.delete();
            return;
         }

         try {
            FileUtils.copyFile(temp, FuyMain.getJar());
            temp.delete();
         } catch (IOException e) {
            throw new RuntimeException(e);
         }
      }));
   }

   @SubscribeEvent
   public void onJoinWorld(WorldStateEvent event) {
      if (!event.isFirstJoinWorld() || FabricLoader.getInstance().isDevelopmentEnvironment()) return;

      update().thenAccept(result -> {
         if (result == Result.ON_LATEST) return;

         ChatUtil.message(result.getMessage());
      });
   }

   public static Promise<Result> update() {
      if (FabricLoader.getInstance().isDevelopmentEnvironment()) return Promise.of(Result.DEV_ENV);

      File temp = getTempFile();
      if (temp.exists()) FileUtils.deleteFile(temp);

      return new Update.Request()
              .<Result>thenApplyStage((promise, optional) -> {
                 Update update;

                 if (optional.isEmpty()) {
                    promise.complete(Result.ERROR);
                    return;
                 } else update = optional.get();

                 if (!update.greaterThan(FuyMain.getVersion())) {
                    promise.complete(Result.ON_LATEST);
                    return;
                 }

                 FileUtils.createNewFile(temp);

                 try {
                    InputStream download = update.download();

                    Files.copy(download, temp.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    promise.complete(Result.SUCCESSFUL);
                 } catch (IOException e) {
                    if (temp.exists()) FileUtils.deleteFile(temp);
                    LOGGER.error("Error while downloading update", e);
                    temp.delete();

                    promise.complete(Result.ERROR);
                 }
              }).completeOnTimeout(Result.ERROR, 1, ChronoUnit.MINUTES);
   }

   private static File getTempFile() {
      File file = TEMP_DIRECTORY.toFile();
      File tempDir = file.getParentFile();

      if (!tempDir.exists() && !tempDir.mkdirs())
         throw new RuntimeException("Could not create directories for %s".formatted(tempDir));

      return file;
   }

   public enum Result {
      SUCCESSFUL("Successfully downloaded update, it will be applied on shutdown", ChatFormatting.GREEN),
      ON_LATEST("Fuy.gg is already on the latest version", ChatFormatting.YELLOW),
      DEV_ENV("Cannot download update while inside development environment", ChatFormatting.RED),
      ERROR("An error has occurred while trying to update Fuy.gg", ChatFormatting.RED);

      private final StyledText message;

      Result(String message, ChatFormatting... formattings) {
         this.message = StyledText.fromComponent(ChatUtil.component(message, formattings));
      }

      public StyledText getMessage() {
         return message;
      }
   }
}
