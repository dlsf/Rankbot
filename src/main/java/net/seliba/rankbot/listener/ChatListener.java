package net.seliba.rankbot.listener;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.seliba.rankbot.Rankbot;
import net.seliba.rankbot.files.LevelDao;
import net.seliba.rankbot.files.VotesDao;
import net.seliba.rankbot.runnables.VoteUpdateRunnable;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class ChatListener extends ListenerAdapter {

  private static final Logger LOGGER = LogManager.getLogger(Rankbot.class.getName());
  private static List<User> sendMessageUsers = new ArrayList<>();
  private static List<Long> disabledChannels = Arrays
      .asList(486921595047247872L, 486910521329844227L);
  private JDA jda;
  private LevelDao levelDao;
  private VotesDao votesDao;
  private boolean sentTop = false;

  public ChatListener(JDA jda, LevelDao levelDao, VotesDao votesDao) {
    this.jda = jda;
    this.levelDao = levelDao;
    this.votesDao = votesDao;
  }

  private static void removeUser(User user) {
    sendMessageUsers.remove(user);
  }

  @Override
  public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
    User user = event.getMember().getUser();
    if(user.equals(jda.getSelfUser())){
      return;
    }
    String message = event.getMessage().getContentRaw();
    if (message.equalsIgnoreCase("!rank")) {
      LOGGER.info("Sende Rank-Nachricht an " + user.getName());
      sendLevelMessage(user, event.getChannel());
      event.getMessage().delete().queue();
      return;
    }
    if(message.split(" ")[0].equalsIgnoreCase("!rank") && message.split(" ").length == 2 && event.getMessage().getMentionedMembers().size() == 1) {
      LOGGER.info("Sende Rank-Nachricht von " + event.getMessage().getMentionedMembers().get(0).getEffectiveName() + " an " + user.getName());
      sendLevelMessage(event.getMessage().getMentionedMembers().get(0).getUser(), event.getChannel());
      return;
    }
    if(message.equalsIgnoreCase("!top")) {
      if(sentTop) {
        sendTempMessage(user, event.getChannel(), "Top", "Dieser Command darf nur aller 2 Minuten ausgeführt werden!");
        return;
      }
      LOGGER.info("Sende Top-Nachricht an " + user.getName());
      sentTop = true;
      LinkedList<User> users = levelDao.getSortedUsers(jda);
      StringBuilder builder = new StringBuilder();
      for(int i = users.size() - 1; i > users.size() - 11; i--) {
        User currentUser = users.get(i);
        builder.append(currentUser.getAsMention() +
            " | Level " +
            levelDao.getLevel(currentUser) +
            " (" + levelDao.getXp(currentUser) +
            " / " +
            levelDao.getXpToLevelup(levelDao.getLevel(currentUser)) +
            ")" +
            "\n");
      }
      sendMessage(user, event.getChannel(), "Top", builder.toString());
      new Thread(() -> {
        try {
          Thread.currentThread().sleep(120000L);
        } catch (InterruptedException ex) {
          LOGGER.error(ex, ex);
          Thread.currentThread().interrupt();
          throw new RuntimeException(ex);
        }
        sentTop = false;
      }).start();
      return;
    }
    if(message.split(" ")[0].equalsIgnoreCase("!vote")) {
      if(message.split(" ").length != 2 || event.getMessage().getMentionedMembers().size() != 1) {
        sendTempMessage(event.getAuthor(), event.getChannel(), "Vote", "Bitte verwende !vote @User");
        event.getMessage().delete().queue();
        return;
      }
      if(event.getChannel().getIdLong() != 555106694439501827L) {
        return;
      }
      Calendar calendar = Calendar.getInstance();
      int day = calendar.get(Calendar.DAY_OF_WEEK);
      if(day != 1 && day != 7) {
        sendTempMessage(user, event.getChannel(), "Vote", "Du darfst nur am Wochenende voten!");
        return;
      }
      if(event.getMessage().getMentionedMembers().get(0).getUser().getId().equals(event.getAuthor().getId())) {
        event.getMessage().delete().queue();
        sendTempMessage(user, event.getChannel(), "Vote", "Du darfst nicht für dich selber voten!");
        return;
      }
      if(votesDao.hasVoted(event.getAuthor())) {
        event.getMessage().delete().queue();
        sendTempMessage(user, event.getChannel(), "Vote", "Du hast bereits gevotet!");
        return;
      }
      if(event.getMessage().getMentionedMembers().get(0).getRoles().contains(jda.getRoleById(486161927765098496L)) || event.getMessage().getMentionedMembers().get(0).getRoles().contains(jda.getRoleById(551802664879390720L))) {
        event.getMessage().delete().queue();
        sendTempMessage(user, event.getChannel(), "Vote", "Dieser Nutzer ist bereits ein Experte!");
        return;
      }
      LOGGER.info(user.getName() + " hat für " + event.getMessage().getMentionedMembers().get(0).getEffectiveName() + " gevotet!");
      votesDao.vote(user, event.getMessage().getMentionedMembers().get(0).getUser());
      event.getMessage().delete().queue();
      sendTempMessage(user, event.getChannel(), "Vote", "Du hast erfolgreich für " + (event.getMessage().getMentionedMembers().get(0).getNickname() != null ? event.getMessage().getMentionedMembers().get(0).getNickname() : event.getMessage().getMentionedMembers().get(0).getEffectiveName()) + " abgestimmt!");
      return;
    }
    if(message.equalsIgnoreCase("!winner") && event.getAuthor().getIdLong() == 450632370354126858L) {
      VoteUpdateRunnable.stopVoting();
      return;
    }
    if(message.startsWith("!votes") && event.getMessage().getMentionedMembers().size() == 1) {
      Member member = event.getMember();
      if(member.getRoles().contains(jda.getRoleById(486161927765098496L)) || member.getIdLong() == 450632370354126858L) {
        sendTempMessage(user, event.getChannel(), "Votes", "Der User " + event.getMessage().getMentionedMembers().get(0).getAsMention() + " hat " + votesDao.getVotes(event.getMessage().getMentionedMembers().get(0).getUser()) + " Votes!");
      } else if(member.equals(event.getMessage().getMentionedMembers().get(0))) {
        sendTempMessage(user, event.getChannel(), "Votes", "Du hast " + votesDao.getVotes(event.getMessage().getMentionedMembers().get(0).getUser()) + " Votes!");
      } else {
        sendTempMessage(user, event.getChannel(), "Votes", "Du darfst nur deine eigenen Votes einsehen!");
      }
      return;
    }
    if(message.startsWith("!message") && user.getIdLong() == 450632370354126858L) {
      String[] splittedText = message.split(" ");
      String text = "";
      for (int i = 1; i < splittedText.length; i++) {
        text = text + splittedText[i] + " ";
      }
      event.getChannel().sendMessage(new MessageBuilder()
          .setContent(text)
          .build()).queue();
    }
    if (sendMessageUsers.contains(user)) {
      return;
    }
    if (disabledChannels.contains(event.getChannel().getIdLong())) {
      return;
    }
    new Thread(() -> {
      try {
        Thread.currentThread().sleep(60000L);
      } catch (InterruptedException ex) {
        LOGGER.error(ex, ex);
        Thread.currentThread().interrupt();
        throw new RuntimeException(ex);
      }
      ChatListener.removeUser(user);
    }).start();
    sendMessageUsers.add(user);
    levelDao.giveXP(event.getMember(), event.getGuild());
  }

  private void sendLevelMessage(User user, TextChannel textChannel) {
    Long level = levelDao.getLevel(user);
    Long xp = levelDao.getXp(user);
    long xpToLevelup = levelDao.getXpToLevelup(level);
    StringBuilder stringBuilder = new StringBuilder();
    double barProgress = (((double) xp) / xpToLevelup) * 20;
    for(int i = 0; i < barProgress; i++) {
      stringBuilder.append("█");
    }
    for(int i = 0; i < 20 - barProgress; i++) {
      stringBuilder.append("▒");
    }
    sendMessage(user, textChannel, "Rank",
        "Level: " + level +
            "\nXP: " + xp +
            " / " + xpToLevelup +
            "\n" + stringBuilder.toString()
    );
  }

  private void sendMessage(User author, TextChannel textChannel, String title, String message) {
    textChannel.sendMessage(new EmbedBuilder()
        .setAuthor(author.getName(), author.getAvatarUrl(), author.getAvatarUrl())
        .setTitle(title)
        .setDescription(message)
        .setColor(Color.GREEN)
        .build())
        .queue();
  }

  private void sendTempMessage(User author, TextChannel textChannel, String title, String message) {
    textChannel.sendMessage(new EmbedBuilder()
        .setAuthor(author.getName(), author.getAvatarUrl(), author.getAvatarUrl())
        .setTitle(title)
        .setDescription(message)
        .setColor(Color.GREEN)
        .build())
        .queue(msg -> msg.delete().queueAfter(15, TimeUnit.SECONDS) );
  }

}
