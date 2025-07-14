package dev.late.ethernal.loading.window.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public interface WindowOpeningCallback {
   Event<WindowOpeningCallback> EVENT = EventFactory.createArrayBacked(WindowOpeningCallback.class, (listeners) -> {
      return () -> {
         WindowOpeningCallback[] var1 = listeners;
         int var2 = listeners.length;

         for(int var3 = 0; var3 < var2; ++var3) {
            WindowOpeningCallback l = var1[var3];
            l.onWindowOpening();
         }

      };
   });

   void onWindowOpening();
}
