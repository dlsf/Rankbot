package net.seliba.rankbot.command;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.User;

import java.awt.*;

/**
 * @author Johnny_JayJay (https://www.github.com/JohnnyJayJay)
 */
public final class Messages {

    private Messages() {}

    public static MessageEmbed createMessage(User author, String title, String description) {
        return new EmbedBuilder()
                .setAuthor(author.getName(), null, author.getAvatarUrl())
                .setTitle(title)
                .setDescription(description)
                .setColor(Color.GREEN)
                .build();
    }

}
