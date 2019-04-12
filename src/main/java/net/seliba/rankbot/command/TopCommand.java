package net.seliba.rankbot.command;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.util.List;
import net.dv8tion.jda.core.entities.User;
import net.seliba.rankbot.files.LevelDao;

/**
 * @author Johnny_JayJay (https://www.github.com/JohnnyJayJay)
 */
public class TopCommand extends Command {

  private final LevelDao levelDao;

  public TopCommand(LevelDao levelDao) {
    this.levelDao = levelDao;
    this.name = "top";
    this.cooldown = 120;
  }

  @Override
  protected void execute(CommandEvent event) {
    List<User> sortedUsers = levelDao.getSortedUsers(event.getJDA());
    List<User> topUsers = sortedUsers.subList(sortedUsers.size() - 1, sortedUsers.size() - 11);
    event.reply(Messages.createMessage(event.getAuthor(), "Top", buildTopView(topUsers)));
  }

  private String buildTopView(List<User> topUsers) {
    StringBuilder builder = new StringBuilder();
    for (User user : topUsers) {
      long level = levelDao.getLevel(user);
      builder
          .append(user.getAsMention())
          .append(" | Level ")
          .append(level)
          .append(" (")
          .append(levelDao.getXp(user))
          .append(" / ")
          .append(levelDao.getXpToLevelup(level))
          .append(")");
    }
    return builder.toString();
  }

  @Override
  public String getCooldownError(CommandEvent event, int remaining) {
    return "Dieser Command darf nur alle 2 Minuten ausgeführt werden! Bitte warte noch `"
        + remaining
        + "` Sekunden.";
  }
}
