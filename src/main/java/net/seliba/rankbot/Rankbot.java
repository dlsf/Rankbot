package net.seliba.rankbot;

import java.io.File;
import java.io.IOException;

import java.util.Scanner;
import javax.security.auth.login.LoginException;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.seliba.rankbot.files.LevelDao;
import net.seliba.rankbot.files.TomlData;
import net.seliba.rankbot.files.VotesDao;
import net.seliba.rankbot.listener.ChatListener;
import net.seliba.rankbot.runnables.NewVideoRunnable;
import net.seliba.rankbot.runnables.VoteUpdateRunnable;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class Rankbot {

  private static final Logger LOGGER = LogManager.getLogger(Rankbot.class.getName());
  private static JDA jda;
  private static TomlData levelData, votingResults, votingList;
  private static LevelDao levelDao;
  private static VotesDao votesDao;
  private static String latestVideoId;
  private static File videoFile;
  private static File expertVotingFile;

  public static void main(String[] args) throws IOException {
    LOGGER.info("Der Bot wurde gestartet");
    try {
      LOGGER.info("Login wird ausgefuehrt...");
      jda = new JDABuilder(AccountType.BOT)
          .setToken(getToken())
          .build();
    } catch (LoginException ex) {
      LOGGER.error(ex, ex);
      System.exit(-1);
    } finally {
      LOGGER.info("Login erfolgreich");
    }
    levelData = new TomlData("levels");
    votingList = new TomlData("voted");
    votingResults = new TomlData("results");

    levelDao = new LevelDao(levelData);
    votesDao = new VotesDao(jda, votingResults, votingList);

    LOGGER.info("Registriere Events...");
    jda.addEventListener(new ChatListener(jda, levelDao, votesDao));

    videoFile = new File("latest.txt");
    expertVotingFile = new File("voting-data.txt");
    latestVideoId = new Scanner(videoFile).nextLine();

    Thread newVideoFetcherThread = new Thread(new NewVideoRunnable(jda, videoFile, latestVideoId));
    Thread voteUpdateScheduler = new Thread(new VoteUpdateRunnable(jda, votesDao, getExpertVotingStatus()));

    newVideoFetcherThread.start();
    voteUpdateScheduler.start();
  }

  private static String getToken() {
    File tokenFile = new File("token.txt");
    String token = null;

    try {
      Scanner scanner = new Scanner(tokenFile);
      token = scanner.nextLine();
    } catch (IOException exception) {
      LOGGER.error(exception, exception);
      System.exit(-1);
    }
    return token;
  }

  private static boolean getExpertVotingStatus() {
    Boolean votingStatus = false;
    try {
      Scanner scanner = new Scanner(expertVotingFile);
      votingStatus = Boolean.valueOf(scanner.nextLine());
    } catch (IOException exception) {
      LOGGER.error(exception, exception);
      System.exit(-1);
    }
    return votingStatus;
  }

}
