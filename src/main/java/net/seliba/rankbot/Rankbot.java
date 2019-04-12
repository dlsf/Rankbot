package net.seliba.rankbot;

import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import javax.security.auth.login.LoginException;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.seliba.rankbot.command.MessageCommand;
import net.seliba.rankbot.command.RankCommand;
import net.seliba.rankbot.command.TopCommand;
import net.seliba.rankbot.command.VoteCommandModule;
import net.seliba.rankbot.files.LevelDao;
import net.seliba.rankbot.files.TomlData;
import net.seliba.rankbot.files.VotesDao;
import net.seliba.rankbot.runnables.NewVideoRunnable;
import net.seliba.rankbot.runnables.VoteUpdateRunnable;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class Rankbot {

  private static final Logger LOGGER = LogManager.getLogger(Rankbot.class.getName());
  private static JDA jda;
  private static File expertVotingFile;

  public static void main(String[] args) throws IOException, LoginException {
    LOGGER.info("Der Bot wurde gestartet");
    TomlData levelData = new TomlData("levels");
    TomlData votingList = new TomlData("voted");
    TomlData votingResults = new TomlData("results");

    LevelDao levelDao = new LevelDao(levelData);
    VotesDao votesDao = new VotesDao(jda, votingResults, votingList);
    LOGGER.info("Login wird ausgefuehrt...");
    CommandClient client =
        new CommandClientBuilder()
            .addCommands(new MessageCommand(), new RankCommand(levelDao), new TopCommand(levelDao))
            .addAnnotatedModule(new VoteCommandModule(votesDao))
            .setPrefix("!")
            .useHelpBuilder(false)
            .setOwnerId("450632370354126858")
            .build();
    jda = new JDABuilder(AccountType.BOT).addEventListener(client).setToken(getToken()).build();

    LOGGER.info("Login erfolgreich");

    File videoFile = new File("latest.txt");
    expertVotingFile = new File("voting-data.txt");
    String latestVideoId = new Scanner(videoFile).nextLine();

    Thread newVideoFetcherThread = new Thread(new NewVideoRunnable(jda, videoFile, latestVideoId));
    Thread voteUpdateScheduler =
        new Thread(new VoteUpdateRunnable(jda, votesDao, getExpertVotingStatus()));

    newVideoFetcherThread.start();
    voteUpdateScheduler.start();
  }

  private static String getToken() {
    Path tokenFile = Paths.get("token.txt");
    try {
      return Files.lines(tokenFile).findFirst().orElseThrow(IOException::new);
    } catch (IOException exception) {
      LOGGER.error(exception, exception);
      System.exit(-1);
    }
    return null;
  }

  private static boolean getExpertVotingStatus() {
    try {
      return Files.lines(expertVotingFile.toPath())
          .map(Boolean::valueOf)
          .findFirst()
          .orElseThrow(IOException::new);
    } catch (IOException e) {
      LOGGER.error("Error:", e);
      System.exit(-1);
    }
    return false;
  }
}
