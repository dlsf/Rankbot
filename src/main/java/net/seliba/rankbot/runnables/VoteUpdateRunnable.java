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
import java.util.Random;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.seliba.rankbot.Rankbot;
import net.seliba.rankbot.files.VotesDao;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class VoteUpdateRunnable implements Runnable {

  private static final Logger LOGGER = LogManager.getLogger(Rankbot.class.getName());

  private static boolean voteMessageSent = false;
  private static JDA jda;
  private static VotesDao votesDao;
  private static boolean votingStatus;

  public VoteUpdateRunnable(JDA jda, VotesDao votesDao, boolean votingStatus) {
    this.jda = jda;
    this.votesDao = votesDao;
    this.votingStatus = votingStatus;
  }

  @Override
  public void run() {
    LOGGER.info("Thread #2 gestartet!");
    try {
      Thread.currentThread().sleep(5000L);
      while (true) {
        LOGGER.info("Ueberpruefe auf Voting-Statusaenderung...");
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        if(day == 7 && !voteMessageSent) {
          startVoting();
        } else if(day == 2 && voteMessageSent) {
          stopVoting();
        }
        Thread.currentThread().sleep(300000L);
      }
    } catch (InterruptedException ex) {
      LOGGER.error(ex, ex);
      Thread.currentThread().interrupt();
      throw new RuntimeException(ex);
    }
  }

  private void startVoting() {
    if(votingStatus = false) {
      votingStatus = true;
      try {
        Files.write(Paths.get(new File("voting-data.txt").toURI()), Collections.singletonList(String.valueOf(votingStatus)), Charset.forName("UTF-8"));
      } catch (IOException exception) {
        LOGGER.error(exception, exception);
      }
      return;
    }
    LOGGER.info("Starte Experten-Voting...");
    sendEmbedMessage(jda.getTextChannelById(555106694439501827L), "Voting", "Ein neues Expertenvoting hat begonnen. Stimme ab jetzt mit !vote @USER für deinen Kandidaten ab!");
    voteMessageSent = true;
    votingStatus = false;
    try {
      Files.write(Paths.get(new File("voting-data.txt").toURI()), Collections.singletonList(String.valueOf(votingStatus)), Charset.forName("UTF-8"));
    } catch (IOException exception) {
      LOGGER.error(exception, exception);
    }
  }

  //TODO: Remove public static -> change usage in ChatListener
  public static void stopVoting() {
    LOGGER.info("Stoppe Experten-Voting...");
    voteMessageSent = false;
    List<User> possibleWinners = votesDao.getWinners();
    User winner = possibleWinners.get(new Random().nextInt(possibleWinners.size()));
    LOGGER.info("Gewinner des Votings: " + winner.getName());
    sendEmbedMessage(jda.getTextChannelById(555106694439501827L), "Voting", "Das Expertenvoting ist beendet. Neuer Experte: " + winner.getAsMention());
    jda.getGuildById(486161636105650176L).getController().addSingleRoleToMember(getMember(winner), jda.getRoleById(551802664879390720L)).queue();
    votesDao.deleteVotes();
  }

  private static void sendEmbedMessage(TextChannel textChannel, String title, String message) {
    textChannel.sendMessage(new EmbedBuilder()
        .setTitle(title)
        .setDescription(message)
        .setColor(Color.GREEN)
        .build())
        .queue();
  }

  private static Member getMember(User user) {
    for(Guild guild : jda.getGuilds()) {
      if(guild.getIdLong() == 486161636105650176L) {
        for(Member member : guild.getMembers()) {
          if(member.getUser().getIdLong() == user.getIdLong()) {
            return member;
          }
        }
      }
    }
    return null;
  }

}
