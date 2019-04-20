package net.seliba.rankbot.runnables;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.seliba.rankbot.Rankbot;
import net.seliba.rankbot.files.VotesDao;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class VoteUpdateRunnable {

  private static final Logger LOGGER = LogManager.getLogger(Rankbot.class.getName());
  private static boolean voteMessageSent = false;
  private static JDA jda;
  private static VotesDao votesDao;
  private static boolean votingStatus;

  private ScheduledExecutorService scheduledExecutorService;

  public VoteUpdateRunnable(JDA jdaObject, VotesDao votesDaoObject, boolean votingStatusObject) {
    jda = jdaObject;
    votesDao = votesDaoObject;
    votingStatus = votingStatusObject;

    this.scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
  }

  private static Optional<Member> getMember(User user) {
    for (Guild guild : jda.getGuilds()) {
      if (guild.getIdLong() == 486161636105650176L) {
        for (Member member : guild.getMembers()) {
          if (member.getUser().getIdLong() == user.getIdLong()) {
            return Optional.of(member);
          }
        }
      }
    }
    return Optional.empty();
  }

  private static void sendEmbedMessage(TextChannel textChannel, String title, String message) {
    textChannel
        .sendMessage(
            new EmbedBuilder()
                .setTitle(title)
                .setDescription(message)
                .setColor(Color.GREEN)
                .build())
        .queue();
  }

  public void stopVoting() {
    LOGGER.info("Stoppe Experten-Voting...");
    voteMessageSent = false;
    List<User> possibleWinners = votesDao.getWinners();
    User winner = possibleWinners.get(new Random().nextInt(possibleWinners.size()));
    LOGGER.info("Gewinner des Votings: " + winner.getName());
    sendEmbedMessage(
        jda.getTextChannelById(555106694439501827L),
        "Voting",
        "Das Expertenvoting ist beendet. Neuer Experte: " + winner.getAsMention());
    jda.getGuildById(486161636105650176L)
        .getController()
        .addSingleRoleToMember(getMember(winner).orElseThrow(NoSuchElementException::new),
            jda.getRoleById(551802664879390720L))
        .queue();
    votesDao.deleteVotes();
  }

  public void run() {
    LOGGER.info("Thread #2 gestartet!");
    scheduledExecutorService.scheduleWithFixedDelay(() -> {
      LOGGER.info("Ueberpruefe auf Voting-Statusaenderung...");
      Calendar calendar = Calendar.getInstance();
      int day = calendar.get(Calendar.DAY_OF_WEEK);
      if (day == 7 && !voteMessageSent) {
        startVoting();
      } else if (day == 2 && voteMessageSent) {
        stopVoting();
      }
    }, 5, 300, TimeUnit.SECONDS);
  }

  private void startVoting() {
    if (votingStatus = false) {
      votingStatus = true;
      try {
        File file = new File("voting-data.txt");
        if (!file.exists()) {
          file.createNewFile();
        }
        Files.write(
            Paths.get(new File("voting-data.txt").toURI()),
            Collections.singletonList(String.valueOf(votingStatus)),
            Charset.forName("UTF-8"));
      } catch (IOException exception) {
        LOGGER.error(exception, exception);
      }
      return;
    }
    LOGGER.info("Starte Experten-Voting...");
    sendEmbedMessage(
        jda.getTextChannelById(555106694439501827L),
        "Voting",
        "Ein neues Expertenvoting hat begonnen. Stimme ab jetzt mit !vote @USER für deinen Kandidaten ab!");
    voteMessageSent = true;
    votingStatus = false;
    try {
      Files.write(
          Paths.get(new File("voting-data.txt").toURI()),
          Collections.singletonList(String.valueOf(votingStatus)),
          Charset.forName("UTF-8"));
    } catch (IOException exception) {
      LOGGER.error(exception, exception);
    }
  }
}
