package net.seliba.rankbot.command;

import java.awt.Color;
import java.util.concurrent.TimeUnit;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.User;

/**
 * @author Johnny_JayJay (https://www.github.com/JohnnyJayJay)
 */
final class Messages {

  private Messages() {}

  public static MessageEmbed createMessage(User author, String title, String description) {
    return new EmbedBuilder()
        .setAuthor(author.getName(), null, author.getAvatarUrl())
        .setTitle(title)
        .setDescription(description)
        .setColor(Color.GREEN)
        .build();
  }

  public static void deleteShortly(Message message) {
    message.delete().queueAfter(15, TimeUnit.SECONDS);
  }
}
