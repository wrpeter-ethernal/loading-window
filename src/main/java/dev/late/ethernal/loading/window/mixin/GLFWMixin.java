package dev.late.ethernal.loading.window.mixin;

import dev.late.ethernal.loading.window.event.WindowOpeningCallback;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.NativeType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({GLFW.class})
public class GLFWMixin {
   @Inject(
      method = {"glfwCreateWindow(IILjava/lang/CharSequence;JJ)J"},
      at = {@At("HEAD")},
      remap = false
   )
   private static void loading_window$onCreateWindow(int width, int height, @NativeType("char const *") CharSequence title, @NativeType("GLFWmonitor *") long monitor, @NativeType("GLFWwindow *") long share, CallbackInfoReturnable<Long> ci) {
      ((WindowOpeningCallback)WindowOpeningCallback.EVENT.invoker()).onWindowOpening();
   }
}
