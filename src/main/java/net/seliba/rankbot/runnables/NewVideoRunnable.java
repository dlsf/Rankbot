package net.seliba.rankbot.runnables;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.TextChannel;
import net.seliba.rankbot.Rankbot;
import net.seliba.rankbot.youtube.NewVideoFetcher;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class NewVideoRunnable implements Runnable {

  private static final Logger LOGGER = LogManager.getLogger(Rankbot.class.getName());

  private JDA jda;
  private File videoFile;
  private String latestVideoId;

  public NewVideoRunnable(JDA jda, File videoFile, String latestVideoId) {
    this.jda = jda;
    this.videoFile = videoFile;
    this.latestVideoId = latestVideoId;
  }

  private static void sendMessage(TextChannel textChannel, String message) {
    textChannel.sendMessage(new MessageBuilder().setContent(message).build()).queue();
  }

  @Override
  public void run() {
    LOGGER.info("Thread #1 gestartet!");
    try {
      Thread.sleep(5000L);
      while (true) {
        LOGGER.info("Ueberpruefe auf neue Videos...");
        String videoData = NewVideoFetcher.getLatestVideoData();
        if (!latestVideoId.equalsIgnoreCase(videoData)) {
          LOGGER.info("Neues Video gefunden, sende Nachricht...");
          sendMessage(
              jda.getTextChannelById(486571558748553219L),
              ":tada: WOOOOOHOOOO!  @everyone! :tada:\n"
                  + "\n"
                  + "BiVieh hat ein neues Video hochgeladen! :tv:\n"
                  + "Viel Spaß!\n"
                  + "https://www.youtube.com/watch?v="
                  + videoData
                  + "\n"
                  + "\n"
                  + ":computer:  Zur Website: https://bivieh-dev.de/ :computer:");
        }
        latestVideoId = videoData;
        videoFile.delete();
        videoFile.createNewFile();
        Files.write(
            Paths.get(videoFile.toURI()),
            Collections.singletonList(latestVideoId),
            Charset.forName("UTF-8"));
        Thread.sleep(300000L);
      }
    } catch (Exception ex) {
      LOGGER.error(ex, ex);
      Thread.currentThread().interrupt();
      throw new RuntimeException(ex);
    }
  }
}
