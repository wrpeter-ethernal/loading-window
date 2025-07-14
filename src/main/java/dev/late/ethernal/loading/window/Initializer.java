package dev.late.ethernal.loading.window;

import java.util.Optional;

import dev.late.ethernal.loading.window.event.WindowOpeningCallback;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Initializer implements ModInitializer {
   public static final Logger LOGGER = LoggerFactory.getLogger("loading-window");

   public void onInitialize() {
      WindowOpeningCallback.EVENT.register(() -> {
         PreLaunch.frame.ifPresentOrElse((frame) -> {
            frame.setVisible(false);
            frame.dispose();
            PreLaunch.frame = Optional.empty();
         }, () -> {
            LOGGER.info("Starting MC loading windows");
         });
      });
   }
}
