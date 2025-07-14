package dev.late.ethernal.loading.window;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.swing.*;

import dev.late.ethernal.loading.window.utils.GifDecoder;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PreLaunch implements PreLaunchEntrypoint {
   public static Optional<JFrame> frame = Optional.empty();
   public static Logger LOGGER = LoggerFactory.getLogger("loading-window");
   public static final File CONFIG_FOLDER = new File(FabricLoader.getInstance().getConfigDir().toFile(), "loading-windows");

   public void onPreLaunch() {
      if (isMac()) {
         LOGGER.warn("Cannot open loading window on Mac due to OS limitations regarding AWT.");
      } else {
         if (!CONFIG_FOLDER.exists()) {
            CONFIG_FOLDER.mkdir();
         }

         try {
            this.createAndShowUI();
            LOGGER.info("Starting MC loading windows");
         } catch (Exception var2) {
            LOGGER.error("Unable to launch loading screen.", var2.getMessage());
         }
      }
   }

   private void createAndShowUI() throws Exception {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      JFrame loadingFrame = new PreLaunch.LoadingFrame("Ethernal Client");
      loadingFrame.setVisible(true);
      frame = Optional.of(loadingFrame);
   }

   private static boolean isMac() {
      String os = System.getProperty("os.name").toLowerCase();
      return os.contains("mac");
   }

   public static List<Image> getScaledGifIcon(File file) throws FileNotFoundException {
      GifDecoder gifDecoder = new GifDecoder();
      int state = gifDecoder.read(new FileInputStream(file)); // ✅ Usamos correctamente el int devuelto
      if (state != GifDecoder.STATUS_OK) {
         return null;
      }

      ArrayList<Image> frames = new ArrayList<>();
      for (int i = 0; i < gifDecoder.getFrameCount(); ++i) {
         try {
            frames.add(gifDecoder.getFrame(i));
         } catch (Exception var6) {
            Initializer.LOGGER.warn("Error while parsing gif animation " + file.getName(), var6);
         }
      }

      return frames;
   }

   private static class LoadingFrame extends JFrame {
      private PreLaunch.LoadingBarPane pane;

      public LoadingFrame(String title) throws FileNotFoundException {
         super(title);
         this.setLayout(new BorderLayout());
         File image = new File(PreLaunch.CONFIG_FOLDER, "background.gif");
         File icon = new File(PreLaunch.CONFIG_FOLDER, "icon.png");
         boolean isGif = true;

         if (icon.exists()) {
            this.setIconImage(new ImageIcon(icon.getAbsolutePath()).getImage());
         }

         if (!image.exists()) {
            image = new File(PreLaunch.CONFIG_FOLDER, "background.jpg");
            isGif = false;
         }

         if (!image.exists()) {
            this.setResizable(false);
            JProgressBar progressBar = new JProgressBar();
            progressBar.setIndeterminate(true);
            progressBar.setPreferredSize(new Dimension(256, (int) progressBar.getPreferredSize().getHeight()));
            JPanel mainPanel = new JPanel();
            mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            mainPanel.setLayout(new BorderLayout());
            mainPanel.add(new JLabel("Initializing Minecraft..."), "North");
            mainPanel.add(progressBar, "Center");
            this.setContentPane(mainPanel);
            this.pack();
            this.setLocationRelativeTo(null);
         } else {
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            int width = screenSize.width / 3;
            int height = screenSize.height / 3;
            ImageIcon backgroundImage = new ImageIcon(image.getAbsolutePath());
            ImageIcon imgScaled = new ImageIcon(backgroundImage.getImage().getScaledInstance(width, height, Image.SCALE_DEFAULT));

            if (isGif) {
               List<Image> gif = PreLaunch.getScaledGifIcon(image);
               this.pane = new PreLaunch.LoadingBarPane(gif, imgScaled.getImage(), imgScaled.getIconWidth(), imgScaled.getIconHeight());
            } else {
               this.pane = new PreLaunch.LoadingBarPane(null, imgScaled.getImage(), imgScaled.getIconWidth(), imgScaled.getIconHeight());
            }

            this.setContentPane(this.pane);
            this.setSize(imgScaled.getIconWidth(), imgScaled.getIconHeight());
            this.setLocationRelativeTo(null);
            this.setResizable(false);
            this.setUndecorated(true);
         }
      }

      public void setVisible(boolean b) {
         if (!b) {
            this.pane.timer.stop();
            if (this.pane.frames != null) {
               this.pane.frames.clear();
            }
         }

         super.setVisible(b);
      }
   }

   public static class LoadingBarPane extends JPanel {
      private final List<Image> frames;
      private int frameIndex = 0;
      private long lastTime;
      private final Timer timer;
      private final Image icon;
      private float loadingBarPosition;

      public LoadingBarPane(@Nullable List<Image> frames, Image icon, int width, int height) {
         this.setPreferredSize(new Dimension(width, height));
         this.setLayout(null);
         this.icon = icon;
         this.frames = frames;
         this.loadingBarPosition = 0.0F;

         this.timer = new Timer(40, (e) -> {
            this.loadingBarPosition += 0.01F;
            this.tick();
            if (this.loadingBarPosition > 1.28F) {
               this.loadingBarPosition = 0.0F;
            }
            this.repaint();
         });
         this.timer.start();
      }

      protected void paintComponent(Graphics g) {
         super.paintComponent(g);
         if (this.frames == null) {
            g.drawImage(this.icon, 0, 0, this.getWidth(), this.getHeight(), this);
         } else if (this.frames.size() > this.frameIndex) {
            g.drawImage(this.frames.get(this.frameIndex), 0, 0, this.getWidth(), this.getHeight(), this);
         } else if (!this.frames.isEmpty()) {
            g.drawImage(this.frames.get(0), 0, 0, this.getWidth(), this.getHeight(), this);
         }

         this.drawLoadingBar(g);
      }

      private void drawLoadingBar(Graphics g) {
         Graphics2D g2d = (Graphics2D) g;
         int barHeight = 43; // nueva altura de la barra
         int y = this.getHeight() - barHeight;

         Paint paint = new GradientPaint(
                 0.0F,
                 this.getHeight(),
                 new Color(100, 170, 220, 255), // azul claro más oscuro, opaco
                 0.0F,
                 this.getHeight() - barHeight,
                 new Color(100, 170, 220, 0),   // transparente en la parte superior de la barra
                 false
         );

         g2d.setPaint(paint);
         int x = (int) (this.getWidth() * this.loadingBarPosition);
         int width = this.getWidth() / 4;
         g2d.fillRect(x - width, y, width, barHeight);
      }

      public void tick() {
         if (this.frames != null) {
            if (this.frameIndex >= this.frames.size()) {
               this.frameIndex = 0;
            }

            if (this.lastTime == 0L) {
               this.lastTime = System.currentTimeMillis();
               this.frameIndex = 0;
            } else {
               long rest = System.currentTimeMillis() - this.lastTime;
               if (rest >= 62L) {
                  this.lastTime = System.currentTimeMillis();
                  ++this.frameIndex;
               }
            }
         }
      }
   }
}